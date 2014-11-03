//
//  LineSearchRequestFactory.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "CXRequest.h"

@interface LineSearchRequestFactory : NSObject

+ (CXRequest *)searchForLineByStation:(NSUInteger)stationId;

@end
