//
//  Segment.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "Segment.h"

@implementation Segment

- (id)init {
    self = [super init];

    if (self) {
        self.distance = 0;
        self.fare = 0;
        self.minutes = 0;
        self.seatType = SeatTypeUnknown;
        self.line = [[Line alloc] init];
        self.fromStation = [[Station alloc] init];
        self.toStation = [[Station alloc] init];
    }
    
    return self;
}

- (NSString *)description {
    return [NSString stringWithFormat: @"Segment: { from: %@, to: %@, line: %@, fare: %lu, minutes: %lu }",
            self.fromStation, self.toStation, self.line, (unsigned long)self.fare, (unsigned long)self.minutes];
}

- (NSUInteger)totalCharge {
    return self.fare + self.additionalCharge;
}

#pragma mark - NSCoding

-(void)encodeWithCoder:(NSCoder *)encoder {
    [encoder encodeInteger:self.distance forKey:@"distance"];
    [encoder encodeInteger:self.fare forKey:@"fare"];
    [encoder encodeInteger:self.additionalCharge forKey:@"additional_charge"];
    [encoder encodeInteger:self.minutes forKey:@"minutes"];
    [encoder encodeInteger:self.seatType forKey:@"seat_type"];
    [encoder encodeObject:self.line forKey:@"line"];
    [encoder encodeObject:self.fromStation forKey:@"from_station"];
    [encoder encodeObject:self.toStation forKey:@"to_station"];
    [encoder encodeBool:self.fromIsCommuterPass forKey:@"from_is_commuter_pass"];
    [encoder encodeBool:self.toIsCommuterPass forKey:@"to_is_commuter_pass"];
}

-(id)initWithCoder:(NSCoder *)decoder {
    self.distance = [decoder decodeIntegerForKey:@"distance"];
    self.fare = [decoder decodeIntegerForKey:@"fare"];
    self.additionalCharge = [decoder decodeIntegerForKey:@"additional_charge"];
    self.minutes = [decoder decodeIntegerForKey:@"minutes"];
    self.seatType = [decoder decodeIntegerForKey:@"seat_type"];
    self.line = [decoder decodeObjectForKey:@"line"];
    self.fromStation = [decoder decodeObjectForKey:@"from_station"];
    self.toStation = [decoder decodeObjectForKey:@"to_station"];
    self.fromIsCommuterPass = [decoder decodeBoolForKey:@"from_is_commuter_pass"];
    self.toIsCommuterPass = [decoder decodeBoolForKey:@"to_is_commuter_pass"];
    
    return self;
}

@end
