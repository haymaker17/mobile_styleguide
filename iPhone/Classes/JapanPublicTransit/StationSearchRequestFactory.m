//
//  StationSearchRequestFactory.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXRequest.h"
#import "StationSearchRequestFactory.h"

@implementation StationSearchRequestFactory

+ (CXRequest *)searchForStation:(NSString *)query {
    NSString *path = [NSString stringWithFormat:@"mobile/Expense/SearchListItems"];

    NSString *requestTemplate =
    @"<ListSearchCriteria>"
    "<FieldId>JPTStationKey</FieldId>"
    "<FtCode>RPTINFO</FtCode>"
    "<Query>%@</Query>"
    "</ListSearchCriteria>";
    
    NSString *requestBody = [NSString stringWithFormat:requestTemplate, query];
    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path requestXML:requestBody];
    return cxRequest;
}

@end
