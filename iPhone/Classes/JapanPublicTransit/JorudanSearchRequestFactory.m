//
//  JorudanSearchRequestFactory.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXRequest.h"
#import "DateUtils.h"
#import "JPTUtils.h"
#import "JorudanSearchRequestFactory.h"
#import "Route.h"

@implementation JorudanSearchRequestFactory

+ (CXRequest *)searchJorudanForDate:(NSDate *)date
                        fromStation:(Station *)fromStation
                          toStation:(Station *)toStation
                        viaStation1:(Station *)throughStation1
                        viaStation2:(Station *)throughStation2
                       withSeatType:(SeatType)seatType
                        isRoundTrip:(BOOL)isRoundTrip
                       isIcCardFare:(BOOL)isIcCardFare {
    
    NSString *path = [NSString stringWithFormat:@"mobile/JPTransport/SearchJorudanRoutes"];
 
    NSString *fromStationXml = [NSString stringWithFormat:@"<FromStnKey>%@</FromStnKey>", fromStation.key];
    NSString *toStationXml = [NSString stringWithFormat:@"<ToStnKey>%@</ToStnKey>", toStation.key];
    NSString *viaStation1Xml = [JorudanSearchRequestFactory xmlForViaStation1:throughStation1];
    NSString *viaStation2Xml = [JorudanSearchRequestFactory xmlForViaStation2:throughStation2];
    NSString *seatTypeXml = [NSString stringWithFormat:@"<SeatType>%d</SeatType>", seatType];
    NSString *countryKeyXml = [NSString stringWithFormat:@"<EntryCrnKey>%d</EntryCrnKey>", 73];
    NSString *roundTripXml = [NSString stringWithFormat:@"<IsRoundTrip>%@</IsRoundTrip>", [JPTUtils stringForBoolean:isRoundTrip]];
    NSString *dateXml = [NSString stringWithFormat:@"<Date>%@</Date>", [DateUtils dateFormattedForMWS:date]];
    NSString *icCardFareXml = [NSString stringWithFormat:@"<IsIcCard>%@</IsIcCard>", [JPTUtils stringForBoolean:isIcCardFare]];
    
    NSMutableString *requestBody = [[NSMutableString alloc] init];
    
    [requestBody appendString:@"<SearchJorudanRoutes>"];
    [requestBody appendString:fromStationXml];
    [requestBody appendString:toStationXml];
    
    if (viaStation1Xml != nil) {
        [requestBody appendString:viaStation1Xml];
    }
    
    if (viaStation2Xml != nil) {
        [requestBody appendString:viaStation2Xml];
    }
    
    [requestBody appendString:seatTypeXml];
    [requestBody appendString:countryKeyXml];
    [requestBody appendString:roundTripXml];
    [requestBody appendString:dateXml];
    [requestBody appendString:icCardFareXml];
    [requestBody appendString:@"</SearchJorudanRoutes>"];
     
    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path requestXML:requestBody];
    
    return cxRequest;
}

+ (NSString *)xmlForViaStation1:(Station *)station {
    return [JorudanSearchRequestFactory xmlForStationElement:@"ViaStn1Key" andValue:station.key];
}

+ (NSString *)xmlForViaStation2:(Station *)station {
    return [JorudanSearchRequestFactory xmlForStationElement:@"ViaStn2Key" andValue:station.key];
}

+ (NSString *)xmlForStationElement:(NSString *)stationName andValue:(NSString *)stationKey {
    NSString *xml = nil;
    
    if (stationKey != 0) {
        xml = [NSString stringWithFormat:@"<%@>%@</%@>", stationName, stationKey, stationName];
    }
    
    return xml;
}

@end
