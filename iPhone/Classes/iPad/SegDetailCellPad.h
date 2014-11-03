//
//  SegDetailCellPad.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ExSystem.h" 

#import "Detail.h"
@class DetailViewController;
#import "MapViewController.h"
#import "WebViewController.h"
#import "SegmentData.h"
#import "DetailViewController.h"

@interface scrolling2 : UIScrollView {
	
}
@end

@interface scrollingIV2 : UIImageView {
	DetailViewController	*parentVC;	
	NSArray				*imageArray;
	int					pos;
}

@property (strong, nonatomic) DetailViewController	*parentVC;
@property (strong, nonatomic) NSArray			*imageArray;
@property int pos;
@end

@interface SegDetailCellPad : UITableViewCell <UIScrollViewDelegate, UITableViewDelegate, UITableViewDataSource> {
	//UIScrollView			*scroller;
	UITableView				*tableDetails;
	UIImageView				*iv, *ivVendor;
	UIToolbar				*tb;
	UIPageControl			*pager;

	NSMutableArray			*aDetails, *details, *hotelImagesArray;
	Detail					*detail;
	DetailViewController	*dVC;
	RootViewController		*rootVC;
	EntitySegment			*segment;
	
	scrolling2				*scroller;
	NSMutableArray			*imageArray;
	IBOutlet UIPageControl	*pageControl;
	BOOL					pageControlIsChangingPage;
	UILabel					*lblBackground;
	UILabel					*lblBack1, *lblBack2;
}

@property (nonatomic, strong) EntitySegment						*segment;

//@property (nonatomic, retain) IBOutlet UIScrollView				*scroller;
@property (nonatomic, strong) IBOutlet UITableView				*tableDetails;
@property (nonatomic, strong) IBOutlet UIImageView				*iv;
@property (nonatomic, strong) IBOutlet UIImageView				*ivVendor;
@property (nonatomic, strong) IBOutlet UIToolbar				*tb;
@property (nonatomic, strong) IBOutlet UIPageControl			*pager;
@property (nonatomic, strong) IBOutlet UILabel					*lblBackground;

@property (nonatomic, strong) Detail							*detail;
@property (nonatomic, strong) DetailViewController				*dVC;
@property (nonatomic, strong) NSMutableArray					*aDetails;
@property (nonatomic, strong) NSMutableArray					*details;
@property (nonatomic, strong) NSMutableArray					*hotelImagesArray;
@property (nonatomic, strong) RootViewController				*rootVC;

@property (strong, nonatomic) IBOutlet UIScrollView		*scroller;
@property (strong, nonatomic) IBOutlet NSMutableArray	*imageArray;
@property (nonatomic, strong) UIPageControl				*pageControl;

@property (nonatomic, strong) IBOutlet UILabel					*lblBack1;
@property (nonatomic, strong) IBOutlet UILabel					*lblBack2;

-(IBAction)goSomeplace:(id)sender;
-(void)callNumber:(NSString *)phoneNum;
-(IBAction)loadWebView:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle;
@end
