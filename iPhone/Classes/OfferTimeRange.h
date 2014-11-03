//
//  OfferTimeRange.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/2/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface OfferTimeRange : NSObject {
    NSString *endDateTimeUTC;
    NSString *startDateTimeUTC;
}

@property (nonatomic, strong) NSString *endDateTimeUTC;
@property (nonatomic, strong) NSString *startDateTimeUTC;
@end
