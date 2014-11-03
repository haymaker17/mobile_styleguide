//
//  CCFormatUtilities.h
//  ConcurMobile
//
//  Created by laurent mery on 24/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CCFormatUtilities : NSObject



#pragma mark - DATE

#pragma mark - from WebService (NSString) to Internal (NSDate)

/*
 * avoid multiple entrancec formats (try to change webservice before adding new entrance format)
 */
+(NSDate*)dateWithMdyyyy:(NSString*)Mdyyyy;
+(NSDate*)dateWithYYYYMMddTHHmmss:(NSString*)YYYYMMddTHHmmss;
+(NSDate*)dateWithYYYYMMddHHmmss:(NSString*)YYYYMMddHHmmss;
+(NSDate*)dateWithHma:(NSString*)hma;


#pragma mark - from Internal (NSDate) to WebService (NSString)



#pragma mark - Internal (NSDate) To UI (NSString)

/*
 * Returns a localized date format string representing the given date format components arranged appropriately for the specified locale.
 * Different locales have different conventions for the ordering of date components. You use this method to get an appropriate date formated string for a given set of template for a specified locale (currentLocal if nil)
 * example with 2014-08-06 and @"EEE MMM dd yyyy" as template (myTemplate)
 * (local en_US: Wed, Aug 06, 2014) (local fr_FR: mer. 06 août 2014) (local ja_JP: 2014年8月6日)
 * set nil for localisedOrNil's argument to used currentLocale (argument used specifically for unit test)
 * template examples:
 
		EEEE	Monday
		EEE		Mon
		yyyy	2014
		yy		14
		ddMMyy	14/04/24 (this method ordered fields with localized information)
		Hma		02:08 PM
 
 */
+(NSString*)formatedDate:(NSDate*)date withTemplate:(NSString*)myTSemplate localisedOrNil:(NSLocale*)localeOrNil;



#pragma mark - from UI (NSString : from input) To Internal (NSDate)




#pragma mark - From WebService (NSString) To UI (NSString)  --  shotcut

+(NSString*)formatedDateMdyyyy:(NSString*)Mdyyyy withTemplate:(NSString*)template;
+(NSString*)formatedDateYYYYMMddTHHmmss:(NSString*)YYYYMMddTHHmmss withTemplate:(NSString*)template;
+(NSString*)formatedTimeHma:(NSString*)hma withTemplate:(NSString*)template;

+(NSString*)formatedDate:(NSString*)value withTemplate:(NSString*)template;







#pragma mark - AMOUNT



#pragma mark - internal(NSString) To UI (NSString)
+(NSString*)formatAmount:(NSString*)value withCurrency:(NSString*)crnCode localisedOrNil:(NSLocale*)localeOrNil;


#pragma mark - from UI (NSString : from input) to Internal (NSString)


@end

