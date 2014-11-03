//
//  HomeTableViewCellDescription.h
//  ConcurMobile
//
//  Created by ernest cho on 12/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 All the home cell types.
 These are used by the Home9VC to decide what action to take when the cell is touched.

 We used to use constants, but they're hard to keep track of.
 This is also consistent with how iPad does this.
 */
typedef NS_ENUM(NSInteger, HomeTableViewCellType) {
    TravelRequestHomeCell,
    TripsHomeCell,
    FlightBookingHomeCell,
    HotelBookingHomeCell,
    CarBookingHomeCell,
    RailBookingHomeCell,
    ExpensesHomeCell,
    ExpenseReportsHomeCell,
    ApprovalsHomeCell
};

@interface HomeTableViewCellDescription : NSObject

// cell layout
@property (nonatomic, readwrite, strong) NSString *icon;

// cell data
@property (nonatomic, readwrite, assign) HomeTableViewCellType cellType;
@property (nonatomic, readwrite, strong) NSString *label;
@property (nonatomic, readwrite, strong) NSString *sublabel;
@property (nonatomic, readwrite, strong) NSNumber *count;
@property (nonatomic, readwrite, assign) BOOL disabled;

@end
