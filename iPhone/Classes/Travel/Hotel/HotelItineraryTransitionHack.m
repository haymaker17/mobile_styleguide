//
//  HotelItineraryTransitionHack.m
//  ConcurMobile
//
//  Created by ernest cho on 10/15/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelItineraryTransitionHack.h"
#import "TripManager.h"

// informs user when the itin transition is complete
typedef void(^HotelItineraryTransitionHackBlock)();

@interface HotelItineraryTransitionHack()
@property (nonatomic, readonly, copy) HotelItineraryTransitionHackBlock completion;


@property (nonatomic, readonly, strong) NSString *recordLocator;
@property (nonatomic, readonly, strong) NSString *itineraryLocator;
@end

@implementation HotelItineraryTransitionHack

// this starts the process of refreshing itineraries using the legacy Concur Mobile code
- (void)requestHotelItineraryWithRecordLocator:(NSString *)recordLocator itineraryLocator:(NSString *)itineraryLocator completion:(void (^)())completion
{
    _completion = completion;
    _recordLocator = recordLocator;
    _itineraryLocator = itineraryLocator;

    [self refreshHomeScreen];
    [self updateItinerary];
}

- (void)respondToFoundData:(Msg *)msg
{
    [self goToOldItineraryScreen];

    // closes wait dialog
    if (self.completion) {
        self.completion();
    }
}

// updates the itinerary
- (void)updateItinerary
{
    NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.recordLocator, @"RECORD_LOCATOR", @"HOTEL_BOOKING", @"TO_VIEW", self.itineraryLocator, @"ITIN_LOCATOR", nil];
    [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

// loads up some core data stuff and goes to the old itinerary screen
- (void)goToOldItineraryScreen
{
    // get trip info
    EntityTrip *trip = [[TripManager sharedInstance] fetchByItinLocator:self.itineraryLocator];
    NSString *tripKey = trip.tripKey;

    // build pbag
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SKIP_PARSE", nil];
    pBag[@"POP_TO_ROOT_VIEW"] = @"YES";
    pBag[@"TRIP"] = trip;
    pBag[@"TRIP_KEY"] = tripKey;

    // go back to home and launch the trip screen
    [ConcurMobileAppDelegate switchToView:TRIP_DETAILS viewFrom:HOTEL_BOOKING ParameterBag:pBag];
}

// refreshes the home screen and loads part of the data needed for trip segment details. yea IDK...
- (void)refreshHomeScreen
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING", nil];
    [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:nil];
}

@end
