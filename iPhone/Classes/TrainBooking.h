//
//  TrainBooking.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TrainBookingItem.h"

@interface TrainBooking : NSObject 
{
	NSString		*departureDate, *departureTime, *returnDate, *returnTime, *locationFrom, *locationTo;
	int				numAdults, numKids, numInfants;
	BOOL			isRoundTrip;
	TrainBookingItem	*departureTBI, *returnTBI;
}

@property (strong, nonatomic) NSString		*departureDate;
@property (strong, nonatomic) NSString		*departureTime;
@property (strong, nonatomic) NSString		*returnDate;
@property (strong, nonatomic) NSString		*returnTime;
@property (strong, nonatomic) NSString		*locationFrom;
@property (strong, nonatomic) NSString		*locationTo;
@property int			numAdults;
@property int			numKids;
@property int			numInfants;
@property BOOL			isRoundTrip;
@property (strong, nonatomic) TrainBookingItem	*departureTBI;
@property (strong, nonatomic) TrainBookingItem	*returnTBI;

@end
