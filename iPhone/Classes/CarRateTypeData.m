//
//  CarRateTypeData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 2/17/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "CarRateTypeData.h"


@implementation CarRateTypeData
@synthesize lowerLimit, rateType, upperLimit;
@synthesize		aCarRateKeys;
@synthesize		dictCarRates, iUpper, iLower;

-(id) init
{
	self = [super init];
    if (self)
    {
        self.aCarRateKeys = [[NSMutableArray alloc] initWithObjects:nil];
        self.dictCarRates = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    }
	return self;
}

-(int) distanceInRateFrom:(int)start to:(int)end
{
    if ([rateType isEqualToString:@"COM_FIXED_PER"])
    {
        return end-start;
    }
    
    int lower = MAX(0, [lowerLimit intValue]-1);
    int upper = [upperLimit intValue];
    
    start = MAX(start, lower);
    end = MIN(end, upper);
    
    int dist = MAX(0, end-start);
    
    return dist;
}

-(CarRateData *)rateForDate:(NSDate*)date 
{
    CarRateData *carRateToUse = nil;
    for(NSString *carRateKey in dictCarRates) //for(NSString *carRateKey in carConfig.aCarRateKeys)
    {
        CarRateData *cRate = dictCarRates[carRateKey];
        //NSLog(@"cRate.dateStart = %@", cRate.dateStart);
        if ([date compare:cRate.dateStart] == NSOrderedDescending)
        {
            //NSOrderedDescending: //date is greater than carConfig start date
            if(carRateToUse == nil)
                carRateToUse = cRate;
            else if([carRateToUse.dateStart compare:cRate.dateStart] == NSOrderedAscending) //MOB-4339: changed to ascending for the order
                carRateToUse = cRate; //this looped car rate is closer to the date we want...
            
        }
    }
    
    return carRateToUse;
}

@end
