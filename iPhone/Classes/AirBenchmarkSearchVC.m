//
//  AirBenchmarkSearchVC.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 08/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItinDetailCell.h"
#import "BookingCellData.h"
#import "HotelLocationViewController.h"
#import "LocationResult.h"
#import "DateEditVC.h"
#import "AirBenchmarkData.h"
#import "AirBenchmarkResultsVC.h"
#import "AirBenchmarkSearchVC.h"
#import "UserConfig.h"

@interface AirBenchmarkSearchVC ()

@property (nonatomic, strong) NSMutableArray *tableRowCellsData;
@property (nonatomic) BOOL isRoundTrip;
@property (nonatomic, strong) DateTimePopoverVC *pickerPopOverVC;

@end

@implementation AirBenchmarkSearchVC

- (instancetype)initWithTitle:(NSString*)title
{
    AirBenchmarkSearchVC *vc = [[UIStoryboard storyboardWithName:@"TravelPoints" bundle:nil] instantiateViewControllerWithIdentifier:@"AirBenchmarkSearchVC"];
    vc.navigationItem.title = title;
    return vc;
}

- (IBAction)changedOneWayRoundTripValue:(UISegmentedControl *)sender
{
    self.isRoundTrip = sender.selectedSegmentIndex;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {// If iOS 7
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
    
    self.isRoundTrip = YES;
    [self.segmentTripDirection setTitle:[@"One Way" localize] forSegmentAtIndex:0];
    [self.segmentTripDirection setTitle:[@"Round Trip" localize] forSegmentAtIndex:1];
    self.segmentTripDirection.selectedSegmentIndex = self.isRoundTrip ? 1 : 0;
    [self createTableData];
    [self makeSearchButton];
    if ([UIDevice isPad]) {
        UIBarButtonItem *closeButton = [[UIBarButtonItem alloc] initWithTitle:[@"Close" localize] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
        self.navigationItem.leftBarButtonItem = closeButton;
    }
    
    if([[UserConfig getSingleton].travelPointsConfig[@"AirTravelPointsEnabled"] boolValue])
        self.lblHeader.text = [@"PRICE_TO_BEAT_AIR_SEARCH_HEADER" localize];
    else
        self.lblHeader.text = [@"PRICE_TO_BEAT_DISABLED_AIR_SEARCH_HEADER" localize];
    
    self.title = [@"Air Price to Beat" localize];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.lblHeader sizeToFit];
    CGFloat tableHeightOffset = self.lblHeader.frame.origin.y + self.lblHeader.frame.size.height + 8 - self.tableView.frame.origin.y;
    self.tableView.frame = CGRectMake(self.tableView.frame.origin.x, self.lblHeader.frame.origin.y + self.lblHeader.frame.size.height + 8, self.tableView.frame.size.width, self.tableView.frame.size.height - tableHeightOffset);
}

- (void)makeSearchButton
{
    UIBarButtonItem *searchButton = [[UIBarButtonItem alloc] initWithTitle:[@"Search" localize] style:UIBarButtonItemStyleBordered target:self action:@selector(searchButtonClicked:)];
    self.navigationItem.rightBarButtonItem = searchButton;
}

- (void)closeView:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

-(void)searchButtonClicked:(id)sender
{
    BOOL isRoundTrip = self.segmentTripDirection.selectedSegmentIndex;
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW",  nil];
    for (BookingCellData *bcd in self.tableRowCellsData)
    {
        if (![bcd.val length]) {
            MobileAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Error"]
                                  message:[@"Required Fields Missing" localize]
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
            [alert show];
            return;
        }
        if ([bcd.cellID isEqualToString:@"From"]) {
            pBag[@"StartIata"] = bcd.val2;
        }
        else if ([bcd.cellID isEqualToString:@"To"]) {
            pBag[@"EndIata"] = bcd.val2;
        }
        else if ([bcd.cellID isEqualToString:@"DepartureDate"]) {
            pBag[@"Date"] = [DateTimeFormatter formatDateYYYYMMddByDate:bcd.dateValue];
        }
    }
    pBag[@"isRound"] = @(isRoundTrip);
    self.navigationItem.rightBarButtonItem = nil;
    [self showLoadingViewWithText:[@"Loading Data" localize]];
	[[ExSystem sharedInstance].msgControl createMsg:AIR_BENCHMARK_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


-(void)createTableData
{
	self.tableRowCellsData = [[NSMutableArray alloc] initWithObjects:nil];
    
	BookingCellData *bcd = [[BookingCellData alloc]init];
	bcd.cellID = @"From";
	bcd.lbl = [Localizer getLocalizedText:@"Departure City"];
	bcd.val = @"";
	bcd.isDisclosure = YES;
	bcd.isDetailLocation = YES;
	[self.tableRowCellsData addObject:bcd];
	
	bcd = [[BookingCellData alloc]init];
	bcd.cellID = @"To";
	bcd.lbl = [Localizer getLocalizedText:@"Arrival City"];
	bcd.val = @"";
	bcd.isDisclosure = YES;
	bcd.isDetailLocation = YES;
	[self.tableRowCellsData addObject:bcd];
	
	//Departure date
	bcd = [[BookingCellData alloc] init];
	bcd.cellID = @"DepartureDate";
	bcd.lbl = [Localizer getLocalizedText:@"Departure Date"];
    bcd.dateValue = [DateTimeFormatter getDateWithoutTimeInGMT:[DateTimeFormatter getCurrentLocalDateTimeInGMT]];// [NSDate date];
    bcd.val = [DateTimeFormatter formatDateForBooking:bcd.dateValue];
	[self.tableRowCellsData addObject:bcd];
}

-(void) respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:AIR_BENCHMARK_DATA])
    {
        [self makeSearchButton];
        [self hideLoadingView];
        if (self.navigationController.visibleViewController == self)
        { //Only show results if the VC is still displayed
            NSDictionary *logDict = [NSDictionary dictionaryWithObjectsAndKeys:msg.parameterBag[@"StartIata"], @"Departure Location", msg.parameterBag[@"EndIata"], @"Arrival Location", msg.parameterBag[@"Date"], @"Departure Date", nil];
            if ([(AirBenchmarkData*)msg.responder isSuccess])
            {
                [self performSegueWithIdentifier:@"displayAirBenchmarkPrice" sender:msg];
                [Flurry logEvent:@"Price-to-Beat: Air Price-to-Beat Results Viewed" withParameters:logDict];
            }
            else
            {
                NSString *message = [[(AirBenchmarkData*)msg.responder message] length] ? [(AirBenchmarkData*)msg.responder message] : [@"Sorry, we did not find a Price to Beat. Try searching again." localize];
                MobileAlertView *alertView = [[MobileAlertView alloc] initWithTitle:[@"No Results" localize] message:message delegate:nil cancelButtonTitle:[@"LABEL_CLOSE_BTN" localize] otherButtonTitles:nil];
                [alertView show];
                [Flurry logEvent:@"Price-to-Beat: Air Price-to-Beat Results Not Found" withParameters:logDict];
            }
        }
    }
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"displayAirBenchmarkPrice"]) {
        AirBenchmarkResultsVC *vc = segue.destinationViewController;
        vc.benchmarkData = [(AirBenchmarkData*)[(Msg*)sender responder] benchmark];
        vc.fromAirportFullName = [(BookingCellData*)self.tableRowCellsData[0] val];
        vc.toAirportFullName = [(BookingCellData*)self.tableRowCellsData[1] val];
    }
}

#pragma mark TableView Datasource methods
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.tableRowCellsData.count;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    BookingCellData *bcd = self.tableRowCellsData[indexPath.row];
    ItinDetailCell *cell = (ItinDetailCell*)[tableView dequeueReusableCellWithIdentifier:@"ItinDetailCell"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[ItinDetailCell class]])
                cell = (ItinDetailCell *)oneObject;
    }
    
    cell.lblLabel.text = bcd.lbl;
    
    cell.lblValue.text = bcd.val;
    cell.ivDot.hidden = YES;
    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    
    return cell;
}

#pragma mark TableView Delegate methods
- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    if (section==0)
        return self.viewContainingTripDirectionSegment;
    return nil;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    if (section == 0)
        return 50;
    return 1;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    BookingCellData *bcd = self.tableRowCellsData[indexPath.row];
    
    if([bcd.cellID isEqualToString:@"From"] || [bcd.cellID isEqualToString:@"To"])
    {
        HotelLocationViewController * vc = [[HotelLocationViewController alloc] initWithNibName:@"HotelLocationViewController" bundle:nil];
        vc.locationDelegate = self;
        vc.neverShowOffices = YES;
        vc.isAirportOnly = YES;
        vc.tag = [NSString stringWithFormat:@"%ld",(long)indexPath.row]; // send selected row as Tag so we know which value to change later when user selects a location
        if([UIDevice isPad])
        {
            if([ExSystem is6Plus])
                vc.modalPresentationStyle = UIModalPresentationCurrentContext;
            else
                vc.modalPresentationStyle = UIModalPresentationFormSheet;
            [self presentViewController:vc animated:YES completion:nil];
        }
        else
        {
            // Finds generic homeVC
            [[ConcurMobileAppDelegate findHomeVC] presentViewController:vc animated:YES completion:nil];
        }
    }
    else if ([bcd.cellID isEqualToString:@"DepartureDate"])
    {
        if ([UIDevice isPad])
        {
            [self pickerDateTapped:self IndexPath:indexPath];
        }
        else
        {
            DateEditVC *dvc = [[DateEditVC alloc] initWithNibName:@"DateEditVC" bundle:nil];
            dvc.context = @(indexPath.row);
            dvc.date = bcd.dateValue;
            dvc.viewTitle = bcd.lbl;
            dvc.delegate = self;
            [dvc view];
            [self.navigationController pushViewController:dvc animated:YES];
            dvc.datePicker.minimumDate = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
        }
    }
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

#pragma mark Location Delegate methods

-(void)locationSelected:(LocationResult *)locationResult tag:(NSString *)tag
{
    int selectedRow = [tag intValue]; // tag was previously sent the user's selected row index
    BookingCellData *bcd = self.tableRowCellsData[selectedRow];
    bcd.val = locationResult.location;
    bcd.val2 = locationResult.iataCode;
    bcd.values = [NSMutableArray arrayWithObject:locationResult.countryAbbrev];
    [self.tableView reloadData];
}

#pragma mark PopOver Methods
- (void)pickerDateTapped:(id)sender IndexPath:(NSIndexPath *)indexPath
{
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
	
	
	self.pickerPopOverVC = [[DateTimePopoverVC alloc] initWithNibName:@"DateTimePopoverVC" bundle:nil];
	self.pickerPopOverVC.isDate = YES;
	self.pickerPopOverVC.delegate = self;
	self.pickerPopOverVC.indexPath = indexPath;
	
    BookingCellData *bcd = self.tableRowCellsData[indexPath.row];
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:self.pickerPopOverVC];
	[self.pickerPopOverVC initDate:bcd.dateValue];
    
	//MOB-3813, only allow one year out and make minimum date today
	self.pickerPopOverVC.datePicker.maximumDate = [NSDate dateWithTimeIntervalSinceNow:(60.0 * 60.0 * 24.0 * 365.0)];
    NSDate* now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
	self.pickerPopOverVC.datePicker.minimumDate = now;
    
	CGRect cellRect = [self.tableView rectForRowAtIndexPath:indexPath];
	CGRect myRect = [self.view convertRect:cellRect fromView:self.tableView];
	
    [self.pickerPopOver presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionLeft animated:YES];
}

- (void)cancelPicker
{
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
}


- (void)donePicker:(NSDate *)dateSelected
{
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
}

- (void)pickedDate:(NSDate *)dateSelected
{
    BookingCellData *bcdDate = self.tableRowCellsData[self.pickerPopOverVC.indexPath.row];
    bcdDate.dateValue = dateSelected;
    bcdDate.val = [DateTimeFormatter formatDateForBooking:bcdDate.dateValue];
    [self.tableView cellForRowAtIndexPath:self.pickerPopOverVC.indexPath];
    [self.tableView reloadData];
}

- (void)dateSelected:(NSObject *)context withValue:(NSDate *)date
{
    BookingCellData *bcdDate = self.tableRowCellsData[[(NSNumber*)context intValue]];
    bcdDate.dateValue = date;
    bcdDate.val = [DateTimeFormatter formatDateForBooking:bcdDate.dateValue];
    [self.tableView reloadData];
}

@end
