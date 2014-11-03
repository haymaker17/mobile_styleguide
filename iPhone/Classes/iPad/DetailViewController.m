//
//  DetailViewController.m
//  PlayThing
//
//  Created by Paul Kramer on 4/30/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//  Updated by Pavan: 11/02/2012
//      Added/updated new code for enabling offers with coredata
//      Cleanup dead code.
//
//

#import "FeedbackManager.h"
#import "HotelCancel.h"
#import "DetailViewController.h"
#import "ExSystem.h" 

//#import "LoginViewController.h"
#import "SettingsViewController.h"
#import "TripsCell.h"
#import "ViewConstants.h"
#import "TripDetailCell.h"
#import "TripSegments.h"
#import "HotelImageData.h"
#import "HotelImagesData.h"
#import "ImageUtil.h"
#import "ConcurMobileAppDelegate.h"
#import "ButtonSegment.h"
//#import "ItinDetailsHotelCellPad.h"
#import "FormatUtils.h"
#import "ItinDetailsParkingCell.h"
#import "SegmentStuff.h"
#import "TripAirSegmentCell.h"
#import "iPadImageViewerVC.h"
#import "ItinDetailsCellLabelPad.h"
#import "TripAirSegmentCellPad.h"
//#import "ItinDetailsCarCellPad.h"
#import "Location.h"
#import "HotelViewController.h"
#import "CarViewController.h"
#import "MobileAlertView.h"
#import "FlightStatsViewController.h"
#import "CarCancel.h"
#import "AppsUtil.h"
#import "OfferCell.h"
#import "OfferMultiLinkVC.h"
#import "DateTimeConverter.h"
#import "EntityTrip.h"
#import "ViolationDetailsVC.h"
#import "FlightScheduleVC.h"
#import "AmtrakCancel.h"
#import "KeyboardNavigationController.h"
#import "TripPaneHeader_iPad.h"
#import "TripPaneFooter_iPad.h"
#import "MixedFontLabeler.h"

// chatter
#import "Config.h"
#import "ChatterPostLookup.h"
#import "ChatterFeedViewController.h"
#import "ChatterTripPostViewController.h"

#import "GovTAField.h"
#import "GovDutyLocationVC.h"
#import "GovDocDetailVC.h"
#import "GovDocDetailVC_iPad.h"
#import "GovDocInfoFromTripLocatorData.h"

@interface DetailViewController (Private)
- (void)addOfferEntriesInternal:(NSString *)segKey ma:(NSMutableArray *)ma seg:(EntitySegment *)seg ;
-(void)makeFilteredSegments;
-(void)checkOfferDisplaySelection;
-(void) startListeningToCurrentLocationUpdates;
-(void) stopListeningToCurrentLocationUpdates;
-(void) navigateToHome;
@end

@implementation DetailViewController

#define MAX_HEADER_ICONS 8

#define BUTTON_ID_BOOK_HOTEL    @"BUTTON_ID_BOOK_HOTEL"
#define BUTTON_ID_BOOK_CAR      @"BUTTON_ID_BOOK_CAR"
#define BUTTON_ID_REFRESH_TRIP  @"BUTTON_ID_REFRESH_TRIP"
#define BUTTON_ID_TOGGLE_OFFERS @"BUTTON_ID_TOGGLE_OFFERS"
// Looks like we programmatically build buttons in this view. I prefer interface builder, just cause it's safer with future iOS versions.
#define BUTTON_ID_CHATTER_TRIP_FEED @"BUTTON_ID_CHATTER_TRIP_FEED"
#define BUTTON_ID_CHATTER_POST @"BUTTON_ID_CHATTER_POST"


#define BUTTON_ID_VIEW_AUTH        @"BUTTON_ID_VIEW_AUTH"


#define kAlertConfirmCancelHotel 18541
#define kAlertConfirmCancelCar 18543
#define kAlertConfirmCancelAmtrak 18545
#define		kAlertViewRateApp	101781
#define kAlertViewHotelCancelSuccessMessage 12828

@synthesize detailItem, detailDescriptionLabel, hotelCancelIndexPath,isOffersHidden,filteredSegments, hasValidOffers, btnCancelSegKey;
//@synthesize allowAddBooking;
@synthesize tripsData, listKeys, tripTable, trip;
@synthesize tripKey, tripBits, keys, tableDetails, viewType, ivLogo;
@synthesize navigationBar, navTitle, holdView, holdText, holdActivity, ivLoading;
@synthesize		ivBarLeft, ivBarRight, ivBarMiddle, scroller, lblTripName, lblTripDates, pageControl;
@synthesize oopeListVC, tripsListVC, iPadHome, ivBack, ivBlotter, dictSegRows;
@synthesize hotelCell, hotelImagesArray, btnCar, btnHotel,btnOffers, btnRefresh;
@synthesize	lblTripDetails, lblBackBorder, lblPreviousTrip, lblNextTrip, btnNext, btnPrevious, dictHotelImageURLs, lblOffline;
@synthesize	ivTopTable, ivBottomTable, ivTripPanel, ivTripIcon, activeTrip, reportsListVC, scrollerButtons, nextTrip, previousTrip, dictHotelImages, headerImg, btnCancel;


#pragma mark -
#pragma mark Managing the detail item

/*
 When setting the detail item, update the view and dismiss the popover controller if it's showing.
 */
- (void)setDetailItem:(id)newDetailItem {
    if (detailItem != newDetailItem) {
        detailItem = newDetailItem;
        
        // Update the view.
        [self configureView];
    }

    if (popoverController != nil) {
        [popoverController dismissPopoverAnimated:YES];
    }  

}


- (void)configureView {
    // Update the user interface for the detail item.
    //detailDescriptionLabel.text = [detailItem description];   
}

#pragma mark -
#pragma mark Rotation support

// Ensure that the view controller supports rotation and that the split view can therefore show in both portrait and landscape.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

-(void) hideButtonsForRoles
{
	if ([[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER])
	{
		[btnCar setHidden:NO];
		[btnHotel setHidden:NO];
	}
	else 
	{
		[btnCar setHidden:YES];
		[btnHotel setHidden:YES];
	}
}

-(void) checkOffline
{
    if ([ExSystem connectedToNetwork]) {
        [self.lblOffline setHidden:YES];
    } else {
        [self.lblOffline setHidden:NO];
    }
}

#pragma mark -
#pragma mark Panel Buttons 
-(void) buttonBookHotelPressed:(id)sender
{
	[self checkOffline];
	[self buttonBook:0];
}

-(void) buttonBookCarPressed:(id)sender
{
	[self checkOffline];
	[self buttonBook:1];
}

-(void) buttonRefreshTripPressed:(id)sender
{
	[self checkOffline];
	
	if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Actions offline"] 
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return;
	}
	[self refreshData];
}

-(void) refreshData
{
	if(![ExSystem connectedToNetwork])
		return;
	
	[self.loadingView setHidden:NO];
	[self.view bringSubviewToFront:self.loadingView];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"DetailViewController", @"TO_VIEW", @"YES", @"REFRESHING", trip.itinLocator, @"ITIN_LOCATOR", trip.tripKey, @"TRIP_KEY", nil];
	[[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


#pragma mark -
#pragma mark View lifecycle
 // Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.rightTableView.dataSource = self;
    self.rightTableView.delegate = self;
    
	[self startListeningToCurrentLocationUpdates];
    
    if (![ExSystem sharedInstance].isGovernment)
    {
        [GlobalLocationManager startTrackingSignificantLocationUpdates];
    }
    
	if([ExSystem connectedToNetwork])
		[self.loadingView setHidden:NO];
	[self.view bringSubviewToFront:self.loadingView];
	dayWidth = 357.0;
	
	//[iPadHome adjustHomeButtons:self];
	
	self.dictHotelImages = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	self.dictHotelImageURLs = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	self.loadingLabel.text = [Localizer getLocalizedText:@"Loading Trip"];
	
	if(![ExSystem connectedToNetwork])
		[self.loadingView setHidden:YES];
    
    
    //hide empty rows for table
    //[self.tableDetails setTableFooterView:[UIView new]];
    
    [self checkOfferDisplaySelection];
    
    [self updateViews];
}


- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];

	if([ExSystem isLandscape])
		ivLogo.image = [UIImage imageNamed:@"iPad_Landscape_Fill.png"];
	else {
		ivLogo.image = [UIImage imageNamed:@"Default-Portrait.png"];
	}
	
	if(self.trip != nil)
		[ivLogo setHidden:YES];
	else 
		[ivLogo setHidden:NO];
	
	self.title = trip.tripName;
    
    //[AppRating offerChoiceToRateApp:self alertTag:kAlertViewRateApp];
    // DISABLE feedback manager for Gov
    if (![Config isGov])
    {
        [[FeedbackManager sharedInstance] requestRatingFromViewController:self withBlock:^{
            [self afterChoiceToRateApp];
        }];
    }
//#warning remove this debug code
//    NSString *recordLocator = @"";
//    BookingData* primaryBooking = [trip getPrimaryBooking];
//    if (primaryBooking != nil && primaryBooking.recordLocator != nil)
//        recordLocator = primaryBooking.recordLocator;
//    NSLog(@"recordLocator viewdidappear %@", recordLocator);
}

// resize the table?
//-(void)viewWillAppear:(BOOL)animated
//{
//    NSInteger numberOfCellsInSection = 0;
//    CGFloat totalRowHeight = 0;
//    //finding the number of cells in your table view by looping through its sections
//    for (NSInteger section = 0; section < [self numberOfSectionsInTableView:self.tableDetails]; section++)
//    {
//        numberOfCellsInSection = [self tableView:self.tableDetails numberOfRowsInSection:section];
//        for (NSInteger i = 0; i < numberOfCellsInSection; i++)
//        {
//            totalRowHeight += [self tableView:self.tableDetails heightForRowAtIndexPath:[NSIndexPath indexPathForRow:i inSection:section]];
//        }
//    }
//    
//    //CGFloat height = numberOfCells * self.tableDetails.rowHeight;
//    
//    CGRect tableFrame = self.tableDetails.frame;
//    tableFrame.size.height = totalRowHeight;
//    self.tableDetails.frame = tableFrame;
//    
//    [tableDetails reloadData];
//}

- (void)viewWillDisappear:(BOOL)animated {
    [pickerPopOver dismissPopoverAnimated:YES];

    [super viewWillDisappear:animated];
}

/*
- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
}
*/

- (void)viewDidUnload {
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    //self.popoverController = nil;

	self.navigationBar = nil;
	self.btnCar = nil;
	self.btnHotel = nil;
    self.btnOffers = nil;
	self.btnRefresh = nil;
	
	self.holdView = nil;
	self.holdText = nil;
	self.holdActivity = nil;
	self.ivLoading = nil;
    self.lblOffline = nil;
    [self stopListeningToCurrentLocationUpdates];
    [GlobalLocationManager stopTrackingSignificantLocationUpdates];
}

#pragma mark - BaseDetailVC_iPad Overrides
- (UIView*) loadHeaderView
{
    UIView *headerView = (UIView*)[[NSBundle mainBundle] loadNibNamed:@"TripPaneHeader_iPad" owner:self options:nil][0];
    return headerView;
}

- (UIView*) loadFooterView
{
    UIView *headerView = (UIView*)[[NSBundle mainBundle] loadNibNamed:@"TripPaneFooter_iPad" owner:self options:nil][0];
    return headerView;
}

#pragma mark -
#pragma mark Memory management


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
	NSLog(@"Low memory warning from Trips Detail on the iPad");
}


- (void)dealloc {
    [self stopListeningToCurrentLocationUpdates];
	
	
	

   // [toolbar release];
    
	
	
	
	
	
	
	
	
	//[pickerPopOver release];
	
	
	
	
	
	
}


#pragma mark -
#pragma mark System Buttons
-(void)makeSystemButtons
{

}


- (IBAction)buttonSettingsPressed:(id)sender
{
	SettingsViewController *svc = [[SettingsViewController alloc] init];
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
	svc.modalPresentationStyle = UIModalPresentationFormSheet;
#endif
	[self presentViewController:svc animated:YES completion:nil];

}


#pragma mark -
#pragma mark Show the Trip
-(void)displayTrip:(EntityTrip *)newTrip TripKey:(NSString *)newTripKey
{
	self.trip = newTrip; //[tripsData.trips objectForKey:key];
	self.activeTrip = newTrip;
    
    if (self.isViewLoaded)
        [self updateViews];

	if(!isLoadingHotelImage)
		self.loadingView.hidden = YES;
}

-(void) setUpPreviousNextTrips
{
	//MOB-3256
	[btnNext setHidden:YES];
	[btnPrevious setHidden:YES];
	
	if(tripsData == nil || activeTrip == nil || [tripsData.keys count] < 2)
		return;
	
	int currTripPos = -1;
	int iPos = 0;
	
	for (NSString *key in tripsData.keys)
	{
		if([key isEqualToString:activeTrip.tripKey])
		{
			currTripPos = iPos;
			break;
		}
		iPos++;
	}
	

	
	if (currTripPos != -1) {
		//we have a position!
		if (currTripPos > 0) {
			//we have a previous trip
			//self.previousTrip = [tripsData.trips objectForKey:[tripsData.keys objectAtIndex:(currTripPos - 1)]];
            
            //MOB-10675
            NSString *prevTripKey = (tripsData.keys)[(currTripPos -1)];
            if(prevTripKey != nil)
            {
                self.previousTrip = [[TripManager sharedInstance] fetchByTripKey:prevTripKey];
                [btnPrevious setHidden:NO];
            }
            
//			UISwipeGestureRecognizer *Recognizer = [[[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(Perform_Swiped_left:)] autorelease];
//			Recognizer.direction = UISwipeGestureRecognizerDirectionLeft;
//			[self.view addGestureRecognizer:Recognizer];
			
			//lblPreviousTrip.text = @"<< Previous Trip"; // [NSString stringWithFormat:@"<< %@", previousTrip.tripName];
			//[btnPrevious setHidden:NO];
		}
		
		if (currTripPos < ([tripsData.keys count] - 1)) {
			//we have a next trip
			//self.nextTrip = [tripsData.trips objectForKey:[tripsData.keys objectAtIndex:(currTripPos + 1)]];
            
            //MOB-10675
            NSString *nextTripKey = (tripsData.keys)[(currTripPos +1)];
            if (nextTripKey != nil)
            {
                self.nextTrip = [[TripManager sharedInstance] fetchByTripKey:nextTripKey];
                [btnNext setHidden:NO];
            }
//			UISwipeGestureRecognizer *Recognizer = [[[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(Perform_Swiped_right:)] autorelease];
//			Recognizer.direction = UISwipeGestureRecognizerDirectionRight;
//			[self.view addGestureRecognizer:Recognizer];
			
			//lblNextTrip.text = @"Next Trip >>"; // [NSString stringWithFormat:@"%@ >>", nextTrip.tripName];
			//[btnNext setHidden:NO];
		}
		

	}
	
}

-(void) loadTrip:(NSMutableDictionary *)pBag
{
    [self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Data"]];
    [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

- (void) Perform_Swiped_left:(UISwipeGestureRecognizer*)sender
{
	//NSLog(@"Swiped Left");
	if(previousTrip != nil)
	{		
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:previousTrip, @"TRIP", @"PREVIOUS", @"DIRECTION", nil];
		[iPadHome switchToDetail:@"Trip" ParameterBag:pBag];
	}
}

- (void) Perform_Swiped_right:(UISwipeGestureRecognizer*)sender
{
	//NSLog(@"Swiped Right");
	if(nextTrip != nil)
	{
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nextTrip, @"TRIP", @"NEXT", @"DIRECTION", nil];
		[iPadHome switchToDetail:@"Trip" ParameterBag:pBag];
	}
}


#pragma mark -
#pragma mark MVC Methods


//tripDetails
-(NSString *)getViewIDKey
{
	return TRIP_DETAILS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

-(void) navigateToHome
{
	// Get the home from app delegate. 
   	UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
    // Force home screen to refresh its trips data
    if ([homeVC respondsToSelector:@selector(refreshTripsData)])
    {
        [homeVC performSelector:@selector(refreshTripsData) withObject:nil];
    }

    // Show trips list from home since user navigated to trip detail from trips list. 
    [self.navigationController popToRootViewControllerAnimated:NO];
    if ([homeVC respondsToSelector:@selector(tripsButtonPressed)])
    {
        [homeVC performSelector:@selector(tripsButtonPressed) withObject:nil afterDelay:0.4f];
    }

}

-(void)respondToFoundData:(Msg *)msg
{//respond to data that might be coming from the cache
	
    if ([msg.idKey isEqualToString:AMTRAK_CANCEL])
    {
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
		
		AmtrakCancel *amtrakCancel = (AmtrakCancel *)msg.responder;
		if([amtrakCancel isKindOfClass:[AmtrakCancel class]] && amtrakCancel.isSuccess)
		{
            [iPadHome refreshTripData];
            [self.navigationController popToRootViewControllerAnimated:YES];
            [iPadHome buttonTripsPressed:iPadHome];
		}
        else
        {
            NSString *errorTitle = nil;
            NSString *errorMessage = [Localizer getLocalizedText:@"Cancel Failed"];
            if (amtrakCancel.errorMessage != nil && amtrakCancel.errorMessage.length > 0)
            {
                errorTitle = [Localizer getLocalizedText:@"Cancel Failed"];
                errorMessage = amtrakCancel.errorMessage;
            }
            
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:errorTitle message:errorMessage delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles: nil];
            [av show];
        }
    }
	else if ([msg.idKey isEqualToString:HOTEL_CANCEL])
	{
        [self hideWaitView];
		HotelCancel *hotelCancel = (HotelCancel *)msg.responder;
		if([hotelCancel isKindOfClass:[HotelCancel class]] && hotelCancel.isSuccess)
		{
            NSString *confirmationText = hotelCancel.cancellationNumber && [hotelCancel.cancellationNumber length] ?
            [NSString stringWithFormat:[@"Your hotel reservation has been successfully cancelled. Cancellation Number: %@" localize],hotelCancel.cancellationNumber] :
            [@"Concur was unable to obtain a cancellation number for your hotel cancellation. In order to obtain a cancellation number" localize];
            
            MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"Booking cancelled" localize]
                                                                    message:confirmationText
                                                                   delegate:self
                                                          cancelButtonTitle:[LABEL_CLOSE_BTN localize]
                                                          otherButtonTitles: nil];
            alert.tag = kAlertViewHotelCancelSuccessMessage;
            [alert show];
        }
        else if([hotelCancel isKindOfClass:[HotelCancel class]] && !hotelCancel.isSuccess)
        {
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:([hotelCancel.errorMessage length] ? [@"Cancel Failed" localize] : nil)
                                                                 message:([hotelCancel.errorMessage length] ? hotelCancel.errorMessage : [@"Cancel Failed" localize])
                                                                delegate:nil
                                                       cancelButtonTitle:[LABEL_OK_BTN localize]
                                                       otherButtonTitles: nil];
            [av show];
        }
	}
    else if ([msg.idKey isEqualToString:CAR_CANCEL])
	{
        [self hideWaitView];
		CarCancel *carCancel = (CarCancel *)msg.responder;
		if([carCancel isKindOfClass:[CarCancel class]] && carCancel.isSuccess)
		{
			NSMutableDictionary *segs = [TripData getSegmentsOrderByDate:trip];
            // MOB-11341 - log flurry event
            //Type:<Hotel, Car, Air, Train>, ItemsLeftInItin:<count>
            NSDictionary *dictionary = @{@"Type": @"Car", @"ItemsLeftInItin": [NSString stringWithFormat:@"%d", [segs count]]};
            [Flurry logEvent:@"Book: Cancel" withParameters:dictionary];

            // MOB-6881
            // Car data segment is not deleted yet. It will be deleted after call with "TRIPS_DATA"
            // So check for atleast 1 segment in case there is only car booking
			if([segs count] > 1)
			{
				[self refreshData];
                [[ConcurMobileAppDelegate findiPadHomeVC] refreshTripData];
			}
			else 
			{		
                [self navigateToHome];
			}
		}
        else if([carCancel isKindOfClass:[CarCancel class]] && !carCancel.isSuccess)
        {
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:([carCancel.errorMessage length] ? [@"Cancel Failed" localize] : nil)
                                                                 message:([carCancel.errorMessage length] ? carCancel.errorMessage : [@"Cancel Failed" localize])
                                                                delegate:nil
                                                       cancelButtonTitle:[LABEL_OK_BTN localize]
                                                       otherButtonTitles: nil];
            [av show];
        }
	}
    else if ([msg.idKey isEqualToString:AIR_CANCEL])
	{
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
		
		AirCancel *airCancel = (AirCancel *)msg.responder;
		if([airCancel isKindOfClass:[AirCancel class]] && airCancel.isSuccess)
		{
            [iPadHome refreshTripData];
            [self.navigationController popToRootViewControllerAnimated:YES];
            [iPadHome buttonTripsPressed:iPadHome];
		}
        else
        {
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Cancel Failed"] message:[Localizer getLocalizedText:@"Cancel Failed"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles: nil];
            [av show];
        }
	}
    else if ([msg.idKey isEqualToString:TRIPS_DATA] && msg.parameterBag[@"ITIN_LOCATOR"] && msg.parameterBag[@"LOADING_SINGLE_ITIN"])
    {
        if(self.isWaitViewShowing)
            [self hideWaitView];
        else if(self.isLoadingViewShowing)
            [self hideLoadingView];
        
        NSString *itinLocator = (msg.parameterBag)[@"ITIN_LOCATOR"];
        if ([self.tripDetailsRequestId isEqualToString:msg.parameterBag[@"TRIPDETAILSREQUEST_UUID"]])
        {
            EntityTrip *selectedTrip = [[TripManager sharedInstance] fetchByItinLocator:itinLocator];
            [self displayTrip:selectedTrip TripKey:selectedTrip.tripKey];
        }
    }
	else if ([msg.idKey isEqualToString:TRIPS_DATA] && (msg.parameterBag)[@"REFRESHING"] != nil)
	{
		self.tripsData = (TripsData *)msg.responder;
        
		NSString *key = (msg.parameterBag)[@"TRIP_KEY"];
        self.trip = [[TripManager sharedInstance] fetchByTripKey:key];
		self.tripKey = key;
		
		[self displayTrip:trip TripKey:key];

        UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
        
        if([homeVC respondsToSelector:@selector(refreshUIWithTripsData:)])
        {
            [homeVC  performSelector:@selector(refreshUIWithTripsData:) withObject:tripsData];
		}
	}
    else if ([msg.idKey isEqualToString:TRIPS_DATA] && (msg.parameterBag)[TO_VIEW] != nil)
	{//below is the pattern of getting the object you want and using it.
        if ((msg.parameterBag)[@"NAVIGATE_TO_HOME"])
        {
            [self hideWaitView];
            // Control would never reach here. Navigatetohome method should just show the app home
            UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
            if ([homeVC respondsToSelector:@selector(respondToFoundData)])
            {
                [homeVC performSelector:@selector(respondToFoundData) withObject:msg];
            }
            
            [self.navigationController popToRootViewControllerAnimated:NO]; // Don't cause table to be redrawn with YES
            if ([homeVC respondsToSelector:@selector(tripsButtonPressed)])
            {
                [homeVC performSelector:@selector(tripsButtonPressed) withObject:nil];
            }
            
        }
        
		NSString *toView = (msg.parameterBag)[TO_VIEW];
		if (![toView isEqualToString:TRIP_DETAILS])
		{
			return;
		}
		
		NSString *currTripKey = (msg.parameterBag)[TRIP_KEY];
		self.tripKey = currTripKey;
		self.listKeys = [[NSMutableArray alloc] initWithObjects:currTripKey,nil];//tripsData.keys;
		
		self.trip = (msg.parameterBag)[TRIP]; //[tripsData.trips objectForKey:key];
        
		[self formatSegmentCells];
        [self checkOfferDisplaySelection];
		[self makeFilteredSegments];
        
		[self.tripTable reloadData];
		
		self.title = trip.tripName;
	}
	else if ([msg.idKey isEqualToString:HOTEL_IMAGES])
	{
		HotelImagesData *hids = (HotelImagesData *)msg.responder; //should be an array of URLs
		
		NSString *section = (msg.parameterBag)[@"SECTION"]; //so we know what row and section to update, could be many
		NSString *row = (msg.parameterBag)[@"ROW"];

		if(section == nil || row == nil)
			return; //get out, must be getting an error from the server
		
		[self fillImageURLs:hids.keys Section:[section intValue] Row:[row intValue]];
		
		NSString *key = [NSString stringWithFormat:@"%@,%@,IMAGE0", section, row];
		if( hids != nil && dictHotelImages[key] != nil && [hids.keys count] > 0)
		{
			//we have a cell.iv that is linked to what was just returned.  Fetch that image and show it.  Directly.
			HotelImageData *hid = (hids.keys)[0];
			id path = hid.hotelImage; 
			NSURL *url = [NSURL URLWithString:path];
			NSData *data = [NSData dataWithContentsOfURL:url];
			UIImage *img = [[UIImage alloc] initWithData:data];
			UIImageView *iv = dictHotelImages[key];
			iv.image = img;
		}
		isLoadingHotelImage = NO;
		[self.loadingView setHidden:YES];

	}
//	else if ([msg.idKey isEqualToString:IMAGE] & msg.parameterBag != nil)
//	{
//		NSString *goesTo = [msg.parameterBag objectForKey:@"GOES_TO"];
//		if([msg.parameterBag objectForKey:@"IMAGE_VIEW"] != nil)
//		{
//			UIImage *img = [msg.parameterBag objectForKey:@"UIIMAGE"];
//			[img initWithData:msg.data];
//			UIImage *gotImg = [[UIImage alloc] initWithData:msg.data];
//			//img = gotImg;
//			UIImageView *iv = [msg.parameterBag objectForKey:@"IMAGE_VIEW"];
//			iv.image = gotImg;
//		}
//	}
	else if ([msg.idKey isEqualToString:IMAGE] && msg.parameterBag != nil)
	{
		NSString *goesTo = (msg.parameterBag)[@"GOES_TO"];
		if ([goesTo isEqualToString:@"INFO"])
		{
			//update the info box
			UIImage *gotImg = [[UIImage alloc] initWithData:msg.data];
			ItinDetailsCellInfo *cell = (ItinDetailsCellInfo *)msg.cell;
			[cell.imgVendor setImage:gotImg];
			[cell.imgVendor setHidden:NO];
		}
		else if((msg.parameterBag)[@"IMAGE_VIEW"] != nil)
		{
//			UIImage *img = [msg.parameterBag objectForKey:@"UIIMAGE"];
//			[img initWithData:msg.data];
			UIImage *gotImg = [[UIImage alloc] initWithData:msg.data];
			//img = gotImg;
			UIImageView *iv = (msg.parameterBag)[@"IMAGE_VIEW"];
			iv.image = gotImg;
		}
	}
	else if ([msg.idKey isEqualToString:VENDOR_IMAGE] && msg.parameterBag != nil)
	{//segment should already be set
		if ((msg.parameterBag)[VENDOR_IMAGE] != nil)
		{
			NSString *vType = (msg.parameterBag)[@"VendorType"];
			NSString *vCode = (msg.parameterBag)[@"VCode"];
			
			NSString *imageFileNameWithType = [NSString stringWithFormat:@"[%@]%@.gif",vType, vCode];
			NSString *pBagName = (msg.parameterBag)[VENDOR_IMAGE];
			if ([pBagName isEqualToString:imageFileNameWithType])
			{
				//update the vendor
				UIImage *gotImg = [[UIImage alloc] initWithData:msg.data];
				ItinDetailsCellLabel *cell = (ItinDetailsCellLabel *)msg.cell;
				[cell.imgView setImage:gotImg];
				[cell.imgView setHidden:NO];
			}
			//[vType release];
		}
		
	}
    else if ([msg.idKey isEqualToString:GOV_DOC_INFO_FROM_TRIP_LOCATOR])
    {
        if ([self isViewLoaded])
            [self hideWaitView];

		GovDocInfoFromTripLocatorData *docInfo = (GovDocInfoFromTripLocatorData *)msg.responder;
        
        [self showAuthView:docInfo];
    }

    [self updateViews];
}

- (void)formatSegmentCells 
{
	NSMutableDictionary *segDictByDate = [[NSMutableDictionary alloc] init];
	NSMutableArray *holdKeys = [[NSMutableArray alloc] init];
	
	for(EntityBooking * bd in trip.relBooking)
	{
		for(EntitySegment* seg in bd.relSegment)
		{
			if (seg.relStartLocation.dateLocal == nil)
			{
				seg.relStartLocation.dateLocal = @"1900-01-01 01:01";
			}
			
			NSString *formedDate = [DateTimeFormatter formatDateEEEMMMdd:seg.relStartLocation.dateLocal];
			
			if (segDictByDate[formedDate] == nil ) 
			{
				NSMutableArray *ma = [[NSMutableArray alloc] initWithObjects:seg, nil];
				segDictByDate[formedDate] = ma;
				[holdKeys addObject:seg.relStartLocation.dateLocal];
			}
			else
			{
				NSMutableArray *ma = segDictByDate[formedDate];
				/////////
				if([seg.type isEqualToString:@"HOTEL"])
					[ma addObject:seg];
				else 
				{
					int lastPos = [ma count] - 1;
					SegmentData *lastSeg = ma[lastPos];
					if([lastSeg.type isEqualToString:@"HOTEL"])
					{
						[ma insertObject:seg atIndex:lastPos];
						//int x = 0;
					}
					else 
						[ma addObject:seg];
				}
				
				/////////
				//[ma addObject:seg];
			}
		}
	}
	
	//now sort inside each day
	for(NSString *segDate in segDictByDate)
	{
		NSMutableArray *ma = segDictByDate[segDate];
		NSSortDescriptor *descriptor = [[NSSortDescriptor alloc] initWithKey:@"relStartLocation.dateLocal" ascending:YES];
		[ma sortUsingDescriptors:[NSMutableArray arrayWithObjects:descriptor,nil]];
		
		NSMutableArray *a = [[NSMutableArray alloc] initWithArray:ma];
		int iCount = 0;
		for(int i = 0; i < [a count]; i++)
		{
			SegmentData *s = a[i];
			if([s.type isEqualToString:@"HOTEL"])
			{
				//shove to the back
				[ma removeObjectAtIndex:i - iCount];
				[ma addObject:s];
				iCount++;
				//break;
			}
		}
	}
	
	//shove the date back into the header slot
	for(NSString *segDate in segDictByDate)
	{
		NSMutableArray *ma = segDictByDate[segDate];
		[ma insertObject:segDate atIndex:0];
	}
	
	
	NSArray *sortedKeys = [holdKeys sortedArrayUsingSelector:@selector(caseInsensitiveCompare:)];
	
	holdKeys = [[NSMutableArray alloc] init];
	int cnt = [sortedKeys count];
	for (int x = 0; x < cnt; x++)
	{
		NSString *sortedDate = sortedKeys[x]; 
		[holdKeys addObject:[DateTimeFormatter formatDateEEEMMMdd:sortedDate]];
	}

	
	self.tripBits = segDictByDate;
	self.keys = holdKeys;
	
	[self inspectAddBookingRows];
	
}



-(void)inspectAddBookingRows
{
	for(NSString *segDate in self.tripBits)
	{
		if(![ segDate isEqualToString: TRIP_DETAILS_TEXT])
		{
//			NSMutableArray *ma = [self.tripBits objectForKey:segDate];
//			int hasTaxi = 0;
//			int hasCar = 0;
////			int *hasHotel = 0;
////			int *hasAir = 0;
//			for(int y=1; y < [ma count]; y++)
//			{//always skip the header
//				SegmentData *seg = [ma objectAtIndex:y];
//				if ([seg.type isEqualToString: SEG_TYPE_CAR])
//				{
//					hasCar = y;
//				}
////				else if ([seg.type isEqualToString: SEG_TYPE_HOTEL])
////				{
////					hasHotel = y;
////				}
//				else if ([seg.type isEqualToString: SEG_TYPE_RIDE])
//				{
//					hasTaxi = y;
//				}
////				else if ([seg.type isEqualToString: SEG_TYPE_AIR])
////				{
////					hasAir = y;
////				}
//			}
			
//			if (hasTaxi == 0 & hasCar == 0) 
//			{
//				SegmentData *seg = [[SegmentData alloc] init];
//				[seg setType:BOOK_TAXI];
//				[seg setIdKey:[NSString stringWithFormat:@"%@-%@", BOOK_TAXI, segDate]];
//				[seg setSegmentName:[Localizer getLocalizedText:@"SLV_BOOK_A_TAXI"]]; 
//				
////				int *maxMa = [ma count];
////				if(hasAir > 0)
////					maxMa = hasAir + 1;
////				if (hasHotel > 0) 
////					maxMa = hasHotel -1;
////				
//				//[ma insertObject:seg atIndex:maxMa];
//				[ma addObject:seg];
//				
//				[seg release];
//			}
		}
	}
}




-(UIImage *)scaleImageToFit:(UIImage *) img MaxW:(float)maxW MaxH:(float)maxH
{
	int w = img.size.width;
	int h = img.size.height;
	float scaler = (float)w / maxW;
	if(w <= maxW && h > maxH)
	{
		scaler = (float)h / maxH;
		w = w / scaler;
		return [ImageUtil imageWithImage:img scaledToSize:CGSizeMake(w, maxH)];
	}
	else 
	{
		h = h / scaler;
		if(h > maxH)
		{
			scaler = (float)h / maxH;
			w = maxW / scaler;
			return [ImageUtil imageWithImage:img scaledToSize:CGSizeMake(w, maxH)];
		}
		else 
			return [ImageUtil imageWithImage:img scaledToSize:CGSizeMake(maxW, h)];//[[UIImage alloc] initWithData:mydata];
	}
}

//-(void) manipulateCarCell:(SegDetailCellPad *)cell Seg:(SegmentData *)segment
//{
//	[cell.contentView bringSubviewToFront:cell.iv];
//	for(UIView *v in cell.scroller.subviews)
//		[v removeFromSuperview];
//	
//	float w = 211; //cell.scroller.view.frame.size.width;
//	float h = 243; //cell.scroller.view.frame.size.height;
//	
//	UIImage *gotImg = [[ExSystem sharedInstance].imageControl getCarImageSynch:segment.vendor CountryCode:segment.startAirportCountryCode 
//		ClassOfCar:segment.classOfCar BodyType:segment.bodyType FetchURI:segment.imageCarURI];
//
//	//BOOOOOOOOOOM
//	if(gotImg == nil)
//	{
//		//resize table to fill whole screen
//		cell.tableDetails.autoresizingMask = UIViewAutoresizingFlexibleWidth;
//		if ([ExSystem isLandscape]) 
//			cell.tableDetails.frame = CGRectMake(0, 44, 700, 249);
//		else
//			cell.tableDetails.frame = CGRectMake(0, 44, 700, 249);
//
//		//cell.tableDetails.autoresizingMask = UIViewAutoresizingFlexibleWidth;
//	}
//	else 
//	{
//		UIImage *img = [self scaleImageToFit:gotImg MaxW:w MaxH:h];
//		
//		[cell.iv setImage:img];
//
//		cell.tableDetails.autoresizingMask = UIViewAutoresizingNone;
//		if ([ExSystem isLandscape]) 
//			cell.tableDetails.frame = CGRectMake(210, 44, 400, 252);
//		else 
//			cell.tableDetails.frame = CGRectMake(210, 44, 460, 252);
//	}
//}

-(void) deselect: (id) sender
{
	[self.tripTable deselectRowAtIndexPath:[self.tripTable indexPathForSelectedRow] animated:YES];
}

- (void)switchToSegmentView:(NSString *)idKey SegmentType:(NSString *)segmentType TripKey:(NSString *)ourTripKey Segment:(SegmentData *)segment
{
	if (segment != nil) 
	{
		//NSLog(@"segment idKey:%@, type:%@, ourTripKey:%@", segment.idKey, segment.type, ourTripKey);
		NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:segment.idKey, @"SegmentKey", ourTripKey, @"TripKey", segment.type, @"SegmentType", segment, @"Segment", @"YES", @"SKIP_PARSE", nil];
		
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



-(void) killMe:(id)sender
{
	[self.view removeFromSuperview];
}


-(UIView *) makeDayView:(NSString *)dayTitle Pos:(int)iPos
{
	float w = dayWidth;
	float h = 665.0;
	//float x = (w * iPos);
	float y = 0;
	
	if([ExSystem isLandscape])
	{
		//dayWidth = 337.0;
		h = 408.0;
		//w = dayWidth;
		//x = (w * iPos);
	}
	
    UIView *v = [[UIView alloc] initWithFrame:CGRectMake(w * iPos, y, w, h)];
	
	UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	
	if([ExSystem isLandscape])
		iv.image = [UIImage imageNamed:@"itinerary_day"];
	else 
		iv.image = [UIImage imageNamed:@"itinerary_day_portrait"];
	
	[v addSubview:iv];
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(10, 0, 322, 32)];
	[lbl setNumberOfLines:1];
	[lbl setTextAlignment:NSTextAlignmentLeft];
	[lbl setFont:[UIFont boldSystemFontOfSize:17]];
	[lbl setText:dayTitle];
	[lbl setTextColor:[UIColor whiteColor]];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setLineBreakMode:NSLineBreakByTruncatingTail];
	[v addSubview:lbl];
	
	return v;
	
}


-(UIView *) makeSegmentRow:(int)iPos Segment:(EntitySegment *)segment ParentView:(UIView *)parentView NextY:(float)nextY
{
	float w = dayWidth - 9;
	float h = 100.0;
	float y = nextY;
	NSString *segmentImage = @"nav_hotel_dark";
	
	if([segment.type isEqualToString:@"AIR"])
		segmentImage = @"nav_flight_dark";
	else if([segment.type isEqualToString:@"DINING"])
		segmentImage = @"nav_dine_dark";
	else if([segment.type isEqualToString:@"CAR"])
		segmentImage = @"nav_car_dark";
	else if([segment.type isEqualToString:@"RAIL"])
		segmentImage = @"nav_rail_dark";
	else if([segment.type isEqualToString:@"TAXI"] || [segment.type isEqualToString:@"RIDE"])
		segmentImage = @"nav_taxi_dark";
	
	NSMutableArray *a = [self makeSegmentLabels:segment];
	h = [a count] * 20.0;
	
	UIView *v = [[UIView alloc] initWithFrame:CGRectMake(4, nextY, w, h)];
	v.backgroundColor = [UIColor whiteColor];
	
	float x = 1.0;
	y = (h - 57.0) / 2;
	UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(x, y, 57.0, 57.0)];
	iv.image = [UIImage imageNamed:segmentImage];
	[v addSubview:iv];
	
	for(UILabel *lbl in a)
		[v addSubview:lbl];
	
	UILabel *lblBack = [[UILabel alloc] initWithFrame:CGRectMake(0, h-1, w, 1)];
	lblBack.backgroundColor = [UIColor grayColor];
	[v addSubview:lblBack];
	
	ButtonSegment *btn = [[ButtonSegment alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	btn.segment = segment;
	btn.parentView = v;
	btn.dayView = parentView;
	[btn addTarget:self action:@selector(showSegment:) forControlEvents:UIControlEventTouchUpInside];
	[v addSubview:btn];
	
	return v;
}


-(UILabel *) makeBasicLabel:(NSString *)text IsBold:(BOOL)isBold FontSize:(int)fontSize FontColor:(UIColor *)fontColor Y:(float)y X:(float)x W:(float)w H:(float)h
{
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(x, y, w, h)];
	if (isBold)
		lbl.font = [UIFont boldSystemFontOfSize:fontSize];
	else 
		lbl.font = [UIFont systemFontOfSize:fontSize];
	
	lbl.textColor = fontColor;
	
	lbl.text = text;
	
	lbl.backgroundColor = [UIColor clearColor];
	
	return lbl;
	
}


-(NSMutableArray *) makeSegmentLabels:(EntitySegment *)segment
{
//	NSString *line1 = nil;
//	NSString *line2 = nil;
//	NSString *line3 = nil;
//	NSString *line4 = nil;
	
	NSMutableArray *lines = [[NSMutableArray alloc] initWithObjects:nil];
	
	float y = 5.0;
	int iPos = 0;
	float h = 20.0;
	float w = dayWidth;
	
	NSString *location = nil;
	if(segment.confirmationNumber == nil)
		segment.confirmationNumber= @"--";
	
	NSString *startFormatted = nil;
	if(segment.relStartLocation.dateLocal != nil)
	{
		startFormatted = [DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal];
	}
	else 
	{
		startFormatted = @"";
	}
	
	NSString *vendorName;
	if (segment.vendorName != nil)
	{
		vendorName = segment.vendorName;
	}
	else 
	{
		vendorName = segment.vendor;
	}
	
	
	if ([segment.type isEqualToString:SEG_TYPE_AIR])
	{
		[lines addObject: [NSString stringWithFormat:@"%@ %@ %@", segment.relStartLocation.airportCity, [Localizer getLocalizedText:@"SLV_TO"], segment.relEndLocation.airportCity]];
		
        //Departure Time and Date
        NSMutableString *departTime = [NSMutableString string];
        NSMutableString *departDate = [NSMutableString string];
        [SegmentData getDepartTimeString:segment timeStr:departTime dateStr:departDate];
		
		[lines addObject:  [NSString stringWithFormat:@"%@ %@ %@", vendorName, segment.flightNumber, departTime]]; //line 2
		
		//terminal gate redux
		//cell.lblArriveGateTerminal.text = [self getGateTerminal:segment.endGate Terminal:segment.endTerminal];
        NSMutableString *term = [NSMutableString string];
        NSMutableString *gate = [NSMutableString string];
        
        [SegmentData getDepartTermGate:segment terminal:term gate:gate];
        
		[lines addObject: [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal t Gate t"], term, gate]];
		
        //Arrival Time and Date
        NSMutableString *arriveTime = [NSMutableString string];
        NSMutableString *arriveDate = [NSMutableString string];
        [SegmentData getArriveTimeString:segment timeStr:arriveTime dateStr:arriveDate];

		[lines addObject:[NSString stringWithFormat:@"Arrive %@ %@ %@", segment.relEndLocation.cityCode, arriveDate, arriveTime]];
		
		if(segment.confirmationNumber == nil)
			segment.confirmationNumber= @"--";
		[lines addObject: [NSString stringWithFormat:@"%@ #%@", [Localizer getLocalizedText:@"SLV_CONFIRMATION"], segment.confirmationNumber]]; //line 4
	}
	else if ([segment.type isEqualToString:BOOK_TAXI])
	{
//		actionText = [Localizer getLocalizedText:@"SLV_BOOK_A_TAXI"];
//		vendor = [Localizer getLocalizedText:@"SLV_CLICK_ON"];
//		location = [Localizer getLocalizedText:@"SLV_TAXI_MAGIC"];
//		details = [Localizer getLocalizedText:@"SLV_TAXI_MAGIC2"];
//		[cell setAccessoryType: UITableViewCellAccessoryNone];
//		[cell.btnDrill setHidden:YES];
	}
	else if ([segment.type isEqualToString:SEG_TYPE_CAR])
	{
		[lines addObject:  vendorName];
		[lines addObject:  [NSString stringWithFormat:[Localizer getLocalizedText:@"SLV_PICKUP_AT"], [DateTimeFormatter formatTimeForTravel: segment.relStartLocation.dateLocal]]] ;//line 2
		if(segment.relStartLocation.airportCity != nil)
			[lines addObject:  [NSMutableString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"SLV_FROM"], segment.relStartLocation.airportCity]]; //line 3
		else if(segment.relStartLocation.cityCode != nil)
			[lines addObject:  [NSMutableString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"SLV_FROM"], segment.relStartLocation.cityCode]]; 
		else 
			[lines addObject:  [NSMutableString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"SLV_FROM"], @"?"]]; 
		
		//line4 = nil; // [NSString stringWithFormat:@"%@ #%@", [Localizer getLocalizedText:@"SLV_CONFIRMATION"], segment.confirmationNumber]; //line 4
	}
	else if ([segment.type isEqualToString:SEG_TYPE_RIDE])
	{
		[lines addObject:  [NSString stringWithFormat:[Localizer getLocalizedText:@"SLV_PICKUP_BY"], vendorName, startFormatted]];		
		[lines addObject:  [NSString stringWithFormat:@"%@, %@, %@", segment.relStartLocation.address, segment.relStartLocation.city, segment.relStartLocation.state]];//line 2
		if( segment.pickupInstructions != nil)
			[lines addObject:  segment.pickupInstructions]; //line 3
		
		if(segment.meetingInstructions != nil)
			[lines addObject:  segment.meetingInstructions]; //line 4
	}
	else if ([segment.type isEqualToString:SEG_TYPE_HOTEL])
	{
        NSString *vendorName;
        if (segment.segmentName != nil)
            vendorName = segment.segmentName;
        else if (segment.vendorName != nil)
            vendorName = segment.vendorName;
        else 
            vendorName = segment.vendor;
        
		[lines addObject:  vendorName];
		location = segment.relStartLocation.address;//line 2
		
		if(segment.relStartLocation.city != nil)
			location = [NSString stringWithFormat:@"%@,", segment.relStartLocation.city];
		
		if(segment.relStartLocation.state != nil)
			location =  [NSString stringWithFormat:@"%@ %@,", location, segment.relStartLocation.state];
		
		if(segment.relStartLocation.postalCode)
			location =  [NSString stringWithFormat:@"%@ %@", location, segment.relStartLocation.postalCode];
		
		if(location != nil)
			[lines addObject:location];

		//[lines addObject:  nil;// [NSString stringWithFormat:@"%@ #%@", [Localizer getLocalizedText:@"SLV_CONFIRMATION"], segment.confirmationNumber]; //line 4
	}
	else if ([segment.type isEqualToString:SEG_TYPE_RAIL])
	{
		NSString *railStation = [SegmentData getRailStation:segment.relStartLocation];
		
		[lines addObject:  [NSString stringWithFormat:@"%@ %@ %@", railStation, [Localizer getLocalizedText:@"SLV_AT"], startFormatted]];
		
		NSString *endRailStation = [SegmentData getRailStation:segment.relEndLocation];

		if( segment.trainNumber == nil)
			segment.trainNumber = @"--";
		[lines addObject:  [NSString stringWithFormat:@"on %@ %@ %@ %@", vendorName, segment.trainNumber, [Localizer getLocalizedText:@"SLV_TO"], endRailStation]]; //line 2
		
		NSString *platform = segment.relStartLocation.platform;
		NSString *wagon = segment.wagonNumber;
		if(platform == nil)
			platform = @"--";
		
		if(wagon == nil)
			wagon = @"--";
		[lines addObject:  [NSMutableString stringWithFormat:[Localizer getLocalizedText:@"SLV_PLATFORM_WAGON"], platform, wagon]]; //line 3	
		if(segment.confirmationNumber == nil)
			segment.confirmationNumber= @"--";
		[lines addObject:  [NSString stringWithFormat:@"%@ #%@",[Localizer getLocalizedText:@"SLV_CONFIRMATION"], segment.confirmationNumber]]; //line 4
	}
	else if ([segment.type isEqualToString:SEG_TYPE_PARKING])
	{
		[lines addObject:  [NSString stringWithFormat:[Localizer getLocalizedText:@"SLV_DROP_OFF"], vendorName, startFormatted]];
		location = [SegmentData getMapAddress:segment.relStartLocation];
		
        if(location != nil)
			[lines addObject:location];
		
			//line3 == nil;
		
		//line4 = nil;
	}
	else if ([segment.type isEqualToString:SEG_TYPE_DINING] || [segment.type isEqualToString:SEG_TYPE_EVENT])
	{
		[lines addObject:  [NSString stringWithFormat:@"%@ %@", segment.segmentName, startFormatted]];
		[lines addObject:  segment.relStartLocation.address];//line 2
		[lines addObject:  [NSMutableString stringWithFormat:@"%@, %@ %@", segment.relStartLocation.city, segment.relStartLocation.state, segment.relStartLocation.postalCode]]; //line 3
		//[lines addObject:  nil; // [NSString stringWithFormat:@"%@ #%@", [Localizer getLocalizedText:@"SLV_CONFIRMATION"], segment.confirmationNumber]; //line 4
	}
	else
	{
		[lines addObject: [NSString stringWithFormat:@"%@", startFormatted]];
	}
	
	NSMutableArray *a = [[NSMutableArray alloc] initWithObjects:nil];
	iPos = 0;
	
	y = 0;
	float x = 60;
	
	for(NSString *s in lines)
	{
		UILabel *lblHead = nil;
		
		if(iPos == 0)
			lblHead = [self makeBasicLabel:s IsBold:YES FontSize:18.0 FontColor:[UIColor blackColor] Y:((iPos * h) + y) X:x W:w - 70.0 H:h];
		else 
			lblHead = [self makeBasicLabel:s IsBold:NO FontSize:17.0 FontColor:[UIColor blackColor] Y:((iPos * h) + y) X:x W:w - 70.0 H:h];
		
		[a addObject:lblHead];
		iPos++;
	}
	
//	if(line1 != nil)
//	{
//		UILabel *lblHead = [self makeBasicLabel:line1 IsBold:YES FontSize:18.0 FontColor:[UIColor blackColor] Y:((iPos * h) + y) X:x W:w - 70.0 H:h];
//		[a addObject:lblHead];
//		iPos++;
//	}
//	if(line2 != nil)
//	{
//		UILabel *lblHead = [self makeBasicLabel:line2 IsBold:NO FontSize:17.0 FontColor:[UIColor blackColor] Y:((iPos * h) + y) X:x W:w - 70.0 H:h];
//		[a addObject:lblHead];
//		iPos++;
//	}
//	if(line3 != nil)
//	{
//		UILabel *lblHead = [self makeBasicLabel:line3 IsBold:NO FontSize:17.0 FontColor:[UIColor blackColor] Y:((iPos * h) + y) X:x W:w - 70.0 H:h];
//		[a addObject:lblHead];
//		iPos++;
//	}
//	if(line4 != nil)
//	{
//		UILabel *lblHead = [self makeBasicLabel:line4 IsBold:NO FontSize:17.0 FontColor:[UIColor blackColor] Y:((iPos * h) + y) X:x W:w - 70.0 H:h];
//		[a addObject:lblHead];
//		iPos++;
//	}
	
	
	return a;
}	

#pragma mark -
#pragma mark Show Segment in Popover
-(void) showSegment:(id)sender
{
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
	}
	
	ButtonSegment *btn = (ButtonSegment *)sender;
	EntitySegment *seg = btn.segment;
	
	itinDetailsVC = [[ItinDetailsViewController alloc] initWithNibName:@"ItinDetailsViewController" bundle:nil];
	//itinDetailsVC.view;
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:itinDetailsVC];    
	
	int currPage = pageControl.currentPage;
	//currPage = currPage - 1;
	CGFloat adjustSide = currPage * dayWidth;
	
	float x = btn.frame.origin.x + scroller.frame.origin.x + btn.parentView.frame.origin.x + btn.dayView.frame.origin.x - adjustSide;
	float y = btn.frame.origin.y + scroller.frame.origin.y + btn.parentView.frame.origin.y + btn.frame.size.height - 20.0;

	//float viewW = 337.0;

	CGRect rect = CGRectMake(x, y, 59, 1);
	
    [self.pickerPopOver presentPopoverFromRect:rect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionUp animated:YES]; 

	// Calling loadSegment *after* the popover is presented, because the view controller will only receive the message response if it's visible.
	[itinDetailsVC loadSegment:trip.tripKey Trip:trip SegmentKey:seg.idKey Segment:seg SegmentType:seg.type];
}


#pragma mark -
#pragma mark The Guts
- (void)setupPage
{
	scroller.delegate = self;
	
	//[self.scroller setBackgroundColor:[UIColor blackColor]];
	[scroller setCanCancelContentTouches:NO];
	
	scroller.indicatorStyle = UIScrollViewIndicatorStyleWhite;
	scroller.clipsToBounds = YES;
	scroller.scrollEnabled = YES;
	scroller.pagingEnabled = YES;
	//NSLog(@"numberofpages = %d", [keys count]);
	self.pageControl.numberOfPages = [keys count];

}

#pragma mark -
#pragma mark UIScrollViewDelegate stuff
- (void)scrollViewDidScroll:(UIScrollView *)_scrollView
{
    if (pageControlIsChangingPage) {
        return;
    }
	
	/*
	 *	We switch page at 50% across
	 */
    CGFloat pageWidth = dayWidth;// _scrollView.frame.size.width;
	//pageWidth = dayWidth;
    int page = floor((_scrollView.contentOffset.x - pageWidth / 2) / pageWidth) + 1;
    pageControl.currentPage = page;
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)_scrollView 
{
    pageControlIsChangingPage = NO;
}

#pragma mark -
#pragma mark PageControl stuff
- (IBAction)changePage:(id)sender 
{
	/*
	 *	Change the scroll view
	 */
    CGRect frame = scroller.frame;
    frame.origin.x = frame.size.width * pageControl.currentPage;
    frame.origin.y = 0;
	
    [scroller scrollRectToVisible:frame animated:NO];
	
	/*
	 *	When the animated scrolling finishings, scrollViewDidEndDecelerating will turn this off
	 */
    pageControlIsChangingPage = YES;
}



#pragma mark -
#pragma mark popover methods
- (IBAction)buttonExpensesPressed:(id)sender
{
	if(pickerPopOver != nil)
	{
		[pickerPopOver dismissPopoverAnimated:YES];
	}
	
	oopeListVC = [[OutOfPocketListViewController alloc] initWithNibName:@"OutOfPocketListViewController" bundle:nil];
	[oopeListVC loadExpenses];
	
	UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:oopeListVC];
	
	localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[localNavigationController setToolbarHidden:NO];
	
	[self presentViewController:localNavigationController animated:YES completion:nil];

}

- (IBAction)buttonReportsPressed:(id)sender
{
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
	
	self.reportsListVC = [[ActiveReportListViewController alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
	reportsListVC.iPadHome = iPadHome;
	reportsListVC.fromMVC = self;
	reportsListVC.isPad = YES;
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:reportsListVC];               
	
	UIButton *btn = (UIButton *)sender;
	float viewW = 80.0;
	int btnPos = btn.tag;
	float x = middleX + (btnPos * viewW);
	
	float y = 660.0;
	if(![ExSystem isLandscape])
	{
		float screenH = 1004.0;
		y = screenH - 90.0;
	}
	
	CGRect rect = CGRectMake(x, y, viewW, 1);
	
    [self.pickerPopOver presentPopoverFromRect:rect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES]; 

	// Calling loadReports *after* the popover is presented, because the view controller will only receive the message response if it's visible.
	[reportsListVC loadReports];
}


- (IBAction)buttonTripsPressed:(id)sender
{
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
	
	tripsListVC = [[TripsViewController alloc] initWithNibName:@"TripsView" bundle:nil];
	tripsListVC.iPadHome = iPadHome;
	tripsListVC.fromMVC = self;

	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:tripsListVC];               
	
    // before changing line below, review MOB-12642. there was an abundance of code here to manually move the window popover that was removed.
    [self.pickerPopOver presentPopoverFromBarButtonItem:sender permittedArrowDirections:UIPopoverArrowDirectionUp animated:YES];

	// Calling loadTrips *after* the popover is presented, because the view controller will only receive the message response if it's visible.
	[tripsListVC loadTrips];
}

-(IBAction)btnCancelPressed:(id)sender
{
    UIButton *button = (UIButton*) sender;
    ItinDetailsCellLabelPad *cell;

    // Apple changed how deep the view stack is.
    // We really should have kept our own reference to the parent instead of relying on the number of layers up to the cell...
    UIView *superview = button.superview;
    while (!cell && superview)
    {
        if([superview isKindOfClass:[ItinDetailsCellLabelPad class]])
        {
            cell = (ItinDetailsCellLabelPad*)superview;
        }
        else
        {
            superview = superview.superview;
        }
    }

    NSIndexPath *idxPath = [self.tableDetails indexPathForCell:cell];
    // This label has the segkey to cancel
    self.btnCancelSegKey = cell.labelValue1.text;
    
    if([cell.segmentType isEqualToString:SEG_TYPE_HOTEL])
	{
		self.hotelCancelIndexPath = idxPath;
		[self cancelHotelPressed];
	}
    else if([cell.segmentType isEqualToString:SEG_TYPE_CAR])
	{
		self.hotelCancelIndexPath = idxPath;
		[self cancelCarPressed];
	}
    else if ([cell.segmentType isEqualToString:SEG_TYPE_RAIL])
    {
        self.hotelCancelIndexPath = idxPath;
        [self cancelAmtrakPressed];
    }
}

-(void) dismissPopovers
{
	if (popoverController != nil) {
        [popoverController dismissPopoverAnimated:YES];
    } 
	if (pickerPopOver != nil) {
        [pickerPopOver dismissPopoverAnimated:YES];
    } 
}

-(void) configureOfferCell:(OfferCell *)cell offer:(EntityOffer*)offer
{ 
    [cell configureLabelFontForLabel:cell.lblTitle WithText:offer.title];
    //    cell.lblTitle.text = offer.title;
    [cell.activity startAnimating];
    [cell.contentView bringSubviewToFront:cell.activity];
    cell.lblTitle.textColor = [UIColor blackColor];
    
    [[OfferManager sharedInstance] processImageDataWithBlock:^(NSData *imageData) {
		if (self.view.window) {
			UIImage *image = [UIImage imageWithData:imageData];
			cell.ivIcon.image = image;
			[cell.activity stopAnimating];
		}
	} offer:offer];
}


#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return [keys count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	NSString *key = keys[section];
    if (isOffersHidden) {
        return [filteredSegments[key] count];
    }
    else
    {
        NSMutableArray *day = dictSegRows[key];
        return [day count];
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    NSUInteger row = [indexPath row];
	NSUInteger section = [indexPath section];
	NSString *key = keys[section];
	
    NSMutableArray *sectionData = nil;
    if (isOffersHidden)
        sectionData = filteredSegments[key];
    else 
        sectionData = dictSegRows[key];
    
	NSMutableArray *aSegs = sectionData[row];
	
	SegmentRow *segRow = nil;
	NSObject *o = sectionData[row];
	
	if([o isKindOfClass:[SegmentRow class]] || [o isKindOfClass:[EntityOffer class]])
		segRow = sectionData[row];
	else
		segRow = aSegs[0];

	EntitySegment *segment = nil;
    // check if the segRow is not an offer class
    if (![o isKindOfClass:[EntityOffer class]])
        segment = segRow.segment;
    
    if ([o isKindOfClass:[EntityOffer class]])
    {
        EntityOffer *offer = (EntityOffer*)o;
        OfferCell *offerCell = nil;
        offerCell = (OfferCell *)[tableView dequeueReusableCellWithIdentifier: @"OfferCell"];
        if (offerCell == nil)  
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"OfferCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[OfferCell class]])
                    offerCell = (OfferCell *)oneObject;
        }
        [self configureOfferCell:offerCell offer:offer];
        [offerCell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
        return offerCell;
    }
    else if (segRow.isSep)
	{
		SepCell *cell = (SepCell *)[tableDetails dequeueReusableCellWithIdentifier: @"SepCell"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SepCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[SepCell class]])
					cell = (SepCell *)oneObject;
		}
		
		return cell;
	}
	
	if(segRow.isSpecialCell)
	{
		if([segRow.specialCellType isEqualToString:@"HOTEL"])
		{
			ItinDetailsHotelCarCellPad *cell = (ItinDetailsHotelCarCellPad *)[tableDetails dequeueReusableCellWithIdentifier: @"ItinDetailsHotelCarCellPad"];
			if (cell == nil)  
			{
				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailsHotelCarCellPad" owner:self options:nil];
				for (id oneObject in nib)
					if ([oneObject isKindOfClass:[ItinDetailsHotelCarCellPad class]])
						cell = (ItinDetailsHotelCarCellPad *)oneObject;
			}
			
			cell.parentVC = self;

			NSString *key = [NSString stringWithFormat:@"%d,%d,IMAGE0", section, row];
			if(dictHotelImages[key] == nil && segment.gdsId != nil && segment.propertyId != nil && [ExSystem connectedToNetwork])
			{
				isLoadingHotelImage = YES;
				[self.loadingView setHidden:NO];
				[self.view bringSubviewToFront:self.loadingView];
				dictHotelImages[key] = cell.ivHotelAlbum;
				
				//go fetch the array of images... don't do it for every cell draw!
				NSString *sSection = [NSString stringWithFormat:@"%d", section];
				NSString *sRow = [NSString stringWithFormat:@"%d", row];
				NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"HOTELCELL", @"TO_VIEW", @"YES", @"REFRESHING"
											 , segment.gdsId, @"GDS", segment.propertyId, @"PROPERTY_ID", segment, @"SEGMENT", sSection, @"SECTION", sRow, @"ROW", nil]; //, cell, @"CELL"
				[[ExSystem sharedInstance].msgControl createMsg:HOTEL_IMAGES CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
				//end fetch call
			}
			else {
				UIImageView *iv = dictHotelImages[key];
				if(iv != nil)
				{
					cell.ivHotelAlbum.image = iv.image;
                    // MOB-9821 pass in image urls instead image views
                    key = [NSString stringWithFormat:@"%d-%d", section, row];
					//key = [NSString stringWithFormat:@"%d,%d,IMAGES", section, row];
					cell.imageArray = dictHotelImageURLs[key];
				}
			}
            
            [cell setSelectionStyle:UITableViewCellSelectionStyleNone];
            [cell.ivTripType setImage:[UIImage imageNamed:@"icon_ipad_hotel"]];

            if([segment.type isEqualToString:@"HOTEL"] && segment.segmentName != nil)
            {
                NSString *vendorName;
                if (segment.segmentName != nil)
                    vendorName = segment.segmentName;
                else if (segment.vendorName != nil)
                    vendorName = segment.vendorName;
                else
                    vendorName = segment.vendor;
                cell.lblHotelCarVendor.text = vendorName;
            }
            else
                cell.lblHotelCarVendor.text = segment.vendorName;

            UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:segment.vendor VendorType:@"h" RespondToIV:cell.ivVendorIcon];
            cell.ivVendorIcon.image = gotImg;
            
            if (segment.confirmationNumber != nil)
                cell.lblConfirmNum.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONF_NUM"], segment.confirmationNumber];
            else
                cell.lblConfirmNum.text = @"";
            
			[cell.scroller setHidden:YES];
			[cell.pageControl setHidden:YES];
			[cell setSelectionStyle:UITableViewCellSelectionStyleNone];
            
            CAGradientLayer *gradient = [CAGradientLayer layer];
            UIColor *topColor = [UIColor colorWithRed:255/255.f green:255/255.f blue:255/255.f alpha:1.0];
            UIColor *bottomColor = [UIColor colorWithRed:206/255.f green:206/255.f blue:206/255.f alpha:1.0];
            
            [gradient setFrame:cell.bounds];
            [gradient setColors:@[(id)topColor.CGColor, (id)bottomColor.CGColor]];
            [cell.layer insertSublayer:gradient atIndex:0];
            
			return cell;
		}
		else if([segRow.specialCellType isEqualToString:@"PARKING"])
		{
			ItinDetailsParkingCell *cell = (ItinDetailsParkingCell *)[tableDetails dequeueReusableCellWithIdentifier: @"ItinDetailsParkingCell"];
			if (cell == nil)  
			{
				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailsParkingCell" owner:self options:nil];
				for (id oneObject in nib)
					if ([oneObject isKindOfClass:[ItinDetailsParkingCell class]])
						cell = (ItinDetailsParkingCell *)oneObject;
			}
			
			cell.lblVendor.text = segment.vendorName;
			
			if(segment.relStartLocation.address != nil)
			{
				cell.lblAddress1.text = segment.relStartLocation.address;
				cell.lblAddress2.text = [NSString stringWithFormat:@"%@, %@, %@", segment.relStartLocation.city, segment.relStartLocation.state, segment.relStartLocation.postalCode];
			}
			else {
				[cell.lblAddress1 setHidden:YES];
				[cell.lblAddress2 setHidden:YES];
				[cell.ivMap setHidden:YES];
			}
			
			if(segment.phoneNumber != nil)
				cell.lblPhone.text = segment.phoneNumber;
			else {
				[cell.lblPhone setHidden:YES];
				[cell.ivPhone setHidden:YES];
			}
			return cell;
		}
		else if([segRow.specialCellType isEqualToString:@"AIR"])
		{
			TripAirSegmentCellPad *cell = (TripAirSegmentCellPad *)[tableDetails dequeueReusableCellWithIdentifier: @"TripAirSegmentCellPad"];
			if (cell == nil)  
			{
				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TripAirSegmentCellPad" owner:self options:nil];
				for (id oneObject in nib)
					if ([oneObject isKindOfClass:[TripAirSegmentCellPad class]])
						cell = (TripAirSegmentCellPad *)oneObject;
			}
			
            [cell setSelectionStyle:UITableViewCellSelectionStyleNone];
            
            NSString *vendorName = segment.vendorName;
            if (vendorName == nil) {
                vendorName = segment.vendorName;
            }
            NSString *vendor = [NSString stringWithFormat:@"%@ %@", vendorName ?: @"", segment.flightNumber ?: @""];
            UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:segment.vendor VendorType:@"a" RespondToIV:cell.ivVendorIcon];
            cell.ivVendorIcon.image = gotImg;
            cell.lblVendor.text = vendor;
            
            if (segment.operatedBy != nil)
            {
              
                NSString *opVendor = segment.operatedByVendor;
                if(segment.operatedByVendor == nil) {
                    opVendor = segment.operatedBy;
                }

                NSString *flightNum = segment.operatedByFlightNumber;
                
                if (segment.operatedByFlightNumber == nil) {
                    flightNum = @"";
                } else {
                    flightNum = [NSString stringWithFormat:@" %@", flightNum];
                }
                
                NSString *OBvendor = [NSString stringWithFormat:@" (%@%@)", opVendor, flightNum];
                
                //cell.ivVendorIcon.image = gotImg;
                cell.lblVendor.text = [cell.lblVendor.text stringByAppendingString:OBvendor];
            }
            
            if (segment.confirmationNumber != nil)
                cell.lblConfirmation.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONF_NUM"], segment.confirmationNumber];
            else
                cell.lblConfirmation.text = @"";
            
			cell.lblDepartAirport.text = [NSString stringWithFormat:@"Depart %@ (%@)", segment.relStartLocation.airportCity, segment.relStartLocation.cityCode];
			
            //Departure Time and Date
            NSMutableString *departTime = [NSMutableString string];
            NSMutableString *departDate = [NSMutableString string];
            [SegmentData getDepartTimeString:segment timeStr:departTime dateStr:departDate];
            
            NSArray *aTime = [departTime componentsSeparatedByString:@" "];     // separte time for AM/PM
            if([aTime count] == 2)
            {
                cell.lblDepartTime.text = aTime[0];
                cell.lblDepartAMPM.text = aTime[1];
            }
            else
            {
                cell.lblDepartTime.text = departTime;
                cell.lblDepartAMPM.text = @"";
            }
			//cell.lblDepartTime.text = departTime;
			cell.lblDepartDate.text = departDate; //[DateTimeFormatter formatDateForTravel:segment.startDateLocal];
			cell.lblDepartGateTerminal.text = [self getGateTerminal:segment.relStartLocation.gate Terminal:segment.relStartLocation.terminal];
			//terminal gate redux
			//cell.lblArriveGateTerminal.text = [self getGateTerminal:segment.endGate Terminal:segment.endTerminal];
            
            NSMutableString *term = [NSMutableString string];
            NSMutableString *gate = [NSMutableString string];
            [SegmentData getDepartTermGate:segment terminal:term gate:gate];
			cell.lblDepartGateTerminal.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal t Gate t"], term, gate];
			// end redux
			
			cell.lblArriveAirport.text = [NSString stringWithFormat:@"Arrive %@ (%@)", segment.relEndLocation.airportCity, segment.relEndLocation.cityCode];
			
            //Arrival Time and Date
            NSMutableString *arriveTime = [NSMutableString string];
            NSMutableString *arriveDate = [NSMutableString string];
            [SegmentData getArriveTimeString:segment timeStr:arriveTime dateStr:arriveDate];

			//cell.lblArriveTime.text = arriveTime; // [DateTimeFormatter formatTimeForTravel:segment.endDateLocal];
            aTime = [arriveTime componentsSeparatedByString:@" "];
            if([aTime count] == 2)
            {
                cell.lblArriveTime.text = aTime[0];
                cell.lblArriveAMPM.text = aTime[1];
            }
            else
            {
                cell.lblArriveTime.text = arriveTime;
                cell.lblArriveAMPM.text = @"";
            }
            cell.lblTravelTime.text = [DateTimeFormatter formatDuration:segment.relStartLocation.dateUtc endDate:segment.relEndLocation.dateUtc];
            term = [NSMutableString string];
            gate = [NSMutableString string];
            [SegmentData getArriveTermGate:segment terminal:term gate:gate];
			cell.lblArriveGateTerminal.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal t Gate t"], term, gate];
			// end redux
			cell.lblArriveDate.text = arriveDate; //[DateTimeFormatter formatDateForTravel:segment.endDateLocal];
			
            CAGradientLayer *gradient = [CAGradientLayer layer];
            UIColor *topColor = [UIColor colorWithRed:255/255.f green:255/255.f blue:255/255.f alpha:1.0];
            UIColor *bottomColor = [UIColor colorWithRed:206/255.f green:206/255.f blue:206/255.f alpha:1.0];

            [gradient setFrame:cell.bounds];
            [gradient setColors:@[(id)topColor.CGColor, (id)bottomColor.CGColor]];
            [cell.layer insertSublayer:gradient atIndex:0];
            
			return cell;
		}
		else if([segRow.specialCellType isEqualToString:@"CAR"])
		{
			ItinDetailsHotelCarCellPad *cell = (ItinDetailsHotelCarCellPad *)[tableDetails dequeueReusableCellWithIdentifier: @"ItinDetailsHotelCarCellPad"];
			if (cell == nil)  
			{
				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailsHotelCarCellPad" owner:self options:nil];
				for (id oneObject in nib)
					if ([oneObject isKindOfClass:[ItinDetailsHotelCarCellPad class]])
						cell = (ItinDetailsHotelCarCellPad *)oneObject;
			}
			
            [cell setSelectionStyle:UITableViewCellSelectionStyleNone];
            [cell.ivTripType setImage:[UIImage imageNamed:@"icon_ipad_car"]];
            
            //reset vendor anc confirm# x
            CGRect lblFrame = cell.lblHotelCarVendor.frame;
            lblFrame.origin = CGPointMake(cell.ivHotelAlbum.frame.origin.x, lblFrame.origin.y);
            cell.lblHotelCarVendor.frame = lblFrame;
            
            lblFrame = cell.lblConfirmNum.frame;
            lblFrame.origin = CGPointMake(cell.ivHotelAlbum.frame.origin.x, lblFrame.origin.y);
            cell.lblConfirmNum.frame = lblFrame;
            
            [cell.ivHotelAlbum setHidden:YES];

            if([segment.type isEqualToString:@"CAR"] && segment.segmentName != nil)
            {
                NSString *vendorName;
                if (segment.segmentName != nil)
                    vendorName = segment.segmentName;
                else if (segment.vendorName != nil)
                    vendorName = segment.vendorName;
                else
                    vendorName = segment.vendor;
                cell.lblHotelCarVendor.text = vendorName;
            }
            else
                cell.lblHotelCarVendor.text = segment.vendorName;
			
            UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:segment.vendor VendorType:@"c" RespondToIV:cell.ivVendorIcon];
            cell.ivVendorIcon.image = gotImg;
            
            if (segment.confirmationNumber != nil)
                cell.lblConfirmNum.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONF_NUM"], segment.confirmationNumber];
            else
                cell.lblConfirmNum.text = @"";

			cell.parentVC = self;
			
            CAGradientLayer *gradient = [CAGradientLayer layer];
            UIColor *topColor = [UIColor colorWithRed:255/255.f green:255/255.f blue:255/255.f alpha:1.0];
            UIColor *bottomColor = [UIColor colorWithRed:206/255.f green:206/255.f blue:206/255.f alpha:1.0];
            
            [gradient setFrame:cell.bounds];
            [gradient setColors:@[(id)topColor.CGColor, (id)bottomColor.CGColor]];
            [cell.layer insertSublayer:gradient atIndex:0];
            
			return cell;
		}
		else if([segRow.specialCellType isEqualToString:@"RIDE"])
		{
			ItinDetailsCellInfo *cell = (ItinDetailsCellInfo *)[tableDetails dequeueReusableCellWithIdentifier: INFO_NIB];
			if (cell == nil)  
			{
				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:INFO_NIB owner:self options:nil];
				for (id oneObject in nib)
					if ([oneObject isKindOfClass:[ItinDetailsCellInfo class]])
						cell = (ItinDetailsCellInfo *)oneObject;
			}
			// @"%@\n%@, %@, %@"
			NSString *location = [SegmentData getMapAddress:segment.relStartLocation];
			
			[cell.btnPhone setTitle:[NSString stringWithFormat:@"  %@", segment.phoneNumber] forState:UIControlStateNormal];
			cell.phoneNumber = segment.phoneNumber;
			cell.labelMap.text = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MAP"];
			cell.mapAddress = location;
			cell.labelAddress1.text = location; // [NSString stringWithFormat:@"%@ (%@)", segment.totalRate, segment.currency];
			cell.labelAddress2.text = location;
			
			cell.mapAddress = location;
			//cell.idVC = self;
			cell.vendorCode = segment.vendor;
			cell.vendorName = segment.vendorName;
			
			UIImage *gotImg = [UIImage imageNamed:@"yellowCab.jpg"]; //todo: hard coded place holder.  Right now we have no images to insert here
			[cell.imgVendor setImage:gotImg];
			return cell;
		}
		else if([segRow.specialCellType isEqualToString:@"DINING"])
		{
			ItinDetailsCellInfo *cell = (ItinDetailsCellInfo *)[tableDetails dequeueReusableCellWithIdentifier: INFO_NIB];
			if (cell == nil)  
			{
				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:INFO_NIB owner:self options:nil];
				for (id oneObject in nib)
					if ([oneObject isKindOfClass:[ItinDetailsCellInfo class]])
						cell = (ItinDetailsCellInfo *)oneObject;
			}
// @"%@\n%@, %@, %@"			
			NSString *location = [SegmentData getMapAddress:segment.relStartLocation];
			
			cell.labelMap.text = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MAP"];
			cell.labelAddress1.text = location; // [NSString stringWithFormat:@"%@ (%@)", segment.totalRate, segment.currency];
			cell.labelAddress2.text = location;
			
			cell.mapAddress = location;
			//cell.idVC = self;
			cell.vendorCode = segment.vendor;
			cell.vendorName = segment.segmentName;
			
			UIImage *gotImg = [UIImage imageNamed:@"fakeDining"]; //todo: hard coded place holder.  Right now we have no images to insert here
			[cell.imgVendor setImage:gotImg];
			
			return cell;
			
		}
		else if([segRow.specialCellType isEqualToString:@"RAIL"])
		{
			TripAirSegmentCellPad *cell = (TripAirSegmentCellPad*)[tableView dequeueReusableCellWithIdentifier:@"TripAirSegmentCellPad"];
			if (cell == nil)
			{
				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TripAirSegmentCellPad" owner:self options:nil];
				for (id oneObject in nib)
					if ([oneObject isKindOfClass:[TripAirSegmentCellPad class]])
						cell = (TripAirSegmentCellPad *)oneObject;
			}
            
			[cell setSelectionStyle:UITableViewCellSelectionStyleNone];
            [cell.ivTripType setImage:[UIImage imageNamed:@"icon_ipad_rail"]];
            
			NSString *railStation = [SegmentData getRailStation:segment.relStartLocation];
			NSString *endRailStation = [SegmentData getRailStation:segment.relEndLocation];
            NSString *vendor = [NSString stringWithFormat:@"%@ %@", segment.vendorName, segment.trainNumber];
            
            if (segment.operatedBy != nil)
            {
                //NSLog(@"segment.operatedBy = %@" , segment.operatedBy);
                UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:segment.operatedBy VendorType:@"a" RespondToIV:cell.ivVendorIcon];
                
                NSString *opVendor = segment.operatedByVendor;
                if(segment.operatedByVendor == nil)
                    opVendor = segment.operatedBy;
                
                NSString *trainNumber = segment.operatedByTrainNumber;
                
                if(segment.operatedByTrainNumber == nil)
                    trainNumber = @"";
                else
                    trainNumber = [NSString stringWithFormat:@" %@", trainNumber];
                
                NSString *OBvendor = [NSString stringWithFormat:@"(%@%@)", opVendor, trainNumber];
                
                cell.ivVendorIcon.image = gotImg;
                cell.lblVendor.text = OBvendor;
            }
            else
            {
                UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:segment.vendor VendorType:@"a" RespondToIV:cell.ivVendorIcon];
                cell.ivVendorIcon.image = gotImg;
                cell.lblVendor.text = vendor;
            }
            
            if (segment.confirmationNumber != nil)
                cell.lblConfirmation.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONF_NUM"], segment.confirmationNumber];
            else
                cell.lblConfirmation.text = @"";
            
			cell.lblDepartDate.text = [DateTimeFormatter formatDateMedium:segment.relStartLocation.dateLocal];
            NSArray *aTime = [[DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal] componentsSeparatedByString:@" "];     // separte time for AM/PM
            if([aTime count] == 2)
            {
                cell.lblDepartTime.text = aTime[0];
                cell.lblDepartAMPM.text = aTime[1];
            }
            else
            {
                cell.lblDepartTime.text =  [DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal];
                cell.lblDepartAMPM.text = @"";
            }
			cell.lblDepartAirport.text = railStation;
            
            aTime = [[DateTimeFormatter formatTimeForTravel:segment.relEndLocation.dateLocal] componentsSeparatedByString:@" "];     // separte time for AM/PM
            if([aTime count] == 2)
            {
                cell.lblArriveTime.text = aTime[0];
                cell.lblArriveAMPM.text = aTime[1];
            }
            else
            {
                cell.lblArriveTime.text =  [DateTimeFormatter formatTimeForTravel:segment.relEndLocation.dateLocal];
                cell.lblArriveAMPM.text = @"";
            }
			cell.lblArriveDate.text = [DateTimeFormatter formatDateMedium:segment.relEndLocation.dateLocal];
			cell.lblArriveAirport.text = endRailStation;
			cell.lblTravelTime.text = [DateTimeFormatter formatDuration:segment.relStartLocation.dateLocal endDate:segment.relEndLocation.dateLocal];// [FormatUtils formatDuration:[segment.duration intValue]];
            
            [cell.lblDepartGateTerminal setHidden:YES];
            [cell.lblArriveGateTerminal setHidden:YES];
            
//			if(segment.classOfService == nil)
//				segment.classOfService = @"--";
//			if(segment.cabin == nil)
//				segment.cabin = @"--";
//			cell.lblSeat.text = [NSString stringWithFormat:@"Seat Class: %@-%@", segment.classOfService, segment.cabin];
//			cell.lblAmount.text = [Localizer getLocalizedText:@"Departing Train"]; // [FormatUtils formatMoney:segment.totalRate crnCode:@"USD"];
//			cell.lblTrain.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"Train: token"], segment.trainNumber];
            
            CAGradientLayer *gradient = [CAGradientLayer layer];
            UIColor *topColor = [UIColor colorWithRed:255/255.f green:255/255.f blue:255/255.f alpha:1.0];
            UIColor *bottomColor = [UIColor colorWithRed:206/255.f green:206/255.f blue:206/255.f alpha:1.0];
            
            [gradient setFrame:cell.bounds];
            [gradient setColors:@[(id)topColor.CGColor, (id)bottomColor.CGColor]];
            [cell.layer insertSublayer:gradient atIndex:0];
			return cell;
		}
	}
	else 
	{
		ItinDetailsCellLabelPad *cell = (ItinDetailsCellLabelPad *)[tableView dequeueReusableCellWithIdentifier:@"ItinDetailsCellLabelPad"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailsCellLabelPad" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[ItinDetailsCellLabelPad class]])
					cell = (ItinDetailsCellLabelPad *)oneObject;
		}
        
        for(UIView *v in cell.contentView.subviews)
        {
            if(v.tag == 666)            //this tag is in MixedFontLabeler for all labels added
            {
                [v removeFromSuperview];
            }
            else
            {
                v.hidden = NO;
            }
        }
		
		cell.ivBackground.hidden = YES;
		[cell.btn1 setHidden:YES];
		[cell.btn2 setHidden:YES];
		[cell.labelValue1 setHidden:YES];
		[cell.labelValue2 setHidden:YES];
		[cell.labelValue3 setHidden:YES];
		[cell.labelValue4 setHidden:YES];
		[cell.labelValue5 setHidden:YES];
		[cell.labelValue6 setHidden:YES];
		
		[cell.lv1 setHidden:YES];
		[cell.lv2 setHidden:YES];
		[cell.lv3 setHidden:YES];
		[cell.lv4 setHidden:YES];
		[cell.lv5 setHidden:YES];
		[cell.lv6 setHidden:YES];
		
		[cell.labelLabel setHidden:YES];
		[cell.labelValue setHidden:YES];
		[cell.labelVendor setHidden:YES];
        [cell.lblShadow setHidden:YES];
		[cell.btn1 setHidden:YES];
        [cell.btnCancel setHidden:YES];
		
		cell.labelLabel.text = segRow.rowLabel;
		cell.labelValue.text = segRow.rowValue;
		
		int tag = 767;
		for (UIImageView *iView in [cell.contentView subviews])
		{
			if (iView.tag == tag) 
				[iView removeFromSuperview];
		}

		if([aSegs isKindOfClass:[NSMutableArray class]])
		{
            if ([aSegs count] == 1)
            {
                SegmentRow *segRow2 = aSegs[0];
				UIColor *color = segRow2.color;
				float wOverride = 0;
                if([ExSystem isLandscape])
                {
                    wOverride = 670.0;
                    if (segRow2.showDisclosure == YES)
                        wOverride -= 15;    //account in disclosure_indicator icon
                }
                else
                {
                    wOverride = 490;
                    if (segRow2.showDisclosure == YES)
                        wOverride -= 15;    //account in disclosure_indicator icon
                }
                [self adjustLabel:cell.labelValue1 LabelValue:cell.lv1 HeadingText:segRow2.rowLabel ValueText:segRow2.rowValue ValueColor:color W:wOverride];
            }
            else if([aSegs count] > 1)
            {
                [self.view addSubview:cell];

                // Prepare MixedLabeler
                float fontSize = 15.0;
                UIFont * regularFont = [UIFont fontWithName:@"Helvetica" size:fontSize];
                UIFont * boldFont = [UIFont fontWithName:@"Helvetica-Bold" size:fontSize];
                MixedFontLabeler *labeler = [MixedFontLabeler mixedFontLabelerWithRegularFont:regularFont boldFont:boldFont]; // spacing = pixels between labels
                NSMutableArray *valueArray = [[NSMutableArray alloc] initWithCapacity:6];           // 6 values for one row to the most
                [valueArray addObject:@(cell.labelValue1.frame.origin.x)];
                
                for (int i = 0; i < [aSegs count]; i++)
                {
                    SegmentRow *aSegment = aSegs[i];
                    NSString *val = [NSString stringWithFormat:@"%@: ", aSegment.rowLabel];
                    [valueArray addObject:[MixedFontLabel labelWithText:val bold:YES]];
                    [valueArray addObject:[MixedFontLabel labelWithText:aSegment.rowValue bold:NO]];
                    [valueArray addObject:@8.0f];
                }
                
                [labeler addLabels:valueArray toView:cell.contentView yPos:3];
            }
        }
		else if(!segRow.isVendorRow) {
			[cell.labelLabel setHidden:NO];
			[cell.labelValue setHidden:NO];
		}

		cell.labelValue1.textColor = [UIColor blackColor];
		cell.ivIcon.image = nil;
        
		if(segRow.isFlightStats)
		{
			[cell.ivIcon setHidden:NO];
			if([TripData isFlightDelayedOrCancelled:segment.relFlightStats])
				cell.ivIcon.image = [UIImage imageNamed:@"flightStatsDotRed"];
			else 
				cell.ivIcon.image = [UIImage imageNamed:@"flightStatsDotGreen"];
			float x = 0;
            if(cell.lv1 != nil)
               x = cell.lv1.frame.origin.x; //cell.lv1.frame.origin
            if(cell.ivIcon != nil)
                cell.ivIcon.frame = CGRectMake(x, 6, 20, 20);
            if(cell.lv1 != nil)
                cell.lv1.frame = CGRectMake(x + 25, cell.lv1.frame.origin.y, cell.lv1.frame.size.width, cell.lv1.frame.size.height);

			[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
		}
		else if(segRow.isWeb || segRow.isMap || segRow.isApp || segRow.isSeat || segRow.isFlightStats ||segRow.isFlightSchedule || (segRow.showDisclosure && segRow.isDescription))
		{
			[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
			if(segRow.isEmblazoned)
				cell.labelValue1.textColor = [UIColor redColor];
		}
        else if(segRow.isCancel)
        {
            [cell.btnCancel setHidden:NO];
            [cell.btnCancel setTitle:segRow.rowLabel forState:UIControlStateNormal];
            // Set this label to segment key so it can be used when cancelling
            cell.labelValue1.text = segRow.segment.idKey;
            // Hide the label
            cell.labelValue1.hidden = YES;
            [cell setSelectionStyle:UITableViewCellEditingStyleNone];
            [cell.btnCancel addTarget:self action:@selector(btnCancelPressed:) forControlEvents:UIControlEventTouchUpInside];
        }
		else if(segRow.isEmblazoned)
		{
			cell.labelValue1.textColor = [UIColor redColor];
			[cell setAccessoryType:UITableViewCellAccessoryNone];
		}
		else {
			cell.labelValue1.textColor = [UIColor blackColor];
			[cell setAccessoryType:UITableViewCellAccessoryNone];
		}
		
		if(segRow.isVendorRow)
		{
			UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:segment.vendor VendorType:segRow.vendorType RespondToIV:cell.imgView];
			cell.imgView.image = gotImg;
			cell.imgView.hidden = NO;
			cell.ivIcon.hidden = YES;
			cell.ivBackground.hidden = NO;
			cell.labelLabel.hidden = YES;
			cell.labelValue.hidden = YES;
			cell.labelValue1.hidden = YES;
			cell.lv1.hidden = YES;
			
			if([segment.type isEqualToString:@"DINING"])
				cell.labelVendor.text = segment.segmentName;
            else if([segment.type isEqualToString:@"HOTEL"] && segment.segmentName != nil)
            {
                NSString *vendorName;
                if (segment.segmentName != nil)
                    vendorName = segment.segmentName;
                else if (segment.vendorName != nil)
                    vendorName = segment.vendorName;
                else 
                    vendorName = segment.vendor;
                cell.labelVendor.text = vendorName;
            }
			else 
				cell.labelVendor.text = segment.vendorName;
            
			cell.labelVendor.hidden = NO;
			float x = 2;
			
			float imgHW = 24;
			float y = (32 - imgHW) / 2;
			tag = 767;
			
			if([segment.type isEqualToString:@"CAR"])
			{
				CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
				UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
				[imgBack setImage:[UIImage imageNamed:@"icon_ipad_car"]];
				[cell.contentView addSubview:imgBack];
				imgBack.tag = tag;
			}
            else if([segment.type isEqualToString:@"AIR"])
			{
				CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
				UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
				[imgBack setImage:[UIImage imageNamed:@"icon_ipad_air"]];
				[cell.contentView addSubview:imgBack];
				imgBack.tag = tag;
			}
			else if([segment.type isEqualToString:@"RIDE"])
			{
				CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
				UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
				[imgBack setImage:[UIImage imageNamed:@"icon_ipad_taxi"]];
				[cell.contentView addSubview:imgBack];
				imgBack.tag = tag;
			}
			else if([segment.type isEqualToString:@"RAIL"])
			{
				CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
				UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
				[imgBack setImage:[UIImage imageNamed:@"icon_ipad_rail"]];
				[cell.contentView addSubview:imgBack];
				imgBack.tag = tag;
			}
			else if([segment.type isEqualToString:@"DINING"])
			{
				CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
				UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
				[imgBack setImage:[UIImage imageNamed:@"dining_24X24_PNG"]];
				[cell.contentView addSubview:imgBack];
				imgBack.tag = tag;
			}
			else if([segment.type isEqualToString:@"HOTEL"])
			{
				CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
				UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
				[imgBack setImage:[UIImage imageNamed:@"icon_ipad_hotel"]];
				[cell.contentView addSubview:imgBack];
				imgBack.tag = tag;
			}
			else if([segment.type isEqualToString:@"PARKING"])
			{
				CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
				UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
				[imgBack setImage:[UIImage imageNamed:@"icon_ipad_parking"]];
				[cell.contentView addSubview:imgBack];
				imgBack.tag = tag;
			}
		}
		else 
		{
			if(segRow.isMap || segRow.isWeb || segRow.isPhone || segRow.isApp || segRow.isFlightStats)
			{
				//cell.labelLabel.hidden = YES;
				cell.ivIcon.hidden = NO;
			}
			else 
			{
				//cell.labelLabel.hidden = NO;
				cell.ivIcon.hidden = YES;
			}
			//cell.labelValue.hidden = NO;
			cell.labelVendor.hidden = YES;
			cell.imgView.hidden = YES;
		}
		
        cell.segmentType = segment.type;
		return cell;
	}
	
    return nil;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{	

	NSUInteger row = [indexPath row];
	NSUInteger section = [indexPath section];
	NSString *key = keys[section];
	
	NSMutableArray *sectionData = nil;
    if (isOffersHidden)
        sectionData = filteredSegments[key];
    else 
        sectionData = dictSegRows[key];
    
	NSMutableArray *aSegs = sectionData[row];
	
	SegmentRow *segRow = nil;
	NSObject *o = sectionData[row];
	
	if([o isKindOfClass:[SegmentRow class]] || [o isKindOfClass:[EntityOffer class]])
		segRow = sectionData[row];
	else {
		segRow = aSegs[0];
	}
	
    if([aSegs isKindOfClass:[EntityOffer class]])
		return 40;
//	else if( (![aSegs isKindOfClass:[SegmentRow class]]) && ([aSegs count] > 2) && ([aSegs count] < 5))
//		return 56;
//	else if((![aSegs isKindOfClass:[SegmentRow class]]) && ([aSegs count] >= 5) && ([aSegs count] < 7))
//		return 80;
//	else if((![aSegs isKindOfClass:[SegmentRow class]]) && ([aSegs count] >= 7) && ([aSegs count] < 9))
//		return 80;
	else {

		EntitySegment *segment = segRow.segment;
		
		if(segRow.isSep)
		{
			return 5;
		}
		else if(segRow.isAirVendor)
		{
			if (segment.operatedBy != nil) 
				return 44;
			else 
				return 28;
		}
		else if(segRow.isSpecialCell && [segRow.specialCellType isEqualToString:@"PARKING"])
		{
			int h = 80;
			if(segment.relStartLocation.address == nil)
				h = h - 35;
			
			if(segment.phoneNumber == nil)
				h = h - 20;
			
			return h;
		}
		else if(segRow.isSpecialCell && [segRow.specialCellType isEqualToString:@"AIR"])
		{
			return 188;
		}
		else if(segRow.isSpecialCell && [segRow.specialCellType isEqualToString:@"HOTEL"])
		{
			return 60;
		}
		else if(segRow.isSpecialCell && [segRow.specialCellType isEqualToString:@"RAIL"])
		{
			return 188;
		}
		else if (row == 0 && section == 0 && [segment.type isEqualToString:@"PARKING"])
		{
			return 95;
		}
		else if (segRow.isSpecialCell && [segment.type isEqualToString:@"CAR"])
		{
			return 60;
		}
		else if (segRow.isSpecialCell && [segment.type isEqualToString:@"RIDE"])
		{
			return 95;
		}
		else if (row == 1 && section == 0 && [segment.type isEqualToString:@"DINING"])
		{
			return 95;
		}
		else if (row == 1 && section == 1 && [segment.type isEqualToString:@"EVENT"])
		{
			return 95;
		}
		else 
		{
			return 32;
		}

	}
}

#pragma mark -
#pragma mark Table Header Overrides
- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
	NSString *key = keys[section];
    // Fit landscape size because the label will not overlay on top of the superview.
	float w = 700.0; //tableDetails.bounds.size.width;
//	if([ExSystem isLandscape])
//		w=700.0;
	float h = 44.0;
	
	UIView* customView = [[UIView alloc] initWithFrame:CGRectMake(0.0, 0.0, w, h)];
	[customView setBackgroundColor:[UIColor colorWithRed:218/255.f green:225/255.f blue:235/255.f alpha:1.f]];
	CGRect lblRect =  CGRectMake(15, 0, w, h - 4);
	
	UILabel *titleLabel = [[UILabel alloc] initWithFrame:lblRect];
	[titleLabel setText:key];
	[titleLabel setBackgroundColor:[UIColor clearColor]];
	[titleLabel setTextAlignment:NSTextAlignmentLeft];
	[titleLabel setFont:[UIFont fontWithName:@"HelveticaNeue-Bold" size:20]];
	[titleLabel setTextColor:[UIColor colorWithRed:10/255.f green:70/255.f blue:119/255.f alpha:1.f]];
	titleLabel.numberOfLines = 1;
	titleLabel.lineBreakMode = NSLineBreakByTruncatingTail;
	
	UILabel *lblLine = [[UILabel alloc] initWithFrame:CGRectMake(0, h - 4, w, 4)];
	[lblLine setBackgroundColor:[UIColor colorWithRed:10/255.f green:70/255.f blue:119/255.f alpha:1.f]];

	//not used
//	CGRect myImageRect = CGRectMake(0, 0, w, h);
//	UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
//	imgBack.image = [UIImage imageNamed:@"yellow_date_header"];
//	[imgBack setContentMode:UIViewContentModeScaleToFill];
//	[customView addSubview:imgBack];
	[customView addSubview:lblLine];
	[customView addSubview:titleLabel];
	
	
	return customView;
}

- (CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
	return 44.0;
}


//need to make sure that a click on header means nothing
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{

	NSUInteger row = [newIndexPath row];
	NSUInteger section = [newIndexPath section];
	NSString *key = keys[section];
	
	NSMutableArray *sectionData = nil;
    if (isOffersHidden)
        sectionData = filteredSegments[key];
    else 
        sectionData = dictSegRows[key];
    
	NSMutableArray *aSegs = sectionData[row];
	
	SegmentRow *segRow = nil;
	if([aSegs isKindOfClass:[NSMutableArray class]])
		segRow = aSegs[0];
	else 
		segRow = sectionData[row];
    EntitySegment *segment = nil;
    if (![aSegs isKindOfClass:[EntityOffer class]]) {
        segment = segRow.segment;
    }
	
	[self checkOffline];
	if ([aSegs isKindOfClass:[EntityOffer class]])
    {
        segment = (EntitySegment*)aSegs;
        EntityOffer *offer = (EntityOffer*)segment;
        NSString *offerAction = offer.offerAction;

        if([offerAction isEqualToString:@"MULTI_LINK"])
        {
           // TODO: Enable multilink offers
    //        if ([EntityOffer.links count] > 0) 
    //        {
    //            OfferMultiLinkVC *multiLinkVC = [[OfferMultiLinkVC alloc] initWithNibName:@"OfferMultiLinkVC" bundle:nil];
    //            multiLinkVC.title = offer.offerVendor;
    //           // multiLinkVC.links = offer.links;
    //            multiLinkVC.navigationController.title = offer.offerVendor;
    //            
    //            OfferCell *cell = (OfferCell*)[tableView cellForRowAtIndexPath:newIndexPath];
    //            multiLinkVC.icon = cell.ivIcon.image;
    //            
    //            UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:multiLinkVC];
    //            localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
    //            [localNavigationController setToolbarHidden:NO];
    //            
    //            localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
    //            localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
    //            
    //            [self presentViewController:localNavigationController animated:YES completion:nil];
    //            
    //        }
    //        else
    //        {
    //            [tableView deselectRowAtIndexPath:newIndexPath animated:YES];
    //        }
        }
        else if([offerAction isEqualToString:@"WEB_LINK"] || [offerAction isEqualToString:@"SITE_LINK"] )
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
    else if(segRow.showDisclosure == YES && segRow.isDescription)
    {
        //set violation detail
        ViolationDetailsVC *vc = [[ViolationDetailsVC alloc] initWithNibName:@"ViolationDetailsVC" bundle:nil];
        vc.violationText = segRow.rowValue;

        //set customized naviBar button
        UINavigationBar *naviBarObj = [[UINavigationBar alloc] initWithFrame:CGRectMake(0, 0, 70, 44)];
        naviBarObj.translucent = YES;
        UIBarButtonItem *btn = [[UIBarButtonItem alloc] initWithTitle:[@"Close" localize] style:UIBarButtonItemStyleBordered target:vc action:@selector(closeMe:)];
        UINavigationItem *navigItem = [[UINavigationItem alloc] init];
        navigItem.leftBarButtonItem = btn;
        naviBarObj.items = @[navigItem];
        
        //Load above info into navigation Controler
        UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:vc];
        [localNavigationController.view addSubview:naviBarObj];
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        [localNavigationController setNavigationBarHidden:NO];
        
        [self presentViewController:localNavigationController animated:YES completion:NULL];
    }
//    else if(segRow.isCancel && [segment.type isEqualToString:SEG_TYPE_HOTEL])
//	{
//		self.hotelCancelIndexPath = newIndexPath;
//		[self cancelHotelPressed];
//	}
//    else if(segRow.isCancel && [segment.type isEqualToString:SEG_TYPE_CAR])
//	{
//		self.hotelCancelIndexPath = newIndexPath;
//		[self cancelCarPressed];
//	}
//    else if (segRow.isCancel && [segment.type isEqualToString:SEG_TYPE_RAIL])
//    {
//        self.hotelCancelIndexPath = newIndexPath;
//        [self cancelAmtrakPressed];
//    }
	else if (segRow.isFlightStats)
	{
//        if([[ExSystem sharedInstance] hasRole:@"FlightTracker_User"])
            [self showFlightStats:segment];
	}
    else if (segRow.isFlightSchedule)
    {
        [self showFlightSchedule:segment];
    }
	else if(segRow.isApp)
	{
        if([[ExSystem sharedInstance] hasRole:@"FlightTracker_User"])
        {
            NSDictionary *dict = @{@"Type": @"Mobiata"};
            [Flurry logEvent:@"External App: Launch" withParameters:dict];

            BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:segRow.url]];
            
            if (didLaunch == NO) 
            {
                NSURL *appStoreUrl = [NSURL URLWithString:@"http://www.mobiata.com/flighttrack-app-concur"];  // Mobiata
                [[UIApplication sharedApplication] openURL:appStoreUrl];
            }
        }

	}
	else if(segRow.isWeb && segRow.isAirport)
	{
		
		if(![self onlineCheck])
			return;
		
        if([[ExSystem sharedInstance] hasRole:@"GateGuru_User"])
        {
            NSString *url = [NSString stringWithFormat:@"gateguru://airports/%@", segRow.iataCode];
            [AppsUtil launchGateGuruAppWithUrl:url];
        }
	}
	else if(segRow.isWeb)
	{
		if(![self onlineCheck])
			return;
		
		if(!segRow.isSeat)
			[self loadWebView:segRow.url WebViewTitle:[NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Web site for t"], segRow.viewTitle]];
		else 
			[self loadWebViewSeat:segRow.url WebViewTitle:[NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Web site for t"], segRow.viewTitle]];
	}
	else if(segRow.isMap)
	{
		if(![self onlineCheck])
			return;
        
        if ([segment.type isEqualToString:@"CAR"]) {
            NSString *addr = nil;
            // Location of picking up rental car
            if(segment.relStartLocation.address != nil && [segRow.rowLabel hasPrefix:[Localizer getLocalizedText:@"Pickup"]])
            {// @"%@ %@ %@ %@"
                addr = [SegmentData getMapAddress:segment.relStartLocation withLineBreaker:NO withDelimitor:NO];
            }
            else if(segment.relStartLocation.address == nil && segment.relStartLocation.cityCode != nil && [segRow.rowLabel hasPrefix:[Localizer getLocalizedText:@"Pickup"]])
            {
                NSMutableString *addr1 = [[NSMutableString alloc] initWithString:@""];
                [addr1 appendString:segment.relStartLocation.cityCode];

                if(segment.relStartLocation.airportState != nil)
                {
                    if([addr1 length] > 0)
                        [addr1 appendString:@" "];
                    [addr1 appendString:segment.relStartLocation.airportState];
                }

                if(segment.relStartLocation.airportCountryCode != nil)
                {
                    if([addr1 length] > 0)
                        [addr1 appendString:@" "];
                    [addr1 appendString:segment.relStartLocation.airportCountryCode];
                }
                addr = addr1;
            }

            if (addr != nil)
            {
                [self goSomeplace:addr VendorName:segment.vendorName VendorCode:segment.vendor];
                return;
            }
            
            // Location of returning rental car
            if(segment.relEndLocation.address != nil && [segRow.rowLabel hasPrefix:[Localizer getLocalizedText:@"Returning"]])
            {// @"%@ %@ %@ %@"
                addr = [SegmentData getMapAddress:segment.relEndLocation withLineBreaker:NO withDelimitor:NO];
            }
            else if(segment.relEndLocation.address == nil && segment.relEndLocation.cityCode != nil && [segRow.rowLabel hasPrefix:[Localizer getLocalizedText:@"Returning"]])
            {
                NSMutableString *addr1 = [[NSMutableString alloc] initWithString:@""];
                [addr1 appendString:segment.relEndLocation.cityCode];

                if(segment.relEndLocation.airportState != nil)
                {
                    if([addr1 length] > 0)
                        [addr1 appendString:@" "];
                    [addr1 appendString:segment.relEndLocation.airportState];
                }
                
                if(segment.relEndLocation.airportCountryCode != nil)
                {
                    if([addr1 length] > 0)
                        [addr1 appendString:@" "];
                    [addr1 appendString:segment.relEndLocation.airportCountryCode];
                }
                addr = addr1;
            }
            
            if (addr != nil)
            {
                [self goSomeplace:addr VendorName:segment.vendorName VendorCode:segment.vendor];
                return;
            }
        }
        else
        {
            NSString *mapAddress = [SegmentData getMapAddress:segment.relStartLocation withLineBreaker:NO withDelimitor:YES];
            //[NSString stringWithFormat:@"%@, %@, %@ %@", segment.startAddress, segment.startCity, segment.startState, segment.startPostalCode];
            if ([mapAddress length])
            {
                NSString *vendorName;
                if (segment.segmentName != nil)
                    vendorName = segment.segmentName;
                else if (segment.vendorName != nil)
                    vendorName = segment.vendorName;
                else
                    vendorName = segment.vendor;
                [self goSomeplace:mapAddress VendorName:vendorName VendorCode:segment.vendor];
            }
            else
            {
                mapAddress = [NSString stringWithFormat:@"(%@) %@\n%@, %@", segment.relStartLocation.cityCode, segment.relStartLocation.airportName, segment.relStartLocation.airportCity, segment.relStartLocation.airportState];
                [self goSomeplace:mapAddress VendorName:[NSString stringWithFormat:@"%@ %@", segment.vendorName, segment.segmentName == nil? segment.relStartLocation.airportName : segment.segmentName] VendorCode:segment.vendor];
            }
        }
	}

	return;
}

-(BOOL)onlineCheck
{
	if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Actions offline"] 
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return NO;
	}
	else 
		return YES;
}

#pragma mark Offers

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
        
        for (SegmentData* object in dictSegRows[key]) {
            if (![object isKindOfClass:[EntityOffer class]]) 
            {
                NSMutableArray *b = filteredSegments[key];
                [b addObject:object];
            }
        }
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
    }
}

-(void) hideOffers: (id)sender
{
    ([ExSystem sharedInstance].tripOffersDisplayPreference)[trip.tripKey] = @"NO";
    isOffersHidden = YES;
    if ([sender respondsToSelector:@selector(setTitle:)])
        [sender setTitle:[Localizer getLocalizedText:@"Show Offers"]];// forState:UIControlStateNormal];
    [self.rightTableView reloadData];
}

-(void)showOffers: (id)sender
{
    ([ExSystem sharedInstance].tripOffersDisplayPreference)[trip.tripKey] = @"YES";
    isOffersHidden = NO;
    if ([sender respondsToSelector:@selector(setTitle:)])
        [sender setTitle:[Localizer getLocalizedText:@"Hide Offers"]];// forState:UIControlStateNormal];
    [self.rightTableView reloadData];
}

-(void)showHideOffers:(id)sender
{
    (!isOffersHidden)?[self hideOffers:sender]:[self showOffers:sender];
}

-(BOOL)isOfferValid:(EntityOffer*)offer
{
    if ([[ExSystem sharedInstance].offersValidityChecking isEqualToString:@"NO"] || ([OfferManager hasValidTimeRange:offer ] && [OfferManager hasValidProximity:offer]))
    {
        self.hasValidOffers = TRUE;
        return TRUE;
    }
    
    return FALSE;

}

- (void)addOfferEntriesInternal:(NSString *)segKey ma:(NSMutableArray *)ma seg:(EntitySegment *)seg
{
#pragma warn support offers
    NSString *segKeyStart = @"Start";
    NSString *segKeyDuration = @"Duration";
    NSString *segKeyEnd = @"End";
    
    NSUInteger index = [ma indexOfObject:seg];
   
    if (index == NSNotFound)
        return;

   
    
    NSArray *arrOffers = [[OfferManager sharedInstance] fetchOffersBySegIdKeyAndSegmentSide:segKey segmentSide:segKeyStart ];//(NSMutableArray*)[self.offers objectForKey:segKeyStart];
    
    for (EntityOffer *offer in arrOffers)
    {
        if ([self isOfferValid:offer]) 
        {

            [ma insertObject:offer atIndex:index];
            index++;
        }
    }
    
    arrOffers = [[OfferManager sharedInstance] fetchOffersBySegIdKeyAndSegmentSide:segKey segmentSide:segKeyDuration]; //(NSMutableArray*)[self.offers objectForKey:segKeyDuration];
    for (EntityOffer *offer in arrOffers) {
        if ([self isOfferValid:offer]) 
        {

            [ma insertObject:offer atIndex:index];
            index++;
        }
    }
    

    index++;
    
    arrOffers = [[OfferManager sharedInstance] fetchOffersBySegIdKeyAndSegmentSide:segKey segmentSide:segKeyEnd]; // (NSMutableArray*)[self.offers objectForKey:segKeyEnd];
    for (EntityOffer *offer in arrOffers) {
        if ([self isOfferValid:offer]) 
        {

            [ma insertObject:offer atIndex:index];

            index++;
        }
    }
}

#pragma mark -
#pragma mark Trip Formatting
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


-(void)reloadHotelImages
{
	float w = 100; //cell.scroller.view.frame.size.width;
	float h = 90; //cell.scroller.view.frame.size.height;
	
	if(hotelImagesArray != nil && [hotelImagesArray count] > 0)
	{
		for(UIView *v in hotelCell.scroller.subviews)
			[v removeFromSuperview];
		
		hotelCell.imageArray = hotelImagesArray;
		UIImageView	*iv = hotelImagesArray[0];
		iv.frame = CGRectMake(0, 0, w, h);
		iv.contentMode = UIViewContentModeCenter;
		iv.image = [self scaleImageToFit:iv.image MaxW:w MaxH:h];
		[iv setBackgroundColor:[UIColor whiteColor]];
		[hotelCell.scroller addSubview:iv];
		
		UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
		[btn addTarget:self action:@selector(showHotelImages:) forControlEvents:UIControlEventTouchUpInside];
		btn.frame = CGRectMake(0, 0, w, h);
		[hotelCell.scroller addSubview:btn];
		
	}

}

- (void)getOrderedSegments:(EntityTrip *)currTrip
{
	NSMutableDictionary *segDictByDate = [[NSMutableDictionary alloc] init];
	NSMutableArray *holdKeys = [[NSMutableArray alloc] init];
#pragma warn offers	
//    self.offers = currTrip.offers;
    
	for(EntityBooking *bd in currTrip.relBooking)
	{
		for(EntitySegment *seg in bd.relSegment)
		{
			if (seg.relStartLocation.dateLocal == nil)
			{
				seg.relStartLocation.dateLocal = @"1900-01-01 01:01";
			}
			
			NSString *formedDate = [DateTimeFormatter formatDateForTravel:seg.relStartLocation.dateLocal];
			if (segDictByDate[formedDate] == nil ) 
			{
				NSMutableArray *ma = [[NSMutableArray alloc] initWithObjects:seg, nil];
				segDictByDate[formedDate] = ma;
				[holdKeys addObject:seg.relStartLocation.dateLocal];
			}
			else
			{
				NSMutableArray *ma = segDictByDate[formedDate];
				[ma addObject:seg];
			}
		}
	}
	
	//now sort inside each day
	for(NSString *segDate in segDictByDate)
	{
		NSMutableArray *ma = segDictByDate[segDate];
		NSSortDescriptor *descriptor = [[NSSortDescriptor alloc] initWithKey:@"relStartLocation.dateLocal" ascending:YES];
		[ma sortUsingDescriptors:[NSMutableArray arrayWithObjects:descriptor,nil]];
		
		NSMutableArray *a = [[NSMutableArray alloc] initWithArray:ma];
		int iCount = 0;
		for(int i = 0; i < [a count]; i++)
		{
			EntitySegment *s = a[i];
			if([s.type isEqualToString:@"HOTEL"])
			{
				//shove to the back
				[ma removeObjectAtIndex:i - iCount];
				[ma addObject:s];
				iCount++;
				//break;
			}
		}
	}
	
	//shove the date back into the header slot
	for(NSString *segDate in segDictByDate)
	{
		NSMutableArray *ma = segDictByDate[segDate];
		[ma insertObject:segDate atIndex:0];
	}
    
	NSArray *sortedKeys = [holdKeys sortedArrayUsingSelector:@selector(caseInsensitiveCompare:)];
	
	holdKeys = [[NSMutableArray alloc] init];
	int cnt = [sortedKeys count];
	for (int x = 0; x < cnt; x++)
	{
		NSString *sortedDate = sortedKeys[x]; 
		[holdKeys addObject:[DateTimeFormatter formatDateForTravel:sortedDate]];
	}
	

    for(NSString *key in holdKeys)
	{
		NSMutableArray *segs = segDictByDate[key];
        NSMutableArray *ma = [[NSMutableArray alloc] initWithArray:segs];
        
        for(NSObject *obj in segs)
        {
           
            if([obj isKindOfClass:[EntitySegment class]])
            {
            
                EntitySegment *seg = (EntitySegment *)obj;
                
                [self addOfferEntriesInternal: seg.idKey ma: ma seg: seg];
            }
        }
        segDictByDate[key] = ma;
    }

    
	self.tripBits = segDictByDate;
	self.keys = holdKeys;
	SegmentStuff *segStuff = [[SegmentStuff alloc] init];
	//Let's dump out all of the segments, with their rows, into an array that is then used to populate the table for an itin...
	
	dictSegRows = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	for(NSString *key in keys)
	{
		NSMutableArray *segs = tripBits[key];
		
		NSMutableArray *segmentDayRows = [[NSMutableArray alloc] initWithObjects:nil];
		int iPos = 0;
		for(EntitySegment *seg in segs)
		{
			if(iPos > 0)
			{
				if([segmentDayRows count] > 0)
				{
					//we have some, let's make a sep row
					SegmentRow *sr = [[SegmentRow alloc] init];
					sr.isSep = YES;
					[segmentDayRows addObject:sr];
				}
				
				NSMutableArray *segmentRows = nil;
				// Offers are added as offer objects
                if([seg isKindOfClass:[EntityOffer class]])
                {
                    if (segmentRows == nil) {
                        segmentRows = [[NSMutableArray alloc] initWithObjects:seg, nil];
                    }
                    else {
                        [segmentRows addObject:seg];
                    }
                }
                else if([seg.type isEqualToString:@"AIR"])
					segmentRows = [segStuff fillAirSections:seg];
				else if([seg.type isEqualToString:@"HOTEL"])
					segmentRows = [segStuff fillHotelSections:seg];
				else if([seg.type isEqualToString:@"CAR"])
					segmentRows = [segStuff fillCarSections:seg];
				else if([seg.type isEqualToString:@"RIDE"])
					segmentRows = [segStuff fillRideSections:seg];
				else if([seg.type isEqualToString:@"DINING"])
					segmentRows = [segStuff fillDiningSections:seg];
				else if([seg.type isEqualToString:@"RAIL"])
					segmentRows = [segStuff fillRailSections:seg];
				                     
				for(SegmentRow *segRow in segmentRows)
					[segmentDayRows addObject:segRow];
			}
			iPos++;
		}
		dictSegRows[key] = segmentDayRows;
	}
    
	
	[self fillTripDetails:currTrip];
}

-(void) fillTripDetails:(EntityTrip *)currTrip
{	
	NSMutableString *details = [[NSMutableString alloc] initWithString:@"\n"];
	
	if(currTrip.itinSourceName != nil)
		[details appendString:[NSString stringWithFormat:@"Itin Source: %@\n", currTrip.itinSourceName]];
	
	NSString *cac = [TripData getCompanyAccountingCodes:currTrip];
	if([cac length] > 0)
		[details appendString:[NSString stringWithFormat:@"%@: %@\n", [Localizer getLocalizedText:@"Accounting Code"], cac]];
	
	NSString *rl = [TripData getRecordLocators:currTrip];
	if([rl length] > 0)
		[details appendString:[NSString stringWithFormat:@"%@: %@\n", [Localizer getLocalizedText:@"Trip Locator"], rl]];
	
	NSString *bTypes = [TripData getTypes:currTrip];
	if([bTypes length] > 0)
		[details appendString:[NSString stringWithFormat:@"%@: %@\n", [Localizer getLocalizedText:@"Type"], bTypes]];
	
	NSString *val = details;
	int w = lblTripDetails.frame.size.width;
	float y = lblTripDates.frame.origin.y + lblTripDates.frame.size.height + 30;

	CGFloat height =  [FormatUtils getTextFieldHeight:w Text:val FontSize:14.0f];

	lblTripDetails.frame = CGRectMake(lblTripDetails.frame.origin.x, y, w, height);
	
	lblTripDetails.text = details;
}

-(void) fillTripSegmentIcons:(EntityTrip *) t
{
	int iImagePos = 0;
	int startX = 55;
	int imageWidth = 26;
	int imgHW = 24;
	int y = lblTripDates.frame.origin.y + lblTripDates.frame.size.height;
	int tag = 967;
	float x = 55.0;
	
	if([ExSystem isLandscape])
	{
		//x = 80;
		startX = 80;
	}
	
	for (UIImageView *iView in [self.view subviews]) 
	{
		if (iView.tag == tag) 
			[iView removeFromSuperview];
	}
	
	if([t.hasAir boolValue] == YES)
	{
		x = startX + (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		//[imagBack setMode:
		[imgBack setImage:[UIImage imageNamed:@"icon_ipad_air"]];
		imgBack.tag = tag;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if([t.hasHotel boolValue] == YES)
	{
		x = startX + (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"icon_ipad_hotel"]];
		imgBack.tag = tag;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if([t.hasParking boolValue] == YES)
	{
		x = startX + (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"icon_ipad_parking"]];
		imgBack.tag = tag;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if([t.hasEvent boolValue] == YES)
	{
		x = startX + (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		imgBack.tag = tag;
		[imgBack setImage:[UIImage imageNamed:@"24_event.png"]];
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if([t.hasDining boolValue] == YES)
	{
		x = startX + (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		imgBack.tag = tag;
		[imgBack setImage:[UIImage imageNamed:@"dining_24X24_PNG.png"]];
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if([t.hasRail boolValue] == YES)
	{
		x = startX + (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"icon_ipad_rail"]];
		imgBack.tag = tag;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if([t.hasRide boolValue] == YES)
	{
		x = startX + (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"icon_ipad_taxi"]];
		imgBack.tag = tag;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if([t.hasCar boolValue] == YES)
	{
		x = startX + (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"icon_ipad_car"]];
		[self.view addSubview:imgBack];
		imgBack.tag = tag;
	}
}	

-(void)goSomeplace:(NSString *)mapAddress VendorName:(NSString *)vendorName VendorCode:(NSString *)vendorCode
{
	MapViewController *mapView = [[MapViewController alloc] init];
	mapView.mapAddress = mapAddress;
	mapView.anoTitle = vendorName;
	mapView.anoSubTitle = mapAddress;
	
    UINavigationController  *navCon = [[UINavigationController alloc] initWithRootViewController:mapView];
    navCon.modalPresentationStyle =  UIModalPresentationFormSheet;
    [self presentViewController:navCon animated:YES completion:NULL];
    
}


-(void)callNumber:(NSString *)phoneNum
{
	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", phoneNum]]];
}


-(IBAction)loadWebView:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle
{
	//do web view
	WebViewController *webView = [[WebViewController alloc] init];
	webView.url = [NSString stringWithFormat:@"http://%@", specialValueWeb];
	webView.viewTitle = webViewTitle;
    UIBarButtonItem *btn = [[UIBarButtonItem alloc] initWithTitle:[@"Close" localize] style:UIBarButtonItemStyleBordered target:webView action:@selector(closeMe:)];
    webView.navigationItem.leftBarButtonItem = btn;
    
    UINavigationController  *navCon = [[UINavigationController alloc] initWithRootViewController:webView];
    navCon.modalPresentationStyle =  UIModalPresentationFormSheet;
	[iPadHome presentViewController:navCon animated:YES completion:NULL];
    
    webView.myToolBar.hidden = YES;
    webView.labelTitle.hidden = YES;
    webView.webView.frame = CGRectMake(0, 0, 540, 620);

}

-(IBAction)loadWebViewSeat:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle
{
	WebViewController *webView = [[WebViewController alloc] init];
	webView.url = specialValueWeb;
	webView.viewTitle = webViewTitle;
    
    UIBarButtonItem *btn = [[UIBarButtonItem alloc] initWithTitle:[@"Close" localize] style:UIBarButtonItemStyleBordered target:webView action:@selector(closeMe:)];
    webView.navigationItem.leftBarButtonItem = btn;
    webView.title = webViewTitle;

    UINavigationController  *navCon = [[UINavigationController alloc] initWithRootViewController:webView];
    navCon.modalPresentationStyle =  UIModalPresentationFormSheet;
	[iPadHome presentViewController:navCon animated:YES completion:NULL];
    
    webView.myToolBar.hidden = YES;
    webView.labelTitle.hidden = YES;
    webView.webView.frame = CGRectMake(0, 0, 540, 620);

    
}


#pragma mark -
#pragma mark Image Stuff
-(void) fetchHotelImages:(NSMutableArray *) imageArray
{
	float w = 211; //cell.scroller.view.frame.size.width;
	float h = 243; //cell.scroller.view.frame.size.height;
	
	//rip through the array and clear out any duplicates that I can find, the images might actually be duplicates, but if they have a different name...
	NSMutableArray *imageURLArray = [[NSMutableArray alloc] initWithObjects:nil];
	for(int i = 0; i < [imageArray count]; i++)
	{
		HotelImageData *hid = imageArray[i];
		BOOL hasImage = NO;
		for (NSString *img in imageURLArray)
		{
			if([img isEqualToString:hid.hotelImage])
			{
				hasImage = YES;
				break;
			}
		}
		if(!hasImage)
			[imageURLArray addObject:hid.hotelImage];
	}
	
	int iPos = 0;
	
	if(hotelImagesArray == nil)
	{
		self.hotelImagesArray = [[NSMutableArray alloc] initWithObjects:nil];
		
		for(NSString *imageURL in imageURLArray)
		{
			UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(iPos * w, 0, w, h)];
			
			//do things to load the actual image
			UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
			[iv setImage:img];
			[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:imageURL RespondToImage:img IV:iv MVC:self];
			[hotelImagesArray addObject:iv];
			iPos++;
		}
		
		[self reloadHotelImages];
	}
	
}

-(IBAction) showHotelImages:(id)sender
{

	iPadImageViewerVC *vc = [[iPadImageViewerVC alloc] init];
	vc.imageArray = (NSMutableArray *)hotelImagesArray;
	vc.modalPresentationStyle = UIModalPresentationFormSheet;
	[self presentViewController:vc animated:YES completion:nil];
}

#pragma mark -
#pragma mark Image Handlers
-(void) fillImageURLs: (NSMutableArray*) imageURLs Section:(int) section Row:(int)row
{
	//we have the URLs, now get the images associated with those urls
	
	float w = 211; //cell.scroller.view.frame.size.width;
	float h = 243; //cell.scroller.view.frame.size.height;
	
	NSMutableArray *imageURLArray = [[NSMutableArray alloc] initWithObjects:nil];
	for(int i = 0; i < [imageURLs count]; i++)
	{
		//try to clear out any duplicate urls
		HotelImageData *hid = imageURLs[i];
		BOOL hasImage = NO;
		for (NSString *img in imageURLArray)
		{
			if([img isEqualToString:hid.hotelImage])
			{
				hasImage = YES;
				break;
			}
		}
		if(!hasImage)
			[imageURLArray addObject:hid.hotelImage];
	}
	
	dictHotelImageURLs[[NSString stringWithFormat:@"%d-%d", section, row]] = imageURLArray; //add to a dictionary all of the urls for this particular section-row
	// we will be passing off this array on the image press
	
	NSString *key = [NSString stringWithFormat:@"%d,%d,URLS", section, row];
	NSArray *a = [[NSArray alloc] initWithArray:imageURLs];
	dictHotelImages[key] = a;
	
	NSMutableArray *images = [[NSMutableArray alloc] initWithObjects:nil];
	key = [NSString stringWithFormat:@"%d,%d,IMAGES", section, row];
	int iPos = 0;
	for(NSString *imageURL in imageURLArray)
	{
		UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
		//do things to load the actual image
		UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
		[iv setImage:img];
		[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:imageURL RespondToImage:img IV:iv MVC:self]; //firing off the fetch, loads the image into the imageview
		if(iPos == 0)
			[images addObject:iv]; //this is the only iv that we want to keep around...
		iPos++;
	}
	dictHotelImages[key] = images; //the images array should only have one imageview tucked inside of it
	
	//need to see if the cell is visible and/or not dequed.  If so, I need to set the imagearray of that cell to the found images
	NSUInteger _path2[2] = {section, row};
	NSIndexPath *_indexPath2 = [[NSIndexPath alloc] initWithIndexes:_path2 length:2];
	
	UITableViewCell *cell = [tableDetails cellForRowAtIndexPath:_indexPath2];
	ItinDetailsHotelCarCellPad *cellHotel = (ItinDetailsHotelCarCellPad*)cell;

	if([cellHotel isKindOfClass:[ItinDetailsHotelCarCellPad class]])
		cellHotel.imageArray = imageURLArray; // images;

}


#pragma mark Label Adjustment
-(void) adjustLabel:(UILabel *) lblHeading LabelValue:(UILabel*) lblVal HeadingText:(NSString *) headText ValueText:(NSString *) valText ValueColor:(UIColor *) color W:(float)wOverride
{
	[lblHeading setHidden:NO];
	[lblVal setHidden:NO];
	
    NSString *val = @"";
    if ([headText length])
      	val = [NSString stringWithFormat:@"%@:", headText];
	lblHeading.text = val;// [NSString stringWithFormat:@"%@: %@", segRow2.rowLabel, segRow2.rowValue];
	
	CGSize lblSize = [val sizeWithFont:lblHeading.font];
	float x = lblHeading.frame.origin.x + lblSize.width + 5;

	float w = (lblHeading.frame.size.width - lblSize.width);
	if(wOverride > 0)
		w = wOverride - lblSize.width;
	lblVal.frame = CGRectMake(x, lblVal.frame.origin.y, w, lblVal.frame.size.height);
	lblVal.text = valText;
	
	if(color == nil)
		lblVal.textColor = [UIColor blackColor];
	else 
		lblVal.textColor = color;
}

#pragma mark -
#pragma Table cell hold operation used for cut/copy/paste edit menu
//MOB-9452 iPad copy paste function on confirmation #, vendor and hotel name
-(BOOL) tableView:(UITableView *)tableView shouldShowMenuForRowAtIndexPath:(NSIndexPath *)indexPath
{
    SegmentRow *segRow = [self getCopyEnabledSegRow:indexPath];
    if (segRow != nil)
    {
        if (segRow.isCopyEnable) {
            return YES;
        }
        else
            return NO;
    }
    else
        return NO;
}

-(BOOL) tableView:(UITableView *)tableView canPerformAction:(SEL)action forRowAtIndexPath:(NSIndexPath *)indexPath withSender:(id)sender
{
    return (action == @selector(copy:));
}

-(void) tableView:(UITableView *)tableView performAction:(SEL)action forRowAtIndexPath:(NSIndexPath *)indexPath withSender:(id)sender
{
    NSString *copyText = nil;
    UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
    
    SegmentRow *segRow = [self getCopyEnabledSegRow:indexPath];
    if (segRow != nil)
    {
        copyText = segRow.rowValue;
        [pasteboard setString:copyText];
    }
}

-(SegmentRow *) getCopyEnabledSegRow:(NSIndexPath *)indexPath
{
    NSUInteger selectedSection = [indexPath section];
    NSUInteger selectedRow = [indexPath row];
    NSString *key = keys[selectedSection];
    
    NSMutableArray *sectionData = nil;                                      //segment section divided by dates
    if (isOffersHidden)
        sectionData = filteredSegments[key];
    else
        sectionData = dictSegRows[key];
    
    NSMutableArray *aSegs = sectionData[selectedRow];        //segment row within a section
    
    SegmentRow *segRow = nil;                                               //one segment item with in a segment row
	if([aSegs isKindOfClass:[NSMutableArray class]])
    {
        NSUInteger count = 0;
        for(SegmentRow *tempSeg in aSegs)                                   //look for copyEnabled segment
        {
            if (tempSeg.isCopyEnable)
                segRow = aSegs[count];                       // same as segRow = tempSeg ??
            
            count ++;
        }
    }
    else
        segRow = sectionData[selectedRow];
    return segRow;
}

#pragma mark -
#pragma mark Book hotel and car from trip
#pragma mark -
#pragma mark UIActionSheetDelegate Methods

- (void)buttonBook:(NSInteger)buttonIndex 
{
	if(![self onlineCheck])
		return;
	
	//
	// First try to get booking location/dates from a flight segment
	//
	NSMutableDictionary* pBag = [TripData getHotelAndCarDefaultsFromFlightInTripSegments:tripBits withKeys:keys];
	
	if (buttonIndex == 0) // If we're booking a hotel
	{
		if ([pBag count] == 0)	// If we still dont' have booking location/dates, try to get them from a rail segment
			pBag = [TripData getHotelAndCarDefaultsFromRailInTripSegments:tripBits withKeys:keys];
		
		if ([pBag count] == 0)	// If we still dont' have booking location/dates, try to get them from a car segment
			pBag = [TripData getHotelAndCarDefaultsFromCarInTripSegments:tripBits withKeys:keys];
		
		if ([pBag count] == 0)	// If we still dont' have booking location/dates, try to get them from a hotel segment
			pBag = [TripData getHotelAndCarDefaultsFromHotelInTripSegments:tripBits withKeys:keys];
	} 
	else if (buttonIndex == 1) // If we're booking a car
	{
		if ([pBag count] == 0)	// If we still dont' have booking location/dates, try to get them from a rail segment
			pBag = [TripData getHotelAndCarDefaultsFromRailInTripSegments:tripBits withKeys:keys];
		
		if ([pBag count] == 0)	// If we still dont' have booking location/dates, try to get them from a hotel segment
			pBag = [TripData getHotelAndCarDefaultsFromHotelInTripSegments:tripBits withKeys:keys];
		
		if ([pBag count] == 0)	// If we still dont' have booking location/dates, try to get them from a car segment
			pBag = [TripData getHotelAndCarDefaultsFromCarInTripSegments:tripBits withKeys:keys];
	}
	
	pBag[@"SHORT_CIRCUIT"] = @"YES";
	
	// Add the trip key, client locator, and record locator
	pBag[@"TRIP_KEY"] = (trip.cliqbookTripId == nil ? @"" : trip.cliqbookTripId);
	pBag[@"CLIENT_LOCATOR"] = (trip.clientLocator == nil ? @"" : trip.clientLocator);
	
	NSString *recordLocator = @"";
	EntityBooking* primaryBooking = [TripData getPrimaryBooking:trip];
	if (primaryBooking != nil && primaryBooking.recordLocator != nil)
		recordLocator = primaryBooking.recordLocator;
	pBag[@"RECORD_LOCATOR"] = recordLocator;
	
	if (buttonIndex == 0)
	{
        if(pickerPopOver != nil)
			[pickerPopOver dismissPopoverAnimated:YES];

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
            
            // Add a dummy root vc for the modal dialog, so that we can switch out the GovSelectTANumVC to land on booking criteria page
            FormViewControllerBase *fakeRootVC = [[FormViewControllerBase alloc] initWithNibName:@"EditFormView" bundle:nil];
            UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:fakeRootVC];
            localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
            [localNavigationController setToolbarHidden:NO];

            [self presentViewController:localNavigationController animated:YES completion:nil];

            // Pop up Per diem selecting
            [GovDutyLocationVC showDutyLocationVC:fakeRootVC withCompletion:@"Book Hotel" withFields:taFields withDelegate:nil withPerDiemRate:NO asRoot:YES];
        }
        else{
            HotelViewController *nextController = [[HotelViewController alloc] initWithNibName:@"HotelViewController" bundle:nil];
            nextController.hideCustomFields = YES;
            
            //nextController.parameterBag = pBag;
            Msg *msg = [[Msg alloc] init];
            msg.idKey = @"FAKE";
            msg.parameterBag = pBag;
            
            UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
            
            localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
            
            [localNavigationController setToolbarHidden:NO];
            
            [self presentViewController:localNavigationController animated:YES completion:nil];        
            [nextController respondToFoundData:msg];
        }
	}
	else if (buttonIndex == 1)
	{
		//[ConcurMobileAppDelegate switchToView:@"CAR" viewFrom:[self getViewIDKey] ParameterBag:pBag];
		CarViewController *nextController = [[CarViewController alloc] initWithNibName:@"CarViewController" bundle:nil];
        nextController.hideCustomFields = YES;
		//nextController.parameterBag = pBag;
		Msg *msg = [[Msg alloc] init];
		msg.idKey = @"FAKE";
		msg.parameterBag = pBag;
		
		if(pickerPopOver != nil)
			[pickerPopOver dismissPopoverAnimated:YES];
		
        if([Config isGov])
        {
            NSArray* taFields = [GovTAField makeEmptyTAFields];
            GovTAField* authFld = [GovTAField getAuthField:taFields];
            authFld.useExisting = YES;
            authFld.fieldValue = [@"Use Existing Authorization" localize];
            authFld.access = @"RO";
            GovTAField* perdiemFld = [GovTAField getPerDiemField:taFields];
            perdiemFld.useExisting = YES;
            perdiemFld.fieldValue = [@"Use Existing Duty Location" localize];
            nextController.taFields = [NSMutableArray arrayWithArray:taFields];
        }
        
		UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
		
		localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
		
		[localNavigationController setToolbarHidden:NO];
		
		[self presentViewController:localNavigationController animated:YES completion:nil];
		[nextController respondToFoundData:msg];
        
	}
}


#pragma mark -
#pragma mark Hotel Cancel
-(void) cancelHotelPressed
{
    UIAlertView *alert = nil;
    if ([Config isGov])
    {
        alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Please Confirm"]
                              message:[Localizer getLocalizedText:@"Select 'OK' if you are sure you want to cancel this hotel reservation."]
                              delegate:self
                              cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                              otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
    }
    else
    {
        alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Please Confirm"]
                              message:[Localizer getLocalizedText:@"Are you sure you want to cancel this hotel reservation?"]
                              delegate:self
                              cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                              otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
    }
	alert.tag = kAlertConfirmCancelHotel;
	[alert show];
}

-(void) cancelCarPressed
{
    UIAlertView *alert = nil;
    if ([Config isGov])
    {
        alert = [[MobileAlertView alloc]
                 initWithTitle:[Localizer getLocalizedText:@"Please Confirm"]
                 message:[Localizer getLocalizedText:@"Select 'OK' if you are sure you want to cancel this car reservation."]
                 delegate:self
                 cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                 otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
    }
    else
    {
        alert = [[MobileAlertView alloc]
                 initWithTitle:[Localizer getLocalizedText:@"Please Confirm"]
                 message:[Localizer getLocalizedText:@"Are you sure you want to cancel this car reservation?"]
                 delegate:self
                 cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                 otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
    }
	alert.tag = kAlertConfirmCancelCar;
	[alert show];
}

-(void) cancelAmtrakPressed
{
	UIAlertView *alert = [[MobileAlertView alloc]
						  initWithTitle:[Localizer getLocalizedText:@"Please Confirm"]
						  message:[Localizer getLocalizedText:@"Are you sure you want to cancel this rail reservation?"]
						  delegate:self
						  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
						  otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
	alert.tag = kAlertConfirmCancelAmtrak;
	[alert show];
}

-(void) cancelAmtrak
{
    if(![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Operation Not Supported Offline"]
							  delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                              otherButtonTitles:nil];
		[alert show];
    }
    else
    {
        NSMutableDictionary *pBag = [self makeCancellationParameterBag];
        
        pBag[@"Reason"] = [Localizer getLocalizedText:@"Rail cancelled from mobile device"];
        
        [[ExSystem sharedInstance].msgControl createMsg:AMTRAK_CANCEL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES Options:SILENT_ERROR RespondTo:self];
        
        [self showWaitView];
    }
}

-(void) cancelCar
{
    if(![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Operation Not Supported Offline"]
							  delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                              otherButtonTitles:nil];
		[alert show];
    }
    else
    {
        NSMutableDictionary *pBag = [self makeCancellationParameterBag];
        
        pBag[@"Reason"] = [Localizer getLocalizedText:@"Car cancelled from mobile device"];

        [[ExSystem sharedInstance].msgControl createMsg:CAR_CANCEL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        
        [self showWaitView];
    }
}


-(void) cancelHotel
{
    if(![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Operation Not Supported Offline"]
							  delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                              otherButtonTitles:nil];
		[alert show];
    }
    else
    {
        NSMutableDictionary *pBag = [self makeCancellationParameterBag];
        
        pBag[@"Reason"] = [Localizer getLocalizedText:@"Hotel cancelled from mobile device"];

        [[ExSystem sharedInstance].msgControl createMsg:HOTEL_CANCEL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        
        [self showWaitView];
    }
}


-(void) navigateAfterSuccessfulHotelCancellation
{
    NSMutableDictionary *segs = [TripData getSegmentsOrderByDate:trip];
    // MOB-11341 - log flurry event
    //Type:<Hotel, Car, Air, Train>, ItemsLeftInItin:<count>
    NSDictionary *dictionary = @{@"Type": @"Hotel", @"ItemsLeftInItin": [NSString stringWithFormat:@"%d", [segs count]]};
    [Flurry logEvent:@"Book: Cancel" withParameters:dictionary];
    
    //
    // Hotel data segment is not deleted yet. It will be deleted after call with "TRIPS_DATA"
    // So check for atleast 1 segment in case there is only Hotel booking
    if([segs count] > 1)
    {
        [self refreshData];
        [iPadHome refreshTripData];
    }
    else
    {
        [self navigateToHome];
    }
}


-(NSMutableDictionary*) makeCancellationParameterBag
{

	// Add the trip key, client locator, and record locator
	NSString *tripId = (trip.cliqbookTripId == nil ? @"" : trip.cliqbookTripId);
	NSString *recordLocator = @"";
	EntityBooking* primaryBooking = [TripData getPrimaryBooking:trip];
	if (primaryBooking != nil && primaryBooking.recordLocator != nil)
		recordLocator = primaryBooking.recordLocator;
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:tripId, @"TripId"
								 ,self.btnCancelSegKey, @"SegmentKey"
								 ,primaryBooking.bookSource, @"BookingSource"
								 ,recordLocator, @"RecordLocator"
								 , nil];
    return pBag;
}

#pragma mark -
#pragma mark Alert Methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if (alertView.tag == kAlertConfirmCancelHotel)
	{
		if (buttonIndex == 1)
			[self cancelHotel];
	}
    else 	if (alertView.tag == kAlertConfirmCancelCar)
	{
		if (buttonIndex == 1)
			[self cancelCar];
	}
    else if (alertView.tag == kAlertConfirmCancelAmtrak)
    {
		if (buttonIndex == 1)
			[self cancelAmtrak];
    }
    else if (alertView.tag == kAlertViewHotelCancelSuccessMessage)
    {
        [self navigateAfterSuccessfulHotelCancellation];
    }
}


#pragma mark -
#pragma mark Flight Stats
-(void) showFlightStats:(EntitySegment *)seg
{
	FlightStatsViewController *flightStatsVC = [[FlightStatsViewController alloc] initWithNibName:@"FlightStatsViewController" bundle:nil];
	flightStatsVC.tripKey = trip.tripKey;
	flightStatsVC.segmentKey = seg.idKey;

	UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:flightStatsVC];	
	navController.modalPresentationStyle = UIModalPresentationFormSheet;
	[navController setToolbarHidden:YES];
	[navController setNavigationBarHidden:YES];
	[self presentViewController:navController animated:YES completion:nil];
	
}

#pragma mark -
#pragma mark Flight Schedule
-(void) showFlightSchedule:(EntitySegment *)seg
{
    FlightScheduleVC *vc = [[FlightScheduleVC alloc] initWithNibName:@"FlightScheduleVC" bundle:nil];
    vc.segment = seg;

    UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:vc];
	navController.modalPresentationStyle = UIModalPresentationFormSheet;

    // The FlightScheduleVC does NOT have a standard navigation controller.  It's set in the xib for that view only. :/
	[navController setToolbarHidden:YES];
	[navController setNavigationBarHidden:YES];
	[self presentViewController:navController animated:YES completion:nil];
}


#pragma mark -
#pragma mark Managing the popover

- (void)showRootPopoverButtonItem:(UIBarButtonItem *)barButtonItem {
}

- (void)invalidateRootPopoverButtonItem:(UIBarButtonItem *)barButtonItem {
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
        [self getOrderedSegments:activeTrip];
        [self checkOfferDisplaySelection];
        [self makeFilteredSegments];
        [tableDetails reloadData];
    }
}

#pragma mark - Bar Methods
-(void) configureBars
{
    self.navigationController.navigationBar.hidden = NO;
    self.navigationController.toolbarHidden = NO;
    
    // Nav bar
    self.navigationItem.title = self.trip.tripName;

    UIBarButtonItem *btnTripsList = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"TRIPS"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonTripsPressed:)];
    self.navigationItem.rightBarButtonItem = btnTripsList;
    
    // Tool bar
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
    if (self.hasValidOffers)
    {
        NSString *offerTitle = (isOffersHidden)?[Localizer getLocalizedText:@"Show Offers"]:[Localizer getLocalizedText:@"Hide Offers"];
        UIBarButtonItem *btnToggleOffers = [[UIBarButtonItem alloc] initWithTitle:offerTitle style:UIBarButtonItemStyleBordered target:self action:@selector(showHideOffers:)];
        NSArray *toolbarItems = @[flexibleSpace, flexibleSpace, btnToggleOffers];
        [self setToolbarItems:toolbarItems];
    }
    else
    {
        [self setToolbarItems:@[]]; // Empty toolbar
    }
}


#pragma mark - Button Methods
- (void) makeButtonLabels
{
    NSMutableArray *descriptors = [NSMutableArray array];

    if ([[ExSystem sharedInstance] hasTravelBooking] && [trip.allowAddHotel boolValue])
    {
        [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_BOOK_HOTEL title:[Localizer getLocalizedText:@"Book Hotel"]]];
    }
    
    if ([[ExSystem sharedInstance] hasTravelBooking] && [trip.allowAddCar boolValue])
    {
        [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_BOOK_CAR title:[Localizer getLocalizedText:@"Book Car"]]];
    }
    
    [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_REFRESH_TRIP title:[Localizer getLocalizedText:@"Refresh Trip"]]];
    

    if ([Config isGov])
    {
        if (trip.cliqbookTripId != nil && trip.authNum != nil)
        {
            [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_VIEW_AUTH title:[Localizer getLocalizedText:@"View Authorization"]]];
        }
    }

    // chatter test
    if ([Config isSalesforceChatterEnabled]) {
        [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_CHATTER_POST title:[Localizer getLocalizedText:@"Post Trip"]]];
        [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_CHATTER_TRIP_FEED title:[Localizer getLocalizedText:@"Chatter"]]];
    }

    [self setButtonDescriptors:descriptors];
}

- (void) didPressButtonAtIndex:(int)buttonIndex withId:(NSString*)buttonId inRect:(CGRect)rect
{
    if ([buttonId isEqualToString:BUTTON_ID_BOOK_HOTEL])
        [self buttonBookHotelPressed:nil];
    if ([buttonId isEqualToString:BUTTON_ID_BOOK_CAR])
        [self buttonBookCarPressed:nil];
    if ([buttonId isEqualToString:BUTTON_ID_REFRESH_TRIP])
        [self buttonRefreshTripPressed:nil];
    if ([buttonId isEqualToString:BUTTON_ID_TOGGLE_OFFERS])
        [self showHideOffers:nil];
    if (([buttonId isEqualToString:BUTTON_ID_CHATTER_TRIP_FEED]))
        [self openChatterTripFeed];
    if (([buttonId isEqualToString:BUTTON_ID_CHATTER_POST]))
        [self postToChatter];

    if ([Config isGov])
    {
        if ([buttonId isEqualToString:BUTTON_ID_VIEW_AUTH])
            [self getNewAuthDocInfo];
    }
}

- (void)postToChatter
{
    ChatterTripPostViewController *view = [[ChatterTripPostViewController alloc] initWithTripDescription:[self tripDescription] recordLocator:self.activeTrip.recordLocator];
    [self openViewController:view];
}

// opens chatter feed
- (void)openChatterTripFeed
{
    ChatterPostLookup *lookup = [[ChatterPostLookup alloc] init];
    NSString *itemId = [lookup getPostItemIdForTrip:self.activeTrip.recordLocator];

    // we're kicking out to chatter right away now, I'm just keeping this around since chatter spec is in flux and I might need this again.
    if (itemId == nil) {
        [AppsUtil launchChatterApp];
        //ChatterTripPostViewController *view = [[ChatterTripPostViewController alloc] initWithTripDescription:[self tripDescription] recordLocator:self.activeTrip.recordLocator];
        //[self openViewController:view];
    } else {
        [AppsUtil launchChatterApp];
        //ChatterFeedViewController *view = [[ChatterFeedViewController alloc] initWithItemId:itemId];
        //[self openViewController:view];
    }
}

// trip description used in a chatter post.
- (NSString *)tripDescription
{
	NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
	// specify timezone
    [dateFormat setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormat setLocale:[NSLocale currentLocale]];

	[dateFormat setDateFormat: @"EEE MMM dd"];

	NSString *startFormatted = [dateFormat stringFromDate:trip.tripStartDateLocal];
	NSString *endFormatted = [dateFormat stringFromDate:trip.tripEndDateLocal];

	return [NSString stringWithFormat:@"%@. %@ - %@", trip.tripName, startFormatted, endFormatted];
}

- (void)openViewController:(UIViewController *)view
{
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:view];
    navi.modalPresentationStyle = UIModalPresentationFormSheet;
    [self presentViewController:navi animated:YES completion:nil];
}

#pragma mark - Header Methods
- (void) updateHeaderView
{
    TripPaneHeader_iPad* headerView = (TripPaneHeader_iPad*)self.leftPaneHeaderView;
    headerView.tripName.text = self.trip.tripName;
    NSDate *tripDate = self.trip.tripStartDateLocal;
    headerView.tripDate.text = [DateTimeFormatter formatDateMediumByDate:tripDate];
    
    if ([self.trip.recordLocator length]) {
        headerView.tripRecordLocator.text = [NSString stringWithFormat:[@"Agency Record Locator: %@" localize], self.trip.recordLocator];
        headerView.tripRecordLocator.hidden = NO;
    }
    if ([self.trip.travelPointsPosted length]) {
        headerView.tripTravelPoints.text = [NSString stringWithFormat:[@"Total Travel Points: %@" localize], self.trip.travelPointsPosted];
        headerView.tripTravelPoints.hidden = NO;
    }
    
    if (!headerView.tripRecordLocator.hidden && !headerView.tripTravelPoints.hidden) {
        headerView.tripName.frame = CGRectMake(headerView.tripName.frame.origin.x, 14.0, headerView.tripName.frame.size.width, headerView.tripName.frame.size.height);
        headerView.tripDate.frame = CGRectMake(headerView.tripDate.frame.origin.x, 67.0, headerView.tripDate.frame.size.width, headerView.tripDate.frame.size.height);
        headerView.tripRecordLocator.frame = CGRectMake(headerView.tripRecordLocator.frame.origin.x, 91.0, headerView.tripRecordLocator.frame.size.width, headerView.tripRecordLocator.frame.size.height);
    }
    
    if (keys != nil && keys.count > 0)
    {
        NSMutableDictionary *segmentTypes = [NSMutableDictionary dictionary];
        int ivPos = 0;
        
        for(NSString *key in keys)
        {
            NSMutableArray *segs = tripBits[key];
            for (NSObject *obj in segs)
            {
                if (![obj isKindOfClass:[EntitySegment class]])
                    continue; // This isn't a segment, so keep going
                
                EntitySegment* seg = (EntitySegment*)obj;
                if (seg.type == nil || segmentTypes[seg.type] != nil)
                    continue; // We've seen this type of segment before, so keep going
                
                segmentTypes[seg.type] = seg.type; // Remember that we've seen this type
                
                if (ivPos < (MAX_HEADER_ICONS - 1))
                {
                    NSString *segmentImageName = [self imageNameForSegmentType:seg.type];
                    if (segmentImageName != nil)
                    {
                        NSString *imageViewName = [NSString stringWithFormat:@"iv%i", ivPos];
                        UIImageView *imageView = (UIImageView*)[headerView valueForKey:imageViewName];
                        imageView.image = [UIImage imageNamed:segmentImageName];
                        ivPos++;
                    }
                }
            }
        }
    }
}

#pragma mark - Footer Methods
-(void) updateFooterView
{
}

#pragma mark - View Update Methods
- (void) updateViews
{
	[self getOrderedSegments:self.trip];
    [self checkOfferDisplaySelection];
    [self makeFilteredSegments];

    [self configureBars];
    [self updateHeaderView];
    [self updateFooterView];
    [self makeButtonLabels];
    [self.rightTableView reloadData];
    [self.view setNeedsLayout];
    
    if (self.loadingView != nil)
        [self hideLoadingView];
    self.loadingView.hidden = YES;
}

#pragma mark - Helper Methods
- (NSString*) imageNameForSegmentType:(NSString*)segmentType
{
    if([segmentType isEqualToString:@"CAR"])
    {
        return @"icon_ipad_car";
    }
    else if([segmentType isEqualToString:@"AIR"])
    {
        return @"icon_ipad_air";
    }
    else if([segmentType isEqualToString:@"RIDE"])
    {
        return @"icon_ipad_taxi";
    }
    else if([segmentType isEqualToString:@"RAIL"])
    {
        return @"icon_ipad_rail";
    }
    else if([segmentType isEqualToString:@"DINING"])
    {
        return @"dining_24X24_PNG";
    }
    else if([segmentType isEqualToString:@"HOTEL"])
    {
        return @"icon_ipad_hotel";
    }
    else if([segmentType isEqualToString:@"PARKING"])
    {
        return @"icon_ipad_parking";
    }
    return nil;
}

#pragma -mark Government Auth functions

-(void) getNewAuthDocInfo
{
    [self showWaitView];
    
    // Get doc info from TANum/Locator
    NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:trip.cliqbookTripId, @"TRIP_LOCATOR", trip.authNum, @"TA_NUM", nil];
    [[ExSystem sharedInstance].msgControl createMsg:GOV_DOC_INFO_FROM_TRIP_LOCATOR CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) showAuthView:(GovDocInfoFromTripLocatorData*) currentDocInfo
{
    if (currentDocInfo != nil)
    {
        NSString * docType = currentDocInfo.currentDocType;
        NSString * docName = currentDocInfo.currentDocName;
        if (docType != nil && docName != nil)
        {
            if([UIDevice isPad])
            {
                [GovDocDetailVC_iPad showDocDetailWithTraveler:currentDocInfo.travid withDocName:docName withDocType:docType];
            }
            else
            {
                [GovDocDetailVC showAuthFromRootWithDocName:docName withDocType:docType];
            }
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
        // MOB-18894 Change alert message.
        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Unable To Access"] message:[Localizer getLocalizedText:@"To view a stamped document use the Authorizations tab on the Home screen"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
        [av show];
    }
}
@end
