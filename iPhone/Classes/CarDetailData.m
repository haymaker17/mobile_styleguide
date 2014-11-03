//
//  CarDetailData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 2/17/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "CarDetailData.h"


@implementation CarDetailData
@synthesize carKey, criteriaName, distanceToDate, isPreferred, vehicleId;
@synthesize	aCarRateTypes;
@synthesize odometerStart;

-(id)init
{
    self = [super init];
    if (self) {
        self.aCarRateTypes = [[NSMutableArray alloc] initWithObjects:nil];
    }
	return self;
}

@end
