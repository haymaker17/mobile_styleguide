//
//  CardsGetPersonalAndTransactions.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "PersonalCardData.h"

@interface CardsGetPersonalAndTransactions : MsgResponder 
{
	NSXMLParser				*dataParser;
	NSString				*currentElement, *path;
	NSString				*isInElement;
	NSMutableDictionary		*cards;
	NSMutableArray			*keys;
	PersonalCardData		*pCard;
}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary		*cards;
@property (nonatomic, strong) PersonalCardData			*pCard;
@property (nonatomic, strong) NSMutableArray			*keys;

//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

@end
