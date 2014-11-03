//
//  Fusion2014TripDetailsViewController.m
//  ConcurMobile
//
//  Created by Shifan Wu on 4/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "Fusion2014TripDetailsViewController.h"
#import "Fusion2014HotelDetailCell.h"
#import "Fusion2014FlightDetailCell.h"
#import "Fusion2014TripDetailAddSegment.h"
#import "Fusion2014FlightSectionHeader.h"
#import "HotelAnnotation.h"
#import "TripsViewController.h"

@interface Fusion2014TripDetailsViewController ()

@property (nonatomic, strong) EntityTrip *selectedTrip;
@property (nonatomic, strong) NSMutableDictionary *tripSegments;
@property (nonatomic, strong) NSMutableArray *tripKeys;

@property (strong, nonatomic) IBOutlet UIButton *btnBack;
@end

@implementation Fusion2014TripDetailsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {

    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self.tableView registerNib:[UINib nibWithNibName:@"Fusion2014FlightDetailCell" bundle:nil] forCellReuseIdentifier:@"Fusion2014FlightDetailCell"];
    [self.tableView registerNib:[UINib nibWithNibName:@"Fusion2014HotelDetailCell" bundle:nil] forCellReuseIdentifier:@"Fusion2014HotelDetailCell"];
    
    self.tripSegments = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    self.tripKeys = [[NSMutableArray alloc] initWithArray:nil];
    
    [self.navigationController setNavigationBarHidden:YES];
    [self.navigationController setToolbarHidden:YES];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/


#pragma mark - Trip display functions
- (void)displayTrip:(EntityTrip *)newTrip TripKey:(NSString *)newTripKey
{
    self.tripSegments = [TripData makeSegmentDictGroupedByDate:newTrip];
    self.tripKeys = [TripData makeSegmentArrayGroupedByDate:newTrip];
    NSString *startDate = [DateTimeFormatter formatDateEEEMMMddByDate:newTrip.tripStartDateLocal];
    NSString *endDate = [DateTimeFormatter formatDateEEEMMMddByDate:newTrip.tripEndDateLocal];
    self.lblTripDateRange.text = [NSString stringWithFormat:@"%@ - %@", [self convertDateFormat:startDate withWeekDay:NO], [self convertDateFormat:endDate withWeekDay:NO]];
    self.lblTripName.text = newTrip.tripName;
    [self.tableView reloadData];
}

- (void) loadTrip:(NSMutableDictionary *)pBag
{
    [self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Data"]];
    [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


#pragma mark - UITableViewDataSource
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [self.tripSegments count];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *key = self.tripKeys[[indexPath section]];
    NSArray *segments = self.tripSegments[key];

    EntitySegment *segment = segments[indexPath.row];
    
    if([segment isKindOfClass:[EntityOffer class]])
        return 0;
    else if ([segment.type isEqualToString:@"HOTEL"])
        return 285.0;
    else if ([segment.type isEqualToString:@"AIR"])
        return 325.0;
    else
        return 250.0;   // default for now
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if ([self.tripSegments count] == 0)
        return 0;
    
    NSString *key = self.tripKeys[section];
    NSArray *nameSection = (NSArray *) self.tripSegments[key];
    
    return [nameSection count];
}

-(CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 44.0;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    Fusion2014FlightSectionHeader *view = [[NSBundle mainBundle] loadNibNamed:@"Fusion2014FlightSectionHeader" owner:self options:nil][0];
    if (![self.tripKeys[section] isEqualToString:@"SUMMARY"])
    {
        view.lblTravelDate.text = [self convertDateFormat:self.tripKeys[section] withWeekDay:YES];
        [view.lblDivider setHidden:NO];
    }

    if (section == 0)
    {
        [view.lblDivider setHidden:YES];
    }
    return view;
}

//- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
//{
//    NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"Fusion2014TripDetailAddSegment" owner:self options:nil];
//    return footerView;
//}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSInteger section = [indexPath section];
    
    NSString *key = self.tripKeys[section];
    NSArray *segments = self.tripSegments[key];
    
    NSObject *objSeg = segments[indexPath.row];
    
    UITableViewCell *tbCell = nil;
    if ([objSeg isKindOfClass:[EntitySegment class]])
    {
        EntitySegment *segment = segments[indexPath.row];
        segment = [[TripManager sharedInstance] fetchSegmentByIdKey:segment.idKey tripKey:self.selectedTrip.tripKey];
        if ([segment.type isEqualToString:@"AIR"])
        {
            Fusion2014FlightDetailCell *cell = (Fusion2014FlightDetailCell *)[self.tableView dequeueReusableCellWithIdentifier:@"Fusion2014FlightDetailCell"];
            [self configureFlightCell:cell segment:segment];
            tbCell = (UITableViewCell *)cell;
        }
        else if ([segment.type isEqualToString:@"HOTEL"])
        {
            Fusion2014HotelDetailCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion2014HotelDetailCell"];
            [self configureHotelCell:cell segment:segment];
            tbCell = (UITableViewCell *)cell;
        }
        else if ([segment.type isEqualToString:@"CAR"])
        {
            UITableViewCell *cell = [[UITableViewCell alloc] init];
            cell.textLabel.text = @"This booking type does not have a matching cell.";
            tbCell = cell;
        }
        else if([segment.type isEqualToString:@"RAIL"])
        {
            UITableViewCell *cell = [[UITableViewCell alloc] init];
            cell.textLabel.text = @"This booking type does not have a matching cell.";
            tbCell = cell;
        }
    }
    else if ([objSeg isKindOfClass:[EntityOffer class]])
    {
        UITableViewCell *cell = [[UITableViewCell alloc] init];
        cell.textLabel.text = @"This booking type does not have a matching cell.";
        tbCell = cell;
    }

    return tbCell;
}

- (void)configureFlightCell:(Fusion2014FlightDetailCell *)cell segment:(EntitySegment *)segment
{
    cell.lblSegmentTitle.text = [NSString stringWithFormat:@"Depart to %@, %@", segment.relEndLocation.airportCity, segment.relEndLocation.airportState];
    
    cell.lblArrivalCity.text = [segment.relEndLocation.airportCity uppercaseString];
    cell.lblDepartureCity.text = [segment.relStartLocation.airportCity uppercaseString];
    cell.lblDepartureAirportCode.text = segment.relStartLocation.cityCode;
    cell.lblArrivalAirportCode.text = segment.relEndLocation.cityCode;
    NSMutableString *departTime = [NSMutableString string];
    NSMutableString *departDate = [NSMutableString string];
    NSMutableString *arrivalTime = [NSMutableString string];
    [SegmentData getDepartTimeString:segment timeStr:departTime dateStr:departDate];

    cell.lblDepartureTime.text = departTime;
    [SegmentData getArriveTimeString:segment timeStr:arrivalTime dateStr:departDate];
    cell.lblArrivalTime.text = arrivalTime;
    
    int flightMinutes = [segment.duration intValue];
    int flightHours = flightMinutes / 60;
    
    if (flightHours > 0)
        flightMinutes = flightMinutes - (flightHours * 60);
    cell.lblFlightDuration.text = [NSString stringWithFormat:@"%dh %dm,", flightHours, flightMinutes];
    
    cell.lblFlightNumber.text = [NSString stringWithFormat:@"%@ %@", segment.vendorName , segment.flightNumber];
    
    NSMutableString *terminal = [NSMutableString string];
    NSMutableString *gate = [NSMutableString string];
    [SegmentData getArriveTermGate:segment terminal:terminal gate:gate];

    cell.lblTerminalNumber.text =  terminal;
    cell.lblGateNumber.text = gate;
    cell.lblSeatNumber.text = segment.seatNumber;
    cell.lblConfirmationNumber.text = segment.confirmationNumber;

    UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:segment.vendor VendorType:@"a" RespondToIV:cell.ivAirlineLogo];
    cell.ivAirlineLogo.image = gotImg;

    [cell.lblTitleDividerLine.layer setBorderWidth:2.0];
    [cell.lblTitleDividerLine.layer setBorderColor:[[UIColor colorWithPatternImage:[UIImage imageNamed:@"Fusion14_Itin_cellDivider"]] CGColor]];

    [cell.lblflighSummaryDividerline.layer setBorderWidth:2.0];
    [cell.lblflighSummaryDividerline.layer setBorderColor:[[UIColor colorWithPatternImage:[UIImage imageNamed:@"Fusion14_Itin_cellDivider"]] CGColor]];

    [cell.lblFinalDividerLine.layer setBorderWidth:2.0];
    [cell.lblFinalDividerLine.layer setBorderColor:[[UIColor colorWithPatternImage:[UIImage imageNamed:@"Fusion14_Itin_cellDivider"]] CGColor]];
}

- (void)configureHotelCell:(Fusion2014HotelDetailCell *)cell segment:(EntitySegment *)segment
{
    // Extract information from segment
    NSString *vendorName;
    if (segment.segmentName != nil)
        vendorName = segment.segmentName;
    else if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else
        vendorName = segment.vendor;
    
    NSNumber *latitude = segment.relStartLocation.latitude;
    NSNumber *longitude = segment.relStartLocation.longitude;
    
    MKCoordinateSpan span = MKCoordinateSpanMake(0.01f, 0.01f);
    CLLocationCoordinate2D coordinate = {[latitude doubleValue],[longitude doubleValue]};
    MKCoordinateRegion region = {coordinate, span};
    MKCoordinateRegion regionThatFits = [cell.mvHotelMap regionThatFits:region];
    [cell.mvHotelMap setDelegate:self];
    HotelAnnotation *myPin = [[HotelAnnotation alloc] init];
    myPin.coordinate = coordinate;
    
    // Setting style apperance of the Cell
    cell.uvCardBackground.layer.cornerRadius = 5;
    cell.uvCardBackground.layer.masksToBounds = YES;
    [cell.lblLine1.layer setBorderWidth:2.0];
    [cell.lblLine1.layer setBorderColor:[[UIColor colorWithPatternImage:[UIImage imageNamed:@"Fusion14_Itin_cellDivider"]] CGColor]];
    [cell.lblLine2.layer setBorderWidth:2.0];
    [cell.lblLine2.layer setBorderColor:[[UIColor colorWithPatternImage:[UIImage imageNamed:@"Fusion14_Itin_cellDivider"]] CGColor]];

    // Setting values to the UI element
    cell.lblHotelName.text = vendorName;
    cell.lblPhoneNumber.text = segment.phoneNumber;
    cell.lblHotelAddress.text = [NSString stringWithFormat:@"%@, %@",segment.relStartLocation.address, segment.relStartLocation.city];
    [cell.mvHotelMap setRegion:regionThatFits animated:YES];
    [cell.mvHotelMap addAnnotation:myPin];
    cell.lblConfirmationNumber.text = segment.confirmationNumber;

    [cell.lblHintText setHidden:YES];
    [cell.btnCancel setHidden:NO];
    [cell.btnEdit setHidden:NO];
}


#pragma mark - UITableViewDelegate


// trash code
- (void)loadTripByItinLocator:(NSString *)itinLocator
{
    self.selectedTrip = [[TripManager sharedInstance] fetchByItinLocator:itinLocator];
    [self displayTrip:self.selectedTrip TripKey:self.selectedTrip.tripKey];
}

#pragma mark - Message responder
- (void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:TRIPS_DATA] && msg.parameterBag[@"ITIN_LOCATOR"] && msg.parameterBag[@"LOADING_SINGLE_ITIN"])
    {
        if(self.isLoadingViewShowing)
            [self hideLoadingView];
        
        NSString *itinLocator = (msg.parameterBag)[@"ITIN_LOCATOR"];
        if ([self.tripDetailsRequestId isEqualToString:msg.parameterBag[@"TRIPDETAILSREQUEST_UUID"]])
        {
            self.selectedTrip = [[TripManager sharedInstance] fetchByItinLocator:itinLocator];
            [self displayTrip:self.selectedTrip TripKey:self.selectedTrip.tripKey];
        }
    }
    else if ([msg.idKey isEqualToString:TRIPS_DATA] && (msg.parameterBag)[@"ITIN_LOCATOR"])
    {
        if(self.isLoadingViewShowing)
            [self hideLoadingView];
        
        NSString *itinLocator = (msg.parameterBag)[@"ITIN_LOCATOR"];
        if ([self.tripDetailsRequestId isEqualToString:msg.parameterBag[@"TRIPDETAILSREQUEST_UUID"]])
        {
            self.selectedTrip = [[TripManager sharedInstance] fetchByItinLocator:itinLocator];
            [self displayTrip:self.selectedTrip TripKey:self.selectedTrip.tripKey];
        }
    }
}


//#pragma mark - MKMapViewDelegate
- (MKAnnotationView*)mapView:(MKMapView *)mapView viewForAnnotation:(id<MKAnnotation>)annotation
{
    MKPinAnnotationView *myPin = [[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"current"];
    [myPin setImage:[UIImage imageNamed:@"icon_map_pin"]];
    return myPin;
}

- (IBAction)btnBackPressed:(id)sender
{
//    if previous viewController is TripDetails then just pop otherwise, poptorootand push it over home
    UIViewController *previous = [self previousViewController];
    if (previous == nil) {
        return ;    // dont do anything
    }
    else if ([previous isKindOfClass:[TripsViewController class]])
        [self.navigationController popViewControllerAnimated:YES];
    else
        [self.navigationController popToRootViewControllerAnimated:YES];
}

- (UIViewController *)previousViewController
{
    NSInteger numberOfViewControllers = self.navigationController.viewControllers.count;
    
    if (numberOfViewControllers < 2)
        return nil;
    else
        return [self.navigationController.viewControllers objectAtIndex:numberOfViewControllers - 2];
}


#pragma - mark UIScrollViewDelegate
- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    CGFloat sectionHeaderHeight = 44.0;
    if (scrollView.contentOffset.y<=sectionHeaderHeight&&scrollView.contentOffset.y>=0) {
        scrollView.contentInset = UIEdgeInsetsMake(-scrollView.contentOffset.y, 0, 0, 0);
    } else if (scrollView.contentOffset.y>=sectionHeaderHeight) {
        scrollView.contentInset = UIEdgeInsetsMake(-sectionHeaderHeight, 0, 0, 0);
    }
}

- (NSString *)convertDateFormat:(NSString *)fromDate withWeekDay:(BOOL)hasWeekday
{
    NSArray *array = [fromDate componentsSeparatedByCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    array = [array filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"SELF != ''"]];

    NSString *month = @"";
    NSString *date = @"";
    NSString *day = @"";
    NSString *year = @"";
    switch ([array count]) {
        case 1:     // should NEVER run
        {
            month = @"JUN";
            day   = @"10";
            year  = @"";
            break;
        }
        case 2:     // should be month and day
        {
            month = array[0];
            day   = array[1];
            break;
        }
        case 3:     // should be month day and year
        {
            day = array[0];
            month   = array[1];
            date  = array[2];
            NSLog(@"Day:%@ Month:%@ date:%@", day, month, date);
            break;
        }
        default:
            break;
    }
    
    if ([month hasPrefix:@"Jan"]) month = @"JANUARY";
    else if ([month hasPrefix:@"Feb"]) month = @"FEBRUARY";
    else if ([month hasPrefix:@"Mar"]) month = @"MARCH";
    else if ([month hasPrefix:@"Apr"]) month = @"APRIL";
    else if ([month hasPrefix:@"May"]) month = @"MAY";
    else if ([month hasPrefix:@"Jun"]) month = @"JUNE";
    else if ([month hasPrefix:@"Jul"]) month = @"JULY";
    else if ([month hasPrefix:@"Aug"]) month = @"AUGUST";
    else if ([month hasPrefix:@"Sept"]) month = @"SEPTEMBER";
    else if ([month hasPrefix:@"Oct"]) month = @"OCTOBER";
    else if ([month hasPrefix:@"Nov"]) month = @"NOVEMBER";
    else if ([month hasPrefix:@"Dec"]) month = @"DECEMBER";

    if ([day hasPrefix:@"Mon"]) day = @"MONDAY";
    else if ([day hasPrefix:@"Tue"]) day = @"TUESDAY";
    else if ([day hasPrefix:@"Wed"]) day = @"WEDNESDAY";
    else if ([day hasPrefix:@"Thu"]) day = @"THURSDAY";
    else if ([day hasPrefix:@"Fri"]) day = @"FRIDAY";
    else if ([day hasPrefix:@"Sat"]) day = @"SATURDAY";
    else if ([day hasPrefix:@"Sun"]) day = @"SUNDAY";
    
    if ([date hasPrefix:@"0"]) date = [date substringFromIndex:1];

    NSString *finalDate = @"";
    if (hasWeekday)
        finalDate = [NSString stringWithFormat:@"%@, %@ %@", day, month, date];
    else
        finalDate = [NSString stringWithFormat:@"%@ %@", month, date];
    return finalDate;
}

@end
