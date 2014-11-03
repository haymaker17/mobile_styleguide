//
//  MsgResponderCommon.h
//  ConcurMobile
//
//  Created by yiwen on 11/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"

@interface MsgResponderCommon : MsgResponder 
{
	NSString				*currentElement, *path;
	NSMutableString			*buildString;
	BOOL					inElement;
}

@property (nonatomic, strong) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableString			*buildString;

-(void) flushData;
-(id)init;
-(void) respondToXMLData:(NSData *)data;
-(void)parserDidStartDocument:(NSXMLParser *)parser;
-(void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError;
-(void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict;
-(void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName;
-(void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string;
-(void)parserDidEndDocument:(NSXMLParser *)parser;

-(void)encodeWithCoder:(NSCoder *)coder;
-(id)initWithCoder:(NSCoder *)coder;

+(NSString*) getUnqualifiedName:(NSString*)qualifiedName;

@end
