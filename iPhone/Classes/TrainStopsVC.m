//
//  TrainStopsVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainStopsVC.h"
#import "TrainStationData.h"

@implementation TrainStopsVC
@synthesize aStops, tableList, selectedRow;
@synthesize  rcd;
@synthesize  segmentDepart, segmentArrive;
@synthesize  train, lblLegs, lblTrain, lblStation;

/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	[self initList];
	[tableList reloadData];
}




- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}



-(void) initList
{
	aStops = [[NSMutableArray alloc] initWithObjects:nil];
	
	TrainStationData *tsd = [[TrainStationData alloc] init];
	tsd.stationCode = @"SEA";
	tsd.stationName = @"Seattle, King Street Station";
	tsd.stationAddress = @"303 South Jackson Street";
	tsd.stationZip = @"98104";
	tsd.stationPhone = @" (206) 382-4125";
	tsd.city = @"Seattle";
	tsd.stationState = @"WA";
	tsd.waitTime = 0;
	tsd.countryCode = @"USA";
	tsd.iataCode = @"SEA";
	[aStops addObject:tsd];
	
	tsd = [[TrainStationData alloc] init];
	tsd.stationCode = @"TAC";
	tsd.iataCode = @"TAC";
	tsd.stationName = @"Tacoma";
	tsd.stationAddress = @"Tacoma Amtrak";
	tsd.stationPhone = @"";
	tsd.stationZip = @"98421";
	tsd.city = @"Tacoma";
	tsd.stationState = @"WA";
	tsd.waitTime = 10;
	tsd.countryCode = @"USA";
	[aStops addObject:tsd];
	
	tsd = [[TrainStationData alloc] init];
	tsd.stationCode = @"OLW";
	tsd.iataCode = @"OLW";
	tsd.stationName = @"Olympia-Lacey Centennial Station";
	tsd.stationAddress = @"6600 Yelm Highway SE";
	tsd.stationPhone = @"";
	tsd.stationZip = @"98513";
	tsd.city = @"Lacey";
	tsd.stationState = @"WA";
	tsd.waitTime = 10;
	tsd.countryCode = @"USA";
	[aStops addObject:tsd];
	
	tsd = [[TrainStationData alloc] init];
	tsd.stationCode = @"CTL";
	tsd.iataCode = @"CTL";
	tsd.stationName = @"Centralia Union Depot";
	tsd.stationAddress = @"210 Railroad Avenue";
	tsd.stationPhone = @"";
	tsd.stationZip = @"98531";
	tsd.city = @"Centralia";
	tsd.stationState = @"WA";
	tsd.waitTime = 5;
	tsd.countryCode = @"USA";
	[aStops addObject:tsd];
	
	tsd = [[TrainStationData alloc] init];
	tsd.stationCode = @"KEL";
	tsd.iataCode = @"KEL";
	tsd.stationName = @"Kelso Multimodal Transportation Center";
	tsd.stationAddress = @"501 South First Avenue";
	tsd.stationPhone = @"";
	tsd.stationZip = @"98626";
	tsd.city = @"Kelso";
	tsd.stationState = @"WA";
	tsd.waitTime = 5;
	tsd.countryCode = @"USA";
	[aStops addObject:tsd];
	
	tsd = [[TrainStationData alloc] init];
	tsd.stationCode = @"VAN";
	tsd.iataCode = @"VAN";
	tsd.stationName = @"Vancouver Station";
	tsd.stationAddress = @"1301 West 11th Street";
	tsd.stationPhone = @"";
	tsd.stationZip = @"98660";
	tsd.city = @"Vancouver";
	tsd.stationState = @"WA";
	tsd.waitTime = 10;
	tsd.countryCode = @"USA";
	[aStops addObject:tsd];
	
	tsd = [[TrainStationData alloc] init];
	tsd.stationCode = @"PDX";
	tsd.iataCode = @"PDX";
	tsd.stationName = @"Union Station (Portland)";
	tsd.stationAddress = @"800 NW Sixth Ave";
	tsd.stationPhone = @"";
	tsd.stationZip = @"97209";
	tsd.city = @"Portland";
	tsd.stationState = @"OR";
	tsd.waitTime = 0;
	tsd.countryCode = @"USA";
	[aStops addObject:tsd];
	
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return [aStops count];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	//NSUInteger section = [indexPath section];
	
	TrainStopCell *cell = (TrainStopCell*)[tableView dequeueReusableCellWithIdentifier:@"TrainStopCell"];
	if (cell == nil)
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TrainStopCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[TrainStopCell class]])
				cell = (TrainStopCell *)oneObject;
	}
	
	if(row == 0)
		cell.iv.image = [UIImage imageNamed:@"TrainStop_start.png"];
	else if(row == ([aStops count] - 1))
		cell.iv.image = [UIImage imageNamed:@"TrainStop_end.png"];
	else 
		cell.iv.image = [UIImage imageNamed:@"TrainStop.png"];
	
	if(selectedRow == row && row == 0)
		cell.iv.image = [UIImage imageNamed:@"TrainStop_start_selected.png"];
	else if(selectedRow == row && row == ([aStops count] - 1))
		cell.iv.image = [UIImage imageNamed:@"TrainStop_end_selected.png"];
	else if (selectedRow == row)
		cell.iv.image = [UIImage imageNamed:@"TrainStop_selected.png"];
	
	TrainStationData *tsd = aStops[row];
	cell.lblStop.text = [NSString stringWithFormat:@"(%@) %@", tsd.stationCode, tsd.stationName];
//	cell.lbl1.text = [NSString stringWithFormat:@"%@", tsd.stationAddress];
//	cell.lbl2.text = [NSString stringWithFormat:@"%@, %@ %@", tsd.city, tsd.stationState, tsd.stationZip];
	
//	if(selectedRow == row])
//		[cell setAccessoryType:UITableViewCellAccessoryDetailDisclosureButton];
//	else {
//		[cell setAccessoryType:UITableViewCellAccessoryNone];
//	}
	[cell setAccessoryType:UITableViewCellAccessoryDetailDisclosureButton];
	
	return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	int lastSelectedRow = selectedRow;
	selectedRow = indexPath.row;
	
	NSUInteger _path[2] = {indexPath.section, lastSelectedRow};
	NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
	NSArray *_indexPaths = @[_indexPath];
	[tableView reloadRowsAtIndexPaths:_indexPaths withRowAnimation:NO];
	
	NSUInteger path[2] = {indexPath.section, indexPath.row};
	_indexPath = [[NSIndexPath alloc] initWithIndexes:path length:2];
	_indexPaths = @[_indexPath];
	[tableView selectRowAtIndexPath:_indexPath animated:YES scrollPosition: UITableViewScrollPositionNone];
	[tableView reloadRowsAtIndexPaths:_indexPaths withRowAnimation:YES];
	
	TrainStationData *tsd = aStops[selectedRow];
	NSString *place = [NSString stringWithFormat:@"%@ %@ %@ %@", tsd.stationAddress, tsd.city, tsd.stationState, tsd.stationZip];
	[self goSomeplace:place VendorName:tsd.stationName VendorCode:tsd.stationCode];
	
}


-(IBAction)cancelView:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];	
}

-(void)goSomeplace:(NSString *)mapAddress VendorName:(NSString *)vendorName VendorCode:(NSString *)vendorCode
{
	MapViewController *mapView = [[MapViewController alloc] init];
	mapView.lblAddress.text = mapAddress;
	mapView.mapAddress = mapAddress;
	mapView.anoTitle = vendorName;
	mapView.anoSubTitle = mapAddress;
	[self presentViewController:mapView animated:YES completion:nil]; 
	
}

@end
