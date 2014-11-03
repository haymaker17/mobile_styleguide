//
//  ConditionalFieldsData.m
//  ConcurMobile
//
//  Created by Concur on 3/15/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ConditionalFieldsActionList.h"

@implementation ConditionalFieldsActionList

- (void)setObject:(id)obj atIndexedSubscript:(NSUInteger)idx
{
    @throw [NSException exceptionWithName:@"NotImplementedException" reason:@"Function not implemented" userInfo:nil];
}

- (id) objectAtIndexedSubscript: (NSUInteger) index;
{
    for (ConditionalFieldAction *dynAction in self.fieldActions)
    {
        if (dynAction.field == index)
        {
            return dynAction;
        }
    }
    return nil;
}

- (void) add: (ConditionalFieldAction *) value
{
    [self.fieldActions addObject:value];
}

#pragma mark Instance  Methods

-(id)init
{
    self = [super init];
    if (self)
    {
        self.fieldActions = [[NSMutableArray alloc] init];
    }
    return self;
}

-(void) removeAll
{
    [self.fieldActions removeAllObjects];
}

@end

@implementation ConditionalFieldAction
@end
