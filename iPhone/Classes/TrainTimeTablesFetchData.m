//
//  TrainTimeTablesFetchData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainTimeTablesFetchData.h"
#import "AmtrakShop.h"

@implementation TrainTimeTablesFetchData

@synthesize path, currentElement, items, obj, keys, railChoices, currentViolation;

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
	isInElement = @"NO";
	currentElement = @"";
	[self flushData];	
	return self;
}


-(NSString *)getMsgIdKey
{
	return TRAIN_SHOP;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.path = [NSString stringWithFormat:@"%@/Mobile/Rail/AmtrakShopV2",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	return msg;
}


-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post
	AmtrakShop *shop = parameterBag[@"SHOP"];
	
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<RailShop>"];
	[bodyXML appendString:@"<ArrivalStation>%@</ArrivalStation>"];
	[bodyXML appendString:@"<ArrivalStationTimeZoneId>%@</ArrivalStationTimeZoneId>"];
	[bodyXML appendString:@"<ClassOfTravel>%@</ClassOfTravel>"];
	[bodyXML appendString:@"<DepartureDateTime>%@</DepartureDateTime>"];
	[bodyXML appendString:@"<DepartureStation>%@</DepartureStation>"];
	[bodyXML appendString:@"<DepartureStationTimeZoneId>%@</DepartureStationTimeZoneId>"];
	[bodyXML appendString:@"<DirectOnly>%@</DirectOnly>"];
	[bodyXML appendString:@"<NumberOfPassengers>%@</NumberOfPassengers>"];
	[bodyXML appendString:@"<RefundableOnly>%@</RefundableOnly>"];
	if(shop.returnDateTime != nil)
		[bodyXML appendString:@"<ReturnDateTime>%@</ReturnDateTime>"];
	[bodyXML appendString:@"</RailShop>"];
	
//	TrainStationData *stationFrom = [parameterBag objectForKey:@"STATION_FROM"];
//	TrainStationData *stationTo = [parameterBag objectForKey:@"STATION_TO"];
//	NSString *departureDate = [parameterBag objectForKey:@"DEPARTURE_DATE"];
//	NSString *arrivalDate = [parameterBag objectForKey:@"ARRIVAL_DATE"];
	
	NSString* formattedBodyXml = nil;
	if(shop.returnDateTime != nil)
	{
		formattedBodyXml = [NSString stringWithFormat:bodyXML,
		  shop.arrivalStation, 
		  shop.arrivalStationTimeZoneId,
		  @"Y",
		  shop.departureDateTime,
		  shop.departureStation,
		  shop.departureStationTimeZoneId,
		  @"false",
		  @"1",
		  @"false",
		  shop.returnDateTime
		  ];
	}
	else {
		formattedBodyXml = [NSString stringWithFormat:bodyXML,
							shop.arrivalStation, 
							shop.arrivalStationTimeZoneId,
							@"Y",
							shop.departureDateTime,
							shop.departureStation,
							shop.departureStationTimeZoneId,
							@"false",
							@"1",
							@"false"
							];
	}

	
//	NSLog(@"%@", formattedBodyXml);
	return formattedBodyXml;
}


-(void) flushData
{
	
}



- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	self.railChoices = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	
	isInElement = @"YES";
	
	if ([elementName isEqualToString:@"RailChoice"])
	{		
		self.obj = [[RailChoiceData alloc] init];
	}
	else if ([elementName isEqualToString:@"SegmentOption"])
	{		
		self.obj.segment = [[RailChoiceSegmentData alloc] init];
	}
	else if ([elementName isEqualToString:@"Flight"])
	{		
		self.obj.segment.train = [[RailChoiceTrainData alloc] init];
	}
	else if ([elementName isEqualToString:@"Violation"])
	{
		self.currentViolation = [[HotelViolation alloc] init];
	}
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"RailChoice"])
	{
		items[@"RAIL_CHOICE"] = obj;
		[keys addObject:obj];
		
		//NSLog(@"keys count = %d", [keys count]);
		//NSLog(@"items count = %d", [items count]);
		
		if(obj.groupId != nil)
		{
			//This is so that we can aggregate on groupId
			NSMutableArray *aChoices = railChoices[obj.groupId];
			if(aChoices == nil)
				aChoices = [[NSMutableArray alloc] initWithObjects:nil];
			
			[aChoices addObject:obj];
			railChoices[obj.groupId] = aChoices;
		}
	}
	else if ([elementName isEqualToString:@"SegmentOption"])
	{		
		[obj.segments addObject:obj.segment];
		//[obj.segment release];
		//self.obj.segment = [[RailChoiceSegmentData alloc] init];
	}
	else if ([elementName isEqualToString:@"Flight"])
	{		
		[obj.segment.trains addObject:obj.segment.train];
		//[obj.segment.train release];
		//self.obj.segment.train = [[RailChoiceSegmentData alloc] init];
	}
	else if ([elementName isEqualToString:@"Violation"])
	{
		if (obj != nil)
			[obj.violations addObject:currentViolation];
		self.currentViolation = nil;
	}
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	//NSLog(@"currentElement = %@ string = %@", currentElement, string);
	
	if ([currentElement isEqualToString:@"BaseFare"])
	{
		[obj setBaseFare:string];
	}
	else if ([currentElement isEqualToString:@"Cost"])
	{
		[obj setCost:string];
	}
    else if ([currentElement isEqualToString:@"ChoiceId"])
	{
		[obj setChoiceId:string];
	}
	else if ([currentElement isEqualToString:@"Currency"])
	{
		[obj setCurrencyCode:string];
	}
	else if ([currentElement isEqualToString:@"Description"])
	{
		[obj setDescript:string];
	}
	else if ([currentElement isEqualToString:@"GdsName"])
	{
		[obj setGdsName:string];
	}
	else if ([currentElement isEqualToString:@"ImageUri"])
	{
		[obj setImageUri:string];
	}
	else if ([currentElement isEqualToString:@"MaxEnforcementLevel"])
	{
		[obj setMaxEnforcementLevel:@([string intValue])];
	}
	else if ([currentElement isEqualToString:@"TotalElapsedTime"])
	{
		[obj.segment setTotalTime:[string intValue]];
	}
	else if ([currentElement isEqualToString:@"Carrier"])
	{
		[obj.segment.train setCarrier:string];
	}
	else if ([currentElement isEqualToString:@"FltNum"])
	{
		[obj.segment.train setFltNum:string];
	}
	else if ([currentElement isEqualToString:@"DepDateTime"])
	{
		[obj.segment.train setDepDateTime:string];
	}
	else if ([currentElement isEqualToString:@"ArrDateTime"])
	{
		[obj.segment.train setArrDateTime:string];
	}
	else if ([currentElement isEqualToString:@"AircraftCode"])
	{
		[obj.segment.train setAircraftCode:string];
	}
	else if ([currentElement isEqualToString:@"Meals"])
	{
		[obj.segment.train setMeals:string];
	}
	else if ([currentElement isEqualToString:@"BIC"])
	{
		[obj.segment.train setBic:string];
	}
	else if ([currentElement isEqualToString:@"FltClass"])
	{
		[obj.segment.train setFltClass:string];
	}
	else if ([currentElement isEqualToString:@"FlightTime"])
	{
		[obj.segment.train setFlightTime:string];
	}
	else if ([currentElement isEqualToString:@"DepAirp"])
	{
		[obj.segment.train setDepAirp:string];
	}
	else if ([currentElement isEqualToString:@"ArrAirp"])
	{
		[obj.segment.train setArrAirp:string];
	}
	
	else if ([currentElement isEqualToString:@"GroupId"])
	{
		[obj setGroupId:string];
	}
	else if ([currentElement isEqualToString:@"Bucket"])
	{
		[obj setBucket:string];
	}
	else if ([currentElement isEqualToString:@"VendorCode"])
	{
		[obj setVendorCode:string];
	}
    else if ([currentElement isEqualToString:@"Code"])
    {
        if (currentViolation != nil)
            currentViolation.code = string;
    }
    else if ([currentElement isEqualToString:@"Message"])
    {
        if (currentViolation != nil)
            currentViolation.message = string;
    }
    else if ([currentElement isEqualToString:@"EnforcementLevel"])
    {
        if (currentViolation != nil)
            currentViolation.enforcementLevel = string;
    }
    else if ([currentElement isEqualToString:@"ViolationType"])
    {
        if (currentViolation != nil)
            currentViolation.violationType = string;
    }
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


@end
