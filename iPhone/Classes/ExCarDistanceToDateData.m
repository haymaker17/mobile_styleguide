//
//  ExCarDistanceToDateData.m
//  ConcurMobile
//
//  Created by yiwen on 3/17/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ExCarDistanceToDateData.h"
#import "DataConstants.h"
//-
@implementation ExCarDistanceToDateData
@synthesize carKey, tranDate, excludeRpeKey, distanceToDate;

-(NSString *)getMsgIdKey
{
	return CAR_DISTANCE_TO_DATE_DATA;
}

-(void) flushData
{
	[super flushData];
	self.distanceToDate = nil;
}



-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
	self.excludeRpeKey = parameterBag[@"RPE_KEY"];
	self.carKey = parameterBag[@"CAR_KEY"];
	self.tranDate = parameterBag[@"TRAN_DATE"];
	
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/DistanceToDate/%@/%@",[ExSystem sharedInstance].entitySettings.uri, self.carKey, self.tranDate];
	if (self.excludeRpeKey != nil)
		self.path = [NSString stringWithFormat:@"%@/%@", self.path, self.excludeRpeKey];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
	if ([currentElement isEqualToString:@"string"])
	{
		self.distanceToDate = buildString;
	}
	
}

@end
