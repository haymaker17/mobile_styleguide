//
//  FlightStatsViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 9/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FlightStatsViewController.h"
#import "ExSystem.h" 

#import "TripsData.h"
#import "TripData.h"
#import "SegmentData.h"
#import "FlightStatsData.h"
#import "ItinDetailsCellLabel.h"
#import "BookingLabelValueCell.h"
#import "WebViewController.h"
#import "MapViewController.h"
#import "AppsUtil.h"
#import "Config.h"

@implementation FlightStatsViewController

@synthesize tBar;
@synthesize fetchView;
@synthesize spinnerFetch;
@synthesize tableList;
@synthesize tripKey;
@synthesize segmentKey;
@synthesize segment;
@synthesize sections;
@synthesize navBar;
@synthesize doneBtn;
@synthesize lblLoading;

#define kSectionDeparture				0
#define kSectionDiversion				1
#define kSectionArrival					2
#define kSectionBaggage					3
#define kSectionAircraft				4

#define kRowHeader						0	// All sections have this row

#define kRowDepartureStatus				1
#define kRowDepartureScheduled			2
#define kRowDepartureEstimated			3
#define kRowDepartureActual				4
#define kRowDepartureTerminalScheduled	5
#define kRowDepartureTerminalActual		6
#define kRowDepartureGate				7

#define kRowDiversionCity				1
#define kRowDiversionAirport			2

#define kRowArrivalScheduled			1
#define kRowArrivalEstimated			2
#define kRowArrivalTerminal				3
#define kRowArrivalGate					4
#define kRowArrivalStatus				5

#define kRowBaggageClaim				1

#define kRowAircraft					1


#pragma mark -
#pragma mark Button Handler Methods

-(IBAction) donePressed:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];
}


#pragma mark -
#pragma mark Refresh Methods

-(void) makeRefreshButton:(NSString *)dtRefreshed
{
	const int refreshDateWidth = 280;	// 160
	const int refreshDateHeight = 20;	// 30
	
	UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, refreshDateWidth, refreshDateHeight)];
	UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, refreshDateWidth, refreshDateHeight)];
	//lblText.numberOfLines = 2;
	//lblText.lineBreakMode = NSLineBreakByWordWrapping;
	//lblText.textAlignment = NSTextAlignmentLeft;
	lblText.text = dtRefreshed;
	if( false == [ExSystem is7Plus] )
    {
        [lblText setBackgroundColor:[UIColor clearColor]];
        [lblText setTextColor:[UIColor whiteColor]];
        [lblText setShadowColor:[UIColor blackColor]];
        [lblText setShadowOffset:CGSizeMake(0, -1)];
	}
    [lblText setFont:[UIFont boldSystemFontOfSize:12.0f]];
	[cv addSubview:lblText];
	
	UIBarButtonItem *btnRefresh = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRefresh target:self action:@selector(refreshData)];
	UIBarButtonItem *btnRefreshDate = [[UIBarButtonItem alloc] initWithCustomView:cv];
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSArray *toolbarItems = @[btnRefreshDate, flexibleSpace, btnRefresh];
	[tBar setItems:toolbarItems animated:NO];
}


-(void) refreshData
{
	[fetchView setHidden:NO];
	[self.view bringSubviewToFront:fetchView];
	[spinnerFetch startAnimating];
	EntityTrip *trip = [[TripManager sharedInstance] fetchByTripKey:tripKey];
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: trip.itinLocator, @"ITIN_LOCATOR", nil];
	[[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


#pragma mark -
#pragma mark RespondToFoundData Method

-(void)respondToFoundData:(Msg *)msg
{//respond to data that might be coming from the cache
	
	if ([msg.idKey isEqualToString:TRIPS_DATA])
	{
		[fetchView setHidden:YES];
		[spinnerFetch stopAnimating];

		NSString *dt = [DateTimeFormatter formatDateTimeMediumByDate:msg.dateOfData];
		dt = [NSString stringWithFormat:[Localizer getLocalizedText:@"Last updated"], dt];
		[self makeRefreshButton:dt];
		
		self.segment = nil;
		
		TripsData* tripsData = (TripsData *)msg.responder;
        
		if (tripsData != nil)
		{
			EntityTrip *trip = [[TripManager sharedInstance] fetchByTripKey:tripKey];
            //[tripsData.trips objectForKey:tripKey];
			if (trip != nil)
			{
				self.segment = [[TripManager sharedInstance] fetchBySegmentKey:segmentKey inTrip:trip];
				self.sections = [[NSMutableArray alloc] initWithObjects:nil]; // initWithCapacity:5];
								 
				[self fillAirSections];

			}
		}
				
		[tableList reloadData];
	}
}


#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];

	[self makeRefreshButton:@""];
	navBar.topItem.title = [Localizer getLocalizedText:@"Flight Status"];
	doneBtn.title = [Localizer getLocalizedText:@"LABEL_DONE_BTN"];
	lblLoading.text = [Localizer getLocalizedText:@"Loading Data"];
    
    self.tBar.tintColor = [UIColor darkBlueConcur_iOS6];
    self.navBar.tintColor = [UIColor darkBlueConcur_iOS6];
}

- (void)viewDidLayoutSubviews
{
    if ([self respondsToSelector:@selector(topLayoutGuide)])
    {
        CGRect viewBounds = self.view.bounds;
        CGFloat topBarOffset = self.topLayoutGuide.length;
        if (self.view.frame.origin.y != topBarOffset)
            [self.view setFrame:CGRectMake(viewBounds.origin.x, topBarOffset, viewBounds.size.width, viewBounds.size.height-topBarOffset)];
    }
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	[self refreshData];
}

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
	return [sections count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	NSMutableArray *a = sections[section];
	return [a count];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	
	NSUInteger row = [indexPath row];
	NSUInteger section = [indexPath section];
	
	NSMutableArray *sectionData = sections[section];
	SegmentRow *segRow = sectionData[row];
    
    ItinDetailCell *cell = (ItinDetailCell *)[tableView dequeueReusableCellWithIdentifier: @"ItinDetailCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[ItinDetailCell class]])
                cell = (ItinDetailCell *)oneObject;
    }
    
    cell.lblValue.frame = CGRectMake(10, 25, 300, 21);
    cell.ivDot.hidden = YES;
    if(segRow.isFlightStats)
    {
        cell.ivDot.hidden = NO;
        cell.lblValue.frame = CGRectMake(30, 25, 280, 21);
        if([segment.relFlightStats.departureShortStatus isEqualToString:@"DY"] || [segment.relFlightStats.departureShortStatus isEqualToString:@"Delayed"]
           || [segment.relFlightStats.departureShortStatus isEqualToString:@"Cancelled"])
            cell.ivDot.image = [UIImage imageNamed:@"flight_status_red"];
        else
            cell.ivDot.image = [UIImage imageNamed:@"flight_status_green"];
    }
    cell.lblLabel.text = segRow.rowLabel;
    cell.lblValue.text = segRow.rowValue;
    if(segRow.isApp || segRow.isMap || segRow.isPhone)
        [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    else
        [cell setAccessoryType:UITableViewCellAccessoryNone];
    
    
    return cell;
}

- (NSString*)notApplicableIfNil:(NSString*)str
{
	if (str != nil)
		str = [str stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]];

	return (str ? str : @"--");
}


#pragma mark -
#pragma mark Table view delegate
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
{
	
	if(section == 0)
		return @"";
	else if(section == 1)
		return [Localizer getLocalizedText:@"Departure Information"];
	else 
		return [Localizer getLocalizedText:@"Arrival Information"];
}

- (CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 50;
}


//need to make sure that a click on header means nothing
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
	NSUInteger section = [newIndexPath section];
    NSUInteger row = [newIndexPath row];
	
	NSMutableArray *sectionData = sections[section];
	SegmentRow *segRow = sectionData[row];
	
	if(segRow.isApp)
	{
        if([[ExSystem sharedInstance] hasRole:@"FlightTracker_User"])
        {
            NSDictionary *dict = @{@"Type": @"Mobiata"};
            [Flurry logEvent:@"External App: Launch" withParameters:dict];

            BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:segRow.url]];
            
            if (didLaunch == NO) {
                NSURL *appStoreUrl = [NSURL URLWithString:@"http://www.mobiata.com/flighttrack-app-concur"];  // Mobiata
                [[UIApplication sharedApplication] openURL:appStoreUrl];
            }
        }
	}
	else if (segRow.isFlightStats)
	{
		//[self showFlightStats]; //method does not exist
	}
	else if(segRow.isWeb && segRow.isAirport)
	{
        if([[ExSystem sharedInstance] hasRole:@"GateGuru_User"])
        {
            NSString *url = [NSString stringWithFormat:@"gateguru://airports/%@", segRow.iataCode];
            [AppsUtil launchGateGuruAppWithUrl:url];
        }

	}
	else if(segRow.isWeb)
	{
		if(!segRow.isSeat)
			[self loadWebView:segRow.url WebViewTitle:[NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Web site for t"], segRow.viewTitle]];
		else 
			[self loadWebViewSeat:segRow.url WebViewTitle:[NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Web site for t"], segRow.viewTitle]];
	}
	else if(segRow.isMap)
	{
        NSString *mapAddress = [SegmentData getMapAddress:segment.relStartLocation];
		
		if ([mapAddress length])
		{
			[self goSomeplace:mapAddress VendorName:[NSString stringWithFormat:@"%@ %@", segment.vendorName, segment.segmentName] VendorCode:segment.vendor];
		} 
		else 
		{
            mapAddress = [NSString stringWithFormat:@"(%@) %@\n%@, %@", segment.relStartLocation.cityCode, segment.relStartLocation.airportName, segment.relStartLocation.airportCity, segment.relStartLocation.airportState];
			[self goSomeplace:mapAddress VendorName:[NSString stringWithFormat:@"%@ %@", segment.vendorName, segment.segmentName == nil? segment.relStartLocation.airportName : segment.segmentName] VendorCode:segment.vendor];
		}
	}
}


#pragma mark -
#pragma mark Go someplace

-(void)goSomeplace:(NSString *)mapAddress VendorName:(NSString *)vendorName VendorCode:(NSString *)vendorCode
{
	MapViewController *mapView = [[MapViewController alloc] init];
	mapView.mapAddress = mapAddress;
	mapView.anoTitle = vendorName;
	mapView.anoSubTitle = mapAddress;
	[self presentViewController:mapView animated:YES completion:nil]; 
}


-(void)callNumber:(NSString *)phoneNum
{
	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", phoneNum]]];
}


-(IBAction)loadWebView:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle
{
	
	//do web view
	WebViewController *webView = [[WebViewController alloc] init];
	webView.url = [NSString stringWithFormat:@"https://%@", specialValueWeb];
	webView.viewTitle = webViewTitle;
	[self presentViewController:webView animated:YES completion:nil]; 
	
}

-(IBAction)loadWebViewSeat:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle
{
	WebViewController *webView = [[WebViewController alloc] init];
	webView.url = specialValueWeb;
	webView.viewTitle = webViewTitle;
	[self presentViewController:webView animated:YES completion:nil]; 
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
#pragma mark Making Yeezy Flexible Again

-(void) fillAirSections
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"Airline"];
	segRow.rowValue = [NSString stringWithFormat:@"%@ %@", segment.vendorName, segment.flightNumber];
	segRow.isAirVendor = YES;
	[section addObject:segRow];
	
	if(segment.operatedBy != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Operated By"];
        
        NSString *opVendor = segment.operatedByVendor;
        if(segment.operatedByVendor == nil)
            opVendor = segment.operatedBy;
        
        NSString *flightNum = segment.operatedByFlightNumber;
        
        if(segment.operatedByFlightNumber == nil)
            flightNum = @"";
        else 
            flightNum = [NSString stringWithFormat:@" %@", flightNum];
        
        NSString *OBvendor = [NSString stringWithFormat:@"(%@ %@)", opVendor, flightNum];
        
		segRow.rowValue = OBvendor;
		segRow.isOperatedBy = YES;
		[section addObject:segRow];
	}

	if([TripData isFlightDelayedOrCancelled: segment.relFlightStats])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Flight Status"];
		segRow.rowValue = segment.relFlightStats.departureShortStatus;
		segRow.isEmblazoned = YES;
		segRow.isFlightStats = YES;
		[section addObject:segRow];
	}
	else if(segment.relFlightStats.departureShortStatus != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Flight Status"];
		segRow.rowValue = segment.relFlightStats.departureShortStatus;
		segRow.isFlightStats = YES;
		[section addObject:segRow];
	}
	else
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Flight Status"];
		segRow.rowValue = [Localizer getLocalizedText: @"Scheduled On-time"];
		segRow.isFlightStats = YES;
		[section addObject:segRow];
	}
	
	//on hold until Mobiata gives me a good link
	if(segment.confirmationNumber != nil && segment.flightNumber != nil && segment.vendor != nil && segment.relStartLocation.cityCode != nil && segment.relEndLocation.cityCode != nil && segment.relStartLocation.dateLocal != nil && segment.relEndLocation.dateLocal != nil && ![[ExSystem sharedInstance] isGovernment])
	{
		NSString *depDate = [NSString stringWithFormat:@"%@%@", [DateTimeFormatter formatDateyyyyMMdd:segment.relStartLocation.dateLocal], [DateTimeFormatter formatTimeHHmm:segment.relStartLocation.dateLocal]];
		NSString *arrDate = [NSString stringWithFormat:@"%@%@", [DateTimeFormatter formatDateyyyyMMdd:segment.relEndLocation.dateLocal], [DateTimeFormatter formatTimeHHmm:segment.relEndLocation.dateLocal]];

        NSString *flightTrackURL = [NSString stringWithFormat:
                                    @"moflighttrack:saveFlight?departureDate=%@&departureAirportID=%@&arrivalAirportID=%@&airlineID=%@&flightNumber=%@&arrivalDate=%@&notes=Concur&confirmationNumber=%@&source=concur"
                                    ,depDate, segment.relStartLocation.cityCode, segment.relEndLocation.cityCode
                                    , segment.vendor, segment.flightNumber, arrDate, segment.confirmationNumber];					
        
        //NSLog(@"flightTrackURL=%@", flightTrackURL);
        segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = @"Flight Tracker";
        segRow.rowValue = @"Track your flight";
        segRow.url = flightTrackURL;
        segRow.isApp = YES;
        [section addObject:segRow];
	}
	
	//Departure Details
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_AIRPORT"];
    segRow.rowValue = [SegmentData getAirportFullName:segment.relStartLocation];
	segRow.isWeb = YES;
	segRow.isAirport = YES;
	segRow.iataCode = segment.relStartLocation.cityCode;
	segRow.url = [[ExSystem sharedInstance] getURLMap:@"AIRPORTS" LocalConstant:segment.relStartLocation.cityCode];
	segRow.viewTitle = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal Map for t"], segment.relStartLocation.cityCode];
	[section addObject:segRow];
	
	//new section
	if(segment.relFlightStats.departureScheduled != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Scheduled Departure"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.departureScheduled];
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.departureEstimated != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Estimated Departure"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.departureEstimated];
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.departureActual != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Actual Departure"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.departureActual];
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.departureStatusReason != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Departure Status"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = segment.relFlightStats.departureStatusReason;
		if ([segment.relFlightStats.departureShortStatus isEqualToString:@"DY"] || [segment.relFlightStats.departureShortStatus isEqualToString:@"Delayed"]) 
			segRow.isEmblazoned = YES;
		[section addObject:segRow];
	}
	
	//flight duration
	if(segment.duration != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Flight Duration"]; 
		int flightMinutes = [segment.duration intValue];
		int flightHours = flightMinutes / 60;
		
		if (flightHours > 0) 
			flightMinutes = flightMinutes - (flightHours * 60);
		
		NSString *dur = [NSString stringWithFormat:[Localizer getLocalizedText:@"%d Hours and %d Minute(s)"], flightHours, flightMinutes];
		
		if(flightHours < 1)
			dur = [NSString stringWithFormat:[Localizer getLocalizedText:@"%d Minute(s)"], flightMinutes];
		else if (flightHours == 1)
			[NSString stringWithFormat:[Localizer getLocalizedText:@"%d Hour and %d Minute(s)"], flightHours, flightMinutes];
		
		segRow.rowValue = dur;
		[section addObject:segRow];
	}
	

	
//	//Departure Flight Details section
	if(segment.relFlightStats.equipmentScheduled != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Scheduled Equipment"];
		segRow.rowValue = segment.relFlightStats.equipmentScheduled;
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.equipmentActual != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Actual Equipment"];
		segRow.rowValue = segment.relFlightStats.equipmentActual;
		[section addObject:segRow];
	}
	

	//Arrival Section
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	
	//arrival airport
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
	NSString *airportURL = [[ExSystem sharedInstance] getURLMap:@"AIRPORTS" LocalConstant:segment.relEndLocation.cityCode];
	segRow.url = airportURL; //@"www.airportterminalmaps.com/SEATAC-airport-terminal-map.html";
	segRow.viewTitle = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal Map for t"], segment.relEndLocation.cityCode];
	segRow.rowValue = [SegmentData getAirportFullName:segment.relEndLocation];
    //[NSString stringWithFormat:@"(%@) %@, %@", segment.endCityCode, segment.endAirportName, segment.endAirportState];
	if (airportURL == nil || [airportURL length] <= 0) 
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_AIRPORT"]; 
	segRow.isWeb = YES;
	segRow.isAirport = YES;
	segRow.iataCode = segment.relEndLocation.cityCode;
	[section addObject:segRow];
	
	//arrival date time
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"Scheduled Arrival"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
	segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relEndLocation.dateLocal];
	[section addObject:segRow];
	
	if(segment.relFlightStats.arrivalEstimated != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Estimated Arrival"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.arrivalEstimated];
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.arrivalActual != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Actual Arrival"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.arrivalActual];
		[section addObject:segRow];
	}
	
	//arrival terminal gate
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [NSString stringWithFormat:@"%@\n%@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TERMINAL"], 
					   [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_GATE"]];
    NSMutableString *term = [NSMutableString string];
    NSMutableString *gate = [NSMutableString string];
    
    [SegmentData getArriveTermGate:segment terminal:term gate:gate];
	segRow.rowValue = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal t Gate t"], term, gate];
	[section addObject:segRow];
	
	if(segment.relFlightStats.baggageClaim != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText: @"Baggage Claim"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = segment.relFlightStats.baggageClaim;
		[section addObject:segRow];
	}
	
	[sections addObject:section];
    
    [self.tableList reloadData];
	
}



@end

