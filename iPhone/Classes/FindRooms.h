//
//  FindRooms.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "EntityHotelBooking.h"
#import "EntityHotelDetail.h"
#import "EntityHotelRoom.h"
#import "EntityHotelFee.h"
#import "EntityHotelViolation.h"
#import "HotelBookingManager.h"

@class RoomResult;
@class HotelFee;
@class HotelDetail;
@class HotelViolation;
@class HotelSearch;

@interface FindRooms : MsgResponder
{
	NSString				*currentElement;
	NSString				*path;
	NSMutableString			*buildString;
	RoomResult				*currentRoomResult;
	HotelFee				*currentHotelFee;
	HotelDetail				*currentHotelDetail;
	HotelViolation			*currentHotelViolation;
	HotelSearch				*hotelSearch;
    EntityHotelBooking      *hotelBooking;
    EntityHotelDetail       *hotelDetail;
    EntityHotelFee          *hotelFee;
    EntityHotelRoom         *hotelRoom;
    EntityHotelViolation    *hotelViolation;
}

@property (nonatomic, strong) EntityHotelBooking    *hotelBooking;
@property (nonatomic, strong) EntityHotelDetail       *hotelDetail;
@property (nonatomic, strong) EntityHotelFee          *hotelFee;
@property (nonatomic, strong) EntityHotelRoom         *hotelRoom;
@property (nonatomic, strong) EntityHotelViolation    *hotelViolation;
@property (nonatomic, strong) NSString				*currentElement;
@property (nonatomic, strong) NSString				*path;
@property (nonatomic, strong) NSMutableString		*buildString;
@property (nonatomic, strong) RoomResult			*currentRoomResult;
@property (nonatomic, strong) HotelFee				*currentHotelFee;
@property (nonatomic, strong) HotelDetail			*currentHotelDetail;
@property (nonatomic, strong) HotelViolation		*currentHotelViolation;
@property (nonatomic, strong) HotelSearch			*hotelSearch;

-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;


@end
