//
//  TestDriveRegisterVC_ipad.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TestDriveRegisterVC_ipad : UIViewController<ExMsgRespondDelegate, UITextViewDelegate>

@property (weak, nonatomic) IBOutlet UITextField *testDriveEmail;
@property (weak, nonatomic) IBOutlet UITextField *testDrivePassword;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaHeight;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coEmailTxtFieldTop;

@property (nonatomic, copy) void (^onCloseTestDriveTapped)(void);
- (IBAction)SignInPressed:(id)sender;
- (IBAction)privacyPolicyBtnPressed:(id)sender;
- (IBAction)termsOfUseBtnPressed:(id)sender;
- (IBAction)registerForTestDrive:(id)sender;

@end
