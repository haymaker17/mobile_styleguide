//
//  UberRequest.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import "CTEError.h"

@interface UberRequest : NSObject
@property (nonatomic)           CLLocationCoordinate2D  pickupLocation;
@property (nonatomic, strong)   NSString                *pickupNickname;
@property (nonatomic, strong)   NSString                *pickupAddress;
@property (nonatomic)           CLLocationCoordinate2D  dropoffLocation;
@property (nonatomic, strong)   NSString                *dropoffNickname;
@property (nonatomic, strong)   NSString                *dropoffAddress;

-(id)initWithServerToken:(NSString *)token;
-(BOOL)isUberInstalled;
-(BOOL)requestCar;
-(void)requestPriceWithSuccess:(void (^) (NSArray *prices))success failure:(void (^)(CTEError *error))failure;
-(void)requestTimeWithSuccess:(void (^) (NSArray *prices))success failure:(void (^)(CTEError *error))failure;



@end

