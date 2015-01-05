//
//  SaveReportEntryData.m
//  ConcurMobile
//
//  Created by yiwen on 12/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SaveReportEntryData.h"
#import "DataConstants.h"

@implementation SaveReportEntryData
@synthesize entry, rptTotalPosted, rptTotalClaimed, rptTotalApproved, attendees, formKey;
@synthesize fields, actionStatus;
@synthesize curExpKey, curExpType;

-(NSString *)getMsgIdKey
{
	return SAVE_REPORT_ENTRY_DATA;
}

-(NSString *)makeXMLBody
{//knows how to make a post
	
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] 
								initWithString:@"<ReportEntry xmlns='http://schemas.datacontract.org/2004/07/Snowbird' xmlns:i='http://www.w3.org/2001/XMLSchema-instance'>"];


	[bodyXML appendString:[NSString stringWithFormat:@"<ExpKey>%@</ExpKey>", self.curExpKey]];
	if ([self.formKey length])
		[bodyXML appendString:[NSString stringWithFormat:@"<FormKey>%@</FormKey>", self.formKey]];

	if ([self.entry.parentRpeKey length])
	{
		[bodyXML appendString:[NSString stringWithFormat:@"<ParentRpeKey>%@</ParentRpeKey>", self.entry.parentRpeKey]];
	}

    // MOB-21311
    // in order to be compatible with the old UI logic, we had to put the EReceiptImageId in the ReceiptImageId field
    // this causes subtle problems if we try to save with it.
    if ([self.entry.receiptImageId length] && self.entry.eReceiptImageId == nil)
	{
		[bodyXML appendString:[NSString stringWithFormat:@"<ReceiptImageId>%@</ReceiptImageId>", self.entry.receiptImageId]];
	}

	if ([self.entry.rpeKey length])
		[bodyXML appendString:[NSString stringWithFormat:@"<RpeKey>%@</RpeKey>", self.entry.rpeKey]];

	[bodyXML appendString:[NSString stringWithFormat:@"<RptKey>%@</RptKey>", self.rptKey]];
	[bodyXML appendString:@"<Fields>"];
	
    for (int ix = 0; ix < [self.fields count]; ix ++)
	{
		FormFieldData* ff = (self.fields)[ix];
		[bodyXML appendString:@"<FormField>"];
        
        // From home screen, add mileage may not have expType object, but type will be MILEG
		if(([self.curExpKey isEqualToString:@"MILEG"] ||(self.curExpType != nil && [self.curExpType isPersonalCarMileage]))
		   &&
		   ([ff.iD isEqualToString:@"TransactionCurrencyName"] 
			|| [ff.iD isEqualToString:@"PatKey"] || [ff.iD isEqualToString:@"ExpKey"] 
			|| [ff.iD isEqualToString:@"TransactionAmount"]))
		{
			[bodyXML appendString:@"<Access>RW</Access>"];
        } else if ([ff.access length]) {
			[bodyXML appendString:[NSString stringWithFormat:@"<Access>%@</Access>", ff.access]];
        }
		
		if ([ff.ctrlType length])
			[bodyXML appendString:[NSString stringWithFormat:@"<CtrlType>%@</CtrlType>", ff.ctrlType]];
		if ([ff.dataType length])
			[bodyXML appendString:[NSString stringWithFormat:@"<DataType>%@</DataType>", ff.dataType]];
		if ([ff.ftCode length])
			[bodyXML appendString:[NSString stringWithFormat:@"<FtCode>%@</FtCode>", ff.ftCode]];
		if ([ff.iD length])
			[bodyXML appendString:[NSString stringWithFormat:@"<Id>%@</Id>", ff.iD]];
		if ([ff.liCode length])
			[bodyXML appendString:[NSString stringWithFormat:@"<LiCode>%@</LiCode>", [NSString stringByEncodingXmlEntities:ff.liCode]]];
		if ([ff.liKey length])
			[bodyXML appendString:[NSString stringWithFormat:@"<LiKey>%@</LiKey>", ff.liKey]];
		if ([ff.listKey length])
			[bodyXML appendString:[NSString stringWithFormat:@"<ListKey>%@</ListKey>", ff.listKey]];
		if ([ff.parLiKey length])
			[bodyXML appendString:[NSString stringWithFormat:@"<ParLiKey>%@</ParLiKey>", ff.parLiKey]];
		if ([ff.fieldValue length])
		{
			if (![ff.iD isEqualToString:@"VenLiKey"] || ff.liKey != nil)
				[bodyXML appendString:[NSString stringWithFormat:@"<Value>%@</Value>", [NSString stringByEncodingXmlEntities:[ff getServerValue]]]];
		}
		[bodyXML appendString:@"</FormField>"];
		
	}
	[bodyXML appendString:@"</Fields>"];
	
    //MOB-15052 : XML order matters since MWS uses datacontractSerializer
    if (self.entry.taxforms != nil && [curExpType hasVATForm]) {
        [bodyXML appendString:[self.entry.taxforms getSaveXML]];
    }

	if (self.attendees != nil)
	{
		[bodyXML appendString:@"<Attendees>"];
		
		for (AttendeeData *attendee in self.attendees)
		{
			[bodyXML appendString:@"<Attendee xmlns=''>"];
			[bodyXML appendString:[NSString stringWithFormat:@"<Amount>%@</Amount>", attendee.amount]];
			[bodyXML appendString:[NSString stringWithFormat:@"<AtnKey>%@</AtnKey>", attendee.attnKey]];
			[bodyXML appendString:[NSString	stringWithFormat:@"<InstanceCount>%i</InstanceCount>", attendee.instanceCount]];
			[bodyXML appendString:[NSString stringWithFormat:@"<IsAmountEdited>%@</IsAmountEdited>", (attendee.isAmountEdited ? @"Y" : @"N")]];
			[bodyXML appendString:[NSString stringWithFormat:@"<VersionNumber>%@</VersionNumber>", attendee.versionNumber]];
			[bodyXML appendString:@"</Attendee>"];
		}
		
		[bodyXML appendString:@"</Attendees>"];
	}
	
    if ([self.entry.noShowCount length])
    {
        [bodyXML appendString:[NSString stringWithFormat:@"<NoShowCount>%@</NoShowCount>", self.entry.noShowCount]];
    }


	[bodyXML appendString:@"</ReportEntry>"];

	//NSLog(@"Save report entry: %@", bodyXML);
	
	return bodyXML;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.rptKey = parameterBag[@"RPT_KEY"];
	self.entry = parameterBag[@"ENTRY"];	
	self.formKey = parameterBag[@"FORM_KEY"];	
	self.fields = parameterBag[@"FIELDS"];  // Fields with updated data
	self.curExpKey = parameterBag[@"CUR_EXP_KEY"];
    self.curExpType = parameterBag[@"CUR_EXP_TYPE"];
	self.roleCode = parameterBag[@"ROLE_CODE"];
	self.attendees = parameterBag[@"ATTENDEES"];
	NSString* cpDown = parameterBag[@"COPY_DOWN_TO_CHILD_FORMS"];
	
	if (![roleCode length])
		self.roleCode = ROLE_EXPENSE_TRAVELER;
	
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/SaveReportEntryV4/%@", 
				[ExSystem sharedInstance].entitySettings.uri, self.roleCode];
	if ([@"Y" isEqualToString:cpDown])
		self.path = [NSString stringWithFormat:@"%@/%@", self.path, @"CopyDownToChildForms"];
	
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
	else if ([currentElement isEqualToString:@"ReportTotalPosted"])
	{
		[self setRptTotalPosted:buildString];
	}
	else if ([currentElement isEqualToString:@"ReportTotalClaimed"])
	{
		[self setRptTotalClaimed:buildString];
	}
	else if ([currentElement isEqualToString:@"ReportTotalApproved"])
	{
		[self setRptTotalApproved:buildString];
	}
	else if ([currentElement isEqualToString:@"SavedRpeKey"])
	{
		self.rpeKey = buildString;
	}
}	

#pragma mark -
#pragma mark PartialReportDataBase Methods
-(ReportData*) updateReportObject:(ReportData*) obj
{
	if (obj == nil)
		return obj;
	[super updateReportObject:obj];
	// Update report totals
	obj.totalPostedAmount = self.rptTotalPosted;
	obj.totalClaimedAmount = self.rptTotalClaimed;
	obj.totalApprovedAmount = self.rptTotalApproved;
	return obj;
}

@end
