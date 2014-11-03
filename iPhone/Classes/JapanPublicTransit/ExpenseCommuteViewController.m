//
//  ExpenseCommuteViewController.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/20/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "DateUtils.h"
#import "ExpenseCommuteViewController.h"
#import "JPTUtils.h"
#import "Localizer.h"
#import "NavUtils.h"
#import "RouteExpense.h"
#import "RouteExpenseManager.h"
#import "RouteManager.h"
#import "SelectReportViewController.h"
#import "TableUtils.h"
#import "UIColor+JPT.h"

#define ACTION_SAVE_AS_UNSUBMITTED 0
#define ACTION_ADD_TO_REPORT 1

@interface ExpenseCommuteViewController ()

@property (assign) BOOL adjustedForTextInput;

@end

@implementation ExpenseCommuteViewController

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        self.routeExpense = [[RouteExpense alloc] init];
        
        self.navigationItem.title = [Localizer getLocalizedText:@"expense_route"];
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

    [self setupTripHeader];
    
    [self setupNotificationListeners];
}

#pragma mark - Business logic

- (BOOL)canSaveFavorite {
    return YES;
}

- (void)isFavoriteToggled:(id)sender {
    if ([TableUtils isSwitchControl:sender]) {
        self.routeExpense.isFavorite = [TableUtils switchValue:sender];
    }
}

- (void)personalExpenseToggled:(id)sender {
    if ([TableUtils isSwitchControl:sender]) {
        self.routeExpense.isPersonalExpense = [TableUtils switchValue:sender];
    }
}

//- (void)saveAsExpense {
//    [[RouteExpenseManager sharedInstance] saveExpense:self.routeExpense];
//    
//    if (self.routeExpense.isFavorite) {
//        [[RouteManager sharedInstance] saveFavoriteRoute:self.routeExpense.route];
//    }
//    
//    [self gotoIndexToTab:2];
//}

- (void)setupNotificationListeners {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateBusinessPurpose:)
                                                 name:@"BusinessPurpose" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateComments:)
                                                 name:@"Comments" object:nil];
}

- (void)setupTripHeader {
    Route *route = self.routeExpense.route;
    
    // Synopsis
    //
    self.tripSynopsisLabel.text = [route synopsis];
    
    // Meta data
    //
    NSString *tripType = [route type];
    NSString *tripDuration = [JPTUtils labelForMinutes:[route minutes]];
    NSString *tripSeatType = [JPTUtils labelForSeatType:[route seatType]];
    NSString *metadata = [NSString stringWithFormat:@"%@ / %@ / %@",
                          tripType, tripSeatType, tripDuration];
    self.tripMetaDataLabel.text = metadata;
    
    // Price
    //
    self.tripPriceLabel.text = [JPTUtils labelForFare:[route fare]];
    
    // Date
    //
    self.tripDateLabel.text = [DateUtils dateFormattedForUI:route.date];
}

-(void)updateBusinessPurpose:(NSNotification *)notification {
    self.routeExpense.purpose = [notification object];
    
    [self.formTable reloadData];
}

-(void)updateComments:(NSNotification *)notification {
    self.routeExpense.comment = [notification object];
    
    [self.formTable reloadData];
}

#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldEndEditing:(UITextField *)textField {
    return YES;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self.view endEditing:YES];

    return YES;
}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSUInteger numRows = 0;
    
    switch (section) {
        case 0:
            numRows = 2;
            break;
        case 1:
            numRows = 1;
            break;
        case 2:
            numRows = 1;
            break;
    }
    
    return numRows;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 3;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView
                             dequeueReusableCellWithIdentifier:@"FormCell"];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"FormCell"];
    }
    
    int section = [indexPath section];
    int row = [indexPath row];
    
    switch (section) {
        case 0:
            if (row == 0) {
                cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"business_purpose"] forTable:tableView];
                cell.detailTextLabel.text = self.routeExpense.purpose;
            }
            else if (row == 1) {
                cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"Comments"] forTable:tableView];
                cell.detailTextLabel.text = self.routeExpense.comment;
            }
            break;
        case 1:
            cell = [TableUtils toggleCellWithLabel:[Localizer getLocalizedText:@"personal_expense"]
                                          forTable:tableView
                                         withValue:self.routeExpense.isPersonalExpense
                                          onTarget:self
                                       andSelector:@selector(personalExpenseToggled:)];
            break;
        case 2:
            cell = [TableUtils toggleCellWithLabel:[Localizer getLocalizedText:@"add_as_favorite"]
                                          forTable:tableView
                                         withValue:self.routeExpense.isFavorite
                                          onTarget:self
                                       andSelector:@selector(isFavoriteToggled:)];
            break;
    }
    
    return cell;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    int section = [indexPath section];
    int row = [indexPath row];
    
    switch (section) {
        case 0:
            if (row == 0) {
                NSString *title = [Localizer getLocalizedText:@"business_purpose"];
                [NavUtils gotoTextChooserWithText:self.routeExpense.purpose
                                         andTitle:title
                                fromViewController:self
                             withNotificationName:@"BusinessPurpose"];
            } else if (row == 1) {
                NSString *title = [Localizer getLocalizedText:@"Comments"];
                [NavUtils gotoTextChooserWithText:self.routeExpense.comment
                                         andTitle:title
                                fromViewController:self
                             withNotificationName:@"Comments"];
            }
            break;
    }
}

@end
