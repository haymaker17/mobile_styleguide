//
//  CTELocation.h
//  ConcurSDK
//
//  Created by Pavan Adavi on 7/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTELocation : NSObject

@property  double latitude;
@property  double longitude;
@property (strong, nonatomic) NSString *city;
@property (strong, nonatomic) NSString *state;
@property (strong, nonatomic) NSString *country;
@property (strong, nonatomic) NSString *zipCode;
@property (strong, nonatomic) NSString *name;
@property (strong, nonatomic) NSString *location;
@property (strong, nonatomic) NSString *locationCode;
@property (strong, nonatomic) NSString *countryAbbrev;
@property (strong, nonatomic) NSString *iataCode;


@end
