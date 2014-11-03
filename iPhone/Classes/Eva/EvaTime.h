//
//  EvaTime.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//
/**
 * Complex Eva Time object to represent departure/arrival/pickupdate time etc from json response.
 * Eg:
 "Departure": {
 "Date": "2013-06-05",
 "Time": "01:00:00"
 },
 */

#import <Foundation/Foundation.h>

@interface EvaTime : NSObject

/**
 * Represent a specific date and time if given.
 */
@property (nonatomic, copy) NSString *date;

/**
 * Example: <code>fly to ny 3/4/2010 at 10am</code> results: <code>date</code>: <code>2010-04-03</code>, <code>time</code>:
 * <code>10:00:00</code>.
 */
@property (nonatomic, copy) NSString *time;

/**
 * May represent: A range starting from Date/Time. Example: <code>next week</code> results: <code>date</code>:
 * <code>2010-10-25</code>, <code>delta</code>: <code>days=+6</code> A duration without an anchor date. Example:
 * <code>hotel for a week</code> results: <code>delta</code>: <code>days=+7</code>
 */
@property (nonatomic, copy) NSString *delta;

/**
 * A restriction on the date/time requirement. Values can be: <code>no_earlier</code>, <code>no_later</code>,
 * <code>no_more</code>, <code>no_less</code>, <code>latest</code>, <code>earliest</code>
 *
 * Example: <code>depart NY no later than 10am</code> results: <code>restriction</code>: <code>no_later</code>,
 * <code>time</code>: <code>10:00:00</code>
 */
@property (nonatomic, copy) NSString *restriction;

/**
 * A boolean flag representing that a particular time has been calculated from other times, and not directly derived from the
 * input text. In most cases if an arrival time to a location is specified, the departure time from the previous location is
 * calculated.
 */
@property BOOL calculated;

// Methods
-(id)initWithDict:(NSDictionary *)dictionary;
-(void)parseJson;

-(NSDate *)getDateTime;

@end
