//
//  GoGoUtils.m
//  ConcurMobile
//
//  Created by Richard Puckett on 3/4/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "GoGoUtils.h"

@implementation GoGoUtils

+ (GoGoOffer *)offerFromParseObject:(PFObject *)pfObject {
    GoGoOffer *offer = [[GoGoOffer alloc] init];
    
    offer.objectId = pfObject.objectId;
    offer.itineraryId = [pfObject objectForKey:@"itineraryId"];
    offer.messageId = [pfObject objectForKey:@"messageId"];
    offer.segmentKey = [pfObject objectForKey:@"segmentKey"];
    offer.status = [pfObject objectForKey:@"status"];
    
    offer.isSilent = [[pfObject objectForKey:@"isSilent"] boolValue];
    
    return offer;
}

@end
