//
//  ReportEntryFormData.m
//  ConcurMobile
//
//  Created by yiwen on 12/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReportEntryFormData.h"
#import "ReportData.h"
#import "DataConstants.h"

//-
@implementation ReportEntryFormData
@synthesize rpeKey, expKey;
@synthesize parentRpeKey, includeFormDef;

-(NSString *)getMsgIdKey
{
	return REPORT_ENTRY_FORM_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.rptKey = parameterBag[@"RPT_KEY"];
	self.rpeKey = parameterBag[@"RPE_KEY"];	
	self.expKey = parameterBag[@"EXP_KEY"];
	self.parentRpeKey = parameterBag[@"PARENT_RPE_KEY"];
	if (self.parentRpeKey != nil)
	{
		self.path = [NSString stringWithFormat:@"%@/mobile/Expense/ReportEntryItemizeFormV4/Y/%@/%@/%@",
					[ExSystem sharedInstance].entitySettings.uri, self.expKey, self.rptKey, self.parentRpeKey];
	}
	else if (self.rptKey != nil)
	{
		self.path = [NSString stringWithFormat:@"%@/mobile/Expense/ReportEntryFormV4/%@/%@",
				[ExSystem sharedInstance].entitySettings.uri, self.expKey, self.rptKey];
	}
	else {
        if (![self.expKey length])
            self.expKey = @"UNDEF";
        
		self.path = [NSString stringWithFormat:@"%@/mobile/Expense/ReportEntryFormV4/%@", 
					[ExSystem sharedInstance].entitySettings.uri, self.expKey];
	}

	if (self.rpeKey != nil)
		self.path = [NSString stringWithFormat:@"%@/%@", self.path, self.rpeKey];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	if ([elementName isEqualToString:@"ReportEntryDetail"])
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
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if ([elementName isEqualToString:@"ReportEntryDetail"])
	{
		// leave the object at rpt.entry
		// If rpeKey is nil, initialize the transaction date to current date
		if (rpeKey == nil && rpt.entry != nil && rpt.entry.parentRpeKey == nil)
		{
			rpt.entry.transactionDate = [CCDateUtilities formatDateToYearMonthDateTimeZoneMidNight:[NSDate date]];
			for (NSString* key in rpt.entry.fieldKeys)
			{
				FormFieldData* fld = (rpt.entry.fields)[key];
				if ([fld.iD isEqualToString:@"TransactionDate"])
				{
					fld.fieldValue = rpt.entry.transactionDate;
					break;
				}
			}
		}
        
        if ([self.rpt.entry.ereceiptId lengthIgnoreWhitespace] && ![self.rpt.entry.eReceiptImageId lengthIgnoreWhitespace])
            self.rpt.entry.eReceiptImageId = @"HACK e-receipt Image ID";
        
        //MOB-21147 - had to do it four times otherwise status is lost due to keyArchiver
        if (self.rpt.entry.eReceiptImageId != nil) {
            // MOB-21147 - Set hasReceipt
            // if its e-receipt then copy ereceipt image id to receiptimage id field. Also mark that we have a receipt
            [rpt.entry setHasMobileReceipt:@"Y"];
            [rpt.entry setReceiptImageId:self.rpt.entry.eReceiptImageId];
        }
	}
	else
		[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
}

@end
