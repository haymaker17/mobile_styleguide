//
//  NSDateAdditions.m
//  ConcurMobile
//
//  Created by charlottef on 3/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "NSDateAdditions.h"

@implementation NSDate (NSDateAdditions)

- (NSString*)dateGroupByMonth
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init];
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	[formatter setDateFormat:@"MMM yyyy"];
    NSString* dateString = [formatter stringFromDate:self];
	return dateString;
}

@end
