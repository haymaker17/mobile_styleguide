//  ".objc_class_name_DateTimeFormatter", referenced from:



//  DateTimeFormatter.h
//  ConcurMobile
//
//  Created by Paul Kramer on 2/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface DateTimeFormatter : NSObject 
{

}
//__attribute__ ((deprecated("use NSString+DateFormat")))
+(NSString *)formatDateTimeEEEMMMddHHmmaazzz:(NSString *)dt;

// Added for Fusion 14 flight summary
// This method gets just the time with AM and PM 
+(NSString *)formatTimeHHmmaa:(NSDate *)startDate;

//+(NSString *)formatTimeHHmmaazzz:(NSString *)dt __attribute__ ((deprecated("use NSString+DateFormat")));

+(NSString *)formatTimeHHmm:(NSString *)dt;

+(NSString *)formatDateEEEMMMdd:(NSString *)dt;

+(NSString *)formatDateMedium:(NSString *)dt;

+(NSString *)formatDateShort:(NSString *)dt;

+(NSString *)formatDateLong:(NSString *)dt;

//+(NSString *)formatDateFull:(NSString *)dt;

//+(NSString *)formatTimeFull:(NSString *)dt __attribute__ ((deprecated("unreferenced")));

//+(NSString *)formatTimeLong:(NSString *)dt __attribute__ ((deprecated("unreferenced")));

//+(NSString *)formatTimeShort:(NSString *)dt __attribute__ ((deprecated("unreferenced")));

+(NSString *)formatDateTimeFull:(NSString *)dt;

//+(NSString *)formatDateTimeLong:(NSString *)dt __attribute__ ((deprecated("unreferenced")));

+(NSString *)formatDateTimeFullByDate:(NSDate *)dt __attribute__ ((deprecated("unreferenced")));

//+(NSString *)formatDateTimeShort:(NSString *)dt __attribute__ ((deprecated("unreferenced")));

//+(NSString *)formatDateTimeMedium:(NSString *)dt __attribute__ ((deprecated("unreferenced")));

+(NSString *)formatDateTimeMediumByDate:(NSDate *)dt;

+(NSString *)formatHour:(NSInteger)hour;

+(NSString *)formatTimeForTravel:(NSString *) sDate;

+(NSString *)formatDateTimeForTravel:(NSString *) sDate;

+(NSString *)formatDateTimeForTravelByDate:(NSDate *) dt;

+(NSString *)formatDateForTravel:(NSString *) sDate;

+(NSString *)formatDateForTravelByDate:(NSDate *) dt;

+(NSString *)formatDateForTravelyyyymmdd:(NSString *) sDate __attribute__ ((deprecated("unreferenced")));

+(NSString *)formatDateForBooking:(NSDate *)dt;

+(NSString *)formatDateMediumByDate:(NSDate *)dt;

+(NSString *)formatDateFullByDate:(NSDate *)dt __attribute__ ((deprecated("unreferenced")));

+(NSString *)formatDateYYYYMMddByDate:(NSDate *)dt;

+(NSDate *) getNSDate:(NSString*) string Format:(NSString *)format;

+(NSDate *)getLocalDate:(NSString *)dt;

+(NSString *) getLocalDateAsString:(NSDate*)dt;

+(NSString *)formatLocalDateMedium:(NSString *)dt;

+(NSString *)formatDateyyyyMMdd:(NSString *) sDate;

+(NSString *)formatDateEEEMMMddByDate:(NSDate*)date __attribute__ ((deprecated("unreferenced")));

//+(NSString *)formatDateEEEMMMddyyyy:(NSString *)dt __attribute__ ((deprecated("unreferenced")));
+(NSString *)formatDateEEEMMMddyyyyByDate:(NSDate *)date;

+(NSString *)formatDateForBooking:(NSDate *)dt TimeZone:(NSTimeZone *)tz;

+(NSString *)formatDateMediumByDate:(NSDate *)dt TimeZone:(NSTimeZone *)tz;

+(NSString *)formatDateForCarOrHotelTravelByDate:(NSDate *) dt;
+(NSString *)formatDateMediumByHotelOrCarDate:(NSDate *)dt inTimeZone:(NSTimeZone *)timeZone;
+(NSString *)formatHotelOrCarDateForBooking:(NSDate *)dt;
+(NSString *)formatHotelOrCarDateForBooking:(NSDate *)dt inTimeZone:(NSTimeZone*)timeZone;

+(NSString *)formatDateTimeMediumByDateLTZ:(NSDate *)dt;
// Gov does not require TIME in the endpoint "SaveTMExpenseForm", it wants the date in "01/21/14"
// http://10.24.61.100/qawiki/index.php/TravelManager_MWS_Endpoints#SaveTMExpenseForm
+(NSDate *) getNSDateForGov:(NSString*)string Format:(NSString *)format TimeZone:(NSTimeZone *)tz;
+(NSDate *) getNSDate:(NSString*) string Format:(NSString *)format  TimeZone:(NSTimeZone *)tz;

+(NSString *) formatDuration:(NSString *)startDate endDate:(NSString *) endDate;

+(NSString *) formatDateForExpenseServer:(NSDate *)dt;

+(NSString *)formatDateFromServer:(NSString *) sDate returnFormat:(NSString *) returnFormat;

+(NSDate *) getLocalDateMedium:(NSString *)string  __attribute__ ((deprecated("unreferenced")));

+(NSString*)formatDateFromReceiptStoreToString:(NSString*)sDate returnFormat:(NSString *) returnFormat;
+(NSString*)formatDateFromReceiptStoreDate:(NSDate*)dt returnFormat:(NSString *) returnFormat;
+(NSDate*)dateFromReceiptStoreToString:(NSString*)sDate;

+(NSString *)formatDateYYYYMMddByDateLocal:(NSDate *)dt;
+(NSString *)formatDateTimeEEEhmmaaByDate:(NSDate *) dt;
+(NSString *)formatDateTimeEEEhmmaa:(NSString *) sDate __attribute__ ((deprecated("unreferenced")));
+(NSString *)formatDateForAirBookingByDate:(NSDate *) dt  __attribute__ ((deprecated("unreferenced")));
+(NSString *)formatDateEEEByDate:(NSDate *) dt;
+(NSString *)formatDateTimeForTravelCliqbookByDate:(NSDate *) dt;
+(NSString *) formatDate:(NSDate*) dt Format:(NSString *)format  TimeZone:(NSTimeZone *)tz;

// Pass tz as nil to use local time zone
+(NSDate*) getDateWithoutTime:(NSDate*) origDate withTimeZoneAbbrev:(NSString*) tzAbbrev;
// Convert given local date in GMT (absolute relative) date
+(NSDate*) getDateWithoutTimeInGMT:(NSDate*) date;
+(NSInteger) getTimeInSeconds:(NSDate*) date withTimeZoneAbbrev:(NSString*) tzAbbrev;

// Get now datetime without timezone info
+(NSDate*) getCurrentLocalDateTimeInGMT;
+(NSDate*) getNextHourDateTimeInGMT;
+(NSString*) formatBookingDateTime:(NSDate*) fullDate;

+(NSString *)formatExpenseDateEEEMMMDDYYYY:(NSDate *)dt;

+(NSDate*)getISO8601Date:(NSString*)dateString;

+(NSDate*)getISO8601ZoneDate:(NSString*)dateString;

+(BOOL)userSettingsPrefers24HourFormat;

+(NSString *)getDateFormatForString:(NSString *)exampleDateStr;

+(NSString *)getDateFormatString;
+(NSDate *) getNSDateFromMWSDateString:(NSString *) dateString;

@end
