//
//  AsyncTaskManager.h
//  ConcurMobile
//
//  Created by Richard Puckett on 10/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AsyncTask.h"

typedef void (^CXAsyncCompletionBlock)(void);

@interface AsyncTaskManager : NSObject

+ (AsyncTaskManager *)sharedInstance;

- (void)addTask:(AsyncTask *)task;

- (void)addTask:(AsyncTask *)task
    withCompletionBlock:(CXAsyncCompletionBlock)completionBlock;

@property (strong, nonatomic) NSOperationQueue *operationQueue;

@end
