//
//  HotelListViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/23/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelListViewController.h"
#import "ExSystem.h" 

#import "HotelSearchResultsViewController.h"
#import "FindHotels.h"
#import "HotelListCell.h"
#import "HotelDescriptor.h"
#import "ExSystem.h" 

#import "LabelConstants.h"
#import "MobileActionSheet.h"
#import "HotelSearch.h"
#import "HotelSearchCriteria.h"
#import "Config.h"
#import "UserConfig.h"
#import "HotelBenchmarkResultsVC.h"

@implementation HotelListViewController

@synthesize fetchedResultsController=__fetchedResultsController;

#define SORT_BY_PREFERRED_VENDORS	0
#define SORT_BY_VENDOR_NAMES		1
#define SORT_BY_PRICE				2
#define SORT_BY_DISTANCE			3
#define SORT_BY_RATING				4
#define SORT_BY_MOST_RECOMMENDED    5

#pragma mark -
#pragma mark Initialization
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // MOB-7867 Make sure MOC is valid in respondToFoundData, before viewDidLoad is called.
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*)[[UIApplication sharedApplication] delegate];
        self.managedObjectContext = [ad managedObjectContext];
    }
    return self;
}

#pragma mark - Seed
- (IBAction)priceToBeatHeaderInfoClicked:(UIButton *)sender {
    HotelBenchmarkResultsVC *vc = [[HotelBenchmarkResultsVC alloc] initWithTitle:[@"Price to Beat" localize]];
    NSMutableString *headerText = [[NSMutableString alloc] init];
    if ([self.hotelSearchMVC.hotelBenchmarkRangeString length]) {
        [headerText appendFormat:(self.hotelSearchMVC.isBenchmarkRange ? [@"The Price-to-Beat range is between %@." localize] : [@"The Price-to-Beat is %@." localize]),self.hotelSearchMVC.hotelBenchmarkRangeString];
        [headerText appendString:@"\n"];
    }
    if ([[UserConfig getSingleton].travelPointsConfig[@"HotelTravelPointsEnabled"] boolValue]) {
        [headerText appendString:[@"BOOKING_UNDER_P2B_EARNS_POINTS" localize]];
    }
    else {
        [headerText appendString:[@"BOOKING_UNDER_P2B_SAVES_MONEY" localize]];
    }
    vc.headerText = headerText;
    vc.benchmarksList = self.hotelSearchMVC.hotelBenchmarks;
    [self.hotelSearchMVC.navigationController pushViewController:vc animated:YES];
    
    [self logFlurryEventsForTravelPoints];
}

- (void)logFlurryEventsForTravelPoints
{
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    dict[@"Type"] = @"Hotel";
    dict[@"From Screen"] = @"Hotel List";
    [Flurry logEvent:@"Price-to-Beat: Price-to-Beat Range Viewed" withParameters:dict];
}

-(void) setSeedData
{
    self.sortOrder = SORT_BY_PREFERRED_VENDORS;
}

#pragma mark -
#pragma mark Notifications
-(void)respondToFoundData:(Msg *)msg
{
    [self hideLoadingView];
    [self refetchData];
    [self notifyChange];
    [self updateToolbar];
}

-(void)notifyChange
{
	[super notifyChange];
    
    [self setPriceToBeatHeader];
    
	[self populateSortedHotels];
	
	[self sortHotels];
    
    [self.tblView reloadData];
}

-(void)setSortOrderBasedOnRecommendations
{
    if ([[HotelBookingManager sharedInstance] isAnyHotelRecommended])
        self.sortOrder = SORT_BY_MOST_RECOMMENDED;
    else if (self.sortOrder == SORT_BY_MOST_RECOMMENDED)
        [self setSeedData];
}

-(void)setPriceToBeatHeader
{
    // Need to check if there is a benchmark to display or not
    if ([self.hotelSearchMVC.hotelBenchmarkRangeString length] || [self.hotelSearchMVC.travelPointsInBank intValue])
    {
        self.lblBenchmark.frame = CGRectMake(self.lblBenchmark.frame.origin.x, self.lblBenchmark.frame.origin.y, self.tblView.bounds.size.width - 35, self.lblBenchmark.frame.size.height);
        self.lblBenchmark.attributedText = [self getPriceToBeatHeaderAttributedText];
        [self.lblBenchmark sizeToFit];
        CGFloat tableHeaderHeight = self.lblBenchmark.frame.origin.y + self.lblBenchmark.frame.size.height + 8;
        self.viewForTableViewHeader.frame = CGRectMake(self.viewForTableViewHeader.frame.origin.x, self.viewForTableViewHeader.frame.origin.y
                                                       , self.viewForTableViewHeader.frame.size.width, tableHeaderHeight);
        //        CGFloat yForInfoButton = self.lblBenchmark.frame.origin.y + ((self.lblBenchmark.frame.size.height - self.priceToBeatInfoButton.frame.size.height) / 2);
        //        self.priceToBeatInfoButton.frame = CGRectMake(self.priceToBeatInfoButton.frame.origin.x, yForInfoButton, self.priceToBeatInfoButton.frame.size.width, self.priceToBeatInfoButton.frame.size.height);
        self.tblView.tableHeaderView = self.viewForTableViewHeader;
        self.viewForTableViewHeader.hidden = NO;
    }
    else
    {
        self.viewForTableViewHeader.hidden = YES;
        self.tblView.tableHeaderView = nil;
    }
}


#pragma mark -
#pragma mark View lifecycle
- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if (self.managedObjectContext == nil)
    {
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate *)[[UIApplication sharedApplication] delegate];
        self.managedObjectContext = [ad managedObjectContext];
    }
    
    [self setSortOrderBasedOnRecommendations];
    
    [self refetchData];
	
	//tblView.allowsSelection = NO;
	
	if([UIDevice isPad])
		self.title = [Localizer getLocalizedText:@"HOTEL_SEARCH_RESULTS"];
	
	// Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    self.lblSubheading.text = @"";
    self.lblHeader.text = @"";
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self setPriceToBeatHeader];
	
	[self populateSortedHotels];
	[self sortHotels];

	[self.tblView reloadData];
}
- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    if (self.lastIndexPath != nil)
    {
        [self.tblView selectRowAtIndexPath:self.lastIndexPath animated:NO scrollPosition:UITableViewScrollPositionNone];
    }

}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

-(void)didSwitchViews
{
	 if (self.hotelSearchMVC.selectedHotelIndex > -1)
	 {
		 NSUInteger selectedParentHotelIndex = [self.hotelSearchMVC.hotelSearch.selectedHotelIndex intValue];
		 
		 // TODO: maybe make this faster
		 for (NSUInteger i = 0; i < [self.sortedHotels count]; i++)
		 {
			 HotelDescriptor *hotel = self.sortedHotels[i];
			 if (hotel.parentHotelIndex == selectedParentHotelIndex)
			 {
				 NSIndexPath* indexPath = [NSIndexPath indexPathForRow:i inSection:0];
				 [self.tblView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionTop animated:YES];
				 return;
			 }
		 }
	 }
}

-(NSAttributedString *)getPriceToBeatHeaderAttributedText
{
    NSDictionary *attributes = @{NSFontAttributeName : [UIFont systemFontOfSize:14], NSForegroundColorAttributeName : [UIColor blackColor]};
    NSDictionary *blueTextattributes = @{NSFontAttributeName : [UIFont systemFontOfSize:14], NSForegroundColorAttributeName : [UIColor bookingBlueColor]};
    
    NSMutableAttributedString *text = [[NSMutableAttributedString alloc] init];
    NSString *travelPoints = self.hotelSearchMVC.travelPointsInBank;
    if ([self.hotelSearchMVC.hotelBenchmarkRangeString length])
    {
        NSString *p2b = self.hotelSearchMVC.hotelBenchmarkRangeString;
        NSString *p2bText = [[@"Price to Beat" localize] stringByAppendingFormat:@": %@", p2b];
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:p2bText attributes:attributes]];
        [text addAttributes:blueTextattributes range:[p2bText rangeOfString:p2b]];
        if ([travelPoints intValue]) {
            [text appendAttributedString:[[NSAttributedString alloc] initWithString:@"\n" attributes:attributes]];
        }
    }
    
    if ([travelPoints intValue]) {
        NSString *pointsText = [NSString stringWithFormat:[@"Travel Points Bank: %@ points" localize], travelPoints];
        [text appendAttributedString:[[NSMutableAttributedString alloc] initWithString:pointsText attributes:attributes]];
    }
    
    return text;
}


#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
   // NSLog(@"[[self.fetchedResultsController sections] count] = %d", [[self.fetchedResultsController sections] count]);
    return [[self.fetchedResultsController sections] count] + 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if(section == 1)
        return 0;
    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
//    NSLog(@"[sectionInfo numberOfObjects] = %d", [sectionInfo numberOfObjects]);
    return [sectionInfo numberOfObjects];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"HotelListSimplerCell";
 	NSUInteger row = [indexPath row];
	//HotelDescriptor *hotel = (HotelDescriptor*)[sortedHotels objectAtIndex:row];
	//HotelListCell *cell = [self makeAndConfigureHotelListCellForTable:tableView hotel:hotel.detail];
    HotelListCell *cell = (HotelListCell *)[self.tblView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelListSimplerCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[HotelListCell class]])
                cell = (HotelListCell*)oneObject;
    }
    cell.lblStarting.text = [@"Starting" localize];
    [self configureCell:cell indexPath:indexPath];
	cell.hotelIndex = row;
	return cell;
}

-(void)configureCell:(HotelListCell*)cell indexPath:(NSIndexPath *)indexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityHotelBooking *hotel = (EntityHotelBooking *)managedObject;
    
    cell.parentMVC = self;
    
    cell.name.text = hotel.hotel;
    cell.address1.text = hotel.addr1;
    
    NSString *state = ((hotel.stateAbbrev != nil && [hotel.stateAbbrev length]) > 0 ? hotel.stateAbbrev : hotel.state);

    // CityStateZip variable needs building up so that it doesn't look amateur
    NSString *cityStateZip = @"";
    
    if ([hotel.city length])
    {
        cityStateZip = hotel.city;
        if ([state length] || [hotel.zip length])
        {
            cityStateZip = [cityStateZip stringByAppendingString:@", "];
        }
    }
    if ([state length])
    {
        cityStateZip = [cityStateZip stringByAppendingString:state];
        if ([hotel.zip length])
        {
            cityStateZip = [cityStateZip stringByAppendingString:@" "];
        }
    }
    if ([hotel.zip length])
    {
        cityStateZip = [cityStateZip stringByAppendingString:hotel.zip];
    }

    cell.address2.text = cityStateZip;
    
    cell.phone.text =  hotel.phone;
    cell.distance.text = [NSString stringWithFormat:@"%@ %@", hotel.distance, hotel.distanceUnit];
    
    [cell.amount setTextColor:[UIColor bookingBlueColor]];

    if([hotel.isSoldOut boolValue])
    {
        cell.amount.text = [Localizer getLocalizedText:@"Sold Out"];
        cell.lblStarting.hidden = YES;
    }
    else if([hotel.isNoRates boolValue])
    {
        cell.amount.text = [Localizer getLocalizedText:@"No Rates"];
        cell.lblStarting.hidden = YES;
    }
    else if([hotel.isAddtional boolValue])
    {
        cell.amount.text = [Localizer getLocalizedText:@"View Rates"];
        cell.lblStarting.hidden = NO;
    }
    else
    {
        NSString *currencyCodeForCheapestRate = [hotel.cheapestRoomRate floatValue] == [hotel.relCheapRoom.rate floatValue] ? hotel.relCheapRoom.crnCode : hotel.relCheapRoomViolation.crnCode;
        cell.amount.text = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", [hotel.cheapestRoomRate floatValue]] crnCode:currencyCodeForCheapestRate];
        cell.lblStarting.hidden = NO;
    }

    int asterisks = [hotel.starRating intValue];// .starRatingAsterisks;
    if (asterisks == 0)
    {
        cell.starRating.hidden = YES;
        cell.shadowStarRating.hidden = YES;
        cell.notRated.hidden = YES;
        cell.ivStars.hidden = NO;
        cell.ivStars.image = [UIImage imageNamed:@"stars_0"];
    }
    else
    {
        cell.ivStars.hidden = NO;
        int starCount = asterisks;
        if(starCount == 1)
            cell.ivStars.image = [UIImage imageNamed:@"stars_1"];
        else if(starCount == 2)
            cell.ivStars.image = [UIImage imageNamed:@"stars_2"];
        else if(starCount == 3)
            cell.ivStars.image = [UIImage imageNamed:@"stars_3"];
        else if(starCount == 4)
            cell.ivStars.image = [UIImage imageNamed:@"stars_4"];
        else if(starCount == 5)
            cell.ivStars.image = [UIImage imageNamed:@"stars_5"];
        cell.starRating.hidden = NO;
        cell.shadowStarRating.hidden = NO;
        cell.notRated.hidden = YES;
    }
    
    
    int diamonds = [hotel.hotelPrefRank intValue];
    if (diamonds == 0)
    {
        cell.ivDiamonds.hidden = YES;
    }
    else
    {
        //NSLog(@"diamonds %d", diamonds);
        cell.ivDiamonds.hidden = NO;
        if(diamonds == 4)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_1"];
        else if(diamonds == 5)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_2"];
        else if(diamonds == 10)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_3"];
        else if(diamonds == 1)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_1"];
        else if(diamonds == 2)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_2"];
        else if(diamonds == 3)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_3"];
    }
    // Load the logo asynchronously
    //	if (hotelResult.propertyUri != nil && [hotelResult.propertyUri length] > 0)
    //	{
    //		cell.logoView.image = nil;  // The cell may have been dequeued with an image already in it.  Get rid of it.
    //		UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
    //		[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:hotelResult.propertyUri RespondToImage:img IV:cell.logoView MVC:hotelSearchMVC];
    //	}
    
    cell.logoView.image = nil;  // The cell may have been dequeued with an image already in it.  Get rid of it.
    if ([hotel.relHotelImage count] > 0)
    {
        for(EntityHotelImage *image in hotel.relHotelImage)
        {
            UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
            //HotelImageData *hid = [hotelResult.propertyImagePairs objectAtIndex:0];
            [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:image.thumbURI RespondToImage:img IV:cell.logoView MVC:self.hotelSearchMVC];
            break;
        }
    }
    
    if ([[hotel.recommendationSource stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]] length]) {
        cell.ivRecommendation.hidden = NO;
        cell.recommendationText.hidden = NO;
        cell.recommendationText.text = [self getRecommendationTextForHotel:hotel];
    }
    else
    {
        cell.ivRecommendation.hidden = YES;
        cell.recommendationText.hidden = YES;
    }

    if ([hotel.travelPoints intValue] != 0) {
        cell.lblTravelPoints.hidden = NO;
        if ([hotel.travelPoints intValue] > 0) {
            cell.lblTravelPoints.text = [NSString stringWithFormat:[@"Earn %d pts." localize],[hotel.travelPoints intValue]];
            cell.lblTravelPoints.textColor = [UIColor bookingGreenColor];
        }
        else {
            cell.lblTravelPoints.text = [NSString stringWithFormat:[@"Use %d pts." localize],-[hotel.travelPoints intValue]];
            cell.lblTravelPoints.textColor = [UIColor bookingRedColor];
        }
    }
    else {
        cell.lblTravelPoints.hidden = YES;
    }
    
    if ([hotel.isFedRoom boolValue] && [Config isGov])
    {
        [cell.isFedRoomView setHidden:NO];
    }
    else
    {
        [cell.isFedRoomView setHidden:YES];
    }
}

-(NSString *)getRecommendationTextForHotel:(EntityHotelBooking*) hotel
{
//    UserStay – you have stayed
//    CompanyStay – your colleagues stayed
//    UserFavorite – your favorite
//    CompanyFavorite - your colleagues favorite
//    CompanyPreferred – your company preferred
//    ItemRecommendation - other users like you like this hotel
//    MeetingRecommendation - your colleagues staying here on your check in date
    NSString *recommendationText;
    BOOL isSingularValue = [hotel.recommendationDisplayValue integerValue] <= 1;
    if ([hotel.recommendationSource isEqualToString:@"UserStay"])
    {
        recommendationText = isSingularValue ? [@"You have stayed here earlier" localize] : [NSString stringWithFormat:[@"You have stayed here %@ times" localize],hotel.recommendationDisplayValue];
    }
    else if ([hotel.recommendationSource isEqualToString:@"CompanyStay"])
    {
        recommendationText = isSingularValue ? [@"Your colleague has stayed here" localize] : [NSString stringWithFormat:[@"Your colleagues have stayed here %@ times" localize],hotel.recommendationDisplayValue];
    }
    else if ([hotel.recommendationSource isEqualToString:@"UserFavorite"])
    {
        recommendationText = [@"Your favorite" localize];
    }
    else if ([hotel.recommendationSource isEqualToString:@"CompanyFavorite"])
    {
        recommendationText = isSingularValue ? [@"Your colleague's favorite" localize] : [NSString stringWithFormat:[@"%@ of your colleagues' favourite" localize],hotel.recommendationDisplayValue];
    }
    else if ([hotel.recommendationSource isEqualToString:@"CompanyPreferred"])
    {
        recommendationText = [@"Your company's preferred" localize];
    }
    else if ([hotel.recommendationSource isEqualToString:@"ItemRecommendation"])
    {
        recommendationText = [@"Similar users like this hotel" localize];
    }
    else if ([hotel.recommendationSource isEqualToString:@"MeetingRecommendation"])
    {
        recommendationText = isSingularValue ? [@"Your colleague is staying here on your check-in date" localize] : [NSString stringWithFormat:[@"%@ of your colleagues are staying here on your check-in date" localize],hotel.recommendationDisplayValue];
    }
    else
    {
        recommendationText = [@"Recommended" localize];
    }
    return recommendationText;
}


#pragma mark -
#pragma mark Table view delegate
-(CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityHotelBooking *hotel = (EntityHotelBooking *)managedObject;
    if ([[hotel.recommendationSource stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]] length])
        return 90;
    return 74;
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	self.lastIndexPath = indexPath;
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityHotelBooking *entity = (EntityHotelBooking *)managedObject;
	
	[self.hotelSearchMVC showRoomsForSelectedHotel:entity];
}

- (void) tableView:(UITableView *)tableView accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	HotelDescriptor *hotel = self.sortedHotels[row];
	
	[self.hotelSearchMVC.hotelSearch selectHotel:hotel.parentHotelIndex];
	
//	[hotelSearchMVC showRoomsForSelectedHotel];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    if (tableView.tableHeaderView && section == 0)
        return [@"Hotels" localize];
    return nil;
}

-(UIView*) tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
{
    if(section == 1 && self.hotelSearchMVC.totalCount > 0)
    {
        id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][0];
        //NSLog(@"viewForFooterInSection [sectionInfo numberOfObjects] %d self.hotelSearchMVC.totalCount %d", [sectionInfo numberOfObjects], hotelSearchMVC.totalCount );
        if([sectionInfo numberOfObjects] >= (self.hotelSearchMVC.totalCount))
            return nil;
        
        __autoreleasing UIView *footer = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
        footer.backgroundColor = [UIColor whiteColor];
        UIButton *btn = [[UIButton alloc] initWithFrame:CGRectMake(10, 0, 300, 44)];
        [btn addTarget:self action:@selector(fetchMore:) forControlEvents: UIControlEventTouchUpInside];
        [btn setTitle:[Localizer getLocalizedText:@"Load more hotels"] forState:UIControlStateNormal];
        //R:2 G:64 B:112
        [btn setTitleColor:[UIColor colorWithRed:2/255.0 green:64/255.0 blue:112/255.0 alpha:1.0] forState:UIControlStateNormal];
        [btn setTitleColor:[UIColor grayColor] forState:UIControlStateSelected];
        [btn.titleLabel setFont:[UIFont fontWithName:@"HelveticaNeue-Bold" size:16]];
        [footer addSubview:btn];
        
//        UILabel *lbl = [[[UILabel alloc] initWithFrame:CGRectMake(10, 10, 300, 30)] autorelease];
//        lbl.text = @"BOOOOOO";
//        lbl.backgroundColor = [UIColor clearColor];
//        [footer addSubview:lbl];
        return footer;
    }
    else
        return nil;
}

-(CGFloat) tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
    if(section == 1 && self.hotelSearchMVC.totalCount > 0)
    {
        id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][0];
        // NSLog(@"heightForFooterInSection [sectionInfo numberOfObjects] %d self.hotelSearchMVC.totalCount %d", [sectionInfo numberOfObjects], hotelSearchMVC.totalCount );
        if([sectionInfo numberOfObjects] >= (self.hotelSearchMVC.totalCount))
        {
            //NSLog(@"a");
            return 0;
        }
        else
        {
            //NSLog(@"b");
            return 44;
        }
    }
        
    else
        return 0;
}


#pragma mark -
#pragma mark Action Sheet button handlers
- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    self.actionPopOver = nil;
}

- (IBAction)buttonReorderPressed:(id)sender
{
	// TODO: implement reordering
}

-(IBAction)buttonActionPressed:(id)sender
{
    if (self.actionPopOver) {
        [self.actionPopOver dismissWithClickedButtonIndex:-1 animated:YES];
        self.actionPopOver = nil;
        return;
    }

	self.actionPopOver = [[MobileActionSheet alloc] initWithTitle:[Localizer getLocalizedText:@"Sort By"]
											   delegate:self 
									  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
								 destructiveButtonTitle:nil
									  otherButtonTitles:  [Localizer getLocalizedText:@"Preferred Vendors"]
														, [Localizer getLocalizedText:@"Vendor Names"]
														, [Localizer getLocalizedText:@"Price"]
														, [Localizer getLocalizedText:@"Distance"]
														, [Localizer getLocalizedText:@"Rating"]
                                                        , [[HotelBookingManager sharedInstance] isAnyHotelRecommended] ? [Localizer getLocalizedText:@"Recommendation"] : nil
														, nil];
	if([UIDevice isPad])
		[self.actionPopOver showFromBarButtonItem:sender animated:YES];
	else 
	{
		self.actionPopOver.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
		[self.actionPopOver showFromToolbar:[ConcurMobileAppDelegate getBaseNavigationController].toolbar];
	}
}


#pragma mark -
#pragma mark UIActionSheetDelegate Methods

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if (actionSheet.cancelButtonIndex == buttonIndex)
		return;
	
	if (buttonIndex == 0)
	{
		self.sortOrder = SORT_BY_PREFERRED_VENDORS;
	}
	else if (buttonIndex == 1)
	{
		self.sortOrder = SORT_BY_VENDOR_NAMES;
	} 
	else if (buttonIndex == 2)
	{
		self.sortOrder = SORT_BY_PRICE;
	} 
	else if (buttonIndex == 3)
	{
		self.sortOrder = SORT_BY_DISTANCE;
	} 
	else if (buttonIndex == 4)
	{
		self.sortOrder = SORT_BY_RATING;
	}
    else if (buttonIndex == 5)
    {
        self.sortOrder = SORT_BY_MOST_RECOMMENDED;
    }
	
	[self sortHotels];
    
    //MOB-9734
    //After sorting search result, scroll back to top of the table view
    if([[self.fetchedResultsController fetchedObjects] count] > 0 )
    {
        NSIndexPath *topIndexPath = [NSIndexPath indexPathForRow:0 inSection:0];
        [self.tblView scrollToRowAtIndexPath:(NSIndexPath *) topIndexPath atScrollPosition:UITableViewScrollPositionTop animated:YES];
    }
}


#pragma mark -
#pragma mark Sorting Methods

- (void)sortHotels 
{
    [self refetchData];
	[self.tblView reloadData];
    [self updateToolbar];
}

#pragma mark -
#pragma mark Populate Methods
- (void)populateSortedHotels
{
	if (self.hotelSearchMVC.hotelSearch.hotels == nil)
	{
		self.sortedHotels = nil;
		return;
	}
	else
	{
		NSMutableArray *hotels = [[NSMutableArray alloc] init];
		

		for (NSUInteger i = 0; i < [self.hotelSearchMVC.hotelSearch.hotels count]; i++)
		{
			HotelDescriptor* hotel = [[HotelDescriptor alloc] init];
			
			hotel.parentHotelIndex = i;
			hotel.detail = (self.hotelSearchMVC.hotelSearch.hotels)[i];
			[hotels addObject:hotel];
			
		}
		self.sortedHotels = hotels;
		
	}
}


#pragma mark -
#pragma mark Toolbar Methods

- (void)updateToolbar
{
//	if (hotelSearchMVC.hotelSearch.hotels != nil)
//	{
        self.hotelSearchMVC.childSortOrder = self.sortOrder;
		[self.hotelSearchMVC showFullToolbar];
        [self updateHeading];
//	}
//	else
//	{
//		[hotelSearchMVC showMinimalToolbar];
//	}
}


#pragma mark - Heading
-(void) updateHeading
{
    self.lblSubheading.text = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateForBooking:self.hotelSearchMVC.hotelSearch.hotelSearchCriteria.checkinDate], [DateTimeFormatter formatDateForBooking:self.hotelSearchMVC.hotelSearch.hotelSearchCriteria.checkoutDate]];
    
    self.lblHeader.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"Hotels near"], self.hotelSearchMVC.hotelSearch.hotelSearchCriteria.locationResult.location];
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
    self.lblHeader = nil;
    self.lblSubheading = nil;
    self.tblView = nil;
}

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
//    NSSortDescriptor *sort =  nil; //[[NSSortDescriptor alloc] initWithKey:@"hotel" ascending:YES];
//    NSSortDescriptor *sort2 = nil; //[[NSSortDescriptor alloc] initWithKey:@"distance" ascending:YES];
//    NSSortDescriptor *sort3 = nil; 
    
    // Note that:
    // isAdditional = NO, means that a dollar amount is being shown
    // isAddtional = YES, means that 'View Rates' is being shown instead of a dollar amount.
    // Therefore, we always sort first by isAddtional, then by cheapestRoomRate
    // so that items with dollar amounts will always appear before items with 'View Rates'
    
    if (self.sortOrder == SORT_BY_PREFERRED_VENDORS)
	{
		sortDescriptors = @[[[NSSortDescriptor alloc] initWithKey:@"hotelPrefRank" ascending:NO],
                            [[NSSortDescriptor alloc] initWithKey:@"isAddtional" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"cheapestRoomRate" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"distance" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"hotel" ascending:YES]];
	}
	if (self.sortOrder == SORT_BY_VENDOR_NAMES)
	{
        sortDescriptors = @[[[NSSortDescriptor alloc] initWithKey:@"hotel" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"isAddtional" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"cheapestRoomRate" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"distance" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"hotel" ascending:YES]];
        
	}
	else if (self.sortOrder == SORT_BY_PRICE)
	{
        sortDescriptors = @[[[NSSortDescriptor alloc] initWithKey:@"isAddtional" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"cheapestRoomRate" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"chainName" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"distance" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"hotel" ascending:YES]];
	}
	else if (self.sortOrder == SORT_BY_DISTANCE)
	{
        sortDescriptors = @[[[NSSortDescriptor alloc] initWithKey:@"distance" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"hotel" ascending:YES]];
	}
	else if (self.sortOrder == SORT_BY_RATING)
	{
        sortDescriptors = @[[[NSSortDescriptor alloc] initWithKey:@"starRating" ascending:NO],
                            [[NSSortDescriptor alloc] initWithKey:@"isAddtional" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"cheapestRoomRate" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"distance" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"hotel" ascending:YES]];
	}
    else if (self.sortOrder == SORT_BY_MOST_RECOMMENDED)
    {
        sortDescriptors = @[[[NSSortDescriptor alloc] initWithKey:@"recommendationScore" ascending:NO],
                            [[NSSortDescriptor alloc] initWithKey:@"isAddtional" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"cheapestRoomRate" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"distance" ascending:YES],
                            [[NSSortDescriptor alloc] initWithKey:@"hotel" ascending:YES]];
    }
	
    [fetchRequest setSortDescriptors:sortDescriptors];

    //[fetchRequest setFetchBatchSize:20];
    
    NSFetchedResultsController *theFetchedResultsController = 
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest 
                                        managedObjectContext:self.managedObjectContext sectionNameKeyPath:nil 
                                                   cacheName:nil];
    self.fetchedResultsController = theFetchedResultsController;
    __fetchedResultsController.delegate = self;
    
    
    return __fetchedResultsController;    
    
}


#pragma mark - Fetched results controller delegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    //self hideWaitView];
    [self.tblView beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo
           atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
{
    switch(type)
    {
        case NSFetchedResultsChangeInsert:
            [self.tblView insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [self.tblView deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject
       atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type
      newIndexPath:(NSIndexPath *)newIndexPath
{
    UITableView *tableView = self.tblView;
    
    switch(type)
    {
            
        case NSFetchedResultsChangeInsert:
            [tableView insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeUpdate:
        {
            HotelListCell *cell = (HotelListCell*)[self.tblView cellForRowAtIndexPath:indexPath];
            [self configureCell:cell indexPath:indexPath];
            break;
        }
            
        case NSFetchedResultsChangeMove:
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [tableView insertRowsAtIndexPaths:@[newIndexPath]withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    [self.tblView endUpdates];
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
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::viewDidLoad: fetchedResultsController %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
        }
	}
}


-(IBAction)fetchMore:(id)sender
{    
    [self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading more Hotels"]];
    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][0];
    
    NSUInteger newStart = [sectionInfo numberOfObjects];
    NSUInteger numberOfRecordsToRequest = MIN(self.hotelSearchMVC.totalCount - [sectionInfo numberOfObjects],30);
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"", @"HOTEL_SEARCH", @"YES", @"SKIP_CACHE", [NSString stringWithFormat:@"%lu", (unsigned long)newStart], @"STARTPOS", [NSString stringWithFormat:@"%lu",(unsigned long)numberOfRecordsToRequest], @"NUMRECORDS", self.hotelSearchCriteria, @"HOTEL_SEARCH_CRITERIA", self.hotelSearch, @"HOTEL_SEARCH",nil];
	[[ExSystem sharedInstance].msgControl createMsg:FIND_HOTELS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}
@end

