//
//  ChooseDateViewController.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/21/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChooseDateViewController.h"
#import "Localizer.h"

@interface ChooseDateViewController ()

@end

@implementation ChooseDateViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // If iOS 7
    //
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
    
    UIBarButtonItem *rightButton = [[UIBarButtonItem alloc]
                                   initWithTitle:[Localizer getLocalizedText:@"today"]
                                   style:UIBarButtonItemStylePlain
                                   target:self
                                   action:@selector(onRightBarButtonTapped:)];
    
    [self.navigationItem setRightBarButtonItem:rightButton];
    
    self.datePicker.date = self.date;
}

- (IBAction)dateChanged:(id)sender {
    NSDate *date = [self.datePicker date];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:@"TripDate" object:date];
}

- (void)onRightBarButtonTapped:(id)sender {
    NSDate *now = [NSDate date];
    
    [self.datePicker setDate:now];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:@"TripDate" object:now];
}

@end
