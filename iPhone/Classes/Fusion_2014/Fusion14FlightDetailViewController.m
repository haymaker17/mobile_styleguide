//
//  Fusion14FlightDetailViewController.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/24/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "Fusion14FlightDetailViewController.h"
#import "Fusion14FlightDetailsCell.h"
#import "Fusion14FlightDetailsHeaderCell.h"
#import "Fusion14FlightTripDetailsCell.h"
#import "Fusion14FlightPaymentInfoCell.h"
#import "Config.h"
#import "HotelSearch.h"
#import "Fusion14HotelSearchResultsViewController.h"
#import "EntityAirCriteria.h"
#import "LabelConstants.h"

#import "WaitViewController.h"
#import "FusionMockServer.h"

#define kFieldIdFrequentFlyer @"FrequentFlyer"
#define kAlertTagFrequentFlyerSoftStop  300201
#define kAlertUnhandledViolations 300202
#define kAlertGropuAuthUsed 300203
#define kSectionViolation 200
#define kSectionRuleMessages 1000
#define kSectionCC 999
#define kFlightPosAffinity 997
#define kCreditCardCvv 76598
#define kSectionTripFields 1001
#define kSectionPreSellFlightOptions 12112
#define kSectionTridDetails 888

#define kRowManageViolations @"RowManageViolations"
#define kRowUsingPointsViolations @"RowUsingPointsViolations"
#define kRowViolationsText @"RowViolationsText"
#define kRowViolationReason @"RowViolationReason"
#define kRowViolationJustification @"RowViolationJustification"

@interface Fusion14FlightDetailViewController ()

@property (nonatomic, strong) NSManagedObjectContext                *managedObjectContext;
@property (nonatomic, strong) NSFetchedResultsController            *fetchedResultsController;
@property (strong, nonatomic) NSMutableDictionary                   *dictSections;
@property BOOL editedDependentCustomField;
//@property (strong, nonatomic) EntityTravelCustomFields *selectedCustomField;
@property (nonatomic, strong) NSMutableArray          *tcfRows;
@property BOOL hideCustomFields; // Hide custom fields when coming from existing trip
@property (strong, nonatomic) NSMutableArray			*aSections;
@property (strong, nonatomic) NSMutableArray			*aClass;
@property (strong, nonatomic) NSMutableDictionary       *dictClass;

@property (strong, nonatomic) NSMutableArray            *aButtons;
@property(strong, nonatomic) CreditCard                 *chosenCreditCard;
@property (nonatomic, strong) NSArray                   *creditCards;
@property (strong, nonatomic) PreSellOptions            *preSellOptions;

@property (strong, nonatomic) IBOutlet UIView *uvSlideReserve;
@property (strong, nonatomic) IBOutlet UILabel *lblTotalCost;

@property int   chosenCardIndex;

@property HotelSearch *hotelSearch;

@end

@implementation Fusion14FlightDetailViewController

@synthesize fetchedResultsController=__fetchedResultsController;

-(id)initWithCoder:(NSCoder *)aDecoder
{
    
    self = [super initWithCoder:aDecoder];
    if (self) {
        // Custom initialization
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*)[[UIApplication sharedApplication] delegate];
        self.managedObjectContext = [ad managedObjectContext];
        
    }
    return self;
}

#pragma mark - view cycle
- (void)viewDidLoad
{
    [super viewDidLoad];
    //Custom Fields
    self.aSections = [[NSMutableArray alloc] initWithObjects: nil];
    self.dictSections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    
//    [self fillClass];
    self.aButtons = [[NSMutableArray alloc] initWithObjects:@"Total", @"Card", @"Delivery",  nil];
    self.chosenCardIndex = -1;
    [self chooseFirstCard];
    
    //[self makeFake];
    NSArray *aFilters = [[AirFilterManager sharedInstance] fetchByFareIdSegmentPos:self.airSummary.fareId segPos:kSectionCC];
    for(EntityAirFilter *aFilter in aFilters)
    {
        [[AirFilterManager sharedInstance] deleteObj:aFilter];
    }
    
    aFilters = [[AirFilterManager sharedInstance] fetchByFareIdSegmentPos:self.airSummary.fareId segPos:kSectionRuleMessages];
    for(EntityAirFilter *aFilter in aFilters)
    {
        [[AirFilterManager sharedInstance] deleteObj:aFilter];
    }
    
    aFilters = [[AirFilterManager sharedInstance] fetchByFareIdSegmentPos:self.airSummary.fareId segPos:kSectionViolation];
    for(EntityAirFilter *aFilter in aFilters)
    {
        [[AirFilterManager sharedInstance] deleteObj:aFilter];
    }

    //Total Cost
    EntityAirFilter *afTotal = (EntityAirFilter*)[[AirFilterManager sharedInstance] makeNew];
    afTotal.segmentPos = @kSectionTridDetails;
    afTotal.flightPos = @kSectionTridDetails;
    afTotal.crnCode = self.airSummary.crnCode;
    afTotal.fare = self.airSummary.fare;
    afTotal.fareId = self.airSummary.fareId;
    [[AirFilterManager sharedInstance] saveIt:afTotal];
    afTotal = nil;
    
    //Credit Card
    EntityAirFilter *afCard = (EntityAirFilter*)[[AirFilterManager sharedInstance] makeNew];
    afCard.segmentPos = @kSectionCC;
    afCard.flightPos = @kSectionCC;
    afCard.fareId = self.airSummary.fareId;
    [[AirFilterManager sharedInstance] saveIt:afCard];
    afCard = nil;
    

    
    NSArray *airRuleArray = [[AirRuleManager sharedInstance] fetchByFareId:self.airSummary.fareId];
    int i = 0;
    for(EntityAirRules *airRule in airRuleArray)
    {
        EntityAirFilter *af = (EntityAirFilter*)[[AirFilterManager sharedInstance] makeNew];
        af.segmentPos = @kSectionRuleMessages;
        int ix = ([airRule.exceptionLevel intValue] + i) * -1;
        af.flightPos = @(ix);
        af.crnCode = airRule.exceptionMessage;
        af.fare = airRule.exceptionLevel;
        af.fareId = self.airSummary.fareId;
        [[AirFilterManager sharedInstance] saveIt:af];
        af = nil;
    }
    
    [self processFilterDataForArray];
    
//    if (!hideCustomFields)
//    {
//        [self fetchCustomFields];
//        
//        // prepopulate the custom fields from cache
//        [aSections removeObject:@"TRIP_FIELDS"]; // Removes the instance if any. Will be re-added if needed.
//        self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAllFieldsAtStart:NO];
//        if (tcfRows != nil && [tcfRows count] > 0)
//        {
//            [aSections addObject:@"TRIP_FIELDS"];
//            dictSections[@"TRIP_FIELDS"] = self.tcfRows;
//        }
//    }
//    else
//        [self hideLoadingView];
    
//    [self.view];
    //    [self refetchData];
//    [self makeReserveButton:nil];
//    [self fetchPreSellOptions];
    [self refetchData];
    [self.tableView reloadData];
    [self updateToolbar];
//    [self setDepartureViewData];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self setTableViewHeader];
//    [self setDepartureViewData];
}

- (void)setTableViewHeader
{
    self.tableView.tableHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, self.tableView.frame.size.width, 44.0)];
    
    Fusion14FlightDetailsHeaderCell *headerCell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion14FlightDetailsHeaderCell"];
    
    // TODO: set the loaction and date for the hotel search
    [self.tableView dequeueReusableHeaderFooterViewWithIdentifier:@"Fusion14FlightDetailsHeaderCell"];
    [self.tableView.tableHeaderView addSubview:headerCell];
    
    headerCell.lblFlightDetailHeader.text = self.travelDate;
//    self.lblDepartToCitySummaryText.text = [NSString stringWithFormat:@"Depart to %@", self.flightToCity];
//    self.lblReturnToCitySummaryText.text = [NSString stringWithFormat:@"Return to %@", self.returnToCity];
}

- (void)updateToolbar
{
    UISwipeGestureRecognizer *swipeGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(completeReservation)];
    [swipeGestureRecognizer setDirection:UISwipeGestureRecognizerDirectionRight];
    [self.uvSlideReserve addGestureRecognizer:swipeGestureRecognizer];
    
    UIBarButtonItem *leftPadding = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil];
    leftPadding.width = -16;
    
    [self setToolbarItems:@[leftPadding,[[UIBarButtonItem alloc] initWithCustomView:self.uvSlideReserve]]];
    [self.navigationController.toolbar setTranslucent:NO];
    [self.navigationController.toolbar setTintColor:[UIColor redColor]];
    [self.navigationController setToolbarHidden:NO];
}


#pragma mark - room reservation
-(void)completeReservation
{
    // Add flight sell(reserve) code here?
    [WaitViewController showWithText:nil animated:YES];

// Now they want this hard coded
//    FusionMockServer *server = [FusionMockServer sharedInstance];
//    server.departForSFOTime = [self fixDate:self.airSummary.departureTime];
//    server.arriveAtSFOTime = [self fixDate:self.airSummary.arrivalTime];
//    server.departForSEATime = [self fixDate:self.airSummary.roundArrivalTime];
//    server.arriveAtSEATime = [self fixDate:self.airSummary.roundDepartureTime];
//    [server addMockForSanFranciscoTripItinerary];

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [NSThread sleepForTimeInterval:5.0f];
        dispatch_async(dispatch_get_main_queue(), ^{
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];

            // shows adding hotel overlay
            OverlayView2 *overlay = [[OverlayView2 alloc] initWithNibNamed:@"Fusion14AddHotelConfirmationView"];
            overlay.frame = self.navigationController.view.bounds;
            overlay.delegate = self;
            [self.navigationController.view addSubview:overlay];
        });
    });
}

- (NSString *)fixDate:(NSDate *)date
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	[dateFormatter setDateFormat: @"yyyy-MM-dd'T'HH:mm:ss"];

    return [dateFormatter stringFromDate:date];
}

-(void) processFilterDataForArray
{
    NSArray *a = [[AirFilterManager sharedInstance] fetchAirFilters:self.airSummary.fareId];
    NSMutableArray *aFilters = [[NSMutableArray alloc] initWithObjects: nil];
    int segPos = -1;
    for(int i = 0; i < [a count]; i++)
    {
        //NSLog(@"segmentPos %d", segPos);
        EntityAirFilter *filter = a[i];
        NSLog(@"filter is: %@", filter.segmentPos);
        if(segPos == -1)
        {
            aFilters = [[NSMutableArray alloc] initWithObjects: nil];
            segPos = [filter.segmentPos intValue];
        }
        else if([filter.segmentPos intValue] != segPos)
        {
            //new section
            [self.dictSections removeObjectForKey:[NSString stringWithFormat:@"%i", segPos]];
            self.dictSections[[NSString stringWithFormat:@"%i", segPos]] = aFilters;
            [self.aSections removeObject:[NSString stringWithFormat:@"%i", segPos]];
            [self.aSections addObject:[NSString stringWithFormat:@"%i", segPos]];
            aFilters = [[NSMutableArray alloc] initWithObjects: nil];
            segPos = [filter.segmentPos intValue];
        }

        [aFilters addObject:filter];
    }
    
    //do the last one
    [self.dictSections removeObjectForKey:[NSString stringWithFormat:@"%i", segPos]];
    self.dictSections[[NSString stringWithFormat:@"%i", segPos]] = aFilters;
    [self.aSections removeObject:[NSString stringWithFormat:@"%i", segPos]];
    [self.aSections addObject:[NSString stringWithFormat:@"%i", segPos]];
    
}

#pragma mark - credit card

-(void)fetchPreSellOptions // Note: Pre-sell for car only contains CC details, and is only called when car.sendCreditCard is YES -- Change this when car has loyalty programs data
{
    NSMutableDictionary *paramBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"AIR_BOOKING", @"TO_VIEW", @"YES", @"REFRESHING"
                                     , self.airSummary.choiceId , @"CHOICE_ID",  nil];
	
	[[ExSystem sharedInstance].msgControl createMsg:PRE_SELL_OPTIONS CacheOnly:@"NO" ParameterBag:paramBag SkipCache:YES Options:SILENT_ERROR RespondTo:self];
    [WaitViewController showWithText:nil animated:YES];
}

-(void)chooseFirstCard
{
//    if ([self.creditCards count] < 1)
//	{
//        [aButtons removeObjectAtIndex:1];
//        [aButtons insertObject:(self.isPreSellOptionsLoaded ? [Localizer getLocalizedText:@"Unavailable"] : [@"Loading..." localize]) atIndex:1];
//		chosenCardIndex = -1;
//	}
//	else {
		self.chosenCardIndex = 0;
		self.chosenCreditCard = self.creditCards[0];
//		if(self.chosenCreditCard.name == nil || self.chosenCreditCard.maskedNumber == nil){
//			[self performSelector:@selector(chooseFirstCard) withObject:nil afterDelay:2.0f];
//		}
    
		NSString *cardName = self.chosenCreditCard.name;
		NSString *cardMask = self.chosenCreditCard.maskedNumber;
        
        // MOB-13173 cut down the card num to fit the field space by only showing two "*"
        NSString *trimedCardNum = nil;
		if(cardName == nil)
			cardName = @"";
		
		if(cardMask == nil)
			cardMask = @"";
        else if ([cardMask length] >= 6)
            trimedCardNum = [cardMask substringFromIndex:[cardMask length] - 6];
        else
            trimedCardNum = cardMask;
		
        [self.aButtons removeObjectAtIndex:1];
        [self.aButtons insertObject:[ NSString stringWithFormat:@"%@ %@", cardName, trimedCardNum] atIndex:1];
//		[self makeReserveButton:self];
    EntityAirFilter *af = (EntityAirFilter*)[[AirFilterManager sharedInstance] fetchByFareIdSegmentPosFlightPos:self.airSummary.fareId segPos:kSectionCC flightPos:999];
    af.bic = self.aButtons[1];
    
    [self.tableView reloadData];
}

#pragma mark - table view delegate
- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    if (section == 0) {
        return 0.0;
    }
    return 13.0f;
}

//- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
//{
//    NSString *sectionName = self.aSections[section];
//    if ([sectionName integerValue] == kSectionCC){
//        return 13.0f;
//    }
//    return 0.0;
//}

- (UIView*)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    if (section != 0) {
        UIView *sectionHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, self.tableView.frame.size.width, 13.0f)];
        [sectionHeaderView setBackgroundColor:[UIColor colorWithRed:233.0/255.0 green:233.0/255.0 blue:233.0/255.0 alpha:1.0]];
        return sectionHeaderView;
    }
    return  nil;
}

//- (UIView*)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
//{
//     NSString *sectionName = self.aSections[section];
//    if ([sectionName integerValue] == kSectionCC) {
//        UIView *sectionHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, self.tableView.frame.size.width, 13.0f)];
//        [sectionHeaderView setBackgroundColor:[UIColor colorWithRed:233.0/255.0 green:233.0/255.0 blue:233.0/255.0 alpha:1.0]];
//        return sectionHeaderView;
//    }
//    return  nil;
//}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 4;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 1;
//    NSString *sectionName = self.aSections[section];
//    if ([sectionName isEqualToString:@"0"] || [sectionName isEqualToString:@"1"]) {
//        NSArray *a = self.dictSections[sectionName];
//        NSLog(@"%i rows in section %i", [a count], section);
//        return [a count];
//    }
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 2) {
        return 151.0;
    }
    else if (indexPath.section == 3){
        return 45.0;
    }
    return 172.0f;
    
//    NSString *sectionName = self.aSections[indexPath.section];
//    NSArray *a = self.dictSections[sectionName];
//    //        NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
//    EntityAirFilter *entity = (EntityAirFilter *)a[indexPath.row];
//    if ([entity.segmentPos intValue] == kSectionCC) {
//        if (indexPath.row == 0) {
//            return 45.0f;
//        } else{
//        return 0.0f;
//        }
//    }
//    else if ([entity.segmentPos intValue] == kSectionTridDetails){
//        return 151.0f;
//    }
//    else if([entity.carrier isEqualToString:@"LAYOVER"]){
//        return 0.0;
//    }
//
//    return 172.0f;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
//    NSString *sectionName = self.aSections[indexPath.section];
//    NSArray *a = self.dictSections[sectionName];
//    EntityAirFilter *entity = (EntityAirFilter *)a[indexPath.row];
    
//    if ([sectionName intValue] == kSectionTridDetails) {//Credit Card & Total
    if (indexPath.section == 2) {
        Fusion14FlightTripDetailsCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion14FlightTripDetailsCell"];
        
        // hard code
        self.lblTotalCost.text = cell.lblPrice.text = @"$216";
        cell.lblTravelPoints.text = @"+59 pts";
        cell.lblFinalTripSummary.text = @"Mon, June 9 - Wed, June 11";
        
//            NSString *crnCode = entity.crnCode;
//            if(![crnCode length])
//                crnCode = @"USD";
//            cell.lblPrice.text = [FormatUtils formatMoney:[entity.fare stringValue] crnCode:crnCode];
//            self.lblTotalCost.text = cell.lblPrice.text;
//
//            if ([self.airSummary.travelPoints intValue] != 0 && (!self.airSummary.isUsingPointsAgainstViolations || [self.airSummary.isUsingPointsAgainstViolations boolValue]))
//            {
//                cell.lblTravelPoints.hidden = NO;
//                if ([self.airSummary.travelPoints intValue] > 0) {
//                    cell.lblTravelEarnedText.text = @"Points Earned";
//                    cell.lblTravelPoints.text = [NSString stringWithFormat: @"+ %d pts.",[self.airSummary.travelPoints intValue]];
//                }
//                else {
//                    cell.lblTravelEarnedText.text = @"Points Used";
//                    cell.lblTravelPoints.text = [NSString stringWithFormat:@"%d pts.",-[self.airSummary.travelPoints intValue]];
//                    cell.lblTravelPoints.textColor = [UIColor bookingRedColor];
//                }
//            }
            return cell;
    }
//    else if ([entity.segmentPos intValue] == kSectionCC){
    else if (indexPath.section == 3){
        Fusion14FlightPaymentInfoCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion14FlightPaymentInfoCell"];
//        cell.lblCreditCardLastfour.text = entity.bic;
        return cell;
    }
    else {
        Fusion14FlightDetailsCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion14FlightDetailsCell"];
        [self configureCell:cell atIndexPath:indexPath];
//        if ([entity.carrier isEqualToString:@"LAYOVER"]) {
//            cell.hidden = YES;
//        }
        return cell;
    }
}

- (void)configureCell:(Fusion14FlightDetailsCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0) {
        cell.lblDepartToCitySummaryText.text = @"Depart to San Francisco, CA";
        cell.lblDepartureTime.text = @"9:41 AM";
        cell.lblArrivalTime.text = @"12:00 PM";
        cell.lblDepartureAirportCode.text = @"SEA";
        cell.lblArrivalAirportCode.text = @"SFO";
        cell.lblArrivalCityName.text = @"Seattle, WA";
        cell.lblArrivalCityName.text = @"San Francisco, CA";
        cell.lblAirlineName.text = @"United 698";
        cell.lblFlightDuration.text = @"2h 19m,";
        cell.lblNumberofStops.text = @"Nonstop";
    }
    else if (indexPath.section == 1){
        cell.lblDepartToCitySummaryText.text = @"Return to Seattle, WA";
        cell.lblDepartureTime.text = @"2:30 PM";
        cell.lblArrivalTime.text = @"4:35 PM";
        cell.lblDepartureAirportCode.text = @"SFO";
        cell.lblArrivalAirportCode.text = @"SEA";
        cell.lblArrivalCityName.text = @"San Francisco, CA";
        cell.lblArrivalCityName.text = @"Seattle, WA";
        cell.lblAirlineName.text = @"United 642";
        cell.lblFlightDuration.text = @"2h 5m,";
        cell.lblNumberofStops.text = @"Nonstop";
    }
    
//    NSString *sectionName = self.aSections[indexPath.section];
//    NSArray *a = self.dictSections[sectionName];
//    EntityAirFilter *entity = (EntityAirFilter *)a[indexPath.row];
//
//    NSString *airlineCode = entity.carrier;
//    NSString *airlineName = (self.airShop.vendors)[airlineCode];
//    
//    cell.lblArrivalAirportCode.text = entity.endIata;
//    cell.lblDepartureAirportCode.text = entity.startIata;
//
//    cell.lblAirlineName.text = [NSString stringWithFormat:@"%@ %@", airlineName, entity.flightNum];
//    // Hard code image in storyboard
////    UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:airlineCode VendorType:@"a_small" RespondToIV:cell.ivAirlineLogo];
////    if(gotImg != nil)
////        cell.ivAirlineLogo.image = gotImg;
//    
//    NSString *departureCity = (self.airShop.airportCityCodes)[entity.startIata];
//    NSString *ArrivalCity = (self.airShop.airportCityCodes)[entity.endIata];
//    cell.lblDepartureCityName.text = departureCity;
//    cell.lblArrivalCityName.text = ArrivalCity;
//    
//    if (indexPath.section == 0) {
//        cell.lblDepartToCitySummaryText.text = [NSString stringWithFormat:@"Depart to %@", ArrivalCity];
//    }
//    else{
//        cell.lblDepartToCitySummaryText.text = [NSString stringWithFormat:@"Return to %@", departureCity];
//    }
//    NSString *departureTime = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:entity.departureTime];
//    NSString *arrivalTime = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:entity.arrivalTime];
//    if (departureTime.length > 3) {
//       cell.lblDepartureTime.text = [departureTime substringFromIndex:4];
//    } else{
//        cell.lblDepartureTime.text = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:entity.departureTime];
//    }
//    if (arrivalTime.length > 3) {
//        cell.lblArrivalTime.text = [arrivalTime substringFromIndex:4];
//    } else {
//        cell.lblArrivalTime.text = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:entity.arrivalTime];
//    }
//    
//    // Default duration to a blank string
//    NSString *dur = @"";
//    int flightMinutes = [entity.flightTime intValue];
//    if (flightMinutes > 0)
//    {
//        // Only format a new duration string if there will be a duration to show
//        // This was added as we had a problem with no flight duration being provided by MWS
//        // leading to us showing 0h 0m
//        int flightHours = flightMinutes / 60;
//        if (flightHours > 0)
//            flightMinutes = flightMinutes - (flightHours * 60);
//        dur = [NSString stringWithFormat:@"%dh %dm%@", flightHours, flightMinutes, @","];
//    }
//    cell.lblFlightDuration.text = dur;
//    cell.lblNumberofStops.text = [self getFlightStopString:[entity.numStops integerValue]];
}


//#pragma mark - Class Stuff
//-(void) fillClass
//{
//    self.aClass = [[NSMutableArray alloc] initWithObjects:@"Y", @"W", @"C", @"F", @"ANY", nil];
//    self.dictClass = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[Localizer getLocalizedText:@"Economy"], @"Y", [Localizer getLocalizedText:@"Premium Economy"], @"W",[Localizer getLocalizedText:@"Business"], @"C", [Localizer getLocalizedText:@"First"], @"F", [Localizer getLocalizedText:@"Any"], @"ANY", nil];
//}


- (NSString*)getFlightStopString:(NSInteger)numOfStop
{
    NSString *stopStr = nil;
    if (numOfStop > 1) {
        stopStr = [NSString stringWithFormat:@"%li stops", (long)numOfStop];
    }
    else if (numOfStop == 1) {
        stopStr = @"1 stop";
    }
    else
    {
        stopStr = @"Nonstop";
    }
    return stopStr;
}

#pragma mark - Fetched results controller
- (NSFetchedResultsController *)fetchedResultsController
{
    if (__fetchedResultsController != nil) {
        return __fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityAirFilter" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"segmentPos" ascending:YES];
    NSSortDescriptor *sort2 = [[NSSortDescriptor alloc] initWithKey:@"flightPos" ascending:YES];
    
    [fetchRequest setSortDescriptors:@[sort, sort2]];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(fareId = %@)", self.airSummary.fareId];
    [fetchRequest setPredicate:pred];
    
    NSFetchedResultsController *theFetchedResultsController =
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                        managedObjectContext:self.managedObjectContext sectionNameKeyPath:@"segmentPos"
                                                   cacheName:@"Root"];
    self.fetchedResultsController = theFetchedResultsController;
    __fetchedResultsController.delegate = self;
    
    
    
    return __fetchedResultsController;
    
}

#pragma mark - Fetched results controller delegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableView beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo
           atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
{
    switch(type)
    {
        case NSFetchedResultsChangeInsert:
            [self.tableView insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [self.tableView deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject
       atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type
      newIndexPath:(NSIndexPath *)newIndexPath
{
    UITableView *tableView = self.tableView;
    
    switch(type)
    {
            
        case NSFetchedResultsChangeInsert:
            [tableView insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeUpdate:
            [tableView reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
            break;
            
        case NSFetchedResultsChangeMove:
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [tableView insertRowsAtIndexPaths:@[newIndexPath]withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableView endUpdates];
}

#pragma mark - reset and then fetch the managed results
-(void) refetchData
{
    self.fetchedResultsController = nil;
    NSError *error;
	if (![[self fetchedResultsController] performFetch:&error]) {
		// Update to handle the error appropriately.
		NSLog(@"Unresolved error viewDidLoad fetching %@, %@", error, [error userInfo]);
        
        if ([Config isDevBuild]) {
            exit(-1);  // Fail
        } else {
            // be more graceful when dying abort();
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"airShopResults::viewDidLoad: fetchedResultsController %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
        }
	}
}


#pragma mark - OverlayClickDelegate
- (void)buttonYesClicked
{
    [self sendHotelSearchMsg];
}

- (void)buttonNoClicked
{
    
}

-(EntityAirCriteria *) loadEntityAirCriteria
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityAirCriteria" inManagedObjectContext:[ExSystem sharedInstance].context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *a = [[ExSystem sharedInstance].context executeFetchRequest:fetchRequest error:&error];
    
    if([a count] > 0)
        return a[0];
    else
        return nil;
}

-(void) sendHotelSearchMsg
{
    EntityAirCriteria *entityAirCriteria = [self loadEntityAirCriteria];
    
    // Hard code SFO city lat/long
    self.hotelSearch = [[HotelSearch alloc] init];
    self.hotelSearch.hotelSearchCriteria.checkinDate = [NSDate dateWithTimeInterval:((60 * 60) * 24) sinceDate:entityAirCriteria.DepartureDate]; // Hard code date to the 10th for hotel search
    self.hotelSearch.hotelSearchCriteria.checkoutDate = entityAirCriteria.ReturnDate;
    if (self.hotelSearch.hotelSearchCriteria.locationResult == nil)
    {
        self.hotelSearch.hotelSearchCriteria.locationResult = [[LocationResult alloc] init];
    }
    self.hotelSearch.hotelSearchCriteria.locationResult.latitude = @"37.77493";
    self.hotelSearch.hotelSearchCriteria.locationResult.longitude = @"-122.41942";
    self.hotelSearch.hotelSearchCriteria.locationResult.location = @"San Francisco, CA, USA";
    // Hard code SFO city lat/long
    
    Fusion14HotelSearchResultsViewController *nextController = [[UIStoryboard storyboardWithName:@"Fusion14HotelSearchResultsViewController" bundle:nil] instantiateInitialViewController];
    [self.navigationController pushViewController:nextController animated:YES];
    nextController.hotelSearch = self.hotelSearch;
    NSMutableDictionary *pBag =nil;
    if ([Config isNewAirBooking]) {
        pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.hotelSearch, @"HOTEL_SEARCH", @"YES", @"SKIP_CACHE", @"0", @"STARTPOS", @"300", @"NUMRECORDS", nil];
    }else{
        pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.hotelSearch, @"HOTEL_SEARCH", @"YES", @"SKIP_CACHE", @"0", @"STARTPOS", @"30", @"NUMRECORDS", nil];
    }
    
    [[ExSystem sharedInstance].msgControl createMsg:FIND_HOTELS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:nextController];
}

-(void)didProcessMessage:(Msg *)msg
{
    if ([msg.idKey isEqualToString:PRE_SELL_OPTIONS])
	{
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
//        self.isPreSellOptionsLoaded = YES;
        self.preSellOptions = (PreSellOptions *)msg.responder;
        self.creditCards = self.preSellOptions.creditCards;
//        self.affinityPrograms = self.preSellOptions.affinityPrograms;
        
        
//        if (self.preSellOptions.isCreditCardCvvRequired)
//            [self createCvvRow];
//        [self createPreSellFlightOptionsSection];
        
//        if ([self.affinityPrograms count])
//        {
//            [self createAffinitySection];
//            if (self.preSellOptions.defaultProgram)
//                [self optionSelected:self.preSellOptions.defaultProgram withIdentifier:kFieldIdFrequentFlyer];
//        }
//        [self chooseCard:([self.creditCards count] ? 0 : -1)];

        
        if (!self.preSellOptions.isRequestSuccessful)
        {
            MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"Error" localize]
                                                                    message:[@"An error has occurred retrieving sell information fields. Reservation cannot be done at this time. Please try later." localize]
                                                                   delegate:nil
                                                          cancelButtonTitle:[LABEL_CLOSE_BTN localize]
                                                          otherButtonTitles:nil];
            [alert show];
        }
    }
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
