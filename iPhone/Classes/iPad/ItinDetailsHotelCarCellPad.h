//
//  ItinDetailsHotelCarCellPad.h
//  ConcurMobile
//
//  Created by Shifan Wu on 3/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ItinDetailsViewController.h"

@interface ItinDetailsHotelCarCellPad : UITableViewCell<UIScrollViewDelegate>
{
    UILabel             *lblCarVendor;
    UILabel             *lblConfirmNum;
    UIImageView         *ivTripType;
    UIImageView         *ivVendorIcon;
    UIImageView         *ivHotelAlbum;
    UIButton            *btnShowHotelImage;
    
    NSMutableArray			*imageArray;
	IBOutlet UIPageControl	*pageControl;
  	MobileViewController	*parentVC;
   	BOOL					pageControlIsChangingPage;    
}

@property (strong, nonatomic) IBOutlet UILabel *lblHotelCarVendor;
@property (strong, nonatomic) IBOutlet UILabel *lblConfirmNum;
@property (strong, nonatomic) IBOutlet UIImageView *ivTripType;
@property (strong, nonatomic) IBOutlet UIImageView *ivVendorIcon;
@property (strong, nonatomic) IBOutlet UIImageView *ivHotelAlbum;
@property (strong, nonatomic) IBOutlet UIButton *btnShowHotelImage;

@property (strong, nonatomic) NSMutableArray	*imageArray;
@property (nonatomic, strong) UIPageControl				*pageControl;
@property (nonatomic, strong) MobileViewController *parentVC;
@property (strong, nonatomic) IBOutlet UIScrollView *scroller;

- (IBAction)showHotelImages:(id)sender;
/* for pageControl */
- (IBAction)changePage:(id)sender;

/* internal */
- (void)setupPage;
@end
