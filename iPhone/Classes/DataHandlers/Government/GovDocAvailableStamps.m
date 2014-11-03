//
//  GovDocAvailableStamps.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/20/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovDocAvailableStamps.h"

@implementation GovDocAvailableStamps
@synthesize travelerId, docName, docType, userId, stampInfoList, returnToInfoList, sigRequired;

-(id) init
{
    if (self = [super init])
    {
        self.stampInfoList = [[NSMutableDictionary alloc] init];
        self.returnToInfoList = [[NSMutableArray alloc] init];
    }
    return self;
}
@end
