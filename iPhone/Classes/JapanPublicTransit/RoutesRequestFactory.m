//
//  RoutesRequestFactory.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXRequest.h"
#import "RoutesRequestFactory.h"

@implementation RoutesRequestFactory

+ (CXRequest *)addRoutesToReport:(NSString *)reportKey {
    NSString *path = [NSString stringWithFormat:@"mobile/JPTransport/AddJpyTransRoutes"];
    
    return [[CXRequest alloc] initWithServicePath:path requestXML:@""];
}

+ (CXRequest *)routesForReport:(NSString *)reportKey {
    NSString *path = [NSString stringWithFormat:@"mobile/JPTransport/GetJpyTransRoutes/%@", reportKey];
    
    return [[CXRequest alloc] initWithServicePath:path];
}

@end
