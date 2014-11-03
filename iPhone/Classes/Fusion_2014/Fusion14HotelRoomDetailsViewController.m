//
//  Fusion14HotelRoomDetailsViewController.m
//  ConcurMobile
//
//  Created by Sally Yan on 4/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "Fusion14HotelRoomDetailsViewController.h"
#import "Fusion14HotelBookingViewController.h"
#import "RoomsListHeaderImageCell.h"
#import "Fusion14HotelRoomDetailsCell.h"
#import "HotelSearch.h"
//#import "HotelSearchCriteria.h"
#import "FindRooms.h"
#import "PolicyViolationConstants.h"
#import "WaitViewController.h"
#import "Config.h"
#import "EntityHotelImage.h"


@interface Fusion14HotelRoomDetailsViewController ()

@property (nonatomic, strong) RoomsListHeaderImageCell *headerCell;
@property (nonatomic, strong) Fusion14HotelBookingViewController *hotelBookingVC;
@property (nonatomic, strong) HotelSearch	*hotelSearch;
@property (nonatomic, strong) EntityHotelBooking *hotelBooking;
@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, strong) NSFetchedResultsController *fetchedResultsController;
@property (nonatomic, strong) NSArray *roomList;

@end

@implementation Fusion14HotelRoomDetailsViewController

@synthesize fetchedResultsController=__fetchedResultsController;


- (id) initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    
    if (self) {
        ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*)[[UIApplication sharedApplication] delegate];
        self.managedObjectContext = appDelegate.managedObjectContext;
    }
    return self;
}


#pragma mark - view controller methods
- (void)viewDidLoad
{
    [super viewDidLoad];
    [self setNavigationBarRightButtons];
    
    if (self.hotelSearch == nil) {
        [WaitViewController showWithText:nil animated:YES];
    }
    else {
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
    }
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    self.title = @"Rooms";
    
    [self.navigationController setToolbarHidden:YES];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void) setNavigationBarRightButtons
{
    UIButton *btnMapView = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 27, 27)];
    //    [btnMapView addTarget:self action:nil forControlEvents:UIControlEventTouchUpInside];
    [btnMapView setBackgroundImage:[UIImage imageNamed:@"icon_nav_map"] forState:UIControlStateNormal];
    UIBarButtonItem *btnMap = [[UIBarButtonItem alloc] initWithCustomView:btnMapView];
    
    [self.navigationItem setRightBarButtonItems:[NSArray arrayWithObjects:btnMap, nil]];
    
}

#pragma mark - Navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"Fusion14HotelBookingViewController"]) {
        self.hotelBookingVC = segue.destinationViewController;
    }
}

#pragma mark - set up view header
-(void)setTableViewHeader
{
    self.tableView.tableHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, self.tableView.frame.size.width, 275.0f)];
    
    self.headerCell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion14HotelRoomDetailsTableHeaderCell"];

    [self.tableView dequeueReusableHeaderFooterViewWithIdentifier:@"Fusion14HotelRoomDetailsTableHeaderCell"];
    [self.tableView.tableHeaderView addSubview:self.headerCell];
    
    CGRect sepFrame = CGRectMake(15, self.tableView.tableHeaderView.frame.size.height - 1 , 320, 1);
    UIView *seperatorView = [[UIView alloc] initWithFrame:sepFrame];
    seperatorView.backgroundColor = [UIColor colorWithWhite:224.0/255.0 alpha:1.0];
    [self.tableView.tableHeaderView addSubview:seperatorView];
    
    [self configureHeader:self.headerCell];
}

-(void)configureHeader:(RoomsListHeaderImageCell*)cell
{
    // Disabled all Fusion related code. 
//	cell.labelHotelName.text = self.hotelBooking.hotel;
//	
//    NSString *distanceUnit = [self.hotelBooking.distanceUnit isEqualToString:@"mi"]?@"miles" : self.hotelBooking.distanceUnit;
//    cell.labelHotelDistance.text = [NSString stringWithFormat:@"%@ %@", self.hotelBooking.distance, distanceUnit];
//    
//    // set up the location
//    NSString *cityAndState = nil;
//    if ([self.hotelBooking.city length]) {
//        cityAndState = self.hotelBooking.city;
//    }
//    if ([self.hotelBooking.state length]){
//        if (cityAndState != nil) {
//            cityAndState = [[cityAndState stringByAppendingString:@", "] stringByAppendingString:self.hotelBooking.state];
//        }
//        else{
//            cityAndState = self.hotelBooking.state;
//        }
//    }
//    if (cityAndState != nil) {
//        cell.labelHotelCityAndState.text = cityAndState;
//    }
//    
//    // get the rating stars image
//    int asterisks = [self.hotelBooking.starRating intValue];
//    if (asterisks == 0){
//        cell.imageViewHotelRating.hidden = YES;
//    }
//    else
//    {
//        cell.imageViewHotelRating.hidden = NO;
//        int starCount = asterisks;
//        if(starCount == 1)
//            cell.imageViewHotelRating.image = [UIImage imageNamed:@"hotel_one_star"];
//        else if(starCount == 2)
//            cell.imageViewHotelRating.image = [UIImage imageNamed:@"hotel_two_star"];
//        else if(starCount == 3)
//            cell.imageViewHotelRating.image = [UIImage imageNamed:@"hotel_three_star"];
//        else if(starCount == 4)
//            cell.imageViewHotelRating.image = [UIImage imageNamed:@"hotel_four_star"];
//        else if(starCount == 5)
//            cell.imageViewHotelRating.image = [UIImage imageNamed:@"hotel_five_star"];
//    }
//	
//    // get the hotel image
//    cell.imageViewHotelIcon.image = nil;  // The cell may have been dequeued with an image already in it.  Get rid of it.
//    cell.imageViewHotelImage.image = nil;
//    if ([self.hotelBooking.relHotelImage count] > 0)
//    {
//        for(EntityHotelImage *image in self.hotelBooking.relHotelImage)
//        {
//            UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
//            [[ExSystem sharedInstance].imageControl getImageAsynchWithUrl:image.thumbURI RespondToImage:img IV:cell.imageViewHotelIcon];
////            [[ExSystem sharedInstance].imageControl getImageAsynchWithUrl:image.imageURI RespondToImage:img IV:cell.imageViewHotelImage];
//            break;
//        }
//    }
//    
//    // Show a hardcoded image for Fusion14 demo.
//    // Later iterate through images and show the image which fits the view nicely.
//    double delayInSeconds = 0.5;
//    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
//    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
//        // This does NOT fail the test cause it's on another thread.
//        cell.imageViewHotelImage.image = [UIImage imageNamed:@"Fusion14_static_Hotel"];
//    });
//
//    cell.segmentedCtrl.selectedSegmentIndex = 1;

}


#pragma mark - Fetched results controller
- (NSFetchedResultsController*) fetchedResultsController
{
    if (__fetchedResultsController != nil) {
        return __fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityHotelRoom" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sorter = [[NSSortDescriptor alloc] initWithKey:@"rate" ascending:YES];
    [fetchRequest setSortDescriptors:@[sorter]];
    
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(relHotelBooking = %@)", self.hotelBooking];
    [fetchRequest setPredicate:predicate];
    
    NSFetchedResultsController *theFetchedResultsController = [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                                                                                  managedObjectContext:self.managedObjectContext
                                                                                                    sectionNameKeyPath:nil
                                                                                                             cacheName:nil];
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

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    NSLog(@"Num of rows %i", [[self.fetchedResultsController sections] count]);
     return [[self.fetchedResultsController sections] count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // fake the room description for The Westin St Francis
    if ([self.hotelBooking.hotel isEqualToString:@"The Westin St Francis"]) {
        return 3;
    }
    
    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
    NSLog(@"[sectionInfo numberOfObjects] = %d", [sectionInfo numberOfObjects]);
    return [sectionInfo numberOfObjects];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    Fusion14HotelRoomDetailsCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Fusion14HotelRoomDetailsCell" forIndexPath:indexPath];
    [self configurCell:cell cellForRowAtIndexPath:indexPath];
    
    return cell;
}


-(void) configurCell:(Fusion14HotelRoomDetailsCell*)cell cellForRowAtIndexPath:(NSIndexPath*)indexPath
{
    if (self.roomList != nil && [self.roomList count] > indexPath.row) {
         NSManagedObject *managedObject = (NSManagedObject*)[self.roomList objectAtIndex:indexPath.row];
        EntityHotelRoom *room = (EntityHotelRoom*)managedObject;
        
        cell.viewCancellation.hidden = YES; // don't need to show the cancellation policy for now
        
        
        // set up travel points
        int travelPoints = [room.travelPoints intValue];
        if (travelPoints > 0) {
            cell.labelTravelPoints.hidden = NO;
            cell.labelTravelPoints.text = [NSString stringWithFormat:@"+%i pts", travelPoints];
        } else{
            cell.labelTravelPoints.hidden = YES;
        }
        
        // fake price for The Westin St Francis
//        if ([self.hotelBooking.hotel isEqualToString:@"The Westin St Francis"]) {
//            if (indexPath.row == 0) {
//                cell.labelRoomPrice.text = @"405";
//            }
//            else if(indexPath.row == 1){
//                cell.labelRoomPrice.text = @"425";
//            }
//            else{
//                cell.labelRoomPrice.text = @"465";
//            }
//        } else {
             NSString *price = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", [room.rate doubleValue]] crnCode:room.crnCode];
             int indexOfDot = [price rangeOfString:@"."].location;
             if (indexOfDot > 0 && indexOfDot < price.length) {
                 cell.labelRoomPrice.text = [price substringToIndex:indexOfDot];
             }

            
            // Check how long the rate text would be on a single line
            NSDictionary *attributes = [NSDictionary dictionaryWithObjectsAndKeys:cell.labelRoomPrice.font, NSFontAttributeName, nil];
            //MOB-16858 CoreData: error: Serious application error.  Exception was caught during Core Data change processing.  This is usually a bug within an observer of NSManagedObjectContextObjectsDidChangeNotification.  NSConcreteAttributedString initWithString:: nil value with userInfo (null)
            CGFloat fontsize = [[[NSAttributedString alloc] initWithString:cell.labelRoomPrice.text attributes:attributes] size].width; // this line throws exception if cell is nil (NSInvalidArgumentException is thrown reason "NSConcreteAttributedString initWithString:: nil value"), adding if-check at the start of method to return if cell is nil
            
            if (cell.labelRoomPrice.frame.size.width < fontsize)
            {
                // If the rate text exceeds the width of the label frame, we need to split it
                // We can't trust how the auto word-wrap will perform the split, we get odd results
                // If the rate contains a space, split at that point and insert a newline instead of the space.
                NSRange range = [cell.labelRoomPrice.text rangeOfCharacterFromSet:[NSCharacterSet whitespaceCharacterSet]];
                // MOB16423 For parts of Europe ' $' may appear at the end, we don't want to force the split in this situation
                if (range.location > 0 && range.location < ([cell.labelRoomPrice.text length]-2))
                {
                    NSString *newString3 = [NSString stringWithFormat:@"%@\n%@", [cell.labelRoomPrice.text substringToIndex:range.location], [cell.labelRoomPrice.text substringFromIndex:range.location+1]];
                    [cell.labelRoomPrice setText: newString3];
                }
            }
//        }
        if ([self.hotelBooking.hotel isEqualToString:@"The Westin St. Francis"]) {
            if (indexPath.row == 0) {
                cell.labelRoomType.text = @"Classic Queen – Landmark";
                cell.labelRoomSize.text = @"1 Queen";
            }
            else if(indexPath.row == 1){
                cell.labelRoomType.text = @"Traditional King – Landmark";
                cell.labelRoomSize.text = @"1 King";
            }
            else{
                cell.labelRoomType.text = @"Grand Deluxe King – Tower";
                cell.labelRoomSize.text = @"1 King";
            }
        } else {
        
            // set up room details
            NSString *title = nil;
            NSString *subTitle = nil;
            int index = [room.summary rangeOfString:@"-"].location;
            if (index > 0 && index < room.summary.length) {
                title = [room.summary substringWithRange:NSMakeRange(0,index)];
            }
            if (index + 2 < room.summary.length) {
                subTitle = [room.summary substringFromIndex:index + 2];
            }
            
            // the string format returned from server does not always contains @"-"
            if (![title length] && ![subTitle length]) {
                subTitle = room.summary;
            }

            if ([title length]) {
                cell.labelRoomType.hidden = NO;
                cell.labelRoomType.text = title;
            } else{
                cell.labelRoomType.hidden = YES;
            }
            if ([subTitle length]) {
                cell.labelRoomSize.hidden = NO;
                cell.labelRoomSize.text = subTitle;
            } else{
                cell.labelRoomSize.hidden = YES;
            }
        }

//        NSArray *roomDetails = [room.summary componentsSeparatedByString:@"-"];
//        if ([roomDetails count] > 1) {
//            cell.labelRoomType.text = [roomDetails objectAtIndex:0];
//            cell.labelRoomSize.text = [roomDetails objectAtIndex:1];
//        }
//        else{
//            cell.labelRoomType.text = room.summary;
//            cell.labelRoomSize.hidden = YES;
//        }
        if (indexPath.row < 3) {
            cell.labelRecommendationTag.hidden = NO;
            cell.labelRecommendationTag.text = @"Recommended";
            CGSize textSize = [cell.labelRecommendationTag.text sizeWithFont:cell.labelRecommendationTag.font];
            cell.coRecommendationTagWidth.constant = textSize.width + 6;
            cell.coRecommendationTagHeight.constant = textSize.height+ 6;
        }
        else{
            cell.labelRecommendationTag.hidden = YES;
        }

//        // MOB-8067 This was fixed in branch, but not migrated onto trunk
//        if([room maxEnforcementLevel] != nil)
//        {
//            int eLevel = [[room maxEnforcementLevel] intValue];
//            //        NSLog(@"eLevel = %d", eLevel);
//            if(eLevel < kViolationLogForReportsOnly || eLevel == 100) {
//                cell.labelRecommendationTag.hidden = YES;
//            }
////            else if(eLevel >= kViolationLogForReportsOnly && eLevel <= kViolationRequiresPassiveApproval)
////            {
////                //            [cell.rate setTextColor:[UIColor bookingYellowColor]];
////                cell.ivException.image = [UIImage imageNamed:@"icon_yellowex"];
////                cell.ivException.hidden = YES;
////            }
//            // need approval
//            else if(eLevel > kViolationRequiresPassiveApproval && eLevel <= kViolationRequiresApproval) {
//                cell.labelRecommendationTag.hidden = NO;
//            }
//            else if(eLevel == kViolationAutoFail){
//                //            [cell.rate setTextColor:[UIColor bookingGrayColor]];
//                cell.labelRecommendationTag.hidden = YES;
//            }
//            else{
//                //            [cell.rate setTextColor:[UIColor bookingRedColor]];
//                cell.ivException.image = [UIImage imageNamed:@"icon_redex"];
//                cell.ivException.hidden = NO;
//            }
//        }

        
//        cell.lblDepositRequired.hidden = ![room.depositRequired boolValue];
//        cell.lblDepositRequired.text = [@"Deposit required" localize];
        
        
//        if ([room.travelPoints intValue] != 0) {
//            cell.lblTravelPoints.hidden = NO;
//            if ([room.travelPoints intValue] > 0) {
//                cell.lblTravelPoints.text = [NSString stringWithFormat:[@"Earn %d pts." localize],[room.travelPoints intValue]];
//                cell.lblTravelPoints.textColor = [UIColor bookingGreenColor];
//            }
//            else {
//                cell.lblTravelPoints.text = [NSString stringWithFormat:[@"Use %d pts." localize],-[room.travelPoints intValue]];
//                cell.lblTravelPoints.textColor = [UIColor bookingRedColor];
//            }
//        }
//        else {
//            cell.lblTravelPoints.hidden = YES;
//        }
        
    }
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 97.0;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    
    [self reserveRoomAtIndex:[indexPath row]];
}

-(void)reserveRoomAtIndex:(int)roomIndex
{
	//[hotelSearch.selectedHotel.detail selectRoom:roomIndex];
    
    //NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:[NSIndexPath indexPathForRow:roomIndex inSection:0]];
    NSManagedObject *managedObject = (self.roomList)[roomIndex];
    EntityHotelRoom *room = (EntityHotelRoom *)managedObject;
    
    if ([room maxEnforcementLevel] != nil && [[room maxEnforcementLevel] intValue] != kViolationAutoFail) {

//        if ([self.travelPointsInBank length]) {
//            pBag[@"TRAVEL_POINTS_IN_BANK"] = self.travelPointsInBank;
//        }
//        Fusion14HotelBookingViewController *nextController = [[HotelBookingViewController alloc] initWithNibName:@"HotelBookingViewController" bundle:nil];
//        [nextController view];
//        nextController.taFields = self.taFields;
        //For Flurry
//        self.hotelBookingVC.isVoiceBooking = self.isVoiceBooking;
        // MOB-9547 Do not display custom fields if add car/hotel
//        nextController.hideCustomFields = [hotelSearch.tripKey length];
    }
    else {
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
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.hotelSearch, @"HOTEL_SEARCH", self.hotelBooking, @"HOTEL_BOOKING", room, @"HOTEL_ROOM", self.hotelSearch.hotelSearchCriteria, @"HOTEL_SEARCH_CRITERIA", @"YES", @"SHORT_CIRCUIT", nil];
    Msg *msg = [[Msg alloc] init];
    msg.parameterBag = pBag;
    msg.idKey = @"SHORT_CIRCUIT";
    [self.hotelBookingVC didProcessMessage:msg];
}

#pragma mark - MWS responses
-(void)didProcessMessage:(Msg *)msg
{
    BOOL updateView = NO;
	if ([msg.idKey isEqualToString:FIND_HOTEL_ROOMS])
	{
		FindRooms *findRooms = (FindRooms *)msg.responder;
        if ([findRooms.hotelBooking isFault]) // Discard old defunct results.
            return;
        
		self.hotelSearch = findRooms.hotelSearch;
        self.hotelBooking = findRooms.hotelBooking;
        [self setTableViewHeader];
//        [self configureHeader:NO];
        updateView = YES;
	}
	else if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"])
	{
		// A short circuit indicates that the data is available in the parameter bag.
		self.hotelSearch = (HotelSearch*)(msg.parameterBag)[@"HOTEL_SEARCH"];
        self.hotelBooking = (EntityHotelBooking*)(msg.parameterBag)[@"HOTEL_BOOKING"];
        
        //need to update the table header
        [self setTableViewHeader];
//        [self configureHeader:NO];
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
//        [self setPriceToBeatHeader];
        [WaitViewController hideAnimated:nil withCompletionBlock:nil];
        [self refetchData];
        [self.tableView reloadData];
//        [self makeToolbar:hotelSearch.hotelSearchCriteria roomCount:[NSNumber numberWithInt:[hotelBooking.relHotelRoom count]]];
    }
}

@end
