//
//  SegmentSelectVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/18/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "SegmentSelectVC.h"
#import "TripsCell.h"
#import "SegmentSelectCell.h"
#import "DateTimeFormatter.h"
#import "FormatUtils.h"

#import "ReportViewControllerBase.h"

#define kSelected 1
#define kNotSelected 2
#define kExpensed 3

@implementation SegmentSelectVC
@synthesize tableList, trip, tripsData, aSegments, dictSelected;

#pragma mark - MVC Stuff
-(void)respondToFoundData:(Msg *)msg
{//respond to data that might be coming from the cache
    [self hideWaitView];
    
	if ([msg.idKey isEqualToString:EXPENSE_TRIPIT_TRIP])
	{
        TripItExpenseTripData *data = (TripItExpenseTripData*)msg.responder;
        NSString *rptKey = data.rptKey;
        
        if (data.actionStatus != nil && data.actionStatus.status != nil && [data.actionStatus.status isEqualToString:@"SUCCESS"] && rptKey != nil && [rptKey length] > 0)
        {
            [ReportViewControllerBase refreshSummaryData];
            
            NSMutableDictionary * pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                          @"YES", @"POP_TO_ROOT_VIEW", 
                                          rptKey, @"ID_KEY", 
                                          //self.rpt, @"REPORT",
                                          ROLE_EXPENSE_TRAVELER, @"ROLE", // TODO: Yiwen, is the right role?
                                          //self.rpt, @"REPORT_DETAIL",
                                          rptKey, @"RECORD_KEY", @"YES", @"SHORT_CIRCUIT", nil];
            
            if(![UIDevice isPad])
            {
                [ConcurMobileAppDelegate switchToView:ACTIVE_ENTRIES viewFrom:TRIPIT_AUTH ParameterBag:pBag];
            }
            else {
                iPadHomeVC *padHomeVC = [ConcurMobileAppDelegate findiPadHomeVC];
                [padHomeVC hideNoDataView];
                [padHomeVC.btnTripItTable setSelected:YES];
                [padHomeVC.btnReportsTable setSelected:NO];
                
                [padHomeVC.reportsTableView setHidden:YES];
                [padHomeVC.tripItTripsTableView setHidden:NO];
                [padHomeVC.view bringSubviewToFront: padHomeVC.tripItTripsTableView];
                padHomeVC.tripsVC.tableList = padHomeVC.tripItTripsTableView;
                [padHomeVC.tripsVC loadTripsWithExpenseData];
                [padHomeVC hideNoDataView];
                [padHomeVC checkStateOfTrips];
                [self dismissViewControllerAnimated:YES completion:nil];
                return;
            }
    
            
            // TODO: Handle iPad!  Probably should not call RootViewController on iPad!
        }
        else
        {
            NSString *errorMessage = nil;
            
            if (data.actionStatus != nil && [data.actionStatus.errMsg length])
                errorMessage = data.actionStatus.errMsg;
            else
                errorMessage = [Localizer getLocalizedText:@"SmartExpense was unable to expense your trip. Please try again later."];
            
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Error"] message:errorMessage delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
            [av show];
        }
    }
}

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


#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.title = trip.tripName;
    self.aSegments = [TripData makeSegmentsArray:trip];
    self.dictSelected = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    
//    for(SegmentData *seg in aSegments)
//        [dictSelected setObject:[NSNumber numberWithInt:kSelected] forKey:seg.idKey];
    
    self.navigationController.toolbarHidden = NO;
    if(!trip.isExpensed)
    {
        UIBarButtonItem *btnExpenseIt = [ExSystem makeColoredButton:@"BLUE" W:100 H:32 Text:[Localizer getLocalizedText:@"Expense Trip"] SelectorString:@"expenseTrip:" MobileVC:self];
        UIBarButtonItem *btnFlex =[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
        [self setToolbarItems:@[btnFlex, btnExpenseIt]];
    }
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.tableList = nil;
}


#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
    
}


- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
    return [[TripData makeSegmentsArray:trip] count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	static NSString *MyCellIdentifier = @"SegmentSelectCell";
    
    SegmentSelectCell *cell = (SegmentSelectCell *)[tableView dequeueReusableCellWithIdentifier: MyCellIdentifier];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SegmentSelectCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[SegmentSelectCell class]])
                cell = (SegmentSelectCell *)oneObject;
    }
    
    EntitySegment *segment = aSegments[indexPath.row];

    if([segment.type isEqualToString:SEG_TYPE_AIR])
        [self configureCellAir:cell segment:segment];
    else if([segment.type isEqualToString:SEG_TYPE_CAR])
        [self configureCellCar:cell segment:segment];
    else if([segment.type isEqualToString:SEG_TYPE_HOTEL])
        [self configureCellHotel:cell segment:segment];
    else if([segment.type isEqualToString:SEG_TYPE_RIDE])
        [self configureCellRide:cell segment:segment];
    else if([segment.type isEqualToString:SEG_TYPE_RAIL])
        [self configureCellRail:cell segment:segment];
    else if([segment.type isEqualToString:SEG_TYPE_PARKING])
        [self configureCellParking:cell segment:segment];
    
//    if(indexPath.row == 1)
//        [dictSelected setObject:[NSNumber numberWithInt:kExpensed] forKey:segment.idKey];
    
//    NSNumber *selState = [dictSelected objectForKey:segment.idKey];
//    
//    if([selState intValue] == kSelected)
//        cell.ivCheck.image = [UIImage imageNamed:@"check_greenselect"];
//    else if([selState intValue] == kNotSelected)
//        cell.ivCheck.image = [UIImage imageNamed:@"check_unselect"];
//    else if([selState intValue] == kExpensed)
    if(trip.isExpensed)
        cell.ivCheck.image = [UIImage imageNamed:@"check_gray"];
    else {
        cell.ivCheck.image = [UIImage imageNamed:@"check_greenselect"];
    }
    
    if(segment.totalRate != nil && segment.currency != nil)
        cell.lblAmount.text = [FormatUtils formatMoneyWithNumber:segment.totalRate crnCode:segment.currency];
    else if(segment.dailyRate != nil && segment.currency != nil)
        cell.lblAmount.text = [FormatUtils formatMoneyWithNumber:segment.dailyRate crnCode:segment.currency];
    else
        cell.lblAmount.text = @"";
    
    [cell setAccessoryType:UITableViewCellAccessoryNone];
    
    return cell;
    
}

#pragma mark -
#pragma mark Table Delegate Methods 

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{

    SegmentData *segment = aSegments[newIndexPath.row];
    NSNumber *selState = dictSelected[segment.idKey];
    
    if([selState intValue] == kSelected)
    {
        dictSelected[segment.idKey] = @kNotSelected;
        [tableView reloadRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
    }
    else if([selState intValue] == kNotSelected)
    {
        dictSelected[segment.idKey] = @kSelected;
        [tableView reloadRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
    }
}


- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 54;
}

#pragma mark - Configure Cells
-(void) configureCellParking:(SegmentSelectCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_parking"];
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblHeading.text = vendorName;
    
}

-(void)configureCellHotel:(SegmentSelectCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_lodging"];
    
    NSString *vendorName;
    if (segment.segmentName != nil)
        vendorName = segment.segmentName;
    else if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblHeading.text = vendorName;
    cell.lblSubHeading.text = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateEEEMMMdd:segment.relStartLocation.dateLocal], [DateTimeFormatter formatDateEEEMMMdd:segment.relEndLocation.dateLocal]];
    cell.lblSubHeading2.text = segment.confirmationNumber;
}


-(void) configureCellRide:(SegmentSelectCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_taxi"];
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblHeading.text = vendorName;
    
}


-(void) configureCellCar:(SegmentSelectCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_car"];
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblHeading.text = vendorName;
    
    NSString *departTime = @"";
    if(segment.relFlightStats.departureActual != nil)
        departTime = [DateTimeFormatter formatTimeForTravel:segment.relFlightStats.departureActual];
    else if(segment.relFlightStats.departureEstimated != nil)
        departTime = [DateTimeFormatter formatTimeForTravel:segment.relFlightStats.departureEstimated];
    else 
        departTime = [DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal];
    
    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        cell.lblSubHeading.text = [NSString stringWithFormat:@"%@ %@", aTime[0], aTime[1]];
    }
    else
        cell.lblSubHeading.text = departTime;
    
    cell.lblSubHeading2.text = @"";
    
}

-(void) configureCellRail:(SegmentSelectCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_rail"];
    
    NSString *railStation = [SegmentData getRailStation:segment.relStartLocation];
    
    NSString *endRailStation = [SegmentData getRailStation: segment.relEndLocation];

    cell.lblHeading.text = [NSString stringWithFormat:@"%@ - %@", railStation, endRailStation];
}

-(void) configureCellAir:(SegmentSelectCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_flight"];
    cell.lblHeading.text = [NSString stringWithFormat:@"%@ %@ %@", segment.relStartLocation.airportCity, [Localizer getLocalizedText:@"SLV_TO"], segment.relEndLocation.airportCity];;
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblSubHeading.text = [NSString stringWithFormat:@"%@ %@", vendorName, segment.flightNumber]; 
    
    cell.lblSubHeading2.text = [DateTimeFormatter formatDateTimeEEEMMMddHHmmaazzz:segment.relStartLocation.dateLocal];
    
}


#pragma mark - Expense Trip
-(IBAction)expenseTrip:(id)sender
{
    [self showWaitViewWithText:[@"Expensing the Trip" localize]];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:trip.itinLocator, @"ItinLocatorId", nil];
	[[ExSystem sharedInstance].msgControl createMsg:EXPENSE_TRIPIT_TRIP CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    
//    BOOL haveSelected = NO;
//    
//    for(NSString *key in dictSelected)
//    {
//        NSNumber *selState = [dictSelected objectForKey:key];
//        if([selState intValue] == kSelected)
//        {
//            haveSelected = YES;
//            break;
//        }
//    }
//    
//    if(!haveSelected)
//    {
//        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"No Segments Selected"] message:[Localizer getLocalizedText:@"No trip segments have been selected"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles: nil];
//        [av show];
//        [av release];
//        return;
//    }
//    
//    MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Please Confirm"] message:[Localizer getLocalizedText:@"Would you like to create an Expense Report from the selected trip segments"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"No"] otherButtonTitles:[Localizer getLocalizedText:@"Yes"], nil];
//    [av show];
//    [av release];
}

-(void) alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if(buttonIndex == 1)
    {
        NSMutableArray *a = [[NSMutableArray alloc] initWithObjects: nil];
        for(NSString *key in dictSelected)
        {
            [a addObject:key];
        }
        
        for(NSString *key in a)
        {
            NSNumber *selState = @kExpensed;
            dictSelected[key] = selState;
        }
        [self.tableList reloadData];
    }
}

#pragma mark - Close
-(IBAction)closeMe:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}
@end
