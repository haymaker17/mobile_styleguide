//
//  AirShopResultsVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/5/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "AirShopResultsVC.h"
#import "FormatUtils.h"
#import "AirShopFilterVC.h"
#import "AirFilterSummaryManager.h"
#import "AirFilterManager.h"
#import "EntityAirFilter.h"
#import "EntityAirFilterSummary.h"
#import "DateTimeFormatter.h"
#import "FormatUtils.h"
#import "Config.h"
#import "TextViewController.h"
#import "UserConfig.h"

@implementation AirShopResultsVC
@synthesize fetchedResultsController=__fetchedResultsController;


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

-(NSString *) getViewIDKey
{
    return @"AIRSHOPSUMMARY";
}

#pragma mark - View lifecycle
-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setToolbarHidden:YES];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    [[AirFilterSummaryManager sharedInstance] deleteAll];
    [[AirFilterManager sharedInstance] deleteAll];
    //[self makeFake];
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    self.managedObjectContext = [ad managedObjectContext];
    [self refetchData];
//    self.fetchedResultsController = nil;
//    self.fetchedResultsController = [self fetchedResultsController];
    EntityAirCriteria *airCrit = [self loadEntity];
    NSString *departureCity = (self.airShop.airportCityCodes)[airCrit.DepartureAirportCode];
    NSString *arrivalCity = (self.airShop.airportCityCodes)[airCrit.ReturnAirportCode];
    if(departureCity == nil)
    {
        departureCity = @"";
    }
    if(arrivalCity == nil)
    {
        arrivalCity = @"";
    }
    self.lblHeading.text = [NSString stringWithFormat:@"(%@) %@ to (%@) %@", airCrit.DepartureAirportCode, departureCity, airCrit.ReturnAirportCode, arrivalCity];
    
    if(self.airShop.isRoundTrip)
    {
        self.lblDates.text = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateMediumByDate:airCrit.DepartureDate], [DateTimeFormatter formatDateMediumByDate:airCrit.ReturnDate]];
    }
    else
    {
        self.lblDates.text = [NSString stringWithFormat:@"%@", [DateTimeFormatter formatDateMediumByDate:airCrit.DepartureDate]];
    }
    
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
    
    self.title = [@"Results Summary" localize];
    
    if([self.airShop.stopChoices count] < 1 && [self.airShop.rateTypeChoices count] <1)
        [self showNoDataView:self asSubviewOfView:self.tableList];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.tableList = nil;
    self.lblHeading = nil;
    self.lblDates = nil;
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
    AirShopSummaryCell *cell = (AirShopSummaryCell *)[tableView dequeueReusableCellWithIdentifier: @"AirShopSummaryCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"AirShopSummaryCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[AirShopSummaryCell class]])
                cell = (AirShopSummaryCell *)oneObject;
    }
    
    [self configureCell:cell atIndexPath:indexPath];
    return cell;
 
}


#pragma mark - Cell Config
- (void)configureCell:(AirShopSummaryCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityAirShopResults *entity = (EntityAirShopResults *)managedObject;
    
    UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:entity.airline VendorType:@"a_small" RespondToIV:cell.ivLogo];
    if(gotImg != nil)
        cell.ivLogo.image = gotImg;
    
    NSString *prefRanking = (self.airShop.prefRankings)[entity.airline];
    //NSLog(@"prefRanking=%@", prefRanking);
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

    if([entity.airline isEqualToString:@"ZZZZZZZZTOTAL"])
    {
        if ([Config isGov])
        {
            NSString *rateTypeName = @"";
            if ([entity.rateType isEqualToString: @"LimitedCapacity"])
            {
                rateTypeName = [Localizer getLocalizedText:@"Govt. Contract Discounted"];
                // show or not show icon on total flight row in each section
                //cell.ivPref.image = [UIImage imageNamed:@"flags_3"];
            }
            else if ([entity.rateType isEqualToString: @"Contract"])
            {
                rateTypeName = [Localizer getLocalizedText:@"Govt. Contract"];
                //cell.ivPref.image = [UIImage imageNamed:@"flags_3"];
            }
            else if ([entity.rateType isEqualToString: @"ContractBusiness"])
            {
                rateTypeName = [Localizer getLocalizedText:@"Govt. Contract"];
                //cell.ivPref.image = [UIImage imageNamed:@"flags_3"];
            }
            else if ([entity.rateType isEqualToString: @"MeToo"])
            {
                rateTypeName = [Localizer getLocalizedText:@"Non - Contract Government"];
                //cell.ivPref.image = [UIImage imageNamed:@"flags_2"];
            }
            else if ([entity.rateType isEqualToString: @"LowestPublished"])
                rateTypeName = [Localizer getLocalizedText:@"Lowest Published"];
            else if ([entity.rateType isEqualToString:@"None"])
                rateTypeName = [Localizer getLocalizedText:@"Other Rate Type"];

            cell.lblAirline.text = [ NSString stringWithFormat:[Localizer getLocalizedText:@"All token flights"], rateTypeName] ;
        }
        else
        {
            NSString *stopName = @"Stops";
            if([entity.numStops intValue] == 0)
                stopName = @"Nonstops";
            else
                stopName = [NSString stringWithFormat:@"%d Stops", [entity.numStops intValue]];
            cell.lblAirline.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"All token"], stopName] ;
        }
    }
    else if([entity.airline isEqualToString:@"   TOTAL"])
    {
        cell.lblAirline.text = [Localizer getLocalizedText:@"See All"];
    }
    else
        cell.lblAirline.text = entity.airlineName;
    
    NSString *crnCode = entity.crnCode;
    if(![crnCode length])
        crnCode = @"USD";
    cell.lblCost.text = [FormatUtils formatMoney:[entity.lowestCost stringValue] crnCode:crnCode] ;
    [cell.lblCost setTextColor:[UIColor bookingBlueColor]];
    cell.lblResultCount.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"token results"],[entity.numChoices intValue]];
    cell.lblStarting.text = [@"Starting" localize];
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


#pragma mark -
#pragma mark Table Delegate Methods 
//-(UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
//{
//    id <NSFetchedResultsSectionInfo> sectionInfo = [[self.fetchedResultsController sections] objectAtIndex:section];
//    if([sectionInfo.name intValue] == kSECTION_EXPENSE_POS)
//    {
//        EntityHome *entity = (EntityHome*)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS];
//        if(entity != nil && [entity.itemCount intValue] <= 0)
//        {
//            [viewFooter setHidden:NO];
//            return viewFooter;
//        }
//        else if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER] && entity == nil)
//        {
//            [viewFooter setHidden:NO];
//            return viewFooter;
//        }
//    }
//    
//    return nil;
//}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
{	
    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
    if ([Config isGov])
    {
        if ([sectionInfo.name isEqualToString: @"LimitedCapacity"])
            return [Localizer getLocalizedText:@"Govt. Contract Discounted"];
        
        if ([sectionInfo.name isEqualToString: @"Contract"])
            return [Localizer getLocalizedText:@"Govt. Contract"];

        if ([sectionInfo.name isEqualToString: @"ContractBusiness"])
            return [Localizer getLocalizedText:@"Govt. Contract"];
        
        if ([sectionInfo.name isEqualToString: @"MeToo"])
            return [Localizer getLocalizedText:@"Non - Contract Government"];
        
        if ([sectionInfo.name isEqualToString: @"LowestPublished"])
            return [Localizer getLocalizedText:@"Lowest Published"];
        
        if ([sectionInfo.name isEqualToString:@"None"])
            return [Localizer getLocalizedText:@"Other Rate Type"];
        else
            return @"";
    }

    if([sectionInfo.name intValue] == -1)
        return tableView.tableHeaderView ? [@"All Results" localize] : @"";
    if([sectionInfo.name intValue] == 0)
        return [@"Nonstop" localize];
    if([sectionInfo.name intValue] == 1)
        return [@"1 Stop" localize];
    else 
        return [NSString stringWithFormat:[@"N Stops" localize], [sectionInfo.name intValue]];
	
}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    [[AirFilterSummaryManager sharedInstance] deleteAll];
    [[AirFilterManager sharedInstance] deleteAll];
    
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:newIndexPath];
    EntityAirShopResults *entity = (EntityAirShopResults *)managedObject;
    
    AirShopFilterVC *vc = [[AirShopFilterVC alloc] initWithNibName:@"AirShopFilterVC" bundle:nil];
    vc.taFields = self.taFields;
    vc.airShopResults = entity;
    vc.airShop = self.airShop;
    [vc view];
    vc.lblHeading.text = self.lblHeading.text;
    vc.lblDates.text = self.lblDates.text;
    [self.navigationController pushViewController:vc animated:YES];

    NSString *airline = @"*";
    if(entity.airline != nil && ![entity.airline isEqualToString:@"ZZZZZZZZTOTAL"])
        airline = entity.airline;

    NSMutableDictionary *pBag = nil;
    if ([Config isGov])
    {
        NSString *rateType = @"";
        if (entity.rateType != nil)
            rateType = entity.rateType;
        
        pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: airline, @"AIRLINE", rateType, @"RATETYPE", self.airShop, @"AIRSHOP", nil];
    }
    else
    {
        NSString *numStops = @"0";
        if(entity.numStops != nil)
            numStops = [NSString stringWithFormat:@"%d", [entity.numStops intValue]];
        
        if([entity.airline isEqualToString:@"   TOTAL"])
            numStops = @"*";
        
        pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: airline, @"AIRLINE", numStops, @"NUMSTOPS", self.airShop, @"AIRSHOP", nil];
    }
	[[ExSystem sharedInstance].msgControl createMsg:AIR_FILTER CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:vc];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 65;	
}


#pragma mark - Fetched results controller
- (NSFetchedResultsController *)fetchedResultsController 
{
    if (__fetchedResultsController != nil) {
        return __fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityAirShopResults" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    NSSortDescriptor *sort;
    
    if ([Config isGov])
        sort = [[NSSortDescriptor alloc] initWithKey:@"rateType" ascending:YES];
    else
        sort = [[NSSortDescriptor alloc] initWithKey:@"numStops" ascending:YES];

    NSSortDescriptor *sort2 = [[NSSortDescriptor alloc] initWithKey:@"airline" ascending:YES];
    
    [fetchRequest setSortDescriptors:@[sort, sort2]];
    
    NSFetchedResultsController *theFetchedResultsController;
    if ([Config isGov])
    {
        theFetchedResultsController =
        [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                            managedObjectContext:self.managedObjectContext sectionNameKeyPath:@"rateType"
                                                       cacheName:nil];
    }
    else{
        theFetchedResultsController =
        [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                            managedObjectContext:self.managedObjectContext sectionNameKeyPath:@"numStops"
                                                       cacheName:nil];
    }

    self.fetchedResultsController = theFetchedResultsController;
    __fetchedResultsController.delegate = self;
    
    
    return __fetchedResultsController;
}


#pragma mark - Fetched results controller delegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    [self hideWaitView];
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

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type newIndexPath:(NSIndexPath *)newIndexPath
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
            [self configureCell:(AirShopSummaryCell*)[self.tableList cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
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
    dict[@"From Screen"] = @"Air Search Results";
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
}

                            
#pragma mark -
#pragma mark Last Entity
-(EntityAirCriteria *) loadEntity
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

#pragma NoDataMasterViewDelegate method
-(void) actionOnNoData:(id)sender
{
    [self.navigationController popViewControllerAnimated:YES];
}

- (NSString*) titleForNoDataView
{
    return [@"No Flights Found" localize];
}

@end
