//
//  SaveReportEntryReceipt.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/12/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "SaveReportEntryReceipt.h"
#import "DataConstants.h"
//-
@implementation SaveReportEntryReceipt
@synthesize entry,actionStatus;
@synthesize receiptImageId;

-(NSString *)getMsgIdKey
{
	return SAVE_REPORT_ENTRY_RECEIPT;
}


-(NSString *)makeXMLBody
{//knows how to make a post
	
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] 
								initWithString:@"<ReportEntry xmlns='http://schemas.datacontract.org/2004/07/Snowbird' xmlns:i='http://www.w3.org/2001/XMLSchema-instance'>"];
	
	if (self.receiptImageId != nil)
	{
		[bodyXML appendString:[NSString stringWithFormat:@"<ReceiptImageId>%@</ReceiptImageId>",self.receiptImageId]];
	}

	if ([self.entry.rpeKey length]) 
	{
		[bodyXML appendString:[NSString stringWithFormat:@"<RpeKey>%@</RpeKey>", self.entry.rpeKey]];
	}
	
	[bodyXML appendString:[NSString stringWithFormat:@"<RptKey>%@</RptKey>", self.rptKey]];
	[bodyXML appendString:@"</ReportEntry>"];
	return bodyXML;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.rptKey = ((ReportData*) parameterBag[@"REPORT"]).rptKey;
	self.entry = parameterBag[@"ENTRY"];	
	self.roleCode = parameterBag[@"ROLE_CODE"];
	self.receiptImageId = parameterBag[@"RECEIPT_IMAGE_ID"];
    
	if(roleCode == nil)
		roleCode = ROLE_EXPENSE_TRAVELER;
	
	NSString* urlPath = [NSString stringWithFormat:@"%@/mobile/Expense/SaveReportEntryReceiptV4/%@", 
				[ExSystem sharedInstance].entitySettings.uri, roleCode];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:urlPath MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	
	return msg;
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	if ([elementName isEqualToString:@"ReportEntry"])
	{
		rpt = [[ReportData alloc] init];
		inReport = YES;
		inComment = NO;
		inItemize = NO;
		inCompanyDisbursements = NO;
		inFormField = NO;
		inAttendee = NO;
		inEntry = YES;
	}
	else if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.actionStatus = nil;
		actionStatus = [[ActionStatus alloc] init];
	}
	
	
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if ([elementName isEqualToString:@"ReportEntry"])
	{
		// leave the object at rpt.entry
	}
	else
	{
		[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
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

