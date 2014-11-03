//
//  ItineraryStopCell.h
//  ConcurMobile
//
//  Created by Wes Barton on 1/22/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "ItineraryStop.h"
#import "CKCalendarView.h"

@class CKCalendarView;

@interface ItineraryStopCell : UITableViewCell <CKCalendarDelegate>

@property (strong, nonatomic) IBOutlet UILabel *dayTripLabel;
@property (strong, nonatomic) IBOutlet UISwitch *dayTripSwitch;

// Used in the detail view
@property (weak,nonatomic) IBOutlet UILabel *stopCity;

@property (weak,nonatomic) IBOutlet UILabel *departureCity;
@property (weak,nonatomic) IBOutlet UILabel *arrivalCity;

@property (weak,nonatomic) IBOutlet UIDatePicker *DepartureDatePicker;
@property (weak,nonatomic) IBOutlet UIDatePicker *BorderCrossingDatePicker;
@property (weak,nonatomic) IBOutlet UIDatePicker *ArrivalDatePicker;

@property (weak,nonatomic) IBOutlet UILabel *departureDate;
@property (weak,nonatomic) IBOutlet UILabel *arrivalDate;

@property (weak,nonatomic) IBOutlet UILabel *stopDate;
@property (weak,nonatomic) IBOutlet UILabel *stopTime;
@property (weak,nonatomic) IBOutlet UILabel *stopLabel;

@property (strong, nonatomic) IBOutlet UIImageView *stopErrorIndicator;

@property (weak, nonatomic) IBOutlet UITextField *stopTimeText;
@property (weak, nonatomic) IBOutlet UITextField *stopDateText;

@property (weak,nonatomic) IBOutlet UILabel *stopNumber;
@property (weak,nonatomic) IBOutlet UILabel *arrivalRateLocation;
@property (weak,nonatomic) IBOutlet UILabel *borderCrossingDate;

@property (weak, nonatomic) IBOutlet UILabel *FromLabel;
@property (weak, nonatomic) IBOutlet UILabel *ToLabel;
@property (weak, nonatomic) IBOutlet UILabel *RateLocationLabel;

@property (nonatomic, retain) UIDatePicker *timePicker;
@property (nonatomic, retain) CKCalendarView *calendarView;
@property (strong, nonatomic) IBOutlet UITextView *headerText;

@property ItineraryStop *itineraryStop;
@property NSInteger *itineraryIndex;

@property (weak, nonatomic) NSString *whichStop;

@property (copy,nonatomic) void(^onDateSelected)(NSDate *date);

+ (NSDate *)mergeDateTime:(NSDate *)mergeDate mergeTime:(NSDate *)mergeTime;

+ (NSDate *)getStartOfBusinessDay:(NSDate *)baseDate;

+ (NSDate *)getEndOfBusinessDay:(NSDate *)baseDate;

- (void)setCellTimeLabels:(NSDate *)date;
- (void)setCellDateLabels:(NSDate *)date;

@end
