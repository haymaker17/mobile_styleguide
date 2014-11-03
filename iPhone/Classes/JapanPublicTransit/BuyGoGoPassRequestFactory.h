//
//  BuyGoGoPassRequestFactory.h
//  ConcurMobile
//
//  Created by Richard Puckett on 12/9/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXRequest.h"

@interface BuyGoGoPassRequestFactory : NSObject

+ (CXRequest *)buyGoGoPassWithItineraryLocator:(NSString *)itineraryLocator
                                 andSegmentKey:(NSString *)segmentKey
                               andCreditCardId:(NSString *)creditCardId;

@end
