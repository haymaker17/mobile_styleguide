//
//  TestDriveTour1VC.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 1/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TestDriveTour1VC : UIViewController

- (IBAction)launchTestDrive:(id)sender;
@property NSUInteger pageIndex;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coTourImageWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coTourImageHeight;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coImageToLabelsVerticalSpace;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormViewBottomVerticalSpace;

@property (nonatomic, copy) void (^onLaunchTestDriveTapped)(void);

@end
