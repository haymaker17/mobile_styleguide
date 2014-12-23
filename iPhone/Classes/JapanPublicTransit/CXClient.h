//
//  CXClient.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXRequest.h"

typedef void (^CXSuccessBlock)(NSString *result);
typedef void (^CXFailureBlock)(NSError *error);

@interface CXClient : NSObject

@property (strong, nonatomic) NSString *xml;

+ (CXClient *)sharedClient;

- (void)performRequest:(CXRequest *)apiRequest
                            success:(CXSuccessBlock)success
                            failure:(CXFailureBlock)failure;

@end
