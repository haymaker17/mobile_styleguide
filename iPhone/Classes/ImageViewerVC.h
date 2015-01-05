//
//  ImageViewerVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ImageUtil.h"
#import "MobileViewController.h"

@interface ImageViewerVC : MobileViewController <UIScrollViewDelegate>{
	UIScrollView			*scroller;
	UIToolbar				*tb;
	NSMutableArray			*imageArray;
	IBOutlet UIPageControl* pageControl;
	BOOL pageControlIsChangingPage;
	NSInteger						pagePos;
}

@property (strong, nonatomic) IBOutlet UIScrollView				*scroller;
@property (strong, nonatomic) IBOutlet UIToolbar				*tb;
@property (strong, nonatomic) IBOutlet NSMutableArray			*imageArray;
@property (nonatomic, strong) UIPageControl* pageControl;
@property NSInteger pagePos;

-(IBAction) closeMe;
-(void) manipulateImages;

/* for pageControl */
- (IBAction)changePage:(id)sender;

/* internal */
- (void)setupPage;

-(UIImage *)scaleImageToFit:(UIImage *) img MaxW:(float)maxW MaxH:(float)maxH;
@end
