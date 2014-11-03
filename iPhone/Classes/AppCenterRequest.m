//
//  AppCenterRequest.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 03/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AppCenterRequest.h"
#import "AppCenterResponseParser.h"
#import "CTENetworking.h"

@interface AppCenterRequest()
@property (nonatomic, strong) CTENetworking *network;
@property (nonatomic, strong) NSString *listingId;
@end

@implementation AppCenterRequest

-(id)init
{
    self = [super init];
    if (self) {
        self.network = [[CTENetworking alloc] init];
    }
    return self;
}

-(id)initWithListingId:(NSString *)listingId
{
    self = [self init];
    if (self) {
        self.listingId = listingId;
    }
    return self;
}

-(void)requestListOfApps:(void (^) (NSArray *appListings, NSString *info))success failure:(void (^)(CTEError *error))failure
{
    [self.network getJSON:self.serviceURL success:^(NSDictionary *responseObject) {
        AppCenterResponseParser *parser = [[AppCenterResponseParser alloc] initWithJsonResponse:responseObject];
        if (success) {
            success(parser.appListings, parser.info);
        }
    } failure:^(CTEError *error) {
        if (failure) {
            failure(error);
        }
    }];
}

- (NSString *)serviceURL
{
    NSString *url = @"/mobile/marketplace/v1.0/appcenter/GetListings";
    if (self.listingId != nil && [self.listingId length] > 0)  {
        return [NSString stringWithFormat:@"%@/%@", url, self.listingId];
    }
    
    return url;
}
@end