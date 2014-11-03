//
//  AttendeeGroup.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 4/3/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "AttendeeGroup.h"
#import "AttendeeData.h"

@implementation AttendeeGroup
@synthesize groupKey, name;
@dynamic		firstName;
@dynamic		lastName;



- (NSString*) firstName	// Dynamic readonly property
{
	return @"";
}

- (NSString*) lastName	// Dynamic readonly property for search result sorting, make sure groups are always on top
{
    return @"  ";
}


@end
