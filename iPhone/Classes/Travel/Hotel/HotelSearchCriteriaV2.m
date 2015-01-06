//
//  HotelSearchCriteriaV2.m
//  ConcurMobile
//
//  Created by Sally Yan on 7/21/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

/**
 HotelSearchCriteriaV2 has the similar functionality as HotelSearchCriteria. 
 This class is being used for the new hotel search instead of HotelSearchCriteria.
 */

#import "HotelSearchCriteriaV2.h"

@implementation HotelSearchCriteriaV2

-(BOOL) isHotelSearchCriteriaValid

{
    if (!self.checkinDate || !self.checkoutDate || !self.longitude || !self.latitude) {
        return NO;
    }
    return YES;
}

+ (HotelSearchCriteriaV2 *) initializeDefaultsFromTripDictionary:(NSDictionary *)pBag
{
    HotelSearchCriteriaV2 *criteria = [[HotelSearchCriteriaV2 alloc] init];
    
    NSString* defaultTripKey = (NSString*)pBag[@"TRIP_KEY"];
    NSString* defaultHotelLocation = (NSString*)pBag[@"DEFAULT_HOTEL_LOCATION"];
    NSString* defaultHotelLatitude = (NSString*)pBag[@"DEFAULT_HOTEL_LATITUDE"];
    NSString* defaultHotelLongitude = (NSString*)pBag[@"DEFAULT_HOTEL_LONGITUDE"];
    NSString* defaultHotelCheckinDate = (NSString*)pBag[@"DEFAULT_HOTEL_CHECKIN_DATE"];
    NSString* defaultHotelCheckoutDate = (NSString*)pBag[@"DEFAULT_HOTEL_CHECKOUT_DATE"];
    
    criteria.tripId = defaultTripKey;
    if (defaultHotelLocation != nil && defaultHotelLatitude != nil && defaultHotelLongitude != nil)
    {
//        LocationResult *locationResult = [[LocationResult alloc] init];
        criteria.locationName = defaultHotelLocation;
        criteria.latitude = [defaultHotelLatitude doubleValue];
        criteria.longitude = [defaultHotelLongitude doubleValue];
//        hotelSearch.hotelSearchCriteria.locationResult = locationResult;
    }
    
    if (defaultHotelCheckinDate != nil)
    {
        NSDate *nullCheckedDate = [DateTimeFormatter getNSDateFromMWSDateString:defaultHotelCheckinDate];
        if (nullCheckedDate != nil)
        {
            criteria.checkinDate = nullCheckedDate;
        }
    }
    
    if (defaultHotelCheckoutDate != nil)
    {
        NSDate *nullCheckedDate = [DateTimeFormatter getNSDateFromMWSDateString:defaultHotelCheckoutDate];
        if (nullCheckedDate != nil)
        {
            criteria.checkoutDate = nullCheckedDate;
        }
    }
    if (!criteria.checkinDate || !criteria.checkoutDate) {
        criteria.checkinDate = [NSDate date];
        criteria.checkoutDate = [NSDate dateWithTimeInterval:24*60*60 sinceDate:criteria.checkinDate];
    }
    return criteria;
}

@end
