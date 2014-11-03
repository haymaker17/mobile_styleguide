//
//  FindHotels.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "HotelResult.h"
#import "HotelSearch.h"
#import "HotelSearchCriteria.h"
#import "EntityHotelBooking.h"
#import "EntityHotelCheapRoom.h"
#import "EntityHotelImage.h"
#import "EntityHotelViolation.h"
#import "HotelViolation.h"
#import "HotelBookingManager.h"

@class HotelImageData;

@interface FindHotels : MsgResponder
{
	NSString				*currentElement;
	NSString				*pathRoot;
	NSString				*path;
	HotelSearch				*hotelSearch;
	HotelResult				*currentHotelResult;
	HotelImageData			*currentImagePair;
    NSMutableString         *buildString;
    EntityHotelCheapRoom    *cheapRoom;
    EntityHotelImage        *hotelImage;
    EntityHotelViolation    *hotelViolation;
    int totalCount;
}

@property int totalCount;
@property (nonatomic, strong) EntityHotelBooking      *hotelBooking;
@property (nonatomic, strong) EntityHotelBooking      *existingBooking;
@property (nonatomic, strong) EntityHotelCheapRoom    *cheapRoom;
@property (nonatomic, strong) EntityHotelImage        *hotelImage;
@property (nonatomic, strong) EntityHotelViolation    *hotelViolation;

@property (nonatomic, strong) NSMutableString         *buildString;
@property (nonatomic, strong) NSString				*currentElement;
@property (nonatomic, strong) NSString				*pathRoot;
@property (nonatomic, strong) NSString				*path;
@property (nonatomic, strong) HotelSearch			*hotelSearch;
@property (nonatomic, strong) HotelResult			*currentHotelResult;
@property (nonatomic, strong) HotelImageData		*currentImagePair;
@property (nonatomic) BOOL							inCheapestRoom;
@property (nonatomic) BOOL							inCheapestRoomWithViolation;
@property (nonatomic) BOOL                          inActionStatus;
@property (nonatomic, strong) NSString              *errorMessage;
@property (nonatomic, strong) NSString              *commonResponseCode;
@property (nonatomic, strong) NSString              *commonResponseSystemMessage;
@property (nonatomic, strong) NSString              *commonResponseUserMessage;
@property (nonatomic, strong) NSArray               *hotelBenchmarks;
@property (nonatomic, strong) NSString              *travelPointsInBank;



-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;
-(NSString *)makeXMLBody;
-(NSString *)makeXMLBody:(NSString*)startPos withCount:(NSString*)count;


@end
