//
//  UploadableReceipt.m
//  ConcurMobile
//
//  Created by charlottef on 11/5/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//
//  Change log
//      Updated on 4/22 by Pavan for MOB-12986


#import "UploadableReceipt.h"
#import "UploadReceiptData.h"
#import "UploadableItemDelegate.h"
#import "MobileEntryManager.h"
#import "ReceiptCache.h"

@interface UploadableReceipt ()
    @property BOOL isPdfReceipt;

@end

@implementation UploadableReceipt

@synthesize uploadableItemDelegate = _uploadableItemDelegate;
@synthesize localReceiptImageId;

#pragma mark - Initialization

-(id) initWithLocalReceiptImageId:(NSString*)localId
{
    self = [super init];
    if (self)
    {
        self.localReceiptImageId = localId;
    }
	return self;
}

#pragma mark - UploadableItem Methods

-(void) uploadItemWithUUID:(NSString*)uuid isPdfReceipt:(BOOL)isPdf delegate:(id<UploadableItemDelegate>)delegate
{
    self.uploadableItemDelegate = delegate;
    self.isPdfReceipt = isPdf;
    
    NSString *receiptImagePath = [UploadableReceipt filePathForLocalReceiptImageId:localReceiptImageId isPdfReceipt:isPdf];
    NSData *receiptImageData = [NSData dataWithContentsOfFile:receiptImagePath];
    
    NSString *url = [NSString stringWithFormat:@"%@/mobile/Expense/SaveReceipt/%@", [ExSystem sharedInstance].entitySettings.uri, @"mobile"];

    // TODO: check that server supports uuid at this endpoint
    // MOB-21462: include the case of Pdf receipt
    NSMutableDictionary *pBag;
    if (isPdf) {
         pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:url, @"URL", receiptImageData,@"PDF", uuid, @"MSG_UUID", nil];
    } else {
         pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:url, @"URL", receiptImageData,@"IMAGE", uuid, @"MSG_UUID", nil];
    }
   
    [[ExSystem sharedInstance].msgControl createMsg:UPLOAD_IMAGE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

#pragma mark - Message handling methods

-(void) didProcessMessage:(Msg *)msg
{
	if ([msg.idKey isEqualToString:UPLOAD_IMAGE_DATA])
	{
		UploadReceiptData *receiptData = (UploadReceiptData*)msg.responder;
        
        NSDictionary* pBag = msg.parameterBag;
        NSString *uuid = pBag[@"MSG_UUID"];
        
        if (msg.didConnectionFail || msg.errBody != nil || ![receiptData.returnStatus isEqualToString:@"SUCCESS"])
		{
            if (msg.didConnectionFail)
            {
                [[MCLogging getInstance] log:@"UploadableReceipt::didProcessMessage: connection failed" Level:MC_LOG_DEBU];
                NSDictionary *dict = @{@"Failure": @"Connection failed"};
                [Flurry logEvent:@"Receipts: Failure" withParameters:dict];
            }
            else
            {
                [[MCLogging getInstance] log:@"UploadableReceipt::didProcessMessage: server could not save receipt" Level:MC_LOG_DEBU];
                NSDictionary *dict = @{@"Failure": @"Server could not save receipt"};
                [Flurry logEvent:@"Receipts: Failure" withParameters:dict];
            }
            
            if (self.uploadableItemDelegate != nil)
                [self.uploadableItemDelegate didFailToUploadItemWithUUID:uuid];
		}
		else
		{
            ReceiptCache *receiptCache = [[ReceiptCache alloc] init];

            // Get the path of the uploadable receipt image
            NSString *uploadableReceiptImagePath = [UploadableReceipt filePathForLocalReceiptImageId:self.localReceiptImageId isPdfReceipt:self.isPdfReceipt];
            
            // Write the full size image to the receipt cache
            // MOB-21462: set data type as pdf when it is a pdf receipt
            NSString *dataType = self.isPdfReceipt ? PDF : JPG;
            [receiptCache cacheFullSizeReceiptFromFilePath:uploadableReceiptImagePath dataType:dataType receiptId:receiptData.receiptImageId];
            
            // Write the thumb nail image to the receipt cache
            NSData *receiptImageData = [NSData dataWithContentsOfFile:uploadableReceiptImagePath];
            UIImage *thumbNailImage = [UIImage imageWithData:receiptImageData];
            NSData *thumbNailData = UIImageJPEGRepresentation(thumbNailImage, 0.25);
            [receiptCache cacheThumbNailReceiptData:thumbNailData dataType:JPG receiptId:receiptData.receiptImageId];
            
            // Notify the delegate
            NSDictionary *details = @{@"LOCAL_RECEIPT_IMAGE_ID": localReceiptImageId, @"SERVER_RECEIPT_IMAGE_ID": receiptData.receiptImageId};

            if (self.uploadableItemDelegate != nil)
                [self.uploadableItemDelegate didUploadItemWithUUID:uuid details:details];
        }
 	}
}

#pragma mark - Dequeue method

+(void) didDequeueEntityInstanceId:(NSString*)entityInstanceId isPdfReceipt:(BOOL)isPdfReceipt
{
    // Delete the receipt image from the ReceiptsToUpload folder
    NSString *localReceiptImageFilePath = [UploadableReceipt filePathForLocalReceiptImageId:entityInstanceId isPdfReceipt:isPdfReceipt];
    NSError *error = nil;
    [[NSFileManager defaultManager] removeItemAtPath:localReceiptImageFilePath error:&error];
}


#pragma mark - Helpers
    
+(NSString*) filePathForLocalReceiptImageId:(NSString*)localReceiptImageId isPdfReceipt:(BOOL)isPdfReceipt
{
    NSString* userFolderName = [ExSystem sharedInstance].userName;
    NSString* const folderName = @"ReceiptsToUpload";
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];
    NSString* folderPath = [documentsDirectory stringByAppendingPathComponent:folderName];
    folderPath = [folderPath stringByAppendingPathComponent:userFolderName];
    
	if (![fileManager fileExistsAtPath:folderPath])
	{
		if (![fileManager createDirectoryAtPath:folderPath withIntermediateDirectories:TRUE attributes:nil error:nil])
		{
			return nil;
		}
	}
    // hanlde the pdf also
    // get the entitytype from localreceiptimageid
    // MOB-21462: if it is pdf receipt, the extension name should be .pdf
    NSString *filePath;
    if (isPdfReceipt) {
        filePath = [folderPath stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.pdf", localReceiptImageId]];
    }
    else{
        filePath = [folderPath stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.jpg", localReceiptImageId]];
    }
    return filePath;
}

+(UIImage*) imageForLocalReceiptImageId:(NSString*)localReceiptImageId
{
    NSString *receiptImagePath = [UploadableReceipt filePathForLocalReceiptImageId:localReceiptImageId isPdfReceipt:NO];
    UIImage *receiptImage = [UIImage imageWithContentsOfFile:receiptImagePath];
    return receiptImage;
}

// MOB-21462: get pdf receipt data
+(NSData*) receiptDataForLocalReceiptImageId:(NSString*)localReceiptImageId
{
    NSString *receiptImagePath = [UploadableReceipt filePathForLocalReceiptImageId:localReceiptImageId isPdfReceipt:YES];
    NSData *receiptData = [NSData dataWithContentsOfFile:receiptImagePath];
    return receiptData;
}

@end
