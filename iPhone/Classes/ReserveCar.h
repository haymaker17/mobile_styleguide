//
//  ReserveCar.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"

@class CarReservationRequest;
@class CarReservationResponse;


@interface ReserveCar : MsgResponder
{
	NSString					*currentElement;
	NSString					*pathRoot;
	NSString					*path;
	CarReservationRequest		*carReservationRequest;
	CarReservationResponse		*carReservationResponse;
}


@property (nonatomic, strong) NSString					*currentElement;
@property (nonatomic, strong) NSString					*pathRoot;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) CarReservationRequest		*carReservationRequest;
@property (nonatomic, strong) CarReservationResponse	*carReservationResponse;


-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;
-(NSString *)makeXMLBody:(NSMutableDictionary *)pBag;


@end
