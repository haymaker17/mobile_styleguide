//
//  SignInPasswordViewController.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/17/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RotatingBaseVC.h"
#import "CTEError.h"

@interface SignInPasswordViewController : RotatingBaseVC
@property (strong, nonatomic) NSString   *userId;
@property (strong, nonatomic) NSString   *secret;
@property (strong, nonatomic) NSString   *userEmail;
@property SignInUserType signInUserType;

// Public for SignInWithTouchID to use;
-(void) loginAndShowHome:(NSString *)loginXML;
-(void) handleSignInError:(CTEError *)error;
@end
