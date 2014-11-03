//
//  ReceiptCache.m
//  ConcurMobile
//
//  Created by charlottef on 11/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "ReceiptCache.h"
#import "ReceiptStoreReceipt.h"

@interface ReceiptCache (private)
-(NSString*) thumbNailIdFromFullSizeReceiptId:(NSString*)dataId;
@end

@implementation ReceiptCache

static ReceiptCache *sharedInstance;

+(ReceiptCache*) sharedInstance
{
	if (sharedInstance != nil)
	{
		return sharedInstance;
	}
	else
	{
		@synchronized (self)
		{
			if (sharedInstance == nil)
			{
				sharedInstance = [[ReceiptCache alloc] init];
			}
		}
		return sharedInstance;
	}
}

#pragma mark - Lookup methods

-(NSData*) getFullSizeReceiptForId:(NSString*)dataId dataType:(NSString**)dataType;
{
    return [self getCachedDataForId:dataId dataType:dataType];
}

-(NSData*) getThumbNailReceiptForId:(NSString*)dataId dataType:(NSString**)dataType
{
    NSString *thumbNailDataId = [self thumbNailIdFromFullSizeReceiptId:dataId];
    return [self getCachedDataForId:thumbNailDataId dataType:dataType];
}

#pragma mark - Update Methods

-(BOOL) cacheFullSizeReceiptData:(NSData*)data dataType:(NSString*)dataType receiptId:(NSString*)receiptId
{
    return [self cacheData:data dataType:dataType dataId:receiptId];
}

-(BOOL) cacheThumbNailReceiptData:(NSData*)data dataType:(NSString*)dataType receiptId:(NSString*)receiptId
{
    NSString *thumbNailReceiptId = [self thumbNailIdFromFullSizeReceiptId:receiptId];
    return [self cacheData:data dataType:dataType dataId:thumbNailReceiptId];
}

-(BOOL) cacheFullSizeReceiptFromFilePath:(NSString*)srcPath dataType:(NSString*)dataType receiptId:(NSString*)receiptId
{
    return [super cacheDataFromFilePath:srcPath dataType:dataType dataId:receiptId];
}

#pragma mark - Purge method
-(void) purgeUnusedReceiptImagesFromCache:(NSArray*)receiptObjects // Array of ReceiptStoreReceipt objects
{
    // The commented out implementation only checked which receipts are used by the receipt store.  So it purged receipts used by quick expenses.  Need to find a more comprehensive way of determining whether receipts are being used before purging them.  For now, skip the purge.
    return;
    
    /*
    if (receiptObjects != nil && receiptObjects.count > 0)
    {
        NSMutableDictionary *usedReceipts = [[NSMutableDictionary alloc] init];

        for (ReceiptStoreReceipt *receipt in receiptObjects)
        {
            NSString *receiptImageId = receipt.receiptImageId;
            [usedReceipts setObject:receiptImageId forKey:receiptImageId];

            NSString *receiptThumbNailId = [self thumbNailIdFromFullSizeReceiptId:receiptImageId];
            [usedReceipts setObject:receiptThumbNailId forKey:receiptThumbNailId];
        }
        
        [self purgeUnusedDataFromCache:usedReceipts];
    }
    */
}

-(BOOL) deleteReceiptsMatchingId:(NSString*)dataId
{
    BOOL result = [self deleteFilesMatchingId:dataId]; // Delete full size receipt
    [self deleteFilesMatchingId:[self thumbNailIdFromFullSizeReceiptId:dataId]]; // Delete receipt thumbnail
    return result;
}

#pragma mark - DataCache Overrides

-(NSString*) folderForCache
{
    NSString* userFolderName = [ExSystem sharedInstance].userName;
    NSString *receiptFolderName = @"Receipts";
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];
    NSString* folderPath = [documentsDirectory stringByAppendingPathComponent:receiptFolderName];
    folderPath = [folderPath stringByAppendingPathComponent:userFolderName];
    
	if (![fileManager fileExistsAtPath:folderPath])
	{
		if (![fileManager createDirectoryAtPath:folderPath withIntermediateDirectories:TRUE attributes:nil error:nil])
		{
			return nil;
		}
	}
    
    return folderPath;
}

#pragma mark - Helper Methods

-(NSString*) thumbNailIdFromFullSizeReceiptId:(NSString*)dataId
{
    NSString *thumbNailId = [NSString stringWithFormat:@"%@%@", dataId, @"_TN"];
    return thumbNailId;
}

@end
