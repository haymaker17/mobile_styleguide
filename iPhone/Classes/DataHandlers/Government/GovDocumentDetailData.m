//
//  GovDocumentDetailData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovDocumentDetailData.h"
#import "DateTimeFormatter.h"
#import "FormatUtils.h"
#import "GovDocumentManager.h"
#import "EntityGovDocument.h"

@interface GovDocumentDetailData(Private)

-(void)fillException:(NSString *)string;
-(void)fillAccountCode:(NSString *)string;
-(void)fillExpense:(NSString *)string;
-(void)fillPerdiem:(NSString *)string;
-(void)fillDocInfo:(NSString *)string;

@end

@implementation GovDocumentDetailData

-(void) flushData
{
    [super flushData];
    self.inExpense = NO;
    self.inAudit = NO;
    self.inPerdiem = NO;
    self.inException = NO;
    self.inAccountCode = NO;
    self.inReasonCode = NO;
    self.inTripTypeListRow = NO;
}

-(NSString *)getMsgIdKey
{
	return GOV_DOCUMENT_DETAIL;
}

-(NSString *)makeXMLBody
{
//    <TMDocRequest>
//    <docName>TA3652</docName>
//    <docType>AUTH</docType>
//    <travid>13767168</travid>
//    </TMDocRequest>
    
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<TMDocRequest>"];
	[bodyXML appendString:@"<docName>%@</docName>"];
	[bodyXML appendString:@"<docType>%@</docType>"];
    if ([self.travelerId length])
        [bodyXML appendString:[NSString stringWithFormat:@"<travid>%@</travid>", self.travelerId]];
    
	[bodyXML appendString:@"</TMDocRequest>"];
	
	
	NSString* formattedBodyXml = nil;
	
	formattedBodyXml = [NSString stringWithFormat:bodyXML,
						[NSString stringByEncodingXmlEntities:self.docName], self.docType];
	
	return formattedBodyXml;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    self.travelerId = [parameterBag objectForKey:@"TRAVELER_ID"];
    self.docName = [parameterBag objectForKey:@"DOC_NAME"];
    self.docType = [parameterBag objectForKey:@"DOC_TYPE"];
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/GetTMDocDetail",[ExSystem sharedInstance].entitySettings.uri];
    
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
	
	if ([elementName isEqualToString:@"DocDetailInfo"])
	{
		self.currentDoc = [[GovDocumentDetail alloc] init];
	}
	else if ([elementName isEqualToString:@"AccountCode"])
	{
		self.inAccountCode = YES;
        self.currentAccountCode = [[GovDocAccountCode alloc] init];
	}
	else if ([elementName isEqualToString:@"ReasonCode"])
	{
		self.inReasonCode = YES;
        self.currentReasonCode = [[GovDocReasonCode alloc] init];
	}
	else if ([elementName isEqualToString:@"Audits"])
	{
		self.inAudit = YES;
	}
	else if ([elementName isEqualToString:@"PerdiemTDY"])
	{
		self.inPerdiem = YES;
	}
	else if ([elementName isEqualToString:@"TDY"] && _inPerdiem)
	{
        self.currentPerdiem = [[GovDocPerdiemTDY alloc] init];
	}
	else if (!self.inExpense && [elementName isEqualToString:@"Exception"])
	{
		self.inException = YES;
        self.currentException = [[GovDocException alloc] init];
	}
	else if ([elementName isEqualToString:@"Expense"])
	{
		self.inExpense = YES;
        self.currentExpense = [[GovDocExpense alloc] init];
	}
    else if ([elementName isEqualToString:@"TripTypeListRow"])
    {
        self.inTripTypeListRow = YES;
        self.currentTripTypeCode = [[GovDocTripTypeCode alloc] init];
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    if ([elementName isEqualToString:@"AccountCode"])
	{
		self.inAccountCode = NO;
        if (self.currentAccountCode != nil && self.currentAccountCode.account != nil)
        {
            [self.currentDoc.accountCodes addObject:self.currentAccountCode];
        }
        self.currentAccountCode = nil;
	}
    else if ([elementName isEqualToString:@"Expense"])
	{
		self.inExpense = NO;
        if (self.currentExpense != nil && self.currentExpense.expDate != nil)
        {
            [self.currentDoc.expenses addObject:self.currentExpense];
        }
        self.currentExpense = nil;
	}
	else if ([elementName isEqualToString:@"ReasonCode"])
	{
		self.inReasonCode = NO;
        if (self.currentReasonCode != nil && self.currentReasonCode.code != nil)
        {
            [self.currentDoc.reasonCodes addObject:self.currentReasonCode];
        }
        self.currentReasonCode = nil;
	}
	else if ([elementName isEqualToString:@"Audits"])
	{
		self.inAudit = NO;
	}
	else if ([elementName isEqualToString:@"PerdiemTDY"])
	{
		self.inPerdiem = NO;
	}
	else if ([elementName isEqualToString:@"TDY"] && _inPerdiem)
	{
        if (self.currentPerdiem != nil && self.currentPerdiem.beginTdy != nil)
            [self.currentDoc.perdiemTDY addObject:self.currentPerdiem];
        self.currentPerdiem = nil;
	}
	else if (self.inException && [elementName isEqualToString:@"Exception"])
	{
		self.inException = NO;
        if (self.currentException != nil && self.currentException.name != nil)
            [self.currentDoc.exceptions addObject:self.currentException];
        self.currentException = nil;
	}
    else if ([elementName isEqualToString:@"DocDetailInfo"])
    {
        // Pull up data from list info and fill in doc detail object
        self.currentDoc.docType = self.docType;
        EntityGovDocument* docListInfo = [[GovDocumentManager sharedInstance] fetchDocumentByDocName:self.currentDoc.docName withType:self.currentDoc.docType withTravelerId:self.currentDoc.travelerId];
        if (docListInfo != nil)
        {
            self.currentDoc.travelerName = docListInfo.travelerName;
            self.currentDoc.tripBeginDate = docListInfo.tripBeginDate;
            self.currentDoc.tripEndDate = docListInfo.tripEndDate;
            self.currentDoc.needsStamping = docListInfo.needsStamping;
            self.currentDoc.gtmDocType = docListInfo.gtmDocType;
            self.currentDoc.docTypeLabel = docListInfo.docTypeLabel;
            // TODO - check AuthForVch
        }
    }
    else if ([elementName isEqualToString:@"TripTypeListRow"])
    {
        self.inTripTypeListRow = NO;
        if (self.currentTripTypeCode != nil && self.currentTripTypeCode.tripType != nil)
            [self.currentDoc.tripTypeCodes addObject:self.currentTripTypeCode];
        self.currentTripTypeCode = nil;
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];

    if (self.inExpense)
	{
        [self fillExpense:buildString];
    }
    else if (self.inAccountCode)
    {
        [self fillAccountCode:buildString];
    }
    else if (self.inReasonCode)
    {
        [self fillReasonCode:buildString];
    }
    else if (self.inPerdiem)
    {
        [self fillPerdiem:buildString];
    }
    else if (self.inException)
    {
        [self fillException:buildString];
    }
    else if (self.inTripTypeListRow)
    {
        [self fillTripTypeList:buildString];
    }
    else
        [self fillDocInfo:buildString];
}

#pragma mark Entry Nodes
-(void)fillReasonCode:(NSString *)string
{
    if ([currentElement isEqualToString:@"Code"])
    {
		[self.currentReasonCode setCode:buildString];
    }
    else if ([currentElement isEqualToString:@"Comments"])
    {
		[self.currentReasonCode setComments:buildString];
    }
}

-(void)fillAccountCode:(NSString *)string
{
    if ([currentElement isEqualToString:@"Account"])
    {
		[self.currentAccountCode setAccount:buildString];
    }
    else if ([currentElement isEqualToString:@"Amount"])
    {
		[self.currentAccountCode setAmount:[FormatUtils decimalNumberFromServerString:buildString]];
    }
}

-(void)fillException:(NSString *)string
{
    if ([currentElement isEqualToString:@"Name"])
    {
        [self.currentException setName:[buildString capitalizedString]];
    }
    else if ([currentElement isEqualToString:@"Error_Status"])
    {
		[self.currentException setErrorStatus:buildString];
    }
    else if ([currentElement isEqualToString:@"Comments"])
    {
		[self.currentException setComments:buildString];
    }
}

-(void)fillExpense:(NSString *)string
{
    if ([currentElement isEqualToString:@"expid"])
    {
		[self.currentExpense setExpId:buildString];
    }
    else if ([currentElement isEqualToString:@"ExpDate"])
	{
		[self.currentExpense setExpDate:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"ExpenseDesc"])
    {
		[self.currentExpense setExpenseDesc:buildString];
    }
    else if ([currentElement isEqualToString:@"Amount"])
    {
		[self.currentExpense setAmount:[FormatUtils decimalNumberFromServerString:buildString]];
    }
    else if ([currentElement isEqualToString:@"PaymentMethod"])
    {
		[self.currentExpense setPaymentMethod:buildString];
    }
    else if ([currentElement isEqualToString:@"Reimbursable"])
    {
		[self.currentExpense setReimbursable:@([buildString boolValue])];
    }
    else if ([currentElement isEqualToString:@"Reimbursable"])
    {
		[self.currentExpense setReimbursable:@([buildString boolValue])];
    }
    else if ([currentElement isEqualToString:@"ExceptionCmt"])
    {
		[self.currentExpense setExceptionCmt:buildString];
    }
    else if ([currentElement isEqualToString:@"ExpenseCategory"])
    {
        [self.currentExpense setExpenseCategory:buildString];
    }
    else if ([currentElement isEqualToString:@"imageid"])
    {
        [self.currentExpense setImageId:buildString];
    }
}

-(void)fillPerdiem:(NSString *)string
{
    if ([currentElement isEqualToString:@"BeginTdy"])
	{
		[self.currentPerdiem setBeginTdy:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"EndTdy"])
	{
		[self.currentPerdiem setEndTdy:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"PerdiemLocation"])
    {
		[self.currentPerdiem setPerdiemLocation:buildString];
    }
    else if ([currentElement isEqualToString:@"Rate"])
    {
		[self.currentPerdiem setRate:buildString];
    }
//    else if ([currentElement isEqualToString:@"Rate"])
//    {
//		[self.currentExpense setAmount:[FormatUtils decimalNumberFromServerString:buildString]];
//    }
//    <BeginTdy>2012-04-18</BeginTdy>
//    <PerdiemLocation>WASHINGTON,DC</PerdiemLocation>               <EndTdy>2012-04-21</EndTdy>
//    <Rate> 77.00 / 46.00 (10/01/10-09/30/12)</Rate>
}

-(void)fillDocInfo:(NSString *)string
{
    if (self.inAudit)
    {
        if ([currentElement isEqualToString:@"Passed"] && [buildString lengthIgnoreWhitespace])
            [self.currentDoc setAuditPassed:[NSNumber numberWithInt:[buildString intValue]]];
        else if ([currentElement isEqualToString:@"Failed"] && [buildString lengthIgnoreWhitespace])
            [self.currentDoc setAuditFailed:[NSNumber numberWithInt:[buildString intValue]]];
    }
    else if ([currentElement isEqualToString:@"TripEndDate"])
	{
		[self.currentDoc setTripEndDate:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"TravelerId"])
    {
		[self.currentDoc setTravelerId:buildString];
    }
    else if ([currentElement isEqualToString:@"TANumber"])
    {
		[self.currentDoc setTANumber:buildString];
    }
    else if ([currentElement isEqualToString:@"CurrentStatus"])
    {
		[self.currentDoc setCurrentStatus:buildString];
    }
    else if ([currentElement isEqualToString:@"PurposeCode"])
    {
		[self.currentDoc setPurposeCode:buildString];
    }
    else if ([currentElement isEqualToString:@"EmissionsLbs"])
    {
        [self.currentDoc setEmissionsLbs:[NSDecimalNumber decimalNumberWithString:buildString]];
    }
    else if ([currentElement isEqualToString:@"DocumentName"])
    {
		[self.currentDoc setDocName:buildString];
    }
    else if ([currentElement isEqualToString:@"ApproveLabel"])
    {
		[self.currentDoc setApproveLabel:buildString];
    }
    else if ([currentElement isEqualToString:@"TotalEstCost"])
    {
		[self.currentDoc setTotalEstCost:[FormatUtils decimalNumberFromServerString:buildString]];
    }
    else if ([currentElement isEqualToString:@"NonReimbursableAmount"])
    {
        [self.currentDoc setNonReimbursableAmount:[FormatUtils decimalNumberFromServerString:buildString]];
    }
    else if ([currentElement isEqualToString:@"AdvAmtRequested"])
    {
        [self.currentDoc setAdvAmtRequested:[FormatUtils decimalNumberFromServerString:buildString]];
    }
    else if ([currentElement isEqualToString:@"AdvApplied"])
    {
        [self.currentDoc setAdvApplied:[FormatUtils decimalNumberFromServerString:buildString]];
    }
    else if ([currentElement isEqualToString:@"PayToChargeCard"])
    {
        [self.currentDoc setPayToChargeCard:[FormatUtils decimalNumberFromServerString:buildString]];
    }
    else if ([currentElement isEqualToString:@"PayToTraveler"])
    {
        [self.currentDoc setPayToTraveler:[FormatUtils decimalNumberFromServerString:buildString]];
    }
    if ([currentElement isEqualToString:@"TripBeginDate"])
	{
		[self.currentDoc setTripBeginDate:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"GtmDocType"])
    {
		[self.currentDoc setGtmDocType:buildString];
    }
    else if ([currentElement isEqualToString:@"DocType"])
    {
		[self.currentDoc setDocType:buildString];
    }
    else if ([currentElement isEqualToString:@"TravelerName"])
    {
		[self.currentDoc setTravelerName:buildString];
    }
    else if([currentElement isEqualToString:@"Comments"])
    {
        [self.currentDoc setComments:buildString];
    }
    else if([currentElement isEqualToString:@"DocImageID"])
    {
        [self.currentDoc setReceiptId:buildString];
    }
    else if([currentElement isEqualToString:@"UseTypeCode"])
    {
        [self.currentDoc setRequireTypeCode:[buildString boolValue]];
    }
    // TODO - get the following two from core data/list info
//    else if ([currentElement isEqualToString:@"NeedsStamping"])
//    {
//		[self.currentDoc setTotalExpCost:[FormatUtils decimalNumberFromServerString:buildString]];
//    }
//    else if ([currentElement isEqualToString:@"AuthForVch"])
//    {
//		[self.currentDoc setTotalExpCost:[FormatUtils decimalNumberFromServerString:buildString]];
//    }
//    tripEndDate,travelerId,purposeCode,docName,approveLabel,docTypeLabel,totalExpCost,tripBeginDate,gtmDocType,docType,travelerName,needsStamping,authForVch
}

-(void)fillTripTypeList:(NSString *)string
{
    if ([currentElement isEqualToString:@"TripType"])
    {
        [self.currentTripTypeCode setTripType:string];
    }
    else if ([currentElement isEqualToString:@"TypeCode"])
    {
        [self.currentTripTypeCode setTypeCode:string];
    }
    else if ([currentElement isEqualToString:@"Selected"])
    {
        [self.currentTripTypeCode setSelected:@([buildString boolValue])];
    }
}

/*
 <?xml version="1.0"?>
 <dsDocDetail xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
 <DocDetailInfo>
 < Expenses>Ernest1</ Expenses>
 <Comments>Ernest Testing 1</Comments>
 ... <!- - more fields from GetTMDocSummary except Expenses -- >
 <AccountCodes>
 <TravelerId>13767168</TravelerId>
 <AccountCode>
 <TravelerId>13767168</TravelerId>
 <Account>ETS2-1/Jandak</Account>
 <Amount>2003.00</Amount>
 </AccountCode>
 </AccountCodes>
 <Audits>
 <TravelerId>13767168</TravelerId>
 <Passed>5</Passed>
 <Failed>1</Failed>
 </Audits>
 <PerdiemTDY>
 <TravelerId>13767168</TravelerId>
 <TDY>
 <TravelerId>13767168</TravelerId>
 <BeginTdy>2012-04-18</BeginTdy>
 <PerdiemLocation>WASHINGTON,DC</PerdiemLocation>               <EndTdy>2012-04-21</EndTdy>
 <Rate>    224.00 /     71.00 (03/01/12-06/30/12)</Rate>
 </TDY>
 </PerdiemTDY>
 <Exceptions><Exception>…</Exception></Exceptions>
 <Expenses>
 <Expense>
 <ExpenseCategory>Com. Carrier</ExpenseCategory>
 <TotalExpCatCost>678.90</TotalExpCatCost>
 <Expense >
 <ExpenseCategory >Lodging-PerDiem</ ExpenseCategory>
 <Amount>999.00</Amount>
 <Exception>NO</Exception>
 <ExpDate>2012-07-10</ExpDate>
 <ExpenseDesc>Airline Flight</ExpenseDesc>
 <PaymentMethod>IBA</PaymentMethod>
 <Reimbursable>true</Reimbursable>
 <ExceptionCmt/>
 </Expense>
 …
 </Expenses>
 <ReasonCodes>
 <ReasonCode>< Code></ Code><Comments></Comments></ReasonCode>
 </ReadonCodes>
 </DocDetailInfo>
 </dsDocDetail >
 */

//-(void)saveToLocalCache:(NSString*) uId withCacheMeta:(CacheData*) cacheData
//{
//	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
//    NSString *documentsDirectory = [paths objectAtIndex:0];
//	NSString *msgId = ACTIVE_REPORT_DETAIL_DATA;
//	// Make sure a valid rpt object is stored in cache
//	if (self.rpt != nil && (self.rpt.rptKey != nil||(self.rpt.entry != nil && self.rpt.entry.rpeKey!=nil)))
//	{
//		@synchronized(cacheData)
//		{
//			NSString *theRptKey = self.rptKey != nil? self.rptKey: (self.rpt.rptKey == nil? self.rpt.entry.rptKey : self.rpt.rptKey);
//			// TODO - synchronize on this report meta data only
//			//NSString *cacheKey = [NSString stringWithFormat:@"%@_%@_%@", msgId, recordKey, uId];
//			NSString *archivePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"%@_%@_%@", msgId, theRptKey, uId]];
//			ReportData* obj = [NSKeyedUnarchiver unarchiveObjectWithFile:archivePath];
//			obj = [self updateReportObject:obj];
//			// Save the updated report
//			if (obj != nil)
//			{
//				[NSKeyedArchiver archiveRootObject:obj toFile: archivePath];
//				[cacheData saveCacheMetaData:msgId UserID:uId RecordKey:theRptKey];
//			}
//		}
//	}
//}
//
//-(void)loadFromLocalCache:(NSString*) uId withCacheMeta:(CacheData*) cacheData
//{
//}

@end
