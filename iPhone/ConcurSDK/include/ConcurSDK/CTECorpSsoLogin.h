//
//  CTECorpSsoLogin.h
//  ConcurSDK
//
//  Created by ernest cho on 2/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEError.h"

@interface CTECorpSsoLogin : NSObject

- (id)initWithSingleSignOnToken:(NSString *)token;
- (void)loginWithSuccess:(void (^)())success failure:(void (^)(CTEError *error))failure;

// concur mobile needs the login xml back.
- (void)loginConcurMobileWithSuccess:(void (^)(NSString *loginXML))success failure:(void (^)(CTEError *error))failure;

@end
