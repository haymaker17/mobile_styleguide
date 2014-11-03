//
//  TestDriveTour1VC.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 1/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "TestDriveTour1VC.h"
#import "DataConstants.h"

@interface TestDriveTour1VC ()

@end

@implementation TestDriveTour1VC

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    if([ExSystem is5])
    {
        self.coTourImageHeight.constant = 220;
        self.coTourImageWidth.constant = 280;
        self.coImageToLabelsVerticalSpace.constant = 20;
        
    }
    self.coFormViewBottomVerticalSpace.constant = 0;
    
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)launchTestDrive:(id)sender {
    
    //Add flurry
    NSDictionary *dict = @{@"Result": @"Completed"};
    [Flurry logEvent:@"Test Drive:Tour" withParameters:dict];

    [Flurry endTimedEvent:@"Test Drive:Tour" withParameters:nil];
    
    if (self.onLaunchTestDriveTapped) {
        self.onLaunchTestDriveTapped();
    }
    ALog(@"launchTestDrive");
}
@end
