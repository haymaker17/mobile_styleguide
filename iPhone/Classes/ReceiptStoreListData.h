//
//  ReceiptStoreListData.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/8/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "ReceiptStoreReceipt.h"
#import "Msg.h"
#import "DataConstants.h"

@interface ReceiptStoreListData : MsgResponder {
	ReceiptStoreReceipt		*receiptInfo;
	NSString				*status;
	NSMutableArray			*receiptObjects; // contains receiptInfo objects
	NSXMLParser				*dataParser;
	NSString				*currentElement, *path;
	NSMutableString			*buildString;
    NSMutableDictionary     *obsoleteReceipts; // Receipts that no longer exist
}

@property (nonatomic,strong) NSString					*status;
@property (nonatomic,strong) ReceiptStoreReceipt		*receiptInfo;
@property (nonatomic,strong) NSMutableArray				*receiptObjects;
@property (nonatomic,strong) NSXMLParser				*dataParser;
@property (nonatomic,copy)	 NSString					*currentElement;
@property (nonatomic,strong) NSString					*path;
@property (nonatomic,strong) NSMutableString			*buildString;
@property (nonatomic,strong) NSMutableDictionary        *obsoleteReceipts;

- (void)parseXMLFileAtData:(NSData *)webData;
- (Msg *) newMsg:(NSMutableDictionary *)parameterBag;
- (id)init;

@end
