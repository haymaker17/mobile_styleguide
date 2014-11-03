//
//  AddToReportData.m
//  ConcurMobile
//
//  Created by yiwen on 4/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "AddToReportData.h"
#import "FormatUtils.h"
#import "EntityMobileEntry.h"
#import "SmartExpenseManager.h"
#import "Config.h"

@interface AddToReportData()
@property (nonatomic, strong) NSArray *smartExpenses;
@property (nonatomic, strong) NSArray *smartExpenseIds;
@end

@implementation AddToReportData
@synthesize reportStatus, meStatusDict, pctStatusDict, cctStatusDict, curStatus;
@synthesize inReportStatus, inEntriesStatus;
@synthesize meKeys, pctKeys, cctKeys, rcKeys, meAtnMap, reportName;

- (NSString*) getReportElementName
{
	return @"Report";
}

-(id)init
{
    self=[super init];
	if (self)
    {
        meStatusDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        pctStatusDict =[[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        cctStatusDict =[[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        self.smartExpenseIdsStatusDict =[[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        self.smartExpenses = nil;
        inReportStatus = YES;
        inEntriesStatus = NO;
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return ADD_TO_REPORT_DATA;
}

-(void) flushData
{
	inReportStatus = YES;
	inEntriesStatus = NO;
	[self.meStatusDict removeAllObjects];
	[self.pctStatusDict removeAllObjects];
    [self.cctStatusDict removeAllObjects];
    [self.smartExpenseIdsStatusDict removeAllObjects];
	self.curStatus = nil;
	self.reportStatus = nil;
	[super flushData];
}

-(BOOL) isSmartExpenseKey:(NSString *)key
{
    if (self.smartExpenses.count > 0) {
        for (int i=0; i<self.smartExpenses.count; i++) {
            EntityMobileEntry *tmp = (EntityMobileEntry *)self.smartExpenses[i];
            if ([key isEqualToString:tmp.key] || [key isEqualToString:tmp.cctKey] || [key isEqualToString:tmp.pctKey]) {
                return YES;
            }
        }
    }
    return NO;
}

-(NSString *)makeXMLBody
{	
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<AddToReportMap>"];
	if (meAtnMap != nil && [meAtnMap count] > 0)
	{
		[bodyXML appendString:@"<AttendeesMap xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">"];
		NSEnumerator *enumerator = [meAtnMap keyEnumerator];
		
		NSString* meKey;
		while ((meKey = (NSString*)[enumerator nextObject]) != nil)
		{
			NSArray* attendees = (NSArray*)meAtnMap[meKey];
			if (attendees != nil)
			{
				[bodyXML appendString:@"<AttendeeEntryMap>"];
				//[bodyXML appendString:@"<Attendees xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">"];
				[bodyXML appendString:@"<Attendees>"];
				for (AttendeeData*atn in attendees)
				{
					[bodyXML appendString:@"<Attendee>"];
					[bodyXML appendString:[NSString stringWithFormat:@"<Amount>%@</Amount>", atn.amount]];
					[bodyXML appendString:[NSString stringWithFormat:@"<AtnKey>%@</AtnKey>", atn.attnKey]];
					[bodyXML appendString:[NSString stringWithFormat:@"<VersionNumber>%@</VersionNumber>", atn.versionNumber]];

					/*
					[bodyXML appendString:[NSString stringWithFormat:@"<AtnTypeCode>%@</AtnTypeCode>", atn.atnTypeCode]];

					NSString *company = [atn getNullableValueForFieldId:@"Company"];
					if (company != nil)
						[bodyXML appendString:[NSString stringWithFormat:@"<Company>%@</Company>", company]];
					
					NSString *firstName = [atn getNullableValueForFieldId:@"FirstName"];
					if (firstName != nil)
						[bodyXML appendString:[NSString stringWithFormat:@"<FirstName>%@</FirstName>", firstName]];
					
					NSString *lastName = [atn getNullableValueForFieldId:@"LastName"];
					if (lastName != nil)
						[bodyXML appendString:[NSString stringWithFormat:@"<LastName>%@</LastName>", lastName]];
					*/
					
					[bodyXML appendString:@"</Attendee>"];
				}
				[bodyXML appendString:@"</Attendees>"];
				[bodyXML appendString:[NSString stringWithFormat:@"<MeKey>%@</MeKey>", meKey]];
				[bodyXML appendString:@"</AttendeeEntryMap>"];
			}
		}
		[bodyXML appendString:@"</AttendeesMap>"];		
	}
	if (cctKeys.count > 0)
	{
        [bodyXML appendString:@"<CctKeys xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">"];
		for (int ix = 0; ix < [cctKeys count]; ix++)
		{
			NSString *cctKey = (NSString*)cctKeys[ix];
            [bodyXML appendString:@"<a:string>"];
            [bodyXML appendString:cctKey];
            [bodyXML appendString:@"</a:string>"];
		}
        [bodyXML appendString:@"</CctKeys>"];
	}
	if (meKeys.count > 0)
	{
        [bodyXML appendString:@"<MeKeys xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">"];
		for (int ix = 0; ix < [meKeys count]; ix++)
		{
            NSString *meKey = (NSString*)meKeys[ix];
            [bodyXML appendString:@"<a:string>"];
            [bodyXML appendString:meKey];
            [bodyXML appendString:@"</a:string>"];
		}
        [bodyXML appendString:@"</MeKeys>"];
	}
	if (pctKeys.count > 0)
	{
        [bodyXML appendString:@"<PctKeys xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">"];
		for (int ix = 0; ix < [pctKeys count]; ix++)
		{
			NSString *pctKey = (NSString*)pctKeys[ix];
            [bodyXML appendString:@"<a:string>"];
            [bodyXML appendString:pctKey];
            [bodyXML appendString:@"</a:string>"];
		}
        [bodyXML appendString:@"</PctKeys>"];
	}
	if (reportName != nil)
		[bodyXML appendString:[NSString stringWithFormat:@"<ReportName>%@</ReportName>", [FormatUtils makeXMLSafe:reportName]]];
	else {
		NSString* rptStr = [NSString stringWithFormat:@"<RptKey>%@</RptKey>", rptKey];
		[bodyXML appendString:rptStr];
	}
    //
    // E-Receipt only pass smartExpenseIds
    if (self.smartExpenseIds.count > 0)
    {
        [bodyXML appendString:@"<SmartExpenseIds xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">"];
        for (int ix = 0; ix < [self.smartExpenseIds count]; ix++)
        {
            NSString *smartExpenseId = (NSString*)self.smartExpenseIds[ix];
            [bodyXML appendString:@"<a:string>"];
            [bodyXML appendString:smartExpenseId];
            [bodyXML appendString:@"</a:string>"];
        }
        [bodyXML appendString:@"</SmartExpenseIds>"];
    }
    // expenses from ReceiptCapture/ExpenseIt
    if (rcKeys.count > 0)
    {
        [bodyXML appendString:@"<SmartExpenseIds xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">"];
        for (int ix = 0; ix < [rcKeys count]; ix++)
		{
			NSString *rcKey = (NSString*)rcKeys[ix];
			[bodyXML appendString:@"<a:string>"];
            [bodyXML appendString:rcKey];
            [bodyXML appendString:@"</a:string>"];   
		}
        [bodyXML appendString:@"</SmartExpenseIds>"];
    }
	
    if (self.smartExpenses.count > 0) {
        [bodyXML appendString:@"<SmartExpenses xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">"];
        for (int i=0; i<self.smartExpenses.count; i++) {
            EntityMobileEntry *tmp = (EntityMobileEntry *)self.smartExpenses[i];
            [bodyXML appendString:@"<SmartExpensesAddMap>"];
            if (tmp.cctKey.length > 0)
            {
                [bodyXML appendString:[NSString stringWithFormat:@"<CctKey>%@</CctKey>", tmp.cctKey]];
                [bodyXML appendString:[NSString stringWithFormat:@"<MeKey>%@</MeKey>", tmp.smartExpenseMeKey]];
            }
            else if (tmp.pctKey.length > 0)
            {
                [bodyXML appendString:[NSString stringWithFormat:@"<MeKey>%@</MeKey>", tmp.smartExpenseMeKey]];
                [bodyXML appendString:[NSString stringWithFormat:@"<PctKey>%@</PctKey>", tmp.pctKey]];
            }
            [bodyXML appendString:@"</SmartExpensesAddMap>"];
        }
        [bodyXML appendString:@"</SmartExpenses>"];
    }

    if(![Config isEreceiptsEnabled]){
        [bodyXML appendString:@"<UsesMatch>"];
        if ([ExSystem sharedInstance].entitySettings.smartExpenseEnabledOnReports.boolValue) {
            [bodyXML appendString:@"Y"];
        } else {
            [bodyXML appendString:@"N"];
        }
        [bodyXML appendString:@"</UsesMatch>"];

    }
	[bodyXML appendString:@"</AddToReportMap>"];
	return bodyXML;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.meKeys = parameterBag[@"ME_KEYS"];
	self.pctKeys = parameterBag[@"PCT_KEYS"];
	self.cctKeys = parameterBag[@"CCT_KEYS"];
    self.rcKeys = parameterBag[@"RC_KEYS"];
	self.meAtnMap = parameterBag[@"ME_ATN_MAP"];
	self.rptKey = parameterBag[@"RPT_KEY"];
	self.reportName = parameterBag[@"REPORT_NAME"];
    self.smartExpenses = parameterBag[@"SmartExpenseList"];
    self.smartExpenseIds = parameterBag[@"SmartExpenseIds"];
    
    if ([Config isEreceiptsEnabled] ) {
        self.path = [NSString stringWithFormat:@"%@/mobile/Expense/AddToReportV5",
                     [ExSystem sharedInstance].entitySettings.uri ];
    }
    else{
        self.path = [NSString stringWithFormat:@"%@/mobile/Expense/AddToReportV4",
				[ExSystem sharedInstance].entitySettings.uri ];
    }
    
	//,  [parameterBag objectForKey:@"ID_KEY"], kRoleCode
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
	
	if (inReportStatus && [elementName isEqualToString:@"ActionStatus"])
	{
		self.reportStatus = nil;
		reportStatus = [[ActionStatus alloc] init];
	}
	else if (!inReport && [elementName isEqualToString:@"Entries"])
	{
		inEntriesStatus = YES;
        inReportStatus = NO;
	}
    else if (!inReport && [elementName isEqualToString:@"Transactions"])
    {
        inPcTransactions = YES;
        inReportStatus = NO;
    }
    else if (!inReport && [elementName isEqualToString:@"CcTransactions"])
    {
        inCcTransactions = YES;
        inReportStatus = NO;
    }
    else if (!inReport && [elementName isEqualToString:@"SmartExpenses"])
    {
        inSmartExpenses = YES;
        inReportStatus = NO;
    }
	else if ([elementName isEqualToString:@"ActionStatus"]) // ActionStatus not in report
	{
		self.curStatus = nil;
		curStatus = [[ActionStatus alloc] init];
	}
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
	if (inEntriesStatus && [elementName isEqualToString:@"Entries"])
	{
		inEntriesStatus = NO;
        inReportStatus = YES;
	}
	else if (inCcTransactions && [elementName isEqualToString:@"CcTransactions"])
	{
        inCcTransactions = NO;
        inReportStatus = YES;
    }
	else if (inPcTransactions && [elementName isEqualToString:@"Transactions"])
	{
        inPcTransactions = NO;
        inReportStatus = YES;
    }
    else if (inSmartExpenses && [elementName isEqualToString:@"SmartExpenses"])
    {
        inSmartExpenses = NO;
        inReportStatus = YES;
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
	if (inReportStatus && [currentElement isEqualToString:@"Status"])
	{
		self.reportStatus.status = string;
	}	
	else if (inReportStatus && [currentElement isEqualToString:@"ErrorMessage"])
	{
		self.reportStatus.errMsg = buildString;
	}	
	else if ([currentElement isEqualToString:@"Status"])
	{
		self.curStatus.status = string;
	}
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.curStatus.errMsg = buildString;
	}
	else if (inEntriesStatus && [currentElement isEqualToString:@"MeKey"])
	{
		(self.meStatusDict)[buildString] = curStatus;
	}
	else if (inPcTransactions && [currentElement isEqualToString:@"PctKey"])
	{
		(self.pctStatusDict)[buildString] = curStatus;
	}
	else if (inCcTransactions && [currentElement isEqualToString:@"CctKey"])
	{
		(self.cctStatusDict)[buildString] = curStatus;
	}
    else if (inSmartExpenses && [currentElement isEqualToString:@"SmartExpenseId"])
    {
        (self.smartExpenseIdsStatusDict)[buildString] = curStatus;
    }
}

-(BOOL) hasFailedEntry
{
    for (NSString* meKey in self.meStatusDict)
    {
        ActionStatus* entryStatus = (self.meStatusDict)[meKey];
        if (![entryStatus.status isEqualToString:@"SUCCESS"] && ![entryStatus.status isEqualToString:@"SUCCESS_SMARTEXP"])
            return TRUE;
    }
    for (NSString* cctKey in self.cctStatusDict)
    {
        ActionStatus* entryStatus = (self.cctStatusDict)[cctKey];
        if (![entryStatus.status isEqualToString:@"SUCCESS"] && ![entryStatus.status isEqualToString:@"SUCCESS_SMARTEXP"])
            return TRUE;
    }
    for (NSString* pctKey in self.pctStatusDict)
    {
        ActionStatus* entryStatus = (self.pctStatusDict)[pctKey];
        if (![entryStatus.status isEqualToString:@"SUCCESS"] && ![entryStatus.status isEqualToString:@"SUCCESS_SMARTEXP"])
            return TRUE;
    }
    for (NSString* smartExpenseId in self.smartExpenseIdsStatusDict)
    {
        ActionStatus* entryStatus = (self.smartExpenseIdsStatusDict)[smartExpenseId];
        if (![entryStatus.status isEqualToString:@"SUCCESS"] && ![entryStatus.status isEqualToString:@"SUCCESS_SMARTEXP"])
            return TRUE;
    }

    return FALSE;
}


@end
