//
//  MobileExpneseDelete.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//
//  Class Handle request/response to the delete Mobile expense entries
//  if Delete is successful the calling VC should delete the corresponding coredate entries
//  for PersonalCardTransactions - just a success message is returned in the response, so we have to store the pctkeys and respondtofoundDataCan Use these
//
// Change log
// MOB-13656 - 6/11/13 - Pavan Adavi
// Each return status from MWS is stored in a dictionary as keyvalue pair, and each return status is them added to list of statuses.
// keytype is stored to determine type of mobileentry.

// MWS Response sample for each entry type
/*
 <ArrayOfActionStatus xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
 <ActionStatus>
 <ErrorMessage xmlns="http://schemas.datacontract.org/2004/07/Snowbird">Failed to delete mobile entry</ErrorMessage>
 <Status xmlns="http://schemas.datacontract.org/2004/07/Snowbird">FAILURE</Status>
 <MeKey>noCc2$szEPv5S$pCWswkdeaqqAN6Ubg6Q</MeKey>
 </ActionStatus>
 </ArrayOfActionStatus>
 
 HideCorporateCard
 <ActionStatus xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
 <Status xmlns="http://schemas.datacontract.org/2004/07/Snowbird">SUCCESS</Status>
 <CcTransactions>
 <ActionStatus>
 <Status xmlns="http://schemas.datacontract.org/2004/07/Snowbird">SUCCESS</Status>
 <CctKey>ntl4ATTlgXfa9kGdG62FaScxyVnQ$pSg</CctKey>
 </ActionStatus>
 <ActionStatus>
 <Status xmlns="http://schemas.datacontract.org/2004/07/Snowbird">SUCCESS</Status>
 <CctKey>ntl4ATTlid$sugjbUBKAd$sJFCuJQZk7w</CctKey>
 </ActionStatus>
 </CcTransactions>
 </ActionStatus>
 
 Hidepersonal
 <ActionStatus xmlns="http://schemas.datacontract.org/2004/07/Snowbird"
 xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
 <ErrorMessage>Transaction hide failed.</ErrorMessage>
 <Status>FAILURE</Status> < !-- or SUCCESS -- >
 </ActionStatus>
 
 */


#import "MobileExpenseDelete.h"

@implementation MobileExpenseDelete

{
    NSString	*currentElement;
    NSString	*path;
    BOOL isInElement, isInCct, isInPCT;

}
@synthesize returnStatus, keysToKill, returnFailure, returnFailures;


-(id)init
{
    self = [super init];
	if (self)
    {
        isInElement = NO;
        currentElement = @"";
        [self flushData];
    }
	return self;
}

-(NSString *)getMsgIdKey
{
	return ME_DELETE_DATA;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    //set up the message
    
	if (parameterBag != nil && [@"PCT_TYPE" isEqualToString:(NSString*)parameterBag[@"TYPE"]])
	{
		path = [NSString stringWithFormat:@"%@/mobile/Expense/HidePersonalCardTransactions",[ExSystem sharedInstance].entitySettings.uri];
        isInPCT = YES;
	}
	else if (parameterBag != nil && [@"CCT_TYPE" isEqualToString:(NSString*)parameterBag[@"TYPE"]])
	{
		path = [NSString stringWithFormat:@"%@/mobile/Expense/HideCorporateCardTransactions",[ExSystem sharedInstance].entitySettings.uri];
		isInCct = YES;
	}
	else
	{
		path = [NSString stringWithFormat:@"%@/mobile/Expense/DeleteMobileEntries",[ExSystem sharedInstance].entitySettings.uri];
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
    returnFailures = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
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
	
	currentElement = elementName;
	 
    // if its either corp card entries or mobile entries.
    // Personal car returns only success. 
    if ([elementName isEqualToString:@"ActionStatus"])
	{
		returnFailure = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	}
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    
	if ([elementName isEqualToString:@"ActionStatus"])
	{
        // ME_KEY is nil if ActionStatus is end of document for ccTransactions response
        // MOB-13656 - see summary above , dictionary count = 1 for PCT_TYPE
        // MOB-13688 - Added support for personal card transactions
        if(returnFailure  != nil && [returnFailure count] > 1)
        {
            NSString *key = returnFailure[OOP_TYPE] != nil ? returnFailure[OOP_TYPE] : returnFailure[CCT_TYPE];
            returnFailures[key] = returnFailure;
            returnFailure = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        }
	}

}



- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}

 // save for the success/failure for each key and Expense Type.
- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		returnFailure[@"ERROR_MESSAGE"] = string;
	}
	else if ([currentElement isEqualToString:@"Status"])
	{
        if(isInPCT || isInCct) // Store the return status
        {
            returnStatus = string ;
            isInPCT = isInCct = NO;
        }
        returnFailure[@"STATUS"] = string;
	}
	else if ([currentElement isEqualToString:@"MeKey"])
	{
		returnFailure[OOP_TYPE] = string;
	}	
    else if ([currentElement isEqualToString:@"CctKey"])
	{
		returnFailure[CCT_TYPE] = string;
	}

}


- (void)parserDidEndDocument:(NSXMLParser *)parser
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


@end

