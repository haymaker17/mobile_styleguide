//
//  TripDetailsViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/09.
//  Copyright 2009 Concur Technologies. All rights reserved.
//  Updated by Pavan: 11/02/2012
//  Added/updated new code for enabling offers with coredata
//  Cleanup dead code.
//

#import "FeedbackManager.h"
#import "TripDetailsViewController.h"
#import "ExSystem.h" 

#import "SampleData.h"
#import "ViewConstants.h"
#import "TripData.h"
#import "SegmentData.h"
#import "TripAirSegmentCell.h"
#import "LabelConstants.h"
#import "Location.h"
#import "MobileActionSheet.h"
#import "MapViewController.h"
#import "OfferCell.h"
#import "OfferMultiLinkVC.h"
#import "AppsUtil.h"
#import "HotelViewController.h"
#import "CarViewController.h"
#import "TripRejectCommentVC.h"
#import "TripApproveOrReject.h"
#import "AgencyAssistanceData.h"
#import "TripViolationSummaryVC.h"
#import "HotelSearchTableViewController.h"


#import "GovTAField.h"
#import "GovDutyLocationVC.h"
#import "GovDocInfoFromTripLocatorData.h"
#import "GovDocDetailVC.h"

@interface TripDetailsViewController (Private) 
-(void)configureOfferCell:(OfferCell *)cell offer:(EntityOffer*)offer;
-(void) updateOfferToolbarBtn:(NSString *)text;
-(void) checkOfferDisplaySelection;
-(void) startListeningToCurrentLocationUpdates;
-(void) stopListeningToCurrentLocationUpdates;
@end

@implementation TripDetailsViewController
@synthesize isOffersHidden, hasValidOffers ,filteredSegments,showBookingOption;
@synthesize tripsData;
@synthesize listKeys;
@synthesize tableList;
@synthesize navBar;

@synthesize labelTripName;
@synthesize labelStart;
@synthesize labelEnd;
@synthesize labelLocator;
@synthesize trip;

@synthesize tripKey, lastTripKey;
@synthesize tripBits;
@synthesize keys;

@synthesize	fetchView;
@synthesize	lblFetch;
@synthesize	spinnerFetch, wentSomewhere;
@synthesize lblDate,lblAmount,lblBottom,lblName,viewApprovalHeader, isApproval, tripToApprove;

#define		kAlertViewRateApp	101781
#define kAlertViewConfirmTripApproval 121223
#define kAlertViewConfirmAgentCall 121443

#pragma mark - MVC Methods
-(NSString *)getViewIDKey
{
	return TRIP_DETAILS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

-(void)makeFilteredSegments
{
    if (filteredSegments == nil) {
        self.filteredSegments = [[NSMutableDictionary alloc] init];
    }
    else {
        [filteredSegments removeAllObjects];
    }
    
    for (NSString *key in keys) {
        NSMutableArray *a = [[NSMutableArray alloc] init];
        filteredSegments[key] = a;
        
        for (EntitySegment* object in tripBits[key]) {
            if (![object isKindOfClass:[EntityOffer class]])
            {
                NSMutableArray *b = filteredSegments[key];
                [b addObject:object];
            }
        }
    }
}

-(void) setupToolbar:(NSDate*) dateOfData
{
    if (self.isApproval) {
        return;
    }
    NSString *dt = [DateTimeFormatter formatDateTimeMediumByDateLTZ:dateOfData];
    dt = [NSString stringWithFormat:[Localizer getLocalizedText:@"Last updated"], dt];
    
    if(![ExSystem connectedToNetwork])
	{
		[self makeOfflineBarWithLastUpdateMsg:dt];
        self.navigationItem.rightBarButtonItem = nil;
	}
    else
    {
		[self makeRefreshButton:dt];
        
        //MOB-12355 Car/ Hotel booking option does not appear on flight booking confirmation.
        if ([[ExSystem sharedInstance] hasTravelBooking] &&
            ([trip.allowAddCar boolValue] || [trip.allowAddHotel boolValue]))
        {
            UIBarButtonItem *btnAction = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonActionPressed:)];
            self.navigationItem.rightBarButtonItem = btnAction;
        }
        else if([[ExSystem sharedInstance] isGovernment])
        {
            UIBarButtonItem *btnAction = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonActionPressed:)];
            self.navigationItem.rightBarButtonItem = btnAction;
        }
        else
            self.navigationItem.rightBarButtonItem = nil;
    }
    
}

-(void)loadTrip:(Msg*)msg
{
    NSString *key = (msg.parameterBag)[@"TRIP_KEY"];
    if ([key lengthIgnoreWhitespace])
    {
        self.tripKey = key;
        self.trip = [[TripManager sharedInstance] fetchByTripKey:self.tripKey];
    }
    else
    {
        NSString *itinLocator = (msg.parameterBag)[@"ITIN_LOCATOR"];
        self.trip = [[TripManager sharedInstance] fetchByItinLocator:itinLocator];
        self.tripKey = self.trip.tripKey;
    }
    self.listKeys = [[NSMutableArray alloc] initWithObjects:self.tripKey,nil];//tripsData.keys;
    
    self.keys = [[NSMutableArray alloc] initWithObjects: nil];
    self.tripBits = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    
    self.keys = [TripData makeSegmentArrayGroupedByDate:trip];
    self.tripBits = [TripData makeSegmentDictGroupedByDate:trip];
    
    if ([self.trip.recordLocator length] || [self.trip.travelPointsPosted length]) {
        [self updateHeaderAgencyRecordLocator:self.trip.recordLocator travelPointsPosted:self.trip.travelPointsPosted];
    }
    
    [self makeFilteredSegments];
    [self loadSummaryData];
}

#define SUMMARY_HEADER @"SUMMARY"
#define VIOLATION_SUMMARY @"Violation Summary"

-(void)loadSummaryData
{
    if (isApproval && [self.trip.relViolation count]) {
        [self.keys insertObject:SUMMARY_HEADER atIndex:0];
        [self.tripBits setObject:@[VIOLATION_SUMMARY] forKey:SUMMARY_HEADER];
        [self.filteredSegments setObject:@[VIOLATION_SUMMARY] forKey:SUMMARY_HEADER];
    }
}

-(void)respondToFoundData:(Msg *)msg
{//respond to data that might be coming from the cache

	if ([msg.idKey isEqualToString:TRIPS_DATA] && ((msg.parameterBag)[@"REFRESHING"] != nil))
	{
        [self loadTrip:msg];

        //MOB-12355 GOV - Car/ Hotel booking option does not appear on flight booking confirmation
		self.tripsData = (TripsData *)msg.responder;
        
        [self checkOfferDisplaySelection];
        
        [self setupToolbar:msg.dateOfData];
        
		[tableList reloadData];
		[fetchView setHidden:YES];
		[spinnerFetch stopAnimating];
        [self hideLoadingView];
        
        // MOB-9206 refresh TripsData for both TripDetails and Trips view, upon car cancel
        [TripsViewController refreshViewsWithTripsData:msg fromView:self];
	}
	else if ([msg.idKey isEqualToString:TRIPS_DATA] && ((msg.parameterBag)[TO_VIEW] != nil))
	{//below is the pattern of getting the object you want and using it.

        // MOB-11059 Need to refresh trip when TRIPS_DATA comes from TRIPS view.
		NSString *toView = (msg.parameterBag)[TO_VIEW];
//		if (![toView isEqualToString:TRIP_DETAILS])
//        {
//            // MOB-9209 refresh TripsData for both TripDetails and Trips view, upon add car
//            [TripsViewController refreshViewsWithTripsData:msg fromView:self];
//			return;
//        }
//        
        // MOB-11059 always refresh trip
        [self loadTrip:msg];

        [self checkOfferDisplaySelection];
		[self.tableList reloadData];
		
        [self setupToolbar:msg.dateOfData];
		
		self.title = trip.tripName;
        labelTripName.text = trip.tripName;
        labelStart.text = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateForTravelByDate:trip.tripStartDateLocal], [DateTimeFormatter formatDateForTravelByDate:trip.tripEndDateLocal]];
        
		UINavigationItem *navItem;
		navItem = [UINavigationItem alloc];
		
		UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
		label.backgroundColor = [UIColor clearColor];
		label.font = [UIFont boldSystemFontOfSize:20.0];
		label.shadowColor = [UIColor colorWithWhite:0.0 alpha:0.5];
		label.textAlignment = NSTextAlignmentCenter;
		label.textColor =[UIColor whiteColor];
		label.text=self.title;		
		navItem.titleView = label;
		
		[navBar pushNavigationItem:navItem animated:YES];
		[navBar setDelegate:self]; 
        // MOB-9206 refresh TripsData for both TripDetails and Trips view.
        if (![toView isEqualToString:@"TRIPS"] && ![toView isEqualToString:TRIP_DETAILS] && !msg.isCache)
        {
            [TripsViewController refreshViewsWithTripsData:msg fromView:self];
        }
        else{
            // MOB-21170 - iOS8 is having a problem with cars. The home view refresh (which loads
            // trips into CoreData) is happening after the itin view loads.
            //
            // So if bookings are missing after a booking is made, we need to call a refresh
            if ([ExSystem is8Plus] && (self.trip.relBooking == nil || [self.trip.relBooking count] == 0))
            {
                [self refreshData];
            }
        }
	}
	else if ([msg.idKey isEqualToString:VENDOR_IMAGE] && (msg.parameterBag != nil))
	{//segment should already be set
		UIImage *gotImg = [[UIImage alloc] initWithData:msg.data];
		UIImageView	*iv = (msg.parameterBag)[@"IMAGE_VIEW"];
		iv.image = gotImg;
	}
    else if ([msg.idKey isEqualToString:APPROVE_TRIPS_DATA])
    {
        if ([self isViewLoaded])
            [self hideWaitView];
        
        TripApproveOrReject *tripApproveMsg = (TripApproveOrReject *)msg.responder;
        
        if (tripApproveMsg.isSuccess) {
            id vc = [self.navigationController viewControllers][[self.navigationController.viewControllers count]  - 2];
            if ([vc respondsToSelector:@selector(setForceFetchTripApprovalList:)]) {
                [vc setForceFetchTripApprovalList:YES];
            }
            [self.navigationController popViewControllerAnimated:NO];
        }
        else {
            MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"Error" localize]
                                                                    message:[@"An unexpected error occurred. Please try again later." localize]//tripApproveMsg.responseErrorMessage
                                                                   delegate:nil
                                                          cancelButtonTitle:[@"OK" localize]
                                                          otherButtonTitles: nil];
            [alert show];
        }
        
    }
    else if ([msg.idKey isEqualToString:TRAVEL_AGENCY_ASSISTANCE_INFO])
    {
        if([self isViewLoaded] && self.view.window)
        {
            [self hideWaitView];
            
            AgencyAssistanceData *assistanceData = (AgencyAssistanceData *)msg.responder;
            if ([assistanceData.changedItinLocator length] && ![trip.itinLocator isEqualToString:assistanceData.changedItinLocator])
            {
                trip.itinLocator = assistanceData.changedItinLocator;
                trip.tripKey = assistanceData.changedItinLocator;
                [[TripManager sharedInstance] saveIt:trip];
                
                self.tripKey = assistanceData.changedItinLocator;
                self.lastTripKey = assistanceData.changedItinLocator;
            }
            
            if ([assistanceData.recordLocator length] && ![trip.recordLocator isEqualToString:assistanceData.recordLocator])
            {
                trip.recordLocator = assistanceData.recordLocator;
                [[TripManager sharedInstance] saveIt:trip];
                [self updateHeaderAgencyRecordLocator:assistanceData.recordLocator travelPointsPosted:trip.travelPointsPosted];
            }
            
            if ([assistanceData.preferredPhoneNumber length]) {
                [self displayCallAgencyAlert:assistanceData];
            }
            else {
                MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"Error" localize]
                                                                        message:[@"An unexpected error occurred. Please try again later." localize]//tripApproveMsg.responseErrorMessage
                                                                       delegate:nil
                                                              cancelButtonTitle:[@"OK" localize]
                                                              otherButtonTitles: nil];
                [alert show];
            }
        }
    }
    else if ([msg.idKey isEqualToString:GOV_DOC_INFO_FROM_TRIP_LOCATOR])
    {
        if ([self isViewLoaded])
            [self hideWaitView];
        
		GovDocInfoFromTripLocatorData *docInfo = (GovDocInfoFromTripLocatorData *)msg.responder;
        
        [self showAuthView:docInfo];
    }
}

-(void) callAgency
{
    self.didMakePhoneCall = YES;
    [Flurry logEvent:@"Travel Agency: Phoned Travel Agent"];
    
    NSString *numberToCallDigitsOnly = [[self.agencyPhoneNumber componentsSeparatedByCharactersInSet:[[NSCharacterSet characterSetWithCharactersInString:@"+0123456789"] invertedSet]] componentsJoinedByString:@""];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@",numberToCallDigitsOnly]]];
}

-(void) displayAgencyAssistAlert
{
    MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"Attention" localize]
                                                            message:[@"If any changes have been made by your agent, your itinerary may take a few minutes to update." localize]
                                                           delegate:self
                                                  cancelButtonTitle:[@"OK" localize]
                                                  otherButtonTitles:nil];
    [alert show];
}

-(void) displayCallAgencyAlert:(AgencyAssistanceData *)assistData
{
    BOOL canPhone = [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"tel://"]];
    NSString *title = assistData.preferredPhoneNumber;
    NSString *displayText;
    if ([assistData.recordLocator length])
    {
        displayText = [NSString stringWithFormat:[@"Your agent may ask for the Trip Record Locator: %@" localize],assistData.recordLocator];
    }
    else
    {
        displayText = [assistData.errorMessage lengthIgnoreWhitespace] ? assistData.errorMessage : [@"Trip Record Locator for this trip could not be retrieved." localize];
    }
    self.agencyPhoneNumber = assistData.preferredPhoneNumber;
    
    MobileAlertView *alert;
    if (canPhone)
    {
        alert = [[MobileAlertView alloc] initWithTitle:title
                                               message:displayText
                                              delegate:self
                                     cancelButtonTitle:[LABEL_CANCEL_BTN localize]
                                     otherButtonTitles:[@"Call" localize], nil];
    }
    else
    {
        alert = [[MobileAlertView alloc] initWithTitle:title
                                               message:[NSString stringWithFormat:@"%@ %@",[@"Your device cannot make a phone call." localize],displayText]
                                              delegate:self
                                     cancelButtonTitle:[LABEL_OK_BTN localize]
                                     otherButtonTitles:nil];
    }
    alert.tag = kAlertViewConfirmAgentCall;
    [alert show];
}

-(void) updateHeaderAgencyRecordLocator:(NSString *)recLoc travelPointsPosted:(NSString *)points
{
    if ([recLoc length]){
        self.labelRecordLocator.text = [NSString stringWithFormat:[@"Agency Record Locator: %@" localize], recLoc];
        self.labelRecordLocator.hidden = NO;
    }
    if ([points length]){
        self.labelTravelPoints.text = [NSString stringWithFormat:[@"Total Travel Points: %@" localize], points];
        self.labelTravelPoints.hidden = NO;
    }
    if ([recLoc length] || [points length])
    {
        self.labelTripName.frame = CGRectMake(self.labelTripName.frame.origin.x, 4.0, self.labelTripName.frame.size.width, self.labelTripName.frame.size.height);
        self.labelTripName.font = [UIFont fontWithName:self.labelTripName.font.fontName size:19];
        self.labelStart.frame = CGRectMake(self.labelStart.frame.origin.x, 26.0, self.labelStart.frame.size.width, self.labelStart.frame.size.height);
        self.labelStart.font = [UIFont fontWithName:self.labelStart.font.fontName size:16];
    }
    if ([recLoc length] && [points length]) {
        self.labelRecordLocator.frame = CGRectMake(self.labelRecordLocator.frame.origin.x, 44.0, self.labelRecordLocator.frame.size.width, self.labelRecordLocator.frame.size.height);
        self.labelTravelPoints.frame = CGRectMake(self.labelTravelPoints.frame.origin.x, 61.0, self.labelTravelPoints.frame.size.width, self.labelTravelPoints.frame.size.height);
        self.imageViewTripHeader.frame = CGRectMake(self.imageViewTripHeader.frame.origin.x, self.imageViewTripHeader.frame.origin.y, self.imageViewTripHeader.frame.size.width, 85.0);
        self.tableList.frame = CGRectMake(self.tableList.frame.origin.x, self.imageViewTripHeader.frame.origin.x + self.imageViewTripHeader.frame.size.height, self.tableList.frame.size.width, self.tableList.frame.size.height);
    }
}

-(void) checkOfferDisplaySelection
{
    NSString *offerDisplayPref = nil;
    
    if ([ExSystem sharedInstance].tripOffersDisplayPreference != nil) 
        offerDisplayPref = ([ExSystem sharedInstance].tripOffersDisplayPreference)[trip.tripKey];
    else
        [ExSystem sharedInstance].tripOffersDisplayPreference = [[NSMutableDictionary alloc] init];
    
    if (offerDisplayPref != nil && [offerDisplayPref isEqualToString:@"NO"]) 
        isOffersHidden = YES;
    else
    {
        if (trip.tripKey != nil) 
            ([ExSystem sharedInstance].tripOffersDisplayPreference)[trip.tripKey] = @"YES";
        
        isOffersHidden = NO;
         hasValidOffers = NO;
    	// Check if there are valid offers in the segment list
    	for (NSString *key in keys)
    	{
        	// if object is of type EntityOffer, there is an offer
        	for (NSObject* object in tripBits[key])
        	{
            	if ([object isKindOfClass:[EntityOffer class]])
            	{
                	hasValidOffers = YES;
                	break;
            	}
        	} 
    	} // end for loop
    }// end else 

}

-(UIBarButtonItem*)getOffersBarButton:(NSString*)t
{
    UIBarButtonItem * showHideOffersBtn = nil;
    if ([ExSystem is7Plus])
        showHideOffersBtn = [[UIBarButtonItem alloc] initWithTitle:t style:UIBarButtonItemStyleBordered target:self action:@selector(showHideOffers)];
    else
        showHideOffersBtn = [ExSystem makeColoredButton:@"DARK_BLUE_OFFERS" W:110 H:32 Text:t SelectorString:@"showHideOffers" MobileVC:self];
	
	return showHideOffersBtn;
}

-(void) updateOfferToolbarBtn:(NSString *)text
{
    UIBarButtonItem *btnShowHideOffers = [self getOffersBarButton:text];
    NSMutableArray *tItems = [NSMutableArray arrayWithArray:self.toolbarItems];
    [tItems removeObjectAtIndex:0];
    [tItems insertObject:btnShowHideOffers atIndex:0];
    
    [self setToolbarItems:tItems animated:YES];
}

-(void) hideOffers
{
    ([ExSystem sharedInstance].tripOffersDisplayPreference)[trip.tripKey] = @"NO";
    isOffersHidden = YES;
    [self updateOfferToolbarBtn:[Localizer getLocalizedText:@"Show Offers"]];
    [tableList reloadData];
}

-(void)showOffers
{
    ([ExSystem sharedInstance].tripOffersDisplayPreference)[trip.tripKey] = @"YES";
    isOffersHidden = NO;
    [self updateOfferToolbarBtn:[Localizer getLocalizedText:@"Hide Offers"]];
    [tableList reloadData];
}

-(void)showHideOffers
{
    (!isOffersHidden)?[self hideOffers]:[self showOffers];
}

#pragma mark - View Controller stuff
-(void) viewWillDisappear:(BOOL)animated
{
    if (self.isApproval) {
        // This checks if the view is unloaded from Navigation Controller
        if ([[self.navigationController viewControllers] indexOfObject:self] == NSNotFound && trip) {
            [[TripManager sharedInstance] deleteObj:trip];
        }
    }
    else if (trip.tripKey != nil) // MOB-7592 In case of cancelled hotel/car booking
    {
        if (isOffersHidden) 
        {
            ([ExSystem sharedInstance].tripOffersDisplayPreference)[trip.tripKey] = @"NO";
        }
        else 
        {
            ([ExSystem sharedInstance].tripOffersDisplayPreference)[trip.tripKey] = @"YES";
        }
    }
    [super viewWillDisappear:animated];
}

-(void)viewWillAppear:(BOOL)animated
{
    [self.navigationController setToolbarHidden:NO];
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated 
{
	if(![lastTripKey isEqualToString:trip.tripKey])
		[tableList setContentOffset:CGPointZero animated:NO];
	
	self.lastTripKey = trip.tripKey;
	
	[super viewDidAppear:animated];
    
    if(wentSomewhere) {
        //[AppRating offerChoiceToRateApp:self alertTag:kAlertViewRateApp];
        // DISABLE feedback manager for Gov
        if (![Config isGov])
        {
            [[FeedbackManager sharedInstance] requestRatingFromViewController:self withBlock:^{
                [self afterChoiceToRateApp];
            }];
        }
    }
    
    if (showBookingOption)
        [self buttonActionPressed:self];
}

-(void)applicationEnteredForeground
{
    if (self.didMakePhoneCall && ![ExSystem isPhoneInUse]) {
        self.didMakePhoneCall = NO;
        [self displayAgencyAssistAlert];
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
	[ConcurMobileAppDelegate  switchViews:sender ParameterBag:nil];
}

- (void) resetAllOutletLabelTexts
{
    self.labelTripName.text = @"";
    self.labelStart.text = @"";
    self.labelRecordLocator.text = @"";
    self.lblName.text = @"";
    self.lblDate.text = @"";
    self.lblAmount.text = @"";
    self.lblBottom.text = @"";
}

- (void)viewDidLoad 
{
    [super viewDidLoad];
	[self resetAllOutletLabelTexts];
	[fetchView setHidden:YES];
    [self hideLoadingView];
	[self makeRefreshButton:@""];
    
    UIBarButtonItem *btnAction = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonActionPressed:)];
    self.navigationItem.rightBarButtonItem = btnAction;
    
    NSDictionary *dict = @{@"Nearest Segment to now in hours": @"Need a value"};
    [Flurry logEvent:@"Itin: View Itin Segments" withParameters:dict];
   
    self.wentSomewhere = NO;
    
    [self checkOfferDisplaySelection];
    [self startListeningToCurrentLocationUpdates];
    
    if ([self.cameFrom isEqualToString:AIR_SELL])
        self.showBookingOption = YES;
    else
        self.showBookingOption = NO;
    
    if (![ExSystem sharedInstance].isGovernment)
    {
        [GlobalLocationManager startTrackingSignificantLocationUpdates];
    }

    viewApprovalHeader.hidden = !self.isApproval;
    if (self.isApproval) {
        [self initTripApproval];
    }
}

-(void)initTripApproval
{
    self.title = [@"Trip Approval" localize];
    self.navigationItem.rightBarButtonItem = nil;
    UIBarButtonItem *btnReject = [[UIBarButtonItem alloc] initWithTitle:[@"Reject" localize] style:UIBarButtonItemStyleBordered target:self action:@selector(rejectTripApprovalWithComment:)];
    UIBarButtonItem *btnApprove = [[UIBarButtonItem alloc] initWithTitle:[@"Approve" localize] style:UIBarButtonItemStyleBordered target:self action:@selector(btnApproveTripPressed:)];
    UIBarButtonItem *btnFlex = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    NSArray *toolbarButtons = @[btnReject,btnFlex,btnApprove];
    [self setToolbarItems:toolbarButtons];
    
    self.lblName.text = self.tripToApprove.travelerName;
    NSMutableString *approveByString = [[NSMutableString alloc] initWithString:[NSString stringWithFormat:@"%@: ",[@"Approve by" localize]]];
    if (self.tripToApprove.approveByDate == nil)
    {
        [approveByString appendString:[@"No date specified" localize]];
    }
    else
    {
        [approveByString appendString:[NSString stringWithFormat:@"%@ %@ %@",[DateTimeFormatter formatDateEEEByDate:self.tripToApprove.approveByDate],[DateTimeFormatter formatDateMediumByDate:self.tripToApprove.approveByDate TimeZone:[NSTimeZone localTimeZone]],[DateTimeFormatter formatDate:self.tripToApprove.approveByDate Format:([DateTimeFormatter userSettingsPrefers24HourFormat]?@"HH:mm zzz":@"hh:mm aaa zzz") TimeZone:[NSTimeZone localTimeZone]]]]; // Change to appropriate Deadline message
    }
    self.lblDate.text = approveByString;
    self.lblBottom.text = self.tripToApprove.tripName;
    self.lblAmount.text = [FormatUtils formatMoneyWithNumber:self.tripToApprove.totalTripCost crnCode:self.tripToApprove.totalTripCostCrnCode];
    [self loadApprovalTrip];
}

- (void) rejectedWithComment:(NSString *)comment
{
    [self sendTripApprovalMsgWithAction:@"reject" withComment:comment];
}


- (void) rejectionCancelled
{
    // No operation
}


- (void)approveTrip
{
    [self sendTripApprovalMsgWithAction:@"approve" withComment:nil];
}


- (void)sendTripApprovalMsgWithAction:(NSString *)action withComment:(NSString *)comment
{
    NSString *waitDisplayText = [action isEqualToString:@"approve"] ? @"Approving Trip" : @"Rejecting Trip";
    [self showWaitViewWithText:[waitDisplayText localize]];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING", action, @"ACTION", self.tripToApprove, @"TRIP_TO_APPROVE", nil];
    if (comment)
        pBag[@"COMMENT"] = comment;
	
    [[ExSystem sharedInstance].msgControl createMsg:APPROVE_TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


-(void)rejectTripApprovalWithComment:(id)sender
{
    TripRejectCommentVC *vc = [[TripRejectCommentVC alloc] initWithNibName:@"TripRejectCommentVC" bundle:nil];
    vc.tripRejectDelegate = self;
    if ([UIDevice isPad]) {
        [self.navigationController pushViewController:vc animated:YES];
    }
    else {
        UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:vc];
        navi.modalPresentationStyle = UIModalPresentationFormSheet;
        [self presentViewController:navi animated:YES completion:nil];
    }
}


-(void)btnApproveTripPressed:(id)sender
{
    MobileAlertView *confirmationAlert = [[MobileAlertView alloc] initWithTitle:[@"Please Confirm" localize]
                                                                        message:[@"Are you sure you want to approve this trip?" localize]
                                                                       delegate:self
                                                              cancelButtonTitle:[LABEL_CANCEL_BTN localize]
                                                              otherButtonTitles:[LABEL_OK_BTN localize], nil];
    confirmationAlert.tag = kAlertViewConfirmTripApproval;
    [confirmationAlert show];
}

-(void) refreshData
{
//	[fetchView setHidden:NO];
//	[self.view bringSubviewToFront:fetchView];
//	[spinnerFetch startAnimating];
    
    [self showLoadingViewWithText:[Localizer getLocalizedText:@"Refreshing Data"]];

    // MOB-17892 Reset right action button, to prevent 'Sheet can not be presented because the view is not in a window' error
    // I would like to know exactly why we get the error at all. It seems to be triggered in MobileActionSheet.m when
    // the showFromToolbar method calls [super showFromToolbar:view]. Seems to be something wrong with the view
    // object, but I cannot spot anything different. Stackoverflow responses suggest that we shouldn't be
    // sub-classing UIActionSheet because it is not designed for it, but that's likely an issue for another day.
    // https://developer.apple.com/library/ios/documentation/UIKit/Reference/UIActionSheet_Class/Reference/Reference.html
    // see Subclassing Notes
    self.navigationItem.rightBarButtonItem = nil;
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING", trip.itinLocator, @"ITIN_LOCATOR", trip.tripKey, @"TRIP_KEY", nil];
	[[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) getAgencyInfoForCall
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING", trip.itinLocator, @"ITIN_LOCATOR", nil];
	[[ExSystem sharedInstance].msgControl createMsg:TRAVEL_AGENCY_ASSISTANCE_INFO CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    [self showWaitViewWithText:[@"Loading Data" localize]];
}


-(void) loadApprovalTrip
{   
    [self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Data"]];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING", @"YES", @"ForApprover", self.tripToApprove.itinLocator, @"ITIN_LOCATOR", self.tripToApprove.travelerCompanyId, @"COMPANY_ID",tripToApprove.travelerUserId, @"USER_ID", nil];
	[[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


- (void)didReceiveMemoryWarning 
{
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload 
{
    [self setLabelRecordLocator:nil];
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
    [self stopListeningToCurrentLocationUpdates];
}


- (void)dealloc 
{
    [self stopListeningToCurrentLocationUpdates];
	
}



-(NSString *)getGateTerminal:(NSString *)gate Terminal:(NSString *)terminal
{
	NSString *location = @"";
	
	if (terminal == nil)
	{
		location = [NSString stringWithFormat:@"%@ - %@ -", [Localizer getLocalizedText:@"SLV_TERMINAL"]
					, [Localizer getLocalizedText:@"SLV_GATE"]];
	}
	else 
	{
		if (gate == nil)
			gate = @"-";
		location = [NSMutableString stringWithFormat:@"%@ %@ %@ %@", [Localizer getLocalizedText:@"SLV_TERMINAL"], terminal, 
					[Localizer getLocalizedText:@"SLV_GATE"], gate];	
	}
	
	return location;
}


#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [tripBits count];
    
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if ([tripBits count] == 0)
        return 0;
    
    NSString *key = keys[section];
    NSArray *nameSection = nil;
    
    if (isOffersHidden) 
        nameSection = filteredSegments[key];
    else
        nameSection = (NSArray*)tripBits[key];
         
    return [nameSection count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger section = [indexPath section];
    
    NSString *key = keys[section]; 
    NSArray *segments = nil;
    
    if (isOffersHidden) {
        segments = filteredSegments[key];
    }
    else {
        segments = tripBits[key];
    }
    
   
    NSObject *segobj =  segments[indexPath.row];
    UITableViewCell *tvCell = nil;

    if([segobj isKindOfClass:[EntityOffer class]] ) // if object is offer handle it
    {
        OfferCell *offerCell = nil;
        offerCell = (OfferCell *)[tableList dequeueReusableCellWithIdentifier: @"OfferCell"];
        if (offerCell == nil)  
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"OfferCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[OfferCell class]])
                    offerCell = (OfferCell *)oneObject;
        }
        
        [offerCell setAccessoryType:UITableViewCellAccessoryNone];

        [self configureOfferCell:offerCell offer:(EntityOffer*)segobj ];
        
        tvCell = (UITableViewCell*)offerCell;
    }
    else if([segobj isKindOfClass:[EntitySegment class]])
    {
        EntitySegment *segment = segments[indexPath.row];
        // MOB-11059 Reload segment to avoid crash
        segment = [[TripManager sharedInstance] fetchSegmentByIdKey:segment.idKey tripKey:self.tripKey];

        TripSegmentCell *cell = nil;
        cell = (TripSegmentCell *)[tableList dequeueReusableCellWithIdentifier: @"TripSegmentCell"];
        
        if (cell == nil)  
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TripSegmentCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[TripSegmentCell class]])
                    cell = (TripSegmentCell *)oneObject;
        }
        
        // reset the font and number of lines, just incase this cell has been configured for a hotel cell
        cell.lblTime.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:21.0f];
        cell.lblTime.numberOfLines = 1;

        // reset the framesize to default
        CGRect rect = cell.lblTime.frame;
        cell.lblTime.frame = CGRectMake(20, rect.origin.y, 65, 22);
        [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];

        if([segment.type isEqualToString:SEG_TYPE_AIR])
            [self configureCellAir:cell segment:segment];
        else if([segment.type isEqualToString:SEG_TYPE_CAR])
            [self configureCellCar:cell segment:segment];
        else if([segment.type isEqualToString:SEG_TYPE_HOTEL])
            [self configureCellHotel:cell segment:segment];
        else if([segment.type isEqualToString:SEG_TYPE_RIDE])
            [self configureCellRide:cell segment:segment];
        else if([segment.type isEqualToString:SEG_TYPE_RAIL])
            [self configureCellRail:cell segment:segment];
        else if([segment.type isEqualToString:SEG_TYPE_PARKING])
            [self configureCellParking:cell segment:segment];
        tvCell = (UITableViewCell*)cell;
    }
    else if ([segobj isKindOfClass:[NSString class]])
    {
        tvCell = [self makeDrillCell:tableView withText:[(NSString*)segobj localize] withImage:@"icon_summary_button" enabled:YES];
    }
    
    return tvCell;
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
    [cell setAccessoryType:UITableViewCellAccessoryNone];

    return cell;
}

-(void) configureOfferCell:(OfferCell *)cell offer:(EntityOffer*)offer
{
    [cell configureLabelFontForLabel:cell.lblTitle WithText:offer.title];
    [cell.activity startAnimating];
    [cell.contentView bringSubviewToFront:cell.activity];
   
    [[OfferManager sharedInstance] processImageDataWithBlock:^(NSData *imageData) {
		if (self.view.window) {
			UIImage *image = [UIImage imageWithData:imageData];
			cell.ivIcon.image = image;
			[cell.activity stopAnimating];
		}
	} offer:offer];
}

-(void) configureCellParking:(TripSegmentCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_parking"];
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblHeading.text = vendorName;
    
    NSString *departTime = @"";
    departTime = [DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal];
    
    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        cell.lblTime.text = aTime[0];
        cell.lblAmPm.text = aTime[1];
    }
    else
    {
        cell.lblTime.text = departTime;
        cell.lblAmPm.text = @"";
    }
    
    if(segment.relStartLocation.address != nil)
        cell.lblSub1.text = segment.relStartLocation.address;
    else
        cell.lblSub1.text = @"";
    
    cell.lblSub2.text = [SegmentData getCityStateZip:segment.relStartLocation];
}

-(void)configureCellHotel:(TripSegmentCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_lodging"];
    
    NSString *vendorName;
    if (segment.segmentName != nil)
        vendorName = segment.segmentName;
    else if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblHeading.text = vendorName;
    
    cell.lblAmPm.text = @"";//[Localizer getLocalizedText:@"In"];
    cell.lblTime.text = [Localizer getLocalizedText:@"Check  In"];
    CGRect rect = cell.lblTime.frame;
    cell.lblTime.frame = CGRectMake(20, rect.origin.y, 65, 40);
    cell.lblTime.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:15.0f];
    cell.lblTime.numberOfLines = 2;
    
    if(segment.relStartLocation.address != nil)
        cell.lblSub1.text = segment.relStartLocation.address;
    else
        cell.lblSub1.text = @"";
    
    cell.lblSub2.text = [SegmentData getCityStateZip:segment.relStartLocation];
}


-(void) configureCellRide:(TripSegmentCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_taxi"];
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblHeading.text = vendorName;
    
    NSString *departTime = @"";
    departTime = [DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal];
    
    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        cell.lblTime.text = aTime[0];
        cell.lblAmPm.text = aTime[1];
    }
    else
    {
        cell.lblTime.text = departTime;
        cell.lblAmPm.text = @"";
    }
    
    if(segment.relStartLocation.address != nil)
        cell.lblSub1.text = segment.relStartLocation.address;
    else
        cell.lblSub1.text = @"";
    
    cell.lblSub2.text = [SegmentData getCityStateZip:segment.relStartLocation];
}


-(void) configureCellCar:(TripSegmentCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_car"];
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblHeading.text = vendorName;
    
    NSMutableString *departTime = [NSMutableString string];
    NSMutableString *departDate = [NSMutableString string];
    [SegmentData getDepartTimeString:segment timeStr:departTime dateStr:departDate];
    
    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        cell.lblTime.text = aTime[0];
        cell.lblAmPm.text = aTime[1];
    }
    else
    {
        cell.lblTime.text = departTime;
        cell.lblAmPm.text = @"";
    }
    cell.lblSub1.text = [SegmentData getAirportCity:segment.relStartLocation];

    NSString *location = [SegmentData getCityState:segment.relStartLocation];
    if([location length] == 0 && [segment.classOfCarLocalized length])
        location = segment.classOfCarLocalized;
    cell.lblSub2.text = location;
}

-(void) configureCellRail:(TripSegmentCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_rail"];
    
    NSString *railStation = [SegmentData getRailStation:segment.relStartLocation];
    
    NSString *endRailStation = [SegmentData getRailStation:segment.relEndLocation];

    cell.lblHeading.text = [NSString stringWithFormat:@"%@ - %@", railStation, endRailStation];
    
    NSMutableString *departTime = [NSMutableString string];
    [SegmentData getDepartTimeString:segment timeStr:departTime dateStr:nil];

    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        cell.lblTime.text = aTime[0];
        cell.lblAmPm.text = aTime[1];
    }
    else
    {
        cell.lblTime.text = departTime;
        cell.lblAmPm.text = @"";
    }
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    if( segment.trainNumber == nil)
         segment.trainNumber = @"--";
    
    cell.lblSub1.text = [NSString stringWithFormat:@"%@ %@", vendorName, segment.trainNumber]; 

    NSString *platform = segment.relStartLocation.platform;
    NSString *wagon = segment.wagonNumber;
    if(platform == nil)
        platform = @"--";
    
    if(wagon == nil)
        wagon = @"--";
    
    cell.lblSub2.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"SLV_PLATFORM_WAGON"], platform, wagon];

}

-(void) configureCellAir:(TripSegmentCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_flight"];
    cell.lblHeading.text = [NSString stringWithFormat:@"%@ %@ %@", segment.relStartLocation.airportCity, [Localizer getLocalizedText:@"SLV_TO"], segment.relEndLocation.airportCity];;
    
    NSMutableString *departTime = [NSMutableString string];
    [SegmentData getDepartTimeString:segment timeStr:departTime dateStr:nil];

    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        cell.lblTime.text = aTime[0];
        cell.lblAmPm.text = aTime[1];
    }
    else
    {
        cell.lblTime.text = departTime;
        cell.lblAmPm.text = @"";
    }
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblSub1.text = [NSString stringWithFormat:@"%@ %@", vendorName ?: @"", segment.flightNumber ?: @""];
    
    NSMutableString *term = [NSMutableString string];
    NSMutableString *gate = [NSMutableString string];
    
    [SegmentData getDepartTermGate:segment terminal:term gate:gate];
    
    cell.lblSub2.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal t Gate t"], term, gate];
}


-(void) deselect: (id) sender
{
	[self.tableList deselectRowAtIndexPath:[self.tableList indexPathForSelectedRow] animated:YES];
}


- (void)switchToSegmentView:(NSString *)idKey SegmentType:(NSString *)segmentType TripKey:(NSString *)ourTripKey Segment:(EntitySegment *)segment
{
	if (segment != nil) 
	{
		//NSLog(@"segment idKey:%@, type:%@, ourTripKey:%@", segment.idKey, segment.type, ourTripKey);
		NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.trip, @"TRIP", segment.idKey, @"SegmentKey", ourTripKey, @"TripKey", segment.type, @"SegmentType", segment, @"Segment", @"YES", @"SKIP_PARSE", nil];
		
		if ([segmentType isEqualToString:@"AIR"])
		{
			[ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:dict];
		}
		else if ([segmentType isEqualToString:@"HOTEL"])
		{
			[ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:dict];
		}
		else if ([segmentType isEqualToString:@"CAR"])
		{
			[ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:dict];
		}
		else if ([segmentType isEqualToString:@"PARKING"])
		{
			[ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:dict];
		}
		else if ([segmentType isEqualToString:@"RIDE"])
		{
			[ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:dict];
		}
		else if ([segmentType isEqualToString:@"DINING"] || [segmentType isEqualToString:@"RAIL"] || [segmentType isEqualToString:@"EVENT"])
		{
			[ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:dict];
		}
	}
}


#pragma mark -
#pragma mark Table View Delegate Methods
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    NSUInteger section = [newIndexPath section];
    
    NSString *key = keys[section];
    NSArray *segments = nil;
    
    if (isOffersHidden) {
        segments = filteredSegments[key];
    }
    else {
        segments = tripBits[key];
    }
    
    EntitySegment *segment = segments[newIndexPath.row];
    // MOB-11059 prevent crash
    if (![segment isKindOfClass:[EntityOffer class]] && ![segment isKindOfClass:[NSString class]])
    {
        segment = [[TripManager sharedInstance] fetchSegmentByIdKey:segment.idKey tripKey:self.tripKey];
    }
    
    if (segment == nil)
        return;
    
    // Handle offers action
    if ([segment isKindOfClass:[EntityOffer class]])
    {
       
        EntityOffer *offer = (EntityOffer*)segment;
        NSString *offerAction = offer.offerAction;
    
    
        if([offerAction isEqualToString:@"MULTI_LINK"])
        {
        //TODO : Check validity , find out what is multi_link
            // And fix all the below code
//        EntityOffer *offer = (EntityOffer*)segment;
//        if ([offer.links count] > 0) 
//        {
//            OfferMultiLinkVC *multiLinkVC = [[OfferMultiLinkVC alloc] initWithNibName:@"OfferMultiLinkVC" bundle:nil];
//            multiLinkVC.title = offer.offerVendor;
//            multiLinkVC.links = offer.links;
//            multiLinkVC.navigationController.title = offer.offerVendor;
//            
//            OfferCell *cell = (OfferCell*)[tableView cellForRowAtIndexPath:newIndexPath];
//            multiLinkVC.icon = cell.ivIcon.image;
//            
//            [self.navigationController pushViewController:multiLinkVC animated:YES];
//        }
//        else
//        {
//            [tableView deselectRowAtIndexPath:newIndexPath animated:YES];
//        }
        }
   
        else if([offerAction isEqualToString:@"WEB_LINK"] || [offerAction isEqualToString:@"SITE_LINK"])
        {
            NSString *url = offer.actionURL;

            // If actionURL begins with a forward slash, then it is a partial url and needs to be appended to a base url.
            if (0 == [offer.actionURL rangeOfString:@"/"].location)
                url = [NSString stringWithFormat:@"%@%@", [ExSystem sharedInstance].entitySettings.uri, offer.actionURL];
        
            if ([offerAction isEqualToString:@"SITE_LINK"])
            {
                
                    url = [NSString stringWithFormat:@"%@%@", [ExSystem sharedInstance].entitySettings.uri, offer.actionURL];
                    BOOL hasQueryString = (NSNotFound != [offer.actionURL rangeOfString:@"?"].location);
                    NSString *separator = (hasQueryString ? @"&" : @"?");
                    NSString *sessionType = @"concurMobile"; // Do NOT localize. MOB-10209 explains session type.
                    url = [NSString stringWithFormat:@"%@%@sessionId=%@&sessionType=%@", url, separator, [ExSystem sharedInstance].sessionID, sessionType];
            }
            //MOB-9095 : All SITE_LINK and WEB_LINK offers are opened using external browser.
            [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
            
        }
        else if([offerAction isEqualToString:@"NULL_LINK"])
        {
            [AppsUtil launchMapsWithOffer:offer];
        }
        else if([offerAction isEqualToString:@"APP_LINK"])
        {
            if([offer.offerVendor isEqualToString:@"Taxi Magic"])
                [AppsUtil launchTaxiMagicApp];
        }
    }
    else if([segment isKindOfClass:[EntitySegment class]])
    {
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.trip, @"TRIP", segment.idKey, @"SegmentKey", self.tripKey, @"TripKey", segment.type, @"SegmentType", segment, @"Segment", @"YES", @"SKIP_PARSE", @"Something", @"TO_VIEW", nil];
        if (self.isApproval) {
            [self navigateToItinDetails:pBag];
        }
        else {
            [ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:pBag];
        }
        
        [tableView deselectRowAtIndexPath:newIndexPath animated:YES];
        
        self.wentSomewhere = YES;
    }
    else if ([segment isKindOfClass:[NSString class]])
    {
        TripViolationSummaryVC *violationSummaryVC = [[TripViolationSummaryVC alloc] initWithNibName:@"TripViolationSummaryVC" bundle:nil];
        violationSummaryVC.lblAmountText = self.lblAmount.text;
        violationSummaryVC.lblBottomText = self.lblBottom.text;
        violationSummaryVC.lblDateText = self.lblDate.text;
        violationSummaryVC.lblNameText = self.lblName.text;
        violationSummaryVC.trip = self.trip;
        [self.navigationController pushViewController:violationSummaryVC animated:YES];
        [self deselect:nil];
    }
}

-(void)navigateToItinDetails:(NSMutableDictionary *)pBag
{
    //[pBag setObject:@"YES" forKey:@"ForTripApproval"];
    ItinDetailsViewController *vc = [[ItinDetailsViewController alloc] initWithNibName:@"ItinDetailsViewController" bundle:nil];
    vc.isTripApproval = YES;
    [self.navigationController pushViewController:vc animated:YES];
    Msg *msg = [[Msg alloc] init];
    msg.parameterBag = pBag;
    msg.idKey = TRIPS_DATA;
    [vc respondToFoundData:msg];
}


- (NSIndexPath *)tableView:(UITableView *)tableView willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView reloadData];
    return indexPath;
}


-(NSString *) tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    if ([keys[section] isEqualToString:SUMMARY_HEADER])
        return nil;
    return keys[section];
}

- (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title
               atIndex:(NSInteger)index
{
    NSString *key = keys[index];
    if (key == UITableViewIndexSearch)
    {
        [tableView setContentOffset:CGPointZero animated:NO];
        return NSNotFound;
    }
    else return index;
    
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger section = [indexPath section];
    
    NSString *key = keys[section];
    NSArray *segments = nil;
    
    if (isOffersHidden) {
        segments = filteredSegments[key];
    }
    else {
        segments = tripBits[key];
    }
    
    EntitySegment *segment = segments[indexPath.row];
    
    
    if([segment isKindOfClass:[EntityOffer class]])
        return 40;
    else if([segment isKindOfClass:[NSString class]])
        return 44;
    else
        return 62;
}


-(float) getHeightForSegment:(EntitySegment *) seg
{
	float lineH = 15.0; //yep, arbitrary
	float h = 76.0;
	
	if([seg.type isEqualToString:SEG_TYPE_PARKING])
	{
		if(seg.relStartLocation.address == nil)
			h = h - lineH;

		if(seg.relStartLocation.city == nil && seg.relStartLocation.state == nil && seg.relStartLocation.postalCode == nil && seg.relStartLocation.cityCode == nil)
			h = h - lineH;
		
		h = h - lineH;
		return h;
	}
	else if([seg.type isEqualToString:SEG_TYPE_HOTEL] || [seg.type isEqualToString:SEG_TYPE_CAR] || [seg.type isEqualToString:SEG_TYPE_DINING])
		return h - lineH;
	else 
		return 76.0;
}


#pragma mark -
#pragma mark UIActionSheetDelegate Methods

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    MobileActionSheet *mas = (MobileActionSheet *)actionSheet;
    //
	// First try to get booking location/dates from a flight segment
	//
	NSMutableDictionary* pBag = [TripData getHotelAndCarDefaultsFromFlightInTripSegments:tripBits withKeys:keys];
	
	if ([@"Book Hotel" isEqualToString:[mas getButtonId:buttonIndex]]) // If we're booking a hotel
	{
		if ([pBag count] == 0)	// If we still dont' have booking location/dates, try to get them from a rail segment
            pBag = [TripData getHotelAndCarDefaultsFromRailInTripSegments:tripBits withKeys:keys];

		if ([pBag count] == 0)	// If we still dont' have booking location/dates, try to get them from a car segment
			pBag = [TripData getHotelAndCarDefaultsFromCarInTripSegments:tripBits withKeys:keys];
		
		if ([pBag count] == 0)	// If we still dont' have booking location/dates, try to get them from a hotel segment
			pBag = [TripData getHotelAndCarDefaultsFromHotelInTripSegments:tripBits withKeys:keys];
	} 
	else if ([@"Book Car" isEqualToString:[mas getButtonId:buttonIndex]]) // If we're booking a car
	{
		if ([pBag count] == 0)	// If we still dont' have booking location/dates, try to get them from a rail segment
			pBag = [TripData getHotelAndCarDefaultsFromRailInTripSegments:tripBits withKeys:keys];

		if ([pBag count] == 0)	// If we still dont' have booking location/dates, try to get them from a hotel segment
			pBag = [TripData getHotelAndCarDefaultsFromHotelInTripSegments:tripBits withKeys:keys];
		
		if ([pBag count] == 0)	// If we still dont' have booking location/dates, try to get them from a car segment
			pBag = [TripData getHotelAndCarDefaultsFromCarInTripSegments:tripBits withKeys:keys];
	}
    //MOB-10902 and MOB-10903
    //No matter what user select, actionsheet only show up once, and that's after booking air.
	self.showBookingOption = NO;
    
	pBag[@"SHORT_CIRCUIT"] = @"YES";

	// Add the trip key, client locator, and record locator
	pBag[@"TRIP_KEY"] = (trip.cliqbookTripId == nil ? @"" : trip.cliqbookTripId);
	pBag[@"CLIENT_LOCATOR"] = (trip.clientLocator == nil ? @"" : trip.clientLocator);
	
	NSString *recordLocator = @"";
	EntityBooking* primaryBooking = [TripData getPrimaryBooking:trip];
	if (primaryBooking != nil && primaryBooking.recordLocator != nil)
		recordLocator = primaryBooking.recordLocator;
	pBag[@"RECORD_LOCATOR"] = recordLocator;
	
    MobileViewController *mvc = nil;

	if ([@"Book Hotel" isEqualToString:[mas getButtonId:buttonIndex]])
    {
        if ([Config isNewHotelBooking] && ![Config isGov]) {
            [HotelSearchTableViewController showHotelSearchScreen:self.navigationController withSearchCriteria:[HotelSearchCriteriaV2 initializeDefaultsFromTripDictionary:pBag]];
            return;
        }
        mvc = [[HotelViewController alloc] initWithNibName:@"HotelViewController" bundle:nil];
        [(HotelViewController *)mvc setHideCustomFields:YES];
        if ([Config isGov])
        {
            NSArray* taFields = [GovTAField makeEmptyTAFields];
            GovTAField* authFld = [GovTAField getAuthField:taFields];
            authFld.useExisting = YES;
            authFld.fieldValue = [@"Use Existing Authorization" localize];
            authFld.access = @"RO";
            // Need to pass pBag all the way to HotelVC for loc/date defaults and tripKey
            GovTAField* perdiemFld = [GovTAField getPerDiemField:taFields];
            perdiemFld.tripDefaults = pBag;
            // Pop up Per diem selecting
            [GovDutyLocationVC showDutyLocationVC:self withCompletion:@"Book Hotel" withFields:taFields withDelegate:nil withPerDiemRate:NO asRoot:NO];
            return;
        }
    }
	else if ([@"Book Car" isEqualToString:[mas getButtonId:buttonIndex]])
    {
        mvc = [[CarViewController alloc] initWithNibName:@"CarViewController" bundle:nil];
        [(CarViewController *)mvc setHideCustomFields:YES];
        if ([Config isGov])
        {
            NSArray* taFields = [GovTAField makeEmptyTAFields];
            GovTAField* authFld = [GovTAField getAuthField:taFields];
            authFld.useExisting = YES;
            authFld.fieldValue = [@"Use Existing Authorization" localize];
            authFld.access = @"RO";
            GovTAField* perdiemFld = [GovTAField getPerDiemField:taFields];
            perdiemFld.useExisting = YES;
            perdiemFld.fieldValue = [@"Use Existing Duty Location" localize];
            ((CarViewController *)mvc).taFields = [NSMutableArray arrayWithArray:taFields];
        }
    }
    else if ([@"View Authorization" isEqualToString:[mas getButtonId:buttonIndex]])
    {
        if ([Config isGov])
            [self getNewAuthDocInfo];
    }
    
    if (mvc != nil) 
    {
        Msg *msg = [[Msg alloc] init];
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        [mvc respondToFoundData:msg];
        [[self navigationController] pushViewController:mvc animated:YES];
    }
    
}

#pragma -mark Government Auth functions
-(void) getNewAuthDocInfo
{
    [self showWaitView];
    
    // Get doc info from TANum/Locator
    NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:trip.cliqbookTripId, @"TRIP_LOCATOR", trip.authNum, @"TA_NUM", nil];
    [[ExSystem sharedInstance].msgControl createMsg:GOV_DOC_INFO_FROM_TRIP_LOCATOR CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(BOOL) showAuthView:(GovDocInfoFromTripLocatorData*) currentDocInfo
{
    if (currentDocInfo != nil)
    {
        NSString * docType = currentDocInfo.currentDocType;
        NSString * docName = currentDocInfo.currentDocName;
        if (docType != nil && docName != nil)
        {
            [GovDocDetailVC pushAuthWithDocName:docName withDocType:docType];
            return YES;
        }
        else
        {
            // Add alert when showing auth failed
            // MOB-18894 Change alert message.
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Unable To Access"] message:[Localizer getLocalizedText:@"To view a stamped document use the Authorizations tab on the Home screen"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
            [av show];
        }
    }
    else
    {
        // Add alert when showing auth failed
        // MOB-18894 Change alert message.
        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Unable To Access"] message:[Localizer getLocalizedText:@"To view a stamped document use the Authorizations tab on the Home screen"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
        [av show];
    }
    return NO;
}

#pragma mark -
#pragma mark Button handlers

-(IBAction)buttonActionPressed:(id)sender
{
	MobileActionSheet * filterAction = nil;
	NSMutableArray *buttonIds = [[NSMutableArray alloc] init];

    if ([[ExSystem sharedInstance] hasTravelBooking] && [trip.allowAddHotel boolValue])
    {
        // do not localize button ids. these strings are localized for display below when adding button title
        // localizing here will cause the button to fail in other languages because this id is string matched vs the english version to launch button action
        [buttonIds addObject:@"Book Hotel"];
    }
    if ([[ExSystem sharedInstance] hasTravelBooking] && [trip.allowAddCar boolValue])
    {
        // do not localize button ids. these strings are localized for display below when adding button title
        // localizing here will cause the button to fail in other languages because this id is string matched vs the english version to launch button action
        [buttonIds addObject:@"Book Car"];
    }
	if ([Config isGov])
	{
		// do not localize button ids. these strings are localized for display below when adding button title
    	// localizing here will cause the button to fail in other languages because this id is string matched vs the english version to launch button action
    	[buttonIds addObject:@"View Authorization"];
	}

    if ([buttonIds count]) {
        filterAction = [[MobileActionSheet alloc] initWithTitle:nil
                                                       delegate:self
                                              cancelButtonTitle:nil
                                         destructiveButtonTitle:nil
                                              otherButtonTitles:nil];
        for (NSString *btnId in buttonIds) {
            [filterAction addButtonWithTitle:[Localizer getLocalizedText:btnId]];
        }
        filterAction.btnIds = [buttonIds copy];
        [filterAction addButtonWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
        filterAction.cancelButtonIndex = [buttonIds count];
    }
	
	filterAction.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
	
	[filterAction showFromToolbar:[ConcurMobileAppDelegate getBaseNavigationController].toolbar];
}


-(NSMutableString *) combineAddress:(NSString *)addr city:(NSString *)city state:(NSString *)st country:(NSString *)cntry zip:(NSString *)zip
{
	__autoreleasing NSMutableString *base = [[NSMutableString alloc] initWithString:@""];
	
	if(addr != nil)
		[base appendString:addr];
	
	if(city != nil)
	{
		if([base length] > 0)
			[base appendString:@" "];
		
		[base appendString:city];
	}
	
	if(st != nil)
	{
		if([base length] > 0)
			[base appendString:@", "];
		
		[base appendString:st];
	}
	
	if(zip != nil)
	{
		if([base length] > 0)
			[base appendString:@" "];
		
		[base appendString:zip];
	}
	
	if(cntry != nil)
	{
		if([base length] > 0)
			[base appendString:@", "];
		
		[base appendString:cntry];
	}
	
	return base;
}


-(void) makeRefreshButton:(NSString *)dtRefreshed
{
    if(![ExSystem connectedToNetwork])
        return;
    
	const int buttonWidth = 160;
	const int buttonHeight = 30;
	UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	lblText.numberOfLines = 2;
	lblText.lineBreakMode = NSLineBreakByWordWrapping;
	lblText.textAlignment = NSTextAlignmentRight;
	lblText.text = dtRefreshed;

    if( false == [ExSystem is7Plus] )
    {
        // iOS6 shows a black shadow behind white refresh text
        // iOS7 shows this as white text on white background so leave colors default
        [lblText setBackgroundColor:[UIColor clearColor]];
        [lblText setTextColor:[UIColor whiteColor]];
        [lblText setShadowColor:[UIColor blackColor]];
        [lblText setShadowOffset:CGSizeMake(0, -1)];
	}
    [lblText setFont:[UIFont boldSystemFontOfSize:12.0f]];
	
    [cv addSubview:lblText];
	
	[lblFetch setText:[Localizer getLocalizedText:@"Fetching Data"]];

	UIBarButtonItem *btnRefresh = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRefresh target:self action:@selector(refreshData)];
	UIBarButtonItem *btnRefreshDate = [[UIBarButtonItem alloc] initWithCustomView:cv];
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
    UIBarButtonItem *btnShowHideOffers = nil;
#pragma warn support offers
    if (self.hasValidOffers) {
        NSString *titleOffers = (isOffersHidden)?[Localizer getLocalizedText:@"Show Offers"]:[Localizer getLocalizedText:@"Hide Offers"];
        btnShowHideOffers = [self getOffersBarButton:titleOffers];
    }
    
	NSMutableArray *toolbarItems = nil;
    if (btnShowHideOffers != nil) 
    {
        toolbarItems = [NSMutableArray arrayWithObjects:btnShowHideOffers, flexibleSpace, btnRefreshDate, btnRefresh, nil];
    }
    else
    {
        toolbarItems = [NSMutableArray arrayWithObjects:flexibleSpace, btnRefreshDate, btnRefresh, nil];
    }
    if ([self canCallTravelAgent]) {
        UIBarButtonItem *callAgentButton = [[UIBarButtonItem alloc]initWithTitle:[@"Call Travel Agent" localize] style:UIBarButtonItemStyleBordered target:self action:@selector(getAgencyInfoForCall)];
        [toolbarItems insertObject:callAgentButton atIndex:0];
    }
    
	[self setToolbarItems:toolbarItems animated:NO];

}

-(BOOL)canCallTravelAgent
{
    return [[ExSystem sharedInstance] hasRole:ROLE_CALL_AGENT_ENABLED];// YES;
}

-(BOOL)hasValidOffers
{
    if ([self canCallTravelAgent])
        return NO;
    return hasValidOffers;
}

#pragma mark - Alert Delegate
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
    if (alertView.tag == kAlertViewConfirmTripApproval)
    {
        if (alertView.cancelButtonIndex != buttonIndex) {
            [self approveTrip];
        }
    }
    else if (alertView.tag == kAlertViewConfirmAgentCall)
    {
        if (alertView.cancelButtonIndex != buttonIndex) {
            [self callAgency];
        }
    }
}

#pragma mark Current Location tracking Methods
-(void) startListeningToCurrentLocationUpdates
{
	NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
	[defaultCenter addObserver:self selector:@selector(receivedCurrentLocationUpdate:) name:CURRENT_LOCATION_UPDATE object:nil];
}

-(void) stopListeningToCurrentLocationUpdates
{
	NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
	[defaultCenter removeObserver:self name:CURRENT_LOCATION_UPDATE object:nil];
}

-(void) receivedCurrentLocationUpdate:(NSNotification*)notification
{
    if (trip != nil) 
    {
        self.tripBits = [TripData makeSegmentDictGroupedByDate:trip];
        [tableList reloadData];
    }
}
@end
