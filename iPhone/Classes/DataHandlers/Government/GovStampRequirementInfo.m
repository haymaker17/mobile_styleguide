//
//  GovStampRequirementInfo.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/20/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovStampRequirementInfo.h"

static NSMutableDictionary* stampReasonDict;

@implementation GovStampRequirementInfo
@synthesize stampName, reasonRequired;

+ (void)initialize
{
	if (self == [GovStampRequirementInfo class])
	{
        // Perform initialization here.
		stampReasonDict = [[NSMutableDictionary alloc] init];
    }
}

+ (NSNumber* ) requiresReason:(NSString*)stampName
{
    GovStampRequirementInfo * info = (GovStampRequirementInfo*) [stampReasonDict objectForKey:stampName];
    
    if (info != nil)
        return info.reasonRequired;
    
    return nil;
}

+ (void) registerStampReasonInfo:(GovStampRequirementInfo*) info
{
    [stampReasonDict setObject:info forKey:info.stampName];
}

@end
