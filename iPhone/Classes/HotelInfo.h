//
//  HotelInfo.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class RoomResult;
@class HotelFee;
@class HotelDetail;

@interface HotelInfo : NSObject
{
	NSMutableArray			*roomResults;
	NSNumber				*selectedRoomIndex;
	NSMutableArray			*hotelFees;
	NSMutableArray			*hotelDetails;
}

@property (nonatomic, strong) NSMutableArray		*roomResults;
@property (nonatomic, strong) NSNumber				*selectedRoomIndex;
@property (nonatomic, strong) NSMutableArray		*hotelFees;
@property (nonatomic, strong) NSMutableArray		*hotelDetails;
@property (weak, nonatomic, readonly) RoomResult			*selectedRoom;

-(void)selectRoom:(NSUInteger)roomIndex;

@end
