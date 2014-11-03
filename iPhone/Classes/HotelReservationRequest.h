//
//  HotelReservationRequest.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface HotelReservationRequest : NSObject

@property (nonatomic, strong) NSString	*bicCode;
@property (nonatomic, strong) NSString	*creditCardId;
@property (nonatomic, strong) NSString	*hotelChainCode;
@property (nonatomic, strong) NSString	*propertyId;
@property (nonatomic, strong) NSString	*propertyName;
@property (nonatomic, strong) NSString	*sellSource;
@property (nonatomic, strong) NSString	*tripKey;
@property (nonatomic, strong) NSString	*violationCode;
@property (nonatomic, strong) NSString	*violationJustification;
@property (nonatomic) BOOL isUsingTravelPointsAgainstViolations;

@end
