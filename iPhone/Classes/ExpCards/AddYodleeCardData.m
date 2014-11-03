//
//  AddYodleeCardData.m
//  ConcurMobile
//
//  Created by yiwen on 11/15/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AddYodleeCardData.h"
#import "FormFieldData.h"

@implementation AddYodleeCardData

-(NSString *)getMsgIdKey
{
	return ADD_YODLEE_CARD_DATA;
}



-(NSString *)makeXMLBody
{
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@""];
	[bodyXML appendString:@"<YodleeCardLoginInfo>"];
	[bodyXML appendString:[NSString stringWithFormat:@"<ContentServiceId>%@</ContentServiceId><Fields>", self.contentServiceId]];
    
    int nFields = [self.fields count];
	for (int ix = 0; ix < nFields; ix ++)
	{
		FormFieldData* ff = (self.fields)[ix];
		[bodyXML appendString:@"<FormField  xmlns=\"http://schemas.datacontract.org/2004/07/Snowbird\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">"];
	
		if ([ff.iD length])
			[bodyXML appendString:[NSString stringWithFormat:@"<Id>%@</Id>", ff.iD]];
		if ([ff.fieldValue length])
			[bodyXML appendString:[NSString stringWithFormat:@"<Value>%@</Value>", [NSString stringByEncodingXmlEntities:ff.fieldValue]]];

        [bodyXML appendString:@"</FormField>"];

    }
	[bodyXML appendString:@"</Fields></YodleeCardLoginInfo>"];
    return bodyXML;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    
    self.contentServiceId = parameterBag[@"ContentServiceId"];
    self.fields = parameterBag[@"Fields"];

	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/AddYodleeCard", 
                 [ExSystem sharedInstance].entitySettings.uri];
    
    self.path = [path stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"text/xml"];
    [msg setBody:[self makeXMLBody]];
	[msg setMethod:@"POST"];
	
	return msg;
}

#pragma mark -
#pragma mark Parsing
- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.status = [[ActionStatus alloc] init];
	}
	else if ([elementName isEqualToString:@"PersonalCard"])
	{
		self.card = [[PersonalCard alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
	if ([currentElement isEqualToString:@"Status"])
	{
		self.status.status = buildString;
	}	
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.status.errMsg = buildString;
	}
	if ([currentElement isEqualToString:@"AccountNumberLastFour"])
	{
		[self.card setAccountNumberLastFour:buildString];
	}
	else if ([currentElement isEqualToString:@"CardName"])
	{
		[self.card setCardName:buildString];
	}
	else if ([currentElement isEqualToString:@"CrnCode"])
	{
		[self.card setCrnCode:buildString];
	}
	else if ([currentElement isEqualToString:@"PcaKey"])
	{
		[self.card setPcaKey:buildString];
	}
}


@end
