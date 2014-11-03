//
//  ItineraryAllowanceAdjustmentViewController.h
//  ConcurMobile
//
//  Created by Wes Barton on 2/20/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ItineraryConfig;
@class Itinerary;
@class ItineraryStop;

static const int hiddenRowHeight = 0;

static const int numberOfRowsInSection = 14;

static const int dayHeaderCellRowIndex = 0;
static const int excludedHeaderCellRowIndex = 1;
static const int mealAllowanceHeaderCellRowIndex = 2;

static const int breakfastAllowanceCellRowIndex = 3;

static const int lunchAllowanceCellRowIndex = 4;
static const int dinnerAllowanceCellRowIndex = 5;

static const int otherAllowanceHeaderCellRowIndex = 6;
static const int overnightCellRowIndex = 7;

static const int userEntryBreakfastAmountCellRowIndex = 8;
static const int userEntryBreakfastAmountCurrencyCellRowIndex = 9;
static const int userEntryBreakfastAmountExchangeRateCellRowIndex = 10;

static const int percentRuleCellRowIndex = 11;
static const int extendedTripCellRowIndex = 12;
static const int lodgingTypeCellRowIndex = 13;

static const BOOL useSegmentedMealProvided = NO;

@interface ItineraryAllowanceAdjustmentViewController : UITableViewController

@property BOOL showHeaderText;

@property NSString *rptKey;
@property (nonatomic, copy) NSString *crnCode;
@property(nonatomic, weak) NSString *selectedItinKey;

@property NSString *taDayKey;
@property BOOL expandAllDays;
@property BOOL hideGenerateExpenseButton;

@property (nonatomic, copy) void(^onSuccessfulSave)(NSDictionary *);

@property (weak, nonatomic) IBOutlet UINavigationItem *navBar;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *generateExpensesButton;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *BackButton;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *barbuttonSpacer;

@property NSString *role;
@property BOOL hasCloseButton;

@property(nonatomic, strong) NSDateFormatter *dateFormatterMedium;

- (IBAction)breakfastExchangeRateEndEdit:(id)sender;

- (void)actionBack:(id)sender;

- (IBAction)breakfastAmountEndEdit:(id)sender;
@end
