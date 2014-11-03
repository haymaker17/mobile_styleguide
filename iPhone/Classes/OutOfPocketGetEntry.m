//
//  OutOfPocketGetEntry.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "OutOfPocketGetEntry.h"

@implementation OutOfPocketGetEntry

@synthesize path, currentElement, oope;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	//NSLog(@"OutOfPocketGetEntry::parseXMLFileAtData");
	
	dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
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
        [self flushData];
    }
	return self;
}

-(NSString *)getMsgIdKey
{
	return OOPE_DATA;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	NSString *meKey = parameterBag[@"ME_KEY"];
	
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetMobileEntry/%@",[ExSystem sharedInstance].entitySettings.uri, meKey];	

	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	
	[msg setContentType:@"application/xml"];
	
	[msg setMethod:@"GET"];
	
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
	
	isInElement = @"YES";
	
	if ([elementName isEqualToString:@"MobileEntry"])
	{
		oope = [[OOPEntry alloc] init];
	}
	else if ([elementName isEqualToString:@"CrnCode"])
	{
	}
	else if ([elementName isEqualToString:@"ExpKey"])
	{
	}
	else if ([elementName isEqualToString:@"ExpName"])
	{
	}
	else if ([elementName isEqualToString:@"MeKey"])
	{
	}
	else if ([elementName isEqualToString:@"TransactionAmount"])
	{
	}
	else if ([elementName isEqualToString:@"TransactionDate"])
	{
	}
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"MobileEntry"])
	{
		//NSLog(@"oope.meKey=%@", oope.meKey);
	}
	else if ([elementName isEqualToString:@"CrnCode"])
	{
	}
	else if ([elementName isEqualToString:@"ExpKey"])
	{
	}
	else if ([elementName isEqualToString:@"ExpName"])
	{
	}
	else if ([elementName isEqualToString:@"MeKey"])
	{
	}
	else if ([elementName isEqualToString:@"TransactionAmount"])
	{
	}
	else if ([elementName isEqualToString:@"TransactionDate"])
	{
	}
	
}



- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if ([currentElement isEqualToString:@"MobileEntry"])
	{
		
	}
	else if ([currentElement isEqualToString:@"CrnCode"])
	{
		[oope setCrnCode:string];
	}
	else if ([currentElement isEqualToString:@"ExpKey"])
	{
		[oope setExpKey:string];
	}
	else if ([currentElement isEqualToString:@"ExpName"])
	{
		[oope setExpName:string];
	}
	else if ([currentElement isEqualToString:@"MeKey"])
	{
		[oope setMeKey:string];
	}
	else if ([currentElement isEqualToString:@"TransactionAmount"])
	{
		[oope setTranAmount:[string doubleValue]]; 
	}
	else if ([currentElement isEqualToString:@"TransactionDate"])
	{
		NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
		[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
		NSDate *dt = [dateFormatter dateFromString:string];
		[oope setTranDate:dt]; 
	}
	else if ([currentElement isEqualToString:@"Comment"])
	{
		[oope setComment:string];
	}
	else if ([currentElement isEqualToString:@"LocationName"])
	{
		[oope setLocationName:string]; 
	}
	else if ([currentElement isEqualToString:@"VendorName"])
	{
		[oope setVendorName:string]; 
	}
	else if ([currentElement isEqualToString:@"HasReceiptImage"])
	{
		[oope setHasReceipt:string]; 
	}	
    else if ([currentElement isEqualToString:@"ReceiptImageId"])
	{
        [oope setReceiptImageId:string];
	}
	else if ([currentElement isEqualToString:@"ReceiptImage"])
	{
        NSData *data = [NSData dataWithBase64String:string]; // [NSData base64DataFromString:string];;
		[oope setReceiptData:data];
	}	
	
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}



@end
