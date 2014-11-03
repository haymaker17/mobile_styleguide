//
//  Flurry+Additions.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 1/8/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "Flurry+Additions.h"

@implementation Flurry (Additions)

+ (void)signInSuccessFlurry:(NSString*)userType
{
    NSDictionary *dict = @{@"Credential Type": @"Pin or Password", @"User Type": userType};
    [Flurry logEvent:@"Sign In: Success" withParameters:dict];
}

+ (void)signInFailureFlurry:(NSString*)type
{
    NSDictionary *dict = @{@"Type": type};
    [Flurry logEvent:@"Sign In: Failure" withParameters:dict];
}

static NSInteger signInOverallCount = 0;
+ (void)signInOverallFlurry:(NSString*)finalAction
{
    // question: the accout is locked if login in fails for 3 times, why Over 5?
    // need to increase the tryAgainCount by 1 because it starts with 0;
    NSString *countString;
    if(++signInOverallCount > 5)
    {
        countString = @"Over 5";
    }
    else
    {
        countString = [NSString stringWithFormat:@"%i", signInOverallCount];
    }
    
    NSDictionary *dict = @{@"Try Again Count": countString, @"Final": finalAction };
    [Flurry logEvent:@"Sign In: Overall" withParameters:dict];
}

+ (void)resetSignInOverallCount{
    signInOverallCount = 0;
}

@end
