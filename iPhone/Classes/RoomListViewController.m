//
//  RoomListViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "RoomListViewController.h"
#import "ExSystem.h" 

#import "FindRooms.h"
#import "HotelSearch.h"
#import "HotelResult.h"
#import "HotelInfo.h"
#import "RoomResult.h"
#import "RoomListCell.h"
#import "RoomListSummaryCell.h"
#import "HotelSearchResultsViewController.h"
#import "HotelDetailedMapViewController.h"
#import "HotelBookingViewController.h"
#import "FormatUtils.h"
#import "AsyncImageView.h"
#import "HotelDetailsViewController.h"
#import "ConcurMobileAppDelegate.h"
#import "PolicyViolationConstants.h"
#import "Config.h"
#import "UserConfig.h"
#import "TextViewController.h"

@implementation RoomListViewController

@synthesize hotelSearch, hotelBooking;
@synthesize fetchedResultsController=__fetchedResultsController;
@synthesize managedObjectContext=__managedObjectContext;

@synthesize hotelName;
@synthesize address1;
@synthesize address2;
@synthesize address3;
@synthesize phone;
@synthesize distance;
@synthesize starRating;
@synthesize shadowStarRating;
@synthesize notRated;
@synthesize isAddressLinked;
@synthesize currentPage, ivHotel, btnHotel, aImageURLs;
@synthesize ivStars, ivDiamonds, lblPreferred, imageViewerMulti, tblView, viewHeader;
@synthesize taFields;
@synthesize roomList = _roomList;

#define kSectionHotelSummary 0
#define kSectionRooms 1
#define kAlertHotelSoldOut 76334

-(void)reserveRoomAtIndex:(int)roomIndex
{
	//[hotelSearch.selectedHotel.detail selectRoom:roomIndex];
    
    //NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:[NSIndexPath indexPathForRow:roomIndex inSection:0]];
    NSManagedObject *managedObject = (self.roomList)[roomIndex];
    EntityHotelRoom *room = (EntityHotelRoom *)managedObject;
    
    if ([room maxEnforcementLevel] != nil && [[room maxEnforcementLevel] intValue] != kViolationAutoFail) {
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:hotelSearch, @"HOTEL_SEARCH", self.hotelBooking, @"HOTEL_BOOKING", room, @"HOTEL_ROOM", hotelSearch.hotelSearchCriteria, @"HOTEL_SEARCH_CRITERIA", @"YES", @"SHORT_CIRCUIT", nil];
        if ([self.travelPointsInBank length]) {
            pBag[@"TRAVEL_POINTS_IN_BANK"] = self.travelPointsInBank;
        }
        HotelBookingViewController *nextController = [[HotelBookingViewController alloc] initWithNibName:@"HotelBookingViewController" bundle:nil];
        // MOB-9547 Do not display custom fields if add car/hotel
        nextController.hideCustomFields = [hotelSearch.tripKey length];
        [nextController view];
        nextController.taFields = self.taFields;
        //For Flurry
        nextController.isVoiceBooking = self.isVoiceBooking;
        Msg *msg = [[Msg alloc] init];
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        [self.navigationController pushViewController:nextController animated:YES];
        [nextController respondToFoundData:msg];
    }
    else
    {
        NSMutableString *violationMessage = [[NSMutableString alloc] init];
        NSArray *hotelViolations = [[HotelBookingManager sharedInstance] fetchViolationsByRoom:room];
        
        for (EntityHotelViolation *hotelViolation in hotelViolations) {
            if ([[hotelViolation enforcementLevel] intValue] == kViolationAutoFail)
                [violationMessage appendFormat:([violationMessage length] ? @"\n%@" : @"%@"),hotelViolation.message];
        }
        
        NSString *displayMessage = [violationMessage length] ? violationMessage : [@"Your company's travel policy will not allow this reservation." localize];
        //Display error message
        MobileAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Reservation Not Allowed"]
                                  message:displayMessage
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                  otherButtonTitles:nil];
		[alert show];
    }
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return HOTEL_ROOM_LIST;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void)respondToFoundData:(Msg *)msg
{
    BOOL updateView = NO;
	if ([msg.idKey isEqualToString:FIND_HOTEL_ROOMS])
	{
		FindRooms *findRooms = (FindRooms *)msg.responder;
        if ([findRooms.hotelBooking isFault]) // Discard old defunct results.
            return;
        
		self.hotelSearch = findRooms.hotelSearch;
        self.hotelBooking = findRooms.hotelBooking;
        [self configureHeader:NO];
        updateView = YES;
        if ([self.hotelBooking.isNoRates boolValue] || [self.hotelBooking.isSoldOut boolValue]) {
            //Display error message
            MobileAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:nil
                                      message:[Localizer getLocalizedText:@"NO_RATES_SOLD_OUT_USER_MESSAGE"]
                                      delegate:self
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                      otherButtonTitles:nil];
            alert.tag = kAlertHotelSoldOut;
            [alert show];
        }
	}
	else if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"])
	{
		// A short circuit indicates that the data is available in the parameter bag.
		self.hotelSearch = (HotelSearch*)(msg.parameterBag)[@"HOTEL_SEARCH"];
        self.hotelBooking = (EntityHotelBooking*)(msg.parameterBag)[@"HOTEL_BOOKING"];
        [self configureHeader:NO];
        updateView = YES;
	}
    else
    {
        // Most likely an IMAGE response has been directed here
        //NSLog(@"Find Hotel Rooms and Short Circuit conditions were not met");
        //NSLog(msg.idKey);
    }
    
	if (updateView)
    {
        [self setPriceToBeatHeader];
        [self hideLoadingView];
        [self refetchData];
        [tblView reloadData];
        [self makeToolbar:hotelSearch.hotelSearchCriteria roomCount:[NSNumber numberWithInteger:[hotelBooking.relHotelRoom count]]];
    }
}

- (void)initData:(NSMutableDictionary*)paramBag
{
	if (paramBag != nil)
	{
		HotelSearchCriteria *hotelSearchCriteria = (HotelSearchCriteria*)paramBag[@"HOTEL_SEARCH_CRITERIA"];
		if (hotelSearchCriteria != nil)
		{
			[self makeToolbar:hotelSearchCriteria roomCount:nil];
		}
	}
	
	self.hotelSearch = nil;
}


#pragma mark -
#pragma mark Initialization

/*
- (id)initWithStyle:(UITableViewStyle)style {
    // Override initWithStyle: if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
    if ((self = [super initWithStyle:style])) {
    }
    return self;
}
*/


#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];

    if (hotelSearch == nil)
		[self showLoadingViewWithText:[Localizer getLocalizedText:@"Fetching Data"]];
	else
		[self hideLoadingView];

	//if([UIDevice isPad])
		self.title = [Localizer getLocalizedText:@"HOTEL_ROOM_LIST"];
		// So that rvc knows where to post resposne data
    [ExSystem sharedInstance].sys.topViewName = [self getViewIDKey];
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    self.managedObjectContext = [ad managedObjectContext];
//    [self refetchData];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	[self setPriceToBeatHeader];
	//tblView.allowsSelection = NO;

}

/*
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}
*/
/*
- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
}
*/
/*
- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
}
*/

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

-(void)setPriceToBeatHeader
{
    // Need to check if there is a benchmark to display or not
    if ([self.hotelBooking.benchmarkPrice doubleValue] || [self.travelPointsInBank intValue])
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
        self.tblView.tableHeaderView = nil;
        self.viewForTableViewHeader.hidden = YES;
    }
}

-(NSAttributedString *)getPriceToBeatHeaderAttributedText
{
    NSDictionary *attributes = @{NSFontAttributeName : [UIFont systemFontOfSize:14], NSForegroundColorAttributeName : [UIColor blackColor]};
    NSDictionary *blueTextattributes = @{NSFontAttributeName : [UIFont systemFontOfSize:14], NSForegroundColorAttributeName : [UIColor bookingBlueColor]};
    
    NSMutableAttributedString *text = [[NSMutableAttributedString alloc] init];
    NSString *travelPoints = self.travelPointsInBank;
    if ([self.hotelBooking.benchmarkPrice doubleValue])
    {
        NSString *p2b = [FormatUtils formatMoney:[self.hotelBooking.benchmarkPrice stringValue] crnCode:self.hotelBooking.benchmarkCurrency];
        
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

-(void) alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == kAlertHotelSoldOut) {
        [self.navigationController popViewControllerAnimated:YES];
    }
}


#pragma mark -
#pragma mark Table view data source
#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
   // NSLog(@"[[self.fetchedResultsController sections] count] = %d", [[self.fetchedResultsController sections] count]);
    return [[self.fetchedResultsController sections] count];

}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
//    NSLog(@"[sectionInfo numberOfObjects] = %d", [sectionInfo numberOfObjects]);
    return [sectionInfo numberOfObjects];
}
//
//- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
//{
//	return (hotelBooking == nil ? 0 : 2);	// Summary section + Room section
//}
//
//
//- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
//{
//    NSLog(@"count %d", [hotelBooking.relHotelRoom count]);
//	return (kSectionHotelSummary == section ? 1 : [hotelBooking.relHotelRoom count]);
//}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
//	NSUInteger section = [indexPath section];
// 	NSUInteger row = [indexPath row];
	
//	if (kSectionHotelSummary == section)
//	{
//		RoomListSummaryCell *cell = [RoomListSummaryCell makeAndConfigureCellForTableView:tableView owner:self hotel:hotelSearch.selectedHotel showAddressLink:YES];
//		cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
//		return cell;
//	}
//	else
//	{
		RoomListCell *cell = (RoomListCell*)[tableView dequeueReusableCellWithIdentifier:ROOM_LIST_CELL_REUSABLE_IDENTIFIER];
		if (cell == nil)
			cell = [self makeRoomListCell];

        [self configureCell:cell indexPath:indexPath];
		

		return cell;
//	}
}

-(void)configureCell:(RoomListCell*)cell indexPath:(NSIndexPath *)indexPath
{
    if (!cell) {
        return;
    }
    //NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    NSManagedObject *managedObject = (self.roomList)[indexPath.row];
    EntityHotelRoom *room = (EntityHotelRoom *)managedObject;
    
//    RoomResult *roomResult = [hotelSearch.selectedHotel.detail.roomResults objectAtIndex:row];
//    int violationCount = [room.relHotelViolation count]; // [roomResult.violations count];
    cell.parentMVC = self;
    [cell.rate setText: [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", [room.rate doubleValue]] crnCode:room.crnCode]];

    // Check how long the rate text would be on a single line
    NSDictionary *attributes = [NSDictionary dictionaryWithObjectsAndKeys:cell.rate.font, NSFontAttributeName, nil];
    //MOB-16858 CoreData: error: Serious application error.  Exception was caught during Core Data change processing.  This is usually a bug within an observer of NSManagedObjectContextObjectsDidChangeNotification.  NSConcreteAttributedString initWithString:: nil value with userInfo (null)
    CGFloat fontsize = [[[NSAttributedString alloc] initWithString:cell.rate.text attributes:attributes] size].width; // this line throws exception if cell is nil (NSInvalidArgumentException is thrown reason "NSConcreteAttributedString initWithString:: nil value"), adding if-check at the start of method to return if cell is nil
    
    if (cell.rate.frame.size.width < fontsize)
    {
        // If the rate text exceeds the width of the label frame, we need to split it
        // We can't trust how the auto word-wrap will perform the split, we get odd results
        // If the rate contains a space, split at that point and insert a newline instead of the space.
        NSRange range = [cell.rate.text rangeOfCharacterFromSet:[NSCharacterSet whitespaceCharacterSet]];
        // MOB16423 For parts of Europe ' $' may appear at the end, we don't want to force the split in this situation
        if (range.location > 0 && range.location < ([cell.rate.text length]-2))
        {
            NSString *newString3 = [NSString stringWithFormat:@"%@\n%@", [cell.rate.text substringToIndex:range.location], [cell.rate.text substringFromIndex:range.location+1]];
            [cell.rate setText: newString3];
        }
    }
    
    if (![ExSystem is7Plus])
    {
        // We call the alignment here for iOS6 as it seems to ignore the call made in RoomListCell.didMoveToSuperview
        // Also iOS7 doesn't do anything when we make this call here, it prefers the call in RoomListCell.didMoveToSuperview
        [cell.rate setVerticalAlignment:VerticalAlignmentBottom];
    }
    
    [cell.lblSub1 setText:room.summary];
    cell.roomIndex = (int)indexPath.row;
    cell.rate.textColor = [UIColor bookingBlueColor];
    // MOB-8067 This was fixed in branch, but not migrated onto trunk
    if([room maxEnforcementLevel] != nil)
    {
        int eLevel = [[room maxEnforcementLevel] intValue];
        //        NSLog(@"eLevel = %d", eLevel);
        if(eLevel < kViolationLogForReportsOnly || eLevel == 100)
        {
//            [cell.rate setTextColor:[UIColor bookingGreenColor]];
            cell.ivException.hidden = YES;
        }
        else if(eLevel >= kViolationLogForReportsOnly && eLevel <= kViolationRequiresPassiveApproval)
        {
//            [cell.rate setTextColor:[UIColor bookingYellowColor]];
            cell.ivException.image = [UIImage imageNamed:@"icon_yellowex"];
            cell.ivException.hidden = YES;
        }    
        else if(eLevel > kViolationRequiresPassiveApproval && eLevel <= kViolationRequiresApproval)
        {
//            [cell.rate setTextColor:[UIColor bookingRedColor]];
            cell.ivException.image = [UIImage imageNamed:@"icon_redex"];
            cell.ivException.hidden = NO;
        } 
        else if(eLevel == kViolationAutoFail)
        {
//            [cell.rate setTextColor:[UIColor bookingGrayColor]];
            cell.ivException.hidden = YES;
        } 
        else
        {
//            [cell.rate setTextColor:[UIColor bookingRedColor]];
            cell.ivException.image = [UIImage imageNamed:@"icon_redex"];
            cell.ivException.hidden = NO;
        }
    }
    else
    {
//        [cell.rate setTextColor:[UIColor bookingGreenColor]];
        cell.ivException.hidden = YES;
    }
    
    
    cell.lblSub3.hidden = YES;
    NSString *cellText = @"";
    
    if ([room.depositRequired boolValue])
    {
        cellText = [@"Deposit required" localize];
    }
    
    if([UserConfig getSingleton].showGDSNameInSearchResults && [room.gdsName length])
    {
        if ([room.depositRequired boolValue])
        {
            cellText = [NSString stringWithFormat:@"%@ (%@)", cellText, room.gdsName];
        }
        else
        {
            cellText = [NSString stringWithFormat:@"(%@)", room.gdsName];
        }
    }
    
    if ([cellText length])
    {
        cell.lblSub3.hidden = NO;
        cell.lblSub3.text = cellText;
    }
    

//    cell.parentMVC = self;
//    
//    cell.name.text = hotel.hotel;
//    cell.address1.text = hotel.addr1;
//    
//    NSString *state = ((hotel.stateAbbrev != nil && [hotel.stateAbbrev length]) > 0 ? hotel.stateAbbrev : hotel.state);
//    /*MOB-4400
//     Fixed by checking to see if state is nil.  If so, make it an empty string.*/
//    if(state == nil)
//        state = @"";
//    
//    NSString *cityStateZip = [NSString stringWithFormat:@"%@, %@ %@", hotel.city, state,  hotel.zip];
//    cell.address2.text = cityStateZip;
//    
//    cell.phone.text =  hotel.phone;
//    cell.distance.text = [NSString stringWithFormat:@"%@ %@", hotel.distance, hotel.distanceUnit];
//    
//    cell.amount.text = [((EntityHotelCheapRoom*)hotel.relCheapRoom).rate stringValue];
//    
//    int asterisks = [hotel.starRating intValue];// .starRatingAsterisks;
//    if (asterisks == 0)
//    {
//        cell.starRating.hidden = YES;
//        cell.shadowStarRating.hidden = YES;
//        cell.notRated.hidden = YES;
//        cell.ivStars.hidden = NO;
//        cell.ivStars.image = [UIImage imageNamed:@"stars_0"];
//    }
//    else
//    {
//        cell.ivStars.hidden = NO;
//        int starCount = asterisks;
//        if(starCount == 1)
//            cell.ivStars.image = [UIImage imageNamed:@"stars_1"];
//        else if(starCount == 2)
//            cell.ivStars.image = [UIImage imageNamed:@"stars_2"];
//        else if(starCount == 3)
//            cell.ivStars.image = [UIImage imageNamed:@"stars_3"];
//        else if(starCount == 4)
//            cell.ivStars.image = [UIImage imageNamed:@"stars_4"];
//        else if(starCount == 5)
//            cell.ivStars.image = [UIImage imageNamed:@"stars_5"];
//        cell.starRating.hidden = NO;
//        cell.shadowStarRating.hidden = NO;
//        cell.notRated.hidden = YES;
//    }
//    
//    
//    int diamonds = [hotel.hotelPrefRank intValue];
//    if (diamonds == 0)
//    {
//        cell.ivDiamonds.hidden = YES;
//    }
//    else
//    {
//        //NSLog(@"diamonds %d", diamonds);
//        cell.ivDiamonds.hidden = NO;
//        if(diamonds == 4)
//            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_1"];
//        else if(diamonds == 5)
//            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_2"];
//        else if(diamonds == 10)
//            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_3"];
//        else if(diamonds == 1)
//            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_1"];
//        else if(diamonds == 2)
//            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_2"];
//        else if(diamonds == 3)
//            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_3"];
//    }
//    // Load the logo asynchronously
//    //	if (hotelResult.propertyUri != nil && [hotelResult.propertyUri length] > 0)
//    //	{
//    //		cell.logoView.image = nil;  // The cell may have been dequeued with an image already in it.  Get rid of it.
//    //		UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
//    //		[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:hotelResult.propertyUri RespondToImage:img IV:cell.logoView MVC:parentMVC];
//    //	}
//    
//    if ([hotel.relHotelImage count] > 0)
//    {
//        for(EntityHotelImage *image in hotel.relHotelImage)
//        {
//            cell.logoView.image = nil;  // The cell may have been dequeued with an image already in it.  Get rid of it.
//            UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
//            //HotelImageData *hid = [hotelResult.propertyImagePairs objectAtIndex:0];
//            [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:image.thumbURI RespondToImage:img IV:cell.logoView MVC:parentMVC];
//            break;
//        }
//    }
//    //
    
    if ([room.travelPoints intValue] != 0) {
        cell.lblTravelPoints.hidden = NO;
        if ([room.travelPoints intValue] > 0) {
            cell.lblTravelPoints.text = [NSString stringWithFormat:[@"Earn %d pts." localize],[room.travelPoints intValue]];
            cell.lblTravelPoints.textColor = [UIColor bookingGreenColor];
        }
        else {
            cell.lblTravelPoints.text = [NSString stringWithFormat:[@"Use %d pts." localize],-[room.travelPoints intValue]];
            cell.lblTravelPoints.textColor = [UIColor bookingRedColor];
        }
    }
    else {
        cell.lblTravelPoints.hidden = YES;
    }
}

#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	int row = (int)[indexPath row];
	
	// As of today (July 28, 2010), the row contains a 'Reserve' button which does not
	// look good when selected.  Clear the selection to work around the issue.
	[tableView deselectRowAtIndexPath:indexPath animated:NO];

    [self reserveRoomAtIndex:row];

}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if ([self.roomList count] > indexPath.row)
    {
        NSManagedObject *managedObject = (self.roomList)[indexPath.row];
        EntityHotelRoom *room = (EntityHotelRoom *)managedObject;
        if ([room.depositRequired boolValue] || ([room.gdsName length] && [UserConfig getSingleton].showGDSNameInSearchResults))
            return 88;
    }
    
	return 72;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    if (tableView.tableHeaderView && section == 0)
        return [@"Rooms" localize];
    return nil;
}


//-(UIView*) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
//{
//    return viewHeader;
//}
//
//-(CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
//{
//    return 95;
//}

- (void) tableView:(UITableView *)tableView accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:hotelSearch, @"HOTEL_SEARCH", @"YES", @"SHORT_CIRCUIT", nil];
	if([UIDevice isPad])
	{
		HotelDetailsViewController *nextController = [[HotelDetailsViewController alloc] initWithNibName:@"HotelDetailsViewController" bundle:nil];
		Msg *msg = [[Msg alloc] init];
		msg.parameterBag = pBag;
		msg.idKey = @"SHORT_CIRCUIT";
		[nextController respondToFoundData:msg];
		[self.navigationController pushViewController:nextController animated:YES];
	}
	else 
		[ConcurMobileAppDelegate switchToView:HOTEL_DETAILS viewFrom:HOTEL_ROOM_LIST ParameterBag:pBag];
}


-(IBAction)showDetails:(id)sender
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:hotelSearch, @"HOTEL_SEARCH", self.hotelBooking, @"HOTEL_BOOKING", @"YES", @"SHORT_CIRCUIT", nil];
	if([UIDevice isPad])
	{
		HotelDetailsViewController *nextController = [[HotelDetailsViewController alloc] initWithNibName:@"HotelDetailsViewController" bundle:nil];
		Msg *msg = [[Msg alloc] init];
		msg.parameterBag = pBag;
		msg.idKey = @"SHORT_CIRCUIT";
		[nextController respondToFoundData:msg];
		[self.navigationController pushViewController:nextController animated:YES];
	}
	else 
		[ConcurMobileAppDelegate switchToView:HOTEL_DETAILS viewFrom:HOTEL_ROOM_LIST ParameterBag:pBag];
}

#pragma mark -
#pragma mark Make cell

-(RoomListCell*)makeRoomListCell
{
	NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"RoomListCell" owner:self options:nil];
	for (id oneObject in nib)
	{
		if ([oneObject isKindOfClass:[RoomListCell class]])
		{
			RoomListCell *cell = (RoomListCell*)oneObject;
			[cell.reserveButton setTitle:[Localizer getLocalizedText:@"Reserve"] forState:UIControlStateNormal];
			return cell;
		}
	}
	return nil;
}


#pragma mark -
#pragma mark HotelSummaryDelegate

-(void)addressPressed:(id)sender
{
	HotelDetailedMapViewController * vc = [[HotelDetailedMapViewController alloc] initWithNibName:@"HotelDetailedMapViewController" bundle:nil];
//	vc.hotelResult = hotelSearch.selectedHotel;
	
	if([UIDevice isPad])
	{
		vc.title = hotelSearch.selectedHotel.hotel;
		vc.modalPresentationStyle = UIModalPresentationFormSheet;
		[self presentViewController:vc animated:YES completion:nil];
		
	}
	else
    {
        [[ConcurMobileAppDelegate findHomeVC] presentViewController:vc animated:YES completion:nil];
    }
    
}

-(void)phonePressed:(id)sender
{
	RoomListSummaryCell *cell = (RoomListSummaryCell*)sender;
	NSString *phoneNumber = cell.phone.text;
	
	NSString *digitsOnlyPhoneNumber = [[phoneNumber componentsSeparatedByCharactersInSet:[[NSCharacterSet characterSetWithCharactersInString:@"0123456789"] invertedSet]] componentsJoinedByString:@""];
	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", digitsOnlyPhoneNumber]]];
}


#pragma mark -
#pragma mark Toolbar Methods

- (void)makeToolbar:(HotelSearchCriteria*)hotelSearchCriteria roomCount:(NSNumber*)roomCount
{
	NSArray *toolbarItems = nil;
	
	if (roomCount == nil)
	{
		toolbarItems = @[];
	}
	else
	{
        UIBarButtonItem *btnFlex = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
		UIBarButtonItem *btnRoomCount = [self makeRoomCountButton:[roomCount intValue]];
		toolbarItems = @[btnFlex, btnRoomCount, btnFlex];
	}
	
	[self setToolbarItems:toolbarItems animated:NO];
}


- (UIBarButtonItem*)makeRoomCountButton:(int)roomCount
{
	const int buttonWidth = 110;
	const int buttonHeight = 30;
	
	UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	
	UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	lblText.numberOfLines = 2;
	lblText.lineBreakMode = NSLineBreakByWordWrapping;
	lblText.textAlignment = NSTextAlignmentCenter;
//	NSString *numberOfRoomsStr = [Localizer getLocalizedText:@"NUMBER_OF_ROOMS"];
//	NSString *foundCountStr = [Localizer getLocalizedText:@"FOUND_COUNT"];
//	NSString *numberOfRoomsFoundFormatStr = [NSString stringWithFormat:@"%@\n%@", numberOfRoomsStr, foundCountStr];
	lblText.text = [NSString stringWithFormat:@"%d %@", roomCount, [Localizer getLocalizedText:@"Results"]];
    if(![ExSystem is7Plus])
    {
        [lblText setBackgroundColor:[UIColor clearColor]];
        [lblText setTextColor:[UIColor whiteColor]];
    }
//	[lblText setShadowColor:[UIColor grayColor]];
//	[lblText setShadowOffset:CGSizeMake(1, 1)];
	[lblText setFont:[UIFont boldSystemFontOfSize:15.0f]];
	[cv addSubview:lblText];
	
	__autoreleasing UIBarButtonItem* btnResultCount = [[UIBarButtonItem alloc] initWithCustomView:cv];
	
	
	return btnResultCount;
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


- (void)dealloc {
    
	if (imageViewerMulti != nil)
		imageViewerMulti.parentVC = nil;

}



#pragma mark - Fetched results controller
- (NSFetchedResultsController *)fetchedResultsController 
{
    if (__fetchedResultsController != nil) {
        return __fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityHotelRoom" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sort =  [[NSSortDescriptor alloc] initWithKey:@"rate" ascending:YES];
//    NSSortDescriptor *sort2 = nil; //[[NSSortDescriptor alloc] initWithKey:@"distance" ascending:YES];
	
    [fetchRequest setSortDescriptors:@[sort]];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(relHotelBooking = %@)", self.hotelBooking];
    [fetchRequest setPredicate:pred];
    
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
            RoomListCell *cell = (RoomListCell*)[self.tblView cellForRowAtIndexPath:indexPath];
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
        if ([Config isDevBuild]) {
            exit(-1);  // Fail
        } else {
            // be more graceful when dying abort();
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::viewDidLoad: fetchedResultsController %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
        }
	}
    else {
        NSSortDescriptor *sorter = [[NSSortDescriptor alloc]initWithKey:@"rate" ascending:YES comparator:^(id a, id b){
            return [a compare:b options:NSNumericSearch];}];
        NSArray *sortDescriptors = @[sorter];
        self.roomList = [[self.fetchedResultsController fetchedObjects] sortedArrayUsingDescriptors:sortDescriptors];
    }
}


-(void)configureHeader:(BOOL)showAddressLink
{
    self.isAddressLinked = showAddressLink;
	
	hotelName.text = self.hotelBooking.hotel;
	
	address1.text = self.hotelBooking.addr1;
	
	NSMutableString *cityStateZip = [[NSMutableString alloc] initWithString:@""];// [NSString stringWithFormat:@"%@, %@ %@", hotelResult.city, hotelResult.stateAbbrev, hotelResult.zip];
    if(self.hotelBooking.city != nil)
        [cityStateZip appendString:self.hotelBooking.city];
    
    if(self.hotelBooking.stateAbbrev != nil)
    {   [cityStateZip appendString:@", "];
        [cityStateZip appendString:self.hotelBooking.stateAbbrev];
    }
    
    if(self.hotelBooking.zip != nil)
    {   [cityStateZip appendString:@" "];
        [cityStateZip appendString:self.hotelBooking.zip];
    }
    
	address2.text = cityStateZip;
	
	if (!showAddressLink)
	{
		address1.textColor = [UIColor blackColor];
		address2.textColor = [UIColor blackColor];
	}
	
	phone.text = self.hotelBooking.phone;
	
	distance.text = [NSString stringWithFormat:@"%@ %@", self.hotelBooking.distance, self.hotelBooking.distanceUnit];
	
	int asterisks = [self.hotelBooking.starRating intValue];
	if (asterisks == 0)
	{
		starRating.hidden = YES;
		shadowStarRating.hidden = YES;
		notRated.hidden = YES;
        ivStars.hidden = NO;
        ivStars.image = [UIImage imageNamed:@"stars_0"];
	}
	else
	{
        ivStars.hidden = NO;
        int starCount = asterisks;
        if(starCount == 1)
            ivStars.image = [UIImage imageNamed:@"stars_1"];
        else if(starCount == 2)
            ivStars.image = [UIImage imageNamed:@"stars_2"];
        else if(starCount == 3)
            ivStars.image = [UIImage imageNamed:@"stars_3"];
        else if(starCount == 4)
            ivStars.image = [UIImage imageNamed:@"stars_4"];
        else if(starCount == 5)
            ivStars.image = [UIImage imageNamed:@"stars_5"];
		starRating.hidden = NO;
		shadowStarRating.hidden = NO;
		notRated.hidden = YES;
	}
	//starRating.text = (asterisks == nil ? @"" : asterisks);
    
    int diamonds = [self.hotelBooking.hotelPrefRank intValue];
	if (diamonds == 0)
	{
		ivDiamonds.hidden = YES;
        lblPreferred.text = [Localizer getLocalizedText:@"Not Preferred"];
	}
	else
	{
        //NSLog(@"diamonds %d", diamonds);
        ivDiamonds.hidden = NO;
        if(diamonds == 4)
        {
            ivDiamonds.image = [UIImage imageNamed:@"diamonds_1"];
            lblPreferred.text = [Localizer getLocalizedText:@"Least Preferred"];
        }
        else if(diamonds == 5)
        {
            ivDiamonds.image = [UIImage imageNamed:@"diamonds_2"];
            lblPreferred.text = [Localizer getLocalizedText:@"Preferred"];
        }
        else if(diamonds == 10)
        {
            ivDiamonds.image = [UIImage imageNamed:@"diamonds_3"];
            lblPreferred.text = [Localizer getLocalizedText:@"Most Preferred"];
        }
        else if(diamonds == 1)
        {
            ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_1"];
            lblPreferred.text = [Localizer getLocalizedText:@"Chain Least Preferred"];
        }
        else if(diamonds == 2)
        {
            ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_2"];
            lblPreferred.text = [Localizer getLocalizedText:@"Chain Preferred"];
        }
        else if(diamonds == 3)
        {
            ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_3"];
            lblPreferred.text = [Localizer getLocalizedText:@"Chain Most Preferred"];
        }
        
        CGSize lblSize = [lblPreferred.text sizeWithFont:lblPreferred.font];
        
        int x = lblPreferred.frame.origin.x + lblSize.width + 4;
        ivDiamonds.frame = CGRectMake(x, ivDiamonds.frame.origin.y, ivDiamonds.frame.size.width, ivDiamonds.frame.size.height);
	}
	
	//The propertyImagePairs is an array of arrays that has both the thumbnail and the actual hotel image URL in the second array.
	//[self configureWithImagePairs:hotelResult.propertyImagePairs Owner:owner];
	self.imageViewerMulti = [[ImageViewerMulti alloc] init];	// Retain count = 2
	 // Retain count = 1
	imageViewerMulti.parentVC = self;
	
	[imageViewerMulti configureWithImagePairsForHotel:hotelBooking Owner:self ImageViewer:ivHotel] ; //]:hotelResult.propertyImagePairs Owner:owner ImageViewer:ivHotel];
	imageViewerMulti.aImageURLs = [imageViewerMulti getImageURLsForHotel:hotelBooking ]; //:hotelResult.propertyImagePairs];
	self.aImageURLs = [imageViewerMulti getImageURLsForHotel:hotelBooking];
	//self.aImageURLs = [self getImageURLs:hotelResult.propertyImagePairs];
}

-(IBAction) showHotelImages:(id)sender
{
	[imageViewerMulti showHotelImages:sender];	
}

- (IBAction)priceToBeatHeaderInfoClicked:(UIButton *)sender
{
    TextViewController *tvc = [[TextViewController alloc] initWithTitle:[@"Price to Beat" localize]];
    if([[UserConfig getSingleton].travelPointsConfig[@"HotelTravelPointsEnabled"] boolValue]) {
        tvc.text = [@"P2B_AIR_BOOKING_HEADER_POINTS_ENABLED" localize];
    }
    else {
        tvc.text = [@"P2B_AIR_BOOKING_HEADER_POINTS_DISABLED" localize];
    }
    [self.navigationController pushViewController:tvc animated:YES];
    
    [self logFlurryEventsForTravelPoints];
}

- (void)logFlurryEventsForTravelPoints
{
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    dict[@"Type"] = @"Hotel";
    dict[@"From Screen"] = @"Room List";
    [Flurry logEvent:@"Price-to-Beat: Price-to-Beat Range Viewed" withParameters:dict];
}
@end

