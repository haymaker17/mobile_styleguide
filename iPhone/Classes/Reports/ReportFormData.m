//
//  ReportFormData.m
//  ConcurMobile
//
//  Created by yiwen on 2/23/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReportFormData.h"
#import "ReportData.h"
#import "DataConstants.h"

//-
@implementation ReportFormData
@synthesize polKey;

-(NSString *)getMsgIdKey
{
	return REPORT_FORM_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.rptKey = parameterBag[@"RPT_KEY"];
	self.polKey = parameterBag[@"POL_KEY"];
	if (self.polKey != nil && self.rptKey != nil)
	{
		self.path = [NSString stringWithFormat:@"%@/mobile/Expense/ReportForm/%@/%@", 
					[ExSystem sharedInstance].entitySettings.uri, self.polKey, self.rptKey];
	}
	else if (self.polKey != nil)
	{
		self.path = [NSString stringWithFormat:@"%@/mobile/Expense/ReportForm/%@", 
					[ExSystem sharedInstance].entitySettings.uri, self.polKey];
	}
	else {
		self.path = [NSString stringWithFormat:@"%@/mobile/Expense/ReportForm/", 
					[ExSystem sharedInstance].entitySettings.uri];
	}
	
	NSLog(@"reportformdata path = %@", path);
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	if ([elementName isEqualToString:@"ReportDetail"])
	{
		rpt = [[ReportData alloc] init];
		inReport = YES;
		inComment = NO;
		inItemize = NO;
		inCompanyDisbursements = NO;
		inFormField = NO;
		inAttendee = NO;
		inEntry = NO;
	}	
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if ([elementName isEqualToString:@"ReportDetail"])
	{
		// If rptKey is nil, initialize the report date to current date
		if (rptKey == nil)
		{
			rpt.reportDate = [CCDateUtilities formatDateToYearMonthDateTimeZoneMidNight:[NSDate date]];
			for (NSString* key in rpt.fieldKeys)
			{
				FormFieldData* fld = (rpt.fields)[key];
				if ([fld.iD isEqualToString:@"UserDefinedDate"])
				{
					fld.fieldValue = rpt.reportDate;
					break;
				}
			}
		}
	}
	else
		[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
}


@end
