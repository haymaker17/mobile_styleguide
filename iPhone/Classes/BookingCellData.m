//
//  BookingCellData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "BookingCellData.h"


@implementation BookingCellData
@synthesize lbl, val, val2, isDisclosure, isSegmented, isDetailLocation, cellID, values, stationArrive, stationDepart, dateValue, extendedTime;

-(id)init
{
    self = [super init];
    if (self)
    {
        values = [[NSMutableArray alloc] initWithObjects:nil];
        self.dateValue = [NSDate date];
        self.extendedTime = 1;
    }
	return self;
}


@end
