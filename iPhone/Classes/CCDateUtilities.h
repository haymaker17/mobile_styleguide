//
//  CCDateUtilities.h
//  ConcurMobile
//
//  Created by ernest cho on 1/10/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CCDateUtilities : NSObject

// generic 
+(NSString *)formatDateForExchangeRateEndpoint:(NSString *) serverXMLDate;
+(NSString *)formatDateToMonthAndYear:(NSString *)sDate;

// for parsing the date string for expense list
+(NSDate*)formatDateToNSDateYYYYMMddTHHmmss:(NSString *)dateFromServer;
+(NSString *) formatDateToISO8601DateTimeInString:(NSDate*)dt;
+(NSString *)getDateFormatString;
+(NSString *) formatDateToYearMonthDateTimeZoneMidNight:(NSDate *)dt;
+(NSString *)formatDateYYYYMMddByNSDate:(NSDate *)dt;

// for receipt store
+(NSDate*)formatDateForReceiptInfoEntity:(NSString*)sDate;
+(NSString*)formatDateToEEEMonthDayYear:(NSDate*)date;
+(NSString*)formatDateToTime:(NSDate*)date;
+(NSString *)formatDateMediumByDate:(NSDate *)dt;
+(NSString*)formatDateToEEEMonthDayYearTime:(NSString*)sDate;
+(NSString *)formatDateToEEEMonthDayYearTimeFromNSDate:(NSDate *)dt;

// for generating report
+(NSString *)formatDateShortStyle:(NSString *)sDate;
+(NSString *)formatDateToMMMddYYYFromString:(NSString *)dt;
+(NSDate*)formatDateStringWithTimeZoneToNSDateWithLocalTimeZone:(NSString *)sDate;

// for date picker
+(NSString *)formatDateToMonthYearFromDateStringWithTimeZone:(NSString *)dateString;
+(NSDate*)formatDateStringWithoutTimeZoneToNSDate:(NSString *)sDate;

// for unit test only
+(NSDate *) formatDateFromServerWithoutTimeZone:(NSString *) dateString;

//// Do NOT use these methods directly.  They're only public for unit tests.
//+ (NSString *)convertDate:(NSString *)dateAsString withFormat:(NSString *)inputFormat outputFormat:(NSString *)outputFormat isGMT:(BOOL)isGMT;

@end
