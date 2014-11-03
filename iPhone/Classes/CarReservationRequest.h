//
//  CarReservationRequest.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CarBookingTripData.h"

@interface CarReservationRequest : NSObject
{
	NSString			*carId;
	NSString			*creditCardId;
	CarBookingTripData	*carBookingTripData;
	NSString			*violationReasonCode;
	NSString			*violationJustification;
}

@property (nonatomic, strong) NSString				*carId;
@property (nonatomic, strong) NSString				*creditCardId;
@property (nonatomic, strong) CarBookingTripData	*carBookingTripData;
@property (nonatomic, strong) NSString				*violationReasonCode;
@property (nonatomic, strong) NSString				*violationJustification;

@end
