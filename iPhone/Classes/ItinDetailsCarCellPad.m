//
//  ItinDetailsCarCellPad.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ItinDetailsCarCellPad.h"
#import "ImageViewerVC.h"
#import "iPadImageViewerVC.h"
#import "DetailViewController.h"

NSString * const ITIN_DETAILS_CELLPADCAR_REUSABLE_IDENTIFIER = @"ItinDetailsCarCellPad";

@implementation ItinDetailsCarCellPad

@synthesize		lblLine1, lblLine2, lblLine3, lblLine4, lblLine5;
@synthesize		scroller;
@synthesize		imageArray;
@synthesize		pageControl, parentVC, imgVendor, btn, btnReturn, btnPickup, vendor, vendorCode, addr, addr2, lblLine1Value, lblLine2Value;

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
	return ITIN_DETAILS_CELLPADCAR_REUSABLE_IDENTIFIER;
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
#pragma mark iPad Stuff
-(IBAction) showHotelImages:(id)sender
{
	
	iPadImageViewerVC *vc = [[iPadImageViewerVC alloc] init];
	vc.imageArray = (NSMutableArray *)imageArray;
	vc.modalPresentationStyle = UIModalPresentationFormSheet;
	[parentVC presentViewController:vc animated:YES completion:nil];
	
}

-(IBAction) showAddress:(id)sender
{
	DetailViewController *dvc = (DetailViewController *)parentVC;
	UIButton *button = (UIButton *)sender;
	NSString *goToAddress = @"";
	if(button.tag == 1)
		goToAddress = addr;
	else 
		goToAddress = addr2;
	//NSLog(@"gotoaddress %@", goToAddress);
	[dvc goSomeplace:goToAddress VendorName:vendor VendorCode:vendorCode];
}
@end





