//
//  DetailViewController.h
//  PlayThing
//
//  Created by Paul Kramer on 4/30/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "TripsData.h"
#import "ExSystem.h" 

#import "MapViewController.h"
#import "WebViewController.h"
#import "SegmentData.h"
#import "ItinDetailsViewController.h"
#import "OutOfPocketListViewController.h"
#import "TripsViewController.h"
#import "SepCell.h"
#import "ActiveReportListViewController.h"
//#import "ItinDetailsHotelCellPad.h"
#import "ItinDetailsHotelCarCellPad.h"
#import "LabelConstants.h"
#import "AirCancel.h"
#import "OfferManager.h"
#import "BaseDetailVC_iPad.h"

@class ActiveReportListViewController;
@interface DetailViewController : BaseDetailVC_iPad <UIPopoverControllerDelegate, UITableViewDelegate, UITableViewDataSource, UIScrollViewDelegate> 
{
    UIPopoverController			*popoverController;
	UINavigationBar				*navigationBar;
	UINavigationItem			*navTitle;

    id detailItem;
    UILabel						*detailDescriptionLabel;
	
	TripsData					*tripsData;
	EntityTrip					*trip, *activeTrip, *nextTrip, *previousTrip;
//    NSDictionary                *allowAddBooking;
	NSMutableArray				*listKeys;
	UITableView					*tripTable;
	
	NSMutableDictionary			*tripBits;
	NSMutableDictionary			*dictSegRows;
    NSMutableArray				*keys;
	NSString					*tripKey;
	
	UITableView					*tableDetails;
	
	NSString					*viewType;
	
	UIImageView					*ivLogo;
	UIImageView					*ivBack, *ivBlotter;
	
	UIImageView					*ivBarLeft, *ivBarRight, *ivBarMiddle;
	NSMutableArray				*aBtns;
	int							middleX;

	UIScrollView				*scroller;
	UILabel						*lblTripName, *lblTripDates;
	ItinDetailsViewController	*itinDetailsVC;
	
	IBOutlet UIPageControl		*pageControl;
	BOOL						pageControlIsChangingPage;
	CGFloat						dayWidth;
	
	OutOfPocketListViewController	*oopeListVC;
	TripsViewController				*tripsListVC;
	iPadHomeVC						*iPadHome;
	ItinDetailsHotelCarCellPad			*hotelCell;
	NSMutableArray					*hotelImagesArray;
	
	UILabel							*lblTripDetails, *lblBackBorder;
	UIImageView						*ivTopTable, *ivBottomTable, *ivTripPanel, *ivTripIcon;
	ActiveReportListViewController	*reportsListVC;
	UIScrollView					*scrollerButtons;
	
	UILabel							*lblPreviousTrip, *lblNextTrip;
	NSMutableDictionary				*dictHotelImages, *dictHotelImageURLs;
	UIButton						*btnNext, *btnPrevious;
	UIImageView						*headerImg;
	
	UIButton						*btnCar, *btnHotel, *btnRefresh, *btnCancel, *btnOffers;
	
	UIView							*holdView;
	UILabel							*holdText, *lblOffline;
	UIActivityIndicatorView			*holdActivity;
	UIImageView						*ivLoading;
	
	BOOL							isLoadingHotelImage;
	
	NSIndexPath						*hotelCancelIndexPath;
    
    BOOL                            isOffersHidden;
    BOOL                            hasValidOffers;
    NSMutableDictionary             *filteredSegments;
}
@property (nonatomic, strong) NSString							*btnCancelSegKey;
@property (nonatomic,strong) NSMutableDictionary            *filteredSegments;
@property (strong, nonatomic) IBOutlet UILabel *lblOffline;
//@property (strong, nonatomic) NSDictionary                  *allowAddBooking;
@property (nonatomic, strong) NSIndexPath					*hotelCancelIndexPath;
@property (nonatomic, strong) IBOutlet UILabel					*holdText;
@property (nonatomic, strong) IBOutlet UIActivityIndicatorView	*holdActivity;
@property (strong, nonatomic) IBOutlet UIView					*holdView;
@property (strong, nonatomic) IBOutlet UIImageView				*ivLoading;
@property (strong, nonatomic)  IBOutlet UIButton			*btnCar;
@property (strong, nonatomic)  IBOutlet UIButton			*btnHotel;
@property (strong, nonatomic)  IBOutlet UIButton			*btnRefresh;
@property (strong, nonatomic)  IBOutlet UIButton			*btnCancel;
@property (strong, nonatomic)  IBOutlet UIButton			*btnOffers;
@property (strong, nonatomic)  IBOutlet UIImageView			*headerImg;
@property (nonatomic, strong) IBOutlet UIButton				*btnNext;
@property (nonatomic, strong) IBOutlet UIButton				*btnPrevious;
@property (strong, nonatomic) NSMutableDictionary				*dictHotelImages;
@property (strong, nonatomic) NSMutableDictionary				*dictHotelImageURLs;
@property (strong, nonatomic) IBOutlet UILabel						*lblPreviousTrip;
@property (strong, nonatomic) IBOutlet UILabel						*lblNextTrip;
@property (strong, nonatomic) IBOutlet UIScrollView		*scrollerButtons;
@property (nonatomic, strong) IBOutlet UILabel						*lblTripDetails;
@property (nonatomic, strong) IBOutlet UILabel						*lblBackBorder;
@property (nonatomic, strong) IBOutlet UIImageView					*ivTopTable;
@property (nonatomic, strong) IBOutlet UIImageView					*ivBottomTable;
@property (nonatomic, strong) IBOutlet UIImageView					*ivTripPanel;
@property (nonatomic, strong) IBOutlet UIImageView					*ivTripIcon;
@property (strong, nonatomic) ItinDetailsHotelCarCellPad	*hotelCell;
@property (strong, nonatomic) NSMutableArray					*hotelImagesArray;
@property (nonatomic, strong) OutOfPocketListViewController		*oopeListVC;
@property (nonatomic, strong) TripsViewController				*tripsListVC;
@property (nonatomic, strong) IBOutlet UIImageView				*ivBarLeft;
@property (nonatomic, strong) IBOutlet UIImageView				*ivBarRight;
@property (nonatomic, strong) IBOutlet UIImageView				*ivBarMiddle;
@property (nonatomic, strong) IBOutlet UIImageView				*ivBack;
@property (nonatomic, strong) IBOutlet UIImageView				*ivBlotter;
@property (nonatomic, strong) IBOutlet UINavigationBar			*navigationBar;
@property (nonatomic, strong) IBOutlet UINavigationItem			*navTitle;
@property (nonatomic, strong) IBOutlet UIImageView				*ivLogo;
@property (nonatomic, strong) IBOutlet UITableView				*tableDetails;
@property (nonatomic, strong) id detailItem;
@property (nonatomic, strong) IBOutlet UILabel					*detailDescriptionLabel;
@property (nonatomic, strong) TripsData							*tripsData;
@property (nonatomic, strong) NSMutableArray					*listKeys;
@property (nonatomic, strong) IBOutlet UITableView				*tripTable;
@property (nonatomic, strong) EntityTrip						*trip;
@property (nonatomic, strong) EntityTrip						*activeTrip;
@property (nonatomic, strong) EntityTrip						*previousTrip;
@property (nonatomic, strong) EntityTrip						*nextTrip;
@property (nonatomic, strong) NSString							*tripKey;
@property (nonatomic, strong) NSMutableDictionary				*tripBits;
@property (nonatomic, strong) NSMutableDictionary				*dictSegRows;
@property (nonatomic, strong) NSMutableArray					*keys;
@property (nonatomic, strong) NSString							*viewType;
@property (nonatomic, strong) IBOutlet UIScrollView				*scroller;
@property (nonatomic, strong) IBOutlet UILabel					*lblTripName;
@property (nonatomic, strong) IBOutlet UILabel					*lblTripDates;

@property (nonatomic, strong) UIPageControl						*pageControl;
@property (nonatomic, strong) iPadHomeVC					*iPadHome;
@property (strong, nonatomic) ActiveReportListViewController	*reportsListVC;
@property (assign, nonatomic) BOOL isOffersHidden;
@property BOOL hasValidOffers;
@property (nonatomic, strong) NSString *tripDetailsRequestId;

-(void)makeSystemButtons;
-(void)displayTrip:(EntityTrip *)newTrip TripKey:(NSString *)newTripKey;
- (void)formatSegmentCells;

-(UILabel *) makeBasicLabel:(NSString *)text IsBold:(BOOL)isBold FontSize:(int)fontSize FontColor:(UIColor *)fontColor Y:(float)y X:(float)x W:(float)w H:(float)h;
-(NSMutableArray *) makeSegmentLabels:(EntitySegment *)segment;
-(UIView *) makeDayView:(NSString *)dayTitle Pos:(int)iPos;


-(void) showSegment:(id)sender;
-(UIView *) makeSegmentRow:(int)iPos Segment:(EntitySegment *)segment ParentView:(UIView *)parentView  NextY:(float)nextY;

- (void)getOrderedSegments:(EntityTrip *)currTrip;
- (void)setupPage;

-(void) killMe:(id)sender;

-(NSString *)getGateTerminal:(NSString *)gate Terminal:(NSString *)terminal;
-(void) fillTripDetails:(EntityTrip *)currTrip;
-(void) fillTripSegmentIcons:(EntityTrip *) t;
-(void)goSomeplace:(NSString *)mapAddress VendorName:(NSString *)vendorName VendorCode:(NSString *)vendorCode;
-(void)callNumber:(NSString *)phoneNum;
-(IBAction)loadWebView:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle;
-(IBAction)loadWebViewSeat:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle;
- (IBAction)buttonReportsPressed:(id)sender;

- (void) Perform_Swiped_left:(UISwipeGestureRecognizer*)sender;
- (void) Perform_Swiped_right:(UISwipeGestureRecognizer*)sender;
-(void) setUpPreviousNextTrips;
-(void) loadTrip:(NSMutableDictionary *)pBag;
-(void) fillImageURLs: (NSMutableArray*) imageURLs Section:(int) section Row:(int)row;

- (void)configureView;
-(void)inspectAddBookingRows;
-(void) adjustLabel:(UILabel *) lblHeading LabelValue:(UILabel*) lblVal HeadingText:(NSString *) headText ValueText:(NSString *) valText ValueColor:(UIColor *) color  W:(float)wOverride;

-(void) buttonBookHotelPressed:(id)sender;
-(void) buttonBookCarPressed:(id)sender;
-(void) buttonRefreshTripPressed:(id)sender;
-(void) refreshData;

- (void)buttonBook:(NSInteger)buttonIndex;

-(SegmentRow *) getCopyEnabledSegRow:(NSIndexPath *)indexPath;

-(void) hideButtonsForRoles;

-(BOOL)onlineCheck;

-(void) checkOffline;

-(void) cancelHotelPressed;
-(void) cancelHotel;
-(void) showFlightStats:(EntitySegment *)seg;
-(void) cancelCar;
-(void) cancelCarPressed;

@end
