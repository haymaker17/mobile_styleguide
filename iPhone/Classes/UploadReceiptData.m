//
//  UploadReceiptData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/5/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "UploadReceiptData.h"

#import "DateTimeFormatter.h"
#import "NSStringAdditions.h"
#import "FormatUtils.h"

@implementation UploadReceiptData


@synthesize path, currentElement, entry, returnStatus, meKey,receiptImageId,receiptImageUrl;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
//	
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
	return UPLOAD_IMAGE_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	NSData *imgData = nil;
	if (parameterBag != nil && parameterBag[@"ENTRY"] != nil) 
	{
		self.entry = parameterBag[@"ENTRY"];
	}
	
	self.path = parameterBag[@"URL"]; 
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	

    // MOB-9414 Image is not a part of report entry before save succeeds.  Check image first. 	
    if (parameterBag != nil && parameterBag[@"IMAGE"] != nil)
	{
        // This is preshrank image (ExReceiptManager::uploadReceipt)
        imgData = parameterBag[@"IMAGE"];
		[msg setBodyData:(NSMutableData*)imgData];
	}
    
    if (parameterBag != nil && parameterBag[@"PDF"] != nil)
	{
        imgData = parameterBag[@"PDF"];
		[msg setBodyData:(NSMutableData*)imgData];
	}
    
    else if (parameterBag != nil & parameterBag[@"ENTRY"] != nil)
	{
        // TODO: Should never check entry.receiptImage.  Keep the code for backward compatibility only
        // MOB-12986 : Cannot do this as receiptimg is not in this object
        // imgData = UIImageJPEGRepresentation(entry.receiptImage, 0.9f);
        [msg setBodyData:(NSMutableData*)imgData];
	}
	
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Uploading receipt of size: %lu bytes", (unsigned long)imgData.length] Level:MC_LOG_INFO];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
    if (parameterBag != nil && parameterBag[@"PDF"] != nil)
        [msg setContentType:@"application/pdf"];
    else
        [msg setContentType:@"image/jpeg"];
    [msg setExpectedContentLength:[imgData length]];
	[msg setMethod:@"POST"];
	
	return msg;
}


-(void) flushData
{
	self.entry = nil;
	self.path = nil;
	self.receiptImageId = nil;
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
	
	if ([elementName isEqualToString:@"ExpenseType"])
	{
		//		if(et != nil)
		//			[et release];
		//		
		//		et = [[ExpenseTypeData alloc] init];
	}
	
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
        // MOB-12986 : This might be problematic if key is sent and cctkey and pctkey are present
        // Since entry.key is set to pctkey or cctkey for credit cards
		entry.key = string;
		self.meKey = string;
	}
	else if ([currentElement isEqualToString:@"ReceiptImageId"])
	{
		self.receiptImageId = string;
	}
	else if ([currentElement isEqualToString:@"ReceiptImageURL"])
	{
		self.receiptImageUrl = (receiptImageUrl!=nil)?[receiptImageUrl stringByAppendingString:string]:string;
	}
	
	//	else if ([currentElement isEqualToString:@"ExpKey"])
	//	{
	//		[et setExpKey:string];
	//	}
	//	else if ([currentElement isEqualToString:@"ExpName"])
	//	{
	//		[et setExpName:string];
	//	}
	//	else if ([currentElement isEqualToString:@"ParentExpName"])
	//	{
	//		[et setParentExpName:string];
	//	}
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


@end
