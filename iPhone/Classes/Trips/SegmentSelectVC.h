//
//  SegmentSelectVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/18/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "TripData.h"
#import "TripsData.h"
#import "SegmentSelectCell.h"
#import "TripItExpenseTripData.h"
#import "iPadHomeVC.h"
#import "EntitySegment.h"

@interface SegmentSelectVC : MobileViewController <UITableViewDataSource, UITableViewDelegate, UIAlertViewDelegate>
{
    UITableView *tableList;
    TripsData					*tripsData;
	EntityTrip					*trip;
    NSArray                     *aSegments;
    NSMutableDictionary         *dictSelected;
}

@property (nonatomic, strong) IBOutlet UITableView      *tableList;
@property (nonatomic, strong) TripsData					*tripsData;
@property (nonatomic, strong) EntityTrip					*trip;
@property (nonatomic, strong) NSArray                     *aSegments;
@property (nonatomic, strong) NSMutableDictionary         *dictSelected;

-(void) configureCellAir:(SegmentSelectCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellCar:(SegmentSelectCell *)cell segment:(EntitySegment *)segment;
-(void)configureCellHotel:(SegmentSelectCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellRide:(SegmentSelectCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellRail:(SegmentSelectCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellParking:(SegmentSelectCell *)cell segment:(EntitySegment *)segment;

#pragma mark - Expense Trip
-(IBAction)expenseTrip:(id)sender;

#pragma mark - Close
-(IBAction)closeMe:(id)sender;
@end
