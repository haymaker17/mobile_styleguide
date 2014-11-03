//
//  ApproveReportEntryData.m
//  ConcurMobile
//
//  Created by Yuri on 1/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveReportEntryData.h"
#import "Msg.h"
#import "DataConstants.h"
#import "RootViewController.h"

static int traceLevel = 1;

#define LOG_IF(level, x) { if(level<=traceLevel) x; }

@implementation ApproveReportEntryData

@synthesize parameterBag;
@synthesize entries;
@synthesize keys;
@synthesize isInElement;
@synthesize currentElement;
@synthesize currentEntry;
@synthesize msg;
@synthesize path;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	NSString* sData = [[NSString alloc] initWithData:webData encoding:NSASCIIStringEncoding];
	LOG_IF(1, NSLog(@"ApproveReportEntryData::parseXMLFileAtData, data: \n%@", sData));
	[sData release];
	
	// Parser only needed in the scope of implementation, single thread situation
	NSXMLParser* dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
	[dataParser release];
}

// MsgReponder APIs
-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self flushData];
	[self parseXMLFileAtData:data];
}

-(void) init:(MsgControl *)msgControl mainRootViewController:(RootViewController *)mainRootVC  ParameterBag:(NSMutableDictionary *)pBag
{//set up the messageØ
	[super init];
	
	parameterBag = pBag;
	isInElement = false;
    entries = [[NSMutableDictionary alloc] init];
	keys = [[NSMutableArray alloc] init];
	NSLog(@" param bag=%@", parameterBag);
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetReportDetail/%@/%@", 
						mainRootVC.settings.uri, 
						[parameterBag objectForKey:@"RptKey"],
						[parameterBag objectForKey:@"RoleCode"]];
	
	NSLog(@" rpts path=%@", self.path);
	self.msg = [Msg alloc];
    [self.msg init:APPROVE_REPORT_ENTRY_DATA State:@"" Position:nil MessageData:nil URI:self.path MessageResponder:self ParameterBag:parameterBag];	
    [self.msg release];
}


-(void) getData:(MsgControl *)msgControl
{
	[msgControl add:msg];
}

-(void) flushData
{
	if(self.keys != nil)
	{
		for(int x = 0; x < [self.keys count]; x++)
		{
            id key = [self.keys objectAtIndex:x];
			[self.entries removeObjectForKey:key];
		}
	}
    
    [self.keys removeAllObjects];
}

- (void)dealloc 
{
	[parameterBag release];
	[entries release];
	[keys release];
    [msg release];
    [path release];
    [currentElement release];
	[super dealloc];
}

// SAX Parsing APIs
- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	//NSLog(@"found file and started parsing");
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	NSString * errorString = [NSString stringWithFormat:@"Unable to authenticate from web site (Error code %i )", [parseError code]];
	//NSLog(@"error parsing XML: %@", errorString);
	
	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error loading content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	[errorAlert show];
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    NSLog(@"rpt::before copy retaincount=%d", [elementName retainCount]);
	
	self.currentElement = elementName;
    NSLog(@"rpt::after copy retaincount=%@ %d",elementName, [elementName retainCount]);
    NSLog(@"rpt::after copy retaincount=%d", [self.currentElement retainCount]);
	
	isInElement = true;
	
	if ([elementName isEqualToString:@"ReportEntryDetail"])
	{//alloc the trip instance
		NSLog(@"Starting XML Parsing");
		if (currentEntry == nil)
		{
			currentEntry = [[NSMutableDictionary alloc] init];
			NSLog(@"ReportEntryData::didStartElement currentEntry was alloc retaincount=%d", [currentEntry retainCount]);
			
		}
		else 
		{
			NSLog(@"ReportEntryData::didStartElement currentEntry was NOT nil retaincount=%d", [currentEntry retainCount]);
			int rc = [currentEntry retainCount];
			[currentEntry release];
			if (rc > 1)
			{
				NSLog(@"ReportEntryData::didStartElement currentEntry was released retaincount=%d", [currentEntry retainCount]);
			}
			currentEntry = [[NSMutableDictionary alloc] init];
			NSLog(@"ReportEntryData::didStartElement currentEntry was reallocated retaincount=%d", [currentEntry retainCount]);
		}
	}
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = false;
	
	if ([elementName isEqualToString:@"ReportEntryDetail"]) 
	{
		NSLog(@"ReportEntryData::didEndElement currentEntry pre added to dict retaincount=%d", [currentEntry retainCount]);
		[self.entries setObject:currentEntry forKey:[currentEntry valueForKey:@"RpeKey"]];
		NSLog(@"ReportEntryData::didEndElement currentEntry NOW added to dict retaincount=%d", [currentEntry retainCount]);
		
		[self.keys addObject:[currentEntry valueForKey:@"RpeKey"]];
		//[trip release];
		NSLog(@"Done XML Parsing");
	}
}

- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	NSString *myString = [string stringByReplacingOccurrencesOfString:@"\n" withString:@""];
    NSLog(@"rpt::in element retaincount=%@ %d",self.currentElement, [self.currentElement retainCount]);
	
	if (!isInElement)
		return;
	
	if ([myString isEqualToString:@""] || string == nil)
		return;
	
    NSDictionary *
	ENTRY_FIELDS = [[NSDictionary alloc] initWithObjectsAndKeys:
								@"ApprovedAmount", @"ApprovedAmount",
								@"ExpName", @"ExpName",
								@"HasAllocation", @"HasAllocation",
								@"HasAttendees", @"HasAttendees",
								@"HasComments", @"HasComments",
								@"HasExceptions", @"HasExceptions",
								@"IsCreditCardCharge", @"IsCreditCardCharge",
								@"IsItemized", @"IsItemized",
								@"IsPersonal", @"IsPersonal",
								@"LocationName", @"LocationName",
								@"RpeKey", @"RpeKey",
								@"TransactionAmount", @"TransactionAmount",
								@"TransactionCrnCode", @"TransactionCrnCode",
								@"TransactionDate", @"TransactionDate",
								
								@"Fields", @"Fields",
								@"FormField", @"FormField",
								@"Id", @"Id",
								@"Label", @"Label",
								@"Value", @"Value",
								nil];
    
    // TODO- use a dictionary
	if ([ENTRY_FIELDS objectForKey:currentElement] != nil) 
 	{
		[currentEntry setValue:string forKey:currentElement];
	}
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}

@end
