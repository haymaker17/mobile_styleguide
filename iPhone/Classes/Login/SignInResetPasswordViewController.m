//
//  SignInResetPasswordViewController.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/1/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SignInResetPasswordViewController.h"
#import "UserDefaultsManager.h"
#import "CTELogin.h"
#import "ApplicationLock.h"
#import "WaitViewController.h"
#import "CTEErrorMessage.h"
#import "AnalyticsTracker.h"

#import "KeychainManager.h"

@interface SignInResetPasswordViewController ()
@property (weak, nonatomic) IBOutlet UITextField *txtNewPassword;
@property (weak, nonatomic) IBOutlet UITextField *txtConfirmNewPassword;
@property (weak, nonatomic) IBOutlet UIButton *btnResetPassword;
@property (weak, nonatomic) IBOutlet UILabel *lblHelpText;
- (IBAction)btnResetPasswordTapped:(id)sender;

@property (nonatomic, strong) NSString *userId;
@property (nonatomic, strong) NSString *helpText;
@property SignInUserType signInUserType;
@property (nonatomic, strong) NSString *deviceVerificationKey;
@property (nonatomic, strong) NSString *userEmail;
@property (nonatomic, strong) NSString *secret;

@end

@implementation SignInResetPasswordViewController

int const constContinueTag = 222;
int const constEmailLinkExpiredTag = 223;
NSString * const constInvalidDeviceServerError = @"error.mismatched_keys";
NSString * const constEmailExpiredServerError = @"error.request_expired";
NSString * const constMobileDisabledServerError = @"error.mobile_disabled";


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

#pragma mark - viewcontroller methods

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    // Get the usertype and verification key from the userdefaults
    self.helpText = [UserDefaultsManager getSignPasswordHelpText];
    self.userEmail = [UserDefaultsManager getSignInUserEmail];
    self.signInUserType = [UserDefaultsManager getSignInUserType];
    self.userId = [UserDefaultsManager getSignInUserId];
    self.txtNewPassword.delegate = self;
    self.txtConfirmNewPassword.delegate = self;
    
    self.deviceVerificationKey = [UserDefaultsManager getSignInPasswordResetDeviceVerificationKey];
    
    // Localize the UI elements
    if (self.signInUserType == kPasswordUser)
    {
        self.txtNewPassword.placeholder = [@"New Password" localize];
        self.txtConfirmNewPassword.placeholder = [@"Confirm New Password" localize];
        [self.btnResetPassword setTitle:[@"Reset Password" localize] forState:UIControlStateNormal];
        self.title = [@"Reset Concur Password" localize];
    }
    else
    {
        self.txtNewPassword.placeholder = [@"New Mobile Password (PIN)" localize];
        self.txtConfirmNewPassword.placeholder = [@"Confirm New Mobile Password (PIN)" localize];
        [self.btnResetPassword setTitle:[@"Reset Mobile (PIN) Password" localize] forState:UIControlStateNormal];
        self.title = [@"Reset Mobile (PIN) Password" localize];
    }
    
    // set help text if present
    self.lblHelpText.text = self.helpText;
    
}

- (void)viewDidLayoutSubviews
{
    [self.lblHelpText sizeToFit];
}
- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/
#pragma mark - Handle Reset button

- (IBAction)btnResetPasswordTapped:(id)sender {
    // Check offline
    
    if (![ExSystem connectedToNetwork])
    {
        UIAlertView *alertView = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Offline"]
                              message:[Localizer getLocalizedText:@"Please wait until you are online to login"]
                              delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
        [alertView show];
        alertView.tag = kInvalidPassword;
        NSString *eventLabel = [NSString stringWithFormat:@"Type: %@", @"Offline"];
        [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Reset Attempt Failure" eventLabel:eventLabel eventValue:nil];
        return;
    }
    // Check if either of them are not nil
    if (![self.txtNewPassword.text lengthIgnoreWhitespace] || ![self.txtConfirmNewPassword.text lengthIgnoreWhitespace] || ![self.txtNewPassword.text isEqualToString:self.txtConfirmNewPassword.text]) {
        // Show message
        NSString *alertTitle = [@"Passwords Don't Match" localize];
        NSString *alertMessage = [@"There must have been a typo. Please re-enter the password so it matches exactly in both fields" localize];

        MobileAlertView *alertView = [[MobileAlertView alloc]
                                      initWithTitle:alertTitle
                                      message:alertMessage
                                      delegate:self
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                      otherButtonTitles:nil];
        [alertView show];
        alertView.tag = kInvalidPassword;
        return;
    }
    
    // if everything is good then make the library call
    self.secret = self.txtConfirmNewPassword.text;
    NSString *keyFromEmail = [[ApplicationLock sharedInstance] getResetPinKeypart];
    
    if (self.deviceVerificationKey == nil) {
        // if key part is missing then show invalid device message
        // Show message
        NSString *alertTitle = [@"Invalid Device" localize];
        NSString *alertMessage = [@"The password reset was requested from another device. Please use the same device you requested the password reset." localize];
        
        MobileAlertView *alertView = [[MobileAlertView alloc]
                                      initWithTitle:alertTitle
                                      message:alertMessage
                                      delegate:self
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                      otherButtonTitles:nil];
        [alertView show];
        alertView.tag = kInvalidPassword;
        // Log Google Analytics.
        NSString *eventLabel = [NSString stringWithFormat:@"Type: %@", @"Invalid Device"];
        [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Reset Pin Attempt Failure" eventLabel:eventLabel eventValue:nil];
         
        return;
    }
    
    [WaitViewController showWithText:@"" animated:YES];
    
    // if we reached here then we are good to reset password. Make library call to reset password/ Pin
    if (self.signInUserType == kPasswordUser) {
        
        [CTELogin resetPasswordWithEmail:self.userEmail newPassword:self.secret keyFromSDK:self.deviceVerificationKey keyFromEmail:keyFromEmail success:^{
            
            MobileAlertView *alertView = [[MobileAlertView alloc]
                                          initWithTitle:[@"Password Successfully Reset" localize]
                                          message:nil
                                          delegate:self
                                          cancelButtonTitle:[@"Continue" localize]
                                          otherButtonTitles:nil];
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            [alertView show];
            alertView.tag = constContinueTag;
            [self clearUserDefaults];
            NSString *eventLabel = [NSString stringWithFormat:@"Type: %@", @"Mobile Password"];
            [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Reset Attempt Success" eventLabel:eventLabel eventValue:nil];
            
        } failure:^(CTEError *error) {
            
            // Donot clear defaults if there is error, user might request email again.
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            [self handlePasswordResetError:error];
            
        }];
        
    }
    else        // This should be PasswordUser
    {
        
        [CTELogin resetMobilePasswordWithEmail:self.userEmail newMobilePassword:self.secret keyFromSDK:self.deviceVerificationKey keyFromEmail:keyFromEmail success:^{
            
            MobileAlertView *alertView = [[MobileAlertView alloc]
                                          initWithTitle:[@"Mobile Password Successfully Reset" localize]
                                          message:nil
                                          delegate:self
                                          cancelButtonTitle:[@"Continue" localize]
                                          otherButtonTitles:nil];
            
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            [alertView show];
            alertView.tag = constContinueTag;
            [self clearUserDefaults];
            NSString *eventLabel = [NSString stringWithFormat:@"Type: %@", @"Password"];
            [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Reset Attempt Success" eventLabel:eventLabel eventValue:nil];
            
        } failure:^(CTEError *error) {
           
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            [self handlePasswordResetError:error];
            
        }];

    }

}

/**
 * Clears the userdefaults set for password reset request.
 */
-(void) clearUserDefaults
{
    
    [UserDefaultsManager setSignPasswordHelpText:nil];
    [UserDefaultsManager setSignInUserEmail:nil];
    [UserDefaultsManager setSignInUserId:nil];
    [UserDefaultsManager setSignInPasswordResetDeviceVerificationKey:nil];

}

/**
 * Handles error from the passoword/mobile password reset library call
 */
-(void) handlePasswordResetError:(CTEError *)cteError
{
    // Get the error message from the CteError and show the error message in alert box
    // For mobile password reset there are specific message codes
    CTEErrorMessage *cteErrorMessage =  nil;
    // get a specific title for each message, start with default message
    NSString *errorTitle = @"Unable To Reset Password";
    NSString *errorMessage = nil;
    // <Bad Pins, Invalid Device, Request Expired, Invalid Length, Mobile Disabled, Offline, Other Error> , start with default type
    // Other Error/invalid length messages are not detectable because of generic server message
    NSString *eventLabel =  [NSString stringWithFormat:@"Type: %@", @"Bad Pins"];
    
    if([cteError.concurErrorMessages count] > 0 )
    {
        // Check if there are any Servererror's in the CTEError
        cteErrorMessage = (CTEErrorMessage *)cteError.concurErrorMessages[0];
        
        if ([cteErrorMessage.systemMessage isEqualToString:constInvalidDeviceServerError]) {
            
            errorTitle = [@"Invalid Device" localize];
            errorMessage = [@"The password reset was requested from another device. Please use the same device you requested the password reset." localize];
            eventLabel  = [NSString stringWithFormat:@"Type: %@", @"Invalid Device"];
         }
        else if ([cteErrorMessage.systemMessage isEqualToString:constEmailExpiredServerError]) {
            
            // FOR Email give user an options
            errorTitle = [@"Email Link Expired" localize];
            errorMessage = [@"Please try again and use the link within 24 hours of receipt." localize];
            // Login should not fail here, in case it failed just show a generic message to the user.
            MobileAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:errorTitle
                                      message:errorMessage
                                      delegate:self
                                      cancelButtonTitle:[@"Cancel" localize]
                                      otherButtonTitles:[@"Send Again" localize] ,nil];
            
            alert.Tag = constEmailLinkExpiredTag;
            [alert show];
            
            eventLabel  = [NSString stringWithFormat:@"Type: %@", @"Request Expired"];
            [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Reset Pin Attempt Failure" eventLabel:eventLabel eventValue:nil];
            return;

        }
        else if ([cteErrorMessage.systemMessage isEqualToString:constMobileDisabledServerError]) {
            
            errorTitle = [@"Mobile Disabled" localize];
            errorMessage = [@"Your company has disabled Concur for mobile. Please contact your administrator." localize];
            eventLabel  = [NSString stringWithFormat:@"Type: %@", @"Mobile Disabled"];
        }
        else
            errorMessage = cteErrorMessage.userMessage ;

    }
    // Login should not fail here, in case it failed just show a generic message to the user.
    MobileAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:errorTitle
                              message:errorMessage
                              delegate:self
                              cancelButtonTitle:[@"OK" localize]
                              otherButtonTitles:nil];
    
    alert.Tag = kInvalidPassword;
    [alert show];
    
    // Google Analytics -
    [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Reset Attempt Failure" eventLabel:eventLabel eventValue:nil];
    
}

#pragma mark - Do login process

-(void)makeLoginRequestandLogin
{
    
    [ExSystem sharedInstance].userName = self.userId;
    [[ExSystem sharedInstance] clearUserCredentialsAndSession]; // PIN will be saved later if and when authentication succeeds
    [[ExSystem sharedInstance] saveSettings];
    [WaitViewController showWithText:@"" animated:YES];
    [CTELogin loginConcurMobileWithUsername:self.userId Password:self.secret success:^(NSString *loginXML) {
        
        ALog(@"::User %@ Login successful::" , self.userId);
        [self loginAndShowHome:loginXML];
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        
    } failure:^(CTEError *error) {
        
        // Ideally this should not happen unless there is a nework error or a service failure.
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        [self handleSignInError:error];
        
    }];

}

-(void) handleSignInError:(CTEError *)error
{
    
    CTEErrorMessage *cteErrorMessage = nil;
    
    // Check if there are any message
    if ([error.concurErrorMessages count]> 0 ) {
        cteErrorMessage = error.concurErrorMessages[0];
    }
    
    // Login should not fail here, in case it failed just show a generic message to the user.
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:nil
                          message:[Localizer getLocalizedText:@"Unable to sign in"]
                          delegate:self
                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                          otherButtonTitles:nil];
    
    [alert show];
    
    // log info
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"::Login failed for user:%@:: with error ::%@::" , self.userId, cteErrorMessage.systemMessage == nil ? cteErrorMessage.systemMessage : error.concurErrorResponse] Level:MC_LOG_INFO];
    alert.Tag = kInvalidPassword;
    
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
    // Lot of this method code and other methods called in this are duplicates in password sign in. Identify a proper way to group them into a logical utility Class later
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
        // clean touchID credentials.
        KeychainManager *keychainManager = [[KeychainManager alloc] init];
        [keychainManager clearACLuserID];
        [keychainManager clearACLpassword];
        NSString *eventLabel = [NSString stringWithFormat:@"Type: %@", @"Remote Wipe"];
        [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Failed Attempt" eventLabel:eventLabel eventValue:nil];
        [self resetInputFields];
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
	if (msg.responseCode == 200 && auth != nil && [auth.remoteWipe isEqualToString:@"Y"])
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

#pragma mark - UI Handlers
//  Method to clear the passwords after error
-(void)resetInputFields
{
    [self.txtNewPassword setText:@""];
    [self.txtConfirmNewPassword setText:@""];
    
    if([self.txtNewPassword respondsToSelector:@selector(hideFloatingLabel)])
        [self.txtNewPassword performSelector:(@selector(hideFloatingLabel)) withObject:nil afterDelay:0.0];
    
    if([self.txtConfirmNewPassword respondsToSelector:@selector(hideFloatingLabel)])
        [self.txtConfirmNewPassword performSelector:(@selector(hideFloatingLabel)) withObject:nil afterDelay:0.0];

    [self.txtNewPassword becomeFirstResponder];
}

/**
 Handle alert message buttons
 */
- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    
    if (alertView.tag == constEmailLinkExpiredTag)
    {
        [self resetInputFields];
        // Send the email link again
        if (buttonIndex != alertView.cancelButtonIndex)
        {
            [self sendResetEmailAgain];
        }
    }
    else if (alertView.tag == kInvalidPassword)
    {
        [self resetInputFields];
    }
    else if (alertView.tag ==  constContinueTag)
    {
        if (buttonIndex == alertView.cancelButtonIndex)
        {
            [self makeLoginRequestandLogin];
        }
        
    }
    
}

#pragma mark - resend reset request

// This is a duplication of code from the SignInPasswordVC. identify a better way of handling the common code.
-(void)sendResetEmailAgain
{
    [UserDefaultsManager setSignInPasswordResetDeviceVerificationKey:nil];
    
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
    [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Request Pin Reset" eventLabel:eventLabel eventValue:nil];

    // If there is no email configured then show an alert message that password cannot be reset
    if(![self.userEmail lengthIgnoreWhitespace])
    {
        // send the user back to sign in screen
        [self.navigationController popToRootViewControllerAnimated:YES];
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


@end
