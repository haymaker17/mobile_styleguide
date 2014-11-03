//
//  Route.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "Route.h"
#import "Segment.h"

@interface Route : NSObject <NSCoding, NSCopying>

@property (copy, nonatomic) NSString *uuid;
@property (copy, nonatomic) NSDate *date;
@property (copy, nonatomic) NSString *entryType;
@property (assign) NSUInteger fare;
@property (assign) BOOL isRoundTrip;
@property (assign) NSUInteger minutes;
@property (assign) SeatType seatType;
@property (strong, nonatomic) NSMutableArray *segments;

- (Segment *)firstSegment;
- (Segment *)lastSegment;
- (Station *)firstStation;
- (Station *)lastStation;
- (NSString *)metadata;
- (void)addSegment:(Segment *)segment;
- (NSString *)synopsis;
- (NSArray *)throughStations;
- (NSString *)type;

@end
