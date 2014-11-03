//
//  SigninRegisterVC_ipad.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "SigninRegisterVC_ipad.h"
#import "UniversalTourVC_ipad.h"
#import "LoginViewController.h"
#import "TestDriveRegisterVC_ipad.h"
#import "AnalyticsTracker.h"

@interface SigninRegisterVC_ipad ()

@end

@implementation SigninRegisterVC_ipad

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self.navigationController setNavigationBarHidden:YES animated:NO];
	// Do any additional setup after loading the view.

    // MOB-16251 - this part of the hack forces the keyboard to not appear
    LoginViewController *loginViewController = (LoginViewController*)[ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:@"LOGIN"];
    loginViewController.skipKeyboardDisplayHack = true;
    
    // This may need to change.  Because "Startup" may not show after successful sign in
    [AnalyticsTracker initializeScreenName:@"Startup"];
}

-(void)viewWillAppear:(BOOL)animated
{
    [self.navigationController setNavigationBarHidden:YES animated:NO];
    [self willAnimateRotationToInterfaceOrientation:[UIApplication sharedApplication].statusBarOrientation duration:0];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [AnalyticsTracker resetScreenName];
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration{
    
    if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
        self.coLogoAreaTop.constant = 120;
        self.coLogoAreaLea.constant = 30;
        self.coFormAreaTop.constant = 120;
        self.coFormAreaLea.constant = 520;
    } else if (UIInterfaceOrientationIsPortrait(toInterfaceOrientation)) {
        self.coLogoAreaTop.constant = 100;
        self.coLogoAreaLea.constant = 148;
        self.coFormAreaTop.constant = 350;
        self.coFormAreaLea.constant = 168;
    }
    [self.view setNeedsUpdateConstraints];
    [UIView animateWithDuration:duration animations:^{
        [self.view layoutIfNeeded];
    }];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)SignInBtnPressed:(id)sender
{
    NSLog(@"Show Login view");
    
    if (self.onCloseTestDriveTapped) {
        self.onCloseTestDriveTapped();
    }
    [Flurry logEvent:[NSString stringWithFormat:@"%@,%@", fCatStartup,fNameSignInClick] ];
    // MOB-16251 - this part of the hack forces the keyboard to appear on entry to the login view
    LoginViewController *loginViewController = (LoginViewController*)[ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:@"LOGIN"];
    loginViewController.skipKeyboardDisplayHack = false;
    [loginViewController viewWillAppear:YES];
    
    [AnalyticsTracker initializeScreenName:@"Email Lookup"];
}

// In a story board-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([segue.identifier isEqualToString:@"LearnMore"])
    {
        UniversalTourVC_ipad *nextVC = (UniversalTourVC_ipad *)segue.destinationViewController;
        nextVC.didSegueFromOtherScreen = YES;
        
        // Add flurry
        [Flurry logEvent:[NSString stringWithFormat:@"%@,%@", fCatSignIn,fNameLearnMoreClick] ];
    }
    if ([segue.identifier isEqualToString:@"RegisterTestDrive"])
    {
        TestDriveRegisterVC_ipad *nextVC = (TestDriveRegisterVC_ipad *)segue.destinationViewController;
        [nextVC setOnCloseTestDriveTapped:^{
            [self SignInBtnPressed:self];
        }];
        //Add flurry event
        [Flurry logEvent:[NSString stringWithFormat:@"%@,%@", fCatStartup,fNameTestDriveClick] ];
    }

}
@end
