//
//  LocalAuthenticationTests.m
//  ConcurMobile
//
//  Created by Shifan Wu on 8/4/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "LocalAuthenticationTests.h"

@import LocalAuthentication;

@interface LocalAuthenticationTests ()

@end

@implementation LocalAuthenticationTests


#pragma mark - Tests

+ (BOOL)canEvaluatePolicy
{
    LAContext *context = [[LAContext alloc] init];
    __block  NSString *msg;
    NSError *error;
    BOOL success;
    
    // test if we can evaluate the policy, this test will tell us if Touch ID is available and enrolled
    success = [context canEvaluatePolicy: LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error];
    if (success) {
        msg =[NSString stringWithFormat:NSLocalizedString(@"TOUCH_ID_IS_AVAILABLE", nil)];
        DLog(@"%@" ,msg);
    } else {
        msg =[NSString stringWithFormat:NSLocalizedString(@"TOUCH_ID_IS_NOT_AVAILABLE", nil)];
        DLog(@"%@" ,msg);
    }
    
    return success;
}


@end
