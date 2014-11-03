//
//  ItinDetailsViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#if defined(CORP) & defined(ENTERPRISE)
#import <PassKit/PassKit.h>
#endif

#import "TripsData.h"
#import "TripData.h"
#import "SegmentData.h"
#import "ItinDetailsCellLabel.h"
#import "ItinDetailsCellInfo.h"
#import "ImageControl.h"
#import "SegmentRow.h"
#import "FlightStatsData.h"
#import "ImageControl.h"
#import "TripAirVendorCell.h"
#import "TrainBookingDetailCell.h"
#import "TripDetailsViewController.h"
#import "TripsViewController.h"
#import "ItinDetailCell.h"
#import "ImageViewerMulti.h"
#import "iPadImageViewerVC.h"

#import "ItinDetailsHotelCell2.h"

#if defined(CORP) & defined(ENTERPRISE)
@interface ItinDetailsViewController : MobileViewController  <UITableViewDelegate, UITableViewDataSource, PKAddPassesViewControllerDelegate>
#else
@interface ItinDetailsViewController : MobileViewController  <UITableViewDelegate, UITableViewDataSource>
#endif
{
	NSMutableDictionary	*listData;
	NSMutableArray		*sections;
	UITableView			*tableList;
	UINavigationBar		*navBar;
	NSString			*carrierURL;
	TripsData			*tripsData;
	NSString			*tripKey, *segmentKey,*segmentType;
	EntitySegment		*segment;
	
	UILabel				*labelOperatedBy;
	UILabel				*labelVendor;
	UILabel				*labelStart;
	UILabel				*labelEnd;
	UILabel				*labelLocator;
	UILabel				*labelStartLabel;
	UILabel				*labelEndLabel;
	UILabel				*labelLocatorLabel, *labelBorder;
	UIImageView			*imgHead, *imgBar1, *imgBar2, *imgBoardingPass, *imgVendor;
	UILabel				*lvVendor, *lvTerminal, *lvTerminalValue, *lvGate, *lvGateValue, *lvSeat, *lvSeatValue1, *lvSeatValue2, *lvVendorFlight, *lvOperatedBy
						,*lvDeparture, *lvArrival, *lvFirstName, *lvLastName, *lvFrequentFlyerNum, *lvCarrierFlight, *lvClassGate, *lvTime;
	NSMutableArray		*keys, *hotelImagesArray;
	
	int					pagePos;
	
	//fetching view
	UIView					*fetchView;
	UILabel					*lblFetch;
	UIActivityIndicatorView	*spinnerFetch;
//	ItinDetailsHotelCell2	*hotelCell;
	
	NSMutableDictionary				*dictHotelImages;
	NSMutableDictionary				*dictHotelImageURLs;
	EntityTrip						*trip;
    
    //new
    UILabel                         *lblHeading, *lblSub1, *lblSub2, *lblSub3;
    UIImageView                     *ivHeaderImage, *ivHeaderBackground, *ivHotelBackground;
    UIView                          *viewHotelHeader;
    UIActivityIndicatorView         *activityImage;
    NSMutableArray                  *aHotelImageViews;
    
    //air rail field
    UILabel                         *lblHeadingAir, *lblAirConfirm, *lblAirline, *lblDepart, *lblArrive, *lblDepartTime, *lblDepartAMPM, *lblArriveTime, *lblArriveAMPM, *lblDepartDate, *lblArriveDate, *lblDepartTerminal, *lblArriveTerminal, *lblDepartGate, *lblArriveGate;
    UIView                          *viewAirHeader;
                       
}

extern NSString * const INFO_NIB;
extern NSString * const LABEL_NIB;
extern NSString * const ITIN_DETAILS_VIEW;

@property (nonatomic, strong) IBOutlet UILabel                         *lblHeadingAir;
@property (nonatomic, strong) IBOutlet UILabel                         *lblAirConfirm;
@property (nonatomic, strong) IBOutlet UILabel                         *lblAirline;
@property (nonatomic, strong) IBOutlet UILabel                         *lblDepart;
@property (nonatomic, strong) IBOutlet UILabel                         *lblArrive;
@property (nonatomic, strong) IBOutlet UILabel                         *lblDepartTime;
@property (nonatomic, strong) IBOutlet UILabel                         *lblDepartAMPM;
@property (nonatomic, strong) IBOutlet UILabel                         *lblArriveTime;
@property (nonatomic, strong) IBOutlet UILabel                         *lblArriveAMPM;
@property (nonatomic, strong) IBOutlet UILabel                         *lblDepartDate;
@property (nonatomic, strong) IBOutlet UILabel                         *lblArriveDate;
@property (nonatomic, strong) IBOutlet UILabel                         *lblDepartTerminal;
@property (nonatomic, strong) IBOutlet UILabel                         *lblArriveTerminal;
@property (nonatomic, strong) IBOutlet UILabel                         *lblDepartGate;
@property (nonatomic, strong) IBOutlet UILabel                         *lblArriveGate;
@property (nonatomic, strong) IBOutlet UIView                          *viewAirHeader;

@property (nonatomic, strong) NSMutableArray                           *aHotelImageViews;
@property (nonatomic, strong) IBOutlet UIActivityIndicatorView         *activityImage;
@property (nonatomic, strong) IBOutlet UIView                          *viewHotelHeader;
@property (nonatomic, strong) IBOutlet UIImageView                     *ivHeaderImage;
@property (nonatomic, strong) IBOutlet UIImageView                     *ivHeaderBackground;
@property (nonatomic, strong) IBOutlet UIImageView                     *ivHotelBackground;
@property (nonatomic, strong) IBOutlet UILabel                         *lblHeading;
@property (nonatomic, strong) IBOutlet UILabel                         *lblSub1;
@property (nonatomic, strong) IBOutlet UILabel                         *lblSub2;
@property (nonatomic, strong) IBOutlet UILabel                         *lblSub3;

@property int pagePos;
@property (nonatomic, strong) EntityTrip				*trip;
@property (nonatomic, strong) NSString				*carrierURL;
@property (nonatomic, strong) NSMutableDictionary	*listData;
@property (nonatomic, strong) NSMutableArray		*sections;
@property (nonatomic, strong) IBOutlet UITableView	*tableList;
@property (nonatomic, strong) IBOutlet UINavigationBar *navBar;
@property (nonatomic, strong) NSString				*tripKey;
@property (nonatomic, strong) NSString				*segmentKey;
@property (nonatomic, strong) NSString				*segmentType;
@property (nonatomic, strong) EntitySegment			*segment;
//@property (strong, nonatomic) ItinDetailsHotelCell2	*hotelCell;

@property (nonatomic, strong) IBOutlet UILabel		*labelOperatedBy;
@property (nonatomic, strong) IBOutlet UILabel		*labelVendor;
@property (nonatomic, strong) IBOutlet UILabel		*labelStart;
@property (nonatomic, strong) IBOutlet UILabel		*labelEnd;
@property (nonatomic, strong) IBOutlet UILabel		*labelLocator;
@property (nonatomic, strong) IBOutlet UILabel		*labelStartLabel;
@property (nonatomic, strong) IBOutlet UILabel		*labelEndLabel;
@property (nonatomic, strong) IBOutlet UILabel		*labelLocatorLabel;
@property (nonatomic, strong) IBOutlet UILabel		*labelBorder;
@property (nonatomic, strong) IBOutlet UIImageView	*imgHead;
@property (nonatomic, strong) IBOutlet UIImageView	*imgBar1;
@property (nonatomic, strong) IBOutlet UIImageView	*imgBar2;
@property (nonatomic, strong) IBOutlet UIImageView	*imgVendor;

@property (nonatomic, strong) IBOutlet UILabel *lvVendor;
@property (nonatomic, strong) IBOutlet UILabel *lvTerminal;
@property (nonatomic, strong) IBOutlet UILabel *lvTerminalValue;
@property (nonatomic, strong) IBOutlet UILabel *lvGate;
@property (nonatomic, strong) IBOutlet UILabel *lvGateValue;
@property (nonatomic, strong) IBOutlet UILabel *lvSeat;
@property (nonatomic, strong) IBOutlet UILabel *lvSeatValue1;
@property (nonatomic, strong) IBOutlet UILabel *lvSeatValue2;
@property (nonatomic, strong) IBOutlet UILabel *lvVendorFlight;
@property (nonatomic, strong) IBOutlet UILabel *lvOperatedBy;
@property (nonatomic, strong) IBOutlet UILabel *lvDeparture;
@property (nonatomic, strong) IBOutlet UILabel *lvArrival;
@property (nonatomic, strong) IBOutlet UILabel *lvFirstName;
@property (nonatomic, strong) IBOutlet UILabel *lvLastName;
@property (nonatomic, strong) IBOutlet UILabel *lvFrequentFlyerNum;
@property (nonatomic, strong) IBOutlet UILabel *lvCarrierFlight;
@property (nonatomic, strong) IBOutlet UILabel *lvClassGate;
@property (nonatomic, strong) IBOutlet UILabel *lvTime;
@property (nonatomic, strong) IBOutlet UIImageView *imgBoardingPass;

@property (nonatomic, strong) NSMutableArray		*keys;
@property (nonatomic, strong) NSMutableArray		*hotelImagesArray;

//fetching view
@property (nonatomic, strong) IBOutlet UIView					*fetchView;
@property (nonatomic, strong) IBOutlet UILabel					*lblFetch;
@property (nonatomic, strong) IBOutlet UIActivityIndicatorView	*spinnerFetch;

@property (strong, nonatomic) NSMutableDictionary				*dictHotelImages;
@property (strong, nonatomic) NSMutableDictionary				*dictHotelImageURLs;
@property BOOL isTripApproval;

-(void)goSomeplace:(NSString *)mapAddress VendorName:(NSString *)vendorName VendorCode:(NSString *)vendorCode;

-(void)fillParkingSections;
-(void)fillAirSections;
-(void)fillCarSections;
-(void)fillHotelSections;

-(void) makeRefreshButton:(NSString *)dtRefreshed;
-(BOOL) shouldShowFlightStatsButton:(NSString*)dtRefreshed;
-(BOOL) shouldShowCancelHotelButton;
-(void) refreshData;
-(void) showFlightStats;
-(void) cancelHotelPressed;
-(void) cancelHotel;
-(void)fillRideSections;
-(void)fillDiningSections;
-(void)fillRailSections;
-(void)reloadHotelImages;

-(NSString *)getAircraftURL:(NSString *)vendorCode AircraftCode:(NSString *)aircraftCode;
-(IBAction)loadWebViewSeat:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle;
-(void) loadSegment:(NSString *) key Trip:(EntityTrip *)trip SegmentKey:(NSString *)segKey Segment:(EntitySegment *)seg SegmentType:(NSString *)segType;
-(void) fillImageURLs: (NSMutableArray*) imageURLs;

-(void) configureAirHeader;
-(void) configureCarHeader;
-(void) configureHotelHeader;
-(void) configureRailHeader;
-(void) configureParkingHeader;
-(void) configureRideHeader;
-(void) configureBasicHeader:(NSString*) headerText sub1Text:(NSString *)sub1Text imageName:(NSString *)imageName;

-(IBAction) buttonPressedViewHotelImages:(id)sender;
-(UIImage *)getCarImageAsynch:(NSString *)vCode CountryCode:(NSString *)countryCode ClassOfCar:(NSString *)classOfCar BodyType:(NSString *)bodyType FetchURI:(NSString *)fetchURI;
-(UIImage *)scaleImageToFit:(UIImage *) img MaxW:(float)maxW MaxH:(float)maxH;

-(BOOL) shouldShowCancelCarButton;
-(void) cancelCarPressed;
-(void)cancelCar;
@end
