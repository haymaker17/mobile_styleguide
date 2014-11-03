//
//  NSDateFormatter+Additions.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/29/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "NSDateFormatter+Additions.h"

NSString * const CCDateFormatEEE = @"EEE";
NSString * const CCDateFormatEEE_hcmm_aa = @"EEE h:mm aa";
NSString * const CCDateFormatEEE_MMM_dd = @"EEE MMM dd";
NSString * const CCDateFormatEEE_MMM_dd_hcmm_aa = @"EEE MMM dd h:mm aa";
NSString * const CCDateFormatEEE_MMM_dd_yyyy = @"EEE MMM dd yyyy";
NSString * const CCDateFormatEEE_MMM_dd_HHcmm_aa_zzz = @"EEE MMM dd HH:mm aa zzz";
NSString * const CCDateFormatHHcmm_aa_zzz = @"HH:mm aa zzz";
NSString * const CCDateFormatHHmm = @"HHmm";

NSString * const CCDateFormatyyy_MM_dd = @"yyyy MM dd";
NSString * const CCDateFormatyyyyMMdd = @"yyyyMMdd";
NSString * const CCDateFormatMMddyy = @"MMddyy";
NSString * const CCDateFormatMMMyyy = @"MMM yyyy";

NSString * const CCDateFormatReceiptStore = @"yyyy-MM-dd HH:mm:ss";
NSString * const CCDateFormatBooking = @"EEE MMM dd, h:mm aa";

NSString * const CCDateFormatISO8601Date = @"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
NSString * const CCDateFormatISO8601DateTime = @"yyyy-MM-dd'T'HH:mm:ss";
NSString * const CCDateFormatISO8601DateTime12Hr = @"yyyy-MM-dd'T'hh:mm:ss";
NSString * const CCDateFormatISO8601ZoneDate = @"yyyy-MM-dd'T'HH:mm:ss.SSSZ";
NSString * const CCDateFormatISO8601DateOnly = @"yyyy-MM-dd";

NSString * const CCDateFormatISO8601 = @"yyyy-MM-dd'T'HH:mm:ssZZZZZ";
NSString * const CCDateFormatISO8601ZoneMidNight = @"yyyy-MM-dd'T'00:00:00";

static NSMutableDictionary *addedFormatters;

@implementation NSDateFormatter (Additions)

+ (NSDateFormatter *)dateFormatterWithFormat:(NSString *)format timeZoneWithAbbreviation:(NSString *)timeZone locale:(NSLocale *)locale{
    if (!timeZone) {
        timeZone = [NSTimeZone localTimeZone].abbreviation;
    }
    if (!locale) {
        locale = [NSLocale systemLocale];
    }
    NSString *key =[ NSString stringWithFormat:@"%@-%@-%@",format,timeZone,[locale localeIdentifier]];
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        addedFormatters = [NSMutableDictionary dictionaryWithCapacity:1];
    });
    NSDateFormatter *aDateFormatter = [addedFormatters objectForKey:key];
    if (aDateFormatter != nil) {
        return aDateFormatter;
    }
    aDateFormatter = [[NSDateFormatter alloc] init];
    [aDateFormatter setDateFormat:format];
    [aDateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:timeZone ]];
    [aDateFormatter setLocale:locale];
    [addedFormatters setObject:aDateFormatter forKey:key];
    return aDateFormatter;
}

+ (void)clearAddedFormatter{
    if (addedFormatters) {
        [addedFormatters removeAllObjects];
    }
}

@end
