//
//  GoGoPurchaseRequestFactory.m
//  ConcurMobile
//
//  Created by Richard Puckett on 11/27/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXAuthenticatedRequest.h"
#import "CXRequest.h"
#import "GoGoPurchaseRequestFactory.h"

@implementation GoGoPurchaseRequestFactory

+ (CXRequest *)sendPurchaseRequest {
    NSString *path = [NSString stringWithFormat:@"where/we/going"];
    
    NSString *requestTemplate =
    @"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:gog=\"http://gogoair.com/ws/gogostorefront\">"
    "<soapenv:Header />"
    "<soapenv:Body>"
    "<gog:GogoPurchase>"
    "<!--You may enter the following 10 items in any order-->"
    "<gog:VendorId>CNQR100</gog:VendorId>"
    "<gog:TransactionId>cbea7ffb-1fa8-40f9-838f-319f15788289</gog:TransactionId>"
    "<gog:SessionId>GOGO16461</gog:SessionId>"
    "<gog:AirlineCode>AAL</gog:AirlineCode>"
    "<gog:PurchaseTime>2013-11-05T13:26:39.2899021-05:00</gog:PurchaseTime>"
    "<gog:Payer>"
    "<!--You may enter the following 2 items in any order-->"
    "<gog:PaymentCard>"
    "<gog:CardNumber>xxxxxxxxxxxx0435</gog:CardNumber>"
    "<gog:NameOnCard>Edward L Johns</gog:NameOnCard>"
    "<gog:ExpirationMonth>10</gog:ExpirationMonth>"
    "<gog:ExpirationYear>14</gog:ExpirationYear>"
    "<!-- Optional for Gogo. -->"
    "<!--<gog:CVVNumber>123</gog:CVVNumber>-->"
    "<gog:BillingAddress>"
    "<!--Optional:-->"
    "<!--<gog:AddressLine1>?</gog:AddressLine1> -->"
    "<!--Optional:-->"
    "<!--<gog:City>?</gog:City> -->"
    "<!--Optional:-->"
    "<!--<gog:StateOrRegion>?</gog:StateOrRegion> -->"
    "<!--Optional:-->"
    "<!--<gog:Country>?</gog:Country>-->"
    "<gog:PostalCode>43214</gog:PostalCode>"
    "</gog:BillingAddress>"
    "</gog:PaymentCard>"
    "<gog:EmailAddress>Edd.Johns@alliancedata.com</gog:EmailAddress>"
    "</gog:Payer>"
    "<gog:CustomerOrders>"
    "<!--1 or more repetitions:-->"
    "<gog:CustomerOrder>"
    "<gog:EmailAddress>Edd.Johns@alliancedata.com</gog:EmailAddress>"
    "<!--Optional:-->"
    "<gog:CustomerName>"
    "<!--You may enter the following 4 items in any order-->"
    "<!--Optional:-->"
    "<gog:FirstName>Edward</gog:FirstName>"
    "<gog:LastName>Johns</gog:LastName>"
    "</gog:CustomerName>"
    "<gog:OrderLines>"
    "<!--1 or more OrderLines -->"
    "<gog:OrderLine>"
    "<gog:ProductCode>AGTALT5324</gog:ProductCode>"
    "<gog:Price>14.00</gog:Price>"
    "<gog:Tax>0.00</gog:Tax>"
    "</gog:OrderLine>"
    "</gog:OrderLines>"
    "</gog:CustomerOrder>"
    "</gog:CustomerOrders>"
    "<!--Optional:-->"
    "<gog:Locale>en_US</gog:Locale>"
    "</gog:GogoPurchase>"
    "</soapenv:Body>"
    "</soapenv:Envelope>";
    
//    NSString *requestBody = [NSString stringWithFormat:requestTemplate, stationId];
//    
    CXRequest *cxRequest = [CXAuthenticatedRequest requestWithMethod:@"POST"
                                                                path:@"path"];
    
//    NSMutableURLRequest *urlRequest = cxRequest.urlRequest;
//    
//    urlRequest.HTTPBody = [requestBody dataUsingEncoding:NSUTF8StringEncoding];
    
    return cxRequest;
}

@end
