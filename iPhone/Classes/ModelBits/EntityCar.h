//
//  EntityCar.h
//  ConcurMobile
//
//  Created by Chris Butcher on 07/11/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityCar : NSManagedObject

@property (nonatomic, retain) NSNumber * dropoffExtendedHour;
@property (nonatomic, retain) NSString * dropoffIata;
@property (nonatomic, retain) NSString * dropoffLocation;
@property (nonatomic, retain) NSString * pickupLocation;
@property (nonatomic, retain) NSNumber * pickupExtendedHour;
@property (nonatomic, retain) NSString * smokingPreferenceCode;
@property (nonatomic, retain) NSString * isOffAirport;
@property (nonatomic, retain) NSString * pickupIata;
@property (nonatomic, retain) NSString * dropoffLatitude;
@property (nonatomic, retain) NSString * dropoffLongitude;
@property (nonatomic, retain) NSString * pickupLatitude;
@property (nonatomic, retain) NSDate * dropoffDate;
@property (nonatomic, retain) NSString * carTypeCode;
@property (nonatomic, retain) NSDate * pickupDate;
@property (nonatomic, retain) NSString * pickupLongitude;

@end
