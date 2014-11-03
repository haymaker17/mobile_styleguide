//
//  Flurry+Additions.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 1/8/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "Flurry.h"

@interface Flurry (Additions)
+ (void)signInSuccessFlurry:(NSString*)userType;

+ (void)signInFailureFlurry:(NSString*)type;

+ (void)signInOverallFlurry:(NSString*)finalAction;
+ (void)resetSignInOverallCount;

@end
