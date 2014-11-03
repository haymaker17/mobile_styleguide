//
//  CarViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseTableViewController.h"
#import "DateSpanDelegate.h"
#import "LocationDelegate.h"
#import "DateTimePopoverVC.h"
#import "iPadHomeVC.h"
#import "DateTimeOneDelegate.h"
#import "CarType.h"
#import "FieldEditDelegate.h"
@class CarSearchCriteria;
@class CarBookingTripData;


@interface CarViewController : BookingBaseTableViewController  <UITableViewDelegate, 
    UITableViewDataSource, 
    LocationDelegate, 
    DateTimeOneDelegate,
    DateTimePopoverDelegate,
    FieldEditDelegate,
    BoolEditDelegate>
{
	CarSearchCriteria	*carSearchCriteria;
	CarBookingTripData	*carBookingTripData;
	BOOL					isCancelled;
	DateTimePopoverVC		*pickerPopOverVC;
    
    UIView                   *viewSearching;
    UILabel                  *lblSearchTitle;
    UILabel                  *lblSearchFrom;
    UILabel                  *lblSearchTo;
}

@property (strong, nonatomic) EntityTravelCustomFields *selectedCustomField;
@property BOOL editedDependentCustomField;
@property BOOL isDirty;
@property BOOL hideCustomFields; // Hide custom fields when coming from existing trip
@property (strong, nonatomic) NSMutableArray *aSections;
@property (strong, nonatomic) NSMutableArray *tcfRows;
@property (strong, nonatomic) IBOutlet UIView                   *viewSearching;
@property (strong, nonatomic) IBOutlet UILabel                  *lblSearchTitle;
@property (strong, nonatomic) IBOutlet UILabel                  *lblSearchFrom;
@property (strong, nonatomic) IBOutlet UILabel                  *lblSearchTo;

@property (nonatomic, strong) CarSearchCriteria		*carSearchCriteria;
@property (nonatomic, strong) CarBookingTripData	*carBookingTripData;
@property (nonatomic) BOOL							isCancelled;
@property (nonatomic, strong) DateTimePopoverVC		*pickerPopOverVC;

@property (strong, nonatomic) NSMutableArray                    *taFields;  // TravelAuth fields for GOV

-(void)initializeCarSearchCriteria;

-(void)makeSearchButton;
-(IBAction)btnSearch:(id)sender;
-(void)stopSearch;
-(void)cancelSearch:(id)sender;

-(void)makeCancelButton;

-(NSDate*)dateFromString:(NSString*)string;

-(void)changePickupDate:(NSDate*)date timeInMinutes:(NSInteger)extHour;  // Called by CarDateTimeVC
-(void)changeDropoffDate:(NSDate*)date timeInMinutes:(NSInteger)extHour; // Called by CarDateTimeVC
-(void)closeView:(id)sender;
- (void)pickerDateTapped:(id)sender IndexPath:(NSIndexPath *)indexPath;

-(NSDate *)addDaysToDate:(NSDate *)dateDepart NumDaysToAdd:(int)daysToAdd;

+ (void) showCarVC:(UINavigationController*)navi withTAFields:(NSArray*) taFlds;

@end
