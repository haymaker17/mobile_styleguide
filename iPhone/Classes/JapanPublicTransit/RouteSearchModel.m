//
//  RouteSearchModel.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "DateUtils.h"
#import "JPTUtils.h"
#import "RouteSearchModel.h"
#import "Station.h"

@implementation RouteSearchModel

- (id)init {
    self = [super init];
    
    if (self) {
        self.date = [NSDate date];
        self.stations = [[NSMutableArray alloc] init];
        self.lines = [[NSMutableArray alloc] init];
        self.isIcCardFare = YES;
    }
    
    return self;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"RouteSearchModel: { isRoundTrip: %d, seatType: %d, %@ }",
            self.isRoundTrip, self.seatType, self.stations];
}

// Do not need to serialize this for time being.
//
//#pragma mark - NSCoding
//
//-(void)encodeWithCoder:(NSCoder *)encoder {
//    [encoder encodeObject:self.date forKey:@"date"];
//    [encoder encodeBool:self.isRoundTrip forKey:@"is_round_trip"];
//    [encoder encodeObject:self.stations forKey:@"stations"];
//    [encoder encodeObject:self.stations forKey:@"lines"];
//}
//
//-(id)initWithCoder:(NSCoder *)decoder {
//    self.date = [decoder decodeObjectForKey:@"date"];
//    self.isRoundTrip = [decoder decodeBoolForKey:@"is_round_trip"];
//    self.stations = [decoder decodeObjectForKey:@"stations"];
//    self.stations = [decoder decodeObjectForKey:@"lines"];
//    
//    return self;
//}

#pragma mark - Business logic

- (NSString *)firstStationName {
    Station *firstStation = [self.stations objectAtIndex:0];
    
    return firstStation.name;
}

- (NSString *)firstThroughStationName {
    Station *firstThroughStation = [self.stations objectAtIndex:1];
    
    return firstThroughStation.name;
}

- (NSString *)lastStationName {
    Station *lastStation = [self.stations lastObject];
    
    return lastStation.name;
}

- (NSString *)metadata {
    NSString *prettyDate = [DateUtils dateFormattedForUI:self.date];
    NSString *routeTypeLabel = [JPTUtils labelForRouteType:self.isRoundTrip];
    NSString *seatTypeLabel = [JPTUtils labelForSeatType:self.seatType];
    
    return [NSString stringWithFormat:@"%@ / %@ / %@", prettyDate, routeTypeLabel, seatTypeLabel];
}

- (NSString *)metadataWithDate:(BOOL)withDate {
    NSString *metadata = nil;
    
    if (withDate) {
        metadata = [self metadata];
    } else {
        NSString *routeTypeLabel = [JPTUtils labelForRouteType:self.isRoundTrip];
        NSString *seatTypeLabel = [JPTUtils labelForSeatType:self.seatType];
    
        metadata = [NSString stringWithFormat:@"%@ / %@", routeTypeLabel, seatTypeLabel];
    }
    
    return metadata;
}

- (NSString *)secondThroughStationName {
    Station *secondThroughStation = [self.stations objectAtIndex:2];
    
    return secondThroughStation.name;
}

- (NSString *)synopsis {
    return [NSString stringWithFormat:@"%@ - %@", self.firstStationName, self.lastStationName];
}

@end
