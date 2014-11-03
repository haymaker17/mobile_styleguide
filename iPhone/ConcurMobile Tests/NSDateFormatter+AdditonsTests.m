#import <XCTest/XCTest.h>

#import "NSDateFormatter+Additions.h"

@interface NSDateFormatterTests : XCTestCase

@end

@implementation NSDateFormatterTests


- (void)testISO8601{
    NSDateFormatter *isoFormat1 = [NSDateFormatter dateFormatterWithFormat:CCDateFormatISO8601Date timeZoneWithAbbreviation:@"GMT" locale:[NSLocale systemLocale]];
    XCTAssertTrue(([isoFormat1.dateFormat isEqualToString:CCDateFormatISO8601Date]), @"should be equal");
    XCTAssertTrue(([isoFormat1.timeZone isEqual:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]]),@"");
    XCTAssertTrue(([isoFormat1.locale isEqual:[NSLocale systemLocale]]),@"");
   
    NSDateFormatter *isoFormat2 = [NSDateFormatter dateFormatterWithFormat:CCDateFormatISO8601DateOnly timeZoneWithAbbreviation:@"GMT" locale:[NSLocale systemLocale]];
    XCTAssertTrue(([isoFormat2.dateFormat isEqualToString:CCDateFormatISO8601DateOnly]), @"should be equal");
    XCTAssertTrue(([isoFormat2.timeZone isEqual:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]]),@"");
    XCTAssertTrue(([isoFormat2.locale isEqual:[NSLocale systemLocale]]),@"");
    
    NSDateFormatter *isoFormat3 = [NSDateFormatter dateFormatterWithFormat:CCDateFormatISO8601DateTime timeZoneWithAbbreviation:@"GMT" locale:[NSLocale systemLocale]];
    XCTAssertTrue(([isoFormat3.dateFormat isEqualToString:CCDateFormatISO8601DateTime]), @"should be equal");
    XCTAssertTrue(([isoFormat3.timeZone isEqual:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]]),@"");
    XCTAssertTrue(([isoFormat3.locale isEqual:[NSLocale systemLocale]]),@"");
    
    NSDateFormatter *isoFormat4 = [NSDateFormatter dateFormatterWithFormat:CCDateFormatISO8601ZoneDate timeZoneWithAbbreviation:@"GMT" locale:[NSLocale systemLocale]];
    XCTAssertTrue(([isoFormat4.dateFormat isEqualToString:CCDateFormatISO8601ZoneDate]), @"should be equal");
    XCTAssertTrue(([isoFormat4.timeZone isEqual:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]]),@"");
    XCTAssertTrue(([isoFormat4.locale isEqual:[NSLocale systemLocale]]),@"");

}
                   
- (void)testMWSDates{
    NSDateFormatter *isoFormat1 = [NSDateFormatter dateFormatterWithFormat:CCDateFormatBooking timeZoneWithAbbreviation:@"GMT" locale:[NSLocale systemLocale]];
    XCTAssertTrue(YES, );
    XCTAssertTrue(([isoFormat1.dateFormat isEqualToString:CCDateFormatBooking]), @"should be equal");
    XCTAssertTrue(([isoFormat1.timeZone isEqual:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]]),@"");
    XCTAssertTrue(([isoFormat1.locale isEqual:[NSLocale systemLocale]]),@"");
    
    NSDateFormatter *isoFormat2 = [NSDateFormatter dateFormatterWithFormat:CCDateFormatReceiptStore timeZoneWithAbbreviation:@"GMT" locale:[NSLocale systemLocale]];
    XCTAssertTrue(([isoFormat2.dateFormat isEqualToString:CCDateFormatReceiptStore]), @"should be equal");
    XCTAssertTrue(([isoFormat2.timeZone isEqual:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]]),@"");
    XCTAssertTrue(([isoFormat2.locale isEqual:[NSLocale systemLocale]]),@"");

}

@end