//
//  CarDetailsViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseTableViewController.h"
#import "HotelBookingCell.h"
#import "EntityTravelCustomFields.h"
#import "BoolEditDelegate.h"

@class Car;
@class CarSearchCriteria;
@class CarReservationResponse;
@class CarBookingTripData;
@class ItinDetailsCellLabel;
@class ItinDetailsCellInfo;
@class ImageCache;

@interface CarDetailsViewController : MobileViewController <UITableViewDelegate, UITableViewDataSource,
    BoolEditDelegate>
{
}

@property (nonatomic, strong) IBOutlet UITableView	*tableList;
@property (nonatomic, strong) CarReservationResponse      *carRezResponse;

//Custom Fields Properties
@property BOOL isDirty;
@property (strong, nonatomic) NSMutableDictionary *dictSections;
@property (strong, nonatomic) IBOutlet UIView                  *viewSegmentHeader;
@property BOOL editedDependentCustomField;
@property (strong, nonatomic) EntityTravelCustomFields *selectedCustomField;
@property (nonatomic, strong) NSMutableArray          *tcfRows;
@property BOOL hideCustomFields; // Hide custom fields when coming from existing trip
@property (strong, nonatomic) NSMutableArray			*aSections;

@property (nonatomic, strong) CarSearchCriteria *carSearchCriteria;
@property (nonatomic, strong) Car					*car;
@property (nonatomic, strong) NSNumber				*creditCardIndex;
@property (nonatomic, strong) NSArray               *creditCards;
@property (nonatomic, strong) NSArray				*violationReasons;
@property (nonatomic, strong) NSArray				*violationReasonLabels;
@property (nonatomic, strong) NSString				*violationReasonCode;
@property (nonatomic, strong) NSString				*violationJustification;
@property (nonatomic, strong) CarBookingTripData	*carBookingTripData;
@property (nonatomic, strong) IBOutlet UIView		*updatingItineraryView;
@property (nonatomic, strong) IBOutlet UILabel		*updatingItineraryLabel;
@property (nonatomic, strong) ImageCache			*imageCache;
@property BOOL isPreSellOptionsLoaded;

@property (nonatomic, strong) IBOutlet UILabel             *lblHeading;
@property (nonatomic, strong) IBOutlet UILabel             *lblSubheading1;
@property (nonatomic, strong) IBOutlet UILabel             *lblSubheading2;
@property (nonatomic, strong) IBOutlet UIImageView         *ivCar;

@property (strong, nonatomic) NSMutableArray          *taFields;  // TravelAuth fields for GOV

-(NSString*)getCreditCard;
-(BOOL)canChooseCreditCard;

-(void)configureReserveButton;
-(IBAction)btnReserve:(id)sender;

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex;

-(void)requestReservation;
-(void)showCarReservationResponse:(CarReservationResponse*)carReservationResponse;

-(NSUInteger)getViolationsCount;
-(NSString*)getViolations;
-(NSString*)getViolationReason;
-(NSString*)getViolationJustification;
-(int)getIndexForViolationReasonCode:(NSString*)reasonCode;
-(NSString *)textForVendorDayHours:(NSString *)dayOfWeek openingTime:(NSString *)openingTime closingTime:(NSString *)closingTime;


-(void) updateHeader;
-(void) updateViolationReasons;

-(void)populateSections;

#pragma mark - Custom Fields Methods
-(UITableViewCell *)configureCustomFieldCellAtIndexPath:(NSIndexPath *)indexPath;
-(void) onSelectLongTextOrNumericFieldCellAtIndexPath:(NSIndexPath *)indexPath;
-(BOOL) hasPendingRequiredTripFields;
-(void) reloadCustomFieldsSection;
-(void) updateDynamicCustomFields;
@end
