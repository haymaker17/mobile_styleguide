//
//  CarImageDH.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/25/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "CarImageDH.h"
#import "DataConstants.h"

@implementation CarImageDH
@synthesize path, currentElement, dict, isInElement;

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
        self.isInElement = @"NO";
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return CAR_IMAGE;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    
	self.path = parameterBag[@"XURL"]; //[NSString stringWithFormat:@"%@/mobile/Home/GetCountSummary",[ExSystem sharedInstance].entitySettings.uri];		
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"image/jpg"];
	[msg setMethod:@"GET"];
	
	return msg;
}



-(void) flushData
{
	
	self.dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
}


- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	//NSLog(@"found file and started parsing");
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	//	NSString * errorString = [NSString stringWithFormat:@"Parser Error (Error code %i )", [parseError code]];
	//	//NSLog(@"error parsing XML: %@", errorString);
	//	
	//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error Parsing Content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	//	[errorAlert show];
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	
	self.isInElement = @"YES";
	
	if ([elementName isEqualToString:@"PersonalCard"])
	{
        
	}
	else if ([elementName isEqualToString:@"PersonalCardTransaction"])
	{
	}
	
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	self.isInElement = @"NO";
	
	if ([elementName isEqualToString:@"PersonalCard"])
	{
        
	}
	else if ([elementName isEqualToString:@"PersonalCardTransaction"])
	{
        
	}
	
	
}



- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if ([currentElement isEqualToString:@"MobileEntryCount"])
	{
		dict[@"MobileEntryCount"] = string;
	}
	if ([currentElement isEqualToString:@"CorporateCardTransactionCount"])
	{
		dict[@"CorporateCardTransactionCount"] = string;
	}
	else if ([currentElement isEqualToString:@"ReportsToApproveCount"])
	{
		dict[@"ReportsToApproveCount"] = string;
	}
	else if ([currentElement isEqualToString:@"UnsubmittedReportsCount"])
	{
		dict[@"UnsubmittedReportsCount"] = string;
	}
	else if ([currentElement isEqualToString:@"UnsubmittedReportsCrnCode"])
	{
		dict[@"UnsubmittedReportsCrnCode"] = string;
	}
	
	else if ([currentElement isEqualToString:@"UnsubmittedReportsTotal"])
	{
		dict[@"UnsubmittedReportsTotal"] = string;
	}
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}



@end
