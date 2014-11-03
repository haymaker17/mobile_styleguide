//
//  TestViewController.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 19/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "UberIntroductionVC.h"
#import "UberRequest.h"

@interface UberIntroductionVC ()

@end

@implementation UberIntroductionVC

- (id)init
{
    self = [super initWithNibName:@"UberIntroductionVC" bundle:nil];
    if (self) {
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    [self.scrollView addSubview:self.contentView];
    [self.scrollView setContentSize:CGSizeMake(self.contentView.frame.size.width,self.contentView.frame.size.height)];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(IBAction)didPressUberButton:(id)sender
{
//    NSDictionary *dict = @{@"Type": @"Uber"};
//    [Flurry logEvent:@"External App: Launch" withParameters:dict];
//    
//    UberRequest *launchUber = [[UberRequest alloc] init];
//    [launchUber requestCar];
}

@end
