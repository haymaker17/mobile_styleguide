//
//  DateTimeConverter.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 11/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface DateTimeConverter : NSObject
{
}

+ (NSDate*) gmtDateWithSameComponentsAsLocalCurrentDate;
+ (NSDate*) gmtDateWithSameComponentsAsLocalDate:(NSDate*)localDate;
+ (NSDate*) localDateWithSameComponentsAsGmtDate:(NSDate*)gmtDate;
+ (NSDate*) targetTimeZoneDateWithSameComponentsAsSourceTimeZoneDate:(NSDate*)sourceDate sourceTimeZone:(NSTimeZone*)tzSource targetTimeZone:(NSTimeZone*)tzTarget;

@end
