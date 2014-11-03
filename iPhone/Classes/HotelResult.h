//
//  HotelResult.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HotelInfo.h"

@interface HotelResult : NSObject
{
	NSString		*chainCode;
	NSString		*chainName;
	NSString		*propertyId;
	NSString		*address1;
	NSString		*address2;
	NSString		*city;
	double			latitude;
	double			longitude;
	NSString		*distance;
	NSString		*distanceUnit;
	NSString		*hotel;
	NSString		*phone;
	NSString		*state;
	NSString		*stateAbbrev;
	NSString		*tollfree;
	NSString		*zip;
	double			cheapestRoomRate;
	NSString		*cheapestRoomCurrencyCode;
	double			cheapestRoomWithViolationRate;
	NSString		*cheapestRoomWithViolationCurrencyCode;
	NSString		*starRating;
	NSString		*propertyUri;
	NSString		*propertyThumbnailUri;
	NSMutableArray	*propertyImagePairs;
	HotelInfo		*detail;
    int             hotelPrefRank;
}
@property int             hotelPrefRank;
@property (strong, nonatomic) NSString			*chainCode;
@property (strong, nonatomic) NSString			*chainName;
@property (strong, nonatomic) NSString			*propertyId;
@property (strong, nonatomic) NSString			*address1;
@property (strong, nonatomic) NSString			*address2;
@property (strong, nonatomic) NSString			*city;
@property (nonatomic) double					latitude;
@property (nonatomic) double					longitude;
@property (strong, nonatomic) NSString			*distance;
@property (strong, nonatomic) NSString			*distanceUnit;
@property (strong, nonatomic) NSString			*hotel;
@property (strong, nonatomic) NSString			*phone;
@property (strong, nonatomic) NSString			*state;
@property (strong, nonatomic) NSString			*stateAbbrev;
@property (strong, nonatomic) NSString			*tollfree;
@property (strong, nonatomic) NSString			*zip;
@property (nonatomic) double					cheapestRoomRate;
@property (strong, nonatomic) NSString			*cheapestRoomCurrencyCode;
@property (nonatomic) double					cheapestRoomWithViolationRate;
@property (strong, nonatomic) NSString			*cheapestRoomWithViolationCurrencyCode;
@property (strong, nonatomic) NSString			*starRating;
@property (strong, nonatomic) NSString			*propertyUri;
@property (strong, nonatomic) NSMutableArray	*propertyImagePairs;
@property (strong, nonatomic) HotelInfo			*detail;

@property (readonly, nonatomic) double			cheapestRoomRateWithOrWithoutViolation;
@property (weak, readonly, nonatomic) NSString		*cheapestRoomCurrencyCodeWithOrWithoutViolation;
@property (weak, readonly, nonatomic) NSString		*cheapestRoomRateAsFormattedString;

@property (weak, readonly, nonatomic) NSString		*starRatingAsterisks;

@property (weak, readonly, nonatomic) NSString		*propertyThumbnailUri;



@end
