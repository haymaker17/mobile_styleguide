//
//  IpmTests.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "XCTestCase+AsyncTesting.h"
#import "CTENetworkSettings.h"
#import "CTELogin.h"
#import "IpmRequest.h"

@interface IpmTests : XCTestCase

@end

@implementation IpmTests

- (void)setUp
{
    [super setUp];
    [[CTENetworkSettings sharedInstance] useRqa3ServerURL];
    [[CTENetworkSettings sharedInstance] enableDebugMode];
}

- (void)tearDown
{
    [[CTENetworkSettings sharedInstance] disableDebugMode];
    [[CTENetworkSettings sharedInstance] useRqa3ServerURL];
    [super tearDown];
}

- (void)testGetValidIpmMessages
{
    // Blocking login
    //    BOOL loginSuccessful = [CTELogin loginWithUsername:@"mswe@snwfull.com" Password:@"password1"];
    BOOL loginSuccessful = [CTELogin loginWithUsername:@"sabre@devtravel.com" Password:@"0000"];
    if (!loginSuccessful) {
        XCTFail(@"Login Failed");
        [self notify:XCTAsyncTestCaseStatusFailed];
    }
    else
    {
        // request XML
        IpmRequest *request = [[IpmRequest alloc] initWithTarget:@"webLHBanner"];
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
        
        [self waitForStatus:XCTAsyncTestCaseStatusSucceeded timeout:60];
    }
}
@end
