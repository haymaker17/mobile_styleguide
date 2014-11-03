//
//  CarListViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseTableViewController.h"
#import "CarRateCell.h"

@class CarListCell;
@class CarBookingTripData;
@class CarSearchCriteria;

@interface CarListViewController : BookingBaseTableViewController<UITableViewDelegate, UITableViewDataSource, UIActionSheetDelegate>
{
}

@property (nonatomic, strong) NSMutableArray		*vendors;
@property (nonatomic, strong) NSArray				*carsInOriginalOrder;
@property (nonatomic) NSUInteger					sortOrder;
@property (nonatomic, strong) CarBookingTripData	*carBookingTripData;
@property (nonatomic, strong) CarSearchCriteria		*carSearchCriteria;

@property (nonatomic, strong) IBOutlet UILabel             *lblHeading;
@property (nonatomic, strong) IBOutlet UILabel             *lblSubheading;

@property (strong, nonatomic) NSMutableArray          *taFields;  // TravelAuth fields for GOV

- (void)sortCars;
- (void)sortCarsByVendorNames:(NSMutableArray *)cars;
- (void)sortCarsByPrice:(NSMutableArray *)cars;

- (void)populateVendors:(NSArray*)cars combineAllVendors:(BOOL)combineAllVendors;

+ (NSString *)shortenLocation:(NSString *)location;
+ (UIBarButtonItem *)makeSearchCriteriaButton:(CarSearchCriteria*)carSearchCriteria;
- (void)makeToolbar;

- (CarRateCell*)makeCarListCell;

- (UIBarButtonItem *)makeSearchResultsButton:(CarSearchCriteria*)carSearchCriteria;
@end
