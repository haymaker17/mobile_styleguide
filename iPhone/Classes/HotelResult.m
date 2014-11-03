//
//  HotelResult.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelResult.h"
#import "FormatUtils.h"
#import "HotelImageData.h"


@implementation HotelResult


@synthesize chainCode;
@synthesize chainName;
@synthesize	propertyId;
@synthesize	address1;
@synthesize	address2;
@synthesize	city;
@synthesize latitude;
@synthesize longitude;
@synthesize	distance;
@synthesize	distanceUnit;
@synthesize	hotel;
@synthesize	phone;
@synthesize	state;
@synthesize stateAbbrev;
@synthesize	tollfree;
@synthesize	zip;
@synthesize cheapestRoomRate;
@synthesize	cheapestRoomCurrencyCode;
@synthesize cheapestRoomWithViolationRate;
@synthesize cheapestRoomWithViolationCurrencyCode;
@synthesize starRating;
@synthesize propertyUri;
@synthesize propertyImagePairs;
@synthesize detail, hotelPrefRank;


@dynamic cheapestRoomRateWithOrWithoutViolation;
@dynamic cheapestRoomCurrencyCodeWithOrWithoutViolation;
@dynamic cheapestRoomRateAsFormattedString;

@dynamic starRatingAsterisks;

@dynamic propertyThumbnailUri;


-(id)init
{
	self = [super init];
	if (self)
    {
        self.propertyImagePairs = [[NSMutableArray alloc] initWithObjects:nil];
	}
	return self;
}

- (double)cheapestRoomRateWithOrWithoutViolation
{
	return (cheapestRoomRate != 0 ? cheapestRoomRate : cheapestRoomWithViolationRate);
}


- (NSString*)cheapestRoomCurrencyCodeWithOrWithoutViolation
{
	return (cheapestRoomCurrencyCode != nil ? cheapestRoomCurrencyCode : cheapestRoomWithViolationCurrencyCode);
}


- (NSString *)cheapestRoomRateAsFormattedString
{
	double amount = [self cheapestRoomRateWithOrWithoutViolation];
	NSString *crnCode = [self cheapestRoomCurrencyCodeWithOrWithoutViolation];
	return [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", amount] crnCode:crnCode];
}

- (NSString *)starRatingAsterisks
{
	NSString *asterisks = nil;	// No rating
	
	if (starRating != nil)
	{
		int starCount = [starRating intValue];
		if (starCount > 0)
		{
			NSString *allAsterisks = @"*****";
			asterisks = [allAsterisks substringToIndex:(starCount > 5 ? 5 : starCount)];
		}
	}
	
	return asterisks;
}

- (NSString *)propertyThumbnailUri
{
	if ([propertyImagePairs count] > 0)
	{
		HotelImageData *firstPropertyImagePair = propertyImagePairs[0];
		return firstPropertyImagePair.hotelThumbnail;
	}
	return nil;
}



@end
