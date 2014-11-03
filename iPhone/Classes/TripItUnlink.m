//
//  TripItUnlink.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 4/23/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TripItUnlink.h"

@implementation TripItUnlink

@synthesize actionStatus;

#pragma mark - Lifecycle


#pragma mark - Message Creation

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = [NSString stringWithFormat:@"%@/mobile/tripit/UnlinkFromTripIt",[ExSystem sharedInstance].entitySettings.uri];
	Msg* msg = [[Msg alloc] initWithData:UNLINK_FROM_TRIPIT State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setMethod:@"POST"];
	return msg;
}

-(BOOL) isActionStatusSuccess
{
    return (actionStatus != nil && actionStatus.status != nil && [actionStatus.status isEqualToString:@"SUCCESS"]);
}

#pragma mark - Parser

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.actionStatus = nil;
		actionStatus = [[ActionStatus alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
	if ([currentElement isEqualToString:@"Status"])
	{
		self.actionStatus.status = buildString;
	}	
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.actionStatus.errMsg = buildString;
	}
}	


@end
