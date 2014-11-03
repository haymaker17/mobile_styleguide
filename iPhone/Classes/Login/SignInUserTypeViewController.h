//
//  SignInUserTypeViewController.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RotatingBaseVC.h"

@interface SignInUserTypeViewController : RotatingBaseVC

@property (weak, nonatomic) IBOutlet UITextField *txtWorkEmailField;

// MOB-18862 - use same hack as MOB-16251 for new signin flow
@property bool skipKeyboardDisplayHack;
-(NSString *) getViewIDKey;
-(void)setFocusToTextField;


@end
