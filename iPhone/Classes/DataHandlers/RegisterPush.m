//
//  RegisterPush.m
//  ConcurMobile
//
//  Created by Paul Schmidtr on 8/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "RegisterPush.h"

@implementation RegisterPush


@synthesize path, currentElement, items, keys, buildString, actionStatus;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	self.items = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	self.keys = [[NSMutableArray alloc] initWithObjects:nil];
	
	dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self flushData];
	[self parseXMLFileAtData:data];
}

-(id)init
{
	self = [super init];
    if (self)
    {
        isInElement = @"NO";
        currentElement = @"";
        [self flushData];
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return REGISTER_PUSH;
}

-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post
	/*
     <DeviceInfo>
     <IsTest>N</IsTest>  
     <PhoneId>EE1CC8E780B40672C4462E497A77D3A1B9A7CC656C2853D362C0083506B7C3BB</PhoneId>
     <Platform>iOS Phone</Platform>
     </DeviceInfo>
     */
    
    NSString *platform = @"iOS Phone";  // will also need iOS Tablet
	NSString *phoneId = parameterBag[@"PHONE_ID"];
	NSString *isTest = parameterBag[@"IS_TEST"];
	
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<DeviceInfo>"];
    if (isTest != nil)
        [bodyXML appendString:[NSString stringWithFormat:@"<IsTest>%@</IsTest>", [NSString stringByEncodingXmlEntities:isTest]]];
    
    [bodyXML appendString:[NSString stringWithFormat:@"<PhoneId>%@</PhoneId>", [NSString stringByEncodingXmlEntities:phoneId]]];
    [bodyXML appendString:[NSString stringWithFormat:@"<Platform>%@</Platform>", [NSString stringByEncodingXmlEntities:platform]]];
        


    
	[bodyXML appendString:@"</DeviceInfo>"];
	
	return bodyXML;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	self.path = [NSString stringWithFormat:@"%@/Mobile/Notification/RegisterV2",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	
	return msg;
}




-(void) flushData
{
	
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{	
    self.currentElement = elementName;
	
	isInElement = @"YES";
    self.buildString = [[NSMutableString alloc] initWithString:@""];
    
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.actionStatus = [[ActionStatus alloc] init];
	}
   
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [buildString appendString:string];
    
	if ([currentElement isEqualToString:@"Status"])
	{
		self.actionStatus.status = buildString;
	}	
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.actionStatus.errMsg = buildString;
	}
	
    
}	

@end
