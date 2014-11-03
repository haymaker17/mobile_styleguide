//
//  LoginViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 10/27/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#import "MobileViewController.h"
#import "iPadHomeVC.h"
#import "EditInlineCell.h"
#import "EditInlineCellDelegate.h"
#import "EditField.h"
#import "LoginDelegate.h"
#import "GovWarningMessagesData.h"
#import "LoginHelpContentsVC.h"
#import "RPFloatingPlaceholderTextField.h"

@class RootViewController;

@interface LoginViewController : MobileViewController <UITableViewDataSource, UITableViewDelegate,
    UIAlertViewDelegate, EditInlineCellDelegate, UITextViewDelegate>{

	UIButton	*registerButton, *settingsButton;

    int                     authenticationType;

    BOOL					isAuthenticate, isJoinVerify;

	iPadHomeVC				*padHomeVC;
    
    UITableView             *tableList;
    
    NSString                *userId, *secret;
    
    UIImageView             *ivBackground, *ivLogo;
    
    UIView                  *viewSignUp;
    UIView                  *viewSignUpBtn;
        
    UIView                  *viewCorpSignUp;
    UIView                  *viewGovNotice;
    UIButton                *btnJoinGreen;
        
    // Generated UI
    UIButton                *btnSignUp;
    UIButton                *btnResetPassword;
    UILabel                 *lblHaveNoAccount;
    UIButton                *btnSignIn;

    UIButton                *btnCorpSignUp;
    UIButton                *btnCorpSignUpAccessory;
        
    UIButton                *btnForgotPassword;
    UIButton                *btnSSOSignIn;

    id<LoginDelegate>       loginDelegate;
    EntityWarningMessages       *allMessages;

}
@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;
@property (nonatomic, strong) EntityWarningMessages         *allMessages;

@property (strong, nonatomic) id<LoginDelegate>      loginDelegate;
@property (nonatomic, strong) IBOutlet UIButton             *btnJoinGreen;

@property (nonatomic, strong) IBOutlet UIImageView          *ivBackground;
@property (nonatomic, strong) IBOutlet UIImageView          *ivLogo;
@property (nonatomic, strong) IBOutlet UIButton				*registerButton;
@property (nonatomic, strong) IBOutlet UIButton				*settingsButton;
@property (nonatomic, strong) IBOutlet UITableView          *tableList;
@property BOOL isAuthenticate;

@property (nonatomic, strong) IBOutlet UIView *viewKeyboard;

@property (nonatomic, strong) iPadHomeVC                    *padHomeVC;
@property (strong, nonatomic) NSString                      *userId;
@property (strong, nonatomic) NSString                      *secret;
@property (assign, nonatomic) int                           authenticationType;

@property (nonatomic, strong) IBOutlet UIButton				*btnSignIn;
@property (nonatomic, strong) IBOutlet UIButton				*btnSignUp;
@property (nonatomic, strong) IBOutlet UIButton             *btnResetPassword;
@property (nonatomic, strong) IBOutlet UILabel              *lblHaveNoAccount;
@property (nonatomic, strong) IBOutlet UIView               *viewSignUp;
@property (nonatomic, strong) IBOutlet UIView               *viewSignUpBtn;
@property (nonatomic, strong) IBOutlet UIView               *viewGovNotice;
@property (nonatomic, strong) IBOutlet UIView               *viewCorpSignUp;
@property (nonatomic, strong) IBOutlet UIButton             *btnCorpSignUp;
@property (nonatomic, strong) IBOutlet UIButton             *btnCorpSignUpAccessory;
@property (nonatomic, strong) IBOutlet UIButton             *btnSSOSignIn;
@property int tryAgainCount;

#pragma  mark - New storyboard handlers for login.storyboard
@property (weak, nonatomic) IBOutlet UITextField *txtUsernameField;
@property (weak, nonatomic) IBOutlet UITextField *txtPasswordField;
@property (readonly) BOOL isLoadedFromStoryboard;
@property (weak, nonatomic) IBOutlet UIButton *btnSignInWithStoryBoard;
@property (weak, nonatomic) IBOutlet UIButton *btnSiginInHelpStoryBoard;
@property (strong, nonatomic) IBOutlet UIButton  *btnSSOSignInWithStoryBoard;

#pragma mark - New ipad storyboard constraint handlers

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaLea;

- (IBAction)registerTestDrivePressed:(id)sender;

// MOB-16251 - bug where keyboard pops up over sign in / register for test drive screen
// setting this to true skips display of keyboard in viewWillAppear
// it is required to complete this hack that the skipped functionality must be called before
// the login view is displayed
@property bool skipKeyboardDisplayHack;


#pragma mark - Corporate button handlers
- (IBAction)buttonCorpSignUpPressed:(id)sender; // Called when you press "Sign In Help" at bottom of screen
- (IBAction)buttonSSOPressed:(id)sender;       // Called when you press the "Company Code Sign In" button
- (IBAction)buttonSettingsPressed:(id)sender;  // Called when you press the 'i' info button in the bottom right
- (IBAction)buttonSignInPressed:(id)sender;     // Called when you press the "Sign In" button

#pragma mark - Non-corporate button handlers
@property (nonatomic, strong) IBOutlet UIButton             *btnForgotPassword;
- (IBAction)buttonResetPasswordPressed:(id) sender;

-(void)resetForLandscape;
-(void)resetForPortrait;

-(void)layoutPad:(BOOL)forceLandscape;
-(void)recordPinOrPassword;
-(void)signInPad;

-(void) markFirstResponder:(int) row;
- (IBAction)backgroundTap:(id)sender;
- (IBAction)backgroundSSOTap:(id)sender;

-(void) resignFirstResponder:(int) row;

#pragma mark - Gov button handlers
- (IBAction)btnWarningPressed:(id)sender;
- (IBAction)btnPrivacyActPressed:(id)sender;

#pragma mark - Inline edit cell callbacks
- (BOOL) cellTextFieldShouldReturn:(EditInlineCell *)cell;
-(IBAction) cellTextEdited:(EditInlineCell*)sender;
-(void) cellScrollMeUp:(EditInlineCell*)sender;

#pragma mark - View Finders
+(LoginViewController*) findLoginViewController;
+(BOOL) isLoginViewShowing;

#pragma mark - close me
-(void)closeMe:(id)sender;

- (void) dismissKeyboard;

@end
