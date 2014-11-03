//
//  FlightStatsData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FlightStatsData.h"


@implementation FlightStatsData

@synthesize	equipmentScheduled, equipmentActual, equipmentRegistration, departureTerminalScheduled, departureTerminalActual, departureGate;
@synthesize departureStatusReason, departureShortStatus, departureLongStatus;
@synthesize arrivalTerminalScheduled, arrivalTerminalActual, arrivalGate, baggageClaim, diversionCity;
@synthesize diversionAirport, arrivalStatusReason, arrivalShortStatus, arrivalLongStatus;
@synthesize	departureEstimated, departureActual, departureScheduled, lastUpdatedUTC, arrivalScheduled, arrivalEstimated, arrivalActual;


@end
