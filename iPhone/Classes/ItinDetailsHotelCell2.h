//
//  ItinDetailsHotelCell2.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ItinDetailsViewController.h"
#import "ImageViewerMulti.h"
#import "MobileViewController.h"

//@interface scrolling : UIScrollView {
//	
//}
//@end
//
//@interface scrollingIV : UIImageView {
//	ItinDetailsViewController	*parentVC;	
//	NSArray				*imageArray;
//	int					pos;
//}
//
//@property (retain, nonatomic) ItinDetailsViewController	*parentVC;
//@property (retain, nonatomic) NSArray			*imageArray;
//@property int pos;
//@end


@interface ItinDetailsHotelCell2 : UITableViewCell <UIScrollViewDelegate>{
	UILabel					*lblLine1, *lblLine2, *lblLine3, *lblLine4, *lblLine5;
	//scrolling				*scroller;
	NSMutableArray			*imageArray;
	IBOutlet UIPageControl	*pageControl;
	BOOL					pageControlIsChangingPage;
	//MobileViewController	*parentVC; //ItinDetailsViewController
	UIImageView				*iv;
	UIButton				*btn;
	
	//image viewer stuff//////////////////////
	ImageViewerMulti		*imageViewerMulti;
	NSMutableArray			*aImageURLs;
	MobileViewController	*parentVC;
	UIButton				*btnImage;
	UIImageView				*ivImage;
	//////////////////////////////////////////
	
	UIActivityIndicatorView	*activity;
}

@property (strong, nonatomic) IBOutlet UIActivityIndicatorView	*activity;
@property (strong, nonatomic) IBOutlet UIButton			*btn;
@property (strong, nonatomic) IBOutlet UIImageView		*iv;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine1;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine2;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine3;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine4;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine5;
@property (strong, nonatomic) IBOutlet UIScrollView		*scroller;
@property (strong, nonatomic) NSMutableArray	*imageArray;
@property (nonatomic, strong) UIPageControl				*pageControl;
//@property (nonatomic, retain) MobileViewController *parentVC;

/* for pageControl */
- (IBAction)changePage:(id)sender;

/* internal */
- (void)setupPage;

-(IBAction) showHotelImages:(id)sender;

//image viewer stuff///////////////////////////////////////////////////////////////
@property (nonatomic, strong) ImageViewerMulti				*imageViewerMulti;
@property (nonatomic, strong) NSMutableArray				*aImageURLs;
@property (nonatomic, strong) MobileViewController			*parentVC;
@property (nonatomic, strong) IBOutlet UIButton				*btnImage;
@property (nonatomic, strong) IBOutlet UIImageView			*ivImage;
-(void)configureImages:(id)owner propertyImagePairs:(NSArray*)propertyImagePairs;
-(IBAction) showHotelImages:(id)sender;
///////////////////////////////////////////////////////////////////////////////////
@end
