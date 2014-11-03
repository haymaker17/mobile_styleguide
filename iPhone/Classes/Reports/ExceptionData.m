//
//  ExceptionData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/5/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ExceptionData.h"


@implementation ExceptionData
@synthesize exceptionsStr, severityLevel;


#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
    [coder encodeObject:exceptionsStr	forKey:@"exceptionsStr"];
	[coder encodeObject:severityLevel	forKey:@"severityLevel"];
}

- (id)initWithCoder:(NSCoder *)coder {
    self.exceptionsStr = [coder decodeObjectForKey:@"exceptionsStr"];
	self.severityLevel = [coder decodeObjectForKey:@"severityLevel"];
    return self;
}


@end
