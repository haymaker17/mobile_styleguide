//
//  NSString+DateFormat.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/29/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "NSString+DateFormat.h"



@implementation NSString (DateFormat)

- (NSDate *)dateWithFormat:(NSString*)format timeZone:(NSString*)timezone{

    return [self dateWithFormat:format timeZone:timezone locale:nil];
}

- (NSDate *)dateWithFormat:(NSString*)format timeZone:(NSString*)timezone locale:(NSLocale *)locale{
    
    NSDateFormatter *formatter = [NSDateFormatter dateFormatterWithFormat:format timeZoneWithAbbreviation:timezone locale:locale];
    return [formatter dateFromString:self];
}

@end

