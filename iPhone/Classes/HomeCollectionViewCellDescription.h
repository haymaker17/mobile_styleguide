//
//  HomeCollectionViewCellDescription.h
//  ConcurHomeCollectionView
//
//  Created by ernest cho on 11/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 All the home cell types.  
 These are used by the HomeCollectionView to decide what action to take when the cell is touched.
 
 I could have used the iconName or label, but I feel double tasking a variable leads to problems later.
 */
typedef NS_ENUM(NSInteger, HomeCollectionViewCellType) {
    TripsHomeCell,
    FlightBookingHomeCell,
    HotelBookingHomeCell,
    CarBookingHomeCell,
    RailBookingHomeCell,
    ExpensesHomeCell,
    ExpenseReportsHomeCell,
    ApprovalsHomeCell,
    GovAuthorizationHomeCell,
    GovVoucherHomeCell,
    GovStampDocumentCell,
};

/**
 All the home cell styles.  The size and element layout varies.
 These are used by the HomeCollectionView to decide which layout to use to display the Cell data.
 */
typedef NS_ENUM(NSInteger, HomeCollectionViewCellStyle) {
    HomeCellDefaultLandscape,
    HomeCellDefaultPortrait,
    HomeCellTravelOnlyTripsLandscape,
    HomeCellTravelOnlyBookingLandscape,
    HomeCellTravelOnlyTripsPortrait,
    HomeCellTravelOnlyBookingPortrait,
    HomeCellExpenseOnlyLandscape,
    HomeCellExpenseOnlyPortrait,
    HomeCellApprovalOnlyLandscape,
    HomeCellApprovalOnlyPortrait,
    HomeCellTravelAndApprovalLandscape,
    HomeCellExpenseAndTravelOnlyTripsLandscape,
    HomeCellExpenseAndTravelOnlyTripsPortrait,
    HomeCellGovOnlyDocumentsLandscape,
    HomeCellGovOnlyDocumentsPortrait
};

/**
 Home screen cell description, data only
 */
@interface HomeCollectionViewCellDescription : NSObject

// cell layout
@property (nonatomic, readwrite, assign) HomeCollectionViewCellStyle cellStylePortrait;
@property (nonatomic, readwrite, assign) HomeCollectionViewCellStyle cellStyleLandscape;
@property (nonatomic, readwrite, strong) NSString *icon;

// cell data
@property (nonatomic, readwrite, assign) HomeCollectionViewCellType cellType;
@property (nonatomic, readwrite, strong) NSString *topLabel;
@property (nonatomic, readwrite, strong) NSString *label;
@property (nonatomic, readwrite, strong) NSString *sublabel;
@property (nonatomic, readwrite, strong) NSNumber *count;
@property (nonatomic, readwrite, assign) BOOL disabled;

@end
