//
//  MobileTourVC.h
//  ConcurMobile
//
//  Created by Sally Yan on 2/19/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MobileTourViewController : UIViewController <UIScrollViewDelegate>

@property (weak, nonatomic) IBOutlet UIPageControl *pageControl;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coContainerLeft_iPad;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coContainerBottom_iPad;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coContainerWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coContainerHeight;

@property (copy, nonatomic) void(^onDismissTapped)(void);

// MOB-17649 need to fix rotation
// Breaking some serious encapsulation to rotate the home...
@property (nonatomic, readwrite, strong) HomeLoaderVC *home;
- (void)didRotate:(NSNotification *)notification;

@end
