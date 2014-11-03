//
//  FusionMockServer.h
//  ConcurMobile
//
//  Created by ernest cho on 4/21/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface FusionMockServer : NSObject

// flight to SFO
@property (nonatomic, readwrite, strong) NSString *departForSFOTime;
@property (nonatomic, readwrite, strong) NSString *arriveAtSFOTime;

// flight to SEA
@property (nonatomic, readwrite, strong) NSString *departForSEATime;
@property (nonatomic, readwrite, strong) NSString *arriveAtSEATime;

+ (id)sharedInstance;

// fake itinerary to san francisco
- (void)addMockForSanFranciscoTripItinerary;
- (void)addMockForTripSummaries;

- (void)addMocksForHotelBooking;

@end
