//
//  MobileExpneseSave.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//
//  Class to save/update an expense entry.
//

#import "MobileExpenseSave.h"
#import "FormatUtils.h"
#import "MobileEntryManager.h"

@implementation MobileExpenseSave


@synthesize path, currentElement, entry, returnStatus, meKey;


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
	return ME_SAVE_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	if (parameterBag != nil & parameterBag[@"ENTRY"] != nil)
		self.entry = parameterBag[@"ENTRY"];
    
    NSString * msgUuid = nil;
    if (parameterBag != nil && parameterBag[@"MSG_UUID"] != nil)
        msgUuid = parameterBag[@"MSG_UUID"];
	
	//clearImage
	if(entry.receiptImageId == nil && ![entry.hasReceipt isEqualToString:@"Y"])
	{
		//NSLog(@"clearing image");
		self.path = [NSString stringWithFormat:@"%@/Mobile/Expense/SaveMobileEntry/Y",[ExSystem sharedInstance].entitySettings.uri];
	}
	else
		self.path = [NSString stringWithFormat:@"%@/Mobile/Expense/SaveMobileEntry",[ExSystem sharedInstance].entitySettings.uri];
	
    NSLog(@"Saving oop with receipt id = %@, \n path = %@",entry.receiptImageId,path);
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	
	if (parameterBag != nil & parameterBag[@"ENTRY"] != nil)
	{
		[msg setBody:[self makeSaveBody]];
	}
    
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
    [msg setUuid:msgUuid];
	
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



//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData
{    
	[super parseXMLFileAtData:webData];
}


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	
	[self flushData];
	[self parseXMLFileAtData:data];
}

-(NSString *)makeSaveBody
{
	__autoreleasing NSMutableString *body = [[NSMutableString alloc] initWithString:@"<MobileEntry>"];
    
	// Add CCT_KEY, if linked with a Cct object
	if (entry.cctKey != nil && entry.smartExpenseMeKey == nil)
	{
		[body appendString:@"<CctKey>"];
		[body appendString:entry.cctKey];
		[body appendString:@"</CctKey>"];
	}
	
	if(entry.comment != nil)
	{
		[body appendString:@"<Comment>"];
		[body appendString:[FormatUtils makeXMLSafe:entry.comment]];
		[body appendString:@"</Comment>"];
	}
    
    if(entry.crnCode != nil)
    {
        [body appendString:@"<CrnCode>"];
        [body appendString:entry.crnCode];
        [body appendString:@"</CrnCode>"];
    }
    
    if(entry.expKey != nil)
    {
        [body appendString:@"<ExpKey>"];
        [body appendString:entry.expKey];
        [body appendString:@"</ExpKey>"];
    }
	
	if(entry.hasReceipt != nil)
	{
		[body appendString:@"<HasReceiptImage>"];
		if (entry.receiptImageId == nil && [entry.hasReceipt isEqualToString:@"Y"]) //the receipt might be in a loading state when the user hit SAVE MOB-3435
			[body appendString:@"Y"];
		else if (entry.receiptImageId == nil)
			[body appendString:@"N"];
		else
			[body appendString:@"Y"];
		
		[body appendString:@"</HasReceiptImage>"];
	}
	
	if(entry.locationName != nil)
	{
		[body appendString:@"<LocationName>"];
		[body appendString:[FormatUtils makeXMLSafe:entry.locationName]];
		[body appendString:@"</LocationName>"];
	}
    
	//MOB-13656
    if(entry.key != nil)
	{
		[body appendString:@"<MeKey>"];
		[body appendString:entry.key];
		[body appendString:@"</MeKey>"];
	}

    // MOB-14606 changing expense type splits a smart expense.  the server needs the mekey of the match.
    else if(entry.smartExpenseMeKey != nil)
	{
		[body appendString:@"<MeKey>"];
		[body appendString:entry.smartExpenseMeKey];
		[body appendString:@"</MeKey>"];
	}

    
    NSMutableString *body2 = [[NSMutableString alloc] initWithString:@""];
	// Add PCT_KEY, if linked with a Pct object
	if (entry.pctKey != nil)
	{
        if(entry.pctKey != nil)
        {
            [body2 appendString:@"<PctKey>"];
            [body2 appendString:entry.pctKey];
            [body2 appendString:@"</PctKey>"];
        }
	}
    
    if (entry.receiptImageId != nil && entry.hasReceipt != nil && [entry.hasReceipt isEqualToString:@"Y"])
    {
        if(entry.receiptImageId != nil)
        {
            [body2 appendString:@"<ReceiptImageId>"];
            [body2 appendString:entry.receiptImageId];
            [body2 appendString:@"</ReceiptImageId>"];
        }
    }
    
	[body2 appendString:@"<TransactionAmount>"];
	[body2 appendString:[NSString stringWithFormat:@"%f", [entry.transactionAmount floatValue]]];
	[body2 appendString:@"</TransactionAmount>"];
    if(entry.transactionDate != nil)
    {
        [body2 appendString:@"<TransactionDate>"];
        [body2 appendString: [CCDateUtilities formatDateYYYYMMddByNSDate:entry.transactionDate]];
        [body2 appendString:@"</TransactionDate>"];
    }
    
	if(entry.vendorName != nil)
	{
		[body2 appendString:@"<VendorName>"];
		[body2 appendString:[FormatUtils makeXMLSafe:entry.vendorName]];
		[body2 appendString:@"</VendorName>"];
	}
	
	[body2 appendString:@"</MobileEntry>"];
	
	[body appendString:body2];
    
	return body;
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
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"Status"])
	{
        
	}
}



- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if ([currentElement isEqualToString:@"Status"])
	{
		self.returnStatus = string;
	}
	else if ([currentElement isEqualToString:@"MeKey"])
	{
		self.meKey = string;
	}
}


- (void)parserDidEndDocument:(NSXMLParser *)parser
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}



@end
