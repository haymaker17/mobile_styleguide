//
//  HotelBookingViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseTableViewController.h"
#import "HotelSummaryDelegate.h"
#import "UIImageScrollView.h"
#import "ImageViewerMulti.h"
#import "EntityHotelBooking.h"
#import "EntityHotelRoom.h"
#import "EntityHotelViolation.h"
#import "HotelBookingManager.h"
#import "EntityTravelCustomFields.h"
#import "BoolEditDelegate.h"

@class HotelSearch;
@class HotelReservationResponse;
@class HotelSearchCriteria;

@interface HotelBookingViewController : MobileViewController <UITableViewDelegate, UITableViewDataSource,
    BoolEditDelegate,
    HotelSummaryDelegate>
{
	HotelSearch		*hotelSearch;
	NSNumber		*creditCardIndex;
	UIView			*updatingItineraryView;
	NSArray			*violationReasons;
	NSArray			*violationReasonLabels;
	UILabel			*updatingItineraryLabel;
    
//    HotelSummaryDelegate	*hotelSummaryDelegate;
	UIImageScrollView		*scroller;
	UILabel					*hotelName;
	UILabel					*address1;
	UILabel					*address2;
	UILabel					*address3;
	UILabel					*phone;
	UILabel					*distance;
	UILabel					*starRating;
	UILabel					*shadowStarRating;
	UILabel					*notRated;
	BOOL					isAddressLinked;
	int						currentPage;
	
	UIImageView				*ivHotel;
	UIButton				*btnHotel;
	NSMutableArray			*aImageURLs;
	ImageViewerMulti		*imageViewerMulti;
    UIImageView             *ivStars;
    HotelReservationResponse    *  hotelRezResponse;
    
    EntityHotelBooking      *hotelBooking;
    EntityHotelRoom         *hotelBookingRoom;
    
    //Custom Fields 
    BOOL                hideCustomFields;
    BOOL                editedDependentCustomField;
    BOOL                isDirty;
    UITableView         *tableList;
}
@property (strong, nonatomic) IBOutlet UITableView *tableList;

//Custom Fields Properties
@property BOOL isDirty;
@property (strong, nonatomic) NSMutableDictionary *dictSections;
@property (strong, nonatomic) IBOutlet UIView                  *viewSegmentHeader;
@property BOOL editedDependentCustomField;
@property (strong, nonatomic) EntityTravelCustomFields *selectedCustomField;
@property (nonatomic, strong) NSMutableArray          *tcfRows;
@property BOOL hideCustomFields; // Hide custom fields when coming from existing trip
@property (strong, nonatomic) NSMutableArray			*aSections;

@property (nonatomic, strong) EntityHotelBooking      *hotelBooking;
@property (nonatomic, strong) EntityHotelRoom         *hotelBookingRoom;

@property (strong, nonatomic) HotelReservationResponse    *  hotelRezResponse;
@property (nonatomic, strong) IBOutlet UIImageView             *ivStars;
//@property (nonatomic, assign) HotelSummaryDelegate			*hotelSummaryDelegate;
@property (nonatomic, strong) IBOutlet UIImageScrollView	*scroller;
@property (nonatomic, strong) IBOutlet UILabel				*hotelName;
@property (nonatomic, strong) IBOutlet UILabel				*address1;
@property (nonatomic, strong) IBOutlet UILabel				*address2;
@property (nonatomic, strong) IBOutlet UILabel				*address3;
@property (nonatomic, strong) IBOutlet UILabel				*phone;
@property (nonatomic, strong) IBOutlet UILabel				*distance;
@property (nonatomic, strong) IBOutlet UILabel				*starRating;
@property (nonatomic, strong) IBOutlet UILabel				*shadowStarRating;
@property (nonatomic, strong) IBOutlet UILabel				*notRated;
@property (nonatomic) BOOL									isAddressLinked;
@property (nonatomic) int									currentPage;
@property (nonatomic, strong) IBOutlet UIImageView			*ivHotel;
@property (nonatomic, strong) IBOutlet UIButton				*btnHotel;
@property (nonatomic, strong) NSMutableArray				*aImageURLs;
@property (nonatomic, strong) ImageViewerMulti				*imageViewerMulti;
@property (nonatomic, strong) NSString                      *travelPointsInBank;

@property (nonatomic, strong) HotelSearch		*hotelSearch;
@property (nonatomic, strong) NSNumber			*creditCardIndex;
@property (nonatomic, strong) NSArray           *creditCards;
@property (nonatomic, strong) NSString          *cancellationPolicyText;
@property (nonatomic, strong) IBOutlet UIView	*updatingItineraryView;
@property (nonatomic, strong) NSArray			*violationReasons;
@property (nonatomic, strong) NSArray			*violationReasonLabels;
@property (nonatomic, strong) IBOutlet UILabel	*updatingItineraryLabel;

@property (strong, nonatomic) NSMutableArray                *taFields;  // TravelAuth fields for GOV
@property BOOL isVoiceBooking;
@property BOOL isPreSellOptionsLoaded;
@property BOOL cancellationPolicyNeedsViewing;

-(NSString*)getRoomRate;
-(NSString*)getRoomDescription;
-(NSString*)getCreditCard;
-(BOOL)canChooseCreditCard;
-(bool)isBookingAllowed;
-(NSUInteger)getViolationsCount;
-(NSString*)getViolations;
-(NSString*)getViolationReason;
-(NSString*)getViolationJustification;
-(int)getIndexForViolationReasonCode:(NSString*)reasonCode;

-(void)configureConfirmButton;
-(IBAction)btnConfirm:(id)sender;

-(void)requestReservation;
-(void)showHotelReservationResponse:(HotelReservationResponse*)hotelReservationResponse;

-(void)initData:(NSMutableDictionary*)paramBag;
-(void)makeToolbar:(HotelSearchCriteria*)hotelSearchCriteria;

-(void) makeHeader;
-(void) updateViolationReasons;

-(void)populateSections;

#pragma mark - Custom Fields Methods
-(UITableViewCell *)configureCustomFieldCellAtIndexPath:(NSIndexPath *)indexPath;
-(void) onSelectLongTextOrNumericFieldCellAtIndexPath:(NSIndexPath *)indexPath;
-(BOOL) hasPendingRequiredTripFields;
-(void) reloadCustomFieldsSection;
-(void) updateDynamicCustomFields;
@end
