//
//  YodleeCardAgreementVC.h
//  ConcurMobile
//
//  Created by yiwen on 11/28/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "MobileViewController.h"

@interface YodleeCardAgreementVC : MobileViewController<UIWebViewDelegate>
{
    UIWebView *webView;
}

@property (strong, nonatomic) IBOutlet UIWebView *webView;

@end
