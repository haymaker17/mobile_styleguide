//
//  HotelItineraryTransitionHack.h
//  ConcurMobile
//
//  Created by ernest cho on 10/15/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 *  This Hack handles the old msg used to load trips and the hotel itin
 */
@interface HotelItineraryTransitionHack : MobileViewController

- (void)requestHotelItineraryWithRecordLocator:(NSString *)recordLocator itineraryLocator:(NSString *)itineraryLocator completion:(void (^)())completion;

@end