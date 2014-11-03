//
//  Policy.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 4/25/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "Policy.h"

@implementation Policy
@synthesize polKey, submitConfirmationKey, approvalConfirmationKey;

static NSMutableDictionary * policyXmlToPropertyMap;

+ (NSMutableDictionary*) getXmlToPropertyMap
{
	return policyXmlToPropertyMap;
}

+ (void)initialize
{
	if (self == [Policy class]) 
	{
        // Perform initialization here.
		policyXmlToPropertyMap = [[NSMutableDictionary alloc] init];
		policyXmlToPropertyMap[@"PolKey"] = @"PolKey";
		policyXmlToPropertyMap[@"SubmitConfirmationKey"] = @"SubmitConfirmationKey";
		policyXmlToPropertyMap[@"ApprovalConfirmationKey"] = @"ApprovalConfirmationKey";
    }
}


@end
