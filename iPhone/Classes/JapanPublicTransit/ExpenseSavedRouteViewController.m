//
//  ExpenseSavedRouteViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChooseDateViewController.h"
#import "ChooseTextViewController.h"
#import "CXRequest.h"
#import "DateUtils.h"
#import "ExpenseSavedRouteViewController.h"
#import "JPTUtils.h"
#import "Localizer.h"
#import "NavUtils.h"
#import "RouteExpenseManager.h"
#import "RouteManager.h"
#import "SelectReportViewController.h"
#import "TableUtils.h"

@interface ExpenseSavedRouteViewController ()

@property (assign) CGPoint scrollOffset;

@end

@implementation ExpenseSavedRouteViewController

- (void)didMoveToParentViewController:(UIViewController *)parent {
    if (parent == nil) {
        [[NSNotificationCenter defaultCenter] removeObserver:self];
    }
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
    
    self.routeView.route = self.routeExpense.route;
    [self.routeView sizeToFit];
    
    int totalHeight = 0;
    int routeViewHeight = self.routeView.frame.size.height;
    
    [self setupTripHeader];
    
//    FancyDatePickerView *datePicker = [[FancyDatePickerView alloc] initWithFrame:CGRectMake(0, 0, 320, 206)];
//    datePicker.delegate = self;
//    
//    self.dateTextView.inputView = datePicker;
    
    self.purposeTextField.text = self.routeExpense.purpose;
    self.commentTextField.text = self.routeExpense.comment;
    self.toggleFavoriteSwitch.on = self.routeExpense.isPersonalExpense;
    
    self.dateTextView.text = [DateUtils dateFormattedForUI:self.routeExpense.route.date];
    
    CGRect formTableBarFrame = self.formTableBar.frame;
    formTableBarFrame.origin.y += routeViewHeight;
    self.formTableBar.frame = formTableBarFrame;
    
    CGRect formContainerFrame = self.formTable.frame;
    formContainerFrame.origin.y += routeViewHeight;
    self.formTable.frame = formContainerFrame;
    
    [self.formTable sizeToFit];
    
    self.formTable.backgroundView = nil;
    
    for (UIView *v in self.scrollView.subviews) {
        if (v.isHidden == NO) {
            totalHeight += v.frame.size.height;
        }
    }
    
    // TODO: Don't know why we need to add 140 here. Won't scroll all
    // content unless we do, though.
    //
    self.scrollView.contentSize = CGSizeMake(320, totalHeight + 140);
    
    [self setupNotificationListeners];
}

#pragma mark - Business logic

- (void)deleteSavedRouteExpense {
    [[RouteExpenseManager sharedInstance] deleteRouteExpense:self.routeExpense];
}

- (void)personalExpenseToggled:(id)sender {
    if ([TableUtils isSwitchControl:sender]) {
        self.routeExpense.isPersonalExpense = [TableUtils switchValue:sender];
    }
}


//- (void)saveAsExpense {
//    [[RouteExpenseManager sharedInstance] saveExpense:self.routeExpense];
//    
//    [self gotoIndexToTab:2];
//}

- (void)setRouteView:(RouteView *)routeView {
    _routeView = routeView;
    
    [self.formTable reloadData];
}

- (void)setupNotificationListeners {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateTripDate:)
                                                 name:@"TripDate" object:nil];
    
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
    
    NSString *metadata;
    if ([route minutes] > 0) {
        NSString *tripDuration = [JPTUtils labelForMinutes:[route minutes]];
        metadata = [NSString stringWithFormat:@"%@ / %@", tripType, tripDuration];
    } else {
         metadata = tripType;
    }
    
    
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

-(void)updateTripDate:(NSNotification *)notification {
    self.routeExpense.route.date = [notification object];
    
    [self.formTable reloadData];
}

#pragma mark - UITextFieldDelegate

- (void)textFieldDidEndEditing:(UITextField *)textField {
    //[self setDoneButtonState];
}

- (BOOL)textFieldShouldEndEditing:(UITextField *)textField {
    return YES;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self.view endEditing:YES];
    
    return YES;
}
//
//#pragma mark - UIActionSheetDelegate
//
//- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
//    switch (buttonIndex) {
//        case 0:
//            [self saveAsExpense];
//            break;
//        case 1:
//            [self selectReport];
//            break;
//    }
//}

#pragma mark - UIScrollViewDelegate

//- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView {
//    [self.view endEditing:YES];
//}

//#pragma mark - FancyDatePickerDelegate
//
//- (void)datePickerDidDismiss:(FancyDatePickerView *)picker {
//    [self.view endEditing:YES];
//}
//
//- (void)datePicker:(FancyDatePickerView *)picker didChangeDate:(NSDate *)date {
//    self.dateTextView.text = [DateUtils dateFormattedForUI:date];
//}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSUInteger numRows = 0;
    
    switch (section) {
        case 0:
            numRows = 1;
            break;
        case 1:
            numRows = 2;
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
            cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"trip_date"]
                                              forTable:tableView];
            cell.detailTextLabel.text = [DateUtils dateFormattedForUI:self.routeExpense.route.date];
            break;
        case 1:
            if (row == 0) {
                cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"business_purpose"]
                                                  forTable:tableView];
                cell.detailTextLabel.text = self.routeExpense.purpose;
            }
            else if (row == 1) {
                cell = [TableUtils disclosureCellWithLabel:[Localizer getLocalizedText:@"Comments"]
                                                  forTable:tableView];
                cell.detailTextLabel.text = self.routeExpense.comment;
            }
            break;
        case 2:
            cell = [TableUtils toggleCellWithLabel:[Localizer getLocalizedText:@"personal_expense"]
                                          forTable:tableView
                                         withValue:self.routeExpense.isPersonalExpense
                                          onTarget:self
                                       andSelector:@selector(personalExpenseToggled:)];
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
            [NavUtils gotoDateChooserWithDate:self.routeExpense.route.date
                                     andTitle:[Localizer getLocalizedText:@"Date"]
                           fromViewController:self];
            //[self gotoDateChooser];
            break;
        case 1:
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

#pragma mark - ReportActionDelegate

- (void)didChooseReport:(ReportData *)report {
    [self deleteSavedRouteExpense];
    
    [super didChooseReport:report];
}

@end
