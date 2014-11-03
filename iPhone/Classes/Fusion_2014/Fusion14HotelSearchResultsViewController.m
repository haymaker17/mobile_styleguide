//
//  Fusion14HotelSearchResultsViewController.m
//  ConcurMobile
//
//  Created by Sally Yan on 3/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "Fusion14HotelSearchResultsViewController.h"
#import "Fusion14HotelSearchResultsTableViewHeaderCell.h"
#import "Fusion14HotelRoomDetailsViewController.h"
#import "HotelBenchmarkData.h"
#import "Fusion14HotelListCell.h"
#import "LabelConstants.h"
#import "RoundedRectView.h"
#import "Config.h"
#import "CTEError.h"
#import "CTEErrorMessage.h"
#import "CTENetworkSettings.h"
#import "WaitViewController.h"

#import <math.h>

@interface Fusion14HotelSearchResultsViewController ()

@property (nonatomic, strong) NSMutableArray *sections;
@property (nonatomic, strong) NSString *sectionTitle;
@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;
@property (nonatomic, strong) NSFetchedResultsController *fetchedResultsController;

@property (nonatomic, strong) Fusion14HotelSearchResultsTableViewHeaderCell *headerCell;

// variables for polling
@property (nonatomic, strong) NSTimer                     *timer;
@property long ticks;
@property (nonatomic) CFTimeInterval                       pollingStartTime;
@property BOOL readyToSendPollRequest;
@property (nonatomic, strong) UIView                      *coverView;
@property (nonatomic, strong) NSArray                     *hotelBenchmarks;
@property (nonatomic, strong) NSString                    *travelPointsInBank;
@property (nonatomic, strong) NSString                    *hotelBenchmarkRangeString;
@property (nonatomic) BOOL                                 isBenchmarkRange;

// results from hotel recommendations endpoint
@property (nonatomic, strong) NSArray                     *recommendedHotels;
@property (nonatomic, strong)Fusion14HotelRoomDetailsViewController *roomDetailsVC;

@end

@implementation Fusion14HotelSearchResultsViewController

@synthesize fetchedResultsController=__fetchedResultsController;

// keys for sections
NSString *const kRecommendations = @"Recommended Hotels";
NSString *const kSearchResults = @"Hotel Search Results";

#define POLLING_TIMEOUT             90
#define POLLING_INTERVAL            2

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

#pragma mark - view controller methods
- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // set up navigation bar buttons at right hand side
    [self setNavigationBarRightButtons];

    [WaitViewController showWithText:nil animated:nil];

    [self refetchData]; // make sure get the fetchedResultsController to fetch data.
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    self.title = @"Hotels";
   [self setTableViewHeader];
    
    if (self.pollingView)
    {
        // Addressing an iOS7 issue where the polling view failed to appear.
        [self.view bringSubviewToFront:self.coverView];
        [self.view bringSubviewToFront:self.pollingView];
    }
    [self.navigationController setToolbarHidden:YES];
}

- (void)viewDidUnload {
    self.fetchedResultsController = nil;
}


- (void) setNavigationBarRightButtons
{
    UIButton *btnMoreView = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 27, 27)];
    //    [moreButton addTarget:self action:nil forControlEvents:UIControlEventTouchUpInside];
    [btnMoreView setBackgroundImage:[UIImage imageNamed:@"fusion14_icon_nav_more"] forState:UIControlStateNormal];
    UIBarButtonItem *btnMore = [[UIBarButtonItem alloc] initWithCustomView:btnMoreView];
    
    UIButton *btnMapView = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 27, 27)];
    //    [btnMapView addTarget:self action:nil forControlEvents:UIControlEventTouchUpInside];
    [btnMapView setBackgroundImage:[UIImage imageNamed:@"icon_nav_map"] forState:UIControlStateNormal];
    UIBarButtonItem *btnMap = [[UIBarButtonItem alloc] initWithCustomView:btnMapView];

    [self.navigationItem setRightBarButtonItems:[NSArray arrayWithObjects:btnMap, btnMore, nil]];
    
}

#pragma mark - Navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"Fusion14HotelRoomDetailsViewController"]) {
        self.roomDetailsVC = segue.destinationViewController;
    }
}

#pragma mark - set up view header
-(void)setTableViewHeader
{
    self.tableView.tableHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, self.tableView.frame.size.width, 108.0f)];
    
    self.headerCell = [self.tableView dequeueReusableCellWithIdentifier:@"HotelSearchResultsTableViewHeaderCell"];
    [self updateHeader];
    
    // TODO: set the loaction and date for the hotel search
    [self.tableView dequeueReusableHeaderFooterViewWithIdentifier:@"HotelSearchResultsTableViewHeaderCell"];
    [self.tableView.tableHeaderView addSubview:self.headerCell];
}


-(void) updateHeader
{
//    if ([self.hotelBenchmarkRangeString length] || [self.travelPointsInBank length]){
//        self.headerCell.labelPriceToBeat.text = [self getPriceToBeatHeaderAttributedText];
//    }
    [self setPriceToBeatLabel];
    
    NSString *location = [NSString stringWithFormat:@"%@", self.hotelSearch.hotelSearchCriteria.locationResult.location];
    NSString *locationWithoutCountry = nil;
//     = [NSString stringWithFormat:@"%@", self.hotelSearch.hotelSearchCriteria.locationResult.location];
    
    NSRange lastComma = [location rangeOfString:@"," options:NSBackwardsSearch];
    
    if(lastComma.location != NSNotFound) {
        locationWithoutCountry = [location substringToIndex:lastComma.location];
    }
    self.headerCell.labelLocation.text = locationWithoutCountry;

    self.headerCell.labelDate.text = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateEEEMMMddByDate:self.hotelSearch.hotelSearchCriteria.checkinDate], [DateTimeFormatter formatDateEEEMMMddByDate:self.hotelSearch.hotelSearchCriteria.checkoutDate]];
}

-(void)setPriceToBeatLabel
{
//    if ([self.hotelBenchmarkRangeString length] || [self.travelPointsInBank length]){
//        self.headerCell.labelPriceToBeat.text = [self getPriceToBeatHeaderAttributedText];
//    }
//    else{
//        self.headerCell.labelPriceToBeat.text = @"";
//    }
    // =========== Hard Code price to beat for Fusion =========
    self.headerCell.labelPriceToBeat.text = @"$426";
        // get total width of the price - to- beat view
        self.headerCell.coTextPriceToBeatWidth.constant = [self getPriceToBeatLabelSizeWithLabel:self.headerCell.labelPriceToBeat].width + 10;
        float totalWidth = self.headerCell.imageViewIcon.frame.size.width + 10.0f + self.headerCell.labelTitlePriceToBeat.frame.size.width + self.headerCell.coTextPriceToBeatWidth.constant;
        self.headerCell.coViewPriceToBeatWidth.constant = totalWidth;
        
        // set the text at the middle of the view
        self.headerCell.coPriceToBeatLeft.constant = ( 300.0f - totalWidth ) / 2;
    
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    id <NSFetchedResultsSectionInfo> sectionInfo = [__fetchedResultsController sections][section];
//    ALog(@"[sectionInfo numberOfObjects] = %d", [sectionInfo numberOfObjects]);
    return [sectionInfo numberOfObjects];
}


-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 110.0f;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    Fusion14HotelListCell *hotelListCell = [tableView dequeueReusableCellWithIdentifier:@"HotelListCell" forIndexPath:indexPath];
    
//    NSInteger section = [indexPath section];
//    EntityHotelBooking *hotel = nil;
//    if (section == 0 && self.recommendedHotels != nil && [indexPath row] < 3) {
//        hotel = [self.recommendedHotels objectAtIndex:[indexPath row]];
//    }
//    else
//    {
//        // since there is only one section in the core data, so need to change teh section to 0
//        NSIndexPath *newIndexPath = [NSIndexPath indexPathForItem:[indexPath row] inSection:0];
//        NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:newIndexPath];
//        hotel = (EntityHotelBooking *)managedObject;
//    }
    
    [self configurCell:hotelListCell forRowAtIndexPath:indexPath];
    return hotelListCell;
}


-(void)configurCell:(Fusion14HotelListCell*)cell forRowAtIndexPath:(NSIndexPath*)indexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityHotelBooking *hotel = (EntityHotelBooking *)managedObject;
    
    cell.hotelName.text = hotel.hotel;
    
    // set price
    NSString *currencyCodeForCheapestRate = [hotel.cheapestRoomRate floatValue] == [hotel.relCheapRoom.rate floatValue] ? hotel.relCheapRoom.crnCode : hotel.relCheapRoomViolation.crnCode;
    if (![currencyCodeForCheapestRate length]) {
        cell.hotelPrice.text = @"$309";
//        cell.hotelPrice.font = [cell.hotelPrice.font fontWithSize:12.0];
    }
    else{
        cell.hotelPrice.hidden = NO;
        cell.hotelPrice.font = [cell.hotelPrice.font fontWithSize:20.0];
        NSString *price = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", [hotel.cheapestRoomRate floatValue]] crnCode:currencyCodeForCheapestRate];
        int index = [price rangeOfString:@"."].location;
        if (index > 0 && index < price.length) {
            cell.hotelPrice.text = [price substringToIndex:index];
        }
    }
    
    // distance unit
    NSString *distanceUnit = [hotel.distanceUnit isEqualToString:@"mi"]?@"miles" : hotel.distanceUnit;
    cell.hotelDistance.text = [NSString stringWithFormat:@"%@ %@", hotel.distance, distanceUnit];
    
    // set up the location
    NSString *cityAndState = nil;
    if ([hotel.city length]) {
        cityAndState = hotel.city;
    }
    if ([hotel.state length]){
        if (cityAndState != nil) {
            cityAndState = [[cityAndState stringByAppendingString:@", "] stringByAppendingString:hotel.state];
        }
        else{
            cityAndState = hotel.state;
        }
    }
    if (cityAndState != nil) {
        cell.hotelCityAndState.text = cityAndState;
    }

    // get the rating stars image
    int asterisks = [hotel.starRating intValue];// .starRatingAsterisks;
    if (asterisks == 0){
        cell.hotelRating.hidden = YES;
    }
    else
    {
        cell.hotelRating.hidden = NO;
        int starCount = asterisks;
        if(starCount == 1)
            cell.hotelRating.image = [UIImage imageNamed:@"fusion14_hotel_one_star"];
        else if(starCount == 2)
            cell.hotelRating.image = [UIImage imageNamed:@"fusion14_hotel_two_star"];
        else if(starCount == 3)
            cell.hotelRating.image = [UIImage imageNamed:@"fusion14_hotel_three_star"];
        else if(starCount == 4)
            cell.hotelRating.image = [UIImage imageNamed:@"fusion14_hotel_four_star"];
        else if(starCount == 5)
            cell.hotelRating.image = [UIImage imageNamed:@"fusion14_hotel_five_star"];
    }
    
    // display label for recommendation, preffered
    NSInteger row = [indexPath row];
//    if (row < 3){

    if (row < 3 && [hotel.isFusion14Recommended isEqualToNumber:@YES]) {
        cell.hotelPreferred.text = @"Recommended";
        cell.hotelPreferred.backgroundColor = [UIColor colorWithRed:98.0/255 green:172.0/255 blue:100.0/255 alpha:1.0];
        [cell.hotelPreferred setHidden:NO];
    }

    else if ([hotel.hotelPrefRank intValue] > 4 ){
        cell.hotelPreferred.text = @"Preferred";
        cell.hotelPreferred.backgroundColor = [UIColor colorWithRed:0/255 green:120.0/255 blue:200.0/255 alpha:1.0];
        [cell.hotelPreferred setHidden:NO];
    }
    else {
        [cell.hotelPreferred setHidden:YES];
    }
    
    if (![cell.hotelPreferred isHidden]) {
        CGSize textSize = [cell.hotelPreferred.text sizeWithFont:cell.hotelPreferred.font];
        cell.coHotelPreferredWidth.constant = textSize.width + 6;
        cell.coHotelPreferredHeight.constant = textSize.height+ 6;
    }
    
    // set up travel points
    int travelPoints = [hotel.travelPoints intValue];
    if (travelPoints > 0) {
        cell.travelPoints.hidden = NO;
        cell.travelPoints.text = [NSString stringWithFormat:@"+%i pts", travelPoints];
    } else{
        cell.travelPoints.hidden = YES;
    }
    
    if ([cell.hotelName.text isEqualToString:@"InterContinental Mark Hopkins San Francisco"])
    {
        cell.travelPoints.text = @"+59 pts";
        [cell.travelPoints setHidden:NO];
    }
    
    // get the hotel image
    cell.hotelImage.image = nil;  // The cell may have been dequeued with an image already in it.  Get rid of it.
    if ([hotel.relHotelImage count] > 0)
    {
        for(EntityHotelImage *image in hotel.relHotelImage)
        {
            UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
            [[ExSystem sharedInstance].imageControl getImageAsynchWithUrl:image.thumbURI RespondToImage:img IV:cell.hotelImage];
            break;
        }
    }
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityHotelBooking *entity = (EntityHotelBooking*)managedObject;
    [self showRoomsForSelectedHotel:entity viewController:self.roomDetailsVC];
    
}

- (void)showRoomsForSelectedHotel: (EntityHotelBooking*)hotelBooking viewController:(Fusion14HotelRoomDetailsViewController*)roomDetailsViewController
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: hotelBooking, @"HOTEL_BOOKING", self.hotelSearch, @"HOTEL_SEARCH", self.hotelSearch.hotelSearchCriteria, @"HOTEL_SEARCH_CRITERIA", @"YES", @"SKIP_CACHE", nil];
	
    //	// If hotel details (which includes room data) is already available, then don't send another request to the server
	if (hotelBooking.relHotelRoom != nil && [hotelBooking.relHotelRoom count] > 0) {
		pBag[@"SHORT_CIRCUIT"] = @"YES";
    }
    
    // call the view before pushing it onto the navigation controller, otherwise the loading waitscreen detects a presentingViewController and doesn't resize correctly.
    [self.roomDetailsVC view]; // added this for iOS7, so that the outlets are set before 'respondToFoundData:' message is sent
    
    if (pBag[@"SHORT_CIRCUIT"] != nil) {
        Msg *msg = [[Msg alloc] init];
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        [roomDetailsViewController didProcessMessage:msg];
    }
    else {
        [[ExSystem sharedInstance].msgControl createMsg:FIND_HOTEL_ROOMS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:roomDetailsViewController];
    }
}

#pragma mark - price to beat

-(NSString *)getPriceToBeatHeaderAttributedText
{
    NSString *priceToBeat = nil;
    NSString *roundedPriceToBeat = nil;
    NSString *travelPoints = self.travelPointsInBank;
    if ([self.hotelBenchmarkRangeString length])
    {
        priceToBeat = self.hotelBenchmarkRangeString;
        
//        if ([travelPoints intValue]) {
            NSArray *priceArray = [travelPoints componentsSeparatedByString:@"-"];
        if ([priceArray count] > 1) {
            for( NSString *price in priceArray){
                int index = [price rangeOfString:@"."].location;
                if (index > 0 && index < price.length) {
                    if (roundedPriceToBeat == nil) {
                        roundedPriceToBeat = [price substringToIndex:index];
                    }else{
                        roundedPriceToBeat = [roundedPriceToBeat stringByAppendingString:@"-"];
                        roundedPriceToBeat = [roundedPriceToBeat stringByAppendingString:[price substringToIndex:index]];
                    }
                }
            }
        }
        else{
            int index = [priceToBeat rangeOfString:@"."].location;
            roundedPriceToBeat = [priceToBeat substringToIndex:index];
        }

            roundedPriceToBeat = [roundedPriceToBeat stringByAppendingString:@"\n"];
        }
//    }
    
    if ([travelPoints intValue]) {
        NSString *pointsText = [NSString stringWithFormat:@"Travel Points Bank: %@ points", travelPoints];
        roundedPriceToBeat = [roundedPriceToBeat stringByAppendingString:pointsText];
    }
    
    // ========= Hard coded for Fusion 2014 for SFO hotel =======
    
    return @"$426";
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

- (CGSize)getPriceToBeatLabelSizeWithLabel:(UILabel*)label
{
    CGSize maxSize = CGSizeMake(130.0f, 25.0f);
    CGSize labelSize = [label.text sizeWithFont:label.font constrainedToSize:maxSize lineBreakMode:label.lineBreakMode];
    CGRect newFrame = label.frame;
    newFrame.size.width = labelSize.width;
    
    // reset label frame
    [label setFrame:newFrame];
    return label.frame.size;
}

//-(UIView*)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
//{
//    NSString *sectionKey = self.sections[section];
//    
//    if ([sectionKey isEqualToString:kRecommendations]){
//        self.sectionTitle = @"RECOMMENDED";
//    }
//    else{
//        self.sectionTitle = @"SEARCH RESULTS";
//    }
//    return [self getHeaderViewWithTitle:self.sectionTitle];
//}


//-(CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
//{
//    CGFloat headerHeight = [self getHeaderViewWithTitle:self.sectionTitle].frame.size.height;
//    return headerHeight;
//}
//
//
//-(UIView*)getHeaderViewWithTitle:(NSString*)title;
//{
//    UIView *headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.tableView.bounds.size.width, 25)];
//    [headerView setBackgroundColor:[UIColor grayColor]];
//    UILabel *headerTitle = [[UILabel alloc] initWithFrame:CGRectMake(15, 2.5, 200, 20)];
//    
//    [headerTitle setText:title];
//    [headerTitle setTextColor:[UIColor concurBlueColor]];
//    [headerTitle setFont:[UIFont fontWithName:@"Helvetica Neue" size:10.0]];
//    [headerTitle setTextAlignment:NSTextAlignmentLeft];
//    
//    [headerView addSubview:headerTitle];
//    return headerView;
//}


#pragma mark - Fetched results controller
- (NSFetchedResultsController *)fetchedResultsController
{
    if (__fetchedResultsController != nil) {
        return __fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityHotelBooking" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSArray *sortDescriptors;
    sortDescriptors = @[[[NSSortDescriptor alloc] initWithKey:@"isFusion14Recommended" ascending:NO],
                        [[NSSortDescriptor alloc] initWithKey:@"hotelPrefRank" ascending:NO],
                        [[NSSortDescriptor alloc] initWithKey:@"starRating" ascending:NO],
                        [[NSSortDescriptor alloc] initWithKey:@"isAddtional" ascending:YES],
                        [[NSSortDescriptor alloc] initWithKey:@"cheapestRoomRate" ascending:YES],
                        [[NSSortDescriptor alloc] initWithKey:@"distance" ascending:YES],
                        [[NSSortDescriptor alloc] initWithKey:@"hotel" ascending:YES]];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    NSFetchedResultsController *theFetchedResultsController =
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                        managedObjectContext:self.managedObjectContext sectionNameKeyPath:nil
                                                   cacheName:nil];
    self.fetchedResultsController = theFetchedResultsController;
    __fetchedResultsController.delegate = self;
    
    return __fetchedResultsController;
}

-(void)setFetchedResultsController:(NSFetchedResultsController*)newValue
{
	__fetchedResultsController = newValue;
}


#pragma mark - Fetched results controller delegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    //self hideWaitView];
    [self.tableView beginUpdates];
}

-(void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableView endUpdates];
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

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type newIndexPath:(NSIndexPath *)newIndexPath
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
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"Fusion14HotelSearchResultsViewController::viewDidLoad: fetchedResultsController %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
        }
	}
}


#pragma - responses from server
-(void) didProcessMessage:(Msg *)msg
{
    [[MCLogging getInstance] log:@"HotelSearchResultsViewController::respondToFoundData" Level:MC_LOG_DEBU];
    
	if (msg.parameterBag != nil && (msg.parameterBag)[@"SHOW_HOTELS"] != nil &&	(msg.parameterBag)[@"HOTEL_SEARCH"] != nil)
    {
		self.hotelSearch = (HotelSearch*)(msg.parameterBag)[@"HOTEL_SEARCH"];
        
        // Check if a handled error was returned by Search3
        FindHotels *findHotels = (FindHotels*)msg.responder;
        self.headerCell.labelPriceToBeat.text = @"";

        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [NSThread sleepForTimeInterval:5.0f];
            dispatch_async(dispatch_get_main_queue(), ^{
                [WaitViewController hideAnimated:YES withCompletionBlock:nil];

                self.headerCell.labelPriceToBeat.text = @"$426";


                if ([findHotels.commonResponseUserMessage length])
                {
                    [self killTimer];

                    // Display error to the customer
                    UIAlertView *alert = [[MobileAlertView alloc]
                                          initWithTitle:[Localizer getLocalizedText:@"Search Failed"]
                                          message:[findHotels commonResponseUserMessage]
                                          delegate:self
                                          cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
                                          otherButtonTitles:nil];
                    [alert show];
                    [WaitViewController hideAnimated:YES withCompletionBlock:nil];
                    //            [self destroyPollingView];
                    self.navigationController.toolbarHidden = YES;
                }
                else
                {
                    if (self.hotelSearch.isPolling){
                        // We are using polling/hotel-streaming
                        if (self.hotelSearch.isFinal){
                            // if we have received a final flag, then kill the timer to stop checking for a timeout
                            if (self.timer){
                                [self killTimer];
                            }
                        }
                        else{
                            // Set the flag so that we can send a new poll request
                            self.readyToSendPollRequest = YES;
                        }
                    }

                    // Select the first hotel result by default
                    if (self.hotelSearch.hotels != nil && [self.hotelSearch.hotels count] > 0)
                        [self.hotelSearch selectHotel:0];

                    if ([findHotels.hotelBenchmarks count]) {
                        self.hotelBenchmarks = findHotels.hotelBenchmarks;
                    }
                    if ([findHotels.travelPointsInBank length]) {
                        self.travelPointsInBank = findHotels.travelPointsInBank;
                    }

                    if (!self.hotelSearch.isPolling || self.hotelSearch.isFinal)
                    {
                        [self updateHeader];

                        [self setTemporaryHotelRecommendation];

                        // enable the map/list switch navigation button
                        //                self.navigationItem.rightBarButtonItem = self.switchButton;

                        // Show the back button and enable it again
                        //                [self.activeViewController updateToolbar];
                        //                [self.navigationItem setHidesBackButton:NO];

                        // Remove the hotel rate pooling wait screen
                        //                [self destroyPollingView];
                        [WaitViewController hideAnimated:YES withCompletionBlock:nil];

                        // On rare occasions MWS sends back a message saying that it is finished, when no rates have been found
                        // But this only happens for streaming
                        if (self.hotelSearch.isPolling && self.hotelSearch.ratesFound == NO) {
                            // No rates were received, so warm the user and return to previous VC
                            [self noRatesReceived];
                        }
                        self.navigationController.toolbarHidden = YES;
                    }

                    // If the poll flag is set, and we have not reached the timeout
                    if (self.readyToSendPollRequest && self.ticks < POLLING_TIMEOUT)
                    {
                        // If the timer is nil, then we are about to send the first poll request
                        if (!self.timer || !self.timer.isValid){
                            //                    [self createPollingView];
                            
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

            });
        });

        
    }
    else if ([msg.idKey isEqualToString:FIND_HOTELS])
    {
        // Check if a handled error was returned by Search2
        FindHotels *findHotels = (FindHotels*)msg.responder;
        if ([findHotels.errorMessage length])
        {
//                [self cancelSearch:self];
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Search Failed"]
                                  message:[findHotels errorMessage]
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
                                  otherButtonTitles:nil];
            [alert show];
        }
        else
        {
            NSArray *aHotels = [[HotelBookingManager sharedInstance] fetchAll];

            //			FindHotels *findHotels = (FindHotels*)msg.responder;
            //			self.hotelSearch = findHotels.hotelSearch;
//                [self cancelSearch:self];
            if ([aHotels count] == 0)
            {
                UIAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:[Localizer getLocalizedText:@"HOTEL_VIEW_NO_HOTELS_TITLE"]
                                      message:[Localizer getLocalizedText:@"HOTEL_VIEW_NO_HOTELS_MESSAGE"]
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:@"Cancel"]
                                      otherButtonTitles:nil];
                [alert show];
            }
            else
            {
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SHOW_HOTELS", self.hotelSearch, @"HOTEL_SEARCH", @"YES", @"SHORT_CIRCUIT", nil];
                pBag[@"HOTEL_SEARCH_CRITERIA"] = (msg.parameterBag)[@"HOTEL_SEARCH_CRITERIA"];
                pBag[@"TOTAL_COUNT"] = @(findHotels.totalCount);

                msg.parameterBag = pBag;
                msg.idKey = @"SHORT_CIRCUIT";
                [self didProcessMessage:msg];
            }
        }
    }
}

#pragma mark - set up hotel recommendations
-(void)setTemporaryHotelRecommendation
{
    NSFetchRequest *fetchRequest=[NSFetchRequest fetchRequestWithEntityName:@"EntityHotelBooking"];
//    NSArray *predicateArray = @[@"11", @"28331", @"1151"];
//    NSPredicate *predicate = [NSCompoundPredicate predicateWithFormat:@"propertyId == %@" argumentArray:predicateArray];

   
//    NSPredicate *predicate = [NSPredicate pr:@"(lat < %f && lng < %f && addr1 CONTAINS %@)", latitude, longitude, hotelRecommended.address1];
//    fetchRequest.predicate = predicate;
    

   NSArray *hotelsArray = [self.managedObjectContext executeFetchRequest:fetchRequest error:nil];
    if (hotelsArray != nil) {
        for(EntityHotelBooking *hotel in hotelsArray){
            if ([hotel.propertyId isEqualToString:@"11"] || [hotel.propertyId isEqualToString:@"28331"] || [hotel.propertyId isEqualToString:@"1151"]) {
                [hotel setValue:@YES forKey:@"isFusion14Recommended"];
            }
        }
        [self saveEntityHotelBooking];
    }

//    if (hotelBooking != nil) {
//        double score = [hotelRecommended.totalScore doubleValue];
//        [hotelBooking setValue:[NSNumber numberWithDouble:score] forKey:@"fusion14RecommendationScore"];
//    }
}

-(void)setFusion14RecommendedHotels
{
    // get the top 3 recommended hotels, sorted by score
    NSFetchRequest *fetchRequestForRecHotels=[NSFetchRequest fetchRequestWithEntityName:@"EntityHotelBooking"];
    NSPredicate *predicateWithScore = [NSPredicate predicateWithFormat:@"fusion14RecommendationScore > 0"];
    NSSortDescriptor *sorter = [[NSSortDescriptor alloc] initWithKey:@"fusion14RecommendationScore" ascending:NO];
    [fetchRequestForRecHotels setFetchLimit:3];
    fetchRequestForRecHotels.predicate = predicateWithScore;
    [fetchRequestForRecHotels setSortDescriptors:[NSArray arrayWithObjects:sorter, nil]];
    
    // set the flag isFusion14Recommended == YES for the top 3 recommended hotel
    self.recommendedHotels = [self.managedObjectContext executeFetchRequest:fetchRequestForRecHotels error:nil];
    if (self.recommendedHotels != nil) {
        for(EntityHotelBooking *hotel in self.recommendedHotels){
            [hotel setValue:@YES forKey:@"isFusion14Recommended"];
        }
        [self saveEntityHotelBooking];
    }
}

-(void)saveEntityHotelBooking
{
    NSError *error;
    if (![self.managedObjectContext save:&error]){
        ALog(@"Whoops, couldn't save object: %@", [error localizedDescription]);
    }else{
        DLog(@"successfully save changes on updating hotel recommendation score.")
    }
}

#pragma mark - Polling Message

// Send the hotel polling message
-(void) sendHotelPollingMsg
{
	[[MCLogging getInstance] log:@"HotelSearchResultsViewController::sendHotelPollingMsg" Level:MC_LOG_DEBU];
//    self.hotelSearch.hotelSearchCriteria.locationResult.latitude = @"37.7879";
//    self.hotelSearch.hotelSearchCriteria.locationResult.longitude = @"122.4072";
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.hotelSearch, @"HOTEL_SEARCH", @"YES", @"SHOW_HOTELS", @"YES", @"SKIP_CACHE", @"0", @"STARTPOS", @"300", @"NUMRECORDS", self.hotelSearch.pollingID, @"POLLINGID", nil];
    [[ExSystem sharedInstance].msgControl createMsg:FIND_HOTELS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
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
	[[MCLogging getInstance] log:@"Fusion14HotelSearchResultsViewController::timerCallback" Level:MC_LOG_DEBU];
    self.ticks++;
    // Check if we have reached the polling timeout
    if (self.ticks >= POLLING_TIMEOUT && self.timer != nil)
    {
        [self killTimer];
        
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

#pragma mark - Polling stuffs
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

// Create the cancel button used to cancel hotel polling
-(void)makeCancelButton
{
    self.navigationController.toolbarHidden = NO;
    
    UIBarButtonItem *btnCancel = nil;
//    if ([ExSystem is7Plus])
//    {
        btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN] style:UIBarButtonSystemItemCancel target:self action:@selector(btnCancel:)];
        [btnCancel setTintColor:[UIColor redColor]];
//    }
//    else
//        btnCancel = [ExSystem makeColoredButton:@"RED" W:100 H:30.0 Text:[Localizer getLocalizedText:LABEL_CANCEL_BTN]  SelectorString:@"btnCancel:" MobileVC:self];
    
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSArray *toolbarItems = @[flexibleSpace, btnCancel, flexibleSpace];
	[self setToolbarItems:toolbarItems animated:YES];
}

// Method called when the cancel button is clicked to stop the hotel polling
-(IBAction)btnCancel:(id)sender
{
    [self killTimer];
    [WaitViewController hideAnimated:YES withCompletionBlock:nil];
//    [self destroyPollingView];
    [self.navigationController popViewControllerAnimated:YES];
    
}

@end
