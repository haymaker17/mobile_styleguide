//
//  FormatUtilsTests.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>

#import "FormatUtils.h"

@interface FormatUtilsTests : XCTestCase

@end

@implementation FormatUtilsTests

- (void)testMoneyFormatting {
    NSString *result = [FormatUtils formatMoneyWithNumber:[NSNumber numberWithInteger:23] crnCode:@"JPY"];
    
    XCTAssertEqualObjects(result, @"¥23");
    
    result = [FormatUtils formatMoneyWithNumber:[NSNumber numberWithFloat:23.3] crnCode:@"JPY"];
    
    XCTAssertEqualObjects(result, @"¥23");
    
    result = [FormatUtils formatMoneyWithNumber:nil crnCode:@"JPY"];
    
    XCTAssertEqualObjects(result, @"¥0");
}


// Tests for + (NSString*) formatMoney :(NSString*)amount crnCode:(NSString*) crnCode

- (void)testMoneyFormatterWithString {
    NSString *result = [FormatUtils formatMoney:@"" crnCode:@"JPY"];
    
    XCTAssertEqualObjects(result, @"¥0");
    
    result = [FormatUtils formatMoney:nil crnCode:@"JPY"];
    
    XCTAssertEqualObjects(result, @"¥0");

    result = [FormatUtils formatMoney:@" " crnCode:@"JPY"];
    
    XCTAssertEqualObjects(result, @"¥0");

    
}

- (void)testRoundingJPYCurrencyMoneyFormatter
{
    NSString *result = [FormatUtils formatMoney:@"0.0" crnCode:@"JPY"];
    
    XCTAssertEqualObjects(result, @"¥0");
    
    result = [FormatUtils formatMoney:@"0.1" crnCode:@"JPY"];
    
    XCTAssertEqualObjects(result, @"¥0");
    
    result = [FormatUtils formatMoney:@"0.5" crnCode:@"JPY"];
    
    XCTAssertEqualObjects(result, @"¥0");

    result = [FormatUtils formatMoney:@"0.6" crnCode:@"JPY"];
    
    XCTAssertEqualObjects(result, @"¥1");
    
    result = [FormatUtils formatMoney:@"0.59999" crnCode:@"JPY"];
    
    XCTAssertEqualObjects(result, @"¥1");

}

- (void)testRoundingUSDCurrencyMoneyFormatter
{
// Test USD rounding
     NSString *result = [FormatUtils formatMoney:@"0.1" crnCode:@"USD"];

    XCTAssertEqualObjects(result, @"$0.10");

    result = [FormatUtils formatMoney:@"0.5" crnCode:@"USD"];

    XCTAssertEqualObjects(result, @"$0.50");

    result = [FormatUtils formatMoney:@"0.6" crnCode:@"USD"];

    XCTAssertEqualObjects(result, @"$0.60");

    result = [FormatUtils formatMoney:@"0.59999" crnCode:@"USD"];

    XCTAssertEqualObjects(result, @"$0.60");
    
}


@end
