//
//  TourVC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 5/6/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TourVC.h"
#import "TourContentVC.h"

static NSString *kImageKey = @"imageKey";
static NSString *kUpperLabelKey = @"upperLabelKey";
static NSString *kLowerLabelKey = @"lowerLabelKey";

@implementation TourVC

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        NSString *path = [[NSBundle mainBundle] pathForResource:@"TourImageNames" ofType:@"plist"];
        self.contentList = [NSArray arrayWithContentsOfFile:path];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self.navigationController setToolbarHidden:YES animated:YES];
    
    NSUInteger numberPages = self.contentList.count;
    
    // view controllers are created lazily
    // in the meantime, load the array with placeholders which will be replaced on demand
    NSMutableArray *controllers = [[NSMutableArray alloc] init];
    for (NSUInteger i = 0; i < numberPages; i++)
    {
		[controllers addObject:[NSNull null]];
    }
    self.viewControllers = controllers;
    
    // a page is the width of the scroll view
    self.scrollView.pagingEnabled = YES;
    self.scrollView.contentSize = CGSizeMake(CGRectGetWidth(self.scrollView.frame) * numberPages, CGRectGetHeight(self.scrollView.frame));
    self.scrollView.showsHorizontalScrollIndicator = NO;
    self.scrollView.showsVerticalScrollIndicator = NO;
    self.scrollView.scrollsToTop = NO;
    self.scrollView.delegate = self;
    
    self.pageControl.numberOfPages = numberPages;
    self.pageControl.currentPage = 0;
    
    // pages are created on demand
    // load the visible page
    // load the page on either side to avoid flashes when the user starts scrolling
    //
    [self loadScrollViewWithPage:0];
    [self loadScrollViewWithPage:1];

}

- (void)loadScrollViewWithPage:(NSUInteger)page
{
    if (page >= self.contentList.count)
        return;
    
    // replace the placeholder if necessary
    TourContentVC *controller = (self.viewControllers)[page];
    if ((NSNull *)controller == [NSNull null])
    {
        controller = [[TourContentVC alloc] initWithImageName:@"tour3"];
        (self.viewControllers)[page] = controller;
    }
    
    // add the controller's view to the scroll view
    if (controller.view.superview == nil)
    {
        CGRect frame = self.scrollView.frame;
        frame.origin.x = CGRectGetWidth(frame) * page;
        frame.origin.y = 0;
        controller.view.frame = frame;
        
        [self addChildViewController:controller];
        [self.scrollView addSubview:controller.view];
        [controller didMoveToParentViewController:self];
        
        NSDictionary *numberItem = (self.contentList)[page];
        NSString *upperLabel = [Localizer getLocalizedText:[numberItem valueForKey:kUpperLabelKey]];
        NSString *lowerLabel = [Localizer getLocalizedText:[numberItem valueForKey:kLowerLabelKey]];
        controller.tourImage.image = [UIImage imageNamed:[numberItem valueForKey:kImageKey]];
        
        // Get corrected font, set label font, set label text [lower label]
        // MOB-13334 Don't calculate upper label fonts. Use the same font as the lower label.
        // The lower laber needs adjust to fit the screen. Use lower label fonts so they are same font on same page
//        UIFont *correctedFont = [controller getFontForLabelWidth:controller.lblUpper.frame.size.width labelHeight:controller.lblUpper.frame.size.height minmumFontSize:8 fontName:@"HelveticaNeue-Bold" stringValue:upperLabel desiredFontSize:17];
//        [controller.lblUpper setFont:correctedFont];
//        controller.lblUpper.text = upperLabel;
        
        // MOB-15356 tour crash on click
        // This is caused by loading second page of tour. Lower label on second page needs special format which cause framework think it is an Array instead of String.
        // Fixed by treat second page seperately than other pages.
        // Get corrected font, set label font, set label text [lower label]
        UIFont *correctedFont = nil;
        if (page != 1)
        {
            correctedFont = [controller getFontForLabelWidth:controller.lblLower.frame.size.width labelHeight:controller.lblLower.frame.size.height minmumFontSize:8 fontName:@"HelveticaNeue-Bold" stringValue:lowerLabel desiredFontSize:17];
            
            [controller.lblUpper setFont:correctedFont];
            controller.lblUpper.text = upperLabel;
            [controller.lblLower setFont:correctedFont];
            controller.lblLower.text = lowerLabel;
        }
        
        if (page == 1)
        {
            NSArray *arrayLabel = [numberItem valueForKey:kLowerLabelKey];
            
            NSMutableString * bulletList = [NSMutableString stringWithCapacity:arrayLabel.count*30];
            for (NSString * s in arrayLabel)
            {
                [bulletList appendFormat:@"\u2022 %@\n", [Localizer getLocalizedText:s]];
            }
            correctedFont = [controller getFontForLabelWidth:controller.lblLower.frame.size.width labelHeight:controller.lblLower.frame.size.height minmumFontSize:8 fontName:@"HelveticaNeue-Bold" stringValue:bulletList desiredFontSize:17];
            
            [controller.lblUpper setFont:correctedFont];
            controller.lblUpper.text = upperLabel;
            [controller.lblLower setFont:correctedFont];
            [controller.lblLower setTextAlignment:NSTextAlignmentLeft];
            [controller.lblLower setText:bulletList];
        }
    }
}

// at the end of scroll animation, reset the boolean used when scrolls originate from the UIPageControl
- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView
{
    // switch the indicator when more than 50% of the previous/next page is visible
    CGFloat pageWidth = CGRectGetWidth(self.scrollView.frame);
    NSUInteger page = floor((self.scrollView.contentOffset.x - pageWidth / 2) / pageWidth) + 1;
    self.pageControl.currentPage = page;
    
    // load the visible page and the page on either side of it (to avoid flashes when the user starts scrolling)
    [self loadScrollViewWithPage:page - 1];
    [self loadScrollViewWithPage:page];
    [self loadScrollViewWithPage:page + 1];
    
    // a possible optimization would be to unload the views+controllers which are no longer visible
}

- (void)gotoPage:(BOOL)animated
{
    NSInteger page = self.pageControl.currentPage;
    
    // load the visible page and the page on either side of it (to avoid flashes when the user starts scrolling)
    [self loadScrollViewWithPage:page - 1];
    [self loadScrollViewWithPage:page];
    [self loadScrollViewWithPage:page + 1];
    
	// update the scroll view to the appropriate page
    CGRect bounds = self.scrollView.bounds;
    bounds.origin.x = CGRectGetWidth(bounds) * page;
    bounds.origin.y = 0;
    [self.scrollView scrollRectToVisible:bounds animated:animated];
}

- (IBAction)changePage:(id)sender
{
    [self gotoPage:YES];    // YES = animate
}
@end
