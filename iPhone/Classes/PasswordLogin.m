//
//  PasswordLogin.m
//  ConcurMobile
//
//  Created by yiwen on 4/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "PasswordLogin.h"
#import "FormatUtils.h"


@implementation PasswordLogin
@synthesize path;
@synthesize userName;
@synthesize password;
@synthesize regSessionID;
@synthesize authenticated;
@synthesize entityType;
@synthesize timedOut;
@synthesize currentElement;


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self parseXMLFileAtData:data];
}


- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.userName = parameterBag[@"USER_ID"];
	self.password = parameterBag[@"PASSWORD"];
	self.path = [NSString stringWithFormat:@"%@/mobile/MobileSession/PasswordLogin",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:PWD_LOGIN_DATA State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	return msg;
}


-(NSString *)makeXMLBody
{//knows how to make a post
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<Credentials>"];
	[bodyXML appendString:@"<LoginID>%@</LoginID>"];
	[bodyXML appendString:@"<Password>%@</Password>"];
	[bodyXML appendString:@"</Credentials>"];
	NSString* result = [NSString stringWithFormat:bodyXML, [FormatUtils makeXMLSafe:userName], [FormatUtils makeXMLSafe:password]];
	return result;
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	////NSLog(@"found file and started parsing");
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
//	NSString * errorString = [NSString stringWithFormat:@"Unable to authenticate from web site (Error code %i )", [parseError code]];
	//NSLog(@"error parsing XML: %@", errorString);
	
	//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error loading content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	//	[errorAlert show];
	//	[errorAlert release];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	self.currentElement = elementName;
	if ([elementName isEqualToString:@"Session"]) {
		self.regSessionID = @"";
		self.authenticated = @"YES";
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	//	if ([elementName isEqualToString:@"channel"]) {
	//		[item setObject:condition forKey:@"condition"];
	//		[item setObject:conditionCode forKey:@"conditionCode"];
	//		[item setObject:temp forKey:@"temp"];
	//	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if ([currentElement isEqualToString:@"ID"])
	{
		self.regSessionID = string;
		self.authenticated = @"YES";
	}
	else if ([currentElement isEqualToString:@"EntityType"])
	{
		self.entityType = string;
	}
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}



@end

