//
//  EntityAirFilterSummary+ViolationDetails.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 20/03/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "EntityAirFilterSummary+ViolationDetails.h"
#import "AirViolationManager.h"
#import "AirFilterSummaryManager.h"

@implementation EntityAirFilterSummary (ViolationDetails)

- (NSString *)violationTextsNewLineSeparated
{
    return [[self violationTextsAsArray] componentsJoinedByString:@"\n"];
}

- (NSArray *)violationTextsAsArray
{
    NSArray *aviolations = [[AirViolationManager sharedInstance] fetchByFareId:self.fareId];
    return [aviolations valueForKeyPath:@"@distinctUnionOfObjects.message"];
}

- (NSString *)getViolationReasonDescription
{
	return self.relAirViolationCurrent.message;
}

- (void)setViolationReasonUserSelection:(ViolationReason *)reason
{
    self.violationReason = reason.description;
    if(self.relAirViolationCurrent == nil)
        self.relAirViolationCurrent = [[AirFilterSummaryManager sharedInstance] makeNewViolation];
    
    self.relAirViolationCurrent.code = reason.code;
    self.relAirViolationCurrent.message = reason.description;
    [[AirFilterSummaryManager sharedInstance] saveIt:self];
}

- (NSString *)getViolationReasonCode
{
	return self.relAirViolationCurrent.code;
}

- (NSString *)getFareType
{
    return @"A";
}

@end
