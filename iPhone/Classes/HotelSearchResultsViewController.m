//
//  HotelSearchResultsViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelSearchResultsViewController.h"
#import "ExSystem.h" 

#import "FindHotels.h"
#import "HotelCollectionViewController.h"
#import "HotelListViewController.h"
#import "HotelMapViewController.h"
#import "HotelSearch.h"
#import "RoomListViewController.h"
#import "RoundedRectView.h"
#import "LabelConstants.h"
#import "HotelBenchmarkData.h"
#import "Config.h"


@implementation HotelSearchResultsViewController

#define SORT_BY_PREFERRED_VENDORS	0
#define SORT_BY_VENDOR_NAMES		1
#define SORT_BY_PRICE				2
#define SORT_BY_DISTANCE			3
#define SORT_BY_RATING				4
#define SORT_BY_MOST_RECOMMENDED    5
#define POLLING_TIMEOUT             90
#define POLLING_INTERVAL            2

#pragma mark -
#pragma mark Hotel and Room methods

- (void)showRoomsForSelectedHotel: (EntityHotelBooking*)hotelBooking
{
    if ([hotelBooking.isNoRates boolValue] || [hotelBooking.isSoldOut boolValue]) {
        //Display error message
        MobileAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:nil
                                  message:[Localizer getLocalizedText:@"NO_RATES_SOLD_OUT_USER_MESSAGE"]
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                  otherButtonTitles:nil];
        [alert show];
        return;
    }
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: hotelBooking, @"HOTEL_BOOKING", self.hotelSearch, @"HOTEL_SEARCH", self.hotelSearch.hotelSearchCriteria, @"HOTEL_SEARCH_CRITERIA", @"YES", @"SKIP_CACHE", nil];
	
//	// If hotel details (which includes room data) is already available, then don't send another request to the server
	if (hotelBooking.relHotelRoom != nil && [hotelBooking.relHotelRoom count] > 0)
    {
		pBag[@"SHORT_CIRCUIT"] = @"YES";
    }
	
    RoomListViewController *nextController = [[RoomListViewController alloc] initWithNibName:@"RoomListViewController" bundle:nil];
    nextController.taFields = self.taFields;
    // For Flurry
    nextController.isVoiceBooking = self.isVoiceBooking;
    nextController.travelPointsInBank = self.travelPointsInBank;

    // call the view before pushing it onto the navigation controller, otherwise the loading waitscreen detects a presentingViewController and doesn't resize correctly.
    [nextController view]; // added this for iOS7, so that the outlets are set before 'respondToFoundData:' message is sent
    [self.navigationController pushViewController:nextController animated:YES];

    if (pBag[@"SHORT_CIRCUIT"] != nil) 
    {
        Msg *msg = [[Msg alloc] init];
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        [nextController respondToFoundData:msg];
    }
    else
    {
        [[ExSystem sharedInstance].msgControl createMsg:FIND_HOTEL_ROOMS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:nextController];
        
        BOOL isRecommendedHotel = [hotelBooking.recommendationSource length] ? YES : NO;
        BOOL isAnyHotelRecommended = isRecommendedHotel || [[HotelBookingManager sharedInstance] isAnyHotelRecommended];
        NSDictionary *dict = @{@"Recommended": (isRecommendedHotel ? @"YES" : @"NO"), @"Search had recommendations" : (isAnyHotelRecommended ? @"YES" : @"NO"), @"Property ID": (hotelBooking.propertyId ?: @"")};
        [Flurry logEvent:@"Hotel Recommendations: Hotel Rates Viewed" withParameters:dict];
    }
}


#pragma mark -
#pragma mark MobileViewController Methods

-(NSString *)getViewIDKey
{
	return HOTEL_SEARCH_RESULTS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void)respondToFoundData:(Msg *)msg
{
    [[MCLogging getInstance] log:@"HotelSearchResultsViewController::respondToFoundData" Level:MC_LOG_DEBU];
	if (msg.parameterBag != nil &&
		(msg.parameterBag)[@"SHOW_HOTELS"] != nil &&
		(msg.parameterBag)[@"HOTEL_SEARCH"] != nil)
	{
		self.hotelSearch = (HotelSearch*)(msg.parameterBag)[@"HOTEL_SEARCH"];

        // Check if a handled error was returned by Search3
        FindHotels *findHotels = (FindHotels*)msg.responder;
        if ([findHotels.commonResponseUserMessage length])
        {
            [self killTimer];

            // Log the search metrics
            [self logSearchComplete:YES withError:@"Search failed - check GDS logs"];
            
            // Display error to the customer
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Search Failed"]
                                  message:[findHotels commonResponseUserMessage]
                                  delegate:self
                                  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
                                  otherButtonTitles:nil];
            [alert show];
            [self destroyPollingView];
        }
        else
        {
            if (self.hotelSearch.isPolling)
            {
                // We are using polling/hotel-streaming
                if (self.hotelSearch.isFinal)
                {
                    // if we have received a final flag, then kill the timer to stop checking for a timeout
                    if (self.timer)
                    {
                        [self killTimer];
                    }
                }
                else
                {
                    // Set the flag so that we can send a new poll request
                    self.readyToSendPollRequest = YES;
                }
            }

            // Select the first hotel result by default
            if (self.hotelSearch.hotels != nil && [self.hotelSearch.hotels count] > 0)
                [self.hotelSearch selectHotel:0];
            
            int resultCount = [(msg.parameterBag)[@"TOTAL_COUNT"] intValue];
            if (resultCount > 0)
            {
                // Only set the totalCount on the first load of hotel data, polling does not return a value and will
                self.totalCount = resultCount;
            }
            else if (self.hotelSearch.isFinal && findHotels != nil)
            {
                self.totalCount = findHotels.totalCount; // totalcount may change in the final polling result (some hotels may be out of policy and removed by MWS)
            }
            
            // Note: the toolbar must be made BEFORE the listController and mapControllers
            // are notified of the change since they may try to change th toolbar in response,
            if (!self.hotelSearch.isPolling || self.hotelSearch.isFinal)
            {
                [self makeToolbar];
            }
            
            if ([findHotels.hotelBenchmarks count]) {
                self.hotelBenchmarks = findHotels.hotelBenchmarks;
            }
            if ([findHotels.travelPointsInBank length]) {
                self.travelPointsInBank = findHotels.travelPointsInBank;
            }
            
            // Notify both controllers of the change so each can update its view.
            // Keeping both up-to-date ensures a smooth transition animation between views.
            [self.listController notifyChange];
            self.listController.hotelSearchCriteria = (msg.parameterBag)[@"HOTEL_SEARCH_CRITERIA"];
            self.listController.hotelSearch = (msg.parameterBag)[@"HOTEL_SEARCH"];
            [self.mapController notifyChange];
            if (!self.hotelSearch.isPolling || self.hotelSearch.isFinal)
            {
                self.title = [Localizer getLocalizedText:@"HOTEL_SEARCH_RESULTS"];

                // enable the map/list switch navigation button
                self.navigationItem.rightBarButtonItem = self.switchButton;

                // Show the back button and enable it again
                [self.activeViewController updateToolbar];
                [self.navigationItem setHidesBackButton:NO];

                // Remove the hotel rate pooling wait screen
                [self destroyPollingView];

                // On rare occasions MWS sends back a message saying that it is finished, when no rates have been found
                // But this only happens for streaming
                if (self.hotelSearch.isPolling && self.hotelSearch.ratesFound == NO)
                {
                    // Log the search metrics
                    [self logSearchComplete:YES withError:@"No rates were received"];
                    // No rates were received, so warm the user and return to previous VC
                    [self noRatesReceived];
                }
                else
                {
                    [self logSearchComplete:YES withError:nil];
                }
            }
        
            // If the poll flag is set, and we have not reached the timeout
            if (self.readyToSendPollRequest && self.ticks < POLLING_TIMEOUT)
            {
                // If the timer is nil, then we are about to send the first poll request
                if (!self.timer || !self.timer.isValid)
                {
                    // Create polling wait message
                    [self createPollingView];
                    
                    // Setup the timer
                    self.timer = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(timerCallback:) userInfo:nil repeats:YES];
                    // Record the polling start time
                    self.pollingStartTime = CACurrentMediaTime();
                }
                
                // Set the poll flag so that no further poll requests can be sent by mistake
                self.readyToSendPollRequest = NO;
                
                // Time to send a call to get the hotel rates
                [self sendHotelPollingMsg];
            }
        }
	}
}

#pragma mark - Flurry metrics
// Record search metrics in Flurry
-(void)logSearchComplete:(BOOL)success withError:(NSString*)errMsg
{
    CFTimeInterval totalSearchDuration = CACurrentMediaTime() - self.hotelSearch.searchStartTime;
    NSString *errorMessage = @"";
    if (errMsg != nil)
    {
        errorMessage = errMsg;
    }
    if (self.hotelSearch.isPolling)
    {
        // Hotel streaming ON
        CFTimeInterval pollingDuration = CACurrentMediaTime() - self.pollingStartTime;
        NSDictionary *dictionary = @{@"Success": success ? @"YES" : @"NO", @"Failure": errorMessage, @"SearchDuration": [NSString stringWithFormat:@"%f", totalSearchDuration], @"NumberOfPolls": [NSString stringWithFormat:@"%i", self.numberOfPolls], @"PollingDuration": [NSString stringWithFormat:@"%f", pollingDuration]};
        [Flurry logEvent:@"Book: Hotel Search (Streaming ON)" withParameters:dictionary];
    }
    else
    {
        // Hotel streaming OFF
        NSDictionary *dictionary = @{@"Success": success ? @"YES" : @"NO", @"Failure": errorMessage, @"SearchDuration": [NSString stringWithFormat:@"%f", totalSearchDuration]};
        [Flurry logEvent:@"Book: Hotel Search (Streaming OFF)" withParameters:dictionary];
    }

}

#pragma mark - Alert Stuff
-(void) alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    [self.navigationController popViewControllerAnimated:YES];
}

#pragma mark - Timer
-(void) killTimer
{
    self.readyToSendPollRequest = NO;
    [self.timer invalidate];
    self.timer = nil;
}
// Callback method called when the timer scheduled interval is met
-(void)timerCallback:(NSTimer *) theTimer
{
	[[MCLogging getInstance] log:@"HotelSearchResultsViewController::timerCallback" Level:MC_LOG_DEBU];
    self.ticks++;
    // Check if we have reached the polling timeout
    if (self.ticks >= POLLING_TIMEOUT && self.timer != nil)
    {
        [self killTimer];
        
        // Log the search metrics
        [self logSearchComplete:YES withError:@"Polling timed out"];

        // Display timeout message and return to previous VC
        [self noRatesReceived];
    }
}

// Display message that no rates were received and return control to previous VC
-(void)noRatesReceived
{
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:[Localizer getLocalizedText:@"HOTEL_VIEW_TIMEOUT_TITLE"]
                          message:[Localizer getLocalizedText:@"HOTEL_VIEW_TIMEOUT_MESSAGE"]
                          delegate:self
                          cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
                          otherButtonTitles:nil];
    [alert show];
    
    // Remove the hotel rate pooling wait screen
    [self destroyPollingView];

}

#pragma mark - Polling Message

// Send the hotel polling message
-(void) sendHotelPollingMsg
{
	[[MCLogging getInstance] log:@"HotelSearchResultsViewController::sendHotelPollingMsg" Level:MC_LOG_DEBU];
    NSMutableDictionary *pBag = nil;
        pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.hotelSearch, @"HOTEL_SEARCH", @"YES", @"SHOW_HOTELS", @"YES", @"SKIP_CACHE", @"0", @"STARTPOS", @"30", @"NUMRECORDS", self.hotelSearch.pollingID, @"POLLINGID", nil];
	[[ExSystem sharedInstance].msgControl createMsg:FIND_HOTELS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    self.numberOfPolls++;
}

#pragma mark - Polling Wait View

// Create the wait screen which is shown during hotel polling
-(void)createPollingView
{
	[[MCLogging getInstance] log:@"HotelSearchResultsViewController::createPollingView" Level:MC_LOG_DEBU];
    
	// Find the view that will parent the polling view
    UIView *parentView = self.navigationController.view;
	
	float pw = parentView.bounds.size.width;
	float ph = parentView.bounds.size.height;
    
	float w = 200;
	float h = 100;
    
	// Create the polling view
	self.pollingView = [[RotatingRoundedRectView alloc] initWithFrame:CGRectMake((pw - w) / 2, (ph - h) / 2, w, h)];
    self.pollingView.isRotatingDisabled = YES;
	
	// Make a "Fetching..." label
	UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, (h / 2) - 45, w, 37)];
	[label setText:[Localizer getLocalizedText:@"Fetching Hotel Rates"]];
	[label setBackgroundColor:[UIColor clearColor]];
	[label setTextAlignment:NSTextAlignmentCenter];
	[label setFont:[UIFont boldSystemFontOfSize:18.0f]];
	[label setTextColor:[UIColor whiteColor]];
	[label setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
	[label setShadowOffset:CGSizeMake(0.0f, -1.0f)];
	label.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleBottomMargin;
	[self.pollingView addSubview:label];
	
	// Create an activity indicator
	const CGFloat activityDiameter = 37.0;
	UIActivityIndicatorView *activity = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake((w / 2) - (activityDiameter / 2), label.frame.origin.y + label.frame.size.height + 4, activityDiameter, activityDiameter)];
	[activity setHidesWhenStopped:YES];
	[activity setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleWhiteLarge];
	activity.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleBottomMargin;
	[activity startAnimating];
	[self.pollingView addSubview:activity];

    [self makeCancelButton];
    
    // Create a view that will cover everything behind the polling view
    self.coverView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, pw, ph - 42)]; // Incs ref count by 2
    // Decs ref count by 1
    [self.coverView setBackgroundColor:[UIColor whiteColor]];
    if ([ExSystem is7Plus])
    {
        // the white-out effect needs to be more intense for iOS7
        [self.coverView setAlpha:0.6];
    }
    else
    {
        [self.coverView setAlpha:0.3];
    }
    [parentView addSubview:self.coverView];
    [self.coverView setHidden:NO];

	// Add the polling view to its parent and show it
	[parentView addSubview:self.pollingView];
	[self.pollingView setHidden:NO];
    [parentView bringSubviewToFront:self.pollingView];

}

// Destroy the wait screen which is shown during hotel polling
-(void)destroyPollingView
{
	[[MCLogging getInstance] log:@"HotelSearchResultsViewController::destroyPollingView" Level:MC_LOG_DEBU];
    if (self.pollingView)
    {
        [self.pollingView removeFromSuperview];
        self.pollingView = nil;
        [self.coverView removeFromSuperview];
        self.coverView = nil;
    }
}

#pragma mark -
#pragma mark - Button and Misc

- (IBAction)btnSwitchViews:(id)sender
{
	[self switchViews];
}

- (IBAction)buttonReorderPressed:(id)sender
{
	[self.activeViewController buttonReorderPressed:sender];
}

- (IBAction)buttonActionPressed:(id)sender
{
	[self.activeViewController buttonActionPressed:sender];
}

-(void)showMap
{
	// Switch to map view if it's not already showing
	if (self.listController == self.activeViewController)
	{
		[self switchViews];
	}
}

// Method called when the cancel button is clicked to stop the hotel polling
-(IBAction)btnCancel:(id)sender
{
    [self killTimer];
    [self destroyPollingView];
    [self.navigationController popViewControllerAnimated:YES];

}

#pragma mark -
#pragma mark - Animation

- (void)switchViews
{
	// Show minimal toolbar (search criteria only, no buttons) while the views are switching
	[self showMinimalToolbar];
	
	// Switch the views
	[UIView beginAnimations:@"View Flip" context:nil];
	[UIView	setAnimationDuration:1.25];
	[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
	
	if (self.listController == self.activeViewController)
	{
		[UIView setAnimationTransition:UIViewAnimationTransitionFlipFromRight forView:self.view cache:YES];
		[self.mapController viewWillAppear:YES];
		[self.listController viewWillDisappear:YES];
		
		[self.listController.view removeFromSuperview];
		self.activeViewController = self.mapController;
		[self.view insertSubview:self.mapController.view atIndex:0];
		
		[self.listController viewDidDisappear:YES];
		[self.mapController viewDidAppear:YES];
	}
	else
	{
		[UIView setAnimationTransition:UIViewAnimationTransitionFlipFromLeft forView:self.view cache:YES];
		[self.listController viewWillAppear:YES];
		[self.mapController viewWillDisappear:YES];
		
		[self.mapController.view removeFromSuperview];
		self.activeViewController = self.listController;
		[self.view insertSubview:self.listController.view atIndex:0];
		
		[self.mapController viewDidDisappear:YES];
		[self.listController viewDidAppear:YES];
	}
	
	[UIView setAnimationDelegate:self];
	[UIView setAnimationDidStopSelector:@selector(animationDidStop:finished:context:)];
	
	[UIView commitAnimations];
	
	// Switch the button title
	self.switchButton.title = (self.activeViewController == self.listController ? [Localizer getLocalizedText:@"Map"] : [Localizer getLocalizedText:@"List"]);
}

- (void)animationDidStop:(NSString*)animationID finished:(NSNumber*)finished context:(void*)context
{
	[self.activeViewController didSwitchViews];
	[self.activeViewController updateToolbar];
}


#pragma mark -
#pragma mark Initialization

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
	if (!(self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) return nil;
	
	self.listController = [[HotelListViewController alloc] initWithNibName:@"HotelListViewController" bundle:nil];
    self.listController.hotelSearchMVC = self;
    [self.listController setSeedData];
	
	self.mapController = [[HotelMapViewController alloc] initWithNibName:@"HotelMapViewController" bundle:nil];
	self.mapController.parentMVC = self;

	self.activeViewController = self.listController;
	
	return self;
}


#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
	NSString* title = (self.activeViewController == self.listController ? [Localizer getLocalizedText:@"Map"] : [Localizer getLocalizedText:@"List"]);
	self.switchButton = [[UIBarButtonItem alloc] initWithTitle:title style:UIBarButtonItemStyleBordered target:self action:@selector(btnSwitchViews:)];
	
    // Ideally this check should not be needed, but for some reason, even though we un-hide the button later, it was not appearing
    if([[ExSystem sharedInstance] siteSettingHotelStreamingEnabled])
    {
        [self.navigationItem setHidesBackButton:YES];
    }
    
    // Uncomment the following line to preserve selection between presentations.
    //self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
	
	//if([UIDevice isPad])
	// So rvc knows where to post the response
    [ExSystem sharedInstance].sys.topViewName = [self getViewIDKey];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    self.navigationController.toolbarHidden = NO;

	if([UIDevice isPad])
	{
 		self.listController.view.frame = CGRectMake(0, 0, 540, 620 - 88);
		self.mapController.view.frame = CGRectMake(0, 0, 540, 620 - 88);
	}
	
    [self.listController view];
	[self.listController viewWillAppear:animated];
	[self.view insertSubview:self.listController.view atIndex:0];
	[self.listController viewDidAppear:YES];

	[self.mapController viewWillAppear:animated];
	[self.view insertSubview:self.mapController.view atIndex:0];
	[self.mapController viewDidAppear:YES];

    // MOB-5024 When entering from landscape the list/map view is not sized correctly.
    self.listController.view.frame = self.view.frame;
    self.mapController.view.frame = self.view.frame;

	// Bring the active view to the front
	[self.view bringSubviewToFront:self.activeViewController.view];
    
    if (self.pollingView)
    {
        // Addressing an iOS7 issue where the polling view failed to appear.
        [self.view bringSubviewToFront:self.coverView];
        [self.view bringSubviewToFront:self.pollingView];
    }
	
	[self.activeViewController updateToolbar];
}

- (void)viewWillDisappear:(BOOL)animated
{
	if ([self.listController isViewLoaded])
		[self.listController viewWillDisappear:YES];
	
	if ([self.mapController isViewLoaded])
		[self.mapController viewWillDisappear:YES];
	
	[super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	if ([self.listController isViewLoaded])
	{
		[self.listController.view removeFromSuperview];
		[self.listController viewDidDisappear:YES];
	}

	if ([self.mapController isViewLoaded])
	{
		[self.mapController.view removeFromSuperview];
		[self.mapController viewDidDisappear:YES];
	}

	[super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


#pragma mark -
#pragma mark Toolbar Methods

+ (UIBarButtonItem *)makeSearchCriteriaButton:(HotelSearchCriteria*)hotelSearchCriteria
{
	NSString *checkinDate = [DateTimeFormatter formatDateForCarOrHotelTravelByDate:hotelSearchCriteria.checkinDate];
	NSString *checkoutDate = [DateTimeFormatter formatDateForCarOrHotelTravelByDate:hotelSearchCriteria.checkoutDate];
	NSString *searchCriteria = [NSString stringWithFormat:@"%@ -\n%@", checkinDate, checkoutDate];
	
	const int buttonWidth = 110;
	const int buttonHeight = 30;
	
	UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	
	UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	lblText.numberOfLines = 2;
	lblText.lineBreakMode = NSLineBreakByWordWrapping;
	lblText.textAlignment = NSTextAlignmentLeft;
	lblText.text = searchCriteria;
	[lblText setBackgroundColor:[UIColor clearColor]];
	[lblText setTextColor:[UIColor whiteColor]];
	[lblText setShadowColor:[UIColor grayColor]];
	[lblText setShadowOffset:CGSizeMake(1, 1)];
	[lblText setFont:[UIFont boldSystemFontOfSize:12.0f]];
	[cv addSubview:lblText];

	__autoreleasing UIBarButtonItem* btnSearchCriteria = [[UIBarButtonItem alloc] initWithCustomView:cv];
	
	return btnSearchCriteria;
}

- (UIBarButtonItem*)makeHotelCountButton:(int)resultCount
{
	const int buttonWidth = 200;
	const int buttonHeight = 32;
	
	UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	
	UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	lblText.numberOfLines = 1;
	lblText.lineBreakMode = NSLineBreakByTruncatingTail;
	lblText.textAlignment = NSTextAlignmentCenter;
//	NSString *numberOfHotelsStr = [Localizer getLocalizedText:@"Results"];
//	NSString *foundStr = [Localizer getLocalizedText:@"FOUND_COUNT"];
//	NSString *numberOfHotelsFormatString = [NSString stringWithFormat:@"%@", numberOfHotelsStr];
	
//#define SORT_BY_PREFERRED_VENDORS	0
//#define SORT_BY_VENDOR_NAMES		1
//#define SORT_BY_PRICE				2
//#define SORT_BY_DISTANCE			3
//#define SORT_BY_RATING				4
    NSString *sortName = nil;
    switch(self.childSortOrder)
    {
        case SORT_BY_PREFERRED_VENDORS:
            sortName = [Localizer getLocalizedText:@"Preferred Vendors"];
            break;
        case SORT_BY_VENDOR_NAMES:
            sortName = [Localizer getLocalizedText:@"Vendor Names"];
            break;
        case SORT_BY_PRICE:
            sortName = [Localizer getLocalizedText:@"Price"];
            break;
        case SORT_BY_DISTANCE:
            sortName = [Localizer getLocalizedText:@"Distance"];
            break;
        case SORT_BY_RATING:
            sortName = [Localizer getLocalizedText:@"Star Rating"];
            break;
        case SORT_BY_MOST_RECOMMENDED:
            sortName = [Localizer getLocalizedText:@"Recommendation"];
            break;
    }
    //NSLog(@"%@", [NSString stringWithFormat:@"%d %@ %@ %@", resultCount, [Localizer getLocalizedText:@"Results"], [Localizer getLocalizedText:@"sorted by"], sortName]);
    lblText.text = [NSString stringWithFormat:@"%d %@ %@", resultCount, [Localizer getLocalizedText:@"results by"], sortName];
    
    if(![ExSystem is7Plus])
	{
        // Only change the results text color if using iOS6
        [lblText setBackgroundColor:[UIColor clearColor]];
        [lblText setTextColor:[UIColor whiteColor]];
    }
    [lblText setFont:[UIFont systemFontOfSize:11]];
//	[lblText setShadowColor:[UIColor grayColor]];
//	[lblText setShadowOffset:CGSizeMake(1, 1)];
	[cv addSubview:lblText];
	
	__autoreleasing UIBarButtonItem* btnResultCount = [[UIBarButtonItem alloc] initWithCustomView:cv];
	
	
	return btnResultCount;
}

// Create the cancel button used to cancel hotel polling
-(void)makeCancelButton
{
    self.navigationController.toolbarHidden = NO;
    
    UIBarButtonItem *btnCancel = nil;
    if ([ExSystem is7Plus])
    {
        btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN] style:UIBarButtonSystemItemCancel target:self action:@selector(btnCancel:)];
        [btnCancel setTintColor:[UIColor redColor]];
    }
    else
        btnCancel = [ExSystem makeColoredButton:@"RED" W:100 H:30.0 Text:[Localizer getLocalizedText:LABEL_CANCEL_BTN]  SelectorString:@"btnCancel:" MobileVC:self];
    
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSArray *toolbarItems = @[flexibleSpace, btnCancel, flexibleSpace];
	[self setToolbarItems:toolbarItems animated:YES];
}

- (void)makeToolbar
{
    int resultCount = [[[HotelBookingManager sharedInstance] fetchAll] count];
	self.btnSearchCriteria = [HotelSearchResultsViewController makeSearchCriteriaButton:self.hotelSearch.hotelSearchCriteria];
	self.btnFlexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	self.btnHotelCount = [self makeHotelCountButton:resultCount];
	//self.btnReorder = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRewind target:nil action:@selector(buttonReorderPressed:)];		
    self.btnAction = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Sort"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonActionPressed:)];
	[self showFullToolbar];
}

- (void)showMinimalToolbar
{
    int resultCount = [[[HotelBookingManager sharedInstance] fetchAll] count];
    self.btnFlexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    self.btnHotelCount = [self makeHotelCountButton:resultCount];
	NSArray *toolbarItems = @[self.btnFlexibleSpace, self.btnHotelCount, self.btnFlexibleSpace];
	[self setToolbarItems:toolbarItems animated:NO];
}

- (void)showFullToolbar
{
    if (self.hotelSearch.isFinal || !self.hotelSearch.isPolling)
    {
        // We only want to render this toolbar when we have received all the results, or if hotel polling is off
        if (!self.btnAction)
            self.btnAction = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Sort"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonActionPressed:)];
        if (!self.btnFlexibleSpace) 
            self.btnFlexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
        int resultCount = [[[HotelBookingManager sharedInstance] fetchAll] count];
        self.btnHotelCount = [self makeHotelCountButton:resultCount];
        //NSMutableArray *toolbarItems = [NSArray arrayWithObjects:btnSearchCriteria, btnFlexibleSpace, btnReorder, btnAction, nil];
        NSArray *toolbarItems = @[self.btnAction, self.btnFlexibleSpace, self.btnHotelCount, self.btnFlexibleSpace];
        [self setToolbarItems:toolbarItems animated:NO];
    }
}

- (void)setHotelBenchmarks:(NSArray *)hotelBenchmarks
{
    _hotelBenchmarks = hotelBenchmarks;
    self.hotelBenchmarkRangeString = [HotelBenchmarkData getBenchmarkRangeFromBenchmarks:hotelBenchmarks];
    if ([self.hotelBenchmarkRangeString length]) { // Find if Range string is actually a Range or it has just one unique value
        NSArray *distinctPrices = [hotelBenchmarks valueForKeyPath:@"@distinctUnionOfObjects.price"];
        BOOL benchmarksHaveUnavailablePrices = [[hotelBenchmarks valueForKeyPath:@"@min.price"] floatValue] == 0.0;
        int countOfUniqueBenchmarks = benchmarksHaveUnavailablePrices ? [distinctPrices count] - 1 : [distinctPrices count];
        self.isBenchmarkRange = countOfUniqueBenchmarks > 1;
    }
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
}

@end

