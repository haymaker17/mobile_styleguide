//
//  MobileTourCollectionVC.h
//  ConcurMobile
//
//  Created by Sally Yan on 2/19/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MobileTourCollectionViewController : UICollectionViewController

@property (copy,nonatomic) void(^onPageDidChange)(NSInteger newPageIndex);
@property NSInteger currentPageIndex;

@property (readonly) NSInteger totalNumberOfPages;

@end
