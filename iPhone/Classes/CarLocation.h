//
//  CarLocation.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CarLocation : NSObject
{
	NSString	*address1;
	NSString	*address2;
	NSString	*countryCode;
	NSString	*state;
	double		latitude;
	double		longitude;
	NSString	*locationCategory;
	NSString	*locationName;
	NSString	*phoneNumber;
	NSString	*iataCode;
	NSString	*chainCode;
    NSString    *moOpen;
    NSString    *moClose;
    NSString    *tuOpen;
    NSString    *tuClose;
    NSString    *weOpen;
    NSString    *weClose;
    NSString    *thOpen;
    NSString    *thClose;
    NSString    *frOpen;
    NSString    *frClose;
    NSString    *saOpen;
    NSString    *saClose;
    NSString    *suOpen;
    NSString    *suClose;
}

@property (nonatomic, strong) NSString	*address1;
@property (nonatomic, strong) NSString	*address2;
@property (nonatomic, strong) NSString	*countryCode;
@property (nonatomic, strong) NSString	*state;
@property (nonatomic) double			latitude;
@property (nonatomic) double			longitude;
@property (nonatomic, strong) NSString	*locationCategory;
@property (nonatomic, strong) NSString	*locationName;
@property (nonatomic, strong) NSString	*phoneNumber;
@property (nonatomic, strong) NSString	*iataCode;
@property (nonatomic, strong) NSString	*chainCode;
@property (nonatomic, strong) NSString	*moOpen;
@property (nonatomic, strong) NSString	*moClose;
@property (nonatomic, strong) NSString	*tuOpen;
@property (nonatomic, strong) NSString	*tuClose;
@property (nonatomic, strong) NSString	*weOpen;
@property (nonatomic, strong) NSString	*weClose;
@property (nonatomic, strong) NSString	*thOpen;
@property (nonatomic, strong) NSString	*thClose;
@property (nonatomic, strong) NSString	*frOpen;
@property (nonatomic, strong) NSString	*frClose;
@property (nonatomic, strong) NSString	*saOpen;
@property (nonatomic, strong) NSString	*saClose;
@property (nonatomic, strong) NSString	*suOpen;
@property (nonatomic, strong) NSString	*suClose;
@end
