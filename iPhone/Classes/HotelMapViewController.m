//
//  HotelMapViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelMapViewController.h"
#import "HotelSearchResultsViewController.h"
#import "HotelResult.h"
#import "LocationResult.h"
#import "HotelAnnotation.h"
#import "HotelAnnotationView.h"
#import "HotelListCell.h"
#import "HotelSearch.h"
#import "ExSystem.h" 

#import "HotelDescriptor.h"
#import "EntityHotelImage.h"

#define SORT_BY_PREFERRED_VENDORS	0
#define SORT_BY_VENDOR_NAMES		1
#define SORT_BY_PRICE				2
#define SORT_BY_DISTANCE			3
#define SORT_BY_RATING				4

@implementation HotelMapViewController


@synthesize tblView;
@synthesize mapView;


#pragma mark -
#pragma mark Notifications

-(void)notifyChange
{
	[super notifyChange];

	[self updateMap];
}


#pragma mark -
#pragma mark Map methods
-(void)updateMap
{
	// Remove all existing annotations
	[mapView removeAnnotations:[mapView annotations]];

//	// Return if there's nothing to show
//	if (parentMVC.hotelSearch.hotels == nil || [parentMVC.hotelSearch.hotels count] == 0)
//		return;
    
    NSArray *aHotel = [[HotelBookingManager sharedInstance] fetchedAllSorted:SORT_BY_PREFERRED_VENDORS];
    
    if(aHotel == nil || [aHotel count] == 0)
        return;
	
    if(parentMVC.selectedHotelIndex < 0)
        parentMVC.selectedHotelIndex = 0;
	// Find the biggest and smallest hotel latitudes and longitudes
//    NSArray *aHotelMinMax = [[HotelBookingManager sharedInstance] fetchedAllSorted:SORT_BY_DISTANCE];
	EntityHotelBooking *firstHotel = aHotel[0];
    //EntityHotelBooking *lastHotel = [aHotelMinMax objectAtIndex:[aHotelMinMax count] -1];
	double smallestLatitude = [firstHotel.lat doubleValue];
	double biggestLatitude = [firstHotel.lat doubleValue];
	double smallestLongitude = [firstHotel.lng doubleValue];
	double biggestLongitude = [firstHotel.lng doubleValue];
	for (EntityHotelBooking *hotelResult in aHotel)
	{
		double hotelLatitude = [hotelResult.lat doubleValue];
		double hotelLongitude = [hotelResult.lng doubleValue];

		if (hotelLatitude < smallestLatitude)
			smallestLatitude = hotelLatitude;
		if (hotelLatitude > biggestLatitude)
			biggestLatitude = hotelLatitude;
		if (hotelLongitude < smallestLongitude)
			smallestLongitude = hotelLongitude;
		if (hotelLongitude > biggestLongitude)
			biggestLongitude = hotelLongitude;
	}
	
	double boxWidth = (biggestLatitude - smallestLatitude);
	double boxHeight = (biggestLongitude - smallestLongitude);
	
	if (boxWidth < 0)
		boxWidth = 0 - boxWidth;
	if (boxHeight < 0)
		boxHeight = 0 - boxHeight;
	
	if (boxWidth == 0)
		boxWidth = 0.005;
	if (boxHeight == 0)
		boxHeight = 0.005;
	
	// The center location is the location around which the map will be centered.
	CLLocationCoordinate2D centerLocation;
	centerLocation.latitude = smallestLatitude + (boxWidth / 2);
	centerLocation.longitude = smallestLongitude + (boxHeight / 2);
	
	// The span is as big as it needs to be to show all the hotel
	MKCoordinateSpan span;
	span.latitudeDelta = boxWidth;
	span.longitudeDelta = boxHeight;
	
	// Set the map region
	MKCoordinateRegion region;
	region.center = centerLocation;
	region.span = span;
	
	[mapView setRegion:region animated:YES];
	[mapView regionThatFits:region];
	
	// Add the annotations (push pins)
	NSUInteger hotelCount = [aHotel count];
	for (int hotelIndex = 0; hotelIndex < hotelCount; hotelIndex++)
	{
		EntityHotelBooking* hotelResult = (EntityHotelBooking*)aHotel[hotelIndex];
		CLLocationCoordinate2D hotelCoordinate;
		hotelCoordinate.latitude = [hotelResult.lat doubleValue];
		hotelCoordinate.longitude = [hotelResult.lng doubleValue];
		
		HotelAnnotation *hotelAnnotation = [HotelAnnotation alloc];
		hotelAnnotation.coordinate = hotelCoordinate;
		hotelAnnotation.title = hotelResult.hotel;
		hotelAnnotation.subtitle = hotelResult.addr1;
		hotelAnnotation.hotelIndex = hotelIndex;
		
		[mapView addAnnotation:hotelAnnotation];
		
	}

	// Show the selected hotel after a very brief delay.  Note: attempting to do it
	// immediately will fail because the annotation views are not available yet.
	[self performSelector:@selector(showSelectedHotel) withObject:nil afterDelay:0.5f];
}

-(void)annotationSelected:(HotelAnnotationView*)annotation
{
    parentMVC.selectedHotelIndex = (int)annotation.hotelIndex;
//	[parentMVC.hotelSearch selectHotel:annotation.hotelIndex];
	[self showSelectedHotel];
}

-(void)showSelectedHotel
{
	int selectionIndex = -1;
    
	if (parentMVC.selectedHotelIndex != -1)
		selectionIndex = parentMVC.selectedHotelIndex;
	
	// Turn selected hotel's pin green and all others red
	for (HotelAnnotation *ann in mapView.annotations)
	{
		HotelAnnotationView *annView = (HotelAnnotationView*)[mapView viewForAnnotation:ann];
		if (annView != nil)
		{
			if (ann.hotelIndex == selectionIndex)
				annView.pinColor = MKPinAnnotationColorGreen;
			else
				annView.pinColor = MKPinAnnotationColorRed;
		}
	}
	
	[tblView reloadData];
}


#pragma mark -
#pragma mark MKMapViewDelegate methods


-(MKAnnotationView *)mapView:(MKMapView *)thismapView viewForAnnotation:(id <MKAnnotation>)annotation
{
    static NSString *HotelAnnotationIdentifier = @"HotelAnnotationIdentifier";
	
	__autoreleasing HotelAnnotationView	*annotationView = (HotelAnnotationView*)[thismapView dequeueReusableAnnotationViewWithIdentifier:HotelAnnotationIdentifier];
	if (annotationView == nil)
		annotationView = [[HotelAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:HotelAnnotationIdentifier];
	
	annotationView.mapController = self;
	annotationView.hotelIndex = ((HotelAnnotation*)annotation).hotelIndex;
	return annotationView;
}


#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	if (parentMVC.selectedHotelIndex == -1)
		return 0;
	else
		return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return 1;
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSArray *aHotel = [[HotelBookingManager sharedInstance] fetchedAllSorted:0];
    if([aHotel count] == 0)
        return nil;
    
    if(parentMVC.selectedHotelIndex < 0)
        parentMVC.selectedHotelIndex = 0;
    
	EntityHotelBooking* hotelResult = aHotel[parentMVC.selectedHotelIndex];
    
    static NSString *CellIdentifier = @"HotelListSimplerCell";
	
	HotelListCell *cell = (HotelListCell *)[self.tblView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil)
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelListSimplerCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[HotelListCell class]])
				cell = (HotelListCell*)oneObject;
    }
    
	//HotelListCell *cell = [self makeAndConfigureHotelListCellForTable:tableView hotel:hotelResult];
    [self configureCell:cell hotelBooking:hotelResult];
	
	cell.address1.textColor = [UIColor blackColor];
	cell.address2.textColor = [UIColor blackColor];
	
	cell.hotelIndex = parentMVC.selectedHotelIndex;
	return cell;
}

-(void)configureCell:(HotelListCell*)cell hotelBooking:(EntityHotelBooking*)hotel
{

    cell.parentMVC = self;
    
    cell.name.text = hotel.hotel;
    cell.address1.text = hotel.addr1;
    
    NSString *state = ((hotel.stateAbbrev != nil && [hotel.stateAbbrev length]) > 0 ? hotel.stateAbbrev : hotel.state);

    // CityStateZip variable needs building up so that it doesn't look amateur
    NSString *cityStateZip = @"";
    
    if ([hotel.city length])
    {
        cityStateZip = hotel.city;
        if ([state length] || [hotel.zip length])
        {
            cityStateZip = [cityStateZip stringByAppendingString:@", "];
        }
    }
    if ([state length])
    {
        cityStateZip = [cityStateZip stringByAppendingString:state];
        if ([hotel.zip length])
        {
            cityStateZip = [cityStateZip stringByAppendingString:@" "];
        }
    }
    if ([hotel.zip length])
    {
        cityStateZip = [cityStateZip stringByAppendingString:hotel.zip];
    }
    
    cell.address2.text = cityStateZip;
    
    cell.phone.text =  hotel.phone;
    cell.distance.text = [NSString stringWithFormat:@"%@ %@", hotel.distance, hotel.distanceUnit];
    
    if([hotel.isSoldOut boolValue])
    {
        cell.amount.text = [Localizer getLocalizedText:@"Sold Out"];
        cell.lblStarting.hidden = YES;
    }
    else if([hotel.isNoRates boolValue])
    {
        cell.amount.text = [Localizer getLocalizedText:@"No Rates"];
        cell.lblStarting.hidden = YES;
    }
    else if([hotel.isAddtional boolValue])
    {
        cell.amount.text = [Localizer getLocalizedText:@"View Rates"];
        cell.lblStarting.hidden = NO;
    }
    else
    {
        NSString *currencyCodeForCheapestRate = [hotel.cheapestRoomRate floatValue] == [hotel.relCheapRoom.rate floatValue] ? hotel.relCheapRoom.crnCode : hotel.relCheapRoomViolation.crnCode;
        cell.amount.text = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", [hotel.cheapestRoomRate floatValue]] crnCode:currencyCodeForCheapestRate];
        cell.lblStarting.hidden = NO;
    }
    
    int asterisks = [hotel.starRating intValue];// .starRatingAsterisks;
    if (asterisks == 0)
    {
        cell.starRating.hidden = YES;
        cell.shadowStarRating.hidden = YES;
        cell.notRated.hidden = YES;
        cell.ivStars.hidden = NO;
        cell.ivStars.image = [UIImage imageNamed:@"stars_0"];
    }
    else
    {
        cell.ivStars.hidden = NO;
        int starCount = asterisks;
        if(starCount == 1)
            cell.ivStars.image = [UIImage imageNamed:@"stars_1"];
        else if(starCount == 2)
            cell.ivStars.image = [UIImage imageNamed:@"stars_2"];
        else if(starCount == 3)
            cell.ivStars.image = [UIImage imageNamed:@"stars_3"];
        else if(starCount == 4)
            cell.ivStars.image = [UIImage imageNamed:@"stars_4"];
        else if(starCount == 5)
            cell.ivStars.image = [UIImage imageNamed:@"stars_5"];
        cell.starRating.hidden = NO;
        cell.shadowStarRating.hidden = NO;
        cell.notRated.hidden = YES;
    }
    
    
    int diamonds = [hotel.hotelPrefRank intValue];
    if (diamonds == 0)
    {
        cell.ivDiamonds.hidden = YES;
    }
    else
    {
        //NSLog(@"diamonds %d", diamonds);
        cell.ivDiamonds.hidden = NO;
        if(diamonds == 4)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_1"];
        else if(diamonds == 5)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_2"];
        else if(diamonds == 10)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_3"];
        else if(diamonds == 1)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_1"];
        else if(diamonds == 2)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_2"];
        else if(diamonds == 3)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_3"];
    }
    
    if ([hotel.relHotelImage count] > 0)
    {
        for(EntityHotelImage *image in hotel.relHotelImage)
        {
            cell.logoView.image = nil;  // The cell may have been dequeued with an image already in it.  Get rid of it.
            UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
            //HotelImageData *hid = [hotelResult.propertyImagePairs objectAtIndex:0];
            [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:image.thumbURI RespondToImage:img IV:cell.logoView MVC:parentMVC];
            break;
        }
    }
    
    cell.ivRecommendation.hidden = YES;
    cell.recommendationText.hidden = YES;
    
    if ([hotel.travelPoints intValue] != 0) {
        cell.lblTravelPoints.hidden = NO;
        if ([hotel.travelPoints intValue] > 0) {
            cell.lblTravelPoints.text = [NSString stringWithFormat:[@"Earn %d pts." localize],[hotel.travelPoints intValue]];
            cell.lblTravelPoints.textColor = [UIColor bookingGreenColor];
        }
        else {
            cell.lblTravelPoints.text = [NSString stringWithFormat:[@"Use %d pts." localize],-[hotel.travelPoints intValue]];
            cell.lblTravelPoints.textColor = [UIColor bookingRedColor];
        }
    }
    else {
        cell.lblTravelPoints.hidden = YES;
    }
    
    // Load the logo asynchronously
    //	if (hotelResult.propertyUri != nil && [hotelResult.propertyUri length] > 0)
    //	{
    //		cell.logoView.image = nil;  // The cell may have been dequeued with an image already in it.  Get rid of it.
    //		UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
    //		[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:hotelResult.propertyUri RespondToImage:img IV:cell.logoView MVC:parentMVC];
    //	}
    
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
    //    
    
}


#pragma mark -
#pragma mark Table view delegate

- (void) tableView:(UITableView *)tableView accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath
{
    NSArray *aHotel = [[HotelBookingManager sharedInstance] fetchedAllSorted:0];
    
    if(aHotel == nil || [aHotel count] == 0)
        return;
    
    if(parentMVC.selectedHotelIndex == -1)
        parentMVC.selectedHotelIndex = 0;
    
    EntityHotelBooking *entity = aHotel[parentMVC.selectedHotelIndex];
    
	[parentMVC showRoomsForSelectedHotel:entity];
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSArray *aHotel = [[HotelBookingManager sharedInstance] fetchedAllSorted:0];
    
    if(aHotel == nil || [aHotel count] == 0)
        return;
    
    if(parentMVC.selectedHotelIndex == -1)
        parentMVC.selectedHotelIndex = 0;
    
    EntityHotelBooking *entity = aHotel[parentMVC.selectedHotelIndex];

	[parentMVC showRoomsForSelectedHotel:entity];
}

-(CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 74;
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

/*
- (void)viewDidLoad {
    [super viewDidLoad];

    // Uncomment the following line to preserve selection between presentations.
    self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}
*/

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	
	//tblView.allowsSelection = NO;

	[self updateMap];
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

/*
-(void)didSwitchViews
{
	// Called when the view switch animation is finished.
}
*/


#pragma mark -
#pragma mark Toolbar Methods

- (void)updateToolbar
{
	[parentMVC showMinimalToolbar];
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
	self.tblView = nil;
	self.mapView = nil;
}


- (void)dealloc
{
	mapView.delegate = nil;
}


@end

