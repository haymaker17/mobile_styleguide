//
//  TripItAuthVC.m
//  ConcurMobile
//
//  Created by  on 3/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TripItAuthVC.h"
#import "TripItRequestTokenData.h"
#import "TripItAccessTokenData.h"
#import "TripItExpenseTripData.h"

#import "ReportViewControllerBase.h"
#import "Localizer.h"
#import "TripItLink.h"

@interface TripItAuthVC (private)
-(void) didObtainRequestToken;
-(void) didAuthorizeRequestToken;
-(void) didDeclineToAuthorizeRequestToken;
+(NSString*) getTripItDomain;
+(NSString*) getTripItAuthorizationUrl;
@end

@implementation TripItAuthVC

@synthesize webView, requestTokenKey, requestTokenSecret;

#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return TRIPIT_AUTH;
}

#pragma mark - Nib and Memory

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
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.title = [@"Sign in to TripIt" localize];
    
//    //todo, to test
//    [ExSystem sharedInstance].isTripItLinked = NO;
//    [[ExSystem sharedInstance] saveSystem];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

#pragma mark - View event handlers
- (void)viewDidAppear:(BOOL)animated 
{
	[super viewDidAppear:animated]; 
    
    // TODO: This might not be the right place to obtain the request token.  Needs reassessment.
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil]; // This mvc will not be called unless pbag is provided
    [[ExSystem sharedInstance].msgControl createMsg:OBTAIN_TRIPIT_REQUEST_TOKEN CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    
    [self showWaitView];
}

-(void) dealloc
{
    webView.delegate = nil;
}

#pragma mark - Helpers

+(NSString*) getTripItDomain
{
    NSRange rqa3Range = [[ExSystem sharedInstance].entitySettings.uri rangeOfString:@"rqa3" options:NSCaseInsensitiveSearch];
    return (rqa3Range.location == NSNotFound ? @"m.tripit.com" : @"m.dev.tripit.com");
}

+(NSString*) getTripItAuthorizationUrl
{
    return [NSString stringWithFormat:@"https://%@/oauth/authorize", [TripItAuthVC getTripItDomain]];
}

#pragma mark - Handlers
-(void) didObtainRequestToken
{
    // TODO: Figure out a robust way to get the URL.  The URL exists in Concur's database.  The Concur server
    // could pass down this URL along with the request token key that we obtain with the OBTAIN_TRIPIT_REQUEST_TOKEN
    // message.  The most robust solution would be to use that.
    //
    NSString *authorizationCallback = @"http://localhost/mobile/auth"; // After the user presses 'Grant' or 'Deny', TripIt will send a 302 (redirect) to this callback.  We will intercept it; we will not allow the web view to actually go there.
    
    NSString *fullUrl = [NSString stringWithFormat:@"%@?oauth_token=%@&oauth_callback=%@", [TripItAuthVC getTripItAuthorizationUrl], requestTokenKey, authorizationCallback];
    
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"TripItAuthVC::didObtainRequestToken: Web view url is %@", fullUrl] Level:MC_LOG_DEBU];
    
    // Remove any TripIt-related cookies
    NSArray* cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookies];
    for (NSHTTPCookie *cookie in cookies)
    {
        if (cookie.domain != nil && [cookie.domain rangeOfString:@"tripit" options:NSCaseInsensitiveSearch].location != NSNotFound)
        {
            [[NSHTTPCookieStorage sharedHTTPCookieStorage] deleteCookie:cookie];
        }
    }
    
    NSURL *reqUrl = [NSURL URLWithString:fullUrl];

	NSURLRequest *req = [NSURLRequest requestWithURL:reqUrl];
    [webView loadRequest:req];
}

-(void) didAuthorizeRequestToken
{
	[[MCLogging getInstance] log:@"TripItAuthVC::didAuthorizeRequestToken" Level:MC_LOG_DEBU];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.requestTokenKey, @"REQUEST_TOKEN_KEY", self.requestTokenSecret, @"REQUEST_TOKEN_SECRET", nil];
    [[ExSystem sharedInstance].msgControl createMsg:TRIPIT_LINK CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    
    [self showWaitView];
}

-(void) didDeclineToAuthorizeRequestToken
{
    MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Unable to link"] message:[Localizer getLocalizedText:@"SmartExpense was denied access to your TripIt account."] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
    [av show];
    
    if(![UIDevice isPad])
    {
        [self.navigationController popViewControllerAnimated:YES];
    }
    else
    {
        iPadHomeVC* padVC = [ConcurMobileAppDelegate findiPadHomeVC];
        [padVC checkStateOfTrips];
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}

#pragma mark - MessageHandlers

-(void)respondToFoundData:(Msg *)msg
{
	[[MCLogging getInstance] log:@"TripItAuthVC::respondToFoundData" Level:MC_LOG_DEBU];
        
	if ([msg.idKey isEqualToString:OBTAIN_TRIPIT_REQUEST_TOKEN])
	{
        [self hideWaitView];
        
        TripItRequestTokenData *data = (TripItRequestTokenData*)msg.responder;
        
        if (data.requestTokenKey != nil && data.requestTokenSecret != nil)
        {
            // SUCCEESSFULLY obtained request token
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"TripItAuthVC::respondToFoundData: request token key is %@", data.requestTokenKey] Level:MC_LOG_DEBU];
            self.requestTokenKey = data.requestTokenKey;
            self.requestTokenSecret = data.requestTokenSecret;
            [self didObtainRequestToken];
        }
        else
        {
            [[MCLogging getInstance] log:@"TripItAuthVC::respondToFoundData: failed to obtain request token" Level:MC_LOG_DEBU];
            
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Error"] message:[Localizer getLocalizedText:@"An unexpected error occurred. Please try again later."] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
            [av show];
            
            [self closeMe:self];
        }
    }
    else if ([msg.idKey isEqualToString:TRIPIT_LINK])
    {
        [self hideWaitView];
        
        TripItLink *linkData = (TripItLink*)msg.responder;
        ActionStatus *linkStatus = linkData.linkStatus;

        bool didSucceed = (msg.responseCode == 200 && linkStatus != nil && linkStatus.status != nil && [linkStatus.status isEqualToString:@"SUCCESS"]);
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"TripItAuthVC::respond to found data: attempt to link to TripIt account %@", (didSucceed ? @"succeeded" : @"failed")] Level:MC_LOG_DEBU];
        
        if (didSucceed)
        {
            [ExSystem sharedInstance].isTripItLinked = YES;
            [[ExSystem sharedInstance] saveSettings];
            
            if (true)// TODO: if no trip to expense, then just let the user know that the link succeeded
            {
                NSString *alertTitle = nil;
                NSString *alertMessage = nil;
                
                // Even if linking of accounts was successful, the syncing of trips may not have been.  If there was a problem syncing trips, then a message about it will be inside the ErrorMessage node.
                if (linkStatus != nil && [linkStatus.errMsg length])
                {
                    alertTitle = [Localizer getLocalizedText:@"Warning"];
                    alertMessage = linkStatus.errMsg;
                }
                else
                {
                    alertTitle = [Localizer getLocalizedText:@"Link Success"];
                    alertMessage = [Localizer getLocalizedText:@"You have successfully linked your TripIt account to SmartExpense."];
                }
                
                MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:alertTitle message:alertMessage delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
                [av show];
                
                if(![UIDevice isPad])
                {
                    [self.navigationController popViewControllerAnimated:NO];
                    [ConcurMobileAppDelegate switchToView:TRIPS viewFrom:HOME_PAGE ParameterBag:nil];
                }
                else
                {
                   iPadHomeVC* padVC = [ConcurMobileAppDelegate findiPadHomeVC];
                   [padVC checkStateOfTrips];
                   [self dismissViewControllerAnimated:YES completion:nil];
                }
            }
            else // Else begin the expensing of the trip
            {
                // If TripIt launched SmartExpense and provided the id of a trip to be expensed, but the accounts had to be linked first, then now is the time to go ahead and expense the trip.
            }
        }
        else
        {
            NSString *errorMessage = nil;
            
            if (linkStatus != nil && [linkStatus.errMsg length])
                errorMessage = linkStatus.errMsg;
            else
                errorMessage = [Localizer getLocalizedText:@"SmartExpense was unable to access your TripIt account.  Please try again later."];
            
             MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Error"] message:errorMessage delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
            [av show];
            
            [self closeMe:self];
        }
    }
}

#pragma mark - UIWebViewDelegate Methods
- (BOOL) webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    NSString *url = request.URL.absoluteString;
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"TripItAuthVC::shouldStartLoadWithRequest: url is %@", url] Level:MC_LOG_DEBU];
    ;
    
    NSRange callbackRange = [url rangeOfString:[TripItAuthVC getTripItDomain] options:NSCaseInsensitiveSearch];
    if (callbackRange.location != NSNotFound)
        return YES;

    NSRange oauthTokenRange = [url rangeOfString:@"oauth_token" options:NSCaseInsensitiveSearch];
    
    BOOL gotPermission = (oauthTokenRange.location != NSNotFound);
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"TripItAuthVC::shouldStartLoadWithRequest: access was %@", (gotPermission ? @"granted" : @"denied")] Level:MC_LOG_DEBU];
    ;
    
    if (gotPermission)
    {
        [self didAuthorizeRequestToken];
    }
    else
    {
        [self didDeclineToAuthorizeRequestToken];
    }
    
    return NO;
}


#pragma mark - close
-(void) closeMe:(id) sender
{
    if(![UIDevice isPad])
    {
        [self.navigationController popViewControllerAnimated:NO];
    }
    else
    {
        [self dismissViewControllerAnimated:YES completion:nil];
    }

}
@end
