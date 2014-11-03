//
//  DownloadViolationReasons.m
//  ConcurMobile
//
//  Created by ernest cho on 8/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "DownloadViolationReasons.h"
#import "Msg.h"
#import "TravelViolationReasons.h"
#import "ViolationReason.h"

@implementation DownloadViolationReasons

@synthesize travelViolationReasons;
@synthesize currentReason;


-(Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    self.path = [NSString stringWithFormat:@"%@/mobile/Config/GetReasonCodes/",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:DOWNLOAD_TRAVEL_VIOLATIONREASONS State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	return msg;
}

-(void) parserDidStartDocument:(NSXMLParser *)parser
{
    [super parserDidStartDocument:parser];
    
    self.travelViolationReasons = [[TravelViolationReasons alloc] init];
}

-(void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
    self.currentElement = elementName;
    if ([elementName isEqualToString:@"ReasonCode"]) {
        self.currentReason = [[ViolationReason alloc] init];
    }
}

-(void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    if ([elementName isEqualToString:@"ReasonCode"])
	{
        (travelViolationReasons.violationReasons)[currentReason.code] = currentReason;
        self.currentReason = nil;
    }
}

-(void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if ([currentElement isEqualToString:@"Description"])
    {
        currentReason.description = buildString;
    }
    else if ([currentElement isEqualToString:@"Id"])
    {
        currentReason.code = buildString;
    }
    else if ([currentElement isEqualToString:@"ViolationType"])
    {
        currentReason.violationType = buildString;
    }
}

-(void)parserDidEndDocument:(NSXMLParser *)parser
{
    [super parserDidStartDocument:parser];
    if (travelViolationReasons != nil) {
        [TravelViolationReasons setSingleton:travelViolationReasons];
    }
}

@end
