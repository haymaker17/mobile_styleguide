//
//  DateTimeConverter.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 11/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DateTimeConverter.h"


@implementation DateTimeConverter

+ (NSDate*) gmtDateWithSameComponentsAsLocalCurrentDate
{
	NSDate* currentDate = [NSDate date];
	return [DateTimeConverter gmtDateWithSameComponentsAsLocalDate:currentDate];
}

+ (NSDate*) gmtDateWithSameComponentsAsLocalDate:(NSDate*)localDate
{
	NSTimeZone *localTimeZone = [NSTimeZone localTimeZone];
	NSTimeZone *gmtTimeZone = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
	return [DateTimeConverter targetTimeZoneDateWithSameComponentsAsSourceTimeZoneDate:localDate sourceTimeZone:localTimeZone targetTimeZone:gmtTimeZone];
}

+ (NSDate*) localDateWithSameComponentsAsGmtDate:(NSDate*)gmtDate
{
	NSTimeZone *localTimeZone = [NSTimeZone localTimeZone];
	NSTimeZone *gmtTimeZone = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
	return [DateTimeConverter targetTimeZoneDateWithSameComponentsAsSourceTimeZoneDate:gmtDate sourceTimeZone:gmtTimeZone targetTimeZone:localTimeZone];
}

+ (NSDate*) targetTimeZoneDateWithSameComponentsAsSourceTimeZoneDate:(NSDate*)sourceDate sourceTimeZone:(NSTimeZone*)tzSource targetTimeZone:(NSTimeZone*)tzTarget
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 

    [dateFormatter setTimeZone:tzSource];
	
	[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
	NSString *sourceDateString = [dateFormatter stringFromDate:sourceDate];

	[dateFormatter setTimeZone:tzTarget];
	NSDate *dt = [dateFormatter dateFromString:sourceDateString];
	return dt;
}	

@end
