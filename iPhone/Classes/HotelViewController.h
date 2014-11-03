//
//  HotelViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseTableViewController.h"
#import "DateSpanDelegate.h"
#import "LocationDelegate.h"
#import "DateTimePopoverVC.h"
#import "iPadHomeVC.h"
#import "EntityHotelBooking.h"
#import "HotelBookingManager.h"
#import "FieldEditDelegate.h"
@class HotelSearch;
@class LocationResult;

@interface HotelViewController : BookingBaseTableViewController <UITableViewDelegate, UITableViewDataSource, DateSpanDelegate, LocationDelegate, DateTimePopoverDelegate,
    BoolEditDelegate,
    FieldEditDelegate
    >
{
	HotelSearch				*hotelSearch;
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
@property (strong, nonatomic) NSMutableArray          *aSections;
@property (strong, nonatomic) NSMutableArray          *tcfRows;
@property (strong, nonatomic) NSMutableArray          *taFields;  // TravelAuth fields for GOV

@property (strong, nonatomic) IBOutlet UIView                   *viewSearching;
@property (strong, nonatomic) IBOutlet UILabel                  *lblSearchTitle;
@property (strong, nonatomic) IBOutlet UILabel                  *lblSearchFrom;
@property (strong, nonatomic) IBOutlet UILabel                  *lblSearchTo;

@property (strong, nonatomic) HotelSearch	*hotelSearch;
@property (nonatomic) BOOL					isCancelled;
@property (nonatomic, strong) DateTimePopoverVC		*pickerPopOverVC;

@property (weak, nonatomic) IBOutlet UIButton *btnEvaSearch;
- (IBAction)ShowEvaSearchUI:(id)sender;
@property (weak, nonatomic) IBOutlet UIView *EvaBtnView;

-(void)initializeHotelSearch;
-(void)makeSearchButton;
-(IBAction)btnSearch:(id)sender;
-(void)cancelSearch:(id)sender;
-(void)makeCancelButton;


-(void)closeView:(id)sender;
- (void)pickerDateTapped:(id)sender IndexPath:(NSIndexPath *)indexPath;
- (void)pickerTimeTapped:(id)sender IndexPath:(NSIndexPath *)indexPath;
-(NSDate *)addDaysToDate:(NSDate *)dateDepart NumDaysToAdd:(int)daysToAdd;

+ (void) showHotelVC:(UINavigationController*)navi withTAFields:(NSArray*) taFlds;

@end
