//
//  UberTests.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "XCTestCase+AsyncTesting.h"
#import "CTENetworkSettings.h"
#import "UberRequest.h"

@interface UberTests : XCTestCase

@end

@implementation UberTests

- (void)setUp
{
    [super setUp];
    [[CTENetworkSettings sharedInstance] enableDebugMode];
}

- (void)tearDown
{
    [[CTENetworkSettings sharedInstance] disableDebugMode];
    [super tearDown];
}
//@"uber://?action=setPickup&pickup[latitude]=51.510843&pickup[longitude]=-0.602588&pickup[nickname]=Concur%20Slough&pickup[formatted_address]=7%20Bath%20Rd%2C%20Slough%2C%20Berkshire%20SL1%203UA"

- (void)testGetTimeToPickupInSlough
{
    CLLocationCoordinate2D pickup = CLLocationCoordinate2DMake(51.5309, 0.1233);
    
    UberRequest *request = [[UberRequest alloc] initWithServerToken:@"b0SBUiffHcC0pnrYpZlYFdVmlXCgKQd0sbjn_yKj"];
    [request setPickupLocation:pickup];
    [request requestTimeWithSuccess:^(NSArray *times) {
        [self notify:XCTAsyncTestCaseStatusSucceeded];
    } failure:^(CTEError *error) {
        XCTFail(@"Failure: %@", error);
        [self notify:XCTAsyncTestCaseStatusFailed];
        
    }];
    
    [self waitForStatus:XCTAsyncTestCaseStatusSucceeded timeout:60];
}

- (void)testGetPricesFromSloughToKingsCross
{
    CLLocationCoordinate2D pickup = CLLocationCoordinate2DMake(51.510843, -0.602588);
    CLLocationCoordinate2D kingsCross = CLLocationCoordinate2DMake(51.5309, 0.1233);
    
    UberRequest *request = [[UberRequest alloc] initWithServerToken:@"b0SBUiffHcC0pnrYpZlYFdVmlXCgKQd0sbjn_yKj"];
    [request setPickupLocation:pickup];
    [request setDropoffLocation:kingsCross];
    [request requestPriceWithSuccess:^(NSArray *prices) {
        [self notify:XCTAsyncTestCaseStatusSucceeded];
    } failure:^(CTEError *error) {
        XCTFail(@"Failure: %@", error);
        [self notify:XCTAsyncTestCaseStatusFailed];
        
    }];
    
    [self waitForStatus:XCTAsyncTestCaseStatusSucceeded timeout:60];
}

@end
