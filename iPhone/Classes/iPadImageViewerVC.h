//
//  iPadImageViewerVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 9/27/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ImageUtil.h"
#import "MobileViewController.h"
#import	"TouchIV.h"

@interface iPadImageViewerVC : MobileViewController <UIScrollViewDelegate>{
	UIScrollView			*scroller;
	UIToolbar				*tb;
	NSMutableArray			*imageArray;
    UIPageControl           *pageControl;
	BOOL                    pageControlIsChangingPage;
	int						pagePos;
	CGFloat					initialDistance;
	BOOL					isThumbs;
}

@property (strong, nonatomic) IBOutlet UIScrollView				*scroller;
@property (strong, nonatomic) IBOutlet UIToolbar				*tb;
@property (strong, nonatomic) NSMutableArray                    *imageArray;
@property (nonatomic, strong) IBOutlet UIPageControl            *pageControl;
@property int pagePos;

-(IBAction) closeMe:(id)sender;
-(void) manipulateImages;

/* for pageControl */
- (IBAction)changePage:(id)sender;

/* internal */
- (void)setupPage;

-(UIImage *)scaleImageToFit:(UIImage *) img MaxW:(float)maxW MaxH:(float)maxH;
-(IBAction) switchToSingleView:(id)sender;
-(IBAction) switchToGridView:(id)sender;
-(void) makeToolBarSingle;
-(void) makeToolBarGrid;

-(void)loadHotelImages:(NSMutableArray *)aImageURLs;
-(void) manipulateImagesOG;
@end
