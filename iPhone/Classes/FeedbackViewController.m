//
//  FeedbackViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 12/2/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ATConnect.h"
#import "FeedbackManager.h"
#import "FeedbackViewController.h"

@implementation FeedbackViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        // Custom initialization
    }
    
    return self;
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    ATConnect *connection = [FeedbackManager sharedInstance].connection;
    
    [connection presentMessageCenterFromViewController:self];
}

@end
