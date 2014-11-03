    //
//  BeaconManager.m
//  ConcurMobile
//
//  Created by Richard Puckett on 6/6/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CTENetworking.h"

#import "BeaconManager.h"

static NSString *const BEACON_REGION1_IDENTIFIER = @"Concur Line Start";
static NSString *const BEACON_REGION2_IDENTIFIER = @"Concur Line End";

//static NSString *const BEACON_REGION_UUID_ESTIMOTE = @"B9407F30-F5F8-466E-AFF9-25556B57FE6D";
static NSString *const BEACON_REGION1_UUID_CONCUR = @"256C1539-B7CC-4342-842F-D420312C5C6C";
static NSString *const BEACON_REGION2_UUID_CONCUR = @"4AC60CD2-0A78-47B1-B066-A05F5C00467B";
static NSString *const BEACON_TRIGGER_UUID_CONCUR = @"78973563-6931-4728-BE29-6BEF05F997A5";

static NSString *const REGION_ENTER_SERVICE_URL = @"http://siren.mobiyana.com/api/v1/beacon/region/enter";

@interface BeaconManager ()
@property (strong, nonatomic) CLLocationManager *locationManager;
@property (strong, nonatomic) CLBeaconRegion *region1;
@property (strong, nonatomic) CLBeaconRegion *region2;
@end

@implementation BeaconManager

__strong static id _sharedInstance = nil;

+ (BeaconManager *)sharedInstance {
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        _sharedInstance = [[self alloc] init];
    });
    
    return _sharedInstance;
}

- (id)init {
    self = [super init];
    
    if (self) {
        [self initRegion1];
        [self initRegion2];
    }
    
    return self;
}

- (void)initRegion1 {
    NSUUID *uuid = [[NSUUID alloc] initWithUUIDString:BEACON_REGION1_UUID_CONCUR];
    
    self.region1 = [[CLBeaconRegion alloc] initWithProximityUUID:uuid
                                                      identifier:BEACON_REGION1_IDENTIFIER];
    
    self.region1.notifyEntryStateOnDisplay = YES;
}

- (void)initRegion2 {
    NSUUID *uuid = [[NSUUID alloc] initWithUUIDString:BEACON_REGION2_UUID_CONCUR];
    
    self.region2 = [[CLBeaconRegion alloc] initWithProximityUUID:uuid
                                                      identifier:BEACON_REGION2_IDENTIFIER];
    
    self.region2.notifyEntryStateOnDisplay = YES;
}

#pragma mark - CLLocationManagerDelegate

- (void)locationManager:(CLLocationManager *)manager
	  didDetermineState:(CLRegionState)state
              forRegion:(CLRegion *)region {
    
    if ([region isKindOfClass:[CLBeaconRegion class]]) {
        CLBeaconRegion *beaconRegion = (CLBeaconRegion *) region;
        
        if (state == CLRegionStateInside) {
            [self.locationManager startRangingBeaconsInRegion:beaconRegion];
        }
    }
}

- (void)locationManager:(CLLocationManager *)manager
        didRangeBeacons:(NSArray *)beacons
               inRegion:(CLBeaconRegion *)region {
    
    [self.locationManager stopRangingBeaconsInRegion:region];
    
    [self sendEnterEventForBeacons:beacons inRegion:region];
}

#pragma mark - Util

- (NSString *)timestamp {
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];

    NSLocale *enUSPOSIXLocale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
    
    [dateFormatter setLocale:enUSPOSIXLocale];
    [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ssZZZZZ"];
    
    NSDate *now = [NSDate date];
    
    return [dateFormatter stringFromDate:now];
}

- (void)sendEnterEventForBeacons:(NSArray *)beacons inRegion:(CLBeaconRegion *)region {
    NSMutableDictionary *json = [[NSMutableDictionary alloc] init];
    
    if (!([beacons count] > 0)) {
        // Don't bother sending empty beacon arrays
        return;
    }
    
    NSString *userId = [[ExSystem sharedInstance] deviceId];
    
    if (!userId) {
        // Stop monitoring if we can't identify device.
        //
        [self stopMonitoring];
        
        return;
    }

    NSString *timestamp = [self timestamp];
    
    [json setValue:timestamp forKey:@"timestamp"];
    
    [json setValue:userId forKey:@"userId"];
    
    [json setValue:[[UIDevice currentDevice] model] forKey:@"deviceModel"];
    
    NSString *appVersion = [[NSBundle mainBundle] objectForInfoDictionaryKey:(NSString*)kCFBundleVersionKey];
    
    [json setValue:appVersion forKey:@"appVersion"];
    
    NSString *osVersion = [[UIDevice currentDevice] systemVersion];
    
    [json setValue:osVersion forKey:@"osVersion"];
    
    NSMutableArray *beaconArray = [[NSMutableArray alloc] init];
    
    for (CLBeacon *beacon in beacons) {
        NSString *uuid = region.proximityUUID.UUIDString;
        
        NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
        [dict setValue:uuid forKey:@"uuid"];
        [dict setValue:beacon.major forKey:@"major"];
        [dict setValue:beacon.minor forKey:@"minor"];
        [dict setValue:[NSNumber numberWithInteger:beacon.rssi] forKey:@"rssi"];
        [dict setValue:[NSNumber numberWithDouble:beacon.accuracy] forKey:@"accuracy"];
        
        [beaconArray addObject:dict];
    }
    
    [json setObject:beaconArray forKey:@"beacons"];
    
    CTENetworking *request = [[CTENetworking alloc] init];
    
    [request postJSON:REGION_ENTER_SERVICE_URL requestJSON:json success:nil failure:^(CTEError *error) {
        DLog(@"Beacon service error: %@", error);
    }];
}

- (void)startMonitoring {
    if ([ExSystem is7Plus]) {
        if ([CLLocationManager isMonitoringAvailableForClass:[CLBeaconRegion class]]) {
            if (self.locationManager && self.locationManager.monitoredRegions.count > 0) {
                // Already Monitoring
                return;
            }
            
            self.locationManager = [[CLLocationManager alloc] init];
            
            self.locationManager.delegate = self;
            
            [self.locationManager startMonitoringForRegion:self.region1];
            [self.locationManager startMonitoringForRegion:self.region2];
        } else {
            NSLog(@"This device does not support monitoring iBeacon regions");
        }
    }
}

- (void)stopMonitoring {
    if ([ExSystem is7Plus]) {
        if ([CLLocationManager isMonitoringAvailableForClass:[CLBeaconRegion class]]) {
            [self.locationManager stopMonitoringForRegion:self.region1];
            [self.locationManager stopMonitoringForRegion:self.region2];
        }
    }
}

@end
