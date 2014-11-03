//
//  CXStack.h
//  ConcurMobile
//
//  Created by Richard Puckett on 11/6/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

@interface CXStack : NSObject

@property (strong, nonatomic) NSMutableArray *ary;

- (void)clear;
- (id)pop;
- (void)push:(id)obj;

@end
