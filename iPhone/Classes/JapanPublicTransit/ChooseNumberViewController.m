//
//  ChooseNumberViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChooseNumberViewController.h"

@interface ChooseNumberViewController ()

@end

@implementation ChooseNumberViewController

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        // Empty.
    }
    
    return self;
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    // Bring up the keyboard.
    //
    [self.textField becomeFirstResponder];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // If iOS 7
    //
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
    
    // Set initial contents.
    //
    self.textField.text = self.contents;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:self.notificationName
                                                        object:self.textField.text];
}

@end
