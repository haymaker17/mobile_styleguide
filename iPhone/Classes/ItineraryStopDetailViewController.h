//
//  ItineraryStopDetailViewController.h
//  ConcurMobile
//
//  Created by Wes Barton on 2/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ItineraryStop;
@class Itinerary;
@class ItineraryConfig;

@interface ItineraryStopDetailViewController : UITableViewController  <UIAlertViewDelegate>



@property BOOL showHeaderText;

@property Itinerary *itinerary;

@property Itinerary *itinerarySwitched;

@property ItineraryConfig *itineraryConfig;

@property ItineraryStop *itineraryStop;

@property NSMutableDictionary *paramBag;

@property BOOL isSingleDay;

@property (nonatomic, strong) NSArray *locations;

@property (nonatomic, copy) void(^onSuccessfulSave)(NSDictionary *);

+ (NSDateFormatter *)getItineraryTimeFormatter;
+ (NSDateFormatter *)getItineraryDateFormatter;

- (IBAction)dayTripSwitchChanged:(id)sender;

@end

static const int HeaderSectionIndex = 0;
static const int DayTripSectionIndex = 1;

static const int RegularFromSectionIndex = 2;
static const int RegularBorderCrossingSectionIndex = 3;
static const int RegularToSectionIndex = 4;

static const int SingleFromSectionIndex = 2;
static const int SingleBorderCrossingOneSectionIndex = 3;
static const int SingleToSectionIndex = 4;
static const int SingleBorderCrossingTwoSectionIndex = 5;
static const int SingleReturnSectionIndex = 6;


static const int From0CityIndex = 0;
static const int From1DateIndex = 1;
static const int From2DatePickerIndex = 2;
static const int From3TimeInput = 3;
static const int From4BIndex = 4;
static const int From5CIndex = 5;

static const int BorderCrossing0DateIndex = 0;
static const int BorderCrossingDate1PickerIndex = 1;

static const int To0CityIndex = 0;
static const int To1DateIndex = 1;
static const int To2DatePickerIndex = 2;
static const int To3TimeInput = 3;
static const int To4BIndex = 4;
static const int To5CIndex = 5;

static const int Return0TimeInputIndex = 0;
