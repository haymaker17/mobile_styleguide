//
//  CTELocationSearch.h
//  ConcurSDK
//
//  Created by Pavan Adavi on 7/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEError.h"

@interface CTELocationSearch : NSObject


- (id)initWithAddress:(NSString *)address
       isAirportsOnly:(BOOL)isAirportsOnly;

- (void)searchLocationsWithSuccess:(void (^)(NSArray *locations))success failure:(void (^)(CTEError *error))failure;

- (NSArray *)search;

@end
