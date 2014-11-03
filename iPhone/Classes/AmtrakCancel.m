//
//  AmtrakCancel.m
//  ConcurMobile
//
//  Created by charlottef on 1/14/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AmtrakCancel.h"

@implementation AmtrakCancel
@synthesize isSuccess, canceledEntireTrip, errorMessage;

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
	[self flushData];
	[self parseXMLFileAtData:data];
}

-(id)init
{
	self = [super init];
    if (self)
    {
        isSuccess = NO;
        [self flushData];
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return AMTRAK_CANCEL;
}

-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{
	/*
     <CancelCriteria>
     <BookingSource>Worldspan</BookingSource>
     <Reason>Not my color</Reason>
     <RecordLocator>4N4U4J</RecordLocator>
     <SegmentKey>Car_40020114US1_2011-04-11T08:30:00_2011-04-14T12:00:00</SegmentKey>
     <TripId>2857304</TripId>
     </CancelCriteria>*/
	
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
	
	
	NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML,
						bookingSource, reason, recordLocator, segmentKey, tripId];
	return formattedBodyXml;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	self.isSuccess = NO;
    self.path = [NSString stringWithFormat:@"%@/Mobile/Rail/AmtrakCancel",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag Options:SILENT_ERROR];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	
	return msg;
}

-(void) flushData
{
	
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if ([self.currentElement isEqualToString:@"Status"])
	{
		if([string isEqualToString:@"SUCCESS"])
			self.isSuccess = YES;
		else
			self.isSuccess = NO;
	}
	else if ([self.currentElement isEqualToString:@"ErrorMessage"])
	{
        self.errorMessage = string;
	}
}

@end
