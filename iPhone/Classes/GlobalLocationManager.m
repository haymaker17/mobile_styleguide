//
//  GlobalLocationManager.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 12/14/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "GlobalLocationManager.h"

static GlobalLocationManager *sharedInstance;
NSString * const CURRENT_LOCATION_UPDATE = @"CURRENT_LOCATION_UPDATE";
NSString * const CURRENT_LOCATION_FAILED = @"CURRENT_LOCATION_FAILED";
NSString * const LOCATION_AUTHORIZATION_NOT_ALLOWED = @"LOCATION_AUTHORIZATION_NOT_ALLOWED";
NSString * const LOCATION_AUTHORIZATION_ALLOWED = @"LOCATION_AUTHORIZATION_UNKNOWN";


@implementation GlobalLocationManager
@synthesize locationManager;
@synthesize currentLocation;

+(GlobalLocationManager*)sharedInstance
{
    if (sharedInstance != nil) 
	{
		return sharedInstance;
	}
	else 
	{
		@synchronized (self)
		{
			if (sharedInstance == nil) 
			{
				sharedInstance = [[GlobalLocationManager alloc] init];
			}
		}
		return sharedInstance;
	}
}

-(GlobalLocationManager*)init
{
    self = [super init];
    if (self)
	{
        self.locationManager = [[CLLocationManager alloc] init];
	}
    
    return self; 
}


#pragma mark CLLocationManagerDelegate methods
- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
// Send notification out when locationmanager fails to get location.

    NSNotification *notif = [NSNotification notificationWithName:CURRENT_LOCATION_FAILED object:nil userInfo:nil];
    // logs the error
    ALog(@"Error: Unable to get user location");
    if ([error domain] == kCLErrorDomain) {
        
        // We handle CoreLocation-related errors here
        switch ([error code]) {
                // "Don't Allow" on two successive app launches is the same as saying "never allow". The user
                // can reset this for all apps by going to Settings > General > Reset > Reset Location Warnings.
            case kCLErrorDenied:
                //post notification of location update for interested view controllers to update their views
                ALog(@"User denied Location access");
                [[NSNotificationCenter defaultCenter] postNotification:notif];

                break;
            case kCLErrorLocationUnknown:
                ALog(@"Error:User Location unknown");
                
            default:
                break;
        }
    } else {
        // We handle all non-CoreLocation errors here
        
    }
}

- (void)locationManager:(CLLocationManager *)manager didUpdateHeading:(CLHeading *)newHeading
{
    
}


- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation
{
    NSLog(@"GlobalLocationManager:new location received: lat:%f, long: %f",newLocation.coordinate.latitude, newLocation.coordinate.longitude);
    self.currentLocation = newLocation;
    //post notification of location update for interested view controllers to update their views
    NSNotification *notif = [NSNotification notificationWithName:CURRENT_LOCATION_UPDATE object:nil userInfo:nil];
	[[NSNotificationCenter defaultCenter] postNotification:notif];
}

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status
{
    // Notify user choice.
    if (status == kCLAuthorizationStatusDenied || status == kCLAuthorizationStatusRestricted) {
        NSNotification *notif = [NSNotification notificationWithName:LOCATION_AUTHORIZATION_NOT_ALLOWED object:nil userInfo:nil];
        [[NSNotificationCenter defaultCenter] postNotification:notif];

    }
    else
    {
        // Check for iOS 8
        if ([ExSystem is8Plus]){
            if (status == kCLAuthorizationStatusAuthorized || status == kCLAuthorizationStatusAuthorizedAlways || status == kCLAuthorizationStatusAuthorizedWhenInUse )
            {
                // Ideally for we want to notify actual user choice such as always or when in use etc. For now we are good with this.
                NSNotification *notif = [NSNotification notificationWithName:LOCATION_AUTHORIZATION_ALLOWED object:nil userInfo:nil];
                [[NSNotificationCenter defaultCenter] postNotification:notif];
            }
        }
        
    }

}

#pragma mark Location Tracking methods
- (void)startSignificantChangeUpdates
{
    if (nil == locationManager)
        locationManager = [[CLLocationManager alloc] init];
    
    locationManager.delegate = self;
    // For iOS 8 first ask the user permission.
    // if user agrees then start monitoring
    if ([locationManager respondsToSelector:@selector(requestAlwaysAuthorization)]) {
        [locationManager requestAlwaysAuthorization];
    }
    [locationManager startMonitoringSignificantLocationChanges];
}

+(void)startTrackingSignificantLocationUpdates
{
    NSLog(@"GlobalLocationManager:startTrackingSignificantLocationUpdates");
    [[GlobalLocationManager sharedInstance] startSignificantChangeUpdates];
}

+(void)stopTrackingSignificantLocationUpdates
{
     NSLog(@"GlobalLocationManager:stopTrackingSignificantLocationUpdates");
    [[GlobalLocationManager sharedInstance].locationManager stopMonitoringSignificantLocationChanges];
}

+(CLLocation *)currentLocation
{
   
    return [GlobalLocationManager sharedInstance].currentLocation;
}


@end
