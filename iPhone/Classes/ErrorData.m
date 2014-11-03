//
//  ErrorData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ErrorData.h"


@implementation ErrorData

@synthesize path, rootVC, currentElement, errors , error, keys, errorCount;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	self.errors = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	self.keys = [[NSMutableArray alloc] initWithObjects:nil];
    [super parseXMLFileAtData:webData];
}


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	
	[self flushData];
	[self parseXMLFileAtData:data];
}

-(id)init
{
	self = [super init];
    if (self)
    {
        isInElement = @"NO";
        currentElement = @"";
        errorCount = 0;
        [self flushData];
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return ERROR_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
//	self.path = [NSString stringWithFormat:@"%@/Mobile/Expense/GetExpenseTypes", settings.uri];
//	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
//	[msg setHeader:[ExSystem sharedInstance].sessionID];
//	[msg setContentType:@"application/xml"];
//	[msg setMethod:@"GET"];
	
	return nil;
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
	
	if ([elementName isEqualToString:@"Error"])
	{
		errorCount++;
		
		
		error = [[NSMutableDictionary alloc] init];
	}
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"Error"])
	{
		NSString *errKey = [NSString stringWithFormat:@"error%d", errorCount ];
		errors[errKey] = error;
		[keys addObject:errKey];
	}
}



- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	NSLog(@"%@ Error Value: %@", currentElement, string);
	if ([currentElement isEqualToString:@"Message"])
	{
		error[currentElement] = string;
	}
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}




@end
