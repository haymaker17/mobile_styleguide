//
//  SaveReportReceipt.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 12/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SaveReportReceipt.h"

#import "DataConstants.h"

@implementation SaveReportReceipt

-(NSString *)getMsgIdKey
{
	return SAVE_REPORT_RECEIPT;
}

-(NSString *)makeXMLBody
{
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc]
                                                initWithString:@"<AddReportReceiptAction xmlns:i='http://www.w3.org/2001/XMLSchema-instance'>"];
	
	[bodyXML appendString:[NSString stringWithFormat:@"<ReceiptImageId>%@</ReceiptImageId>",self.rpt.receiptImageId]];// self.rpt.receiptImageId]];
	[bodyXML appendString:[NSString stringWithFormat:@"<RptKey>%@</RptKey>", self.rpt.rptKey]];
	[bodyXML appendString:@"</AddReportReceiptAction>"];
	
	return bodyXML;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.rpt = parameterBag[@"REPORT"];
    
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/AddReportReceipt",
                 [ExSystem sharedInstance].entitySettings.uri];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	
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
