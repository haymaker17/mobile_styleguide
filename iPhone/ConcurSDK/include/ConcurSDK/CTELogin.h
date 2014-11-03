//
//  CTELogin.h
//  ConcurSDK
//
//  Created by ernest cho on 2/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEError.h"
#import "CTEUserLookupResult.h"

@interface CTELogin : NSObject

/**
 Login to concur with username and password. Also accepts the mobile pin instead of password.
 This method will handle the login response object and only report success or failure.
 
 @param username
        Concur username. This is usually work email.
 @param password
        Concur password.  For some users this will be a mobile specific password.  Previously called a PIN.
 @param success
        Success callback block.  This block is called on successful login.
 @param failure
        Failure callback block.  This block is called on login failure with a @c CTEError @c.
 */
+ (void)loginWithUsername:(NSString *)username Password:(NSString *)password
                  success:(void (^)())success
                  failure:(void (^)(CTEError *error))failure;

/**
 Login to concur with username and password. Also accepts the mobile pin instead of password.
 This version is for ConcurMobile.

 @param username
        Concur username. This is usually work email.
 @param password
        Concur password.  For some users this will be a mobile specific password.  Previously called a PIN.
 @param success
        Success callback block.  This block is called on successful login. Includes the login response XML.
 @param failure
        Failure callback block.  This block is called on login failure with a @c CTEError @c.
 */
+ (void)loginConcurMobileWithUsername:(NSString *)username Password:(NSString *)password
                              success:(void (^)(NSString *loginXML))success
                              failure:(void (^)(CTEError *error))failure;

/**
 Login to concur with username and password.  Also accepts the mobile pin instead of password.
 This version is blocking!  Mainly used in unit tests.
 
 @param username
        Concur username. This is usually work email.
 @param password
        Concur password.  For some users this will be a mobile specific password.  Previously called a PIN.
 @return YES if login was successful
 */
+ (BOOL)loginWithUsername:(NSString *)username Password:(NSString *)password;

/**
 Lookup the Company Single Sign On URL with a company code.
 
 @param companyCode
        Company code used to lookup the single sign on webpage URL.  This is obtained from the Concur website or from the company admin.
 @param success
        Success callback block.  This block is called on successful lookup with the single sign on @c url @c.
 @param failure
        Failure callback block.  This block is called on lookup failure with a @c CTEError @c.
 */
+ (void)lookupCompanySingleSignOnURLByCompanyCode:(NSString *)companyCode
                                          success:(void (^)(NSString *url))success
                                          failure:(void (^)(CTEError *error))failure;

/**
 Lookup user by email.  This will determine user login type and server.  
 
 This does not authenticate the user.
 
 @param emailOrUsername
        Concur registered work email or Concur Username
 @param success
        Success callback block.  This block is called on successful lookup with a @c CTEUserLookupResult @c.
 @param failure
        Failure callback block.  This block is called on lookup failure with a @c CTEError @c.
 */
+ (void)lookupUserByEmailOrUsername:(NSString *)emailOrUsername
                  success:(void (^)(CTEUserLookupResult *result))success
                  failure:(void (^)(CTEError *error))failure;

/**
 Request a MobilePassword (PIN) reset email be sent to the user's registered email.

 @param email
        Concur registered work email.  This is usually also the Concur login username.
 @param success
        Success callback block.  This block is called on successful lookup with the @c verificationKey @c.
 @param failure
        Failure callback block.  This block is called on lookup failure with a @c CTEError @c.
 */
+ (void)requestMobilePasswordResetWithEmail:(NSString *)email
                                    success:(void (^)(NSString *verificationKey, NSString *passwordHelpText))success
                                    failure:(void (^)(CTEError *error))failure;

/**
 Request a Password reset email be sent to the user's registered email.

 @param email
        Concur registered work email.  This is usually also the Concur login username.
 @param success
        Success callback block.  This block is called on successful lookup with the @c verificationKey @c.
 @param failure
        Failure callback block.  This block is called on lookup failure with a @c CTEError @c.
 */
+ (void)requestPasswordResetWithEmail:(NSString *)email
                              success:(void (^)(NSString *verificationKey, NSString *passwordHelpText))success
                              failure:(void (^)(CTEError *error))failure;

/**
 Reset the Mobile Password (PIN).

 @param email
        Concur registered work email.  This is usually also the Concur login username.
 @param newMobilePassword
        New mobile password
 @param keyFromSDK
        Key provided by SDK when a reset is requested
 @param keyFromEmail
        Key provided via email when a reset is requested
 @param success
        Success callback block.  This block is called on successful mobile password change.
 @param failure
        Failure callback block.  This block is called on lookup failure with a @c CTEError @c.
 */
+ (void)resetMobilePasswordWithEmail:(NSString *)email
                   newMobilePassword:(NSString *)newMobilePassword
                          keyFromSDK:(NSString *)keyFromSDK
                        keyFromEmail:(NSString *)keyFromEmail
                             success:(void(^)())success
                             failure:(void (^)(CTEError *error))failure;

/**
 Reset the Concur Password.

 @param email
        Concur registered work email.  This is usually also the Concur login username.
 @param newPassword
        New concur password
 @param keyFromSDK
        Key provided by SDK when a reset is requested
 @param keyFromEmail
        Key provided via email when a reset is requested
 @param success
        Success callback block.  This block is called on successful password change.
 @param failure
        Failure callback block.  This block is called on lookup failure with a @c CTEError @c.
 */
+ (void)resetPasswordWithEmail:(NSString *)email
                   newPassword:(NSString *)newPassword
                    keyFromSDK:(NSString *)keyFromSDK
                  keyFromEmail:(NSString *)keyFromEmail
                       success:(void(^)())success
                       failure:(void (^)(CTEError *error))failure;


@end
