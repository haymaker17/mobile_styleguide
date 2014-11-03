//
//  GoGoMessageRequestFactory.h
//  ConcurMobile
//
//  Created by Richard Puckett on 1/5/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "GoGoCloud.h"
#import <Parse/Parse.h>

@interface GoGoMessageRequestFactory : NSObject

+ (void)messageRequestForMessageId:(NSString *)messageId
                      successBlock:(IPMSingleSuccessBlock)successBlock
                      failureBlock:(IPMFailureBlock)failureBlock;

@end
