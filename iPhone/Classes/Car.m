//
//  Car.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "Car.h"
#import "CarShop.h"
#import "CarResult.h"
#import "CarChain.h"
#import "CarDescription.h"
#import "CarLocation.h"
#import "HotelViolation.h"

@implementation Car

@dynamic carId;
@dynamic currencyCode;
@dynamic dailyRate;
@dynamic totalRate;
@dynamic sendCreditCard;
@dynamic freeMiles;
@dynamic violations;
@dynamic chainName;
@dynamic chainLogoUri;
@dynamic carClass;
@dynamic carBody;
@dynamic carTrans;
@dynamic carAC;
@dynamic pickupDate;
@dynamic pickupLocationName;
@dynamic dropoffDate;
@dynamic dropoffLocationName;
@dynamic pickupLocationLatitude;
@dynamic pickupLocationLongitude;
@dynamic pickupLocationAddress;
@dynamic pickupLocationPhoneNumber;
@dynamic maxEnforcementLevel;

-(NSString*)carId
{
	return self.carResult.carId;
}

-(NSString*)currencyCode
{
	return self.carResult.currencyCode;
}

-(double)dailyRate
{
	return self.carResult.dailyRate;
}

-(double)totalRate
{
	return self.carResult.totalRate;
}

-(bool)sendCreditCard
{
    return self.carResult.sendCreditCard;
}

-(NSString*)freeMiles
{
//	NSString *mileage = carResult.freeMiles;
	
//	if ([mileage isEqualToString:@"UNL"])
//	{
//		return @"Unlimited miles";
//	}
	
	return self.carResult.freeMiles;
}

-(NSArray*)violations
{
	return self.carResult.violations;
}

-(NSString*)chainName
{
	return self.carChain.name;
}

-(NSString*)chainLogoUri
{
	return self.carChain.imageUri;
}

-(NSString*)imageUri
{
	return self.carResult.imageUri;
}

-(NSString*)carClass
{
	return self.carDescription.carClass;
}

-(NSString*)carBody
{
	return self.carDescription.carBody;
}

-(NSString*)carTrans
{
	return self.carDescription.carTrans;
}

-(NSString*)carAC
{
	return self.carDescription.carAC;
}

-(NSDate*)pickupDate
{
	return self.carShop.pickupDate;
}

-(NSString*)pickupLocationName
{
    NSString *chainCode = nil;
    for(NSString *key in self.carShop.carLocations)
    {
        CarLocation* carLocation = (self.carShop.carLocations)[key];
        chainCode = [carLocation.chainCode uppercaseString];
        break;
    }
    //NSLog(@"carShop.pickupIata %@", carShop.pickupIata);
	NSString* locationName = [self locationNameFromIata:self.carShop.pickupIata chainCode:chainCode];
	return (locationName != nil ? locationName : self.carShop.pickupIata);
}

-(NSDate*)dropoffDate
{
	return self.carShop.dropoffDate;
}

-(NSString*)dropoffLocationName
{
    NSString *chainCode = nil;
    for(CarLocation* carLocation in self.carShop.carLocations)
    {
        chainCode = carLocation.chainCode;
        break;
    }
    
	NSString* locationName = [self locationNameFromIata:self.carShop.dropoffIata chainCode:chainCode];
	return (locationName != nil ? locationName : self.carShop.dropoffIata);
}

-(NSString*)locationNameFromIata:(NSString*)iata chainCode:(NSString *)chainCode
{
	if (iata != nil)
	{
//        NSString *key = [NSString stringWithFormat:@"%@-%@", iata, [chainCode uppercaseString]];
        //NSLog(@"locationNameFromIata: car key = %@", key);
		CarLocation* carLocation = (self.carShop.carLocations)[[NSString stringWithFormat:@"%@-%@", iata, chainCode]];
		if (carLocation != nil)
		{
			return carLocation.locationName;
		}
	}
	return nil;
}

-(NSString*)locationPhoneFromIata:(NSString*)iata chainCode:(NSString *)chainCode
{
	if (iata != nil)
	{
//        NSString *key = [NSString stringWithFormat:@"%@-%@", iata, [chainCode uppercaseString]];
        //NSLog(@"locationNameFromIata: car key = %@", key);
		CarLocation* carLocation = (self.carShop.carLocations)[[NSString stringWithFormat:@"%@-%@", iata, chainCode]];
		if (carLocation != nil)
		{
			return carLocation.phoneNumber;
		}
	}
	return nil;
}

-(double)pickupLocationLatitude
{
	return self.carPickupLocation.latitude;
}

-(double)pickupLocationLongitude
{
	return self.carPickupLocation.longitude;
}

-(NSString*)pickupLocationAddress
{
	return self.carPickupLocation.address1;
}

-(NSString*)pickupLocationPhoneNumber
{
    NSString *chainCode = [self.carChain.code uppercaseString];
	NSString* phoneNum = [self locationPhoneFromIata:self.carShop.pickupIata chainCode:chainCode];
	return phoneNum; // carPickupLocation.phoneNumber;
}

-(NSNumber*) maxEnforcementLevel
{
    return self.carResult.maxEnforcementLevel;
}

-(NSString*)gdsName
{
    return self.carResult.gdsName;
}




@end
