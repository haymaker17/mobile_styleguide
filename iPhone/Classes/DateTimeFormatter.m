//
//  DateTimeFormatter.m
//  ConcurMobile
//
//  Created by Paul Kramer on 2/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DateTimeFormatter.h"
#import "NSStringAdditions.h"

@implementation DateTimeFormatter

+(BOOL) needToLocalizeFormatString
{
    NSUserDefaults* defs = [NSUserDefaults standardUserDefaults];
    
    NSArray* languages = [defs objectForKey:@"AppleLanguages"];
    
    NSString* preferredLang = languages[0];
    
    NSLocale* locale = [NSLocale currentLocale];
    NSString* lang = [locale localeIdentifier];
    BOOL isAsianLang = [@"ja" isEqualToString:preferredLang] || [@"zh-Hans" isEqualToString:preferredLang] || [@"zh-Hant" isEqualToString:preferredLang];
    BOOL isAsianLocale = [@"ja_JP" isEqualToString:lang] || [lang hasPrefix: @"zh_"];
    
    return isAsianLang && isAsianLocale; // MOB-8166 reformat for Asian locales in Asian languages
}

+(NSString *)formatDateTimeEEEMMMddHHmmaazzz:(NSString *)dt
{
    //[NSDateFormatter dateFormatterWithFormat:CCDateFormatEEE_MMM_dd_HHcmm_aa_zzz timeZoneWithAbbreviation:@"GMT" locale:[NSLocale currentLocale]];
	NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
	[dateFormat setDateFormat: @"EEE MMM dd HH:mm aa zzz"];
	// Mob-2568
    [dateFormat setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormat setLocale:[NSLocale currentLocale]];
	
	NSDate *startDate = [DateTimeFormatter getNSDateFromMWSDateString:dt];
	NSString *startFormatted = [dateFormat stringFromDate:startDate];
	
	return startFormatted;
}

// Added for Fusion 14 flight summary
+(NSString *)formatTimeHHmmaa:(NSDate *)startDate
{
	NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
	[dateFormat setDateFormat: @"HH:mm aa"];
	// Mob-2568
    [dateFormat setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormat setLocale:[NSLocale currentLocale]];
	
//	NSDate *startDate = [DateTimeFormatter getNSDate:dt Format:@"yyyy-MM-dd'T'HH:mm:ss"];// [NSDate dateWithNaturalLanguageString:dt locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
	NSString *startFormatted = [dateFormat stringFromDate:startDate];
	
	return startFormatted;	
}

+(NSString *)formatTimeHHmm:(NSString *)dt
{
	NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
	[dateFormat setDateFormat: @"HHmm"];
	
	// Mob-2568
    [dateFormat setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormat setLocale:[NSLocale currentLocale]];
	
	NSDate *startDate = [DateTimeFormatter getNSDateFromMWSDateString:dt];
	NSString *startFormatted = [dateFormat stringFromDate:startDate];
	
	return startFormatted;	
}

+(NSString *)formatDateEEEMMMdd:(NSString *)dt
{
	
	NSDate *date = [DateTimeFormatter getNSDateFromMWSDateString:dt];
    
    return [DateTimeFormatter formatDateEEEMMMddByDate:date];
}
    
+(NSString *)formatDateEEEMMMddByDate:(NSDate*)date
{
	NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    NSLocale* locale = [NSLocale currentLocale];
    if ([self needToLocalizeFormatString]) // MOB-8166 reformat for Asian locales
    {
        NSString * format = [NSDateFormatter dateFormatFromTemplate:@"EEE MMM dd" options:0 locale:locale];
        [dateFormat setDateFormat:format];
    }
    else
        [dateFormat setDateFormat: @"EEE MMM dd"];
	
	// Mob-2568
    [dateFormat setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormat setLocale:[NSLocale currentLocale]];

	NSString *startFormatted = [dateFormat stringFromDate:date];
	
	return startFormatted;	
}

+(NSString *)formatDateEEEMMMddyyyyByDate:(NSDate *)date 
{    
    
	NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    
    NSLocale* locale = [NSLocale currentLocale];
    if ([self needToLocalizeFormatString]) // MOB-8166 reformat for Asian locales
    {
        NSString * format = [NSDateFormatter dateFormatFromTemplate:@"EEE MMM dd yyyy" options:0 locale:locale];
        [dateFormat setDateFormat:format];
    }
    else
        [dateFormat setDateFormat: @"EEE MMM dd yyyy"];
	
	// Mob-2568
    [dateFormat setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormat setLocale:[NSLocale currentLocale]];
	
		NSString *startFormatted = [dateFormat stringFromDate:date];
	
	return startFormatted;
	
}

// This function is used to format date for server calls hence it uses en_US_POSIX locale
// Do not use this function to display a date to the user as it wouldn't be localised
+(NSString *)formatDateYYYYMMddByDate:(NSDate *)dt
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	[dateFormatter setLocale:[[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"]];
	[dateFormatter setDateFormat: @"yyyy-MM-dd"];
    
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;
}

+(NSString *)formatDateYYYYMMddByDateLocal:(NSDate *)dt
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];

    [dateFormatter setTimeZone:[NSTimeZone localTimeZone]];

	[dateFormatter setDateFormat: @"yyyy-MM-dd"];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;
}

+(NSString *)formatDateMedium:(NSString *)dt
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	[dateFormatter setTimeStyle:NSDateFormatterNoStyle];
	NSDate *date = [DateTimeFormatter getNSDateFromMWSDateString:dt];
	NSString *formattedDateString = [dateFormatter stringFromDate:date];
	////NSLog(@"formattedDateShortString for locale %@: %@", [[dateFormatter locale] localeIdentifier], formattedDateString);
	return formattedDateString;
}

+(NSString *)formatDateLong:(NSString *)dt
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterLongStyle];
	[dateFormatter setTimeStyle:NSDateFormatterNoStyle];
	NSDate *date = [DateTimeFormatter getNSDateFromMWSDateString:dt];
	NSString *formattedDateString = [dateFormatter stringFromDate:date];
	////NSLog(@"formattedDateShortString for locale %@: %@", [[dateFormatter locale] localeIdentifier], formattedDateString);
	return formattedDateString;
}

// DJ- unreferenced- Delete if you see this after Dec 2013
//+(NSString *)formatDateFull:(NSString *)dt
//{	
//	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
//	// Mob-2568
//    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
//	// Localizing date
//	[dateFormatter setLocale:[NSLocale currentLocale]];
//	
//	[dateFormatter setDateStyle:NSDateFormatterFullStyle];
//	[dateFormatter setTimeStyle:NSDateFormatterNoStyle];
//	NSDate *date = [DateTimeFormatter getNSDate:dt Format:@"yyyy-MM-dd'T'HH:mm:ss"];//[NSDate dateWithNaturalLanguageString:dt locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
//	NSString *formattedDateString = [dateFormatter stringFromDate:date];
//	////NSLog(@"formattedDateShortString for locale %@: %@", [[dateFormatter locale] localeIdentifier], formattedDateString);
//	return formattedDateString;
//}
// DJ- unreferenced- Delete if you see this after Dec 2013
//+(NSString *)formatTimeFull:(NSString *)dt
//{	
//	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
//	// Mob-2568
//    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
//	// Localizing date
//	[dateFormatter setLocale:[NSLocale currentLocale]];
//	
//	[dateFormatter setDateStyle:NSDateFormatterNoStyle];
//	[dateFormatter setTimeStyle:NSDateFormatterFullStyle];
//	NSDate *date = [DateTimeFormatter getNSDate:dt Format:@"yyyy-MM-dd'T'HH:mm:ss"];//[NSDate dateWithNaturalLanguageString:dt locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
//	NSString *formattedDateString = [dateFormatter stringFromDate:date];
//	////NSLog(@"formattedDateShortString for locale %@: %@", [[dateFormatter locale] localeIdentifier], formattedDateString);
//	return formattedDateString;
//}
// DJ- unreferenced- Delete if you see this after Dec 2013
//+(NSString *)formatTimeLong:(NSString *)dt
//{	
//	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
//	// Mob-2568
//    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
//	// Localizing date
//	[dateFormatter setLocale:[NSLocale currentLocale]];
//	
//	[dateFormatter setDateStyle:NSDateFormatterNoStyle];
//	[dateFormatter setTimeStyle:NSDateFormatterLongStyle];
//	NSDate *date = [DateTimeFormatter getNSDate:dt Format:@"yyyy-MM-dd'T'HH:mm:ss"];//[NSDate dateWithNaturalLanguageString:dt locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
//	NSString *formattedDateString = [dateFormatter stringFromDate:date];
//	////NSLog(@"formattedDateShortString for locale %@: %@", [[dateFormatter locale] localeIdentifier], formattedDateString);
//	return formattedDateString;
//}
// DJ- unreferenced- Delete if you see this after Dec 2013
//+(NSString *)formatTimeShort:(NSString *)dt
//{	
//	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
//	// Mob-2568
//    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
//	// Localizing date
//	[dateFormatter setLocale:[NSLocale currentLocale]];
//	
//	[dateFormatter setDateStyle:NSDateFormatterNoStyle];
//	[dateFormatter setTimeStyle:NSDateFormatterShortStyle];
//	NSDate *date = [DateTimeFormatter getNSDate:dt Format:@"yyyy-MM-dd'T'HH:mm:ss"];//[NSDate dateWithNaturalLanguageString:dt locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
//	NSString *formattedDateString = [dateFormatter stringFromDate:date];
//	////NSLog(@"formattedDateShortString for locale %@: %@", [[dateFormatter locale] localeIdentifier], formattedDateString);
//	return formattedDateString;
//}

+(NSString *)formatDateTimeFull:(NSString *)dt
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterFullStyle];
	[dateFormatter setTimeStyle:NSDateFormatterFullStyle];
	NSDate *date = [DateTimeFormatter getNSDateFromMWSDateString:dt];
	NSString *formattedDateString = [dateFormatter stringFromDate:date];
	////NSLog(@"formattedDateShortString for locale %@: %@", [[dateFormatter locale] localeIdentifier], formattedDateString);
	return formattedDateString;
}

+(NSString *)formatDateTimeFullByDate:(NSDate *)dt
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterFullStyle];
	[dateFormatter setTimeStyle:NSDateFormatterFullStyle];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	////NSLog(@"formattedDateShortString for locale %@: %@", [[dateFormatter locale] localeIdentifier], formattedDateString);
	return formattedDateString;
}

+(NSString *)formatDateTimeMediumByDate:(NSDate *)dt
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	[dateFormatter setTimeStyle:NSDateFormatterMediumStyle];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;
}

+(NSString *)formatDateTimeMediumByDateLTZ:(NSDate *)dt
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];

    [dateFormatter setTimeZone:[NSTimeZone localTimeZone]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	[dateFormatter setTimeStyle:NSDateFormatterMediumStyle];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;
}

+(NSString *)formatDateMediumByDate:(NSDate *)dt
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;
}

+(NSString *)formatDateMediumByDate:(NSDate *)dt TimeZone:(NSTimeZone *)tz
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [dateFormatter setTimeZone:tz];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;
}

+(NSString *)formatDateFullByDate:(NSDate *)dt
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterFullStyle];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;
}

// DJ- unreferenced- Delete if you see this after Dec 2013
//+(NSString *)formatDateTimeLong:(NSString *)dt
//{	
//	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
//	// Mob-2568
//    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
//	// Localizing date
//	[dateFormatter setLocale:[NSLocale currentLocale]];
//	
//	[dateFormatter setDateStyle:NSDateFormatterLongStyle];
//	[dateFormatter setTimeStyle:NSDateFormatterLongStyle];
//	NSDate *date = [DateTimeFormatter getNSDate:dt Format:@"yyyy-MM-dd'T'HH:mm:ss"];//[NSDate dateWithNaturalLanguageString:dt locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
//	NSString *formattedDateString = [dateFormatter stringFromDate:date];
//	////NSLog(@"formattedDateShortString for locale %@: %@", [[dateFormatter locale] localeIdentifier], formattedDateString);
//	return formattedDateString;
//}
// DJ- unreferenced- Delete if you see this after Dec 2013
//+(NSString *)formatDateTimeShort:(NSString *)dt
//{	
//	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
//	// Mob-2568
//    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
//	// Localizing date
//	[dateFormatter setLocale:[NSLocale currentLocale]];
//	
//	[dateFormatter setDateStyle:NSDateFormatterShortStyle];
//	[dateFormatter setTimeStyle:NSDateFormatterShortStyle];
//	NSDate *date = [DateTimeFormatter getNSDate:dt Format:@"yyyy-MM-dd'T'HH:mm:ss"];//[NSDate dateWithNaturalLanguageString:dt locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
//	NSString *formattedDateString = [dateFormatter stringFromDate:date];
//	////NSLog(@"formattedDateShortString for locale %@: %@", [[dateFormatter locale] localeIdentifier], formattedDateString);
//	return formattedDateString;
//}
// DJ- unreferenced- Delete if you see this after Dec 2013
//+(NSString *)formatDateTimeMedium:(NSString *)dt
//{	
//	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
//	// Mob-2568
//    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
//	// Localizing date
//	[dateFormatter setLocale:[NSLocale currentLocale]];
//	
//	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
//	[dateFormatter setTimeStyle:NSDateFormatterMediumStyle];
//	NSDate *date = [DateTimeFormatter getNSDate:dt Format:@"yyyy-MM-dd'T'HH:mm:ss"];//[NSDate dateWithNaturalLanguageString:dt locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
//	NSString *formattedDateString = [dateFormatter stringFromDate:date];
//	////NSLog(@"formattedDateShortString for locale %@: %@", [[dateFormatter locale] localeIdentifier], formattedDateString);
//	return formattedDateString;
//}

+(NSString *)formatHour:(NSInteger)hour
{
	NSDateComponents *components = [[NSDateComponents alloc] init];
	[components setHour:hour];
	[components setMinute:0];
	[components setSecond:0];
	
	NSCalendar *calendar = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
	NSDate *date = [calendar dateFromComponents:components];

	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone localTimeZone]]; // timeZoneWithAbbreviation:@"GMT"]];
	
	[dateFormatter setDateStyle:NSDateFormatterNoStyle];
	[dateFormatter setTimeStyle:NSDateFormatterShortStyle];
	NSString* formattedHour = [dateFormatter stringFromDate:date];

	
	return formattedHour;
}

+(NSString *)formatTimeForTravel:(NSString *) sDate
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	NSDate *dt = [DateTimeFormatter getNSDateFromMWSDateString:sDate];
	[dateFormatter setDateFormat:@"h:mm aa"];
	NSString *madeValue = [dateFormatter stringFromDate:dt];
	return 	madeValue;
}

+(NSString *)formatDateTimeForTravel:(NSString *) sDate
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	NSDate *dt = [DateTimeFormatter getNSDateFromMWSDateString:sDate];
	return [DateTimeFormatter formatDateTimeForTravelByDate:dt];
}

+(NSString *)formatDateTimeEEEhmmaa:(NSString *) sDate
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
	NSDate *dt = [dateFormatter dateFromString:sDate];
	return [DateTimeFormatter formatDateTimeEEEhmmaaByDate:dt];
}

+(NSString *)formatDateTimeEEEhmmaaByDate:(NSDate *) dt
{
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [formatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[formatter setLocale:[NSLocale currentLocale]];
	
	[formatter setDateFormat:@"EEE h:mm aa"];
    //NSLog(@"[formatter stringFromDate:dt] = %@", [formatter stringFromDate:dt]);
	return [formatter stringFromDate:dt];	
}

+(NSString *)formatDateEEEByDate:(NSDate *) dt
{
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [formatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[formatter setLocale:[NSLocale currentLocale]];
	
	[formatter setDateFormat:@"EEE"];
    //NSLog(@"[formatter stringFromDate:dt] = %@", [formatter stringFromDate:dt]);
	return [formatter stringFromDate:dt];	
}

+(NSString *)formatDateTimeForTravelByDate:(NSDate *) dt
{
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [formatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[formatter setLocale:[NSLocale currentLocale]];
	
    NSLocale* locale = [NSLocale currentLocale];
    if ([self needToLocalizeFormatString]) // MOB-8166 reformat for Asian locales
    {
        NSString * format = [NSDateFormatter dateFormatFromTemplate:@"EEE MMM dd h:mm aa" options:0 locale:locale];
        [formatter setDateFormat:format];
    }
    else
        [formatter setDateFormat:@"EEE MMM dd h:mm aa"];
	return [formatter stringFromDate:dt];	
}

+(NSString *)formatDateTimeForTravelCliqbookByDate:(NSDate *) dt
{
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [formatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[formatter setLocale:[NSLocale currentLocale]];
	
	[formatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
	return [formatter stringFromDate:dt];	
}

+(NSString *)formatDateForTravel:(NSString *) sDate
{
	NSDate *dt = [DateTimeFormatter getNSDateFromMWSDateString:sDate];
	
	return [DateTimeFormatter formatDateForTravelByDate:dt];
}

+(NSString *)formatDateForTravelByDate:(NSDate *) dt
{
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [formatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[formatter setLocale:[NSLocale currentLocale]];

    NSLocale* locale = [NSLocale currentLocale];
    if ([self needToLocalizeFormatString]) // MOB-8166 reformat for Asian locales
    {
        NSString * format = [NSDateFormatter dateFormatFromTemplate:@"EEE MMM dd" options:0 locale:locale];
        [formatter setDateFormat:format];
    }
    else
        [formatter setDateFormat:@"EEE MMM dd"];
	NSString *formattedString = [formatter stringFromDate:dt];
	return formattedString;	
}

+(NSString *)formatDateForAirBookingByDate:(NSDate *) dt
{
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	// Mob-2568, MOB-7240
    [formatter setTimeZone:[NSTimeZone localTimeZone]];  // This is a local time
	// Localizing date
	[formatter setLocale:[NSLocale currentLocale]];
	
	[formatter setDateFormat:@"yyyy-MM-dd"];
	NSString *formattedString = [formatter stringFromDate:dt];
	return formattedString;	
}

+(NSString *)formatDateForTravelyyyymmdd:(NSString *) sDate
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
	NSDate *dt = [dateFormatter dateFromString:sDate];
	
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [formatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[formatter setLocale:[NSLocale currentLocale]];
	
	[formatter setDateFormat:@"yyyy MM dd"];
	return [formatter stringFromDate:dt];	
}

+(NSString *)formatDateForBooking:(NSDate *)dt
{
	NSString *dayMonthYear = [self formatDateMediumByDate:dt  TimeZone:[NSTimeZone localTimeZone]];
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	// Mob-2568

	[formatter setTimeZone:[NSTimeZone localTimeZone]]; 
	// Localizing date
	[formatter setLocale:[NSLocale currentLocale]]; //this is not exactly good.
	
	[formatter setDateFormat:@"EEE"];
	NSString *weekday = [formatter stringFromDate:dt];	
	
    if (weekday == nil && dayMonthYear == nil)
        return @""; // To prevent "(null) (null)"
    else
        return [NSString stringWithFormat:@"%@ %@", weekday, dayMonthYear]; //right here we are specifying a format that is probably not in locale.
	//for example en_US will show dayMonthYear as Mar 12, 2011 whereas UK will come back as 12 Mar 2011.  So, you can never specify the format of this string
	//if you want to go string to NSDate... because you don't know what the date format actually is.
}

+(NSString *)formatDateForBooking:(NSDate *)dt TimeZone:(NSTimeZone *)tz
{
	NSString *dayMonthYear = [self formatDateMediumByDate:dt];
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [formatter setTimeZone:tz];
	// Localizing date
	[formatter setLocale:[NSLocale currentLocale]];
	
	[formatter setDateFormat:@"EEE"];
	NSString *weekday = [formatter stringFromDate:dt];	
	
	return [NSString stringWithFormat:@"%@ %@", weekday, dayMonthYear];
}


+(NSDate *) getLocalDate:(NSString*) string
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
	NSDate *dt = [dateFormatter dateFromString:string];
	return dt;
}

+(NSDate *) getLocalDateMedium:(NSString*) string
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 

    [dateFormatter setTimeZone:[NSTimeZone localTimeZone]];
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	NSDate *dt = [dateFormatter dateFromString:string];
	return dt;
}

+(NSString *) getLocalDateAsString:(NSDate*)dt
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 

	[dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
	NSString *str = [dateFormatter stringFromDate:dt];
	return str;
}


+(NSDate *) getNSDate:(NSString*) string Format:(NSString *)format
{
    NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:format timeZoneWithAbbreviation:@"GMT" locale:[NSLocale currentLocale]];
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateFormat:format]; //@"yyyy-MM-dd'T'HH:mm:ss"
	NSDate *dt = [dateFormatter dateFromString:string];
	return dt;
}

//MOB-15712: Get the date format based on 12/24hr setting for a string.
+(NSString *)getDateFormatForString:(NSString *)exampleDateStr
{
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setLocale:[NSLocale currentLocale]];
    [formatter setDateStyle:NSDateFormatterNoStyle];
    [formatter setTimeStyle:NSDateFormatterShortStyle];
    NSRange amRange = [exampleDateStr rangeOfString:[formatter AMSymbol]];
    NSRange pmRange = [exampleDateStr rangeOfString:[formatter PMSymbol]];
    BOOL is24h = (amRange.location == NSNotFound && pmRange.location == NSNotFound);
    return is24h ? @"yyyy-MM-dd'T'HH:mm:ss" : @"yyyy-MM-dd'T'hh:mm:ss";
}

+(NSDate *) getNSDateForGov:(NSString*)string Format:(NSString *)format TimeZone:(NSTimeZone *)tz
{
    if (![string length])
		return nil;

	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init ];
    
    [dateFormatter setTimeZone:tz];

	[dateFormatter setLocale:[NSLocale currentLocale]];

	[dateFormatter setDateFormat:format];
	NSDate *dt = [dateFormatter dateFromString:string];

	return dt;
}

+(NSDate *) getNSDate:(NSString*)string Format:(NSString *)format TimeZone:(NSTimeZone *)tz
{
	if (![string length])
		return nil;
    
	//this method bypassess the MOB-2568 fix, that actually messes up dates used for comparison.
	//the purpose of this method is to take a string (that is in local) and get an NSDate object back that represents that displayed date.
	//So, we actually want the GMT NSDate to be an accurate reflection of the locally formatted date...
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init ];

    [dateFormatter setTimeZone:tz];

//	[dateFormatter setLocale:[[[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"] autorelease]];
//
//	[dateFormatter setFormatterBehavior:NSDateFormatterBehavior10_4];
	
	[dateFormatter setLocale:[NSLocale currentLocale]];
	//[dateFormatter setLocale:[[[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"] autorelease]];
	
	[dateFormatter setDateFormat:format]; //@"yyyy-MM-dd'T'HH:mm:ss"
	NSDate *dt = [dateFormatter dateFromString:string];
    
    if (dt == nil)
    {
        //MOB-15712: Zero out time value in the string passed in
        //fld.fieldValue can be in 24 hr, but phone setting is 12 hr.
        //If time has no value like 00:00:00, things works fine. If time has value like 23:59:00, this cause string parsing unsuccessful.
        //We only show day month and year on UI.  Time is not used and visable from UI.
        //Modify time only for string date parsing.
        if ( ![self userSettingsPrefers24HourFormat] && [[self getDateFormatForString:string] isEqualToString:@"yyyy-MM-dd'T'HH:mm:ss"])
        {
            NSString *timeZero = [NSString stringWithFormat:@"%@%@", [string substringToIndex:11], @"00:00:00"] ;
            string = timeZero;
        }
        dt = [dateFormatter dateFromString:string];
    }
    
	return dt;
}

+(NSString *) formatDate:(NSDate*) dt Format:(NSString *)format  TimeZone:(NSTimeZone *)tz
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [dateFormatter setTimeZone:tz];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateFormat:format];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;    
}

+(NSString *)formatLocalDateMedium:(NSString *)dt
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	[dateFormatter setTimeStyle:NSDateFormatterNoStyle];
	NSDate *date = [DateTimeFormatter getLocalDate: dt];
	NSString *formattedDateString = [dateFormatter stringFromDate:date];
	////NSLog(@"formattedDateShortString for locale %@: %@", [[dateFormatter locale] localeIdentifier], formattedDateString);
	return formattedDateString;
}


+(NSString *)formatDateyyyyMMdd:(NSString *) sDate
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	NSDate *dt = [DateTimeFormatter getNSDateFromMWSDateString:sDate];
	
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	[formatter setDateFormat:@"yyyyMMdd"];
	return [formatter stringFromDate:dt];
}

+(NSString *)formatDateFromServer:(NSString *) sDate returnFormat:(NSString *) returnFormat
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
	// Mob-2568
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
	NSDate *dt = [dateFormatter dateFromString:sDate];
	
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	[formatter setDateFormat:returnFormat];
	return [formatter stringFromDate:dt];
}

+(NSString *)formatDateForCarOrHotelTravelByDate:(NSDate *) dt
{
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	// Mob-2568
    [formatter setTimeZone:[NSTimeZone localTimeZone]];
	// Localizing date
	[formatter setLocale:[NSLocale currentLocale]];
	
    NSLocale* locale = [NSLocale currentLocale];
    if ([self needToLocalizeFormatString]) // MOB-8166 reformat for Asian locales
    {
        NSString * format = [NSDateFormatter dateFormatFromTemplate:@"EEE MMM dd" options:0 locale:locale];
        [formatter setDateFormat:format];
    }
    else
        [formatter setDateFormat:@"EEE MMM dd"];
	NSString *formattedString = [formatter stringFromDate:dt];
	return formattedString;	
}

+(NSString *)formatDateMediumByHotelOrCarDate:(NSDate *)dt inTimeZone:(NSTimeZone *)timeZone
{	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];

    [dateFormatter setTimeZone:timeZone];
	
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	NSString *formattedDateString = [dateFormatter stringFromDate:dt];
	return formattedDateString;
}

+(NSString *)formatHotelOrCarDateForBooking:(NSDate *)dt
{
    return [DateTimeFormatter formatHotelOrCarDateForBooking:dt inTimeZone:[NSTimeZone localTimeZone]];
}

+(NSString *)formatHotelOrCarDateForBooking:(NSDate *)dt inTimeZone:(NSTimeZone*)timeZone
{
	NSString *dayMonthYear = [self formatDateMediumByHotelOrCarDate:dt inTimeZone:timeZone];
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];

	[formatter setTimeZone:timeZone];
	
	// Localizing date
	[formatter setLocale:[NSLocale currentLocale]];
	
	[formatter setDateFormat:@"EEE"];
	NSString *weekday = [formatter stringFromDate:dt];	
	
	return [NSString stringWithFormat:@"%@ %@", weekday, dayMonthYear];
}

+(NSString *) formatDuration:(NSString *)startDate endDate:(NSString *) endDate
{
	NSDate *dtStart = [DateTimeFormatter getNSDateFromMWSDateString:startDate];
	NSDate *dtEnd = [DateTimeFormatter getNSDateFromMWSDateString:endDate];
	NSTimeInterval interval = [dtEnd timeIntervalSinceDate:dtStart];
    int days = interval / (60 * 60 * 24);
    interval -= days * (60 * 60 * 24);
    int hours = interval / (60 * 60);
    interval -= hours * (60 * 60);
    int minutes = interval / 60;
	
	NSMutableString *sDuration = [[NSMutableString alloc] initWithString:@""];
	
	if(days > 1)
		[sDuration appendString:[NSString stringWithFormat:@"%d %@", days, [Localizer getLocalizedText:@"days"]]];
	else if(days == 1)
		[sDuration appendString:[NSString stringWithFormat:@"%d %@", days, [Localizer getLocalizedText:@"day"]]];
	
	if(hours > 1)
	{
		[sDuration appendString:@" "];
		[sDuration appendString:[NSString stringWithFormat:@"%d %@", hours, [Localizer getLocalizedText:@"hours"]]];
	}
	else if(hours == 1)
	{
		[sDuration appendString:@" "];
		[sDuration appendString:[NSString stringWithFormat:@"%d %@", hours, [Localizer getLocalizedText:@"hour"]]];
	}
	
	if(minutes > 1)
	{
		[sDuration appendString:@" "];
		[sDuration appendString:[NSString stringWithFormat:@"%d %@", minutes, [Localizer getLocalizedText:@"minutes"]]];
	}
	else if(minutes == 1)
	{
		[sDuration appendString:@" "];
		[sDuration appendString:[NSString stringWithFormat:@"%d %@", minutes, [Localizer getLocalizedText:@"minute"]]];
	}
	
	return sDuration;
}

+(NSString*)formatDateFromReceiptStoreToString:(NSString*)sDate returnFormat:(NSString *) returnFormat
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
	NSDate *dt = [dateFormatter dateFromString:sDate];
	
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	[formatter setDateFormat:returnFormat];
	return [formatter stringFromDate:dt];
}

// Pass tz as nil to use local time zone
+(NSDate*) getDateWithoutTime:(NSDate*) origDate withTimeZoneAbbrev:(NSString*) tzAbbrev 
{
    NSTimeZone* tz = tzAbbrev== nil? [NSTimeZone localTimeZone] : [NSTimeZone timeZoneWithAbbreviation:tzAbbrev];
    
    NSString* dateStr = [DateTimeFormatter formatDate:origDate Format:@"yyyy-MM-dd" TimeZone:tz];
    
    NSDate* dateWithoutTime = [DateTimeFormatter getNSDate:dateStr Format:@"yyyy-MM-dd" TimeZone:tz];
    
    return dateWithoutTime;
}

+(NSDate*) getDateWithoutTimeInGMT:(NSDate*) date
{
    NSTimeZone* tz = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
    
    NSString* dateStr = [DateTimeFormatter formatDate:date Format:@"yyyy-MM-dd" TimeZone:tz];
    
    NSDate* dateWithoutTime = [DateTimeFormatter getNSDate:dateStr Format:@"yyyy-MM-dd" TimeZone:tz];
    return dateWithoutTime;
}

+(NSInteger) getTimeInSeconds:(NSDate*) date withTimeZoneAbbrev:(NSString*) tzAbbrev
{
    NSDate* dawnDate = [DateTimeFormatter getDateWithoutTime:date withTimeZoneAbbrev:tzAbbrev];    
    return [date timeIntervalSinceDate:dawnDate];
}

// MOB-7192 Strip out timezone info in current local time, so that we can compare it with travel time in GMT(w/o timezone info).
+(NSDate*) getCurrentLocalDateTimeInGMT
{
    NSTimeZone* localTZ = [NSTimeZone localTimeZone];
    NSInteger offsetGMT = [localTZ secondsFromGMT];
    NSDate* now = [NSDate dateWithTimeIntervalSinceNow:offsetGMT];    
    return now;
}

/*
 * https://github.com/erica/NSDate-Extensions may help here
 */

+(NSDate*) getNextHourDateTimeInGMT
{ 
    NSDate *now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
    
    NSDate* dateWithoutTime = [DateTimeFormatter getDateWithoutTime:now withTimeZoneAbbrev:@"GMT"];
    NSInteger timeInMinutes = [now timeIntervalSinceDate:dateWithoutTime]/60;
    timeInMinutes = (timeInMinutes+59)/60*60;
    
    NSDate* nextHour = [dateWithoutTime dateByAddingTimeInterval:timeInMinutes * 60];
    return nextHour;
}

+(NSString*) formatBookingDateTime:(NSDate *)fullDate
{
    NSTimeZone* gmtTz = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
    // Fix for : MOB-7820
    return [DateTimeFormatter formatDate:fullDate Format:@"EEE MMM dd, h:mm aa" TimeZone:gmtTz];
}

+(NSString *)formatExpenseDateEEEMMMDDYYYY:(NSDate *)dt
{
    return [self formatDateForBooking:dt];
}

+(NSDate*)getISO8601Date:(NSString*)dateString
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init]; 
    [dateFormatter setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]]; // GMT
    [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"];
    NSDate *date = [dateFormatter dateFromString:dateString];
    return date;
}

+(BOOL)userSettingsPrefers24HourFormat
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateStyle:NSDateFormatterNoStyle];
    [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
    NSString *currentTimeString = [dateFormatter stringFromDate:[NSDate date]];
    
    return ([currentTimeString rangeOfString:[dateFormatter AMSymbol]].location == NSNotFound &&
            [currentTimeString rangeOfString:[dateFormatter PMSymbol]].location == NSNotFound);
}

+(NSDate *) getNSDateFromMWSDateString:(NSString *) dateString
{
    static NSDateFormatter *dateFormatter = nil; // Caching- Creating a date formatter is not a cheap operation and this one does not depend on UserSettings so it is not going to change
    if (dateFormatter == nil) {
        NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
        
        dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setLocale:enUSPOSIXLocale];
        [dateFormatter setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]]; // GMT time Zone
        [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
    }

    return [dateFormatter dateFromString:dateString];
}


@end
