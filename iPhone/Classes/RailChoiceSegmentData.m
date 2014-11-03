//
//  RailChoiceSegmentData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "RailChoiceSegmentData.h"


@implementation RailChoiceSegmentData
@synthesize isReturn;
@synthesize totalTime;
@synthesize	trains, train;

-(id)init
{
    self = [super init];
    if (self)
    {
        trains = [[NSMutableArray alloc] initWithObjects:nil];
    }
	return self;
}


@end
