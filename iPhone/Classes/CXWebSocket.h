//
//  CXWebSocket.h
//  ConcurMobile
//
//  Created by Richard Puckett on 12/27/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "SocketIO.h"

@interface CXWebSocket : NSObject <CLLocationManagerDelegate>

@property (copy, nonatomic) NSString *deviceToken;
@property (copy, nonatomic) NSString *xid;
@property (strong, nonatomic) CLLocationManager *locationManager;
@property (strong, nonatomic) CLLocation *location;

+ (CXWebSocket *)sharedClient;

- (void)connectWithDeviceToken:(NSString *)deviceToken;
- (void)disconnect;

@end
