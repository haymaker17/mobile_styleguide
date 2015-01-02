//
//  HotelInfo.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelInfo.h"
#import "RoomResult.h"


@implementation HotelInfo

@synthesize roomResults;
@synthesize selectedRoomIndex;
@synthesize hotelFees;
@synthesize hotelDetails;

@dynamic selectedRoom;

- (RoomResult*)selectedRoom
{
	if (selectedRoomIndex == nil)
	{
		return nil;
	}
	else
	{
		return roomResults[[selectedRoomIndex intValue]];
	}
}

-(void)selectRoom:(NSUInteger)roomIndex
{
	if (roomIndex > [roomResults count])
		NSLog(@"ERROR: Setting roomIndex to out-of-bounds value: %ld.  Valid values are 0 through %lu", (unsigned long)roomIndex, (unsigned long)[roomResults count]);
	
	self.selectedRoomIndex = [NSNumber numberWithInteger:roomIndex];
}

-(id)init
{
	self = [super init];
	if (self)
    {
        self.roomResults = [[NSMutableArray alloc] initWithObjects:nil];	// Retain count = 2
         
        self.hotelFees = [[NSMutableArray alloc] initWithObjects:nil];	// Retain count = 2
        
        self.hotelDetails = [[NSMutableArray alloc] initWithObjects:nil];	// Retain count = 2

    }
	return self;
}


@end
