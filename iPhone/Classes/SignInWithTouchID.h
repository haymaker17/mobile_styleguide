//
//  SignInWithTouchID.h
//  ConcurMobile
//
//  Created by Shifan Wu on 8/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface SignInWithTouchID : NSObject

#pragma mark -
#pragma mark Keychain stuff
- (NSString *)loadACLuserID;
- (void)saveACLuserID:(NSString*)ACLuserID;
- (void)clearACLuserID;

- (NSString *)loadACLpassword;
- (void)saveACLpassword:(NSString *)ACLpassword;
- (void)clearACLpassword;

+ (BOOL) canEvaluatePolicy;

- (void) signInWithUserName:(NSString *) userName Passwrod:(NSString *) password;
@end
