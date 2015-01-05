//
//  GoviPadHome9VC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 4/9/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GoviPadHome9VC.h"
#import "ApplicationLock.h"

#import "SummaryData.h"
#import "UploadQueueViewController.h"
#import "UploadQueue.h"

#import "GoviPadHome9TravelCell.h"
#import "iPadHome9Cell.h"

#import "SettingsViewController.h"
#import "GovDocumentManager.h"
#import "GovExpenseEditViewController.h"
#import "TripsViewController.h"
#import "GovUnappliedExpensesVC.h"
#import "GovDocumentListVC.h"
#import "GovLoginNoticeVC.h"
#import "GovSelectTANumVC.h"
#import "CarViewController.h"
#import "HotelViewController.h"
#import "TrainBookVC.h"

#import "UIColor+CCPalette.h"
#import "UIColor+ConcurColor.h"
#import "NSStringAdditions.h"

// Collection View
#import "HomeCollectionView.h"

#define kSECTION_TRAVEL @"TRIPS_BUTTON"
#define kSECTION_CURRENT_TRIP @"TRIP_BUTTON"

#define kACTION_BOOK_TRAVEL 111
#define PRIVACY_ACT_ALERT_ACTION 113

@interface GoviPadHome9VC ()

@property BOOL requireHomeScreenRefresh;
@property (nonatomic, strong) SummaryData *summaryData;
@property (nonatomic, strong) EntityTrip *currentTrip;
@property (nonatomic, strong) NSMutableDictionary *serverCallCounts;

@property (nonatomic, strong) EntityWarningMessages *allMessages;

// This class handles the UI, it does NOT handle data or network calls
@property (nonatomic, readwrite, strong) IBOutlet HomeCollectionView *homeCollectionView;
// need to close the CollectionView refresh control after a refresh
@property (nonatomic, readwrite, strong) UIRefreshControl *collectionViewRefreshControl;

// Makes it easier to switch from a TableViewController to a ViewController
@property (nonatomic, readwrite, getter=isRefreshing) BOOL refreshing;

- (void) setupNavBar;
- (BOOL) checkBookAir;

@end

@implementation GoviPadHome9VC

@synthesize lblOffline, offlineHeaderView;
@synthesize rootVC,currentTrip;
@synthesize requireHomeScreenRefresh;
@synthesize tripsData;
@synthesize postLoginAttribute;
@synthesize btnBookTravel;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]; // [ initWithStyle:style];
    
    if (self)
    {
        requireHomeScreenRefresh = YES;
        
        // respond to total of 3 servercalls in the class
        self.serverCallCounts = [[NSMutableDictionary alloc] initWithCapacity:3];
    }
    return self;
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return @"GOV_iPAD_HOME_PAGE90";
}

#pragma mark - ApplicationLock Notifications
-(void) doPostLoginInitialization
{
    [self fetchHomePageDataAndSkipCache:YES];
    requireHomeScreenRefresh = NO;
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
    {
        MobileAlertView *alert = [self getPrivacyActView:self];
        [alert show];
    }
    // update based on role information
    [self updateCollectionViewLayout];
}


#pragma mark -Gov Warning messages
-(MobileAlertView*) getPrivacyActView:(UIViewController* )del
{
    NSManagedObjectContext *context = [ExSystem sharedInstance].context;
    NSArray *allMessage = [BaseManager fetchAll:@"EntityWarningMessages" withContext:context];
    if ([allMessage count] > 0)
    {
        self.allMessages = (EntityWarningMessages*) [allMessage objectAtIndex:0];
    }
    
    __autoreleasing MobileAlertView *alert = [[MobileAlertView alloc]
                                              initWithTitle:self.allMessages.privacyTitle
                                              message:self.allMessages.privacyText
                                              delegate:del
                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                              otherButtonTitles:nil];
    alert.tag = PRIVACY_ACT_ALERT_ACTION;
    return alert;
}

-(void) savePrarmetersAfterLogin:(NSDictionary *) pBag
{
    if (pBag != nil)
    {
        [self.postLoginAttribute addEntriesFromDictionary:pBag];
    }
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [ExSystem sharedInstance].sys.topViewName = self.getViewIDKey;
    
    self.rootVC = [[RootViewController alloc] init];
    
 	[ExSystem sharedInstance].msgControl.rootVC = rootVC;
    
    self.postLoginAttribute = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];

    if (![BaseManager hasEntriesForEntityName:@"EntityWarningMessages" withContext:[ad managedObjectContext]])
    {
        [[ExSystem sharedInstance].msgControl createMsg:GOV_WARNING_MSG CacheOnly:@"NO" ParameterBag:nil SkipCache:NO RespondTo:self];
        [self.serverCallCounts setObject:GOV_WARNING_MSG forKey:GOV_WARNING_MSG];
    }

    
    if([[ExSystem sharedInstance].sys.showWhatsNew boolValue])
    {
        // First time use of 9.0 clean older version data
        [self clearHomeData];
    }
    
    if ([ExSystem isLandscape])
    {
        self.view.frame = CGRectMake(0, 0, 1024, 768);
    }
    
    self.offlineHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0, 62, 768, 18)];
    self.offlineHeaderView.autoresizingMask = (UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin);
    
    self.lblOffline = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 768, 18)];
    self.lblOffline.autoresizingMask = (UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin);
    
    [self setupNavBar];
    [ExSystem setStatusBarBlack];
    
    if ([ExSystem sharedInstance].sys.productLine == PROD_GOVERNMENT){
        NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:@"Gov", @"Type", nil];
        [Flurry logEvent:@"User: Type" withParameters:dictionary];
    }
    else{
        NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:@"CTE", @"Type", nil];
        [Flurry logEvent:@"User: Type" withParameters:dictionary];
    }
    
    [self setupCollectionView];
    [self updateCollectionViewLayout];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    // Handle background to foreground etc states
    
    if ([[ApplicationLock sharedInstance] isLoggedIn])
	{
        if (requireHomeScreenRefresh)
		{
            [self fetchHomePageData];
            requireHomeScreenRefresh = NO;
        }
        [self checkOffline];
	}
    else
    {
        [[ApplicationLock sharedInstance] onHomeScreenAppeared];
    }
}

- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
    
    // Set the background image of toolbar here so the user doesnt see the blue transition from other screen
    UIImage *toolbarimage = [UIImage imageNamed:@"bar_bottom"];
    [self.navigationController.toolbar setBackgroundImage:toolbarimage forToolbarPosition:UIToolbarPositionAny barMetrics:UIBarMetricsDefault];
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
    // Reset Toolbar background for other view
    [self.navigationController.toolbar setBackgroundImage:nil forToolbarPosition:UIToolbarPositionAny barMetrics:UIBarMetricsDefault];
    // Reset the offline bar
    if(![ExSystem connectedToNetwork])
    {
    	// Need a better way to check if there was offline bar.
        
        [self.offlineHeaderView removeFromSuperview];
//        [self.tableView setContentInset:UIEdgeInsetsMake(0,0,0,0)];
    }
    
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
    [self.view setNeedsDisplay];
    
    if ([self.pickerPopOver isPopoverVisible] && [self.pickerPopOver.contentViewController isKindOfClass:[UIImagePickerController class]])
    {
        [self.pickerPopOver dismissPopoverAnimated:YES];
        self.pickerPopOver = nil;
    }
    [self rotateHomeCollectionViewToInterfaceOrientation:toInterfaceOrientation];
}

-(void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [super willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - response processing and UI updating
-(void) didProcessMessage:(Msg *)msg
{
    [self respondToFoundData:msg];
}

-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:TRIPS_DATA])
	{
		TripsData* tripsDataObj = (TripsData *)msg.responder;
		[self refreshUIWithTripsData:tripsDataObj];
        self.tripsData = tripsDataObj;
        [self.serverCallCounts removeObjectForKey:TRIPS_DATA];
	}
    else if ([msg.idKey isEqualToString:GOV_WARNING_MSG])
    {
        GovWarningMessagesData *messages = (GovWarningMessagesData*) msg.responder;
        if (messages != nil)
        {
            NSManagedObjectContext *context = [ExSystem sharedInstance].context;
            NSArray *allMessage = [BaseManager fetchAll:@"EntityWarningMessages" withContext:context];
            if ([allMessage count] > 0)
            {
                self.allMessages = (EntityWarningMessages*) [allMessage objectAtIndex:0];
            }
        }
        [self.serverCallCounts removeObjectForKey:GOV_WARNING_MSG];
    }

    
	// close loading view if there are no more servercalls
    if([self.serverCallCounts count]  == 0)
    {
        [self hideWaitView];
        [self hideLoadingView];
        // need to close the collection view refresh thing, this is awkward.
        // this is how we handled it in the old UI and it works well enough where I dont want to refactor it.
        if ([self isRefreshing])
        {
            self.refreshing = NO;
            if (self.collectionViewRefreshControl)
            {
                [self.collectionViewRefreshControl endRefreshing];
            }
        }
    }
}


#pragma mark - Data implementation
-(void) clearHomeData
{
    if(currentTrip != nil)
    {
        self.currentTrip = nil;
    }
    [[HomeManager sharedInstance] clearAll];
}

- (void)fetchHomePageData
{
    [self fetchHomePageDataAndSkipCache:NO];
}

- (void)fetchHomePageDataAndSkipCache:(BOOL)shouldSkipCache
{
    [self refreshSummaryData];
	if([[ExSystem sharedInstance] isValidSessionID:[ExSystem sharedInstance].sessionID] || [@"OFFLINE" isEqualToString:[ExSystem sharedInstance].sessionID])
	{
        [self getTripsData:shouldSkipCache];
    }
}


-(void)refreshUIWithSummaryData:(SummaryData*)sd
{
    int iPos = 1;
    EntityHome *entity = nil;
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
    {
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON];
        entity.name = [Localizer getLocalizedText:@"Authorizations"];
        entity.subLine  = [Localizer getLocalizedText:@"View and update authorizations"];
        entity.imageName = @"ipad_icon_stampeddoc";
        entity.key = kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
        entity.rowPosition = [NSNumber numberWithInt:iPos];
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_REPORTS];
        entity.name = [Localizer getLocalizedText:@"Vouchers"];
        entity.subLine  = [Localizer getLocalizedText:@"View, create and update vouchers"];
        entity.key = kSECTION_EXPENSE_REPORTS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
        entity.imageName = @"icon_report";
        entity.rowPosition = [NSNumber numberWithInt:iPos];
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;

        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_APPROVALS];
        entity.name = [Localizer getLocalizedText:@"Stamp Documents"];
        entity.subLine  = [Localizer getLocalizedText:@"Approve authorizations and vouchers"];
        entity.key = kSECTION_EXPENSE_APPROVALS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
        entity.imageName = @"icon_approvals_ipad";
        entity.rowPosition = [NSNumber numberWithInt:iPos];
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_CARDS];
        entity.name = [Localizer getLocalizedText:@"Expenses"];
        entity.subLine  = [Localizer getLocalizedText:@"View unapplied expenses"];
        entity.key = kSECTION_EXPENSE_CARDS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
        entity.imageName = @"icon_expenses_ipad";
        entity.rowPosition = [NSNumber numberWithInt:iPos];
        [[HomeManager sharedInstance] saveIt:entity];
        iPos ++;
    }
     else
    {
        //kill off approvals if the user should not have it
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS];
        if(entity != nil)
        	[[HomeManager sharedInstance] deleteObj:entity];
    }
}

// Get Trips data
- (void) refreshUIWithTripsData: (TripsData *) tripsData
{
    int upcoming = 0;
    int active = 0;
    
    NSArray *aTrips = [[TripManager sharedInstance] fetchAll];
	
    // MOB-7192 Strip out timezone info in current local time, so that we can compare it with travel time in GMT(w/o timezone info).
    NSDate* now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
    
    // MOB-5945 Whenever new TripsData comes, we need to reset active/upcoming trips as well as currentTrip
	if ([aTrips count] > 0) // AJC - please delete this commented out code if present past 2013-12-13 /*&& self.currentTrip == nil*/
	{
		for (int i = 0; i < [aTrips count]; i++)
		{
			EntityTrip* trip = (EntityTrip*)[aTrips objectAtIndex:i];
            // MOB-5945 - Make comparison consistent with the logic in TripsViewController
			if (([trip.tripEndDateLocal compare:now] == NSOrderedDescending) && ([trip.tripStartDateLocal compare:now] == NSOrderedAscending))
            {
                //is the end date of the looped to trip greater than today and is the startdate before now?
                active ++;
                if (self.currentTrip == nil)
                {
                    self.currentTrip = trip;
                }
                else
                {
                    if ([trip.tripStartDateLocal compare:currentTrip.tripStartDateLocal] == NSOrderedAscending)
                    {
                        self.currentTrip = trip;
                    }
                }
            }
		}
	}
    else
        self.currentTrip = nil;  // No trips
    
    if ([aTrips count] > 0)
	{
		for (int i = 0; i < [aTrips count]; i++)
		{
			EntityTrip* trip = (EntityTrip*)[aTrips objectAtIndex:i];
			//NSDate* startDate = [DateTimeFormatter getLocalDate:trip.tripStartDateLocal];
			if ([trip.tripStartDateLocal compare:now] == NSOrderedAscending) //is the start date of the looped to trip greater than today?
				continue; //yup yup
			
            upcoming++;
		}
	}
	
    //Current Trip Row
	if (currentTrip != nil)
	{
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_CURRENT_TRIP];
        entity.name = currentTrip.tripName;
        entity.subLine  = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateForTravelByDate:currentTrip.tripStartDateLocal],[DateTimeFormatter formatDateForTravelByDate:currentTrip.tripEndDateLocal]];
        entity.keyValue = currentTrip.tripKey;
        entity.sectionValue = kSECTION_TRIPS;
        entity.sectionPosition = [NSNumber numberWithInt:kSECTION_TRIPS_POS];
        entity.rowPosition = [NSNumber numberWithInt:0];
        entity.key = kSECTION_CURRENT_TRIP;
        [[HomeManager sharedInstance] saveIt:entity];
	}
    else if(currentTrip == nil)
    {
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_CURRENT_TRIP];
        entity.name = [Localizer getLocalizedText:@"No Active Trips"];
        entity.subLine  = @"";

        entity.sectionValue = kSECTION_TRIPS;
        entity.sectionPosition = [NSNumber numberWithInt:kSECTION_TRIPS_POS];
        entity.rowPosition = [NSNumber numberWithInt:0];
        entity.key = kSECTION_CURRENT_TRIP;
        [[HomeManager sharedInstance] saveIt:entity];
    }
    
    //Trips Row
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRAVEL];
    entity.name = [Localizer getLocalizedText:@"Trips"];
    
    NSString *sub = nil;
    if (active==0 && upcoming == 0)
    {
        sub = [Localizer getLocalizedText:@"TRAVEL_NEG_TEXT"];
    }
    else
    {
        sub = [NSString stringWithFormat:[Localizer getLocalizedText:@"int active int upcoming"], active, upcoming];
    }
    entity.subLine = sub;
    entity.key = kSECTION_TRAVEL;
    entity.sectionValue = kSECTION_TRIPS;
    entity.sectionPosition = [NSNumber numberWithInt:kSECTION_TRIPS_POS];
    entity.imageName = @"home_icon_trip";
    entity.rowPosition = [NSNumber numberWithInt:1];
    [[HomeManager sharedInstance] saveIt:entity];
}

//Place a server call to get Trips data
// Move these to a common homeutilities object
-(void)getTripsData:(BOOL)shouldSkipCache
{
    if (([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER] || [[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER]))
    {
        NSMutableDictionary* pBag2 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag2 SkipCache:shouldSkipCache RespondTo:self];
        [self.serverCallCounts setObject:TRIPS_DATA forKey:TRIPS_DATA];
//        servercalls++;
    }
    else
    {
        //remove travel data if the user does not have role
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_TRAVEL];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
    }
    
}


#pragma mark - Upload and offline
-(void) showUploadViewController
{
    UploadQueueViewController *vc = [[UploadQueueViewController alloc] initWithNibName:@"UploadQueueViewController" bundle:nil];
    UIBarButtonItem *btnUpload = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Upload"] style:UIBarButtonItemStyleBordered target:vc action:@selector(startUpload)];
    vc.title = [Localizer getLocalizedText:@"Upload Queue"];
    UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:vc action:@selector(closeMe:)];
    
    vc.navigationItem.rightBarButtonItem = btnUpload;
    vc.navigationItem.leftBarButtonItem = btnClose;
    
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:vc];
    
    [navi setModalPresentationStyle:UIModalPresentationFormSheet];
    navi.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];
    [self presentViewController:navi animated:YES completion:NULL];
}

-(void) checkOffline
{
    if(![ExSystem connectedToNetwork])
    {
        [self makeOfflineHeader];
        [self.navigationController.view addSubview:self.offlineHeaderView];
    }
    else
    {
        [self.offlineHeaderView removeFromSuperview];
    }
}

-(UIView *)makeOfflineHeader
{
    NSString *offlinemsg = [Localizer getLocalizedText:@"Offline"];
    
    [self.offlineHeaderView setBackgroundColor:[UIColor redColor]];
    self.lblOffline.text = offlinemsg;
    
    self.lblOffline.textColor = [UIColor lightGrayColor];
    self.lblOffline.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:17.0f];
    self.lblOffline.textAlignment =  NSTextAlignmentCenter;
    self.lblOffline.backgroundColor = [UIColor clearColor];
    
    return self.offlineHeaderView;
}

// Checks and displays alert if offline
-(void)ShowOfflineAlert
{
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:[Localizer getLocalizedText:@"Offline"]
                          message:[Localizer getLocalizedText:@"Operation Not Supported Offline"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                          otherButtonTitles:nil];
    [alert show];
}

#pragma mark - UI related elements
-(void)ResetBarColors:(UINavigationController *)navcontroller
{
    navcontroller.toolbar.tintColor = [UIColor darkBlueConcur_iOS6];
    navcontroller.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];
}

#pragma mark - Button Press
-(IBAction)btnCurrTripPressed:(id)sender
{
    [self checkOffline];
	
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}
	
    if (self.currentTrip != nil)
    {
        //MOB-10675
        EntityTrip* activeTrip = [[TripManager sharedInstance] fetchByTripKey:currentTrip.tripKey];
        if (activeTrip != nil)
        {
            // If the trip detail screen is already being shown, then pop it
            UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
            if ([homeVC.navigationController.topViewController isKindOfClass:[DetailViewController class]])
                [homeVC.navigationController popViewControllerAnimated:NO];
            
            // Create a new trip detail view
            DetailViewController *newDetailViewController = [[DetailViewController alloc] initWithNibName:@"BaseDetailVC_iPad" bundle:nil];
            
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"DetailViewController", @"TO_VIEW", @"YES", @"REFRESHING", activeTrip.itinLocator, @"ITIN_LOCATOR", activeTrip.tripKey, @"TRIP_KEY", nil];
            [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:newDetailViewController];
            [newDetailViewController showLoadingView];
            
            [newDetailViewController.ivLogo setHidden:YES];

            UINavigationController *homeNavigationController = homeVC.navigationController;
            [homeNavigationController pushViewController:newDetailViewController animated:YES];
            
            [newDetailViewController displayTrip:activeTrip TripKey:activeTrip.tripKey];
        }
    }
    
}

-(IBAction)tripsButtonPressed:(id)sender
{
    [self checkOffline];
    NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:@"View Trips", @"Action", nil];
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
	
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
        pickerPopOver = nil;
	}
	
	if([tripsData.trips count] == 1)
	{
		NSString *key = [tripsData.keys objectAtIndex:0];
        EntityTrip *trip = [[TripManager sharedInstance] fetchByTripKey:key];
        
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:trip, @"TRIP", nil];
        //TODO: Show trip detail vhen only one trip
		//[self switchToDetail:@"Trip" ParameterBag:pBag];
		return;
	}
	
	TripsViewController *tripsListVC = [[TripsViewController alloc] initWithNibName:@"TripsView" bundle:nil];
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:tripsListVC];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	//R 162 , G 160, B 160
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:YES completion:nil];
	[tripsListVC loadTrips];
}

-(IBAction)btnAuthorizationsPressed:(id)sender
{
    [self showGovDocumentListView:GOV_DOC_TYPE_AUTH];
}

-(IBAction)btnVouchersPressed:(id)sender
{
    [self showGovDocumentListView:GOV_DOC_TYPE_VOUCHER];
}

-(IBAction)btnStampDocumentsPressed:(id)sender
{
    [self showGovDocumentListView:GOV_DOC_TYPE_STAMP];
}

-(IBAction)btnExpensesPressed:(id)sender
{
    [GovUnappliedExpensesVC showUnappliedExpenses:self];
}

-(IBAction)btnBookTravelPressed:(id)sender
{
    [self checkOffline];
    NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:@"Book Trip", @"Action", nil];
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];

    UIActionSheet *bookingAction = nil;
    
    bookingAction = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN] destructiveButtonTitle:nil otherButtonTitles:[Localizer getLocalizedText:@"Book Air"], [Localizer getLocalizedText:@"Book Hotel"], [Localizer getLocalizedText:@"Book Car"], [Localizer getLocalizedText:@"Book Rail"], nil];
    bookingAction.tag = kACTION_BOOK_TRAVEL;
    // Hard code location for action sheet
	// This is only known way to make trip booking sheet shows at the right location on iPad
	// TODO: Handle rotation while action sheet is showing
    if (self.interfaceOrientation == UIInterfaceOrientationPortrait)
    {
        NSLog(@"orientation: %ld", self.interfaceOrientation);
        [bookingAction showFromRect:CGRectMake(191, 954, 1, 1) inView:[UIApplication sharedApplication].keyWindow animated:NO];
    }
    else if (self.interfaceOrientation == UIInterfaceOrientationPortraitUpsideDown)
    {
        NSLog(@"orientation: %ld", self.interfaceOrientation);
        [bookingAction showFromRect:CGRectMake(578, 77, 1, 1) inView:[UIApplication sharedApplication].keyWindow animated:NO];
    }
    else if (self.interfaceOrientation == UIInterfaceOrientationLandscapeRight)
    {
        NSLog(@"orientation: %ld", self.interfaceOrientation);
        [bookingAction showFromRect:CGRectMake(78, 255, 1, 1) inView:[UIApplication sharedApplication].keyWindow animated:NO];
    }
    else if(self.interfaceOrientation == UIInterfaceOrientationLandscapeLeft)
    {
        NSLog(@"orientation: %ld", self.interfaceOrientation);
        [bookingAction showFromRect:CGRectMake(695, 768, 1, 1) inView:[UIApplication sharedApplication].keyWindow animated:NO];
    }
}

-(IBAction)btnQEpressed:(id)sender
{
    NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:@"Quick Expense", @"Action", nil];
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    GovExpenseEditViewController* vc = [[GovExpenseEditViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
    
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:vc];
    navi.modalPresentationStyle = UIModalPresentationFormSheet;
    [self.navigationController presentViewController:navi animated:YES completion:nil];
}

-(void) showGovDocumentListView:(NSString*) filter
{
    GovDocumentListVC *vc = [[GovDocumentListVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    vc.filter = filter;
    //    [self.navigationController pushViewController:vc animated:YES];
    
    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:vc];
    localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
    [localNavigationController setToolbarHidden:NO];
    
    localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
    localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
    
    NSString* msgId = [filter isEqualToString:GOV_DOC_TYPE_STAMP]? GOV_DOCUMENTS_TO_STAMP: GOV_DOCUMENTS;
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    [[ExSystem sharedInstance].msgControl createMsg:msgId CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:vc];
    
    [self presentViewController:localNavigationController animated:YES completion:nil];
}

#pragma -mark Book travel event handler
-(void)btnBookFlightsPressed
{
    NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:@"Book Air", @"Action", nil];
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];

    if(![self checkBookAir])
        return;
    
    // Add a dummy root vc for the modal dialog, so that we can switch out the GovSelectTANumVC to land on booking criteria page
	FormViewControllerBase *nextController = [[FormViewControllerBase alloc] initWithNibName:@"EditFormView" bundle:nil];
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:NO completion:nil];
    [GovSelectTANumVC showSelectTANum:nextController withCompletion:@"Book Air" withFields:nil withDelegate:nil asRoot:YES];
}

-(void)btnBookCarPressed
{
    // Add a dummy root vc for the modal dialog, so that we can switch out the GovSelectTANumVC to land on booking criteria page
	FormViewControllerBase *nextController = [[FormViewControllerBase alloc] initWithNibName:@"EditFormView" bundle:nil];
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:NO completion:nil];
    [GovSelectTANumVC showSelectTANum:nextController withCompletion:@"Book Car" withFields:nil withDelegate:nil asRoot:YES];
}

-(void)btnBookHotelPressed
{
    // Add a dummy root vc for the modal dialog, so that we can switch out the GovSelectTANumVC to land on booking criteria page
	FormViewControllerBase *nextController = [[FormViewControllerBase alloc] initWithNibName:@"EditFormView" bundle:nil];
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:NO completion:nil];
    [GovSelectTANumVC showSelectTANum:nextController withCompletion:@"Book Hotel" withFields:nil withDelegate:nil asRoot:YES];
}

-(void)btnBookRailPressed
{
    // Add a dummy root vc for the modal dialog, so that we can switch out the GovSelectTANumVC to land on booking criteria page
	FormViewControllerBase *nextController = [[FormViewControllerBase alloc] initWithNibName:@"EditFormView" bundle:nil];
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	
	[self presentViewController:localNavigationController animated:NO completion:nil];
    [GovSelectTANumVC showSelectTANum:nextController withCompletion:@"Book Rail" withFields:nil withDelegate:nil asRoot:YES];
}

# pragma mark -
# pragma mark Refresh and utility methods

// Return whether the task is completed
// Implement pulldown refresh
-(BOOL) refreshView:(UIRefreshControl*) refresh
{
    
    NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:@"Refresh Data", @"Action", nil];
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    [self fetchHomePageDataAndSkipCache:YES];
    
    return NO;
}

// Get everything all over again
-(void) forceReload
{
    requireHomeScreenRefresh = YES;
//    isDataReady = NO;
    [self fetchHomePageData];
}

// Post a server call to refresh Trips data
// Temp fix only.Server call is actually not needed here. We can update coredata when a new trip is added.
-(void) refreshTripsData
{
    if ([ExSystem connectedToNetwork])
    {
        [self getTripsData:YES];
    }
}

// Post a server call to refresh summary data
// Temp fix only. Server call is actually not needed here. we can simply update core data when summary is udpated
-(void) refreshSummaryData
{
    int iPos = 1;
    EntityHome *entity = nil;
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER])
    {
        //        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_QUICK];
        //        entity.name = [Localizer getLocalizedText:@"Quick Expense"];
        //        entity.subLine  = [Localizer getLocalizedText:@"Capture expense and receipt"];
        //        entity.key = kSECTION_EXPENSE_QUICK;
        //        entity.sectionValue = kSECTION_EXPENSE;
        //        entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
        //        entity.imageName = @"icon_quickexpense";
        //        entity.rowPosition = [NSNumber numberWithInt:iPos];
        //        [[HomeManager sharedInstance] saveIt:entity];
        //        iPos++;
        
        //TODO: need to add trip Data
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON];
        entity.name = [Localizer getLocalizedText:@"Authorizations"];
        entity.subLine  = [Localizer getLocalizedText:@"View and update authorizations"];
        entity.imageName = @"ipad_icon_stampeddoc";
        entity.key = kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
        entity.rowPosition = [NSNumber numberWithInt:iPos];
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_REPORTS];
        entity.name = [Localizer getLocalizedText:@"Vouchers"];
        entity.subLine  = [Localizer getLocalizedText:@"View, create and update vouchers"];
        entity.key = kSECTION_EXPENSE_REPORTS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
        entity.imageName = @"icon_report";
        entity.rowPosition = [NSNumber numberWithInt:iPos];
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
        
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_APPROVALS];
        entity.name = [Localizer getLocalizedText:@"Stamp Documents"];
        entity.subLine  = [Localizer getLocalizedText:@"Approve authorizations and vouchers"];
        entity.key = kSECTION_EXPENSE_APPROVALS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
        entity.imageName = @"icon_approvals_ipad";
        entity.rowPosition = [NSNumber numberWithInt:iPos];
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_CARDS];
        entity.name = [Localizer getLocalizedText:@"Expenses"];
        entity.subLine  = [Localizer getLocalizedText:@"View unapplied expenses"];
        entity.key = kSECTION_EXPENSE_CARDS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
        entity.imageName = @"icon_expenses_ipad";
        entity.rowPosition = [NSNumber numberWithInt:iPos];
        [[HomeManager sharedInstance] saveIt:entity];
        iPos ++;
    }
    // Approvals - show approvals if user has any of these roles
    else
    {
        //kill off approvals if the user should not have it
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS];
        if(entity != nil)
        	[[HomeManager sharedInstance] deleteObj:entity];
    }
}

// To show login view if user is not logged in.
-(void) showManualLoginView
{
    if ([ConcurMobileAppDelegate isLoginViewShowing])
        return;
    
    // MOB-16161- Load new login UI , new login UI is in a storyboard.
    LoginViewController* lvc = [[UIStoryboard storyboardWithName:@"Login_iPad" bundle:nil] instantiateViewControllerWithIdentifier:@"GovLogin"];
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:lvc];
	[self presentViewController:navi animated:YES completion:nil];
    ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    appDelegate.topView = LOGIN;
    lvc.loginDelegate = self;
}


#pragma  mark - login delegate
/**
 This method actally removes the login view and not the home view itself
 */
- (void)dismissYourself:(UIViewController*)vc;
{
    [vc dismissViewControllerAnimated:YES completion:nil];
}

- (void)setupNavBar
{
    UIImageView *img = nil;

    if ([ExSystem is7Plus])
        img = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"concur_logo_iOS7"]];
    else
        img = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"concur_logo_titlebar"]];
    self.title = @"Concur";
    
    self.navigationItem.titleView = img;
    // Check offline and update header
    [self checkOffline];

    // Wire up Moremenu and messageCenter actions    
    SEL settingMenuSelector = @selector(showSettingMenu:);

    self.navigationItem.leftBarButtonItem = [self getNavBarButtonWithImage:@"icon_menu_settings" withSelector:settingMenuSelector];;
}

// Nav bar buttons
-(UIBarButtonItem *)getNavBarButtonWithImage:(NSString *)imgName withSelector:(SEL)selectorName
{
    UIButton* mbtn =[UIButton buttonWithType:UIButtonTypeCustom];
    UIImage* mImage = [UIImage imageNamed:imgName];
    [mbtn addTarget:self action:selectorName forControlEvents:UIControlEventTouchUpInside];
    [mbtn setImage:mImage forState:UIControlStateNormal];
    mbtn.frame = CGRectMake(0, 0, mImage.size.width, mImage.size.height);
    UIBarButtonItem *menuButton = [[UIBarButtonItem alloc]initWithCustomView:mbtn];
    
    return menuButton;
    
}

// Create custom button for toolbar
-(UIBarButtonItem *)getCustomBarButton:(NSString *)imgName withTitle:(NSString *)title withBorder:(UIColor *)borderColor withSelector:(SEL)selectorName isEnabled:(BOOL)isEnabled
{
    UIImageView *imgView = [[UIImageView alloc] initWithImage: [UIImage imageNamed:imgName]];
    CGSize titleFrame = [title sizeWithFont:[UIFont fontWithName:@"HelveticaNeue-Bold" size:17.0f] forWidth:250 lineBreakMode:nil];
    UIImage *toolbarimage = [UIImage imageNamed: @"bar_bottom"];
    
    UIImage *btnbackground = [UIImage imageNamed:@"buttons_gray_blank"];
    
    float imgHeight = imgView.image.size.height;
    float offset = (toolbarimage.size.height - imgHeight);
    float frameHeight = imgHeight + offset;
    float frameWidth = imgView.image.size.width + titleFrame.width + 15.0;

    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, frameWidth, frameHeight)];
    
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];;
    UIBarButtonItem *barButton = nil;
    
    [button setImage:[UIImage imageNamed:imgName] forState:UIControlStateNormal];
    [button setTitle:title forState:UIControlStateNormal];
    [button.titleLabel setFont:[UIFont fontWithName:@"HelveticaNeue-Bold" size:17.0f]];
    [button setTitleColor:[UIColor colorWithRed:22/255.f green:66/255.f blue:113/255.f alpha:1.f] forState:UIControlStateNormal];
    [button setBackgroundImage:btnbackground forState:UIControlStateNormal];

    [button sizeToFit];
    //[button setAutoresizingMask:(UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight)];
    [button addTarget:self action:selectorName forControlEvents:UIControlEventTouchUpInside];
    button.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
    button.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;

    [view addSubview:button];
    barButton = [[UIBarButtonItem alloc] initWithCustomView:view];
    
    barButton.enabled = isEnabled;
    return barButton;
}

-(void)showSettingMenu:(id)sender
{    
    SettingsViewController *svc = [[SettingsViewController alloc] init];
    
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:svc];

    if(self.pickerPopOver != nil)
    {
        [pickerPopOver dismissPopoverAnimated:YES];
        self.pickerPopOver = nil;
    }
    
    navi.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
    navi.modalPresentationStyle = UIModalPresentationFormSheet;
    [self presentViewController:navi animated:YES completion:nil];
}

-(void)ShowRoleDisabledAlert
{
    // Travel cell is disabled so show disabled alert.
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:[Localizer getLocalizedText:@"ROLE_DISABLED_ALERT_TITLE"]
                          message:[Localizer getLocalizedText:@"ROLE_DISABLED_TEXT"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                          otherButtonTitles:nil];
    [alert show];
    
}

-(void)ShowModuleDisabledAlert
{
    // Travel cell is disabled so show disabled alert.
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:[Localizer getLocalizedText:@"MODULE_DISABLED_ALERT_TITLE"]
                          message:[Localizer getLocalizedText:@"MODULE_DISABLED_ALERT_TEXT"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                          otherButtonTitles:nil];
    [alert show];
    
}


#pragma -mark actionSheet delegate function
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (actionSheet.tag == kACTION_BOOK_TRAVEL)
    {
        [self checkOffline];
        
        if(![ExSystem connectedToNetwork])
        {
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Offline"]
                                  message:[Localizer getLocalizedText:@"Bookings offline"]
                                  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
            [alert show];
            return;
        }
        
        
        if(pickerPopOver != nil)
        {
            [pickerPopOver dismissPopoverAnimated:YES];
            pickerPopOver = nil;
        }

        if (buttonIndex == 0)       // book air
        {
            [self btnBookFlightsPressed];
        }
        else if (buttonIndex == 1)  // book hotel
        {
            [self btnBookHotelPressed];
        }
        else if (buttonIndex == 2)  // book car
        {
            [self btnBookCarPressed];
        }
        else if (buttonIndex == 3)  // book rail
        {
            [self btnBookRailPressed];
        }
    }
}


#pragma -mark Utilite methods
-(BOOL) checkBookAir
{
    NSString* msg = nil;
    if (!([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_AIR_BOOKING_ENABLED]))
    {
        msg = [Localizer getLocalizedText:@"AIR_BOOKING_DISABLED_MSG"];
    }
    else
    {
        NSString* profileStatus = [[ExSystem sharedInstance] getUserSetting:@"ProfileStatus" withDefault:@"0"];
        // MOB-10390 Allow users with profileStatus 1 (missing middlename, gender) to go ahead and search air.
        if (![profileStatus isEqualToString:@"0"] && ![profileStatus isEqualToString:@"1"])
        {
            if ([profileStatus isEqualToString:@"20"])
                profileStatus = @"2";
            NSString* msgKey = [NSString stringWithFormat:@"AIR_BOOKING_PROFILE_%@_MSG", profileStatus];
            msg = [NSString stringWithFormat:@"%@\n\n%@", [Localizer getLocalizedText:msgKey], [@"AIR_BOOKING_PROFILE_PROLOG_MSG" localize]];
        }
        else
            return TRUE;
    }
    
    MobileAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:nil
                              message:msg
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                              otherButtonTitles:nil];
    [alert show];
    return FALSE;
}


#pragma mark -
#pragma mark Alert Methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == PRIVACY_ACT_ALERT_ACTION)
    {
        // Display rules of behavior for gov Safe harbor required user
        if ([[postLoginAttribute objectForKey:@"NEED_SAFEHARBOR"] isEqualToString:@"true"])
        {
            GovLoginNoticeVC *noticeVC = [[GovLoginNoticeVC alloc] initWithNibName:@"LoginHelpTopicVC" bundle:nil];
            noticeVC.title = [Localizer getLocalizedText:@"Rules of Behavior"];
            UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:noticeVC];
            localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
            [self presentViewController:localNavigationController animated:YES completion:nil];
        }
    }
}


#pragma mark -
#pragma mark Collection View methods
/**
 Setup collection view
 */
- (void)setupCollectionView
{
    self.homeCollectionView.govDelegate = self;
    [self rotateHomeCollectionViewToInterfaceOrientation:self.interfaceOrientation];
}

/**
 Update the collection view based on roles
 */
- (void)updateCollectionViewLayout
{
    // Overall Home layout
    // Uses ExSystem to figure out what layout to use
    [self.homeCollectionView switchLayoutToGovOnly];

    if (![[ExSystem sharedInstance] siteSettingAllowsTravelBooking]) {
        // Do we need to disable trips cell too?  If I remember correctly, this is for booking only.
        [self.homeCollectionView disableFlightBooking];
        [self.homeCollectionView disableHotelBooking];
        [self.homeCollectionView disableCarBooking];
        [self.homeCollectionView disableRailBooking];
    }
}

/**
 The layout of the HomeCollectionView could change quite a bit depending on orientation.
 */
- (void)rotateHomeCollectionViewToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
    if (UIInterfaceOrientationIsPortrait(toInterfaceOrientation)) {
        [self.homeCollectionView switchToPortrait];
    } else {
        [self.homeCollectionView switchToLandscape];
    }
}
@end
