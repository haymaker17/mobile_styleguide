//
//  ItinDetailsHotelCell2.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ItinDetailsHotelCell2.h"
#import "ImageViewerVC.h"
#import "iPadImageViewerVC.h"

NSString * const ITIN_DETAILS_CELL_REUSABLE_IDENTIFIER = @"ItinDetailsHotelCell2";

@implementation ItinDetailsHotelCell2

@synthesize		lblLine1, lblLine2, lblLine3, lblLine4, lblLine5;
@synthesize		scroller;
@synthesize		imageArray;
@synthesize		pageControl, iv, btn;
@synthesize		imageViewerMulti, aImageURLs, parentVC, btnImage, ivImage, activity;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if ((self = [super initWithStyle:style reuseIdentifier:reuseIdentifier])) {
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return ITIN_DETAILS_CELL_REUSABLE_IDENTIFIER;
}




//#pragma mark -
//#pragma mark Scroller Methods
////- (void)scrollViewDidScroll:(UIScrollView *)sender
////{
////	
////}
//
////- (void)scrollViewDidEndScrollingAnimation:(UIScrollView *)newScrollView
////{
////    CGFloat pageWidth = scroller.frame.size.width;
////    float fractionalPage = scroller.contentOffset.x / pageWidth;
////    NSInteger nearestNumber = lround(fractionalPage);
////	
////	//    if (pager.pageIndex != nearestNumber)
////	//    {
////	////        PageViewController *swapController = currentPage;
////	////        currentPage = nextPage;
////	////        nextPage = swapController;
////	//    }
////	
////    //pager.currentPage = currentPage.pageIndex;
////}
//
//
//- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event 
//{
//	int x = 0;
//}



#pragma mark -
#pragma mark The Guts
- (void)setupPage
{
	scroller.delegate = self;
	
	[self.scroller setBackgroundColor:[UIColor blackColor]];
	[scroller setCanCancelContentTouches:NO];
	
	scroller.indicatorStyle = UIScrollViewIndicatorStyleWhite;
	scroller.clipsToBounds = YES;
	scroller.scrollEnabled = YES;
	scroller.pagingEnabled = YES;
	
	//	NSUInteger nimages = 0;
	//	CGFloat cx = 0;
	//	for (; ; nimages++) {
	//		NSString *imageName = [NSString stringWithFormat:@"image%d.jpg", (nimages + 1)];
	//		UIImage *image = [UIImage imageNamed:imageName];
	//		if (image == nil) {
	//			break;
	//		}
	//		UIImageView *imageView = [[UIImageView alloc] initWithImage:image];
	//		
	//		CGRect rect = imageView.frame;
	//		rect.size.height = image.size.height;
	//		rect.size.width = image.size.width;
	//		rect.origin.x = ((scrollView.frame.size.width - image.size.width) / 2) + cx;
	//		rect.origin.y = ((scrollView.frame.size.height - image.size.height) / 2);
	//		
	//		imageView.frame = rect;
	//		
	//		[scrollView addSubview:imageView];
	//		[imageView release];
	//		
	//		cx += scrollView.frame.size.width;
	//	}
	
	if (imageArray != nil) 
	{
		self.pageControl.numberOfPages = [imageArray count];
	}

	//[scroller setContentSize:CGSizeMake(cx, [scrollView bounds].size.height)];
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
	if(![UIDevice isPad])
	{
		ItinDetailsViewController *vc = (ItinDetailsViewController*)parentVC;
		vc.pagePos = page;
	}
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
    CGRect frame = scroller.frame;
    frame.origin.x = frame.size.width * pageControl.currentPage;
    frame.origin.y = 0;
	
    [scroller scrollRectToVisible:frame animated:YES];
	
	/*
	 *	When the animated scrolling finishings, scrollViewDidEndDecelerating will turn this off
	 */
    pageControlIsChangingPage = YES;
}

#pragma mark -
#pragma mark ImageViewerMulti Methods
-(void)configureImages:(id)owner propertyImagePairs:(NSArray*)propertyImagePairs
{
	self.imageViewerMulti = [[ImageViewerMulti alloc] init];
	imageViewerMulti.parentVC = owner;
	
	[imageViewerMulti configureWithImagePairs:propertyImagePairs Owner:owner ImageViewer:ivImage];
	imageViewerMulti.aImageURLs = [imageViewerMulti getImageURLs:propertyImagePairs];
	self.aImageURLs = [imageViewerMulti getImageURLs:propertyImagePairs];
}

-(IBAction) showHotelImages:(id)sender
{	
	if(imageViewerMulti == nil)
	{
		self.imageViewerMulti = [[ImageViewerMulti alloc] init];
		imageViewerMulti.parentVC = parentVC;
		
		imageViewerMulti.aImageURLs = aImageURLs;
	}
	[imageViewerMulti showHotelImages:sender];	
}

@end


//@implementation scrollingIV
//@synthesize parentVC, imageArray, pos;
//
//- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
//	UITouch *touch = [[event allTouches] anyObject];
//	// Mob-2571
//	if ([touch tapCount] == 1)
//	{
//		ImageViewerVC *vc = [[ImageViewerVC alloc] init];
//		vc.imageArray = (NSMutableArray *)imageArray;
//		vc.pagePos = parentVC.pagePos;
//		[parentVC presentViewController:vc animated:YES completion:nil];
//		[vc release];
//		[self.nextResponder touchesBegan:touches withEvent:event];
//		
////		if(self.zoomScale > 1.0){
////			[self setZoomScale:1 animated:YES];
////		}else {
////			[self setZoomScale:3 animated:YES];
////		}
//	}
//}
//
//-(void)dealloc
//{
//	[parentVC release];
//	[imageArray release];
//	[super dealloc];
//}
//
//
//@end
//
//@implementation scrolling
//
//- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
//	UITouch *touch = [[event allTouches] anyObject];
//	// Mob-2571
//	if ([touch tapCount] == 1)
//	{
//		if(self.zoomScale > 1.0){
//			[self setZoomScale:1 animated:YES];
//		}else {
//			[self setZoomScale:3 animated:YES];
//		}
//	}
//}


//@end
