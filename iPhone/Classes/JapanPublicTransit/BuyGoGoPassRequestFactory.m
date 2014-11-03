//
//  BuyGoGoPassRequestFactory.m
//  ConcurMobile
//
//  Created by Richard Puckett on 12/9/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "BuyGoGoPassRequestFactory.h"

@implementation BuyGoGoPassRequestFactory

+ (CXRequest *)buyGoGoPassWithItineraryLocator:(NSString *)itineraryLocator
                                 andSegmentKey:(NSString *)segmentKey
                               andCreditCardId:(NSString *)creditCardId {
    
    NSString *path = [NSString stringWithFormat:@"Mobile/WebMobile/InTouch/BuyGogoPass"];

    NSString *requestTemplate =
    @"<GoGoPassPurchase>"
    @"<CreditCardId>%@</CreditCardId>"
    @"<ItineraryLocator>%@</ItineraryLocator>"
    @"<PassType>DAY</PassType>"
    @"<SegmentKey>%@</SegmentKey>"
    @"<SessionId>%@</SessionId>"
    @"</GoGoPassPurchase>";
    
    NSString *sessionId = [[ExSystem sharedInstance] sessionID];
    
    NSString *requestBody = [NSString stringWithFormat:requestTemplate,
                             creditCardId,
                             itineraryLocator,
                             segmentKey,
                             sessionId];
    
    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path requestXML:requestBody];
    
    return cxRequest;
}

@end
