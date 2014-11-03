//
//  AsyncTask.m
//  ConcurMobile
//
//  Created by Richard Puckett on 10/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AsyncTask.h"

@implementation CXOperationProxy

- (id)initWithBlock:(AsyncTaskBlock)block {
    self = [super init];
    
    if (self) {
        self.block = block;
    }
    
    return self;
}

- (void)main {
    self.block();
}

- (void)setExecutionBlock:(AsyncTaskBlock)block {
    self.block = block;
}

@end

@implementation AsyncTask

- (id)init {
    return [self initWithBlock:nil];
}

- (id)initWithBlock:(AsyncTaskBlock)block {
    self = [super init];
    
    if (self) {
        self.operation = [[CXOperationProxy alloc] initWithBlock:block];
    }
    
    return self;
}

- (void)setExecutionBlock:(AsyncTaskBlock)block {
    [self.operation setExecutionBlock:block];
}

@end


