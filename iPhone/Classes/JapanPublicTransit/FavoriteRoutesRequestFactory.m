//
//  FavoriteRoutesRequestFactory.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXRequest.h"
#import "FavoriteRoutesRequestFactory.h"

@implementation FavoriteRoutesRequestFactory

+ (CXRequest *)favoriteRoutes {
    NSString *path = [NSString stringWithFormat:@"mobile/JPTransport/GetJpyFavoriteRoutes"];
    
    return [[CXRequest alloc] initWithServicePath:path];
}

@end
