//
//  TripSegments.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SegmentData.h"
#import "ExSystem.h" 

#import "Detail.h"
#import "TripDetailCell.h"

@interface TripSegments : NSObject 
{

}

extern NSString * const ITIN_DETAILS_VIEW;

-(void) fillHotelCell:(UITableViewCell *)cell Segment:(EntitySegment *)segment RVC:(RootViewController *)rootViewController;
-(NSMutableArray *) makeTripDetails:(NSString *)segmentType Segment:(EntitySegment *)segment RVC:(RootViewController *)rootViewController;
-(NSMutableArray *) makeHotelDetails:(EntitySegment *)segment RVC:(RootViewController *)rootViewController;

-(UILabel *) makeDetailValue:(NSString *) lblText YPos:(float)yPos XPos:(float)xPos CellWidth:(float)cellW;
-(UILabel *) makeDetailLabel:(NSString *) lblText YPos:(float)yPos XPos:(float)xPos;
-(float) getHotelCellH:(EntitySegment *)segment RVC:(RootViewController *)rootViewController;

-(void) fillAirCell:(UITableViewCell *)cell Segment:(EntitySegment *)segment RVC:(RootViewController *)rootViewController;
-(float) getAirCellH:(EntitySegment *)segment RVC:(RootViewController *)rootViewController;
-(NSMutableArray *) makeAirDetails:(EntitySegment *)segment RVC:(RootViewController *)rootViewController;

-(UIButton *) makeDetailValueButton:(NSString *) lblText YPos:(float)yPos XPos:(float)xPos CellWidth:(float)cellW Det:(Detail *)detail  TripCell:(TripDetailCell *)cell;

-(NSMutableArray *) makeCarDetails:(EntitySegment *)segment RVC:(RootViewController *)rootViewController;
-(NSMutableArray *) makeRideDetails:(EntitySegment *)segment RVC:(RootViewController *)rootViewController;
-(NSMutableArray *) makeDiningDetails:(EntitySegment *)segment RVC:(RootViewController *)rootViewController;
-(NSMutableArray *) makeRailDetails:(EntitySegment *)segment RVC:(RootViewController *)rootViewController;

-(NSString *)formatDuration:(int)duration;
@end
