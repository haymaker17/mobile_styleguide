//
//  GovAirShopResultsVC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 2/19/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "GovAirShopResultsVC.h"
#import "AirFilterSummaryManager.h"
#import "AirFilterManager.h"
#import "AirShopFilterVC.h"
#import "GovAirShopSectionHeader.h"
#import "GovAirShopAllResultsCell.h"
#import "GovAirShopSummaryCell.h"
#import "Config.h"
#import "EntityAirShopResults.h"

@interface GovAirShopResultsVC ()

@property (strong, nonatomic) IBOutlet NSLayoutConstraint *coItinHeaderHeight;
@property (strong, nonatomic) IBOutlet NSLayoutConstraint *coTableListHeight;

@end

@implementation GovAirShopResultsVC

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [[AirFilterSummaryManager sharedInstance] deleteAll];
    [[AirFilterManager sharedInstance] deleteAll];
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    self.managedObjectContext = [ad managedObjectContext];
    [self refetchData];
    
    EntityAirCriteria *airCrit = [self loadEntity];
    if(self.airShop.isRoundTrip)
    {
        NSString *departureCity = (self.airShop.airportCityCodes)[airCrit.DepartureAirportCode];
        NSString *arrivalCity = (self.airShop.airportCityCodes)[airCrit.ReturnAirportCode];
        if(departureCity == nil)
            departureCity = @"";
        if(arrivalCity == nil)
            arrivalCity = @"";
        
        self.lblHeading.text = [NSString stringWithFormat:@"(%@) %@ to (%@) %@", airCrit.DepartureAirportCode, departureCity, airCrit.ReturnAirportCode, arrivalCity];
        self.lblDates.text = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateMediumByDate:airCrit.DepartureDate], [DateTimeFormatter formatDateMediumByDate:airCrit.ReturnDate]];
    }
    else
    {
        self.lblHeading.text = [NSString stringWithFormat:@"(%@) %@ to (%@) %@", airCrit.DepartureAirportCode, (self.airShop.airportCityCodes)[airCrit.DepartureAirportCode] , airCrit.ReturnAirportCode, (self.airShop.airportCityCodes)[airCrit.ReturnAirportCode]];
        self.lblDates.text = [NSString stringWithFormat:@"%@", [DateTimeFormatter formatDateMediumByDate:airCrit.DepartureDate]];
    }

    // Need to check if there is a benchmark to display or not
    if (self.airShop.benchmark != nil && self.airShop.benchmark.price != nil)
    {
        NSString *crnCode = self.airShop.benchmark.crnCode;
        if(![crnCode length])
            crnCode = @"USD";
        self.lblBenchmark.text = [NSString stringWithFormat:@"%@: %@", [@"Benchmark" localize], [FormatUtils formatMoney:[self.airShop.benchmark.price stringValue] crnCode:crnCode]];
        if (self.lblBenchmark.hidden)
        {
            // Only make adjustments if the we have a benchmark to show and the label is currently hidden
            // Just incase we are reusing old object references
            // I'm being very defensive here
            self.coItinHeaderHeight.constant += 14;
            self.coTableListHeight.constant -= 14;
            [self.lblBenchmark setHidden:NO];
        }
    }
    else
    {
        if (!self.lblBenchmark.hidden)
        {
            // Only make adjustments if we have no benchmark and the label is visible
            // Just incase we are reusing old object references
            // I'm being very defensive here
            self.coItinHeaderHeight.constant -= 14;
            self.coTableListHeight.constant += 14;
            [self.lblBenchmark setHidden:YES];
        }
    }
    
    self.title = [@"Results Summary" localize];
    
    if([self.airShop.stopChoices count] < 1 && [self.airShop.rateTypeChoices count] <1)
        [self showNoDataView:self asSubviewOfView:self.tableList];
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setToolbarHidden:YES];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(NSString *)getViewIDKey
{
    return @"AIRSHOPSUMMARY";
}

#pragma mark -
#pragma mark UITableViewDataSource Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [[self.fetchedResultsController sections] count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
    return [sectionInfo numberOfObjects];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    if (section == 0) {
        return 0;
    }
    else
        return 55;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0 && indexPath.row == 0) {
        return 35;
    }
    else
        return 65;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0 && indexPath.section == 0)
    {
        NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
        EntityAirShopResults *entity = (EntityAirShopResults *)managedObject;
        
        GovAirShopAllResultsCell *cell = [tableView dequeueReusableCellWithIdentifier:@"GovAirShopAllResultsCell"];
        
        NSString *crnCode = entity.crnCode;
        if(![crnCode length])
            crnCode = @"USD";
        NSString *cellLabelValue = [NSString stringWithFormat:[Localizer getLocalizedText:@"SEE ALL token RESULTS (starting at token)"], [entity.numChoices intValue], [FormatUtils formatMoney:[entity.lowestCost stringValue] crnCode:crnCode]];

        cell.lblAllResults.text = cellLabelValue;
        return cell;
    }
    else
    {
        GovAirShopSummaryCell *cell = (GovAirShopSummaryCell *)[tableView dequeueReusableCellWithIdentifier: @"GovAirShopSummaryCell"];
        [self configureCell:cell atIndexPath:indexPath];
        return cell;
    }
}

#pragma mark - Cell Config
- (void)configureCell:(GovAirShopSummaryCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityAirShopResults *entity = (EntityAirShopResults *)managedObject;
    
    UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:entity.airline VendorType:@"a_small" RespondToIV:cell.ivLogo];
    if(gotImg != nil)
        cell.ivLogo.image = gotImg;
    
    NSString *prefRanking = (self.airShop.prefRankings)[entity.airline];
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
    
    cell.lblAirline.text = entity.airlineName;
    
    NSString *crnCode = entity.crnCode;
    if(![crnCode length])
        crnCode = @"USD";
    cell.lblCost.text = [FormatUtils formatMoney:[entity.lowestCost stringValue] crnCode:crnCode];
    [cell.lblCost setTextColor:[UIColor bookingBlueColor]];
    cell.lblResultCount.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"token results"],[entity.numChoices intValue]];
    cell.lblStarting.text = [@"Starting" localize];
}

#pragma mark -
#pragma mark UITableViewDelegate Methods
-(UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    if ([Config isGov] && section != 0)
    {
        // Set up label
        GovAirShopSectionHeader *headerView = (GovAirShopSectionHeader*)[[NSBundle mainBundle] loadNibNamed:@"GovAirShopSectionHeader" owner:self options:nil][0];
        id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
        EntityAirShopResults *entity = [self loadEntityAirShopResults:@"ZZZZZZZZTOTAL" withRateType:sectionInfo.name];
        NSString *resultChoiceCount = @"";
        
        if ([sectionInfo.name isEqualToString: @"LimitedCapacity"])
        {
            headerView.lblResultsType.text = [Localizer getLocalizedText:@"Govt. Contract Discounted"];
        }
        else if ([sectionInfo.name isEqualToString: @"Contract"])
        {
            headerView.lblResultsType.text =  [Localizer getLocalizedText:@"Govt. Contract"];
        }
        else if ([sectionInfo.name isEqualToString: @"ContractBusiness"])
        {
            headerView.lblResultsType.text =  [Localizer getLocalizedText:@"Govt. Contract"];
        }
        else if ([sectionInfo.name isEqualToString: @"MeToo"])
        {
            headerView.lblResultsType.text =  [Localizer getLocalizedText:@"Non - Contract Government"];
        }
        else if ([sectionInfo.name isEqualToString: @"LowestPublished"])
        {
            headerView.lblResultsType.text =  [Localizer getLocalizedText:@"Lowest Published"];
        }
        else if ([sectionInfo.name isEqualToString:@"None"])
        {
            headerView.lblResultsType.text =  [Localizer getLocalizedText:@"Other Rate Type"];
        }
        else
        {
            headerView.lblResultsType.text =  @"";
        }
        
        if ([ExSystem is7Plus])
        {
            // We make this call here for iOS7, as iOS6 seems to ignore this
            [headerView.lblResultsType setVerticalAlignment:VerticalAlignmentBottom];
        }
        
        resultChoiceCount = [NSString stringWithFormat:@"%@", [entity.numChoices stringValue]];
        [headerView.btnAllResults setTitle:[NSString stringWithFormat:[Localizer getLocalizedText:@"SEE ALL token RESULTS"],resultChoiceCount] forState:UIControlStateNormal];
        [headerView.btnAllResults setAction:kUIButtonBlockTouchUpInside withBlock:^{
            [self buttonSectionHeaderClicked:entity];
        }];
        
        return headerView;
    }
    else
        return nil;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    [[AirFilterSummaryManager sharedInstance] deleteAll];
    [[AirFilterManager sharedInstance] deleteAll];
    
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:newIndexPath];
    EntityAirShopResults *entity = (EntityAirShopResults *)managedObject;
    
    [self buttonSectionHeaderClicked:entity];
    
    [tableView deselectRowAtIndexPath:newIndexPath animated:YES];
}


#pragma mark - Fetched results controller
- (NSFetchedResultsController *)fetchedResultsController
{
    if (_fetchedResultsController != nil) {
        return _fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityAirShopResults" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    // Set up predicate to ignore "All 'token' results" row in flight table
    NSString *excludeValue = @"ZZZZZZZZTOTAL";
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"NOT ((airline LIKE %@) && airlineName LIKE %@)", excludeValue, excludeValue];
    [fetchRequest setPredicate:predicate];
    
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
    _fetchedResultsController.delegate = self;
    
    return _fetchedResultsController;
}


#pragma mark - Fetched results controller delegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    [self hideWaitView];
    [self.tableList beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
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
            [self configureCell:(GovAirShopSummaryCell*)[self.tableList cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
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

#pragma mark - Section header button handler
- (void)buttonSectionHeaderClicked:(EntityAirShopResults *)entity
{
    [[AirFilterSummaryManager sharedInstance] deleteAll];
    [[AirFilterManager sharedInstance] deleteAll];

    AirShopFilterVC *vc = [[AirShopFilterVC alloc] initWithNibName:@"AirShopFilterVC" bundle:nil];
    vc.taFields = self.taFields;
    vc.airShopResults = entity;
    vc.airShop = self.airShop;
    [vc view];
    vc.lblHeading.text = self.lblHeading.text;
    vc.lblDates.text = self.lblDates.text;
    vc.lblBenchmark.text = self.lblBenchmark.text;
    [vc.lblBenchmark setHidden:self.lblBenchmark.hidden];
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


#pragma mark -
#pragma mark Load Entity
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

-(EntityAirShopResults *) loadEntityAirShopResults:(NSString * )airline withRateType:(NSString *)rateType
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityAirShopResults" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];

    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"((airline LIKE %@) && rateType LIKE %@)", airline, rateType];
    [fetchRequest setPredicate:predicate];
    
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
