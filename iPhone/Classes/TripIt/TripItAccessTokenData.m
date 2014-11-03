//
//  TripItAccessTokenData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 3/30/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TripItAccessTokenData.h"

@implementation TripItAccessTokenData

#pragma mark - Lifecycle Methods


#pragma mark - Message Creation

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = [NSString stringWithFormat:@"%@/mobile/tripit/ObtainTripItAccessToken",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:OBTAIN_TRIPIT_ACCESS_TOKEN State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setMethod:@"POST"];
	return msg;
}

@end
