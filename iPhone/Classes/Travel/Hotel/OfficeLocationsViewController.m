//
//  OfficeLocationsViewController.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 08/08/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "OfficeLocationsViewController.h"
#import "OfficeLocationsDataSource.h"
#import "LocationSearchCellData.h"
#import "SystemConfig.h"

@interface OfficeLocationsViewController ()

@property (nonatomic,strong) OfficeLocationsDataSource *tableData;

@end

@implementation OfficeLocationsViewController

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
    self.searchDisplayController.searchBar.placeholder = [Localizer getLocalizedText:@"Search for an office location"];
    self.title = [Localizer getLocalizedText:@"Office Locations"];
    
    [self changeSearchBarStyle];
    
    // initialize the datasource
    SystemConfig *systemConfig = [SystemConfig getSingleton];
    self.tableData = [[OfficeLocationsDataSource alloc] initWithOfficeLocationArray:systemConfig.officeLocations];

    self.tableData.delegate = self;
    [self.tableData loadContent];
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


-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
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
    
    LocationSearchCellData *cellData = (LocationSearchCellData *)dataObject;
    UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:cellData.cellIdentifier];
    //        [cell setCellData:cellData];
    CTELocation *location = [cellData getCTELocation];
    cell.textLabel.text = location.location;
    cell.textLabel.numberOfLines = 2;
        
    return cell;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (self.onLocationSelected) {
        id dataObject = [self.tableData itemAtIndexPath:indexPath];
        
        LocationSearchCellData *cellData = (LocationSearchCellData *)dataObject;
        // Send back to the search criteria view and set the search destination to selected value.
        
        [self.navigationController dismissViewControllerAnimated:YES completion:^{
            self.onLocationSelected([cellData getCTELocation]);
        }];
    }
}

-(void)dismissSearchView
{
    [self.navigationController dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - UISearchBar

-(BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString
{
    // Dont reload the table yet. Load the table only when there are search results.
    // TODO: Show some animations that we are searching for something
    [self.tableData searchLocation:searchString];
    
    return NO;
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

@end
