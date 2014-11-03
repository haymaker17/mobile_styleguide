//
//  WebViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/11/09.
//  Copyright 2009 Concur. All rights reserved.
//
//  UPDATED BY: Shifan Wu on 10/01/2012
//
//  Add more functions to support webview, include:
//      --offline Mode
//      --refresh function
//      --app into background
//      --successfuly/Loading error
//      --navigation support


#import "WebViewController.h"
#import "ExSystem.h" 



@implementation WebViewController

@synthesize webView;
@synthesize rootViewController;
@synthesize url;
@synthesize labelTitle;
@synthesize viewTitle;
@synthesize activityIndicator;
@synthesize myToolBar;
@synthesize closeBtn;

#define RGB(r, g, b) [UIColor colorWithRed:r/255.0 green:g/255.0 blue:b/255.0 alpha:1]
#define RGBA(r, g, b, a) [UIColor colorWithRed:r/255.0 green:g/255.0 blue:b/255.0 alpha:a]

-(IBAction)closeMe:(id)sender
{
	if([UIDevice isPad])
		[self dismissViewControllerAnimated:YES completion:NULL];
	else 
		[self dismissViewControllerAnimated:YES completion:NULL];
}

// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    //return (interfaceOrientation == UIInterfaceOrientationPortrait);
	return YES;
}

/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
        // Custom initialization
    }
    return self;
}
*/

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
}
*/

- (void)viewDidLayoutSubviews
{
    if ([self respondsToSelector:@selector(topLayoutGuide)])
    {
        CGRect viewBounds = self.view.bounds;
        CGFloat topBarOffset = self.topLayoutGuide.length;
        
        [self.view setFrame:CGRectMake(viewBounds.origin.x, topBarOffset, viewBounds.size.width, viewBounds.size.height-topBarOffset)];
    }
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
	labelTitle.text = viewTitle;
  	webView.delegate = self;
    
	NSURL *thisUrl = [NSURL URLWithString:url];
	//NSMutableURLRequest *requestObj = [NSURLRequest requestWithURL:thisUrl];
    NSMutableURLRequest *requestObj = [[NSMutableURLRequest alloc] init];
    [requestObj setCachePolicy:NSURLRequestReturnCacheDataElseLoad];
    [requestObj setURL:thisUrl];
    [requestObj setHTTPMethod:@"GET"];
    
	[webView loadRequest:requestObj];
    
    [self.closeBtn setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys: [UIColor concurBlueColor], UITextAttributeTextColor,nil] forState:UIControlStateNormal];
    self.closeBtn.title = [Localizer getLocalizedText:@"Close"];

    
    if ([ExSystem is7Plus])
    {
        [self.labelTitle setTextColor:[UIColor blackColor]];
        [self.labelTitle setShadowColor:[UIColor clearColor]];
    }
}

/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
*/

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
    [super viewDidUnload];
    
    self.webView = nil;
}


- (void)dealloc 
{
	webView.delegate = nil;
}

#pragma mark -
#pragma mark webview items
- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
	[activityIndicator stopAnimating];
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
	[activityIndicator stopAnimating];
}

- (void)webViewDidStartLoad:(UIWebView *)webView
{
	[activityIndicator setHidesWhenStopped:YES];
	[activityIndicator startAnimating];
}

@end
