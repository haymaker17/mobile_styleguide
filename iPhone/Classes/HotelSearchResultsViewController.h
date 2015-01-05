//
//  HotelSearchResultsViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseViewController.h"
#import "HotelBookingManager.h"
#import "EntityHotelBooking.h"

@class HotelCollectionViewController;
@class HotelListViewController;
@class HotelMapViewController;
@class HotelSearch;
@class HotelResult;
@class HotelSearchCriteria;
@class RotatingRoundedRectView;

@interface HotelSearchResultsViewController : BookingBaseViewController <UIAlertViewDelegate>
{
}

@property int totalCount;
@property int selectedHotelIndex;
@property NSUInteger childSortOrder;
@property long ticks;
@property int numberOfPolls;
@property BOOL readyToSendPollRequest;
@property (nonatomic) CFTimeInterval                            pollingStartTime;
@property (nonatomic, strong) HotelListViewController           *listController;
@property (nonatomic, strong) HotelCollectionViewController		*mapController;
@property (nonatomic, strong) HotelCollectionViewController		*activeViewController;
@property (nonatomic, strong) HotelSearch						*hotelSearch;
@property (nonatomic, strong) UIBarButtonItem					*switchButton;
@property (nonatomic, strong) UIBarButtonItem					*btnSearchCriteria;
@property (nonatomic, strong) UIBarButtonItem					*btnFlexibleSpace;
@property (nonatomic, strong) UIBarButtonItem					*btnHotelCount;
@property (nonatomic, strong) UIBarButtonItem					*btnReorder;
@property (nonatomic, strong) UIBarButtonItem					*btnAction;
@property (nonatomic, strong) UIBarButtonItem					*btnCancelPolling;
@property (nonatomic, strong) RotatingRoundedRectView           *pollingView;
@property (nonatomic, strong) UIView                            *coverView;
@property (nonatomic, strong) NSTimer                           *timer;

@property (nonatomic, strong) NSArray                           *hotelBenchmarks;
@property (nonatomic, strong) NSString                          *travelPointsInBank;
@property (nonatomic, strong) NSString                          *hotelBenchmarkRangeString;
@property (nonatomic) BOOL                                      isBenchmarkRange;
@property (strong, nonatomic) NSMutableArray                    *taFields;  // TravelAuth fields for GOV
@property BOOL isVoiceBooking;

- (IBAction)btnSwitchViews:(id)sender;
- (IBAction)buttonReorderPressed:(id)sender;
- (IBAction)buttonActionPressed:(id)sender;
- (IBAction)btnCancel:(id)sender;

//- (void)initData:(NSMutableDictionary*)paramBag;

- (void)makeToolbar;
- (void)showFullToolbar;
- (void)showMinimalToolbar;
- (void)switchViews;
- (void)showMap;
- (void)showRoomsForSelectedHotel: (EntityHotelBooking*)hotelBooking;
- (void)timerCallback:(NSTimer *) theTimer;
- (void)noRatesReceived;
- (void)createPollingView;
- (void)destroyPollingView;
- (void)logSearchComplete:(BOOL)success withError:(NSString*)errMsg;

+ (UIBarButtonItem *)makeSearchCriteriaButton:(HotelSearchCriteria*)hotelSearchCriteria;
- (UIBarButtonItem*)makeHotelCountButton:(NSUInteger)resultCount;

@end
