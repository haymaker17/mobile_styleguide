//
//  UploadReceiptData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/5/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "ExpenseTypeData.h"
#import "EntityMobileEntry.h"

@interface UploadReceiptData : MsgResponder 
{
	NSXMLParser				*dataParser;
	NSString				*currentElement, *path;
	
	NSString				*isInElement, *returnStatus, *meKey;
	EntityMobileEntry		*entry;
	NSString				*receiptImageId;
	NSString				*receiptImageUrl;
}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) EntityMobileEntry			*entry;
@property (nonatomic, strong) NSString					*returnStatus;
@property (nonatomic, strong) NSString					*meKey;
@property (nonatomic, strong) NSString					*receiptImageId;
@property (nonatomic, strong) NSString					*receiptImageUrl;

//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

@end
