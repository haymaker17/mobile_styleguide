//
//  TestDriveTourPageViewController_iPhone.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 12/31/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TestDriveTourPageViewController_iPhone.h"
#import "TestDriveTour1VC.h"

@interface TestDriveTourPageViewController_iPhone ()

@property (strong, nonatomic) UIBarButtonItem *skipBarBtn;
@property (strong, nonatomic) NSDate *startTime;

@end

@implementation TestDriveTourPageViewController_iPhone

- (void)viewDidLoad
{
    [self.navigationController setNavigationBarHidden:NO animated:NO];
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.pageViewController = [self.storyboard instantiateViewControllerWithIdentifier:@"PageViewController"];
    self.pageViewController.delegate = self;
    self.pageViewController.dataSource = self;
    
    UIViewController *startingViewController = [self viewControllerAtIndex:0];
    NSArray *viewControllers = @[startingViewController];
    [self.pageViewController setViewControllers:viewControllers direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
    // Change the size of page view controller
    self.pageViewController.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height - 1);

    [self.view setBackgroundColor:[UIColor concurBlueColor]];
    [self.navigationItem setHidesBackButton:YES animated:NO];
    
    [self addChildViewController:self.pageViewController];
    [self.view addSubview:self.pageViewController.view];
    [self.pageViewController didMoveToParentViewController:self];
    
    self.skipBarBtn = [[UIBarButtonItem alloc] initWithTitle:@"Skip" style:UIBarButtonItemStylePlain target:self action:@selector(closeStoryBoard)];
    
}


-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    self.title = @"Test Drive";
    [self.navigationController setNavigationBarHidden:NO animated:NO];
    self.navigationItem.rightBarButtonItem = self.skipBarBtn ;
    [Flurry logEvent:@"Test Drive:Tour" timed:YES];
    self.startTime = [NSDate date];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:NO];
    //Add flurry
    NSInteger timeInSeconds =  -round( [self.startTime timeIntervalSinceNow]);
    NSDictionary *dict = @{@"Seconds on Tour":[NSString stringWithFormat:@"%d" ,timeInSeconds]};
    [Flurry logEvent:@"Test Drive:Tour" withParameters:dict];
    // End timer
    [Flurry endTimedEvent:@"Test Drive:Tour" withParameters:nil];

}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (TestDriveTour1VC *)viewControllerAtIndex:(NSUInteger)index
{
    if (index > 2) {
        return nil;
    }
    TestDriveTour1VC *viewController = nil;
    if (index==0) {
        viewController =  [self.storyboard instantiateViewControllerWithIdentifier:@"TestDriveTour1"];
        viewController.pageIndex = 0;
    }
    else
    {
        viewController = [self.storyboard instantiateViewControllerWithIdentifier:@"TestDriveTour2"];
        viewController.pageIndex = 1;
        [viewController setOnLaunchTestDriveTapped:^{
            [self closeStoryBoard];
        }];

    }
    return viewController;
}

-(NSUInteger)getIndex:(UIViewController *)viewController
{
    NSUInteger index = NSNotFound;
    
    if([viewController.restorationIdentifier isEqualToString:@"TestDriveTour1"] )
        index = 0;
    if ([viewController.restorationIdentifier isEqualToString:@"TestDriveTour2"]) {
        index = 1;
    }
    return index;
}

#pragma mark - Page View Controller Data Source

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerBeforeViewController:(UIViewController *)viewController
{
    
   NSUInteger index = ((TestDriveTour1VC*) viewController).pageIndex;
    
    if ((index == 0) || (index == NSNotFound)) {
        return nil;
    }
    
    index--;
    return [self viewControllerAtIndex:index];
}

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerAfterViewController:(UIViewController *)viewController
{
     NSUInteger index = ((TestDriveTour1VC*) viewController).pageIndex;
    
    if (index == NSNotFound) {
        return nil;
    }
    
    index++;
    if (index > 1 ) {
        return nil;
    }
    return [self viewControllerAtIndex:index];
}

- (NSInteger)presentationCountForPageViewController:(UIPageViewController *)pageViewController
{
    // Two tour screens as of now
    return 2;
}

- (NSInteger)presentationIndexForPageViewController:(UIPageViewController *)pageViewController
{
    return 0;
}

#pragma mark - Page View Controller Delegate


- (void)pageViewController:(UIPageViewController *)pageViewController didFinishAnimating:(BOOL)finished previousViewControllers:(NSArray *)previousViewControllers transitionCompleted:(BOOL)completed
{
    if(completed && [ExSystem is7Plus])
    {
        NSString *screenid = ((UIViewController *)previousViewControllers[0]).restorationIdentifier;
         //Show/Hide the button
        if ([screenid isEqualToString:@"TestDriveTour2"]) {
            self.skipBarBtn.title = @"Skip";
            self.skipBarBtn.enabled = YES;
        }
        else {
            self.skipBarBtn.enabled = NO;
            self.skipBarBtn.title = @"";
        }
    }

}

-(void)closeStoryBoard
{
    //Add flurry
    NSDictionary *dict = @{@"Result": @"Skipped"};
    [Flurry logEvent:@"Test Drive:Tour" withParameters:dict];
    
    // Close this storyboard view
    if (self.onSkipTapped) {
        self.onSkipTapped();
    }
}

@end
