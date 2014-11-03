//
//  CXStack.m
//  ConcurMobile
//
//  Created by Richard Puckett on 11/6/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXStack.h"

@implementation CXStack

- (id)init {
    if(self = [super init]) {
        self.ary = [[NSMutableArray alloc] init];
    }
    
    return self;
}

- (void)push:(id)obj {
    [self.ary addObject:obj];
}

- (id)pop {
    id obj = [self.ary lastObject];
    
    if(obj) {
        [self.ary removeLastObject];
    }
    
    return obj;
}

- (void)clear {
    [self.ary removeAllObjects];
}

@end
