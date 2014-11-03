//
//  AllowanceAmountViewController.m
//  ConcurMobile
//
//  Created by Wes Barton on 3/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AllowanceAmountViewController.h"

@interface AllowanceAmountViewController ()

@end

@implementation AllowanceAmountViewController

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

//    NSLog(@"self.amount = %@", self.amount);
//    NSLog(@"self.amountField.text = %@", self.amountField.text);

    self.amountField.text = self.amount;


}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    // Make the amount field start editing
    [self.amountField becomeFirstResponder];
}


- (void)viewWillDisappear:(BOOL)animated {
    // Need to do this because the default back button cant be attached to exit from IB
    // You connect the viewcontroller to the exit to create the segue, then call it from code.
    [self performSegueWithIdentifier:@"returnAmount" sender:self];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)changeAmountField:(id)sender forEvent:(UIEvent *)event {
    UITextField *f = (UITextField *)sender;
    self.amount = f.text;
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/


@end
