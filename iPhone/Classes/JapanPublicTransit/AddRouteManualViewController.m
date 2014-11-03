//
//  AddRouteManualViewController.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AddRouteExpenseToReportRequestFactory.h"
#import "AddRouteManualViewController.h"
#import "AnalyticsManager.h"
#import "ChooseDateViewController.h"
#import "ChooseLineViewController.h"
#import "ChooseNumberViewController.h"
#import "ChooseStationViewController.h"
#import "ChooseTextViewController.h"
#import "DateUtils.h"
#import "FirstViewController.h"
#import "JPTUtils.h"
#import "Line.h"
#import "Localizer.h"
#import "NavUtils.h"
#import "ReportDetailViewController.h"
#import "RouteExpenseManager.h"
#import "RouteManager.h"
#import "SelectReportViewController.h"
#import "Station.h"
#import "TableUtils.h"

@interface AddRouteManualViewController ()

@end

@implementation AddRouteManualViewController

- (void)didMoveToParentViewController:(UIViewController *)parent {
    if (parent != nil) {
        [[AnalyticsManager sharedInstance] pushImpression:@"Manual"];
        
        [[AnalyticsManager sharedInstance] logCategory:@"JPT"
                                              withName:@"Add Route"
                                               andType:@"Manual"];
    } else {
        [[AnalyticsManager sharedInstance] popImpression:@"Manual"];
        [[NSNotificationCenter defaultCenter] removeObserver:self];
    }
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        self.routeExpense = [[RouteExpense alloc] init];
        
        self.routeExpense.route.entryType = @"MANL";
        
        [self.routeExpense.route.segments addObject:[[Segment alloc] init]];
        [self.routeExpense.route.segments addObject:[[Segment alloc] init]];
        
        self.navigationItem.title = [Localizer getLocalizedText:@"add_new_route"];
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self setupNotificationListeners];
}

- (void)viewDidUnload {
    [super viewDidUnload];
    
    [[AnalyticsManager sharedInstance] popImpression:@"Manual"];
}

#pragma mark - Business logic

- (BOOL)canSaveFavorite {
    return YES;
}

- (void)arrivalCommuterPassToggled:(id)sender {
    if ([TableUtils isSwitchControl:sender]) {
        Segment *lastSegment = [self.routeExpense.route lastSegment];
        
        lastSegment.toIsCommuterPass = [TableUtils switchValue:sender];
    }
}

- (void)departureCommuterPassToggled:(id)sender {
    if ([TableUtils isSwitchControl:sender]) {
        Segment *firstSegment = [self.routeExpense.route firstSegment];
        
        firstSegment.fromIsCommuterPass = [TableUtils switchValue:sender];
    }
}

- (void)isFavoriteToggled:(id)sender {
    if ([TableUtils isSwitchControl:sender]) {
        self.routeExpense.isFavorite = [TableUtils switchValue:sender];
    }
}

- (BOOL)isFormComplete {
    if (self.routeExpense.route.firstSegment.fare == 0) {
        return NO;
    }
    
    if (self.routeExpense.route.firstSegment.fromStation.name == nil) {
        return NO;
    }
    
    if (self.routeExpense.route.firstSegment.line.name == nil) {
        return NO;
    }
    
    if (self.routeExpense.route.lastSegment.toStation.name == nil) {
        return NO;
    }
    
    if (self.routeExpense.route.lastSegment.line.name == nil) {
        return NO;
    }
    
    return YES;
}

// Doing a little data massage dance here. Doing this because we can't currently show
// a RouteView with two segments AND two lines. UX is just not set up to handle that
// case. So we squash the route down into one segment here for persistence and
// display purposes.
//
- (void)onSaveTapped:(id)sender {
    if ([self isFormComplete]) {
        Segment *departureSegment = [self.routeExpense.route.segments objectAtIndex:0];
        Segment *arrivalSegment = [self.routeExpense.route.segments objectAtIndex:1];
        
        Segment *segment = [[Segment alloc] init];
        
        segment.fromStation = departureSegment.fromStation;
        segment.toStation = arrivalSegment.toStation;
        segment.line = departureSegment.line;
        segment.fare = departureSegment.fare;
        segment.additionalCharge = departureSegment.additionalCharge;
        segment.fromIsCommuterPass = departureSegment.fromIsCommuterPass;
        segment.toIsCommuterPass = arrivalSegment.toIsCommuterPass;
        
        [self.routeExpense.route.segments removeAllObjects];
        
        [self.routeExpense.route.segments addObject:segment];
    }
    
    [super onSaveTapped:sender];
}

- (void)personalExpenseToggled:(id)sender {
    if ([TableUtils isSwitchControl:sender]) {
        self.routeExpense.isPersonalExpense = [TableUtils switchValue:sender];
    }
}

- (void)promptUserToEnterArrivalStation {
    NSString *errorMessageKey = @"An invalid station for arrival has been specified.  Please select a station to arrive at.";
    
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil
                                                    message:[Localizer getLocalizedText:errorMessageKey]
                                                   delegate:nil
                                          cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                          otherButtonTitles:nil];
    
    [alert show];
}

- (void)promptUserToEnterDepartureStation {
    NSString *errorMessageKey = @"An invalid station for departure has been specified.  Please select a station to depart from.";
    
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil
                                                    message:[Localizer getLocalizedText:errorMessageKey]
                                                   delegate:nil
                                          cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                          otherButtonTitles:nil];
    
    [alert show];
}

- (void)roundTripToggled:(id)sender {
    if ([TableUtils isSwitchControl:sender]) {
        self.routeExpense.route.isRoundTrip = [TableUtils switchValue:sender];
    }
}

- (void)setupNotificationListeners {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateTripDate:)
                                                 name:@"TripDate" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateDepartureStationName:)
                                                 name:@"DepartureStation" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateDepartureLine:)
                                                 name:@"DepartureLine" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateArrivalStationName:)
                                                 name:@"ArrivalStation" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateArrivalLine:)
                                                 name:@"ArrivalLine" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateCost:)
                                                 name:@"Cost" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateAdditionalCharge:)
                                                 name:@"AdditionalCharge" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateBusinessPurpose:)
                                                 name:@"BusinessPurpose" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateComments:)
                                                 name:@"Comments" object:nil];
}

#pragma mark - Business

- (UITableViewCell *)tableView:(UITableView *)tableView arrivalStationSectionCellForRow:(NSUInteger)row {
    UITableViewCell *cell = nil;
    
    switch (row) {
        case 0: {
            Segment *segment = [self.routeExpense.route.segments objectAtIndex:1];
            
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"Station"]
                                             andDetail:segment.toStation.name
                                              forTable:tableView
                                              required:YES];
            
            break;
        }
        case 1: {
            Segment *segment = [self.routeExpense.route.segments objectAtIndex:1];

            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"line"]
                                             andDetail:segment.line.name
                                              forTable:tableView
                                              required:YES];
            
            break;
        }
        case 2: {
            Segment *segment = [self.routeExpense.route.segments objectAtIndex:1];
            
            cell = [TableUtils toggleCellWithLabel:[Localizer getLocalizedText:@"commuter_pass"]
                                    forTable:tableView
                                   withValue:segment.toIsCommuterPass
                                          onTarget:self
                                 andSelector:@selector(arrivalCommuterPassToggled:)];
            break;
        }
    }
    
    return cell;
}

- (UITableViewCell *)tableView:(UITableView *)tableView departureStationSectionCellForRow:(NSUInteger)row {
    UITableViewCell *cell = nil;
    
    switch (row) {
        case 0: {
            Segment *segment = [self.routeExpense.route.segments objectAtIndex:0];
            
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"Station"]
                                             andDetail:segment.fromStation.name
                                              forTable:tableView
                                              required:YES];
            
            break;
        }
        case 1: {
            Segment *segment = [self.routeExpense.route.segments objectAtIndex:0];
            
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"line"]
                                             andDetail:segment.line.name
                                              forTable:tableView
                                              required:YES];
            
            break;
        }
        case 2: {
            Segment *segment = [self.routeExpense.route.segments objectAtIndex:0];
            
            cell = [TableUtils toggleCellWithLabel:[Localizer getLocalizedText:@"commuter_pass"]
                                    forTable:tableView
                                   withValue:segment.fromIsCommuterPass
                                          onTarget:self
                                 andSelector:@selector(departureCommuterPassToggled:)];
            break;
        }
    }
    
    return cell;
}

- (UITableViewCell *)tableView:(UITableView *)tableView tripDetailsSectionCellForRow:(NSUInteger)row {
    UITableViewCell *cell = nil;
    
    switch (row) {
        case 0:
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"cost"]
                                             andDetail:[JPTUtils labelForFare:self.routeExpense.route.firstSegment.fare]
                                              forTable:tableView
                                              required:YES];
            break;
        case 1:
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"additional_charge"]
                                             andDetail:[JPTUtils labelForFare:self.routeExpense.route.firstSegment.additionalCharge]
                                              forTable:tableView
                                              required:NO];
            break;
        case 2:
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"business_purpose"]
                                              forTable:tableView];
            cell.detailTextLabel.text = self.routeExpense.purpose;
            break;
        case 3:
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"Comments"]
                                              forTable:tableView];
            cell.detailTextLabel.text = self.routeExpense.comment;
            break;
    }
    
    return cell;
}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 7;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = nil;
    
    int section = [indexPath section];
    int row = [indexPath row];
    
    switch (section) {
        case 0: {
            NSString *detail = [DateUtils dateFormattedForUI:self.routeExpense.route.date];
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"trip_date"]
                                             andDetail:detail
                                              forTable:tableView
                                              required:YES];
            break;
        }
        case 1:
            cell = [self tableView:tableView departureStationSectionCellForRow:row];
            break;
        case 2:
            cell = [self tableView:tableView arrivalStationSectionCellForRow:row];
            break;
        case 3:
            cell = [TableUtils toggleCellWithLabel:[Localizer getLocalizedText:@"Round Trip"]
                                    forTable:tableView
                                   withValue:self.routeExpense.route.isRoundTrip
                                          onTarget:self
                                 andSelector:@selector(roundTripToggled:)];
            break;
        case 4:
            cell = [self tableView:tableView tripDetailsSectionCellForRow:row];
            break;
        case 5:
            cell = [TableUtils toggleCellWithLabel:[Localizer getLocalizedText:@"personal_expense"]
                                    forTable:tableView
                                   withValue:self.routeExpense.isPersonalExpense
                                          onTarget:self
                                 andSelector:@selector(personalExpenseToggled:)];
            break;
        case 6:
            cell = [TableUtils toggleCellWithLabel:[Localizer getLocalizedText:@"add_as_favorite"]
                                    forTable:tableView
                                   withValue:self.routeExpense.isFavorite
                                          onTarget:self
                                 andSelector:@selector(isFavoriteToggled:)];
            break;
        default:
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
            numRows = 3;
            break;
        case 2:
            numRows = 3;
            break;
        case 3:
            numRows = 1;
            break;
        case 4:
            numRows = 4;
            break;
        case 5:
            numRows = 1;
            break;
        case 6:
            numRows = 1;
            break;
        default:
            break;
    }
    
    return numRows;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    NSString *title = nil;
    
    switch (section) {
        case 1:
            title = [Localizer getLocalizedText:@"From Station"];
            break;
        case 2:
            title = [Localizer getLocalizedText:@"To Station"];
            break;
    }
    
    return title;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [self.tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    int section = [indexPath section];
    int row = [indexPath row];
    
    switch (section) {
        case 0:
            [NavUtils gotoDateChooserWithDate:self.routeExpense.route.date
                                     andTitle:[Localizer getLocalizedText:@"Date"]
                            fromViewController:self];
            break;
        case 1:
            if (row == 0) {
                [NavUtils gotoStationChooserWithTitle:[Localizer getLocalizedText:@"Station"]
                                    fromViewController:self
                                 withNotificationName:@"DepartureStation"];
            } else if (row == 1) {
                if ([self.routeExpense.route.firstStation.key length]) {
                    [NavUtils gotoLineChooserWithTitle:[Localizer getLocalizedText:@"line"]
                                         forStationKey:self.routeExpense.route.firstStation.key
                                     fromViewController:self
                                  withNotificationName:@"DepartureLine"];
                } else {
                    [self promptUserToEnterDepartureStation];
                }
            }
            break;
        case 2:
            if (row == 0) {
                [NavUtils gotoStationChooserWithTitle:[Localizer getLocalizedText:@"Station"]
                                    fromViewController:self
                                 withNotificationName:@"ArrivalStation"];
            } else if (row == 1) {
                if ([self.routeExpense.route.lastStation.key length]) {
                    [NavUtils gotoLineChooserWithTitle:[Localizer getLocalizedText:@"line"]
                                         forStationKey:self.routeExpense.route.lastStation.key
                                     fromViewController:self
                                  withNotificationName:@"ArrivalLine"];
                } else {
                    [self promptUserToEnterArrivalStation];
                }
            }
            break;
        case 4:
            if (row == 0) {
                NSString *title = [Localizer getLocalizedText:@"Fee"];
                [NavUtils gotoNumberChooserWithNumber:self.routeExpense.route.firstSegment.fare
                                             andTitle:title
                                    fromViewController:self
                                 withNotificationName:@"Cost"];
            } else if (row == 1) {
                    NSString *title = [Localizer getLocalizedText:@"additional_charge"];
                    [NavUtils gotoNumberChooserWithNumber:self.routeExpense.route.firstSegment.additionalCharge
                                                 andTitle:title
                                       fromViewController:self
                                     withNotificationName:@"AdditionalCharge"];
            } else if (row == 2) {
                NSString *title = [Localizer getLocalizedText:@"business_purpose"];
                [NavUtils gotoTextChooserWithText:self.routeExpense.purpose
                                         andTitle:title
                                fromViewController:self
                             withNotificationName:@"BusinessPurpose"];
            } else if (row == 3) {
                NSString *title = [Localizer getLocalizedText:@"Comments"];
                [NavUtils gotoTextChooserWithText:self.routeExpense.comment
                                         andTitle:title
                                fromViewController:self
                             withNotificationName:@"Comments"];
            }
            break;
        default:
            break;
    }
}

#pragma mark - NSNotification Handlers

-(void)updateAdditionalCharge:(NSNotification *)notification {
    NSString *charge = [notification object];

    self.routeExpense.route.firstSegment.additionalCharge = [charge integerValue];
    
    [self.tableView reloadData];
}

-(void)updateArrivalLine:(NSNotification *)notification {
    Segment *segment = [self.routeExpense.route.segments objectAtIndex:1];
    segment.line = [notification object];
    
    [self.tableView reloadData];
}

-(void)updateArrivalStationName:(NSNotification *)notification {
    Segment *segment = [self.routeExpense.route.segments objectAtIndex:1];
    segment.toStation = [notification object];
    
    [self.tableView reloadData];
}

-(void)updateBusinessPurpose:(NSNotification *)notification {
    self.routeExpense.purpose = [notification object];
    
    [self.tableView reloadData];
}

-(void)updateComments:(NSNotification *)notification {
    self.routeExpense.comment = [notification object];
    
    [self.tableView reloadData];
}

-(void)updateCost:(NSNotification *)notification {
    NSString *cost = [notification object];

    self.routeExpense.route.firstSegment.fare = [cost integerValue];
    
    [self.tableView reloadData];
}

-(void)updateTripDate:(NSNotification *)notification {
    self.routeExpense.route.date = [notification object];

    [self.tableView reloadData];
}

-(void)updateDepartureLine:(NSNotification *)notification {
    Segment *segment = [self.routeExpense.route.segments objectAtIndex:0];
    segment.line = [notification object];
    
    [self.tableView reloadData];
}

-(void)updateDepartureStationName:(NSNotification *)notification {
    Segment *segment = [self.routeExpense.route.segments objectAtIndex:0];
    segment.fromStation = [notification object];
    
    [self.tableView reloadData];
}

@end
