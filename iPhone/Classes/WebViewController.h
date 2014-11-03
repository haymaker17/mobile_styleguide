//
//  WebViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/11/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ExSystem.h" 


@interface WebViewController : MobileViewController <UIWebViewDelegate>
{
	UIWebView			*webView;
	RootViewController	*rootViewController;
	NSString			*url, *viewTitle;
	UILabel				*labelTitle;
	UIActivityIndicatorView	*activityIndicator;
	UIToolbar			*myToolBar;
    UIBarButtonItem     *closeBtn;
}

@property (strong, nonatomic) IBOutlet UIWebView *webView;
@property (strong, nonatomic) RootViewController *rootViewController;
@property (strong, nonatomic) NSString *url;
@property (strong, nonatomic) NSString *viewTitle;
@property (nonatomic, strong) IBOutlet UILabel *labelTitle;
@property (nonatomic, strong) IBOutlet UIActivityIndicatorView	*activityIndicator;
@property (nonatomic, strong) IBOutlet UIToolbar *myToolBar;
@property (nonatomic, strong) IBOutlet UIBarButtonItem *closeBtn;

-(IBAction)closeMe:(id)sender;

@end
