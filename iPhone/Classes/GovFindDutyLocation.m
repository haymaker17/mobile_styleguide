//
//  GovFindLocation.m
//  ConcurMobile
//
//  Created by Shifan Wu on 12/6/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovFindDutyLocation.h"

@implementation GovFindDutyLocation

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.address = parameterBag[@"ADDRESS"];
	self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/LocationSearch",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:GOV_FIND_DUTY_LOCATION State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	return msg;
}

@end

