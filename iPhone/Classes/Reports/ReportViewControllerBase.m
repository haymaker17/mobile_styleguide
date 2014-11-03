//
//  ReportViewControllerBase.m
//  ConcurMobile
//
//  Created by yiwen on 4/20/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "FeedbackManager.h"
#import "ReportViewControllerBase.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "SubmitReportData.h"
#import "ViewConstants.h"
#import "ConcurMobileAppDelegate.h"

#import "LabelConstants.h"
#import "SubmitNeedReceiptsViewController.h"
#import "ReportRejectionViewController.h"
#import "DrillCell.h"
#import "ActiveReportListViewController.h"
#import "ListFieldEditVC.h"
#import "FormatUtils.h"
#import "ApproveReportsData.h"
#import "ApproverSearchVC.h"
#import "PDFVC.h"
#import "RecallReportData.h"
#import "UserConfig.h"
#import "ExpenseConfirmation.h"
#import "Config.h"
#import "ConditionalFieldsList.h"

#define		kBtnApproveTag		101777
#define		kBtnSendBackTag		101778

#define		kAlertViewApprove	101777
#define		kAlertViewText		101779
#define		kAlertViewSubmit	101780
#define		kAlertViewRateApp	101781
#define		kAlertViewRateAppApproval	101782
#define     kActionSheetApprovalActions  101101

@implementation ReportViewControllerBase
@synthesize role, inAction, rpt, doReload, approvalStatKey;

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

- (void)setSeedData:(NSDictionary*)pBag
{
    // Override in subclass to initialize view content data
}
-(void) recalculateSections
{
    // Override in subclass to initialize sections content data    
}



#pragma mark - View lifecycle
- (void)timeToDie
{
	doReload = YES;
}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
	[self timeToDie];
    [super didReceiveMemoryWarning];
    NSLog(@"LOW MEMORY WARNNG from %@", [[self class] description]);
    // Release any cached data, images, etc that aren't in use.}
}

- (void) refreshView
{
    if (rpt != nil && [self isViewLoaded])
	{
        [self recalculateSections];
		[tableList reloadData];
		[self setupToolbar];
	}
    else
        self.doReload = YES;
}

- (void)viewWillAppear:(BOOL)animated
{
	if(doReload)
	{
		doReload = NO;
        [self refreshView];
	}
    else if (isLoading)
    {
    	// Wait view is shown too many times. also loading view is not covering the full screen. 
        if(![self isWaitViewShowing] && ![self isLoadingViewShowing])
            [self showWaitViewWithText:[Localizer getLocalizedText:@"Loading Data"]];
    }
	
    [self.navigationController setToolbarHidden:NO];
	[super viewWillAppear:animated];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
}

#pragma mark Common Action Methods
-(BOOL) isApproving
{
	if (self.role == nil || ![@"MOBILE_EXPENSE_TRAVELER" isEqualToString:self.role])
		return TRUE;
	else 
		return FALSE;
}
-(BOOL) canSubmit
{
    // Not in action and has entry and status is Unsubmitted
	return !inAction && self.rpt.apsKey != nil && 
        ([self.rpt.apsKey isEqualToString:@"A_NOTF"] || [self.rpt.apsKey isEqualToString:@"A_RESU"])
        && self.rpt.entries != nil && [self.rpt.entries count] > 0;
}

-(BOOL) canEdit
{
    // Not in action and (either new rpt or not submitted report)
	return ![self isApproving] && 
           !inAction && self.rpt != nil && 
           ( self.rpt.rptKey == nil || 
             (self.rpt.apsKey != nil && ([self.rpt.apsKey isEqualToString:@"A_NOTF"] || [self.rpt.apsKey isEqualToString:@"A_RESU"])));
}

-(BOOL) canUpdateReceipt
{
    return [self canEdit] || [self.rpt.apsKey isEqualToString:@"A_RHLD"];
}

-(void) searchApprovers:(SubmitReportData*)srd
{
    ApproverSearchVC* lvc = [[ApproverSearchVC alloc] initWithNibName:@"ApproverSearchVC" bundle:nil];
    
    BOOL canDrawSubmit = [srd.canDrawSubmit isEqualToString:@"Y"];
    [lvc setSeedData:srd.rptKey approver:srd.approver canDrawSubmit:canDrawSubmit delegate:self];

	if([UIDevice isPad])
	{
		UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:lvc];
		localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
		[localNavigationController setToolbarHidden:NO];
		localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
		localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
		
		[self presentViewController:localNavigationController animated:YES completion:nil];
	}
	else
        [self.navigationController pushViewController:lvc animated:YES];
    
}

// Process submit/approve/sendback action results
-(void) respondToFoundData:(Msg *)msg
{
	NSString * curRole = msg.parameterBag == nil? nil : (msg.parameterBag)[@"ROLE"];
	if (curRole != nil)
		self.role = curRole;
    
	if ([msg.idKey isEqualToString:APPROVE_REPORTS_DATA])
	{//this is the entry point for send back and also for approve
		inAction = FALSE;
		
		NSString* errMsg = msg.errBody;
		if (errMsg == nil && msg.responder != nil)
		{
			ApproveReportsData* srd = (ApproveReportsData*) msg.responder;
			if (srd.reportStatus != nil && srd.reportStatus.errMsg != nil)
			{
                errMsg = srd.reportStatus.errMsg;
            }
        }
		if (errMsg != nil) 
		{
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:msg.errCode
								  message:errMsg
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
								  otherButtonTitles:nil];
			[alert show];
            [self setupToolbar];
		}
		else 
		{
            // DISABLE feedback manager for Gov
            if (![Config isGov])
            {
                [[FeedbackManager sharedInstance] requestRatingFromViewController:self withBlock:^{
                    [self afterChoiceToRateApp];
                }];
            }
            // Use new homevc and post refresh call if you can
            // Any new home should have refreshsummaryData so it can be called to refresh expenses
            UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
            if ([homeVC respondsToSelector:@selector(refreshSummaryData)])
            {
                [homeVC performSelector:@selector(refreshSummaryData) withObject:nil];
            }
		}
		
		[self hideWaitView];
	}
	else if ([msg.idKey isEqualToString:RECALL_REPORT_DATA]) 
	{//this is the entry point for recall
		inAction = FALSE;
        RecallReportData* srd = (RecallReportData*) msg.responder;
		NSString* errMsg = msg.errBody;
        if (errMsg == nil && msg.responseCode != 200)
        {
            errMsg = [@"Recall Failed" localize];
        }
		if (errMsg == nil && msg.responder != nil)
		{
			if (srd.actionStatus != nil && srd.actionStatus.errMsg != nil)
			{
                errMsg = srd.actionStatus.errMsg;
            }
        }
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
        
        if (errMsg != nil)
        {			
            UIAlertView *alert = [[MobileAlertView alloc] 
                                  initWithTitle:[Localizer getLocalizedText:@"Recall Failed"]
                                  message:errMsg
                                  delegate:nil 
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
            [alert show];
        }
        else
        {
            self.rpt.apsKey = @"A_RESU";
            self.rpt.apvStatusName = [@"A_RESU" localize]; // Sent back to employee
            if ([UIDevice isPad] && [self isKindOfClass:[ReportDetailViewController_iPad class]])
            {
                ReportDetailViewController_iPad* pvc = (ReportDetailViewController_iPad*) self;
                pvc.rptDetail.apsKey = @"A_RESU";
                pvc.rptDetail.apvStatusName = [@"A_RESU" localize]; // Sent back to employee
            }
//            // Update rpt status and submit workflow info
//            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY", [self getViewIDKey], @"TO_VIEW", nil];
//            
//            [[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORT_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
//            [pBag release];
            
            [self refreshReportList];
//            [self setupToolbar];
            [self refreshView]; // setupToolbar is called by refreshView
            [ReportViewControllerBase refreshSummaryData];
        }
    }
	else if ([msg.idKey isEqualToString:SUBMIT_REPORT_DATA]) 
	{//this is the entry point for submit
		inAction = FALSE;
        
        SubmitReportData* srd = (SubmitReportData*) msg.responder;
		NSString* errMsg = msg.errBody;
		if (errMsg == nil && msg.responder != nil)
		{
			if (srd.reportStatus != nil && srd.reportStatus.errMsg != nil)
			{
				if ([srd.reportStatus.status isEqualToString:@"not_an_approver"])
				{
					errMsg = [Localizer getLocalizedText:@"ERROR_ACCOUNT_NOT_CONFIGURED"];
				}
				else if ([srd.reportStatus.status isEqualToString:@"error.submit.missing_reqd_fields"])
				{
					errMsg = [Localizer getLocalizedText:@"ERROR_REQUIRED_FIELDS_MISSING"];
				}
				else
				{
					errMsg = srd.reportStatus.errMsg;
				}
			}
		}
		
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
		

        if (srd != nil && 
            ([srd.reportStatus.status isEqualToString:@"review_approval_flow_approver"] ||
             [srd.reportStatus.status isEqualToString:@"no_approver"]))
        {
            [self searchApprovers:srd];
        }
		else if (errMsg != nil) 
		{
			[self setupToolbar];
            
            NSDictionary *dict = @{@"Success": @"No", @"Failure": errMsg};
            [Flurry logEvent:@"Reports: Submit" withParameters:dict];
            
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:[Localizer getLocalizedText:@"ERROR_SUBMIT_FAILED"]
								  message:errMsg
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
		}
		else 
		{
            NSDictionary *dict = @{@"Success": @"Yes"};
            [Flurry logEvent:@"Reports: Submit" withParameters:dict];

            // DISABLE feedback manager for Gov
            if (![Config isGov])
            {
                [[FeedbackManager sharedInstance] requestRatingFromViewController:self withBlock:^{
                    [self afterChoiceToRateApp];
                }];
            }
            
            [ReportViewControllerBase refreshSummaryData];
            [self refreshReportList]; // MOB-6374
		}
		
		// To refresh to get submit exception
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
									 [self getViewIDKey], @"TO_VIEW", nil];
        
		[[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORT_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
	
}

#pragma mark SubmitNeedReceipts Delegate
- (void)cancelSubmitAfterReceipts
{
	[self dismissViewControllerAnimated:YES completion:nil];
}

- (void)confirmSubmitAfterReceipts
{
	[self dismissViewControllerAnimated:YES completion:nil];
	[self submitReport];
}

- (void) submitReport
{
    [self showWaitView];
	inAction = TRUE;
	[self setupToolbar];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 ACTIVE_ENTRIES, @"TO_VIEW",  
								 self.rpt.rptKey, @"ID_KEY", 
								 nil];
	
	[[ExSystem sharedInstance].msgControl createMsg:SUBMIT_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	
}

// Submit the report
-(IBAction) actionSubmit:(id)sender
{
	// Test imageRequired to make sure we are using a reportDetail object; otherwise, we must wait for the report detail to come back
	// from the server
	if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle: [Localizer getLocalizedText:@"Cannot Submit Report"]
							  message: [Localizer getLocalizedText:@"OFFLINE_MSG"]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
		return;		
	}
    
	if (self.rpt == nil || self.rpt.imageRequired == nil)
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle: [Localizer getLocalizedText:@"Cannot Submit Report"]
							  message: [Localizer getLocalizedText:@"MSG_WAIT_FETCHING_INFO"]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
		return;	
	}
	
	if([ExSystem connectedToNetwork] && self.rpt != nil && self.rpt.imageRequired != nil)
	{
		// Check for no_entries, undefined expense type, & red flag exceptions before submit
        // Mob-4145: We should not be blocking a submit for exceptions. On the web site, the users are not shown an alert or blocked from submitting the report on exceptions.
		NSString *cannotSubmitMsg = nil;
        NSString *cannotSubmitTitle = [Localizer getLocalizedText:@"Cannot Submit Report"];
        //		if (rpt.severityLevel != nil && [rpt.severityLevel isEqualToString:@"ERROR"] )
        //		{
        //			cannotSubmitMsg = [Localizer getLocalizedText:SUBMIT_ERROR_RPT_XCT_LEVEL_MSG];
        //		}
		// Mob-4145 We still need to check for no entry or undefined
		if (rpt.entries == nil || [rpt.entries count] ==0)
		{
			cannotSubmitMsg = [Localizer getLocalizedText:@"SUBMIT_ERROR_NO_ENTRY_MSG"];
		}
		else
		{
			NSMutableArray* rpeKeys = rpt.keys;
			NSMutableDictionary* entries = rpt.entries;
			for (int ix = 0; cannotSubmitMsg == nil && ix < [rpeKeys count]; ix++)
			{
				EntryData* entry = (EntryData*)entries[rpeKeys[ix]];
				if (entry != nil && ((entry.expKey != nil && [entry.expKey isEqualToString:@"UNDEF"]) ||
									 (entry.expKey == nil && [entry.expName isEqualToString:@"Undefined"])))
				{
					cannotSubmitMsg = [Localizer getLocalizedText:@"SUBMIT_ERROR_UNDEF_MSG"];
				}
                else if ([ReportViewControllerBase isCardAuthorizationTransaction:entry])
                {
                    // MOB-13616 Disable report submit if contains card authorization
                    cannotSubmitTitle = [Localizer getLocalizedText:@"Report Not Ready"];
                    cannotSubmitMsg = [Localizer getLocalizedText:@"SUBMIT_ERROR_CCT_AUTH"];
                    
                }
			}
		}
		
		if (cannotSubmitMsg != nil)
		{
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle: cannotSubmitTitle
								  message: cannotSubmitMsg
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]  
								  otherButtonTitles:nil];
			[alert show];
			return;
		}
		
		NSString* receiptRequired = self.rpt.imageRequired;
		NSMutableArray *entriesNeedReceipt = [[NSMutableArray alloc] init];
		NSMutableArray *entriesNeedPaperRecipt = [@[] mutableCopy];
        
		if (receiptRequired!= nil && [receiptRequired isEqualToString:@"Y"])
		{
			// We need to figure out entries needs receipts
			NSArray * keys = self.rpt.keys;
			NSDictionary * entries = self.rpt.entries;
			for (int ix = 0; ix < [keys count]; ix++)
			{
				NSString * key = (NSString*)keys[ix];
				EntryData*entry = (EntryData *) entries[key];
				
				// Logic adapted from SubmitWizard.js
				if ([@"Y" isEqualToString:entry.imageRequired] || [@"Y" isEqualToString: entry.receiptRequired])
				{
					if (entry.ereceiptId == nil &&  (entry.hasMobileReceipt == nil || [@"N" isEqualToString:entry.hasMobileReceipt])
						&& entry.receiptImageId == nil)
					{
						[entriesNeedReceipt addObject:entry];
					}
                    
                    // MOB-13715 need paper receipt flag
                    // Logic adapted from SubmitWizard.js " updateText: function(rpt, confirmAgreement, wizardView, numExpNeedRcpt) "
                    if ([entry.receiptRequired isEqualToString:@"Y"])
                    {
                        [entriesNeedPaperRecipt addObject:entry];
                    }
				}
			}
		}
		
		NSString *alertTitle = [Localizer getLocalizedText:@"CONFIRM_REPORT_SUBMISSION"];
		NSString *alertMessage = [Localizer getLocalizedText:@"MSG_RECEIPT_NOT_REQUIRED"];
		NSString *cancelButtonText = [[NSString alloc] initWithString:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
		// Mob-2516 Localization of Submit label text
		NSString *submitButtonText = [Localizer getLocalizedText:@"LABEL_SUBMIT_BTN"];
		
        // MOB-18287 if Original paper receipt is required show that(needPaperReceipt) instead of needReceipt message.
        if ([entriesNeedPaperRecipt count] > 0 && [entriesNeedReceipt count] > 0 )
        {
            NSMutableSet *mergeSet = [NSMutableSet setWithArray:entriesNeedPaperRecipt];
            [mergeSet addObjectsFromArray:entriesNeedReceipt];
            SubmitNeedReceiptsViewController * dlg = [[SubmitNeedReceiptsViewController alloc] initWithNibName:@"SubmitNeedReceiptsView" bundle:nil];
			dlg.delegate = self;
			dlg.entryList = [mergeSet allObjects];
            dlg.howToProvideMsgType = @"entriesNeedPaperRecipt";
            dlg.rpt = self.rpt;
			[self presentViewController:dlg animated:YES completion:nil];
        }
		else if ([entriesNeedReceipt count] >0)
		{
			// show modal dialog with entries need receipts
			SubmitNeedReceiptsViewController * dlg = [[SubmitNeedReceiptsViewController alloc] initWithNibName:@"SubmitNeedReceiptsView" bundle:nil];
			dlg.delegate = self;
			dlg.entryList = entriesNeedReceipt;
            dlg.howToProvideMsgType = @"entriesNeedReceipt";
            dlg.rpt = self.rpt;
			[self presentViewController:dlg animated:YES completion:nil];
		}
        else if ([entriesNeedPaperRecipt count] > 0)
        {
            // show modal dialog with entries need paper receipts
			SubmitNeedReceiptsViewController * dlg = [[SubmitNeedReceiptsViewController alloc] initWithNibName:@"SubmitNeedReceiptsView" bundle:nil];
			dlg.delegate = self;
			dlg.entryList = entriesNeedPaperRecipt;
            dlg.howToProvideMsgType = @"entriesNeedPaperRecipt";
            dlg.rpt = self.rpt;
			[self presentViewController:dlg animated:YES completion:nil];
        }
        else
        {
			if (receiptRequired!= nil && [receiptRequired isEqualToString:@"Y"])
				alertMessage = [Localizer getLocalizedText:@"MSG_ADDITIONAL_RECEIPT_NOT_REQUIRED"];
			
            // MOB-7870, 8370 (MWS) check and use custom confirmation msg.
            ExpenseConfirmation *submitConf = [[UserConfig getSingleton] submitConfirmationForPolicy:self.rpt.polKey];
            if (submitConf != nil && (submitConf.title != nil && submitConf.text != nil))
            {
                if ([submitConf.title length])
                    alertTitle = submitConf.title;
                if ([submitConf.text length])
                    alertMessage = submitConf.text;
            }
            
			UIAlertView *alert = [[MobileAlertView alloc] initWithTitle: alertTitle 
                                                                message: alertMessage
                                                               delegate: self 
                                                      cancelButtonTitle: cancelButtonText 
                                                      otherButtonTitles: submitButtonText, nil];
			alert.tag = kAlertViewSubmit;
			
			[alert show];
		}
	}
}


#pragma mark -
#pragma mark ReportRejectionDelegate Methods
- (void)rejectedWithComment:(NSString*)comment
{
	//NSLog(@"rptKey = %@, procKey = %@, currentSeq = %@", rpt.rptKey, rpt.processInstanceKey, rpt.currentSequence);
	[self showWaitViewWithText:[Localizer getLocalizedText:@"Sending Back Report"]];
    
	NSArray *toolbarItems = @[];
	[self setToolbarItems:toolbarItems animated:YES];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
								 APPROVE_ENTRIES, @"TO_VIEW", @"YES", @"SKIP_CACHE",comment, @"SendBackComment", nil];
	[[ExSystem sharedInstance].msgControl createMsg:APPROVE_REPORTS_DATA_REJECT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

- (void)rejectionCancelled
{
	// no op
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if(alertView.tag == kAlertViewApprove & buttonIndex == 1)
	{
        [Flurry logEvent:@"Reports: Approve Report"];
        
        [self showWaitViewWithText:[Localizer getLocalizedText:@"Approving Report"]];

		NSArray *toolbarItems = @[];
		[self setToolbarItems:toolbarItems animated:YES];
		//NSLog(@"rptKey = %@, procKey = %@, currentSeq = %@", rpt.rptKey, rpt.processInstanceKey, rpt.currentSequence);
		
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
									 APPROVE_ENTRIES, @"TO_VIEW", @"YES", @"SKIP_CACHE", nil];
        if (self.approvalStatKey != nil)
            pBag[@"STAT_KEY"] = self.approvalStatKey; // MOB-9753 custom approval status
        
		//NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY", @"YES", @"SHORT_CIRCUIT", nil]
		[[ExSystem sharedInstance].msgControl createMsg:APPROVE_REPORTS_DATA_APPROVE CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
		
		
	}
	else if(alertView.tag == kAlertViewApprove)
	{
		[self hideWaitView];
	}
	else if (alertView.tag == kAlertViewSubmit)
	{
		if (buttonIndex == 0){
			[self hideWaitView];
		}
		if (buttonIndex == 1)
        {
            
            [self showWaitView];
			inAction = TRUE;
			[self setupToolbar];
			
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
										 ACTIVE_ENTRIES, @"TO_VIEW", 
										 self.rpt.rptKey, @"ID_KEY", 
										 nil];
			
			[[ExSystem sharedInstance].msgControl createMsg:SUBMIT_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
			
		}
		
	}
    else
    {
        [super alertView:alertView clickedButtonAtIndex:buttonIndex];
    }
    
}

#pragma mark -
#pragma mark Recall
-(IBAction) actionRecall:(id)sender
{
    [self showWaitView];
    
	inAction = TRUE;
	[self setupToolbar];
	
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.rpt.rptKey, @"RPT_KEY", nil];
    [[ExSystem sharedInstance].msgControl createMsg:RECALL_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


#pragma mark -
#pragma mark Approval SendBack Methods
-(void) showApproveActions:(UIActionSheet*)actionSheet
{
    [actionSheet showInView:self.tableList];
}

-(IBAction) actionApproveActions:(id)sender
{
    // MOB-9753 pop up actionsheet for approval
    self.approvalStatKey = nil;
    
    MobileActionSheet * apprvAction = [[MobileActionSheet alloc] initWithTitle:nil
                                                                       delegate:self 
                                                              cancelButtonTitle:nil
                                                         destructiveButtonTitle:nil
                                                              otherButtonTitles:nil];
    int cancelBtnIdx = 0;
    NSMutableArray* btnIds = [[NSMutableArray alloc] init];

    for (WorkflowAction *wf in self.rpt.workflowActions)
    {
        [apprvAction addButtonWithTitle:wf.actionText];
        [btnIds addObject:wf.statKey];
        cancelBtnIdx ++;
    }
    [apprvAction addButtonWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
    apprvAction.cancelButtonIndex = cancelBtnIdx; 
	
	apprvAction.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
	apprvAction.tag = kActionSheetApprovalActions;
    apprvAction.btnIds = btnIds;

    [self showApproveActions:apprvAction];

}

-(IBAction) actionApprove:(id)sender
{
    NSString* nsQuestion = [Localizer getLocalizedText:@"APPROVE_QUESTION_AREYOUSURE"];
    
    // MOB-7870, 8370 (MWS) check and use custom confirmation msg.
    NSString* alertTitle = nil;
    NSString* alertMessage = nil;
    ExpenseConfirmation *approvalConf = [[UserConfig getSingleton] approvalConfirmationForPolicy:self.rpt.polKey];
    if (approvalConf != nil && (approvalConf.title != nil && approvalConf.text != nil))
    {
        if ([approvalConf.title length])
            alertTitle = approvalConf.title;
        if ([approvalConf.text length])
            alertMessage = approvalConf.text;
    }
    else
    {
        alertTitle = [Localizer getLocalizedText:@"APPROVE_PLEASE_CONFIRM"];
        alertMessage = nsQuestion;
    }
    
	UIAlertView* alert = [[MobileAlertView alloc] initWithTitle:alertTitle
                                                        message:alertMessage delegate:self cancelButtonTitle:
						  [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
                                              otherButtonTitles:[Localizer getLocalizedText:@"APPROVE_OK"], 
						  nil];
	
	alert.tag = kAlertViewApprove;
	[alert show];
}


-(IBAction) actionReject:(id)sender
{
	ReportRejectionViewController *rejectionVC = [[ReportRejectionViewController alloc] initWithNibName:@"ReportRejectionViewController" bundle:nil];
//	[rejectionVC setRootViewController:rootViewController];
	rejectionVC.reportRejectionDelegate = self;
	[self presentViewController:rejectionVC animated:YES completion:nil];
}

#pragma mark -
#pragma mark UIActionSheetDelegate Methods
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex 
{
    if (actionSheet.tag == kActionSheetApprovalActions)
	{
		if (buttonIndex == actionSheet.cancelButtonIndex) 	
		{
			return;
		} 
		else 
		{
            self.approvalStatKey = [((MobileActionSheet*) actionSheet) getButtonId:buttonIndex];
			[self actionApprove:nil];
		}	
	}
}

#pragma mark -
#pragma mark Report VC Methods 
- (NSString*) getVendorString:(NSString*) vendor WithLocation:(NSString*) locationName
{
	vendor = [vendor stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
	locationName = [locationName stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
	BOOL noVendor = vendor == nil||[vendor length]==0;
    BOOL noLocation = locationName == nil || [locationName length]==0;
	if (noVendor && noLocation)
	{
		return @"";
	} else if (noVendor)
	{
		return locationName;
	} else if (noLocation)
	{
		return vendor;
	} else {
		//NSLog(@"Vendor '%@' length = %d", vendor, [vendor length]);
		return [NSString stringWithFormat:@"%@, %@", vendor, locationName];
	}
}

+(BOOL)isCardAuthorizationTransaction:(EntryData*)thisEntry
{
    return thisEntry.isCreditCardCharge != nil
           && [thisEntry.isCreditCardCharge isEqualToString:@"Y"]
           && ([CCT_TYPE_AUTH isEqualToString: thisEntry.cctType]
               ||
               [CCT_TYPE_PRE_AUTH isEqualToString:thisEntry.cctType]);
}


-(NSArray*)getIconNames:(EntryData*)thisEntry
{
    __autoreleasing NSMutableArray* imageList = [[NSMutableArray alloc] init];
    
    if((thisEntry.hasExceptions != nil && [thisEntry.hasExceptions isEqualToString:@"Y"]) || [thisEntry.exceptions count] > 0)
    {
        BOOL showAlert = NO;
        for(ExceptionData *ed in thisEntry.exceptions)
        {
            if([ed.severityLevel isEqualToString:@"ERROR"])
            {
                showAlert = YES;
                break;
            }
        }
        
        if (showAlert)
            [imageList addObject:@"icon_redex"];
        else
            [imageList addObject:@"icon_yellowex"];
    }
    
    // Personal card and corporate card is mutually exclusive
    if([ReportViewControllerBase isCardAuthorizationTransaction:thisEntry])
        [imageList addObject:@"icon_card_gray"];
    else if((thisEntry.isCreditCardCharge != nil && [thisEntry.isCreditCardCharge isEqualToString:@"Y"])||
       (thisEntry.isPersonalCardCharge != nil && [thisEntry.isPersonalCardCharge isEqualToString:@"Y"]))
        [imageList addObject:@"icon_card_blue"];

	if(thisEntry.hasMobileReceipt != nil && [thisEntry.hasMobileReceipt isEqualToString:@"Y"])
        [imageList addObject:@"icon_receipt"];
    else if ((thisEntry.receiptRequired != nil && [thisEntry.receiptRequired isEqualToString:@"Y"]) ||
             (thisEntry.imageRequired != nil && [thisEntry.imageRequired isEqualToString:@"Y"]))
        [imageList addObject:@"icon_receiptrequired_entry"]; // MOB-8756
    
//    if(thisEntry.isItemized != nil && [thisEntry.isItemized isEqualToString:@"Y"])
//        [imageList addObject:@"icon_itemize"];
//    
//   	if(thisEntry.hasComments != nil && [thisEntry.hasComments isEqualToString:@"Y"])
//        [imageList addObject:@"icon_comment"];
//    
//    if(thisEntry.hasAttendees != nil && [thisEntry.hasAttendees isEqualToString:@"Y"])
//        [imageList addObject:@"icon_attendee"];
    
    //    if(thisEntry.isPersonal != nil && [thisEntry.isPersonal isEqualToString:@"Y"])
    //		[imgBack setImage:[UIImage imageNamed:@"personal_expense_24X24_PNG.png"]];
    
    
    return imageList;
}

-(void)makeEntryCell:(SummaryCellMLines *)cell Entry:(EntryData *)entry
{
	if ([entry.expKey isEqualToString:@"UNDEF"])
	{
		cell.lblName.textColor = [UIColor redColor];
	}
	else {
		cell.lblName.textColor = [UIColor blackColor];
	}

    NSString* nameStr = entry.expName;
    NSString* amountStr = [FormatUtils formatMoney:entry.transactionAmount crnCode:entry.transactionCrnCode];
	// Entry date is local, not UTC
	// MOB-17429: Missing date when the region is set to UK and 12h
    NSString* line1Str = [CCDateUtilities formatDateToMMMddYYYFromString:entry.transactionDate];
    NSString* line2Str = [self getVendorString:entry.vendorDescription WithLocation:entry.locationName];
	NSArray* iconNames = [self getIconNames:entry];
    
    NSString* image1 = [iconNames count]>0?iconNames[0] : nil;
    NSString* image2 = [iconNames count]>1?iconNames[1] : nil;
    NSString* image3 = [iconNames count]>2?iconNames[2] : nil;

    [cell resetCellContent:nameStr withAmount:amountStr withLine1:line1Str withLine2: line2Str withImage1:image1 withImage2:image2 withImage3:image3];
    
    if([ReportViewControllerBase isCardAuthorizationTransaction:entry])
    {
        cell.lblName.textColor = [UIColor grayColor];
        cell.lblAmount.textColor = [UIColor grayColor];
    }
    else
    {
        cell.lblName.textColor = [UIColor blackColor];
        cell.lblAmount.textColor = [UIColor blackColor];
    }
    
//
//	[self drawNameAmountLabels:cell.lblName AmountLabel:cell.lblAmount];
//	
	[cell setAccessoryType:UITableViewCellAccessoryNone];
}

-(void) viewDidLoad
{
    [super viewDidLoad];

    // MOB-15173 force separator style to be a single line.
    // This should be the default, but for some reason it's not set like that on Report screens.
    if ([self.tableList respondsToSelector:@selector(setSeparatorStyle:)]) {
        [self.tableList setSeparatorStyle:UITableViewCellSeparatorStyleSingleLine];
    }
}

-(UITableViewCell *) makeDrillCell:(UITableView*)tblView withText:(NSString*)command withImage:(NSString*)imgName enabled:(BOOL)flag
{
    DrillCell *cell = (DrillCell*)[tblView dequeueReusableCellWithIdentifier:@"DrillCell"];
	if (cell == nil) 
	{
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"DrillCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[DrillCell class]])
                cell = (DrillCell *)oneObject;
	}
	
    [cell resetCellContent:command withImage:imgName];
    return cell;
}

-(CGFloat)getExceptionTextHeight:(NSString*) text withWidth:(CGFloat)width
{
    CGFloat height =  [FormatUtils getTextFieldHeight:width Text:text FontSize:14.0f];
    
    if((height) < 36)
        height =  36;
    
    return height + 8;  // 4 is for the margin below the text
}

-(UITableViewCell *) makeExceptionCell:(UITableView*)tblView withText:(NSString*)text withImage:(NSString*)imgName
{
    DrillCell *cell = (DrillCell*)[tblView dequeueReusableCellWithIdentifier:@"ExceptionCell"];
	if (cell == nil) 
	{
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ExceptionCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[DrillCell class]])
                cell = (DrillCell *)oneObject;
	}
	
    [cell resetCellContent:text withImage:imgName];
    
    CGRect r = cell.lblName.frame;
    CGFloat width = cell.frame.size.width - 20 - 40;
    CGFloat height = [self getExceptionTextHeight:text withWidth:width];
    cell.lblName.frame = CGRectMake(r.origin.x, 0, width, height);

    return cell;
}


#pragma mark Toolbar Methods
-(void)setupToolbar
{
	if(![ExSystem connectedToNetwork])
	{
		[self makeOfflineBar];
	}
    
    [super setupToolbar];
	
}

-(void) setupRefreshingToolbar
{
	[self setupToolbarWithMessage:[Localizer getLocalizedText:@"Refreshing"] withActivity:YES];
}

-(void) setupToolbarWithMessage:(NSString*) msg withActivity:(BOOL) fAct
{
    [self setupToolbar];

	if(![ExSystem connectedToNetwork])
	{
		return;
	}
	
	UIView *rView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 150, 50)];
	
	UILabel *refreshText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 150, 50)];
    
	[refreshText setTextColor:[UIColor whiteColor]];
	[refreshText setText:msg];
	[refreshText setFont:[UIFont boldSystemFontOfSize:11.0f]];
    
	[refreshText setBackgroundColor:[UIColor clearColor]];
	[refreshText setTextAlignment:NSTextAlignmentCenter];
	[refreshText setNumberOfLines:0];
	
	[refreshText setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
	[refreshText setShadowOffset:CGSizeMake(0.0f, -1.0f)];
	
	if (fAct)
	{
		UIActivityIndicatorView *activity = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(65, 15, 20, 20)];
		activity.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin;
		[activity setHidesWhenStopped:YES];
		[activity setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleWhiteLarge];
		[activity startAnimating];
		
		[rView addSubview:activity];
	}
	[rView addSubview:refreshText];
	
	
	UIBarButtonItem *titleItem = [[UIBarButtonItem alloc] initWithCustomView:rView];
	titleItem.tag = 999;
	
    NSArray* oldItems = self.toolbarItems;
    
    NSMutableArray *toolbarItems = [NSMutableArray arrayWithCapacity:5];
    UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
    flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    [toolbarItems addObject:flexibleSpace];
    [toolbarItems addObject:titleItem];
    [toolbarItems addObject:flexibleSpace];
    
    if (oldItems != nil && [oldItems count]>0)
    {
        // If the last or first item is button, then add to the bar
        UIBarButtonItem *lastItm = oldItems[[oldItems count]-1];
        if (lastItm.action != nil)
            [toolbarItems addObject:lastItm];
        
        if ([oldItems count] > 1)
        {
            UIBarButtonItem* firstItem = oldItems[0];
            if (firstItem.action != nil)
                [toolbarItems insertObject:firstItem atIndex:0];
        }
    } 

    [self setToolbarItems:toolbarItems animated:YES];


}


#pragma mark App Rating Methods

-(void)afterChoiceToRateAppApproval
{
    [[ExSystem sharedInstance].cacheData removeCache:APPROVE_REPORT_DETAIL_DATA UserID:[ExSystem sharedInstance].userName RecordKey:rpt.rptKey];

    if ([UIDevice isPad])
        return;
    
    NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"POP_TO_ROOT_VIEW", nil];
    [ConcurMobileAppDelegate switchToView:APPROVE_REPORTS viewFrom:APPROVE_ENTRIES ParameterBag:pBag];
}

-(void)afterChoiceToRateApp
{
    if ([self.role isEqualToString:ROLE_EXPENSE_MANAGER])
    {
        [self afterChoiceToRateAppApproval];
        return;
    }
    
	[[ExSystem sharedInstance].cacheData removeCache:ACTIVE_REPORT_DETAIL_DATA UserID:[ExSystem sharedInstance].userName RecordKey:rpt.rptKey];

    if ([UIDevice isPad])
        return;

    // TODO - update cache with response data
    NSMutableDictionary* pBag= [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"POP_TO_ROOT_VIEW", nil];
    [ConcurMobileAppDelegate switchToView:ACTIVE_REPORTS viewFrom:ACTIVE_ENTRIES ParameterBag:pBag];
}


#pragma mark - 
#pragma mark Report editing/refresh methods
-(void) refreshReportList
{
	int vcCount = [self.navigationController.viewControllers count];
	
    //	MobileViewController *detailVc = nil;
	MobileViewController *listVc = nil;
	for (int ix = 1; ix < vcCount-1; ix++)
	{
		MobileViewController *parentMVC = (MobileViewController *)(self.navigationController.viewControllers)[ix];
        if ([parentMVC isKindOfClass:ActiveReportListViewController.class])
        {
            listVc = parentMVC;
        }
	}
	
    if (listVc == nil) listVc = self;
    
	NSMutableDictionary* pBag2 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                  listVc==self?@"YES":@"NO", @"SKIP_PARSE", nil];
	[[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORTS_DATA CacheOnly:@"NO" ParameterBag:pBag2 SkipCache:YES RespondTo:listVc];

}

-(void) refreshWithUpdatedReport:(ReportData*) newRpt
{
	int vcCount = [self.navigationController.viewControllers count];
	
    //	MobileViewController *detailVc = nil;
	MobileViewController *listVc = nil;
	for (int ix = 1; ix < vcCount-1; ix++)
	{
		MobileViewController *parentMVC = (MobileViewController *)(self.navigationController.viewControllers)[ix];
        if ([parentMVC isKindOfClass:ActiveReportListViewController.class])
        {
             listVc = parentMVC;
        }
	}
	
	if (newRpt != nil)
	{
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: 
									 newRpt, @"REPORT_DETAIL", newRpt.rptKey, @"ID_KEY", newRpt.rptKey, @"RECORD_KEY", 
									 @"YES", @"SHORT_CIRCUIT", nil]; //, @"YES", @"SKIP_PARSE"
		Msg *msg = [[Msg alloc] init];
		msg.parameterBag = pBag;
		msg.idKey = @"SHORT_CIRCUIT";
		[MsgControl sendMsgToAllVisibleViews:msg];
	}
	else {
		// TODO - Fetch the report header to trigger a report detail cache refresh
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.rpt, @"REPORT", 
									 self.rpt.rptKey, @"ID_KEY", self.rpt.rptKey, @"RECORD_KEY",
									 ACTIVE_REPORT_DETAIL_DATA, @"REFRESH_ALL_WITH_MSG_ID",
									 nil];
		[[ExSystem sharedInstance].msgControl createMsg:REPORT_HEADER_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:nil];
	}
    
	if (listVc == nil) listVc = self;

    NSMutableDictionary* pBag2 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                      listVc==self?@"YES":@"NO", @"SKIP_PARSE", nil];
    [[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORTS_DATA CacheOnly:@"NO" ParameterBag:pBag2 SkipCache:YES RespondTo:listVc];
}

#pragma mark -
#pragma mark Field Based Form Edit methods

-(void)prefetchForListEditor:(ListFieldEditVC*) lvc
{
    FormFieldData* field = lvc.field;
    
    if (!([field.ctrlType isEqualToString:@"checkbox"] || [field.dataType isEqualToString:@"BOOLEANCHAR"] || [field.iD isEqualToString:@"CarKey"]))
	{
		// Prefetch MRU data
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: field, @"FIELD", @"Y", @"MRU", nil];
		if ([field.iD isEqualToString:@"ReceiptType"])
		{
			lvc.rptKey = self.rpt.rptKey;
            if (self.rpt.rptKey != nil)
                pBag[@"RPT_KEY"] = self.rpt.rptKey;
		}
		[[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:lvc];
	}
}

- (FormFieldData*) getCurrentExpenseTypeField
{
    return [self findEditingField:@"ExpKey"];
}

- (void)saveSelectedExpenseType:(ExpenseTypeData*) et
{
    FormFieldData* expTypeField = [self getCurrentExpenseTypeField];
    if (expTypeField != nil && ![expTypeField.liKey isEqualToString:et.expKey])
	{
		expTypeField.liKey = et.expKey;
		expTypeField.fieldValue = et.expName;
		self.isDirty = YES;
		[self refreshField:expTypeField];
	}
	
	[self dismissViewControllerAnimated:YES completion:nil];
}

- (void)cancelExpenseType
{
	[self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - SearchApproverDelegate method
-(void) approverSelected:(ApproverInfo*) approver
{
    // Resubmit
    [self showWaitView];
	inAction = TRUE;
	[self setupToolbar];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 ACTIVE_ENTRIES, @"TO_VIEW",  
								 self.rpt.rptKey, @"ID_KEY",
                                 approver.empKey, @"APPROVER_EMP_KEY",
								 nil];
	
	[[ExSystem sharedInstance].msgControl createMsg:SUBMIT_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	

}

+(void) refreshSummaryData
{
    // use new HomeVC
    UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
    if ([homeVC respondsToSelector:@selector(refreshSummaryData)])
    {
        [homeVC performSelector:@selector(refreshSummaryData) withObject:nil];
    }


}
#pragma mark - Dynamic Field update

- (void) makeDynamicActionServerCall:(FormFieldData*) field
{
    if (![[ExSystem sharedInstance] canUseConditionalFields])
    {
        return;
    }
    
    NSString *fieldValue = [field fieldValue];
    //Special case: while editing a boolean checkbox, The value of the change is held at liKey.
    if([field.dataType isEqualToString:@"BOOLEANCHAR"] && [field.liKey lengthIgnoreWhitespace])
    {
		fieldValue = (field.liKey == nil) ? @"" : field.liKey;
    }

    //if we have a picklist, server is expecting the listKey value not the list text.
    if ([field.ctrlType isEqualToString:@"picklist"] && [field.dataType isEqualToString:@"LIST"])
    {
        fieldValue = (field.liKey == nil) ? @"" : field.liKey;
    }
        
    NSDictionary *pBag = @{@"FF_KEY": field.formFieldKey,
                            @"VALUE": fieldValue,
                        @"ROLE_CODE": self.role};
    
    [[ExSystem sharedInstance].msgControl createMsg:GET_DYNAMIC_ACTIONS
                                          CacheOnly:@"NO"
                                       ParameterBag:[pBag mutableCopy]
                                          SkipCache:YES
                                          RespondTo:self];
}

- (BOOL) updateDynamicFields: (ConditionalFieldsList *) dynFields
                      fields: (NSMutableArray *) fields
{
	if (![[ExSystem sharedInstance] canUseConditionalFields])
    {
        return NO;
    }

    BOOL fRefresh = NO;
    FormFieldData *lastField = nil;
    
    for (FormFieldData *fld in fields)
    {
        ConditionalFieldAction *DynAction = dynFields.conditionalFieldListData[[fld.formFieldKey integerValue]];
        if (DynAction != nil)
        {
            if ([DynAction.action isEqualToString:@"HIDE"] == YES)
            {
                [self hideField:fld];
                fRefresh = YES;
            }
            else
            {
                if (lastField != nil)
                {
                    fld.access = DynAction.access; //original access
                    fld.ctrlType = fld.originalCtrlType; //original Control type (edit, list, ...)
                    [self showField:fld afterField:lastField];
                    fRefresh = YES;
                }
            }
        }
        //Dynamic fields are custom fields and they usually show up at the bottom.
        //We cannot have dynamic fields as first field in table.
        if (!([fld.ctrlType isEqualToString:@"hidden"] || [fld.ctrlType isEqualToString:@"HD"]))
        {
            lastField = fld;
        }
    }
    return fRefresh;
}

-(bool) hasTravelAllowanceEntries:(ReportData*)report
{
    NSMutableDictionary *entries = report.entries;

    for (id key in entries) {
        EntryData *entry = (EntryData *)[entries objectForKey:key];
        if([entry.hasTravelAllowance isEqualToString:@"Y"])
        {
            return YES;
        }
    }

    return NO;
}
@end
