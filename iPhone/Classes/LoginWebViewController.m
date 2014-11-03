//
//  LoginWebViewController.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/28/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "LoginWebViewController.h"
#import "CorpSSOAuthenticate.h"
#import "Authenticate.h"
#import "ApplicationLock.h"

@interface LoginWebViewController (Private)
-(void) launchCompanySSOPage;
-(void) displayRemoteWipeAlert;
-(void) displayServerErrorAlert;
-(void) clearWebBrowserCache;
-(NSHTTPCookie *) getCorpSSOSessionCookie;
-(void) startConcurLogin;
@end

@implementation LoginWebViewController
@synthesize webView, loginUrl, sessionId, loginDelegate;

-(NSString *)getViewIDKey
{
	return CORP_SSO_VIEW;
}

-(void) respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:CORP_SSO_AUTHENTICATION_DATA]) 
    {
        CorpSSOAuthenticate *responder = (CorpSSOAuthenticate *)msg.responder;
        
        if ([self isViewLoaded])
            [self hideLoadingView];
        
        if (msg.errBody == nil && responder != nil) 
        {
            [self startConcurLogin];
        }
        else
        {
            [self displayServerErrorAlert];
        }
    }
    else if ([msg.idKey isEqualToString:AUTHENTICATION_DATA]) 
    {
        Authenticate *auth = (Authenticate *)msg.responder;
        
        if ([self isViewLoaded])
            [self hideLoadingView];
        
        if (auth != nil && [auth.remoteWipe isEqualToString:@"Y"])
        {
            // Handle remote wipe
            [[ApplicationLock sharedInstance] wipeApplication];
            [self displayRemoteWipeAlert];
        }
        else
        {
            // Handle other cases
            if (msg.responseCode == 200 && auth.sessionID != nil && [auth.sessionID length] > 0)
            {
                NSString *userName = [NSString stringWithFormat:@"%@_%@", [ExSystem sharedInstance].entitySettings.firstName, [ExSystem sharedInstance].entitySettings.lastName];
                [[ExSystem sharedInstance] setUserName:userName];
                [[ExSystem sharedInstance] saveUserId:userName];
                
                [[ExSystem sharedInstance] setIsCorpSSOUser:YES];
                //[[ExSystem sharedInstance] setSessionID:auth.sessionID]; // Session will be set by onLoginSucceeded
                
                [[ApplicationLock sharedInstance] onLoginSucceeded:msg];
                
                if ([UIDevice isPad] && loginDelegate != nil && [loginDelegate respondsToSelector:@selector(dismissYourself:)])
                    [self.loginDelegate dismissYourself:self]; 
                else
                    [self dismissViewControllerAnimated:YES completion:nil];
            }
            else
            {
                [self displayServerErrorAlert];
            }
        }
    }
}

-(void) launchCompanySSOPage
{
    self.loginUrl = [[ExSystem sharedInstance] loadCompanySSOLoginPageUrl];
    
    if (loginUrl != nil)
        [webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:loginUrl]]];
}

-(void)loadWebRequest:(NSURLRequest *)request
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:CORP_SSO_VIEW, @"TO_VIEW", request, @"HTTP_REQUEST", nil];
    
	[[ExSystem sharedInstance].msgControl createMsg:CORP_SSO_AUTHENTICATION_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) clearWebBrowserCache
{
    // wipe all cookies on start
    NSHTTPCookie *cookie;
    NSHTTPCookieStorage *storage = [NSHTTPCookieStorage sharedHTTPCookieStorage];
    for (cookie in [storage cookies]) {
        [storage deleteCookie:cookie];
    }
    [[NSUserDefaults standardUserDefaults] synchronize];
    
    // remove all cached responses
    [[NSURLCache sharedURLCache] removeAllCachedResponses];    
}

-(void) displayRemoteWipeAlert
{
    MobileAlertView *alert = [[MobileAlertView alloc] 
                              initWithTitle:[Localizer getLocalizedText:@"Warning"]
                              message:[@"COMPANY_SSO_INITIATED_REMOTE_WIPE" localize]
                              delegate:self 
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                              otherButtonTitles:nil];
    [alert show];
}

-(void) displayServerErrorAlert
{
    MobileAlertView *alert = [[MobileAlertView alloc] 
                          initWithTitle:[Localizer getLocalizedText:@"ERROR"]
                          message:[@"ERROR_CONFIGURING_SESSION" localize]
                          delegate:self 
                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                          otherButtonTitles:nil];
    [alert show];
}

-(void) startConcurLogin
{
    // We have the session cookie, now proceed to login
    NSHTTPCookie *cookie = [self getCorpSSOSessionCookie];

    if (cookie != nil) 
    {
        self.sessionId = [cookie value];

        NSArray *cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookies];
        for (NSHTTPCookie *cookie in cookies)
        {
            if (cookie != nil)
            {
                [[NSHTTPCookieStorage sharedHTTPCookieStorage] deleteCookie:cookie];
            }
        }
        [[NSUserDefaults standardUserDefaults] synchronize];
        
        [self showLoadingViewWithText:[@"CONFIGURING_SESSION" localize]];
        
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:CORP_SSO_VIEW, @"TO_VIEW",sessionId, @"SESSION_ID", @"YES",@"IS_CORP_SSO", nil];
        
        [[ExSystem sharedInstance].msgControl createMsg:AUTHENTICATION_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}


-(NSHTTPCookie *)getCorpSSOSessionCookie
{
    NSArray *cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookies];
    
    NSHTTPCookie *c = nil;
    
    for (NSHTTPCookie *cookie in cookies) 
    {
            if ([[cookie name] isEqualToString:@"OTSESSIONAABQRN"])
        {
            self.sessionId = [cookie value];
            c = cookie;
            break;
        }
    }
    
    return c;
}

#pragma mark -
#pragma mark ViewController Methods
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
}

#pragma mark - View lifecycle
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];
    self.title = [@"Company Sign On" localize];
    
    self.webView = [[UIWebView alloc] initWithFrame:CGRectMake(0, 0, [self.view bounds].size.width, [self.view bounds].size.height)];
    self.view = webView;
    webView.delegate = self;

    [[NSHTTPCookieStorage sharedHTTPCookieStorage] setCookieAcceptPolicy:NSHTTPCookieAcceptPolicyAlways];
    
    // MOB-13264 only if we have autologin disabled clear the web cache
    if (![[ExSystem sharedInstance].entitySettings.autoLogin isEqualToString:@"YES"]) {
        [self clearWebBrowserCache];
    }
    
    [self launchCompanySSOPage];
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    if ([UIDevice isPad]) 
        self.navigationController.navigationBarHidden = NO;
}

-(void)dealloc
{
    webView.delegate = nil;
}

#pragma mark Web View Delgate methods
- (BOOL)webView:(UIWebView *)wv shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{      
    NSString *urlString = [[request URL].absoluteString lowercaseString];
    NSRange rangeOfSubstring = [urlString rangeOfString:@"wait.asp"];
    
    if (rangeOfSubstring.length > 0) 
    {
        [wv stopLoading];
        [self loadWebRequest:request];
        return NO;
    }
    
    return YES;
}

#pragma mark Alert View Delgate methods
- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    NSHTTPCookie *cookie = [self getCorpSSOSessionCookie];
    if (cookie != nil) 
    {
        [[NSHTTPCookieStorage sharedHTTPCookieStorage] deleteCookie:cookie];
    }
    
    [self.navigationController popViewControllerAnimated:YES];
}
@end
