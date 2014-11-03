//
//  TestDriveRegisterVC.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TestDriveRegisterVC.h"
#import "UniversalTourVC.h"
#import "ApplicationLock.h"
#import "AppsUtil.h"
#import "LoginViewController.h"
#import "WaitViewController.h"
#import "TestDriveTourPageViewController_iPhone.h"
#import "NSString+Additions.h"
#import "SignInUserTypeViewController.h"
#import "AnalyticsTracker.h"

@interface TestDriveRegisterVC ()

@property AlertTag alertTag;
@property int retryCount;

@end

@implementation TestDriveRegisterVC

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // MOB-16230 - hook up gesture recognizer to background tap. any touch should dismiss keyboard
    UITapGestureRecognizer* gestureRecognizer = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(backgroundTap:)];
    [self.view addGestureRecognizer:gestureRecognizer];
    
    [self.testDriveEmail setDelegate:(id)self];
    [self.testDrivePassword setDelegate:(id)self];
        // For Flurry : Track how many times user retried.
    self.retryCount = 0;
    
    [AnalyticsTracker initializeScreenName:@"Test Drive Registration"];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    //MOB-16321 Set this title everytime since the viewWillDissappear sets the title to back
    self.title = @"Test Drive";
    [self.navigationController setNavigationBarHidden:NO animated:NO];

}

-(void) viewWillDisappear:(BOOL)animated {
    // Added for flurry event for back button
    if ([self.navigationController.viewControllers indexOfObject:self] == NSNotFound) {
        // back button was pressed.  We know this is true because self is no longer
        // in the navigation stack.
        [Flurry logEvent:[NSString stringWithFormat:@"%@,%@", fCatTestdriveRegistration,fNameBackButtonClick] ];
    }
    
    [AnalyticsTracker resetScreenName];
    [super viewWillDisappear:animated];
}

-(void) viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (IBAction)backgroundTap:(id)sender
{
    // This is called when the little keyboard image is pressed on the iPhone indicating that the keyboard should be dismissed
   [self.view endEditing:YES];
}

/**
 Resets the input fields and sets the cursor on specific field. Always clears password for security.
 */
-(void)resetInputFields
{
    [self.testDrivePassword setText:@""];
    if([self.testDrivePassword respondsToSelector:@selector(hideFloatingLabel)])
        [self.testDrivePassword performSelector:(@selector(hideFloatingLabel)) withObject:nil afterDelay:0.0];
    
    switch (self.alertTag) {
        case kInvalidEmail:
            [self.testDriveEmail becomeFirstResponder];
            break;
        case kInvalidPassword:
            [self.testDrivePassword becomeFirstResponder];
            break;
        default:
            self.testDriveEmail.text = @"";
            if([self.testDriveEmail respondsToSelector:@selector(hideFloatingLabel)])
                [self.testDriveEmail performSelector:(@selector(hideFloatingLabel)) withObject:nil afterDelay:0.0];
            [self.testDriveEmail becomeFirstResponder];
            break;
    }
}


-(void) didProcessMessage:(Msg *)msg
{
    if ([msg.idKey isEqualToString:AUTHENTICATION_DATA])
    {
        // When user registers with the Testdrive is successful MWS returns a login response with sessionid and roles
        // So a success from MWS means login the user into concur app.
        // If there was an error MWS responds with error code <Code>RegTestDriveUserExistError</Code> and the usermessage contains the reason.
        Authenticate *auth = (Authenticate *)msg.responder;
        if([auth.commonResponseCode lengthIgnoreWhitespace])
        {
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            NSString *errTitle = @"Error";
            UIAlertView *alert = nil;
            
            if([auth.commonResponseCode isEqualToString:RegTestDriveUserExistError])
            {
                errTitle = AccountAlreadyExistsMsg;
                self.alertTag = kInvalidEmail;
                alert  = [[MobileAlertView alloc]
                          initWithTitle:errTitle
                          message:@"To access your Concur account, select Sign In"
                          delegate:self
                          cancelButtonTitle:@"Sign In"
                          otherButtonTitles:nil, nil];
                alert.tag = 100;
                //Add flurry
                NSDictionary *dict = @{@"Faiure": @"Account Already Exists"};
                NSString *event = [NSString stringWithFormat:@"%@:%@",fCatTestdriveRegistration, fNameSubmitRegistrationFailure ] ;
                [Flurry logEvent:event withParameters:dict ];
            }
            // Message : The password is too short.  Passwords must be at least 7 characters
            else    // This means that the passwored validation failed
            {
                alert  = [[MobileAlertView alloc]
                          initWithTitle:errTitle
                          message:auth.commonResponseUserMessage
                          delegate:self
                          cancelButtonTitle:@"OK"
                          otherButtonTitles:nil];
                self.alertTag = kInvalidPassword;
                NSDictionary *dict = @{@"Faiure": @"Password Too Short"};
                NSString *event = [NSString stringWithFormat:@"%@:%@",fCatTestdriveRegistration, fNameSubmitRegistrationFailure ] ;
                [Flurry logEvent:event withParameters:dict ];

            }

            [alert show];
            self.retryCount++;
            //Add flurry
            NSDictionary *dict = @{@"Success": @"NO"};
            NSString *event = [NSString stringWithFormat:@"%@:%@",fCatTestdriveRegistration, fNameSubmitRegistration ] ;
            [Flurry logEvent:event withParameters:dict ];
            return;

        }
        // Checking session in addition to response code to work around a bug where 200 was returned
        // even though a session was never created.
        if (msg.responseCode == 200 && auth.sessionID != nil && [auth.sessionID lengthIgnoreWhitespace] > 0)
        {
            //Add flurry
            NSDictionary *dict = @{@"Success": @"YES"};
            NSString *event = [NSString stringWithFormat:@"%@:%@",fCatTestdriveRegistration, fNameSubmitRegistration ] ;
            [Flurry logEvent:event withParameters:dict ];
            // Set pin to nil if any. since we login with password
            [ExSystem sharedInstance].pin = nil;
            // Save password
            // We do NOT support offline login, so don't save digest
            //[[ExSystem sharedInstance] saveDigestForPassword:auth.password];
            [ExSystem sharedInstance].userName = self.testDriveEmail.text;
            [[ExSystem sharedInstance] saveSettings];
            // Save session details
            [[ExSystem sharedInstance] updateSettings:msg];
            
            //close wait view after login succeeded - add delay ??
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];

            // Show the Test drive tour screens
            [self performSegueWithIdentifier:@"PagedTestDriveTour" sender:self];


            // Find the login view and close that view so home can be displayed.
            // This is required step since loginAndAllowAutoLogin checks if this view is displayed before proceeding
            MobileViewController *loginViewController = [ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:@"LOGIN"];
            
            // MOB-19970 We don't need flurry for counting signinretry times anymore.
            
            // Registration success retry
            if(self.retryCount > 0)
            {
                dict = @{@"Registration Attempt Count":[NSString stringWithFormat:@"%@" ,self.retryCount > 5 ? @"Over 5" : [NSString stringWithFormat:@"%d",self.retryCount] ] };
                event = [NSString stringWithFormat:@"%@:%@",fCatTestdriveRegistration, fNameSubmitRegistrationSuccess ] ;
                [Flurry logEvent:event withParameters:dict ];
            }
            //MOB-18179
            if ([Config isNewSignInFlowEnabled]) {
                
                //MOB-18179: If using new sign in Home will take care of removing the storyboard and set the topview accordingly.
                [ConcurMobileAppDelegate unwindToRootView];
                [ApplicationLock sharedInstance].isUserLoggedIn  = YES;
                [ApplicationLock sharedInstance].isShowLoginView = YES;
                [[ApplicationLock sharedInstance] onLoginSucceeded:msg];
                
            }
            else
            {
                
                [loginViewController dismissViewControllerAnimated:NO completion:^{
                    // Do a manual login process similar to what ApplicationLock would do.
                    // This is different for iPad as dismissviewcontrolelr doesnt seem to remove the login view from the navstack.
                    [ConcurMobileAppDelegate unwindToRootView];
                    
                    [ApplicationLock sharedInstance].isUserLoggedIn  = YES;
                    [ApplicationLock sharedInstance].isShowLoginView = YES;
                    
                    [[ApplicationLock sharedInstance] onLoginSucceeded:msg];
                 }];

            }
 
        }
        else
        {
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            // MOB-16349
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle: @"Network Error"
                                  message:@"There was an error processing your request. Please try again"
                                  delegate:self
                                  cancelButtonTitle:@"OK" otherButtonTitles:nil];
            alert.tag = 101;
            self.alertTag = kInvalidPassword;
            [alert show];
            
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"TestDrive Registration failed %@ , msg.responseCode : %d", [ExSystem sharedInstance].userName, msg.responseCode] Level:MC_LOG_ERRO];
            //Add flurry
            NSDictionary *dict = @{@"Success": @"NO"};
            NSString *event = [NSString stringWithFormat:@"%@:%@",fCatTestdriveRegistration, fNameSubmitRegistration ] ;
            [Flurry logEvent:event withParameters:dict ];
            
            //Add flurry
            dict = @{@"Faiure": @"Server Error"};
            event = [NSString stringWithFormat:@"%@:%@",fCatTestdriveRegistration, fNameSubmitRegistrationFailure ] ;
            [Flurry logEvent:event withParameters:dict ];
            self.retryCount++;
        }
 
    }
}



- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)registerForTestDrive:(id)sender
{
    self.retryCount++;
    [self.testDrivePassword resignFirstResponder];
    NSLog(@"Submit Test Drive Registration");

    // Do some validation.valid email validation
    // Create a registration object and submit
    if(![self.testDriveEmail.text lengthIgnoreWhitespace] || ![self.testDrivePassword.text lengthIgnoreWhitespace])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:@"Error"
                              message:@"Email/Password cannot be empty."
                              delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alert show];
        
        if(![self.testDriveEmail.text lengthIgnoreWhitespace])
            self.alertTag = kInvalidEmail;
        else
           self.alertTag = kInvalidPassword;
        
        //Add flurry
        NSDictionary *dict = dict = @{@"Faiure": @"Empty Email and Password"};
        
        if(![self.testDriveEmail.text lengthIgnoreWhitespace] && [self.testDrivePassword.text lengthIgnoreWhitespace] )
          dict = @{@"Faiure": @"Empty Email"};
        else if([self.testDriveEmail.text lengthIgnoreWhitespace] && ![self.testDrivePassword.text lengthIgnoreWhitespace] )
            dict = @{@"Faiure": @"Empty Password"};
        
        NSString *event = [NSString stringWithFormat:@"%@:%@",fCatTestdriveRegistration, fNameSubmitRegistrationFailure ] ;
        [Flurry logEvent:event withParameters:dict ];
        
        return;

    }
    
    if(![self.testDriveEmail.text isValidEmail] )
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:@"Error"
                              message:@"Please enter a valid email in the Email Address field"
                              delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alert show];
        self.alertTag = kInvalidEmail;
        //Add flurry
        NSDictionary *dict = dict = @{@"Faiure": @"Not An Email"};
        NSString *event = [NSString stringWithFormat:@"%@:%@",fCatTestdriveRegistration, fNameSubmitRegistrationFailure ] ;
        [Flurry logEvent:event withParameters:dict ];

        return;
    }

    if(![ExSystem connectedToNetwork])
	{
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:@"Currently Offline"
                              message:@"Actions offline"
                              delegate:nil
                              cancelButtonTitle:@"OK"
                              otherButtonTitles:nil];
        [alert show];
        
        //Add flurry
        NSDictionary *dict = @{@"Faiure": @"Offline"};
        NSString *event = [NSString stringWithFormat:@"%@:%@",fCatTestdriveRegistration, fNameSubmitRegistrationFailure ] ;
        [Flurry logEvent:event withParameters:dict ];

        return;
    }
    
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Registering user for TestDriver with id: %@", [ExSystem sharedInstance].userName] Level:MC_LOG_INFO];
    //
    // Get the OAuth stuff and submit the username password.
    NSString *a = [[MCLogging getInstance] getMessageForField:@"Invalid Field Value"];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:LOGIN, @"TO_VIEW"
                                 , self.testDriveEmail.text, @"USER_ID", self.testDrivePassword.text,@"PASSWORD", @"YES", @"SKIP_CACHE", a, @"TEST_DRIVE", nil];
    [[ExSystem sharedInstance].msgControl createMsg:AUTHENTICATION_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    
    [WaitViewController showWithText:@"Registering for Test Drive,\n please wait..." animated:YES];
 
}

#pragma mark - handle alerts
- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == 100)
    {
        NSDictionary *dict = nil;
        if (buttonIndex == alertView.cancelButtonIndex)
        {
            dict = @{@"Choice": @"Sign In"};
            [self closeStoryBoard];
        }
        else    // Account already exists and user wants to retry
        {
            // Add flurry
            dict = @{@"Choice": @"Retry"};
        }
        NSString *event = [NSString stringWithFormat:@"%@:%@",fCatTestdriveRegistration, fNameSubmitRegistrationAccountAlreadyExists ] ;
        [Flurry logEvent:event withParameters:dict ];

    }

    [self resetInputFields];
}

-(void)closeStoryBoard
{
    // Close this storyboard view
    
    NSLog(@"Close Testdrive Storyboard");
    
    NSArray *viewControllers = [ConcurMobileAppDelegate getAllViewControllers];
    
    if ([[viewControllers lastObject] isKindOfClass:[SignInUserTypeViewController class]]) {
        SignInUserTypeViewController *signInVC = (SignInUserTypeViewController*)[viewControllers lastObject];
            signInVC.txtWorkEmailField.text = self.testDriveEmail.text;
        }
    if (self.onCloseTestDriveTapped) {
        self.onCloseTestDriveTapped();
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    //
    // this method handles next / register button pressed on iphone and ipad for username and password fields
	//
    
    // next was pressed on username field, give focus to password field
    if(textField == self.testDriveEmail)
    {
        [self.testDrivePassword becomeFirstResponder];
    }
    else if(textField == self.testDrivePassword)
    {
        [self registerForTestDrive:nil];
    }

    return YES;
}

// MOB-16321 - On iPhone 4 scroll the table so it can reveal the submit button
-(void) textFieldDidBeginEditing:(UITextField *)textField {
    
    CGRect textFieldRect = [self.btnSubmitRegistration frame];
    [self.tableView setContentOffset:CGPointMake(0, textFieldRect.size.height) animated:YES];
}


#pragma mark - Navigation

// In a story board-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([segue.identifier isEqualToString:@"MoreInfo"])
    {
        UniversalTourVC *controller = (UniversalTourVC *)segue.destinationViewController;
        controller.didSegueFromOtherScreen = YES;
        [controller setOnCloseTestDriveTapped:^{
            if(self.onCloseTestDriveTapped)
                self.onCloseTestDriveTapped();
        }];
    }
    
    if ([segue.identifier isEqualToString:@"PagedTestDriveTour"]) {
        TestDriveTourPageViewController_iPhone *ctrl = segue.destinationViewController;
        [ctrl setOnSkipTapped:^{
            if(self.onCloseTestDriveTapped)
                self.onCloseTestDriveTapped();
        }];
    }
}


- (IBAction)privacyPolicyBtnPressed:(id)sender
{
    [AppsUtil showTestDrivePrivacyLink];
}

- (IBAction)termsOfUseBtnPressed:(id)sender
{
    [AppsUtil showTestDriveTermsofUse];
}


@end
