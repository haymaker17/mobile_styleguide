//
//  JPTUtils.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXClient.h"
#import "ReportData.h"
#import "RouteExpense.h"
#import "RouteSearchModel.h"
#import "Segment.h"

@interface JPTUtils : NSObject

+ (void)addRouteExpense:(RouteExpense *)routeExpense toReport:(ReportData *)report
                success:(CXSuccessBlock)success
                failure:(CXFailureBlock)failure;
+ (NSString *)labelForFare:(NSUInteger)fare;
+ (NSString *)labelForMinutes:(NSUInteger)minutes;
+ (NSString *)labelForRouteType:(BOOL)isRoundTrip;
+ (NSString *)labelForSeatType:(SeatType)type;
+ (NSString *)stringForBoolean:(BOOL)arg;
+ (NSString *)stringForFare:(NSUInteger)fare;

@end
