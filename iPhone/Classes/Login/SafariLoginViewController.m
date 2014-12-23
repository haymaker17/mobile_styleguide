//
//  SafariLoginViewController.m
//  ConcurMobile
//
//  Created by Ray Chi on 11/17/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SafariLoginViewController.h"
#import "CTECorpSsoLogin.h"
#import "ApplicationLock.h"
#import "KeychainManager.h"
#import "WaitViewController.h"
#import "AnalyticsTracker.h"

@interface SafariLoginViewController ()

@end

@implementation SafariLoginViewController

- (id) initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [WaitViewController showWithText:@"Autheticating the session..." animated:YES];
    
    self.token = [[ExSystem sharedInstance] loadConcurSSOAccessToken];
    [[ExSystem sharedInstance] clearAccessToken];
    
    CTECorpSsoLogin *ssoLogin = [[CTECorpSsoLogin alloc] initWithSingleSignOnToken:self.token];
    [ssoLogin loginConcurMobileWithSuccess:^(NSString *loginXML){
        ALog(@" SSO Sign in successful");
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        [self loginAndShowHome:loginXML];
        
    }failure:^(CTEError *error){
        
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:@"Unable to Sign In"
                                                             message:@"We are unable to sign you in with this information"
                                                            delegate:self
                                                   cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                                   otherButtonTitles:nil];
        [av show];

        
    }];
}

-(void) loginAndShowHome:(NSString *)loginXML
{

    DLog(@" Login result : %@" , loginXML);
    Authenticate *auth = [[Authenticate alloc] init];
    [ExSystem sharedInstance].isCorpSSOUser = YES;
    NSMutableDictionary *pBag = [@{@"IS_CORP_SSO": @"YES"} mutableCopy];
    
    Msg *msg = [auth newMsg:pBag];
    [msg.responder parseXMLFileAtData:[loginXML dataUsingEncoding:NSUTF8StringEncoding]];
    [ApplicationLock sharedInstance].isUserLoggedIn  = YES;
    [ApplicationLock sharedInstance].isShowLoginView = YES;

    ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    appDelegate.topView = HOME_PAGE;
    [[ApplicationLock sharedInstance] onLoginSucceeded:msg];
    [self dismissViewControllerAnimated:YES completion:nil];
    
    NSString *eventLabel = [NSString stringWithFormat:@"Credential Type: %@", @"SSO"];
    [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Successful Attempt" eventLabel:eventLabel eventValue:nil];
    
    eventLabel = [NSString stringWithFormat:@"User Type: %@",[[ExSystem sharedInstance] getUserType]];
    [AnalyticsTracker logEventWithCategory:@"Sign In" eventAction:@"Successful Attempt" eventLabel:eventLabel eventValue:nil];

}



- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



@end
