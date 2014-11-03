//
//  SignInUserTypeViewController.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SignInUserTypeViewController.h"
#import "SignInPasswordViewController.h"
#import "SettingsViewController.h"
#import "CustomBackButton.h"
#import "SettingsButton.h"
#import "CTELogin.h"
#import "WaitViewController.h"
#import "CTENetworkSettings.h"
#import "SignInSSOWebViewController.h"
#import "CTEErrorMessage.h"
#import "AnalyticsTracker.h"
#import "LoginOptionsViewController.h"
#import "MCLogging.h"
#import "SignInWithTouchID.h"

@interface SignInUserTypeViewController ()

@property (weak, nonatomic) IBOutlet UIButton   *btnSSOSignIn;
@property (strong, nonatomic) NSString          *userId;
@property (strong, nonatomic) NSString          *workEmail;
@property (strong, nonatomic) NSString          *ssoURL;
@property (strong, nonatomic) NSString          *segueToScreen;
@property SignInUserType signInUserType;


#pragma  mark - New storyboard handlers for Signin.storyboard

@property (weak, nonatomic) IBOutlet UIButton *btnContinue;

- (IBAction) btnContinueTapped:(id)sender;
- (IBAction) btnSSOSignInTapped:(id)sender;
- (IBAction)btnTouchIDTapped:(id)sender;

@end

@implementation SignInUserTypeViewController

NSString * const constShowSSOLoginScreen = @"ShowSSOLoginScreen";
NSString * const constShowPasswordScreen = @"ShowPasswordScreen";

// TODO : Add diagnostic logging to all the login flow.
- (void)appDidEnterForeground:(NSNotification *)notification
{
    if ([[ExSystem sharedInstance].entitySettings.enableTouchID isEqualToString:@"YES"] && [Config isTouchIDEnabled] && ![[ApplicationLock sharedInstance] isLoggedIn] && self.ssoURL == nil)
    {
        [self btnTouchIDTapped:self];
    }
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillEnterForegroundNotification object:nil];
}

- (void)viewDidLoad
{
    self.txtWorkEmailField.delegate = self;
    UIButton* btnSettings = [[SettingsButton alloc]init];
    [btnSettings addTarget:self action:@selector(buttonSettingsPressed:) forControlEvents:UIControlEventTouchUpInside];
        self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:btnSettings];
        
    if ([ConcurTestDrive isAvailable]){
        if ([ExSystem is7Plus]) {
            CustomBackButton *btn = [[CustomBackButton alloc]init];
            [btn addTarget:self action:@selector(showTestDrive) forControlEvents:UIControlEventTouchUpInside];
            UIBarButtonItem *backButton = [[UIBarButtonItem alloc]initWithCustomView:btn];
            self.navigationItem.leftBarButtonItem = backButton;
        } else  {
            // Show a normal back button.
            self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[@"Back" lowercaseString] style:UIBarButtonItemStyleBordered target:self action:@selector(showTestDrive)];
        }
    }
    
    self.title = [@"Sign In" localize];
    [self.btnContinue setTitle:[@"Continue" localize] forState:UIControlStateNormal];
    [self.txtWorkEmailField setPlaceholder:[@"Work email or Concur username" localize]];
    [self.btnSSOSignIn setTitle:[@"Company Sign On" localize] forState:UIControlStateNormal];
    
	// Do any additional setup after loading the view.
    self.userId = @"";
    // set the server path to the path specified in the settings.
    [[CTENetworkSettings sharedInstance] saveServerURL:[ExSystem sharedInstance].entitySettings.uri];
    
    //MOB-19311 : Set the user agent when user first start logging.
    // once the user is logged the MWS picks up the user-agent values automatically from the session.
    [[CTENetworkSettings sharedInstance] setUserAgentString:[MCLogging getDeviceDesc]];
    
    // if there is SSO URL then autoforward to sso web UI
    self.ssoURL = [[ExSystem sharedInstance] loadCompanySSOLoginPageUrl];

    if (self.ssoURL != nil)
    {
        [self performSegueWithIdentifier:constShowSSOLoginScreen sender:self];
    }
    
    [super viewDidLoad];
    
    [AnalyticsTracker initializeScreenName:@"Email Lookup"];
    
    //TouchID
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appDidEnterForeground:) name:UIApplicationWillEnterForegroundNotification object:nil];
}

-(void)viewWillAppear:(BOOL)animated
{
    // Check settings if save username is set if so show it in the text field.
    if ([ExSystem sharedInstance].entitySettings.saveUserName != nil &&
        [[ExSystem sharedInstance].entitySettings.saveUserName isEqualToString:@"YES"] )
    {
        self.userId = [ExSystem sharedInstance].userName;
        self.workEmail = [ExSystem sharedInstance].entitySettings.email;
        
        if ([[ExSystem sharedInstance].userInputOnLogin lengthIgnoreWhitespace])
        {
            self.txtWorkEmailField.text = [ExSystem sharedInstance].userInputOnLogin;
        }
        // For existing users - Get the userid or email and show it up in the text box.
        else if ([self.userId lengthIgnoreWhitespace]) {
            self.txtWorkEmailField.text = self.userId;
        }
        else
        {
            self.txtWorkEmailField.text = [ExSystem sharedInstance].entitySettings.email;
        }
        
    }

    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];

    // TouchID Flow
    if ([[ExSystem sharedInstance].entitySettings.enableTouchID isEqualToString:@"YES"] && [Config isTouchIDEnabled] && self.ssoURL == nil)
    {
        [self btnTouchIDTapped:self];
    }
    else
    {
        // MOB-18862 - use same hack as MOB-16251 for new signin flow
        [self setFocusToTextField];
        //MOB-18465
    }
    [[ApplicationLock sharedInstance] onLoginViewAppeared];
}

-(void)viewDidDisappear:(BOOL)animated
{
    [AnalyticsTracker resetScreenName];
}

-(void)showTestDrive
{
    [self.view endEditing:YES];
    [[ConcurTestDrive sharedInstance] showTestDriveAnimated:YES];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)btnSSOSignInTapped:(id)sender
{
    LoginOptionsViewController* loginOptionVC = [[LoginOptionsViewController alloc] initWithNibName:@"LoginOptionsViewController" bundle:nil];
    [[self navigationController] pushViewController:loginOptionVC animated:YES];
}

- (IBAction)btnTouchIDTapped:(id)sender
{
    SignInWithTouchID *touchID = [[SignInWithTouchID alloc] init];
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void){
        NSString *password = [touchID loadACLpassword];
        
        if( [self.userId lengthIgnoreWhitespace] && [password lengthIgnoreWhitespace] )
        {
            [self signInWithTouchID:touchID userID:self.userId password:password];
        }
    });
}

- (void)signInWithTouchID:(SignInWithTouchID *)obj userID:(NSString *)userID password:(NSString *)password
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [obj signInWithUserName:self.userId Passwrod:password];
    });
}

- (IBAction)btnContinueTapped:(id)sender {
    
    // Do the validations and error messages here.
    self.workEmail = self.txtWorkEmailField.text;
    if (![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Offline"]
                              message:[Localizer getLocalizedText:@"Please wait until you are online to login"]
                              delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
        [alert show];
        
        NSString *eventLabel = [NSString stringWithFormat:@"Type: %@", @"Offline"];
        [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Email Lookup Failure" eventLabel:eventLabel eventValue:nil];
        return;
        [self resetInputFields];
    }

    if (![self.workEmail lengthIgnoreWhitespace] || ![NSString isValidConcurUserId:self.workEmail])
    {
        
        // Show error message for
        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Invalid Email or Username"]
                               message:[NSString stringWithFormat:[Localizer getLocalizedText:@"Your entry does not appear to be a valid work email or Concur Username. For example"], @"\n\n"]
                               delegate:self
                               cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                                   otherButtonTitles:nil];
        [av show];
        self.alertTag = kInvalidEmail;
        [self resetInputFields];
        NSString *eventLabel = [NSString stringWithFormat:@"Type: %@", @"Format Issue"];
        [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Email Lookup Failure" eventLabel:eventLabel eventValue:nil];
        return;
        
    }
    
    // make the network call and if success then show password screen
    [self.view endEditing:YES];
    [WaitViewController showWithText:@"" animated:YES];
    
    [CTELogin lookupUserByEmailOrUsername:self.workEmail success:^(CTEUserLookupResult *result) {
        
            // If success then go to next screen
             ALog(@"::Look up email successful::Username : %@::", [result username]);

                self.userId = result.username;
                self.workEmail = result.email;
                [ExSystem sharedInstance].userInputOnLogin = self.txtWorkEmailField.text;
                [ExSystem sharedInstance].userName = self.userId;
                [ExSystem sharedInstance].entitySettings.email = self.workEmail;
                [[ExSystem sharedInstance] clearUserCredentialsAndSession]; // PIN will be saved later if and when authentication succeeds
                [[ExSystem sharedInstance] saveSettings];

                if (result.isSingleSignOnUser) {
                    // Do an SSO Sign in with the URL here.
                    // Get the SSO URL and show the
                    self.signInUserType = kSSOUser;
                    self.ssoURL = [result companySingleSignOnURL];
                    // go to webview
                    self.segueToScreen = constShowSSOLoginScreen;
                    DLog(@"::SSO User:forwarding to SSO url %@::", self.ssoURL);
                    // MOB-18158 - save SSO url so next time app start will auto fwd to sso url
                    [[ExSystem sharedInstance] saveCompanySSOLoginPageUrl:self.ssoURL];
                }
                else {
                    self.signInUserType = result.isPasswordUser ? kPasswordUser : kMobilePasswordUser ;
                    self.segueToScreen = constShowPasswordScreen ;
                }
        
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            [self performSegueWithIdentifier:self.segueToScreen sender:self];
    
        } failure:^(CTEError *error) {
            
            CTEErrorMessage *cteErrorMessage = nil;
            NSString *errorTitle = [@"Unable to Sign In" localize];
            NSString *errorMessage = nil;
            // Always take first error message in this case.
            if ([error.concurErrorMessages count]> 0 ) {
                cteErrorMessage = error.concurErrorMessages[0];
                errorMessage = cteErrorMessage.userMessage;
                if ([cteErrorMessage.code isEqualToString:@"RATE_LIMIT_1"])
                {
                    errorTitle = [@"Network Error" localize];
                }
            }
            else {
                errorMessage = [@"We are unable to sign you in with this information" localize];
            }
            // log info
            ALog(@"::EmailLookup Failed for user:%@:: with error ::%@::" , self.userId, cteErrorMessage.systemMessage == nil ? cteErrorMessage.systemMessage : error.concurErrorXML);
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            MobileAlertView *alertView = [[MobileAlertView alloc] initWithTitle:errorTitle
                                                                  message:errorMessage
                                                                 delegate:self
                                                        cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                                        otherButtonTitles:nil];
             [alertView show];
             NSString *eventLabel = [NSString stringWithFormat:@"Type: %@", @"Other Error"];
             [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Email Lookup Failure" eventLabel:eventLabel eventValue:nil];
         }];

 }

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:constShowPasswordScreen]) {
        // do something here
        SignInPasswordViewController *nextVC = (SignInPasswordViewController *)segue.destinationViewController;
        nextVC.signInUserType = self.signInUserType;
        nextVC.userEmail = self.workEmail;
        nextVC.userId = self.userId;
         [WaitViewController hideAnimated:YES withCompletionBlock:nil];
    }
    if ([segue.identifier isEqualToString:constShowSSOLoginScreen]) {
        // Segue to SSO webview
        SignInSSOWebViewController *nextVC = (SignInSSOWebViewController*)segue.destinationViewController;
        nextVC.ssoURL = self.ssoURL ;
    }
}

/**
 Resets the input fields and sets the cursor on specific field. Always clears password for security.
 */

-(void)resetInputFields
{
    if (kInvalidEmail) {
        [self.txtWorkEmailField setText:@""];
        if([self.txtWorkEmailField respondsToSelector:@selector(hideFloatingLabel)])
            [self.txtWorkEmailField performSelector:(@selector(hideFloatingLabel)) withObject:nil afterDelay:0.0];
    }
    // MOB-18862 - use same hack as MOB-16251 for new signin flow
    [self setFocusToTextField];
}

// MOB-18862 - use same hack as MOB-16251 for new signin flow
-(void)setFocusToTextField
{
    if(false == self.skipKeyboardDisplayHack)
    {
        [self.txtWorkEmailField becomeFirstResponder];
    }
}
#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return LOGIN;
}


@end
