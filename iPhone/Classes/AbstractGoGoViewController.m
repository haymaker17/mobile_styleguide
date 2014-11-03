//
//  AbstractGoGoViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 12/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AbstractGoGoViewController.h"
#import "WebViewController.h"

@implementation AbstractGoGoViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // If iOS 7
    //
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
}

- (IBAction)didSelectTermsOfService:(id)sender {
    [self presentModalView:@"http://www.gogoair.com/gogo/cms/service_ext.do"];
}

- (IBAction)didSelectPrivacyPolicy:(id)sender {
    [self presentModalView:@"http://www.gogoair.com/gogo/cms/privacy.do"];
}

- (IBAction)didSelectCustomerSupport:(id)sender {
    [self presentModalView:@"https://custhelp.gogoinflight.com/"];
}

- (void)presentModalView:(NSString *)url {
    WebViewController *vc = [[WebViewController alloc] init];
    
    vc.url = url;
    
    [self.navigationController presentViewController:vc animated:YES completion:nil];
}

@end
