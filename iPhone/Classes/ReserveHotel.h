//
//  ReserveHotel.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"

@class HotelReservationRequest;
@class HotelReservationResponse;


@interface ReserveHotel : MsgResponder
{
	NSString					*currentElement;
	NSString					*pathRoot;
	NSString					*path;
	HotelReservationRequest		*hotelReservationRequest;
	HotelReservationResponse	*hotelReservationResponse;
    NSMutableString             *buildString;
}

@property (strong, nonatomic) NSMutableString           *buildString;
@property (nonatomic, strong) NSString					*currentElement;
@property (nonatomic, strong) NSString					*pathRoot;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) HotelReservationRequest	*hotelReservationRequest;
@property (nonatomic, strong) HotelReservationResponse	*hotelReservationResponse;

-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;
-(NSString *)makeXMLBody:(NSMutableDictionary *)pBag;


@end
