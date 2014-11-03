//
//  CXRequest.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXClient.h"
#import "CXRequest.h"

/**
 *  This class is deprecated!  Use CTENetworking.
 *
 *  This class used to interface with AFNetworking 1.x
 *  AFNetworking 2.x is not api compatible with 1.x, so we leave this stub here simplify removal of 1.x code.
 *
 *  Ernest
 *
 */
@implementation CXRequest

- (id)initWithServicePath:(NSString *)path
          requestXML:(NSString *)requestXML {
    
    self = [super init];
    
    if (self) {
        _method = @"POST";
        _path = path;
        _requestXML = requestXML;
    }
    
    return self;
}

- (id)initWithServicePath:(NSString *)path {

    self = [super init];

    if (self) {
        _method = @"GET";
        _path = path;
        _requestXML = nil;
    }

    return self;
}

@end
