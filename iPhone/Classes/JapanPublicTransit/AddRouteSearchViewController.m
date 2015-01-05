//
//  AddRouteSearchViewController.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AddRouteSearchViewController.h"
#import "AnalyticsManager.h"
#import "ChooseDateViewController.h"
#import "ChooseSeatViewController.h"
#import "ChooseStationViewController.h"
#import "DCRoundSwitch.h"
#import "DateUtils.h"
#import "JPTUtils.h"
#import "Localizer.h"
#import "NavUtils.h"
#import "SearchResultsViewController.h"
#import "Station.h"
#import "TableUtils.h"

NSInteger const ROUTE_ATTRIBUTE_DATE = 0;
NSInteger const ROUTE_ATTRIBUTE_DEPARTURE_STATION = 1;
NSInteger const ROUTE_ATTRIBUTE_THROUGH_STATION_1 = 2;
NSInteger const ROUTE_ATTRIBUTE_THROUGH_STATION_2 = 3;
NSInteger const ROUTE_ATTRIBUTE_ARRIVAL_STATION = 4;
NSInteger const ROUTE_ATTRIBUTE_SEAT_TYPE = 5;

@interface AddRouteSearchViewController ()

@end

@implementation AddRouteSearchViewController

- (void)didMoveToParentViewController:(UIViewController *)parent {
    if (parent != nil) {
        [[AnalyticsManager sharedInstance] pushImpression:@"Search"];
        
        [[AnalyticsManager sharedInstance] logCategory:@"JPT"
                                              withName:@"Add Route"
                                               andType:@"Search"];
    } else {
        [[AnalyticsManager sharedInstance] popImpression:@"Search"];
        [[NSNotificationCenter defaultCenter] removeObserver:self];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        self.navigationItem.title = [Localizer getLocalizedText:@"route_search"];
        self.rightButtonText = [Localizer getLocalizedText:@"Search"];
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // If iOS 7
    //
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }

    self.doneButton = [[UIBarButtonItem alloc] initWithTitle:self.rightButtonText
                                                                   style:UIBarButtonItemStylePlain
                                                                  target:self
                                                                  action:@selector(didTapDone:)];
    
    [self.navigationItem setRightBarButtonItem:self.doneButton];

    self.roundTripToggle = [[DCRoundSwitch alloc] init];
    
    self.routeSearchModel = [[RouteSearchModel alloc] init];
    
    self.icCardFareToggle = [[DCRoundSwitch alloc] init];
    
    
    // Pre-populate.
    //
    self.routeSearchModel.stations = [[NSMutableArray alloc] initWithObjects:
                                      [Station empty],
                                      [Station none],
                                      [Station none],
                                      [Station empty],
                                      nil
                                      ];
    
    // Set up notification listeners.
    //
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateTripDate:)
                                                 name:@"TripDate" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateDepartureStationName:)
                                                 name:@"DepartureStation" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateArrivalStationName:)
                                                 name:@"ArrivalStation" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateThroughStation1:)
                                                 name:@"ThroughStation1" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateThroughStation2:)
                                                 name:@"ThroughStation2" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateSeatType:)
                                                 name:@"SeatType" object:nil];
}

- (void)viewDidUnload {
    [super viewDidUnload];
    
    [[AnalyticsManager sharedInstance] popImpression:@"Search"];
}

#pragma mark - Business logic

- (void)didTapDone:(id)sender {
    if ([self isFormComplete]) {
        
        // If not using through stations then blank out any data entered by user
        // before searching.
        //
        if (!self.usingThroughStations) {
            self.routeSearchModel.stations[1] = [Station none];
            self.routeSearchModel.stations[2] = [Station none];
        }
        
        self.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Back"]
                                                                                 style:UIBarButtonItemStylePlain
                                                                                target:nil
                                                                                action:nil];
        
        SearchResultsViewController *vc = [[SearchResultsViewController alloc] init];
        
        vc.routeSearchModel = self.routeSearchModel;
        
        [self.navigationController pushViewController:vc animated:YES];
    } else {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil
                                                        message:[Localizer getLocalizedText:@"Please enter values for required fields, in red, before saving."]
                                                       delegate:nil
                                              cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                              otherButtonTitles:nil];
        
        [alert show];
    }
}

// If any of the required fields are blank then don't let user proceed.
//
- (BOOL)isFormComplete {
    Station *firstStation = [self.routeSearchModel.stations objectAtIndex:0];
    if (firstStation.key == nil) {
        return NO;
    }
    
    Station *lastStation = [self.routeSearchModel.stations lastObject];
    if (lastStation.key == nil) {
        return NO;
    }
    
    return YES;
}

- (UITableViewCell *)tableView:(UITableView *)tableView stationCellForRow:(NSUInteger)row {
    UITableViewCell *cell = nil;
    
    switch (row) {
        case 0:
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"departure_station"]
                                             andDetail:[self.routeSearchModel firstStationName]
                                              forTable:tableView
                                              required:YES];
            break;
        case 1:
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"arrival_station"]
                                             andDetail:[self.routeSearchModel lastStationName]
                                              forTable:tableView
                                              required:YES];
            break;
    }
    
    return cell;
}

- (UITableViewCell *)tableView:(UITableView *)tableView throughStationForRow:(NSUInteger)row {
    UITableViewCell *cell = nil;
    
    if (row == 0) {
        cell = [TableUtils toggleCellWithLabel:[Localizer getLocalizedText:@"through_stations"]
                                      forTable:tableView
                                     withValue:self.usingThroughStations
                                      onTarget:self
                                   andSelector:@selector(toggleThroughStationSection:)];
    } else {
        NSString *stationName;
        
        if (row == 1) {
            stationName = self.routeSearchModel.firstThroughStationName;
        } else if (row == 2) {
            stationName = self.routeSearchModel.secondThroughStationName;
        }
        
        cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"through_station"]
                                         andDetail:stationName
                                          forTable:tableView
                                          required:NO];
    }
    
    return cell;
}

#pragma mark - NSNotification Handlers

- (void)toggleThroughStationSection:(id)sender {
    self.usingThroughStations = !self.usingThroughStations;

    if (self.usingThroughStations) {
        [self.tableView beginUpdates];
        
        NSIndexPath *throughStation1 = [NSIndexPath indexPathForRow:1 inSection:2];
        NSIndexPath *throughStation2 = [NSIndexPath indexPathForRow:2 inSection:2];
        
        [self.tableView insertRowsAtIndexPaths:@[throughStation1, throughStation2]
                              withRowAnimation:UITableViewRowAnimationTop];
        
        [self.tableView endUpdates];
    } else {
        [self.tableView beginUpdates];
        
        NSIndexPath *throughStation1 = [NSIndexPath indexPathForRow:1 inSection:2];
        NSIndexPath *throughStation2 = [NSIndexPath indexPathForRow:2 inSection:2];
        
        [self.tableView deleteRowsAtIndexPaths:@[throughStation1, throughStation2]
                              withRowAnimation:UITableViewRowAnimationFade];
        
        [self.tableView endUpdates];
    }
}

-(void)updateTripDate:(NSNotification *)notification {
    self.routeSearchModel.date = [notification object];

    [self.tableView reloadData];
}

- (void)updateTripType:(id)sender {
    if ([TableUtils isSwitchControl:sender]) {
        self.routeSearchModel.isRoundTrip = [TableUtils switchValue:sender];
    }
}

- (void)updateIcCardFare:(id)sender {
    if ([TableUtils isSwitchControl:sender]) {
        self.routeSearchModel.isIcCardFare = [TableUtils switchValue:sender];
    }
}

-(void)updateArrivalStationName:(NSNotification *)notification {
    [self.routeSearchModel.stations setObject:[notification object] atIndexedSubscript:3];

    [self.tableView reloadData];
}

-(void)updateDepartureStationName:(NSNotification *)notification {
    [self.routeSearchModel.stations setObject:[notification object] atIndexedSubscript:0];

    [self.tableView reloadData];
}

-(void)updateSeatType:(NSNotification *)notification {
    NSNumber *n = [notification object];
    
    self.routeSearchModel.seatType = [n intValue];

    [self.tableView reloadData];
}

-(void)updateThroughStation1:(NSNotification *)notification {
    [self.routeSearchModel.stations setObject:[notification object] atIndexedSubscript:1];
    
    [self.tableView reloadData];
}

-(void)updateThroughStation2:(NSNotification *)notification {
    [self.routeSearchModel.stations setObject:[notification object] atIndexedSubscript:2];
    
    [self.tableView reloadData];
}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 6;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell;
    
    NSInteger section = [indexPath section];
    NSInteger row = [indexPath row];
    
    switch (section) {
        case 0: {
            NSString *detail = [DateUtils dateFormattedForUI:self.routeSearchModel.date];
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"trip_date"]
                                             andDetail:detail
                                              forTable:tableView
                                              required:YES];
            break;
        }
        case 1:
            cell = [self tableView:tableView stationCellForRow:row];
            break;
        case 2: {
            cell = [self tableView:tableView throughStationForRow:row];
            break;
        }
        case 3: {
            NSString *detail = [JPTUtils labelForSeatType:self.routeSearchModel.seatType];
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"seat_type"]
                                             andDetail:detail
                                              forTable:tableView
                                              required:NO];
            break;
        }
        case 4:
            cell = [TableUtils toggleCellWithLabel:[Localizer getLocalizedText:@"Round Trip"]
                                          forTable:tableView
                                         withValue:self.routeSearchModel.isRoundTrip
                                          onTarget:self
                                       andSelector:@selector(updateTripType:)];
            
            break;
        case 5:
            cell = [TableUtils toggleCellWithLabel:[Localizer getLocalizedText:@"ic_card_fare"]
                                          forTable:tableView
                                         withValue:self.routeSearchModel.isIcCardFare
                                          onTarget:self
                                       andSelector:@selector(updateIcCardFare:)];
            
            break;
    }

    return cell;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSInteger numRows = 0;
    
    switch (section) {
        case 0:
            numRows = 1;
            break;
        case 1:
            numRows = 2;
            break;
        case 2:
            if (self.usingThroughStations) {
                numRows = 3;
            } else {
                numRows = 1;
            }
            break;
        case 3:
            numRows = 1;
            break;
        case 4:
            numRows = 1;
            break;
        case 5:
            numRows = 1;
        default:
            break;
    }
    
    return numRows;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    NSInteger section = [indexPath section];
    NSInteger row = [indexPath row];
    
    switch (section) {
        case 0:
            [NavUtils gotoDateChooserWithDate:self.routeSearchModel.date
                                     andTitle:[Localizer getLocalizedText:@"Date"]
                           fromViewController:self];
            break;
        case 1: {
            if (row == 0) {
                [NavUtils gotoStationChooserWithTitle:[Localizer getLocalizedText:@"Station"]
                                   fromViewController:self
                                 withNotificationName:@"DepartureStation"];
            } else if (row == 1) {
                [NavUtils gotoStationChooserWithTitle:[Localizer getLocalizedText:@"Station"]
                                   fromViewController:self
                                 withNotificationName:@"ArrivalStation"];
            }
            break;
        }
        case 2:
            if (row == 1) {
                [NavUtils gotoStationChooserWithTitle:[Localizer getLocalizedText:@"Station"]
                                   fromViewController:self
                                 withNotificationName:@"ThroughStation1"];
            } else if (row == 2) {
                [NavUtils gotoStationChooserWithTitle:[Localizer getLocalizedText:@"Station"]
                                   fromViewController:self
                                 withNotificationName:@"ThroughStation2"];
            }
            break;
        case 3: {
            [NavUtils gotoSeatChooserWithTitle:[Localizer getLocalizedText:@"seat_type"]
                            fromViewController:self];
            break;
        }
        default:
            break;
    }
    
}

@end
