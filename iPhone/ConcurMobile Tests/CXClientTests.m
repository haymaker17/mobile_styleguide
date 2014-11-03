//
//  CXClientTests.m
//  ConcurMobile
//
//  Created by ernest cho on 5/21/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "XCTestCase+AsyncTesting.h"
#import "URLMock.h"

#import "CXClient.h"
#import "CXRequest.h"

#import "CTENetworkSettings.h"

@interface CXClientTests : XCTestCase

@end

/**
 *  CXClient was our old AFNetworking 1.x code, it's now deprecated.
 *
 *  These tests check that the bridge code works ok.
 *
 */
@implementation CXClientTests

- (void)setUp
{
    [super setUp];
    [[CTENetworkSettings sharedInstance] useRqa3ServerURL];

    [UMKMockURLProtocol enable];
    [UMKMockURLProtocol setVerificationEnabled:YES];
}

- (void)tearDown
{
    [UMKMockURLProtocol setVerificationEnabled:NO];
    [UMKMockURLProtocol disable];

    [[CTENetworkSettings sharedInstance] useRqa3ServerURL];
    [super tearDown];
}

/**
 Adds a mock for testGetXMLRequest
 */
- (void)addMockForGetXMLRequest
{
    NSURL *URL = [NSURL URLWithString:@"https://rqa3-cb.concursolutions.com/xmlTest.xml"];
    NSString *response = @"<xml><text>Hello World</text><text>Goodbye World</text></xml>";

    [UMKMockURLProtocol expectMockHTTPGetRequestWithURL:URL responseStatusCode:200 response:response];
}

/**
 Tests get XML call
 */
- (void)testGetXMLRequest
{
    [self addMockForGetXMLRequest];

    NSString *expectedResponse = @"<xml><text>Hello World</text><text>Goodbye World</text></xml>";
    NSString *url = @"/xmlTest.xml";

    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:url];

    [[CXClient sharedClient] performRequest:cxRequest success:^(NSString *result) {
        if ([expectedResponse isEqualToString:result]) {
            [self notify:XCTAsyncTestCaseStatusSucceeded];
        } else {
            XCTFail(@"Failure.  Unexpected response: %@", result);
            [self notify:XCTAsyncTestCaseStatusFailed];
        }
    } failure:^(NSError *error) {
        XCTFail(@"Failure: %@", error);
        [self notify:XCTAsyncTestCaseStatusFailed];
    }];

    [self waitForStatus:XCTAsyncTestCaseStatusSucceeded timeout:10];
}

/**
 Adds a mock for testPostXMLRequest
 */
- (void)addMockForPostXMLRequest
{
    NSURL *URL = [NSURL URLWithString:@"https://rqa3-cb.concursolutions.com/xmlTest2.xml"];

    NSString *request = @"<xml><user>42</user></xml>";
    NSString *response = @"<xml><text>Hello World</text><text>Goodbye World</text></xml>";

    [UMKMockURLProtocol expectMockHTTPPostRequestWithURL:URL request:request responseStatusCode:200 response:response];
}

/**
 Tests post XML call
 */
- (void)testPostXMLRequest
{
    [self addMockForPostXMLRequest];

    NSString *request = @"<xml><user>42</user></xml>";
    NSString *expectedResponse = @"<xml><text>Hello World</text><text>Goodbye World</text></xml>";

    NSString *url = @"/xmlTest2.xml";
    NSLog(@"URL: %@", url);

    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:url requestXML:request];

    [[CXClient sharedClient] performRequest:cxRequest success:^(NSString *result) {
        if ([expectedResponse isEqualToString:result]) {
            [self notify:XCTAsyncTestCaseStatusSucceeded];
        } else {
            XCTFail(@"Failure.  Unexpected response: %@", result);
            [self notify:XCTAsyncTestCaseStatusFailed];
        }
    } failure:^(NSError *error) {
        XCTFail(@"Failure: %@", error);
        [self notify:XCTAsyncTestCaseStatusFailed];
    }];

    [self waitForStatus:XCTAsyncTestCaseStatusSucceeded timeout:10];
}

@end
