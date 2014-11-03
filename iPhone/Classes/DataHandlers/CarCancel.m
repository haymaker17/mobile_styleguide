//
//  CarCancel.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/16/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "CarCancel.h"

@interface CarCancel()
@property (nonatomic, strong) NSMutableString *buildString;
@end

@implementation CarCancel
@synthesize path, currentElement, items, obj, keys, isSuccess;

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
        isInElement = NO;
        isSuccess = NO;
        currentElement = @"";
        [self flushData];
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return CAR_CANCEL;
}

-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post
	/*
    <CarCancel>
    <BookingSource>Worldspan</BookingSource>
    <Reason>Not my color</Reason>
    <RecordLocator>4N4U4J</RecordLocator>
    <SegmentKey>Car_40020114US1_2011-04-11T08:30:00_2011-04-14T12:00:00</SegmentKey>
    <TripId>2857304</TripId>
    </CarCancel>*/
	
	NSString *bookingSource = parameterBag[@"BookingSource"];
	NSString *reason = parameterBag[@"Reason"];
	NSString *recordLocator = parameterBag[@"RecordLocator"];
	NSString *segmentKey = parameterBag[@"SegmentKey"];
	NSString *tripId = parameterBag[@"TripId"];
	
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<CarCancel>"];
	[bodyXML appendString:@"<BookingSource>%@</BookingSource>"];
	[bodyXML appendString:@"<Reason>%@</Reason>"];
	[bodyXML appendString:@"<RecordLocator>%@</RecordLocator>"];
	[bodyXML appendString:@"<SegmentKey>%@</SegmentKey>"];
	[bodyXML appendString:@"<TripId>%@</TripId>"];
	
	[bodyXML appendString:@"</CarCancel>"];
	
	
	NSString* formattedBodyXml = nil;
	
	formattedBodyXml = [NSString stringWithFormat:bodyXML,
						bookingSource, reason, recordLocator, segmentKey, tripId];
	NSLog(@"%@", formattedBodyXml);
	return formattedBodyXml;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	self.isSuccess = NO;
	self.path = [NSString stringWithFormat:@"%@/Mobile/Car/CancelCar",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	
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
	
	isInElement = YES;
    
    self.buildString = [[NSMutableString alloc] init];	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = NO;
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
    
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [self.buildString appendString:string];
    
	if ([currentElement isEqualToString:@"Status"])
	{
        self.isSuccess = [string isEqualToString:@"SUCCESS"];
	}
    else if ([currentElement isEqualToString:@"ErrorMessage"])
    {
        self.errorMessage = [self.buildString copy];
    }
    
	
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
    
}


@end
