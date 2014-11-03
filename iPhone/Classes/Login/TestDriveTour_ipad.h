//
//  TestDriveTour_ipad.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TestDriveTour_ipad : UIViewController

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coNextButtonVerticalSpace;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coNextButtonCenterXAlign;

@property (nonatomic, copy) void (^onSkipTapped)(void);

@end
