//
//  TrainBookingItem.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainBookingItem.h"


@implementation TrainBookingItem
@synthesize		departureStation, departureTime, departureDate, arrivalStation, arrivalTime, arrivalDate;
@synthesize		duration, seatType, departureIata, arrivalIata, trainType;
@synthesize			departureDateTime, arrivalDateTime;
@synthesize			amount, canBuyRooms, canAddBike;
@synthesize	aConnections, aAmenities;


-(id)init
{
    self = [super init];
    if (self)
    {
        self.aConnections = [[NSMutableArray alloc] initWithObjects:nil];
        self.aAmenities = [[NSMutableArray alloc] initWithObjects:nil];
    }
	return self;
}
@end
