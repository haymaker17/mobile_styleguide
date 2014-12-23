//
//  UploadableMobileEntry.m
//  ConcurMobile
//
//  Created by charlottef on 10/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//
//  Change log
//      Updated on 4/22 by Pavan for MOB-12986

#import "UploadableMobileEntry.h"
#import "MobileEntryManager.h"
#import "UploadableItemDelegate.h"
#import "MobileExpenseSave.h"


@implementation UploadableMobileEntry

@synthesize uploadableItemDelegate = _uploadableItemDelegate;
@synthesize managedObjectContext = _managedObjectContext;
@synthesize localId, localReceiptImageId, meEntry;

#pragma mark - Initialization

-(id) initWithEntityInstanceId:(NSString*)entityInstanceId inContext:(NSManagedObjectContext*)context
{
    self = [super init];
    if (self)
    {
        self.managedObjectContext = context;
        //MOB-12986 : Get the MobileEntry associated with the instance id
        self.meEntry = [[MobileEntryManager sharedInstance] fetchByLocalId:entityInstanceId];

        if (meEntry == nil)
            return nil;
        
        self.localId = entityInstanceId;
        self.localReceiptImageId = meEntry.localReceiptImageId;
     }
	return self;
}

#pragma mark - UploadableItem Methods
-(void) uploadItemWithUUID:(NSString*)uuid isPdfReceipt:(BOOL)isPdf delegate:(id<UploadableItemDelegate>)delegate
{
    self.uploadableItemDelegate = delegate;

    // If this quick expense is still referencing a local receipt image
    if (self.localReceiptImageId != nil || self.localReceiptImageId.length > 0)
    {
        // Then log the problem, and notify the delegate that the upload failed.
        [[MCLogging getInstance] log:@"UploadableMobileEntry::uploadItemWithUUID - Did not attempt to upload mobile entry referencing local receipt" Level:MC_LOG_ERRO];
        
        if (self.uploadableItemDelegate != nil)
            [self.uploadableItemDelegate didFailToUploadItemWithUUID:uuid];
        
        return;
    }

    // Start uploading the quick expense
    //
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.meEntry, @"ENTRY", uuid, @"MSG_UUID", nil];
	
	[[ExSystem sharedInstance].msgControl createMsg:ME_SAVE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) didUploadRequiredItemWithUUID:(NSString*)uuid details:(NSDictionary*)details
{
    EntityMobileEntry *mobileEntry = [[MobileEntryManager sharedInstance] fetchByLocalId:self.localId];
    if (mobileEntry != nil)
    {
        mobileEntry.localReceiptImageId = nil;
        mobileEntry.receiptImageId = details[@"SERVER_RECEIPT_IMAGE_ID"];
        if ([mobileEntry.receiptImageId length]) {
            mobileEntry.hasReceipt = @"Y";
        }
        [UploadableMobileEntry saveChangesToContext:self.managedObjectContext];
    }
}

#pragma mark - Message handling methods
-(void) didProcessMessage:(Msg *)msg
{
	
    if ([msg.idKey isEqualToString:ME_SAVE_DATA])
	{
        MobileExpenseSave *meSaved = (MobileExpenseSave *)msg.responder;
        NSDictionary* pBag = msg.parameterBag;
        NSString *uuid = pBag[@"MSG_UUID"];
        
		if (msg.didConnectionFail || ![meSaved.returnStatus isEqualToString:@"SUCCESS"])
		{
            if (msg.didConnectionFail)
                [[MCLogging getInstance] log:@"UploadableMobileEntry::didProcessMessage: connection failed" Level:MC_LOG_DEBU];
            else
                [[MCLogging getInstance] log:@"UploadableMobileEntry::didProcessMessage: server could not save quick expense" Level:MC_LOG_DEBU];
            
            if (self.uploadableItemDelegate != nil)
                [self.uploadableItemDelegate didFailToUploadItemWithUUID:uuid];
            
		}
		else
		{
            //MOB-12986: TODO - get the MWS response and set the mekeys to entity.key so that the expenselist gets updated
            // Work around post a server call to refresh expense list.
            EntityMobileEntry *mobileEntry = (msg.parameterBag)[@"ENTRY"];
            mobileEntry.key = meSaved.meKey;
            
            // this has to be nil cause the uploadableItemDelegate will delete it.
            mobileEntry.localId = nil;
            
            [[MobileEntryManager sharedInstance] saveIt:mobileEntry];
            
            // [[ExSystem sharedInstance].msgControl createMsg:ME_LIST_DATA CacheOnly:@"NO" ParameterBag:nil SkipCache:NO RespondTo:self];
            if (self.uploadableItemDelegate != nil)
                [self.uploadableItemDelegate didUploadItemWithUUID:uuid details:nil];
        }
	}
}

#pragma mark - Dequeue method

+(void) didDequeueEntityInstanceId:(NSString*)entityInstanceId inContext:(NSManagedObjectContext*)context
{
    [[MobileEntryManager sharedInstance] deleteByLocalId:entityInstanceId];
    [UploadableMobileEntry saveChangesToContext:context];
}

#pragma mark - Save method
+(void) saveChangesToContext:(NSManagedObjectContext*)context
{
    NSError *error = nil;
    if (![context save:&error])
        NSLog(@"Whoops, couldn't save object: %@", [error localizedDescription]);
}

@end
