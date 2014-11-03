//
//  AddRouteExpenseToReportRequestFactory.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AddRouteExpenseToReportRequestFactory.h"
#import "CXRequest.h"
#import "DateUtils.h"
#import "JPTUtils.h"

@implementation AddRouteExpenseToReportRequestFactory

+ (CXRequest *)addRouteExpense:(RouteExpense *)routeExpense toReport:(NSString *)reportKey {
    NSString *path = [NSString stringWithFormat:@"mobile/JPTransport/AddJpyTransRoutes"];
    
    NSString *requestTemplate =
    @"<AddJpyTransRoutes>"
    "<RptKey>%@</RptKey>"
    "<ParentOrigin>%@</ParentOrigin>"
    "%@"
    "</AddJpyTransRoutes>";
    
    // 2013-08-08
    
    NSString *routeTemplate =
    @"<Route>"
    "<TransDate>%@</TransDate>"
    "<FromStnKey>%@</FromStnKey>"
    "<FromStnName>%@</FromStnName>"
    "<FromLneKey>%@</FromLneKey>"
    "<FromLneName>%@</FromLneName>"
    "<FromIsCommuterPass>%@</FromIsCommuterPass>"
    "<ToStnKey>%@</ToStnKey>"
    "<ToStnName>%@</ToStnName>"
    "<ToLneKey>%@</ToLneKey>"
    "<ToLneName>%@</ToLneName>"
    "<ToIsCommuterPass>%@</ToIsCommuterPass>"
    "<Cost>%@</Cost>"
    "<AdditionalCharge>%@</AdditionalCharge>"
    "<IsPersonalExpense>%@</IsPersonalExpense>"
    "<IsFavorite>%@</IsFavorite>"
    "<IsRoundTrip>%@</IsRoundTrip>"
    "<BusinessPurpose>%@</BusinessPurpose>"
    "<Comment>%@</Comment>"
    "</Route>";
    
    NSString *allRouteXml = [[NSString alloc] init];
    
    for (int i = 0, ii = [routeExpense.route.segments count]; i < ii; i++) {
        NSString *routeXml = [AddRouteExpenseToReportRequestFactory createRouteXmlForRouteExpense:routeExpense
                                                                                  andSegmentIndex:i
                                                                                     withTemplate:routeTemplate];
        
        allRouteXml = [allRouteXml stringByAppendingString:routeXml];
    }
    
//    Segment *firstSegment = routeExpense.route.firstSegment;
//    Segment *lastSegment = routeExpense.route.lastSegment;
    
    NSString *requestBody = [NSString stringWithFormat:requestTemplate, reportKey, routeExpense.route.entryType, allRouteXml];
    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path requestXML:requestBody];

    return cxRequest;
}

+ (NSString *)createRouteXmlForRouteExpense:(RouteExpense *)routeExpense andSegmentIndex:(int)i withTemplate:(NSString *)routeTemplate {
    Segment *segment = [routeExpense.route.segments objectAtIndex:i];
    
    // Segment-specific
    //
    NSString *fromStnKey = segment.fromStation.key;
    NSString *fromStnName = segment.fromStation.name;
    NSString *fromLneKey = segment.line.key;
    NSString *fromLneName = segment.line.name;
    NSString *fromIsCommuterPass = [JPTUtils stringForBoolean:segment.fromIsCommuterPass];
    
    NSString *toStnKey = segment.toStation.key;
    NSString *toStnName = segment.toStation.name;
    NSString *toLneKey = segment.line.key;
    NSString *toLneName = segment.line.name;
    NSString *toIsCommuterPass = [JPTUtils stringForBoolean:segment.toIsCommuterPass];
    
    NSString *cost = [JPTUtils stringForFare:segment.fare];
    NSString *additionalCharge = [JPTUtils stringForFare:segment.additionalCharge];
    
    // Route-specific.
    //
    NSString *transDate = [DateUtils dateFormattedForMWS:routeExpense.route.date];
    NSString *isPersonalExpense = [JPTUtils stringForBoolean:routeExpense.isPersonalExpense];
    NSString *isFavorite = [JPTUtils stringForBoolean:routeExpense.isFavorite];
    NSString *isRoundTrip = [JPTUtils stringForBoolean:routeExpense.route.isRoundTrip];
    NSString *businessPurpose = routeExpense.purpose;
    NSString *comment = routeExpense.comment;
    
    NSString *routeXml = [NSString stringWithFormat:routeTemplate,
                          transDate,
                          
                          fromStnKey,
                          fromStnName,
                          fromLneKey,
                          fromLneName,
                          fromIsCommuterPass,
                          
                          toStnKey,
                          toStnName,
                          toLneKey,
                          toLneName,
                          toIsCommuterPass,
                          
                          cost,
                          additionalCharge,
                          isPersonalExpense,
                          isFavorite,
                          isRoundTrip,
                          
                          businessPurpose,
                          comment
                          ];

    return routeXml;
}

@end
