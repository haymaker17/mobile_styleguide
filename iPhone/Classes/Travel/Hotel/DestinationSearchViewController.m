//
//  DestinationSearchViewController.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "DestinationSearchViewController.h"
#import "DestinationSearchDataSource.h"
#import "DestinationSearchCellData.h"
#import "LocationSearchCellData.h"
#import "OfficeLocationsViewController.h"
#import "AnalyticsTracker.h"

@interface DestinationSearchViewController ()

@property (nonatomic,strong) DestinationSearchDataSource *tableData;
@property (nonatomic, strong) NSTimer *timer;
@property (nonatomic) BOOL searchIsActive;

@end

@implementation DestinationSearchViewController

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
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    // We need the searchbar in the tableView to appear as it does in the insight demo
    self.tableView.tableHeaderView = self.searchDisplayController.searchBar;
    self.tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectZero];
    [self addCloseButtun];
    self.title = @"Destination";

    // MOB-20459 Colors on search bar
    [self changeSearchBarStyle];

    // initialize the datasource
    self.tableData = [[DestinationSearchDataSource alloc] init];
    self.tableData.delegate = self;
    self.searchDisplayController.searchBar.delegate = self;
    
    if (self.selectedLocation != nil)
    {
        self.searchDisplayController.searchBar.text = self.selectedLocation.location;
        if([self.selectedLocation.location isEqualToString:[@"Current Location" localize]])
        {
            self.tableData.showCurrentLocation = NO;
        }
        else if ([GlobalLocationManager sharedInstance].currentLocation != nil)
            self.tableData.showCurrentLocation = YES;
        // TODO : Check if location is current location.
    }
    else //if ([GlobalLocationManager sharedInstance].currentLocation != nil)
        self.tableData.showCurrentLocation = YES;
    
    self.searchDisplayController.searchBar.placeholder = @"enter location to search";
    
    [self.tableData loadContent];
    [AnalyticsTracker initializeScreenName:@"Search - Destination"];
}

/**
 *  Change the style of Search Bar.
 */
-(void) changeSearchBarStyle
{
    self.searchDisplayController.searchBar.backgroundColor = [UIColor concurBlueColor];
    [[UITextField appearanceWhenContainedIn:[UISearchBar class], nil] setTextColor:[UIColor colorWithRed:56/255.0 green:63/255.0 blue:70/255.0 alpha:1]];
    
    CGSize size = CGSizeMake(30, 30);
    UIGraphicsBeginImageContextWithOptions(size, NO, 5);
    [[UIBezierPath bezierPathWithRoundedRect:CGRectMake(0,0,30,30) cornerRadius:5.0] addClip];
    [[UIColor whiteColor] setFill];
    
    UIRectFill(CGRectMake(0, 0, size.width, size.height));
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    [self.searchDisplayController.searchBar setSearchFieldBackgroundImage:image forState:UIControlStateNormal];
    [self.searchDisplayController.searchBar setBackgroundImage:[self imageWithColor:[UIColor concurBlueColor]]];
    
    [[UIBarButtonItem appearanceWhenContainedIn: [UISearchBar class], nil] setTintColor:[UIColor colorWithRed:191/255.0 green:207/255.0 blue:235/255.0 alpha:1]];
    //
    // Set offset of the searchTex
    [self.searchDisplayController.searchBar setSearchTextPositionAdjustment:UIOffsetMake(6, 0)];
    
}

/**
 *  Draw a image with a specific color.
 */
- (UIImage *)imageWithColor:(UIColor *)color
{
    CGRect rect = CGRectMake(0.0f, 0.0f, 1.0f, 1.0f);
    UIGraphicsBeginImageContext(rect.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    CGContextSetFillColorWithColor(context, [color CGColor]);
    CGContextFillRect(context, rect);
    
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return image;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Search Bar Delegate

-(void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar
{
    // Clear text for new search
    if (self.searchIsActive == NO)
    {
        [searchBar setText:@""];
        self.searchIsActive = YES;
    }
    
    [self.tableData beginEditing];
}

-(void)searchBarTextDidEndEditing:(UISearchBar *)searchBar
{
    if(![searchBar.text lengthIgnoreWhitespace])
    {
        [searchBar setText:self.selectedLocation.location];
        if([searchBar.text isEqualToString:[@"Current Location" localize]])
        {
            self.tableData.showCurrentLocation = NO;
        }
        else{
            self.tableData.showCurrentLocation = YES;
        }
        self.searchIsActive = NO;
        [self.tableData endEditing];
    }
}

- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar
{
    if(self.selectedLocation != nil)
    {
        [searchBar setText:self.selectedLocation.location];
        if([searchBar.text isEqualToString:[@"Current Location" localize]])
        {
            self.tableData.showCurrentLocation = NO;
        }
        else{
            self.tableData.showCurrentLocation = YES;
        }
    }
    self.searchIsActive = NO;
    [searchBar becomeFirstResponder];
}
- (void)searchBarResultsListButtonClicked:(UISearchBar *)searchBar
{
    self.searchIsActive = NO;
}
- (void)searchBar:(UISearchBar *)searchBar selectedScopeButtonIndexDidChange:(NSInteger)selectedScope
{
    self.searchIsActive = NO;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return [self.tableData numberOfSections];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{

    // Return the number of rows in the section
    return [self.tableData.sections[section] count];

}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    AbstractTableViewCellData *cellData = [self.tableData itemAtIndexPath:indexPath];
    return cellData.cellHeight;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    // get the first section data
    id dataObject = [self.tableData itemAtIndexPath:indexPath];

    if ([dataObject isKindOfClass:[DestinationSearchCellData class]]) {
        DestinationSearchCellData *cellData = (DestinationSearchCellData *)dataObject;
        UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:cellData.cellIdentifier];
        //        [cell setCellData:cellData];
        cell.textLabel.text = cellData.title;
        cell.textLabel.font = [UIFont fontWithName:@"HelveticaNeue" size:17.0f];
        cell.textLabel.textColor = [UIColor concurBlueColor];
        cell.imageView.image = [UIImage imageNamed:@"hotel_office_location"];
        return cell;
    }
    else if ([dataObject isKindOfClass:[LocationSearchCellData class]]) {
        LocationSearchCellData *cellData = (LocationSearchCellData *)dataObject;
        UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:cellData.cellIdentifier];
        //        [cell setCellData:cellData];
        CTELocation *location = [cellData getCTELocation];
        cell.textLabel.text = location.location;
        cell.textLabel.font = [UIFont fontWithName:@"HelveticaNeue" size:17.0f];
        if ([cellData isCurrentLocation])
        {
            cell.textLabel.textColor = [UIColor concurBlueColor];
            cell.imageView.image = [UIImage imageNamed:@"hotel_office_target"];
            cell.textLabel.textAlignment = NSTextAlignmentLeft;
        }
        else
        {
            cell.textLabel.textColor = [UIColor grayColor];
        }

        return cell;
    }
    return nil;

    
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
     if (self.onLocationSelected) {
         id dataObject = [self.tableData itemAtIndexPath:indexPath];

         if ([dataObject isKindOfClass:[DestinationSearchCellData class]]) {
             // There is only 1 DestinationSearchCell in this list
            [self performSegueWithIdentifier:@"officeLocations" sender:self];
             
         }
         else if ([dataObject isKindOfClass:[LocationSearchCellData class]]) {
            LocationSearchCellData *cellData = (LocationSearchCellData *)dataObject;
             // Send back to the search criteria view and set the search destination to selected value.
             
             // TODO: Check if current Location is selected or not
             
            [self.navigationController dismissViewControllerAnimated:YES completion:^{
                self.onLocationSelected([cellData getCTELocation]);
                NSString *city = [cellData getCTELocation].city == nil ? [cellData getCTELocation].location : [cellData getCTELocation].city;
                [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Search Destination" eventLabel:city eventValue:nil];
            }];
         }
    }
    [self.tableView deselectRowAtIndexPath:indexPath animated:YES];

}

/*!
 Adds a close button to the navbar when Search criteria section is expanded.
 */
-(void)addCloseButtun
{
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithTitle:[@"Cancel" localize]
                                                                   style:UIBarButtonItemStyleDone
                                                                  target:self
                                                                  action:@selector(dismissSearchView)];
    [self.navigationItem setLeftBarButtonItem:doneButton];
    
}

-(void)dismissSearchView
{
    [self.navigationController dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - UISearchBar

-(BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString
{
    // This timer is used to initiate a search only when the user pauses during typing, thereby reducing number of hits on the locationSearch endpoint
    [self.timer invalidate];
    self.timer = [NSTimer scheduledTimerWithTimeInterval:.9 target:self selector:@selector(initiateLocationSearch) userInfo:searchString repeats:NO];
    
    // Dont reload the table yet. Load the table only when there are search results.
    return NO;
}

-(void) initiateLocationSearch
{
    NSString *searchString = [self.timer userInfo];
    
    if ([searchString lengthIgnoreWhitespace] > 2) {
        // TODO: Show some animations that we are searching for something
        [self.tableData searchLocation:searchString];
    }
}


#pragma mark - AbstractDataSourceDelegates

-(void)dataSourceWillChangeData:(AbstractDataSource *)dataSource
{
    [self.searchDisplayController.searchResultsTableView beginUpdates];
}

-(void)dataSource:(AbstractDataSource *)dataSource didChangeSection:(id<AbstractDataSourceSectionInfo>)sectionInfo atIndex:(NSUInteger)sectionIndex forChangeType:(DataSourceChangeType)type
{
    
    switch(type)
    {
        case kDataSourceChangeInsert:
            [self.searchDisplayController.searchResultsTableView insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case kDataSourceChangeDelete:
            [self.searchDisplayController.searchResultsTableView deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
    
}

- (void)dataSource:(AbstractDataSource *)dataSource didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(DataSourceChangeType)type newIndexPath:(NSIndexPath *)newIndexPath
{
    switch(type)
    {
            
        case kDataSourceChangeInsert:
            [self.searchDisplayController.searchResultsTableView insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case kDataSourceChangeDelete:
            [self.searchDisplayController.searchResultsTableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case kDataSourceChangeUpdate:
            [self.searchDisplayController.searchResultsTableView cellForRowAtIndexPath:indexPath] ;
            break;
            
        case NSFetchedResultsChangeMove:
            [self.searchDisplayController.searchResultsTableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [self.searchDisplayController.searchResultsTableView insertRowsAtIndexPaths:@[newIndexPath]withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
    
}

- (void)dataSourceDidChangeContent:(AbstractDataSource *)dataSource
{
    [self.searchDisplayController.searchResultsTableView endUpdates];

}

#pragma mark - memory management

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    self.tableData.delegate = nil;
	self.tableData = nil;
}


- (void)dealloc
{
	self.tableData.delegate = nil;
    self.tableData = nil;
}

#pragma mark - segue methods

-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // This ensures that after transition the back button will appear as a < with no viewname
    self.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"" style:UIBarButtonItemStylePlain target:nil action:nil];
    
    if ([segue.identifier isEqualToString:@"officeLocations"]) {
        // Set the call back block
        OfficeLocationsViewController *officeLocations = (OfficeLocationsViewController *)segue.destinationViewController;
        officeLocations.onLocationSelected = self.onLocationSelected;
        [officeLocations setCurrentLocation:self.selectedLocation];
        
        [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Office Location" eventLabel:nil eventValue:nil];
    }
}

@end
