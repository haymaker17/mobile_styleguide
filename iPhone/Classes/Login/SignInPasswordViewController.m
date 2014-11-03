//
//  SignInPasswordViewController.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/17/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SignInPasswordViewController.h"
#import "SettingsViewController.h"
#import "CustomBackButton.h"
#import "SettingsButton.h"
#import "CTELogin.h"
#import "CTENetworkSettings.h"
#import "WaitViewController.h"
#import "CTEErrorMessage.h"
#import "UserDefaultsManager.h"
#import "AnalyticsTracker.h"

#import "KeychainManager.h"

@interface SignInPasswordViewController ()

@property int tryAgainCount;
@property (weak, nonatomic) IBOutlet UITextField *txtPasswordField;
@property (weak, nonatomic) IBOutlet UIButton *btnForgotPassword;
@property (weak, nonatomic) IBOutlet UIButton *btnSignIn;
@property (weak, nonatomic) IBOutlet UILabel *lblEnterPassword;
@property (weak, nonatomic) IBOutlet UILabel *lblPassword;

@property (strong, nonatomic) KeychainManager *keyChainManager;

- (IBAction)btnForgotPasswordTapped:(id)sender;
- (IBAction)btnSignInTapped:(id)sender;

@end

@implementation SignInPasswordViewController

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
	// Do any additional setup after loading the view.
    
    if ([ConcurTestDrive isAvailable]){
        if ([ExSystem is7Plus]) {
            CustomBackButton *btn = [[CustomBackButton alloc]init];
            [btn addTarget:self action:@selector(goBack) forControlEvents:UIControlEventTouchUpInside];
            UIBarButtonItem *backButton = [[UIBarButtonItem alloc]initWithCustomView:btn];
            self.navigationItem.leftBarButtonItem = backButton;
        } else  {
            // Show a normal back button.
            self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[@"Back" lowercaseString] style:UIBarButtonItemStyleBordered target:self action:@selector(goBack)];
        }
    }
    
    self.txtPasswordField.delegate = self;
    self.title = [@"Sign In" localize];
    
    [super viewDidLoad];

    if (self.signInUserType == kPasswordUser) {
        [self.btnForgotPassword setTitle:[@"Forgot password?" localize] forState:UIControlStateNormal];
        [self.txtPasswordField setPlaceholder:[@"Password" localize]];
        self.lblEnterPassword.text = [@"Enter your password to sign in" localize];
        [AnalyticsTracker initializeScreenName:@"Enter Password"];
    }
    else if (self.signInUserType == kMobilePasswordUser)
    {
        [self.btnForgotPassword setTitle:[@"Forgot mobile password (PIN)?" localize] forState:UIControlStateNormal];
        [self.txtPasswordField setPlaceholder:[@"Mobile Password (PIN)" localize]];
        self.lblEnterPassword.text = [@"Enter your mobile password (PIN) to sign in" localize];
        [AnalyticsTracker initializeScreenName:@"Enter Mobile Password"];
    }
    
    _keyChainManager = [[KeychainManager alloc] init];
    self.tryAgainCount = 0;
}

// MOB-18862
- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self.txtPasswordField becomeFirstResponder];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    
    [AnalyticsTracker resetScreenName];
}

- (void)goBack
{
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)btnForgotPasswordTapped:(id)sender {
    NSString *eventLabel = nil;
    switch (self.signInUserType) {
        case kPasswordUser:
            eventLabel = [NSString stringWithFormat:@"Type: %@", @"Password"];
            break;
        case kMobilePasswordUser:
            eventLabel = [NSString stringWithFormat:@"Type: %@", @"Mobile Password"];
            break;
            
        default:
            break;
    }
    [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Request Reset" eventLabel:eventLabel eventValue:nil];
    
    // If there is no email configured then show an alert message that password cannot be reset
    if(![self.userEmail lengthIgnoreWhitespace])
    {
        //show alert message from UX
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle: [@"No Email in Profile" localize]
                              message:[@"You do not have an email in your Concur profile. We are unable to send you a password reset email without one. Please go to the Concur Website and add your work email to your profile." localize]
                              delegate:self
                              cancelButtonTitle:@"OK" otherButtonTitles:nil];
        
        [alert show];
        return;
    }
    
    // Call the forgot password endpoint here with relevant data and show an alert message
    // make approripate call for  "I forgot password" , Call an MWS endpoint for resetPIN or password
    
    [WaitViewController showWithText:@"" animated:YES];
    
    if (self.signInUserType == kMobilePasswordUser) {
        
        [CTELogin requestMobilePasswordResetWithEmail:self.userEmail success:^(NSString *verificationKey, NSString *passwordHelpText) {

            [self handleRequestPasswordResetResponse:verificationKey passwordHelpText:passwordHelpText];
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            
        } failure:^(CTEError *error) {
            
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle: [@"Network Error" localize]
                                  message:[@"There was an error processing your request. Please try again" localize]
                                  delegate:self
                                  cancelButtonTitle:@"OK" otherButtonTitles:nil];
            
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            [alert show];
            
        }];
    }
    else
    {
        
        [CTELogin requestPasswordResetWithEmail:self.userEmail success:^(NSString *verificationKey, NSString *passwordHelpText) {
            
            [self handleRequestPasswordResetResponse:verificationKey passwordHelpText:passwordHelpText];
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            
        } failure:^(CTEError *error) {
            
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle: [@"Network Error" localize]
                                  message:[@"There was an error processing your request. Please try again" localize]
                                  delegate:self
                                  cancelButtonTitle:@"OK" otherButtonTitles:nil];
            
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            [alert show];
            
        }];
        
    } // end library call
}

- (IBAction)btnSignInTapped:(id)sender {
    
    self.secret = self.txtPasswordField.text;
    
    // Check if you are offline
    if (![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Offline"]
                              message:[Localizer getLocalizedText:@"Please wait until you are online to login"]
                              delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
        [alert show];
        
        NSString *eventLabel = [NSString stringWithFormat:@"Type: %@", @"Offline"];
        [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Failed Attempt" eventLabel:eventLabel eventValue:nil];
        return;
    }

    // Validate password
    if(![self.secret lengthIgnoreWhitespace]) {
        // Show error message for
        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Incorrect Password"]
                             message:[Localizer getLocalizedText:@"Try again to re-enter your password or select I Forgot to receive signin in instructions"]
                            delegate:self
                           cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"]
                           otherButtonTitles:[Localizer getLocalizedText:@"I Forgot"] ,nil];
        self.alertTag = kInvalidPassword;
        
        [av show];
        return;
    }
    // After validations are done make the networking call
    [WaitViewController showWithText:@"" animated:YES];
    [self.view endEditing:YES];
    [CTELogin loginConcurMobileWithUsername:self.userId Password:self.secret success:^(NSString *loginXML) {
            //MOB-19311 : Dont have to set the useragent string anymore. The mws will pick it up after the session.
            [[CTENetworkSettings sharedInstance] setUserAgentString:nil];
            ALog(@"::User %@ Login successful::" , self.userId);
            [_keyChainManager saveACLpassword:self.secret];
            [self loginAndShowHome:loginXML];
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            
        } failure:^(CTEError *error) {
            
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            [self handleSignInError:error];
            [_keyChainManager clearACLuserID];
            [_keyChainManager clearACLpassword];
        }];
}

-(void) handleSignInError:(CTEError *)error
{
    
    CTEErrorMessage *cteErrorMessage = nil;
    NSString *errorTitle = nil;
    NSString *errorMessage = nil;
    
    // Always take first error message in this case.
    if ([error.concurErrorMessages count]> 0 )
    {
        cteErrorMessage = error.concurErrorMessages[0];
        if ([cteErrorMessage.code isEqualToString:@"RATE_LIMIT_1"])
        {
            errorTitle = [@"Network Error" localize];
        }
        else
        {
            errorTitle = [@"Incorrect Password" localize];
        }
        errorMessage = cteErrorMessage.userMessage;
        
    }
    else
    {
        errorTitle = [@"Unable to Sign In" localize];
        errorMessage = [@"Try again to re-enter your password or select I Forgot to receive signin in instructions" localize];
        
    }
    
    MobileAlertView *alertView = [[MobileAlertView alloc] initWithTitle:errorTitle
                                                                message:errorMessage
                                                               delegate:self
                                                      cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"]
                                                      otherButtonTitles:[Localizer getLocalizedText:@"I Forgot"] ,nil];
    [alertView show];
    
    // log info
    ALog(@"::Login failed for user:%@:: with error ::%@::" , self.userId, cteErrorMessage.systemMessage == nil ? cteErrorMessage.systemMessage : error.concurErrorXML);
    self.alertTag = kInvalidPassword;
    
    NSString *eventLabel = nil;
    if (error.code == 500)
    {
        eventLabel = [NSString stringWithFormat:@"Type: %@", @"Server Error"];
        [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Failed Attempt" eventLabel:eventLabel eventValue:nil];
    }
    else if (error.code == 403)
    {
        eventLabel = [NSString stringWithFormat:@"Type: %@", @"Forbidden"];
        [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Failed Attempt" eventLabel:eventLabel eventValue:nil];
    }
    else if (error.code == 401)
    {
        // Bad credentials means bad password this case
        eventLabel = [NSString stringWithFormat:@"Type: %@", @"Bad Credentials"];
        [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Failed Attempt" eventLabel:eventLabel eventValue:nil];
    }
}

/**
Handle loginresult xml and do the concur login process
*/
-(void) loginAndShowHome:(NSString *)loginXML
{
    NSString *eventLabel = nil;
    switch (self.signInUserType) {
        case kPasswordUser:
            eventLabel = [NSString stringWithFormat:@"Credential Type: %@", @"Password"];
            break;
        case kMobilePasswordUser:
            eventLabel = [NSString stringWithFormat:@"Credential Type: %@", @"Mobile Password"];
            break;
        
        default:
            break;
    }
    [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Successful Attempt" eventLabel:eventLabel eventValue:nil];

    eventLabel = [NSString stringWithFormat:@"User Type: %@",[[ExSystem sharedInstance] getUserType]];
    [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Successful Attempt" eventLabel:eventLabel eventValue:nil];
    
    // Do a manual login process similar to what ApplicationLock would do.
    // Dont unwind views to home here, try to refresh the calling view so that the login view can be shown anywhere
    // Callback blocks for the calling view controller.
    DLog(@" Login result : %@" , loginXML);
    Authenticate *auth = [[Authenticate alloc] init];

    NSMutableDictionary *pBag = [@{@"USER_ID": self.userId} mutableCopy];
    switch (self.signInUserType) {
        case kPasswordUser:
            pBag[@"PASSWORD"] = self.secret;
            break;
        case kMobilePasswordUser:
            pBag[@"PIN"] = self.secret;
            break;
        default:
            break;
    }

    Msg *msg = [auth newMsg:pBag];
    [msg.responder parseXMLFileAtData:[loginXML dataUsingEncoding:NSUTF8StringEncoding]];
    
    // Handle remote wipe.
    BOOL fRemoteWipe = [self handleRemoteWipe:msg];
    if (fRemoteWipe)
    {
        [self resetInputFields];
        // clean touchID credentials.
        [_keyChainManager clearACLuserID];
        [_keyChainManager clearACLpassword];
        NSString *eventLabel = [NSString stringWithFormat:@"Type: %@", @"Remote Wipe"];
        [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Failed Attempt" eventLabel:eventLabel eventValue:nil];
        return;
    }
    [ApplicationLock sharedInstance].isUserLoggedIn  = YES;
    [ApplicationLock sharedInstance].isShowLoginView = YES;
    //MOB-11715 : Clear corpSSO flag and handle sign in
    // If user switches between SSO and normal user then Exsystem will set the isCorpSSO to YES since the url is saved.
    [ExSystem sharedInstance].isCorpSSOUser = NO;
    // Clean the SSO url
    [[ExSystem sharedInstance] clearCompanySSOLoginPageUrl];
    //Login process expectes an (Msg *) object Identify a better way to send the login result to the the
    // For now mock up a msg object.
    ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    appDelegate.topView = HOME_PAGE;
    [[ApplicationLock sharedInstance] onLoginSucceeded:msg];
    // Dismiss the story board
    [self dismissViewControllerAnimated:YES completion:nil];
}


/**
  wipe the concur data clean if remote wipe option is turned on
*/
-(BOOL)handleRemoteWipe:(Msg *)msg
{
	Authenticate *auth = (Authenticate *)msg.responder;
	if (auth != nil && [auth.remoteWipe isEqualToString:@"Y"])
	{
		[[ApplicationLock sharedInstance] wipeApplication];
		
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:nil
                              message:[Localizer getLocalizedText:@"Unable to sign in"]
                              delegate:self
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                              otherButtonTitles:nil];
        
        [alert show];
        
		return YES;
	}
	return NO;
}

/**
 Handle alert message buttons
*/
- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == kInvalidPassword)
    {
        if (buttonIndex != alertView.cancelButtonIndex)
        {
            [self btnForgotPasswordTapped:self];
        }
        else    // try again button clicked
        {
            self.tryAgainCount++;
        }
        
        [self resetInputFields];
    }

}

/**
 Makes library call to request mobile password or password reset email
 */
-(void) handleRequestPasswordResetResponse:(NSString *)verificationKey passwordHelpText:(NSString *)passwordHelpText
{

    NSString *errorTitle = nil;
    NSString *errorMessage = nil;


    if (self.signInUserType == kPasswordUser) {
        
        errorTitle = [@"Password Reset Link Sent" localize];
        errorMessage = [@"Check your email to reset your password and sign in." localize];
    }
    else
    {
        errorTitle = [@"Mobile Password (PIN) Reset Link Sent" localize];
        errorMessage = [@"Open the email from this device to reset your mobile password (PIN) and sign in." localize];
        
    }
    
    // Save the user defaults
    [UserDefaultsManager setSignInPasswordResetDeviceVerificationKey:verificationKey];
    [UserDefaultsManager setSignInUserEmail:self.userEmail];
    [UserDefaultsManager setSignInUserType:self.signInUserType];
    [UserDefaultsManager setSignPasswordHelpText:passwordHelpText];
    [UserDefaultsManager setSignInUserId:self.userId];

    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:errorTitle
                          message:errorMessage
                          delegate:self
                          cancelButtonTitle:[@"Close" localize] otherButtonTitles:nil];
    
    [WaitViewController hideAnimated:YES withCompletionBlock:nil];
    [alert show];
    
}


/**
 Resets the input fields and sets the cursor on specific field. Always clears password for security.
 */
-(void)resetInputFields
{
    [self.txtPasswordField setText:@""];
    if([self.txtPasswordField respondsToSelector:@selector(hideFloatingLabel)])
        [self.txtPasswordField performSelector:(@selector(hideFloatingLabel)) withObject:nil afterDelay:0.0];

    [self.txtPasswordField becomeFirstResponder];
}

@end
