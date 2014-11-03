//
//  SaveReportReceipt.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 12/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SaveReportReceipt2.h"
#import "DataConstants.h"

@implementation SaveReportReceipt2

-(NSString *)getMsgIdKey
{
	return SAVE_REPORT_RECEIPT2;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.rpt = parameterBag[@"REPORT"];
    
    NSString* reportKey = self.rpt.rptKey;
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/AddReportReceiptV2/%@",
                 [ExSystem sharedInstance].entitySettings.uri, reportKey];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	
    NSString* contentType = parameterBag[@"MIME_TYPE"];
    
    [msg setContentType:contentType];
	[msg setMethod:@"POST"];
	
    NSData* fileData = parameterBag[@"FILE_DATA"];
    [msg setBodyData:(NSMutableData*)fileData];

	return msg;
}



- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
}	


@end
