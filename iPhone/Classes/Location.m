//
//  Location.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 2/1/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "Location.h"

@implementation Location

@synthesize dataParser;
@synthesize currentElement;
@synthesize status;
@synthesize parsedResultNode;
@synthesize addressComponents;
@synthesize currentAddressComponent;
@synthesize currentType;
@synthesize typeDictionary;
@synthesize parsingLocationNode;
@synthesize parsingViewportNode;
@synthesize parsingSouthwestNode;
@synthesize parsingNortheastNode;
@synthesize streetAddress;
@synthesize city;
@synthesize state;
@synthesize country;
@synthesize zip;
@synthesize location;
@synthesize viewport;
@synthesize stateAbbrev, countryAbbrev;
@synthesize buildString;

- (id) init
{
    self = [super init];
    if (self)
    {
        self.addressComponents = [[NSMutableArray alloc] init];
        self.typeDictionary = [[NSMutableDictionary alloc] init];
        self.location = [[Coordinate alloc] init];
        self.viewport = [[BoundingBox alloc] init];
    }
	return self;
}

- (void)parseXMLFileAtURL:(NSString *)URL 
{	
	NSURL *xmlURL = [NSURL URLWithString:URL];
	self.dataParser = [[NSXMLParser alloc] initWithContentsOfURL:xmlURL];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}

- (void)parseXMLFileAtData:(NSData *)webData 
{	
	self.dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	//NSString * errorString = [NSString stringWithFormat:@"Location:parser:parseErrorOccurred: (Error code %i )", [parseError code]];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	// If we already parsed a result, then we're not interested in parsing anymore.
	// (The first result is always the best.)
//	if (self.parsedResultNode)
//	{
//		return;
//	}
	
	self.currentElement = elementName;
    self.buildString = [[NSMutableString alloc] init];

	if ([elementName isEqualToString:@"address_component"])
	{
		self.currentAddressComponent = [[AddressComponent alloc] init];
	}
	else if ([elementName isEqualToString:@"type"])
	{
		self.currentType = nil;
	}
	else if ([elementName isEqualToString:@"location"])
	{
		self.parsingLocationNode = YES;
	}
	else if ([elementName isEqualToString:@"viewport"])
	{
		self.parsingViewportNode = YES;
	}
	else if ([elementName isEqualToString:@"southwest"])
	{
		self.parsingSouthwestNode = YES;
	}
	else if ([elementName isEqualToString:@"northeast"])
	{
		self.parsingNortheastNode = YES;
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	self.currentElement = nil;
	
	if ([elementName isEqualToString:@"result"])
	{
		self.parsedResultNode = YES;
	}
	else if ([elementName isEqualToString:@"address_component"])
	{
		if (currentAddressComponent != nil)
		{
			[addressComponents addObject:currentAddressComponent];
		}
		currentAddressComponent = nil;
	}
	else if ([elementName isEqualToString:@"type"])
	{
		if (currentAddressComponent != nil && currentType != nil)
		{
			[currentAddressComponent.types addObject:currentType];
		}
		self.currentType = nil;
	}
	else if ([elementName isEqualToString:@"location"])
	{
		self.parsingLocationNode = NO;
	}
	else if ([elementName isEqualToString:@"viewport"])
	{
		self.parsingViewportNode = NO;
	}
	else if ([elementName isEqualToString:@"southwest"])
	{
		self.parsingSouthwestNode = NO;
	}
	else if ([elementName isEqualToString:@"northeast"])
	{
		self.parsingNortheastNode = NO;
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if (string == nil)
		return;
	
    [self.buildString appendString:string];
	if ([currentElement isEqualToString:@"status"]) 
	{
		self.status = string;
	}
	else if (currentAddressComponent != nil)
	{
		if ([currentElement isEqualToString:@"formatted_address"])
		{
			currentAddressComponent.formattedAddress = [NSString stringWithString: buildString];// strip \n at the end of buildString
		}
		else if ([currentElement isEqualToString:@"short_name"])
		{
			currentAddressComponent.shortName = string;
		}
		else if ([currentElement isEqualToString:@"long_name"])
		{
            // MOB-8466 Make sure all part of the location name is included
			currentAddressComponent.longName = [NSString stringWithString: buildString]; // strip \n at the end of buildString
		}
		else if ([currentElement isEqualToString:@"type"])
		{
			self.currentType = string;
		}
	}
	else if ([currentElement isEqualToString:@"lat"])
	{
		if (parsingLocationNode)
		{
			self.location.latitude = string;
            //NSLog(@"self.location.latitude %@", string);
		}
		else if (parsingViewportNode)
		{
			if (parsingSouthwestNode)
			{
				self.viewport.southwest.latitude = string;
			}
			else if (parsingNortheastNode)
			{
				self.viewport.northeast.latitude = string;
			}
		}
	}
	else if ([currentElement isEqualToString:@"lng"])
	{
		if (parsingLocationNode)
		{
			self.location.longitude = string;
            //NSLog(@"self.location.longitude %@", string);
		}
		else if (parsingViewportNode)
		{
			if (parsingSouthwestNode)
			{
				self.viewport.southwest.longitude = string;
			}
			else if (parsingNortheastNode)
			{
				self.viewport.northeast.longitude = string;
			}
		}
	}
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	// Populate the types dictionary.  The key is the type, the value is the address component.
	// Since it is possible for multiple address components to have the same type, traverse the
	// list of address components backward, override the dictionary entry for a given type each
	// time it is encountered.  Since the traversal is backward, the dictionary entry for a given
	// type will be the first address component in the list with that type.
	//
	int numAddressComponents = (int)[addressComponents count];
	for (int i = numAddressComponents - 1; i >= 0; i--)
	{
		AddressComponent *comp = addressComponents[i];
		for (NSString *compType in comp.types)
		{
			typeDictionary[compType] = comp;
		}
	}
	
	AddressComponent *streeetAddressComponent = [self getAddressComponentOfType:@"street_address"];
	AddressComponent *streetNumberComponent = [self getAddressComponentOfType:@"street_number"];
	AddressComponent *routeComponent = [self getAddressComponentOfType:@"route"];
	AddressComponent *cityComponent = [self getAddressComponentOfType:@"locality"];
	AddressComponent *stateComponent = [self getAddressComponentOfType:@"administrative_area_level_1"];
	AddressComponent *countryComponent = [self getAddressComponentOfType:@"country"];
	AddressComponent *zipComponent = [self getAddressComponentOfType:@"postal_code"];
	
	if (streeetAddressComponent != nil && streeetAddressComponent.formattedAddress != nil)
		self.streetAddress = streeetAddressComponent.formattedAddress;

    if (self.streetAddress == nil && streetNumberComponent != nil && routeComponent != nil)
        self.streetAddress = [NSString stringWithFormat:@"%@ %@", streetNumberComponent.shortName, routeComponent.shortName];
    
	if (cityComponent != nil && cityComponent.longName != nil)
		self.city = cityComponent.longName;
	
	if (countryComponent != nil && countryComponent.longName != nil)
	{
        self.country = countryComponent.longName;
        self.countryAbbrev = countryComponent.shortName;
    }
    
	if (stateComponent != nil && stateComponent.longName != nil && ([self.country isEqualToString:@"United States"] || [self.country isEqualToString:@"Canada"]))
	{
        self.state = stateComponent.longName;
        self.stateAbbrev = stateComponent.shortName;
    }
    
	if (zipComponent != nil && zipComponent.longName != nil)
		self.zip = zipComponent.longName;
}

-(AddressComponent*) getAddressComponentOfType:(NSString*)type
{
	return typeDictionary[type];
}


@end



@implementation AddressComponent

@synthesize formattedAddress;
@synthesize longName;
@synthesize shortName;
@synthesize types;

-(id) init
{
    self = [super init];
    if (self)
    {
        self.types = [[NSMutableArray alloc] initWithObjects: nil];
    }
	return self;
}

@end



@implementation Coordinate

@synthesize latitude;
@synthesize longitude;

@end




@implementation BoundingBox

@synthesize southwest;
@synthesize northeast;

-(id) init
{
    self = [super init];
    if (self)
    {
        self.southwest = [[Coordinate alloc] init];
        self.northeast = [[Coordinate alloc] init];
    }
	return self;
}

@end


