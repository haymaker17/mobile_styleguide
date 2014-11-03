//
//  IgniteTripsDelegate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
@class EntityTrip;

@protocol IgniteTripsDelegate <NSObject>
- (void)tripSelected:(EntityTrip*) trip;
@end
