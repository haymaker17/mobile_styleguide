//
//  HotelCancel.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelCancel.h"

@interface HotelCancel()
@property (nonatomic, strong) NSMutableString *buildString;
@end


@implementation HotelCancel

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
	return HOTEL_CANCEL;
}

-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post
	
//	<CancelCriteria>
//	<BookingSource>Worldspan</BookingSource>
//	<Reason>No chocolates on the pillow</Reason>
//	<RecordLocator>MPRPAI</RecordLocator>
//	<SegmentKey>HOTEL_73608896 $DI$_2010-09-01_2010-09-02</SegmentKey>
//	<TripId>3092</TripId>
//	</CancelCriteria>
	
	NSString *bookingSource = parameterBag[@"BookingSource"];
	NSString *reason = parameterBag[@"Reason"];
	NSString *recordLocator = parameterBag[@"RecordLocator"];
	NSString *segmentKey = parameterBag[@"SegmentKey"];
	NSString *tripId = parameterBag[@"TripId"];
	
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<CancelCriteria>"];
	[bodyXML appendString:@"<BookingSource>%@</BookingSource>"];
	[bodyXML appendString:@"<Reason>%@</Reason>"];
	[bodyXML appendString:@"<RecordLocator>%@</RecordLocator>"];
	[bodyXML appendString:@"<SegmentKey>%@</SegmentKey>"];
	[bodyXML appendString:@"<TripId>%@</TripId>"];
	
	[bodyXML appendString:@"</CancelCriteria>"];
	
	
	__autoreleasing NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML,
						bookingSource, reason, recordLocator, segmentKey, tripId];
	
	NSLog(@"%@", formattedBodyXml);
	return formattedBodyXml;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	self.isSuccess = NO;
	self.path = [NSString stringWithFormat:@"%@/Mobile/Hotel/CancelV2",[ExSystem sharedInstance].entitySettings.uri];
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
	//int x = 0;
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	
	isInElement = YES;
    self.buildString = [[NSMutableString alloc] init];
	
//	if ([elementName isEqualToString:@"RailSellResponse"])
//	{		
//		self.obj = [[AmtrakSellData alloc] init];
//		[obj release];
//	}
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = NO;
	
//	if ([elementName isEqualToString:@"RailSellResponse"])
//	{
//		if (obj.tripLocator != nil) 
//		{
//			[items setObject:obj forKey:obj.tripLocator];
//			[keys addObject:obj.tripLocator];
//		}
//	}
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [self.buildString appendString:string];
	//<Status>SUCCESS</Status>
//	//NSLog(@"element = %@, string = %@", currentElement, string);
	if ([currentElement isEqualToString:@"Status"])
	{
		self.isSuccess = [string isEqualToString:@"SUCCESS"];
	}
	else if ([currentElement isEqualToString:@"CancellationNumber"])
	{
		self.cancellationNumber = string;
	}
    else if ([currentElement isEqualToString:@"ErrorMessage"])
    {
        self.errorMessage = [self.buildString copy];
    }
	
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


@end
