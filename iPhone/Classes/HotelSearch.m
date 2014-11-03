//
//  HotelSearch.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/27/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelSearch.h"
#import "HotelSearchCriteria.h"
#import "HotelResult.h"

@implementation HotelSearch

@synthesize hotelSearchCriteria;
@synthesize hotels;
@synthesize selectedHotelIndex;
@synthesize tripKey;
@synthesize pollingID;
@synthesize isFinal;
@synthesize isPolling;
@synthesize ratesFound;
@synthesize searchStartTime;

@dynamic selectedHotel;

-(id)init
{
	self = [super init];
	if (self)
    {
        self.hotelSearchCriteria = [[HotelSearchCriteria alloc] init];
	
        self.hotels = [[NSMutableArray alloc] initWithObjects: nil];
        self.tripKey = nil;
        self.isPolling = [[ExSystem sharedInstance] siteSettingHotelStreamingEnabled];
        self.searchStartTime = CACurrentMediaTime();
	}
	return self;
}

- (HotelResult*)selectedHotel
{
    __autoreleasing HotelResult* result = nil;
	if (selectedHotelIndex == nil)
	{
        HotelResult *hotel =  [[HotelResult alloc] init];
        [hotels addObject:hotel];
        self.selectedHotelIndex = @0;
		result = hotels[[selectedHotelIndex intValue]]; //return nil;
	}
	else
	{
		result = hotels[[selectedHotelIndex intValue]];
	}
    return result;
}

-(void)selectHotel:(NSUInteger)hotelIndex
{
	if (hotelIndex > [hotels count])
		NSLog(@"ERROR: Setting hotelIndex to out-of-bounds value: %i.  Valid values are 0 through %i", hotelIndex, [hotels count]);
	
	self.selectedHotelIndex = [NSNumber numberWithInt:hotelIndex];
}


@end
