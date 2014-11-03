//
//  Car.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@class CarShop;
@class CarResult;
@class CarChain;
@class CarDescription;
@class CarLocation;

@interface Car : NSObject
{
}

@property (weak, nonatomic, readonly) NSString	*carId;
@property (weak, nonatomic, readonly) NSString	*currencyCode;
@property (nonatomic, readonly) double		dailyRate;
@property (nonatomic, readonly) double		totalRate;
@property (nonatomic, readonly) bool        sendCreditCard;
@property (weak, nonatomic, readonly) NSString	*freeMiles;
@property (weak, nonatomic, readonly) NSArray		*violations;
@property (weak, nonatomic, readonly) NSString	*chainName;
@property (weak, nonatomic, readonly)	NSString	*chainLogoUri;
@property (weak, nonatomic, readonly) NSString	*imageUri;
@property (weak, nonatomic, readonly) NSString	*carClass;
@property (weak, nonatomic, readonly) NSString	*carBody;
@property (weak, nonatomic, readonly) NSString	*carTrans;
@property (weak, nonatomic, readonly) NSString	*carAC;

@property (weak, nonatomic, readonly) NSDate		*pickupDate;
@property (weak, nonatomic, readonly) NSDate		*dropoffDate;

@property (weak, nonatomic, readonly) NSString	*pickupLocationName;
@property (weak, nonatomic, readonly) NSString	*dropoffLocationName;

@property (nonatomic, readonly) double		pickupLocationLatitude;
@property (nonatomic, readonly) double		pickupLocationLongitude;

@property (weak, nonatomic, readonly) NSString	*pickupLocationAddress;
@property (weak, nonatomic, readonly) NSString	*pickupLocationPhoneNumber;

@property (nonatomic, readonly) NSNumber     *maxEnforcementLevel;
@property (nonatomic, readonly) NSString     *gdsName;

// The following properties are set during parsing:
@property (nonatomic, strong) CarShop			*carShop;
@property (nonatomic, strong) CarResult			*carResult;
@property (nonatomic, strong) CarChain			*carChain;
@property (nonatomic, strong) CarDescription	*carDescription;
@property (nonatomic, strong) CarLocation		*carPickupLocation;

-(NSString*)locationNameFromIata:(NSString*)iata chainCode:(NSString *)chainCode;
-(NSString*)locationPhoneFromIata:(NSString*)iata chainCode:(NSString *)chainCode;

@end
