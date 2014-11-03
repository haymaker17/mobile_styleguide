//
//  ReceiptCache.h
//  ConcurMobile
//
//  Created by charlottef on 11/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DataCache.h"

@interface ReceiptCache : DataCache
{
    
}

+(ReceiptCache*) sharedInstance; // There doesn't need to be more than one receipt cache

#pragma mark - Lookup methods

-(NSData*) getFullSizeReceiptForId:(NSString*)dataId dataType:(NSString**)dataType;
-(NSData*) getThumbNailReceiptForId:(NSString*)dataId dataType:(NSString**)dataType;

#pragma mark - Update Methods

-(BOOL) cacheFullSizeReceiptData:(NSData*)data dataType:(NSString*)dataType receiptId:(NSString*)dataId;
-(BOOL) cacheThumbNailReceiptData:(NSData*)data dataType:(NSString*)dataType receiptId:(NSString*)dataId;

-(BOOL) cacheFullSizeReceiptFromFilePath:(NSString*)srcPath dataType:(NSString*)dataType receiptId:(NSString*)receiptId;


#pragma mark - Purge method
-(void) purgeUnusedReceiptImagesFromCache:(NSArray*)receiptObjects; // Array of ReceiptStoreReceipt objects
#pragma mark - Delete method
-(BOOL) deleteReceiptsMatchingId:(NSString*)receiptId;


@end
