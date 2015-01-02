//
//  Fusion14FlightSearchResultsViewController.m
//  ConcurMobile
//
//  Created by Sally Yan on 4/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "Fusion14FlightSearchResultsViewController.h"
#import "Fusion14FlightSearchResultsHeaderCell.h"
#import "Fusion14FlightSearchResultsTableViewCell.h"
#import "Fusion14FlightDetailViewController.h"
#import "AirViolationManager.h"
#import "Config.h"
#import "WaitViewController.h"
#import "DateTimeFormatter.h"

@interface Fusion14FlightSearchResultsViewController ()

@property (nonatomic, strong) NSManagedObjectContext                *managedObjectContext;
@property (nonatomic, strong) NSFetchedResultsController            *fetchedResultsController;
@property (nonatomic, strong) EntityAirShopResults                  *airShopResults;
@property (nonatomic, strong) EntityAirFilterSummary                *entityAirFilterSummary;
@property (nonatomic, strong) Fusion14FlightDetailViewController    *flightDetailVC;
@property (nonatomic, strong) Fusion14FlightSearchResultsHeaderCell  *headerCell;

@end

@implementation Fusion14FlightSearchResultsViewController

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

#pragma mark - View
- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if (self.shouldGetAllResults) {
        [self getAllFlightSearchResults];
        self.shouldGetAllResults = NO;
    }
    
    self.title = @"Flights";
    UIButton *btnMoreView = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 27, 27)];
    //    [moreButton addTarget:self action:nil forControlEvents:UIControlEventTouchUpInside];
    [btnMoreView setBackgroundImage:[UIImage imageNamed:@"fusion14_icon_nav_more"] forState:UIControlStateNormal];
    UIBarButtonItem *btnMore = [[UIBarButtonItem alloc] initWithCustomView:btnMoreView];

    self.navigationItem.rightBarButtonItem = btnMore;
    [self refetchData];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self setTableViewHeader];
    [self.navigationController setToolbarHidden:YES];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - set up view header
-(void)setTableViewHeader
{
    self.tableView.tableHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, self.tableView.frame.size.width, 154.0f)];
    self.headerCell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion14FlightSearchResultsHeaderCell"];
    [self updateHeader];
    
    [self.tableView.tableHeaderView addSubview:self.headerCell];
}


-(void) updateHeader
{
    // hard code....
    self.headerCell.lblDepartureAirportCode.text = @"SEA";
    self.headerCell.lblArrivalAirportCode.text = @"SFO";
    self.headerCell.lblTravelDates.text = @"Mon, June 09 - Wed, June 11";
    
    // This loads the saved criteria from search .
    // This doesnt work currently for Voice search as it doesnt save the search criteria
//    EntityAirCriteria *entityAirCriteria = [self loadEntityAirCriteria];
//    
//    if (entityAirCriteria != nil) {
//        self.headerCell.lblDepartureAirportCode.text = entityAirCriteria.DepartureAirportCode;
//        self.headerCell.lblArrivalAirportCode.text = entityAirCriteria.ReturnAirportCode;
//        // Hard Code date in StoryBoard
////        self.headerCell.lblDepartureCity.text = (self.airShop.airportCityCodes)[entityAirCriteria.DepartureAirportCode];
////        self.headerCell.lblArrivalCity.text = (self.airShop.airportCityCodes)[entityAirCriteria.ReturnAirportCode];
//        // Hard Code date in StoryBoard
//        
//        if(self.airShop.isRoundTrip)
//        {
//            self.headerCell.lblTravelDates.text = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateEEEMMMddByDate:entityAirCriteria.DepartureDate], [DateTimeFormatter formatDateEEEMMMddByDate:entityAirCriteria.ReturnDate]];
//        }
//        else
//        {
//            self.headerCell.lblTravelDates.text = [NSString stringWithFormat:@"%@", [DateTimeFormatter formatDateEEEMMMddByDate:entityAirCriteria.DepartureDate]];
//        }
//    }
    [self setPriceToBeatLabel:self.headerCell];
   

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

#pragma mark - set data for table view
-(void)setRecommendedFlights
{

    NSFetchRequest *fetchRequestSummary=[NSFetchRequest fetchRequestWithEntityName:@"EntityAirFilterSummary"];
    NSFetchRequest *fetchRequestFilter =[NSFetchRequest fetchRequestWithEntityName:@"EntityAirFilter"];
    
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"airlineCode LIKE 'UA'"];
    NSPredicate *predicateFilter = [NSPredicate predicateWithFormat:@"carrier LIKE 'UA'"];
    
    [fetchRequestSummary setPredicate:predicate];
    
    [fetchRequestFilter setPredicate:predicateFilter];
    
    NSArray *flightsArray = [self.managedObjectContext executeFetchRequest:fetchRequestSummary error:nil];
    NSArray *airFilterArray = [self.managedObjectContext executeFetchRequest:fetchRequestFilter error:nil];
    
    if (flightsArray != nil) {
        for(EntityAirFilterSummary *flight in flightsArray){
            for (EntityAirFilter *airFilter in airFilterArray) {
                if ([flight.fareId isEqualToString:airFilter.fareId] && ([flight.numStops integerValue] == 0 && [flight.roundNumStops integerValue] == 0) ) {
                    if ([airFilter.flightNum isEqualToString:@"698"] || [airFilter.flightNum isEqualToString:@"322"] ) {
                        flight.isFusionRecommendedFlight = @55;
                    } else {
//                        [flight setValue:@NO forKey:@"isFusionRecommendedFlight"];
                        flight.isFusionRecommendedFlight = @NO;
                    }
                }
                //[self.managedObjectContext save:nil];
            }
        }
        [self.managedObjectContext save:nil];
    }
    
//    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
//    [formatter setDateFormat:@"HH:mm:ss"];
//    if (flightsArray != nil) {
//        for(EntityAirFilterSummary *flight in flightsArray){
//            
//            // checking departure time
//            NSString *dateStr = [DateTimeFormatter formatTimeHHmmaa:[flight.departureTime copy]];
//            NSString *hour = nil;
//            BOOL isMorning = FALSE;
//            BOOL isAfternoon = FALSE;
//            if (dateStr != nil) {
//                int index = [dateStr rangeOfString:@":"].location;
//                if (index > 0 && index < dateStr.length) {
//                     hour = [dateStr substringToIndex:index];
//                }
//                if ([dateStr rangeOfString:@"PM"].location == NSNotFound && ([hour integerValue]  >= 8 && [hour integerValue] <= 9)) {
//                    isMorning = TRUE;
//                }
//            }
//            
//            // check return date
//            NSString *returnDateString = [DateTimeFormatter formatTimeHHmmaa:[flight.roundArrivalTime copy]];
//            NSString *returnHour = nil;
//            if (returnDateString != nil) {
//                int index = [returnDateString rangeOfString:@":"].location;
//                if (index > 0 && index < dateStr.length) {
//                    returnHour = [returnDateString substringToIndex:index];
//                }
//            }
//
//            if ([returnDateString rangeOfString:@"PM"].location == NSNotFound && ([returnHour integerValue] >= 8 && [returnHour integerValue] < 9)) {
//                isAfternoon = TRUE;
//            }
//            
//            if (isMorning && isAfternoon) {
//                 [flight setValue:@YES forKey:@"isFusionRecommendedFlight"];
//            } else {
//                [flight setValue:@NO forKey:@"isFusionRecommendedFlight"];
//            }
//        }
//    }
//    [self saveEntityAirFilterSummary];
}

-(void)saveEntityAirFilterSummary
{
    NSError *error;
    if (![self.managedObjectContext save:&error]){
        ALog(@"Whoops, couldn't save object: %@", [error localizedDescription]);
    }else{
        DLog(@"successfully save changes on updating hotel recommendation score.")
    }
}


-(void)setPriceToBeatLabel:(Fusion14FlightSearchResultsHeaderCell*)cell
{
    // ========= Hard coded for Fusion 2014 for SFO hotel ======= 
     NSString *priceToBeat = @"$334"; //=  [self getPriceToBeatHeaderText]
    if ([priceToBeat length]){
        [cell.btnPriceToBeat setTitle:[NSString stringWithFormat:@" PRICE-TO-BEAT: %@",priceToBeat ] forState:UIControlStateNormal];
    }
    else{
        [cell.btnPriceToBeat setTitle:@" PRICE-TO-BEAT: Not Available" forState:UIControlStateNormal];
    }
//    // get total width of the price - to- beat view
//    self.headerCell.coTextPriceToBeatWidth.constant = [self getPriceToBeatLabelSizeWithLabel:self.headerCell.labelPriceToBeat].width + 10;
//    float totalWidth = self.headerCell.imageViewIcon.frame.size.width + 10.0f + self.headerCell.labelTitlePriceToBeat.frame.size.width + self.headerCell.coTextPriceToBeatWidth.constant;
//    self.headerCell.coViewPriceToBeatWidth.constant = totalWidth;
//    
//    // set the text at the middle of the view
//    self.headerCell.coPriceToBeatLeft.constant = ( 300.0f - totalWidth ) / 2;
    
}

-(NSString*)getPriceToBeatHeaderText
{
    if ([self.airShop.benchmark.price doubleValue] || [self.airShop.travelPointsInBank intValue]){
        NSString *priceToBeat = nil;
        if ([self.airShop.benchmark.price doubleValue]) {
            priceToBeat = [FormatUtils formatMoney:[self.airShop.benchmark.price stringValue] crnCode:self.airShop.benchmark.crnCode];
            NSUInteger index = [priceToBeat rangeOfString:@"."].location;
            if (index > 0 && index < priceToBeat.length)
            {
                priceToBeat = [priceToBeat substringToIndex:index];
            }
            
            return priceToBeat;
        }
    }
    return nil;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [[self.fetchedResultsController sections] count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
    return [sectionInfo numberOfObjects];
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 194.0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    Fusion14FlightSearchResultsTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Fusion14FlightSearchResultsTableViewCell"];
    
    // Configure the cell...
    [self configureCell:cell atIndexPath:indexPath];

    return cell;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:newIndexPath];
    self.entityAirFilterSummary = (EntityAirFilterSummary *)managedObject;
    
    // for reality, need to check  the violation rules, but for fusion, just skip it for now
    //    if ([self.entityAirFilterSummary maxEnforcementLevel] != nil && [[self.entityAirFilterSummary maxEnforcementLevel] intValue] != kViolationAutoFail)
    self.flightDetailVC.airShopResults = self.airShopResults;
    self.flightDetailVC.airSummary = self.entityAirFilterSummary;
    self.flightDetailVC.airShop = self.airShop;
    self.flightDetailVC.travelDate = self.headerCell.lblTravelDates.text;
    self.flightDetailVC.flightToCity = self.headerCell.lblArrivalCity.text;
    self.flightDetailVC.returnToCity = self.headerCell.lblDepartureCity.text;
}



#pragma mark - Cell Config
- (void)configureCell:(Fusion14FlightSearchResultsTableViewCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    NSString* cellId = [NSString stringWithFormat:@"%ld", (long)[indexPath row]];
    //    NSLog(@"configure cell for %@", cellId);
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityAirFilterSummary *entity = (EntityAirFilterSummary *)managedObject;
    
    if (indexPath.row < 4) {
        cell.lblAirlineName.text = @"United";
        cell.lblPrice.text = @"$216";
        cell.ivAirlineLogo.image = [UIImage imageNamed:@"fusion14_icon_united_airline.png"];
        cell.lblDepartureAirportCode.text = @"SEA";
        cell.lblArrivalAirportCode.text = @"SFO";
        cell.lblReturnDepartureAirportCode.text = @"SFO";
        cell.lblReturnArrivalAirportCode.text = @"SEA";
        cell.lblNumberofStops.text = @"Nonstop";
        cell.lblReturnNumberOfStops.text = @"Nonstop";

        cell.lblTravelPoints.text = @"+59 pts";
        cell.lblRecommended.hidden = NO;
        
        // Departure time is the same
        cell.lblDepartureTime.text = @"9:41 AM";
        cell.lblArrivalTime.text = @"12:00 PM";
        cell.lblFlightDuration.text = @"2h 19m,";
        
        switch (indexPath.row) {
            case (0):{
                cell.lblReturnDepartureTime.text = @"2:30 PM";
                cell.lblReturnArrivalTime.text = @"4:35 PM";
                cell.lblReturnFlightDuration.text = @"2h 5m,";
                break;
            }
            case (1):{
                cell.lblReturnDepartureTime.text = @"2:30 PM";
                cell.lblReturnArrivalTime.text = @"4:35 PM";
                cell.lblReturnFlightDuration.text = @"2h 5m,";
                break;
            }
            case (2):{
                
                cell.lblReturnDepartureTime.text = @"1:24 AM";
                cell.lblReturnArrivalTime.text = @"3:33 PM";
                cell.lblReturnFlightDuration.text = @"2h 9m,";
                break;
            }
            case 3:{
                cell.lblReturnDepartureTime.text = @"11:52 PM";
                cell.lblReturnArrivalTime.text = @"2:00 PM";
                cell.lblReturnFlightDuration.text = @"2h 8m,";
                break;
            }
            default:
                break;
        }
    }
    else{
    
//    cell.lblRecommended.hidden = YES;
    if([self.airShopResults.airline isEqualToString:@"ZZZZZZZZTOTAL"])
    {
        cell.lblAirlineName.text = (self.vendors)[entity.airlineCode];
        NSString *price = [FormatUtils formatMoney:[entity.fare stringValue] crnCode:entity.crnCode];
        NSUInteger index = [price rangeOfString:@"."].location;
        if (index > 0 && index < price.length)
        {
            cell.lblPrice.text = [price substringToIndex:index];
        }
//        UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:entity.airlineCode VendorType:@"a_small" RespondToIV:cell.ivAirlineLogo cellId:cellId];
//        if(gotImg != nil)
//            cell.ivAirlineLogo.image = gotImg;
        cell.ivAirlineLogo.image = [UIImage imageNamed:@"fusion14_icon_united_airline.png"];
    }
    else
    {
            cell.lblAirlineName.text = (self.airShop.vendors)[entity.airlineCode];
            NSString *price = [FormatUtils formatMoney:[entity.fare stringValue] crnCode:entity.crnCode];
            NSUInteger index = [price rangeOfString:@"."].location;
            if (index > 0 && index < price.length)
            {
                cell.lblPrice.text = [price substringToIndex:index];
            }
//            UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:entity.airlineCode VendorType:@"a_small" RespondToIV:cell.ivAirlineLogo cellId:cellId];
//            if(gotImg != nil)
            cell.ivAirlineLogo.image = [UIImage imageNamed:@"fusion14_icon_united_airline.png"];
        }
        
        cell.lblDepartureAirportCode.text = entity.departureIata;
        cell.lblArrivalAirportCode.text = entity.arrivalIata;
        
        [self formatFlightTime:entity.departureTime arrivalDate:entity.arrivalTime labelDeparture:cell.lblDepartureTime labelArrival:cell.lblArrivalTime];
        
        int flightMinutes = [entity.duration intValue];
        int flightHours = flightMinutes / 60;
        if (flightHours > 0)
            flightMinutes = flightMinutes - (flightHours * 60);
        cell.lblFlightDuration.text = [NSString stringWithFormat:@"%dh %dm%@", flightHours, flightMinutes, @","];
        cell.lblNumberofStops.text = [self getFlightStopString:[entity.numStops intValue]];

        cell.lblReturnDepartureAirportCode.text = entity.roundDepartureIata;
        cell.lblReturnArrivalAirportCode.text = entity.roundArrivalIata;
        
        [self formatFlightTime:entity.roundDepartureTime arrivalDate:entity.roundArrivalTime labelDeparture:cell.lblReturnDepartureTime labelArrival:cell.lblReturnArrivalTime];

        
        flightMinutes = [entity.roundDuration intValue];
        flightHours = flightMinutes / 60;
        if (flightHours > 0)
            flightMinutes = flightMinutes - (flightHours * 60);
        cell.lblReturnFlightDuration.text = [NSString stringWithFormat:@"%dh %dm%@", flightHours, flightMinutes, @","];
        cell.lblReturnNumberOfStops.text = [self getFlightStopString:[entity.roundNumStops intValue]];
        
    //    NSString *prefRanking = [entity.pref stringValue];
        
        
        if ([entity.travelPoints intValue] != 0) {
            cell.lblTravelPoints.hidden = NO;
            if ([entity.travelPoints intValue] > 0) {
                cell.lblTravelPoints.text = [NSString stringWithFormat:@"+ %d pts",[entity.travelPoints intValue]];
            }
        }
        else {
            cell.lblTravelPoints.hidden = YES;
        }
        //cell.lblRecommended.hidden = YES;
    //    NSCalendar *calendar = [NSCalendar currentCalendar];
    //    NSDateComponents *components = [calendar components:(NSHourCalendarUnit | NSMinuteCalendarUnit) fromDate:entity.departureTime];
    //    NSInteger hour = [components hour];
    //    NSInteger minute = [components minute];
    //    BOOL isRecommended = (hour == 1 || hour == 2);
        
    //    BOOL isRecommended = [entity.isFusionRecommendedFlight boolValue];
            cell.lblRecommended.hidden = YES;
    }
    
}

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

- (void*)formatFlightTime:(NSDate*)departureDate arrivalDate:(NSDate*)arrivalDate labelDeparture:(UILabel*)lblDeparture labelArrival:(UILabel*)lblArrival
{
    NSString *departureTime = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:departureDate];
    NSString *arrivalTime = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:arrivalDate];
    
    if (departureTime.length > 3) {
        lblDeparture.text = [departureTime substringFromIndex:4];
    } else{
        lblDeparture.text = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:departureDate];
    }
    if (arrivalTime.length > 3) {
        lblArrival.text = [arrivalTime substringFromIndex:4];
    } else {
        lblArrival.text = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:arrivalDate];
    }
}

-(void) fetchAirShopResultsAtIndexPath:(NSIndexPath*)indexPath
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityAirShopResults" inManagedObjectContext:[ExSystem sharedInstance].context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *a = [[ExSystem sharedInstance].context executeFetchRequest:fetchRequest error:&error];
    
    if([a count] > 0){
        self.airShopResults = a[indexPath.row];
    }
}


-(void)didProcessMessage:(Msg *)msg
{
	if ([msg.idKey isEqualToString:AIR_FILTER] )
	{
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        NSArray *aResults = [[AirFilterManager sharedInstance] fetchAll];
        if([aResults count] < 1){
            // Alert
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:nil
                                  message:@"No Flights Found"
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                  otherButtonTitles:nil];
            [alert show];
        }
//        [self setRecommendedFlights];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
//            [self refetchData];
            [self.tableView reloadData];
        });
        
    }
}

-(void)getAllFlightSearchResults
{
    [WaitViewController showWithText:@"" animated:YES];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"   TOTAL", @"AIRLINE", @"*", @"NUMSTOPS", self.airShop, @"AIRSHOP", nil];
    
    [[ExSystem sharedInstance].msgControl createMsg:AIR_FILTER CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

#pragma mark - Fetched results controller
- (NSFetchedResultsController *)fetchedResultsController
{
    if (__fetchedResultsController != nil) {
        return __fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityAirFilterSummary" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"airlineCode LIKE 'UA'"];
    [fetchRequest setPredicate:predicate];
    
    NSSortDescriptor *sortDescriptor = nil;
    NSSortDescriptor *sortDescriptorDate = nil;
    NSSortDescriptor *sort2 = nil;
    
//    if(sortOrder == kSortPrice)
//    {
        sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"isFusionRecommendedFlight" ascending:NO];
        sortDescriptorDate = [[NSSortDescriptor alloc] initWithKey:@"departureTime" ascending:NO];
        sort2 = [[NSSortDescriptor alloc] initWithKey:@"fare" ascending:YES];
        [fetchRequest setSortDescriptors:@[sortDescriptorDate]];
//    }
//    else if(sortOrder == kSortDeparture)
//    {
//        sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"departureTime" ascending:YES];
//        sort2 = [[NSSortDescriptor alloc] initWithKey:@"fare" ascending:YES];
//        NSSortDescriptor *sort3 = [[NSSortDescriptor alloc] initWithKey:@"pref" ascending:YES];
//        [fetchRequest setSortDescriptors:@[sortDescriptor, sort2, sort3]];
//    }
//    else if(sortOrder == kSortPref)
//    {
//        sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"pref" ascending:NO];
//        sort2 = [[NSSortDescriptor alloc] initWithKey:@"fare" ascending:YES];
//        [fetchRequest setSortDescriptors:@[sortDescriptor, sort2]];
//    }
//    else if(sortOrder == kSortDuration)
//    {
//        sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"durationTotal" ascending:YES];
//        sort2 = [[NSSortDescriptor alloc] initWithKey:@"fare" ascending:YES];
//        NSSortDescriptor *sort3 = [[NSSortDescriptor alloc] initWithKey:@"pref" ascending:YES];
//        [fetchRequest setSortDescriptors:@[sortDescriptor, sort2, sort3]];
//    }
    
    
    NSFetchedResultsController *theFetchedResultsController =
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                        managedObjectContext:self.managedObjectContext sectionNameKeyPath:nil
                                                   cacheName:@"Master"];
    self.fetchedResultsController = theFetchedResultsController;
    __fetchedResultsController.delegate = self;
    
    
    return __fetchedResultsController;
}

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
    
    [self.tableView reloadData];
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

#pragma mark - Navigation
// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"ShowFlightDetails"]) {
        self.flightDetailVC = segue.destinationViewController;
    }
}


@end
