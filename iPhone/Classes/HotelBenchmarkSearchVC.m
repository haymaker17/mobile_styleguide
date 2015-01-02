//
//  HotelBenchmarkSearchVC.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 16/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelBenchmarkSearchVC.h"
#import "ItinDetailCell.h"
#import "HotelLocationViewController.h"
#import "LocationResult.h"
#import "DistanceViewController.h"
#import "PickerViewController.h"
#import "HotelBenchmarkResultsVC.h"
#import "HotelBenchmarkData.h"
#import "UserConfig.h"
#import "NSStringAdditions.h"

@interface HotelBenchmarkSearchVC ()

@property (nonatomic, strong) LocationResult *locationResult;
@property (nonatomic, strong) NSArray *monthSymbols;
@property (nonatomic) int selectedMonth;
@property (nonatomic) int radius;
@property (nonatomic) BOOL isMetricDistance;

@end

@implementation HotelBenchmarkSearchVC

- (instancetype)initWithTitle:(NSString *)title
{
    HotelBenchmarkSearchVC *vc = [[UIStoryboard storyboardWithName:@"TravelPoints" bundle:nil] instantiateViewControllerWithIdentifier:@"HotelBenchmarkSearchVC"];
    vc.navigationItem.title = title;
    return vc;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {// If iOS 7
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
    
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    formatter.locale = [NSLocale currentLocale];
    self.monthSymbols = formatter.monthSymbols;
    self.selectedMonth = (int) [[[[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar] components:NSCalendarUnitMonth fromDate:[NSDate date]] month]; // select current month by default
    self.radius = 5;
    
    if([[UserConfig getSingleton].travelPointsConfig[@"HotelTravelPointsEnabled"] boolValue])
        self.lblHeader.text = [@"PRICE_TO_BEAT_HOTEL_SEARCH_HEADER" localize];
    else
        self.lblHeader.text = [@"PRICE_TO_BEAT_DISABLED_HOTEL_SEARCH_HEADER" localize];
    
    [self makeSearchButton];
    if ([UIDevice isPad]) {
        UIBarButtonItem *closeButton = [[UIBarButtonItem alloc] initWithTitle:[@"Close" localize] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
        self.navigationItem.leftBarButtonItem = closeButton;
    }
}

- (void)makeSearchButton
{
    UIBarButtonItem *searchButton = [[UIBarButtonItem alloc] initWithTitle:[@"Search" localize] style:UIBarButtonItemStyleBordered target:self action:@selector(searchButtonClicked:)];
    self.navigationItem.rightBarButtonItem = searchButton;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self.lblHeader sizeToFit];
    CGFloat tableHeightOffset = self.lblHeader.frame.origin.y + self.lblHeader.frame.size.height + 8 - self.tableView.frame.origin.y;
    self.tableView.frame = CGRectMake(self.tableView.frame.origin.x, self.lblHeader.frame.origin.y + self.lblHeader.frame.size.height + 8, self.tableView.frame.size.width, self.tableView.frame.size.height - tableHeightOffset);
}

- (void)closeView:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:HOTEL_BENCHMARK_DATA]) {
        [self makeSearchButton];
        [self hideLoadingView];
        NSDictionary *logDict = [NSDictionary dictionaryWithObjectsAndKeys:(self.locationResult.location ?: @""), @"Location", [self getDistanceString], @"Distance", [@(self.selectedMonth) stringValue], @"Month of Stay", nil];
        if (self.navigationController.visibleViewController == self) { //Only show results if the VC is still displayed
            if ([(HotelBenchmarkData*)msg.responder isSuccess] && [[(HotelBenchmarkData*)msg.responder benchmarksList] count]) {
                [self performSegueWithIdentifier:@"displayHotelBenchmarkPrice" sender:msg];
                [Flurry logEvent:@"Price-to-Beat: Hotel Price-to-Beat Results Viewed" withParameters:logDict];
            }
            else {
                NSString *message = [[(HotelBenchmarkData*)msg.responder message] length] ? [(HotelBenchmarkData*)msg.responder message] : [@"Sorry, we did not find a Price to Beat. Try searching again." localize];
                MobileAlertView *alertView = [[MobileAlertView alloc] initWithTitle:[@"No Results" localize] message:message delegate:nil cancelButtonTitle:[@"LABEL_CLOSE_BTN" localize] otherButtonTitles:nil];
                [alertView show];
                [Flurry logEvent:@"Price-to-Beat: Hotel Price-to-Beat Results Not Found" withParameters:logDict];
            }
        }
    }
    else if ((msg.parameterBag)[@"DISTANCE_VALUE"] != nil && (msg.parameterBag)[@"IS_METRIC_DISTANCE"] != nil)
    {
        self.radius = [(NSNumber*)(msg.parameterBag)[@"DISTANCE_VALUE"] intValue];
        self.isMetricDistance = [(NSNumber*)(msg.parameterBag)[@"IS_METRIC_DISTANCE"] boolValue];
        [self.tableView reloadData];
    }
}

-(void)searchButtonClicked:(id)sender
{
    if (!self.locationResult || !self.selectedMonth) {
            MobileAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:[Localizer getLocalizedText:@"Error"]
                                      message:[@"Required Fields Missing" localize]
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                      otherButtonTitles:nil];
            [alert show];
            return;
    }
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW",  nil];
    pBag[@"Lat"] = self.locationResult.latitude;
    pBag[@"Lon"] = self.locationResult.longitude;
    pBag[@"MonthOfStay"] = [@(self.selectedMonth) stringValue];
    pBag[@"Radius"] = @(self.radius);
    pBag[@"Scale"] = self.isMetricDistance ? @"K" : @"M";
    
    self.navigationItem.rightBarButtonItem = nil;
    [self showLoadingViewWithText:[@"Loading Data" localize]];
	[[ExSystem sharedInstance].msgControl createMsg:HOTEL_BENCHMARK_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 3;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 1;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    ItinDetailCell *cell = (ItinDetailCell*)[tableView dequeueReusableCellWithIdentifier:@"ItinDetailCell"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[ItinDetailCell class]])
                cell = (ItinDetailCell *)oneObject;
    }
    
    cell.ivDot.hidden = YES;
    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    if (indexPath.row == 0) //kSectionLocation == section
    {
        cell.lblLabel.text = [Localizer getLocalizedText:@"HOTEL_VIEW_LOCATION"];
        cell.lblValue.text = self.locationResult.location ?: @"";
    }
    else if (indexPath.row == 1)
    {
        cell.lblLabel.text = [@"Month of stay" localize];
        cell.lblValue.text = self.selectedMonth > 0 && self.selectedMonth <= 12 ? self.monthSymbols[self.selectedMonth - 1] : @"";// month;
    }
    else if (indexPath.row == 2)
    {
        cell.lblLabel.text = [Localizer getLocalizedText:@"HOTEL_VIEW_FIND_HOTELS_WITHIN"];
        cell.lblValue.text = [self getDistanceString];
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    }
    
    return cell;
}

- (NSString *)getDistanceString
{
    NSString* unit  = (self.isMetricDistance ? [@"HOTEL_VIEW_KILOMETERS" localize] : [@"HOTEL_VIEW_MILE" localize]);
    NSString* units = (self.isMetricDistance ? [@"HOTEL_VIEW_KILOMETERS" localize] : [@"miles" localize]);
    
    int iDistanceValue = self.radius;
    NSString* distanceValueString = (iDistanceValue == 100) ? [@"Greater than 25" localize] : [@(self.radius) stringValue];
    return [NSString stringWithFormat:[@"HOTEL_VIEW_NUMBER_SPACE_KILOMETERS_OR_MILES" localize], distanceValueString, (iDistanceValue > 1 ? units : unit)];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0)// kSectionLocation == section)
    {
        HotelLocationViewController * vc = [[HotelLocationViewController alloc] initWithNibName:@"HotelLocationViewController" bundle:nil];
        vc.locationDelegate = self;
        vc.neverShowOffices = NO;
        if (self.locationResult.location)
            vc.initialSearchLocation = self.locationResult;
        
        if([UIDevice isPad])
        {
            vc.modalPresentationStyle = UIModalPresentationFormSheet;
            [self presentViewController:vc animated:YES completion:nil];
        }
        else
        {
            [[ConcurMobileAppDelegate findHomeVC] presentViewController:vc animated:YES completion:nil];
        }
    }
    else if (indexPath.row == 1)
    {
        if ([UIDevice isPad])
            [self displayMonthPickerInPopoverFromRowAtIndexPath:indexPath];
        else
            [self performSegueWithIdentifier:@"displayMonthPicker" sender:indexPath];
    }
    else if (indexPath.row == 2) // Find hotel within X miles
    {
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@(self.radius), @"DISTANCE_VALUE", @(self.isMetricDistance), @"IS_METRIC_DISTANCE", @"YES", @"SHORT_CIRCUIT", nil];

        DistanceViewController *nextController = [[DistanceViewController alloc] initWithNibName:@"DistanceViewController" bundle:nil];
        Msg *msg = [[Msg alloc] init];
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        [nextController respondToFoundData:msg];
        [self.navigationController pushViewController:nextController animated:YES];
    }
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (void) prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"displayMonthPicker"])
    {
        PickerViewController *pvc = segue.destinationViewController;
        pvc.title = [@"Month of stay" localize];
        pvc.pickerViewOptionLabels = self.monthSymbols;
        pvc.pickerViewSelectedOptionIndex = self.selectedMonth == 0 ? 0 : self.selectedMonth - 1;
        pvc.delegate = self;
    }
    else if ([segue.identifier isEqualToString:@"displayHotelBenchmarkPrice"])
    {
        Msg *msg = sender;
        HotelBenchmarkData *benchmarkData = (HotelBenchmarkData*)msg.responder;
        HotelBenchmarkResultsVC *vc = segue.destinationViewController;
        vc.title = [@"Search Results" localize];
        vc.benchmarksList = benchmarkData.benchmarksList;
        vc.searchLocation = self.locationResult.location ?: @"";
        vc.monthOfStayString = self.monthSymbols[self.selectedMonth - 1];
        vc.distanceString = [self getDistanceString];
    }
}

- (void) displayMonthPickerInPopoverFromRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(self.pickerPopOver != nil)
		[self.pickerPopOver dismissPopoverAnimated:YES];
    
    PickerViewController *pvc = [[PickerViewController alloc] initAsPopover];
    pvc.pickerViewOptionLabels = self.monthSymbols;
    pvc.pickerViewSelectedOptionIndex = self.selectedMonth == 0 ? 0 : self.selectedMonth - 1;
    pvc.delegate = self;
    self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:pvc];
	CGRect cellRect = [self.tableView rectForRowAtIndexPath:indexPath];
	CGRect myRect = [self.view convertRect:cellRect fromView:self.tableView];
    self.pickerPopOver.popoverContentSize = CGSizeMake(320.0, 162.0);
    [self.pickerPopOver presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionLeft animated:YES];
}

- (void) locationSelected:(LocationResult *)locationResult tag:(NSString *)tag
{
    self.locationResult = locationResult;
    [self.tableView reloadData];
}

- (void) pickerSelectionChangedToRow:(int)row tag:(id)sender
{
    self.selectedMonth = row + 1;
    [self.tableView reloadData];
}

@end
