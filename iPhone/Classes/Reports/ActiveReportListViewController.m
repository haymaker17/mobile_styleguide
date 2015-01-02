//
//  ActiveReportListViewController.m
//  ConcurMobile
//
//  Created by yiwen on 4/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ActiveReportListViewController.h"
#import "ExSystem.h" 

#import "FormatUtils.h"
#import "ReportData.h"
#import "ActiveReportListData.h"
#import "ActiveReportListCell.h"
#import "ImageUtil.h"
#import "ReportDetailViewController_iPad.h"
#import "ReportSummaryViewController.h"
#import "MobileAlertView.h"
#import "DeleteReportData.h"
#import "ReportDetailViewController.h"
#import "FeedbackManager.h"
#import "Flurry.h"
#import "EntityReport.h"
#import "ReportManager.h"
#import "iPadHome9VC.h"
#import "Config.h"
#import "HelpOverlayFactory.h"
#import "UserConfig.h"

@implementation ActiveReportListViewController
@synthesize aKeys, rals, ivBack, lblBack, titleLabel, showedNo, fetch, doReload, isPad;
@synthesize /*xctImage, cmtImage, resImage, rctImage,*/ iPadHome, fromMVC,groupedReportsLookup,aSections;

@synthesize fetchedResultsController=__fetchedResultsController;
@synthesize managedObjectContext=__managedObjectContext;
@synthesize reportCacheData;

#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return ACTIVE_REPORTS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

-(NSString*) getNoListItemMessage
{
	return [Localizer getLocalizedText:@"No Active Reports"];
}

-(NSString*) getMsgId
{
	return ACTIVE_REPORTS_DATA;
}

- (NSString*) titleForNoDataView
{
    return [Localizer getLocalizedText:@"No Reports"];
}

-(void)respondToFoundData:(Msg *)msg
{   
    if ([msg.idKey isEqualToString:DOWNLOAD_USER_CONFIG])  // Don't want to trigger a NoData view
        return;
    
	if ([msg.idKey isEqualToString:[self getMsgId]])
	{
		ListDataBase *lst = (ListDataBase *)msg.responder;
			
		self.aKeys = lst.keys;
		self.rals = lst.objDict;
		[self prepareReportGroups];
		[self.tableView reloadData];
        
        if ([self isViewLoaded]) {
            [self hideWaitView];
            [self hideLoadingView];
            [self hideNoDataView];
        }
        
        NSArray *group = (NSArray*)groupedReportsLookup[@"Unsubmitted Reports"];
        NSArray *subGroup = (NSArray*)groupedReportsLookup[@"Submitted Reports"];
        NSDictionary *dictionary = @{@"Unsubmitted Count": [NSString stringWithFormat:@"%lu", (unsigned long)[group count]],@"Submitted Pending Count": [NSString stringWithFormat:@"%lu", (unsigned long)[subGroup count]]};
        [Flurry logEvent:@"Reports: List" withParameters:dictionary];
        
        if ([lst isKindOfClass:[ActiveReportListData class]])
        {//MOB-7716
            ActiveReportListData* arl = (ActiveReportListData*)lst;
            NSArray* unsubmittedRptlist = [arl getUnsubmittedReports];        
            [[ReportManager sharedInstance] saveUnsubmittedReportList:unsubmittedRptlist];
        }
        
        if ([@"YES" isEqualToString: (msg.parameterBag)[@"REFRESHING"]])
        {
            [self doneRefreshing];
        }
	}
	else if ([msg.idKey isEqualToString:DELETE_REPORT_DATA])
	{
		NSString* errMsg = msg.errBody;
		DeleteReportData* srd = (DeleteReportData*) msg.responder;
        
		if (errMsg == nil && srd != nil)
		{
			if (srd.actionStatus != nil && srd.actionStatus.errMsg != nil)
			{
				errMsg = srd.actionStatus.errMsg;
			}
		}
		
		if (errMsg != nil) 
		{
            if ([self isViewLoaded]) {
                [self hideWaitView];
            }
            
            self.isRptOpen = NO;        // user won't bring back to Home after close the model.
            
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:[Localizer getLocalizedText:@"Delete Report Failed"]
								  message:errMsg
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
		}
		else 
		{
            // Let refresh msg dismiss the wait view
            
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
										 [self getViewIDKey], @"TO_VIEW", nil];
			[[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORTS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

			// Use new homevc and post refresh call if you can
            // Any new home should have refreshsummaryData so it can be called to refresh expenses
            UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
            if ([homeVC respondsToSelector:@selector(refreshSummaryData)])
            {
                [homeVC performSelector:@selector(refreshSummaryData) withObject:nil];
            }
		}
	}
	
    if ([self isViewLoaded]) {
        if (aKeys == nil || [aKeys count] < 1) 
        {//show we gots no data view
            
            [self showNoDataView:self];
        }
        else if (aKeys != nil & [aKeys count] > 0)
        {//refresh from the server, after an initial no show...
            [self hideNoDataView];
        }
    }
}

-(void)updateReport:(ReportData*) rpt
{
	rals[rpt.rptKey] = rpt;
	self.doReload = YES;
}

-(void)prepareReportGroups
{
	self.aSections = nil;
	if (self.enablefilterUnsubmittedActiveReports)
	{
		self.aSections = [[NSMutableArray alloc] initWithObjects:@"Unsubmitted Reports",nil];
	}
	else 
	{
		self.aSections = [[NSMutableArray alloc] initWithObjects:@"Unsubmitted Reports",@"Submitted Reports",@"Paid Reports",@"Other Reports",nil];	
	}
	
	self.groupedReportsLookup = nil;
	if (self.enablefilterUnsubmittedActiveReports)
	{
		self.groupedReportsLookup = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								[NSMutableArray arrayWithObjects:nil], @"Unsubmitted Reports",nil];
	}
	else 
	{
		self.groupedReportsLookup = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 [NSMutableArray arrayWithObjects:nil], @"Unsubmitted Reports",
								 [NSMutableArray arrayWithObjects:nil], @"Submitted Reports",
								 [NSMutableArray arrayWithObjects:nil], @"Paid Reports",
								 [NSMutableArray arrayWithObjects:nil], @"Other Reports", nil];
	}
	
	if (aKeys!= nil && rals != nil && [aKeys count] > 0) 
	{
		for (NSString *key in aKeys) 
		{
			ReportData *rptData = rals[key];
			
			if (self.enablefilterUnsubmittedActiveReports)
			{
				if ([[self getReportGroupName:rptData] isEqualToString:@"Unsubmitted Reports"]) 
				{
					[self addToGrouped:rptData];
				}
				
			}
			else 
			{
				[self addToGrouped:rptData];
			}
		}
	}
	
	for (NSString *sectionKey in aSections) 
	{
		NSMutableArray *group = (NSMutableArray*)groupedReportsLookup[sectionKey];
		if (group != nil)
		{
			NSSortDescriptor *dateDescriptor = [[NSSortDescriptor alloc] initWithKey:@"reportDate" ascending:NO];
			NSArray *sortDescriptors = @[dateDescriptor];
			NSArray *sortedArray = [group sortedArrayUsingDescriptors:sortDescriptors];
			if (sortedArray != nil)
			{
				groupedReportsLookup[sectionKey] = sortedArray;
			}
		}
	}
}

-(void)addToGrouped:(ReportData*)rpt
{
	NSString *key = nil;
	key = [self getReportGroupName:rpt];
	
	if (key != nil)
	{	
		NSMutableArray *group = (NSMutableArray*)groupedReportsLookup[key];
		[group addObject:rpt];
	}
}

-(NSString*)getReportGroupName:(ReportData*)rpt
{
	/*
	 There will be 4 groups: 
	 Unsubmitted Reports: 
	 For apsKeys => A_NOTF, A_RESU
	 Submitted Reports:
	 For apsKeys => A_APPR, A_ACCO, A_PEND, A_EXTV, A_FILE, A_PECO, A_PVAL,A_RHLD,A_TEXP & payKey => P_PROC, P_PAYC
	 Paid Reports:
	 For payKey => P_PAID
	 Others: Any non standard values of apsKey/payKeys will fall in this group
	 */
	NSString *apsKey = rpt.apsKey;
	NSString *payKey = rpt.payKey;
	
	if ([apsKey isEqualToString:@"A_NOTF"] || [apsKey isEqualToString:@"A_RESU"]) 
	{
		return @"Unsubmitted Reports";
	}
	else if (![payKey isEqualToString:@"P_PAID"] && ([apsKey isEqualToString:@"A_APPR"] 
													 || [apsKey isEqualToString:@"A_ACCO"] 
													 || [apsKey isEqualToString:@"A_PEND"]
													 || [apsKey isEqualToString:@"A_EXTV"]
													 || [apsKey isEqualToString:@"A_FILE"]
													 || [apsKey isEqualToString:@"A_PECO"]
													 || [apsKey isEqualToString:@"A_PVAL"]
													 || [apsKey isEqualToString:@"A_RHLD"]
													 || [apsKey isEqualToString:@"A_TEXP"]
													 || [payKey isEqualToString:@"P_PROC"] 
													 || [payKey isEqualToString:@"P_PAYC"]
													 ))
	{
		return @"Submitted Reports";
	}
	else if ([payKey isEqualToString:@"P_PAID"])
	{
		return @"Paid Reports";
	}
	else 
	{
		return @"Other Reports";
	}
}

#pragma mark -
#pragma mark ActionStatus
-(void)closeView:(id)sender
{
	if([UIDevice isPad])
	{
        if (self.isRptOpen)
        {
            // After delete openning reporting
            // User will be nevigated to home screen.
            iPadHome9VC *padVC = (iPadHome9VC*) [ConcurMobileAppDelegate findHomeVC];
            [padVC.navigationController popToRootViewControllerAnimated:YES];

// OR, get report list to pop up on home after navigation.
//            if ([padVC respondsToSelector:@selector(btnReportsPressed:)])
//            {
//                [padVC.navigationController popToRootViewControllerAnimated:YES];
//                [padVC performSelector:@selector(btnReportsPressed:) withObject:nil afterDelay:0.1f];
//            }
        }
        [self dismissViewControllerAnimated:YES completion:nil];
	}
}


#pragma mark -
#pragma mark ViewController Methods
-(void)viewWillDisappear:(BOOL)animated
{
	self.enablefilterUnsubmittedActiveReports = NO;
	[super viewWillDisappear:animated];
}

-(void)viewWillAppear:(BOOL)animated 
{ 
	if(doReload)
	{
		doReload = NO;
		if(aKeys == nil || [aKeys count] == 0)
		{
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
			[[ExSystem sharedInstance].msgControl createMsg:[self getMsgId] CacheOnly:@"YES" ParameterBag:pBag SkipCache:NO RespondTo:self];
		}
		
		[self.tableView reloadData];
	}

	// MOB-8370 For submit confirmation
    if ([UserConfig getSingleton] == nil)
		[[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_USER_CONFIG CacheOnly:@"NO" ParameterBag:nil SkipCache:YES];

	[super viewWillAppear:animated]; 
}

- (void)viewDidAppear:(BOOL)animated 
{
	[super viewDidAppear:animated];

    [self loadReports];
    
    // Add Test drive help overlay
    BOOL didShowHelpOverlay;
    
    if ([UIDevice isPad]) {
        didShowHelpOverlay = [HelpOverlayFactory addiPadReportListOverlayToView:self.navigationController.view];
    } else {
        didShowHelpOverlay = [HelpOverlayFactory addiPhoneReportListOverlayToView:self.navigationController.view];
    }
    
    if (!didShowHelpOverlay) {
        if ([[FeedbackManager sharedInstance] showRatingOnNextView] && ![Config isGov]) {
            [[FeedbackManager sharedInstance] requestRatingFromViewController:self withBlock:nil];
        }
    }
}

-(IBAction) buttonAddPressed:(id)sender
{
	ReportSummaryViewController *vc = [[ReportSummaryViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
    vc.title = [Localizer getLocalizedText:@"Create Report"];
	vc.role = ROLE_EXPENSE_TRAVELER;
    vc.delegate = self;
	// Retrieve report form data
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 vc.role, @"ROLE_CODE",
								 nil];

	[self.navigationController pushViewController:vc animated:YES];
    
    [[ExSystem sharedInstance].msgControl createMsg:REPORT_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:vc];
}

-(void)setupToolbar
{
	if(![ExSystem connectedToNetwork])
		[self makeOfflineBar];
	else {
		UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
		
        [self.navigationItem setRightBarButtonItem:btnAdd];
	}
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
    [ExSystem sharedInstance].sys.topViewName = self.getViewIDKey;
    
	if([UIDevice isPad])
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
		self.navigationItem.leftBarButtonItem =  [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
	}	
	
	[self.tableView setBackgroundColor:[UIColor whiteColor]];
	
	if (aKeys == nil)
		aKeys = [[NSMutableArray alloc] init];
	
	if (rals == nil)
		rals = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	self.title = [Localizer getViewTitle:[self getViewIDKey]];
	
	[self setupToolbar];
    [self showLoadingView];
    [super viewDidLoad];
}

-(void)timeToDie
{
	doReload = YES;
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
	[self timeToDie];
    [super didReceiveMemoryWarning];
    NSLog(@"LOW MEMORY WARNNG from ActiveReportListViewController");
    // Release any cached data, images, etc that aren't in use.
}


- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}



#pragma mark - Fetched results controller
- (NSFetchedResultsController *)fetchedResultsController 
{
    if (__fetchedResultsController != nil) {
        return __fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityReport" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"sectionPosition" ascending:YES];
    NSSortDescriptor *sort2 = [[NSSortDescriptor alloc] initWithKey:@"reportDate" ascending:YES];
    [fetchRequest setSortDescriptors:@[sort, sort2]];
    
    NSFetchedResultsController *theFetchedResultsController = 
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest 
                                        managedObjectContext:self.managedObjectContext sectionNameKeyPath:@"sectionPosition" 
                                                   cacheName:@"Root"];
    self.fetchedResultsController = theFetchedResultsController;
    __fetchedResultsController.delegate = self;
    
    
    return __fetchedResultsController;    
}


#pragma mark -
#pragma mark Report delete methods

-(ReportData*) getReportAtIndexPath:(NSIndexPath*) indexPath
{
	ReportData *rpt = nil;
	if (groupedReportsLookup != nil && [groupedReportsLookup count] >0) 
	{
		NSArray *rptGroup = (NSArray*)groupedReportsLookup[aSections[[indexPath section]]];
		if (rptGroup != nil)
		{
			rpt = rptGroup[[indexPath row]];
		}
	}
	else if(rpt == nil)
	{
		NSString *key = aKeys[[indexPath row]];
		rpt = rals[key];
	}

	return rpt;
}

// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    
	if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
		ReportData* rpt = [self getReportAtIndexPath:indexPath];
        
        if (self.fromMVC != nil)
        {
            ReportDetailViewController_iPad *rptIPad = (ReportDetailViewController_iPad*) self.fromMVC;
            self.isRptOpen = [rptIPad.rpt.rptKey isEqualToString:rpt.rptKey] ? YES : NO;
        }
        
        if (self.isRptOpen)
        {
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[@"Report is open" localize] message:[@"Are you sure you want to delete the open report?" localize] delegate:self cancelButtonTitle:[@"No" localize] otherButtonTitles:[@"Yes" localize], nil];
            [av show];
        }
        else
        {
            [self deleteReportWith:rpt];
		}
	}
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }
}

-(void) deleteReportWith:(ReportData*)rpt
{
    if (rpt != nil && ( [rpt.apsKey isEqualToString:@"A_NOTF"] || [rpt.apsKey isEqualToString:@"A_RESU"]) )
    {
        [self showWaitViewWithText:[Localizer getLocalizedText:@"Deleting Report"]];
        
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                     rpt.rptKey, @"RPT_KEY",
                                     [self getViewIDKey], @"TO_VIEW", @"YES", @"SKIP_CACHE", nil];
        [[ExSystem sharedInstance].msgControl createMsg:DELETE_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)aTableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    // Detemine if it's in editing mode
	ReportData* rpt = [self getReportAtIndexPath:indexPath];
	if (rpt != nil && ([rpt.apsKey isEqualToString:@"A_NOTF"] || [rpt.apsKey isEqualToString:@"A_RESU"])) 
	{
        return UITableViewCellEditingStyleDelete;
    }
    return UITableViewCellEditingStyleNone;
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView 
{
	return (aSections != nil && [aSections count] >0)?[aSections count]:1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	if (aSections != nil && [aSections count] >0 && [groupedReportsLookup count] >0) 
	{
		NSArray *group = (NSArray*)groupedReportsLookup[aSections[section]];
		return [group count];
	}
	else 
	{
		return [aKeys count];
	}
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
{
	NSString *key  = nil;
	if (groupedReportsLookup != nil && [groupedReportsLookup count] >0 && [aSections count] >0) 
	{
		key = aSections[section];
	}
	
	if (key!= nil) 
	{
		NSMutableArray *a = (NSMutableArray*)groupedReportsLookup[key];
		if (a != nil && [a count] > 0) 
		{
			return [Localizer getLocalizedText:key];
		}
		else 
		{
			return @"";
		}	
	}
	else 
	{
		return nil;
	}	
}

- (UIImageView*) getImageView : (ActiveReportListCell*)cell atPosition: (int) pos
{
	if (pos == 0)
		return cell.image1;
	else if (pos == 1)
		return cell.image2;
	else if (pos ==2)
		return cell.image3;
	else if (pos ==3)
		return cell.image4;
	
	return nil;
}


- (UITableViewCell *)tableView:(UITableView *)tblView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{	
	ReportData *rpt = nil;
	if (groupedReportsLookup != nil && [groupedReportsLookup count] >0) 
	{
		NSArray *rptGroup = (NSArray*)groupedReportsLookup[aSections[[indexPath section]]];
		if (rptGroup != nil)
		{
			rpt = rptGroup[[indexPath row]];
		}
	}
	else if(rpt == nil)
	{
		NSString *key = aKeys[[indexPath row]];
		rpt = rals[key];
	}
	
	ActiveReportListCell *cell = (ActiveReportListCell *)[tblView dequeueReusableCellWithIdentifier: @"ActiveReportListCell"];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ActiveReportListCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[ActiveReportListCell class]])
				cell = (ActiveReportListCell *)oneObject;
	}
//	NSString *amt = [FormatUtils formatMoney:rpt.totalPostedAmount crnCode:rpt.crnCode];
    // MOB-6353 Use requested amount
    NSString *amt = [FormatUtils formatMoney:rpt.totalClaimedAmount crnCode:rpt.crnCode];
	cell.lblName.text = rpt.reportName;
	cell.lblAmount.text = amt;
	cell.lblDate.text = [CCDateUtilities formatDateToMMMddYYYFromString:rpt.reportDate];
	cell.lblRptStatus.text = rpt.apvStatusName;
    
    if ([rpt.aprvEmpName length])
        cell.lblRptStatus.text = [NSString stringWithFormat:@"%@ - %@", rpt.apvStatusName, rpt.aprvEmpName];

	[UtilityMethods drawNameAmountLabels:cell.lblName AmountLabel:cell.lblAmount];

//	cell.lblDate.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleRightMargin; 
//	cell.lblRptStatus.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleRightMargin; 
	
	int imageCount = 0;
	if(rpt.hasException != nil && [rpt.hasException isEqualToString:@"Y"])
	{
		imageCount ++;
	}
 	//MOB-11325 - display Report ready to submit flag
    if(rpt.prepForSubmitEmpKey!=nil &&  [rpt.prepForSubmitEmpKey isEqualToString:@"Y"]) 
	{
		imageCount ++;
	}
    
	int imagePos = 4-imageCount;  // starting position of image views
	for (int ix = 0; ix < imagePos; ix++)
	{
		UIImageView* imageView = [self getImageView:cell atPosition:ix];
		[imageView setImage:nil];
	}
	
	if(rpt.hasException != nil && [rpt.hasException isEqualToString:@"Y"])
	{
		BOOL showAlert = [rpt.severityLevel isEqualToString:@"ERROR"];
		
		UIImageView* imageView = [self getImageView:cell atPosition:imagePos];
        UIImage* xctImage = nil;
		if (showAlert) 
			xctImage = [UIImage imageNamed:@"icon_redex"];
		else 
			xctImage = [UIImage imageWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"icon_yellowex" ofType:@"png"]];
		[imageView setImage: xctImage];
        imagePos++;
	}
    
     //MOB-11325 - display Report ready to submit flag
    if (rpt.prepForSubmitEmpKey!=nil &&  [rpt.prepForSubmitEmpKey isEqualToString:@"Y"])
	{
        
        UIImageView* imageView = [self getImageView:cell atPosition:imagePos];
        UIImage* xctImage = nil;
        xctImage = [UIImage imageNamed:@"icon_ready_submit"];
        [imageView setImage: xctImage];
    }
	cell.rpt = rpt;
	
	[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
	
	return cell;
}

#pragma mark -
#pragma mark Table Delegate Methods 
-(NSIndexPath *)tableView:(UITableView *)tableView 
 willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    return indexPath; 
}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
  	if(![ExSystem connectedToNetwork])
    {
        NSDictionary *dict = @{@"Type": @"Report"};
        [Flurry logEvent:@"Offline: Viewed" withParameters:dict];
    }
    
    NSUInteger row = [newIndexPath row];
	NSUInteger section = [newIndexPath section];

	ReportData *rpt = nil;
	if (groupedReportsLookup != nil && [groupedReportsLookup count] >0) 
	{
		NSArray *rptGroup = (NSArray*)groupedReportsLookup[aSections[section]];
		if (rptGroup != nil)
		{
			rpt = rptGroup[row];
		}
	}
	else if(rpt == nil)
	{
		NSString *key = aKeys[row];
		rpt = rals[key];
	}
	
	if(![UIDevice isPad])
	{
//        [ReportDetailViewController showReportDetail:self withReport:rpt withRole:ROLE_EXPENSE_TRAVELER];

		//NSLog(@"ApprovalList::didSelectRowAtIndexPath rpt.rptKey = %@ AND key = %@", rpt.rptKey, key);
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: rpt, @"REPORT", ROLE_EXPENSE_TRAVELER, @"ROLE", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY", @"YES", @"SHORT_CIRCUIT", nil]; //, @"YES", @"SKIP_PARSE"
		[ConcurMobileAppDelegate switchToView:ACTIVE_ENTRIES viewFrom:ACTIVE_REPORTS ParameterBag:pBag];
	}
	else 
	{
		NSMutableDictionary *pBag = nil;
		NSMutableArray *allKeys = [[NSMutableArray alloc] init];
		NSMutableDictionary *allReports = [[NSMutableDictionary alloc] init];
		
		if ([groupedReportsLookup count] > 0 && [aSections count] >0) 
		{
			for (NSString *sectionKey in aSections)
			{
				NSArray *group = (NSArray*)groupedReportsLookup[sectionKey];
				for (ReportData *rpt in group)
				{
					[allKeys addObject:rpt.rptKey];
					allReports[rpt.rptKey] = rpt;
				}
			}
			
			pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY", @"YES", @"SHORT_CIRCUIT"
					,allKeys, @"REPORT_KEYS", allReports, @"REPORT_DICT", nil]; //, @"YES", @"SKIP_PARSE"	
		}
		else 
		{
			pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY", @"YES", @"SHORT_CIRCUIT"
					,aKeys, @"REPORT_KEYS", rals, @"REPORT_DICT", nil]; //, @"YES", @"SKIP_PARSE"			
		}
        
        pBag[@"COMING_FROM"] = @"REPORT";

		if(fromMVC != nil)
			[fromMVC dismissPopovers];
        
		if([UIDevice isPad]){
			[self dismissViewControllerAnimated:YES completion:nil];
        }
        
        // If the report detail screen is already being shown, then pop it
        iPadHome9VC *homeVC = (iPadHome9VC*)[ConcurMobileAppDelegate findHomeVC];
        if ([homeVC.navigationController.topViewController isKindOfClass:[ReportDetailViewController_iPad class]])
            [homeVC.navigationController popViewControllerAnimated:NO];

        ReportDetailViewController_iPad *newDetailViewController = [[ReportDetailViewController_iPad alloc] initWithNibName:@"ReportDetailViewController_iPad" bundle:nil];
		newDetailViewController.role = ROLE_EXPENSE_TRAVELER;
		newDetailViewController.isReport = YES;
        
        UINavigationController *homeNavigationController = (UINavigationController*)self.presentingViewController;
        [homeNavigationController pushViewController:newDetailViewController animated:YES];

        [newDetailViewController loadReport:pBag]; // Note that the original implementation called switchTopDetail which pushed the VC first and then called loadReport
	}
    [self.tableView deselectRowAtIndexPath:newIndexPath animated:NO];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 60;
}

#pragma mark -
#pragma mark iPad Stuff
-(void)loadReports
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
	[[ExSystem sharedInstance].msgControl createMsg:[self getMsgId] CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
}

#pragma NoDataMasterViewDelegate method
-(void) actionOnNoData:(id)sender
{
    [self buttonAddPressed:nil];
}

#pragma ReportCreatedDelegate method
-(void) goToReportDetailScreen:(ReportData*)newRpt
{
	if([UIDevice isPad])
	{

        NSMutableDictionary * pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
									  //@"YES", @"POPTOVIEW",
									  newRpt.rptKey, @"ID_KEY",
									  newRpt, @"REPORT",
									  newRpt.rptKey, @"RECORD_KEY",
                                      ROLE_EXPENSE_TRAVELER, @"ROLE",
                                      @"YES", @"REPORT_CREATE_WIZARD",
                                      @"YES", @"SHORT_CIRCUIT", nil];
        
        // this forces the next screen to at least refresh the summary.  This pBag system is dumb. - Ernest
        pBag[@"COMING_FROM"] = @"REPORT";
        
        [self.parentViewController dismissViewControllerAnimated:YES completion:nil];
        ReportDetailViewController_iPad *newDetailViewController = [[ReportDetailViewController_iPad alloc] initWithNibName:@"ReportDetailViewController_iPad" bundle:nil];
		newDetailViewController.role = ROLE_EXPENSE_TRAVELER;
		newDetailViewController.isReport = YES;
        
        UINavigationController *homeNavigationController = (UINavigationController*)self.presentingViewController;
        [homeNavigationController pushViewController:newDetailViewController animated:YES];
        [newDetailViewController loadReport:pBag];

        /*
		//double dismiss does not work
		//[self dismissViewControllerAnimated:YES completion:nil];
		//[self dismissViewControllerAnimated:YES completion:nil];	
		// use grand-parent to dismiss worked
		[self.parentViewController dismissModalViewControllerAnimated:YES];	
        
		// use homeVC dismiss worked
		ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
		[delegate.padHomeVC dismissModalViewControllerAnimated:YES];
        
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: newRpt, @"REPORT", newRpt.rptKey, @"ID_KEY", 
									 newRpt, @"REPORT_DETAIL", 
                                     newRpt.rptKey, @"RECORD_KEY", 
                                     ROLE_EXPENSE_TRAVELER, @"ROLE",
                                     @"YES", @"REPORT_CREATE_WIZARD",
                                     @"YES", @"SHORT_CIRCUIT"
									 ,nil];
		
		//[delegate.padHomeVC switchToDetail:@"Report" ParameterBag:pBag];
		// Remove switch to detail and use new homevc
        iPadHome9VC *homeVC = (iPadHome9VC*)[ConcurMobileAppDelegate findHomeVC];
        if ([homeVC.navigationController.topViewController isKindOfClass:[ReportDetailViewController_iPad class]])
            [homeVC.navigationController popViewControllerAnimated:NO];
        
        ReportDetailViewController_iPad *newDetailViewController = [[ReportDetailViewController_iPad alloc] initWithNibName:@"ReportDetailViewController_iPad" bundle:nil];
		newDetailViewController.role = ROLE_EXPENSE_TRAVELER;
		newDetailViewController.isReport = YES;
        
        UINavigationController *homeNavigationController = (UINavigationController*)self.presentingViewController;
        [homeNavigationController pushViewController:newDetailViewController animated:YES];
        [newDetailViewController loadReport:pBag];
         */
		
	}
	else 
	{
		NSMutableDictionary * pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
									  //@"YES", @"POPTOVIEW", 
									  newRpt.rptKey, @"ID_KEY", 
									  newRpt, @"REPORT",
									  newRpt.rptKey, @"RECORD_KEY", 
                                      ROLE_EXPENSE_TRAVELER, @"ROLE",
                                      @"YES", @"REPORT_CREATE_WIZARD",
                                      @"YES", @"SHORT_CIRCUIT", nil];	
        [self.navigationController popViewControllerAnimated:NO];
		[ConcurMobileAppDelegate switchToView:ACTIVE_ENTRIES viewFrom:APPROVE_REPORT_SUMMARY ParameterBag:pBag];
	}
}

// ReportCreatedDelegate 
-(void) reportCreated:(ReportData*) rpt
{
    [self goToReportDetailScreen:rpt];
    // Refresh report list
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
    [[ExSystem sharedInstance].msgControl createMsg:[self getMsgId] CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
}

#pragma Refresh
-(BOOL) refreshView:(UIRefreshControl*) refresh
{
  
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING", nil];
    [[ExSystem sharedInstance].msgControl createMsg:[self getMsgId] CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    return NO;
}

#pragma -mark MobileAlertViewDelegate
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 1) //YES. delete openning document
    {
        ReportDetailViewController_iPad *rptIPad = (ReportDetailViewController_iPad*) self.fromMVC;
        [self deleteReportWith:rptIPad.rpt];
    }
    else if (buttonIndex == alertView.cancelButtonIndex)
    {
        self.isRptOpen = NO;
    }
}
@end
