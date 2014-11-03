//
//  NSDate+StringFormat.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/30/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "NSDate+StringFormat.h"


@implementation NSDate (StringFormat)

- (NSString*) stringWithFormat:(NSString*)format timeZone:(NSString*)timezone{
   
    return [self stringWithFormat:format timeZone:timezone locale:nil];
}

- (NSString*) stringWithFormat:(NSString*)format timeZone:(NSString*)timezone locale:(NSLocale *)locale{
    NSDateFormatter *formatter = [NSDateFormatter dateFormatterWithFormat:format timeZoneWithAbbreviation:timezone locale:locale];
    return [formatter stringFromDate:self];
}

@end
