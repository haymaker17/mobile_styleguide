//
//  CTEHotelViolation.h
//  ConcurSDK
//
//  Created by Christopher Butcher on 22/07/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

// more severe violations should have the higher value
typedef NS_ENUM(NSInteger, CTEHotelViolationEnforcementLevel) {

    // booking is allowed, may have a message for the user.
    CTEHotelBookingAllowed,

    // booking is allowed, requires a violation code and reason.
    CTEHotelBookingAllowedWithViolationCode,

    // booking is allowed, requires a violation code and reason. booking requires approval.
    CTEHotelBookingAllowedWithViolationCodeAndApproval,

    // booking is not allowed.
    CTEHotelBookingNotAllowed
};

@interface CTEHotelViolation : NSObject

@property (nonatomic, readonly, strong) NSNumber *violationId;
@property (nonatomic, readonly, assign) CTEHotelViolationEnforcementLevel enforcementLevel;
@property (nonatomic, readonly, strong) NSString *message;

// really, we should not send back all the violations if one violation overrides the others
@property (nonatomic, readonly, assign) BOOL ignoreOtherViolations;

+ (CTEHotelViolation *)findMostSevereViolation:(NSArray *)violations;

@end
