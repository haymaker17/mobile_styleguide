//
//  TrainBookVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "FindMe.h"
#import "TrainBooking.h"
#import "AmtrakShop.h"
#import "iPadHomeVC.h"
#import "DateTimePopoverVC.h"
#import "EntityRail.h"
#import "BookingCellData.h"
#import "DateTimeOneDelegate.h"
#import "FieldEditDelegate.h"

@interface TrainBookVC : MobileViewController <UITableViewDelegate, UITableViewDataSource, 
    DateTimeOneDelegate, DateTimePopoverDelegate,
    FieldEditDelegate,
    BoolEditDelegate> {
	NSMutableArray			*aSections;
	NSMutableArray	*aList;
	UITableView		*tableList;
	BOOL			isRoundTrip, shouldReload, isCancelled;
	TrainBooking	*trainBooking;
	DateTimePopoverVC		*pickerPopOverVC;
    
    //new
    UIView                  *viewSegmentHeader;
    UISegmentedControl      *segmentTripDirection;
    
    UIView                  *viewSearching;
    UILabel                 *lblSearchTitle, *lblSearchFrom, *lblSearchTo;
}

@property (strong, nonatomic) EntityTravelCustomFields *selectedCustomField;
@property BOOL editedDependentCustomField;
@property BOOL isDirty;
@property BOOL hideCustomFields; // Hide custom fields when coming from existing trip
@property (strong, nonatomic) NSMutableArray *tcfRows;
@property (strong, nonatomic) IBOutlet UIView                   *viewSearching;
@property (strong, nonatomic) IBOutlet UILabel                  *lblSearchTitle;
@property (strong, nonatomic) IBOutlet UILabel                  *lblSearchFrom;
@property (strong, nonatomic) IBOutlet UILabel                  *lblSearchTo;

@property (strong, nonatomic) IBOutlet UISegmentedControl      *segmentTripDirection;
@property (strong, nonatomic) IBOutlet UIView                  *viewSegmentHeader;
@property (nonatomic, strong) DateTimePopoverVC		*pickerPopOverVC;
@property (strong, nonatomic) IBOutlet UITableView		*tableList;
@property (strong, nonatomic) NSMutableArray			*aList;
@property (strong, nonatomic) NSMutableArray			*aSections;
@property BOOL isRoundTrip;
@property BOOL shouldReload;
@property (strong, nonatomic) TrainBooking				*trainBooking;
@property BOOL isCancelled;

@property (strong, nonatomic) NSMutableArray                    *taFields;  // TravelAuth fields for GOV

-(void)initTableData;
-(void)resetForRoundTrip;
-(void) searchBooking:(id)sender;

-(void)makeSearchButton;
-(void)makeCancelButton;

-(NSString *)formatDate:(NSDate *)dt ExtendedHour:(NSInteger)extendedHour;

-(UIView *)makeSelectButton;
-(void)cancelSearch:(id)sender;

+(NSDate *) getNSDateFromString:(NSString *)dt DateFormat:(NSString *)dateFormat;
+(UIBarButtonItem *)makeColoredButton:(UIColor *)btnColor W:(float)w H:(float)h Text:(NSString *)btnTitle Target:(id)target SelectorString:(NSString *)selectorString;

-(void)closeView:(id)sender;
- (void)pickerDateTapped:(id)sender IndexPath:(NSIndexPath *)indexPath;

#pragma mark -
#pragma mark Last Entity
-(EntityRail *) loadEntity;
-(void) saveEntity;
-(void) clearEntity:(EntityRail *) ent;
-(EntityRail *) makeNewEntity;

-(IBAction)setRoundTrip:(id)sender;
-(IBAction)setOneWay;
-(BookingCellData*) getBCD:(NSString *)bcdId;
-(void) showSearchingView:(NSString *) line1 line2:(NSString *)line2;

-(void) loadBCDFromEntity;

+(void) initBCDDate:(BookingCellData*)bcd withDate:(NSDate*)dateSource withTime:(NSNumber*) timeSource;
+(void) initReturnBCDDate:(BookingCellData*) bcdReturn withFromDate:(NSDate*) fromDate afterDays:(NSInteger) daysDiff;

+ (void) showTrainVC:(UINavigationController*)navi withTAFields:(NSArray*) taFlds;

@end
