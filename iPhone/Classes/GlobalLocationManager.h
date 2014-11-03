//
//  GlobalLocationManager.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 12/14/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

extern NSString * const CURRENT_LOCATION_UPDATE;
extern NSString * const CURRENT_LOCATION_FAILED;
extern NSString * const LOCATION_AUTHORIZATION_NOT_ALLOWED ;
extern NSString * const LOCATION_AUTHORIZATION_ALLOWED ;


@interface GlobalLocationManager : NSObject <CLLocationManagerDelegate> {
    CLLocationManager *locationManager;
    CLLocation *currentLocation;
}

@property (nonatomic, strong) CLLocationManager *locationManager;
@property (nonatomic, strong) CLLocation *currentLocation;

+(GlobalLocationManager*)sharedInstance;
+(CLLocation *)currentLocation;

+(void)startTrackingSignificantLocationUpdates;
+(void)stopTrackingSignificantLocationUpdates;
@end
