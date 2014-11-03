//
//  ConcurURLProtocl.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/2/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ConcurURLProtocol.h"

@implementation ConcurURLProtocol


+ (BOOL)canInitWithRequest:(NSURLRequest *)request {

    NSString *bodystring = nil;
    if (request.HTTPBody.length >0){
        bodystring = [NSString stringWithUTF8String:request.HTTPBody.bytes];
    }
    NSLog(@"ConcurURLProtocl request %@ %@ %@",request.HTTPMethod,request.URL.absoluteString,  bodystring);

    if ([request.URL.absoluteString isEqualToString:@"https://www.concursolutions.com/theEndpointYouWouldLikeToMock"]) {
        return YES;
    }
    return NO;
}

+ (NSURLRequest*) canonicalRequestForRequest:(NSURLRequest *)request{
    return request;
}

- (void)startLoading {
    
    
    NSMutableString *bodystring =  [NSMutableString stringWithCapacity:1000];
    [bodystring appendString:@"<AirFilterResults>"
                            "<Choices>"];
    
    for (int i=0; i<2500; i++) {
        [bodystring appendString:@"<AirChoice>"
        "<Crn>USD</Crn>"
        "<Fare>351.8</Fare>"
        "<FareId>839f2fe4-3b30-42b3-b660-58fc6a006590</FareId>"
        "<RateType>LowestPublished</RateType>"
        "<Refundable>false</Refundable>"
        "<Segments>"
        "<AirSegment>"
        "<Distance>2336.26</Distance>"
        "<ElapsedTimeMin>300</ElapsedTimeMin>"
        "<Flights>"
        "<Flight>"
        "<AirMiles>2336</AirMiles>"
        "<AircraftCode>757</AircraftCode>"
        "<ArrivalTime>2013-09-30T17:00:00</ArrivalTime>"
        "<Bic>N</Bic>"
        "<Carrier>AA</Carrier>"
        "<DepartureTime>2013-09-30T09:00:00</DepartureTime>"
        "<Emissions>812.928</Emissions>"
        "<EndIata>MIA</EndIata>"
        "<FlightNum>0208</FlightNum>"
        "<FlightTime>300</FlightTime>"
        "<FltClass>Y</FltClass>"
        "<NumStops>0</NumStops>"
        "<OperatingCarrier />"
        "<StartIata>LAX</StartIata>"
        "<Title>ECONOMY</Title>"
        "</Flight>"
        "</Flights>"
        "<HasFlightOptions>false</HasFlightOptions>"
        "<Key>AA_N_0208_Y_LAX_9/30/2013 9:00:00 AM_839f2fe4-3b30-42b3-b660-58fc6a006590</Key>"
        "<ShowFareReview>false</ShowFareReview>"
        "</AirSegment>"
        "<AirSegment>"
        "<Distance>2336.26</Distance>"
        "<ElapsedTimeMin>335</ElapsedTimeMin>"
        "<Flights>"
        "<Flight>"
        "<AirMiles>2336</AirMiles>"
        "<AircraftCode>757</AircraftCode>"
        "<ArrivalTime>2013-10-04T16:45:00</ArrivalTime>"
        "<Bic>N</Bic>"
        "<Carrier>AA</Carrier>"
        "<DepartureTime>2013-10-04T14:10:00</DepartureTime>"
        "<Emissions>812.928</Emissions>"
        "<EndIata>LAX</EndIata>"
        "<FlightNum>0129</FlightNum>"
        "<FlightTime>335</FlightTime>"
        "<FltClass>Y</FltClass>"
        "<NumStops>0</NumStops>"
        "<OperatingCarrier />"
        "<StartIata>MIA</StartIata>"
        "<Title>ECONOMY</Title>"
        "</Flight>"
        "</Flights>"
        "<HasFlightOptions>false</HasFlightOptions>"
        "<Key>AA_N_0129_Y_MIA_10/4/2013 2:10:00 PM_839f2fe4-3b30-42b3-b660-58fc6a006590</Key>"
        "<ShowFareReview>false</ShowFareReview>"
        "</AirSegment>"
        "</Segments>"
         "</AirChoice>" ];
    }
    [bodystring appendString:@"</Choices>"
     "</AirFilterResults>"];
    
    NSHTTPURLResponse *response = [[NSHTTPURLResponse alloc] initWithURL:self.request.URL MIMEType:@"application/xml" expectedContentLength:bodystring.length textEncodingName:nil];
    
    [[self client] URLProtocol:self didReceiveResponse:response cacheStoragePolicy:NSURLCacheStorageNotAllowed];
    [[self client] URLProtocol:self didLoadData:[bodystring dataUsingEncoding:NSUTF8StringEncoding]];
    [[self client] URLProtocolDidFinishLoading:self];
    
}

- (void)stopLoading{
    NSLog(@"ConcurURLProtocl stop request %@ %@",self.request.HTTPMethod,self.request.URL.absoluteString);
}

@end
