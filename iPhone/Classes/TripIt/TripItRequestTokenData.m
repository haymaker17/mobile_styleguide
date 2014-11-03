//
//  TripItRequestTokenData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 3/30/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TripItRequestTokenData.h"

@implementation TripItRequestTokenData

@synthesize requestTokenKey, requestTokenSecret;

-(NSString *)getMsgIdKey
{
	return OBTAIN_TRIPIT_REQUEST_TOKEN;
}

#pragma mark - Lifecycle Methods


#pragma mark - Message Creation

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = [NSString stringWithFormat:@"%@/mobile/mobilesession/GetTripItRequestToken",[ExSystem sharedInstance].entitySettings.uri];
	NSLog(@"Path: %@", self.path);
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setMethod:@"GET"];
    msg.numOauthLegs = 2;
	return msg;
}

#pragma mark - Parsing Methods
- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
    [super parserDidStartDocument:parser];
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
 	if ([currentElement isEqualToString:@"Key"])
        self.requestTokenKey = buildString;
 	else if ([currentElement isEqualToString:@"Secret"])
        self.requestTokenSecret = buildString;
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
    [super parserDidEndDocument:parser];
}

@end
