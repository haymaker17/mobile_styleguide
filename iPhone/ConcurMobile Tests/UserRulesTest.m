//
//  UserRulesTest.m
//  ConcurMobile
//
//  Created by Sally Yan on 12/6/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>

@interface UserRulesTest : XCTestCase

@end

// All the server responses are from GDS log.

@implementation UserRulesTest

- (void)setUp
{
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown
{
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
    [[ExSystem sharedInstance] clearRoles];
}


- (void)testApprovalOnlyUser
{
    NSString *serverResponse = @"CESUser,MOBILE_EXPENSE_MANAGER,EXPENSE_ONLY_USER,Dining_User,Taxi_Without_Card,Travel_Booking_Enabled,Air_Booking_Enabled,Metro_User,FlightTracker_User,GateGuru_User,TripItAd_User";
    [[ExSystem sharedInstance] makeRoles:serverResponse];
    
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelOnly]), @"isTravelOnly should not be ApprovalOnly");
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelAndExpenseOnlyUser]), @"isTravelAndExpenseOnlyUser should not be ApprovalOnly");
    XCTAssertFalse(([[ExSystem sharedInstance] isExpenseAndApprovalOnlyUser]), @"isExpenseAndApprovalOnlyUser should not be ApprovalOnly");
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelAndApprovalOnlyUser]), @"isTravelAndApprovalOnlyUser should not be ApprovalOnly");
    XCTAssertFalse(([[ExSystem sharedInstance] isExpenseOnlyUser]), @"isExpenseOnlyUser should not be ApprovalOnly");
    
    XCTAssertTrue(([[ExSystem sharedInstance] isApprovalOnlyUser]), @"isApprovalOnlyUser");
}

- (void)testExpenseOnlyUserWithFullServerResponse
{
    NSString *serverResponse = @"CESUser,MOBILE_EXPENSE_TEST_EMP,MOBILE_EXPENSE_TRAVELER,EXPENSE_ONLY_USER,Dining_User,Taxi_Without_Card,Travel_Booking_Enabled,Air_Booking_Enabled,Metro_User,FlightTracker_User,GateGuru_User,TripItAd_User";
    [[ExSystem sharedInstance] makeRoles:serverResponse];
    
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelOnly]), @"isTravelOnly should not be an ExpenseOlnlyUser");
    XCTAssertFalse(([[ExSystem sharedInstance] isApprovalOnlyUser]), @"isApprovalOnlyUser should not be an ExpenseOlnlyUser");
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelAndExpenseOnlyUser]), @"isTravelAndExpenseOnlyUser should not be an ExpenseOlnlyUser");
    XCTAssertFalse(([[ExSystem sharedInstance] isExpenseAndApprovalOnlyUser]), @"isExpenseAndApprovalOnlyUser should not be an ExpenseOlnlyUser");
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelAndApprovalOnlyUser]), @"isTravelAndApprovalOnlyUser should not be an ExpenseOlnlyUser");
    XCTAssertTrue(([[ExSystem sharedInstance] isExpenseOnlyUser]), @"should  be an isExpenseOnlyUser");
}

- (void)testExpenseOnlyUserWithMinServerResponse
{
    NSString *serverResponse = @"EXPENSE_ONLY_USER,MOBILE_EXPENSE_TRAVELER";
    [[ExSystem sharedInstance] makeRoles:serverResponse];
    
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelOnly]), @"isTravelOnly should not be ExpenseOnly");
    XCTAssertFalse(([[ExSystem sharedInstance] isApprovalOnlyUser]), @"isApprovalOnlyUser should not be ExpenseOnly");
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelAndExpenseOnlyUser]), @"isTravelAndExpenseOnlyUser should not be ExpenseOnly");
    XCTAssertFalse(([[ExSystem sharedInstance] isExpenseAndApprovalOnlyUser]), @"isExpenseAndApprovalOnlyUser should not be ExpenseOnly");
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelAndApprovalOnlyUser]), @"isTravelAndApprovalOnlyUser should not be ExpenseOnly");
    XCTAssertTrue(([[ExSystem sharedInstance] isExpenseOnlyUser]), @"should  be an isExpenseOnlyUser");
}


- (void)testTravelAndApprovalOnlyUser
{
    NSString *serverResponse = @"CESUser,ClientCentralAdmin,DemoUser,TravelAgentAdmin,TravelPointsUser,TravelUser,MOBILE_EXPENSE_MANAGER,Dining_User,Taxi_Without_Card,Amtrak_User,Travel_Booking_Enabled,Air_Booking_Enabled,Metro_User,FlightTracker_User,GateGuru_User,TripItAd_User";
    [[ExSystem sharedInstance] makeRoles:serverResponse];
    
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelOnly]), @"isTravelOnly should not be an TravelAndApprovalOnlyUser");
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelAndExpenseOnlyUser]), @"isTravelAndExpenseOnlyUser should not be an TravelAndApprovalOnlyUser");
    XCTAssertFalse(([[ExSystem sharedInstance] isApprovalOnlyUser]), @"isApprovalOnlyUser should not be an TravelAndApprovalOnlyUser");
    XCTAssertFalse(([[ExSystem sharedInstance] isExpenseAndApprovalOnlyUser]), @"isExpenseAndApprovalOnlyUser should not be an TravelAndApprovalOnlyUser");
    XCTAssertFalse(([[ExSystem sharedInstance] isExpenseOnlyUser]), @"isExpenseOnlyUser should not be an TravelAndApprovalOnlyUser");
    
    XCTAssertTrue(([[ExSystem sharedInstance] isTravelAndApprovalOnlyUser]), @"should be an travel and approval only user");
}

- (void)testExpenseAndApprovalOnlyUser
{
    NSString *serverResponse = @"CESUser,ClientCentralAdmin,DemoUser,TravelAgentAdmin,TravelPointsUser,MOBILE_EXPENSE_TEST_EMP,MOBILE_EXPENSE_MANAGER,MOBILE_EXPENSE_TRAVELER,EXPENSE_ONLY_USER,Dining_User,Taxi_Without_Card,Amtrak_User,Travel_Booking_Enabled,Air_Booking_Enabled,Metro_User,FlightTracker_User,GateGuru_User,TripItAd_User";
    [[ExSystem sharedInstance] makeRoles:serverResponse];
    
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelOnly]), @"isTravelOnly should not be an ExpenseAndApprovalOnlyUser");
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelAndExpenseOnlyUser]), @"isTravelAndExpenseOnlyUser should not be an ExpenseAndApprovalOnlyUser");
    XCTAssertFalse(([[ExSystem sharedInstance] isApprovalOnlyUser]), @"isApprovalOnlyUser should not be an ExpenseAndApprovalOnlyUser");
    XCTAssertFalse(([[ExSystem sharedInstance] isTravelAndApprovalOnlyUser]), @"isTravelAndApprovalOnlyUser should not be an ExpenseAndApprovalOnlyUser");
    XCTAssertFalse(([[ExSystem sharedInstance] isExpenseOnlyUser]), @"isExpenseOnlyUser should not be an ExpenseAndApprovalOnlyUser");
    
    XCTAssertTrue(([[ExSystem sharedInstance] isExpenseAndApprovalOnlyUser]), @"should be an Expense and Approval only user");
}


@end
