//
//  FusionMockServer.m
//  ConcurMobile
//
//  Created by ernest cho on 4/21/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FusionMockServer.h"
#import "URLMock.h"

@implementation FusionMockServer

static id sharedInstance = nil;

+ (id)sharedInstance
{
    if (!sharedInstance) {
        sharedInstance = [[self alloc] init];
    }
    return sharedInstance;
}

- (id)init
{
    self = [super init];
    if (self)
    {
        self.departForSFOTime = @"2014-06-09T12:00:00";
        self.arriveAtSFOTime = @"2014-06-09T09:41:00";

        self.departForSEATime = @"2014-06-11T16:35:00";
        self.arriveAtSEATime = @"2014-06-11T14:30:00";

        [self enableMockServer];
    }
    return self;
}

- (void)enableMockServer
{
    [UMKMockURLProtocol enable];
}

- (void)disableMockServer
{
    [UMKMockURLProtocol disable];
}

- (void)addMocksForHotelBooking
{
    [self addMockForHotelSearch];
    [self addMockForHotelPoll];
    [self addMockForRoomDetail];
    [self addMockForRoomPreSell];
}

- (NSString *)loadXMLFile:(NSString *)filename
{
    NSBundle *bundle = [NSBundle bundleForClass:[self class]];
    NSString *path = [bundle pathForResource:filename ofType:@"xml"];
    NSData *data = [NSData dataWithContentsOfFile:path];

    return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
}

- (void)addMockForHotelSearch
{
    NSURL *URL = [NSURL URLWithString:@"https://www.concursolutions.com/mobile/Hotel/Search3"];
    NSString *request = [self loadXMLFile:@"HotelSearchRequest"];
    NSString *response = [self loadXMLFile:@"HotelSearchResponse"];

    [UMKMockURLProtocol expectMockHTTPPostRequestWithURL:URL request:request responseStatusCode:200 response:response];
}

- (void)addMockForHotelPoll
{
    NSURL *URL = [NSURL URLWithString:@"https://www.concursolutions.com/mobile/Hotel/PollSearchResults/a718e930-9b6b-41cc-9151-abe4b983ee1e"];
    NSString *request = [self loadXMLFile:@"HotelPollRequest"];
    NSString *response = [self loadXMLFile:@"HotelPollResponse"];

    [UMKMockURLProtocol expectMockHTTPPostRequestWithURL:URL request:request responseStatusCode:200 response:response];
}

- (void)addMockForRoomDetail
{
    NSURL *URL = [NSURL URLWithString:@"https://www.concursolutions.com/mobile/Hotel/Details/11"];
    NSString *response = [self loadXMLFile:@"RoomDetailResponse"];

    [UMKMockURLProtocol expectMockHTTPGetRequestWithURL:URL responseStatusCode:200 response:response];
}

- (void)addMockForRoomPreSell
{
    NSURL *URL = [NSURL URLWithString:@"https://www.concursolutions.com/Mobile/PreSell/PreSellOptions?choiceId=H11%7CJ1QH2G"];
    NSString *response = [self loadXMLFile:@"RoomPreSellResponse"];

    [UMKMockURLProtocol expectMockHTTPGetRequestWithURL:URL responseStatusCode:200 response:response];
}

/**
 *  Fake itin summary list for Fusion 14
 */
- (void)addMockForTripSummaries
{
    NSURL *URL = [NSURL URLWithString:@"https://www.concursolutions.com/Mobile/Itinerary/GetUserTripListV2/"];
    NSString *response = [self loadXMLFile:@"TripSummariesResponse"];

    [UMKMockURLProtocol expectMockHTTPGetRequestWithURL:URL responseStatusCode:200 response:response];
}

/**
 *  Fake itin to San Francisco for Fusion 14
 */
- (void)addMockForSanFranciscoTripItinerary
{
    NSURL *URL = [NSURL URLWithString:@"https://www.concursolutions.com/Mobile/SingleItinerary"];
    NSString *request = [self loadXMLFile:@"TripItineraryRequest"];

    NSString *response = [NSString stringWithFormat:[self loadXMLFile:@"TripItineraryResponse"],
                          self.departForSFOTime,
                          self.arriveAtSFOTime,
                          self.departForSEATime,
                          self.arriveAtSEATime];
    
    [UMKMockURLProtocol expectMockHTTPPostRequestWithURL:URL request:request responseStatusCode:200 response:response];
}

@end
