//
//  WorkflowAction.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 6/11/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "WorkflowAction.h"

@implementation WorkflowAction
@synthesize statKey, actionText;


- (void)encodeWithCoder:(NSCoder *)coder {
	[coder encodeObject:statKey	forKey:@"statKey"];
	[coder encodeObject:actionText	forKey:@"actionText"];
}



- (id)initWithCoder:(NSCoder *)coder {
	self.statKey = [coder decodeObjectForKey:@"statKey"];
	self.actionText = [coder decodeObjectForKey:@"actionText"];
    return self;
}
@end
