//
//  ChooseTextViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/12/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChooseTextViewController.h"

@interface ChooseTextViewController ()

@end

@implementation ChooseTextViewController

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
    [self.textView becomeFirstResponder];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // If iOS 7
    //
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }

    // Add grey border. Looks more consistent on iOS6.
    //
    self.textView.layer.cornerRadius = 5;
    [self.textView.layer setBorderColor:[[[UIColor grayColor] colorWithAlphaComponent:0.5] CGColor]];
    [self.textView.layer setBorderWidth:1.0];
    self.textView.clipsToBounds = YES;
    
    // Set initial contents.
    //
    self.textView.text = self.contents;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:self.notificationName
                                                        object:self.textView.text];
}

@end
