//
//  CTEDateUtility.h
//  ConcurSDK
//
//  Created by ernest cho on 6/4/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTEDateUtility : NSObject

// api specific conversion methods
+ (NSString *)convertDateToStringForHotelSearch:(NSDate *)date;

// generic conversion methods
+ (NSDate *)convertStringToDate:(NSString *)dateAsString withInputFormat:(NSString *)inputFormat timeZone:(NSTimeZone *)timeZone;
+ (NSString *)convertDateToString:(NSDate *)date withOutputFormat:(NSString *)outputFormat timeZone:(NSTimeZone *)timeZone;

// add time to a date, these just use NSCalendar and NSDateComponents
+ (NSDate *)addDaysToDate:(NSDate *)date daysToAdd:(int)daysToAdd;
+ (NSDate *)addHoursToDate:(NSDate *)date hoursToAdd:(int)hoursToAdd;

// number of calendar days between two dates
+ (NSInteger)daysBetweenDate:(NSDate*)fromDateTime andDate:(NSDate*)toDateTime;

// for unit tests
+ (NSDate *)createDateWithYear:(int)year month:(int)month day:(int)day hour:(int)hour minute:(int)minute second:(int)second timeZone:(NSTimeZone *)timeZone;

@end
