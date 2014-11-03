//
//  SUPERHOMEViewController.m
//  ConcurMobile
//
//  Created by Shifan Wu on 11/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "SUPERHOMEViewController.h"
#import "TabBarViewController.h"
#import "Home9VC.h"

@interface SUPERHOMEViewController ()
@property (nonatomic,strong) Home9VC                 *Home9VC;

@end

@implementation SUPERHOMEViewController

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
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender{
    if ([segue.identifier isEqualToString:@"LOADHOMETABLE"]) {
        self.Home9VC = segue.destinationViewController;
    }
    else if ([segue.identifier isEqualToString:@"LOADTABBAR"]) {
        TabBarViewController *ctrl = segue.destinationViewController;
        [ctrl setSelectOption:^(NSDictionary *option) {
            if ([[option objectForKey:@"Expense"] isEqualToString:@"YES"]) {
                [self.Home9VC buttonQuickExpensePressed:self];
            }
            else if ([[option objectForKey:@"Receipt"] isEqualToString:@"YES"]){
                [self.Home9VC cameraPressed:self];
            }
            else if ([[option objectForKey:@"Book"] isEqualToString:@"YES"]){
                [self.Home9VC bookingsActionPressed:self];
            }
            else if ([[option objectForKey:@"Mileage"] isEqualToString:@"YES"]){
                [self.Home9VC btnMileagePressed:self];
            }
        }];
    }
}

@end
