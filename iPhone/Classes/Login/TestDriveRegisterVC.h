//
//  TestDriveRegisterVC.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ExMsgRespondDelegate.h"
@interface TestDriveRegisterVC : UITableViewController <ExMsgRespondDelegate, UITextViewDelegate>

@property (weak, nonatomic) IBOutlet UITextField *testDriveEmail;
@property (weak, nonatomic) IBOutlet UITextField *testDrivePassword;
@property (weak, nonatomic) IBOutlet UIButton *btnSubmitRegistration;

@property (nonatomic, copy) void (^onCloseTestDriveTapped)(void);
- (IBAction)privacyPolicyBtnPressed:(id)sender;
- (IBAction)termsOfUseBtnPressed:(id)sender;
- (IBAction)registerForTestDrive:(id)sender;

@end
