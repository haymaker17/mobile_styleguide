//
//  MockServer.h
//  ConcurMobile
//
//  Created by ernest cho on 9/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface MockServer : NSObject

+ (id)sharedInstance;

- (void)enableMockServer;
- (void)disableMockServer;

- (void)addMockForHotelSearch;
- (void)addMockForHotelRates;

@end
