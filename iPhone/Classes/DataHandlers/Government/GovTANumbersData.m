//
//  GovTANumbersData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovTANumbersData.h"
#import "DateTimeFormatter.h"

@implementation GovTANumbersData

@synthesize taNumbers, currentTANum;

-(NSString *)getMsgIdKey
{
	return GOV_TA_NUMBERS;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/GetTMAuthNums",[ExSystem sharedInstance].entitySettings.uri];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"TANumberListRow"])
	{
		self.currentTANum = [[GovTANumber alloc] init];
	}
    else if ([elementName isEqualToString:@"TANumberList"])
	{
        self.taNumbers = [[NSMutableArray alloc] init];
	}

}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    if ([elementName isEqualToString:@"TANumberListRow"])
	{
        [self.taNumbers addObject:self.currentTANum];
        self.currentTANum = nil;
	}
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if ([currentElement isEqualToString:@"TANumber"])
    {
		[self.currentTANum setTANumber:buildString];
    }
    else if ([currentElement isEqualToString:@"TAType"])
    {
		[self.currentTANum setTAType:buildString];
    }
    else if ([currentElement isEqualToString:@"PurposeCode"])
    {
		[self.currentTANum setPurposeCode:buildString];
    }
    else if ([currentElement isEqualToString:@"TripBeginDate"])
	{
		[self.currentTANum setTripBeginDate:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"TripEndDate"])
	{
		[self.currentTANum setTripEndDate:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"TALabel"])
    {
		[self.currentTANum setTALabel:buildString];
    }
    else if ([currentElement isEqualToString:@"TADocType"])
    {
		[self.currentTANum setDocType:buildString];
    }
    else if ([currentElement isEqualToString:@"TADocName"])
    {
		[self.currentTANum setDocName:buildString];
    }
    else if ([currentElement isEqualToString:@"PdmLocation"])
    {
		[self.currentTANum setPdmLocation:buildString];
    }
}


@end
