//
//  HotelSearchCriteriaV2.h
//  ConcurMobile
//
//  Created by Sally Yan on 7/21/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HotelSearchCriteriaV2 : NSObject

//TODO: add location class
// Sally: just add this for now, the latitude and longitude shoud be in Location class
@property double latitude;
@property double longitude;
@property double distanceValue;
@property (strong, nonatomic) NSDate	*checkinDate;
@property (strong, nonatomic) NSDate	*checkoutDate;
@property (strong, nonatomic) NSString  *hotelName;
@property BOOL isMetricDistance;
//
// For Filter Use
@property (strong, nonatomic) NSMutableDictionary *filterDict;

-(BOOL) isHotelSearchCriteriaValid;

@end
