//
//  SelectReportViewController.m
//  ConcurMobile
//
//  Created by yiwen on 4/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SelectReportViewController.h"
#import "ExSystem.h" 

#import "FormatUtils.h"
#import "ActiveReportListData.h"
#import "ReportData.h"
#import "SelectReportCell.h"
#import "AddToReportData.h"
#import "LabelConstants.h"
#import "MobileAlertView.h"
#import "ConcurMobileAppDelegate.h"
#import "ReportSummaryViewController.h"
#import "ReportManager.h"
#import "ReportDetailViewController.h"
#import "CarMileageData.h"
#import "SelectReportTableHeader.h"
#import "MobileEntryManager.h"
#import "CCDateUtilities.h"

@interface SelectReportViewController ()
-(void)reportChosen:(BOOL) isRptNew;
-(void)sendAddToReportMsg;
@end

@implementation SelectReportViewController

@synthesize tableView, rptList, showedNo, fetch;
@synthesize parentMVC;
@synthesize meKeys, pctKeys, cctKeys, rcKeys,meAtnMap;
@synthesize rptKey, reportName, selectedRows, padHomeVC;
@synthesize alertViewConnectionError, alertViewConfirmSelection, alertViewFixReportName, isCarMileage, rpt;
@synthesize sections, sectionDataMap;

NSString * const ADD_TO_RPT_CONFIRM_TITLE = @"ADD_TO_RPT_CONFIRM_TITLE";
NSString * const ADD_TO_RPT_CONFIRM_MSG = @"ADD_TO_RPT_CONFIRM_MSG";
NSString * const FIX_REPORT_NAME_MSG = @"FIX_REPORT_NAME_MSG";

#define kAlertViewConnectionErrorTag	11901
#define kAlertViewConfirmSelectionTag	11902
#define kAlertViewFixReportNameTag		11903

#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return SELECT_REPORT;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_MODAL;
}

- (void) resetReportListData: (ActiveReportListData *) arl
{
	NSArray* unsubmittedRptlist = [arl getUnsubmittedReports];

	self.rptList = [ReportData sortReportsByDateDesc:unsubmittedRptlist];
    [[ReportManager sharedInstance] saveUnsubmittedReportList:self.rptList];
    
    self.sectionDataMap = [[NSMutableDictionary alloc] init];
    self.sections = [[NSMutableArray alloc] init];
    for (ReportData* curRpt in rptList)
    {
        NSString* curRptDate = curRpt.reportDate;
        NSString *curMon = [CCDateUtilities formatDateToMonthAndYear:curRptDate];

        NSMutableArray* curMonRptList;
        if ([curMon lengthIgnoreWhitespace]) {
            curMonRptList = (NSMutableArray*)(self.sectionDataMap)[curMon];
        }
        else
        {
            ALog(@" formatDateForActiveReportEndpoint returned Current month as nil for date :%@ ", curRptDate);
        }
        if (curMonRptList == nil)
        {
            curMonRptList = [[NSMutableArray alloc] init];
            (self.sectionDataMap)[curMon] = curMonRptList;
            [self.sections addObject:curMon];
        }
        [curMonRptList addObject:curRpt];
    }
}

-(void)respondToFoundData:(Msg *)msg
{
	// Note: Use the flag of isAcceptingData to NOT to refresh view when the network data comes back 
	// after the view is dismissed 
	// If this form is reused, instead of recreated each time expense type dialog is invoked, we 
	// need to refine this code to update the data store, but not refreshing view elements.
	if (!isAcceptingData)
		return;
	
	if ([msg.idKey isEqualToString:ACTIVE_REPORTS_DATA] && !isAddingToReport)
	{
		[self hideLoadingView];
		ActiveReportListData *arl = (ActiveReportListData *)msg.responder;
		//NSLog(@"report count %d", [arl.keys count]);
		[self resetReportListData: arl];
		[tableView reloadData];
		[self hideWaitView];
	}
	else if ([msg.idKey isEqualToString:ADD_TO_REPORT_DATA])
	{
		isAddingToReport = NO;
		[self hideWaitView];
		AddToReportData *auth = (AddToReportData *)msg.responder;
		NSString* errMsg = msg.errBody;
		if (msg.errBody == nil && msg.responseCode != 200)
        {
            errMsg = [Localizer getLocalizedText:@"Failed to add to report"];
        }
        
        if (errMsg != nil) 
		{
			MobileAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:[Localizer getLocalizedText:@"Connection Error"]
								  message:msg.errBody
								  delegate:self 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]  
								  otherButtonTitles:nil];
			alert.tag = kAlertViewConnectionErrorTag;
			self.alertViewConnectionError = alert;
			[alert show];
		} 
		else
		{
			if(isCarMileage)
			{
                CarMileageData *carMileageData = [[CarMileageData alloc] init];
                [carMileageData userSelectedReport:auth.rpt.rptKey rpt:auth.rpt inView:self];
                /*
				[self.navigationController popViewControllerAnimated:YES];
				if([UIDevice isPad])
					[padHomeVC fetchPersonalCarMileageFormFields:self rptKey:auth.rpt.rptKey rpt:auth.rpt];
				else 
					[[ConcurMobileAppDelegate findRootViewController] fetchPersonalCarMileageFormFields:self rptKey:auth.rpt.rptKey rpt:auth.rpt];
                 */
				
			}
			else
			{
                if ([auth hasFailedEntry])
                {
                    MobileAlertView *alert = [[MobileAlertView alloc] 
                                              initWithTitle:[Localizer getLocalizedText:@"Import Error"]
                                              message:[Localizer getLocalizedText:@"Failed to import entry"]
                                              delegate:nil 
                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]  
                                              otherButtonTitles:nil];
                    [alert show];
                }
                
                // MOB-17077 - Update the Quick expense list here
                // Add to report is success, so update expense list.
                // No need for server round trip. just remove those expense entries from coredata list -- cool
                // Delete rows by each keys
                
                // Handle AddToReportV5
                for(NSString *key in auth.smartExpenseIdsStatusDict) {
                    
                    ActionStatus *status = (auth.smartExpenseIdsStatusDict)[key];
                    if([status.status isEqualToString:@"SUCCESS" ])
                    {
                        [[MobileEntryManager sharedInstance] deleteBySmartExpenseId:key];
                    }
                    
                }

                
                for(NSString *key in auth.pctStatusDict)
                {
                    ActionStatus *status = (auth.pctStatusDict)[key];
                    if([status.status isEqualToString:@"SUCCESS" ])
                    {
                        [[MobileEntryManager sharedInstance] deleteBypctKey:key];
                    }
                    
                }
                
                for(NSString *key in auth.cctStatusDict)
                {
                    ActionStatus *status = (auth.cctStatusDict)[key];
                    if([status.status isEqualToString:@"SUCCESS" ])
                    {
                        [[MobileEntryManager sharedInstance] deleteBycctKey:key];
                    }
                    
                }
                
                for(NSString *key in auth.meStatusDict)
                {
                    ActionStatus *status = (auth.meStatusDict)[key];
                    if([status.status isEqualToString:@"SUCCESS" ])
                    {
                        [[MobileEntryManager sharedInstance] deleteByKey:key];
                    }
                    
                }

                // MOB-7484
                [ReportViewControllerBase refreshSummaryData];

				// Dismiss this dialog
				[self goToReportDetailScreen:auth];
			}
		}
	
	}
}


-(void)reportChosen:(BOOL) isRptNew
{
	if(![ExSystem connectedToNetwork])
	{
	}
	else
	{
		
		if (rptKey == nil)
		{
//			[self updateReportName];
		}
		
        if (self.delegate != nil) {
            if ([self.delegate respondsToSelector:@selector(didChooseReport:)]) {
                [self.delegate didChooseReport:rpt];
                [self.navigationController popViewControllerAnimated:YES];
            }
            
            return;
        }
        
		if(isCarMileage && rptKey != nil)
		{
            CarMileageData *carMileageData = [[CarMileageData alloc] init];
            [carMileageData userSelectedReport:rptKey rpt:rpt inView:self];
		}
		else if (isCarMileage && rptKey == nil && (reportName != nil && [reportName stringByTrimmingCharactersInSet:NSCharacterSet.whitespaceCharacterSet].length >0))
		{
			// add new report from car mileage
            [self showWaitViewWithText:[Localizer getLocalizedText:@"Adding to Report"]];
            [self sendAddToReportMsg];
		}
		else 
		{
			if (rptKey != nil || (reportName != nil && [reportName stringByTrimmingCharactersInSet:NSCharacterSet.whitespaceCharacterSet].length >0))
			{
                if (!isRptNew)
                {
                    NSString *addToReportConfirmMsg = [[NSString alloc] initWithString:[Localizer getLocalizedText:ADD_TO_RPT_CONFIRM_MSG]];
                    NSString *addToReportConfirmTitle = [[NSString alloc] initWithString:[Localizer getLocalizedText:ADD_TO_RPT_CONFIRM_TITLE]];
                    NSString *cancelText = [[NSString alloc] initWithString:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
                    NSString *addText = [[NSString alloc] initWithString:[Localizer getLocalizedText:LABEL_ADD_BTN]];
                    
                    MobileAlertView *alert = [[MobileAlertView alloc] 
                                              initWithTitle:addToReportConfirmTitle
                                              message:addToReportConfirmMsg 
                                              delegate:self 
                                              cancelButtonTitle:cancelText 
                                              otherButtonTitles:addText, nil];
                    alert.tag = kAlertViewConfirmSelectionTag;
                    self.alertViewConfirmSelection = alert;
                    [alert show];
                }
                else
                {
                    [self showWaitViewWithText:[Localizer getLocalizedText:@"Adding to Report"]];
                    [self sendAddToReportMsg];
                }
			}
			else 
			{
				NSString *fixRptSelMsg = [[NSString alloc] initWithString:[Localizer getLocalizedText:FIX_REPORT_NAME_MSG]];
				NSString *closeText = [[NSString alloc] initWithString:[Localizer getLocalizedText:LABEL_CLOSE_BTN]];
				
				MobileAlertView *alert = [[MobileAlertView alloc] 
									  initWithTitle:[Localizer getLocalizedText:@"Error"]
									  message:fixRptSelMsg
									  delegate:self 
									  cancelButtonTitle:closeText
									  otherButtonTitles:nil];
				alert.tag = kAlertViewFixReportNameTag;
				self.alertViewFixReportName = alert;
				[alert show];
			}
		}
		
	}
	
}


#pragma mark -
#pragma mark ViewController Methods
- (void)viewDidAppear:(BOOL)animated 
{
    if (self.rptKey == nil)
    {
        [self showWaitViewWithText:[Localizer getLocalizedText:@"Loading Data"]];
	
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                     SELECT_REPORT, @"TO_VIEW", nil];
        [[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORTS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
	}
	[super viewDidAppear:animated];
}

-(void)viewWillDisappear:(BOOL)animated
{
	isAcceptingData = NO;	// To prevent refresh upon receipt of network data after dismissal
	[super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
    //	NSLog(@"View did disappear");
	if (self.isCarMileage && self.padHomeVC != nil)
	{
		[self.padHomeVC wizardDlgDismissed];
	}
}

-(void)viewWillAppear:(BOOL)animated
{
	isAcceptingData = YES;
	isAddingToReport = NO;
	[super viewWillAppear:animated];
}

-(void) loadHeaderView
{
    SelectReportTableHeader *header = [[SelectReportTableHeader alloc] initWithFrame:CGRectMake(0, 0, 320, 130)];
    self.tableView.tableHeaderView = header;
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
	isAcceptingData = YES;
	isAddingToReport = NO;
	
	rptList = [[NSMutableArray alloc] init];

	//navBar.topItem.title = [Localizer getViewTitle:[self getViewIDKey]];
	self.title = [Localizer getViewTitle:[self getViewIDKey]];
	[tableView setBackgroundColor:[UIColor whiteColor]];
    //[tableView setBackgroundColor:[UIColor colorWithRed:0.882871 green:0.887548 blue:0.892861 alpha:1]];

    [self loadHeaderView];
	[self setupToolbar];
	
	if([UIDevice isPad])
	{
        // Put close button on iPad model view
		UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
		self.navigationItem.leftBarButtonItem = nil;
		self.navigationItem.leftBarButtonItem = btnCancel;
		
	}

    [super viewDidLoad];
}

-(void)closeView:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];
}



- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	[tableView reloadData];
}



- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
    [super viewDidUnload];
}

- (void)dealloc 
{
	if (alertViewConnectionError != nil)
		[alertViewConnectionError clearDelegate];
	
	if (alertViewConfirmSelection != nil)
		[alertViewConfirmSelection clearDelegate];
	
	if (alertViewFixReportName != nil)
		[alertViewFixReportName clearDelegate];
	
	
	
}

#pragma mark -
#pragma mark UIAlertViewDelegate

- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex {
	if (alertView.tag == kAlertViewConnectionErrorTag && alertViewConnectionError != nil)
	{
		[alertViewConnectionError clearDelegate];
		alertViewConnectionError = nil;
	}
	else if (alertView.tag == kAlertViewConfirmSelectionTag && alertViewConfirmSelection != nil)
	{
		[alertViewConfirmSelection clearDelegate];
		alertViewConfirmSelection = nil;
	}
	else if (alertView.tag == kAlertViewFixReportNameTag && alertViewFixReportName != nil)
	{
		[alertViewFixReportName clearDelegate];
		alertViewFixReportName = nil;
	}

	if (buttonIndex == 0){
		[self hideWaitView];

	}
	if (buttonIndex == 1){
        [self showWaitViewWithText:[Localizer getLocalizedText:@"Adding to Report"]];
        [self sendAddToReportMsg];
	}
}

-(void)sendAddToReportMsg
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 SELECT_REPORT, @"TO_VIEW",
                                 self.meKeys, @"ME_KEYS", 
                                 nil];
    if (pctKeys != nil)
        pBag[@"PCT_KEYS"] = self.pctKeys;
    
    if (cctKeys != nil)
        pBag[@"CCT_KEYS"] = self.cctKeys;
    
    if (rcKeys != nil)
        pBag[@"RC_KEYS"] = self.rcKeys;
    
    if (rptKey != nil)
        pBag[@"RPT_KEY"] = self.rptKey;
    
    if (reportName != nil)
        pBag[@"REPORT_NAME"] = reportName;
    
    if (meAtnMap != nil)
        pBag[@"ME_ATN_MAP"] = meAtnMap;

    if (self.smartExpenseList != nil) {
        pBag[@"SmartExpenseList"] = self.smartExpenseList;
    }
    if ([Config isEreceiptsEnabled]) {
        // add smartExpense id's to pBag.
        pBag[@"SmartExpenseIds"] = self.smartExpenseIds;
    }
    isAddingToReport = YES;
    [[ExSystem sharedInstance].msgControl createMsg:ADD_TO_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    
}

-(void) goToReportDetailScreen:(AddToReportData*)ard  // Add to Report from OOP List
{
	if([UIDevice isPad])
	{

		ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        
        UINavigationController *homeNavigationController = (UINavigationController*)delegate.navController;
        
		[self dismissViewControllerAnimated:YES completion:nil];

		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: ard.rpt, @"REPORT", ard.rpt.rptKey, @"ID_KEY", 
									 ard.rpt, @"REPORT_DETAIL", 
                                     ard.rpt.rptKey, @"RECORD_KEY", 
                                     ROLE_EXPENSE_TRAVELER, @"ROLE",
                                     @"YES", @"SHORT_CIRCUIT"
									 ,nil];

        pBag[@"COMING_FROM"] = @"REPORT"; // Force it to fe

        ReportDetailViewController_iPad *newDetailViewController = [[ReportDetailViewController_iPad alloc] initWithNibName:@"ReportDetailViewController_iPad" bundle:nil];
		newDetailViewController.role = ROLE_EXPENSE_TRAVELER;
		//newDetailViewController.iPadHome = homeVC;
		newDetailViewController.isReport = YES;
        
        [homeNavigationController pushViewController:newDetailViewController animated:YES];
        
		//[iPadHome switchToDetail:@"Report" ParameterBag:pBag];
        [newDetailViewController loadReport:pBag]; // Note that the original implementation called
	}
	else 
	{
        ReportDetailViewController *detailVC = [[ReportDetailViewController alloc] initWithNibName:@"ReportHeaderView" bundle:nil];
//        NSArray *newNavStack = [NSArray arrayWithObjects:[self.navigationController.viewControllers objectAtIndex:0], detailVC, nil];
//        [self.navigationController setViewControllers:newNavStack animated:YES];

        // MOB-12689 setViewControllers cause the custom back button to stack on top of default back button
        // causes back button in later screen to be hidden.
        UINavigationController* nav = self.navigationController;
        [nav popToRootViewControllerAnimated:NO];
        [nav pushViewController:detailVC animated:YES];
        
		NSMutableDictionary * pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
									  ard.rpt.rptKey, @"ID_KEY",
									  ard.rpt, @"REPORT",
									  ard.rpt.rptKey, @"RECORD_KEY",
                                      ROLE_EXPENSE_TRAVELER, @"ROLE",
                                      @"YES", @"SHORT_CIRCUIT", nil];
        Msg *msg = [[Msg alloc] init];
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        [detailVC respondToFoundData:msg];
	}
}

#pragma mark -
#pragma mark Table View Data Source Methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView 
{
//    return 1;
    return [self.sections count];
}


// Customize the number of rows in the table view.
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
//	if (section == 0)
//	{
//		return 1;
//	}
//	else
//	{
		if (rptList == nil)
		{
			return 0;
		}
		else
		{
            NSString* sectionKey = sections[section];
            NSMutableArray* secRptList = sectionDataMap[sectionKey];
            return [secRptList count];
			//return [rptList count];
		}
//	}
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
{
//		return [Localizer getLocalizedText:@"Existing Reports"];
    return sections[section];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tblView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    NSString* sectionKey = sections[section];
    NSMutableArray* secRptList = sectionDataMap[sectionKey];
	ReportData *selectedRpt = secRptList[row];

	SelectReportCell *cell = (SelectReportCell *)[tblView dequeueReusableCellWithIdentifier: @"SelectReportCell"];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SelectReportCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[SelectReportCell class]])
				cell = (SelectReportCell *)oneObject;
	}
    //MOB-6353 use requested amt.
	NSString *amt = [FormatUtils formatMoney:selectedRpt.totalClaimedAmount crnCode:selectedRpt.crnCode];
	cell.lblName.text = selectedRpt.reportName;
	cell.lblAmount.text = amt;
	cell.lblDate.text = [CCDateUtilities formatDateToMMMddYYYFromString:selectedRpt.reportDate];

	if(![UIDevice isPad])
	{
		[UtilityMethods drawNameAmountLabelsOrientationAdjusted:cell.lblName AmountLabel:cell.lblAmount LeftOffset:10 RightOffset:10
											Width:tblView.frame.size.width];

		cell.lblDate.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleRightMargin; 
	}
		
	return cell;
}

#pragma mark -
#pragma mark Table Delegate Methods 

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    NSString* sectionKey = sections[section];
    NSMutableArray* secRptList = sectionDataMap[sectionKey];
	self.rpt = secRptList[row];

//	self.rpt = [rptList objectAtIndex:(row)];
	self.rptKey = rpt.rptKey;
	self.reportName = nil;
	[self reportChosen:FALSE];

}



- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 54;
}


#pragma mark -
#pragma mark Create Report Methods
-(void)closeMe:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];
}


-(void)setupToolbar
{
	if([ExSystem connectedToNetwork])
    {
		UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
		
        self.navigationItem.rightBarButtonItem = nil;
        [self.navigationItem setRightBarButtonItem:btnAdd animated:NO];
        
//        [self.navBar.topItem setRightBarButtonItem:btnAdd animated:NO];

	}
    
    if([UIDevice isPad])
    {
        // MOB-6323 Do not use Close when popped above Expense List
        NSUInteger count = [self.navigationController.viewControllers count];
        // No other views behind this.  No need to set up fake back up button
        int viewIndex = 0;
        for (int ix = 0; ix <count; ix++)
        {
            MobileViewController* vc = (MobileViewController*) (self.navigationController.viewControllers)[ix];
            if (vc == self)
            {
                viewIndex = ix;
                break;
            }
        }
        
        if (viewIndex < 1)
        {

            //self.contentSizeForViewInPopover = CGSizeMake(320.0, 360.0);
            UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeMe:)];
            self.navigationItem.leftBarButtonItem = btnClose;
        }
    }

}

-(IBAction) buttonAddPressed:(id)sender
{
	ReportSummaryViewController *vc = [[ReportSummaryViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
    vc.title = [Localizer getLocalizedText:@"Create Report"];
	vc.role = ROLE_EXPENSE_TRAVELER;
    vc.delegate = self;
    // TODO - Pass in delegate
	// Retrieve report form data
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 vc.role, @"ROLE_CODE",
								 nil];
    [self.navigationController pushViewController:vc animated:YES];
    
    /*
    // Pretty sure both of these should use the same push code
    if ([UIDevice isPad]) {
        [self.navigationController pushViewController:vc animated:YES];
	} else {
        [self.parentMVC.navigationController pushViewController:vc animated:YES];
    }
    */
    [[ExSystem sharedInstance].msgControl createMsg:REPORT_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:vc];
    
	
}

-(void) reportCreated:(ReportData*) newRpt
{
    [self.navigationController popViewControllerAnimated:NO];
    self.rptKey = newRpt.rptKey;
    self.rpt = newRpt;
    [self reportChosen:TRUE];
}

@end

