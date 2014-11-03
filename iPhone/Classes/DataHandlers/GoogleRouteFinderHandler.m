//
//  GoogleRouteFinderHandler.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/8/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GoogleRouteFinderHandler.h"
#import "FormViewControllerBase.h"

@implementation GoogleRouteFinderHandler
@synthesize didFail, distance, conn, thisData, inEnd, inStart, inDistance, inDuration, dict, mvc, delegate;

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
	return @"GOOGLE_LOCATION_FINDER";
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
//	self.path = [NSString stringWithFormat:@"%@/Mobile/Air/Search",[ExSystem sharedInstance].entitySettings.uri];
//	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
//	[msg setHeader:[ExSystem sharedInstance].sessionID];
//	[msg setContentType:@"application/xml"];
//	[msg setMethod:@"POST"];
//	[msg setBody:[self makeXMLBody:parameterBag]];
//	return msg;
    return nil;
}


-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post

	return nil;
}


-(void) flushData
{
	
}



- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
    self.dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	self.currentElement = elementName;
	
	isInElement = @"YES";
    self.buildString = [[NSMutableString alloc] initWithString:@""];
	
	if ([elementName isEqualToString:@"duration"])
        self.inDuration = YES;
    else if ([elementName isEqualToString:@"distance"] )
        self.inDistance = YES;
    else if ([elementName isEqualToString:@"start_location"] )
        self.inStart = YES;
    else if ([elementName isEqualToString:@"end_location"] )
        self.inEnd = YES;
	
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"duration"])
        self.inDuration = NO;
    else if ([elementName isEqualToString:@"distance"] )
        self.inDistance = NO;
    else if ([elementName isEqualToString:@"start_location"] )
        self.inStart = NO;
    else if ([elementName isEqualToString:@"end_location"] )
        self.inEnd = NO;
    
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [buildString appendString:string];
    
	if ([currentElement isEqualToString:@"value"] && inDuration)
        dict[@"durationValue"] = buildString;
    else if ([currentElement isEqualToString:@"text"] && inDuration)
        dict[@"durationText"] = buildString;
    
    else if ([currentElement isEqualToString:@"value"] && inDistance)
        dict[@"distanceValue"] = buildString;
    else if ([currentElement isEqualToString:@"text"] && inDistance)
        dict[@"distanceText"] = buildString;
    
    else if ([currentElement isEqualToString:@"lat"] && inStart)
        dict[@"startLat"] = buildString;
    else if ([currentElement isEqualToString:@"lng"] && inStart)
        dict[@"startLng"] = buildString;
    
    else if ([currentElement isEqualToString:@"lat"] && inEnd)
        dict[@"endLat"] = buildString;
    else if ([currentElement isEqualToString:@"lng"] && inEnd)
        dict[@"endLng"] = buildString;
    
    else
        dict[currentElement] = buildString;
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}



-(void) makeDirectionRequest:(NSString *)fromLocation toLocation:(NSString *)toLocation
{
    self.mvc = nil;
    
    NSString *escaped_From =  [fromLocation stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
    NSString *escaped_To =  [toLocation stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
	
    // Contact Google and make a route request
    NSString *requestString = [NSString stringWithFormat:@"https://maps.googleapis.com/maps/api/directions/xml?origin=%@&destination=%@&sensor=true", escaped_From, escaped_To];

	self.thisData = [[NSMutableData alloc] initWithCapacity:(13 * 1024)];
	NSURL *requestUrl = [NSURL URLWithString:requestString];
	NSURLRequest *request = [NSURLRequest requestWithURL:requestUrl];
	self.conn = [NSURLConnection connectionWithRequest:request delegate:self];
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
}

#pragma mark  NSURLConnection delegate Methods
-(void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)moreData
{
	[thisData appendData:moreData];
}

-(void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
    
	[self parseXMLFileAtData:thisData];
    
    [delegate handleGoogleLocation:self.dict didFail:NO];
}

-(void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{    
	UIApplication* app = [UIApplication sharedApplication]; 
	app.networkActivityIndicatorVisible = NO;
    self.didFail = YES;
    [delegate handleGoogleLocation:self.dict didFail:YES];
}


@end
