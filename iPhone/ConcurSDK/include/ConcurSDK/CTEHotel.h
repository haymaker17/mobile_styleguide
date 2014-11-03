//
//  CTEHotel.h
//  ConcurSDK
//
//  Created by ernest cho on 6/18/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTEHotel : NSObject

// basic search info
@property (nonatomic, readonly, strong) NSDate *checkInDate;
@property (nonatomic, readonly, strong) NSDate *checkOutDate;

// hotel information
@property (nonatomic, readonly, strong) NSString *propertyName;
@property (nonatomic, readonly, strong) NSString *addressLine1;
@property (nonatomic, readonly, strong) NSString *addressLine2;
@property (nonatomic, readonly, strong) NSString *street;
@property (nonatomic, readonly, strong) NSString *city;
@property (nonatomic, readonly, strong) NSString *state;
@property (nonatomic, readonly, strong) NSString *stateAbbreviation;
@property (nonatomic, readonly, strong) NSString *zipCode;
@property (nonatomic, readonly, strong) NSString *country;
@property (nonatomic, readonly, strong) NSString *countryCode;
@property (nonatomic, readonly, strong) NSString *phoneNumber;
@property (nonatomic, readonly, strong) NSString *tollFreePhoneNumber;
@property (nonatomic, readonly, strong) NSString *availabilityErrorCode;
@property (nonatomic, readonly, assign) NSInteger numberOfNights;
@property (nonatomic, readonly, strong) NSString *lowestRate;
@property (nonatomic, readonly, strong) NSString *currency;

// location information
@property (nonatomic, readonly, assign) double latitude;
@property (nonatomic, readonly, assign) double longitude;

// distance from search lat/long
@property (nonatomic, readonly, assign) double distance;
@property (nonatomic, readonly, assign) BOOL distanceInKM;

// preference data, this is only part of what's in the server response
// we should simplify the logic around preferences and recommendations
@property (nonatomic, readonly, assign) BOOL isPreferredChain;
@property (nonatomic, readonly, assign) BOOL isCompanyPreferredChain;
@property (nonatomic, readonly, assign) int preferenceType;
@property (nonatomic, readonly, assign) int starRating;

// recommendation
@property (nonatomic, readonly, strong) NSString *recommendationDescription;
@property (nonatomic, readonly, assign) double recommendationScore;

// hotel image urls
@property (nonatomic, readonly, strong) NSArray *images;
@property (nonatomic, readonly, strong) NSString *thumbnail;

// fetches rates, this might complete very quickly if the rates are already cached
// this allows the UI to choose when to lazy load the rate information
- (void)ratesWithCompletionBlock:(void (^)(NSArray *rates))completion;

// required to merge the initial hotel search results with subsequent poll results
@property (nonatomic, readonly, strong) NSString *propertyID;

// for unit tests
@property (nonatomic, readonly, strong) NSString *ratesURL;

@end