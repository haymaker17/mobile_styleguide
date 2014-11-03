//
//  RegisterPush.h
//  ConcurMobile
//
//  Created by Paul Schmidtr on 8/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "ActionStatus.h"


@interface RegisterPush : MsgResponder {
	NSXMLParser				*dataParser;
	NSString				*currentElement, *path;
	
	NSString				*isInElement;
	NSMutableDictionary		*items;
	NSMutableArray			*keys;
    
    NSMutableString         *buildString;
    
    ActionStatus            *actionStatus;

	
}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary		*items;
@property (nonatomic, strong) NSMutableArray			*keys;
@property (nonatomic, strong) NSMutableString           *buildString;
@property (nonatomic, strong) ActionStatus              *actionStatus;

//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;
-(void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict;
-(void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName;
-(void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string;

@end
