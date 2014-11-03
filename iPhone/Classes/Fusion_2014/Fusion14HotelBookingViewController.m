//
//  Fusion14HotelBookingViewController.m
//  ConcurMobile
//
//  Created by Sally Yan on 4/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "Fusion14HotelBookingViewController.h"
#import "Fusion14HotelBookingTableViewHeaderCell.h"
#import "Fusion14HotelBookingDetailsCell.h"
#import "Fusion14HotelBookingPriceCell.h"
#import "Fusion14HotelBookingCancellationCell.h"
#import "Fusion14HotelBookingPaymentDetailsCell.h"
#import "HotelBookingManager.h"
#import "HotelSearch.h"
#import "HotelSearchCriteria.h"
#import "EntityHotelBooking.h"
#import "EntityHotelRoom.h"
#import "ReserveHotel.h"
#import "EntityHotelImage.h"
#import "HotelReservationRequest.h"
#import "HotelReservationResponse.h"
#import "TravelViolationReasons.h"
#import "Fusion2014TripDetailsViewController.h"

#import "CreditCard.h"
#import "PreSellOptions.h"
#import "ViolationReason.h"
#import "WaitViewController.h"
#import "PostMsgInfo.h"

#import "FusionMockServer.h"
#import "ConcurMobileAppDelegate.h"

@interface Fusion14HotelBookingViewController ()

@property (nonatomic, strong) HotelSearch		*hotelSearch;
@property (nonatomic, strong) EntityHotelBooking      *hotelBooking;
@property (nonatomic, strong) EntityHotelRoom         *hotelBookingRoom;

@property BOOL isPreSellOptionsLoaded;
@property (nonatomic, strong) NSNumber			*creditCardIndex;
@property (nonatomic, strong) NSArray           *creditCards;
@property (nonatomic, strong) NSString          *cancellationPolicyText;

@property (nonatomic, strong) NSArray			*violationReasons;
@property (nonatomic, strong) NSArray			*violationReasonLabels;

@property (strong, nonatomic) IBOutlet UIView *uvSlideReserve;
@property (strong, nonatomic) IBOutlet UILabel *lblTotalCost;
@end

@implementation Fusion14HotelBookingViewController

- (id) initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    
    if (self) {
//        ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*)[[UIApplication sharedApplication] delegate];
//        self.managedObjectContext = appDelegate.managedObjectContext;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self setTableViewHeader];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    [self setupToolBar];
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:NO];
    self.title = @" ";
    
    [self.navigationController setToolbarHidden:NO];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)setupToolBar
{
    UISwipeGestureRecognizer *swipeGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(completeReservation)];
    [swipeGestureRecognizer setDirection:UISwipeGestureRecognizerDirectionRight];
    [self.uvSlideReserve addGestureRecognizer:swipeGestureRecognizer];
    
    int numOfNights = [self getNumberOfNightsWithCheckInDate:self.hotelSearch.hotelSearchCriteria.checkinDate checkOutDate:self.hotelSearch.hotelSearchCriteria.checkoutDate];
    self.lblTotalCost.text = [self getTotalPrice:numOfNights];
    
    UIBarButtonItem *leftPadding = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil];
    leftPadding.width = -16;
    
    [self setToolbarItems:@[leftPadding,[[UIBarButtonItem alloc] initWithCustomView:self.uvSlideReserve]]];
}

-(void)setTableViewHeader
{
    self.tableView.tableHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, self.tableView.frame.size.width, 200.0f)];
    Fusion14HotelBookingTableViewHeaderCell *tableHeaderCell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion14HotelBookingTableViewHeaderCell"];
    
    [self.tableView dequeueReusableHeaderFooterViewWithIdentifier:@"Fusion14HotelBookingTableViewHeaderCell"];
    [self.tableView.tableHeaderView addSubview:tableHeaderCell];
    
    [self configureHeaderCell:tableHeaderCell];
}

-(void)configureHeaderCell:(Fusion14HotelBookingTableViewHeaderCell*)headerCell
{
    // Uncomment this later -- disabled for Fusion14 demo
//    if ([self.hotelBooking.relHotelImage count] > 0)
//    {
//        for(EntityHotelImage *image in self.hotelBooking.relHotelImage)
//        {
//            UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
//            [[ExSystem sharedInstance].imageControl getImageAsynchWithUrl:image.imageURI RespondToImage:img IV:headerCell.imageViewHotelImage];
//            break;
//        }
//    }
    // Show a hardcoded image for Fusion14 demo.
    // Later iterate through images and show the image which fits the view nicely.
    double delayInSeconds = 0.5;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        // This does NOT fail the test cause it's on another thread.
        headerCell.imageViewHotelImage.image = [UIImage imageNamed:@"Fusion14_static_Hotel_Room"];
    });

}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 3;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (section == 0) {
        return 2;
    }
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    switch ([indexPath section]) {
        case 0:
        {
            if ([indexPath row] == 0) {
                Fusion14HotelBookingDetailsCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion14HotelBookingDetailsCell" forIndexPath:indexPath];
                NSString *checkinDate = [self getDateString:self.hotelSearch.hotelSearchCriteria.checkinDate];
                NSString *checkoutDate = [self getDateString:self.hotelSearch.hotelSearchCriteria.checkoutDate];
                
                if ([checkinDate length] && [checkoutDate length]) {
                    cell.labelCheckInOutDate.text = [NSString stringWithFormat:@"%@ - %@", checkinDate, checkoutDate];
                    int numOfNights = [self getNumberOfNightsWithCheckInDate:self.hotelSearch.hotelSearchCriteria.checkinDate checkOutDate:self.hotelSearch.hotelSearchCriteria.checkoutDate];
                    if (numOfNights > 1) {
                         cell.labelNumberOfNights.text = [NSString stringWithFormat:@"%i Nights", numOfNights];
                    } else {
                        cell.labelNumberOfNights.text = [NSString stringWithFormat:@"%i Night", numOfNights];
                    }
                }
                return cell;
            } else {
                Fusion14HotelBookingPriceCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion14HotelBookingPriceCell" forIndexPath:indexPath];
                int numOfNights = [self getNumberOfNightsWithCheckInDate:self.hotelSearch.hotelSearchCriteria.checkinDate checkOutDate:self.hotelSearch.hotelSearchCriteria.checkoutDate];
                cell.labelTotalPrice.text = [self getTotalPrice:numOfNights];
                return cell;
            }
        }
        case 1:
        {
            Fusion14HotelBookingCancellationCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion14HotelBookingCancellationCell" forIndexPath:indexPath];
            return cell;
        }
        case 2:
        {
            Fusion14HotelBookingPaymentDetailsCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion14HotelBookingPaymentDetailsCell" forIndexPath:indexPath];
            cell.lableCardNumber.text = @"Mike Hilton";
            return cell;
        }
        default:
            break;
    }
    return nil;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    switch ([indexPath section]) {
        case 0:{
            if ([indexPath row] == 0) {
                return 97.0f;
            } else {
                return 56.0f;
            }
        }
        case 1:{
            return 75.0f;
        }
        case 2:{
            return 45.0f;
        }
        case 3:{
            return 58.0f;
        }
        default:
            break;
    }
    return 0.0;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 13.0f;
}

- (UIView*)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    UIView *sectionHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, self.tableView.frame.size.width, 13.0f)];
    [sectionHeaderView setBackgroundColor:[UIColor colorWithRed:233.0/255.0 green:233.0/255.0 blue:233.0/255.0 alpha:1.0]];
    return sectionHeaderView;
}

-(NSInteger)getNumberOfNightsWithCheckInDate:(NSDate*)checkInDate checkOutDate:(NSDate*)checkOutDate
{
    NSCalendar *gregorianCalendar = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
    NSDateComponents *components = [gregorianCalendar components:NSDayCalendarUnit
                                                        fromDate:checkInDate
                                                          toDate:checkOutDate
                                                         options:0];
    return components.day;
}

-(NSString*)getDateString:(NSDate*)date
{
    NSString *dateStr = [DateTimeFormatter formatHotelOrCarDateForBooking:date];
    int index = [dateStr rangeOfString:@","].location;
    if (index > 0 && index < dateStr.length) {
        dateStr = [dateStr substringToIndex:index];
    }
    return dateStr;
}

-(NSString*)getTotalPrice:(NSInteger)numberOfNights
{
	return [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", [self.hotelBookingRoom.rate doubleValue]*numberOfNights] crnCode:self.hotelBookingRoom.crnCode];
}

-(NSString*)getRoomDescription
{
	return self.hotelBookingRoom.summary; // hotelSearch.selectedHotel.detail.selectedRoom.summary;
}

-(NSString*)getCreditCard
{
	if (self.creditCards == nil || self.creditCardIndex == nil || [self.creditCardIndex integerValue] >= [self.creditCards count])
	{
		return self.isPreSellOptionsLoaded ? [Localizer getLocalizedText:@"Unavailable"] : [@"Loading..." localize];
	}
	else
	{
		CreditCard* card = self.creditCards[[self.creditCardIndex integerValue]];
		return card.name;
	}
}

-(NSString*)getCancellationPolicy
{
	if (self.cancellationPolicyText == nil)
	{
		return self.isPreSellOptionsLoaded ? [Localizer getLocalizedText:@"Unavailable"] : [@"Loading..." localize];
	}
	else
	{
		return [Localizer getLocalizedText:@"Please Select To View"];
	}
}


#pragma mark - MWS responses
-(void)didProcessMessage:(Msg *)msg
{
    if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		if ((msg.parameterBag)[@"HOTEL_SEARCH"] != nil)
		{
			self.hotelSearch = (HotelSearch*)(msg.parameterBag)[@"HOTEL_SEARCH"];
            self.hotelBooking = (msg.parameterBag)[@"HOTEL_BOOKING"];
            self.hotelBookingRoom = (msg.parameterBag)[@"HOTEL_ROOM"];
//            self.travelPointsInBank = (msg.parameterBag)[@"TRAVEL_POINTS_IN_BANK"];
            
//            [self populateSections];
            
			// If a credit card has not already been selected and there is at least one card to choose from,
			// then select the first card.
			if (self.creditCardIndex == nil && [self.creditCards count])
			{
                self.creditCardIndex = @0;
			}
			
//            [self updateViolationReasons];
            [self fetchPreSellOptions];
            //			[tableList reloadData]; // Commented as same calls are being made below
            //          [self makeHeader];
            //
            //			[self configureConfirmButton];
		}
		else if ((msg.parameterBag)[@"CREDIT_CARD_INDEX"] != nil)
		{
			self.creditCardIndex = (NSNumber*)(msg.parameterBag)[@"CREDIT_CARD_INDEX"];
		}
		else if ((msg.parameterBag)[@"OPTION_TYPE_ID"] != nil)
		{
			// We've returned from the HotelOptionsViewController
			NSNumber* selectedRowIndexNumber = (NSNumber*)(msg.parameterBag)[@"SELECTED_ROW_INDEX"];
			NSUInteger selectedRowIndex = [selectedRowIndexNumber intValue];
			ViolationReason *reason = self.violationReasons[selectedRowIndex];
            if(self.hotelBookingRoom.relHotelViolationCurrent == nil)
                self.hotelBookingRoom.relHotelViolationCurrent = [[HotelBookingManager sharedInstance] makeNewViolation];
            
            self.hotelBookingRoom.relHotelViolationCurrent.code = reason.code;
            self.hotelBookingRoom.relHotelViolationCurrent.message = reason.description;
            self.hotelBookingRoom.relHotelViolationCurrent.violationType = reason.violationType;
            [[HotelBookingManager sharedInstance] saveIt:self.hotelBooking];
		}
		else if ((msg.parameterBag)[@"TEXT"] != nil)
		{
            self.hotelBookingRoom.violationJustification = (NSString*)(msg.parameterBag)[@"TEXT"];
		}
        else if ((msg.parameterBag)[@"USE_TRAVEL_POINTS"] != nil)
		{
            self.hotelBookingRoom.isUsingPointsAgainstViolations = (NSNumber*)(msg.parameterBag)[@"USE_TRAVEL_POINTS"];
		}
		
		[self.tableView reloadData];
        [self setTableViewHeader];
		
//		[self configureConfirmButton];
	}
    else if ([msg.idKey isEqualToString:PRE_SELL_OPTIONS])
    {
        self.isPreSellOptionsLoaded = YES;
        PreSellOptions *preSellOptions = (PreSellOptions *)msg.responder;
        
//        self.cancellationPolicyNeedsViewing = NO;
        if ([preSellOptions.cancellationPolicyLines count])
        {
            self.cancellationPolicyText = [preSellOptions.cancellationPolicyLines componentsJoinedByString:@"\n"];
            // This line has been commented out due to initial feedback from UX team
            // They have requested to look into their own approach for ensuring that customers read the policy
            // but we can keep the display of the policy in. They may change their mind, so we'll leave the code in
            // and just disable it for now by commenting this line out.
            //self.cancellationPolicyNeedsViewing = YES;
        }
        self.creditCards = preSellOptions.creditCards;
        
        if (self.creditCardIndex == nil && [self.creditCards count])
            self.creditCardIndex = @0;
        [self.tableView reloadData];
//        [self configureConfirmButton];
        if (!preSellOptions.isRequestSuccessful) {
            MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"Error" localize]
                                                                    message:[@"An error has occurred retrieving sell information fields. Reservation cannot be done at this time. Please try later." localize]
                                                                   delegate:nil
                                                          cancelButtonTitle:@"Cancel"
                                                          otherButtonTitles:nil];
            [alert show];
        }
    }
    else if ([msg.idKey isEqualToString:DOWNLOAD_TRAVEL_CUSTOMFIELDS])
    {
        if ([self isViewLoaded]) {
            self.navigationItem.rightBarButtonItem.enabled = YES;
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        }
        
//        if (msg.errBody == nil && msg.responseCode == 200)
//        {
//            [aSections removeObject:kSectionCustomFields]; // removes the instance if any
//            [aSections addObject:kSectionCustomFields];
//            self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAllFieldsAtStart:NO];
//            [dictSections removeObjectForKey:kSectionCustomFields];
//            dictSections[kSectionCustomFields] = tcfRows;
//            [tableList reloadData];
//        }
    }
	else if ([msg.idKey isEqualToString:RESERVE_HOTEL])
	{
		ReserveHotel *reserveHotel = (ReserveHotel *)msg.responder;
		[self showHotelReservationResponse:reserveHotel.hotelReservationResponse];
        
        if ([self isViewLoaded]){
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        }
        
	}
//	else if ([msg.idKey isEqualToString:TRIPS_DATA] && (msg.parameterBag)[@"ITIN_LOCATOR"])
//	{
//		[updatingItineraryView setHidden:YES];
//		
//        if (hotelSearch.tripKey != nil && ![UIDevice isPad])
//        {
//            // MOB-9566 refresh TripsData for both TripDetails and Trips view.
//            [TripsViewController refreshViewsWithTripsData:msg fromView:self];
//            [self switchToTripDetailView:nil];
//        }
//        else
//        {
//            NSString * itinLocator = nil;
//            if (hotelSearch.tripKey == nil)
//                itinLocator = (NSString*)(msg.parameterBag)[@"ITIN_LOCATOR"];
//            [self switchToTripDetailView:itinLocator];
//        }
//		
//	}
//    else if ([msg.idKey isEqualToString:TRIPS_DATA])
//    {
//        NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.hotelRezResponse.recordLocator, @"RECORD_LOCATOR",[self getViewIDKey], @"TO_VIEW",self.hotelRezResponse.itinLocator,@"ITIN_LOCATOR", nil];
//        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
//    }
}

- (void)updateViolationReasons
{
    if (self.violationReasons == nil || [self.violationReasons count] == 0)
	{
		NSMutableArray *reasons = [[NSMutableArray alloc] init];
		NSMutableArray *labels = [[NSMutableArray alloc] init];
		
        TravelViolationReasons *travelViolationReasons = [TravelViolationReasons getSingleton];
        if (travelViolationReasons != nil && [travelViolationReasons.violationReasons count] > 0) {
            NSArray *hotelViolations = [self.hotelBookingRoom.relHotelViolation allObjects];
            
            NSMutableArray *violationTypes = [[NSMutableArray alloc] initWithObjects:nil];
            
            for (EntityHotelViolation *hotelViolation in hotelViolations) {
                if (hotelViolation.violationType != nil) {
                    [violationTypes addObject:hotelViolation.violationType];
                }
            }
            
            NSMutableArray *tmpReasons = [travelViolationReasons getReasonsFor:violationTypes];
            for (ViolationReason *reason in tmpReasons) {
                [reasons addObject:reason];
                [labels addObject:reason.description];
            }
            
        }
		
		self.violationReasons = reasons;
        self.violationReasonLabels = labels;
	}
}


-(void)fetchPreSellOptions
{
    NSMutableDictionary *paramBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:HOTEL_BOOKING, @"TO_VIEW", @"YES", @"REFRESHING"
                                     , self.hotelBookingRoom.choiceId, @"CHOICE_ID",  nil];
	
	[[ExSystem sharedInstance].msgControl createMsg:PRE_SELL_OPTIONS CacheOnly:@"NO" ParameterBag:paramBag SkipCache:YES Options:SILENT_ERROR RespondTo:self];
}

//- (void)initData:(NSMutableDictionary*)paramBag
//{
//	if (paramBag != nil)
//	{
//		HotelSearchCriteria *hotelSearchCriteria = (HotelSearchCriteria*)paramBag[@"HOTEL_SEARCH_CRITERIA"];
//		if (hotelSearchCriteria != nil)
//		{
//			[self makeToolbar:hotelSearchCriteria];
//		}
//	}
//	
//	if (paramBag == nil || paramBag[@"POPTOVIEW"] == nil)
//		self.hotelSearch = nil;
//}

#pragma mark - room reservation
-(void)completeReservation
{
	self.navigationItem.rightBarButtonItem = nil;	// Remove confirm button from toolbar
	
	NSUInteger ccIndex = [self.creditCardIndex integerValue];
	CreditCard* creditCard = [self.creditCards objectAtIndex:ccIndex];
	
	HotelReservationRequest* reservationRequest = [[HotelReservationRequest alloc] init];
	reservationRequest.bicCode = self.hotelBookingRoom.bicCode; // roomResult.bicCode;
	reservationRequest.creditCardId = (creditCard == nil ? @"" : creditCard.ccId);
	reservationRequest.hotelChainCode = self.hotelBooking.chainCode; // hotelResult.chainCode;
	reservationRequest.propertyId = [NSString stringByEncodingXmlEntities:self.hotelBooking.propertyId];// [NSString stringByEncodingXmlEntities:hotelResult.propertyId];
	reservationRequest.propertyName = [NSString stringByEncodingXmlEntities:self.hotelBooking.hotel]; // [NSString stringByEncodingXmlEntities:hotelResult.hotel];
	reservationRequest.sellSource = self.hotelBookingRoom.sellSource; // roomResult.sellSource;
	reservationRequest.tripKey = self.hotelSearch.tripKey;
    reservationRequest.isUsingTravelPointsAgainstViolations = [self.hotelBookingRoom.isUsingPointsAgainstViolations boolValue];
    
    if (self.hotelBookingRoom.relHotelViolationCurrent != nil)
        reservationRequest.violationCode = self.hotelBookingRoom.relHotelViolationCurrent.code;
    
    reservationRequest.violationJustification = self.hotelBookingRoom.violationJustification;
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:reservationRequest, @"HOTEL_RESERVATION_REQUEST", nil];
    
//    NSString *customFields = [TravelCustomFieldsManager makeCustomFieldsRequestXMLBody];
//    
//    if (customFields != nil)
//        pBag[@"TRAVEL_CUSTOM_FIELDS"] = customFields;

    [self fakeReserve];
//	[[ExSystem sharedInstance].msgControl createMsg:RESERVE_HOTEL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES Options:NO_RETRY RespondTo:self];
//    [WaitViewController showWithText:@"Wait" animated:YES];

}

- (void)fakeReserve
{
    [WaitViewController showWithText:nil animated:YES];

    Fusion2014TripDetailsViewController *ftTripDetailVC = [[UIStoryboard storyboardWithName:@"Fusion2014TripDetailsViewController" bundle:nil] instantiateInitialViewController];
    [ConcurMobileAppDelegate unwindToRootView];
    [[ConcurMobileAppDelegate findHomeVC].navigationController pushViewController:ftTripDetailVC animated:YES];

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [NSThread sleepForTimeInterval:5.0f];
        dispatch_async(dispatch_get_main_queue(), ^{
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];

            NSString *uuid = [PostMsgInfo getUUID];
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"TRIPS", @"TO_VIEW", @"YES", @"REFRESHING",@"YES", @"LOADING_SINGLE_ITIN", @"231929330349", @"ITIN_LOCATOR", nil];
            pBag[@"TRIPDETAILSREQUEST_UUID"] = uuid;
            ftTripDetailVC.tripDetailsRequestId = uuid;
            [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:ftTripDetailVC];
        });
    });

}

-(void)showHotelReservationResponse:(HotelReservationResponse*)hotelReservationResponse
{
    [WaitViewController hideAnimated:YES withCompletionBlock:nil];
	if ([hotelReservationResponse.status isEqualToString:@"SUCCESS"])
	{
        Fusion2014TripDetailsViewController *ftTripDetailVC = [[UIStoryboard storyboardWithName:@"Fusion2014TripDetailsViewController" bundle:nil] instantiateInitialViewController];
        NSString *uuid = [PostMsgInfo getUUID];
        NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:hotelReservationResponse.recordLocator, @"RECORD_LOCATOR", HOTEL_BOOKING, @"TO_VIEW",hotelReservationResponse.itinLocator,@"ITIN_LOCATOR", uuid, @"TRIPDETAILSREQUEST_UUID" ,nil];
        ftTripDetailVC.tripDetailsRequestId = uuid;
        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:ftTripDetailVC];
        
        [self.navigationController pushViewController:ftTripDetailVC animated:YES];
        
	}
	else // failure
	{
		MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Error"]
                                                                message:(hotelReservationResponse.errorMessage && hotelReservationResponse.errorMessage.length) ? hotelReservationResponse.errorMessage : [@"HOTEL_BOOKING_VIEW_BOOKING_ERROR_MESSAGE" localize]
                                                               delegate:self
                                                      cancelButtonTitle:[Localizer getLocalizedText:@"Cancel"]
                                                      otherButtonTitles:nil];
//		alert.tag = kAlertReservationFailed;
		[alert show];
	}
}


@end
