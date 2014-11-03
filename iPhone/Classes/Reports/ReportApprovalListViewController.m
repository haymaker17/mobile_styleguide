//
//  ReportApprovalListViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReportApprovalListViewController.h"
#import "ExSystem.h" 

#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "ReportApprovalListCell.h"
#import "ReportData.h"
#import "ReportApprovalListData.h"
#import "ReportDetailViewController_iPad.h"
#import "Flurry.h"
#import "TravelRequestViewController.h"
#import "ReportDetailViewController.h"
#import "iPadHome9VC.h"
#import "TripApprovalListData.h"
#import "TripApprovalCell.h"
#import "TripToApprove.h"
#import "FormatUtils.h"
#import "TripDetailsViewController.h"
#import "HelpOverlayFactory.h"
#import "UserConfig.h"

@interface ReportApprovalListViewController()
@property (nonatomic, strong) NSMutableArray *sections;
@property (strong, nonatomic) NSMutableArray *aTrips;
@property (atomic, strong) SummaryData* summaryData;

@property (nonatomic,strong) UIRefreshControl *refreshControl;
@property int serverCallCount;
@end

@implementation ReportApprovalListViewController
@synthesize isPad, forceFetchTripApprovalList;
@synthesize	iPadHome, fromMVC;

// used to keep track of what section we're in.  The strings are essentially just tags
NSString *const REPORT_APPROVAL_SECTION = @"Report Approvals";
NSString *const TRAVEL_REQUEST_APPROVAL_SECTION  = @"Travel Requests";
NSString *const INVOICE_APPROVAL_SECTION = @"Invoice Approval";
NSString *const PURCHASE_REQUEST_SECTION = @"Purchase Requests";

#define kSECTION_TRIP_APPROVAL @"Trip Approvals"

- (id)initWithSummaryData:(SummaryData*)summaryData
{
    self = [super initWithNibName:@"ReportApprovalListViewController" bundle:nil];
    if( nil != self )
    {
        self.summaryData = summaryData;
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reportWasApproved) name:@"ReportApprovedNotification" object:nil];
    }
    return self;
}

// need to refresh the whole stupid table if a report is approved
// this is very inefficient, should probably come up with something else.
- (void)reportWasApproved
{
    [self refreshData:nil];
}

// no longer get notified when context saves
- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return APPROVE_REPORTS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

-(void)respondToFoundData:(Msg *)msg
{
    if ([self.refreshControl isRefreshing]) {
        [self.refreshControl endRefreshing];
    }
	if ((msg.parameterBag)[@"FORCE_FETCH"] != nil) 
	{
		self.fetch = YES;
	}
	else if ([msg.idKey isEqualToString:REPORT_APPROVAL_LIST_DATA])
	{
		ReportApprovalListData *ral = (ReportApprovalListData *)msg.responder;
		self.aKeys = ral.keys;
		self.rals = ral.objDict;
        self.serverCallCount--;
        [self.tableList reloadData];
	}
    else if ([msg.idKey isEqualToString:TRIP_APPROVAL_LIST_DATA])
	{
		TripApprovalListData *tripApprovalListData = (TripApprovalListData *)msg.responder;
		self.aTrips = tripApprovalListData.aTripsForApproval;
        self.serverCallCount--;
		[self.tableList reloadData];
	}
    else if ([msg.idKey isEqualToString:SUMMARY_DATA])
	{
        self.summaryData = (SummaryData *)msg.responder;
        self.serverCallCount--;
        [self.tableList reloadData];
    }
	// we only show negative view if there's no other approver roles and ther are no reports to approve
	if ([self hasNoReportsToApprove] && [self hasNoTripsToApprove] && ![[ExSystem sharedInstance]hasRole:ROLE_TRAVEL_REQUEST_APPROVER] && ![[ExSystem sharedInstance]hasRole:ROLE_INVOICE_APPROVER] && ![[ExSystem sharedInstance]hasRole:MOBILE_INVOICE_PAYMENT_USER] && ![[ExSystem sharedInstance]hasRole:ROLE_MOBILE_INVOICE_PURCH_APRVR] )
    {
        [self showNoDataView:self];
	}
    else
    {
		[self hideNoDataView];
	}
    // MOB-18338 and MOB-18797
    if (self.serverCallCount == 0 || self.serverCallCount < 0)
    {
        if ([self isViewLoaded])
        {
            if ([self isWaitViewShowing])
                [self hideWaitView];
            
            if ([self isLoadingViewShowing])
                [self hideLoadingView];
        }
    }
}


#pragma mark -
#pragma mark ViewController Methods
- (void)viewDidAppear:(BOOL)animated 
{
    // !!! : need to be refactored out
	if (self.fetch)
	{
		[self loadApprovals];
		self.fetch = NO;
	}
    if (self.forceFetchTripApprovalList) {
        [self loadTripApprovalsSkippingCache:YES];
        self.forceFetchTripApprovalList = NO;
    }

    if( nil == self.summaryData )
    {
        NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
        [[ExSystem sharedInstance].msgControl createMsg:SUMMARY_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:true RespondTo:self];
        self.serverCallCount++;
    }

    if ([UIDevice isPad]) {
        [HelpOverlayFactory addiPadApprovalListOverlayToView:self.navigationController.view];
    } else {
        [HelpOverlayFactory addiPhoneApprovalListOverlayToView:self.navigationController.view];
    }

    // MOB-18797
    if (self.serverCallCount > 0) {
        [self showLoadingView];
    }
 
	[super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated]; // This checks if the view is unloaded from Navigation Controller
    if ([[self.navigationController viewControllers] indexOfObject:self] == NSNotFound) {
        [self refreshHomeVC];
    }
}

- (void) refreshHomeVC
{
    id homeVC = [ConcurMobileAppDelegate findHomeVC];
    if ([homeVC respondsToSelector:@selector(forceReload)]) {
        [homeVC forceReload];
    }
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
    [super viewDidLoad];
    
    self.title = [Localizer getLocalizedText:@"Approvals"];
    self.modalPresentationStyle = UIModalPresentationFormSheet;

	if([UIDevice isPad])
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
		self.navigationItem.leftBarButtonItem =  [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
	}

    self.serverCallCount = 0;
    
	if(![ExSystem connectedToNetwork])
		[self makeOfflineBar];

    // MOB-8370 For Approval confirmation
    if ([UserConfig getSingleton] == nil)
    {
		[[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_USER_CONFIG CacheOnly:@"NO" ParameterBag:nil SkipCache:YES];
    }
    if ([[ExSystem sharedInstance]hasRole:ROLE_TRIP_APPROVER])
        [self loadTripApprovalsSkippingCache:YES];  //skip cache YES
    
    if ([[ExSystem sharedInstance]hasRole:ROLE_EXPENSE_MANAGER] && [[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals])
        [self loadApprovals];
    
    // !!! :  adding UIRefreshControl directly to the uitableview is not recommended by the spec
    self.refreshControl = [[UIRefreshControl alloc] init];
    [self.refreshControl addTarget:self action:@selector(refreshData:) forControlEvents:UIControlEventValueChanged];
    [self.tableList addSubview:self.refreshControl];
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	[self.tableList reloadData];
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)refreshData:(UIRefreshControl*)refreshControl{
    if ([ExSystem connectedToNetwork])
    {
        NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
        [[ExSystem sharedInstance].msgControl createMsg:SUMMARY_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:true RespondTo:self];
        self.serverCallCount++;
        
        [self loadApprovals];
        [self loadTripApprovalsSkippingCache:YES];
    }
    
}

#pragma mark -
#pragma mark Actions
-(void)closeView:(id)sender
{
	if([UIDevice isPad])
	{
		[self dismissViewControllerAnimated:YES completion:nil];	
	}
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    if (self.sections == nil) {
        self.sections = [[NSMutableArray alloc] init];

        if ([[ExSystem sharedInstance]hasRole:ROLE_TRIP_APPROVER])
            [self.sections addObject:kSECTION_TRIP_APPROVAL];
        if ([[ExSystem sharedInstance]hasRole:ROLE_TRAVEL_REQUEST_APPROVER])
            [self.sections addObject:TRAVEL_REQUEST_APPROVAL_SECTION];
        
        // these two roles are the same according to justin.  why we have two roles i'm not sure. - Ernest
        if (([[ExSystem sharedInstance]hasRole:ROLE_INVOICE_APPROVER] || [[ExSystem sharedInstance]hasRole:MOBILE_INVOICE_PAYMENT_USER]))
            [self.sections addObject:INVOICE_APPROVAL_SECTION];
        
        //MOB-16926 Check if user has the Purchase Reuqest role
        if ([[ExSystem sharedInstance]hasRole:ROLE_MOBILE_INVOICE_PURCH_APRVR])
            [self.sections addObject:PURCHASE_REQUEST_SECTION];
// MOB-16926 -- Move this to last, since report approvals may have a long list, so user doesnt have to scroll down to see invoices
        // MOB-13478 - dont show expense reports if sitesetting is disabled
        if ( ([[ExSystem sharedInstance]hasRole:ROLE_EXPENSE_MANAGER] && [[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals]))
            [self.sections addObject:REPORT_APPROVAL_SECTION];

    }
    
    return [self.sections count];
}


- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return [Localizer getLocalizedText:(self.sections)[section]];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString *sectionID = (self.sections)[section];
    if (sectionID == REPORT_APPROVAL_SECTION)
    {
        // this will be a negative cell.  lets the user know there are no reports to approve instead of an empty section
        if ([self hasNoReportsToApprove] || [self.aKeys count] == 0)
        {
            return 1;
        }
        
        //NSLog(@"[aKeys count] %d", [aKeys count]);
        return [self.aKeys count];
    }
    else if ([sectionID isEqualToString:kSECTION_TRIP_APPROVAL])
    {
        // this will be a negative cell.  lets the user know there are no reports to approve instead of an empty section
        if ([self hasNoTripsToApprove])
        {
            return 1;
        }
        return [self.aTrips count];
    }
    else if (sectionID == TRAVEL_REQUEST_APPROVAL_SECTION)
    {
        return 1;
    }
    else if (sectionID == INVOICE_APPROVAL_SECTION)
    {
        return 1;
    }
    // MOB-16926
    else if (sectionID == PURCHASE_REQUEST_SECTION)
    {
        return 1;
    }

    return 0;
}

// MOB-16926 - updated UI as per UX changes
- (UITableViewCell *)getTravelRequestCell:(UITableView *)tableView
{
    static NSString *reuseIdentifier = @"TravelRequestCell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseIdentifier];
    
    if (cell == nil)
    {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:reuseIdentifier];
    }
    
    if (self.summaryData != nil)
    {
        int travelRequestsCount = [(self.summaryData.dict)[@"TravelRequestApprovalCount"] intValue];
        NSString *detailText = nil;
        
        if (travelRequestsCount == 1) {
            detailText = [Localizer getLocalizedText:@"travel request to approve"];
        }
        else
        {
            detailText  = [Localizer getLocalizedText:@"travel requests to approve"];
        }
        cell.detailTextLabel.text = [NSString stringWithFormat:@"%d %@ ",travelRequestsCount, detailText];
    }
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    [cell.detailTextLabel setFont:[UIFont fontWithName:@"Helvetica Neue" size:15.0]];

    return cell;
}

- (UITableViewCell *)getInvoiceApprovalCell:(UITableView *)tableView
{
    static NSString *reuseIdentifier = @"InvoiceApproval";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseIdentifier];
    
    if (cell == nil)
    {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:reuseIdentifier];
    }
    
	// MOB-16926 - UX updates
    //invoice count should include submission and approval count
    int invoiceCount = 0;
    NSString *detailText = nil;
    if (self.summaryData != nil)
    {
    	NSString *invoicesToApprove = (self.summaryData.dict)[@"InvoicesToApproveCount"];
    	NSString *invoicesToSubmit = (self.summaryData.dict)[@"InvoicesToSubmitCount"];
    	invoiceCount = [invoicesToApprove intValue] + [invoicesToSubmit intValue];
        
        if (invoiceCount == 1) {
            detailText = [Localizer getLocalizedText:@"invoice to review"];
        }
        else
        {
            detailText = [Localizer getLocalizedText:@"invoices to review"];
        }
        
        cell.detailTextLabel.text = [NSString stringWithFormat:@"%d %@", invoiceCount, detailText];
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        [cell.detailTextLabel setFont:[UIFont fontWithName:@"Helvetica Neue" size:15.0]];
    }
    
     return cell;
}

// MOB-16926 - 
- (UITableViewCell *)getPurchaseRequestCell:(UITableView *)tableView
{
    static NSString *reuseIdentifier = @"PurchaseRequest";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseIdentifier];
    
    if (cell == nil)
    {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:reuseIdentifier];
    }
    
    //invoice count should include submission and approval count
    int purchaseRequestCount = 0;
    NSString *subTitle = nil;
    
    if (self.summaryData != nil)
    {
    	purchaseRequestCount = [(self.summaryData.dict)[purchaseRequestsToApproveCount] intValue];
        if (purchaseRequestCount == 1)
        {
            subTitle = [Localizer getLocalizedText:@"purchase request to review"];
        }
        else
        {
            subTitle = [Localizer getLocalizedText:@"purchase requests to review"];
        }
        
        cell.detailTextLabel.text = [NSString stringWithFormat:@"%d %@", purchaseRequestCount,subTitle ];
        
        if (purchaseRequestCount > 0)
        {
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        }
        
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        [cell.detailTextLabel setFont:[UIFont fontWithName:@"Helvetica Neue" size:15.0]];

    }
    
    return cell;
}

- (UITableViewCell *)getNegativeReportApprovalCell:(UITableView *)tableView
{
    static NSString *reuseIdentifier = @"NegativeReportApprovalCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseIdentifier];
    
    if (cell == nil)
    {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:reuseIdentifier];
    }
    // MOB-16926
    if (self.summaryData != nil)
    {
        //cell.textLabel.text = [Localizer getLocalizedText:@"Report Approvals"];
        cell.detailTextLabel.text = [NSString stringWithFormat:@"0 %@ ", [Localizer getLocalizedText:@"reports to approve"]];
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        
        [cell.detailTextLabel setFont:[UIFont fontWithName:@"Helvetica Neue" size:15.0]];
    }
    return cell;
}


- (UITableViewCell *)getNegativeTripApprovalCell:(UITableView *)tableView
{
    static NSString *reuseIdentifier = @"NegativeReportApprovalCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseIdentifier];
    
    if (cell == nil)
    {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:reuseIdentifier];
    }

    cell.detailTextLabel.text = [NSString stringWithFormat:@"0 %@" , [Localizer getLocalizedText:@"trips to approve"]];
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    [cell.detailTextLabel setFont:[UIFont fontWithName:@"Helvetica Neue" size:15.0]];
    return cell;
}


- (void)configureTripApprovalCell:(TripApprovalCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    TripToApprove *trip = self.aTrips[indexPath.row];
    cell.lblName.text = trip.travelerName;
    
    NSMutableString *approveByString = [[NSMutableString alloc] initWithString:[NSString stringWithFormat:@"%@: ",[@"Approve by" localize]]];
    if (trip.approveByDate == nil)
    {
        [approveByString appendString:[@"No date specified" localize]];
    }
    else
    {
        [approveByString appendString:[NSString stringWithFormat:@"%@ %@ %@",[DateTimeFormatter formatDateEEEByDate:trip.approveByDate],[DateTimeFormatter formatDateMediumByDate:trip.approveByDate TimeZone:[NSTimeZone localTimeZone]],[DateTimeFormatter formatDate:trip.approveByDate Format:([DateTimeFormatter userSettingsPrefers24HourFormat]?@"HH:mm zzz":@"hh:mm aaa zzz") TimeZone:[NSTimeZone localTimeZone]]]]; // Change to appropriate Deadline message
    }
    cell.lblDate.text = approveByString;
    
    cell.lblBottom.text = trip.tripName;
    cell.lblAmount.text = [FormatUtils formatMoneyWithNumber:trip.totalTripCost crnCode:trip.totalTripCostCrnCode];

    // MOB-16926 - UI inline with Expenese Report cells 
    [cell.lblName setFont:[UIFont fontWithName:@"Helvetica Neue" size:16.0]];
    [cell.lblAmount setFont:[UIFont fontWithName:@"Helvetica Neue" size:16.0]];
    [cell.lblBottom setFont:[UIFont fontWithName:@"Helvetica Neue" size:14.0]];

    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    NSString *sectionID = (self.sections)[[indexPath section]];

    if ([sectionID isEqualToString:kSECTION_TRIP_APPROVAL])
    {
        if ([self hasNoTripsToApprove])
        {
            return [self getNegativeTripApprovalCell:tableView];
        }
        
        TripApprovalCell *tripCell = (TripApprovalCell *)[tableView dequeueReusableCellWithIdentifier: @"TripApprovalCell"];
        if (tripCell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TripApprovalCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[TripApprovalCell class]])
                    tripCell = (TripApprovalCell *)oneObject;
        }
        [self configureTripApprovalCell:tripCell atIndexPath:(NSIndexPath *)indexPath];
        return tripCell;
    }
        
    
    if (sectionID == TRAVEL_REQUEST_APPROVAL_SECTION)
    {
        return [self getTravelRequestCell:tableView];
    }
    
    if (sectionID == INVOICE_APPROVAL_SECTION)
    {
        return [self getInvoiceApprovalCell:tableView];
    }
    
    if (sectionID == PURCHASE_REQUEST_SECTION)
    {
        return [self getPurchaseRequestCell:tableView];
    }
    

    // check if there are no report approvals.  return a negative cell
    if (sectionID == REPORT_APPROVAL_SECTION)
    {
        if ([self hasNoReportsToApprove] || [self.aKeys count] == 0)
        {
            return [self getNegativeReportApprovalCell:tableView];
        }
    }
    
    NSUInteger row = [indexPath row];
	NSString *key = self.aKeys[row];
	ReportData *rpt = self.rals[key];
	//NSLog(@"ApprovalList rpt.rptKey = %@", rpt.rptKey);
	
	static NSString *cellIdentity =  @"ReportApprovalListCell";
	
	ReportApprovalListCell *cell = (ReportApprovalListCell *)[tableView dequeueReusableCellWithIdentifier: cellIdentity];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ReportApprovalListCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[ReportApprovalListCell class]])
				cell = (ReportApprovalListCell *)oneObject;
	}
	cell.lblName.text = rpt.employeeName;

    // MOB-6353 use requested amount
	cell.lblAmount.text = [FormatUtils formatMoney:rpt.totalClaimedAmount crnCode:rpt.crnCode];
	cell.lblLine1.text = rpt.reportName;
	cell.lblLine2.text = [CCDateUtilities formatDateToMMMddYYYFromString:rpt.reportDate];

	[UtilityMethods drawNameAmountLabels:cell.lblName AmountLabel:cell.lblAmount];
	// MOB-16926 - ux changes 
    [cell.lblName setFont:[UIFont fontWithName:@"Helvetica Neue" size:16.0]];
    [cell.lblAmount setFont:[UIFont fontWithName:@"Helvetica Neue" size:16.0]];

    
	[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];

	int iImagePos = 0;
	int startX = 290;
	int imageWidth = 27;
	
	if([ExSystem isLandscape])
		startX = 434;	
	
	[cell clearAllImagesInCell];
	
    if(rpt.hasException != nil && [rpt.hasException isEqualToString:@"Y"])
	{
		BOOL showAlert = [rpt.severityLevel isEqualToString:@"ERROR"];
		
		if (showAlert) 
			[cell setImageByPosition:iImagePos imageName:@"icon_redex"];
		else 
			[cell setImageByPosition:iImagePos imageName:@"icon_yellowex"];
		iImagePos++;
	}

	int x = startX - (iImagePos * imageWidth);
	cell.lblLine1.autoresizingMask = UIViewAutoresizingNone;
    if(cell.lblLine1 != nil)
        cell.lblLine1.frame = CGRectMake(cell.lblLine1.frame.origin.x, cell.lblLine1.frame.origin.y, x - 20, 21);
        // MOB-16926 - indent inline with other cells
    cell.indentationLevel = 1;
   
	return cell;
}

#pragma mark -
#pragma mark Table Delegate Methods 

- (void)openInvoiceApprovals
{
//AJC -- is this code needed? delete after 2013-09-20 if not needed by then
//    NSURL *url = [[ExSystem sharedInstance] urlForWebExtension:@"invoice-home-page"];
    NSString *baseUrl = [ExSystem sharedInstance].entitySettings.uri;
    NSString *sessionId = [ExSystem sharedInstance].sessionID;
    NSString *locale = [[NSLocale currentLocale] localeIdentifier];
    
    NSString *urlString = [NSString stringWithFormat:@"%@/mobile/web/signin#mobile?pageId=%@&sessionId=%@&locale=%@",
                           baseUrl,
                           @"invoice-home-page",
                           sessionId,
                           locale];
    
    if ([urlString hasPrefix:@"https://rqa"] || [urlString hasPrefix:@"Https://rqa"])
    {
        urlString = [urlString stringByReplacingOccurrencesOfString:@"concursolutions.com" withString:@"concurtech.net"];
    }
    
    NSURL *url = [NSURL URLWithString:urlString];
    
    NSDictionary *dictionary = @{@"Action": @"Invoice"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    TravelRequestViewController *trVC = [[TravelRequestViewController alloc] initWithNibName:@"TRWebViewController" bundle:nil];
    UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:trVC];
    navController.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];
    navController.modalPresentationStyle = UIModalPresentationFormSheet;
    
    trVC.isNOT_TR = YES;
    trVC.viewTitle = [@"Invoice" localize];
    trVC.altURL = url;

    [self presentViewController:navController animated:YES completion:nil];
}

- (void)openPurchaseRequests
{
    if([(self.summaryData.dict)[purchaseRequestsToApproveCount] intValue] == 0)
        return;

    NSString *baseUrl = [ExSystem sharedInstance].entitySettings.uri;
    NSString *sessionId = [ExSystem sharedInstance].sessionID;
    NSString *locale = [[NSLocale currentLocale] localeIdentifier];
    
    NSString *urlString = [NSString stringWithFormat:@"%@/mobile/web/signin#mobile?pageId=%@&sessionId=%@&locale=%@",
                           baseUrl,
                           @"purchaserequest-home-page",
                           sessionId,
                           locale];
    
    // reqired for rqa servers.
    if ([urlString hasPrefix:@"https://rqa"] || [urlString hasPrefix:@"Https://rqa"])
    {
        urlString = [urlString stringByReplacingOccurrencesOfString:@"concursolutions.com" withString:@"concurtech.net"];
    }
    
    NSURL *url = [NSURL URLWithString:urlString];
    
    NSDictionary *dictionary = @{@"Action": @"Purchase Requests"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    TravelRequestViewController *trVC = [[TravelRequestViewController alloc] initWithNibName:@"TRWebViewController" bundle:nil];
    UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:trVC];
    navController.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];
    navController.modalPresentationStyle = UIModalPresentationFormSheet;
    
    trVC.isNOT_TR = YES;
    trVC.viewTitle = [@"Purchase Requests" localize];
    trVC.altURL = url;
    
    [self presentViewController:navController animated:YES completion:nil];
}

- (void)openTravelRequests
{
    NSDictionary *dictionary = @{@"Action": @"Travel Request"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    TravelRequestViewController *trVC = [[TravelRequestViewController alloc] initWithNibName:@"TRWebViewController" bundle:nil];
    UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:trVC];
    navController.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];
	navController.modalPresentationStyle = UIModalPresentationFormSheet;
    
    [self presentViewController:navController animated:YES completion:nil];
}


-(void) openTripApproval:(NSIndexPath *)indexPath
{
    if (![self hasNoTripsToApprove]) {
        TripToApprove *trip = self.aTrips[indexPath.row];
        TripDetailsViewController *vc = [[TripDetailsViewController alloc] initWithNibName:@"TripDetailsView" bundle:nil];
        vc.isApproval = YES;
        vc.tripToApprove = trip;
    //AJC -- is this code needed? delete after 2013-09-20 if not needed by then
    //    [self presentViewController:vc animated:YES completion:nil];
    //    UINavigationController *homeNavigationController = (UINavigationController*)self.presentingViewController;
    //    [homeNavigationController pushViewController:vc animated:YES];
        [self.navigationController pushViewController:vc animated:YES];
    }
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    if(![ExSystem connectedToNetwork])
    {
        NSDictionary *dict = @{@"Type": @"Reports to Approve"};
        [Flurry logEvent:@"Offline: Viewed" withParameters:dict];
    }
    
    // handle travel request and invoice
    NSString *sectionID = (self.sections)[[newIndexPath section]];
    if ([sectionID isEqualToString:kSECTION_TRIP_APPROVAL])
    {
        [self openTripApproval:newIndexPath];
        return;
    }
    
    if (sectionID == TRAVEL_REQUEST_APPROVAL_SECTION)
    {
        [self openTravelRequests];
        return;
    }
    
    if (sectionID == INVOICE_APPROVAL_SECTION)
    {
        [self openInvoiceApprovals];
        return;
    }
    
    if (sectionID == PURCHASE_REQUEST_SECTION)
    {
        [self openPurchaseRequests];
        return;
    }
    

    // you cannot drill down on an empty report list
    if ([self hasNoReportsToApprove] || [self.aKeys count] == 0) {
        return;
    }

    
    NSUInteger row = [newIndexPath row];

	NSString *key = (self.aKeys)[row];
	ReportData *rpt = self.rals[key];
	
	if(![UIDevice isPad])
	{
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY", ROLE_EXPENSE_MANAGER, @"ROLE", @"YES", @"SHORT_CIRCUIT", nil]; 
        
        ReportDetailViewController *view = [[ReportDetailViewController alloc] initWithNibName:@"ReportHeaderView" bundle:nil];
        [view setSeedData:pBag];
        [self.navigationController pushViewController:view animated:YES];
        
        //AJC -- is this code needed? delete after 2013-09-20 if not needed by then
        /*
        // old switchToView code, this is so hard to understand
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY", ROLE_EXPENSE_MANAGER, @"ROLE", @"YES", @"SHORT_CIRCUIT", nil]; 
		[ConcurMobileAppDelegate switchToView:APPROVE_ENTRIES viewFrom:APPROVE_REPORTS ParameterBag:pBag];
         */
	}
	else
    {
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY", ROLE_EXPENSE_MANAGER, @"ROLE", @"YES", @"SHORT_CIRCUIT"
									 ,self.aKeys, @"REPORT_KEYS", self.rals, @"REPORT_DICT", nil]; //, @"YES", @"SKIP_PARSE"
        
        pBag[@"COMING_FROM"] = @"APPROVAL";
		
		if(fromMVC != nil)
			[fromMVC dismissPopovers];
		
		if([UIDevice isPad])
		{
			[self dismissViewControllerAnimated:YES completion:nil];	
		}
		
        // If the report detail screen is already being shown, then pop it
        iPadHome9VC *homeVC = (iPadHome9VC*)[ConcurMobileAppDelegate findHomeVC];
        
        if ([homeVC.navigationController.topViewController isKindOfClass:[ReportDetailViewController_iPad class]])
            [homeVC.navigationController popViewControllerAnimated:NO];
        
        ReportDetailViewController_iPad *newDetailViewController = [[ReportDetailViewController_iPad alloc] initWithNibName:@"ReportDetailViewController_iPad" bundle:nil];
		// MOB-12923
		newDetailViewController.role = ROLE_EXPENSE_MANAGER;
        
        UINavigationController *homeNavigationController = (UINavigationController*)self.presentingViewController;
        [homeNavigationController pushViewController:newDetailViewController animated:YES];
        
        [newDetailViewController loadReport:pBag];
    }
}



- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *sectionID = (self.sections)[[indexPath section]];
    
    if ( (sectionID == REPORT_APPROVAL_SECTION && ![self hasNoReportsToApprove]) || ([sectionID isEqualToString:kSECTION_TRIP_APPROVAL] && ![self hasNoTripsToApprove]) )
    {
        return 60;
    }
    else
    {
        return 40;
    }
}


#pragma mark - Toolbar
-(void)setupToolbar
{
	[[ConcurMobileAppDelegate getBaseNavigationController].toolbar setHidden:NO];
	
	// Mob-2517,2518 Localization of Send Back & Approve
	UIBarButtonItem *btnSendBack = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"APPROVE_SENDBACK_BUTTON_TITLE"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonSendBackPressed:)];
	UIBarButtonItem *btnApprove= [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"APPROVE_APPROVE_BUTTON_TITLE"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonApprovePressed:)];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSArray *toolbarItems = @[btnSendBack, flexibleSpace, btnApprove];
	[self setToolbarItems:toolbarItems animated:YES];
}


#pragma mark - Utility Methods
- (NSString *)titleForNoDataView
{
    return [@"You have no approvals" localize];
}


- (NSString *)imageForNoDataView
{
    return @"neg_approvals_icon";
}


- (BOOL)hasNoInvoicesToApprove
{
    if (self.summaryData == nil)
    {
        return false;
    }
    
    int invoiceCount = 0;
    
    NSString *invoicesToApprove = (self.summaryData.dict)[@"InvoicesToApproveCount"];
    NSString *invoicesToSubmit = (self.summaryData.dict)[@"InvoicesToSubmitCount"];
    invoiceCount = [invoicesToApprove intValue] + [invoicesToSubmit intValue];
    
    if (invoiceCount == 0)
    {
        return true;
    }
    
    return false;
}


- (BOOL)hasNoTravelRequestToApprove
{
    if (self.summaryData == nil)
    {
        return false;
    }
    
    NSString *travelRequestsCount = (self.summaryData.dict)[@"TravelRequestApprovalCount"];
    if ([travelRequestsCount integerValue] == 0)
    {
        return true;
    }
    return false;
}


- (BOOL)hasNoReportsToApprove
{
    if (self.summaryData == nil)
    {
        return false;
    }
    
    NSString *reportsCount = (self.summaryData.dict)[@"ReportsToApproveCount"];
    if ([reportsCount integerValue] == 0 || ![reportsCount length])
    {
        return true;
    }
    return false;
}

#define kTRIPS_TO_APPROVE_COUNT @"TripsToApproveCount"
- (BOOL)hasNoTripsToApprove
{
    if (self.summaryData == nil)
    {
        return false;
    }
    
    NSString *tripsToApproveCount = (self.summaryData.dict)[kTRIPS_TO_APPROVE_COUNT];
    if (self.aTrips != nil && [self.aTrips count] == 0) {
        return YES;
    }
    if ([tripsToApproveCount integerValue] == 0 && [self.aTrips count] == 0)
    {
        return true;
    }
    return false;
}

-(void)loadTripApprovalsSkippingCache:(BOOL)skipCache
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
	[[ExSystem sharedInstance].msgControl createMsg:TRIP_APPROVAL_LIST_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:skipCache RespondTo:self];
    self.serverCallCount++;
}

- (void)loadApprovals
{
    if ([[ExSystem sharedInstance]hasRole:ROLE_EXPENSE_MANAGER] && [[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals])
	{
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
        [[ExSystem sharedInstance].msgControl createMsg:REPORT_APPROVAL_LIST_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
        self.serverCallCount++;
    }
}


@end
