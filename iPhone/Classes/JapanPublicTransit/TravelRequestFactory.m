//
//  TravelRequestFactory.m
//  ConcurMobile
//
//  Created by Richard Puckett on 12/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXRequest.h"
#import "TravelRequestFactory.h"

@implementation TravelRequestFactory

+ (CXRequest *)creditCardsForLoginId:(NSString *)loginId
                         andTravelId:(NSUInteger)travelId
                        forCardTypes:(CardType)cardTypes {
    
    NSString *path = [NSString stringWithFormat:@"/Mobile/InProduct/GetUserCreditCards"];
    
    NSString *requestTemplate =
    @"<GetUserCreditCards>"
    "</GetUserCreditCards>";
    
    NSString *requestBody = [NSString stringWithFormat:@"%@",requestTemplate];
    
    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path requestXML:requestBody];
    
    return cxRequest;
}

@end
