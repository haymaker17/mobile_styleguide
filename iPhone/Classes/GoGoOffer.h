//
//  GoGoOffer.h
//  ConcurMobile
//
//  Created by Richard Puckett on 1/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

@interface GoGoOffer : NSObject

@property (copy, nonatomic) NSString *objectId;
@property (copy, nonatomic) NSString *itineraryId;
@property (copy, nonatomic) NSString *messageId;
@property (copy, nonatomic) NSString *segmentKey;
@property (copy, nonatomic) NSString *status;
@property (assign) BOOL isSilent;

@end
