//
//  WOEID.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/30/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "WOEID.h"
#import "MobileAlertView.h"

@implementation WOEID

@synthesize woeid;

- (void)parseXMLFileAtURL:(NSString *)URL 
{	
	//you must then convert the path to a proper NSURL or it won't work
	NSURL *xmlURL = [NSURL URLWithString:URL];
	
	// here, for some reason you have to use NSClassFromString when trying to alloc NSXMLParser, otherwise you will get an object not found error
	// this may be necessary only for the toolchain
	dataParser = [[NSXMLParser alloc] initWithContentsOfURL:xmlURL];
	
	// Set self as the delegate of the parser so that it will receive the parser delegate methods callbacks.
	[dataParser setDelegate:self];
	
	// Depending on the XML document you're parsing, you may want to enable these features of NSXMLParser.
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	
	[dataParser parse];
}

- (void)parseXMLFileAtData:(NSData *)webData 
{	
	dataParser = [[NSXMLParser alloc] initWithData:webData];	
	// Set self as the delegate of the parser so that it will receive the parser delegate methods callbacks.
	[dataParser setDelegate:self];	
	// Depending on the XML document you're parsing, you may want to enable these features of NSXMLParser.
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];	
	[dataParser parse];
}

- (void)parserDidStartDocument:(NSXMLParser *)parser {
	////NSLog(@"found file and started parsing");
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	//NSString * errorString = [NSString stringWithFormat:@"Unable to download woeid feed from web site (Error code %i )", [parseError code]];
	//NSLog(@"woeid error parsing XML: %@", errorString);
	
//	UIAlertView * errorAlert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Error loading content"] message:errorString delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
//	[errorAlert show];
//	[errorAlert release];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	currentElement = [elementName copy];
	//NSLog(@"WOEID currentElement=%@", currentElement);
	
	//	if ([elementName isEqualToString:@"Placemark"]) 
	//	{
	//		thisId = [attributeDict objectForKey:@"id"]; 
	//	}
	//	
	//	if ([elementName isEqualToString:@"address"]) 
	//	{
	//		//address = thisId;
	//	}
	
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	//	if ([elementName isEqualToString:@"Placemark"]) 
	//	{
	//		//		[item setObject:condition forKey:@"condition"];
	//		//		[item setObject:conditionCode forKey:@"conditionCode"];
	//		//		[item setObject:temp forKey:@"temp"];
	//	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if (string != nil && [currentElement isEqualToString:@"woeid"] && woeid == nil) 
	{
		self.woeid = [[NSMutableString alloc] initWithString:string];
		////NSLog(@"string is %@", string);
		////NSLog(@"woeid is %@", woeid);
	}
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}

@end
