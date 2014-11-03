//
//  FindCars.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"

@class CarSearchCriteria;
@class CarResult;
@class CarChain;
@class CarDescription;
@class CarLocation;
@class CarShop;
@class HotelViolation;


@interface FindCars : MsgResponder
{

	NSString				*currentElement;
	NSString				*path;
	CarSearchCriteria		*criteria;
	CarShop					*carShop;
	CarResult				*currentCarResult;
	CarChain				*currentCarChain;
	CarDescription			*currentCarDescription;
	CarLocation				*currentCarLocation;
	HotelViolation			*currentCarViolation;
}

@property (nonatomic, strong) NSString				*currentElement;
@property (nonatomic, strong) NSString				*path;
@property (nonatomic, strong) CarSearchCriteria		*criteria;
@property (nonatomic, strong) CarShop				*carShop;
@property (nonatomic, strong) CarResult				*currentCarResult;
@property (nonatomic, strong) CarChain				*currentCarChain;
@property (nonatomic, strong) CarDescription		*currentCarDescription;
@property (nonatomic, strong) CarLocation			*currentCarLocation;
@property (nonatomic, strong) HotelViolation		*currentCarViolation;

-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;
-(NSString *)makeXMLBody;


@end
