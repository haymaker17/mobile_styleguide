//
//  TravelRequestViewController.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 5/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TravelRequestViewController.h"

@interface TravelRequestViewController (Private)
-(void) fetchTravelRequest;
-(void) updateNavBarButtons;
-(void) displayBackButton;
-(void) displayCloseBtn;
-(void) displayRefreshBtn;
-(void) goBack;
-(void) pushToVisitedPageList:(NSString *)urlString;
-(BOOL) goToLastVisitedPageURL;
@end

@implementation TravelRequestViewController
@synthesize webView;
@synthesize backBtn;
@synthesize refreshBtn;
@synthesize currentPageURL;
@synthesize visitedPageList;
@synthesize currentIndex, viewTitle, altURL, isNOT_TR;

-(NSString *) getViewIDKey
{
    return TRAVEL_REQUESTS;
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
        [[NSURLCache sharedURLCache] removeAllCachedResponses];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad]; 
    
    if(isNOT_TR)
        self.title = self.viewTitle;
    else
	    self.title = [Localizer getLocalizedText:@"Requests"];
    
    [webView setDelegate:self];
    self.visitedPageList = [[NSMutableDictionary alloc] init];
    self.currentIndex = -1;
    
    if ([ExSystem connectedToNetwork])
    {
        // MOB-10970 Do not show loading view if it is offline
        [self showLoadingView];
        if(!isNOT_TR)
            [self fetchTravelRequest];
        else
            [self fetchAlt];
    }
    else
    {
        // MOB-11016
        [self.navigationItem setLeftBarButtonItem:nil];
        [self displayBackButton];
        [self.navigationItem setRightBarButtonItem:nil];

        [self showOfflineView:self];
    }
    
    if ([UIDevice isPad])
    {
        [self.navigationController setToolbarHidden:YES];
    
        [self.navigationItem setLeftBarButtonItem:nil];
        [self displayBackButton];
    }
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.webView = nil;
}

// MOB-9510
- (void)applicationDidEnterBackground
{
    [super applicationDidEnterBackground];
    [self.webView stopLoading];
}

#pragma mark Web View Delgate methods

- (BOOL)webView:(UIWebView *)wv shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    
//    NSLog(@"orig url %@", self.currentPageURL);
//    NSLog(@"abs url %@", request.URL.absoluteString);

    //  CRMC-55032
    //  Darin Warling (Eden Prairie, Invoice)
    //  Programmatically enable the ability of the user to zoom pages.
    //  This is important to ensure the user can zoom invoice images,
    //  which (as of this writing) are simply PDFs displayed in the
    //  browser. This setting doesn't seem to have any effect on the
    //  rest of the HTML5 UI, which is as intended:
    webView.scalesPageToFit = YES;

//    if (![self.currentPageURL isEqualToString:request.URL.absoluteString])
//    {
    self.currentPageURL = request.URL.absoluteString;
    
    if(self.currentPageURL != nil)
    {
        [self updateNavBarButtons];
        [self pushToVisitedPageList:currentPageURL];
    }
//    }
    return YES;
}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    if ([self isViewLoaded]) 
    {
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"Error" localize] message:[error localizedDescription] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] otherButtonTitles:nil];
        [alert show];
    }
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    [self hideLoadingView];
}


#pragma mark - Fetch pages

-(void) loadRequest:(NSString*) urlString
{
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
    
    [request setCachePolicy:NSURLRequestReturnCacheDataElseLoad];
//    [request setCachePolicy:NSURLRequestReloadIgnoringLocalCacheData];  // Does not guarantee reload
    NSURL *url = [NSURL URLWithString:urlString];
    [request setURL:url];
    [request setHTTPMethod:@"GET"];
    
    [self.webView loadRequest:request];
}

-(void) loadRequestAlt
{
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
    
    [request setCachePolicy:NSURLRequestReturnCacheDataElseLoad];
    [request setURL:self.altURL];
    [request setHTTPMethod:@"GET"];
    
    [self.webView loadRequest:request];
}

-(void) fetchAlt
{
    [self loadRequestAlt];
}

-(void) fetchTravelRequest
{
    NSString *uri = [ExSystem sharedInstance].entitySettings.uri;
    // is it still necessary to convert concursolutions to concurtech to use travel request in QA?
    uri = [uri lowercaseString];
    if ([uri hasPrefix:@"https://rqa"])
    {
        uri = [uri stringByReplacingOccurrencesOfString:@"concursolutions.com" withString:@"concurtech.net"];
    }
    NSString *currentLocale = [[NSLocale currentLocale] localeIdentifier];
    self.currentPageURL = [NSString stringWithFormat:@"%@/mobile/web/signin#mobile?sessionId=%@&pageId=travel-request-approvals-list-page&locale=%@",
                           uri,
                           [ExSystem sharedInstance].sessionID, 
                           currentLocale];
    
    [self loadRequest:self.currentPageURL];
}


#pragma mark - Navigation Buttons

-(BOOL) isRootPage
{
    NSString *currentURL = [webView stringByEvaluatingJavaScriptFromString:@"window.location.href"];

    NSRange range = [currentURL rangeOfString:@"#mobile?" 
                                               options:NSCaseInsensitiveSearch]; // Dummy page underlying the travel request lists page
    
    if(range.location != NSNotFound || [[currentURL lastPathComponent] isEqualToString:@"travel_request_approvals_list.html"]) 
        return TRUE;
    
    return FALSE;
}
-(void) updateNavBarButtons
{
    if([self isRootPage]) 
    {
        // Travel requests lists page
        [self.navigationItem setLeftBarButtonItem:nil];
        [self displayBackButton];
        [self.navigationItem setRightBarButtonItem:nil];

        // TODO - Display Refresh in V2
//        [self displayCloseBtn];
//        [self displayRefreshBtn];
    }
    else
    {
        [self.navigationItem setLeftBarButtonItem:nil];
        [self displayBackButton];
        [self.navigationItem setRightBarButtonItem:nil];
    }
}

-(void) goBack
{
// MOB-16801: when going back to the report list, had to click on the back button for many times. The cause of this problem is that we check isRootPage which is ...concurtech.net/mobile/web/signin#mobile?sessionId=ab24b1f6-afcf-446b-a8f7-4226ce71288e&pageId=travel-request-approvals-list-page&locale=en_US", but we don't need to go back to the sign in session
//  page then can close the modal view because there are some blank pages before it. Found out for Travel Request, the modal view can be closed at page showing all the request
//   approvals list(mobile/web/Travel/RequestApprovalsList), and for Invoice, it is the page show invoice list (mobile/web/inv).
//   purchase request (mobile/web/pr) last object in the purchase request is "pr" added the check for that also.
    
    NSString *currentURL = [webView stringByEvaluatingJavaScriptFromString:@"window.location.href"];
    
    NSArray  *paramList = [currentURL componentsSeparatedByString:@"/"];
    NSString *string = [paramList lastObject];
    if ([string caseInsensitiveCompare:@"requestapprovalslist"] == NSOrderedSame || [string caseInsensitiveCompare:@"inv"] == NSOrderedSame || [string caseInsensitiveCompare:@"pr"] == NSOrderedSame){
        [self dismiss:nil];
    }
    else if ([self isRootPage])  // probably can skip this, but don't know if we are only use this class for Invoice and Travel Request only, so keep it for now
    {
        [self dismiss:nil];
    }
    else if ([self.webView canGoBack])
        [self.webView goBack];
    else {
        [self dismiss:nil];
    }
}

-(void) refresh:(id)sender
{
    if (![webView isLoading])
    {

        [self showLoadingView];
        [[NSURLCache sharedURLCache] removeAllCachedResponses];

//        [webView reload]; // Does not work, the url in request is no longer valid - it loses certain state info and shows web view nav bar.
        // Reset view stack and reload original url 
        [visitedPageList removeAllObjects];
        self.currentIndex = -1;
        [self fetchTravelRequest];
    }
}

-(IBAction) dismiss:(id)sender
{
//    NSLog(@"Can go back %@", self.webView.canGoBack?@"Y":@"N");

    [[NSURLCache sharedURLCache] removeAllCachedResponses];
    [self dismissViewControllerAnimated:YES completion:nil];
}

-(void) displayCloseBtn
{
    UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(dismiss:)];
    [self.navigationItem setLeftBarButtonItem:btnClose];
}

-(void) displayRefreshBtn
{
    UIBarButtonItem *btnRefresh = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRefresh target:self action:@selector(refresh:)];
    [self.navigationItem setRightBarButtonItem:btnRefresh];
}

-(void) displayBackButton
{
    if ([ExSystem is7Plus])
        self.backBtn = [[UIBarButtonItem alloc] initWithTitle:[@"Back" localize] style:UIBarButtonItemStyleBordered target:self action:@selector(goBack)];
    else
        self.backBtn = [ExSystem makeBackButton:[@"Back" localize] target:self action:@selector(goBack)];
    
    [self.navigationItem setLeftBarButtonItem:backBtn];
}

-(BOOL) goToLastVisitedPageURL
{
    BOOL canGoBack = NO;
    
    if (currentIndex > 0)
    {
        currentIndex--;
        self.currentPageURL = visitedPageList[@(currentIndex)];
        canGoBack = YES;
    }
    
    return canGoBack;
}

-(void) pushToVisitedPageList:(NSString *)urlString
{
    currentIndex++;
    visitedPageList[@(currentIndex)] = urlString;
}
@end
