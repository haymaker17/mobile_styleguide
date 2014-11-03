//
//  CCFormatUtilities.m
//  ConcurMobile
//
//  Created by laurent mery on 24/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CCFormatUtilities.h"

@implementation CCFormatUtilities


#pragma mark - DATE

#pragma mark - from WebService (NSString) to Internal (NSDate)


/*
 * avoid multiple entrance formats (try to change webservice before adding new entrance format)
 * TODO: ask to philippe that webservice provide only one entrance date format
 */
+(NSDate*)dateWithMdyyyy:(NSString*)Mdyyyy{
	
	NSDate *nsDate;
	
	if (Mdyyyy != nil) {
		
		static NSDateFormatter *CCdateFormatterMdyyyy = nil;
		if (CCdateFormatterMdyyyy == nil){
			
			CCdateFormatterMdyyyy = [[NSDateFormatter alloc]init];
			[CCdateFormatterMdyyyy setDateFormat:@"M/d/yyyy"];
			[CCdateFormatterMdyyyy setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
		}
		
		nsDate = [CCdateFormatterMdyyyy dateFromString:Mdyyyy];
	}
	
	return nsDate;
}

+(NSDate*)dateWithYYYYMMddTHHmmss:(NSString*)YYYYMMddTHHmmss{
	
	
	NSDate *nsDate;
	
	if (YYYYMMddTHHmmss != nil) {
		
		static NSDateFormatter *CCdateFormatterYYYYMMddTHHmmss = nil;
		if (CCdateFormatterYYYYMMddTHHmmss == nil){
			
			CCdateFormatterYYYYMMddTHHmmss = [[NSDateFormatter alloc]init];
			[CCdateFormatterYYYYMMddTHHmmss setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
			[CCdateFormatterYYYYMMddTHHmmss setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
		}
		
		nsDate = [CCdateFormatterYYYYMMddTHHmmss dateFromString:YYYYMMddTHHmmss];
	}
	
	return nsDate;
}

+(NSDate*)dateWithYYYYMMddHHmmss:(NSString*)YYYYMMddHHmmss{
	
	
	NSDate *nsDate;
	
	if (YYYYMMddHHmmss != nil) {
		
		static NSDateFormatter *CCdateFormatterYYYYMMddHHmmss = nil;
		if (CCdateFormatterYYYYMMddHHmmss == nil){
			
			CCdateFormatterYYYYMMddHHmmss = [[NSDateFormatter alloc]init];
			[CCdateFormatterYYYYMMddHHmmss setDateFormat:@"yyyy-MM-dd HH:mm:ss.0"];
			[CCdateFormatterYYYYMMddHHmmss setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
		}
		
		nsDate = [CCdateFormatterYYYYMMddHHmmss dateFromString:YYYYMMddHHmmss];
	}
	
	return nsDate;
}

+(NSDate*)dateWithHma:(NSString*)hma{
	
	
	NSDate *nsDate;
	
	if (hma != nil) {
		
		static NSDateFormatter *CCfdateFormatterhma = nil;
		if (CCfdateFormatterhma == nil){
			
			CCfdateFormatterhma = [[NSDateFormatter alloc]init];
			[CCfdateFormatterhma setDateFormat:@"h:m a"];
			[CCfdateFormatterhma setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
		}
		
		nsDate = [CCfdateFormatterhma dateFromString:hma];
	}
	
	return nsDate;
}


#pragma mark - from Internal (NSDate) to WebService (NSString)



#pragma mark - Internal (NSDate) To UI (NSString)

/*
 * Returns a localized date format string representing the given date format components arranged appropriately for the specified locale.
 * Different locales have different conventions for the ordering of date components. You use this method to get an appropriate date formated string for a given set of template for a specified locale (currentLocal if nil)
 * example with 2014-08-06 and @"EEE MMM dd yyyy" as template (myTemplate)
 * (local en_US: Wed, Aug 06, 2014) (local fr_FR: mer. 06 août 2014) (local ja_JP: 2014年8月6日)
 * set nil for localisedOrNil's argument to used currentLocale (argument used specifically for unit test)
 */
+(NSString*)formatedDate:(NSDate*)date withTemplate:(NSString*)myTemplate localisedOrNil:(NSLocale*)localeOrNil{
	
	NSLocale *locale = [NSLocale currentLocale];
	
	// If the date formatters aren't already set up, create them and cache them for reuse.
	static NSDateFormatter *CCDateFormatterFormatWithTemplate = nil;
 
	if (CCDateFormatterFormatWithTemplate == nil){
		
		CCDateFormatterFormatWithTemplate = [[NSDateFormatter alloc]init];
		[CCDateFormatterFormatWithTemplate setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
		[CCDateFormatterFormatWithTemplate setLocale:locale];
	}

	if (localeOrNil != nil) {
		[CCDateFormatterFormatWithTemplate setLocale:localeOrNil];
		locale = localeOrNil;
	}
	
	[CCDateFormatterFormatWithTemplate setDateFormat:[NSDateFormatter dateFormatFromTemplate:myTemplate options:nil locale:locale]];
	NSString *value = [CCDateFormatterFormatWithTemplate stringFromDate:date];
	
	if (localeOrNil != nil) {
		[CCDateFormatterFormatWithTemplate setLocale:[NSLocale currentLocale]];
	}
	
	return value;
}



#pragma mark - from UI (NSString : from input) To Internal (NSDate)




#pragma mark - From WebService (NSString) To UI (NSString)  --  shotcut

+(NSString*)formatedDateMdyyyy:(NSString*)Mdyyyy withTemplate:(NSString*)template{
	
	NSString *date = @"";
	
	if (Mdyyyy != nil && ![@"" isEqualToString:Mdyyyy]) {
		
		NSDate *nsdate = [[self class] dateWithMdyyyy:Mdyyyy];
		date = [[self class] formatedDate:nsdate withTemplate:template localisedOrNil:nil];
	}
	
	return date;
}


+(NSString*)formatedDateYYYYMMddTHHmmss:(NSString*)YYYYMMddTHHmmss withTemplate:(NSString*)template{
	
	NSString *date = @"";
	
	if (YYYYMMddTHHmmss != nil && ![@"" isEqualToString:YYYYMMddTHHmmss]) {
		
		NSDate *nsdate = [[self class] dateWithYYYYMMddTHHmmss:YYYYMMddTHHmmss];
		date = [[self class] formatedDate:nsdate withTemplate:template localisedOrNil:nil];
	}
	
	return date;
}

+(NSString*)formatedDateYYYYMMddHHmmss:(NSString*)YYYYMMddHHmmss withTemplate:(NSString*)template{
	
	NSString *date = @"";
	
	if (YYYYMMddHHmmss != nil && ![@"" isEqualToString:YYYYMMddHHmmss]) {
		
		NSDate *nsdate = [[self class] dateWithYYYYMMddHHmmss:YYYYMMddHHmmss];
		date = [[self class] formatedDate:nsdate withTemplate:template localisedOrNil:nil];
	}
	
	return date;
}



+(NSString*)formatedTimeHma:(NSString*)hma withTemplate:(NSString*)template{
	
	NSString *time = @"";
	
	if (hma != nil && ![@"" isEqualToString:hma]) {
		
		NSDate *nsdate = [[self class] dateWithHma:hma];
		time = [[self class] formatedDate:nsdate withTemplate:template localisedOrNil:nil];
	}
	
	return time;
}

+(NSString*)formatedDate:(NSString*)value withTemplate:(NSString*)template{
	
	if (value == nil || [@"" isEqualToString:value]){
		
		value = @"";
	}
	else if ([value length] == 10) {
		
		value = [[self class] formatedDateMdyyyy:value withTemplate:template];
	}
	else if ([@"T" isEqualToString:[value substringWithRange:NSMakeRange(10, 1)]]){
		
		value = [[self class] formatedDateYYYYMMddTHHmmss:value withTemplate:template];
	}
	else {
		
		value = [[self class] formatedDateYYYYMMddHHmmss:value withTemplate:template];
	}
	
	return value;
}



#pragma mark - AMOUNT



#pragma mark - internal(NSString) To UI (NSString)

+(NSString*)formatAmount:(NSString*)value withCurrency:(NSString*)crnCode localisedOrNil:(NSLocale*)localeOrNil;{
	
	if (value == nil || [@"" isEqualToString:value]) {
		
		value = @"0";
	}
	
	NSLocale *locale = [NSLocale currentLocale];

	NSNumber *numberAmount = [NSNumber numberWithDouble:[value doubleValue]];
	
	static NSNumberFormatter *CCNumberFormatter1234321 = nil;
 
	if (CCNumberFormatter1234321 == nil){
		
		CCNumberFormatter1234321 = [[NSNumberFormatter alloc] init];
		[CCNumberFormatter1234321 setLocale:locale];
		[CCNumberFormatter1234321 setNumberStyle:NSNumberFormatterCurrencyStyle];
		[CCNumberFormatter1234321 setFormatterBehavior:NSNumberFormatterBehavior10_4];
	}
	[CCNumberFormatter1234321 setCurrencyCode:crnCode];
	if (localeOrNil != nil) {
		
		[CCNumberFormatter1234321 setLocale:localeOrNil];
	}
	
	NSString *amount = [CCNumberFormatter1234321 stringFromNumber:numberAmount];

	//restore cache
	if (localeOrNil != nil) {
		
		[CCNumberFormatter1234321 setLocale:[NSLocale currentLocale]];
	}
	
	return amount;
}


#pragma mark - from UI (NSString : from input) to Internal (NSString)


@end
