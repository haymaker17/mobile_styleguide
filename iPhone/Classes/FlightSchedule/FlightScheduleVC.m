//
//  FlightScheduleVC.m
//  ConcurMobile
//
//  Created by Paul Schmidt on 18 Dec 2012.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "FlightScheduleVC.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "FlightScheduleCell.h"

#import "EntitySegmentLocation.h"
#import "SegmentData.h"

#import "FSSegment.h"
#import "FSSegmentOption.h"
#import "FSFlight.h"



@implementation FlightScheduleVC

@synthesize segment, flightData, flightOptions;

@synthesize navBar;
@synthesize doneBtn;

@synthesize managedObjectContext=__managedObjectContext;
@synthesize tableList, lblDates, lblHeading, imgHeading;

-(BOOL) allowActionWhileOffline
{
    return YES;
}

-(void) actionOnNoData:(id)sender
{
    [self hideNoDataView];
}

- (NSString *)instructionForNoDataView
{
    return [Localizer getLocalizedText:@"Sorry, The Internet connection appears to be offline. Please try again later."];
}

- (IBAction)donePressed:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}


-(void)respondToFoundData:(Msg *)msg
{
    if ([self isViewLoaded])
        [self hideLoadingView];
    
	if ([msg.idKey isEqualToString:FLIGHT_SCHEDULE_DATA] )
	{
        flightData = (FlightScheduleData*) msg.responder;
        
        flightOptions = [[NSMutableArray alloc] init];
        [flightData appendOptions:flightOptions];
        
        if ([self isViewLoaded])
        {
            if ([flightOptions count] == 0)
                [self showNoDataView:self asSubviewOfView:self.tableList];
            else
                [self refetchData];
        }
    }
}

-(NSString *) getViewIDKey
{
    return @"FLIGHTSCHEDULE";
}

#pragma mark - View Controller Stuff
-(void) dealloc
{
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
    [self.navigationController setToolbarHidden:YES];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    NSString *title = [Localizer getLocalizedText:@"Flight Schedules"];
    
    if ([UIDevice isPad])
    {
        navBar.topItem.title = title;
        doneBtn.title = [Localizer getLocalizedText:@"LABEL_DONE_BTN"];
    }
    else{
        CGFloat move = navBar.bounds.size.height;
        [navBar removeFromSuperview];
        
        [self moveUp:imgHeading by:move stretch:NO];
        [self moveUp:lblDates by:move stretch:NO];
        [self moveUp:lblHeading by:move stretch:NO];
        [self moveUp:tableList by:move stretch:NO];
        
            
        self.title = title;
    }
    
    [[ExSystem sharedInstance].imageControl startVendorImageCache];
    
    
    lblHeading.text = [NSString stringWithFormat:@"%@ %@ %@",
                       [self buildLocation:segment.relStartLocation],
                       [Localizer getLocalizedText:@"SLV_TO"],
                       [self buildLocation:segment.relEndLocation]];
    
    
    NSMutableString *departDate = [NSMutableString string];
    [SegmentData getDepartTimeString:segment timeStr:nil dateStr:departDate];
    
    
    if (segment.relStartLocation.dateLocal != nil)
    {
        lblDates.text = [DateTimeFormatter formatDateMedium:segment.relStartLocation.dateLocal];
    }
    
    
    
    [self showLoadingViewWithText:[Localizer getLocalizedText:@"Fetching Data"]];
    
    NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 segment.relEndLocation.cityCode, @"ArrivalIATACode",
                                 segment.vendor, @"CarrierCode",
                                 segment.relStartLocation.cityCode, @"DepartureIATACode",
                                 segment.relStartLocation.dateLocal, @"FlightDate",
                                 nil];
    
    [[ExSystem sharedInstance].msgControl createMsg:FLIGHT_SCHEDULE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    
}

- (void) moveUp: (UIView*)view by:(CGFloat)y stretch:(BOOL) stretch
{
    CGRect rc = [view frame];
    rc.origin.y -= y;
    
    if (stretch)
        rc.size.height += y;
    [view setFrame:rc];
    
}

- (NSString *) buildLocation: (EntitySegmentLocation*) location
{
    NSMutableString *s = [[NSMutableString alloc] init];
    BOOL needComma = NO;
    BOOL haveState = NO;
    
    [s appendFormat:@"(%@) ", location.cityCode];
    if (location.airportCity != nil)
    {
        [s appendString: location.airportCity];
        needComma = YES;
    }
    
    if (location.airportState != nil)
    {
        if (needComma)
            [s appendString:@", "];
        [s appendString: location.airportState];
        needComma = YES;
        haveState = YES;
    }
    
    if (!haveState && location.airportCountryCode != nil)
    {
        if (needComma)
            [s appendString:@", "];
        [s appendString: location.airportCountryCode];
    }
    
    return s;
}

- (void)viewDidUnload
{
    navBar = nil;
    imgHeading = nil;
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
#pragma mark No Data View delegates
-(NSString*)titleForNoDataView
{
    return [Localizer getLocalizedText:@"No Flights Found"];
}

-(NSString*) imageForNoDataView
{
    return @"neg_airbooking_icon";
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (flightOptions == nil)
        return 0;
    else
        return [flightOptions count];
    
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    
    FlightScheduleCell *cell = (FlightScheduleCell *)[tableView dequeueReusableCellWithIdentifier: @"FlightScheduleCell"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"FlightScheduleCell" owner:self options:nil];
        
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[FlightScheduleCell class]])
                cell = (FlightScheduleCell *)oneObject;
    }
    
    [self configureCell:cell atIndexPath:indexPath];
    return cell;
    
}


#pragma mark -
#pragma mark Table Delegate Methods
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return @"";
}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 69;
}



#pragma mark - reset and then fetch the managed results
-(void) refetchData
{
    
    [tableList reloadData];
}

#pragma mark - Cell Config

- (void)configureCell:(FlightScheduleCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    NSInteger row = [indexPath row];
    NSString* cellId = [NSString stringWithFormat:@"%ld", (long)row];
    
    FSSegmentOption *flightOption = (FSSegmentOption*)flightOptions[row];
    cell.lblAirline.text = [flightOption carrierText];
    
    FSFlight *firstFlight = (flightOption.flights)[0];
    FSFlight *lastFlight = [flightOption.flights lastObject];
    
    if (firstFlight != nil)
    {
        UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:firstFlight.carrier VendorType:@"a_small" RespondToIV:cell.ivLogo cellId:cellId];
        if(gotImg != nil)
            cell.ivLogo.image = gotImg;
    }
    
    cell.lblDepartIata.text = segment.relStartLocation.cityCode;
    cell.lblArriveIata.text = segment.relEndLocation.cityCode;
    
    cell.lblDepartTime.text = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:firstFlight.depDateTime];
    cell.lblArriveTime.text = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:lastFlight.arrDateTime];
    
    int flightMinutes = flightOption.totalElapsedTime;
    int flightHours = flightMinutes / 60;
    if (flightHours > 0)
        flightMinutes = flightMinutes - (flightHours * 60);
    NSString *dur = [NSString stringWithFormat:@"%dh %dm", flightHours, flightMinutes];
    
    int numStops =  (int)[flightOption.flights count] - 1;
    int numSeats = 9999;
    
    
    for (FSFlight *flt in flightOption.flights)
    {
        numStops += flt.numStops;
        
        int fltSeats = 0;
        
        for (FSClassOfService *cos in flt.classesOfService)
        {
            fltSeats += cos.seats;
        }
        numSeats = MIN(numSeats, fltSeats);
    }
    cell.lblSeats.text = numSeats>8? @"9+" : [NSString stringWithFormat:@"%d", numSeats];
    cell.lblSeatsText.text = [Localizer getLocalizedText:@"Seats"];
    
    
    
    cell.lblDurationStops.text = [NSString stringWithFormat:@"%@ / %d Stops", dur, numStops ];
    
    /*
     NSString* cellId = [NSString stringWithFormat:@"%d", [indexPath row]];
     //    NSLog(@"configure cell for %@", cellId);
     NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
     EntityAirFilterSummary *entity = (EntityAirFilterSummary *)managedObject;
     
     if([airShopResults.airline isEqualToString:@"ZZZZZZZZTOTAL"])
     {
     cell.lblAirline.text = [airShop.vendors objectForKey:entity.airlineCode];
     cell.lblCost.text = [FormatUtils formatMoney:[entity.fare stringValue] crnCode:entity.crnCode] ;
     UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:entity.airlineCode VendorType:@"a_small" RespondToIV:cell.ivLogo cellId:cellId];
     if(gotImg != nil)
     cell.ivLogo.image = gotImg;
     }
     else
     {
     cell.lblAirline.text = [airShop.vendors objectForKey:entity.airlineCode];
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
     
     // When debugging, airRule is always nil, therefore, no need for it.
     //    EntityAirRules *airRule = (EntityAirRules*)[[AirRuleManager sharedInstance]fetchMostSevre:entity.fareId];
     
     [cell.lblCost setShadowOffset:CGSizeMake(0, 0)];
     
     EntityAirViolation* highestViolation = [[AirFilterSummaryManager sharedInstance] fetchHighestEnforcement:entity];
     
     if(highestViolation != nil)
     {
     int eLevel = [highestViolation.enforcementLevel intValue];
     //        NSLog(@"eLevel = %d", eLevel);
     if(eLevel < kViolationLogForReportsOnly || eLevel == 100)
     {
     [cell.lblCost setTextColor:[ExColors colorBookingGreen]];
     cell.ivRule.hidden = YES;
     }
     else if(eLevel >= kViolationLogForReportsOnly && eLevel <= kViolationNotifyManager)
     {
     [cell.lblCost setTextColor:[ExColors colorBookingYellow]];
     cell.ivRule.image = [UIImage imageNamed:@"icon_yellowex"];
     cell.ivRule.hidden = NO;
     }
     else if(eLevel >= kViolationRequiresPassiveApproval && eLevel <= kViolationRequiresApproval)
     {
     [cell.lblCost setTextColor:[ExColors colorBookingRed]];
     cell.ivRule.image = [UIImage imageNamed:@"icon_redex"];
     cell.ivRule.hidden = NO;
     }
     else if(eLevel == kViolationAutoFail)
     {
     [cell.lblCost setTextColor:[ExColors colorBookingGray]];
     cell.ivRule.hidden = YES;
     }
     else
     {
     [cell.lblCost setTextColor:[ExColors colorBookingRed]];
     cell.ivRule.image = [UIImage imageNamed:@"icon_redex"];
     cell.ivRule.hidden = NO;
     }
     }
     else
     {
     [cell.lblCost setTextColor:[ExColors colorBookingGreen]];
     cell.ivRule.hidden = YES;
     }
     
     if([entity.refundable boolValue])
     {
     cell.lblRefundable.hidden = NO;
     cell.lblRefundable.text = [Localizer getLocalizedText:@"(Refundable)"];
     }
     else
     {
     cell.lblRefundable.hidden = YES;
     }
     */
}

@end
