//
//  CXWebSocket.m
//  ConcurMobile
//
//  Created by Richard Puckett on 12/27/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXWebSocket.h"

@interface CXWebSocket () <SocketIODelegate>

@property (strong, nonatomic) SocketIO *socketIO;

@end

@implementation CXWebSocket

__strong static id _sharedClient = nil;

+ (CXWebSocket *)sharedClient {
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        _sharedClient = [[self alloc] init];
    });
    
    return _sharedClient;
}

// "development" cannot receive APNS pushes.
// "release" -- including ad-hoc -- can.
//
- (NSString *)buildType {
    NSString *t = @"release";
    
    if ([self isDevelopmentBuild]) {
        t = @"development";
    }
    
    return t;
}

- (id)init {
    self = [super init];
    
    if (self) {
        self.socketIO = [[SocketIO alloc] initWithDelegate:self];
    }
    
    return self;
}

// Based on: https://gist.github.com/steipete/7668246
//
- (BOOL)isDevelopmentBuild {
#if TARGET_IPHONE_SIMULATOR
    return YES;
#else
    static BOOL isDevelopment = NO;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        // There is no provisioning profile in AppStore Apps.
        NSData *data = [NSData dataWithContentsOfFile:[NSBundle.mainBundle pathForResource:@"embedded" ofType:@"mobileprovision"]];
        if (data) {
            const char *bytes = [data bytes];
            NSMutableString *profile = [[NSMutableString alloc] initWithCapacity:data.length];
            for (NSUInteger i = 0; i < data.length; i++) {
                [profile appendFormat:@"%c", bytes[i]];
            }
            // Look for debug value, if detected we're a development build.
            NSString *cleared = [[profile componentsSeparatedByCharactersInSet:NSCharacterSet.whitespaceAndNewlineCharacterSet] componentsJoinedByString:@""];
            isDevelopment = [cleared rangeOfString:@"<key>get-task-allow</key><true/>"].length > 0;
        }
    });
    return isDevelopment;
#endif
}

- (void)connectWithDeviceToken:(NSString *)deviceToken {
    if ([self.socketIO isConnected]) {
        return;
    }
    
    if ([self.socketIO isConnecting]) {
        return;
    }
    
    self.deviceToken = deviceToken;
    
    self.locationManager = [[CLLocationManager alloc] init];
    
    self.locationManager.delegate = self;
    self.locationManager.desiredAccuracy = kCLLocationAccuracyKilometer;
    
    [self.locationManager startUpdatingLocation];
}

- (void)disconnect {
    if ([self.socketIO isConnected]) {
        [self.socketIO disconnect];
    }
}

# pragma mark - SocketIO Delegate

- (void)socketIODidConnect:(SocketIO *)socket {
    //NSLog(@"socket.io connected.");
    
    NSString *userId = [ExSystem sharedInstance].userName;
    
    NSString *userElement = [NSString stringWithFormat:@"\"userId\": \"%@\"", userId];
    NSString *deviceElement = [NSString stringWithFormat:@"\"deviceToken\": \"%@\"", self.deviceToken];
    NSString *modelElement = [NSString stringWithFormat:@"\"deviceModel\": \"%@\"", [[UIDevice currentDevice] model]];
    NSString *buildElement = [NSString stringWithFormat:@"\"buildType\": \"%@\"", [self buildType]];
    
    NSString *appVersion = [[NSBundle mainBundle] objectForInfoDictionaryKey:(NSString*)kCFBundleVersionKey];
    NSString *appVersionElement = [NSString stringWithFormat:@"\"appVersion\": \"%@\"", appVersion];
    
    NSString *osVersion = [[UIDevice currentDevice] systemVersion];
    NSString *osVersionElement = [NSString stringWithFormat:@"\"osVersion\": \"%@\"", osVersion];
    
    double latitude = 0;
    double longitude = 0;
    
    if (self.location) {
        latitude = self.location.coordinate.latitude;
        longitude = self.location.coordinate.longitude;
    }
    
    NSString *latitudeElement = [NSString stringWithFormat:@"\"latitude\": \"%f\"", latitude];
    NSString *longitudeElement = [NSString stringWithFormat:@"\"longitude\": \"%f\"", longitude];
    
    NSString *vendorElement;
    
    if (floor(NSFoundationVersionNumber) >= NSFoundationVersionNumber_iOS_6_0) {
        NSString *uuid = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
        vendorElement = [NSString stringWithFormat:@"\"vendorId\": \"%@\"", uuid];
    } else {
        vendorElement = [NSString stringWithFormat:@"\"vendorId\": \"%@\"", @"unknown"];
    }
    
    NSString *data = [NSString stringWithFormat:@"{%@, %@, %@, %@, %@, %@, %@, %@, %@}",
                      appVersionElement, userElement, deviceElement, osVersionElement,
                      modelElement, buildElement, vendorElement, latitudeElement, longitudeElement];
    
    [socket sendEvent:@"register-device" withData:data];
}

- (void)socketIO:(SocketIO *)socket didReceiveEvent:(SocketIOPacket *)packet {
    //NSLog(@"socket.io received event.");
}

- (void)socketIO:(SocketIO *)socket onError:(NSError *)error {
    //NSLog(@"socket.io error: %@", error);
}

- (void)socketIODidDisconnect:(SocketIO *)socket disconnectedWithError:(NSError *)error {
    //NSLog(@"socket.io disconnected.");
}

#pragma mark - CLLocationManagerDelegate

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
    [self startConnection];
}

- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
    self.location = (CLLocation *) [locations lastObject];
    
    [manager stopUpdatingLocation];
    
    [self startConnection];
}

- (void)startConnection {
    self.socketIO.useSecure = NO;
    
    [self.socketIO connectToHost:@"ipm.mobiyana.com"
                          onPort:80];
}

@end
