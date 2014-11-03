//
//  UploadQueueHelper.m
//  ConcurMobile
//
//  Created by charlottef on 11/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "UploadQueueHelper.h"
#import "UploadQueue.h"

@implementation UploadQueueHelper

@synthesize queue;

-(id) initWithQueue:(UploadQueue*)uploadQueue
{
    self = [super init];
	if (self)
	{
        self.queue = uploadQueue;
	}
	return self;
}

-(void) updateAfterUpload
{
    [self updateQuickExpenses];
}

-(void) updateQuickExpenses
{
    // MOB-12986 :Do not make a server call after upload is complete
    // Commenting out only this line so depending delegates are not broken
    // [[ExSystem sharedInstance].msgControl createMsg:ME_LIST_DATA CacheOnly:@"NO" ParameterBag:nil SkipCache:YES RespondTo:self];
}

-(void) didUpdateQuickExpenses
{
    [self updateReceiptStoreList];
}

-(void) updateReceiptStoreList
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"N", @"FILTER_MOBILE_EXPENSE", nil];
	[[ExSystem sharedInstance].msgControl createMsg:RECEIPT_STORE_RECEIPTS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES	RespondTo:self];
}

-(void) didUpdateReceiptStoreList
{
    [self.queue didUpdateAfterUpload];
}

#pragma mark - Message handling methods

-(void) didProcessMessage:(Msg *)msg
{
    // MOB-12986 : This would be never needed 
	if ([msg.idKey isEqualToString:ME_LIST_DATA()])
	{
        [self didUpdateQuickExpenses];
    }
    else
        if ([msg.idKey isEqualToString:RECEIPT_STORE_RECEIPTS])
    {
        [self didUpdateReceiptStoreList];
    }
}

@end
