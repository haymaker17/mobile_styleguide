//
//  TestDriveTour2_iPadVC.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 1/3/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "TestDriveTour2_iPadVC.h"

@interface TestDriveTour2_iPadVC ()

@end

@implementation TestDriveTour2_iPadVC

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    [self willAnimateRotationToInterfaceOrientation:[UIApplication sharedApplication].statusBarOrientation duration:0];
    [self.navigationController setNavigationBarHidden:NO animated:NO];
    self.title = @"Test Drive";
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Skip" style:UIBarButtonItemStylePlain target:self action:@selector(launchTestDriveTapped:)]; ;

}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self willAnimateRotationToInterfaceOrientation:[UIApplication sharedApplication].statusBarOrientation duration:0];
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration{
    
    if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
        self.coLogoAreaTop.constant = 90;
        self.coLogoAreaLea.constant = 60;
        self.coLogoAreaWidth.constant = 450;
        self.coFormAreaTop.constant = 80;
        self.coFormAreaLea.constant = 550;
        self.coFormAreaWidth.constant = 450;
        self.coNextButtonVerticalSpace.constant = 215;
    } else if (UIInterfaceOrientationIsPortrait(toInterfaceOrientation)) {
        self.coLogoAreaTop.constant = 40;
        self.coLogoAreaLea.constant = 155;
        self.coLogoAreaWidth.constant = 450;
        self.coFormAreaTop.constant = 420;
        self.coFormAreaLea.constant = 153;
        self.coFormAreaWidth.constant = 450;
        self.coNextButtonVerticalSpace.constant = 120;
    }
    [self.view setNeedsUpdateConstraints];
    [UIView animateWithDuration:duration animations:^{
        [self.view layoutIfNeeded];
    }];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)launchTestDriveTapped:(id)sender {
    
    NSDictionary *dict = @{@"Result": @"Completed"};
    [Flurry logEvent:@"Test Drive:Tour" withParameters:dict];

    if (self.onSkipTapped) {
        self.onSkipTapped();
    }
    NSLog(@"Launch Test drive Tapped");

}
@end
