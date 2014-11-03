//
//  WhatsNewView.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/23/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface WhatsNewView : UIView <UIScrollViewDelegate>
{
    BOOL pageControlIsChangingPage;
	int						pagePos;
}

@property (strong, nonatomic) IBOutlet UIScrollView *scroller;
@property (strong, nonatomic) IBOutlet UIScrollView *scrollNew;
@property (strong, nonatomic) IBOutlet UIView *view01;
@property (strong, nonatomic) IBOutlet UIView *view02;
@property (strong, nonatomic) IBOutlet UILabel *lblHeading;
@property (strong, nonatomic) IBOutlet UILabel *lblSwipe;
@property (strong, nonatomic) IBOutlet UILabel *lblFooter;

@property (nonatomic, strong) IBOutlet UIPageControl						*pageControl;

@property int pagePos;
@property BOOL pageControlIsChangingPage;

-(IBAction)closeMe:(id)sender;

/* for pageControl */
- (IBAction)changePage:(id)sender;

@end
