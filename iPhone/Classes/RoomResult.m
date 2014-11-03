//
//  RoomResult.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "RoomResult.h"
#import "SystemConfig.h"
#import "ViolationReason.h"

@implementation RoomResult

@synthesize currencyCode;
@synthesize rate;
@synthesize summary;
@synthesize bicCode;
@synthesize sellSource;
@synthesize violations;
@synthesize violationReasonCode;
@synthesize violationJustification;

@dynamic violationReason;

-(NSString*)violationReason
{
	NSString *reason = nil;
	
	if (violationReasonCode != nil)
	{
		SystemConfig *systemConfig = [SystemConfig getSingleton];
		if (systemConfig != nil)
		{
			ViolationReason *violationReason = (systemConfig.hotelViolationReasons)[violationReasonCode];
			reason = violationReason.description;
		}
	}
	
	return reason;
}

-(id)init
{
	self = [super init];
	if (self)
    {
        self.violations = [[NSMutableArray alloc] initWithObjects:nil];  // Retain count = 2
     
	}
	return self;
}


@end
