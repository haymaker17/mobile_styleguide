//
//  CardsGetPersonalAndTransactions.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CardsGetPersonalAndTransactions.h"


@implementation CardsGetPersonalAndTransactions
@synthesize path, currentElement, cards, pCard, keys;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	
	[cards removeAllObjects]; // = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];	
	[keys removeAllObjects]; // = [[NSMutableArray alloc] initWithObjects:nil];
	dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
	dataParser = nil;
	self.currentElement = nil;
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
        cards = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];	
        keys = [[NSMutableArray alloc] initWithObjects:nil];
        [self flushData];	
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return CARDS_PERSONAL_TRAN_DATA;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	self.path = [NSString stringWithFormat:@"%@/Mobile/Expense/GetPersonalCardsWithTransactions",[ExSystem sharedInstance].entitySettings.uri];
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
	
	if ([elementName isEqualToString:@"PersonalCard"])
	{
		pCard = [[PersonalCardData alloc] init];
	}
	else if ([elementName isEqualToString:@"PersonalCardTransaction"])
	{
	}

	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"PersonalCard"])
	{
		cards[pCard.pcaKey] = pCard;
		[keys addObject:pCard.pcaKey];
	}
	else if ([elementName isEqualToString:@"PersonalCardTransaction"])
	{
		[pCard finishTran];
	}

	
}



- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if ([currentElement isEqualToString:@"AccountNumberLastFour"])
	{
		[pCard setAccountNumberLastFour:string];
	}
	else if ([currentElement isEqualToString:@"CardName"])
	{
		[pCard setCardName:string];
	}
	else if ([currentElement isEqualToString:@"CrnCode"])
	{
		[pCard setCrnCode:string];
	}
	else if ([currentElement isEqualToString:@"PcaKey"])
	{
		[pCard setPcaKey:string];
	}
	
	else if ([currentElement isEqualToString:@"Amount"])
	{
		[pCard.tran setTranAmount:[string doubleValue]];
	}
	else if ([currentElement isEqualToString:@"Category"])
	{
		[pCard.tran setCategory:string];
	}
	else if ([currentElement isEqualToString:@"DatePosted"])
	{
		NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
		[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
		NSDate *dt = [dateFormatter dateFromString:string];
		[pCard.tran setTranDate:dt]; 
	}
	else if ([currentElement isEqualToString:@"Description"])
	{
		[pCard.tran setDescription:string];
	}
	else if ([currentElement isEqualToString:@"ExpKey"])
	{
		[pCard.tran setExpKey:string];
	}
	else if ([currentElement isEqualToString:@"ExpName"])
	{
		[pCard.tran setExpName:string];
	}
	else if ([currentElement isEqualToString:@"PctKey"])
	{
		[pCard.tran setPctKey:string];
	}
	else if ([currentElement isEqualToString:@"Status"])
	{
		[pCard.tran setTranStatus:string];
	}
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}



@end
