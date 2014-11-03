//
//  TrainStationsData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainStationsData.h"


@implementation TrainStationsData

@synthesize path, currentElement, items, obj, keys;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	self.items = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	self.keys = [[NSMutableArray alloc] initWithObjects:nil];

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
	return TRAIN_STATIONS;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	NSString *vendorCode = @"2V"; //amtrak?
	self.path = [NSString stringWithFormat:@"%@/Mobile/Rail/GetRailStationList/%@",[ExSystem sharedInstance].entitySettings.uri, vendorCode];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}




-(void) flushData
{
	
}



- (void)parserDidStartDocument:(NSXMLParser *)parser 
{

}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{

}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	
	isInElement = @"YES";
	
	if ([elementName isEqualToString:@"RailStation"])
	{		
//		if(obj != nil)
//			[obj release];
		//NSLog(@"New railstation didStartElement");
		self.obj = [[TrainStationData alloc] init];
	}
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"RailStation"])
	{
		if (obj.stationCode != nil) 
		{
			items[obj.stationCode] = obj;
			[keys addObject:obj.stationCode];
		}
		
//		if(obj != nil)
//			[obj release];
		//NSLog(@"New railstation didEndElement");
	}
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	//NSLog(@"element = %@, string = %@", currentElement, string);
	if ([currentElement isEqualToString:@"City"])
	{
		[obj setCity:string];
	}
	else if ([currentElement isEqualToString:@"CountryCode"])
	{
		[obj setCountryCode:string];
	}
	else if ([currentElement isEqualToString:@"IataCode"])
	{
		[obj setIataCode:string];
	}
	else if ([currentElement isEqualToString:@"State"])
	{
		[obj setStationState:string];
	}
	else if ([currentElement isEqualToString:@"StationCode"])
	{
		[obj setStationCode:string];
	}
	else if ([currentElement isEqualToString:@"StationName"])
	{
		[obj setStationName:string];
	}
	else if ([currentElement isEqualToString:@"TimeZoneID"])
	{
		[obj setTimeZoneName:string];
	}
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


@end

