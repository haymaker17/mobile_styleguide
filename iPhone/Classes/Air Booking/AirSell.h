//
//  AirSell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/15/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "AmtrakSellData.h"

@interface AirSell : MsgResponder {
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
