//
//  LineSearchRequestFactory.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXRequest.h"
#import "LineSearchRequestFactory.h"

@implementation LineSearchRequestFactory

+ (CXRequest *)searchForLineByStation:(NSUInteger)stationId {
    NSString *path = [NSString stringWithFormat:@"mobile/Expense/SearchListItems"];
    
    NSString *requestTemplate =
    @"<ListSearchCriteria>"
    "<FieldId>JPTLineKey</FieldId>"
    "<FtCode>RPTINFO</FtCode>"
    "<ParentLiKey>%@</ParentLiKey>"
    "</ListSearchCriteria>";
    
    NSString *requestBody = [NSString stringWithFormat:requestTemplate, stationId];
    
    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path requestXML:requestBody];
        
    return cxRequest;
}

@end
