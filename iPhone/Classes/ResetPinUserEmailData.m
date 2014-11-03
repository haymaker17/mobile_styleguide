//
//  ResetPinUserEmailData.m
//  ConcurMobile
//
//  Created by Sally Yan on 7/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
// 

#import "ResetPinUserEmailData.h"

@implementation ResetPinUserEmailData

@synthesize actionStatus,status, serverGUID, clientGUID, errMsg;


-(NSString *)getMsgIdKey
{
	return RESET_PIN_USER_EMAIL_DATA;
}


-(NSString *)makeXMLBody :(NSMutableDictionary *) pBag
{	
//    NSMutableString *bodyXML = [[NSMutableString alloc]
//                                                initWithString:@"<RequestPinReset xmlns='http://schemas.datacontract.org/2004/07/Snowbird' xmlns:i='http://www.w3.org/2001/XMLSchema-instance'>"];
    
    NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<RequestPinReset>"];
    NSString *email = pBag[@"RESET_PIN_EMAIL"];    
	[bodyXML appendString:@"<Email>%@</Email>"];
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSArray *languages = [defaults objectForKey:@"AppleLanguages"];
    NSString *preferredLang = languages[0];
    if (preferredLang != nil)
    {
        [bodyXML appendString:@"<Locale>"];
        [bodyXML appendString:preferredLang];
        [bodyXML appendString:@"</Locale>"];
    }
    [bodyXML appendString:@"</RequestPinReset>"];
    
    NSString *returnVal = [NSString stringWithFormat:bodyXML, [NSString stringByEncodingXmlEntities:email]];
    return returnVal;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    self.path = [NSString stringWithFormat:@"%@/Mobile/MobileSession/RequestPinReset",
                 [ExSystem sharedInstance].entitySettings.uri];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
//	msg.numOauthLegs = 2;
    [msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	
	return msg;
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
    
    if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.actionStatus = nil;
		self.actionStatus = [[ActionStatus alloc] init];
	}
	
	else if ([elementName isEqualToString:@"Status"])
	{
		self.status = nil;
	}
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
//		self.actionStatus.errMsg = buildString;
        self.errMsg = buildString;
	}
    else if ([currentElement isEqualToString:@"KeyPart"])
    {
        self.clientGUID = buildString;
    }
}

@end

