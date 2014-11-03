//
//  HotelRecommendationsRequestFactory.m
//  ConcurMobile
//
//  Created by Richard Puckett on 11/12/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXRequest.h"
#import "HotelRecommendationsRequestFactory.h"

@implementation HotelRecommendationsRequestFactory

+ (CXRequest *)recommendationsForLatitude:(CLLocationDegrees)latitude
                             andLongitude:(CLLocationDegrees)longitude
                                andRadius:(NSUInteger)radius {
    
    NSString *path = [NSString stringWithFormat:@"Mobile/Hotel/GetRecommendations"];
    
    // Keep elements in alphabetical order! MWS uses DataContractSerailizer which, by
    // default, will only correctly parse input in alpha-order. No, really.
    //
    NSString *requestTemplate =
    @"<HotelRecommendations>"
    "<Latitude>%f</Latitude>"
    "<Longitude>%f</Longitude>"
    "<Radius>%d</Radius>"
    "</HotelRecommendations>";
    
    // Package up the date as ISO 8601.
    //
//    NSDateFormatter* dateFormatter = [[NSDateFormatter alloc] init];
//
//    dateFormatter.locale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
//    dateFormatter.dateFormat = @"yyyy-MM-dd'T'HH:mm:ssZZZZZ";
//    
//    NSString *iso8601 = [dateFormatter stringFromDate:date];
    
    NSString *requestBody = [NSString stringWithFormat:requestTemplate, latitude, longitude, radius];

    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path requestXML:requestBody];
    
    return cxRequest;
}

@end
