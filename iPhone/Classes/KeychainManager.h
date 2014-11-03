//
//  KeychainManager.h
//  ConcurMobile
//
//  Created by ernest cho on 12/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface KeychainManager : NSObject

// Used to clean up keychain.
- (void)clearKeychain;
- (void)clearKeychainWithSSKeychain;
- (BOOL)isKeychainEmpty;
- (void)cleanUpOldKeychain;

// Methods used by SignInWithTouchID to save userInput and password on touchID enabled devices
- (NSString *)loadACLuserID;
- (void)saveACLuserID:(NSString*)ACLuserID;
- (void)clearACLuserID;

- (NSString *)loadACLpassword;
- (void)saveACLpassword:(NSString *)ACLpassword;
- (void)clearACLpassword;

// Methods used by ExSystem to save to keychain.
- (NSString *)loadConcurAccessToken;
- (void)saveConcurAccessToken:(NSString *)sToken;

- (NSString *)loadConcurAccessTokenSecret;
- (void)saveConcurAccessTokenSecret:(NSString *)cSecret;

- (NSString *)loadCompanySSOLoginPageUrl;
- (void)saveCompanySSOLoginPageUrl:(NSString *)ssoUrl;
- (void)clearCompanySSOLoginPageUrl;

- (NSString *)loadPin;
- (void)savePin:(NSString *)sPin;

- (NSString *)loadSession;
- (void)saveSession:(NSString *)sSession;

- (NSString *)loadUserId;
- (void)saveUserId:(NSString *)sUserId;
- (void)clearUserId;

- (NSString *)loadUserInputOnLogin;
- (void)saveUserInputOnLogin:(NSString *)sUserInputOnLogin;
- (void)clearUserInputOnLogin;

- (NSString *)loadCompanyCode;
- (void)saveCompanyCode:(NSString *)cCode;
- (void)clearCompanyCode;

// Methods used by the pin reset workflow to save to keychain.
- (NSString *)loadPinResetEmailToken;
- (void)savePinResetEmailToken:(NSString *)token;

- (NSString *)loadPinResetClientToken;
- (void)savePinResetClientToken:(NSString *)token;

@end
