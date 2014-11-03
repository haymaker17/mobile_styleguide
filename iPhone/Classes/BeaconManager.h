//
//  BeaconManager.h
//  ConcurMobile
//
//  Created by Richard Puckett on 6/6/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

@interface BeaconManager : NSObject <CLLocationManagerDelegate>

+ (BeaconManager *)sharedInstance;

- (void)startMonitoring;
- (void)stopMonitoring;

@end
