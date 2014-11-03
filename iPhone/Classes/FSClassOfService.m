//
//  FSClassOfService.m
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "FSClassOfService.h"

@implementation FSClassOfService

@synthesize seats;
@synthesize cabin;

-(void) setAttributesFrom: (NSDictionary*)dict
{
    NSString *seatsAttr = (NSString*)dict[@"seats"];
    seats = [seatsAttr intValue];
    
    cabin = (NSString*)dict[@"cabin"];
    
}

@end
