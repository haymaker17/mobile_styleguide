//
//  TestDriveTour_ipad.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TestDriveTour_ipad.h"
#import "TestDriveTour2_iPadVC.h"

@interface TestDriveTour_ipad ()

@property (strong, nonatomic) NSDate *startTime;

@end

@implementation TestDriveTour_ipad

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
	// Do any additional setup after loading the view.
    [self willAnimateRotationToInterfaceOrientation:[UIApplication sharedApplication].statusBarOrientation duration:0];
    [self.navigationController setNavigationBarHidden:NO animated:NO];
    [self.navigationItem setHidesBackButton:YES animated:NO];
    // By default the @selector(closestoryboard:) sends YES as parameter as (sender is an object)
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Skip" style:UIBarButtonItemStylePlain target:self action:@selector(closeStoryBoard:)]; ;
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    self.title = @"Test Drive";
    
    [self willAnimateRotationToInterfaceOrientation:[UIApplication sharedApplication].statusBarOrientation duration:0];
    // Added Flurry Timed Event
    self.startTime = [NSDate date];
    [Flurry logEvent:@"Test Drive:Tour" timed:YES];

}


- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration{
    
    if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
        self.coLogoAreaTop.constant = 100;
        self.coLogoAreaLea.constant = 60;
        self.coLogoAreaWidth.constant = 450;
        self.coFormAreaTop.constant = 75;
        self.coFormAreaLea.constant = 560;
        self.coFormAreaWidth.constant = 400;
        self.coNextButtonVerticalSpace.constant = 180;
    } else if (UIInterfaceOrientationIsPortrait(toInterfaceOrientation)) {
        self.coLogoAreaTop.constant = 40;
        self.coLogoAreaLea.constant = 190;
        self.coLogoAreaWidth.constant = 400;
        self.coFormAreaTop.constant = 420;
        self.coFormAreaLea.constant = 200;
        self.coFormAreaWidth.constant = 400;
        self.coNextButtonVerticalSpace.constant = 80;
    }
    [self.view setNeedsUpdateConstraints];
    [UIView animateWithDuration:duration animations:^{
        [self.view layoutIfNeeded];
    }];
}

-(void)closeStoryBoard:(BOOL)isTour1
{
    if(isTour1)
    {
        NSDictionary *dict = @{@"Result": @"Skipped"};
        [Flurry logEvent:@"Test Drive:Tour" withParameters:dict];
    }
    //Add flurry
    NSInteger timeInSeconds =  -round( [self.startTime timeIntervalSinceNow]);
    NSDictionary *dict = @{@"Seconds on Tour":[NSString stringWithFormat:@"%ld" ,(long)timeInSeconds]};
    [Flurry logEvent:@"Test Drive:Tour" withParameters:dict];
    // End timer
    [Flurry endTimedEvent:@"Test Drive:Tour" withParameters:nil];

    // Close this storyboard view
    if (self.onSkipTapped) {
        self.onSkipTapped();
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Navigation

// In a story board-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    
    if ([segue.identifier isEqualToString:@"TestDriveTour2"]) {
        TestDriveTour2_iPadVC *ctrl = segue.destinationViewController;
        [ctrl setOnSkipTapped:^{
            [self closeStoryBoard:NO];
        }];

    }
    self.title = @"Back";
}

@end
