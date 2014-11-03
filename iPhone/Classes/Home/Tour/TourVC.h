//
//  TourVC.h
//  ConcurMobile
//
//  Created by Shifan Wu on 5/6/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MobileViewController.h"

@interface TourVC : MobileViewController <UIScrollViewDelegate>

@property (nonatomic, strong) NSArray *contentList;

@property (nonatomic, strong) IBOutlet UIScrollView *scrollView;
@property (nonatomic, strong) IBOutlet UIPageControl *pageControl;
@property (nonatomic, strong) NSMutableArray *viewControllers;

- (IBAction)changePage:(id)sender;

@end
