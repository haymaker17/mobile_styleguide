//
//  OutOfPocketDeleteData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "OutOfPocketDeleteData.h"
#import "DateTimeFormatter.h"
#import "ExSystem.h"

@implementation OutOfPocketDeleteData

@synthesize path, currentElement, returnStatus, keysToKill, returnFailure, returnFailures;

-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self flushData];
	[self parseXMLFileAtData:data];
}

-(NSString *)makeDeleteBody
{
	__autoreleasing NSMutableString *body = [[NSMutableString alloc] init];
	[body appendString:@"<a:ArrayOfstring xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">"];
	for (NSString *key in keysToKill)
	{
		[body appendString:@"<a:string>"];
		[body appendString:key];
		[body appendString:@"</a:string>"];
	}
	[body appendString:@"</a:ArrayOfstring>"];
	
	//NSLog(@"body = %@", body);
	return body;
}




-(id)init
{
    self = [super init];
	if (self)
    {
        isInElement = @"NO";
        currentElement = @"";
        [self flushData];	
    }
	return self;
}

-(NSString *)getMsgIdKey
{
	return DELETE_OOP_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	if (parameterBag != nil && [PCT_TYPE isEqualToString:(NSString*)parameterBag[@"TYPE"]])
	{
		self.path = [NSString stringWithFormat:@"%@/mobile/Expense/HidePersonalCardTransactions",[ExSystem sharedInstance].entitySettings.uri];
	}
	else if (parameterBag != nil && [CCT_TYPE isEqualToString:(NSString*)parameterBag[@"TYPE"]])
	{
		self.path = [NSString stringWithFormat:@"%@/mobile/Expense/HideCorporateCardTransactions",[ExSystem sharedInstance].entitySettings.uri];
	}
	else 
	{
		self.path = [NSString stringWithFormat:@"%@/mobile/Expense/DeleteMobileEntries",[ExSystem sharedInstance].entitySettings.uri];
	}
	

	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	
	if (parameterBag != nil & parameterBag[@"KILL_KEYS"] != nil) 
	{
		self.keysToKill = parameterBag[@"KILL_KEYS"];
		[msg setBody:[self makeDeleteBody]];
	}
	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	
	return msg;
}


-(void) flushData
{
	//	if (keys != nil) 
	//	{
	//		[keys release];
	//	}
	//	
	//	if (oopes != nil) 
	//	{
	//		[oopes release];
	//	}
	
}


- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	////NSLog(@"found file and started parsing");
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	//	NSString * errorString = [NSString stringWithFormat:@"Parser Error (Error code %i )", [parseError code]];
	//	////NSLog(@"error parsing XML: %@", errorString);
	//	
	//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error Parsing Content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	//	[errorAlert show];
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	
	isInElement = @"YES";
	
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		returnFailure = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	}
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		returnFailures[returnFailure[@"ME_KEY"]] = returnFailure;
		returnFailure = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];		
	}
}



- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		returnFailure[@"ERROR_MESSAGE"] = string;
	}
	else if ([currentElement isEqualToString:@"Status"])
	{
		returnFailure[@"STATUS"] = string;
	}
	else if ([currentElement isEqualToString:@"MeKey"])
	{
		returnFailure[@"ME_KEY"] = string;
	}

}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}



@end
