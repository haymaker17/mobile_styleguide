//
//  DateUtils.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "DateUtils.h"

@implementation DateUtils

+ (NSString *)dateFormattedForMWS:(NSDate *)date {
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    
    [dateFormatter setDateFormat:@"yyyy-MM-dd"];
    
    return [dateFormatter stringFromDate:date];
}

+ (NSString *)dateFormattedForUI:(NSDate *)date {
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];

    [dateFormatter setDateFormat:@"EEE MMM d, yyyy"];

    return [dateFormatter stringFromDate:date];
}

@end
