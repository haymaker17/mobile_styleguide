//
//  RouteSearchModel.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "Segment.h"

@interface RouteSearchModel : NSObject

@property (strong, nonatomic) NSDate *date;
@property (assign) BOOL isRoundTrip;
@property (assign) SeatType seatType;
@property (strong, nonatomic) NSMutableArray *stations;
@property (strong, nonatomic) NSMutableArray *lines;
@property (assign) BOOL isIcCardFare;

- (NSString *)firstStationName;
- (NSString *)firstThroughStationName;
- (NSString *)lastStationName;
- (NSString *)metadata;
- (NSString *)metadataWithDate:(BOOL)withDate;
- (NSString *)secondThroughStationName;
- (NSString *)synopsis;

@end
