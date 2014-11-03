//
//  ApproverTAViewController.m
//  ConcurMobile
//
//  Created by Wes Barton on 4/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ApproverTAViewController.h"
#import "ItineraryTableViewController.h"
#import "ItineraryAllowanceAdjustmentViewController.h"
#import "Itinerary.h"

@interface ApproverTAViewController ()

@end

@implementation ApproverTAViewController

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
    NSLog(@"ApproverTAViewController viewDidLoad");

//    self.tabBarController.delegate = self;
//    self.tabBarController.selectedIndex = 1;
    UISegmentedControl *sc = self.segmentedController;
    [sc setTitle:[Localizer getLocalizedText:@"Adjustments"] forSegmentAtIndex:0];
    [sc setTitle:[Localizer getLocalizedText:@"Itinerary"] forSegmentAtIndex:1];

    if(self.hasCloseButton)
    {
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionBack:)];
        self.navigationItem.leftBarButtonItem = btnClose;
    }

}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    NSLog(@"+++ segue.identifier = %@", segue.identifier);
    if([segue.identifier isEqualToString:@"tabEmeddedSegue"])
    {
        self.tabBarController = (UITabBarController *)segue.destinationViewController;
        self.tabBarController.delegate = self;
        self.tabBarController.selectedIndex = 0;
        self.tabBarController.tabBar.hidden = YES;
//        NSArray *viewControllers = [self viewControllers];
//        ItineraryTableViewController *controller = (ItineraryTableViewController *)[viewControllers objectAtIndex:2];

        NSArray *viewControllers = self.tabBarController.viewControllers;
        UINavigationController *c1 = (UINavigationController *)[viewControllers objectAtIndex:1];
        ItineraryTableViewController *x1 = (ItineraryTableViewController *)[c1 topViewController];
        x1.paramBag = self.paramBag;
        x1.role = self.role;

        UINavigationController *c2 = (UINavigationController *)[viewControllers objectAtIndex:0];
        ItineraryAllowanceAdjustmentViewController *x2 = (ItineraryAllowanceAdjustmentViewController *)[c2 topViewController];
        x2.rptKey = [Itinerary getRptKey:self.paramBag];
        x2.crnCode = self.paramBag[@"CrnCode"];
        x2.role = self.role;
        x2.hideGenerateExpenseButton = YES;

//        NSArray *a = [self.tabBarController viewControllers];
//        for (UIViewController *controller in a) {
//            NSLog(@"controller = %@", controller);
//        }
    }
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}

- (BOOL)tabBarController:(UITabBarController *)tabBarController shouldSelectViewController:(UIViewController *)viewController
{
    NSLog(@"~~~~shouldSelectViewController");
    return YES;
}

- (void)tabBarController:(UITabBarController *)tabBarController didSelectViewController:(UIViewController *)viewController
{
    NSLog(@"~~~viewController.didselect = %@", viewController);


    //Should be a navigationcontroller
    UINavigationController *c = (UINavigationController *)viewController;
    UIViewController *c2 = c.topViewController;
    NSLog(@"c2 = %@", c2);

}
- (IBAction)SegmentChanged:(id)sender {
    UISegmentedControl *segment = (UISegmentedControl *)sender;
    if(self.tabBarController != nil)
    {
        self.tabBarController.selectedIndex = segment.selectedSegmentIndex;
    }

}

-(void) actionBack:(id)sender
{
    if ([UIDevice isPad])
    {
        if ([self.navigationController.viewControllers count]>1)
            [self.navigationController popViewControllerAnimated:YES];
        else {
            [self dismissViewControllerAnimated:YES completion:nil];
        }
    }
    else
        [self.navigationController popViewControllerAnimated:YES];
}

- (void)viewWillLayoutSubviews
{
    [super viewWillLayoutSubviews];
    //TODO  Is this covered by the constraints now?
}


@end
