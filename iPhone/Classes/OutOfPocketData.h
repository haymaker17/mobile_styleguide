//
//  OutOfPocketData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ArchivedResponder.h"
#import "Msg.h"
#import "MsgResponder.h"
#import "OOPEntry.h"
#import "PersonalCardData.h"
#import "ImageUtil.h"

@interface OutOfPocketData : MsgResponder <ArchivedResponder, NSXMLParserDelegate>
{
	NSString				*currentElement, *path;
	NSMutableString			*buildString;
	NSString				*isInElement;
    NSMutableDictionary     *obsoleteOopes; // Ooopes that no longer exist
	NSMutableDictionary		*oopes;
	NSMutableArray			*oopKeys;
	OOPEntry				*oope;
	BOOL					isInCard;
	BOOL					isInOOP;
	BOOL					isInCorpCard;
	NSMutableDictionary		*pCards;
	PersonalCardData		*pCard;

}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary       *obsoleteOopes;
@property (nonatomic, strong) NSMutableDictionary		*oopes;
@property (nonatomic, strong) OOPEntry					*oope;
@property (nonatomic, strong) NSMutableArray			*oopKeys;
@property (nonatomic, strong) NSMutableString			*buildString;
@property (nonatomic, strong) NSMutableDictionary		*pCards;
@property (nonatomic, strong) PersonalCardData			*pCard;


- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

@end
