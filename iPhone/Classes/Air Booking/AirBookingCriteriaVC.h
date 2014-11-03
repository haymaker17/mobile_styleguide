//
//  AirBookingCriteriaVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/4/11.
//  Copyright 2011 Concur. All rights reserved.
//
#import "MobileViewController.h"
#import "FindMe.h"
#import "TrainBooking.h"
#import "AmtrakShop.h"
#import "iPadHomeVC.h"
#import "DateTimePopoverVC.h"
#import "EntityAirCriteria.h"
#import "BookingCellData.h"
#import "LocationDelegate.h"
#import "AirShop.h"
#import "AirlineEntry.h"
#import "EntityAirShopResults.h"
#import "AirShopResultsManager.h"
#import "AirShopResultsVC.h"
#import "CreditCard.h"
#import "BoolEditDelegate.h"
#import "DateTimeOneDelegate.h"
#import "TravelCustomFieldsManager.h"
#import "FieldEditDelegate.h"

@interface AirBookingCriteriaVC : MobileViewController <UITableViewDelegate, UITableViewDataSource, BoolEditDelegate, DateTimeOneDelegate, FieldEditDelegate, DateTimePopoverDelegate, LocationDelegate> {
	NSMutableArray			*aSections;
	NSMutableArray	*aList;
	UITableView		*tableList;
	BOOL			isRoundTrip, shouldReload, isCancelled;
	TrainBooking	*trainBooking;
	iPadHomeVC		*iPadHome;
	DateTimePopoverVC		*pickerPopOverVC;
    BookingCellData *returnDateBCD;
    
    //new
    UIView                  *viewSegmentHeader;
    UISegmentedControl      *segmentTripDirection;
    
    UIView                  *viewSearching;
    UILabel                 *lblSearchTitle, *lblSearchFrom, *lblSearchTo;
    NSMutableArray          *aClass;
    NSMutableDictionary     *dictClass;
    
    NSMutableArray          *tcfRows;
}

@property BOOL checkboxDefault;
@property BOOL showCheckbox;
@property BOOL displayedRefundableInfo;
@property BOOL displayedClassOfService;
@property (strong, nonatomic) EntityTravelCustomFields *selectedCustomField;
@property BOOL editedDependentCustomField;
@property BOOL isDirty;
@property BOOL hideCustomFields; // Hide custom fields when coming from existing trip
@property (strong, nonatomic) IBOutlet UIView                   *viewSearching;
@property (strong, nonatomic) IBOutlet UILabel                  *lblSearchTitle;
@property (strong, nonatomic) IBOutlet UILabel                  *lblSearchFrom;
@property (strong, nonatomic) IBOutlet UILabel                  *lblSearchTo;

@property (strong, nonatomic) NSMutableArray          *aClass;
@property (strong, nonatomic) NSMutableDictionary     *dictClass;

@property (strong, nonatomic) IBOutlet UISegmentedControl      *segmentTripDirection;
@property (strong, nonatomic) IBOutlet UIView                  *viewSegmentHeader;
@property (nonatomic, strong) DateTimePopoverVC         *pickerPopOverVC;
@property (strong, nonatomic) IBOutlet UITableView		*tableList;
@property (strong, nonatomic) NSMutableArray			*aList;
@property (strong, nonatomic) NSMutableArray			*aSections;
@property (strong, nonatomic) BookingCellData           *returnDateBCD;
@property BOOL isRoundTrip;
@property BOOL shouldReload;
@property (strong, nonatomic) TrainBooking				*trainBooking;
@property BOOL isCancelled;
@property (nonatomic, strong) NSMutableArray          *tcfRows;
@property (strong, nonatomic) NSMutableArray          *taFields;  // TravelAuth fields for GOV

-(void)initTableData;
-(void)createTableData;
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
-(EntityAirCriteria *) loadEntity;
-(void) saveEntity;
-(void) clearEntity:(EntityAirCriteria *) ent;
-(EntityAirCriteria *) makeNewEntity;

-(IBAction)setRoundTrip:(id)sender;
-(IBAction)setOneWay;
-(BookingCellData*) getBCD:(NSString *)bcdId;
-(void) showSearchingView:(NSString *) line1 line2:(NSString *)line2;

-(void) loadBCDFromEntity;

-(void) dumpResultsIntoEntity:(AirShop*) airShop;

#pragma mark - Class Stuff
-(BOOL) fillClass:(NSArray*) array;

-(NSDate *)addDaysToDate:(NSDate *)dateDepart NumDaysToAdd:(int)daysToAdd;
-(BOOL) canContinue;

#pragma mark - editor callbacks
-(void) boolUpdated:(NSObject*) context withValue:(BOOL) val;
-(void) dateSelected:(NSObject*) context withDate:(NSDate*) date;
- (void)pickedDate:(NSDate *)dateSelected;

+ (void) showAirVC:(UINavigationController*)navi withTAFields:(NSArray*) taFlds;

#pragma mark - Evature
- (IBAction)showEvaSearchUI:(id)sender;
@property (strong, nonatomic) IBOutlet UIView *EvaBtnView;

@end
