//
//  BookingCellData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TrainStationData.h"

@interface BookingCellData : NSObject {
	NSString		*lbl, *val, *val2, *cellID;
	BOOL			isDisclosure, isDetailDate, isDetailSearch, isDetailLocation, isDetailTime, isDetailDateTime, isDetailNumber;
	BOOL			isSegmented;
	UIImage			*imgRow;
	UIColor			*labelColor, *valueColor;
	NSMutableArray	*values;
	TrainStationData	*stationDepart, *stationArrive;
	NSDate			*dateValue;
	NSInteger		extendedTime;
}

@property (strong, nonatomic) NSString		*lbl;
@property (strong, nonatomic) NSString		*val;
@property (strong, nonatomic) NSString		*val2;
@property (strong, nonatomic) NSString		*cellID;
@property (strong, nonatomic) NSMutableArray	*values;
@property (strong, nonatomic) TrainStationData	*stationDepart;
@property (strong, nonatomic) TrainStationData	*stationArrive;


@property (strong, nonatomic) NSDate			*dateValue;
@property NSInteger								extendedTime;

@property BOOL isDisclosure;
@property BOOL isDetailLocation;
@property BOOL isSegmented;


-(id)init;

@end
