//
//  FindLocation.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FindLocation.h"


@implementation FindLocation
@synthesize address;
@synthesize path;
@synthesize currentElement;
@synthesize currentLocationResult;
@synthesize locationResults, buildString;


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self parseXMLFileAtData:data];
}


- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.address = parameterBag[@"ADDRESS"];
	self.path = [NSString stringWithFormat:@"%@/mobile/Location/Search",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:FIND_LOCATION State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	return msg;
}


-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<LocationCriteria>"];
	[bodyXML appendString:@"<Address>%@</Address>"];
    if(parameterBag[@"AIRPORT_ONLY"] != nil)
        [bodyXML appendString:@"<AirportsOnly>true</AirportsOnly>"];
	[bodyXML appendString:@"</LocationCriteria>"];
	
	NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML, [NSString stringByEncodingXmlEntities:address]];
	
	
	return formattedBodyXml;
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	self.locationResults = [[NSMutableArray alloc] initWithObjects:nil];	// Retain count = 2
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	NSString * errorString = [NSString stringWithFormat:@"Unable to get location search results (Error code %i )", [parseError code]];
	NSLog(@"error parsing XML: %@", errorString);
	//TODO: handle error
	
	//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error loading content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	//	[errorAlert show];
	//	[errorAlert release];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	self.currentElement = elementName;
    self.buildString = [[NSMutableString alloc] initWithString:@""];
    
	if ([elementName isEqualToString:@"LocationChoice"])
	{
		self.currentLocationResult = [[LocationResult alloc] init];	// Retain count = 2
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if (currentLocationResult == nil)
		return;
	
	if ([elementName isEqualToString:@"LocationChoice"])
		[locationResults addObject:currentLocationResult];
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if (currentLocationResult == nil)
		return;
	
    [buildString appendString:string];
	if ([currentElement isEqualToString:@"Lat"])
	{
		currentLocationResult.latitude = buildString;
	}
	else if ([currentElement isEqualToString:@"Lon"])
	{
		currentLocationResult.longitude = buildString;
	}
	else if ([currentElement isEqualToString:@"Location"])
	{
		currentLocationResult.location = buildString;	// TODO: Use StringBuilder and handle escape sequences?
	}
    else if ([currentElement isEqualToString:@"CountryAbbrev"])
    {
        currentLocationResult.countryAbbrev = buildString; // MOB-9637 Flex Faring
    }
    else if ([currentElement isEqualToString:@"City"])
    {
        currentLocationResult.city = buildString;
    }
    else if ([currentElement isEqualToString:@"State"])
    {
        currentLocationResult.state = buildString;
    }
    else if ([currentElement isEqualToString:@"Zip"])
    {
        currentLocationResult.zipCode = buildString;
    }
    else if ([currentElement isEqualToString:@"Iata"])
    {
        currentLocationResult.iataCode = buildString;
    }
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}



@end

