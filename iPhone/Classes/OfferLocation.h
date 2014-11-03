//
//  OfferLocation.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/2/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface OfferLocation : NSObject {
    NSNumber *latitude;
    NSNumber *longitude;
    NSNumber *proximity;
    NSNumber *dimension;
    NSMutableArray *overlayList;
}
@property (nonatomic, strong) NSNumber *dimension;
@property (nonatomic, strong) NSMutableArray *overlayList;
@property (nonatomic, strong) NSNumber *latitude;
@property (nonatomic, strong) NSNumber *longitude;
@property (nonatomic, strong) NSNumber *proximity;
@end
