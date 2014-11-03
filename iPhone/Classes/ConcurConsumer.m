//
//  ConcurConsumer.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/4/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "ConcurConsumer.h"
#import "ExSystem.h"

@implementation ConcurConsumer

-(NSString*) getToken
{
	NSString* token = [ExSystem sharedInstance].concurAccessToken;
	return (token == nil ? @"" : token);
}

-(NSString*) getTokenSecret
{
	NSString* secret = [ExSystem sharedInstance].concurAccessTokenSecret;
	return (secret == nil ? @"" : secret);
}

@end
