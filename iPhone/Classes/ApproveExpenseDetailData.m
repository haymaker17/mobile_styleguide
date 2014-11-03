//
//  ApproveExpenseDetail.m
//  ConcurMobile
//
//  Created by Yuri on 1/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveExpenseDetailData.h"
#import "Msg.h"
#import "DataConstants.h"
#import "RootViewController.h"

static int traceLevel = 1;

#define LOG_IF(level, x) { if(level<=traceLevel) x; }

@implementation ApproveExpenseDetailData

@synthesize parameterBag;
@synthesize entries;
@synthesize exceptions, fields;
@synthesize isInElement;
@synthesize currentElement;
@synthesize currentEntry;
@synthesize msg;
@synthesize path;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	NSString* sData = [[NSString alloc] initWithData:webData encoding:NSASCIIStringEncoding];
	LOG_IF(2, NSLog(@"ApproveExpenseDetail::parseXMLFileAtData, data: \n%@", sData));
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
	exceptions = [[NSMutableArray alloc] init];
	fields = [[NSMutableArray alloc] init];
	LOG_IF(2, NSLog(@" param bag=%@", parameterBag));
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense.svc/GetExpenseEntryDetails/%@/%@/%@", 
				 mainRootVC.settings.uri, 
				 [parameterBag objectForKey:@"RptKey"], 
				 [parameterBag objectForKey:@"RpeKey"],
				 [parameterBag objectForKey:@"RoleCode"]
				 ];
	LOG_IF(2, NSLog(@" rpts path=%@", self.path));
	self.msg = [Msg alloc];
    [self.msg init:APPROVE_EXPENSE_DATA State:@"" Position:nil MessageData:nil URI:self.path MessageResponder:self ParameterBag:parameterBag];	
    [self.msg release];
}


-(void) getData:(MsgControl *)msgControl
{
	[msgControl add:msg];
}

-(void) flushData
{
	if(self.exceptions != nil)
	{
		for(int x = 0; x < [self.exceptions count]; x++)
		{
            id exception = [self.exceptions objectAtIndex:x];
			[self.entries removeObjectForKey:exception];
		}
	}
    
    [self.exceptions removeAllObjects];

	if(self.fields != nil)
	{
		for(int x = 0; x < [self.fields count]; x++)
		{
            id field = [self.fields objectAtIndex:x];
			[self.entries removeObjectForKey:field];
		}
	}
    
    [self.fields removeAllObjects];
}

- (void)dealloc 
{
	[parameterBag release];
	[entries release];
	[exceptions release];
	[fields release];
    [msg release];
    [path release];
    [currentElement release];
	[super dealloc];
}

// SAX Parsing APIs
- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	//LOG_IF(2, NSLog(@"found file and started parsing"));
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	NSString * errorString = [NSString stringWithFormat:@"Unable to authenticate from web site (Error code %i )", [parseError code]];
	// LOG_IF(2, NSLog(@"error parsing XML: %@", errorString));
	
	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error loading content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	[errorAlert show];
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    LOG_IF(2, NSLog(@"rpt::before copy retaincount=%d", [elementName retainCount]));
	
	self.currentElement = elementName;
    LOG_IF(2, NSLog(@"rpt::after copy retaincount=%@ %d",elementName, [elementName retainCount]));
    LOG_IF(2, NSLog(@"rpt::after copy retaincount=%d", [self.currentElement retainCount]));
	
	isInElement = true;
	
	if ([elementName isEqualToString:@"FormDetails"])
	{//alloc the trip instance
		LOG_IF(2, NSLog(@"Starting XML Parsing"));
		if (currentEntry == nil)
		{
			currentEntry = [[NSMutableDictionary alloc] init];
			LOG_IF(2, NSLog(@"ExpenseDetailData::didStartElement currentEntry was alloc retaincount=%d", [currentEntry retainCount]));
			
		}
		else 
		{
			LOG_IF(2, NSLog(@"ExpenseDetailData::didStartElement currentEntry was NOT nil retaincount=%d", [currentEntry retainCount]));
			int rc = [currentEntry retainCount];
			[currentEntry release];
			if (rc > 1)
			{
				LOG_IF(2, NSLog(@"ExpenseDetailData::didStartElement currentEntry was released retaincount=%d", [currentEntry retainCount]));
			}
			currentEntry = [[NSMutableDictionary alloc] init];
			LOG_IF(2, NSLog(@"ExpenseDetailData::didStartElement currentEntry was reallocated retaincount=%d", [currentEntry retainCount]));
		}
	}
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = false;
	
	if ([elementName isEqualToString:@"FormDetails"]) 
	{
		// Exceptions have RpeKey
		if([currentEntry valueForKey:@"RpeKey"] != nil)
		{
			LOG_IF(2, NSLog(@"ExpenseDetailData::didEndElement currentEntry pre added to dict retaincount=%d", [currentEntry retainCount]));
			[self.entries setObject:currentEntry forKey:[currentEntry valueForKey:@"RpeKey"]];
			LOG_IF(2, NSLog(@"ExpenseDetailData::didEndElement currentEntry NOW added to dict retaincount=%d", [currentEntry retainCount]));
			
			[self.exceptions addObject:[currentEntry valueForKey:@"RpeKey"]];
			//[trip release];
			LOG_IF(2, NSLog(@"Done XML Parsing"));
		}
		else // Fields have Id
		if([currentEntry valueForKey:@"Id"] != nil)
		{
			LOG_IF(2, NSLog(@"ExpenseDetailData::didEndElement currentEntry pre added to dict retaincount=%d", [currentEntry retainCount]));
			[self.entries setObject:currentEntry forKey:[currentEntry valueForKey:@"Id"]];
			LOG_IF(2, NSLog(@"ExpenseDetailData::didEndElement currentEntry NOW added to dict retaincount=%d", [currentEntry retainCount]));
			
			[self.fields addObject:[currentEntry valueForKey:@"Id"]];
			//[trip release];
			LOG_IF(2, NSLog(@"Done XML Parsing"));
		}
	}
}

- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	LOG_IF(2, NSLog(@"Ahh SHUCKS"));
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	NSString *myString = [string stringByReplacingOccurrencesOfString:@"\n" withString:@""];
    LOG_IF(2, NSLog(@"rpt::in element retaincount=%@ %d",self.currentElement, [self.currentElement retainCount]));
	
	if (!isInElement)
		return;
	
	if ([myString isEqualToString:@""] || string == nil)
		return;
	
    NSDictionary *
	ENTRY_FIELDS = [[NSDictionary alloc] initWithObjectsAndKeys:
					@"FormDetails", @"FormDetails",
					@"Exceptions", @"Exceptions",
					@"CESException", @"CESException",
					@"ExceptionsStr", @"ExceptionsStr",
					@"IsCleared", @"IsCleared",
					@"RpeKey", @"RpeKey",
					@"SeverityLevel", @"SeverityLevel",
					@"Fields", @"Fields",
					@"FormField", @"FormField",
					@"Id", @"Id",
					@"Label", @"Label",
					@"Value", @"Value",
					@"ObjectKey", @"ObjectKey"
					, nil];
    
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
