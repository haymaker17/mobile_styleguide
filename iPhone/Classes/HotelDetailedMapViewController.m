//
//  HotelDetailedMapViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelDetailedMapViewController.h"
#import "HotelResult.h"
#import "ExSystem.h" 

#import "RoomListSummaryCell.h"
#import "HotelAnnotation.h"
#import "AsyncImageView.h"


@implementation HotelDetailedMapViewController


@synthesize mapView, bbiDone, tBar;
@synthesize hotelResult;


#define kSectionHotelSummary 0


#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return HOTEL_DETAILED_MAP;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_MODAL;
}


-(void)respondToFoundData:(Msg *)msg
{
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
	
	[self updateMap];

    // Uncomment the following line to preserve selection between presentations.
    //self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
	
	if([UIDevice isPad])
	{
//		tBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
//		addressOnlySearchBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
//		addressAndOfficeSearchBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
		self.title = hotelResult.hotel;
	}
	
	self.bbiDone = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Done"] style:UIBarButtonItemStyleBordered target:self action:@selector(btnDone:)];
	NSArray *tbItems = @[bbiDone];
	[self.tBar setItems:tbItems];
    
    self.tBar.tintColor = [UIColor darkBlueConcur_iOS6];
    self.tBar.alpha = 0.9f;
}

/*
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}
*/
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


#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 1;
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
//	NSUInteger row = [indexPath row];
	
    if (kSectionHotelSummary == section)
	{
		RoomListSummaryCell *cell = [RoomListSummaryCell makeAndConfigureCellForTableView:tableView owner:self hotel:hotelResult showAddressLink:NO];
		return cell;
	}
	
	return nil;
}


/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/


/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:YES];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/


/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
}
*/


/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    // Navigation logic may go here. Create and push another view controller.
	/*
	 <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:@"<#Nib name#>" bundle:nil];
     // ...
     // Pass the selected object to the new view controller.
	 [self.navigationController pushViewController:detailViewController animated:YES];
	 [detailViewController release];
	 */
}


#pragma mark -
#pragma mark HotelSummaryDelegate

-(void)addressPressed:(id)sender
{
}

-(void)phonePressed:(id)sender
{
	RoomListSummaryCell *cell = (RoomListSummaryCell*)sender;
	NSString *phoneNumber = cell.phone.text;
	
	NSString *digitsOnlyPhoneNumber = [[phoneNumber componentsSeparatedByCharactersInSet:[[NSCharacterSet characterSetWithCharactersInString:@"0123456789"] invertedSet]] componentsJoinedByString:@""];
	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", digitsOnlyPhoneNumber]]];
}


#pragma mark -
#pragma mark Map methods
-(void)updateMap
{
	// Remove all existing annotations
	[mapView removeAnnotations:[mapView annotations]];
	
	// Return if there's nothing to show
	if (hotelResult == nil)
		return;
	
	// The center location is the location around which the map will be centered.
	CLLocationCoordinate2D centerLocation;
	centerLocation.latitude = [hotelResult.lat doubleValue];
	centerLocation.longitude = [hotelResult.lng doubleValue];
	
	// The span is as big as it needs to be to show all the hotel
	MKCoordinateSpan span;
	span.latitudeDelta = 0.005;
	span.longitudeDelta = 0.005;
	
	// Set the map region
	MKCoordinateRegion region;
	region.center = centerLocation;
	region.span = span;
	
	[mapView setRegion:region animated:YES];
	[mapView regionThatFits:region];
	
	// Add the annotation (push pins)
	CLLocationCoordinate2D hotelCoordinate;
	hotelCoordinate.latitude = [hotelResult.lat doubleValue];
	hotelCoordinate.longitude = [hotelResult.lng doubleValue];
	
	HotelAnnotation *hotelAnnotation = [HotelAnnotation alloc];
	hotelAnnotation.coordinate = hotelCoordinate;
	hotelAnnotation.title = hotelResult.hotel;
	hotelAnnotation.subtitle = hotelResult.addr1;
	
	[mapView addAnnotation:hotelAnnotation];
	
}


#pragma mark -
#pragma mark Buttons

-(IBAction) btnDone:(id)sender
{
    if([UIDevice isPad])
        [self dismissViewControllerAnimated:YES completion:nil];
    else
        [self dismissViewControllerAnimated:YES completion:nil];	
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
	self.bbiDone = nil;
	self.mapView = nil;
	self.tBar = nil;
}

- (void)dealloc
{
	mapView.delegate = nil;
}


@end

