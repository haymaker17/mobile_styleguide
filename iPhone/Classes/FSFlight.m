//
//  FSFlight.m
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "FSFlight.h"
#import "DateTimeFormatter.h"

@implementation FSFlight
@synthesize carrier;
@synthesize operatingCarrier;
@synthesize arrDateTime;
@synthesize aircraftCode;
@synthesize numStops;
@synthesize depAirp;
@synthesize arrAirp;
@synthesize fltNum;
@synthesize classesOfService;
@synthesize depDateTime;


-(id)init
{
	self = [super init];
    if (self)
    {
        classesOfService = [[NSMutableArray alloc] init];
    }
	return self;
}


-(FSClassOfService*) getCurrentClassOfService
{
    return (FSClassOfService*)[classesOfService lastObject];
}


#pragma mark -
#pragma mark Simplified parser functions

-(void) startTag:(NSString*)tag withAttributeData:(NSDictionary *) dict
{
    if ([tag isEqualToString:@"CoS"])
    {
        [classesOfService addObject:[[FSClassOfService alloc] init]];
        [[self getCurrentClassOfService] setAttributesFrom:dict];
    }
}

-(void)endTag:(NSString*)tag withText:(NSString*)text
{
    if ([tag isEqualToString:@"Carrier"])
    {
        carrier = text;
    }
    else if ([tag isEqualToString:@"FltNum"])
    {
        fltNum = text;
    }
    else if ([tag isEqualToString:@"OperatingCarrier"])
    {
        operatingCarrier = text;
    }
    else if ([tag isEqualToString:@"DepAirp"])
    {
        operatingCarrier = text;
    }
    else if ([tag isEqualToString:@"DepDateTime"])
    {
        depDateTime = [DateTimeFormatter getNSDate:text Format:@"yyyy-MM-dd'T'HH:mm:ss"];
    }
    else if ([tag isEqualToString:@"ArrAirp"])
    {
        arrAirp = text;
    }
    else if ([tag isEqualToString:@"ArrDateTime"])
    {
        arrDateTime = [DateTimeFormatter getNSDate:text Format:@"yyyy-MM-dd'T'HH:mm:ss"];
    }
    else if ([tag isEqualToString:@"NumStops"])
    {
        numStops = [text intValue];
    }
    else if ([tag isEqualToString:@"AircraftCode"])
    {
        aircraftCode = text;
    }
    
}

@end
