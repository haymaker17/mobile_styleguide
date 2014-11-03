//
//  AppCenterRequest.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 03/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEError.h"

@interface AppCenterRequest : NSObject

-(void)requestListOfApps:(void (^) (NSArray *appListings, NSString *info))success failure:(void (^)(CTEError *error))failure;

@end
