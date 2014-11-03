//
//  IpmRequest.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEError.h"

@interface IpmRequest : NSObject

-(id)initWithTarget:(NSString *)target;
-(void)requestIpmMessagesWithSuccess:(void (^) (NSArray *messages))success failure:(void (^)(CTEError *error))failure;

@end
