//
//  CCDateUtilitiesTests.m
//  ConcurMobile
//
//  Created by ernest cho on 1/10/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "CCDateUtilities.h"
#import "NSDateFormatter+Additions.h"
#import "DateTimeFormatter.h"

@interface CCDateUtilitiesTests : XCTestCase

@end

@implementation CCDateUtilitiesTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

/**
 test DateFormatterWithFormat function of the NSDateFormatter+Additions class. The timezone part only for
 */
-(void)testDateFormatterWithFormat
{
//    (NSDateFormatter *)dateFormatterWithFormat:(NSString *)format timeZoneWithAbbreviation:(NSString *)timeZone locale:(NSLocale *)locale
    NSLocale *currentLocale = [NSLocale currentLocale];
    NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_CA"];
    NSString *timeZone = [[NSTimeZone localTimeZone] abbreviation];
    
    NSDateFormatter *dateFormatter1 = [NSDateFormatter dateFormatterWithFormat:CCDateFormatMMMyyy timeZoneWithAbbreviation:@"GMT" locale:currentLocale];
    NSDateFormatter *dateFormatter2 = [NSDateFormatter dateFormatterWithFormat:CCDateFormatISO8601DateTime timeZoneWithAbbreviation:timeZone locale:locale];
    NSDateFormatter *dateFormatter3 = [NSDateFormatter dateFormatterWithFormat:nil timeZoneWithAbbreviation:nil locale:nil];
    
    NSString *dateFormat1 = [dateFormatter1 dateFormat];
    NSString *dateFormat2 = [dateFormatter2 dateFormat];
    NSString *dateFormat3 = [dateFormatter3 dateFormat];
    
    NSString *locale1 = [[dateFormatter1 locale] localeIdentifier];
    NSString *locale2 = [[dateFormatter2 locale] localeIdentifier];
    NSString *locale3 = [[dateFormatter3 locale] localeIdentifier];
    
    NSString *timeZone1 = [[dateFormatter1 timeZone] abbreviation];
    NSString *timeZone2 = [[dateFormatter2 timeZone] abbreviation];
    NSString *timeZone3 = [[dateFormatter3 timeZone] abbreviation];
    
    XCTAssertFalse(dateFormatter1 == nil, @"Function DateFormatterWithFormat fail - Formater1 is null!");
    XCTAssertFalse(dateFormatter2 == nil, @"Function DateFormatterWithFormat fail - Formater1 is null!");
    XCTAssertFalse(dateFormatter3 == nil, @"Function DateFormatterWithFormat fail - Formater1 is null!");
    
    XCTAssertTrue([dateFormat1 isEqualToString:CCDateFormatMMMyyy], @"dateFormat1 date time format does not match");
    XCTAssertTrue([dateFormat2 isEqualToString:CCDateFormatISO8601DateTime], @"dateFormat2's date time format does not match");
    XCTAssertTrue([dateFormat3 isEqualToString:@""], @"dateFormat3's date time format does not match");
    
    XCTAssertTrue([locale1 isEqualToString:currentLocale.localeIdentifier], @"dateFormat1's locale does not match");
    XCTAssertTrue([locale2 isEqualToString:locale.localeIdentifier], @"dateFormat2's locale does not match");
    XCTAssertTrue([locale3 isEqualToString:@""], @"dateFormat2's locale does not match");
    
    BOOL isLocalTimeZone = [timeZone3 isEqualToString:@"GMT-8"] || [timeZone3 isEqualToString:@"PST"] || [timeZone3 isEqualToString:@"PDT"];
    XCTAssertTrue([timeZone1 isEqualToString:@"GMT"], @"dateFormat1's actual output %@ timezone1 does not match", timeZone1);
    XCTAssertTrue([timeZone2 isEqualToString:timeZone], @"dateFormat2's actual output %@ timezone2 does not match", timeZone2);
    XCTAssertTrue(isLocalTimeZone, @"dateFormat2's actual output %@ timezone3 does not match", timeZone3);
}

/**
 Verifies the output from formatDateForExchangeRateEndpoint.
 */
- (void)checkFormatDateForExchangeRateEndpoint:(NSString *)input expectedOutput:(NSString *)expectedOutput
{
    NSString *actualOutput = [CCDateUtilities formatDateForExchangeRateEndpoint:input];
    XCTAssertTrue([expectedOutput isEqualToString:actualOutput], @"%@ does not match %@", expectedOutput, actualOutput);
}

/**
 Checks formatDateForExchangeRateEndpoint against a list of expected inputs and outputs.
 
 All these are valid inputs and responses.  I'm not testing Apple's ability to accept corrupt formats.
 */
- (void)testFormatDateForExchangeRateEndpoint
{
    NSDictionary *testData = @{// new years eve and new years day
                               @"2014-01-01T00:00:00": @"2014-01-01",
                               @"2013-12-31T00:00:00": @"2013-12-31",
                               // a leap year
                               @"2012-02-29T00:00:00": @"2012-02-29",

                               // currently, the server always returns midnight.
                               // these tests make sure things don't break if times are returned.
                               @"2014-01-01T11:59:59": @"2014-01-01",
                               @"2013-12-31T23:59:59": @"2013-12-31",

                               @"2014-11-11T11:11:11": @"2014-11-11",
                               @"2013-06-06T01:00:00": @"2013-06-06",

                               @"2014-01-01T00:00:01": @"2014-01-01",
                               @"2013-12-31T00:01:00": @"2013-12-31",
                               
                               // dates that broke in CRMC-43667
                               @"2014-07-06T00:00:00": @"2014-07-06",
                               @"2013-07-06T00:00:00": @"2013-07-06"};

    for (id key in testData) {
        [self checkFormatDateForExchangeRateEndpoint:key expectedOutput:[testData objectForKey:key]];
    }
}

/**  test convert date string returned from server in format yyyy-MM-dd'T'HH:mm:ss.
     Don't use this for testing when the locale is set to Japan because am/pm is written in Japanese
 */
- (void)testFormatDateToNSDateYYYYMMddTHHmmss
{
    // it seems there's a bug in ios because it cannot force to 12h even in the general setting for US, Canada, and Austraili
    NSDictionary *testData = nil;
    if (![self canForce12Hr] || [self is24Hr]){
        testData = @{// new years eve and new years day
                               @"1995-01-01T23:59:59": @"1995-01-01 23:59:59",
                               @"2014-01-01T00:00:00": @"2014-01-01 00:00:00",
                               @"2013-12-31T00:00:00": @"2013-12-31 00:00:00",
                               // a leap year
                               @"2012-02-29T00:00:00": @"2012-02-29 00:00:00",
                               
                               // currently, the server always returns midnight.
                               // these tests make sure things don't break if times are returned.
                               @"2014-01-01T11:59:59": @"2014-01-01 11:59:59",
                               @"2013-12-31T23:59:59": @"2013-12-31 23:59:59",
                               
                               @"2014-11-11T11:11:11": @"2014-11-11 11:11:11",
                               @"2013-06-06T01:00:00": @"2013-06-06 01:00:00",
                               
                               @"2014-01-01T00:00:01": @"2014-01-01 00:00:01",
                               @"2013-12-31T00:01:00": @"2013-12-31 00:01:00",
                               
                               // dates that broke in CRMC-43667
                               @"2014-07-06T00:00:00": @"2014-07-06 00:00:00",
                               @"2013-07-06T00:00:00": @"2013-07-06 00:00:00"};
    }else {
        testData = @{// new years eve and new years day
                     @"1995-01-01T23:59:59": @"1995-01-01 11:59:59 pm",
                     @"2014-01-01T00:00:00": @"2014-01-01 12:00:00 am",
                     @"2013-12-31T00:00:00": @"2013-12-31 12:00:00 am",
                     // a leap year
                     @"2012-02-29T00:00:00": @"2012-02-29 12:00:00 am",
                     
                     // currently, the server always returns midnight.
                     // these tests make sure things don't break if times are returned.
                     @"2014-01-01T11:59:59": @"2014-01-01 11:59:59 am",
                     @"2013-12-31T23:59:59": @"2013-12-31 11:59:59 pm",
                     
                     @"2014-11-11T11:11:11": @"2014-11-11 11:11:11 am",
                     @"2013-06-06T01:00:00": @"2013-06-06 01:00:00 am",
                     
                     @"2014-01-01T00:00:01": @"2014-01-01 12:00:01 am",
                     @"2013-12-31T00:01:00": @"2013-12-31 12:01:00 am",
                     
                     // dates that broke in CRMC-43667
                     @"2014-07-06T00:00:00": @"2014-07-06 12:00:00 am",
                     @"2013-07-06T00:00:00": @"2013-07-06 12:00:00 am"};
    }
    
    for (id key in testData) {
        NSDate *date = [CCDateUtilities formatDateToNSDateYYYYMMddTHHmmss:key];
        
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
        [dateFormatter setLocale:[NSLocale currentLocale]];
        [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
        
        NSString *output = [dateFormatter stringFromDate:date];
        NSString *expectedOutput = [testData objectForKey:key];
        XCTAssertFalse(![output isEqualToString:expectedOutput], @"%@ actual output does not matches expected output %@", output, expectedOutput);
    }
}


//  test for format MMM yyyy
- (void)testFormatDateToMonthAndYear
{
    NSDictionary *testData = @{// new years eve and new years day
                               @"1995-01-01T00:00:00": @"Jan 1995",
                               @"2014-01-01T00:00:00": @"Jan 2014",
                               @"2013-12-31T00:00:00": @"Dec 2013",
                               // a leap year
                               @"2012-02-29T00:00:00": @"Feb 2012",
                               
                               // currently, the server always returns midnight.
                               // these tests make sure things don't break if times are returned.
                               @"2014-01-01T11:59:59": @"Jan 2014",
                               @"2013-12-31T23:59:59": @"Dec 2013",
                               
                               @"2014-11-11T11:11:11": @"Nov 2014",
                               @"2013-06-06T01:00:00": @"Jun 2013",
                               
                               @"2014-01-01T00:00:01": @"Jan 2014",
                               @"2013-12-31T00:01:00": @"Dec 2013",
                               
                               @"2014-09-30T00:00:00": @"Sep 2014"};
    
    for (id key in testData) {
        NSString *actualOutput = [CCDateUtilities formatDateToMonthAndYear:key];
        XCTAssertFalse([actualOutput isEqualToString:@""], @"cannot pase the date string %@", key);
        XCTAssertTrue([actualOutput isEqualToString:[testData objectForKey:key]], @"%@ does not match expected output %@", actualOutput, key);
    }
}

#pragma mark - testing date fromat for Expense

- (void) testGetLocalDateAsString
{
    // for this case, the server just returns a date at midnight in GMT, and when it is convert to local time when saving in the core data
    NSDictionary *testData = nil;
    if ([self is24Hr] || ![self canForce12Hr]) {
        testData = @{// new years eve and new years day
                               @"1995-01-01 16:00:00":  @"1995-01-02T00:00:00",
                               @"2014-01-01 16:00:00":  @"2014-01-02T00:00:00",
                               @"2013-12-31 16:00:00":  @"2014-01-01T00:00:00",
                               
                               // a leap year
                               @"2012-02-29 16:00:00":  @"2012-03-01T00:00:00",
                               
                               @"2014-11-11 16:00:00": @"2014-11-12T00:00:00",
                               
                                // daylight saving
                               @"2013-06-06 17:00:00": @"2013-06-07T00:00:00",
                               @"2030-09-30 17:00:00": @"2030-10-01T00:00:00"};
    } else {
        testData = @{// new years eve and new years day
                     @"1995-01-01 16:00:00":  @"1995-01-02T12:00:00 am",
                     @"2014-01-01 16:00:00":  @"2014-01-02T12:00:00 am",
                     @"2013-12-31 16:00:00":  @"2014-01-01T12:00:00 am",
                     
                     // a leap year
                     @"2012-02-29 16:00:00":  @"2012-03-01T12:00:00 am",
                     
                     @"2014-11-11 16:00:00": @"2014-11-12T12:00:00 am",
                     
                     // daylight saving
                     @"2013-06-06 17:00:00": @"2013-06-07T12:00:00 am",
                     @"2030-09-30 17:00:00": @"2030-10-01T12:00:00 am"};
    }
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setTimeZone:[NSTimeZone localTimeZone]];
    [dateFormatter setLocale:[NSLocale currentLocale]];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    
    for (id key in testData){
        if (![self is24Hr] && [self canForce12Hr]){
            NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
            [dateFormatter setLocale:enUSPOSIXLocale];
        }
        NSDate *date = [dateFormatter dateFromString:key];
        NSString *actualOutput = [CCDateUtilities formatDateToISO8601DateTimeInString:date];
        XCTAssertTrue([actualOutput isEqualToString:[testData objectForKey:key]], @"actual out %@ put does not match the expected output %@ of input source %@", actualOutput, [testData objectForKey:key], key);
    }
    
}

/**
 testing the date format when the device is setting to 12hr and 24 hr
 */
- (void) testGetDateFormatString
{
    NSString *actualOutput = [CCDateUtilities getDateFormatString];
    NSString *expectedOutput = @"";
    if ([self is24Hr]) {
        expectedOutput = @"yyyy-MM-dd'T'HH:mm:ss";
    } else {
        expectedOutput = @"yyyy-MM-dd'T'hh:mm:ss";
    }
    XCTAssertFalse([actualOutput isEqualToString:@""], @"actual output is null for getDateFormate method!");
    XCTAssertFalse([actualOutput isEqualToString:@""], @"expected output is null for getDateFormate method!");
    XCTAssertTrue([actualOutput isEqualToString:expectedOutput], @"expected output does not match for getDateFormate method!");
}

- (void) testFormatDateToYearMonthDateTimeZoneMidNight
{
    NSDictionary *testData = @{// new years eve and new years day
                 @"1995-01-01 23:59:59": @"1995-01-01T00:00:00",
                 @"2014-01-01 00:00:00": @"2014-01-01T00:00:00",
                 @"2013-12-31 00:00:00": @"2013-12-31T00:00:00",
                 // a leap year
                 @"2012-02-29 00:00:00": @"2012-02-29T00:00:00",
                 
                 @"2014-01-01 11:59:59": @"2014-01-01T00:00:00",
                 @"2013-12-31 23:59:59": @"2013-12-31T00:00:00",
                 
                 @"2014-11-11 11:11:11": @"2014-11-11T00:00:00",
                 @"2013-06-06 01:00:00": @"2013-06-06T00:00:00",
                 
                 @"2014-01-01 00:00:01": @"2014-01-01T00:00:00",
                 @"2013-12-31 00:01:00": @"2013-12-31T00:00:00",
                 
                 // dates that broke in CRMC-43667
                 @"2014-07-06 00:00:00": @"2014-07-06T00:00:00",
                 @"2013-07-06 00:00:00": @"2013-07-06T00:00:00"};
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setTimeZone:[NSTimeZone localTimeZone]];
    NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
    [dateFormatter setLocale:enUSPOSIXLocale];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    
    for (id key in testData){
        NSDate *date = [dateFormatter dateFromString:key];
        NSString *actualOutput = [CCDateUtilities formatDateToYearMonthDateTimeZoneMidNight:date];
        XCTAssertTrue([actualOutput isEqualToString:[testData objectForKey:key]], @"actual output %@ does not match expected output %@ for input source %@", actualOutput, [testData objectForKey:key], key);
    }
}

/**
 Function DateYYYYMMddByNSDate only needs time. The input data should be NSDate with local timezone
 */
- (void) testformatDateYYYYMMddByNSDate
{
    NSDictionary *testDataPSTTimezone = @{// new years eve and new years day
                               @"1995-01-01 16:00:00": @"1995-01-02",
                               @"2014-01-01 16:00:00": @"2014-01-02",
                               @"2013-12-31 16:00:00": @"2014-01-01",
                               @"2013-06-06 17:00:00": @"2013-06-07",   // it is daylight saving in Jun, so need add 1 hr
                               // a leap year
                               @"2012-02-29 16:00:00": @"2012-03-01"};
    
    NSDictionary *testDataGMTTimezone = @{// new years eve and new years day
                                          @"1995-01-01 00:00:00": @"1995-01-01",
                                          @"2014-01-01 00:00:00": @"2014-01-01",
                                          @"2013-12-31 00:00:00": @"2013-12-31",
                                          @"2013-06-06 00:00:00": @"2013-06-06",
                                          // a leap year
                                          @"2012-02-29 00:00:00": @"2012-02-29"};
    
    NSTimeZone *pstTimeZone = [[NSTimeZone alloc] initWithName:@"PST"];
    NSTimeZone *gmtTimeZone = [[NSTimeZone alloc] initWithName:@"GMT"];
    NSDateFormatter *dateFormatterPST = [self getFormatterWithFormat:@"yyyy-MM-dd HH:mm:ss" locale:[NSLocale currentLocale] timeZone:pstTimeZone];
    NSDateFormatter *dateFormatterGMT = [self getFormatterWithFormat:@"yyyy-MM-dd HH:mm:ss" locale:[NSLocale currentLocale] timeZone:gmtTimeZone];
    
    for (id key in testDataPSTTimezone){
        NSDate *date = [dateFormatterPST dateFromString:key];
        NSString *actualOutput = [CCDateUtilities formatDateYYYYMMddByNSDate:date];
        XCTAssertTrue([actualOutput isEqualToString:[testDataPSTTimezone objectForKey:key]], @"actual output %@ does not match expected output %@ for input source %@", actualOutput, [testDataPSTTimezone objectForKey:key],key);
    }
    
    for (id key in testDataGMTTimezone){
        NSDate *date = [dateFormatterGMT dateFromString:key];
        NSString *actualOutput = [CCDateUtilities formatDateYYYYMMddByNSDate:date];
        XCTAssertTrue([actualOutput isEqualToString:[testDataGMTTimezone objectForKey:key]], @"actual output %@ does not match expected output %@ for input source %@", actualOutput, [testDataGMTTimezone objectForKey:key],key);
    }
    
}

#pragma mark - testing date format for receipt store
// test for receipt store
- (void)testFormatDateForReceiptInfoEntity
{
    NSDictionary *testData = @{// new years eve and new years day
                               @"1995-01-01 22:28:00":  @"1995-01-01 22:28:00",
                               @"2014-01-01 00:00:00":  @"2014-01-01 00:00:00",
                               @"2013-12-31 00:00:00":  @"2013-12-31 00:00:00",
                               // a leap year
                               @"2012-02-29 00:00:00":  @"2012-02-29 00:00:00",
                               
                               @"2014-01-01 11:59:59": @"2014-01-01 11:59:59",
                               @"2013-12-31 23:59:59": @"2013-12-31 23:59:59",
                               
                               @"2014-11-11 11:11:11": @"2014-11-11 11:11:11",
                               @"2013-06-06 01:00:00": @"2013-06-06 01:00:00",
                               
                               @"2014-01-01 00:00:01": @"2014-01-01 00:00:01",
                               @"2013-12-31 13:01:00": @"2013-12-31 13:01:00",
                               
                               @"2030-09-30 00:00:00":  @"2030-09-30 00:00:00"};
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
    [dateFormatter setLocale:[NSLocale currentLocale]];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    
    for (id key in testData) {
        NSDate *actualOutput = [CCDateUtilities formatDateForReceiptInfoEntity:key];
        NSString *outPutDateStr = [dateFormatter stringFromDate:actualOutput];
        XCTAssertFalse([outPutDateStr isEqualToString:@""], @"cannot pase the date string");
        XCTAssertTrue([outPutDateStr isEqualToString:[testData objectForKey:key]], @"Actual output %@ does not match expected output %@", outPutDateStr, [testData objectForKey:key]);
    }
}

- (void) testFormatDateToEEEMonthDayYear {
    NSDictionary *testData = @{// new years eve and new years day
                               @"1995-01-01 00:00:00": @"Sun Jan 01 1995",
                               @"2014-01-01 00:00:00": @"Wed Jan 01 2014",
                               @"2013-12-31 00:00:00": @"Tue Dec 31 2013",
                               // a leap year
                               @"2012-02-29 00:00:00": @"Wed Feb 29 2012",
                               @"2014-01-01 11:59:59": @"Wed Jan 01 2014",
                               @"2013-12-31 23:59:59": @"Tue Dec 31 2013",
                               
                               @"2014-11-11 11:11:11": @"Tue Nov 11 2014",
                               @"2013-06-06 01:00:00": @"Thu Jun 06 2013",
                               
                               @"2014-01-01 00:00:01": @"Wed Jan 01 2014",
                               @"2013-12-31 13:01:00": @"Tue Dec 31 2013",
                               
                               @"2030-09-30 00:00:00": @"Mon Sep 30 2030"};
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    [dateFormatter setLocale:[NSLocale currentLocale]];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    for (id key in testData) {
        NSDate *date = [dateFormatter dateFromString:key];
        NSString *actualOutput = [CCDateUtilities formatDateToEEEMonthDayYear:date];
        XCTAssertFalse([actualOutput isEqualToString:@""], @"cannot pase the date string");
        XCTAssertTrue([actualOutput isEqualToString:[testData objectForKey:key]], @"Actual output %@ does not match expected output %@", actualOutput, [testData objectForKey:key]);
    }
}

- (void) testFormatDateToEEEMonthDayYearTime {
    NSDictionary *testData = nil;
    if ([self is24Hr] || ![self canForce12Hr]){
        testData = @{// new years eve and new years day
                               @"1995-01-01 08:00:00": @"Sun Jan 01 1995 00:00",
                               @"2014-01-01 08:00:00": @"Wed Jan 01 2014 00:00",
                               @"2013-12-31 08:00:00": @"Tue Dec 31 2013 00:00",
                               // a leap year
                               @"2012-02-29 08:00:00": @"Wed Feb 29 2012 00:00",
                               @"2014-01-01 19:59:59": @"Wed Jan 01 2014 11:59",
                               @"2013-01-01 07:59:59": @"Mon Dec 31 2012 23:59",
                               
                               @"2014-11-11 19:11:11": @"Tue Nov 11 2014 11:11",
                               @"2013-06-06 09:00:00": @"Thu Jun 06 2013 02:00",
                               
                               @"2014-01-01 08:00:01": @"Wed Jan 01 2014 00:00",
                               @"2013-12-31 21:01:00": @"Tue Dec 31 2013 13:01",
                               
                               @"2030-09-30 07:00:00": @"Mon Sep 30 2030 00:00"};
    } else {
        testData = @{// new years eve and new years day
                     @"1995-01-01 08:00:00": @"Sun Jan 01 1995 12:00 am",
                     @"2014-01-01 08:00:00": @"Wed Jan 01 2014 12:00 am",
                     @"2013-12-31 08:00:00": @"Tue Dec 31 2013 12:00 am",
                     // a leap year
                     @"2012-02-29 08:00:00": @"Wed Feb 29 2012 12:00 am",
                     @"2014-01-01 19:59:59": @"Wed Jan 01 2014 11:59 am",
                     @"2013-01-01 07:59:59": @"Mon Dec 31 2012 11:59 pm",
                     
                     @"2014-11-11 19:11:11": @"Tue Nov 11 2014 11:11 am",
                     @"2013-06-06 08:00:00": @"Thu Jun 06 2013 01:00 am",
                     
                     @"2014-01-01 08:00:01": @"Wed Jan 01 2014 12:00 am",
                     @"2013-12-31 21:01:00": @"Tue Dec 31 2013 01:01 pm",
                     
                     @"2030-09-30 07:00:00": @"Mon Sep 30 2030 12:00 am"};
    }
    
    for (id key in testData) {
        NSString *output = [CCDateUtilities formatDateToEEEMonthDayYearTime:key];
        
        XCTAssertTrue([output isEqualToString:[testData objectForKey:key]], @"Actual output %@ does not match expected output %@", output, [testData objectForKey:key]);
    }
}

// Note that the format might different for different contries. The test data below is only for the US
- (void) testFormatDateToEEEMonthDayYearTimeFromNSDate
{
    NSDictionary *testData = @{// new years eve and new years day
                               @"1995-01-01 00:00:00": @"Jan 1, 1995, 12:00:00 AM",
                               @"2014-01-01 00:00:00": @"Jan 1, 2014, 12:00:00 AM",
                               @"2013-12-31 00:00:00": @"Dec 31, 2013, 12:00:00 AM",
                               // a leap year
                               @"2012-02-29 00:00:00": @"Feb 29, 2012, 12:00:00 AM",
                               @"2014-01-01 11:59:59": @"Jan 1, 2014, 11:59:59 AM",
                               @"2013-12-31 23:59:59": @"Dec 31, 2013, 11:59:59 PM",
                               
                               @"2014-11-11 11:11:11": @"Nov 11, 2014, 11:11:11 AM",
                               @"2013-06-06 01:00:00": @"Jun 6, 2013, 1:00:00 AM",
                               
                               @"2014-01-01 00:00:01": @"Jan 1, 2014, 12:00:01 AM",
                               @"2013-12-31 13:01:00": @"Dec 31, 2013, 1:01:00 PM",
                               
                               @"2030-09-30 00:00:00": @"Sep 30, 2030, 12:00:00 AM"};
    
    NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
    NSDateFormatter *dateFormatter = [self getFormatterWithFormat:@"yyyy-MM-dd HH:mm:ss" locale:locale timeZone:[NSTimeZone localTimeZone]];
    for (id key in testData) {
        NSDate *date = [dateFormatter dateFromString:key];
        NSString *actualOutput = [CCDateUtilities formatDateToEEEMonthDayYearTimeFromNSDate:date];
        XCTAssertTrue([actualOutput caseInsensitiveCompare:[testData objectForKey:key]] == NSOrderedSame, @"Actual output %@ does not match expected output %@", actualOutput, [testData objectForKey:key]);
    }
}

- (void) testFormatDateToTime
{
    NSDictionary *testData = nil;
    
    if([self is24Hr]) { // device is set to 24 hr format
        testData = @{// new years eve and new years day
                                   @"1995-01-01 08:00:00": @"00:00",
                                   @"2014-01-01 08:00:00": @"00:00",
                                   @"2013-12-31 08:00:00": @"00:00",
                                   // a leap year
                                   @"2012-02-29 08:00:00": @"00:00",
                                   @"2014-01-01 19:59:59": @"11:59",
                                   @"2014-01-01 07:59:59": @"23:59",
                                   
                                   @"2014-11-11 19:11:11": @"11:11",
                                   @"2013-06-06 08:00:00": @"01:00",
                                   
                                   @"2014-01-01 08:00:01": @"00:00",
                                   @"2013-12-31 21:01:00": @"13:01",
                                   
                                   // daylight time pdt
                                   @"2020-09-30 08:00:00": @"01:00"};
    } else {
        testData = @{// new years eve and new years day
                               @"1995-01-01 08:00:00": @"12:00 AM",
                               @"2014-01-01 08:00:00": @"12:00 AM",
                               @"2013-12-31 08:00:00": @"12:00 AM",
                               // a leap year
                               @"2012-02-29 08:00:00": @"12:00 AM",
                               @"2014-01-01 19:59:59": @"11:59 AM",
                               @"2014-01-01 07:59:59": @"11:59 PM",
                               
                               @"2014-11-11 19:11:11": @"11:11 AM",
                               @"2013-06-06 08:00:00": @"01:00 AM",
                               
                               @"2014-01-01 08:00:01": @"12:00 AM",
                               @"2013-12-31 21:01:00": @"01:01 PM",
                               
                               // daylight time pdt
                               @"2020-09-30 08:00:00": @"01:00 AM"};
    }
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    [dateFormatter setLocale:[NSLocale currentLocale]];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    for (id key in testData) {
        NSDate *date = [dateFormatter dateFromString:key];
        NSString *actualOutput = [CCDateUtilities formatDateToTime:date];
        XCTAssertFalse([actualOutput isEqualToString:@""], @"cannot pase the date string");
        XCTAssertTrue([actualOutput caseInsensitiveCompare:[testData objectForKey:key]] == NSOrderedSame, @"Actual output %@ does not match expected output %@ for input %@", actualOutput, [testData objectForKey:key], key);
    }
}


- (void) testFormatDateMediumByDate {
    NSDictionary *testData = @{// new years eve and new years day
                               @"1995-01-01 00:00:00": @"Jan 1, 1995",
                               @"2014-01-01 00:00:00": @"Jan 1, 2014",
                               @"2013-12-31 00:00:00": @"Dec 31, 2013",
                               // a leap year
                               @"2012-02-29 00:00:00": @"Feb 29, 2012",
                               @"2014-01-01 11:59:59": @"Jan 1, 2014",
                               @"2013-12-31 23:59:59": @"Dec 31, 2013",
                               
                               @"2014-11-11 11:11:11": @"Nov 11, 2014",
                               @"2013-06-06 01:00:00": @"Jun 6, 2013",
                               
                               @"2014-01-01 00:00:01": @"Jan 1, 2014",
                               @"2013-12-31 13:01:00": @"Dec 31, 2013",
                               
                               @"2030-09-30 00:00:00": @"Sep 30, 2030"};
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    [dateFormatter setLocale:[NSLocale currentLocale]];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    for (id key in testData) {
        NSDate *date = [dateFormatter dateFromString:key];
        NSString *actualOutput = [CCDateUtilities formatDateMediumByDate:date];
        XCTAssertFalse([actualOutput isEqualToString:@""], @"cannot pase the date string");
        XCTAssertTrue([actualOutput isEqualToString:[testData objectForKey:key]], @"Actual output %@ does not match expected output %@", actualOutput, [testData objectForKey:key]);
    }
}

//  it is only test for PST
- (void)testFormatDateToMonthYearFromDateStringWithTimeZone
{
    // since the dates from the server are in GMT, so need to add 8 hrs to match the PST...
    NSDictionary *testData = @{// new years eve and new years day
                               @"1995-01-01 08:00:00": @"Jan 1995",
                               @"2014-01-01 08:00:00": @"Jan 2014",
                               @"2013-12-31 08:00:00": @"Dec 2013",
                               // a leap year
                               @"2012-02-29 08:00:00": @"Feb 2012",
                               
                               @"2014-01-01 19:59:59": @"Jan 2014",
                               @"2014-01-01 07:59:59": @"Dec 2013",
                               
                               @"2014-11-11 19:11:11": @"Nov 2014",
                               @"2013-06-06 01:00:00": @"Jun 2013",
                               
                               @"2014-01-01 08:00:01": @"Jan 2014",
                               @"2013-12-31 08:01:00": @"Dec 2013",
                               
                               @"2014-09-30 08:00:00": @"Sep 2014"};
    
    for (id key in testData) {
        NSString *actualOutput = [CCDateUtilities formatDateToMonthYearFromDateStringWithTimeZone:key];
        XCTAssertFalse([actualOutput isEqualToString:@""], @"cannot pase the date string %@", key);
        XCTAssertTrue([actualOutput isEqualToString:[testData objectForKey:key]], @"%@ does not match expected output %@ from input source %@", actualOutput,[testData objectForKey:key], key);
    }
}

#pragma mark - testing date time formatting for report
/**
 testing date time formatting function for generating the report name
 */
- (void) testFormatDateShortStyle
{
    NSDictionary *testData = nil;
    if ([self is24Hr] || ![self canForce12Hr]){
        testData = @{// new years eve and new years day
                               @"1995-01-01T00:00:00": @"1/1/95",
                               @"2014-01-01T00:00:00": @"1/1/14",
                               @"2013-12-31T00:00:00": @"12/31/13",
                               // a leap year
                               @"2012-02-29T00:00:00": @"2/29/12",
                               @"2014-01-01T11:59:59": @"1/1/14",
                               @"2013-12-31T23:59:59": @"12/31/13",
                               
                               @"2014-11-11T11:11:11": @"11/11/14",
                               @"2013-06-06T01:00:00": @"6/6/13",
                               
                               @"2014-01-01T00:00:01": @"1/1/14",
                               @"2013-12-31T13:01:00": @"12/31/13",
                               
                               @"2030-09-30T00:00:00": @"9/30/30"};
    } else {
        testData = @{// new years eve and new years day
                     @"1995-01-01T00:00:00": @"01/01/1995",
                     @"2014-01-01T00:00:00": @"01/01/2014",
                     @"2013-12-31T00:00:00": @"31/12/2013",
                     // a leap year
                     @"2012-02-29T00:00:00": @"29/02/2012",
                     @"2014-01-01T11:59:59": @"01/01/2014",
                     @"2013-12-31T23:59:59": @"31/12/2013",
                     
                     @"2014-11-11T11:11:11": @"11/11/2014",
                     @"2013-06-06T01:00:00": @"06/06/2013",
                     
                     @"2014-01-01T00:00:01": @"01/01/2014",
                     @"2013-12-31T13:01:00": @"31/12/2013",
                     
                     @"2030-09-30T00:00:00": @"30/09/2030"};
    }
    for (id key in testData) {
        NSString *actualOutput = [CCDateUtilities formatDateShortStyle:key];
        XCTAssertFalse([actualOutput isEqualToString:@""], @"cannot pase the date string");
        XCTAssertTrue([actualOutput isEqualToString:[testData objectForKey:key]], @"Actual output %@ does not match expected output %@", actualOutput, [testData objectForKey:key]);
    }
}

/**
 testing date time formatting function for generating the report name
 */
- (void) testFormatDateToMMMddYYYFromString
{
    NSDictionary *testData = nil;
    if ([self is24Hr] || ![self canForce12Hr]){
        testData = @{// new years eve and new years day
                     @"1995-01-01T00:00:00": @"Jan 1, 1995",
                     @"2014-01-01T00:00:00": @"Jan 1, 2014",
                     @"2013-12-31T00:00:00": @"Dec 31, 2013",
                     // a leap year
                     @"2012-02-29T00:00:00": @"Feb 29, 2012",
                     @"2014-01-01T11:59:59": @"Jan 1, 2014",
                     @"2013-12-31T23:59:59": @"Dec 31, 2013",
                     
                     @"2014-11-11T11:11:11": @"Nov 11, 2014",
                     @"2013-06-06T01:00:00": @"Jun 6, 2013",
                     
                     @"2014-01-01T00:00:01": @"Jan 1, 2014",
                     @"2013-12-31T13:01:00": @"Dec 31, 2013",
                     
                     @"2030-09-30T00:00:00": @"Sep 30, 2030"};
    } else {
        testData = @{// new years eve and new years day
                     @"1995-01-01T00:00:00": @"1 Jan, 1995",
                     @"2014-01-01T00:00:00": @"1 Jan, 2014",
                     @"2013-12-31T00:00:00": @"31 Dec, 2013",
                     // a leap year
                     @"2012-02-29T00:00:00": @"29 Feb, 2012",
                     @"2014-01-01T11:59:59": @"1 Jan, 2014",
                     @"2013-12-31T23:59:59": @"31 Dec, 2013",
                     
                     @"2014-11-11T11:11:11": @"11 Nov, 2014",
                     @"2013-06-06T01:00:00": @"6 Jun, 2013",
                     
                     @"2014-01-01T00:00:01": @"1 Jan, 2014",
                     @"2013-12-31T13:01:00": @"31 Dec, 2013",
                     
                     @"2030-09-30T00:00:00": @"30 Sep, 2030"};
    }
    for (id key in testData) {
        NSString *output = [CCDateUtilities formatDateToMMMddYYYFromString:key];
        NSString *cleanOuput = [output stringByTrimmingCharactersInSet:[NSCharacterSet punctuationCharacterSet]];
        XCTAssertTrue([cleanOuput isEqualToString:[testData objectForKey:key]], @"Actual output %@ does not match expected output %@", cleanOuput, [testData objectForKey:key]);
    }
}

//+(NSDate *) getNSDate:(NSString*)string Format:(NSString *)format TimeZone:(NSTimeZone *)tz
-(void) testFormatDateStringWithTimeZoneToNSDateWithLocalTimeZone{
    // it seems there's a bug in ios because it cannot force to 12h even in the general setting for US, Canada, and Austraili
    NSDictionary *testData = nil;
    if (![self canForce12Hr] || [self is24Hr]){
        testData = @{// new years eve and new years day
                     @"1995-01-01T23:59:59": @"1995-01-01 23:59:59",
                     @"2014-01-01T00:00:00": @"2014-01-01 00:00:00",
                     @"2013-12-31T00:00:00": @"2013-12-31 00:00:00",
                     // a leap year
                     @"2012-02-29T00:00:00": @"2012-02-29 00:00:00",
                     
                     @"2014-01-01T11:59:59": @"2014-01-01 11:59:59",
                     @"2013-12-31T23:59:59": @"2013-12-31 23:59:59",
                     
                     @"2014-11-11T11:11:11": @"2014-11-11 11:11:11",
                     @"2013-06-06T01:00:00": @"2013-06-06 01:00:00",
                     
                     @"2014-01-01T00:00:01": @"2014-01-01 00:00:01",
                     @"2013-12-31T00:01:00": @"2013-12-31 00:01:00",
 
                     @"2014-07-06T00:00:00": @"2014-07-06 00:00:00",
                     @"2013-07-06T00:00:00": @"2013-07-06 00:00:00"};
    }else {
        testData = @{// new years eve and new years day
                     @"1995-01-01T23:59:59": @"1995-01-01 11:59:59 pm",
                     @"2014-01-01T00:00:00": @"2014-01-01 12:00:00 am",
                     @"2013-12-31T00:00:00": @"2013-12-31 12:00:00 am",
                     // a leap year
                     @"2012-02-29T00:00:00": @"2012-02-29 12:00:00 am",
                     
                     // currently, the server always returns midnight.
                     // these tests make sure things don't break if times are returned.
                     @"2014-01-01T11:59:59": @"2014-01-01 11:59:59 am",
                     @"2013-12-31T23:59:59": @"2013-12-31 11:59:59 pm",
                     
                     @"2014-11-11T11:11:11": @"2014-11-11 11:11:11 am",
                     @"2013-06-06T01:00:00": @"2013-06-06 01:00:00 am",
                     
                     @"2014-01-01T00:00:01": @"2014-01-01 12:00:01 am",
                     @"2013-12-31T00:01:00": @"2013-12-31 12:01:00 am",
                     
                     // dates that broke in CRMC-43667
                     @"2014-07-06T00:00:00": @"2014-07-06 12:00:00 am",
                     @"2013-07-06T00:00:00": @"2013-07-06 12:00:00 am"};
    }
    
    
    NSDateFormatter *dateFormatter = [self getFormatterWithFormat:CCDateFormatReceiptStore locale:nil timeZone:[NSTimeZone localTimeZone]];
    
    for (id key in testData){
        NSDate *dateFromString = [CCDateUtilities formatDateStringWithTimeZoneToNSDateWithLocalTimeZone:key];
        NSString *actualOutput = [dateFormatter stringFromDate:dateFromString];
        
        // still have to figure out why some country use PM and some other use pm
        // assert case insensitive for now
        XCTAssertTrue([[actualOutput lowercaseString] isEqualToString:[[testData objectForKey:key] lowercaseString]], @"actual output %@ does not match expected output %@ for input source %@", actualOutput, [testData objectForKey:key], key);
    }
}



#pragma mark - Helper Methods

-(BOOL)is24Hr
{
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setLocale:[NSLocale currentLocale]];
    [formatter setDateStyle:NSDateFormatterNoStyle];
    [formatter setTimeStyle:NSDateFormatterShortStyle];
    NSString *dateString = [formatter stringFromDate:[NSDate date]];
    NSRange amRange = [dateString rangeOfString:[formatter AMSymbol]];
    NSRange pmRange = [dateString rangeOfString:[formatter PMSymbol]];
    BOOL is24h = (amRange.location == NSNotFound && pmRange.location == NSNotFound);
    return is24h;
}

-(BOOL)canForce12Hr
{
    return !([@"en_US" isEqualToString:[[NSLocale currentLocale] localeIdentifier]] ||
             [@"en_CA" isEqualToString:[[NSLocale currentLocale] localeIdentifier]] ||
             [@"en_AU" isEqualToString:[[NSLocale currentLocale] localeIdentifier]]);
}

- (NSDateFormatter*) getFormatterWithFormat:(NSString*)format locale: (NSLocale*)locale timeZone: (NSTimeZone*)timeZone
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setTimeZone:timeZone];
    [dateFormatter setLocale:locale];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    [dateFormatter setDateFormat:format];
    return dateFormatter;
}

@end
