//
//  AsyncTask.h
//  ConcurMobile
//
//  Created by Richard Puckett on 10/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef void (^AsyncTaskBlock)(void);

// Using proxy so we don't inadvertantly expose all of NSOperation
// to user. User can still get at it, but that's highly discouraged.
//
@interface CXOperationProxy : NSOperation

@property (strong, nonatomic) AsyncTaskBlock block;

@end

@interface AsyncTask : NSObject

@property (strong, nonatomic) CXOperationProxy *operation;

- (id)initWithBlock:(AsyncTaskBlock)block;
- (void)setExecutionBlock:(AsyncTaskBlock)block;

@end
