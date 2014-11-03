//
//  NSDate+StringFormat.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/30/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "NSDateFormatter+Additions.h"

@interface NSDate (StringFormat)

/*
 * returns a date with the specified format and the timeZoneWithAbbreviation
 */
- (NSString*) stringWithFormat:(NSString*)format timeZone:(NSString*)timezone;

/*
 * returns a date with the specified format and the timeZoneWithAbbreviation and locale
 */
- (NSString*) stringWithFormat:(NSString*)format timeZone:(NSString*)timezone locale:(NSLocale*)locale;

@end
