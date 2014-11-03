//
//  SafetyCheckInData.m
//  ConcurMobile
//
//  Created by yiwen on 8/8/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "SafetyCheckInData.h"
#import "DataConstants.h"
#import "ExSystem.h"

@implementation SafetyCheckInData
@synthesize status;


-(NSString *)getMsgIdKey
{
	return SAFETY_CHECK_IN_DATA;
}




-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    // http://localhost/mobile/SafetyCheckIn.ashx?long=12.345&lat=-12.9876&city=redmond&state=wa&ctry=us&assist=Y&days=2&comment=quick
	self.path = [NSString stringWithFormat:@"%@/mobile/SafetyCheckIn.ashx?long=%@&lat=%@", 
                 [ExSystem sharedInstance].entitySettings.uri, parameterBag[@"LONG"], parameterBag[@"LAT"]];
	if (parameterBag[@"CITY"] != nil)
        self.path = [NSString stringWithFormat:@"%@&city=%@", path, parameterBag[@"CITY"]];
	if (parameterBag[@"STATE"] != nil)
        self.path = [NSString stringWithFormat:@"%@&state=%@", path, parameterBag[@"STATE"]];
	if (parameterBag[@"CTRY"] != nil)
        self.path = [NSString stringWithFormat:@"%@&ctry=%@", path, parameterBag[@"CTRY"]];
	if (parameterBag[@"ASSIST"] != nil)
        self.path = [NSString stringWithFormat:@"%@&assist=%@", path, parameterBag[@"ASSIST"]];
	if (parameterBag[@"DAYS"] != nil)
        self.path = [NSString stringWithFormat:@"%@&days=%@", path, parameterBag[@"DAYS"]];
	if (parameterBag[@"COMMENT"] != nil)
        self.path = [NSString stringWithFormat:@"%@&comment=%@", path, parameterBag[@"COMMENT"]];
                 
    self.path = [path stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

#pragma mark -
#pragma mark Parsing
- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.status = [[ActionStatus alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
	if ([currentElement isEqualToString:@"Status"])
	{
		self.status.status = string;
	}	
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.status.errMsg = buildString;
	}
}


@end
