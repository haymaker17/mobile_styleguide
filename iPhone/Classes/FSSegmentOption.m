//
//  FSSegmentOption.m
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "FSSegmentOption.h"


@implementation FSSegmentOption
@synthesize flights;
@synthesize totalElapsedTime;
@synthesize sId;
@synthesize travelConfigId;


-(id)init
{
	self = [super init];
    if (self)
    {
        flights = [[NSMutableArray alloc] init];
    }
	return self;
}

-(FSFlight*) getCurrentFlight
{
    return (FSFlight*)[flights lastObject];
}

-(NSString*) carrierText
{
    NSMutableString *buf = [[NSMutableString alloc] init];

    for (FSFlight *flt in flights)
    {
        if ([buf length] > 0)
            [buf appendString:@" / "];
        [buf appendFormat:@"%@ %@", flt.carrier, flt.fltNum];
    }
    
    return buf;
}

#pragma mark -
#pragma mark Simplified parser functions

-(void) startTag:(NSString*)tag
{
    if ([tag isEqualToString:@"Flight"])
    {
        [flights addObject:[[FSFlight alloc] init]];
    }
}

-(void)endTag:(NSString*)tag withText:(NSString*)text
{
    if ([tag isEqualToString:@"ID"])
    {
        sId = text;
    }
    else if ([tag isEqualToString:@"TravelConfigID"])
    {
        travelConfigId = text;
    }
    else if ([tag isEqualToString:@"TotalElapsedTime"])
    {
        totalElapsedTime = [text intValue];
    }
    
}


@end
