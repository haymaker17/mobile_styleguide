//
//  AbstractGoGoViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 12/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "AbstractIpmViewController.h"

@interface AbstractGoGoViewController : AbstractIpmViewController

- (IBAction)didSelectTermsOfService:(id)sender;
- (IBAction)didSelectPrivacyPolicy:(id)sender;
- (IBAction)didSelectCustomerSupport:(id)sender;

@end
