//
//  AbstractExpenseViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/13/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AbstractExpenseViewController.h"
#import "AnalyticsManager.h"
#import "FirstViewController.h"
#import "JPTUtils.h"
#import "Localizer.h"
#import "ReportDetailViewController.h"
#import "RouteManager.h"

@interface AbstractExpenseViewController ()

@end

@implementation AbstractExpenseViewController

- (void)didMoveToParentViewController:(UIViewController *)parent {
    if (parent == nil) {
        [[NSNotificationCenter defaultCenter] removeObserver:self];
    }
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWillAppear:)
                                                 name:UIKeyboardWillShowNotification
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWillHide:)
                                                 name:UIKeyboardWillHideNotification
                                               object:nil];
}

-(void)keyboardWillAppear:(NSNotification *)notification {
    // Override.
}

-(void)keyboardWillHide:(NSNotification *)notification {
    // Override.
}

@end
