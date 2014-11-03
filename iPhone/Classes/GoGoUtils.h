//
//  GoGoUtils.h
//  ConcurMobile
//
//  Created by Richard Puckett on 3/4/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "GoGoOffer.h"

@interface GoGoUtils : NSObject

+ (GoGoOffer *)offerFromParseObject:(PFObject *)pfObject;

@end
