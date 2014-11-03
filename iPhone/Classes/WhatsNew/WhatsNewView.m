//
//  WhatsNewView.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/23/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "WhatsNewView.h"

@implementation WhatsNewView
@synthesize scroller, view01, view02, scrollNew, pageControl, pagePos, pageControlIsChangingPage, lblSwipe, lblFooter, lblHeading;

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/


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
    CGRect frame = scroller.frame;
    frame.origin.x = frame.size.width * pageControl.currentPage;
    frame.origin.y = 0;
	
    [scroller scrollRectToVisible:frame animated:YES];
	
	/*
	 *	When the animated scrolling finishings, scrollViewDidEndDecelerating will turn this off
	 */
    pageControlIsChangingPage = YES;
}


#pragma mark - Close
-(IBAction)closeMe:(id)sender
{
    [ExSystem sharedInstance].sys.showWhatsNew = NO;
	[[ExSystem sharedInstance] saveSystem];
    [self removeFromSuperview];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:@"DidCloseWhatsNew" object:nil];
}

@end
