//
//  SigninRegisterVC.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SigninRegisterVC : UIViewController

@property (weak, nonatomic) IBOutlet UIButton *SignInBtn;
@property (nonatomic, copy) void (^onCloseTestDriveTapped)(void);
- (IBAction)signInBtnPressed:(id)sender;

- (IBAction)learnMore:(id)sender;
@end
