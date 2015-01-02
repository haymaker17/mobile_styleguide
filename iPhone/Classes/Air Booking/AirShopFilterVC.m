//
//  AirShopFilterVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/8/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AirShopFilterVC.h"
#import "FormatUtils.h"
#import "AirFilterManager.h"
#import "EntityAirFilter.h"
#import "EntityAirFilterSummary.h"
#import "AirShopFilteredResultsVC.h"
#import "DateTimeFormatter.h"
#import "AirFilter.h"
#import "Config.h"
#import "LabelConstants.h"
#import "TextViewController.h"
#import "UserConfig.h"

@implementation AirShopFilterVC

@synthesize fetchedResultsController=__fetchedResultsController;
@synthesize managedObjectContext=__managedObjectContext;
@synthesize tableList, airShopResults, sortOrder, airShop;
@synthesize taFields;

#define kSortPref 0
#define kSortPrice 1
#define kSortDeparture 2
#define kSortDuration 3

#pragma mark -
#pragma mark MVC stuff
-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:AIR_FILTER] )
	{
        NSArray *aResults = [[AirFilterManager sharedInstance] fetchAll];
        if([aResults count] < 1)
            [self showNoDataView:self asSubviewOfView:tableList];
    }
}

-(NSString *) getViewIDKey
{
    return @"AIRSHOPFILTER";
}

#pragma mark - View Controller Stuff
-(void) dealloc
{
    // MOB-9650. Stop listing to fetched results notifications which can occur when the AIR_FILTER
    // message is parsed and written to core data.
    if (__fetchedResultsController != nil)
        __fetchedResultsController.delegate = nil;
}


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
        
        
        
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle
-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setToolbarHidden:NO];
    // Need to check if there is a benchmark to display or not
    if ([self.airShop.benchmark.price doubleValue] || [self.airShop.travelPointsInBank intValue])
    {
        self.lblBenchmark.attributedText = [self getPriceToBeatHeaderAttributedText];
        [self.lblBenchmark sizeToFit];
        CGFloat tableHeaderHeight = self.lblBenchmark.frame.origin.y + self.lblBenchmark.frame.size.height + 8;
        self.viewForTableViewHeader.frame = CGRectMake(self.viewForTableViewHeader.frame.origin.x, self.viewForTableViewHeader.frame.origin.y
                                                       , self.viewForTableViewHeader.frame.size.width, tableHeaderHeight);
        self.tableList.tableHeaderView = self.viewForTableViewHeader;
        self.viewForTableViewHeader.hidden = NO;
    }
    else
    {
        self.viewForTableViewHeader.hidden = YES;
    }
}

-(NSAttributedString *)getPriceToBeatHeaderAttributedText
{
    NSDictionary *attributes = @{NSFontAttributeName : [UIFont systemFontOfSize:14], NSForegroundColorAttributeName : [UIColor blackColor]};
    NSDictionary *blueTextattributes = @{NSFontAttributeName : [UIFont systemFontOfSize:14], NSForegroundColorAttributeName : [UIColor bookingBlueColor]};
    
    NSMutableAttributedString *text = [[NSMutableAttributedString alloc] init];
    NSString *travelPoints = self.airShop.travelPointsInBank;
    if ([self.airShop.benchmark.price doubleValue])
    {
        NSString *p2b = [FormatUtils formatMoney:[self.airShop.benchmark.price stringValue] crnCode:self.airShop.benchmark.crnCode];
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

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self.navigationController setToolbarHidden:NO];
    
    [[ExSystem sharedInstance].imageControl startVendorImageCache];
    
    self.sortOrder = kSortPrice;
    [self showLoadingViewWithText:[Localizer getLocalizedText:@"Fetching Filter Results"]];
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    self.managedObjectContext = [ad managedObjectContext];
    [self refetchData];
    
    self.title = [self getTitleText];
}

- (NSString *)getTitleText
{
    NSString *titleText;
    if ([Config isGov])
    {
        if ([airShopResults.rateType isEqualToString: @"LimitedCapacity"])
            titleText = [Localizer getLocalizedText:@"Govt. Contract Discounted"];
        else if ([airShopResults.rateType isEqualToString: @"Contract"])
            titleText = [Localizer getLocalizedText:@"Govt. Contract"];
        else if ([airShopResults.rateType isEqualToString: @"ContractBusiness"])
            titleText = [Localizer getLocalizedText:@"Govt. Contract"];
        else if ([airShopResults.rateType isEqualToString: @"MeToo"])
            titleText = [Localizer getLocalizedText:@"Non - Contract Government"];
        else if ([airShopResults.rateType isEqualToString: @"LowestPublished"])
            titleText = [Localizer getLocalizedText:@"Lowest Published"];
        else if ([airShopResults.rateType isEqualToString:@"None"])
            titleText = [Localizer getLocalizedText:@"Other Rate Type"];
        else
            titleText = [@"All Flights" localize];
    }

    if ([Config isGov])
    {
        titleText = [Localizer getLocalizedText:@"Select Flight"];
    }
    else if([airShopResults.numStops intValue] == 0)
    {
        titleText = [Localizer getLocalizedText:@"Nonstop"];
        if([airShopResults.airline isEqualToString:@"ZZZZZZZZTOTAL"])
            titleText = [Localizer getLocalizedText:@"All Nonstop"];
    }
    else if([airShopResults.numStops intValue] == 1)
    {
        titleText = [NSString stringWithFormat:@"%d %@", [airShopResults.numStops intValue], [Localizer getLocalizedText:@"Stop"]];
        if([airShopResults.airline isEqualToString:@"ZZZZZZZZTOTAL"])
            titleText = [NSString stringWithFormat:@"%@ %d %@", [Localizer getLocalizedText:@"All"], [airShopResults.numStops intValue], [Localizer getLocalizedText:@"Stop"]];
    }
    else if([airShopResults.numStops intValue] == -1)
    {
        titleText = [@"All Flights" localize];
    }
    else
    {
        titleText = [NSString stringWithFormat:@"%d %@", [airShopResults.numStops intValue], [Localizer getLocalizedText:@"Stops"]];
        if([airShopResults.airline isEqualToString:@"ZZZZZZZZTOTAL"])
            titleText = [NSString stringWithFormat:@"%@ %d %@", [Localizer getLocalizedText:@"All"], [airShopResults.numStops intValue], [Localizer getLocalizedText:@"Stop"]];
    }
    return titleText;
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Clear out the vendorImage Cache
    [[ExSystem sharedInstance].imageControl stopVendorImageCache];

    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [[self.fetchedResultsController sections] count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
    return [sectionInfo numberOfObjects];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    AirShopFilterCell *cell = (AirShopFilterCell *)[tableView dequeueReusableCellWithIdentifier: @"AirShopFilterCell"];
    if (cell == nil)  
    {
        NSArray *nib = nil;
        if(airShop.isRoundTrip)
            nib = [[NSBundle mainBundle] loadNibNamed:@"AirShopFilterCell" owner:self options:nil];
        else
            nib = [[NSBundle mainBundle] loadNibNamed:@"AirShopFilterSingleCell" owner:self options:nil];
        
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[AirShopFilterCell class]])
                cell = (AirShopFilterCell *)oneObject;
    }
    
    [self configureCell:cell atIndexPath:indexPath];
    return cell;
    
}


#pragma mark -
#pragma mark Table Delegate Methods 
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
{	
    return [self.airShop.benchmark.price doubleValue] || [self.airShop.travelPointsInBank intValue] ? [self getTitleText] : nil;
}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:newIndexPath];
    EntityAirFilterSummary *entity = (EntityAirFilterSummary *)managedObject;
    
    if ([entity maxEnforcementLevel] != nil && [[entity maxEnforcementLevel] intValue] != kViolationAutoFail)
    {
        AirShopFilteredResultsVC *vc = [[AirShopFilteredResultsVC alloc] initWithNibName:@"AirShopFilteredResultsVC" bundle:nil];
        vc.airShopResults = self.airShopResults;
        vc.taFields = self.taFields;
        vc.airSummary = entity;
        vc.airShop = self.airShop;
        //Make [vc view] call to force controller to load its view hierarchy:
        [vc view];
        [self.navigationController pushViewController:vc animated:YES];
        vc.lblHeading.text = self.lblHeading.text;
        vc.lblDates.text = self.lblDates.text;
//        vc.lblBenchmark.text = self.lblBenchmark.text;
        [vc.lblBenchmark setHidden:YES]; // TODO: remove lblBenchmark
    }
    else
    {
        NSMutableString *violationMessage = [[NSMutableString alloc] init];
        NSArray *airViolations = [[AirViolationManager sharedInstance] fetchByFareId:entity.fareId];
        
        for (EntityAirViolation *airViolation in airViolations) {
            if ([[airViolation enforcementLevel] intValue] == kViolationAutoFail)
                [violationMessage appendFormat:([violationMessage length] ? @"\n%@" : @"%@"),airViolation.message];
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
        [tableView deselectRowAtIndexPath:newIndexPath animated:YES];
    }
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    CGFloat rowHeight = 69;
    if(airShop.isRoundTrip)
        rowHeight = 103;
    
    if([UserConfig getSingleton].showGDSNameInSearchResults)
    {
        NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
        EntityAirFilterSummary *entity = (EntityAirFilterSummary *)managedObject;

        if([entity.gdsName length])
        {
            rowHeight += 11;
        }
    }
    return rowHeight;
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
    
    NSSortDescriptor *sortDescriptor = nil;
    NSSortDescriptor *sort2 = nil;
    
    if(sortOrder == kSortPrice)
    {
        sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"fare" ascending:YES];
        sort2 = [[NSSortDescriptor alloc] initWithKey:@"pref" ascending:YES];
        [fetchRequest setSortDescriptors:@[sortDescriptor, sort2]];
    }
    else if(sortOrder == kSortDeparture)
    {
        sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"departureTime" ascending:YES];
        sort2 = [[NSSortDescriptor alloc] initWithKey:@"fare" ascending:YES];
        NSSortDescriptor *sort3 = [[NSSortDescriptor alloc] initWithKey:@"pref" ascending:YES];
        [fetchRequest setSortDescriptors:@[sortDescriptor, sort2, sort3]];
    }
    else if(sortOrder == kSortPref)
    {
        sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"pref" ascending:NO];
        sort2 = [[NSSortDescriptor alloc] initWithKey:@"fare" ascending:YES];
        [fetchRequest setSortDescriptors:@[sortDescriptor, sort2]];
    }
    else if(sortOrder == kSortDuration)
    {
        sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"durationTotal" ascending:YES];
        sort2 = [[NSSortDescriptor alloc] initWithKey:@"fare" ascending:YES];
        NSSortDescriptor *sort3 = [[NSSortDescriptor alloc] initWithKey:@"pref" ascending:YES];
        [fetchRequest setSortDescriptors:@[sortDescriptor, sort2, sort3]];
    }

    
    NSFetchedResultsController *theFetchedResultsController = 
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest 
                                        managedObjectContext:self.managedObjectContext sectionNameKeyPath:nil 
                                                   cacheName:@"Master"];
    self.fetchedResultsController = theFetchedResultsController;
    __fetchedResultsController.delegate = self;

    
    return __fetchedResultsController;    
}


#pragma mark - Fetched results controller delegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableList beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo
           atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
{
    switch(type)
    {
        case NSFetchedResultsChangeInsert:
            [self.tableList insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [self.tableList deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject
       atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type
      newIndexPath:(NSIndexPath *)newIndexPath
{
    UITableView *tableView = self.tableList;
    
    switch(type)
    {
            
        case NSFetchedResultsChangeInsert:
            [tableView insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeUpdate:
            [self configureCell:(AirShopFilterCell*)[self.tableList cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
            break;
            
        case NSFetchedResultsChangeMove:
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [tableView insertRowsAtIndexPaths:@[newIndexPath]withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableList endUpdates];
    
    if ([self.fetchedResultsController.sections count] <= 0)
    {
        [self showNoDataView:self asSubviewOfView:tableList];
    }
    else 
    {
        [self hideLoadingView];
        [self hideNoDataView];
        [self makeToolbar];
    }
}

-(void) makeToolbar
{
    NSArray *a = [[AirFilterSummaryManager sharedInstance] fetchAll];
    UIView *viewTotal = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 200, 32)];
    UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 200, 32)];
    NSString *sortByText = @"";
    if(self.sortOrder == kSortDeparture)
        sortByText = [Localizer getLocalizedText:@"results by Earliest Departure"];
    else if(self.sortOrder == kSortDuration)
        sortByText = [Localizer getLocalizedText:@"results by Duration"];
    else if(self.sortOrder == kSortPref)
        sortByText = [Localizer getLocalizedText:@"results by Preferred Carrier"];
    else if(self.sortOrder == kSortPrice)
        sortByText = [Localizer getLocalizedText:@"results by Fare Price"];
    
    lbl.text = [NSString stringWithFormat:@"%lu %@", (unsigned long)[a  count], sortByText];
    if(![ExSystem is7Plus])
	{
        // Only change the results text color if using iOS6
        lbl.backgroundColor = [UIColor clearColor];
        lbl.textColor = [UIColor whiteColor];
    }
    [lbl setFont:[UIFont systemFontOfSize:11]];
    [lbl setTextAlignment:NSTextAlignmentCenter];
    [viewTotal addSubview:lbl];
    UIBarButtonItem *btnTotal = [[UIBarButtonItem alloc] initWithCustomView:viewTotal];

    UIBarButtonItem *btnSort = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Sort"] style:UIBarButtonItemStyleBordered target:self action:@selector(showAction:)];

    UIBarButtonItem *btnFlex = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    NSArray *aBtns = @[btnSort, btnFlex, btnTotal, btnFlex];
    [self setToolbarItems:aBtns];
}

#pragma mark - reset and then fetch the managed results
- (IBAction)priceToBeatHeaderInfoClicked:(UIButton *)sender
{
    TextViewController *tvc = [[TextViewController alloc] initWithTitle:[@"Price to Beat" localize]];
    if([[UserConfig getSingleton].travelPointsConfig[@"AirTravelPointsEnabled"] boolValue]) {
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
    dict[@"Type"] = @"Air";
    dict[@"From Screen"] = @"Air Search Filter Results";
    [Flurry logEvent:@"Price-to-Beat: Price-to-Beat Range Viewed" withParameters:dict];
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
    
    [tableList reloadData];
}

#pragma mark - Cell Config
- (void)configureCell:(AirShopFilterCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    NSString* cellId = [NSString stringWithFormat:@"%ld", (long)[indexPath row]];
//    NSLog(@"configure cell for %@", cellId);
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityAirFilterSummary *entity = (EntityAirFilterSummary *)managedObject;
    
    if([airShopResults.airline isEqualToString:@"ZZZZZZZZTOTAL"])
    {
        cell.lblAirline.text = (airShop.vendors)[entity.airlineCode];
        cell.lblCost.text = [FormatUtils formatMoney:[entity.fare stringValue] crnCode:entity.crnCode] ;
        UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:entity.airlineCode VendorType:@"a_small" RespondToIV:cell.ivLogo cellId:cellId];
        if(gotImg != nil)
            cell.ivLogo.image = gotImg;
    }
    else
    {
        cell.lblAirline.text = (airShop.vendors)[entity.airlineCode];
        cell.lblCost.text = [FormatUtils formatMoney:[entity.fare stringValue] crnCode:entity.crnCode] ;
        UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:entity.airlineCode VendorType:@"a_small" RespondToIV:cell.ivLogo cellId:cellId];
        if(gotImg != nil)
            cell.ivLogo.image = gotImg;
    }
    
    cell.lblDepartIata.text = entity.departureIata;
    cell.lblArriveIata.text = entity.arrivalIata;
    cell.lblDepartTime.text = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:entity.departureTime];
    cell.lblArriveTime.text = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:entity.arrivalTime];
    
    NSString *departDOW = [DateTimeFormatter formatDateEEEByDate:entity.departureTime];
    NSString *arriveDOW = [DateTimeFormatter formatDateEEEByDate:entity.arrivalTime];
    if([departDOW isEqualToString:arriveDOW])
        cell.ivOvernight.image = nil;
    else
        cell.ivOvernight.image = [UIImage imageNamed:@"overnight_flight"];
    
    int flightMinutes = [entity.duration intValue];
    int flightHours = flightMinutes / 60;
    if (flightHours > 0) 
        flightMinutes = flightMinutes - (flightHours * 60);
    NSString *dur = [NSString stringWithFormat:@"%dh %dm", flightHours, flightMinutes];
    cell.lblDurationStops.text = [NSString stringWithFormat:@"%@ / %@ Stops", dur, [entity.numStops stringValue] ];
    
    cell.lblRoundDepartIata.text = entity.roundDepartureIata;
    cell.lblRoundArriveIata.text = entity.roundArrivalIata;
    cell.lblRoundDepartTime.text = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:entity.roundDepartureTime];
    cell.lblRoundArriveTime.text = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:entity.roundArrivalTime];
    
    departDOW = [DateTimeFormatter formatDateEEEByDate:entity.roundDepartureTime];
    arriveDOW = [DateTimeFormatter formatDateEEEByDate:entity.roundArrivalTime];
    if([departDOW isEqualToString:arriveDOW])
        cell.ivRoundOvernight.image = nil;
    else
        cell.ivRoundOvernight.image = [UIImage imageNamed:@"overnight_flight"];
    
    flightMinutes = [entity.roundDuration intValue];
    flightHours = flightMinutes / 60;
    if (flightHours > 0) 
        flightMinutes = flightMinutes - (flightHours * 60);
    dur = [NSString stringWithFormat:@"%dh %dm", flightHours, flightMinutes];
    cell.lblRoundDurationStops.text = [NSString stringWithFormat:@"%@ / %@ Stops", dur, [entity.roundNumStops stringValue] ];
    
    NSString *prefRanking = [entity.pref stringValue];

    if(prefRanking == nil || [prefRanking length] == 0 || [prefRanking intValue] == 0)
        cell.ivPref.image = nil;
    else if([prefRanking intValue] == 2)
        cell.ivPref.image = [UIImage imageNamed: @"diamonds_right_2"];
    else if([prefRanking intValue] == 3)
        cell.ivPref.image = [UIImage imageNamed: @"diamonds_3"];
    else
        cell.ivPref.image = [UIImage imageNamed: @"diamonds_right_1"];
    
    if ([Config isGov])
    {
        if ([entity.rateType isEqualToString: @"LimitedCapacity"])
        {
            cell.ivPref.image = [UIImage imageNamed:@"icon_stars_3"];
        }
        else if ([entity.rateType isEqualToString: @"Contract"])
        {
            cell.ivPref.image = [UIImage imageNamed:@"icon_stars_3"];
        }
        else if ([entity.rateType isEqualToString: @"ContractBusiness"])
        {
            cell.ivPref.image = [UIImage imageNamed:@"icon_stars_3"];
        }
        else if ([entity.rateType isEqualToString: @"MeToo"])
        {
            cell.ivPref.image = [UIImage imageNamed:@"icon_stars_2"];
        }
    }
    // When debugging, airRule is always nil, therefore, no need for it.
    //    EntityAirRules *airRule = (EntityAirRules*)[[AirRuleManager sharedInstance]fetchMostSevre:entity.fareId];
    
    [cell.lblCost setShadowOffset:CGSizeMake(0, 0)];
    cell.lblCost.textColor = [UIColor bookingBlueColor];
    if([entity maxEnforcementLevel] != nil)
    {
        int eLevel = [[entity maxEnforcementLevel] intValue];
        if(eLevel < kViolationLogForReportsOnly || eLevel == 100)
        {
//            [cell.lblCost setTextColor:[UIColor bookingGreenColor]];
            cell.ivRule.hidden = YES;
        }
        else if(eLevel >= kViolationLogForReportsOnly && eLevel <= kViolationNotifyManager)
        {
//            [cell.lblCost setTextColor:[UIColor bookingYellowColor]];
            cell.ivRule.image = [UIImage imageNamed:@"icon_yellowex"];
            cell.ivRule.hidden = NO;
        }    
        else if(eLevel >= kViolationRequiresPassiveApproval && eLevel <= kViolationRequiresApproval)
        {
//            [cell.lblCost setTextColor:[UIColor bookingRedColor]];
            cell.ivRule.image = [UIImage imageNamed:@"icon_redex"];
            cell.ivRule.hidden = NO;
        } 
        else if(eLevel == kViolationAutoFail)
        {
//            [cell.lblCost setTextColor:[UIColor bookingGrayColor]];
            cell.ivRule.hidden = YES;
        } 
        else
        {
//            [cell.lblCost setTextColor:[UIColor bookingRedColor]];
            cell.ivRule.image = [UIImage imageNamed:@"icon_redex"];
            cell.ivRule.hidden = NO;
        }
    }
    else
    {
//        [cell.lblCost setTextColor:[UIColor bookingGreenColor]];
        cell.ivRule.hidden = YES;
    }
    
    if([entity.refundable boolValue])
    {
        if ([Config isGov])
        {
            cell.ivRefundable.hidden = NO;
            cell.lblRefundable.hidden = YES;
        }
        else{
            cell.lblRefundable.hidden = NO;
            cell.lblRefundable.text = [Localizer getLocalizedText:@"(Refundable)"];
        }
    }
    else
    {
        cell.ivRefundable.hidden = YES;
        cell.lblRefundable.hidden = YES;
    }
    
    if ([entity.travelPoints intValue] != 0) {
        cell.lblTravelPoints.hidden = NO;
        if ([entity.travelPoints intValue] > 0) {
            cell.lblTravelPoints.text = [NSString stringWithFormat:[@"Earn %d pts." localize],[entity.travelPoints intValue]];
            cell.lblTravelPoints.textColor = [UIColor bookingGreenColor];
        }
        else {
            cell.lblTravelPoints.text = [NSString stringWithFormat:[@"Use %d pts." localize],-[entity.travelPoints intValue]];
            cell.lblTravelPoints.textColor = [UIColor bookingRedColor];
        }
    }
    else {
        cell.lblTravelPoints.hidden = YES;
    }
    
    cell.lblGdsName.hidden = YES;
    if([UserConfig getSingleton].showGDSNameInSearchResults && [entity.gdsName length])
    {
        cell.lblGdsName.hidden = NO;
        cell.lblGdsName.text = [NSString stringWithFormat:@"(%@)", entity.gdsName];
    }

}


#pragma mark - Action
- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    self.actionPopOver = nil;
}

-(void) showAction:(id)sender
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
                          , [Localizer getLocalizedText:@"Price"]
                          , [Localizer getLocalizedText:@"Earliest Departure"]
                          , [Localizer getLocalizedText:@"Duration"]
                          , nil];
	if([UIDevice isPad])
		[self.actionPopOver showFromBarButtonItem:sender animated:YES];
	else
	{
		self.actionPopOver.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
		[self.actionPopOver showFromToolbar:[ConcurMobileAppDelegate getBaseNavigationController].toolbar];
    }
    
}

-(void) actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if(buttonIndex == 0)
        self.sortOrder = kSortPref;
    else if (buttonIndex == 1)
        self.sortOrder = kSortPrice;
    else if (buttonIndex == 2)
        self.sortOrder = kSortDeparture;
    else if (buttonIndex == 3)
        self.sortOrder = kSortDuration;
    
    [self refetchData];
    
    NSIndexPath* indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
    [tableList scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionTop animated:YES];
    [self makeToolbar];
}
@end
