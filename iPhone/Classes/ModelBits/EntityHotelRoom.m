//
//  EntityHotelRoom.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 14/04/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "EntityHotelRoom.h"
#import "EntityHotelBooking.h"
#import "EntityHotelViolation.h"


@implementation EntityHotelRoom

@dynamic maxEnforcementLevel;
@dynamic isUsingPointsAgainstViolations;
@dynamic canUseTravelPoints;
@dynamic choiceId;
@dynamic travelPoints;
@dynamic depositRequired;
@dynamic summary;
@dynamic sellSource;
@dynamic violationReason;
@dynamic violationJustification;
@dynamic rate;
@dynamic bicCode;
@dynamic crnCode;
@dynamic gdsName;
@dynamic relHotelBooking;
@dynamic relHotelViolationCurrent;
@dynamic relHotelViolation;

@end
