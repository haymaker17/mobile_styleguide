//
//  TripItValidateAccessTokenData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TripItValidateAccessTokenData.h"

@implementation TripItValidateAccessTokenData

@synthesize actionStatus, isTripItLinked, isTripItEmailAddressConfirmed;

-(NSString *)getMsgIdKey
{
	return VALIDATE_TRIPIT_ACCESS_TOKEN;
}

#pragma mark - Lifecycle Methods


#pragma mark - Message Creation

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = [NSString stringWithFormat:@"%@/mobile/tripit/ValidateTripItAccessToken",[ExSystem sharedInstance].entitySettings.uri];
    
	Msg* msg = [[Msg alloc] initWithData:VALIDATE_TRIPIT_ACCESS_TOKEN State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setMethod:@"POST"];
    
    // Body is empty, but message is POST because server state can change.  If token is found to be invalid, it will be deleted from the server and the account will become unlinked on the server.
    
    return msg;
}

#pragma mark - Parsing Methods

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"Status"])
	{
		self.actionStatus = [[ActionStatus alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
	if ([currentElement isEqualToString:@"Status"])
	{
		self.actionStatus.status = buildString;
	}	
	else if ([currentElement isEqualToString:@"IsLinked"])
	{
        self.isTripItLinked = [string isEqualToString:@"true"];
	}	
	else if ([currentElement isEqualToString:@"IsEmailAddressConfirmed"])
	{
        self.isTripItEmailAddressConfirmed = [string isEqualToString:@"true"];
	}	
}

@end
