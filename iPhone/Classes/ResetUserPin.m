//
//  ResetUserPin.m
//  ConcurMobile
//
//  Created by AJ Cram on 6/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ResetUserPin.h"
#import "DataConstants.h"

NSString* const RESET_USER_PIN_CLIENT_KEY = @"ClientKey";
NSString* const RESET_USER_PIN_LOGIN_ID = @"LoginID";
NSString* const RESET_USER_PIN_SERVER_KEY = @"ServerKey";

@interface ResetUserPin()

@property NSString* clientKey;
@property NSString* loginID;
@property NSString* serverKey;

@end

@implementation ResetUserPin

@synthesize actionStatus, status, userID, PinMinLength, requiredMixedCase, requiresNonAlphanum, errMsg;

-(NSString *)getMsgIdKey
{
	return RESET_PIN_RESET_USER_PIN;
}

-(NSString *)makeXMLBody :(NSMutableDictionary *) pBag
{
    NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<ResetUserPin>"];
    NSString *email = pBag[@"RESET_PIN_EMAIL"];
    NSString *keyPartA = pBag[@"RESET_PIN_CLIENT_KEY"];
    NSString *keyPartB = pBag[@"RESET_PIN_SERVER_KEY"];
    NSString *secrete = pBag[@"MOBILE_PIN"];

    [bodyXML appendString:[NSString stringWithFormat:@"<Email>%@</Email>", [NSString stringByEncodingXmlEntities:email]]];
    [bodyXML appendString:[NSString stringWithFormat:@"<KeyPartA>%@</KeyPartA>", keyPartA]];
    [bodyXML appendString:[NSString stringWithFormat:@"<KeyPartB>%@</KeyPartB>", keyPartB]];
    [bodyXML appendString:[NSString stringWithFormat:@"<Pin>%@</Pin>", [NSString stringByEncodingXmlEntities:secrete]]];
    
    [bodyXML appendString:@"</ResetUserPin>"];
    
	return bodyXML;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    self.path = [NSString stringWithFormat:@"%@/Mobile/MobileSession/ResetUserPin",
                 [ExSystem sharedInstance].entitySettings.uri];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];

	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	
	return msg;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.actionStatus = nil;
		self.actionStatus = [[ActionStatus alloc] init];
	}
    else if ([elementName isEqualToString:@"Status"]) 
		self.status = @"";
	
    else if ([elementName isEqualToString:@"MinLength"])
        self.PinMinLength = @"";
    else if ([elementName isEqualToString:@"RequiresMixedCase"])
        self.requiredMixedCase = @"";
    else if ([elementName isEqualToString:@"RequiresNonAlphanum"])
        self.requiresNonAlphanum = @"";
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	if ([currentElement isEqualToString:@"Status"])
	{
		self.status = buildString;
	}
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.errMsg = buildString;
	}
    else if ([currentElement isEqualToString:@"LoginId"])
        self.userID = buildString;
    else if ([currentElement isEqualToString:@"MinLength"])
        self.PinMinLength = buildString;
    else if ([currentElement isEqualToString:@"RequiresMixedCase"])
        self.requiredMixedCase = buildString;
    else if ([currentElement isEqualToString:@"RequiresNonAlphanum"])
        self.requiresNonAlphanum = buildString;
}

@end
