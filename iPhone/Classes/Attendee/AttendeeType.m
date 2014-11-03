//
//  AttendeeType.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 4/25/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "AttendeeType.h"

@implementation AttendeeType
@synthesize atnTypeKey, atnTypeCode, atnTypeName, allowEditAtnCount, isExternal;

static NSMutableDictionary * attendeeTypeXmlToPropertyMap;

+ (NSMutableDictionary*) getXmlToPropertyMap
{
	return attendeeTypeXmlToPropertyMap;
}

+ (void)initialize
{
	if (self == [AttendeeType class]) 
	{
        // Perform initialization here.
		attendeeTypeXmlToPropertyMap = [[NSMutableDictionary alloc] init];
		attendeeTypeXmlToPropertyMap[@"AtnTypeKey"] = @"AtnTypeKey";
		attendeeTypeXmlToPropertyMap[@"AtnTypeCode"] = @"AtnTypeCode";
		attendeeTypeXmlToPropertyMap[@"AtnTypeName"] = @"AtnTypeName";
        attendeeTypeXmlToPropertyMap[@"AllowEditAtnCount"] = @"AllowEditAtnCount";
        attendeeTypeXmlToPropertyMap[@"IsExternal"] = @"IsExternal";
    }
}


@end
