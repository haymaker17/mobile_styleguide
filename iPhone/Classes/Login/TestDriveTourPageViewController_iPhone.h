//
//  TestDriveTourPageViewController_iPhone.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 12/31/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TestDriveTourPageViewController_iPhone : UIViewController <UIPageViewControllerDataSource, UIPageViewControllerDelegate>

@property (nonatomic, weak) UIPageViewController *pageViewController;

@property (nonatomic, copy) void (^onSkipTapped)(void);


@end
