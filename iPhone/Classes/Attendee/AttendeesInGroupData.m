//
//  AttendeesInGroupData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 4/2/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "AttendeesInGroupData.h"

@implementation AttendeesInGroupData
@synthesize groupKey;

-(NSString *)getMsgIdKey
{
	return ATTENDEES_IN_GROUP_DATA;
}


#pragma mark -
#pragma mark Message request
- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.groupKey = parameterBag[@"GROUP_KEY"];
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetAttendeesInGroup/%@",[ExSystem sharedInstance].entitySettings.uri, self.groupKey];	
    
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"GET"];
	return msg;
}


@end
