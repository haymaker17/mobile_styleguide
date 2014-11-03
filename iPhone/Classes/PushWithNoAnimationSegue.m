//
//  PushWithNoAnimationSegue.m
//  ConcurMobile
//
//  Created by Wes Barton on 4/11/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "PushWithNoAnimationSegue.h"

@implementation PushWithNoAnimationSegue
- (void)perform
{
    [[[self sourceViewController] navigationController] pushViewController:self.destinationViewController animated:NO];
}

@end
