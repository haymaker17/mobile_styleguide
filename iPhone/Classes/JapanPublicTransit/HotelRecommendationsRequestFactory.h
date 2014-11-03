//
//  HotelRecommendationsRequestFactory.h
//  ConcurMobile
//
//  Created by Richard Puckett on 11/12/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXRequest.h"

@interface HotelRecommendationsRequestFactory : NSObject

+ (CXRequest *)recommendationsForLatitude:(CLLocationDegrees)latitude
                             andLongitude:(CLLocationDegrees)longitude
                                andRadius:(NSUInteger)radius;

@end
