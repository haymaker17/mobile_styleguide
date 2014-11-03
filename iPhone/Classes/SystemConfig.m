//
//  SystemConfig.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SystemConfig.h"
#import "ViolationReason.h"

@implementation SystemConfig

@synthesize carViolationReasons;
@synthesize hotelViolationReasons;
@synthesize airViolationReasons;
@synthesize officeLocations;
@synthesize isNonRefundableOnly;
@synthesize ruleViolationExplanationRequired;
@synthesize nonRefundableMsg;
@synthesize checkboxDefault;
@synthesize showCheckbox;

static SystemConfig* systemConfigSingleton = nil;

+(SystemConfig*)getSingleton
{
	return systemConfigSingleton;
}

+(void)setSingleton:(SystemConfig*)systemConfig
{
	if (systemConfigSingleton == systemConfig)
		return;
	
	systemConfigSingleton = systemConfig;
}

-(id)init
{
	self = [super init];
	if (self)
    {
        self.carViolationReasons = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        
        self.hotelViolationReasons = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        
        self.airViolationReasons = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        
        self.officeLocations = [[NSMutableArray alloc] init];
	}
	return self;
}



@end
