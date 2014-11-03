//
//  ConcurSDKIntegrationTests.m
//  ConcurMobile
//
//  Created by ernest cho on 3/18/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "XCTestCase+AsyncTesting.h"
#import "CTENetworkSettings.h"
#import "CTELogin.h"
#import "CTEError.h"
#import "CTEUserLookupResult.h"
#import "CTEHotelSearch.h"
#import "CTEDateUtility.h"
#import "CTEHotel.h"
#import "CTELocation.h"
#import "CTELocationSearch.h"

@interface ConcurSDKIntegrationTests : XCTestCase

@end

@implementation ConcurSDKIntegrationTests

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

// Verify that user login works
- (void)testConcurMobileUserLogin
{
    [CTELogin loginConcurMobileWithUsername:@"ernest@snwfull.com" Password:@"password1" success:^(NSString *loginXML) {
        NSLog(@"XML Returned: %@", loginXML);
        [self notify:XCTAsyncTestCaseStatusSucceeded];
    } failure:^(CTEError *error) {
        XCTFail(@"Network Error");
    }];

    [self waitForStatus:XCTAsyncTestCaseStatusSucceeded timeout:20];
}

// Verify that user lookup works
- (void)testUserLookupByEmail
{
    [CTELogin lookupUserByEmailOrUsername:@"ernest@snwfull.com" success:^(CTEUserLookupResult *userLookupResult) {
        if ([userLookupResult username] && [[userLookupResult username] isEqualToString:@"ernest@snwfull.com"]) {
            [self notify:XCTAsyncTestCaseStatusSucceeded];
        } else {
            XCTFail(@"Unexpected username: %@", [userLookupResult username]);
        }
    } failure:^(CTEError *error) {
        XCTFail(@"Network Error: %@", error);
    }];

    [self waitForStatus:XCTAsyncTestCaseStatusSucceeded timeout:20];
}

// Verify that user lookup works
- (void)testSSOUserLookupByEmail
{
    [CTELogin lookupUserByEmailOrUsername:@"exo@snwexsso.com" success:^(CTEUserLookupResult *userLookupResult) {
        if ([userLookupResult username] && [[userLookupResult username] isEqualToString:@"exo@snwexsso.com"]) {
            if ([userLookupResult companySingleSignOnURL]) {
                [self notify:XCTAsyncTestCaseStatusSucceeded];
            } else {
                XCTFail(@"No SSO URL returned");
            }
        } else {
            XCTFail(@"Unexpected username: %@", [userLookupResult username]);
        }
    } failure:^(CTEError *error) {
        XCTFail(@"Network Error: %@", error);
    }];

    [self waitForStatus:XCTAsyncTestCaseStatusSucceeded timeout:20];
}

// pavan is having issues with this method
// https://jira.concur.com/jira/browse/MOB-19702
- (void)testSetUserAgent
{
    NSString *userAgent = @"Ernest's test user agent";
    [[CTENetworkSettings sharedInstance] setUserAgentString:userAgent];
    XCTAssert([userAgent isEqualToString:[[CTENetworkSettings sharedInstance] getUserAgentString]], @"UserAgent string was not set correctly.");
}

// This test does always NOT pass, the reason is the way unit tests work on iOS is kindof broken
// the app is allowed to startup and it overwrites the test server url with the saved url
// We'll have to research this later
// https://jira.concur.com/jira/browse/MOB-19513
- (void)testHotelSearchNonBlocking
{
    [[CTENetworkSettings sharedInstance] useRqa3ServerURL];
    NSLog(@"URL 1: %@", [[CTENetworkSettings sharedInstance] serverURL]);

    // Blocking login
    BOOL loginSuccessful = [CTELogin loginWithUsername:@"mswe@snwfull.com" Password:@"password1"];
    if (!loginSuccessful) {
        XCTFail(@"Login Failed");
    }

    // URL changes here if the app wasn't already set to rqa3!  This is dumb.
    NSLog(@"URL 2: %@", [[CTENetworkSettings sharedInstance] serverURL]);

    // approx seattle lat/long
    double latitude = -122.3;
    double longitude = 47.6;

    // reasonable dates
    NSDate *checkInDate = [CTEDateUtility addDaysToDate:[NSDate date] daysToAdd:3];
    NSDate *checkOutDate = [CTEDateUtility addDaysToDate:checkInDate daysToAdd:3];

    // request XML
    CTEHotelSearch *request = [[CTEHotelSearch alloc] initWithHotelName:@"" latitude:latitude longitude:longitude checkInDate:checkInDate checkOutDate:checkOutDate];

    NSLog(@"URL 3: %@", [[CTENetworkSettings sharedInstance] serverURL]);

    [request searchWithSuccess:^(NSArray *hotels) {
        //test
        NSLog(@"Test Search Passed : Number of hotels returned :%d ", [hotels count]);
//        NSArray *list =  hotels.hotels;
//        for (CTEHotel *entry in list) {
//            NSLog(@"Hotel city and state : %@, %@, image URL: %@", entry.city,entry.stateAbbrevation , entry.thumbnailUrl);
//        }
        
        [self notify:XCTAsyncTestCaseStatusSucceeded];
        
    } failure:^(CTEError *error) {
        //TEst
        XCTFail(@"Hotel Search Failed with error : %@",error.description);
    }];

     // TODO: check that the list isn't empty and has expected values
    [self waitForStatus:XCTAsyncTestCaseStatusSucceeded timeout:20];

}

- (void)testNonBlockingLocationSearch
{
    [[CTENetworkSettings sharedInstance] useRqa3ServerURL];
    NSLog(@"URL 1: %@", [[CTENetworkSettings sharedInstance] serverURL]);
    
    // Blocking login
    BOOL loginSuccessful = [CTELogin loginWithUsername:@"mswe@snwfull.com" Password:@"password1"];
    if (!loginSuccessful) {
        XCTFail(@"Login Failed");
    }
    
    // URL changes here if the app wasn't already set to rqa3!  This is dumb.
    NSLog(@"URL 2: %@", [[CTENetworkSettings sharedInstance] serverURL]);
    // do hotel search
    CTELocationSearch *locationSearch = [[CTELocationSearch alloc] initWithAddress:@"sea" isAirportsOnly:NO];
    //    NSArray *locationsList = [locationSearch search];
    [locationSearch searchLocationsWithSuccess:^(NSArray *locations) {
        NSLog(@"Number of locations found : %d", [locations count]);
        if ([locations count] > 0) {
                 [self notify:XCTAsyncTestCaseStatusSucceeded];
        }
    } failure:^(CTEError *error) {
        XCTFail(@"Hotel Search Failed with errror : %@",error.description);
    }];
    
    // TODO: check that the list isn't empty and has expected values
    [self waitForStatus:XCTAsyncTestCaseStatusSucceeded timeout:20];
}


@end
