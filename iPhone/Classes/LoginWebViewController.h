//
//  LoginWebViewController.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/28/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RequestController.h"
#import "LoginViewController.h"

@interface LoginWebViewController : MobileViewController <UIWebViewDelegate,ExMsgRespondDelegate, UIAlertViewDelegate>{
    UIWebView *webView;
    NSString *loginUrl;
    NSString *sessionId;
    id <LoginDelegate> __weak loginDelegate;
}

@property (nonatomic, weak) id <LoginDelegate> loginDelegate;
@property (nonatomic, strong) NSString *sessionId;
@property (nonatomic, strong) UIWebView *webView;
@property (nonatomic, strong) NSString *loginUrl;
@end
