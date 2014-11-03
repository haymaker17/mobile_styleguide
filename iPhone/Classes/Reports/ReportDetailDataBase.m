//
//  ReportDetailDataBase.m
//  ConcurMobile
//
//  Created by yiwen on 4/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReportDetailDataBase.h"
#import "MCLogging.h"
#import "FormFieldData.h"
#import "ExpenseTypesManager.h"


@implementation ReportDetailDataBase
@synthesize path, currentElement, rpts, rpt, keys, buildString, inReport, inEntry, inComment, inItemize, inAttendee, inException, reportNameBuildString;
@synthesize inFormField;
@synthesize roleCode, rptKey;
@synthesize inCompanyDisbursements, inEmployeeDisbursements;


static NSMutableDictionary* formFieldXmlToPropertyMap = nil;

+ (NSMutableDictionary*) getFormFieldXmlToPropertyMap
{
	return formFieldXmlToPropertyMap;
}

// Initialize msgId to msg class mapping here
+ (void)initialize
{
	if (self == [ReportDetailDataBase class]) 
	{
        // Perform initialization here.
		formFieldXmlToPropertyMap = [[NSMutableDictionary alloc] init];
		formFieldXmlToPropertyMap[@"Value"] = @"FieldValue";
		formFieldXmlToPropertyMap[@"Label"] = @"Label";
		formFieldXmlToPropertyMap[@"Id"] = @"ID";
		formFieldXmlToPropertyMap[@"DataType"] = @"DataType";
		formFieldXmlToPropertyMap[@"CtrlType"] = @"CtrlType";
		formFieldXmlToPropertyMap[@"ListKey"] = @"ListKey";
		formFieldXmlToPropertyMap[@"LiKey"] = @"LiKey";
		formFieldXmlToPropertyMap[@"LiCode"] = @"LiCode";
		formFieldXmlToPropertyMap[@"Required"] = @"Required";
		formFieldXmlToPropertyMap[@"MaxLength"] = @"MaxLength";
		formFieldXmlToPropertyMap[@"Access"] = @"Access";
		formFieldXmlToPropertyMap[@"ParFtCode"] = @"ParFtCode";
		formFieldXmlToPropertyMap[@"FtCode"] = @"FtCode";
		formFieldXmlToPropertyMap[@"ParLiKey"] = @"ParLiKey";
		formFieldXmlToPropertyMap[@"ParFieldId"] = @"ParFieldId";
		formFieldXmlToPropertyMap[@"IsCopyDownSourceForOtherForms"] = @"IsCopyDownSourceForOtherForms";
		formFieldXmlToPropertyMap[@"ItemCopyDownAction"] = @"ItemCopyDownAction";
		formFieldXmlToPropertyMap[@"CopyDownSource"] = @"CpDownSource";
		formFieldXmlToPropertyMap[@"CopyDownFormType"] = @"CpDownFormType";
		formFieldXmlToPropertyMap[@"DefaultValue"] = @"DefaultValue";
        formFieldXmlToPropertyMap[@"ValidationExpression"] = @"ValidationExpression"; 
        formFieldXmlToPropertyMap[@"FailureMsg"] = @"FailureMsg";
        formFieldXmlToPropertyMap[@"IsDynamicField"] = @"IsDynamicField";
        formFieldXmlToPropertyMap[@"FfKey"] = @"FormFieldKey";
        formFieldXmlToPropertyMap[@"OriginalCtrlType"] = @"OriginalCtrlType";
	}
}

- (void) updateFormField:(FormFieldData*)ff property:(NSString*)elementName value: (NSString*)propVal
{
	NSString* propName = formFieldXmlToPropertyMap[elementName];
	if (propName != nil)
	{
        // TODO: refactor using keyvalue encoding
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
        [ff performSelector:NSSelectorFromString([NSString stringWithFormat:@"set%@:", propName]) withObject:buildString];
#pragma clang diagnostic pop
	}
    else if ([elementName isEqualToString:@"HierKey"])
	{
		ff.hierKey = [propVal intValue];
	}
	else if ([elementName isEqualToString:@"HierLevel"])
	{
		ff.hierLevel = [propVal intValue];
	}
	else if ([elementName isEqualToString:@"ParHierLevel"])
	{
		ff.parHierLevel = [propVal intValue];
	}
	
}

- (NSString*) getReportElementName
{
	return @"ReportDetail";
}

-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	
	[self flushData];
	[self parseXMLFileAtData:data];
}


-(id)init
{
    self = [super init];
	if (self)
    {
        isInElement = @"NO";
        self.currentElement = @"";
        inEntry = NO;
        inReport = NO;
        
        self.rpts = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];	
        self.keys = [[NSMutableArray alloc] initWithObjects:nil];
        [self flushData];	
    }
	return self;
}


-(void) flushData
{
	[rpts removeAllObjects]; 	
	[keys removeAllObjects];
 	self.buildString = nil;
    self.reportNameBuildString = nil;
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
	if (inAttendee || inAtnColumns)
		elementName = [ReportDetailDataBase getUnqualifiedName:elementName]; // Remove namespace qualification from attendee sub-nodes
	
	self.currentElement = elementName;
	//[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::didStartElement currentElement = %@", currentElement] Level:MC_LOG_DEBU];
	isInElement = @"YES";
	
	self.buildString = [[NSMutableString alloc] init];
    self.reportNameBuildString = [[NSMutableString alloc] init];
	
	//	if ([currentElement isEqualToString:@"Entries"])
	//		[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::didStartElement ENTRIES currentElement = %@", currentElement] Level:MC_LOG_DEBU];
	//	
	//	if ([currentElement isEqualToString:@"Fields"] & !inCompanyDisbursements & !inEntry & !inItemize)
	//		[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::didStartElement RPT_FIELDS currentElement = %@", currentElement] Level:MC_LOG_DEBU];
	
	if ([elementName isEqualToString:[self getReportElementName]])
	{
		self.rpt = [[ReportData alloc] init];
		inReport = YES;
		inEntry = NO;
		inComment = NO;
		inItemize = NO;
		inCompanyDisbursements = NO;
		inFormField = NO;
		inAttendee = NO;
        inAtnColumns = NO;
	}
	else if ([elementName isEqualToString:@"ReportEntryDetail"])
	{
		inEntry = YES;
	}
	else if ([elementName isEqualToString:@"ReportComment"])
	{
		inComment = YES;
	}
	else if ([elementName isEqualToString:@"ItemizationDetail"])
	{
		inItemize = YES;
	}
	else if ([elementName isEqualToString:@"CompanyDisbursements"])
	{
		inCompanyDisbursements = YES;
	}
	else if ([elementName isEqualToString:@"FormField"])
	{
		inFormField = YES;
	}
	else if ([elementName isEqualToString:@"Attendee"])
	{
		inAttendee = YES;
	}
	else if ([elementName isEqualToString:@"CESException"])
	{
		inException = YES;
	}
	else if ([elementName isEqualToString:@"CompanyDisbursements"])
    {
        inCompanyDisbursements = YES;
    }
	else if ([elementName isEqualToString:@"EmployeeDisbursements"])
    {
        inEmployeeDisbursements = YES;
    }
    else if ([elementName isEqualToString:@"ColumnDefinitions"])
    {
        inAtnColumns = YES;
    }
    else if ([elementName isEqualToString:@"WorkflowActions"])
    {
        rpt.workflowActions = [[NSMutableArray alloc] init];
    }
    else if ([elementName isEqualToString:@"WorkflowAction"])
    {
        rpt.curWorkflowAction = [[WorkflowAction alloc] init];
    }
    //MOB-10862 - Support for dynamic tax fields
    else if ([elementName isEqualToString:@"TaxForms"])
    {
        //self.taxForms =
        self.rpt.entry.taxforms = [[TaxForms alloc] initWithName:elementName attributes:attributeDict parent:self children:nil parser:parser];;
    }


}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if (inAttendee || inAtnColumns)
		elementName = [ReportDetailDataBase getUnqualifiedName:elementName]; // Remove namespace qualification from attendee sub-nodes

	isInElement = @"NO";
	//[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::didEndElement currentElement = %@", currentElement] Level:MC_LOG_DEBU];
	if ([elementName isEqualToString:[self getReportElementName]])
	{
		rpts[rpt.rptKey] = rpt;
		[keys addObject:rpt.rptKey];
		//[rpt release];
		inReport = NO;
	}
	else if ([elementName isEqualToString:@"ReportEntryDetail"])
	{
        if ([self.rpt.entry.ereceiptId lengthIgnoreWhitespace] && ![self.rpt.entry.eReceiptImageId lengthIgnoreWhitespace])
            self.rpt.entry.eReceiptImageId = @"HACK e-receipt Image ID";

		//MOB-21147 - had to do it twice otherwise status is lost due to keyArchiver
        if (self.rpt.entry.eReceiptImageId != nil) {
            // MOB-21147 - Set hasReceipt
            // if its e-receipt then copy ereceipt image id to receiptimage id field. Also mark that we have a receipt
            [rpt.entry setHasMobileReceipt:@"Y"];
            [rpt.entry setReceiptImageId:self.rpt.entry.eReceiptImageId];
        }
  		[rpt finishEntry];
		inEntry = NO;

	}
	else if ([elementName isEqualToString:@"CompanyDisbursements"])
    {
        inCompanyDisbursements = NO;
    }
	else if ([elementName isEqualToString:@"EmployeeDisbursements"])
    {
        inEmployeeDisbursements = NO;
    }
    else if ([elementName isEqualToString:@"FormField"] && inAtnColumns == YES) // Not in attendee/entry, etc.
    {
        [rpt.entry finishAttendeeColumnDef];
        inFormField = NO;
    }
	else if ([elementName isEqualToString:@"FormField"] && inEntry == NO && inItemize == NO && inCompanyDisbursements == NO && inEmployeeDisbursements == NO)
	{
		[rpt finishField];
		inFormField = NO;
	}
	else if ([elementName isEqualToString:@"FormField"] && inEntry == NO && inItemize == NO && inCompanyDisbursements == YES)
	{
		[rpt finishCompanyDisbursementsField];
		inFormField = NO;
	}
	else if ([elementName isEqualToString:@"FormField"] && inEntry == NO && inItemize == NO && inEmployeeDisbursements == YES)
	{
		[rpt finishEmployeeDisbursementsField];
		inFormField = NO;
	}
	else if ([elementName isEqualToString:@"FormField"] && inEntry == YES & inItemize == NO & inCompanyDisbursements == NO & inAttendee == NO)
	{
		[rpt.entry finishField];
		inFormField = NO;
	}
	else if ([elementName isEqualToString:@"FormField"] && inEntry == YES & inItemize == NO & inCompanyDisbursements == NO & inAttendee)
	{
		[rpt.entry.attendee finishField];
		inFormField = NO;
	}
	else if ([elementName isEqualToString:@"ReportComment"] && inEntry == YES  & inItemize == NO)
	{
		[rpt.entry finishComment];
		inComment = NO;
	}
	else if ([elementName isEqualToString:@"FormField"] && inEntry == YES & inItemize == YES)
	{
		[rpt.entry.item finishField];
		inFormField = NO;
	}
	else if ([elementName isEqualToString:@"ReportComment"] && inEntry == YES  & inItemize == YES)
	{
		[rpt.entry.item finishComment];
		inComment = NO;
	}
	else if ([elementName isEqualToString:@"ReportComment"] && inItemize == NO & inEntry == NO)
	{
		[rpt finishComment];
		inComment = NO;
	}
	else if ([elementName isEqualToString:@"ReportComment"])
	{
		inComment = NO;
	}
	else if ([elementName isEqualToString:@"ItemizationDetail"])
	{
		[rpt.entry finishItemization];
		inItemize = NO;
	}
	else if ([elementName isEqualToString:@"CompanyDisbursements"])
	{
		inCompanyDisbursements = NO;
	}
	else if ([elementName isEqualToString:@"FormField"])
	{
		inFormField = NO;
		//[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::didEndElement DONE WITH FormField currentElement = %@", currentElement] Level:MC_LOG_DEBU];
	}
	else if ([elementName isEqualToString:@"Attendee"] && inEntry && inItemize ==NO && inAttendee)
	{
		inAttendee = NO;
		[rpt.entry finishAttendee];
	}
	else if ([elementName isEqualToString:@"Attendee"] && inEntry && inItemize && inAttendee)
	{
		inAttendee = NO;
		[rpt.entry.item finishAttendee];
	}
	else if ([elementName isEqualToString:@"CESException"] && inItemize == NO && inEntry == NO)
	{
		inException = NO;
		[rpt finishException];
	}
	else if ([elementName isEqualToString:@"CESException"] && inItemize == YES && inEntry == YES)
	{
		inException = NO;
		[rpt.entry.item finishException];
	}
	else if ([elementName isEqualToString:@"CESException"] && inItemize == NO && inEntry == YES)
	{
		inException = NO;
		[rpt.entry finishException];
	}
    else if ([elementName isEqualToString:@"ColumnDefinitions"])
    {
        inAtnColumns = NO;
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
	[buildString appendString:string];
	
	/*
	if (inAttendee) 
	{
		// MOB-3572: This line causes a crash that was reported by customers.  See Jira for details.
		//[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::foundCharacters inAttendee currentElement = %@, string = %@", currentElement, string] Level:MC_LOG_DEBU];
	}
	*/
	
	
	//[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::foundCharacters currentElement = %@, string = %@", currentElement, string] Level:MC_LOG_DEBU];
	
	if (inEntry == NO & inItemize == NO & inAttendee == NO)
	{
		if ([currentElement isEqualToString:@"CrnCode"])
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
		else if ([currentElement isEqualToString:@"SeverityLevel"] && inException == NO)
		{
			[rpt setSeverityLevel:buildString];
		}	
		else if ([currentElement isEqualToString:@"LastComment"])
		{
			[rpt setLastComment:buildString];
		}	
		else if ([currentElement isEqualToString:@"CurrentSequence"])
		{
			[rpt setCurrentSequence:buildString];
		}	
		else if ([currentElement isEqualToString:@"PdfUrl"])
		{
			// Replace temp chars we used instead of "&amp;" token back
			string = [string stringByReplacingOccurrencesOfString:@"|" withString:@"&"];
			
			[rpt setPdfUrl:buildString];
		}	
        else if ([currentElement isEqualToString:@"RealPdfUrl"])
		{
			// Replace temp chars we used instead of "&amp;" token back
			string = [string stringByReplacingOccurrencesOfString:@"|" withString:@"&"];
			
			[rpt setRealPdfUrl:buildString];
		}
		else if ([currentElement isEqualToString:@"ProcessInstanceKey"])
		{
			[rpt setProcessInstanceKey:buildString];
		}	
		else if ([currentElement isEqualToString:@"Purpose"] && inComment == NO & inEntry == NO)
		{
			[rpt setPurpose:buildString];
		}	
		else if ([currentElement isEqualToString:@"ReceiptImageAvailable"])
		{
			[rpt setReceiptImageAvailable:buildString];
		}	
		else if ([currentElement isEqualToString:@"ImageRequired"])
		{
			[rpt setImageRequired:buildString];
		}	
		else if ([currentElement isEqualToString:@"ReceiptUrl"])
		{
			[rpt setReceiptUrl:buildString];
		}	
		else if ([currentElement isEqualToString:@"ReportDate"])
		{
			[rpt setReportDate:buildString];
		}	
		else if ([currentElement isEqualToString:@"FormKey"])
		{
			[rpt setFormKey:buildString];
		}	
		else if ([currentElement isEqualToString:@"ReportName"])
		{
			[rpt setReportName:buildString];
		}	
		else if ([currentElement isEqualToString:@"EnableRecall"])
		{
			[rpt setEnableRecall:buildString];
		}	
		else if ([currentElement isEqualToString:@"RptKey"] && inComment == NO & inEntry == NO  & inItemize == NO)
		{
			[rpt setRptKey:buildString];
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
		else if ([currentElement isEqualToString:@"TotalApprovedAmount"])
		{
			[rpt setTotalApprovedAmount:buildString];
		}
		else if ([currentElement isEqualToString:@"TotalDueCompany"])
		{
			[rpt setTotalDueCompany:buildString];
		}
		else if ([currentElement isEqualToString:@"TotalDueCompanyCard"])
		{
			[rpt setTotalDueCompanyCard:buildString];
		}
		else if ([currentElement isEqualToString:@"TotalDueEmployee"])
		{
			[rpt setTotalDueEmployee:buildString];
		}
		else if ([currentElement isEqualToString:@"TotalOwedByEmployee"])
		{
			[rpt setTotalOwedByEmployee:buildString];
		}
		else if ([currentElement isEqualToString:@"TotalPaidByCompany"])
		{
			[rpt setTotalPaidByCompany:buildString];
		}
		else if ([currentElement isEqualToString:@"TotalPersonalAmount"])
		{
			[rpt setTotalPersonalAmount:buildString];
		}
		else if ([currentElement isEqualToString:@"TotalRejectedAmount"])
		{
			[rpt setTotalRejectedAmount:buildString];
		}
		else if ([currentElement isEqualToString:@"TotalPostedAmount"])
		{
			[rpt setTotalPostedAmount:buildString];
		}
		else if ([currentElement isEqualToString:@"ApsKey"])
		{
			[rpt setApsKey:buildString];
		}	
		else if ([currentElement isEqualToString:@"PayKey"])
		{
			[rpt setPayKey:buildString];
		}
		else if ([currentElement isEqualToString:@"PolKey"])
		{
			[rpt setPolKey:buildString];
		}	
		else if ([currentElement isEqualToString:@"ApvStatusName"])
		{
			
			[rpt setApvStatusName:buildString];
		}		
        else if ([currentElement isEqualToString:@"StatKey"])
        {
            [rpt.curWorkflowAction setStatKey:buildString];
        }
        else if ([currentElement isEqualToString:@"ActionText"])
        {
            [rpt.curWorkflowAction setActionText:buildString];
        }
		else if ([currentElement isEqualToString:@"SeverityLevel"] && inException == NO)
		{
			[rpt.exception setSeverityLevel:buildString];
		}		
		else if ([currentElement isEqualToString:@"Comment"] && inEntry == NO & inComment == YES & inItemize == NO)
		{
			
			[rpt.comment setComment:buildString];
		}
		else if ([currentElement isEqualToString:@"CommentBy"] && inEntry == NO & inComment == YES & inItemize == NO)
		{
			[rpt.comment setCommentBy:buildString];
		}
		else if ([currentElement isEqualToString:@"CommentKey"] && inEntry == NO & inComment == YES & inItemize == NO)
		{
			[rpt.comment setCommentKey:buildString];
		}
		else if ([currentElement isEqualToString:@"CreationDate"] && inEntry == NO & inComment == YES & inItemize == NO)
		{
			[rpt.comment setCreationDate:buildString];
		}
		else if ([currentElement isEqualToString:@"IsLatest"] && inEntry == NO & inComment == YES & inItemize == NO)
		{
			[rpt.comment setIsLatest:buildString];
		}	
		else if ([currentElement isEqualToString:@"ExceptionsStr"] && inEntry == NO & inException == YES & inItemize == NO)
		{
			[rpt.exception setExceptionsStr:buildString];
		}
		else if ([currentElement isEqualToString:@"SeverityLevel"] && inEntry == NO & inException == YES & inItemize == NO)
		{
			[rpt.exception setSeverityLevel:buildString];
		}
        else if ([currentElement isEqualToString:@"PrepForSubmitEmpKey"] )
		{
            //MOB-11325
            [rpt setPrepForSubmitEmpKey:buildString];
         }
		
		if(inFormField && inAttendee == NO)
		{
			//			if (!inCompanyDisbursements & !inEntry & !inItemize)
			//				[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::foundCharacters IN RPT FIELD HANDLING currentElement = %@, string = %@", currentElement, string] Level:MC_LOG_DEBU];
			[self updateFormField:rpt.field property:currentElement value:string];
		}
	}
	
	if (inEntry == YES & inItemize == NO & inAttendee == NO)
		[self fillEntry:buildString];
	else if (inEntry == YES & inItemize == YES & inAttendee == NO)
		[self fillItemization:buildString];
	else if (inEntry == YES & inItemize == NO & inAttendee)
		[self fillAttendee:buildString entry:rpt.entry];
	else if (inEntry == YES & inItemize == YES & inAttendee)
		[self fillAttendee:buildString entry:rpt.entry.item];
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	
}


#pragma mark Entry Nodes
-(void)fillEntry:(NSString *)string
{
	if ([currentElement isEqualToString:@"ApprovedAmount"] && inItemize == NO)
	{
		[rpt.entry setApprovedAmount:string];
	}
	else if ([currentElement isEqualToString:@"ExpName"] && inItemize == NO)
	{
		[rpt.entry setExpName:string];
	}
	else if ([currentElement isEqualToString:@"ExpKey"] && inItemize == NO)
	{
		[rpt.entry setExpKey:string];
	}
	else if ([currentElement isEqualToString:@"HasAllocation"] && inItemize == NO)
	{
		[rpt.entry setHasAllocation:string];
	}
	else if ([currentElement isEqualToString:@"HasAttendees"] && inItemize == NO)
	{
		[rpt.entry setHasAttendees:string];
	}
	else if ([currentElement isEqualToString:@"HasAttendeesField"] && inItemize== NO)
	{
		[rpt.entry setHasAttendeesField:string];
	}
	else if ([currentElement isEqualToString:@"FormKey"] && inItemize == NO)
	{
		[rpt.entry setFormKey:string];
	}
	else if ([currentElement isEqualToString:@"HasComments"] && inItemize == NO)
	{
		[rpt.entry setHasComments:string];
	}
	//	else if ([currentElement isEqualToString:@"TransactionDate"])
	//	{
	//		NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
	//		[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
	//		NSDate *dt = [dateFormatter dateFromString:string];
	//		[oope setTranDate:dt]; 
	//		[dateFormatter release];
	//	}
	else if ([currentElement isEqualToString:@"HasExceptions"] && inItemize == NO)
	{
		[rpt.entry setHasExceptions:string];
	}
	else if ([currentElement isEqualToString:@"ParentRpeKey"] && inComment == NO)
	{
		[rpt.entry setParentRpeKey:string];
	}		
	else if ([currentElement isEqualToString:@"IsCreditCardCharge"] && inItemize == NO)
	{
		[rpt.entry setIsCreditCardCharge:string];
	}
	else if ([currentElement isEqualToString:@"IsPersonalCardCharge"] && inItemize == NO)
	{
		[rpt.entry setIsPersonalCardCharge:string];
	}
	else if ([currentElement isEqualToString:@"IsItemized"] && inItemize == NO)
	{
		[rpt.entry setIsItemized:string];
	}
	else if ([currentElement isEqualToString:@"IsPersonal"] && inItemize == NO)
	{
		[rpt.entry setIsPersonal:string]; 
	}		
	else if ([currentElement isEqualToString:@"LocationName"] && inItemize == NO)
	{
		//NSLog(@"LocationName entry %@", string);
		[rpt.entry setLocationName:string];
	}	
	else if ([currentElement isEqualToString:@"RpeKey"] && inComment == NO && inItemize == NO)
	{
		[rpt.entry setRpeKey:string];
	}	
	else if ([currentElement isEqualToString:@"RptKey"] && inComment == NO && inItemize == NO)
	{
		[rpt.entry setRptKey:string];
	}	
	else if ([currentElement isEqualToString:@"TransactionAmount"] && inItemize == NO)
	{
		[rpt.entry setTransactionAmount:string];
	}	
	else if ([currentElement isEqualToString:@"TransactionCrnCode"] && inItemize == NO)
	{
		[rpt.entry setTransactionCrnCode:string];
	}	
	else if ([currentElement isEqualToString:@"TransactionDate"] && inItemize == NO)
	{
		[rpt.entry setTransactionDate:string];
	}	
	else if ([currentElement isEqualToString:@"VendorDescription"] && inItemize == NO)
	{
		//NSLog(@"vendor %@", buildString);
		[rpt.entry setVendorDescription:string];
	}	
	else if ([currentElement isEqualToString:@"NoShowCount"] && inItemize == NO)
	{
		//NSLog(@"vendor %@", buildString);
		[rpt.entry setNoShowCount:string];
	}	
	else if ([currentElement isEqualToString:@"HasMobileReceipt"])
	{
		[rpt.entry setHasMobileReceipt:string];
	}
	else if ([currentElement isEqualToString:@"HasTravelAllowance"])
	{
        [rpt.entry setHasTravelAllowance:string];
	}
	else if ([currentElement isEqualToString:@"TaDayKey"])
	{
        [rpt.entry setTaDayKey:string];
	}
	else if ([currentElement isEqualToString:@"ReceiptRequired"])
	{
		[rpt.entry setReceiptRequired:string];
	}
	else if ([currentElement isEqualToString:@"ReceiptImageId"])
	{
		rpt.receiptImageId = string;
		[rpt.entry setReceiptImageId:string];
	}
	else if ([currentElement isEqualToString:@"EreceiptId"])
	{
		[rpt.entry setEreceiptId:string];
	}
    else if ([currentElement isEqualToString:@"EreceiptImageId"])
    {
        [rpt.entry setEReceiptImageId:string];
        // MOB-21147 - Set hasReceipt
        // if its e-receipt then copy ereceipt image id to receiptimage id field. Also mark that we have a receipt
        [rpt.entry setHasMobileReceipt:@"Y"];
        [rpt.entry setReceiptImageId:self.rpt.entry.eReceiptImageId];

    }
	else if ([currentElement isEqualToString:@"ImageRequired"])
	{
		[rpt.entry setImageRequired:string];
	}
	else if ([currentElement isEqualToString:@"MeKey"])
	{
		[rpt.entry setMeKey:string];
	}
	else if ([currentElement isEqualToString:@"CctType"])
	{
		[rpt.entry setCctType:string];
	}
	else if ([currentElement isEqualToString:@"SeverityLevel"] && inEntry == YES && inException == NO && inItemize == NO)
	{
		[rpt.entry setSeverityLevel:string];
	}
	else if ([currentElement isEqualToString:@"Comment"] && inEntry == YES && inComment == YES && inItemize == NO)
	{
		[rpt.entry.comment setComment:string];
	}
	else if ([currentElement isEqualToString:@"CommentBy"] && inEntry == YES && inComment == YES && inItemize == NO)
	{
		[rpt.entry.comment setCommentBy:string];
	}
	else if ([currentElement isEqualToString:@"CommentKey"] && inEntry == YES && inComment == YES && inItemize == NO)
	{
		[rpt.entry.comment setCommentKey:string];
	}
	else if ([currentElement isEqualToString:@"CreationDate"] && inEntry == YES && inComment == YES && inItemize == NO)
	{
		[rpt.entry.comment setCreationDate:string];
	}
	else if ([currentElement isEqualToString:@"IsLatest"] && inEntry == YES && inComment == YES && inItemize == NO)
	{
		[rpt.entry.comment setIsLatest:string];
	}
	else if ([currentElement isEqualToString:@"RpeKey"] && inEntry == YES && inComment == YES && inItemize == NO)
	{
		[rpt.entry.comment setRpeKey:string];
	}
	else if ([currentElement isEqualToString:@"RptKey"] && inEntry == YES && inComment == YES && inItemize == NO)
	{
		[rpt.entry.comment setRptKey:string];
	}
	
	else if ([currentElement isEqualToString:@"ExceptionsStr"] && inEntry == YES && inException == YES && inItemize == NO)
	{
		[rpt.entry.exception setExceptionsStr:string];
	}
	else if ([currentElement isEqualToString:@"SeverityLevel"] && inEntry == YES && inException == YES && inItemize == NO)
	{
		[rpt.entry.exception setSeverityLevel:string];
	}
	
	if(inCompanyDisbursements == NO && inEntry == YES && inItemize == NO)
	{
		[self updateFormField:rpt.entry.field property:currentElement value:string];
	}
	
}


#pragma mark Itemization Nodes
-(void)fillItemization:(NSString *)string
{
	if ([currentElement isEqualToString:@"ApprovedAmount"] )
	{
		[rpt.entry.item setApprovedAmount:string];
	}
	else if ([currentElement isEqualToString:@"ExpName"])
	{
		[rpt.entry.item setExpName:string];
	}
	else if ([currentElement isEqualToString:@"ExpKey"])
	{
		[rpt.entry.item setExpKey:string];
	}
	else if ([currentElement isEqualToString:@"HasAllocation"])
	{
		[rpt.entry.item setHasAllocation:string];
	}
	else if ([currentElement isEqualToString:@"HasAttendees"])
	{
		[rpt.entry.item setHasAttendees:string];
	}
	else if ([currentElement isEqualToString:@"FormKey"])
	{
		[rpt.entry.item setFormKey:string];
	}
	else if ([currentElement isEqualToString:@"HasComments"])
	{
		[rpt.entry.item setHasComments:string];
	}
	else if ([currentElement isEqualToString:@"HasExceptions"])
	{
		[rpt.entry.item setHasExceptions:string];
	}
	else if ([currentElement isEqualToString:@"IsCreditCardCharge"])
	{
		[rpt.entry.item setIsCreditCardCharge:string];
	}
	else if ([currentElement isEqualToString:@"IsItemized"])
	{
		[rpt.entry.item setIsItemized:string];
	}
	else if ([currentElement isEqualToString:@"IsPersonal"])
	{
		[rpt.entry.item setIsPersonal:string]; 
	}		
	else if ([currentElement isEqualToString:@"LocationName"])
	{
		//NSLog(@"LocationName itemized %@", string);
		[rpt.entry.item setLocationName:string];
	}	
	else if ([currentElement isEqualToString:@"RpeKey"] && inComment == NO)
	{
		[rpt.entry.item setRpeKey:string];
		[rpt.entry.item setParentRpeKey:rpt.entry.rpeKey];
	}	
	else if ([currentElement isEqualToString:@"RptKey"] && inComment == NO)
	{
		[rpt.entry.item setRptKey:string];
	}	
	else if ([currentElement isEqualToString:@"TransactionAmount"])
	{
		[rpt.entry.item setTransactionAmount:string];
	}	
	else if ([currentElement isEqualToString:@"TransactionCrnCode"])
	{
		[rpt.entry.item setTransactionCrnCode:string];
	}	
	else if ([currentElement isEqualToString:@"TransactionDate"])
	{
		[rpt.entry.item setTransactionDate:string];
	}	
	else if ([currentElement isEqualToString:@"VendorDescription"])
	{
		
		//NSLog(@"vendor not itemized %@", buildString);
		[rpt.entry.item setVendorDescription:string];
	}	
	else if ([currentElement isEqualToString:@"NoShowCount"])
	{
		[rpt.entry.item setNoShowCount:string];
	}	
	else if ([currentElement isEqualToString:@"Comment"] && inComment == YES)
	{
		[rpt.entry.item.comment setComment:string];
	}
	else if ([currentElement isEqualToString:@"CommentBy"] && inComment == YES)
	{
		[rpt.entry.item.comment setCommentBy:string];
	}
	else if ([currentElement isEqualToString:@"CommentKey"] && inComment == YES)
	{
		[rpt.entry.item.comment setCommentKey:string];
	}
	else if ([currentElement isEqualToString:@"CreationDate"] && inComment == YES)
	{
		[rpt.entry.item.comment setCreationDate:string];
	}
	else if ([currentElement isEqualToString:@"IsLatest"] && inComment == YES)
	{
		[rpt.entry.item.comment setIsLatest:string];
	}
	else if ([currentElement isEqualToString:@"RpeKey"] && inComment == YES)
	{
		[rpt.entry.item.comment setRpeKey:string];
	}
	else if ([currentElement isEqualToString:@"RptKey"] && inComment == YES)
	{
		[rpt.entry.item.comment setRptKey:string];
	}
	
	else if ([currentElement isEqualToString:@"ExceptionsStr"] && inEntry == YES && inException == YES && inItemize == YES)
	{
		[rpt.entry.item.exception setExceptionsStr:string];
	}
	else if ([currentElement isEqualToString:@"SeverityLevel"] && inEntry == YES && inException == YES && inItemize == YES)
	{
		[rpt.entry.item.exception setSeverityLevel:string];
	}	
	
	if(inCompanyDisbursements == NO)
	{
		[self updateFormField:rpt.entry.item.field property:currentElement value:string];
	}
}


#pragma mark Attendee Nodes
-(void)fillAttendee:(NSString *)string entry:(EntryData*) entry
{
	if ([currentElement isEqualToString:@"Amount"] )
	{
		[entry.attendee setAmount:string];
	}
	if ([currentElement isEqualToString:@"IsAmountEdited"] )
	{
		[entry.attendee setIsAmountEdited:(string != nil && [string isEqualToString:@"Y"])];
	}
	else if ([currentElement isEqualToString:@"AtnKey"])
	{
		[entry.attendee setAttnKey:string];
	}
	else if ([currentElement isEqualToString:@"VersionNumber"])
	{
		[entry.attendee setVersionNumber:string];
	}
	else if ([currentElement isEqualToString:@"CurrentVersionNumber"])
	{
		[entry.attendee setCurrentVersionNumber:string];
	}
	else if ([currentElement isEqualToString:@"AtnTypeName"])
	{
		[entry.attendee setFieldId:@"AtnTypeName" value:string];
	}
	else if ([currentElement isEqualToString:@"AtnTypeKey"])
	{
		[entry.attendee setAtnTypeKey:string];
	}
	else if ([currentElement isEqualToString:@"InstanceCount"])
	{
		entry.attendee.instanceCount = [string intValue];
	}
//	else if ([currentElement isEqualToString:@"Name"])
//	{
//		[entry.attendee setFieldId:@"Name" value:string];
//	}
	else if ([currentElement isEqualToString:@"FirstName"])
	{
		[entry.attendee setFieldId:@"FirstName" value:string];
	}
	else if ([currentElement isEqualToString:@"LastName"])
	{
		[entry.attendee setFieldId:@"LastName" value:string];
	}
	else if ([currentElement isEqualToString:@"Company"])
	{
		[entry.attendee setFieldId:@"Company" value:string];
	}
	else if ([currentElement isEqualToString:@"Title"])
	{
		[entry.attendee setFieldId:@"Title" value:string];
	}
    else if ([currentElement isEqualToString:@"ExternalId"])
    {
        [entry.attendee setFieldId:@"ExternalId" value:string];
    }
	
	
	if(inFormField)
	{
		[self updateFormField:entry.attendee.field property:currentElement value:string];
	}
}

#pragma mark -
#pragma Helper Methods
+(NSString*) getUnqualifiedName:(NSString*)qualifiedName
{
	NSString *unqualifiedName = qualifiedName;
	
	NSArray* nameComponents = [qualifiedName componentsSeparatedByString:@":"];
	if ([nameComponents count] > 1)
	{
		unqualifiedName = nameComponents[([nameComponents count] - 1)];
	}
	
	return unqualifiedName;
}

// the expense key and policy key need to be provided from an external source.
- (void)checkIfServerHandlesAmountsForExpenseKey:(NSString *)expenseKey withPolicy:(NSString *)policyKey;
{
    // set the amount to RO and not required, when the server will handle calculations
    if (rpt.entry != nil) {
        
        BOOL isChild = rpt.entry.isChild;
        BOOL isPersonal = (rpt.entry.isPersonal != nil && [rpt.entry.isPersonal isEqualToString:@"Y"]);
        BOOL isCreditCardCharge = (rpt.entry.isCreditCardCharge != nil && [rpt.entry.isCreditCardCharge isEqualToString:@"Y"]);
        BOOL isPersonalCardCharge = (rpt.entry.isPersonalCardCharge != nil && [rpt.entry.isPersonalCardCharge isEqualToString:@"Y"]);
        BOOL isCash = NO;
        
        // need payment type
        for (NSString* key in rpt.entry.fieldKeys)
        {
            FormFieldData* fld = (rpt.entry.fields)[key];
            if ([fld.iD isEqualToString:@"PatKey"])
            {
                isCash = (fld.liKey != nil && [fld.liKey isEqualToString:@"CASH"]);
                break;
            }
        }
        
        // change the status to not required and read only
        if (!isPersonal && !isPersonalCardCharge && !isCreditCardCharge && !isChild && isCash) {
            
            // does the server handle amount calculations?
            ExpenseTypeData *expenseTypeData = [[ExpenseTypesManager sharedInstance] expenseTypeForVersion:@"V3" policyKey:policyKey expenseKey:expenseKey forChild:NO];
            if ([expenseTypeData serverDoesPostAmountCalculation]) {
                for (NSString* key in rpt.entry.fieldKeys)
                {
                    FormFieldData* fld = (rpt.entry.fields)[key];
                    if ([fld.iD isEqualToString:@"TransactionAmount"] || [fld.iD isEqualToString:@"PostedAmount"])
                    {
                        fld.required = @"N";
                        fld.access = @"RO";
                    }
                }
            }
        }
    }
}


#pragma mark -
#pragma End of Lifecycle

@end
