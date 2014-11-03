//
//  NSDateFormatter+Additions.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/29/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

FOUNDATION_EXPORT NSString * const CCDateFormatEEE;
FOUNDATION_EXPORT NSString * const CCDateFormatEEE_hcmm_aa;
FOUNDATION_EXPORT NSString * const CCDateFormatEEE_MMM_dd;
FOUNDATION_EXPORT NSString * const CCDateFormatEEE_MMM_dd_hcmm_aa;
FOUNDATION_EXPORT NSString * const CCDateFormatEEE_MMM_dd_yyyy;
FOUNDATION_EXPORT NSString * const CCDateFormatEEE_MMM_dd_HHcmm_aa_zzz;
FOUNDATION_EXPORT NSString * const CCDateFormatHHcmm_aa_zzz;
FOUNDATION_EXPORT NSString * const CCDateFormatHHmm;

FOUNDATION_EXPORT NSString * const CCDateFormatyyy_MM_dd;
FOUNDATION_EXPORT NSString * const CCDateFormatyyyyMMdd;
FOUNDATION_EXPORT NSString * const CCDateFormatMMddyy;
FOUNDATION_EXPORT NSString * const CCDateFormatMMMyyy;

FOUNDATION_EXPORT NSString * const CCDateFormatReceiptStore;
FOUNDATION_EXPORT NSString * const CCDateFormatBooking;

FOUNDATION_EXPORT NSString * const CCDateFormatISO8601Date;
FOUNDATION_EXPORT NSString * const CCDateFormatISO8601DateTime;
FOUNDATION_EXPORT NSString * const CCDateFormatISO8601DateTime12Hr;
FOUNDATION_EXPORT NSString * const CCDateFormatISO8601ZoneDate;
FOUNDATION_EXPORT NSString * const CCDateFormatISO8601DateOnly;
FOUNDATION_EXPORT NSString * const CCDateFormatISO8601ZoneMidNight;
FOUNDATION_EXPORT NSString * const CCDateFormatISO8601 NS_AVAILABLE_IOS(6_0);

@interface NSDateFormatter (Additions)

/*
 * convinience method to create and cache dateFormatter
 * format : CCDateFormat
 * timeZoneWithAbbreviation : @"GMT" or nil for local timezone
 * locale : NSLocale or nil for local locale
 */
+ (NSDateFormatter *)dateFormatterWithFormat:(NSString *)format timeZoneWithAbbreviation:(NSString *)timeZone locale:(NSLocale *)locale;

/*
 * clear memory from cached dateFormatters
 * to be called when the app receive memory warning
 */
+ (void)clearAddedFormatter;

@end
