//
//  TripDetailsViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "MobileViewController.h"
#import "TripsData.h"
#import "TripSegmentCell.h"
#import "TripToApprove.h"
#import "OfferWebVC.h"
#import "ReportRejectionDelegate.h"


@interface TripDetailsViewController : MobileViewController  <UITableViewDelegate, UITableViewDataSource, UIActionSheetDelegate, UIAlertViewDelegate, ReportRejectionDelegate>{
	//NSMutableDictionary			*listData;
	NSMutableArray				*listKeys;
	UITableView					*tableList;
	UINavigationBar				*navBar;	
	TripsData					*tripsData;
	EntityTrip					*trip;
	NSMutableDictionary         *tripBits;
    NSMutableArray		*keys;
	NSString			*tripKey;
	
	UILabel *labelTripName;
	UILabel *labelStart;
	UILabel *labelEnd;
	UILabel *labelLocator;
	
	NSString			*lastTripKey;
	
	//fetching view
	UIView					*fetchView;
	UILabel					*lblFetch;
	UIActivityIndicatorView	*spinnerFetch;
    BOOL                    wentSomewhere;
    BOOL                    isOffersHidden;
    BOOL                    hasValidOffers;
    BOOL                    showBookingOption;
    NSMutableDictionary     *filteredSegments;
//    NSDictionary            *allowAddBooking;
}

@property BOOL isOffersHidden;
@property (nonatomic) BOOL hasValidOffers;
@property BOOL wentSomewhere;
@property BOOL showBookingOption;
//@property (nonatomic, strong) NSDictionary      *allowAddBooking;
//@property (nonatomic, retain) NSMutableDictionary *listData;
@property (nonatomic, strong) NSMutableArray *listKeys;
@property (nonatomic, strong) IBOutlet UITableView *tableList;
@property (nonatomic, strong) IBOutlet UINavigationBar *navBar;
@property (nonatomic, strong) TripsData *tripsData;
@property (nonatomic, strong) EntityTrip *trip;

@property (nonatomic, strong) NSString			*lastTripKey;

@property (nonatomic, strong) IBOutlet UILabel *labelTripName;
@property (nonatomic, strong) IBOutlet UILabel *labelStart;
@property (nonatomic, strong) IBOutlet UILabel *labelEnd;
@property (nonatomic, strong) IBOutlet UILabel *labelLocator;
@property (weak, nonatomic) IBOutlet UILabel *labelRecordLocator;
@property (strong, nonatomic) IBOutlet UILabel *labelTravelPoints;
@property (strong, nonatomic) IBOutlet UIImageView *imageViewTripHeader;

@property (nonatomic, strong) NSString				*tripKey;
@property (nonatomic, strong) NSMutableDictionary	*tripBits;
@property (nonatomic, strong) NSMutableArray		*keys;
@property (nonatomic, strong) NSMutableDictionary        *filteredSegments;
@property (nonatomic, strong) NSString *agencyPhoneNumber;
@property (nonatomic) BOOL didMakePhoneCall;

//fetching view
@property (nonatomic, strong) IBOutlet UIView					*fetchView;
@property (nonatomic, strong) IBOutlet UILabel					*lblFetch;
@property (nonatomic, strong) IBOutlet UIActivityIndicatorView	*spinnerFetch;


@property (strong, nonatomic) IBOutlet UILabel *lblName;
@property (strong, nonatomic) IBOutlet UILabel *lblDate;
@property (strong, nonatomic) IBOutlet UILabel *lblAmount;
@property (strong, nonatomic) IBOutlet UILabel *lblBottom;
@property (strong, nonatomic) IBOutlet UIView *viewApprovalHeader;
@property (strong, nonatomic) IBOutlet TripToApprove *tripToApprove;
@property BOOL isApproval;

-(IBAction)switchViews:(id)sender;

-(void) deselect: (id) sender;
- (void)switchToSegmentView:(NSString *)idKey SegmentType:(NSString *)segmentType TripKey:(NSString *)tripKey Segment:(EntitySegment *)segment;

-(float) getHeightForSegment:(EntitySegment *) seg;

-(IBAction)buttonActionPressed:(id)sender;

-(void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex;

-(void) makeRefreshButton:(NSString *)dtRefreshed;
-(void) refreshData;

-(NSMutableString *) combineAddress:(NSString *)addr city:(NSString *)city state:(NSString *)st country:(NSString *)cntry zip:(NSString *)zip;

-(void) configureCellAir:(TripSegmentCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellCar:(TripSegmentCell *)cell segment:(EntitySegment *)segment;
-(void)configureCellHotel:(TripSegmentCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellRide:(TripSegmentCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellRail:(TripSegmentCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellParking:(TripSegmentCell *)cell segment:(EntitySegment *)segment;
-(void)applicationEnteredForeground;
@end
