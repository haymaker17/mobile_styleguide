//
//  CTEHotelSearch.h
//  ConcurSDK
//
//  Created by echo on 7/18/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEError.h"

@interface CTEHotelSearch : NSObject

// Defaults search radius to 5 miles
- (id)initWithHotelName:(NSString *)hotelName
               latitude:(double)latitude
              longitude:(double)longitude
            checkInDate:(NSDate *)checkInDate
           checkOutDate:(NSDate *)checkOutDate;

- (id)initWithHotelName:(NSString *)hotelName
               latitude:(double)latitude
              longitude:(double)longitude
            checkInDate:(NSDate *)checkInDate
           checkOutDate:(NSDate *)checkOutDate
                 radius:(int)radius
           distanceUnit:(NSString *)distanceUnit;

// This function performs a synchronous Hotel Search
- (void)searchWithSuccess:(void (^)(NSArray *hotels))success failure:(void (^)(CTEError *error))failure;

// This function to performs an Async Hotel Search and returns hotels and room rates as it gets them
- (void)asyncSearchWithSuccess:(void (^)(NSArray *hotels, BOOL searchDone))success failure:(void (^)(CTEError *error))failure;

@end
