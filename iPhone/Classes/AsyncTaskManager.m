//
//  AsyncTaskManager.m
//  ConcurMobile
//
//  Created by Richard Puckett on 10/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AsyncTaskManager.h"

@implementation AsyncTaskManager

__strong static id _sharedInstance = nil;

+ (AsyncTaskManager *)sharedInstance {
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        _sharedInstance = [[self alloc] init];
    });
    
    return _sharedInstance;
}

- (id)init {
    self = [super init];
    
    if (self) {
        self.operationQueue = [[NSOperationQueue alloc] init];
    }
    
    return self;
}

- (void)addTask:(AsyncTask *)task {
    [self.operationQueue addOperation:task.operation];
}

- (void)addTask:(AsyncTask *)task
        withCompletionBlock:(CXAsyncCompletionBlock)completionBlock {
    
    [task.operation setCompletionBlock:completionBlock];
    
    [self.operationQueue addOperation:task.operation];
}

@end
