//
//  CarConfigData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 1/20/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "CarConfigData.h"


@implementation CarConfigData
@synthesize		canCreateExp, carcfgKey, companyOrPersonal, configType, crnKey, ctryCode, ctryDistanceUnitCode;
@synthesize		aCarRateKeys;
@synthesize		dictCarRates, isPersonal;
@synthesize		aCarDetailKeys;
@synthesize		dictCarDetails, crnCode;

-(id)init
{
    self = [super init];
    if (self) {
        self.aCarRateKeys = [[NSMutableArray alloc] initWithObjects:nil];
        
        self.dictCarRates = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        
        
        self.aCarDetailKeys = [[NSMutableArray alloc] initWithObjects:nil];
        
        self.dictCarDetails = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    }
	return self;
}

- (CarDetailData*) findDetailForPreferredOrKey:(NSString*)key
{
    CarDetailData *cd = dictCarDetails[key];
    
    if(cd == nil)
    {
        for(NSString *key in dictCarDetails)
        {
            CarDetailData *cdFound = dictCarDetails[key];
            if([cdFound.isPreferred isEqualToString:@"Y"] && [cdFound.aCarRateTypes count] > 0)
            {
                cd = dictCarDetails[key];
                break;
            }
        }
    }
    
    return cd;

}

- (CarRateData *) findRateForDate:(NSDate *)date
{
    CarRateData *carRateToUse = nil;
    for(NSString *carRateKey in aCarRateKeys)
    {
        CarRateData *cRate = dictCarRates[carRateKey];
        if ([date compare:cRate.dateStart] == NSOrderedDescending)
        {
            if(carRateToUse == nil)
                carRateToUse = cRate;
            else if([carRateToUse.dateStart compare:cRate.dateStart] == NSOrderedAscending) //MOB-4339: changed to ascending for the order
                carRateToUse = cRate; //this looped car rate is closer to the date we want...
        }
    }
    
    return carRateToUse;
}




@end
