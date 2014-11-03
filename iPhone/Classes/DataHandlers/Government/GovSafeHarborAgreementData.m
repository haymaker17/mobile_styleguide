//
//  GovSafeHarborAgreementData.m
//  ConcurMobile
//
//  Created by Shifan Wu on 1/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovSafeHarborAgreementData.h"

@implementation GovSafeHarborAgreementData
@synthesize agreeValue;

-(NSString *)getMsgIdKey
{
    return GOV_AGREE_TO_SAFEHARBOR;
}

-(void)flushData
{
    self.agreeValue = @"false";
    [super flushData];
}

-(Msg *)newMsg:(NSMutableDictionary *)parameterBag
{
    self.agreeValue = [parameterBag objectForKey:@"AGREE_VALUE"];
    self.path = [NSString stringWithFormat:@"%@/mobile/Home/AgreeToSafeHarbor/%@", [ExSystem sharedInstance].entitySettings.uri, agreeValue];
    
    Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
    
    return msg;
}

@end
