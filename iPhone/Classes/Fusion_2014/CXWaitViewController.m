//
//  CXWaitViewController.m
//  FusionLab
//
//  Created by Richard Puckett on 4/17/14.
//  Copyright (c) 2014 Creative Technologies Group. All rights reserved.
//

#import "CXWaitViewController.h"

@interface CXWaitViewController ()

@end

@implementation CXWaitViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        // Empty.
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self.distractor startAnimating];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    self.caption.text = self.captionText;
    self.caption.numberOfLines = 0;
    [self.caption sizeToFit];
    
}
- (IBAction)didDismiss:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}

@end
