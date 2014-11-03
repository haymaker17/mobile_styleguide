//
//  TestDriveTour2_iPadVC.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 1/3/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TestDriveTour2_iPadVC : UIViewController

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coNextButtonVerticalSpace;
@property (nonatomic, copy) void (^onSkipTapped)(void);
- (IBAction)launchTestDriveTapped:(id)sender;

@end
