//
//  GoGoMessageRequestFactory.m
//  ConcurMobile
//
//  Created by Richard Puckett on 1/5/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "GoGoMessageRequestFactory.h"
#import "GoGoUtils.h"

@implementation GoGoMessageRequestFactory

+ (void)messageRequestForMessageId:(NSString *)messageId
                      successBlock:(IPMSingleSuccessBlock)successBlock
                      failureBlock:(IPMFailureBlock)failureBlock {
    
    [Parse setApplicationId:@"nNBmg91Dgq5ByD6xxStwSjw0Sra1mphCcUXXS0tL"
                  clientKey:@"dMlRnik84r4SWt7LCvbI6v55JEiCmZPxS81zagbD"];
    
    PFQuery *query = [PFQuery queryWithClassName:@"GoGoMessage"];
    
    [query whereKey:@"messageId" equalTo:messageId];
    
    [query findObjectsInBackgroundWithBlock:^(NSArray *objects, NSError *error) {
        if (!error) {
            if ([objects count] == 1) {
                PFObject *pfObject = objects[0];
                
                GoGoOffer *offer = [GoGoUtils offerFromParseObject:pfObject];
                
                successBlock(offer);
            }
        } else {
            failureBlock(error);
        }
    }];
}

@end
