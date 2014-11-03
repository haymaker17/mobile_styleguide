//
//  FlightScheduleData.m
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "FlightScheduleData.h"
#import "DataConstants.h"
#import "FSSegment.h"
#import "FSSegmentOption.h"
#import "FSFlight.h"
#import "FSClassOfService.h"

@implementation FlightScheduleData

@synthesize path;
@synthesize segmentOptions;



#define kSchedule 1
#define kSegment 2
#define kSegOption 3
#define kFlight 4



+ (NSMutableDictionary*) getTagMap
{
    static NSMutableDictionary *tagMap;
    
    if (tagMap == nil) // not thread safe
    {
        tagMap = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                  @kSchedule, @"Segment",
                  
                  @kSegment, @"SegmentOption",
                  
                  @kSegOption, @"TravelConfigID",
                  @kSegOption, @"Flight",
                  @kSegOption, @"TotalElapsedTime",
                  
                  @kFlight, @"Carrier",
                  @kFlight, @"FltNum",
                  @kFlight, @"OperatingCarrier",
                  @kFlight, @"DepAirp",
                  @kFlight, @"DepDateTime",
                  @kFlight, @"ArrAirp",
                  @kFlight, @"ArrDateTime",
                  @kFlight, @"NumStops",
                  @kFlight, @"AircraftCode",
                  @kFlight, @"CoS",
                
                  nil];
    }
    
    return tagMap;
}

+ (int)typeForTag:(NSString*)tag
{
    NSNumber *n = (NSNumber*)[self getTagMap][tag];
    return [n intValue];
}


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
{
    [self flushData];
	[self parseXMLFileAtData:data];
}

-(id)init
{
	self = [super init];
    if (self)
    {
        [self flushData];
    }
	return self;
}

-(NSString *)getMsgIdKey
{
	return FLIGHT_SCHEDULE_DATA;
}

-(void) appendOptions:(NSMutableArray*)ary;
{
    if (segments == nil)
        return;

    for (FSSegment *seg in segments)
    {
        [seg appendOptions:ary];
    }
}



-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    path = [NSString stringWithFormat:@"%@/Mobile/Air/GetSchedule",[ExSystem sharedInstance].entitySettings.uri];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
    
    NSString* bodyFormat = @"<AirScheduleRequest><ArrivalIATACode>%@</ArrivalIATACode><CarrierCode>%@</CarrierCode><DepartureIATACode>%@</DepartureIATACode><FlightDate>%@</FlightDate></AirScheduleRequest>";
    
	[msg setBody:[NSString stringWithFormat:bodyFormat,
                  parameterBag[@"ArrivalIATACode"],
                  parameterBag[@"CarrierCode"],
                  parameterBag[@"DepartureIATACode"],
                  parameterBag[@"FlightDate"]
                  ]];
    
    
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	
	return msg;
}

-(void) flushData
{
    inSegment = inSegmentOption = inFlight = inClassOfService = NO;
    segments = [[NSMutableArray alloc] init];
}

- (FSSegment*) getCurrentSegment
{
    return (FSSegment*)[segments lastObject];
}

- (FSSegmentOption*) getCurrentSegmentOption
{
    return [[self getCurrentSegment] getCurrentSegmentOption];
}

-(FSFlight*) getCurrentFlight
{
    return [[self getCurrentSegmentOption] getCurrentFlight];
}

-(FSClassOfService*) getCurrentClassOfService
{
    return [[self getCurrentFlight] getCurrentClassOfService];
}
#pragma mark -
#pragma mark Simplified parser functions

-(void) startTag:(NSString*)tag
{
    if ([tag isEqualToString:@"Segment"])
    {
        [segments addObject:[[FSSegment alloc] init]];
    }
}

-(void)endTag:(NSString*)tag withText:(NSString*)text
{
    // There aren't any tags we care about at this level
}

#pragma mark -
#pragma mark Base parser functions

- (void)parserDidStartDocument:(NSXMLParser *)parser
{
    
}

-(void) parserDidEndDocument:(NSXMLParser *)parser
{
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError
{
    
}

/*
 
 #define kSchedule 1
 #define kSegment 2
 #define kSegOption 3
 #define kFlight 4
 
*/


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    buildString = [[NSMutableString alloc] init];
    
    switch ([FlightScheduleData typeForTag:elementName]) {
        case kSchedule:
            [self startTag:elementName];
            break;
            
        case kSegment:
            [[self getCurrentSegment] startTag:elementName];
            break;
            
        case kSegOption:
            [[self getCurrentSegmentOption] startTag:elementName];
            break;
            
        case kFlight:
            [[self getCurrentFlight] startTag:elementName withAttributeData:attributeDict];
            break;
            
        default:
            break;
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    switch ([FlightScheduleData typeForTag:elementName]) {
        case kSchedule:
            [self endTag:elementName withText:buildString];
            break;
            
        case kSegment:
            [[self getCurrentSegment] endTag:elementName withText:buildString];
            break;
            
        case kSegOption:
            [[self getCurrentSegmentOption] endTag:elementName withText:buildString];
            break;
            
        case kFlight:
            [[self getCurrentFlight] endTag:elementName withText:buildString];
            break;
            
        default:
            break;
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [buildString appendString:string];
}

@end
