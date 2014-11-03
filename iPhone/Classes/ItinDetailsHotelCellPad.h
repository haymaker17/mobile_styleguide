//
//  ItinDetailsHotelCellPad.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ItinDetailsViewController.h"



@interface ItinDetailsHotelCellPad : UITableViewCell <UIScrollViewDelegate>{
	UILabel					*lblLine1, *lblLine2, *lblLine3, *lblLine4, *lblLine5;

	NSMutableArray			*imageArray;
	IBOutlet UIPageControl	*pageControl;
	BOOL					pageControlIsChangingPage;
	MobileViewController	*parentVC; //ItinDetailsViewController
	UIImageView				*iv;
	UIButton				*btn, *btnAddress;
	NSString				*vendor, *vendorCode, *addr;
}

@property (strong, nonatomic) IBOutlet UIButton			*btn;
@property (strong, nonatomic) IBOutlet UIButton			*btnAddress;
@property (strong, nonatomic) IBOutlet UIImageView		*iv;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine1;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine2;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine3;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine4;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine5;
@property (strong, nonatomic) IBOutlet UIScrollView		*scroller;
@property (strong, nonatomic) NSMutableArray	*imageArray;
@property (nonatomic, strong) UIPageControl				*pageControl;
@property (nonatomic, strong) MobileViewController *parentVC;

@property (nonatomic, strong) NSString				*vendor;
@property (nonatomic, strong) NSString				*vendorCode;
@property (nonatomic, strong) NSString				*addr;

/* for pageControl */
- (IBAction)changePage:(id)sender;

/* internal */
- (void)setupPage;

-(IBAction) showHotelImages:(id)sender;
-(IBAction) showAddress:(id)sender;
@end
