//
//  TripsViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/13/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "TripsViewController.h"
#import "TripsCell.h"
#import "ExSystem.h" 

#import "SampleData.h"
#import "AdCell.h"
#import "DateTimeFormatter.h"
#import "iPadHomeVC.h"
#import "ExSystem.h"
#import "AppsUtil.h"
#import "SegmentSelectVC.h"
#import "TripItValidateAccessTokenData.h"
#import "TravelBookingActionSheet.h"
#import "PostMsgInfo.h"

#import "CCWebNavigationCrontroller.h"
#import "CCWebBrowser.h"

#import "Fusion2014TripDetailsViewController.h"

#define actionRefreshData @"Refresh Data"
#define actionAgencyInfo @"Agency Info"

#define kActionMetr0 @"Metr0"
#define kActionTaxi @"Taxi Magic"
#define kActionGateGuru @"GateGuru"

const int TRAVEL_APP_ACTION_SHEET_TAG = 1000;
const int REFRESH_AGENCY_ACTION_SHEET_TAG = 1001;

@interface TripsViewController()
@property (nonatomic, strong) TravelBookingActionSheet *bookingSheet;
@property (nonatomic, strong) NSString *tripDetailsRequestId;
@end

@implementation TripsViewController

@synthesize tableList;
@synthesize listKeys;
@synthesize navBar;
@synthesize dictData;
@synthesize	fetchView;
@synthesize	lblFetch;
@synthesize	spinnerFetch, iPadHome, fromMVC, aSections, dictSections, aAction, isExpensed;

-(void)closeView:(id)sender
{
	if([UIDevice isPad])
	{
        // Close button is still active for any actionsheet launched from the same bar.
        // This is just how iOS seems to handle it.
        // Loc says to just dismiss the actionsheets and close when close is touched
        [MobileActionSheet dismissAllMobileActionSheets];
        [self dismissViewControllerAnimated:YES completion:nil];
	}
}

-(NSString *)getViewIDKey
{
	return TRIPS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

// 9.0 UI navigation bar buttons.  This should open the booking sheet from the + button
- (void)makeNavbar
{
    [self.navigationController setNavigationBarHidden:NO];
    
    if ([[ExSystem sharedInstance] hasTravelBooking]) {
        self.navigationItem.rightBarButtonItem =  [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(openBookingSheetFromPlusButton)];
    }
}

- (void)openBookingSheet
{
    if (self.bookingSheet == nil) {
        self.bookingSheet = [[TravelBookingActionSheet alloc] initWithNavigationController:self.navigationController];
    }
   // MOB-14890 - Show action sheet from toolbar
    [self.bookingSheet showActionSheetFromToolBar:self.navigationController.toolbar];
}

- (void)openBookingSheetFromPlusButton
{
    self.tripDetailsRequestId = nil;
    [self hideLoadingView];
    
    if (self.bookingSheet == nil) {
        self.bookingSheet = [[TravelBookingActionSheet alloc] initWithNavigationController:self.navigationController];
    }
    [self.bookingSheet showActionSheetFromBarButtonItem:self.navigationItem.rightBarButtonItem];
}

// 9.0 UI toolbar buttons. This only adds the agency button.  Refresh should be handled by the pull to refresh.
- (void)makeToolbar
{
    if(![ExSystem connectedToNetwork])
    {
        [self makeOfflineBar];
        return;
    }
    
    UIBarButtonItem *refreshButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRefresh target:self action:@selector(refreshData)];
    UIBarButtonItem *agencyButton = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Travel Agency Info"] style:UIBarButtonItemStyleBordered target:self action:@selector(fetchAgencyInfo)];
    UIBarButtonItem *flex = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:self action:nil];
    
    NSMutableArray *toolbarItems = [NSMutableArray arrayWithObjects: agencyButton, flex, refreshButton, nil];
    if ([[ExSystem sharedInstance] hasRole:ROLE_OPEN_BOOKING_USER] && [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_ONLY_USER])
        [toolbarItems removeObject:agencyButton];
	[self setToolbarItems:toolbarItems animated:NO];
}

-(void) refreshData
{
    self.tripDetailsRequestId = nil;
    [self showWaitViewWithText:[Localizer getLocalizedText:@"Refreshing Data"]];
    
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING", nil];
	[[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) fetchAgencyInfo
{
    CCWebBrowser *browser = [[CCWebBrowser alloc] initWithTitle:[Localizer getLocalizedText:@"Travel Agency Info"]];
    
    NSString *uri = [ExSystem sharedInstance].entitySettings.uri;
    uri = [uri lowercaseString];
    if ([uri hasPrefix:@"https://rqa"]) {
        uri = [uri stringByReplacingOccurrencesOfString:@"concursolutions.com" withString:@"concurtech.net"];
    }
    NSString *currentLocale = [[NSLocale currentLocale] localeIdentifier];
    browser.URLString = [NSString stringWithFormat:@"%@/mobile/web/signin#mobile?sessionId=%@&pageId=travel-agency-info-page&locale=%@",
                             uri,
                             [ExSystem sharedInstance].sessionID,
                             currentLocale];
    if (!browser.URLString) {
        // not sure if it is really needed, but let's see what flurry says
        [Flurry logError:@"Travel Agency: Show Info Erroc URLString is empty" message:@"URLString is empty" error:nil];
        return;
    }
    [Flurry logEvent:@"Travel Agency: Show Info"];
    [self.navigationController pushViewController:browser animated:YES];
}

-(void) createSections:(TripsData*) td
{
	NSMutableArray * oT = [[NSMutableArray alloc] initWithObjects:nil];
    NSMutableArray * cT = [[NSMutableArray alloc] initWithObjects:nil];
    NSMutableArray * fT = [[NSMutableArray alloc] initWithObjects:nil];
    NSMutableArray * aAT = [[NSMutableArray alloc] initWithObjects:nil];
    NSMutableArray * aRT = [[NSMutableArray alloc] initWithObjects:nil];
	self.dictSections = [[NSMutableDictionary alloc] initWithObjectsAndKeys:oT, @"Old Trips"
						 , cT, @"Current Trip"
						 , fT, @"Future Trips"
                         , aAT, @"Awaiting Approval"
                         , aRT, @"Rejected", nil];
	
	NSArray *aTrips = [[TripManager sharedInstance] fetchAll];
	for(int i = 0; i < [aTrips count]; i++)
	{
		EntityTrip *trip = (EntityTrip*)aTrips[i];
	     // MOB-7192 Strip out timezone info in current local time, so that we can compare it with travel time in GMT(w/o timezone info).
        NSDate* now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
        
		NSString *sectionKey = @"Current Trip";
        if ([trip.approvalStatus isEqualToString:@"AwaitingApproval"])
        {
            sectionKey = @"Awaiting Approval";
        }
        else if ([trip.approvalStatus isEqualToString:@"RejectedCantOverride"] || [trip.approvalStatus isEqualToString:@"RejectedAndClosed"] || [trip.approvalStatus isEqualToString:@"RejectedOverridable"])
        {
            sectionKey = @"Rejected";
        }
		else if([trip.tripStartDateLocal compare:now] != NSOrderedDescending && [trip.tripEndDateLocal compare:now] != NSOrderedAscending)
		{
			sectionKey = @"Current Trip";
		}
		else if([trip.tripEndDateLocal compare:now] == NSOrderedAscending)
		{
			sectionKey = @"Old Trips";
		}
		else if([trip.tripStartDateLocal compare:now] == NSOrderedDescending)
		{
			sectionKey = @"Future Trips";
		}
		
		NSMutableArray *a = dictSections[sectionKey];
		[a addObject:trip];
		NSSortDescriptor *sorter = [[NSSortDescriptor alloc] initWithKey:@"tripStartDateLocal" ascending:YES];
		[a sortUsingDescriptors:@[sorter]];
		dictSections[sectionKey] = a;
		
	}
    
    [tableList reloadData];
}

#pragma mark - MVC Stuff
-(void)respondToFoundData:(Msg *)msg
{//respond to data that might be coming from the cache
    
    if ([msg.idKey isEqualToString:VALIDATE_TRIPIT_ACCESS_TOKEN])
    {
       TripItValidateAccessTokenData* validationData = (TripItValidateAccessTokenData *)msg.responder;
        if (validationData.actionStatus != nil && validationData.actionStatus.status != nil && [validationData.actionStatus.status isEqualToString:@"SUCCESS"])
        {
            if (!validationData.isTripItLinked)
            {
                // The accounts are no longer linked!
                [ExSystem sharedInstance].isTripItLinked = NO;
                [[ExSystem sharedInstance] saveSettings];
                
                if([UIDevice isPad])
                {
                    [iPadHome checkStateOfTrips];
                }
                else
                {
                    [self.navigationController popToRootViewControllerAnimated:YES];
                }
                
                UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Not Linked"] 
                                                                    message:[Localizer getLocalizedText:@"Your account is no longer linked to TripIt."]
                                                                   delegate:nil 
                                                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] 
                                                          otherButtonTitles:nil];
                [alert show];
                
                return;
            }
            else if (!validationData.isTripItEmailAddressConfirmed) // If accounts are linked, but email not confirmed
            {
                UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"TripIt Account Not Activated"] 
                                                                    message:[Localizer getLocalizedText:@"Please follow the instructions in the email"]//Please follow the instructions in the email you received from TripIt when you set up your account.
                                                                   delegate:nil 
                                                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] 
                                                          otherButtonTitles:nil];
                [alert show];
                
                return;
            }
        }
    }
    else if ([msg.idKey isEqualToString:TRIPS_DATA] && msg.parameterBag[@"ITIN_LOCATOR"] && msg.parameterBag[@"LOADING_SINGLE_ITIN"])
    {
        if(self.isWaitViewShowing)
            [self hideWaitView];
        else if(self.isLoadingViewShowing)
            [self hideLoadingView];
        
        NSString *itinLocator = (msg.parameterBag)[@"ITIN_LOCATOR"];
        TripsData* td = (TripsData *)msg.responder;
        [self createSections:td];
        if ([self.tripDetailsRequestId isEqualToString:msg.parameterBag[@"TRIPDETAILSREQUEST_UUID"]])
        {
            EntityTrip *selectedTrip = [[TripManager sharedInstance] fetchByItinLocator:itinLocator];
            if ([selectedTrip.isItinLoaded boolValue])
            {
                if([UIDevice isPad])
                {
                    [self displayTripOniPad:selectedTrip withLoadedTrip:YES];
                }
                else
                {
                    [self displayTripOniPhone:selectedTrip withLoadedTrip:YES];
                }
            }
        }
    }
	else if ([msg.idKey isEqualToString:TRIPS_DATA])
	{
        //below is the pattern of getting the object you want and using it.
        if(self.isWaitViewShowing)
            [self hideWaitView];
        else if(self.isLoadingViewShowing)
            [self hideLoadingView];
		[self hideNoDataView];
		
		TripsData* td = (TripsData *)msg.responder;
		[self createSections:td];
        
        // check if we have no data
        if (dictSections != nil)
        {
            BOOL isEmpty = YES;
            NSString *key;
            NSEnumerator *enumerator = dictSections.keyEnumerator;
            while (key=[enumerator nextObject])
            {
                if ([dictSections[key] count])
                {
                    isEmpty = NO;
                    break;
                }
            }
            if (isEmpty) {
                [self showNoDataView:self];
            }
        }
		
		if (dictData != nil && [dictData count] == 0)
		{
		}
		else 
		{
            [self.tableList reloadData];
		}

		BOOL isCacheData = NO;
		
		if((msg.parameterBag)[@"CAME_FROM_CACHE"] != nil)
		{
			isCacheData = YES;
			[msg.parameterBag removeObjectForKey:@"CAME_FROM_CACHE"];
		}
		else
        {
			isCacheData = NO;
		}
		
		if (!isCacheData) 
		{
			if (td == nil || td.keys == nil || [td.keys count] < 1) 
			{//show we got no data view
				[self showNoDataView:self];
			}
			else if (td != nil & td.keys != nil & [td.keys count] > 0)
			{//refresh from the server, after an initial no show...
				[self hideNoDataView];
			}
		}
	}
}

// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation 
{
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

-(IBAction)switchViews:(id)sender
{
	[ConcurMobileAppDelegate switchViews:sender ParameterBag:nil];
}


#pragma mark - View Stuff
-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    self.navigationController.toolbarHidden = NO;
    self.navigationController.navigationBarHidden = NO;
}

-(void) viewWillDisappear:(BOOL)animated
{
    self.tripDetailsRequestId = nil;
    [super viewWillDisappear:animated];
}


-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self checkTripItLink];
}

- (void)viewDidLoad 
{
    [super viewDidLoad];
    
    self.aSections = [[NSMutableArray alloc] initWithObjects:@"Current Trip", @"Rejected", @"Awaiting Approval", @"Future Trips", @"Old Trips", nil];
    self.dictSections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    
    if(self.isExpensed)
        [self loadTripsWithExpenseData];
    
    self.aAction = [[NSMutableArray alloc] initWithObjects: nil];
    
    if([[ExSystem sharedInstance] hasRole:@"Metro_User"])
        [aAction addObject:kActionMetr0];
    
    if([[ExSystem sharedInstance] hasRole:@"Taxi_User"])
        [aAction addObject:kActionTaxi];
    
    if([[ExSystem sharedInstance] hasRole:@"GateGuru_User"])
        [aAction addObject:kActionGateGuru];
    
	if([UIDevice isPad])
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
		self.navigationItem.leftBarButtonItem =  [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
        [self refreshData];
	}
	
    if (![ExSystem sharedInstance].isGovernment)
    {
        [GlobalLocationManager startTrackingSignificantLocationUpdates];
    }
    
    [self makeNavbar];
    
    [self makeToolbar];
	[fetchView setHidden:YES];
    
    self.title = [Localizer getLocalizedText:@"Trips"];

    // Load trips when the view first loads. 9.0 home improvement
    [ExSystem sharedInstance].sys.topViewName = self.getViewIDKey;
    [self loadTrips];
}

- (void)requestSummaryData
{
    NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
    [[ExSystem sharedInstance].msgControl createMsg:SUMMARY_DATA CacheOnly:@"YES" ParameterBag:pBag SkipCache:false RespondTo:self];
}

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload 
{
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
    [GlobalLocationManager stopTrackingSignificantLocationUpdates];
}

- (void)displayTripOniPhone:(EntityTrip *)selectedTrip withLoadedTrip:(BOOL)isTripLoaded
{
    if ([Config isNewTravel])
    {
        Fusion2014TripDetailsViewController *ftTripDetailVC = [[UIStoryboard storyboardWithName:@"Fusion2014TripDetailsViewController" bundle:nil] instantiateInitialViewController];
        [self.navigationController pushViewController:ftTripDetailVC animated:YES];
        
        if (isTripLoaded)
        {
            [ftTripDetailVC displayTrip:selectedTrip TripKey:selectedTrip.tripKey];
        }
        else
        {
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING",@"YES", @"LOADING_SINGLE_ITIN", selectedTrip.itinLocator, @"ITIN_LOCATOR", nil];
            self.tripDetailsRequestId = [PostMsgInfo getUUID];
            pBag[@"TRIPDETAILSREQUEST_UUID"] = self.tripDetailsRequestId;
            ftTripDetailVC.tripDetailsRequestId = self.tripDetailsRequestId;
            [ftTripDetailVC loadTrip:pBag];
        }
        
        return;
    }
    
    if (isTripLoaded)
    {
        UIButton *infoButton = [UIButton buttonWithType:UIButtonTypeInfoLight];
        infoButton.tag = 600001;
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:selectedTrip, @"TRIP", selectedTrip.tripKey, @"TRIP_KEY", @"YES", @"SKIP_PARSE", nil];
        [ConcurMobileAppDelegate switchViews:infoButton ParameterBag:pBag];
    }
    else
    {
        // TODO: Change iPhone trip loading same as iPad (see MOB-18084)
        // Key is to stop using switchViews to get to TripDetail.
        [self showWaitViewWithText:[Localizer getLocalizedText:@"Loading Data"]];
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING",@"YES", @"LOADING_SINGLE_ITIN", selectedTrip.itinLocator, @"ITIN_LOCATOR", nil];
        self.tripDetailsRequestId = [PostMsgInfo getUUID];
        pBag[@"TRIPDETAILSREQUEST_UUID"] = self.tripDetailsRequestId;
        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

-(void)displayTripOniPad:(EntityTrip *)trip withLoadedTrip:(BOOL)isTripLoaded
{
    if(fromMVC != nil)
        [fromMVC dismissPopovers];
    
    // Dismiss the popover if it's present.
    if (pickerPopOver != nil)
    {
        [pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
    }
    
    // If the trip detail screen is already being shown, then pop it
    UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
    if ([homeVC.navigationController.topViewController isKindOfClass:[DetailViewController class]])
        [homeVC.navigationController popViewControllerAnimated:NO];
    
    // Create a new trip detail view
    DetailViewController *newDetailViewController = [[DetailViewController alloc] initWithNibName:@"BaseDetailVC_iPad" bundle:nil];
    
    [newDetailViewController.ivLogo setHidden:YES];
    
    UINavigationController *homeNavigationController = homeVC.navigationController;
    [homeNavigationController pushViewController:newDetailViewController animated:YES];
    if (isTripLoaded)
    {
        [newDetailViewController displayTrip:trip TripKey:trip.tripKey];
    }
    else
    {
        NSString *requestUUID = [PostMsgInfo getUUID];
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING",@"YES", @"LOADING_SINGLE_ITIN", trip.itinLocator, @"ITIN_LOCATOR", nil];
        pBag[@"TRIPDETAILSREQUEST_UUID"] = requestUUID;
        newDetailViewController.tripDetailsRequestId = requestUUID;
        [newDetailViewController loadTrip:pBag];
    }
}


#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [aSections count];
    
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	NSString *key = aSections[section];
	NSMutableArray *a = dictSections[key];
    //return [self.listKeys count];
//   NSLog(@"[a count] %d", [a count]);
	if(a == nil)
		return 0;
	else 
		return [a count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	static NSString *MyCellIdentifier = @"TripsCell";

    TripsCell *cell = (TripsCell *)[tableView dequeueReusableCellWithIdentifier: MyCellIdentifier];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TripsCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[TripsCell class]])
                cell = (TripsCell *)oneObject;
    }
		
	NSString *key = aSections[indexPath.section];
	NSMutableArray *a = dictSections[key];
	
	EntityTrip *trip = a[indexPath.row];
	cell.label.text = trip.tripName; 

    NSString *startFormatted = [DateTimeFormatter formatDateEEEMMMddyyyyByDate:trip.tripStartDateLocal]; 
	NSString *endFormatted = [DateTimeFormatter formatDateEEEMMMddyyyyByDate:trip.tripEndDateLocal]; 

	cell.labelDateRange.text = [NSString stringWithFormat:@"%@ - %@", startFormatted, endFormatted];

//    if ([trip.state]) // isWaitingForApproval state == 101; // MOB-7341 We only handle 101 for now
//    {
    UIFont *font = [UIFont systemFontOfSize:14];
    CGSize size = [self getSizeOfText:trip.tripStateMessages withFont:font];
        CGRect frame = cell.labelLabelLine3.frame;
        cell.labelLabelLine3.frame = CGRectMake(frame.origin.x, 48, frame.size.width, size.height);
        cell.labelLabelLine3.text = trip.tripStateMessages;
        cell.labelLabelLine3.lineBreakMode = NSLineBreakByWordWrapping;
        cell.labelLabelLine3.numberOfLines = 0;
        //[cell.labelLabelLine3 sizeToFit];
//    }
//    else
//    {
//        cell.labelLabelLine3.text = @"";
//    }
    cell.lblExpensed.text = @"";
    [cell setAccessoryType:UITableViewCellAccessoryNone];

    return cell;

}

#pragma mark -
#pragma mark Table Delegate Methods 
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
{
    NSString *sectionTitle = aSections[section];
    NSArray *a = dictSections[sectionTitle];
    if([a count] == 0)
        return @"";
    else
        return [Localizer getLocalizedText:sectionTitle];
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    if(![UIDevice isPad])
    {
        [tableView deselectRowAtIndexPath:newIndexPath animated:YES];
    }
    else
    {
        [self dismissViewControllerAnimated:YES completion:nil];
    }
    
    NSString *key = aSections[newIndexPath.section]; // [self.listKeys objectAtIndex:row];
    NSMutableArray *a = dictSections[key];
    EntityTrip *trip = a[newIndexPath.row]; // [self.dictData objectForKey:key];
    
    // MOB-18084 dismiss trip list view after select one trip.
    // Show trip itin view and show loading view there, not on trip list.
    // Trip detail requst happes on itin view as well.
    [self displaySelectedTripDetails:trip];
}

- (void) displaySelectedTripDetails:(EntityTrip *) trip
{
    // showing trip on iPad handls preloaded trip, or if new trip request needed.
    if ([UIDevice isPad])
    {
        [self displayTripOniPad:trip withLoadedTrip:[trip.isItinLoaded boolValue]];
    }
    else
    {
        [self displayTripOniPhone:trip withLoadedTrip:[trip.isItinLoaded boolValue]];
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *key = aSections[indexPath.section];
	NSMutableArray *a = dictSections[key];
	EntityTrip *trip = a[indexPath.row];
    NSString *tripStatusMsg = trip.tripStateMessages;
    UIFont *font = [UIFont systemFontOfSize:14];
    CGSize size = [self getSizeOfText:tripStatusMsg withFont:font];
    
//    if (trip.state = 101) // isWaitingForApproval])
//        return 70;
	return 55 + size.height;
}

- (CGSize)getSizeOfText:(NSString *)text withFont:(UIFont *)font
{
    
    return [text sizeWithFont:font constrainedToSize:CGSizeMake(self.view.frame.size.width, 500)];
}

#pragma mark -
#pragma mark iPad Stuff
-(void)loadTrips
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
	[[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
}

-(void)loadTripsWithExpenseData
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"Y", @"EXPENSED", nil];
    [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void)checkTripItLink
{
    // Validate the TripIt access token.  Doing so will find out whether access has been revoked on the TripIt side, effectively severing the link with Concur.
}


#pragma NoDataMasterViewDelegate method
-(void) actionOnNoData:(id)sender
{
    if([[ExSystem sharedInstance] hasTravelBooking])
        [self openBookingSheet];
}

- (BOOL)adjustNoDataView:(NoDataMasterView*) negView
{
    return NO;
}

- (NSString*) buttonTitleForNoDataView
{
    if([[ExSystem sharedInstance] hasTravelBooking])
        return [Localizer getLocalizedText:@"Book a Trip"];
    else
        return @"";
}

- (NSString *)titleForNoDataView
{
        return [@"You have no upcoming trips" localize];
}

-(BOOL) canShowActionOnNoData
{
    return YES;
}

#pragma mark -
#pragma mark UIActionSheetDelegate method
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex 
{
    if (actionSheet.tag == TRAVEL_APP_ACTION_SHEET_TAG)
    {
        if([aAction count] == buttonIndex)
            return;
        
        NSString *key = aAction[buttonIndex];
        
        if ([key isEqualToString:kActionMetr0])
            [self buttonMetroPressed:nil];
        else if ([key isEqualToString:kActionTaxi])
            [self buttonTaxiPressed:self];
        else if ([key isEqualToString:kActionGateGuru])
            [self buttonAirportsPressed:self];
    }
    else if (actionSheet.tag == REFRESH_AGENCY_ACTION_SHEET_TAG)
    {
        if (buttonIndex != actionSheet.cancelButtonIndex)
        {
            MobileActionSheet* mas = (MobileActionSheet*) actionSheet;
            NSString* btnId = [mas getButtonId:buttonIndex];
            
            if ([btnId isEqualToString:actionRefreshData])
            {
                [self refreshData];
            }
            else if ([btnId isEqualToString:actionAgencyInfo])
                [self fetchAgencyInfo];
        }
    }
}


#pragma mark -
#pragma mark Action Methods
-(void)showAction:(id)sender
{
    if ([UIDevice isPad])
    {//MOB-10615
        [MobileActionSheet dismissAllMobileActionSheets];
    }
    
	UIActionSheet * action = nil;
    
	if([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER] || [[ExSystem sharedInstance] hasRole:@"TravelUser"])
	{        
		action = [[MobileActionSheet alloc] initWithTitle:nil
                                                 delegate:self 
                                        cancelButtonTitle:nil
                                   destructiveButtonTitle:nil
                                        otherButtonTitles:nil];
        
        
        for(int i = 0; i < [aAction count]; i++)
        {
            NSString *role = aAction[i];
            [action addButtonWithTitle:role];
        }
        
        [action addButtonWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
        action.cancelButtonIndex = [aAction count];
        
        action.tag = TRAVEL_APP_ACTION_SHEET_TAG;
	}
    
	
	if([UIDevice isPad])
		[action showFromBarButtonItem:sender animated:YES];
	else 
		[action showFromBarButtonItem:sender animated:YES]; // showFromRect:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height) inView:self.view animated:YES];
	
}


// no longer used in 9.0
-(void)showRefreshAgencyAction:(id)sender
{
    if ([UIDevice isPad])
    {//MOB-10615
        [MobileActionSheet dismissAllMobileActionSheets];
    }

    MobileActionSheet * actionRefreshAgency = [[MobileActionSheet alloc] initWithTitle:nil
                                                                          delegate:self
                                                                 cancelButtonTitle:nil
                                                            destructiveButtonTitle:nil
                                                                 otherButtonTitles:nil];
    
    NSMutableArray* btnIds = [[NSMutableArray alloc] init];
    
    [actionRefreshAgency addButtonWithTitle:[Localizer getLocalizedText:@"Refresh Data"]];
    [btnIds addObject:actionRefreshData];
    [actionRefreshAgency addButtonWithTitle:[Localizer getLocalizedText:@"Travel Agency Info"]];
    [btnIds addObject:actionAgencyInfo];

    [actionRefreshAgency addButtonWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
    actionRefreshAgency.cancelButtonIndex = [btnIds count];
    
    actionRefreshAgency.btnIds = btnIds;
    actionRefreshAgency.tag = REFRESH_AGENCY_ACTION_SHEET_TAG;
    
    [actionRefreshAgency showFromBarButtonItem:sender animated:YES];
    
}

-(void)buttonAirportsPressed:(id)sender
{
	[self performSelector:@selector(switchToAirports:) withObject:nil afterDelay:0.05f];
}


- (void)switchToAirports:(id)sender
{
    [AppsUtil launchGateGuruAppWithUrl:nil];
}


- (void)buttonTaxiPressed:(id)sender
{
    [AppsUtil launchTaxiMagicApp];
}

- (void)buttonMetroPressed:(id)sender
{
    [AppsUtil launchMetroApp];
}

- (IBAction)buttonTripItPressed:(id)sender
{
    [AppsUtil launchTripItApp];
}

-(BOOL) hasTrips
{
    for(NSString *key in dictSections)
    {
        NSMutableArray *a = dictSections[key];
        if(a != nil && [a count] > 0)
            return YES;
    }
    
    return NO;
}

+(void) refreshViewsWithTripsData:(Msg*) msg fromView:(MobileViewController*) srcVc
{
    // if msg is from server, refresh both the TripsViewController and TripDetailsViewController
    // Refresh the TripDetailsViewController only if it exists, since it will then call this api again to refresh its parent, TripVC;
    // If TripDetailsVC does not exist, refresh the TripsVC.
    if (![UIDevice isPad] && !msg.isCache)
    {
        MobileViewController *mvc = [ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:TRIP_DETAILS];
        if (mvc != nil && mvc != srcVc)
            [mvc respondToFoundData:msg];
        else 
        {
            mvc = [ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:TRIPS];
            if (mvc != nil && mvc != srcVc)
                [mvc respondToFoundData:msg];
        }
    }
}

@end
