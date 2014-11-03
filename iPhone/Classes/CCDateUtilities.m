//
//  CCDateUtilities.m
//  ConcurMobile
//
//  Created by ernest cho on 1/10/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CCDateUtilities.h"
#import "NSDateFormatter+Additions.h"

/**
 Utility methods for converting our myriad date formats.
 */
@implementation CCDateUtilities

/**
 Converts the server xml date to the date format used by the Exchange Rate Endpoint.
 
 Example:
 2014-07-06T00:00:00
 https://rqa3-cb.concursolutions.com/mobile/Expense/ExchangeRate/CAD/USD/2014-07-06
 
 Server format: @"yyyy-MM-dd'T'HH:mm:ss"
 Return format: @"yyyy-MM-dd"
 */
+ (NSString *)formatDateForExchangeRateEndpoint:(NSString *) serverXMLDate
{
    return [CCDateUtilities convertDate:serverXMLDate withFormat:@"yyyy-MM-dd'T'HH:mm:ss" outputFormat:@"yyyy-MM-dd" isGMT:NO isInputLocalePOSIX:YES];
}

/**
 Generic date conversion method. Do NOT directly use this method.  Make a specific date format call.
 
 @param dateAsString
 @param inputFormat
 @param outputFormat
 @param isGMT
 @param isInputLocalePOSIX - 12 hour clock breaks with 24 hour string format on non-US locations.  This is very annoying.
 */
+ (NSString *)convertDate:(NSString *)dateAsString withFormat:(NSString *)inputFormat outputFormat:(NSString *)outputFormat isGMT:(BOOL)isGMT isInputLocalePOSIX:(BOOL)isInputLocalePOSIX
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];

    // Some endpoints use GMT instead of local time.
    if (isGMT) {
        [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    }

    // Need to set locale to be able to differentiate date formats.  For example  01/02/2013  is either Jan. 2, 2013 or Feb. 1, 2013.
    if (isInputLocalePOSIX) {
        NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
        [dateFormatter setLocale:enUSPOSIXLocale];
    } else {
        [dateFormatter setLocale:[NSLocale currentLocale]];
    }
    
    // Read in the date string and save it as an NSDate
	[dateFormatter setDateFormat:inputFormat];
	NSDate *date = [dateFormatter dateFromString:dateAsString];

    // Export the NSDate in the output format and locale
    [dateFormatter setLocale:[NSLocale currentLocale]];
    if (outputFormat != nil){
        [dateFormatter setDateFormat:outputFormat];
    }
	return [dateFormatter stringFromDate:date];
}

/**
 Generic dateformatter with current local and GMT
 */
+(NSDateFormatter*)getDateFormatterWithGMTandCurrentLocale {
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init];
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	[dateFormatter setLocale:[NSLocale currentLocale]];
    return dateFormatter;
}

/**
 convert date string from server with time zone format: yyyy-MM-dd'T'HH:mm:ss
 @param sDate - date returned from server, one of the end points is active report endpoint
 */
//+(NSDate *) getLocalDate:(NSString*) string
// same as the one in DateTimeFormatter class : +(NSDate *) getNSDate:(NSString*) string Format:(NSString *)format
+(NSDate*)formatDateToNSDateYYYYMMddTHHmmss:(NSString *)sDate {
    /*
     Server returns yyyy-MM-ddT00:00:00. If user's region setting is not US such as UK and set datetime to 12 hrs format, it can cause the dateformate failure. To resolve this, we need to set up a controlled environment; apple recommends setting the locale to en_US_POSIX which is invariant in time
     */
    
    // first formate the date returned from the server
    
    if (![self is12hr:sDate]){ // somehow the formatter cannot format the string with am/pm
        NSString *timeZero = [NSString stringWithFormat:@"%@%@", [sDate substringToIndex:11], @"00:00:00"] ;
        sDate = timeZero;
    }
    NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
    NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:CCDateFormatISO8601DateTime timeZoneWithAbbreviation:@"GMT" locale:enUSPOSIXLocale];
    NSDate *date = [dateFormatter dateFromString:sDate];
    return date;
}

/**
 convert date string from server with time zone format: yyyy-MM-dd HH:mm:ss
 @param sDate - date returned from server
 */
+(NSDate *) formatDateFromServerWithoutTimeZone:(NSString *) dateString
{
    NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];

    NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:CCDateFormatReceiptStore timeZoneWithAbbreviation:@"GMT" locale:enUSPOSIXLocale];
    return [dateFormatter dateFromString:dateString];
}


#pragma mark - dislaying report lists to add Car Mileage
/**
 @param sDate - date returned from server
 */
+(NSString *)formatDateToMonthAndYear:(NSString *)dateString
{
    NSDate *date = [CCDateUtilities formatDateToNSDateYYYYMMddTHHmmss:dateString];
    
    // convert the date to format MMM yyyy
    if (date != nil) {
        NSLocale *locale = [NSLocale currentLocale];
        NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:CCDateFormatMMMyyy timeZoneWithAbbreviation:@"GMT" locale:locale];
        return [dateFormatter stringFromDate:date];
    }
    return @"";
}

#pragma mark - for expense
/**
 format date to Medium style, exp Nov 23, 1937
 for displaying expense list
 */
//+(NSString *)formatDateMediumByDate:(NSDate *)dt
+(NSString *)formatDateMediumByDate:(NSDate *)dt
{
	NSDateFormatter *dateFormatter = [CCDateUtilities getDateFormatterWithGMTandCurrentLocale];
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;
}

/**
 convert the date to string in format yyyy-MM-dd'T'HH:mm:ss 
 for quick expense, it seems that the date send to server is consider as a GMT time. When save it to core data, it convert to local time (PST),
 and when it displays on UI, it convert to GMT again
 @param dt - date from core data with timezone or returned form mws (it is local date)
 */
// +(NSString *) getLocalDateAsString:(NSDate*)dt
+(NSString *) formatDateToISO8601DateTimeInString:(NSDate*)dt
{
    NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:CCDateFormatISO8601DateTime timeZoneWithAbbreviation:@"GMT" locale:[NSLocale currentLocale]];
	NSString *str = [dateFormatter stringFromDate:dt];
	return str;
}

//MOB-15485: Get the date format based on 12/24hr setting on the device.
+(NSString *)getDateFormatString
{
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setLocale:[NSLocale currentLocale]];
    [formatter setDateStyle:NSDateFormatterNoStyle];
    [formatter setTimeStyle:NSDateFormatterShortStyle];
    
    NSString *dateString = [formatter stringFromDate:[NSDate date]];
    NSRange amRange = [dateString rangeOfString:[formatter AMSymbol]];
    NSRange pmRange = [dateString rangeOfString:[formatter PMSymbol]];
    BOOL is24h = (amRange.location == NSNotFound && pmRange.location == NSNotFound);
    
    if (is24h)
        return CCDateFormatISO8601DateTime;
    else
        return CCDateFormatISO8601DateTime12Hr;
}

/**
 Format date for QuickExpense endPoint, ReportEntry endPoint, and FormFieldBaseViewController class
 @param dt - the date genernate from UI, format 1995-01-01 23:59:59 PST
 */
+(NSString *) formatDateToYearMonthDateTimeZoneMidNight:(NSDate *)dt
{
    NSString *timeZone = [[NSTimeZone localTimeZone] abbreviation];
    NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:CCDateFormatISO8601ZoneMidNight timeZoneWithAbbreviation:timeZone locale:nil];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;
}

/**
 This function is used for making the xml sending to server and the returned string is the date only such as yyyy-MM-dd.
 @param - dt is a local time with hour only - 2014-02-11 16:00:00 PST, so when sending it to server, it needs to be converted to GMT
 MobileExpenseSave, outOfPicketSaveData, ExchangeRateData are calling this method
 */
// +(NSString *)formatDateYYYYMMddByDate:(NSDate *)dt
+(NSString *)formatDateYYYYMMddByNSDate:(NSDate *)dt
{
	NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:CCDateFormatISO8601DateOnly timeZoneWithAbbreviation:@"GMT" locale:nil];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;
}

#pragma mark - for receipt store
/**
 For receiptInfo entity only
 @param sDate - date returned from server
 */
+(NSDate*)formatDateForReceiptInfoEntity:(NSString*)sDate
{
    NSString *dateStr = [self convertDate:sDate withFormat:@"yyyy-MM-dd HH:mm:ss" outputFormat:nil isGMT:YES isInputLocalePOSIX:YES];
    
    NSDateFormatter *dateFormatter = [CCDateUtilities getDateFormatterWithGMTandCurrentLocale];
	[dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    
	NSDate *dt = [dateFormatter dateFromString:dateStr];
    return dt;
}

/**
   get date in format such as Moday Jan 12 2014. Method for receipt store, get called in QuickExpenseReceiptStore class
 */
+(NSString*)formatDateToEEEMonthDayYear:(NSDate*)date {
    NSDateFormatter *dateFormatter= [CCDateUtilities getDateFormatterWithGMTandCurrentLocale];
    [dateFormatter setDateFormat:@"EEE MMM dd yyyy"];
    return [dateFormatter stringFromDate:date];
}

/**
   date in format only with the time component
 */
//+(NSString*)formatDateFromReceiptStoreDate:(NSDate*)dt returnFormat:(NSString *) returnFormat
+(NSString*)formatDateToTime:(NSDate*)date
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init];
	[dateFormatter setLocale:[NSLocale currentLocale]];
    
    [dateFormatter setDateFormat:@"hh:mm a"];
//    NSLog(@"Date for receipt store = %@",[dateFormatter stringFromDate:date]);
    return [dateFormatter stringFromDate:date];
}

//@"EEE MMM dd yyyy HH:mm"
/**
 format date for ReceiptStoreListView
 */
//+(NSString*)formatDateFromReceiptStoreToString:(NSString*)sDate returnFormat:(NSString *) returnFormat
+(NSString*)formatDateToEEEMonthDayYearTime:(NSString*)sDate
{
    // get date returned from server
    NSDate *date = [CCDateUtilities formatDateFromServerWithoutTimeZone:sDate];
    
    // formate the date with specified style
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init];
    [dateFormatter setLocale:[NSLocale currentLocale]];
    
    [dateFormatter setDateFormat:@"EEE MMM dd yyyy HH:mm"];
    return [dateFormatter stringFromDate:date];
}

/**
 format NSDate to a string for ReceiptStoreListView - just format the date to string, no time zone involves 
 this method is similar to formatDateToEEEMonthDayYearTime except parameter
 example format 12 feb 2014 01:33:17 pm
 @param - dt is the NSDate returned from MsgControl class
 */
//+(NSString *)formatDateTimeMediumByDateLTZ:(NSDate *)dt
+(NSString *)formatDateToEEEMonthDayYearTimeFromNSDate:(NSDate *)dt
{
    NSString *localTimeZoneStr = [[NSTimeZone localTimeZone] abbreviation];
    NSLocale *currentLocale = [NSLocale currentLocale];
    
    NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:nil timeZoneWithAbbreviation:localTimeZoneStr locale:currentLocale];
	
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	[dateFormatter setTimeStyle:NSDateFormatterMediumStyle];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;
}

/**
 - format date for ReceiptStoreListView, the return stirng format is short style, example: Feb 2014
 - this method looks similar to formatDateToMonthAndYear and the difference is that input date string
 for formatDateToMonthAndYear has timezone
 */
//+(NSString*)formatDateFromReceiptStoreToString:(NSString*)sDate returnFormat:(NSString *) returnFormat
+(NSString *)formatDateToMonthYearFromDateStringWithTimeZone:(NSString *)dateString
{
    NSDate *date = [CCDateUtilities formatDateFromServerWithoutTimeZone:dateString];
    
    // convert the date to format MMM yyyy
    if (date != nil){

        NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
        [formatter setDateFormat:CCDateFormatMMMyyy];
        return [formatter stringFromDate:date];
    }
    return @"";
}

#pragma mark - for generating report name
//+(NSString *)formatDateShort:(NSString *)dt
/**
 format date to a short style such as 2014-02-09 
 */
+(NSString *)formatDateShortStyle:(NSString *)sDate
{
    // get the date from server
    NSDate *date = [CCDateUtilities formatDateToNSDateYYYYMMddTHHmmss:sDate];
    
    NSLocale *locale = [NSLocale currentLocale];
    NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:nil timeZoneWithAbbreviation:@"GMT" locale:locale];
	[dateFormatter setDateStyle:NSDateFormatterShortStyle];
    
	 return [dateFormatter stringFromDate:date];
}

/**
 Format date to medium style : Feb 2, 2014. The method gets called by ReportDetailViewController, SelectReportViewController, ApprovalListViewController....
 This method is similar to formatDateMediumByDate except the parameter is a NSString.
 @param - dt in format @"yyyy-MM-dd'T'HH:mm:ss"
 */
// MOB-17429: Refactored old method (NSString *)formatDateMedium:(NSString *)dt, and the method (NSString *)formatLocalDateMedium:(NSString *)dt is the same
+(NSString *)formatDateToMMMddYYYFromString:(NSString *)dt
{
    // convert the string returned from server to date
    NSLocale *enUSPosixLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
	NSDateFormatter *dateFormatterEnUSPosix = [NSDateFormatter dateFormatterWithFormat:CCDateFormatISO8601DateTime timeZoneWithAbbreviation:@"GMT" locale:enUSPosixLocale];
    NSDate *date = [dateFormatterEnUSPosix dateFromString:dt];
    
    NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:nil timeZoneWithAbbreviation:@"GMT" locale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	[dateFormatter setTimeStyle:NSDateFormatterNoStyle];

	NSString *formattedDateString = [dateFormatter stringFromDate:date];
	return formattedDateString;
}


#pragma mark - for displaying date on Date Picker

/**
 Convert date string to NSDate for DatePicker with local timezone
 @param - sDate's format is @"yyyy-MM-dd'T'HH:mm:ss"
 */
+(NSDate*)formatDateStringWithTimeZoneToNSDateWithLocalTimeZone:(NSString *)sDate
{
    NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
    NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:CCDateFormatISO8601DateTime timeZoneWithAbbreviation:nil locale:enUSPOSIXLocale];

    if (![self is12hr:sDate]){ // somehow the formatter cannot format the string with am/pm
        NSString *timeZero = [NSString stringWithFormat:@"%@%@", [sDate substringToIndex:11], @"00:00:00"] ;
        sDate = timeZero;
    }
    NSDate *date = [dateFormatter dateFromString:sDate];
    return date;
}

/**
 This method is for display date on datepicker only (quick expense and report)
 Convert date string to NSDate for DatePicker without timezone
 @param - sDate's format is @"yyyy-MM-dd'T'00:00:00"
 */
+(NSDate*)formatDateStringWithoutTimeZoneToNSDate:(NSString *)sDate
{
    NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
    NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:CCDateFormatISO8601ZoneMidNight timeZoneWithAbbreviation:nil locale:enUSPOSIXLocale];
    
    if (![self is12hr:sDate]){ // somehow the formatter cannot format the string with am/pm
        NSString *timeZero = [NSString stringWithFormat:@"%@%@", [sDate substringToIndex:11], @"00:00:00"] ;
        sDate = timeZero;
    }
    [dateFormatter setTimeZone:nil];
    NSDate *date = [dateFormatter dateFromString:sDate];
    return date;
}


#pragma - mark helper methods
/** 
 check if the date string is in 12h format
 */
+(BOOL)is12hr:(NSString*)dateString
{
    NSRange amRange = [dateString rangeOfString:@"am"];
    NSRange pmRange = [dateString rangeOfString:@"pm"];
    BOOL is12h = (amRange.location == NSNotFound && pmRange.location == NSNotFound);
    return is12h;
}

@end
