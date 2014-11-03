//
//  LocationResult.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface LocationResult : NSObject
{
	NSString				*latitude;
	NSString				*longitude;
	NSString				*location;
    NSString                *city;
    NSString                *state;
    NSString                *zipCode;
    NSString                *locationCode;
    NSString                *countryAbbrev; // Used for Flex Faring
}

@property (strong, nonatomic) NSString *latitude;
@property (strong, nonatomic) NSString *longitude;
@property (strong, nonatomic) NSString *location;
@property (strong, nonatomic) NSString *city;
@property (strong, nonatomic) NSString *state;
@property (strong, nonatomic) NSString *zipCode;
@property (strong, nonatomic) NSString *locationCode;
@property (strong, nonatomic) NSString *countryAbbrev;
@property (strong, nonatomic) NSString *iataCode;

@end
