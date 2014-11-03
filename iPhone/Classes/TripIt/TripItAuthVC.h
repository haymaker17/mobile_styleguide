//
//  TripItAuthVC.h
//  ConcurMobile
//
//  Created by  on 3/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "iPadHomeVC.h"

@interface TripItAuthVC : MobileViewController<UIWebViewDelegate>
{
    UIWebView   *webView;
    NSString    *requestTokenKey;
    NSString    *requestTokenSecret;
}

@property (nonatomic, strong) IBOutlet UIWebView    *webView;
@property (nonatomic, strong) NSString              *requestTokenKey;
@property (nonatomic, strong) NSString              *requestTokenSecret;

#pragma mark - close
-(void) closeMe:(id) sender;
@end
