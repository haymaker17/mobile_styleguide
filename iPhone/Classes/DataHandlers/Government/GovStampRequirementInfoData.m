//
//  GovStampRequirementInfoData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovStampRequirementInfoData.h"
#import "FormatUtils.h"

@implementation GovStampRequirementInfoData
@synthesize reqInfo, stampName;


-(NSString *)getMsgIdKey
{
	return GOV_STAMP_REQ_INFO;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.stampName = [parameterBag objectForKey:@"STAMP_NAME"];
    NSString *stampNameHtmlEncoded = [self.stampName stringByAddingPercentEscapesUsingEncoding:NSASCIIStringEncoding];
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/GetTMStampRequirementInfo/%@",[ExSystem sharedInstance].entitySettings.uri, stampNameHtmlEncoded];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"getReasonCodeReqdResponseRow"])
	{
		self.reqInfo = [[GovStampRequirementInfo alloc] init];
	}
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if ([currentElement isEqualToString:@"StampName"])
    {
		[self.reqInfo setStampName:buildString];
    }
    else if ([currentElement isEqualToString:@"ReasonReqd"])
    {
		[self.reqInfo setReasonRequired:@([buildString boolValue])];
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    if ([elementName isEqualToString:@"getReasonCodeReqdResponseRow"])
	{
        [GovStampRequirementInfo registerStampReasonInfo:self.reqInfo];
    }
}

@end
