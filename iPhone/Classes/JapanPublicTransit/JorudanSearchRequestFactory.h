//
//  JorudanSearchRequestFactory.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "CXRequest.h"
#import "Segment.h"
#import "Station.h"

@interface JorudanSearchRequestFactory : NSObject

+ (CXRequest *)searchJorudanForDate:(NSDate *)date
                        fromStation:(Station *)fromStation
                          toStation:(Station *)toStation
                        viaStation1:(Station *)throughStation1
                        viaStation2:(Station *)throughStation2
                       withSeatType:(SeatType)seatType
                        isRoundTrip:(BOOL)isRoundTrip
                       isIcCardFare:(BOOL)isIcCardFare;

@end
