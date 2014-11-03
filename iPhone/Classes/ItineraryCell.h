//
//  ItineraryCell.h
//  ConcurMobile
//
//  Created by Wes Barton on 4/10/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class Itinerary;
@class ItineraryConfig;


@interface ItineraryCell : UITableViewCell  <UIPickerViewDelegate, UIPickerViewDataSource>
@property (weak, nonatomic) IBOutlet UILabel *itineraryName;
@property (weak, nonatomic) IBOutlet UILabel *numberOfStops;
@property (strong, nonatomic) IBOutlet UILabel *itineraryDateRange;
@property (strong, nonatomic) IBOutlet UILabel *itineraryTripLength;

@property (strong, nonatomic) IBOutlet UILabel *itineraryNameLabel;
@property (strong, nonatomic) IBOutlet UITextField *itineraryNameEdit;
@property (strong, nonatomic) IBOutlet UILabel *itineraryExtendedTripLabel;
@property (strong, nonatomic) IBOutlet UISwitch *itineraryExtendedTripSwitch;
@property (strong, nonatomic) IBOutlet UILabel *tripLengthLabel;
@property (strong, nonatomic) IBOutlet UITextField *tripLengthValue;
@property (strong, nonatomic) IBOutlet UILabel *shortDistanceLabel;
@property (strong, nonatomic) IBOutlet UISwitch *shortDistanceSwitch;
@property (strong, nonatomic) IBOutlet UIImageView *indicatorImage;
@property (strong, nonatomic) IBOutlet UITextView *informationText;
@property (strong, nonatomic) IBOutlet UIImageView *expandedIndicatorImage;

@property (nonatomic, retain) UIPickerView *tripLengthPickerView;
@property (copy,nonatomic) void(^onTripLengthSelected)(NSString *selectedValue);

@property ItineraryConfig *itineraryConfig;

@property Itinerary *itinerary;

+ (void)setTripLengthPickerDefault:(Itinerary *)itinerary cell:(ItineraryCell *)cell selectedValue:(NSString *)selectedValue;

+ (void)composeItineraryDateRange:(Itinerary *)itinerary cell:(ItineraryCell *)cell format:(NSString *)format;
@end
