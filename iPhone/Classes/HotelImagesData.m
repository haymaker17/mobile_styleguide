//
//  HotelImagesData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelImagesData.h"


@implementation HotelImagesData

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
	return HOTEL_IMAGES;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	NSString *gds = parameterBag[@"GDS"];
	NSString *propertyId = parameterBag[@"PROPERTY_ID"];
	propertyId = [propertyId stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]];
	//NSLog(@"GDS = %@", gds);
	//NSLog(@"propertyId = %@", propertyId);
	self.path = [NSString stringWithFormat:@"%@/Mobile/Hotel/Images/%@/%@",[ExSystem sharedInstance].entitySettings.uri, gds, propertyId];
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
	
	if ([elementName isEqualToString:@"ImagePair"])
	{		
		self.obj = [[HotelImageData alloc] init];
	}
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"ImagePair"])
	{
		items[obj.hotelImage] = obj;
		[keys addObject:obj];
	}
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	//NSLog(@"element = %@", currentElement);
	if ([currentElement isEqualToString:@"Image"])
	{
		[obj setHotelImage:string];
	}
	else if ([currentElement isEqualToString:@"Thumbnail"])
	{
		[obj setHotelThumbnail:string];
	}

}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


@end



