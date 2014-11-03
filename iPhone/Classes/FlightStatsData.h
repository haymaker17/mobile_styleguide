//
//  FlightStatsData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface FlightStatsData : NSObject {
	
	NSString			*equipmentScheduled, *equipmentActual, *equipmentRegistration, *departureTerminalScheduled, *departureTerminalActual, *departureGate
						,*departureStatusReason, *departureShortStatus, *departureLongStatus
						,*arrivalTerminalScheduled, *arrivalTerminalActual, *arrivalGate, *baggageClaim, *diversionCity
						,*diversionAirport, *arrivalStatusReason, *arrivalShortStatus, *arrivalLongStatus;
	
	NSString				*departureEstimated, *departureActual, *departureScheduled, *lastUpdatedUTC, *arrivalScheduled, *arrivalEstimated, *arrivalActual;

}

@property (nonatomic, strong) NSString			*equipmentScheduled;
@property (nonatomic, strong) NSString			*equipmentActual;
@property (nonatomic, strong) NSString			*equipmentRegistration;
@property (nonatomic, strong) NSString			*departureTerminalScheduled;
@property (nonatomic, strong) NSString			*departureTerminalActual;
@property (nonatomic, strong) NSString			*departureGate;
@property (nonatomic, strong) NSString			*departureStatusReason;
@property (nonatomic, strong) NSString			*departureShortStatus;
@property (nonatomic, strong) NSString			*departureLongStatus;
@property (nonatomic, strong) NSString			*arrivalTerminalScheduled;
@property (nonatomic, strong) NSString			*arrivalTerminalActual;
@property (nonatomic, strong) NSString			*arrivalGate;
@property (nonatomic, strong) NSString			*baggageClaim;
@property (nonatomic, strong) NSString			*diversionCity;
@property (nonatomic, strong) NSString			*diversionAirport;
@property (nonatomic, strong) NSString			*arrivalStatusReason;
@property (nonatomic, strong) NSString			*arrivalShortStatus;
@property (nonatomic, strong) NSString			*arrivalLongStatus;

@property (nonatomic, strong) NSString			*departureEstimated;
@property (nonatomic, strong) NSString			*departureActual;
@property (nonatomic, strong) NSString			*departureScheduled;
@property (nonatomic, strong) NSString			*lastUpdatedUTC;
@property (nonatomic, strong) NSString			*arrivalScheduled;
@property (nonatomic, strong) NSString			*arrivalEstimated;
@property (nonatomic, strong) NSString			*arrivalActual;

@end
