//
//  UIImageScrollView.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "UIImageScrollView.h"
#import "HotelImageData.h"
#import "MobileViewController.h"
#import "ExSystem.h" 

#import "ImageViewerVC.h"

@implementation UIImageScrollView


@synthesize originalWidth;
@synthesize originalHeight;
@synthesize pageControlIsChangingPage;
@synthesize pageControl;
@synthesize imageArray;
@synthesize parentVC;

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
	UITouch *touch = [[event allTouches] anyObject];
	if ([touch tapCount] == 2)
	{
		ImageViewerVC *vc = [[ImageViewerVC alloc] init];
		vc.imageArray = imageArray;
		vc.pagePos = pageControl.currentPage;
		[parentVC presentViewController:vc animated:YES completion:nil];
		[self.nextResponder touchesBegan:touches withEvent:event];
	}
}


- (id)initWithFrame:(CGRect)frame {
    if ((self = [super initWithFrame:frame])) {
        // Initialization code
    }
    return self;
}

-(void)configureWithImagePairs:(NSArray *)propertyImagePairs owner:(MobileViewController*)owner
{
	if (propertyImagePairs == nil)
		return;
	
	int numPropertyImagePairs = [propertyImagePairs count];

	NSMutableArray *urls = [[NSMutableArray alloc] initWithCapacity:numPropertyImagePairs];
	NSMutableDictionary *urlsAlreadySeen = [[NSMutableDictionary alloc] initWithCapacity:numPropertyImagePairs];

	for(int i = 0; i < numPropertyImagePairs; i++)
	{
		HotelImageData *hid = propertyImagePairs[i];
		NSString *url = hid.hotelImage;

		if(!urlsAlreadySeen[url])
		{
			urlsAlreadySeen[url] = @"YES";
			[urls addObject:url];
		}
	}
	
	[self configureWithImageUrls:urls owner:owner];
	
}

-(void)configureWithImageUrls:(NSArray *)imageUrls owner:(MobileViewController*)owner
{
	while ([self.subviews count] > 0)
		[(self.subviews)[0] removeFromSuperview];
		
	if (originalWidth == 0)
		self.originalWidth = self.frame.size.width;
	
	if (originalHeight == 0)
		self.originalHeight = self.frame.size.height;
	
	self.delegate = self;	// This object is its own UIScrollViewDelegate

	self.parentVC = owner;
	
	int imageCount = [imageUrls count];
	
	if (imageCount == 0)
	{
		// TODO: handle case where there are no images to show.  Show 'no image available' message?
		return;
	}

	pageControl.numberOfPages = imageCount;
	pageControl.currentPage = 0;
	self.pageControlIsChangingPage = NO;

	// The size of the UIScrollView (scroller) for this cell is hard-coded here.
	float w = originalWidth;
	float h = originalHeight;
	
	self.contentSize = CGSizeMake(imageCount * w, h);
	self.pagingEnabled = YES;
	self.bounces = YES;
	self.indicatorStyle = UIScrollViewIndicatorStyleBlack;
	int iPos = 0;
	
	NSMutableArray *ivArray = [[NSMutableArray alloc] initWithObjects:nil];
	for (NSString *imageUrl in imageUrls)
	{
		UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(iPos * w, 0, w, h)];
		[ivArray addObject:iv];
		
		//do things to load the actual image
		UIImage *img = [UIImage imageNamed:@"LoadingImage"];
		[iv setImage:img];
		
		[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:imageUrl RespondToImage:img IV:iv MVC:owner];
		
		iv.userInteractionEnabled = YES;
		
		[self addSubview:iv]; 
		iPos++;
	}
	
	self.imageArray = ivArray;
	
}


#pragma mark -
#pragma mark UIScrollViewDelegate stuff
- (void)scrollViewDidScroll:(UIScrollView *)_scrollView
{
	if (pageControlIsChangingPage) {
        return;
    }
	
	/*
	 *	We switch page at 50% across
	 */
    CGFloat pageWidth = _scrollView.frame.size.width;
    int page = floor((_scrollView.contentOffset.x - pageWidth / 2) / pageWidth) + 1;
    pageControl.currentPage = page;
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)_scrollView 
{
    pageControlIsChangingPage = NO;
}


#pragma mark -
#pragma mark PageControl stuff
- (IBAction)changePage:(id)sender 
{
	/*
	 *	Change the scroll view
	 */
    CGRect newFrame = self.frame;
    newFrame.origin.x = newFrame.size.width * pageControl.currentPage;
    newFrame.origin.y = 0;
	
    [self scrollRectToVisible:newFrame animated:YES];
	
	/*
	 *	When the animated scrolling finishings, scrollViewDidEndDecelerating will turn this off
	 */
    pageControlIsChangingPage = YES;
}




@end
