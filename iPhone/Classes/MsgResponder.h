//
//  MsgResponder.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/12/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgHandler.h"
#import "DataConstants.h"



@class Msg;
@class ExSystem;

@interface MsgResponder : MsgHandler <NSXMLParserDelegate, NSCoding>
{
}

//-(void) respondToXML:(NSString *)xml;
//-(void) respondToXMLData:(NSData *)data;
- (void)parseXMLFileAtData:(NSData *)webData;

- (void)encodeWithCoder:(NSCoder *)coder;
- (id)initWithCoder:(NSCoder *)coder;

-(Msg *)newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) respondToXMLData:(NSData *)data;
-(void) respondToXMLData:(NSData *)data withMsg:(Msg*)msg;

// Return the new msg id, also populate pBag for the msg. 
-(void)fillInfoToPropagateMsg:(NSMutableDictionary*) parameterBag forMsgId:(NSString*)msgId;

// This is a generic flag used for MsgControl.
// In subclasses, if the cached data is already in core data and we don't need to parse the cached message file again, then set this falg to NO. 
-(BOOL) shouldParseCachedData;

@end
