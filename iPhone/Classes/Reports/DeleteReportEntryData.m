//
//  DeleteReportEntryData.m
//  ConcurMobile
//
//  Created by yiwen on 6/11/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DeleteReportEntryData.h"
#import "DataConstants.h"

@implementation DeleteReportEntryData
@synthesize rpeKeys, curStatus;

-(NSString *)getMsgIdKey
{
	return DELETE_REPORT_ENTRY_DATA;
}

- (NSString*) getReportElementName
{
	return @"Report";
}


-(NSString *)makeXMLBody
{//knows how to make a post
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<a:ArrayOfstring xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">"];
	if (rpeKeys != nil)
	{
		for (int ix = 0; ix < [rpeKeys count]; ix++)
		{
			[bodyXML appendString:@"<a:string>"];
			[bodyXML appendString:(NSString*)rpeKeys[ix]];
			[bodyXML appendString:@"</a:string>"];
		}
	}
	[bodyXML appendString:@"</a:ArrayOfstring>"];
	return bodyXML;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.rptKey = parameterBag[@"RPT_KEY"];
	self.rpeKeys = parameterBag[@"RPE_KEYS"];
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/DeleteReportEntriesV2", 
				[ExSystem sharedInstance].entitySettings.uri ];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	
	return msg;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.curStatus = nil;
		curStatus = [[ActionStatus alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
	if ([currentElement isEqualToString:@"Status"])
	{
		self.curStatus.status = string;
	}
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.curStatus.errMsg = buildString;
	}	
}


@end
