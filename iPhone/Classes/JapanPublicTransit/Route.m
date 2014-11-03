//
//  Route.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "JPTUtils.h"
#import "Localizer.h"
#import "Route.h"
#import "Segment.h"

@implementation Route

-(id)copyWithZone:(NSZone *)zone {
    Route *clone = [[Route alloc] init];
    
    clone.uuid = [self.uuid copy];
    clone.date = [self.date copy];
    clone.entryType = [self.entryType copy];
    clone.fare = self.fare;
    clone.isRoundTrip = self.isRoundTrip;
    clone.minutes = self.minutes;
    clone.seatType = self.seatType;
    clone.segments = [self.segments copy];
    
    return clone;
}

- (id)init {
    self = [super init];
    
    if (self) {
        CFUUIDRef uuidRef = CFUUIDCreate(kCFAllocatorDefault);
        self.uuid = (NSString *) CFBridgingRelease(CFUUIDCreateString(NULL, uuidRef));
        CFRelease(uuidRef);
        
        self.isRoundTrip = NO;
        self.segments = [[NSMutableArray alloc] init];
        self.date = [NSDate date];
    }
    
    return self;
}

- (void)addSegment:(Segment *)segment {
    Segment *lastSegment = [self.segments lastObject];
    
    if (lastSegment != nil) {
        if (![lastSegment.toStation.name isEqualToString:segment.fromStation.name]) {
            segment.fromStation.name = [NSString stringWithFormat:@"%@ / %@",
                                        lastSegment.toStation.name,
                                        segment.fromStation.name];
        }
    }
    
    [self.segments addObject:segment];
}

- (NSString *)description {
    return [NSString stringWithFormat: @"Route: { uuid: %@, date: %@, fare: %d, minutes: %d, segments: [ %@ ] }",
            self.uuid, self.date, self.fare, self.minutes, self.segments];
}

- (Segment *)firstSegment {
    return [self.segments objectAtIndex:0];
}

- (Segment *)lastSegment {
    return [self.segments lastObject];
}

- (Station *)firstStation {
    Segment *segment = [self.segments objectAtIndex:0];
    
    return segment.fromStation;
}

- (Station *)lastStation {
    Segment *segment = [self.segments lastObject];
    
    return segment.toStation;
}

- (NSString *)metadata {
    NSString *routeType = self.type;
    
    NSString *metadata;
    
    if (self.minutes == 0) {
        metadata = routeType;
    } else {
        NSString *routeDuration = [JPTUtils labelForMinutes:self.minutes];
        
        metadata = [NSString stringWithFormat:@"%@ / %@", routeType, routeDuration];
    }
    
    return metadata;
}

- (NSString *)synopsis {
    Segment *firstSegment = [self.segments objectAtIndex:0];
    Segment *lastSegment = [self.segments lastObject];
    
    NSString *firstStation = firstSegment.fromStation.name;
    NSString *lastStation = lastSegment.toStation.name;
    
    NSString *synopsis = [NSString stringWithFormat:@"%@ - %@", firstStation, lastStation];
    
    return synopsis;
}

- (NSArray *)throughStations {
    
    // If we have less than three routes then we just have
    // the two start/destination stations, no "through" stations.
    //
    if ([self.segments count] < 1) {
        return nil;
    }
    
    NSUInteger throughSegmentRangeStart = 1;
    NSUInteger throughSegmentRangeLength = [self.segments count] - 1;
    
    NSRange range = NSMakeRange(throughSegmentRangeStart, throughSegmentRangeLength);
    
    return [self.segments subarrayWithRange:range];
}

- (NSString *)type {
    NSString *type;
    
    if (self.isRoundTrip == NO) {
        type = [Localizer getLocalizedText:@"One Way"];
    } else {
        type = [Localizer getLocalizedText:@"Round Trip"];
    }
    
    return type;
}

#pragma mark - NSCoding

-(void)encodeWithCoder:(NSCoder *)encoder {
    [encoder encodeObject:self.uuid forKey:@"uuid"];
    [encoder encodeObject:self.date forKey:@"date"];
    [encoder encodeObject:self.entryType forKey:@"entry_type"];
    [encoder encodeInteger:self.fare forKey:@"fare"];
    [encoder encodeInteger:self.minutes forKey:@"minutes"];
    [encoder encodeBool:self.isRoundTrip forKey:@"is_round_trip"];
    [encoder encodeInteger:self.seatType forKey:@"seat_type"];
    [encoder encodeObject:self.segments forKey:@"segments"];
}

-(id)initWithCoder:(NSCoder *)decoder {
    self.uuid = [decoder decodeObjectForKey:@"uuid"];
    self.date = [decoder decodeObjectForKey:@"date"];
    self.entryType = [decoder decodeObjectForKey:@"entry_type"];
    self.fare = [decoder decodeIntegerForKey:@"fare"];
    self.minutes = [decoder decodeIntegerForKey:@"minutes"];
    self.isRoundTrip = [decoder decodeBoolForKey:@"is_round_trip"];
    self.seatType = [decoder decodeIntegerForKey:@"seat_type"];
    self.segments = [decoder decodeObjectForKey:@"segments"];
    
    return self;
}

@end
