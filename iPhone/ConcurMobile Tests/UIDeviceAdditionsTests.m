//
//  UIDeviceAdditionsTests.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 11/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>

@interface UIDeviceAdditionsTests : XCTestCase

@end

@implementation UIDeviceAdditionsTests

- (void)setUp
{
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown
{
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testPad
{
    if (UIUserInterfaceIdiomPad == UI_USER_INTERFACE_IDIOM()) {
        XCTAssert(([UIDevice isPad] == YES), @"it should be an Pad");
        XCTAssert(([UIDevice isPhone] == NO), @"it should not be an Phone");
        
    }
}

- (void)testPhone
{
    if (UIUserInterfaceIdiomPhone == UI_USER_INTERFACE_IDIOM()) {
        XCTAssert(([UIDevice isPhone] == YES), @"it should be an Phone");
        XCTAssert(([UIDevice isPad] == NO), @"it should not be an Pad");
        
    }
}
@end
