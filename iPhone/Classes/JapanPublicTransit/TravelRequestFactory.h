//
//  TravelRequestFactory.h
//  ConcurMobile
//
//  Created by Richard Puckett on 12/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXRequest.h"

typedef NS_ENUM(NSUInteger, CardType) {
    CardTypeGhost       = 1 << 0,
    CardTypePersonal    = 1 << 1
};

@interface TravelRequestFactory : NSObject

+ (CXRequest *)creditCardsForLoginId:(NSString *)loginId
                         andTravelId:(NSUInteger)travelId
                        forCardTypes:(CardType)cardTypes;

@end
