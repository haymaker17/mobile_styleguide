//
//  SigninRegisterVC_ipad.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SigninRegisterVC_ipad : UIViewController

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaLea;
@property (nonatomic, copy) void (^onCloseTestDriveTapped)(void);
- (IBAction)SignInBtnPressed:(id)sender;

@end
