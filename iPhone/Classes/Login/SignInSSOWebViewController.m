//
//  SignInSSOWebViewController.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SignInSSOWebViewController.h"
#import "CTESingleSignOnWebView.h"
#import "ApplicationLock.h"
#import "WaitViewController.h"
#import "AnalyticsTracker.h"

@interface SignInSSOWebViewController ()

@property (weak, nonatomic) IBOutlet CTESingleSignOnWebView *cteSSOWebView;

@end

@implementation SignInSSOWebViewController

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
    // login and callback with success or failure
    [self.cteSSOWebView loadConcurMobileSingleSignOnWebpage:self.ssoURL tokenFound:^() {
        [WaitViewController showWithText:@"" animated:YES];
    } success:^(NSString *loginXML) {
        ALog(@" SSO Sign in successful");
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        [[ExSystem sharedInstance] saveCompanySSOLoginPageUrl:self.ssoURL];
        [self loginAndShowHome:loginXML];
    } failure:^(CTEError *error) {
        ALog(@" SSO Sign in Failed");
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        // TODO : Show the proper error message here . Temp message for now
        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:@"Unable to Sign In"
                                                             message:@"We are unable to sign you in with this information"
                                                            delegate:self
                                                   cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                                   otherButtonTitles:nil];
        [av show];
    }];
    [AnalyticsTracker initializeScreenName:@"SSO"];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    [AnalyticsTracker resetScreenName];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void) loginAndShowHome:(NSString *)loginXML
{
    // Do a manual login process similar to what ApplicationLock would do.
    // Dont unwind views to home here, try to refresh the calling view so that the login view can be shown anywhere
    // Callback blocks for the calling view controller.
    DLog(@" Login result : %@" , loginXML);
    Authenticate *auth = [[Authenticate alloc] init];
    [ExSystem sharedInstance].isCorpSSOUser = YES;
    NSMutableDictionary *pBag = [@{@"IS_CORP_SSO": @"YES"} mutableCopy];
    
    Msg *msg = [auth newMsg:pBag];
    [msg.responder parseXMLFileAtData:[loginXML dataUsingEncoding:NSUTF8StringEncoding]];
    [ApplicationLock sharedInstance].isUserLoggedIn  = YES;
    [ApplicationLock sharedInstance].isShowLoginView = YES;
    //Login process expectes an (Msg *) object Identify a better way to send the login result to the the
    // For now mock up a msg object.
    ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    appDelegate.topView = HOME_PAGE;
    [[ApplicationLock sharedInstance] onLoginSucceeded:msg];
    // Dismiss the story board
    [self dismissViewControllerAnimated:YES completion:nil];
    
    NSString *eventLabel = [NSString stringWithFormat:@"Credential Type: %@", @"SSO"];
    [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Successful Attempt" eventLabel:eventLabel eventValue:nil];
    
    eventLabel = [NSString stringWithFormat:@"User Type: %@",[[ExSystem sharedInstance] getUserType]];
    [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Successful Attempt" eventLabel:eventLabel eventValue:nil];
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

@end
