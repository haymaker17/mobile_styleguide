//
//  FixedAllowanceCell.h
//  ConcurMobile
//
//  Created by Wes Barton on 2/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FixedAllowance.h"

@interface FixedAllowanceCell : UITableViewCell <UIPickerViewDelegate, UIPickerViewDataSource>

@property NSUInteger *section;

@property (weak,nonatomic) IBOutlet UILabel *date;
@property (weak,nonatomic) IBOutlet UILabel *amount;
@property (weak,nonatomic) IBOutlet UISwitch *markedExcluded;
@property (weak, nonatomic) IBOutlet UILabel *location;

@property (weak,nonatomic) IBOutlet UILabel *mealLabel;
@property (weak,nonatomic) IBOutlet UILabel *mealProvidedLabel;

@property (weak,nonatomic)IBOutlet UITextField *mealSelectedValue;

@property (weak,nonatomic) IBOutlet UILabel *breakfastLabel;
@property (weak,nonatomic) IBOutlet UILabel *breakfastAmountLabel;
@property (weak,nonatomic) IBOutlet UILabel *breakfastAmountValue;
@property NSDecimalNumber *breakfastAmount;
@property (strong, nonatomic) IBOutlet UITextField *breakfastAmountText;

@property (weak,nonatomic) IBOutlet UILabel *breakfastCurrencyLabel;
@property (weak,nonatomic) IBOutlet UILabel *breakfastCurrencyValue;

@property (weak,nonatomic) IBOutlet UILabel *breakfastExchangeRateValue;
@property NSDecimalNumber *breakfastExchangeRate;
@property (strong, nonatomic) IBOutlet UITextField *breakfastExchangeRateText;

@property (weak,nonatomic) IBOutlet UISwitch *breakfastIncludedSwitch;
@property (nonatomic,retain) IBOutlet UISegmentedControl *breakfastIncludedSegment;

@property (weak,nonatomic) IBOutlet UILabel *lunchLabel;
@property (weak,nonatomic) IBOutlet UISwitch *lunchIncludedSwitch;
@property (nonatomic,retain) IBOutlet UISegmentedControl *lunchIncludedSegment;

@property (weak,nonatomic) IBOutlet UILabel *dinnerLabel;
@property (weak,nonatomic) IBOutlet UISwitch *dinnerIncludedSwitch;
@property (nonatomic,retain) IBOutlet UISegmentedControl *dinnerIncludedSegment;

@property (weak,nonatomic) IBOutlet UILabel *overnightLabel;
@property (weak,nonatomic) IBOutlet UISwitch *overnightSwitch;

@property (weak, nonatomic) IBOutlet UILabel *extendedTripLabel;
@property (weak, nonatomic) IBOutlet UISwitch *extendedTripSwitch;


@property (weak,nonatomic) IBOutlet UILabel *usePercentageRuleLabel;
@property (weak,nonatomic) IBOutlet UISwitch *usePercentageRuleSwitch;

@property (weak, nonatomic) IBOutlet UILabel *lodgingTypeLabel;
@property (weak, nonatomic) IBOutlet UITextField *lodgingTypeValue;

@property NSString *mealType;
@property NSString *mealProvided;

@property NSString *breakfastCrnCode;
@property NSInteger *breakfastCrnKey;
@property NSString *breakfastCrnName;
@property (weak, nonatomic) IBOutlet UILabel *exchangeRateLabel;

@property (weak,nonatomic) IBOutlet UILabel *mealProvidedStatusLabel;

@property (weak, nonatomic) IBOutlet UILabel *mealAllowanceSectionTitle;
@property (weak, nonatomic) IBOutlet UILabel *otherAllowanceSectionTitle;


@property (nonatomic, retain) UIPickerView *mealAllowancePickerView;
@property (copy,nonatomic) void(^onMealAllowanceSelected)(NSString *selectedValue);

@property (nonatomic, retain) UIPickerView *lodgingTypePickerView;
@property (copy,nonatomic) void(^onLodgingTypeSelected)(NSString *selectedValue);

@property AllowanceControl *allowanceControl;

@property (strong, nonatomic) IBOutlet UISwitch *collapsedSwitch;

@property (strong, nonatomic) IBOutlet UIImageView *expandedIndicatorImage;

+ (NSString *)getMealProvidedValueLabel:(NSString *)provided;
- (NSString *)getLodgingTypeValueLabel:(NSString *)value;

- (void)inputAccessoryViewDidFinish;
- (void)inputAccessoryViewDidFinishLodgingType;
- (void)inputAccessoryViewDidFinishBreakfastAmountText;
- (void)inputAccessoryViewDidFinishBreakfastExchangeRateText;

+ (void)setMealAllowancePickerDefault:(FixedAllowance *)allowance cell:(FixedAllowanceCell *)cell provided:(NSString *)provided;

- (void)setLodgingTypePickerDefault:(NSString *)selected;




@end
