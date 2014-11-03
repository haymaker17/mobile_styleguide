//
//  ReceiptData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReceiptImageMetaData.h"

#define RECEIPT_STORE_IMAGE_CACHE_LIMIT 20


@interface ReceiptData : NSObject {
}

@property (strong, nonatomic) NSMutableDictionary	*imageDict;
@property (strong, nonatomic) NSMutableArray		*receipts;
@property (strong, nonatomic) NSMutableDictionary	*receiptDict;


-(id) initPlistFiles;

-(void) readPlist;
-(void) writeToPlist;

-(ReceiptImageMetaData *) getReceiptMetaDataByKey:(NSString *)key;
-(void) saveReceiptImageMetaData:(ReceiptImageMetaData *) rimd;
-(void) removeImage:(ReceiptImageMetaData *)rimd;

-(void)save:(ReceiptImageMetaData*)rimd;
-(void)deleteFromReceiptsQueue;
-(void)checkAndUpdateQueueLimit;
-(int)getIndexForRimd:(ReceiptImageMetaData*)rimd;
-(void)deleteRimd:(ReceiptImageMetaData*)rimd;
-(void)clearCache;
-(NSDictionary*) makeReceiptDictSerializable;
-(void) deserializeForReceiptDict:(NSString*)plistPath;
@end
