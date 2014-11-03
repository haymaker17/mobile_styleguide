//
//  DeleteReportData.m
//  ConcurMobile
//
//  Created by yiwen on 3/8/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "DeleteReportData.h"
#import "DataConstants.h"
#import "ExSystem.h"

@implementation DeleteReportData
@synthesize rptKey, actionStatus;

-(NSString *)getMsgIdKey
{
	return DELETE_REPORT_DATA;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message

	self.rptKey = parameterBag[@"RPT_KEY"];
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/DeleteReport/%@", 
				[ExSystem sharedInstance].entitySettings.uri, self.rptKey];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	
	return msg;
}




- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.actionStatus = nil;
		actionStatus = [[ActionStatus alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
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
