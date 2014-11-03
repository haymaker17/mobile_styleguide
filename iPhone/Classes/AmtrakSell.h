//
//  AmtrakSell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/10/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "AmtrakSellData.h"

@interface AmtrakSell : MsgResponder {
	NSXMLParser				*dataParser;
	NSString				*currentElement, *path;
	
	NSString				*isInElement;
	NSMutableDictionary		*items;
	NSMutableArray			*keys;
	AmtrakSellData			*obj;
	
}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary		*items;
@property (nonatomic, strong) AmtrakSellData				*obj;
@property (nonatomic, strong) NSMutableArray			*keys;

//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;
@end
