//
//  TravelViolationReasons.m
//  ConcurMobile
//
//  Created by ernest cho on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TravelViolationReasons.h"
#import "ViolationReason.h"
#import "HotelViolation.h"

@implementation TravelViolationReasons

@synthesize violationReasons;

static TravelViolationReasons* travelViolationReasonsSingleton = nil;

+(TravelViolationReasons*)getSingleton
{
    return travelViolationReasonsSingleton;
}

+(void)setSingleton:(TravelViolationReasons*)travelViolations
{
    if (travelViolationReasonsSingleton == travelViolations) {
        return;
    }
    
    travelViolationReasonsSingleton = travelViolations;
}

-(id)init
{
	self = [super init];
    if (self) {
	    self.violationReasons = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    }
    return self;
}

-(NSMutableArray*)getReasonsFor:(NSArray*)violations;
{
    NSMutableArray *tmpReasons = [[NSMutableArray alloc] initWithObjects:nil];
    
    BOOL isGeneralType = false;
    
    // standard types, must show G also
    if ([violations containsObject:@"A"] || [violations containsObject:@"H"] ||
        [violations containsObject:@"C"] || [violations containsObject:@"I"] || [violations containsObject:@"X"])
    {
        isGeneralType = true;
    }
    
    ViolationReason	*reason = nil;
    NSArray *allKeys = [violationReasons allKeys];
    for (NSString *key in allKeys)
    {
        reason = violationReasons[key];
        if ([violations containsObject:reason.violationType] || (isGeneralType && [reason.violationType isEqualToString:@"G"])) {
            [tmpReasons addObject:reason];
        }
    }
    return tmpReasons;
}


@end
