//
//  SigninRegisterVC.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "SigninRegisterVC.h"
#import "UniversalTourVC.h"
#import "ExSystem.h"
#import "LoginViewController.h"
#import "DataConstants.h"
#import "TestDriveRegisterVC.h"
#import "AnalyticsTracker.h"
#import "SignInUserTypeViewController.h"


@interface SigninRegisterVC ()
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrLearnMoreBottom;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coButtonViewBottomSpace;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoVerticalSpace;

@end

@implementation SigninRegisterVC
- (void)viewDidLoad
{
    [super viewDidLoad];
    if ([ExSystem is5]) {
        self.coLogoVerticalSpace.constant = 75;
    }
    
    // MOB-18862 - use same hack as MOB-16251 for new signin flow
    if ([Config isNewSignInFlowEnabled]) {
        SignInUserTypeViewController *loginViewController = (SignInUserTypeViewController*)[ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:@"LOGIN"];
        loginViewController.skipKeyboardDisplayHack = true;
    }
    else{
        // MOB-16251 - this part of the hack forces the keyboard to not appear
        LoginViewController *loginViewController = (LoginViewController*)[ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:@"LOGIN"];
        loginViewController.skipKeyboardDisplayHack = true;
    }

    // This may need to change.  Because "Startup" may not show after successful sign in
    [AnalyticsTracker initializeScreenName:@"Startup"];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [AnalyticsTracker resetScreenName];
}

-(void)viewWillAppear:(BOOL)animated
{
    [self.navigationController setNavigationBarHidden:YES animated:NO];
}

- (IBAction)signInBtnPressed:(id)sender{
    
    NSLog(@"Show Login view");
    // Comments
    if (self.onCloseTestDriveTapped) {
        self.onCloseTestDriveTapped();
    }
    [Flurry logEvent:[NSString stringWithFormat:@"%@:%@", fCatStartup,fNameSignInClick] ];
    
    // MOB-18862 - use same hack as MOB-16251 for new signin flow
    if ([Config isNewSignInFlowEnabled]) {
        SignInUserTypeViewController *loginViewController = (SignInUserTypeViewController*)[ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:@"LOGIN"];
        loginViewController.skipKeyboardDisplayHack = false;
        [loginViewController viewWillAppear:YES];
        [loginViewController setFocusToTextField];
    }
    else{
        // MOB-16251 - this part of the hack forces the keyboard to appear on entry to the login view
        LoginViewController *loginViewController = (LoginViewController*)[ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:@"LOGIN"];
        loginViewController.skipKeyboardDisplayHack = false;
        [loginViewController viewWillAppear:YES];
    }
    
    [AnalyticsTracker initializeScreenName:@"Email Lookup"];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (IBAction)learnMore:(id)sender;
{
    [self performSegueWithIdentifier:@"UniversalTour" sender:self];
 
}

#pragma mark - Navigation

// In a story board-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([segue.identifier isEqualToString:@"LearnMore"])
    {
        UniversalTourVC *controller = (UniversalTourVC *)segue.destinationViewController;
        controller.didSegueFromOtherScreen = YES;
        // Add flurry
        [Flurry logEvent:[NSString stringWithFormat:@"%@:%@", fCatStartup,fNameLearnMoreClick] ];

    }
    if ([segue.identifier isEqualToString:@"RegisterTestDrive"])
    {
        //Add flurry event
        TestDriveRegisterVC *nextVC = (TestDriveRegisterVC *)segue.destinationViewController;
        [nextVC setOnCloseTestDriveTapped:^{
            [self signInBtnPressed:self];
        }];

        [Flurry logEvent:[NSString stringWithFormat:@"%@:%@", fCatStartup,fNameTestDriveClick] ];
    }
    
}



@end
