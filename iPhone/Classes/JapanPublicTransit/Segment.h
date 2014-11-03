//
//  Segment.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "Line.h"
#import "Station.h"

typedef NS_ENUM(NSUInteger, SeatType) {
    SeatTypeReserved,
    SeatTypeUnreserved,
    SeatTypeGreen,
    SeatTypeUnknown
};

@interface Segment : NSObject <NSCoding>

@property (nonatomic, strong) Line *line;
@property (nonatomic, strong) Station *fromStation;
@property (nonatomic, strong) Station *toStation;
@property (assign) NSUInteger fare;
@property (assign) NSUInteger minutes;
@property (assign) NSInteger distance;
@property (assign) SeatType seatType;
@property (assign) NSUInteger additionalCharge;
@property (assign) BOOL fromIsCommuterPass;
@property (assign) BOOL toIsCommuterPass;

- (NSUInteger)totalCharge;

@end
