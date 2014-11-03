//
//  AirCancel.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/16/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AirCancel.h"

@implementation AirCancel
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
	return AIR_CANCEL;
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

	NSString *reason = parameterBag[@"Reason"];
	NSString *recordLocator = parameterBag[@"RecordLocator"];
	
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<AirCancelRequest>"];
	[bodyXML appendString:@"<Comment>%@</Comment>"];
	[bodyXML appendString:@"<RecordLocator>%@</RecordLocator>"];
	[bodyXML appendString:@"</AirCancelRequest>"];
	
	
	NSString* formattedBodyXml = nil;
	
	formattedBodyXml = [NSString stringWithFormat:bodyXML,
						reason, recordLocator];
	
	NSLog(@"%@", formattedBodyXml);
	return formattedBodyXml;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	self.isSuccess = NO;
	self.path = [NSString stringWithFormat:@"%@/Mobile/Air/Cancel",[ExSystem sharedInstance].entitySettings.uri];
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
	if ([currentElement isEqualToString:@"Status"])
	{
		if([string isEqualToString:@"SUCCESS"])
			self.isSuccess = YES;
		else 
			self.isSuccess = NO;
	}

	
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{

}


@end
