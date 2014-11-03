//
//  TrainBookingItem.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface TrainBookingItem : NSObject {
	NSString		*departureStation, *departureTime, *departureDate, *arrivalStation, *arrivalTime, *arrivalDate;
	NSString		*duration, *seatType, *departureIata, *arrivalIata, *trainType;
	NSDate			*departureDateTime, *arrivalDateTime;
	float			amount;
	NSMutableArray	*aConnections, *aAmenities;
	BOOL			canBuyRooms, canAddBike;
}

@property (strong, nonatomic) NSString		*departureStation;
@property (strong, nonatomic) NSString		*departureTime;
@property (strong, nonatomic) NSString		*departureDate;
@property (strong, nonatomic) NSString		*arrivalStation;
@property (strong, nonatomic) NSString		*arrivalTime;
@property (strong, nonatomic) NSString		*arrivalDate;
@property (strong, nonatomic) NSString		*duration;
@property (strong, nonatomic) NSString		*seatType;
@property (strong, nonatomic) NSString		*departureIata;
@property (strong, nonatomic) NSString		*arrivalIata;
@property (strong, nonatomic) NSString		*trainType;
@property (strong, nonatomic) NSDate		*departureDateTime;
@property (strong, nonatomic) NSDate		*arrivalDateTime;
@property float			amount;
@property BOOL			canBuyRooms;
@property BOOL			canAddBike;
@property (strong, nonatomic) NSMutableArray *aConnections;
@property (strong, nonatomic) NSMutableArray *aAmenities;

-(id)init;
@end
