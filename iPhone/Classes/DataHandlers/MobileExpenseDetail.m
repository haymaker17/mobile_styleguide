//
//  MobileExpenseDetail.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//
// Description :
//    NOTE : This class is not required. Expense list has all the details, expense details will just show cached data. 
//    This class gets the expense details for a given expense key, expense key can be cctkey or pctkey or mekey.
//    if coredata entry is found for a given key then the coredata entry is updated with the MWS response.
//

#import "MobileExpenseDetail.h"
#import "MobileEntryManager.h"

@implementation MobileExpenseDetail

@synthesize path, currentElement, entity;

NSString *meKey = nil;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData
{
	//NSLog(@"MobileExpenseDetail::parseXMLFileAtData");
	
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
	return ME_DETAIL_DATA;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    //set up the message
	meKey = parameterBag[@"ME_KEY"];
        
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetMobileEntry/%@",[ExSystem sharedInstance].entitySettings.uri, meKey];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	
	[msg setContentType:@"application/xml"];
	
	[msg setMethod:@"GET"];
	
	return msg;
}

-(void) flushData
{
// Nothing as of now. 
	
}

- (void)parserDidStartDocument:(NSXMLParser *)parser
{
	//NSLog(@"found file and started parsing");
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError
{
		NSString * errorString = [NSString stringWithFormat:@"Parser Error (Error code %i )", [parseError code]];
		NSLog(@"MobileExpenseEntry: error parsing XML: %@", errorString);
	//
	//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error Parsing Content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	//	[errorAlert show];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	
	isInElement = @"YES";
    if (([elementName isEqualToString:@"MobileEntry"] )
		|| [elementName isEqualToString:@"CorporateCardTransaction"]
		|| [elementName isEqualToString:@"PersonalCardTransaction"])
	{
        // check if the object is present in coredata
        self.entity = [[MobileEntryManager sharedInstance] fetchOrMake:meKey];

	}
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
    if (([elementName isEqualToString:@"MobileEntry"] )
		|| [elementName isEqualToString:@"CorporateCardTransaction"]
		|| [elementName isEqualToString:@"PersonalCardTransaction"])
	{
        // Update or save the entity here.
        [[MobileEntryManager sharedInstance]saveIt:self.entity];
    }
	
}



- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    if ([currentElement isEqualToString:@"CrnCode"])
	{
		[self.entity setCrnCode:string];
	}
	else if ([currentElement isEqualToString:@"ExpKey"])
	{
		[self.entity setExpKey:string];
	}
	else if ([currentElement isEqualToString:@"ExpName"])
	{
		[self.entity setExpName:string];
	}
	else if ([currentElement isEqualToString:@"MeKey"])
	{
		[self.entity setKey:string];
	}
	else if ([currentElement isEqualToString:@"TransactionAmount"])
	{
		[self.entity setTransactionAmount:[NSDecimalNumber decimalNumberWithString:string]];
	}
	else if ([currentElement isEqualToString:@"TransactionDate"])
	{
		NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init];
		[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
		NSDate *dt = [dateFormatter dateFromString:string];
		[self.entity setTransactionDate:dt];
	}
	else if ([currentElement isEqualToString:@"Comment"])
	{
		[self.entity setComment:string];
	}
	else if ([currentElement isEqualToString:@"LocationName"])
	{
		[self.entity setLocationName:string];
	}
	else if ([currentElement isEqualToString:@"VendorName"])
	{
		[self.entity setVendorName:string];
	}
	else if ([currentElement isEqualToString:@"HasReceiptImage"])
	{
		[self.entity setHasReceipt:string];
	}
    else if ([currentElement isEqualToString:@"ReceiptImageId"])
	{
        [self.entity setReceiptImageId:string];
	}
	else if ([currentElement isEqualToString:@"ReceiptImage"])
	{
        // Not required since receipt is obtained from receipt store.
//		NSData *data = [NSData dataFromBase64String:string]; // [NSData base64DataFromString:string];;
//		[self.entity setReceiptData:data];
	}
	
}


@end
