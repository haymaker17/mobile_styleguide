//
//  ReportData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReportData.h"


@implementation ReportData

@synthesize		apsKey, payKey, crnCode, employeeName, everSentBack, hasException, lastComment, pdfUrl, processInstanceKey, purpose
, receiptImageAvailable, receiptUrl, reportDate, reportName, rptKey, stepKey, totalClaimedAmount, totalPostedAmount, entry, currentSequence, prepForSubmitEmpKey;
@synthesize imageRequired;
@synthesize apvStatusName;
@synthesize severityLevel;
@synthesize polKey;
@synthesize formKey;
@synthesize totalApprovedAmount;
@synthesize totalDueCompany;
@synthesize totalDueCompanyCard;
@synthesize totalDueEmployee;
@synthesize totalOwedByEmployee;
@synthesize totalPaidByCompany;
@synthesize totalPersonalAmount;
@synthesize totalRejectedAmount;

@synthesize fields;
@synthesize comments;
@synthesize commentKeys;
@synthesize fieldKeys, comment;

@synthesize		entries;
@synthesize		keys, field, exception, exceptions;
@synthesize		receiptImageId;
@synthesize realPdfUrl;

@synthesize companyDisbursements, employeeDisbursements;

@synthesize enableRecall;
@synthesize aprvEmpName;
@synthesize workflowActions, curWorkflowAction;

-(id)init
{
    self = [super init];
	if (self)
    {
        self.entries = [[NSMutableDictionary alloc] init];
        self.entry = [[EntryData alloc] init];
        self.keys = [[NSMutableArray alloc] init];
        
        self.fields = [[NSMutableDictionary alloc] init];
        self.field = [[FormFieldData alloc] init];
        //NSLog(@"1 field address %d", &field);
        self.fieldKeys = [[NSMutableArray alloc] init];
        
        self.comments = [[NSMutableDictionary alloc] init];
        self.comment = [[CommentData alloc] init];
        self.commentKeys = [[NSMutableArray alloc] init];
        
        self.exception = [[ExceptionData alloc] init];
        self.exceptions = [[NSMutableArray alloc] init];
        
        self.companyDisbursements = [[NSMutableArray alloc] init];
        self.employeeDisbursements = [[NSMutableArray alloc] init];
    }
	return self;
}

-(void)finishCompanyDisbursementsField
{
    if (field != nil && field.label != nil)
    {
        [companyDisbursements addObject:field];
        self.field = [[FormFieldData alloc] init];
    }
}

-(void)finishEmployeeDisbursementsField
{
    if (field != nil && field.label != nil)
    {
        [employeeDisbursements addObject:field];
        self.field = [[FormFieldData alloc] init];
    }
}

-(void)finishWorkflowAction
{
    if (curWorkflowAction != nil && curWorkflowAction.statKey != nil)
    {
        [workflowActions addObject:curWorkflowAction];
        self.curWorkflowAction = nil;
    }
}

-(void)finishEntry
{
	if (entry != nil && entry.rpeKey != nil) 
	{
		entries[entry.rpeKey] = entry;
		[keys addObject:entry.rpeKey];

		self.entry = [[EntryData alloc] init];
	}
}


-(void)finishField
{
	if (field.fieldValue == nil)
		field.fieldValue = @"";
	
	if (field != nil && field.iD != nil && field.fieldValue != nil) 
	{
		fields[field.iD] = field;
		[fieldKeys addObject:field.iD];
        
		self.field = [[FormFieldData alloc] init];
	}
	else if (field != nil)
	{
		self.field = [[FormFieldData alloc] init];
	}
}


-(void)finishComment
{
	if (comment != nil) 
	{
		if (comment.commentKey == nil)
			comment.commentKey = @"";
		comments[comment.commentKey] = comment;
		[commentKeys addObject:comment.commentKey];

		self.comment = [[CommentData alloc] init];
	}
}


-(void)finishException
{
	if (exception != nil) 
	{
		[exceptions addObject:exception];

		self.exception = [[ExceptionData alloc] init];
	}
}


#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
	[coder encodeObject:fieldKeys	forKey:@"fieldKeys"];
	[coder encodeObject:fields	forKey:@"fields"];
	[coder encodeObject:commentKeys	forKey:@"commentKeys"];
	[coder encodeObject:comments	forKey:@"comments"];
	[coder encodeObject:exceptions	forKey:@"exceptions"];
	[coder encodeObject:severityLevel	forKey:@"severityLevel"];

    [coder encodeObject:apsKey	forKey:@"apsKey"];
	[coder encodeObject:payKey	forKey:@"payKey"];
    [coder encodeObject:crnCode	forKey:@"crnCode"];
    [coder encodeObject:employeeName	forKey:@"employeeName"];
    [coder encodeObject:everSentBack	forKey:@"everSentBack"];
    [coder encodeObject:hasException	forKey:@"hasException"];
    [coder encodeObject:keys	forKey:@"keys"];
    [coder encodeObject:entries	forKey:@"entries"];
    [coder encodeObject:lastComment	forKey:@"lastComment"];
    [coder encodeObject:pdfUrl	forKey:@"pdfUrl"];
    [coder encodeObject:polKey	forKey:@"polKey"];
    [coder encodeObject:processInstanceKey	forKey:@"processInstanceKey"];
    [coder encodeObject:purpose	forKey:@"purpose"];
    [coder encodeObject:receiptImageAvailable	forKey:@"receiptImageAvailable"];
	[coder encodeObject:receiptImageId	forKey:@"receiptImageId"];
    [coder encodeObject:receiptUrl	forKey:@"receiptUrl"];
    [coder encodeObject:realPdfUrl	forKey:@"realPdfUrl"];
    [coder encodeObject:reportDate	forKey:@"reportDate"];
    [coder encodeObject:reportName	forKey:@"reportName"];
    [coder encodeObject:rptKey	forKey:@"rptKey"];
    [coder encodeObject:stepKey	forKey:@"stepKey"];
    [coder encodeObject:totalClaimedAmount	forKey:@"totalClaimedAmount"];
    [coder encodeObject:totalPostedAmount	forKey:@"totalPostedAmount"];
    [coder encodeObject:currentSequence	forKey:@"currentSequence"];
    [coder encodeObject:imageRequired	forKey:@"imageRequired"];
    [coder encodeObject:apvStatusName	forKey:@"apvStatusName"];
    [coder encodeObject:totalApprovedAmount	forKey:@"totalApprovedAmount"];
    [coder encodeObject:totalDueCompany	forKey:@"totalDueCompany"];
    [coder encodeObject:totalDueCompanyCard	forKey:@"totalDueCompanyCard"];
    [coder encodeObject:totalDueEmployee	forKey:@"totalDueEmployee"];
    [coder encodeObject:totalOwedByEmployee	forKey:@"totalOwedByEmployee"];
    [coder encodeObject:totalPaidByCompany	forKey:@"totalPaidByCompany"];
    [coder encodeObject:totalPersonalAmount	forKey:@"totalPersonalAmount"];
    [coder encodeObject:totalRejectedAmount	forKey:@"totalRejectedAmount"];
    
    [coder encodeObject:companyDisbursements forKey:@"companyDisbursements"];
    [coder encodeObject:employeeDisbursements forKey:@"employeeDisbursements"];
    [coder encodeObject:enableRecall        forKey:@"enableRecall"];
    [coder encodeObject:aprvEmpName         forKey:@"aprvEmpName"];
    [coder encodeObject:prepForSubmitEmpKey forKey:@"prepForSubmitEmpKey"];
    [coder encodeObject:workflowActions     forKey:@"workflowActions"];
}

- (id)initWithCoder:(NSCoder *)coder {
	
	
	self.fieldKeys = [coder decodeObjectForKey:@"fieldKeys"];
	self.fields = [coder decodeObjectForKey:@"fields"];
	self.commentKeys = [coder decodeObjectForKey:@"commentKeys"];
	self.comments = [coder decodeObjectForKey:@"comments"];
	self.exceptions = [coder decodeObjectForKey:@"exceptions"];
	self.severityLevel = [coder decodeObjectForKey:@"severityLevel"];

	self.apsKey = [coder decodeObjectForKey:@"apsKey"];
	self.payKey = [coder decodeObjectForKey:@"payKey"];
	self.crnCode = [coder decodeObjectForKey:@"crnCode"];
	self.employeeName = [coder decodeObjectForKey:@"employeeName"];
	self.everSentBack = [coder decodeObjectForKey:@"everSentBack"];
	self.hasException = [coder decodeObjectForKey:@"hasException"];
	self.keys = [coder decodeObjectForKey:@"keys"];
	self.entries = [coder decodeObjectForKey:@"entries"];
	self.lastComment = [coder decodeObjectForKey:@"lastComment"];
	self.pdfUrl = [coder decodeObjectForKey:@"pdfUrl"];
	self.polKey = [coder decodeObjectForKey:@"polKey"];
	self.processInstanceKey = [coder decodeObjectForKey:@"processInstanceKey"];
	self.purpose = [coder decodeObjectForKey:@"purpose"];
	self.receiptImageAvailable = [coder decodeObjectForKey:@"receiptImageAvailable"];
	self.receiptImageId	= [coder decodeObjectForKey:@"receiptImageId"];
	self.receiptUrl = [coder decodeObjectForKey:@"receiptUrl"];
    self.realPdfUrl = [coder decodeObjectForKey:@"realPdfUrl"];
	self.reportDate = [coder decodeObjectForKey:@"reportDate"];
	self.reportName = [coder decodeObjectForKey:@"reportName"];
	self.rptKey = [coder decodeObjectForKey:@"rptKey"];
	self.stepKey = [coder decodeObjectForKey:@"stepKey"];
	self.totalClaimedAmount = [coder decodeObjectForKey:@"totalClaimedAmount"];
	self.totalPostedAmount = [coder decodeObjectForKey:@"totalPostedAmount"];
	self.currentSequence = [coder decodeObjectForKey:@"currentSequence"];
	self.imageRequired = [coder decodeObjectForKey:@"imageRequired"];
	self.apvStatusName = [coder decodeObjectForKey:@"apvStatusName"];
	self.totalApprovedAmount = [coder decodeObjectForKey:@"totalApprovedAmount"];
	self.totalDueCompany = [coder decodeObjectForKey:@"totalDueCompany"];
	self.totalDueCompanyCard = [coder decodeObjectForKey:@"totalDueCompanyCard"];
	self.totalDueEmployee = [coder decodeObjectForKey:@"totalDueEmployee"];
	self.totalOwedByEmployee = [coder decodeObjectForKey:@"totalOwedByEmployee"];
	self.totalPaidByCompany = [coder decodeObjectForKey:@"totalPaidByCompany"];
	self.totalPersonalAmount = [coder decodeObjectForKey:@"totalPersonalAmount"];
	self.totalRejectedAmount = [coder decodeObjectForKey:@"totalRejectedAmount"];
    
    self.companyDisbursements = [coder decodeObjectForKey:@"companyDisbursements"];
	self.employeeDisbursements = [coder decodeObjectForKey:@"employeeDisbursements"];
    self.enableRecall = [coder decodeObjectForKey:@"enableRecall"];
    self.aprvEmpName = [coder decodeObjectForKey:@"aprvEmpName"];
    self.prepForSubmitEmpKey = [coder decodeObjectForKey:@"prepForSubmitEmpKey"];
    self.workflowActions = [coder decodeObjectForKey:@"workflowActions"];
    return self;
}

-(void) copyHeaderDetail:(ReportData*) header
{
	self.fields = header.fields;
	self.fieldKeys = header.fieldKeys;
	self.comments = header.comments;
	self.commentKeys = header.commentKeys;
	self.exceptions = header.exceptions;
	if (header.severityLevel != nil)	//?
		self.severityLevel = header.severityLevel;

	self.apsKey = header.apsKey;
	self.payKey = header.payKey;
	self.crnCode = header.crnCode;
	self.employeeName = header.employeeName;
	self.everSentBack = header.everSentBack;
	if (header.hasException != nil)
		self.hasException = header.hasException;
	self.lastComment = header.lastComment;
	if (header.pdfUrl!= nil)
		self.pdfUrl = header.pdfUrl;
	if (header.polKey != nil)
		self.polKey = header.polKey;
    if (header.realPdfUrl != nil) {
        self.realPdfUrl = header.realPdfUrl;
    }
	self.processInstanceKey = header.processInstanceKey;
	self.purpose = header.purpose;
	self.receiptImageAvailable = header.receiptImageAvailable;
	self.receiptImageId = header.receiptImageId;
	if (header.receiptUrl!=nil)
		self.receiptUrl = header.receiptUrl;
	self.reportDate = header.reportDate;
	self.reportName = header.reportName;
	self.rptKey = header.rptKey;
	self.stepKey = header.stepKey;
	self.totalClaimedAmount = header.totalClaimedAmount;
	self.totalPostedAmount = header.totalPostedAmount;
	self.currentSequence = header.currentSequence;
	self.imageRequired = header.imageRequired;
	self.apvStatusName = header.apvStatusName;
	self.totalApprovedAmount = header.totalApprovedAmount;
	self.totalDueCompany = header.totalDueCompany;
	self.totalDueCompanyCard = header.totalDueCompanyCard;
	self.totalDueEmployee = header.totalDueEmployee;
	self.totalOwedByEmployee = header.totalOwedByEmployee;
	self.totalPaidByCompany = header.totalPaidByCompany;
	self.totalPersonalAmount = header.totalPersonalAmount;
	self.totalRejectedAmount = header.totalRejectedAmount;
	self.prepForSubmitEmpKey = header.prepForSubmitEmpKey;
    
    self.companyDisbursements = header.companyDisbursements;
    self.employeeDisbursements = header.employeeDisbursements;

    if (header.enableRecall != nil)
        self.enableRecall = header.enableRecall;

    if (header.workflowActions != nil && [header.workflowActions count] > 0)
        self.workflowActions = header.workflowActions;
}

-(void) copyDetail:(ReportData*) detail
{
    [self copyHeaderDetail:detail];
    if ([detail isDetail])
    {
        // TODO - Do we need to do a copy?
        self.keys = detail.keys;
        self.entries = detail.entries;
    }
}

-(BOOL) isDetail
{
     
    if ([self hasEntry]>0)
    {
        EntryData* firstEntry = (self.entries)[(self.keys)[0]];
        return [firstEntry.fields count]>0;
    }
    else
        return self.fields != nil && [self.fields count]>0;
}

-(BOOL) hasEntry
{
    return self.entries != nil && [self.entries count]>0;
}


NSInteger dateSortDescForReportData(id obj1, id obj2, void *context)
{
	ReportData* d1 = (ReportData*) obj1;
	ReportData* d2 = (ReportData*) obj2;
	
	NSString* v1 = d1.reportDate;
	NSString* v2 = d2.reportDate;
	NSComparisonResult res = [v1 compare: v2];
	if (res == NSOrderedAscending)
		return NSOrderedDescending;
	else if (res == NSOrderedDescending)
		return NSOrderedAscending;
	else
		return NSOrderedSame;
}

+(NSArray*) sortReportsByDateDesc:(NSArray*)rptList
{
	NSArray* result = [rptList sortedArrayUsingFunction:dateSortDescForReportData context:NULL];
    return result;
}

@end
