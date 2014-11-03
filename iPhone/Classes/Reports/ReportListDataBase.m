//
//  ReportListDataBase.m
//  ConcurMobile
//
//  Created by yiwen on 4/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReportListDataBase.h"
#import "MCLogging.h"

@implementation ReportListDataBase

@synthesize path, currentElement, rpt, buildString, reportNameBuildString;
@synthesize inEntry;

-(id)init
{
    self = [super init];
	if (self)
    {
        isInElement = @"NO";
        self.currentElement = @"";
        self.inEntry = NO;
    }
	return self;
}

-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	
	[self flushData];
//	[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportListDataBase::parseXMLFileAtData"] Level:MC_LOG_DEBU];
	[self parseXMLFileAtData:data];
}

- (NSString*) getReportElementName
{
	return @"Report";
}

- (NSString*) getEntryElementName
{
	return @"ReportEntry";
}


- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	//	if (msg.errBody == nil) 
	//		msg.errBody = [parseError localizedDescription];
	//	
	//	if (msg.errCode == nil) 
	//		msg.errCode = [NSString stringWithFormat:@"%i", [parseError code]];
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	//[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::didStartElement currentElement = %@", currentElement] Level:MC_LOG_DEBU];
	isInElement = @"YES";
	

	self.buildString = [[NSMutableString alloc] init];
	
	
	reportNameBuildString = [[NSMutableString alloc] init];
	
	if ([elementName isEqualToString: [self getReportElementName]])
	{
		
		
		rpt = [[ReportData alloc] init];
	}
	else if ([elementName isEqualToString:[self getEntryElementName]])
	{
        inEntry = YES;
	}
    else if ([elementName isEqualToString:@"WorkflowActions"])
    {
        rpt.workflowActions = [[NSMutableArray alloc] init];
    }
    else if ([elementName isEqualToString:@"WorkflowAction"])
    {
        rpt.curWorkflowAction = [[WorkflowAction alloc] init];
    }
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	//[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::didEndElement currentElement = %@", currentElement] Level:MC_LOG_DEBU];
	if ([elementName isEqualToString:[self getReportElementName]])
	{
		if (rpt != nil && rpt.rptKey != nil)
		{
			objDict[rpt.rptKey] = rpt;
			[keys addObject:rpt.rptKey];
		}
		//[rpt release];
	}
	else if ([elementName isEqualToString:[self getEntryElementName]])
	{
		[rpt finishEntry];
        inEntry = NO;
	}
    else if ([elementName isEqualToString:@"WorkflowAction"])
    {
        [rpt finishWorkflowAction];
    }
	
}



- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	//[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::foundCharacters currentElement = %@, string = %@", currentElement, string] Level:MC_LOG_DEBU];
	
	[buildString appendString:string];
	
	if ([currentElement isEqualToString:@"ApprovedAmount"])
	{
		[rpt.entry setApprovedAmount:buildString];
	}
	else if ([currentElement isEqualToString:@"ExpName"])
	{
		[rpt.entry setExpName:buildString];
	}
	else if ([currentElement isEqualToString:@"ExpKey"])
	{
		[rpt.entry setExpKey:buildString];
	}
	else if ([currentElement isEqualToString:@"HasAllocation"])
	{
		[rpt.entry setHasAllocation:buildString];
	}
	else if ([currentElement isEqualToString:@"HasAttendees"])
	{
		[rpt.entry setHasAttendees:buildString];
	}
	else if ([currentElement isEqualToString:@"HasComments"])
	{
		[rpt.entry setHasComments:buildString];
	}
	//	else if ([currentElement isEqualToString:@"TransactionDate"])
	//	{
	//		NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
	//		[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
	//		NSDate *dt = [dateFormatter dateFromString:buildString];
	//		[oope setTranDate:dt]; 
	//		[dateFormatter release];
	//	}
	else if ([currentElement isEqualToString:@"HasExceptions"])
	{
		[rpt.entry setHasExceptions:buildString];
	}
	else if ([currentElement isEqualToString:@"IsCreditCardCharge"])
	{
		[rpt.entry setIsCreditCardCharge:buildString];
	}
	else if ([currentElement isEqualToString:@"IsPersonalCardCharge"])
	{
		[rpt.entry setIsPersonalCardCharge:buildString];
	}
	else if ([currentElement isEqualToString:@"IsItemized"])
	{
		[rpt.entry setIsItemized:buildString];
	}
	else if ([currentElement isEqualToString:@"HasMobileReceipt"])
	{
		[rpt.entry setHasMobileReceipt:buildString];
	}
	else if ([currentElement isEqualToString:@"HasTravelAllowance"])
	{
        [rpt.entry setHasTravelAllowance:buildString];
	}
	else if ([currentElement isEqualToString:@"TaDayKey"])
	{
        [rpt.entry setTaDayKey:buildString];
	}
	else if ([currentElement isEqualToString:@"IsPersonal"])
	{
		[rpt.entry setIsPersonal:buildString]; 
	}		
	else if ([currentElement isEqualToString:@"LocationName"])
	{
		[rpt.entry setLocationName:buildString];
	}	
	else if ([currentElement isEqualToString:@"RpeKey"])
	{
		[rpt.entry setRpeKey:buildString];
	}	
	else if ([currentElement isEqualToString:@"TransactionAmount"])
	{
		[rpt.entry setTransactionAmount:buildString];
	}	
	else if ([currentElement isEqualToString:@"TransactionCrnCode"])
	{
		[rpt.entry setTransactionCrnCode:buildString];
	}	
	else if ([currentElement isEqualToString:@"TransactionDate"])
	{
		[rpt.entry setTransactionDate:buildString];
	}	
	else if ([currentElement isEqualToString:@"VendorDescription"])
	{
		
		[rpt.entry setVendorDescription:buildString];
	}	
	else if ([currentElement isEqualToString:@"ApsKey"])
	{
		[rpt setApsKey:buildString];
	}	
	else if ([currentElement isEqualToString:@"PayKey"])
	{
		[rpt setPayKey:buildString];
	}
	else if ([currentElement isEqualToString:@"CrnCode"])
	{
		[rpt setCrnCode:buildString];
	}	
	else if ([currentElement isEqualToString:@"EmployeeName"])
	{
		[rpt setEmployeeName:buildString];
	}	
	else if ([currentElement isEqualToString:@"EverSentBack"])
	{
		[rpt setEverSentBack:buildString];
	}	
	else if ([currentElement isEqualToString:@"HasException"])
	{
		[rpt setHasException:buildString];
	}	
	else if ([currentElement isEqualToString:@"SeverityLevel"])
	{
        if (inEntry)
            [rpt.entry setSeverityLevel:buildString];
        else
            [rpt setSeverityLevel:buildString];
	}	
	else if ([currentElement isEqualToString:@"LastComment"])
	{
		[rpt setLastComment:buildString];
	}	
	else if ([currentElement isEqualToString:@"PdfUrl"])
	{
		// Replace temp chars we used instead of "&amp;" token back
//		string = [string stringByReplacingOccurrencesOfString:@"|" withString:@"&"];
		
		//NSLog(@"buildString %@", buildString);
		[rpt setPdfUrl:buildString];
	}	
    else if ([currentElement isEqualToString:@"RealPdfUrl"])
	{
		// Replace temp chars we used instead of "&amp;" token back
        //		string = [string stringByReplacingOccurrencesOfString:@"|" withString:@"&"];
		
		//NSLog(@"buildString %@", buildString);
		[rpt setRealPdfUrl:buildString];
	}
	else if ([currentElement isEqualToString:@"ProcessInstanceKey"])
	{
		[rpt setProcessInstanceKey:buildString];
	}	
	else if ([currentElement isEqualToString:@"Purpose"])
	{
		[rpt setPurpose:buildString];
	}	
	else if ([currentElement isEqualToString:@"ReceiptImageAvailable"])
	{
		[rpt setReceiptImageAvailable:buildString];
	}	
	else if ([currentElement isEqualToString:@"ReceiptUrl"])
	{
		[rpt setReceiptUrl:buildString];
	}
	else if ([currentElement isEqualToString:@"ReceiptImageId"])
	{
		rpt.receiptImageId = buildString;
		[rpt.entry setReceiptImageId:buildString];
	}
	else if ([currentElement isEqualToString:@"ReportDate"])
	{
		[rpt setReportDate:buildString];
	}	
	else if ([currentElement isEqualToString:@"ReportName"])
	{
		[rpt setReportName:buildString];
	}	
	else if ([currentElement isEqualToString:@"RptKey"])
	{
		[rpt setRptKey:buildString];
        if (rpt.entry != nil)
            rpt.entry.rptKey = buildString;
	}	
	else if ([currentElement isEqualToString:@"StepKey"])
	{
		[rpt setStepKey:buildString];
	}	
	else if ([currentElement isEqualToString:@"TotalClaimedAmount"])
	{
		[rpt setTotalClaimedAmount:buildString];
	}	
	else if ([currentElement isEqualToString:@"TotalPostedAmount"])
	{
		[rpt setTotalPostedAmount:buildString];
	}
	else if ([currentElement isEqualToString:@"CurrentSequence"])
	{
		[rpt setCurrentSequence:buildString];
	}	
	else if ([currentElement isEqualToString:@"ApvStatusName"])
	{
		
		[rpt setApvStatusName:buildString];
	}	
	else if ([currentElement isEqualToString:@"PolKey"])
	{
		[rpt setPolKey:buildString];
	}
    else if ([currentElement isEqualToString:@"AprvEmpName"])
    {
        [rpt setAprvEmpName:buildString];
    }
	else if ([currentElement isEqualToString:@"StatKey"])
    {
        [rpt.curWorkflowAction setStatKey:buildString];
    }
    else if ([currentElement isEqualToString:@"ActionText"])
    {
        [rpt.curWorkflowAction setActionText:buildString];
    }
    else if ([currentElement isEqualToString:@"PrepForSubmitEmpKey"] )
    {
  		//MOB-11325
        [rpt setPrepForSubmitEmpKey:buildString];
    }
}

- (void)parserDidEndDocument:(NSXMLParser *)parser
{
	
}



@end



