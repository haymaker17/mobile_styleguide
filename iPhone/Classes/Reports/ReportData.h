//
//  ReportData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntryData.h"
#import "FormFieldData.h"
#import "ExceptionData.h"
#import "WorkflowAction.h"

@interface ReportData : NSObject 

{
	NSString		*apsKey, *payKey, *crnCode, *employeeName, *everSentBack, *hasException, *lastComment, *pdfUrl, *processInstanceKey, *purpose
	, *receiptImageAvailable, *receiptUrl, *reportDate, *reportName, *rptKey, *stepKey, *totalApprovedAmount, *totalClaimedAmount, *totalDueCompany
	, *totalDueCompanyCard, *totalDueEmployee, *totalOwedByEmployee, *totalPaidByCompany, *totalPersonalAmount, *totalPostedAmount
	, *totalRejectedAmount, *currentSequence, *realPdfUrl;
	
    NSString            *formKey;        // To optimize form switching while changing policy
    
	NSString			*imageRequired;  // Image receipt is required
	NSString			*severityLevel;  // Max exception level, NONE (Empty) / WARNING / ERROR
	NSString			*apvStatusName;  // report status name
	NSString			*polKey;
	NSMutableDictionary	*entries, *fields, *comments;
	NSMutableArray		*keys, *commentKeys, *fieldKeys, *exceptions;
	EntryData			*entry;
	FormFieldData		*field;
	CommentData			*comment;
	ExceptionData		*exception;
	NSString			*receiptImageId;
    
    NSMutableArray      *companyDisbursements; // An array of FormFields
    NSMutableArray      *employeeDisbursements; // An array of FormFields
    
    NSString            *enableRecall;      // Returned in GetReportDetailxx
    NSString            *aprvEmpName;       // MOB-8897 approver employee name, returned in reportlist APIs
    NSString            *prepForSubmitEmpKey;       // MOB-11325 - Boeing - icon to Flag ExpRpt as ready to submit
    
    NSMutableArray      *workflowActions;   // MOB-9753 support custom approval actions; if nil, use standard behavior
    WorkflowAction      *curWorkflowAction;
    
}


@property (strong, nonatomic) NSString *apsKey;
@property (strong, nonatomic) NSString *payKey;
@property (strong, nonatomic) NSString *apvStatusName;
@property (strong, nonatomic) NSString *crnCode;
@property (strong, nonatomic) NSString *employeeName;
@property (strong, nonatomic) NSString *everSentBack;
@property (strong, nonatomic) NSString *hasException;
@property (strong, nonatomic) NSString *lastComment;
@property (strong, nonatomic) NSString *pdfUrl;
@property (strong, nonatomic) NSString *processInstanceKey;
@property (strong, nonatomic) NSString *purpose;
@property (strong, nonatomic) NSString *receiptImageAvailable;
@property (strong, nonatomic) NSString *receiptUrl;
@property (strong, nonatomic) NSString *realPdfUrl;
@property (strong, nonatomic) NSString *imageRequired;
@property (strong, nonatomic) NSString *reportDate;
@property (strong, nonatomic) NSString *reportName;
@property (strong, nonatomic) NSString *rptKey;
@property (strong, nonatomic) NSString *stepKey;
@property (strong, nonatomic) NSString *severityLevel;
@property (strong, nonatomic) NSString *polKey;
@property (strong, nonatomic) NSString *formKey;

@property (strong, nonatomic) NSString *totalClaimedAmount;
@property (strong, nonatomic) NSString *totalPostedAmount;

@property (strong, nonatomic) NSString *totalApprovedAmount;
@property (strong, nonatomic) NSString *totalDueCompany;
@property (strong, nonatomic) NSString *totalDueCompanyCard;
@property (strong, nonatomic) NSString *totalDueEmployee;
@property (strong, nonatomic) NSString *totalOwedByEmployee;
@property (strong, nonatomic) NSString *totalPaidByCompany;
@property (strong, nonatomic) NSString *totalPersonalAmount;
@property (strong, nonatomic) NSString *totalRejectedAmount;

@property (strong, nonatomic) NSMutableDictionary	*fields;
@property (strong, nonatomic) NSMutableDictionary	*comments;
@property (strong, nonatomic) NSMutableArray		*commentKeys;
@property (strong, nonatomic) NSMutableArray		*fieldKeys;

@property (strong, nonatomic) NSMutableDictionary	*entries;
@property (strong, nonatomic) NSMutableArray		*keys;
@property (strong, nonatomic) EntryData				*entry;
@property (strong, nonatomic) FormFieldData			*field;
@property (strong, nonatomic) CommentData			*comment;
@property (strong, nonatomic) ExceptionData			*exception;
@property (strong, nonatomic) NSMutableArray		*exceptions;

@property (strong, nonatomic) NSString				*currentSequence;
@property (strong, nonatomic) NSString				*receiptImageId;

@property (strong, nonatomic) NSString				*enableRecall;

@property (strong, nonatomic) NSMutableArray        *companyDisbursements; 
@property (strong, nonatomic) NSMutableArray        *employeeDisbursements; 

@property (strong, nonatomic) NSString              *aprvEmpName;
@property (strong, nonatomic) NSString              *prepForSubmitEmpKey;

@property (strong, nonatomic) NSMutableArray        *workflowActions;
@property (strong, nonatomic) WorkflowAction        *curWorkflowAction;

-(id)init;
-(void)finishEntry;
-(void)finishField;
-(void)finishComment;
-(void)finishException;
-(void)finishCompanyDisbursementsField;
-(void)finishEmployeeDisbursementsField;
-(void)finishWorkflowAction;

- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;

-(void) copyHeaderDetail:(ReportData*) header;
-(void) copyDetail:(ReportData*) detail;
-(BOOL) isDetail;

-(BOOL) hasEntry;

+(NSArray*) sortReportsByDateDesc:(NSArray*)rptList;

@end
