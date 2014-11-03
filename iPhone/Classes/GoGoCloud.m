//
//  GoGoCloud.m
//  ConcurMobile
//
//  Created by Richard Puckett on 12/30/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GoGoCloud.h"

@implementation GoGoCloud

+ (void)getOfferForUser:(NSString *)userId
        withDeviceToken:(NSString *)deviceToken
           successBlock:(IPMSingleSuccessBlock)successBlock
           failureBlock:(IPMFailureBlock)failureBlock {
    
    [Parse setApplicationId:@"nNBmg91Dgq5ByD6xxStwSjw0Sra1mphCcUXXS0tL"
                  clientKey:@"dMlRnik84r4SWt7LCvbI6v55JEiCmZPxS81zagbD"];

    PFQuery *query = [PFQuery queryWithClassName:@"GoGoMessage"];
    
    [query whereKey:@"userId" equalTo:userId];
    [query whereKey:@"deviceToken" equalTo:deviceToken];
    [query whereKey:@"status" equalTo:@"NEW"];
    [query orderByDescending:@"createdAt"];
    
    query.cachePolicy = kPFCachePolicyNetworkElseCache;
    
    [query getFirstObjectInBackgroundWithBlock:^(PFObject *pfObject, NSError *error) {
        if (!error) {
            GoGoOffer *offer = [[GoGoOffer alloc] init];
            
            offer.objectId = pfObject.objectId;
            offer.itineraryId = [pfObject objectForKey:@"itineraryId"];
            offer.messageId = [pfObject objectForKey:@"messageId"];
            offer.segmentKey = [pfObject objectForKey:@"segmentKey"];
            offer.status = [pfObject objectForKey:@"status"];
            
            if ([pfObject objectForKey:@"isSilent"]) {
                offer.isSilent = YES;
            } else {
                offer.isSilent = NO;
            }
            
            NSLog(@"GoGoOffer %@ silent", offer.isSilent ? @"is" : @"is not");

            pfObject[@"status"] = @"RECEIVED";
            
            [pfObject save];
            
            successBlock(offer);
        } else {
            failureBlock(error);
        }
    }];
}

@end
