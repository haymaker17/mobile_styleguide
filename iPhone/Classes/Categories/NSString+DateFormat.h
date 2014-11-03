//
//  NSString+DateFormat.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/29/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "NSDateFormatter+Additions.h"


@interface NSString (DateFormat)

/*
 * returns a date with the specified format and the timeZoneWithAbbreviation
 */
- (NSDate *)dateWithFormat:(NSString*)format timeZone:(NSString*)timezone;

/*
 * returns a date with the specified format and the timeZoneWithAbbreviation and locale
 */
- (NSDate *)dateWithFormat:(NSString*)format timeZone:(NSString*)timezone locale:(NSLocale*) locale;

@end
