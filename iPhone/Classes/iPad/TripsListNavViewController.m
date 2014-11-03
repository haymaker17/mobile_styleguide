//
//  TripsListNavViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TripsListNavViewController.h"
#import "TripsCell.h"

@implementation TripsListNavViewController
@synthesize tripsData, listKeys, tripTable, dvc;

-(NSString *) getViewIDKey
{
	return @"TRIPS_LIST_NAVI";
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
	self.contentSizeForViewInPopover = CGSizeMake(320.0, 500.0);
    [super viewDidLoad];
}



- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
	
//	if([ExSystem sharedInstance].sessionID != nil)
//	{
//		[self loadTrips];
//	}
}



- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
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






#pragma mark -
#pragma mark Mobile View Controller Methods
-(void)respondToFoundData:(Msg *)msg
{//respond to data that might be coming from the cache
	if ([msg.idKey isEqualToString:TRIPS_DATA])
	{//below is the pattern of getting the object you want and using it.
		self.tripsData = (TripsData *)msg.responder;
		self.listKeys = tripsData.keys;
		
		if([listKeys count] <= 0)
        {
			[self showNoDataView:self];
        }
		else
        {
            [tripTable reloadData];
        }
	}
}


#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
    return [self.listKeys count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    NSUInteger row = [indexPath row];
	static NSString *MyCellIdentifier = @"TripsCell";
	
	//	if (row > 0) 
	//	{
	TripsCell *cell = (TripsCell *)[tableView dequeueReusableCellWithIdentifier: MyCellIdentifier];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TripsCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[TripsCell class]])
				cell = (TripsCell *)oneObject;
	}
	
	[cell.activity stopAnimating];
	
	NSString *key = (self.listKeys)[row];
	EntityTrip *trip = [[TripManager sharedInstance] fetchByTripKey:key];//[tripsData.trips objectForKey:key];
	cell.label.text = trip.tripName; 
	cell.trip = trip;
	
	NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
	// specify timezone
    [dateFormat setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormat setLocale:[NSLocale currentLocale]];
	
	[dateFormat setDateFormat: @"EEE MMM dd"];
	
	NSString *startFormatted = [dateFormat stringFromDate:trip.tripStartDateLocal];
	
	NSString *endFormatted = [dateFormat stringFromDate:trip.tripEndDateLocal];
	
	
	cell.labelDateRange.text = [NSString stringWithFormat:@"%@ - %@", startFormatted, endFormatted];
	
	int x = 0;
	NSMutableString *recLocs;
	recLocs = nil;
	
	int iImagePos = 0;
	int startX = 280;
	int imageWidth = 19;
	int imgHW = 14;
	int y = 23;
	
	for (UIImageView *iView in [cell.contentView subviews]) 
	{
		if (iView.tag >= 900) 
		{
			[iView removeFromSuperview];
		}
		
	}
	
	if([trip.hasAir boolValue] == YES)
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		//[imagBack setMode:
		[imgBack setImage:[UIImage imageNamed:@"airfare_24X24_PNG.png"]];
		imgBack.tag = 900;
		[cell.contentView addSubview:imgBack];
		iImagePos++;
	}
	
	if([trip.hasHotel boolValue] == YES)
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"hotel_24X24_PNG.png"]];
		imgBack.tag = 901;
		[cell.contentView addSubview:imgBack];
		iImagePos++;
	}
	
	if([trip.hasParking boolValue] == YES)
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"parking_24X24_PNG.png"]];
		imgBack.tag = 902;
		[cell.contentView addSubview:imgBack];
		iImagePos++;
	}
	
	if([trip.hasEvent boolValue] == YES)
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		imgBack.tag = 903;
		[imgBack setImage:[UIImage imageNamed:@"24_event.png"]];
		[cell.contentView addSubview:imgBack];
		iImagePos++;
	}
	
	if([trip.hasDining boolValue] == YES)
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		imgBack.tag = 904;
		[imgBack setImage:[UIImage imageNamed:@"dining_24X24_PNG.png"]];
		[cell.contentView addSubview:imgBack];
		iImagePos++;
	}
	
	if([trip.hasRail boolValue] == YES)
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"rail_24X24_PNG.png"]];
		imgBack.tag = 905;
		[cell.contentView addSubview:imgBack];
		iImagePos++;
	}
	
	if([trip.hasRide boolValue] == YES)
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"taxi_24X24_PNG.png"]];
		imgBack.tag = 906;
		[cell.contentView addSubview:imgBack];
		iImagePos++;
	}
	
	if([trip.hasCar boolValue] == YES)
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"rental_car_24X24_PNG.png"]];
		[cell.contentView addSubview:imgBack];
		imgBack.tag = 907;
//		iImagePos++;
	}
	
	return cell;
	
}

#pragma mark -
#pragma mark Table Delegate Methods 
-(NSIndexPath *)tableView:(UITableView *)tableView 
 willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    return indexPath; 
}


- (void)tableView:(UITableView *)tableView 
accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath
{
	//	UIButton *infoButton = [UIButton buttonWithType:UIButtonTypeInfoLight];
	//	infoButton.tag = 600001;
	//	[rootViewController switchViews:infoButton ParameterBag:nil];
}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
	NSUInteger row = [newIndexPath row];
	NSString *key = (self.listKeys)[row];
	TripData *trip = (tripsData.trips)[key];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:trip, @"TRIP", nil];
	[[ConcurMobileAppDelegate findiPadHomeVC] switchToDetail:@"Trip" ParameterBag:pBag];
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 44;
}

#pragma mark -
#pragma mark iPad Stuff
-(void)loadTrips
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
	[[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
}

#pragma NoDataMasterViewDelegate method
-(void) actionOnNoData:(id)sender
{
    
}
@end

