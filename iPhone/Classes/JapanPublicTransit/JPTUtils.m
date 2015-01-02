//
//  JPTUtils.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AddRouteExpenseToReportRequestFactory.h"
#import "CXClient.h"
#import "CXRequest.h"
#import "JPTUtils.h"
#import "Localizer.h"

@implementation JPTUtils

+ (void)addRouteExpense:(RouteExpense *)routeExpense toReport:(ReportData *)report
                success:(CXSuccessBlock)success
                failure:(CXFailureBlock)failure {
    
    CXRequest *request = [AddRouteExpenseToReportRequestFactory addRouteExpense:routeExpense
                                                                       toReport:report.rptKey];
    
    [[CXClient sharedClient] performRequest:request success:success failure:failure];
}

+ (NSString *)labelForFare:(NSUInteger)fare {
    if (fare == 0) {
        return nil;
    }
    
    NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];

    [numberFormatter setCurrencyCode:@"JPY"];
    [numberFormatter setNumberStyle:NSNumberFormatterCurrencyStyle];
    
    return [numberFormatter stringFromNumber:[NSNumber numberWithInteger:fare]];
}

+ (NSString *)labelForMinutes:(NSUInteger)minutes {
    NSString *label;
    
    if (minutes < 60) {
        label = [NSString stringWithFormat:@"%ld %@", (unsigned long)minutes, [Localizer getLocalizedText:@"minutes"]];
    } else {
        NSUInteger hours = minutes / 60;
        int remaining = minutes % 60;
        
        NSString *hourLabel = (hours > 1) ? @"hours" : @"hour";
        NSString *minutesLabel = (remaining > 1) ? @"minutes" : @"minute";
        
        // Instead of "1 hour, 0 minutes" we'll return "1 hour".
        //
        if (remaining > 0) {
            label = [NSString stringWithFormat:@"%ld %@, %d %@",
                     (unsigned long)hours, [Localizer getLocalizedText:hourLabel],
                     remaining, [Localizer getLocalizedText:minutesLabel]];
        } else {
            label = [NSString stringWithFormat:@"%ld %@",
                     (unsigned long)hours, [Localizer getLocalizedText:hourLabel]];
        }
    }
    
    return label;
}

+ (NSString *)labelForRouteType:(BOOL)isRoundTrip {
    NSString *label = nil;
    
    if (isRoundTrip) {
        label = [Localizer getLocalizedText:@"Round Trip"];
    } else {
        label = [Localizer getLocalizedText:@"One Way"];
    }
    
    return label;
}

+ (NSString *)labelForSeatType:(SeatType)type {
    NSString *name = nil;
    
    switch (type) {
        case SeatTypeGreen:
            name = [Localizer getLocalizedText:@"green"];
            break;
        case SeatTypeReserved:
            name = [Localizer getLocalizedText:@"reserved"];
            break;
        case SeatTypeUnknown:
            name = [Localizer getLocalizedText:@"unknown"];
            break;
        case SeatTypeUnreserved:
            name = [Localizer getLocalizedText:@"unreserved"];
            break;
    }
    
    return name;
}

+ (NSString *)stringForBoolean:(BOOL)arg {
    return arg ? @"Y" : @"N";
}

+ (NSString *)stringForFare:(NSUInteger)fare {
    NSString *str;
    
    // A string for a zero-denomination fare should be empty,
    // not "0", nil, NaN, etc.
    //
    if (fare == 0) {
        str = @"";
    } else {
        str = [NSString stringWithFormat:@"%lu", (unsigned long)fare];
    }
    
    return str;
}

@end
