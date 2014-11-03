//
//  DeleteReceipt.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 3/2/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "ReceiptStoreReceipt.h"
#import "Msg.h"
#import "DataConstants.h"

@interface DeleteReceipt : MsgResponder {
	NSString				*receiptImageId;
	NSXMLParser				*dataParser;
	NSString				*status;
	NSString				*currentElement;
}

@property (nonatomic,strong) NSString					*receiptImageId; 
@property (nonatomic,strong) NSXMLParser				*dataParser;
@property (nonatomic,strong) NSString					*status;
@property (nonatomic,copy)   NSString					*currentElement;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

@end
