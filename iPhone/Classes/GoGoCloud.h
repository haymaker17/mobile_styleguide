//
//  GoGoCloud.h
//  ConcurMobile
//
//  Created by Richard Puckett on 12/30/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GoGoOffer.h"

typedef void (^IPMSingleSuccessBlock)(GoGoOffer *offer);
typedef void (^IPMSuccessBlock)(NSArray *objects);
typedef void (^IPMFailureBlock)(NSError *error);

@interface GoGoCloud : NSObject

+ (void)getOfferForUser:(NSString *)userId
        withDeviceToken:(NSString *)deviceToken
           successBlock:(IPMSingleSuccessBlock)successBlock
           failureBlock:(IPMFailureBlock)failureBlock;

@end
