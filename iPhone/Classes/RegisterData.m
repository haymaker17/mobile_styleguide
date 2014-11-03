//
//  RegisterData.m
//  ConcurMobile
//
//  Created by yiwen on 4/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "RegisterData.h"
#import "FormatUtils.h"

#import "DataConstants.h"

@implementation RegisterData

@synthesize currentElement;
@synthesize path;
@synthesize pin;
@synthesize status;
@synthesize errMsg;

-(void) respondToXMLData:(NSData *)data
{
	[self parseXMLFileAtData:data];
}



- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.pin = parameterBag[@"PIN"];
	self.path = [NSString stringWithFormat:@"%@/mobile/MobileSession/Register",[ExSystem sharedInstance].entitySettings.uri];

	Msg* msg = [[Msg alloc] initWithData:REGISTER_DATA State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	
	[msg setHeader:parameterBag[@"SESSION_ID"]];
	
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	
	return msg;
}


-(NSString *)makeXMLBody
{//knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<Registration>"];
	[bodyXML appendString:@"<Pin>%@</Pin>"];
	[bodyXML appendString:@"</Registration>"];
	
	__autoreleasing NSString* result = [NSString stringWithFormat:bodyXML, [FormatUtils makeXMLSafe:pin]];
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
	if ([elementName isEqualToString:@"Status"]) {
		self.status = @"";
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if ([currentElement isEqualToString:@"Status"])
	{
		self.status = string;
	}
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.errMsg = string;
	}
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
}



@end
