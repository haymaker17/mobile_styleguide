//
//  Trips.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/14/09.
//  Copyright 2009 Concur. All rights reserved.
//
//  Updated by Pavan: 11/02/2012
//  Added/updated new code for enabling offers with coredata
//  Cleanup dead code.
//

#import "TripsData.h"
#import "ExSystem.h"
#import "EntitySegmentLocation.h"

@interface TripsData()
@property (nonatomic) BOOL forApprover;
@property (nonatomic) BOOL inRuleViolations;
@end

@implementation TripsData

@synthesize path;

@synthesize trips;
//@synthesize link;
//@synthesize offerLocation;
//@synthesize offerTimeRange;
@synthesize inMultiWebLink;
@synthesize keys;
@synthesize inSegment;
@synthesize inBooking;
@synthesize inAirlineTicket;
@synthesize inActions;
@synthesize trip, booking, segment, flightStats;
@synthesize currentElement, errorCode, errorInfo, buildString;
@synthesize inFlightStats, offer, inOffer, offerLocation, offerTimeRange, offerOverlay;
//@synthesize allowAddBooking;
@synthesize managedObjectContext=__managedObjectContext;
//@synthesize booking;
//@synthesize segment;



//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	//NSLog(@"TripsData::parseXMLFileAtData");
	dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	
//	NSString *s = [[NSString alloc] initWithData:webData encoding:NSStringEncodingConversionExternalRepresentation];
//	NSLog(@"webData = %@", s);
//	[s release];
	
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	////NSLog(@"TripsData::respondToXMLData");	
//    [[TripManager sharedInstance] setContext:[self managedObjectContext]];
    if (!self.forApprover) {
        if (!trip) {
            [self flushData];
        }
        else
        {
            NSFetchRequest *request = [[NSFetchRequest alloc] init];
            NSPredicate *predicate = [NSPredicate predicateWithFormat:@"itinLocator == %@", trip.itinLocator];
            [request setEntity:[NSEntityDescription entityForName:@"EntityTrip" inManagedObjectContext:[self managedObjectContext]]];
            [request setPredicate:predicate];
            
            NSError *error = nil;
            NSArray *results = [[self managedObjectContext] executeFetchRequest:request error:&error];
            [self createNewTripFromOld:[results lastObject]];
        }
    }
	[self parseXMLFileAtData:data];
}

-(void)createNewTripFromOld:(EntityTrip *)oldTrip
{
    self.trip = [TripManager makeNew:[self managedObjectContext]];
    self.trip.approvalStatus = oldTrip.approvalStatus;
    self.trip.approverId = oldTrip.approverId;
    self.trip.approverName = oldTrip.approverName;
    self.trip.cliqbookState = oldTrip.cliqbookState;
    self.trip.tripEndDateLocal = oldTrip.tripEndDateLocal;
    self.trip.tripEndDateUtc = oldTrip.tripEndDateUtc;
    self.trip.isWithdrawn = oldTrip.isWithdrawn;
    self.trip.itinLocator = oldTrip.itinLocator;
    //self.trip.recordLocator = oldTrip.recordLocator; -- Record Locator in Open Booking may be nil while TripSummary returns 'Manual_0'
    self.trip.tripStartDateLocal = oldTrip.tripStartDateLocal;
    self.trip.tripStartDateUtc = oldTrip.tripStartDateUtc;
    self.trip.tripName = oldTrip.tripName;
    self.trip.tripStateMessages = oldTrip.tripStateMessages;
    self.trip.tripStatus = oldTrip.tripStatus;
    [[TripManager sharedInstance] deleteObj:oldTrip withContext:[self managedObjectContext]];
    
    // MOB-13307
    [[OfferManager sharedInstance] deleteAllOffers:[self managedObjectContext]];
}

-(id)init
{
	self = [super init];
    if (self)
    {
        isInElement = @"NO";
        currentElement = @"";
        trips = [[NSMutableDictionary alloc] init];
        keys = [[NSMutableArray alloc] init];
        self.inBooking = @"NO";
        self.inSegment = @"NO";
        self.inActions = @"NO";
        self.inAirlineTicket = @"NO";
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return TRIPS_DATA;
}

-(BOOL) shouldParseCachedData
{
    return NO;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    if (parameterBag[@"TRIP"] != nil || parameterBag[@"ITIN_LOCATOR"])
    {
        return [self createSingleItinMsg:parameterBag];
    }
	
    self.path = [NSString stringWithFormat:@"%@/Mobile/Itinerary/GetUserTripListV2/",[ExSystem sharedInstance].entitySettings.uri];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}



-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag forApprover:(BOOL)isForApprover
{//knows how to make a post
	
    // <TripSpecifier>
    //  <CompanyId>0</CompanyId>  // optional, defaults to logged in user's company
    //  <TripId>0</TripId>        // required, contains the itin locator value.
    //  <UserId>0</UserId>        // optional, defaults to logged in user's id
    // </TripSpecifier>
    NSString *itinLoc = parameterBag[@"ITIN_LOCATOR"];
    NSString *userId = parameterBag[@"USER_ID"];
    NSString *companyId = parameterBag[@"COMPANY_ID"];
    if (!itinLoc) {
        trip = parameterBag[@"TRIP"];
        itinLoc = trip.itinLocator;
    }
    else
    {
        trip = [[TripManager sharedInstance]fetchByItinLocator:itinLoc];
    }
    NSMutableString *xmlBody = [[NSMutableString alloc] initWithString:@"<TripSpecifier>"];
    if (companyId) [xmlBody appendFormat:@"<CompanyId>%@</CompanyId>",companyId];
    if (isForApprover) {
        [xmlBody appendFormat:@"<ForApprover>true</ForApprover><ItinLocator>%@</ItinLocator>",itinLoc];
    } else {
        NSString *trimmedItinLoc = [itinLoc stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];

        [xmlBody appendFormat:@"<TripId>%@</TripId>",trimmedItinLoc];
    }

    if (userId) {
        [xmlBody appendFormat:@"<UserId>%@</UserId>",userId];
    }

    [xmlBody appendFormat:@"</TripSpecifier>"];
    return xmlBody;
}

-(Msg *) createSingleItinMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.forApprover = [parameterBag[@"ForApprover"] boolValue];
	self.path = [NSString stringWithFormat:(self.forApprover ?  @"%@/Mobile/SingleItineraryV2" : @"%@/Mobile/SingleItinerary"),[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag forApprover:self.forApprover]];
	return msg;
}

-(void) flushData
{
	// MOB-1989
	self.trips = [[NSMutableDictionary alloc] init];
	self.keys = [[NSMutableArray alloc] init];

    // Need to uncomment this.  This might have caused timing related failure.
    [TripManager deleteAllWithContext:[self managedObjectContext]];
    [[OfferManager sharedInstance] deleteAllOffers:[self managedObjectContext]];
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{

}

-(void) parserDidEndDocument:(NSXMLParser *)parser
{
    [self saveContext];
    __managedObjectContext = nil;
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{

}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{

	self.currentElement = elementName;

	isInElement = @"YES";
	
	self.buildString = [[NSMutableString alloc] init];
	
	if ([elementName isEqualToString:@"TripListItinerary"] || ([elementName isEqualToString:@"Itinerary"] && self.trip==nil))
    {//alloc the trip instance
		self.trip = [TripManager makeNew:[self managedObjectContext]];
	}
    else if ([currentElement isEqualToString:@"UsedInExpense"])
    {
        trip.isExpensed = @YES;
    }
    else if ([currentElement isEqualToString:@"RuleViolations"])
    {
        self.inRuleViolations = YES;
    }
    else if (self.inRuleViolations && self.violation == nil)
    {
        self.violation = [TripManager makeNewViolation:self.trip manContext:[self managedObjectContext]];
        self.violation.type = currentElement;
    }
    else if ([currentElement isEqualToString:@"Actions"])
    {
        inActions = @"YES";
    }
	else if ([currentElement isEqualToString:@"Booking"])
	{
		inBooking = @"YES";
        self.booking = [TripManager makeNewBooking:self.trip manContext:[self managedObjectContext]];
	}
	else if ([currentElement isEqualToString:@"AirlineTicket"])
	{
		inAirlineTicket = @"YES";
	}
	else if ([currentElement isEqualToString:@"Segment"] && [inBooking isEqualToString:@"YES"] && !self.inOffer)
	{
		inSegment = @"YES";
		self.segment = [TripManager makeNewSegment:self.trip manContext:[self managedObjectContext]];
        segment.relBooking = self.booking;
	}
	else if ([currentElement isEqualToString:@"Air"] && [inBooking isEqualToString:@"YES"])
	{
		inSegment = @"YES";
		[booking setType:@"AIR"];
		[booking setLabel:@"Air Recloc"];
//		[booking addSegment];
		
		//force a flight stat
		//[self forceFeedFlightStats];
	}
	else if ([currentElement isEqualToString:@"Car"] && [inBooking isEqualToString:@"YES"])
	{
		inSegment = @"YES";
		[booking setType:@"CAR"];
		[booking setLabel:@"Car Confirm"];
//		[trip.booking addSegment];
	}
	else if ([currentElement isEqualToString:@"Hotel"] && [inBooking isEqualToString:@"YES"])
	{
		inSegment = @"YES";
		[booking setType:@"HOTEL"];
		[booking setLabel:@"Hotel Confirm"];
//		[trip.booking addSegment];
	}
	else if ([currentElement isEqualToString:@"Dining"] && [inBooking isEqualToString:@"YES"])
	{
		inSegment = @"YES";
		[booking setType:@"DINING"];
		[booking setLabel:@"Dining Confirm"];
//		[trip.booking addSegment];
	}
	else if ([currentElement isEqualToString:@"Ride"] && [inBooking isEqualToString:@"YES"])
	{
		inSegment = @"YES";
		[booking setType:@"RIDE"];
		[booking setLabel:@"Ride Confirm"];
//		[trip.booking addSegment];
	}
	else if ([currentElement isEqualToString:@"Undefined"] && [inBooking isEqualToString:@"YES"])
	{
		inSegment = @"YES";
		[booking setType:@"UNDEFINED"];
		[booking setLabel:@"Undefined Confirm"];
//		[trip.booking addSegment];
	}
	else if ([currentElement isEqualToString:@"Event"] && [inBooking isEqualToString:@"YES"])
	{
		inSegment = @"YES";
		[booking setType:@"EVENT"];
		[booking setLabel:@"Event Confirm"];
//		[trip.booking addSegment];
	}
	else if ([currentElement isEqualToString:@"Rail"] && [inBooking isEqualToString:@"YES"])
	{
		inSegment = @"YES";
		[booking setType:@"RAIL"];
		[booking setLabel:@"Rail Confirm"];
//		[trip.booking addSegment];
	}
	else if ([currentElement isEqualToString:@"Parking"] && [inBooking isEqualToString:@"YES"])
	{
		inSegment = @"YES";
		[booking setType:@"PARKING"];
		[booking setLabel:@"Parking Confirm"];
//		[trip.booking addSegment];
	}
	else if ([currentElement isEqualToString:@"FlightStatus"] && [inSegment isEqualToString:@"YES"])
	{
		self.flightStats = [TripManager makeNewFlightStat:segment manContext:[self managedObjectContext]];
		inFlightStats = YES;
		//[self forceFeedFlightStats];
	}
    else if ([currentElement isEqualToString:@"Offer"])
    {
        self.inOffer = YES;
        
        self.offer = [[OfferManager sharedInstance] makeNew:[self managedObjectContext]];
    }
    else if (self.inOffer && [currentElement isEqualToString:@"Segment"])
    {
        if(attributeDict[@"BookingSource"] != nil)
        {
            offer.bookingSource = attributeDict[@"BookingSource"];
            offer.recordLocator = attributeDict[@"RecordLocator"];
            offer.segmentSide = attributeDict[@"SegmentSide"];
            offer.segmentKey =  attributeDict[@"SegmentKey"];

        }
    }
    else if (self.inOffer && [currentElement isEqualToString:@"Links"])
    {
    	//TODO: Handle multi links
        inMultiWebLink = YES;
        //offer.links = [[NSMutableArray alloc] init];
    }
    else if (self.inOffer && [currentElement isEqualToString:@"Location"])
    {
        self.offerLocation = [[OfferManager sharedInstance] makeNewOfferLocation:self.offer ];
        
    }
    else if (self.inOffer && [currentElement isEqualToString:@"TimeRange"])
    {
        self.offerTimeRange = [[OfferManager sharedInstance] makeNewOfferTimeRange:self.offer ];
        
    }
    else if (self.inOffer && [currentElement isEqualToString:@"OverlayList"])
    {
        self.offerOverlay = [[OfferManager sharedInstance] makeNewOfferOverlay:self.offer];
    }
}

-(void) forceFeedFlightStats
{
	
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    isInElement = @"NO";
	
	if ([elementName isEqualToString:@"TripListItinerary"])
	{
		if (self.trip.itinLocator != nil)
		{
            [TripManager saveItWithContext:self.trip manContext:[self managedObjectContext]];
		}
        self.trip = nil;
	}
    else if ([elementName isEqualToString:@"TripStateMessage"])
    {
        self.tripStateMessage = self.tripStateMessage==nil ? buildString : [self.tripStateMessage stringByAppendingFormat:@".\n%@",buildString];
    }
    else if ([elementName isEqualToString:@"TripStateMessages"])
    {
        self.trip.tripStateMessages = self.tripStateMessage;
        self.tripStateMessage = nil;
    }
    else if (self.inRuleViolations)
    {
        if ([elementName isEqualToString:@"RuleViolations"])
        {
            self.inRuleViolations = NO;
        }
        else if ([self.violation.type isEqualToString:elementName])
        {
            [TripManager saveItWithContext:self.violation manContext:[self managedObjectContext]];
            self.violation = nil;
        }
        else if ([elementName isEqualToString:@"Rule"])
        {
            NSString *ruleText = [buildString stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
            self.violation.rule = self.violation.rule==nil ? ruleText : [self.violation.rule stringByAppendingFormat:@"\n%@",ruleText];
        }
        else if ([elementName isEqualToString:@"Reason"])
        {
            self.violation.reason = [buildString copy];
        }
        else if ([elementName isEqualToString:@"Comments"])
        {
            self.violation.comments = [buildString copy];
        }
        else if ([elementName isEqualToString:@"Refundable"])
        {
            self.violation.costAdditionalInfo = [buildString copy];
        }
        else if ([elementName isEqualToString:@"Cost"] || [elementName isEqualToString:@"Rate"] || [elementName isEqualToString:@"DailyRate"])
        {
            self.violation.cost = [buildString copy];
        }
    }
	else if ([elementName isEqualToString:@"Itinerary"])
	{
		if (trip.tripKey != nil)
		{
            self.trip.isItinLoaded = @YES;
            [TripManager saveItWithContext:self.trip manContext:[self managedObjectContext]];
		}

        self.trip = nil;
	}
    else if ([currentElement isEqualToString:@"Actions"])
    {
        inActions = @"NO";
    }
	else if ([elementName isEqualToString:@"Booking"])
	{
		self.inBooking =  @"NO";
		[TripManager saveItWithContext:self.booking manContext:[self managedObjectContext]];
        self.booking = nil;
	}
	else if ([elementName isEqualToString:@"AirlineTicket"])
	{
		self.inAirlineTicket =  @"NO";
	}
	else if ([elementName isEqualToString:@"Segment"] && !self.inOffer)
	{
		self.inSegment =  @"NO";
        segment.relBooking = booking;
		[TripManager saveItWithContext:self.segment manContext:[self managedObjectContext]];
        self.segment = nil;
	}
	else if ([elementName isEqualToString:@"Air"])
	{
		self.inSegment =  @"NO";
        [TripManager saveItWithContext:self.segment manContext:[self managedObjectContext]];
        self.segment = nil;
	}
	else if ([elementName isEqualToString:@"Car"])
	{
		self.inSegment =  @"NO";
        [TripManager saveItWithContext:self.segment manContext:[self managedObjectContext]];
        self.segment = nil;
	}
	else if ([elementName isEqualToString:@"Hotel"])
	{
		self.inSegment =  @"NO";
        [TripManager saveItWithContext:self.segment manContext:[self managedObjectContext]];
        self.segment = nil;
	}
	else if ([elementName isEqualToString:@"Dining"])
	{
		self.inSegment =  @"NO";
		//		[TripManager saveItWithContext:self.segment manContext:[self managedObjectContext]];
//        self.segment = nil;
	}
	else if ([elementName isEqualToString:@"Ride"])
	{
		self.inSegment =  @"NO";
        [TripManager saveItWithContext:self.segment manContext:[self managedObjectContext]];
        self.segment = nil;
	}
	else if ([elementName isEqualToString:@"Undefined"] )
	{
		self.inSegment =  @"NO";
		//		[TripManager saveItWithContext:self.segment manContext:[self managedObjectContext]];
//        self.segment = nil;
	}
	else if ([elementName isEqualToString:@"Event"])
	{
		self.inSegment =  @"NO";
		//		[TripManager saveItWithContext:self.segment manContext:[self managedObjectContext]];
//        self.segment = nil;
	}
	else if ([elementName isEqualToString:@"Rail"] )
	{
		self.inSegment =  @"NO";
        [TripManager saveItWithContext:self.segment manContext:[self managedObjectContext]];
        self.segment = nil;
	}
	else if ([elementName isEqualToString:@"Parking"])
	{
		self.inSegment =  @"NO";
        [TripManager saveItWithContext:self.segment manContext:[self managedObjectContext]];
        self.segment = nil;
	}
	else if ([elementName isEqualToString:@"FlightStatus"])
	{
		inFlightStats = NO;
        [TripManager saveItWithContext:self.flightStats manContext:[self managedObjectContext]];
        self.flightStats = nil;
	}
    
    if ([elementName isEqualToString:@"Offer"])
    {
        self.inOffer = NO;
        [[OfferManager sharedInstance] saveItWithContext:[self managedObjectContext]];
        self.offer = nil;
    }
    else if (self.inOffer && [elementName isEqualToString:@"Links"])
    {
        inMultiWebLink = NO;
    }
    else if (self.inOffer && inMultiWebLink && [elementName isEqualToString:@"Link"])
    {
// TODO: Check if multi link is needed
//        [offer.links addObject:link];
//        self.link = nil;
    }
//    else if (self.inOffer && [elementName isEqualToString:@"LocalProximity"])
//    {
//        [offer.validityDetails.offerLocations addObject:offerLocation];
//        self.offerLocation = nil;
//    }
    else if (self.inOffer && [elementName isEqualToString:@"TimeRange"])
    {
        [[OfferManager sharedInstance]saveItWithContext:[self managedObjectContext]];
        self.offerTimeRange = nil;
      
    }
    else if (self.inOffer && [elementName isEqualToString:@"Location"])
    {
        [[OfferManager sharedInstance]saveItWithContext:[self managedObjectContext]];
        self.offerTimeRange = nil;
     
    }
    else if (self.inOffer && [elementName isEqualToString:@"Overlay"])
    {
        [[OfferManager sharedInstance]saveItWithContext:[self managedObjectContext]];
        self.offerOverlay = nil;
    }
}

- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}

/*- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"TripListItinerary"])
	{
		if (self.trip.itinLocator != nil)
		{
            [TripManager saveItWithContext:self.trip manContext:[self managedObjectContext]];
		}
        self.trip = nil;
	}
    else if ([elementName isEqualToString:@"TripStateMessage"])
    {
        self.tripStateMessage = self.tripStateMessage==nil ? buildString : [self.tripStateMessage stringByAppendingFormat:@". %@",buildString];
    }
    else if ([elementName isEqualToString:@"TripStateMessages"])
    {
        self.trip.tripStateMessages = self.tripStateMessage;
        self.tripStateMessage = nil;
    }
}*/

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    NSString *myString = [string stringByReplacingOccurrencesOfString:@"\n" withString:@""];
	
	[buildString appendString:string];
	
	if ([isInElement isEqual:@"NO"])
		return;
    
	if ([myString isEqualToString:@""] || string == nil)
		return;
    
    if([currentElement isEqualToString:@"ApprovalStatus"])
    {
        self.trip.approvalStatus = buildString;
    }
    else if([currentElement isEqualToString:@"ApproverId"])
    {
        self.trip.approverId = buildString;
    }
    else if([currentElement isEqualToString:@"ApproverName"])
    {
        self.trip.approverName = buildString;
    }
    else if([currentElement isEqualToString:@"CliqbookState"])
    {
        self.trip.cliqbookState = buildString;
    }
    else if ([currentElement isEqualToString:@"EndDateLocal"] & [self.inBooking isEqualToString:@"NO"])
	{
		self.trip.tripEndDateLocal = [DateTimeFormatter getNSDateFromMWSDateString:buildString];// [DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"];
	}
    else if ([currentElement isEqualToString:@"EndDateUtc"] & [self.inBooking isEqualToString:@"NO"])
	{
		self.trip.tripEndDateUtc = [DateTimeFormatter getNSDateFromMWSDateString:buildString];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"];
	}
    else if([currentElement isEqualToString:@"IsWithdrawn"])
    {
        self.trip.isWithdrawn = @([buildString boolValue]);
    }
    else if([currentElement isEqualToString:@"ItinLocator"])
    {
        self.trip.tripKey = buildString;
        self.trip.itinLocator = buildString;
    }
    else if([currentElement isEqualToString:@"RecordLocator"] && [self.inBooking isEqualToString:@"NO"])
    {
        self.trip.recordLocator = buildString;
    }
    else if ([currentElement isEqualToString:@"StartDateLocal"] && [self.inBooking isEqualToString:@"NO"])
	{
		self.trip.tripStartDateLocal = [DateTimeFormatter getNSDateFromMWSDateString:buildString];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"];
	}
    else if ([currentElement isEqualToString:@"StartDateUtc"] && [self.inBooking isEqualToString:@"NO"])
	{
		self.trip.tripStartDateUtc = [DateTimeFormatter getNSDateFromMWSDateString:buildString];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"];
	}
    else if ([currentElement isEqualToString:@"TripName"])
	{
		self.trip.tripName = buildString;
	}
    else if ([currentElement isEqualToString:@"TripStatus"])
	{
		self.trip.tripStatus = buildString;
	}
//}

//- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
//{
//	NSString *myString = [string stringByReplacingOccurrencesOfString:@"\n" withString:@""];
//
//	[buildString appendString:string];
//
//	if ([isInElement isEqual:@"NO"])
//		return;
//
//	if ([myString isEqualToString:@""] || string == nil)
//		return;
	
	else if ([currentElement isEqualToString:@"ItinID"])
	{
		//[trip setItinID:buildString];
	}
	else if ([currentElement isEqualToString:@"ItinSourceName"])
	{
		[trip setItinSourceName:buildString];
	}
	else if ([currentElement isEqualToString:@"ItinLocator"])
	{
		[trip setTripKey:buildString]; //the identifier for this trip/itin
        trip.itinLocator = buildString;
	}
    else if ([currentElement isEqualToString:@"AuthorizationNumber"])
    {
        [trip setAuthNum:buildString];
    }
	else if ([currentElement isEqualToString:@"CliqbookTripId"])
	{
		[trip setCliqbookTripId:buildString];
	}
	else if ([currentElement isEqualToString:@"ClientLocator"])
	{
		[trip setClientLocator:buildString];
	}
	else if ([currentElement isEqualToString:@"Description"])
    {
        [trip setTripDescription:buildString];
    }
	else if ([currentElement isEqualToString:@"TripName"])
	{
		//[trip setTripName:@"Berlin Hbf. to München Hbf"];
		//NSLog(@"tripName = %@", string);
		
		//[oope setLocationName:buildString]; 
		[trip setTripName:buildString];
	}
	else if ([currentElement isEqualToString:@"State"])
	{
//        if ([buildString length])
//            [trip.state ];
	}
    else if ([currentElement isEqualToString:@"AllowAddAir"])
    {
        trip.allowAddAir = @([buildString boolValue]);
    }
    else if ([currentElement isEqualToString:@"AllowAddCar"])
    {
        trip.allowAddCar = @([buildString boolValue]);
    }
    else if ([currentElement isEqualToString:@"AllowAddHotel"])
    {
        trip.allowAddHotel = @([buildString boolValue]);
    }
    else if ([currentElement isEqualToString:@"AllowAddRail"])
    {
        trip.allowAddRail = @([buildString boolValue]);
    }
    else if ([currentElement isEqualToString:@"AllowCancel"])
    {
        trip.allowCancel = @([buildString boolValue]);
    }
	else if ([currentElement isEqualToString:@"StartDateLocal"] & [self.inBooking isEqualToString:@"NO"])
	{
		[trip setTripStartDateLocal:[DateTimeFormatter getNSDateFromMWSDateString:buildString]];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"]];
	}
    else if ([currentElement isEqualToString:@"RecordLocator"] & [self.inBooking isEqualToString:@"YES"])
	{
		[booking setRecordLocator:buildString];
//		[booking setSegmentsBookingKey:buildString];
	}
	else if ([currentElement isEqualToString:@"BookingSource"] & [self.inBooking isEqualToString:@"YES"])
	{
		[booking setBookSource:buildString];
		[booking setGds:[NSString stringWithFormat:@"%d", [BookingData getGDSId:buildString]]];
	}
	else if ([currentElement isEqualToString:@"IsCliqbookSystemOfRecord"] & [self.inBooking isEqualToString:@"YES"])
	{
        [booking setIsCliqbookSystemOfRecord:[NSNumber numberWithInt:[[string lowercaseString] isEqualToString: @"true"]]];
	}
	else if ([currentElement isEqualToString:@"AgencyPCC"] & [self.inBooking isEqualToString:@"YES"])
	{
		booking.agencyPCC = string;
	}
	else if ([currentElement isEqualToString:@"CompanyAccountingCode"] & [self.inBooking isEqualToString:@"YES"])
	{
		booking.companyAccountingCode = string;
	}
	else if ([currentElement isEqualToString:@"TravelConfigId"] & [self.inBooking isEqualToString:@"YES"])
	{
		booking.travelConfigId = string;
	}
    
    else if ([currentElement isEqualToString:@"SeatNumber"] && [self.inBooking isEqualToString:@"YES"] && [self.inSegment isEqualToString:@"YES"])
		segment.seatNumber = buildString;
    else if ([currentElement isEqualToString:@"ClassOfServiceLocalized"] && [self.inBooking isEqualToString:@"YES"] && [self.inSegment isEqualToString:@"YES"])
		segment.classOfServiceLocalized = buildString;
    
	else if ([currentElement isEqualToString:@"Type"] && [self.inBooking isEqualToString:@"YES"] && ![self.inSegment isEqualToString:@"YES"])
	{
		booking.type = string;
	}
	else if ([currentElement isEqualToString:@"CliqbookId"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setCliqbookId:buildString];
	}
	else if ([currentElement isEqualToString:@"ETicket"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setETicket:buildString];
	}
	else if ([currentElement isEqualToString:@"StartDateLocal"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setDateLocal:buildString];
	}
	else if ([currentElement isEqualToString:@"LegId"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setLegId:buildString];
	}
	else if ([currentElement isEqualToString:@"StartCityCode"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setCityCode:buildString];
	}
	else if ([currentElement isEqualToString:@"EndCityCode"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setCityCode:buildString];
	}
	else if ([currentElement isEqualToString:@"StartTerminal"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setTerminal:buildString];
	}
	else if ([currentElement isEqualToString:@"EndTerminal"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setTerminal:buildString];
	}
	else if ([currentElement isEqualToString:@"StartGate"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setGate:buildString];
	}
	else if ([currentElement isEqualToString:@"EndGate"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setGate:buildString];
	}
	else if ([currentElement isEqualToString:@"Duration"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setDuration:@([buildString intValue])];
	}
	else if ([currentElement isEqualToString:@"ConfirmationNumber"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setConfirmationNumber:buildString];
			//[segment setIdKey:buildString];
	}
	else if ([currentElement isEqualToString:@"FlightNumber"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setFlightNumber:buildString];
	}
	else if ([currentElement isEqualToString:@"Vendor"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setVendor:buildString];
	}
	else if ([currentElement isEqualToString:@"OperatedByVendor"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setOperatedBy:buildString];
	}	
	else if ([currentElement isEqualToString:@"OperatedByFlightNumber"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setOperatedByFlightNumber:buildString];
	}
	else if ([currentElement isEqualToString:@"EndDateLocal"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setDateLocal:buildString];
	}
	else if ([currentElement isEqualToString:@"Status"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setStatus:buildString];
	}
	else if ([currentElement isEqualToString:@"NumStops"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setNumStops:@([buildString intValue])];
	}
	else if ([currentElement isEqualToString:@"AircraftCode"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setAircraftCode:buildString];
	}
    else if ([currentElement isEqualToString:@"AircraftName"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setAircraftName:buildString];
	}
	else if ([currentElement isEqualToString:@"ClassOfService"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setClassOfService:buildString];
	}
	else if ([currentElement isEqualToString:@"Meals"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setMeals:buildString];
	}
	else if ([currentElement isEqualToString:@"SpecialInstructions"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setSpecialInstructions:buildString];
	}
	//PARKIKNG
	else if ([currentElement isEqualToString:@"StartLocation"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setLocation:buildString];
	}
	else if ([currentElement isEqualToString:@"ParkingLocationId"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setParkingLocationId:buildString];
	}
	else if ([currentElement isEqualToString:@"StartAddress"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setAddress:buildString];
	}
	else if ([currentElement isEqualToString:@"StartCity"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setCity:buildString];
	}
	else if ([currentElement isEqualToString:@"StartState"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setState:buildString];
	}
	else if ([currentElement isEqualToString:@"StartPostalCode"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setPostalCode:buildString];
	}
	else if ([currentElement isEqualToString:@"StartCountry"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setCountry:buildString];
	}
	else if ([currentElement isEqualToString:@"PhoneNumber"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setPhoneNumber:buildString];
	}
	else if ([currentElement isEqualToString:@"TotalRate"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setTotalRate: @([ buildString floatValue])];
	}
	else if ([currentElement isEqualToString:@"Currency"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setCurrency:buildString];
	}
	else if ([currentElement isEqualToString:@"Pin"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setPin:buildString];
	}
	else if ([currentElement isEqualToString:@"CancellationPolicy"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setCancellationPolicy:buildString];
	}
	else if ([currentElement isEqualToString:@"DailyRate"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setDailyRate:@([ buildString floatValue])];
	}
	else if ([currentElement isEqualToString:@"RoomDescription"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setRoomDescription:buildString];
	}
	//Ride
	else if ([currentElement isEqualToString:@"PickupInstructions"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setPickupInstructions:buildString];
	}
	else if ([currentElement isEqualToString:@"MeetingInstructions"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setMeetingInstructions:buildString];
	}
	else if ([currentElement isEqualToString:@"EndAddress"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setAddress:buildString];
	}
	else if ([currentElement isEqualToString:@"EndCity"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setCity:buildString];
	}
	else if ([currentElement isEqualToString:@"EndState"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setState:buildString];
	}
	else if ([currentElement isEqualToString:@"EndPostalCode"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setPostalCode:buildString];
	}
	else if ([currentElement isEqualToString:@"DropoffInstructions"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"] )
	{
		[segment setDropoffInstructions:buildString];
	}
	else if ([currentElement isEqualToString:@"RateDescription"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"] )
	{
		[segment setRateDescription:buildString];
	}
	else if ([currentElement isEqualToString:@"SegmentName"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setSegmentName:buildString];
	}
	else if ([currentElement isEqualToString:@"StartLatitude"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setLatitude:@([buildString floatValue])];
	}
	else if ([currentElement isEqualToString:@"StartLongitude"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"] )
	{
		[segment.relStartLocation setLongitude:@([buildString floatValue])];
	}
	else if ([currentElement isEqualToString:@"EndLatitude"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"] )
	{
		[segment.relEndLocation setLatitude:@([buildString floatValue])];
	}
	else if ([currentElement isEqualToString:@"EndLongitude"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setLongitude:@([buildString floatValue])];
	}
	else if ([currentElement isEqualToString:@"ReservationId"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setReservationId:buildString];
	}
	else if ([currentElement isEqualToString:@"NumPersons"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setNumPersons:@([buildString intValue])];
	}
	else if ([currentElement isEqualToString:@"StartAddress2"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setAddress2:buildString];
	}
	//RAIL
	else if ([currentElement isEqualToString:@"TrainNumber"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"] )
	{
		[segment setTrainNumber:buildString];
	}
	else if ([currentElement isEqualToString:@"OperatedByVendor"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setOperatedByVendor:buildString];
	}
	else if ([currentElement isEqualToString:@"OperatedByTrainNumber"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setOperatedByTrainNumber:buildString];
	}
	else if ([currentElement isEqualToString:@"StartRailStation"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"] )
	{
		[segment.relStartLocation setRailStation:buildString];
	}
	else if ([currentElement isEqualToString:@"StartPlatform"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"] )
	{
		[segment.relStartLocation setPlatform:buildString];
	}
	else if ([currentElement isEqualToString:@"EndPlatform"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"] )
	{
		[segment.relEndLocation setPlatform:buildString];
	}
	else if ([currentElement isEqualToString:@"WagonNumber"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setWagonNumber:buildString];
	}
	else if ([currentElement isEqualToString:@"Amenities"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setAmenities:buildString];
	}
	else if ([currentElement isEqualToString:@"EndRailStation"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setRailStation:buildString];
	}
	else if ([currentElement isEqualToString:@"EndPlatform"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setPlatform:buildString];
	}
	else if ([currentElement isEqualToString:@"TrainTypeCode"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setTrainTypeCode:buildString];
	}
	else if ([currentElement isEqualToString:@"Cabin"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setCabin:buildString];
	}	
	else if ([currentElement isEqualToString:@"Miles"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setMiles:@([buildString floatValue])];
	}	
	else if ([currentElement isEqualToString:@"ClassOfServiceLocalized"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setClassOfServiceLocalized:buildString];
	}
	else if ([currentElement isEqualToString:@"SeatNumber"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setSeatNumber:buildString];
	}	
	else if ([currentElement isEqualToString:@"SegmentKey"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setIdKey:buildString];
	}
	else if ([currentElement isEqualToString:@"VendorName"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setVendorName:buildString];
	}
	else if ([currentElement isEqualToString:@"StartCityCodeLocalized"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setCityCodeLocalized:buildString];
	}
	else if ([currentElement isEqualToString:@"StartAirportCity"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setAirportCity:buildString];
	}
	else if ([currentElement isEqualToString:@"StartAirportName"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setAirportName:buildString];
	}
	else if ([currentElement isEqualToString:@"StartAirportState"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setAirportState:buildString];
	}
	else if ([currentElement isEqualToString:@"StartAirportCountry"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setAirportCountry:buildString];
	}
	else if ([currentElement isEqualToString:@"EndCityCodeLocalized"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setCityCodeLocalized:buildString];
	}
	else if ([currentElement isEqualToString:@"EndAirportCity"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setAirportCity:buildString];
	}
	else if ([currentElement isEqualToString:@"EndAirportName"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setAirportName:buildString];
	}
	else if ([currentElement isEqualToString:@"EndAirportState"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setAirportState:buildString];
	}
	else if ([currentElement isEqualToString:@"EndAirportCountry"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setAirportCountry:buildString];
	}
	else if ([currentElement isEqualToString:@"HotelPropertyId"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setPropertyId:buildString];
		[segment setBookSource:booking.bookSource];
		[segment setGdsId:booking.gds];
	}
	else if ([currentElement isEqualToString:@"Type"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		//NSLog(@"Type = %@", string);
		
		if ([string isEqualToString:@"A"])
		{
			[segment setType:@"AIR"];
			[trip setHasAir:@YES];
		}
		else if ([string isEqualToString:@"H"])
		{
			[segment setType:@"HOTEL"];
			trip.hasHotel = @YES;
		}
		else if ([string isEqualToString:@"C"])
		{
			[segment setType:@"CAR"];
			[trip setHasCar:@YES];
		}
		else if ([string isEqualToString:@"F"])
		{
			[segment setType:@"DINING"];
			[trip setHasDining:@YES];
		}
		else if ([string isEqualToString:@"G"])
		{
			[segment setType:@"RIDE"];
			[trip setHasRide:@YES];
		}
		else if ([string isEqualToString:@"P"])
		{
			[segment setType:@"PARKING"];
			[trip setHasParking:@YES];
		}
		else if ([string isEqualToString:@"E"])
		{
			[segment setType:@"EVENT"];
			[trip setHasEvent:@YES];
		}
		else if ([string isEqualToString:@"R"])
		{
			[segment setType:@"RAIL"];
			[trip setHasRail:@YES];
		}
	}	
	else if ([currentElement isEqualToString:@"TransmissionLocalized"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setTransmission:buildString];
	}
	else if ([currentElement isEqualToString:@"NumCars"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setNumCars:@([buildString intValue])];
	}
	else if ([currentElement isEqualToString:@"AirConditionLocalized"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setAirCond:buildString];
	}
	else if ([currentElement isEqualToString:@"Body"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setBodyType:buildString];
	}
	else if ([currentElement isEqualToString:@"BodyLocalized"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setBodyTypeName:buildString];
	}
	else if ([currentElement isEqualToString:@"DiscountCode"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setDiscountCode:buildString];
	}
	else if ([currentElement isEqualToString:@"StatusLocalized"] && [self.inSegment isEqualToString:@"YES"])
	{
		[segment setStatusLocalized:buildString];
	}
	else if ([currentElement isEqualToString:@"SpecialEquipment"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setSpecialEquipment:buildString];
	}
	else if ([currentElement isEqualToString:@"RateType"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setRateType:buildString];
	}
	else if ([currentElement isEqualToString:@"StartAirportCountryCode"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setAirportCountryCode:buildString];
	}
	else if ([currentElement isEqualToString:@"EndAirportCountryCode"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setAirportCountryCode:buildString];
	}
	else if ([currentElement isEqualToString:@"ClassOfCar"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setClassOfCar:buildString];
	}
	else if ([currentElement isEqualToString:@"ClassOfCarLocalized"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setClassOfCarLocalized:buildString];
	}
	else if ([currentElement isEqualToString:@"NumRooms"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setNumRooms:@([ buildString intValue])];
	}
	else if ([currentElement isEqualToString:@"ImageCarURI"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setImageCarURI:buildString];
	}
	else if ([currentElement isEqualToString:@"ImageVendorURI"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment setImageVendorURI:buildString];
	}
	else if ([currentElement isEqualToString:@"StartDateUtc"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setDateUtc:buildString];
	}
    else if ([currentElement isEqualToString:@"EndDateUtc"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setDateUtc:buildString];
	}
    else if ([currentElement isEqualToString:@"Benchmark"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
    {
        segment.travelPointsBenchmark = buildString;
    }
    else if ([currentElement isEqualToString:@"BenchmarkCurrency"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
    {
        segment.travelPointsBenchmarkCurrency = buildString;
    }
    else if ([currentElement isEqualToString:@"PointsPending"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
    {
        segment.travelPointsPending = buildString;
    }
    else if ([currentElement isEqualToString:@"PointsPosted"] & [self.inBooking isEqualToString:@"YES"]  & [self.inSegment isEqualToString:@"YES"])
    {
        segment.travelPointsPosted = buildString;
    }
    else if ([currentElement isEqualToString:@"TotalPoints"] & [self.inBooking isEqualToString:@"NO"]  & [self.inSegment isEqualToString:@"NO"])
    {
        trip.travelPointsPosted = buildString; // We've started using the node 'TotalPoints' for Itin Travel Points
    }
	else if ([currentElement isEqualToString:@"StartRailStationLocalized"] && [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relStartLocation setRailStationLocalized:buildString];
	}
	else if ([currentElement isEqualToString:@"EndRailStationLocalized"] && [self.inSegment isEqualToString:@"YES"])
	{
		[segment.relEndLocation setRailStationLocalized:buildString];
	}
	else if ([currentElement isEqualToString:@"Error"] )
	{
		//[segment setStartDateUTC:buildString];
	}
	else if ([currentElement isEqualToString:@"Message"])
	{
		//[segment setStartDateUTC:buildString];
	}
	else if ([currentElement isEqualToString:@"h1"])
	{
		self.errorInfo = string;
		//self.errorCode = msg.responseCode;
//		NSRange range = [string rangeOfString:@"Bad Request"];
//		if( range.location > -1)
//			self.errorCode = @"500";
	}
	else if (inFlightStats && [currentElement isEqualToString:@"ArrivalActual"])
	{
		[flightStats setArrivalActual:[self trimExcessDateString:buildString]];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"ArrivalEstimated"])
	{
		[flightStats setArrivalEstimated:[self trimExcessDateString:buildString]];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"ArrivalGate"])
	{
		[flightStats setArrivalGate:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"ArrivalLongStatus"])
	{
		[flightStats setArrivalLongStatus:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"ArrivalScheduled"])
	{
		[flightStats setArrivalScheduled:[self trimExcessDateString:buildString]];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"ArrivalShortStatus"])
	{
		[flightStats setArrivalShortStatus:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"ArrivalTerminalActual"])
	{
		[flightStats setArrivalTerminalActual:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"ArrivalTerminalScheduled"])
	{
		[flightStats setArrivalTerminalScheduled:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"BaggageClaim"])
	{
		[flightStats setBaggageClaim:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"DepartureActual"])
	{
		[flightStats setDepartureActual:[self trimExcessDateString:buildString]];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"DepartureEstimated"])
	{
		[flightStats setDepartureEstimated:[self trimExcessDateString:buildString]];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"DepartureGate"])
	{
		[flightStats setDepartureGate:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"DepartureLongStatus"])
	{
		[flightStats setDepartureLongStatus:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"DepartureScheduled"])
	{
		[flightStats setDepartureScheduled:[self trimExcessDateString:buildString]];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"DepartureShortStatus"])
	{
		[flightStats setDepartureShortStatus:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"DepartureTerminalActual"])
	{
		[flightStats setDepartureTerminalActual:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"DepartureTerminalScheduled"])
	{
		[flightStats setDepartureTerminalScheduled:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"EquipmentActual"])
	{
		[flightStats setEquipmentActual:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"EquipmentScheduled"])
	{
		[flightStats setEquipmentScheduled:buildString];
	}
	else if (inFlightStats && [currentElement isEqualToString:@"LastUpdatedUTC"])
	{
		[flightStats setLastUpdatedUTC:[self trimExcessDateString:buildString]];
	}
//Handle offers
    if(inOffer && [currentElement isEqualToString:@"Id"])
        self.offer.offerId = buildString;
    else if(inOffer && [currentElement isEqualToString:@"Title"])
    {
        if (inMultiWebLink) {
            //link.title = buildString;
            //TODO : handle this case
        }
        else {
            offer.title = buildString;
        }
    }
    else if(inOffer && [currentElement isEqualToString:@"OfferVendor"])
        self.offer.offerVendor = buildString;
    else if(inOffer && [currentElement isEqualToString:@"OfferType"])
        self.offer.offerType = buildString;
    else if(inOffer && [currentElement isEqualToString:@"OfferAction"])
        self.offer.offerAction = buildString;
    else if(inOffer && [currentElement isEqualToString:@"ActionUrl"])
    {
        if (inMultiWebLink) {
            //link.actionURL = buildString;
            // TODO : Handle this case
        }
        else {
            offer.actionURL = buildString;
        }
    }
    else if(inOffer && [currentElement isEqualToString:@"ImageUrl"])
        offer.imageURL = buildString;
    else if(inOffer && [currentElement isEqualToString:@"OfferApplication"])
    {
        offer.offerApplication = buildString;
    }
    else if(inOffer && [currentElement isEqualToString:@"HtmlOfferContent"])
        offer.htmlContent = buildString;
    else if(inOffer && [currentElement isEqualToString:@"ImageName"])
    {
        offer.imageName = buildString;
    }
    else if(inOffer && [currentElement isEqualToString:@"Latitude"])
    {
         offer.geoLatitude = @([buildString doubleValue]);
    }
    else if(inOffer && [currentElement isEqualToString:@"Longitude"])
    {
        offer.geoLongitude  = @([buildString doubleValue]);
    }
    else if(inOffer && [currentElement isEqualToString:@"DimensionKm"])
    {
        offer.geoDimensionkm  = @([buildString floatValue]);
    }
        else if(inOffer && [currentElement isEqualToString:@"Overlay"])
    {
        offerOverlay.overLay = buildString;
    }
    else if(inOffer && [currentElement isEqualToString:@"latitude"])
    {
        offerLocation.latitude = @([buildString doubleValue]);
    }
    else if(inOffer && [currentElement isEqualToString:@"longitude"])
    {
        offerLocation.longitude = @([buildString doubleValue]);
    }
    else if(inOffer && [currentElement isEqualToString:@"proximity"])
    {
        offerLocation.proximity = @([buildString doubleValue]);
    }
    else if(inOffer && [currentElement isEqualToString:@"endDateTimeUTC"])
    {
        offerTimeRange.endDateTimeUTC = [DateTimeFormatter getNSDateFromMWSDateString:buildString];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"];
       
    }
    else if(inOffer && [currentElement isEqualToString:@"startDateTimeUTC"])
    {
        offerTimeRange.startDateTimeUTC = [DateTimeFormatter getNSDateFromMWSDateString:buildString];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"];
        
    }
}

-(NSString *)trimExcessDateString:(NSString *)dateString
{
    NSString *dateFormat = @"yyyy-MM-dd'T'HH:mm:ss";
    if ([dateString length] > [dateFormat length] - 2) // minus 2 for the single quotes
        return [dateString substringWithRange:NSMakeRange(0, [dateFormat length] - 2)];
    return dateString;
}


#pragma mark - Context
/**
 Returns the managed object context for the application.
 If the context doesn't already exist, it is created and bound to the persistent store coordinator for the application.
 */
- (NSManagedObjectContext *)managedObjectContext
{
    if (__managedObjectContext != nil)
    {
        return __managedObjectContext;
    }
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSPersistentStoreCoordinator *coordinator = [ad persistentStoreCoordinator];
    if (coordinator != nil)
    {
        __managedObjectContext = [[NSManagedObjectContext alloc] init];
        [__managedObjectContext setPersistentStoreCoordinator:coordinator];
    }
    return __managedObjectContext;
}

- (void)saveContext
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = __managedObjectContext;
    if (managedObjectContext != nil)
    {
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
        {
            /*
             Replace this implementation with code to handle the error appropriately.
             
             abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. If it is not possible to recover from the error, display an alert panel that instructs the user to quit the application by pressing the Home button.
             */
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        } 
    }
}
@end
