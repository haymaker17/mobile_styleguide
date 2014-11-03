//
//  ImagesScrollView.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 27/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTEHotelCellData.h"

@interface ImagesScrollView : UIView <UIScrollViewDelegate>
{
    float previousTouchPoint;
    UIPageControl *pageControl;
    UIScrollView * scrollview ;
    BOOL didEndAnimate;
}
@property (strong ,nonatomic) NSMutableArray *arrayOfDownloadableImages;
@property (nonatomic) NSInteger selectedIndex;
@property (nonatomic, strong) UIButton *closeButton;
@property (nonatomic, strong) CTEHotelCellData *hotelCellData;

@end
