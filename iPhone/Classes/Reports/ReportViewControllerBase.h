//
//  ReportViewControllerBase.h
//  ConcurMobile
//
//  Created by yiwen on 4/20/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "FormViewControllerBase.h"
#import "ReportData.h"
#import "EntryData.h"
#import "SummaryCellMLines.h"
#import "SubmitNeedReceiptsDelegate.h"
#import "ReportRejectionDelegate.h"
#import "ExpenseTypeData.h"
#import "ExpenseTypeDelegate.h"
#import "ApproverSearchDelegate.h"
#import "SubmitReportData.h"
#import "ConditionalFieldsList.h"

@interface ReportViewControllerBase : FormViewControllerBase 
    <UIAlertViewDelegate, 
    SubmitNeedReceiptsDelegate, 
    ExpenseTypeDelegate,
    ApproverSearchDelegate,
    UIActionSheetDelegate,
    ReportRejectionDelegate>
{
    BOOL					inAction; // Submitting, approving
	NSString				*role;  // MOBILE_EXPENSE_MANAGER/TRAVELER/

	ReportData				*rpt;

    BOOL                    doReload;
    BOOL                    isLoading;
    
    NSString                *approvalStatKey; // MOB-9753 custom approval
}

@property (strong, nonatomic) ReportData				*rpt;
@property (strong, nonatomic) NSString					*role;
@property BOOL                                          inAction;
@property BOOL                                          doReload;
@property (strong, nonatomic) NSString                  *approvalStatKey;

// MOB-21355: check if the report is from ReportApprovalListViewController REPORT_APPROVALS section
@property BOOL                                           isReportApproval;

-(NSString *)getViewDisplayType;

-(IBAction) actionApproveActions:(id)sender;
-(IBAction) actionApprove:(id)sender;
-(IBAction) actionReject:(id)sender;
-(IBAction) actionSubmit:(id)sender;
-(void) submitReport;
-(void) searchApprovers:(SubmitReportData*)srd;

-(BOOL) isApproving;
-(BOOL) canEdit;
-(BOOL) canSubmit;
-(BOOL) canUpdateReceipt; // allow attach/append/detach receipts

// API for view data init without respondToFoundData
- (void)setSeedData:(NSDictionary*)pBag;
-(void) recalculateSections;

- (void)refreshView;
-(void) refreshWithUpdatedReport:(ReportData*) newRpt;
-(void) refreshReportList;


- (NSString*) getVendorString:(NSString*) vendor WithLocation:(NSString*) locationName;
-(NSArray*)getIconNames:(EntryData*)thisEntry;
-(void)makeEntryCell:(SummaryCellMLines *)cell Entry:(EntryData *)entry;
-(UITableViewCell *) makeDrillCell:(UITableView*)tblView withText:(NSString*)command withImage:(NSString*)imgName enabled:(BOOL)flag;
-(CGFloat)getExceptionTextHeight:(NSString*) text withWidth:(CGFloat)width;
-(UITableViewCell *) makeExceptionCell:(UITableView*)tblView withText:(NSString*)command withImage:(NSString*)imgName;

-(void)setupToolbar;
-(void)setupToolbarWithMessage:(NSString*) msg withActivity:(BOOL) fAct;
-(void) setupRefreshingToolbar;

-(void)afterChoiceToRateApp;

- (FormFieldData*) getCurrentExpenseTypeField;
- (void)saveSelectedExpenseType:(ExpenseTypeData*) et;
- (void)cancelExpenseType;


// Override to adjust where the action sheet shows
-(void) showApproveActions:(UIActionSheet*)actionSheet;

//Call server to retrieve dynamic fields actions
-(void) makeDynamicActionServerCall: (FormFieldData *) field;

//Dynamic fields refresh
- (BOOL) updateDynamicFields: (ConditionalFieldsList *) dynFields
                      fields: (NSMutableArray *) fields;

- (bool)hasTravelAllowanceEntries:(ReportData *)report;

// Refresh root view report summary
+(void) refreshSummaryData;
+(BOOL)isCardAuthorizationTransaction:(EntryData*)thisEntry;

@end
