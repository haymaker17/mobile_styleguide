//
//  HelpOverlayStatusListTests.m
//  ConcurMobile
//
//  Created by ernest cho on 10/30/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "HelpOverlayStatusList.h"

@interface HelpOverlayStatusListTests : XCTestCase
@property (nonatomic, readwrite, strong) NSString *mockOverlayA;
@property (nonatomic, readwrite, strong) NSString *mockOverlayB;
@property (nonatomic, readwrite, strong) NSString *mockOverlayC;
@end

@implementation HelpOverlayStatusListTests

/**
 Creates mock overlay names
 */
- (void)setUp
{
    [super setUp];
    self.mockOverlayA = @"MockOverlayA";
    self.mockOverlayB = @"MockOverlayB";
    self.mockOverlayC = @"MockOverlayC";
}

/**
 Clears the mock overlay names from the plist file
 
 If the reset doesn't work, the teardown won't work.
 */
- (void)tearDown
{
    HelpOverlayStatusList *list = [HelpOverlayStatusList sharedList];
    [list clearStatusForOverlay:self.mockOverlayA];
    [list clearStatusForOverlay:self.mockOverlayB];
    [list clearStatusForOverlay:self.mockOverlayC];

    [super tearDown];
}

/**
 Verifies an end to end disable and clear cycle.

 Checks if overlayC is disabled.  should be NO
 Disables overlayC.
 Checks if overlayC is disabled.  should be YES
 Clears overlayC.
 Checks if overlayC is disabled.  should be NO
 */
- (void)testCheckC_DisableC_CheckC_ClearC_CheckC
{
    BOOL status = [[HelpOverlayStatusList sharedList] isOverlayDisabled:self.mockOverlayC];
    if (status) {
        XCTFail(@"MockOverlayC is disabled before a disable call!");
    }

    [[HelpOverlayStatusList sharedList] disableOverlay:self.mockOverlayC];
    status = [[HelpOverlayStatusList sharedList] isOverlayDisabled:self.mockOverlayC];

    status = [[HelpOverlayStatusList sharedList] isOverlayDisabled:self.mockOverlayC];
    if (!status) {
        XCTFail(@"MockOverlayC is not disabled after a disable call!");
    }

    [[HelpOverlayStatusList sharedList] clearStatusForOverlay:self.mockOverlayC];
    status = [[HelpOverlayStatusList sharedList] isOverlayDisabled:self.mockOverlayC];
    if (status) {
        XCTFail(@"MockOverlayC is disabled after a clear call!");
    }
}

/**
 Verifies that isOverlayDisabled returns NO after a teardown/setup cycle.
 
 Checks if overlayA is disabled.  should be NO
 */
- (void)testCheckA
{
    BOOL status = [[HelpOverlayStatusList sharedList] isOverlayDisabled:self.mockOverlayA];
    if (status) {
        XCTFail(@"MockOverlay is disabled before disable call!");
    }
}

/**
 Verifies that isOverlayDisabled returns YES after calling disableOverlay.

 Disables overlayA.
 Checks if overlayA is disabled.  should be YES
 */
- (void)testDisableA_CheckA
{
    [[HelpOverlayStatusList sharedList] disableOverlay:self.mockOverlayA];

    BOOL status = [[HelpOverlayStatusList sharedList] isOverlayDisabled:self.mockOverlayA];
    if (!status) {
        XCTFail(@"MockOverlay is not disabled after a disable call!");
    }
}

/**
 Verifies that disableOverlay doesn't disable all overlays.

 Disables overlayA.
 Checks if overlayA is disabled. should be YES
 Checks if overlayB is disabled. should be NO
 */
- (void)testDisableA_CheckA_CheckB
{
    [[HelpOverlayStatusList sharedList] disableOverlay:self.mockOverlayA];

    BOOL status = [[HelpOverlayStatusList sharedList] isOverlayDisabled:self.mockOverlayA];
    if (!status) {
        XCTFail(@"MockOverlayA is not disabled after a disable call!");
    }

    status = [[HelpOverlayStatusList sharedList] isOverlayDisabled:self.mockOverlayB];
    if (status) {
        XCTFail(@"MockOverlayB is disabled when MockOverlayA should be disabled!");
    }
}

/**
 Verifies that disableOverlay doesn't disable all overlays.  Making sure check order doesn't matter.

 Disables overlayB.
 Checks if overlayA is disabled. should be NO
 Checks if overlayB is disabled. should be YES
 */
- (void)testDisableB_CheckA_CheckB
{
    [[HelpOverlayStatusList sharedList] disableOverlay:self.mockOverlayB];

    BOOL status = [[HelpOverlayStatusList sharedList] isOverlayDisabled:self.mockOverlayA];
    if (status) {
        XCTFail(@"MockOverlayA is disabled when MockOverlayB should be disabled!");
    }

    status = [[HelpOverlayStatusList sharedList] isOverlayDisabled:self.mockOverlayB];
    if (!status) {
        XCTFail(@"MockOverlayB is not disabled after a disable call!");
    }
}

@end
