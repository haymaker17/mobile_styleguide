//
//  EntityHotelRoom+ViolationDetails.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 18/03/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "EntityHotelRoom+ViolationDetails.h"
#import "EntityHotelViolation.h"
#import "HotelBookingManager.h"

@implementation EntityHotelRoom (ViolationDetails)

- (NSString *)violationTextsNewLineSeparated
{
    return [[self violationTextsAsArray] componentsJoinedByString:@"\n"];
}

- (NSArray *)violationTextsAsArray
{
    NSArray* violations = [self.relHotelViolation allObjects];
    return [violations valueForKeyPath:@"@distinctUnionOfObjects.message"];
}

- (NSString *)getViolationReasonDescription
{
	return self.relHotelViolationCurrent.message;
}

- (void)setViolationReasonUserSelection:(ViolationReason *)reason
{
    if(self.relHotelViolationCurrent == nil)
        self.relHotelViolationCurrent = [[HotelBookingManager sharedInstance] makeNewViolation];
    
    self.relHotelViolationCurrent.code = reason.code;
    self.relHotelViolationCurrent.message = reason.description;
    self.relHotelViolationCurrent.violationType = reason.violationType;
}

- (NSString *)getViolationReasonCode
{
	return self.relHotelViolationCurrent.code;
}

- (NSString *)getFareType
{
    return @"H";
}

@end
