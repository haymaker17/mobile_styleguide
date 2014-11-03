//
//  AirShop.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/4/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "AirShop.h"
#import "AmtrakShop.h"
#import "AirRuleManager.h"
#import "EntityAirRules.h"

@implementation AirShop
@synthesize path, currentElement, items, obj, aStops, aRateTypes, stopChoices, rateTypeChoices, airlineEntry, numStops, rateTypes, isInStops, isInRateTypes, isInAirlineEntry, buildString, isInVendorCodes, vendors, vendorCode, vendorName, rankValue, rankAirlineCode, prefRankings, isInPrefRanking, isRoundTrip, isInAirportCityCodes, airportCityCodes, cityCode, cityName, airRule;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
//	if([keys retainCount] > 0)
//	{
//		[items release];
//		[keys release];
//	}		
//	items = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];	
//	keys = [[NSMutableArray alloc] initWithObjects:nil];
	
//	NSString *s = [[NSString alloc] initWithData:webData encoding:NSStringEncodingConversionExternalRepresentation];
//	NSLog(@"webData = %@", s);
//	[s release];
	
	dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
    //NSString *s = [[NSString alloc] initWithData:data encoding:NSStringEncodingConversionExternalRepresentation];
	//NSLog(@"air data = %@", s);
	[self flushData];
	[self parseXMLFileAtData:data]; //parseXMLFileAtData:[dog dataUsingEncoding:NSStringEncodingConversionAllowLossy]]; //
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
	return AIR_SHOP;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.path = [NSString stringWithFormat:@"%@/Mobile/Air/Search",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	return msg;
}


-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post
    self.isRoundTrip = [parameterBag[@"isRound"] boolValue];    

    NSString* refundableOnly = parameterBag[@"RefundableOnly"];
    if ([refundableOnly isEqualToString:@"N"]) refundableOnly = @"false";
    if ([refundableOnly isEqualToString:@"Y"]) refundableOnly = @"true";
    if (![refundableOnly length])
        refundableOnly = @"false";
    
    __autoreleasing NSString* formattedBodyXml = @"<AirCriteria><Cabin>%@</Cabin><GetBenchmark>true</GetBenchmark><IncludeDirectConnect>Travelfusion</IncludeDirectConnect><NumTravelers>1</NumTravelers><RefundableOnly>%@</RefundableOnly><Segments><AirSegmentCriteria><Date>%@</Date><EndIata>%@</EndIata><SearchTime>%@</SearchTime><StartIata>%@</StartIata><TimeIsDeparture>true</TimeIsDeparture><TimeWindow>3</TimeWindow></AirSegmentCriteria>";
   
    NSString *cabin = parameterBag[@"ClassOfService"];
    NSString *departDate = parameterBag[@"Date"];
    NSString *endIata = parameterBag[@"EndIata"];
    NSString *searchTime = parameterBag[@"SearchTime"];
    NSString *startIata = parameterBag[@"StartIata"];
    
    formattedBodyXml = [NSString stringWithFormat:formattedBodyXml, cabin, refundableOnly, departDate, endIata, searchTime, startIata];
    
    NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:formattedBodyXml];
    if(isRoundTrip)
    {
        formattedBodyXml = @"<AirSegmentCriteria><Date>%@</Date><EndIata>%@</EndIata><SearchTime>%@</SearchTime><StartIata>%@</StartIata><TimeIsDeparture>true</TimeIsDeparture><TimeWindow>3</TimeWindow></AirSegmentCriteria>";
        
        departDate = parameterBag[@"ReturnDate"];
        endIata = parameterBag[@"StartIata"];
        searchTime = parameterBag[@"ReturnTime"];
        startIata = parameterBag[@"EndIata"];
        formattedBodyXml = [NSString stringWithFormat:formattedBodyXml, departDate, endIata, searchTime, startIata];
        [bodyXML appendString:formattedBodyXml];
    }
    [bodyXML appendString:@"</Segments>"];
    
    // Gov specific attribute
    NSString *govRateType = parameterBag[@"GovRateTypes"];
    [bodyXML appendString:[NSString stringWithFormat:@"<ShowGovRateTypes>%@</ShowGovRateTypes>", govRateType]];
    [bodyXML appendString:@"</AirCriteria>"];
    formattedBodyXml = bodyXML;

	return formattedBodyXml;
}


-(void) flushData
{
	
}



- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	self.stopChoices = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    self.rateTypeChoices = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	
	isInElement = @"YES";
    self.buildString = [[NSMutableString alloc] initWithString:@""];
	
	if ([elementName isEqualToString:@"StopsGroup"])
	{		
		self.aStops = [[NSMutableArray alloc] initWithObjects: nil];
        self.isInStops = YES;
	}
    else if ([elementName isEqualToString:@"GovtRateType"])
    {
        self.aRateTypes = [[NSMutableArray alloc] initWithObjects: nil];
        self.isInRateTypes = YES;
    }
	else if ([elementName isEqualToString:@"AirlineEntry"])
	{		
		self.airlineEntry = [[AirlineEntry alloc] init];
        self.isInAirlineEntry = YES;
	}
	else if ([elementName isEqualToString:@"VendorCodes"])
	{		
		self.isInVendorCodes = YES;
        self.vendors = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
        self.vendorCode = @"";
        self.vendorName = @"";
	}
    else if ([elementName isEqualToString:@"AirportCityCodes"])
	{		
		self.isInAirportCityCodes = YES;
        self.airportCityCodes = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
        self.cityCode = @"";
        self.cityName = @"";
	}
    else if ([elementName isEqualToString:@"Benchmark"])
	{
        self.benchmark = [[Benchmark alloc] init];
        self.isInBenchmark = YES;
	}
    else if ([elementName isEqualToString:@"AirPair"] && isInAirportCityCodes)
	{		
        self.cityCode = @"";
        self.cityName = @"";
	}

    else if ([elementName isEqualToString:@"PreferenceRankings"])
	{
        self.isInPrefRanking = YES;
        self.prefRankings = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    }
    else if ([elementName isEqualToString:@"AirPair"] && isInPrefRanking)
	{
        self.rankValue = nil;
        self.rankAirlineCode = nil;
    }
//    else if ([elementName isEqualToString:@"Rule"])
//    {
//        self.airRule = (EntityAirRules*)[[AirRuleManager sharedInstance] makeNew];
//    }
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"AirlineEntry"])
	{
        if (aStops != nil)
            [aStops addObject:airlineEntry];
        else if(aRateTypes != nil)
            [aRateTypes addObject:airlineEntry];
        
        self.isInAirlineEntry = NO;
	}
	else if ([elementName isEqualToString:@"StopsGroup"])
	{		
        for(AirlineEntry *airEntry in aStops)
            airEntry.numStops =@([numStops intValue]);
        stopChoices[numStops] = aStops;
        self.isInStops = NO;
	}
    else if ([elementName isEqualToString:@"GovtRateType"])
    {
        for(AirlineEntry *airEntry in aRateTypes)
            airEntry.rateType = rateTypes;
        rateTypeChoices[rateTypes] = aRateTypes;
        self.isInRateTypes = NO;
    }
    else if ([elementName isEqualToString:@"AirportCityCodes"])
	{	
        self.isInAirportCityCodes = NO;
    }
    else if ([elementName isEqualToString:@"Benchmark"])
	{
        self.isInBenchmark = NO;
	}
    else if ([elementName isEqualToString:@"VendorCodes"])
	{		
		self.isInVendorCodes = NO;
    }
    else if ([elementName isEqualToString:@"AirPair"] && isInAirportCityCodes)
	{		
		airportCityCodes[cityCode] = cityName;
    }
    else if ([elementName isEqualToString:@"AirPair"] && isInVendorCodes)
	{		
        //NSLog(@"vendorCode=%@, vendorName=%@", vendorCode, vendorName);
		vendors[vendorCode] = vendorName;
    }
    else if ([elementName isEqualToString:@"PreferenceRankings"])
	{
        self.isInPrefRanking = NO;
    }
    else if ([elementName isEqualToString:@"AirPair"] && isInPrefRanking)
	{
        prefRankings[rankAirlineCode] = rankValue;
    }
//    else if ([elementName isEqualToString:@"Rule"])
//    {
//        airRule.AirFilterSummary = 
//        self.airRule = (EntityAirRules*)[[AirRuleManager sharedInstance] makeNew];
//    }

}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	//NSLog(@"currentElement = %@ string = %@", currentElement, string);
    [buildString appendString:string];
	
	if ([currentElement isEqualToString:@"NumStops"] && isInStops)
	{
		self.numStops = buildString;
	}
    else if ([currentElement isEqualToString:@"RateType"])
    {
        self.rateTypes = buildString;
    }
	else if ([currentElement isEqualToString:@"Airline"] && isInAirlineEntry)
	{
		airlineEntry.airline = buildString;
	}
 	else if ([currentElement isEqualToString:@"Crn"] && isInAirlineEntry)
	{
		airlineEntry.crnCode = buildString;
	}   
	else if ([currentElement isEqualToString:@"LowestCost"] && isInAirlineEntry)
	{
		airlineEntry.lowestCost = @([buildString floatValue]);
	}
    else if ([currentElement isEqualToString:@"NumChoices"] && isInAirlineEntry)
	{
		airlineEntry.numChoices = @([buildString intValue]);
	}
    else if ([currentElement isEqualToString:@"Preference"] && isInAirlineEntry)
	{
		airlineEntry.pref = buildString;
	}
    else if ([currentElement isEqualToString:@"TravelPoints"] && isInAirlineEntry)
	{
		airlineEntry.travelPoints = @([buildString intValue]);
	}
    else if ([currentElement isEqualToString:@"Key"] && isInVendorCodes)
	{
		self.vendorCode = buildString;
	}
    else if ([currentElement isEqualToString:@"Value"] && isInVendorCodes)
	{
		self.vendorName = buildString;
	}
    
    else if ([currentElement isEqualToString:@"Key"] && isInPrefRanking)
	{
		self.rankAirlineCode = buildString;
	}
    else if ([currentElement isEqualToString:@"Value"] && isInPrefRanking)
	{
		self.rankValue = buildString;
	}
    
    else if ([currentElement isEqualToString:@"Key"] && isInAirportCityCodes)
	{
		self.cityCode = buildString;
	}
    else if ([currentElement isEqualToString:@"Value"] && isInAirportCityCodes)
	{
		self.cityName = buildString;
	}
    else if (self.isInBenchmark)
    {
        if ([currentElement isEqualToString:@"Currency"])
        {
            self.benchmark.crnCode = buildString;
        }
        else if ([currentElement isEqualToString:@"Date"])
        {
            self.benchmark.date = [DateTimeFormatter getNSDateFromMWSDateString:buildString];
        }
        else if ([currentElement isEqualToString:@"Destination"])
        {
            self.benchmark.destination = buildString;
        }
        else if ([currentElement isEqualToString:@"Origin"])
        {
            self.benchmark.origin = buildString;
        }
        else if ([currentElement isEqualToString:@"Price"])
        {
            self.benchmark.price = @([buildString floatValue]);
        }
        else if ([currentElement isEqualToString:@"RoundTrip"])
        {
            self.benchmark.roundtrip = @([buildString boolValue]);
        }
    }
    else if ([currentElement isEqualToString:@"PointsAvailableToSpend"])
    {
        self.travelPointsInBank = buildString;
    }
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}



@end
