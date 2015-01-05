//
//  LoginViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 10/27/09.
//  Updated to 7.4 on 4/20/2011 by Lord PJK
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "LoginViewController.h"
#import "ExSystem.h"

#import "Weather.h"
#import "Location.h"
#import "WOEID.h"
#import "DataConstants.h"
#import "Authenticate.h"
#import "SettingsViewController.h"
#import "FileManager.h"
#import "MCLogging.h"
#import "MobileAlertView.h"
#import "ExSystem.h"
#import "ApplicationLock.h"
#import "EditField.h"
#import "AppsUtil.h"
#import "LoginOptionsViewController.h"
#import "LoginWebViewController.h"
#import "ExSystem.h"
#import "LoginHelpTopicVC.h"
#import "LoginHelpContentsVC.h"
#import "Config.h"
#import "SettingsButton.h"
#import "CustomBackButton.h"
#import "DataConstants.h"
#import "WaitViewController.h"

#define kAlertTagVerifyEmail 106
#define kAlertTagRedirectToCorpApp 201
#define kAlertTagRedirectToSUApp 202
#define kAlertInvalidConcurCredentials  700101
#define kAlertTagCorpSSORequired 998

typedef enum authType
{
    AuthenticationTypePinOrPassword,
    AuthenticationTypePassword,
    AuthenticationTypePin
} AuthenticationType;

@interface LoginViewController ()
-(void) resignFirstResponder:(int) row;
-(void) handleSignInButtonPressed;
-(void) dismissKeyboard;
@property (strong,nonatomic) ConcurTestDrive *concurTestDrive;
@property AlertTag alertTag;

@end

@implementation LoginViewController
@synthesize btnSignIn, btnSignUp, btnResetPassword, lblHaveNoAccount, viewSignUp,viewSignUpBtn;
@synthesize registerButton;
@synthesize isAuthenticate;
@synthesize viewCorpSignUp, btnForgotPassword;
@synthesize btnCorpSignUp, viewKeyboard, btnSSOSignIn;
@synthesize viewGovNotice;
@synthesize padHomeVC, tableList, userId, secret, authenticationType, settingsButton, ivBackground, btnJoinGreen, ivLogo;
@synthesize btnCorpSignUpAccessory, loginDelegate;
@synthesize allMessages;

-(void) markFirstResponder:(int) row
{
    NSUInteger path[2] = {0, row};
	NSIndexPath *ip = [[NSIndexPath alloc] initWithIndexes:path length:2];
    EditInlineCell *cell = (EditInlineCell*) [tableList cellForRowAtIndexPath:ip];
    [cell.txt becomeFirstResponder];
}

-(void) resignFirstResponder:(int) row
{
    NSUInteger path[2] = {0, row};
	NSIndexPath *ip = [[NSIndexPath alloc] initWithIndexes:path length:2];
    EditInlineCell *cell = (EditInlineCell*) [tableList cellForRowAtIndexPath:ip];
    [cell.txt resignFirstResponder];
}

/**
 Resets the input fields and sets the cursor on specific field. Always clears password for security.
 */
-(void)resetInputFields
{
    [self.txtPasswordField setText:@""];
    if([self.txtPasswordField respondsToSelector:@selector(hideFloatingLabel)])
        [self.txtPasswordField performSelector:(@selector(hideFloatingLabel)) withObject:nil afterDelay:0.0];
    
    switch (self.alertTag) {
        case kInvalidEmail:
            [self.txtUsernameField becomeFirstResponder];
            break;
        case kInvalidPassword:
            [self.txtPasswordField becomeFirstResponder];
            break;
        default:
            self.txtUsernameField.text = @"";
            if([self.txtUsernameField respondsToSelector:@selector(hideFloatingLabel)])
                [self.txtUsernameField performSelector:(@selector(hideFloatingLabel)) withObject:nil afterDelay:0.0];
            [self.txtUsernameField becomeFirstResponder];
            break;
    }
}


- (IBAction)backgroundTap:(id)sender
{
    // This is called when the little keyboard image is pressed on the iPhone indicating that the keyboard should be dismissed
    [self dismissKeyboard];
}

- (IBAction)backgroundSSOTap:(id)sender
{
    // This is called when a button hovering over the "Company Code Sign In" link is pressed.  This button is how we know that the link was pressed while the keyboard was up.  Without this button, the link would not respond when pressed while the keyboard is up.
    [self dismissKeyboard];
    [self buttonSSOPressed:self];
}

- (void) dismissKeyboard
{//clears the keyboard from the view
    [self resignFirstResponder:0];
    [self resignFirstResponder:1];
    // MOB-16029 - Not sure why this was done previously. But this causes the settings icon to disappear so commenting this line
    // self.navigationItem.rightBarButtonItem = nil;
    
    // MOB-16230 - hook up gesture recognizer to background tap. any touch should dismiss keyboard
    [self.view endEditing:YES];
}

#pragma mark - View Methods
- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
	if([UIDevice isPad])
		[self layoutPad:NO];
	else
	{
		if([ExSystem isLandscape])
			[self resetForLandscape];
	}
    /// TODO: refactor ApplicationLock out of here
    [[ApplicationLock sharedInstance] onLoginViewAppeared];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    if([UIDevice isPad])
    {
		[self layoutPad:NO];
        [self willRotateToInterfaceOrientation:[UIApplication sharedApplication].statusBarOrientation duration:0];
    }
    
    // MOB-16251
    // if the hack is off, proceed as normal
    // if the hack is on, avoid becoming first responder because this will pop the keyboard over the initial landing page
    if(false == self.skipKeyboardDisplayHack)
    {
        if(userId != nil && [userId length] > 0)
        {
            [self markFirstResponder:1];
            [self presetUserName];
        }
    }

    [self.txtUsernameField setDelegate:(id)self];
    [self.txtPasswordField setDelegate:(id)self];
}

/**
 Description : This method prepoluates the user name in  new Login UI -> Login.storyboard
 @param none 
 @return none

 */
-(void)presetUserName
{
    self.txtUsernameField.text = ([ExSystem sharedInstance].isCorpSSOUser)? nil : [ExSystem sharedInstance].userName;
    // MOB-16321 --> Do not autoshow keyboard for login screen.
    //[self.txtPasswordField becomeFirstResponder];
}

-(void) viewWillDisappear:(BOOL)animated
{
    //    if(loginDelegate != nil)
    //    {
    //        [self.loginDelegate dismissYourself];
    ////        [padIntroVC dismissModalViewControllerAnimated:NO];
    //    }
    [super viewWillDisappear:animated];
    [self.view endEditing:YES];
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    // MOB-16230 - hook up gesture recognizer to background tap. any touch should dismiss keyboard
    UITapGestureRecognizer* gestureRecognizer = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(backgroundTap:)];
    [self.view addGestureRecognizer:gestureRecognizer];
    
    self.authenticationType = AuthenticationTypePinOrPassword;
    
    self.tryAgainCount = 0;
    
	if([UIDevice isPad])
    {
        [self.viewKeyboard setHidden:YES];
        [self layoutPad:NO];
    }
    else
        ivLogo.hidden = YES;
    
    [super viewDidLoad];
    
    self.viewSignUp.hidden = YES;
    [self.btnSSOSignIn setTitle:[@"Company Code Sign In" localize] forState:UIControlStateNormal];
    
    UIButton* btnSettings = [[SettingsButton alloc]init];
    [btnSettings addTarget:self action:@selector(buttonSettingsPressed:) forControlEvents:UIControlEventTouchUpInside];

    if ([Config isGov])
    {
        self.viewGovNotice.hidden = NO;
        self.viewCorpSignUp.hidden = YES;
        self.viewSignUp.hidden = YES;
        self.viewSignUpBtn.hidden = YES;
        self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:btnSettings];

        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        if (![BaseManager hasEntriesForEntityName:@"EntityWarningMessages" withContext:[ad managedObjectContext]])
            [[ExSystem sharedInstance].msgControl createMsg:GOV_WARNING_MSG CacheOnly:@"NO" ParameterBag:nil SkipCache:NO RespondTo:self];
    }
    else
    {
        self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:btnSettings];
        
        if ([ConcurTestDrive isAvailable])
        {
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
    }
    
    if ([ExSystem is7Plus])
        [self makeTitle];
    else
        self.title = [@"Sign In" localize];
    
    self.userId = @"";
    self.secret = @"";
    
    if ([ExSystem sharedInstance].entitySettings.saveUserName != nil &&
        [[ExSystem sharedInstance].entitySettings.saveUserName isEqualToString:@"YES"] &&
        [ExSystem sharedInstance].userName != nil &&
        [ExSystem sharedInstance].userName.length > 0)
    {
        self.userId = [ExSystem sharedInstance].userName;
    }

    settingsButton.hidden = NO;
    
    NSString *ssoUrl = [[ExSystem sharedInstance] loadCompanySSOLoginPageUrl];
    if (ssoUrl != nil)
    {
        LoginWebViewController *lWVC = [[LoginWebViewController alloc] init];
        [lWVC setLoginUrl:ssoUrl];
        lWVC.loginDelegate = loginDelegate;
        [self.navigationController pushViewController:lWVC animated:YES];
    }

    if([self isLoadedFromStoryboard])
    {
        NSLog(@"Loaded from StoryBoard: %@", [self isLoadedFromStoryboard] ? @"YES" : @"NO");
        [self makeStoryBoardLabels];
    }
}

/**
 Shows the Testdrive Storyboard over the login storyboard.
 */
-(void)showTestDrive
{
    [self.view endEditing:YES];
    
    self.concurTestDrive = [[ConcurTestDrive alloc] init];
    [self.concurTestDrive popTestDriveAnimated:YES];

    // Add flurry
    [Flurry logEvent:[NSString stringWithFormat:@"%@,%@", fCatSignIn,fNameBackButtonClick] ];
}

-(void) makeTitle
{
// Comment the color for now as per UI team feedback. Left the code incase they change their mind
//    NSDictionary *textAttributes = [NSDictionary dictionaryWithObjectsAndKeys:
//                                    [UIColor colorWithRed:5/255.0 green:100/255.0 blue:175/255.0 alpha:1.0],NSForegroundColorAttributeName,
//                                    [UIColor clearColor],NSBackgroundColorAttributeName,nil];
//    
//    self.navigationController.navigationBar.titleTextAttributes = textAttributes;
    // Do no show any color
    self.title = [@"Sign In" localize];
}

/**
 if the view is loaded from a storyboard then this will setup the label title and button titles accordingly.
 */
-(void)makeStoryBoardLabels
{
    NSString *userPlaceholder = [Localizer getLocalizedText:@"LABEL_LOGIN_USER_NAME"];
	NSString *pwdPlaceholder = [Localizer getLocalizedText:@"LABEL_LOGIN_PASSWORD"];

    [self.btnSignInWithStoryBoard setTitle:[Localizer getLocalizedText:@"Sign in to Concur"] forState:UIControlStateNormal];
    [self.btnSSOSignInWithStoryBoard setTitle:[Localizer getLocalizedText:@"Company Code Sign In"] forState:UIControlStateNormal];
    [self.btnSiginInHelpStoryBoard setTitle:[Localizer getLocalizedText:@"Forgot Username or Password?"] forState:UIControlStateNormal];
    self.txtUsernameField.placeholder = userPlaceholder;
    self.txtPasswordField.placeholder = pwdPlaceholder;

}

- (void)btnSSOSignInSetUp
{
    NSString *titleText = [Localizer getLocalizedText:@"Company Code Sign In"];

    UIColor *textColor = [UIColor colorWithRed:2.0/255.0 green:64.0/255.0 blue:116.0/255.0 alpha:1.0];
    self.btnSSOSignIn.layer.borderWidth = .5f;
    self.btnSSOSignIn.layer.cornerRadius = 3.0f;
    self.btnSSOSignIn.layer.borderColor =[[UIColor colorWithRed:(58.0/255) green:(68.0/255) blue:(89.0/255) alpha:1.0] CGColor];
    

    [self.btnSSOSignIn setTitle:titleText forState:UIControlStateNormal];
    self.btnSSOSignIn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    self.btnSSOSignIn.titleLabel.font = [UIFont systemFontOfSize:14.0];
    self.btnSSOSignIn.titleLabel.textColor = textColor;
    [self.btnSSOSignIn setTitleColor:textColor forState:UIControlStateNormal];
}


- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
    self.ivBackground = nil;
    self.btnJoinGreen = nil;
    self.ivLogo = nil;
    self.viewKeyboard = nil;
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return LOGIN;
}

-(NSString *)getViewDisplayType
{
	return @"VIEW_DISPLAY_TYPE_MODAL_NAVI";
}


//for the ipad, do all of it's layout here...
-(void)layoutPad:(BOOL)forceLandscape
{
    ivBackground.image = nil;
    [tableList setBackgroundView:nil];
    [tableList setBackgroundView:[[UIView alloc] init]];
    [tableList setBackgroundColor:UIColor.clearColor];
}


#pragma mark -
#pragma mark Orientation Methods



- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
        self.coLogoAreaTop.constant = 80;
        self.coLogoAreaLea.constant = 30;
        self.coFormAreaTop.constant = 90;
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

	if ([ExSystem isLandscape])
	{
		[self resetForLandscape];
	}
	else
	{
		[self resetForPortrait];
	}
    
    [tableList reloadData];
	
}

-(void)resetForLandscape
{
	if([UIDevice isPad])
		[self layoutPad:YES];
	else
	{
		[UIView beginAnimations:@"Fade" context:nil];
		[UIView setAnimationDelegate:self];
		[UIView setAnimationDidStopSelector:@selector(logoAnimationStart:)];
		[UIView setAnimationDuration:.33];
		[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
		
		[UIView commitAnimations];
	}
}


-(void) logoAnimationStart:(id)sender
{
    //	[UIView beginAnimations:@"Fade" context:nil];
    //	[UIView setAnimationDelegate:self];
    //	[UIView setAnimationDuration:.5];
    //	[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    //	imgSmallLogo.frame = CGRectMake(0, 5, 224, 93);
    //	imgLogo.frame = CGRectMake(-400, 0, 320, 117);
    //	userField.frame = CGRectMake(220, 20, 240, 31);
    //	pwdField.frame = CGRectMake(220, 60, 240, 31);
    //	ivGrayLine.frame = CGRectMake(10, 105, 460, 1);
    //	lblDontHavePin.frame = CGRectMake(220, 110, 254, 20);
    //	[self adjustLabel:lblDontHavePin LabelValue:registerButton HeadingText:lblDontHavePin.text ValueText:@"" ValueColor:nil];
    //	//registerButton.frame = CGRectMake(345, 111, 57, 20);
    //	btnSettings.frame = CGRectMake(440, 111, 18, 18);
    //	[UIView commitAnimations];
}

-(void) logoAnimationPortrait:(id)sender
{
    //	[UIView beginAnimations:@"Fade" context:nil];
    //	[UIView setAnimationDelegate:self];
    //	[UIView setAnimationDuration:.5];
    //	[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    //	imgLogo.frame = CGRectMake(0, 0, 320, 117);
    //	imgSmallLogo.frame = CGRectMake(500, 5, 224, 93);
    //	userField.frame = CGRectMake(20, 118, 280, 31);
    //	pwdField.frame = CGRectMake(20, 157, 280, 31);
    //	ivGrayLine.frame = CGRectMake(22, 105, 276, 1);
    //	lblDontHavePin.frame = CGRectMake(20, 192, 254, 20);
    //	[self adjustLabel:lblDontHavePin LabelValue:registerButton HeadingText:lblDontHavePin.text ValueText:@"" ValueColor:nil];
    //	//registerButton.frame = CGRectMake(141, 192, 57, 20);
    //	btnSettings.frame = CGRectMake(282, 193, 18, 18);
    //	[UIView commitAnimations];
}

-(void)resetForPortrait
{
	if([UIDevice isPad])
		[self layoutPad:NO];
	else
	{
		
		[UIView beginAnimations:@"Fade" context:nil];
		[UIView setAnimationDelegate:self];
		[UIView setAnimationDidStopSelector:@selector(logoAnimationPortrait:)];
		[UIView setAnimationDuration:.33];
		[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
		[UIView commitAnimations];
        
	}
}

-(BOOL) needToSwitchAppForUser:(Msg*)msg
{
	Authenticate *auth = (Authenticate *)msg.responder;
	if (msg.responseCode == 200 && auth != nil)
    {

        if ([[ExSystem sharedInstance] isBronxUserProductLine:auth.entityType productOffering:auth.productOffering])
        {
            UIAlertView *alert = [[MobileAlertView alloc]
                                    initWithTitle:[Localizer getLocalizedText:@"BRONX_USER_CORP_APP_TITLE"]
                                    message:[Localizer getLocalizedText:@"BRONX_USER_CORP_APP_MSG"]
                                    delegate:self
                                    cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
                                    otherButtonTitles:[Localizer getLocalizedText:@"LABEL_OK_BTN"], nil];
                
            alert.tag = kAlertTagRedirectToSUApp;
            [alert show];
                
            [FileManager cleanCache];
            [[ApplicationLock sharedInstance] logout];
            return YES;
        }
    }
    return NO;
}

-(void)handleLoginError:(Msg *)msg
{
    NSString* errBody = msg.errBody;

    // Obtain the authentication data class
    Authenticate *auth = (Authenticate *)msg.responder;
    
    // Check if we caught any errors reported back in an MWSResponse wrapper
    if ([auth.commonResponseCode length] > 0)
    {
        // MWSResponse reported an error
        MobileAlertView *alert = nil;
        
        if ([auth.commonResponseCode isEqualToString:@"RATE_LIMIT_1"])
        {
            // If Akamai is rate-limiting, display the message they have provided us with
            alert = [[MobileAlertView alloc]
                  initWithTitle:nil
                  message:auth.commonResponseUserMessage
                  delegate:self
                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                  otherButtonTitles:nil];
        }
        else
        {
            // Akamai should be the only ones sending MWSResponse messages to the iOS app, so treat anything
            // else as a connection error for the time being.
            if ([auth.commonResponseUserMessage length] > 0)
            {
                errBody = auth.commonResponseUserMessage;
            }
            else
            {
                errBody = auth.commonResponseSystemMessage;
            }
            alert = [[MobileAlertView alloc]
                     initWithTitle:[Localizer getLocalizedText:@"Connection Error"]
                     message:errBody
                     delegate:self
                     cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                     otherButtonTitles:nil];
        }
        
        [alert show];
    }
    else if ([@"sso login required" isEqualToString:[errBody lowercaseString]])
	{
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:nil
                              message:[Localizer getLocalizedText:@"SSO_LOGIN_REQUIRED_MSG"]
                              delegate:self
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                              otherButtonTitles:nil];
        alert.tag = kAlertTagCorpSSORequired;
        [alert show];
	}
    else
    {
        MobileAlertView *alert = nil;
        
        if ([@"passwordexpired" isEqualToString:[errBody lowercaseString]])
        {
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Password Expired"]
                                  message:[Localizer getLocalizedText:@"SU Password Expired Instruction"]
                                  delegate:nil
                                  cancelButtonTitle:nil
                                  otherButtonTitles:[Localizer getLocalizedText:@"LABEL_OK_BTN"], nil];
            [alert show];
            self.alertTag = kInvalidPassword;
        }
        else
        {
            alert = [[MobileAlertView alloc]
                     initWithTitle:[Localizer getLocalizedText:@"INVALID_CONCUR_CREDENTIALS"]
                     message:[Localizer getLocalizedText:@"INVALID_LOGIN_MSG"]
                     delegate:self
                     cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"]
                     otherButtonTitles:[Localizer getLocalizedText:@"I Forgot"], nil];
            alert.tag = kAlertInvalidConcurCredentials;
            self.alertTag = kInvalidPassword;
        }
        
        [alert show];
    }
    
}

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

#pragma mark -
#pragma mark MVC Methods
-(void)respondToFoundData:(Msg *)msg
{//respond to data that might be coming from the cache
    //#warning test code to see the autologin views
    //    return;
	
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"LoginViewController::respondToFoundData msg.idKey: %@", msg.idKey] Level:MC_LOG_INFO];
    
	[self hideWaitView];
    [self hideLoadingView];
    
    // Msgs independent of iPad/iPhone
    if ([msg.idKey isEqualToString:RESET_PASSWORD_DATA])
    {
        [self hideWaitView];
        return;
    }
		
    if ([msg.idKey isEqualToString:AUTHENTICATION_DATA])
    {
    	// MOB-17666 Missing waiting view while signing in
        [self hideWaitView];
        BOOL fRemoteWipe = [self handleRemoteWipe:msg];
        if (!fRemoteWipe)
        {
            // Checking session in addition to response code to work around a bug where 200 was returned
            // even though a session was never created.
            Authenticate *auth = (Authenticate *)msg.responder;
            if (msg.responseCode == 200 && auth.sessionID != nil && [auth.sessionID length] > 0)
            {
                // MOB-7283 - lock single user out of CORP app and corp user out of SU app
                if ([self needToSwitchAppForUser:msg])
                    return;
                
                if ([UIDevice isPhone]){
                    self.modalTransitionStyle = UIModalTransitionStyleCrossDissolve;
                    [self dismissViewControllerAnimated:YES completion:nil];
                }
                
                [self recordPinOrPassword];
                [[ApplicationLock sharedInstance] onLoginSucceeded:msg];
                
                if(loginDelegate != nil && [UIDevice isPad])        // only for iPad
                {
                    [self.loginDelegate dismissYourself:self];
                }
                
            }
            else
            {
                [self handleLoginError:msg];
            }
        }
    }
    else if ([msg.idKey isEqualToString:GOV_WARNING_MSG])
    {
        GovWarningMessagesData *messages = (GovWarningMessagesData*) msg.responder;
        if (messages != nil)
        {
            NSManagedObjectContext *context = [ExSystem sharedInstance].context;
            NSArray *allMessage = [BaseManager fetchAll:@"EntityWarningMessages" withContext:context];
            if ([allMessage count] > 0)
            {
                self.allMessages = (EntityWarningMessages*) [allMessage objectAtIndex:0];
            }
        }
    }
}

- (void) configureTables
{
    if(![UIDevice isPad])
        [tableList setBackgroundView:[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"signin_bckgrd.png"]]];
}

#pragma mark -
#pragma mark Text Field Methods
- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    //
    // this method handles next / done button pressed on iphone and ipad for username and password fields
	//
    
    // next was pressed on username field, give focus to password field
    if(textField == self.txtUsernameField)
    {
        [self.txtPasswordField becomeFirstResponder];
    }
    else if(textField == self.txtUsernameField)
    {
        [self.txtPasswordField becomeFirstResponder];
    }

    // done was pressed on password field, attempt logging in
    if(textField == self.txtPasswordField)
    {
        [self handleSignInButtonPressed];
    }
    
    return YES;
}


- (IBAction)textFieldDoneEditing:(id)sender
{//clears the keyboard from the view
	[sender resignFirstResponder];
}


#pragma mark -
#pragma mark Utility Methods

-(void) recordPinOrPassword
{
	[ExSystem sharedInstance].pin = nil;
	[[ExSystem sharedInstance] saveSettings];
}

-(void)signInPad
{
	if(![ExSystem connectedToNetwork])
	{
	}
	else
	{
        // MOB-6268 Invoking the wait view using performSelector causes problem.
        [self showLoadingViewWithText:[Localizer getLocalizedText:@"Logging In"]];
		[ExSystem sharedInstance].userName = self.userId;
		[[ExSystem sharedInstance] clearUserCredentialsAndSession]; // PIN will be saved later if and when authentication succeeds.
		[[ExSystem sharedInstance] saveSettings];
		
        //		[[MCLogging getInstance] log:[NSString stringWithFormat:@"Login for %@", self.userId == nil? @"":self.userId] Level:MC_LOG_INFO];
        
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:LOGIN, @"TO_VIEW", self.userId, @"USER_ID", @"YES", @"SKIP_CACHE", nil];
        
        if (self.authenticationType == AuthenticationTypePinOrPassword)
            pBag[@"PIN_OR_PASSWORD"] = self.secret;
        else if (self.authenticationType == AuthenticationTypePassword)
            pBag[@"PASSWORD"] = self.secret;
        else
            pBag[@"PIN"] = self.secret;
        
 		[[ExSystem sharedInstance].msgControl createMsg:AUTHENTICATION_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        
	}
}

-(void) hideLoginFields:(BOOL)hide
{
	[registerButton setHidden:hide];
}


- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == kAlertInvalidConcurCredentials)
    {
        if (buttonIndex != alertView.cancelButtonIndex)
        {
            [self buttonCorpSignUpPressed:self];
        }
        else    // try again button clicked
        {
            self.tryAgainCount++;
        }
        [self resetInputFields];
    }
    else if(alertView.tag == kAlertTagVerifyEmail)
        [self showEmailWaitView];
    else if (alertView.tag == kAlertTagRedirectToCorpApp)
    {
        if (buttonIndex != alertView.cancelButtonIndex)
            [AppsUtil launchCorpApp];
    }
    else if (alertView.tag == kAlertTagRedirectToSUApp)
    {
        if (buttonIndex != alertView.cancelButtonIndex)
            [AppsUtil launchSUApp];
    }
    else if (alertView.tag == kAlertTagCorpSSORequired)
    {
        [self buttonSSOPressed:nil];
    }
}


#pragma mark - Corporate button handlers

- (IBAction)buttonCorpSignUpPressed:(id)sender
{
    LoginHelpContentsVC *oVC = [[LoginHelpContentsVC alloc] initWithNibName:@"LoginHelpContentsVC" bundle:nil];
    [self.navigationController pushViewController:oVC animated:YES];
}

-(IBAction) buttonSSOPressed:(id)sender
{
    LoginOptionsViewController *oVC = [[LoginOptionsViewController alloc] initWithNibName:@"LoginOptionsViewController" bundle:nil];
    oVC.loginDelegate = loginDelegate;
    [self.navigationController pushViewController:oVC animated:YES];
}

-(IBAction) buttonSettingsPressed:(id)sender
{
    SettingsViewController *vc = [[SettingsViewController alloc] initBeforeUserLogin];
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:vc];
    navi.modalPresentationStyle = UIModalPresentationFormSheet;
    
    [self.navigationController presentViewController:navi animated:YES completion:nil];
}

- (IBAction)buttonSignInPressed:(id)sender
{
    [self handleSignInButtonPressed];
}

-(void) handleSignInButtonPressed
{
    //MOB-11715 : Clear corpSSO flag and handle sign in
    [ExSystem sharedInstance].isCorpSSOUser = NO;
    // username/password will be set to nil if the login was not done using the new storyboard
    // below check prevents setting the values to nil by mistake
    if([self isLoadedFromStoryboard])
    {
        self.userId = self.txtUsernameField.text;
        self.secret = self.txtPasswordField.text;
        [self.txtPasswordField resignFirstResponder];
    }

    
    if (![ExSystem connectedToNetwork] && self.authenticationType == AuthenticationTypePinOrPassword)
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Offline"]
                              message:[Localizer getLocalizedText:@"Please wait until you are online to login"]
                              delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
        [alert show];
        [self markFirstResponder:1];
        return;
    }
    
    //MOB-5668
    //Fixed by checking for pin and user name existence.  Displaying a prompt that you are invalid.
    // MOB-11688 uTest 565624# Crash when username is null at the beginning and password filled
    if (!self.userId.length)
    {
        self.alertTag = kInvalidEmail;
        if (self.authenticationType == AuthenticationTypePinOrPassword || self.authenticationType == AuthenticationTypePin || self.authenticationType == AuthenticationTypePassword)
        {
            // If pass/pin authenticated, then inform the username of the correct username format
            UIAlertView *av = nil;
            if ([Config isGov])
            {
                av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"INVALID_CONCUR_CREDENTIALS"] message:[NSString stringWithFormat:[Localizer getLocalizedText:@"INVALID_USERID_MSG"], @"\n\n"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"] otherButtonTitles:nil];
            }
            else
            {
                av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"INVALID_CONCUR_CREDENTIALS"] message:[NSString stringWithFormat:[Localizer getLocalizedText:@"INVALID_USERID_MSG"], @"\n\n"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"] otherButtonTitles:[Localizer getLocalizedText:@"Get Help"], nil];
            }
            av.tag = kAlertInvalidConcurCredentials;
            [av show];
            return;
        }
        else
        {
            UIAlertView *av = nil;
            if ([Config isGov])
            {
                av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Invalid User Name"] message:[Localizer getLocalizedText:@"You must enter in a valid user name in order to log in"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"] otherButtonTitles:nil];
            }
            else
            {
                av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Invalid User Name"] message:[Localizer getLocalizedText:@"You must enter in a valid user name in order to log in"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"] otherButtonTitles:[Localizer getLocalizedText:@"Get Help"], nil];
            }
            av.tag = kAlertInvalidConcurCredentials;
            [av show];
            return;
        }
    }
    else
    {
        if (self.authenticationType == AuthenticationTypePinOrPassword || self.authenticationType == AuthenticationTypePin || self.authenticationType == AuthenticationTypePassword)
        {
            // If pass/pin authenticated, then check the username is in the correct format
            NSUInteger occurs = [NSString findAllOccurrences:userId ofString:@"@"];
            NSUInteger firstOccurrence = [NSString findFirstOccurrence:userId ofString:@"@"];
            if (occurs != 1 || firstOccurrence < 1)
            {
                // Inform the username of the correct username format
                MobileAlertView *av = nil;
                if ([Config isGov])
                {
                    av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"INVALID_CONCUR_CREDENTIALS"] message:[NSString stringWithFormat:[Localizer getLocalizedText:@"INVALID_USERID_MSG"], @"\n\n"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"] otherButtonTitles:nil];
                }
                else
                {
                    av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"INVALID_CONCUR_CREDENTIALS"] message:[NSString stringWithFormat:[Localizer getLocalizedText:@"INVALID_USERID_MSG"], @"\n\n"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"] otherButtonTitles:[Localizer getLocalizedText:@"Get Help"], nil];

                }
                av.tag = kAlertInvalidConcurCredentials;
                self.alertTag = kInvalidEmail;
                [av show];
                return;
            }
        }
    }
    
    if (!self.secret.length)
    {
        if (self.authenticationType == AuthenticationTypePinOrPassword)
        {
            MobileAlertView *av = nil;
            if ([Config isGov])
            {
                av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"INVALID_CONCUR_CREDENTIALS"] message:[Localizer getLocalizedText:@"You must enter in a valid password or PIN in order to log in"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"] otherButtonTitles:nil];
            }
            else
            {
                av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"INVALID_CONCUR_CREDENTIALS"] message:[Localizer getLocalizedText:@"You must enter in a valid password or PIN in order to log in"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"] otherButtonTitles:[Localizer getLocalizedText:@"Get Help"], nil];
            }
             av.tag = kAlertInvalidConcurCredentials;
            self.alertTag = kInvalidPassword;
            [av show];
            return;
        }
        else if (self.authenticationType == AuthenticationTypePassword)
        {
            MobileAlertView *av = nil;
            if ([Config isGov])
            {
                av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Invalid Password"] message:[Localizer getLocalizedText:@"You must enter in a valid password in order to log in"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"] otherButtonTitles:nil];
            }
            else
            {
                av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Invalid Password"] message:[Localizer getLocalizedText:@"You must enter in a valid password in order to log in"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"] otherButtonTitles:[Localizer getLocalizedText:@"Get Help"],nil];
            }
            av.tag = kAlertInvalidConcurCredentials;
            [av show];
            return;
        }
        else
        {
            MobileAlertView *av = nil;
            if ([Config isGov])
            {
                av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Invalid PIN"] message:[Localizer getLocalizedText:@"You must enter in a valid PIN in order to log in"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"] otherButtonTitles:nil];
            }
            else
            {
                av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Invalid PIN"] message:[Localizer getLocalizedText:@"You must enter in a valid PIN in order to log in"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Try Again"] otherButtonTitles:[Localizer getLocalizedText:@"Get Help"], nil];

            }
            av.tag = kAlertInvalidConcurCredentials;
            [av show];
            return;
        }
    }
    
    if(![ExSystem connectedToNetwork])
    {
        NSString *alertTitle = nil;
        NSString *alertMessage = nil;
        
        if([ExSystem sharedInstance].userName == nil || [ExSystem sharedInstance].pin == nil)
        {
            alertTitle = [Localizer getLocalizedText:@"Invalid Login Credentials"];
            alertMessage = [Localizer getLocalizedText:@"Must log in online first"];
        }
        else
        {
            alertTitle = [Localizer getLocalizedText:@"Invalid Login Credentials"];
            
            // TODO: Update this when we start supporting offline authentication for AuthenticationTypePinOrPassword
            alertMessage = (self.authenticationType == AuthenticationTypePassword ? @"Username or Password is invalid" : @"Username or PIN is invalid");
        }
        
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:alertTitle
                              message:alertMessage
                              delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
        
        [alert show];
        [self markFirstResponder:1];
    }
    else
    {
        [ExSystem sharedInstance].userName = self.userId;
        [[ExSystem sharedInstance] clearUserCredentialsAndSession]; // PIN will be saved later if and when authentication succeeds
        [[ExSystem sharedInstance] saveSettings];
        
        //			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Login for %@", self.userId == nil? @"":self.userId] Level:MC_LOG_INFO];
        
        NSMutableDictionary *pBag = nil;
        
        pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:LOGIN, @"TO_VIEW", self.userId, @"USER_ID", @"YES", @"SKIP_CACHE", nil];
        
        // MOB-13981 encode the PIN 
        if (self.authenticationType == AuthenticationTypePinOrPassword)
            pBag[@"PIN_OR_PASSWORD"] = [NSString stringByEncodingXmlEntities:self.secret];
        else if (self.authenticationType == AuthenticationTypePassword)
            pBag[@"PASSWORD"] = [NSString stringByEncodingXmlEntities:self.secret];
        else
            pBag[@"PIN"] = [NSString stringByEncodingXmlEntities:self.secret];
        
        [[ExSystem sharedInstance].msgControl createMsg:AUTHENTICATION_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

		// MOB-17666 Missing waiting view while signing in
        [self showWaitViewWithText:[Localizer getLocalizedText: @"Authenticating"]];
        [self resignFirstResponder: 0];
        [self resignFirstResponder: 1];
        
    }
}
#pragma mark - Gov button handlers
- (IBAction)btnPrivacyActPressed:(id)sender
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSArray *allMessage = [BaseManager fetchAll:@"EntityWarningMessages" withContext:[ad managedObjectContext]];
    if ([allMessage count] > 0)
    {
        self.allMessages = allMessage[0];
    }

    MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:allMessages.privacyTitle message:allMessages.privacyText delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
    
    [alert show];
}

- (IBAction)btnWarningPressed:(id)sender
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSArray *allMessage = [BaseManager fetchAll:@"EntityWarningMessages" withContext:[ad managedObjectContext]];
    if ([allMessage count] > 0)
    {
        self.allMessages = allMessage[0];
    }
    
    MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:allMessages.warningTitle message:allMessages.warningText delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];

    [alert show];
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 2;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath

{
    NSUInteger row = [indexPath row];
    
    EditInlineCell *cell = (EditInlineCell *)[tableView dequeueReusableCellWithIdentifier: @"EditInlineCell"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"EditInlineCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[EditInlineCell class]])
                cell = (EditInlineCell *)oneObject;
    }
    
    
    cell.parentVC = self;
    cell.rowPos = indexPath.row;
    
    NSString *userPlaceholder = [Localizer getLocalizedText:@"LABEL_LOGIN_USER_NAME"];
	NSString *pwdPlaceholder = [Localizer getLocalizedText:@"LABEL_LOGIN_PASSWORD"];
    if(row == 0)
    {
        if ([ExSystem sharedInstance].entitySettings.saveUserName != nil &&
            [[ExSystem sharedInstance].entitySettings.saveUserName isEqualToString:@"YES"] &&
            [ExSystem sharedInstance].userName != nil &&
            [ExSystem sharedInstance].userName.length > 0)
        {
            cell.txt.text = ([ExSystem sharedInstance].isCorpSSOUser)? nil : [ExSystem sharedInstance].userName;
        }
        else
        {
            cell.txt.text = @"";
        }

        cell.txt.placeholder = userPlaceholder;
            
        self.userId = cell.txt.text;
            
        cell.txt.secureTextEntry = NO;
        cell.txt.keyboardType = UIKeyboardTypeEmailAddress;
        cell.txt.returnKeyType = UIReturnKeyNext;
        cell.txt.inputAccessoryView = self.viewKeyboard;
        cell.txt.autocorrectionType = UITextAutocorrectionTypeNo;
    }
    else
    {
        cell.txt.placeholder = pwdPlaceholder;
        cell.txt.secureTextEntry = YES;
        cell.txt.keyboardType = UIKeyboardTypeDefault;
        cell.txt.returnKeyType = UIReturnKeyGo;
        cell.txt.text = self.secret;
        cell.txt.placeholder = pwdPlaceholder;
        cell.txt.inputAccessoryView = self.viewKeyboard;
    }
    return cell;
    
}



#pragma mark -
#pragma mark Table View Delegate Methods
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 50;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	
}

-(UIView*) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    return nil;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return @"";
}

-(CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 0;
}

-(CGFloat) tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
    return 150;
}

-(UIView*) tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
{
    if(section == 0)
    {
        float maxW = 320;
        float x = 20;
        
        float y = 15;
        __autoreleasing UIView *v = [[UIView alloc] initWithFrame:CGRectMake(0, 0, maxW, 150)];
        
        {
            self.btnJoinGreen = [ExSystem makeColoredButtonRegular:@"SIGN_IN_GREEN" W:302 H:45 Text:[Localizer getLocalizedText:@"Sign in to Concur"] SelectorString:@"buttonSignInPressed:" MobileVC:self];
            btnJoinGreen.frame = CGRectMake(9, 9, btnJoinGreen.frame.size.width, btnJoinGreen.frame.size.height);
            [btnJoinGreen addTarget:self action:@selector(buttonSignInPressed:) forControlEvents:UIControlEventTouchUpInside];
            
            btnJoinGreen.titleLabel.font = [UIFont boldSystemFontOfSize:17.0];
            
            [v addSubview:btnJoinGreen];
            
            if ([Config isGov])
            {
                // SSO button
                NSString *ssoButtonText = nil;
                ssoButtonText = [Localizer getLocalizedText:@"Agency Code Sign In"];
                
                UIColor *ssoTextColor = [UIColor colorWithRed:2.0/255.0 green:64.0/255.0 blue:116.0/255.0 alpha:1.0];
                self.btnSSOSignIn = [UIButton buttonWithType:UIButtonTypeCustom];
                self.btnSSOSignIn.frame = CGRectMake(x, y + 44, 280, 35);
                [self.btnSSOSignIn setTitle:ssoButtonText forState:UIControlStateNormal];
                self.btnSSOSignIn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
                self.btnSSOSignIn.titleLabel.font = [UIFont systemFontOfSize:14.0];
                self.btnSSOSignIn.titleLabel.textColor = ssoTextColor;
                [self.btnSSOSignIn setTitleColor:ssoTextColor forState:UIControlStateNormal];
                [self.btnSSOSignIn addTarget:self action:@selector(buttonSSOPressed:) forControlEvents:UIControlEventTouchUpInside];
                
                [v addSubview:self.btnSSOSignIn];
            }
            else
            {
                // Forget password button
                NSString *forgotPasswordBtnText = nil;
                forgotPasswordBtnText = [Localizer getLocalizedText:@"Forgot Username or Password?"];
                
                UIColor *forgotPasswordTextColor = [UIColor colorWithRed:2.0/255.0 green:64.0/255.0 blue:116.0/255.0 alpha:1.0];
                self.btnForgotPassword = [UIButton buttonWithType:UIButtonTypeCustom];
                self.btnForgotPassword.frame = CGRectMake(x, y + 44, 280, 35);
                [self.btnForgotPassword setTitle:forgotPasswordBtnText forState:UIControlStateNormal];
                self.btnForgotPassword.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
                self.btnForgotPassword.titleLabel.font = [UIFont systemFontOfSize:14.0];
                self.btnForgotPassword.titleLabel.textColor = forgotPasswordTextColor;
                [self.btnForgotPassword setTitleColor:forgotPasswordTextColor forState:UIControlStateNormal];
                [self.btnForgotPassword addTarget:self action:@selector(buttonCorpSignUpPressed:) forControlEvents:UIControlEventTouchUpInside];
                
                [v addSubview:self.btnForgotPassword];
            }
        }
        return v;
    }
    else
        return nil;
}

-(IBAction)buttonJoinPressed:(id)sender
{
    if (self.userId == nil || [self.userId length] == 0)
    {
        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Invalid email address"] message:[Localizer getLocalizedText:@"You must enter in a valid email address in order for Concur to verify it"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
        [av show];
        return;
    }
    
    NSString *msg = [NSString stringWithFormat:[Localizer getLocalizedText:@"Verification is simple. Just tap on the link in the email we sent to"], self.userId];
    MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Please verify your email"] message:msg delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles: nil];
    av.tag = kAlertTagVerifyEmail;
    [av show];
    
}

-(void) showEmailWaitView
{
    // TODO
}

- (IBAction)buttonResetPasswordPressed:(id) sender
{
    if ([self.userId isValidEmail])
    {
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:LOGIN, @"TO_VIEW",
                                     self.userId, @"LOGIN_ID",
                                     @"YES", @"SKIP_CACHE", nil];
        [[ExSystem sharedInstance].msgControl createMsg:RESET_PASSWORD_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    else
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:nil
                              message:[Localizer getLocalizedText:@"FIX_EMAIL_RESET_PASSWORD"]
                              delegate:self
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                              otherButtonTitles:nil];
        
        [alert show];
        
    }
}

#pragma mark -
#pragma mark EditInlineCellDelegate Methods
- (BOOL)cellTextFieldShouldReturn:(EditInlineCell *)cell
{
    if(cell.txt.secureTextEntry == YES)
    {
        [self buttonSignInPressed:nil];
    }
    else
        [self markFirstResponder:1];
    
    return YES;
}

-(IBAction) cellTextEdited:(EditInlineCell*)cell
{
    if(cell.rowPos ==0)
        self.userId = cell.txt.text;
    else if (cell.rowPos ==1)
        self.secret = cell.txt.text;
}

-(void) cellScrollMeUp:(EditInlineCell*)cell
{    
    self.tableList.contentInset =  UIEdgeInsetsMake(0, 0, 130, 0);
    [self.tableList scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:cell.rowPos inSection:0] atScrollPosition:UITableViewScrollPositionTop animated:YES];
}

#pragma mark SUSignInDelegate
-(void) actionStarted:(NSString*) description
{
    if (!description.length)
        [self showWaitView];
    else
        [self showWaitViewWithText:description];
}

#pragma mark - View Finders
+(LoginViewController*) findLoginViewController
{
	MobileViewController *lvc = [ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:@"LOGIN"];
	return (LoginViewController*)lvc;
}

+(BOOL) isLoginViewShowing
{
	LoginViewController *lvc = [LoginViewController findLoginViewController];
	return (lvc != nil);
}


#pragma mark - close me
-(void)closeMe:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - test drive
// Call show testdrive. 
- (IBAction)registerTestDrivePressed:(id)sender
{
    [self showTestDrive];
}

-(BOOL)isLoadedFromStoryboard
{
    if(self.storyboard !=nil )
    {
        return YES;
    }
    return  NO;
}
@end
