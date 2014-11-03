//
//  FSSegment.m
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "FSSegment.h"
#import "FSSegmentOption.h"

@implementation FSSegment
@synthesize segmentOptions;

-(id)init
{
	self = [super init];
    if (self)
    {
        segmentOptions = [[NSMutableArray alloc] init];
    }
	return self;
}

-(void) appendOptions:(NSMutableArray*)ary
{
    if (segmentOptions == nil)
        return;
    
    [ary addObjectsFromArray:segmentOptions ];
       
}



-(FSSegmentOption*)getCurrentSegmentOption
{
    return (FSSegmentOption*)[segmentOptions lastObject];
}

#pragma mark -
#pragma mark Simplified parser functions

-(void) startTag:(NSString*)tag
{
    if ([tag isEqualToString:@"SegmentOption"])
    {
        [segmentOptions addObject:[[FSSegmentOption alloc] init]];
    }
}

-(void)endTag:(NSString*)tag withText:(NSString*)text
{
    // There aren't any tags we care about at this level
}


@end
