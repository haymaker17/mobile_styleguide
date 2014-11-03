//
//  OfferValidity.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/2/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "OfferLocation.h"
#import "OfferTimeRange.h"

@interface OfferValidity : NSObject {
    NSMutableArray *offerLocations;
    NSMutableArray *offerTimeRanges;
}

@property (nonatomic, strong) NSMutableArray *offerLocations;
@property (nonatomic, strong) NSMutableArray *offerTimeRanges;
@end
