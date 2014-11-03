//
//  UIImageScrollView.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MobileViewController;

@interface UIImageScrollView : UIScrollView <UIScrollViewDelegate>
{
	int					originalWidth;
	int					originalHeight;
	BOOL				pageControlIsChangingPage;
	UIPageControl		*pageControl;
	NSMutableArray		*imageArray;
	UIViewController	*__weak parentVC;
}

@property (nonatomic) int								originalWidth;
@property (nonatomic) int								originalHeight;
@property (nonatomic) BOOL								pageControlIsChangingPage;
@property (nonatomic, strong) IBOutlet UIPageControl	*pageControl;
@property (nonatomic, strong) NSMutableArray			*imageArray;
@property (nonatomic, weak) UIViewController			*parentVC;

-(void)configureWithImagePairs:(NSArray *)propertyImagePairs owner:(MobileViewController*)owner;
-(void)configureWithImageUrls:(NSArray *)imageUrls owner:(MobileViewController*)owner;

/* for pageControl */
- (IBAction)changePage:(id)sender;

@end
