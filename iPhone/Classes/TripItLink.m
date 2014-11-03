//
//  TripItLink.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 5/17/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TripItLink.h"

@implementation TripItLink

@synthesize linkStatus;

-(NSString *)getMsgIdKey
{
	return TRIPIT_LINK;
}

#pragma mark - Lifecycle


#pragma mark - Message Creation

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = [NSString stringWithFormat:@"%@/mobile/tripit/LinkToTripItV2",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
    [msg setBody:[self makeXMLBody:parameterBag]];
    return msg;
}

-(NSString *)makeXMLBody:(NSMutableDictionary*)pBag
{
    NSString *requestTokenKey = pBag[@"REQUEST_TOKEN_KEY"];
    NSString *requestTokenSecret = pBag[@"REQUEST_TOKEN_SECRET"];
    
    NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<Token>"];
	[bodyXML appendString:@"<Key>%@</Key>"];
	[bodyXML appendString:@"<Secret>%@</Secret>"];
	[bodyXML appendString:@"</Token>"];
	
	NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML,
								  requestTokenKey,
								  requestTokenSecret
								  ];
	
	
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"TripItLink::makeXMLBody: body is %@", formattedBodyXml] Level:MC_LOG_DEBU];
    
	return formattedBodyXml;
}

#pragma mark - Parsing

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.linkStatus = [[ActionStatus alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
	if ([currentElement isEqualToString:@"Status"])
	{
		self.linkStatus.status = buildString;
	}	
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.linkStatus.errMsg = buildString;
	}
}
@end
