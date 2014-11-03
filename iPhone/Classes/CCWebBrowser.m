//
//  CCWebBrowser.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 1/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CCWebBrowser.h"

@interface CCWebBrowser ()
@property (weak, nonatomic) IBOutlet UIWebView *webView;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *spinner;

@end

@implementation CCWebBrowser

- (instancetype)initWithTitle:(NSString*)title{
    CCWebBrowser *ctrl = [[UIStoryboard storyboardWithName:@"CCWebBrower" bundle:nil] instantiateInitialViewController];
    ctrl.navigationItem.title = title;
    return ctrl;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    self.webView.delegate = self;
    self.webView.scalesPageToFit = YES;
    NSMutableURLRequest *requestObj = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:self.URLString] cachePolicy:NSURLRequestReturnCacheDataElseLoad timeoutInterval:180];
    [self.webView loadRequest:requestObj];
}
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)dealloc{
    self.webView.delegate = nil;
    [self.webView stopLoading];
}
#pragma mark - UIWebViewDelegate

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    NSLog(@"didFailLoadWithError %@",[error localizedDescription]);
	[self.spinner stopAnimating];
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    NSLog(@"webViewDidFinishLoad");
	[self.spinner stopAnimating];
}

- (void)webViewDidStartLoad:(UIWebView *)webView
{
    NSLog(@"webViewDidStartLoad");
	[self.spinner setHidesWhenStopped:YES];
	[self.spinner startAnimating];
}


@end
