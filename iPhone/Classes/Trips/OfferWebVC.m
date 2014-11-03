//
//  OfferWebVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/26/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "OfferWebVC.h"

@implementation OfferWebVC
@synthesize webView, activityIndicator, url;

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
    if([UIDevice isPad])
    {
        self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
        self.navigationItem.leftBarButtonItem =  [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(doneAction)];
        self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
    }
    
	NSURL *thisUrl = [NSURL URLWithString:url];
	NSURLRequest *requestObj = [NSURLRequest requestWithURL:thisUrl];
	webView.delegate = self;
    [webView loadRequest:requestObj];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.webView = nil;
    self.activityIndicator = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    if ([UIDevice isPad]) {
        return YES;
    }
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark actions
-(void)doneAction
{
	[self dismissViewControllerAnimated:YES completion:nil];
}

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
