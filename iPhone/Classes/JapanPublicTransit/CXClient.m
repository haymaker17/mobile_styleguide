//
//  CXClient.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXClient.h"
#import "ExSystem.h"

// used to switch to AFNetworking 2.0
#import "CTENetworking.h"


/**
 *  This class is deprecated!  Use CTENetworking.
 *
 *  This class used to interface with AFNetworking 1.x
 *  AFNetworking 2.x is not api compatible with 1.x, so we leave this stub here simplify removal of 1.x code.
 *
 *  Ernest
 *
 */
@implementation CXClient

__strong static id _sharedClient = nil;

+ (CXClient *)sharedClient {
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        _sharedClient = [[self alloc] init];
    });
    
    return _sharedClient;
}

- (id)init {
    self = [super init];
    
    if (self) {

    }
    
    return self;
}

- (void)performRequest:(CXRequest *)apiRequest success:(CXSuccessBlock)success failure:(CXFailureBlock)failure {

    CTENetworking *manager = [[CTENetworking alloc] init];

    if ([apiRequest.method isEqualToString:@"POST"]) {
        [manager postXMLToURL:apiRequest.path requestXML:apiRequest.requestXML success:^(NSString *responseObject) {
            if (success) {
                success(responseObject);
            }
        } failure:^(CTEError *error) {
            if (failure) {
                failure(error);
            }
        }];
    } else {
        [manager getXMLFromURL:apiRequest.path success:^(NSString *responseObject) {
            if (success) {
                success(responseObject);
            }
        } failure:^(CTEError *error) {
            if (failure) {
                failure(error);
            }
        }];
    }
}

@end
