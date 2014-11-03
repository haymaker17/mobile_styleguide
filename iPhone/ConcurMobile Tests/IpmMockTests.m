//
//  IpmMockTests.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "XCTestCase+AsyncTesting.h"
#import "CTENetworkSettings.h"
#import "IpmRequest.h"
#include "URLMock.h"

@interface IpmMockTests : XCTestCase

@end

@implementation IpmMockTests

- (void)setUp
{
    [super setUp];
    [[CTENetworkSettings sharedInstance] useRqa3ServerURL];
    [[CTENetworkSettings sharedInstance] enableDebugMode];
    
    // for mock server test only. will need to turn it off when doing live hotel rates testing
    [UMKMockURLProtocol enable];
    [UMKMockURLProtocol setVerificationEnabled:YES];
}

- (void)tearDown
{
    [[CTENetworkSettings sharedInstance] useRqa3ServerURL];
    [[CTENetworkSettings sharedInstance] enableDebugMode];
    
    [UMKMockURLProtocol setVerificationEnabled:NO];
    [UMKMockURLProtocol disable];
    
    [super tearDown];
}

- (NSString *)loadXMLFile:(NSString *)filename
{
    NSBundle *bundle = [NSBundle bundleForClass:[self class]];
    NSString *path = [bundle pathForResource:filename ofType:@"xml"];
    NSData *data = [NSData dataWithContentsOfFile:path];
    
    return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
}
- (void)addMockForIpmMessage
{
    // GET /mobile/ipm/getmsg?target=ios7
    NSURL *URL = [NSURL URLWithString:@"https://rqa3-cb.concursolutions.com/mobile/ipm/getmsg?target=ios7"];
    NSString *response = [self loadXMLFile:@"IpmMockMessages"];
    
    [UMKMockURLProtocol expectMockHTTPGetRequestWithURL:URL responseStatusCode:200 response:response];
}

- (void)testGetValidIpmMessagesFromMockServer
{
    [self addMockForIpmMessage];
    
    // request XML
    IpmRequest *request = [[IpmRequest alloc] initWithTarget:@"ios7"];
    [request requestIpmMessagesWithSuccess:^(NSArray *messages) {
        if ([messages count] > 0)
        {
            [self notify:XCTAsyncTestCaseStatusSucceeded];
        }
        else{
            XCTFail(@"Failure: Empty messages returned");
            [self notify:XCTAsyncTestCaseStatusFailed];
        }
    } failure:^(CTEError *error) {
        XCTFail(@"Failure: %@", error);
        [self notify:XCTAsyncTestCaseStatusFailed];
    }];
    
    [self waitForStatus:XCTAsyncTestCaseStatusSucceeded timeout:30];
}

@end
