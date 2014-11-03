//
//  WorkflowAction.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 6/11/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface WorkflowAction : NSObject
{
    NSString        *statKey;
    NSString        *actionText;
}

@property(nonatomic, strong) NSString   *statKey;
@property(nonatomic, strong) NSString   *actionText;

@end
