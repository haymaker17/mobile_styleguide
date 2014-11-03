//
//  UberRequest.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "UberRequest.h"
#import "UberParser.h"
#import "CTENetworking.h"

@interface UberRequest()
@property (nonatomic, strong)   NSString                *serverToken;
@property (nonatomic, strong)   CTENetworking           *network;
@end

@implementation UberRequest

-(id)init
{
    self = [super init];
    if (self) {
        self.network = [[CTENetworking alloc] init];
    }
    return self;
}

-(id)initWithServerToken:(NSString *)token
{
    self = [self init];
    if (self) {
        self.serverToken = token;
    }
    return self;
}

// Check if Uber is installed on the device
-(BOOL)isUberInstalled
{
    return [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"uber://"]];
}

-(BOOL)requestCar
{
    NSString *requestUrl = [self buildAppRequest];
    return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:requestUrl]];
}

-(void)requestPriceWithSuccess:(void (^) (NSArray *prices))success failure:(void (^)(CTEError *error))failure
{
    [self.network getJSON:self.buildWebRequestForPrice success:^(NSDictionary *responseObject) {
        UberParser *uberParser = [[UberParser alloc] initWithPricesJSON:responseObject];
        if (success) {
            success(uberParser.prices);
        }
    } failure:^(CTEError *error) {
        if (failure) {
            failure(error);
        }
    }];
}

-(void)requestTimeWithSuccess:(void (^) (NSArray *times))success failure:(void (^)(CTEError *error))failure
{
    [self.network getJSON:self.buildWebRequestForTime success:^(NSDictionary *responseObject) {
        UberParser *uberParser = [[UberParser alloc] initWithTimesJSON:responseObject];
        if (success) {
            success(uberParser.times);
        }
    } failure:^(CTEError *error) {
        if (failure) {
            failure(error);
        }
    }];
}

-(NSString *)buildWebRequestForPrice
{
    NSMutableString *requestUrl = [[NSMutableString alloc]initWithString:@"https://api.uber.com/v1/estimates/price"];
    [requestUrl appendString:[NSString stringWithFormat:@"?server_token=%@", self.serverToken]];
    [requestUrl appendString:[NSString stringWithFormat:@"&start_latitude=%f", self.pickupLocation.latitude]];
    [requestUrl appendString:[NSString stringWithFormat:@"&start_longitude=%f", self.pickupLocation.longitude]];
    [requestUrl appendString:[NSString stringWithFormat:@"&end_latitude=%f", self.dropoffLocation.latitude]];
    [requestUrl appendString:[NSString stringWithFormat:@"&end_longitude=%f", self.dropoffLocation.longitude]];
    return requestUrl;
}

-(NSString *)buildWebRequestForTime
{
    NSMutableString *requestUrl = [[NSMutableString alloc]initWithString:@"https://api.uber.com/v1/estimates/time"];
    [requestUrl appendString:[NSString stringWithFormat:@"?server_token=%@", self.serverToken]];
    [requestUrl appendString:[NSString stringWithFormat:@"&start_latitude=%f", self.pickupLocation.latitude]];
    [requestUrl appendString:[NSString stringWithFormat:@"&start_longitude=%f", self.pickupLocation.longitude]];
    return requestUrl;
}

-(NSString *)buildAppRequest
{
    NSString *requestUrl = nil;
    // If user is installed, user custom URL
    if ([self isUberInstalled])
    {
        requestUrl = [self addPickupAndDropoffToUrl:@"uber://?action=setPickup"];
    }
    else
    {
        // Otherwise, we send the request to Uber's mobile site
        // This is what Uber have requested all partners do in their API
        requestUrl = @"https://m.uber.com/sign-up?client_id=xxxx";
    }

    return requestUrl;
}

-(NSString *)addPickupAndDropoffToUrl:(NSString *)url
{
    NSMutableString *requestUrl = [[NSMutableString alloc]initWithString:url];
    
    // Pickup
    if (CLLocationCoordinate2DIsValid(self.pickupLocation))
    {
        [requestUrl appendString:[NSString stringWithFormat:@"&pickup[latitude]=(%f)", self.pickupLocation.latitude]];
        [requestUrl appendString:[NSString stringWithFormat:@"&pickup[longitude]=(%f)", self.pickupLocation.longitude]];
    }
    else
    {
        // If no location for pickup has been set, then use current location
        [requestUrl appendString:@"&pickup=my_location"];
    }
    if (self.pickupNickname != nil)
    {
        [requestUrl appendString:[NSString stringWithFormat:@"&pickup[nickname]=(%@)", self.pickupNickname]];
    }
    if (self.pickupAddress != nil)
    {
        // format the address
        [requestUrl appendString:[NSString stringWithFormat:@"&pickup[formatted_address]=(%@)", [self.pickupAddress stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]]];
    }
    
    // Dropoff
    if (CLLocationCoordinate2DIsValid(self.dropoffLocation))
    {
        [requestUrl appendString:[NSString stringWithFormat:@"&dropoff[latitude]=(%f)", self.dropoffLocation.latitude]];
        [requestUrl appendString:[NSString stringWithFormat:@"&dropoff[longitude]=(%f)", self.dropoffLocation.longitude]];
    }
    if (self.dropoffNickname != nil)
    {
        [requestUrl appendString:[NSString stringWithFormat:@"&dropoff[nickname]=(%@)", self.dropoffNickname]];
    }
    if (self.dropoffAddress != nil)
    {
        // format the address
        [requestUrl appendString:[NSString stringWithFormat:@"&dropoff[formatted_address]=(%@)", [self.dropoffAddress stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]]];
    }
    
    return requestUrl;
}
@end

