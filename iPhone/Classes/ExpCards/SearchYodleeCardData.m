//
//  SearchYodleeCardData.m
//  ConcurMobile
//
//  Created by yiwen on 11/7/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "SearchYodleeCardData.h"

@implementation SearchYodleeCardData

-(NSString *)getMsgIdKey
{
	return SEARCH_YODLEE_CARD_DATA;
}

-(void) flushData
{
	[super flushData];
	self.cardList = [[NSMutableArray alloc] init];
    self.card = nil;
}



-(NSString *)makeXMLBody
{
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@""];
	[bodyXML appendString:@"<SearchCriteria>"];
	[bodyXML appendString:[NSString stringWithFormat:@"<Query>%@</Query>", [NSString stringByEncodingXmlEntities:self.query]]];
	[bodyXML appendString:@"</SearchCriteria>"];
    return bodyXML;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
	self.query = parameterBag[@"QUERY"];
    if (self.query == nil)
        self.query = @"";

    self.isPopular = [@"Y" isEqualToString:parameterBag[@"IS_POPULAR"]];

    if (self.isPopular)
    {
        self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetPopularYodleeCards",[ExSystem sharedInstance].entitySettings.uri];
    }
    else
    {
        self.path = [NSString stringWithFormat:@"%@/mobile/Expense/SearchYodleeCards",[ExSystem sharedInstance].entitySettings.uri];
	}
    Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];

    if (self.isPopular)
    {
        [msg setMethod:@"GET"];
    }
    else
    {
        [msg setMethod:@"POST"];
        [msg setBody:[self makeXMLBody]];
    }
	return msg;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"YodleeCardProvider"])
	{
		self.card = [[YodleeCardProvider alloc] init];
	}
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    
	if ([elementName isEqualToString:@"YodleeCardProvider"])
	{
		[self.cardList addObject:self.card];
		self.card = nil;
	}
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
    if ([@"ContentServiceId" isEqualToString:currentElement])
        self.card.contentServiceId = buildString;
    else if ([@"Name" isEqualToString:currentElement])
        self.card.name = buildString;
	
}

@end
