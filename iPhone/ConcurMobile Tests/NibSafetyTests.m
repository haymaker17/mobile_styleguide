//
//  NibSafetyTests.m
//  ConcurMobile
//
//  Created by ernest cho on 11/14/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>

@interface NibSafetyTests : XCTestCase

@end

@implementation NibSafetyTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

/**
 Checks that checkIfNibExists works on a negative case
 */
- (void)testCheckIfNibExists
{
    if ([self checkIfNibExists:@"DOES NOT EXIST"]) {
        XCTFail(@"Found a nib that should not exist! NibSafetyTests is probably broken.");
    }
}

/**
 Verify all the nibs for HelpOverlayFactory exist
 */
- (void)testHelpOverlayFactory
{
    NSArray *nibs = @[@"iPhoneHomeOverlay",
                      @"iPhoneExpenseListOverlay",
                      @"iPhoneReceiptListOverlay",
                      @"iPhoneReportListOverlay",
                      @"iPhoneReportDetailOverlay",
                      @"iPhoneApprovalListOverlay",
                      @"iPhoneApprovalDetailOverlay",
                      @"iPadHomeOverlay",
                      @"iPadExpenseListOverlay",
                      @"iPadReceiptListOverlay",
                      @"iPadReportListOverlay",
                      @"iPadReportDetailOverlay",
                      @"iPadApprovalListOverlay",
                      @"iPadApprovalDetailOverlay",
                      @"ExampleTestDriveOverlay"];

    [self checkIfNibsExist:nibs];
}

/**
 Verify all the nibs for HomeCollectionView exist
 */
- (void)testHomeCollectionView
{
    NSArray *nibs = @[@"HomeCellDefaultLandscape",
                      @"HomeCellDefaultPortrait",
                      @"HomeCellExpenseOnlyLandscape",
                      @"HomeCellExpenseOnlyPortrait",
                      @"HomeCellTravelOnlyBookingLandscape",
                      @"HomeCellTravelOnlyBookingPortrait",
                      @"HomeCellTravelOnlyTripsLandscape",
                      @"HomeCellTravelOnlyTripsPortrait",
                      @"HomeCellApprovalOnlyLandscape",
                      @"HomeCellApprovalOnlyPortrait",
                      @"HomeCellTravelAndApprovalLandscape",
                      @"HomeCellExpenseAndTravelOnlyTripsPortrait",
                      @"HomeCellExpenseAndTravelOnlyTripsLandscape",
                      @"HomeCellGovOnlyDocumentsLandscape",
                      @"HomeCellGovOnlyDocumentsPortrait"];

    [self checkIfNibsExist:nibs];
}

/**
 Checks for all the nibs in the array.
 */
- (void)checkIfNibsExist:(NSArray *)nibs
{
    NSMutableArray *missingNibs = [[NSMutableArray alloc] init];

    for (NSString *nibName in nibs) {
        if (![self checkIfNibExists:nibName]) {
            [missingNibs addObject:nibName];
        }
    }

    if ([missingNibs count] > 0) {
        [self printMissingNibsAndFail:missingNibs];
    }
}

/**
 Prints all the missing nibs and fails the test
 */
- (void)printMissingNibsAndFail:(NSMutableArray *)missingNibs
{
    NSMutableString *errorMessage = [[NSMutableString alloc] init];
    for (NSString *nibName in missingNibs) {
        [errorMessage appendString:nibName];
        [errorMessage appendString:@"\n"];
    }

    XCTFail(@"Missing nibs!\n%@", errorMessage);
}

/**
 Checks the mainBundle for the given nibName
 */
- (BOOL)checkIfNibExists:(NSString *)nibName
{
    if([[NSBundle mainBundle] pathForResource:nibName ofType:@"nib"] != nil)
    {
        return YES;
    } else {
        return NO;
    }
}

@end
