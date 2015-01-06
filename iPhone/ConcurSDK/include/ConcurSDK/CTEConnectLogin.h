//
//  CTEConnectLogin.h
//  ConcurSDK
//
//  Created by Shifan Wu on 10/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEError.h"

@interface CTEConnectLogin : NSObject


/**
 Login to Concur with username and password. Also accepts the mobile pin instead of password.
 This logs user into Concur and get access token
 
 @param username
 Concur username. This is usually work email.
 @param password
 Concur password.  For some users this will be a mobile specific password.  Previously called a PIN.
 @param success
 Success callback block.  This block is called on successful login.
 @param failure
 Failure callback block.  This block is called on login failure with a @c CTEError @c.
 */
+ (void)loginConcurWithUsername:(NSString *)username Password:(NSString *)password consumerKey:(NSString *)consumerKey
                        success:(void (^)(NSString *loginXML))success
                        failure:(void (^)(CTEError *error))failure;

@end
