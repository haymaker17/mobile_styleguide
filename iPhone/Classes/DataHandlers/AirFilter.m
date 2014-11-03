//
//  AirFilter.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/8/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AirFilter.h"
#import "DateTimeFormatter.h"
#import "AirRuleManager.h"
#import "EntityAirRules.h"
#import "Config.h"

@interface AirFilter()

@property (strong,nonatomic) AirFilterManager *airFilterManager;
@property (strong,nonatomic) AirFilterSummaryManager *airFilterSummaryManager;
@property (strong,nonatomic) AirRuleManager *airRuleManager;
@property (strong,nonatomic) AirViolationManager *airViolationManager;
@property (strong,nonatomic) NSManagedObjectContext *managedObjectContext;

@end

@implementation AirFilter
@synthesize path, currentElement, items, obj, aStops, stopChoices, airlineEntry, numStops, isInStops, isInAirlineEntry, buildString, isInVendorCodes, vendors, vendorCode, vendorName;
@synthesize     crnCode, fare, fareId,airFilter,airFilterSummary, iAirSeg, iFlight, iNumStops, iRoundDuration, iRoundNumStops, iDuration, elapsedTime, airShop, airRule,airViolation, inViolation;

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
{
    // MOB-9650 When a view controller is deallocated, it sends a cancellation notification.  In that case,
    // do not bother to parse the response.
    if (cancellationReceived)
        return;
    
    //we have many calls, and we don't want to to the calls out of order
  //NSString *s = [[NSString alloc] initWithData:data encoding:NSStringEncodingConversionExternalRepresentation];
//	NSLog(@"air data = %@", s);
//    
//    NSString *dog = @"<AirSearchSummary xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><AirportCityCodes><Dictionary><AirPair><Key>SEA</Key><Value>Seattle, WA</Value></AirPair><AirPair><Key>OAK</Key><Value>Oakland, CA</Value></AirPair><AirPair><Key>SFO</Key><Value>San Francisco, CA</Value></AirPair><AirPair><Key>LAX</Key><Value>Los Angeles, CA</Value></AirPair><AirPair><Key>SJC</Key><Value>San Jose, CA</Value></AirPair><AirPair><Key>SLC</Key><Value>Salt Lake City, UT</Value></AirPair><AirPair><Key>PDX</Key><Value>Portland, OR</Value></AirPair><AirPair><Key>PHX</Key><Value>Phoenix, AZ</Value></AirPair></Dictionary></AirportCityCodes><AirportCodes><Dictionary><AirPair><Key>SEA</Key><Value>Seattle Tacoma Intl Arpt</Value></AirPair><AirPair><Key>OAK</Key><Value>Metro Oakland Intl Arpt</Value></AirPair><AirPair><Key>SFO</Key><Value>San Francisco Intl Arpt</Value></AirPair><AirPair><Key>LAX</Key><Value>Los Angeles Intl</Value></AirPair><AirPair><Key>SJC</Key><Value>San Jose Intl Arpt</Value></AirPair><AirPair><Key>SLC</Key><Value>Salt Lake City Intl Arpt</Value></AirPair><AirPair><Key>PDX</Key><Value>Portland Intl Arpt</Value></AirPair><AirPair><Key>PHX</Key><Value>Sky Harbor Intl Arpt</Value></AirPair></Dictionary></AirportCodes><ByStops><StopsGroup><Entries><AirlineEntry><Airline>CO</Airline><LowestCost>263</LowestCost><NumChoices>3</NumChoices><Preference>NYI</Preference></AirlineEntry><AirlineEntry><Airline>DL</Airline><LowestCost>336.79</LowestCost><NumChoices>3</NumChoices><Preference>NYI</Preference></AirlineEntry><AirlineEntry><Airline>UA</Airline><LowestCost>263</LowestCost><NumChoices>9</NumChoices><Preference>NYI</Preference></AirlineEntry><AirlineEntry><Airline>US</Airline><LowestCost>273</LowestCost><NumChoices>6</NumChoices><Preference>NYI</Preference></AirlineEntry><AirlineEntry><Airline>VX</Airline><LowestCost>299.58</LowestCost><NumChoices>1</NumChoices><Preference>NYI</Preference></AirlineEntry></Entries><NumStops>0</NumStops></StopsGroup><StopsGroup><Entries><AirlineEntry><Airline>AA</Airline><LowestCost>283</LowestCost><NumChoices>1</NumChoices><Preference>NYI</Preference></AirlineEntry><AirlineEntry><Airline>DL</Airline><LowestCost>343.79</LowestCost><NumChoices>6</NumChoices><Preference>NYI</Preference></AirlineEntry><AirlineEntry><Airline>UA</Airline><LowestCost>376</LowestCost><NumChoices>11</NumChoices><Preference>NYI</Preference></AirlineEntry><AirlineEntry><Airline>US</Airline><LowestCost>363.3</LowestCost><NumChoices>4</NumChoices><Preference>NYI</Preference></AirlineEntry></Entries><NumStops>1</NumStops></StopsGroup><StopsGroup><Entries><AirlineEntry><Airline>DL</Airline><LowestCost>388.63</LowestCost><NumChoices>3</NumChoices><Preference>NYI</Preference></AirlineEntry></Entries><NumStops>2</NumStops></StopsGroup></ByStops><End>2011-11-23T00:00:00</End><EquipmentCodes><Dictionary><AirPair><Key>73G</Key><Value>Boeing 737-700</Value></AirPair><AirPair><Key>738</Key><Value>Boeing 737-800</Value></AirPair><AirPair><Key>737</Key><Value>Boeing 737</Value></AirPair><AirPair><Key>734</Key><Value>Boeing 737</Value></AirPair><AirPair><Key>319</Key><Value>Airbus A319</Value></AirPair><AirPair><Key>320</Key><Value>Airbus A320</Value></AirPair><AirPair><Key>752</Key><Value>Boeing 757-200</Value></AirPair><AirPair><Key>CR7</Key><Value>Canadair 700</Value></AirPair><AirPair><Key>DH4</Key><Value>DHC8 Dash 8-400</Value></AirPair><AirPair><Key>CRJ</Key><Value>Canadair Jet</Value></AirPair><AirPair><Key>757</Key><Value>Boeing 757-200</Value></AirPair><AirPair><Key>CHG</Key><Value>Change Enroute</Value></AirPair><AirPair><Key>CR9</Key><Value>Canadair 900</Value></AirPair><AirPair><Key>EM2</Key><Value>Embraer EMB-120</Value></AirPair></Dictionary></EquipmentCodes><PreferenceRankings><Dictionary><AirPair><Key>DL</Key><Value>5</Value></AirPair><AirPair><Key>UA</Key><Value>5</Value></AirPair></Dictionary></PreferenceRankings><Route xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><a:string>SEA</a:string><a:string>SFO</a:string><a:string>SEA</a:string></Route><Start>2011-11-20T00:00:00</Start><VendorCodes><Dictionary><AirPair><Key>AS</Key><Value>Alaska Airlines</Value></AirPair><AirPair><Key>AA</Key><Value>American</Value></AirPair><AirPair><Key>CO</Key><Value>Continental</Value></AirPair><AirPair><Key>UA</Key><Value>United</Value></AirPair><AirPair><Key>US</Key><Value>US Airways</Value></AirPair><AirPair><Key>DL</Key><Value>Delta</Value></AirPair><AirPair><Key>VX</Key><Value>Virgin America</Value></AirPair><AirPair><Key>--</Key><Value>Multiple Carriers</Value></AirPair></Dictionary></VendorCodes></AirSearchSummary>";
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
	return AIR_FILTER;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.airShop = parameterBag[@"AIRSHOP"];
	self.path = [NSString stringWithFormat:@"%@/Mobile/Air/Filter",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	return msg;
}


-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post

    NSString *airline = parameterBag[@"AIRLINE"];
    if([airline isEqualToString:@"   TOTAL"])
        airline = @"*";
    
    NSString *formattedBodyXml = @"";
    
    if ([Config isGov])
    {
        NSString *rateType = [parameterBag objectForKey:@"RATETYPE"];
        formattedBodyXml = [NSString stringWithFormat: @"<FilterCriteria><Airline>%@</Airline><RateType>%@</RateType></FilterCriteria>", airline, rateType];
    }
    else
    {
        NSString *numberStops = parameterBag[@"NUMSTOPS"];
        formattedBodyXml = [NSString stringWithFormat: @"<FilterCriteria><Airline>%@</Airline><NumStops>%@</NumStops></FilterCriteria>", airline, numberStops];
    }
	return formattedBodyXml;
}

-(void) flushData
{
	
}


- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	self.stopChoices = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    //
    // Create a new managed Object context in private queue for this class
    // http://stackoverflow.com/questions/19123074/how-to-correctly-manage-the-nsmanagedobjectcontext-in-each-view-controller
    // http://robots.thoughtbot.com/core-data
    // https://developer.apple.com/library/ios/documentation/cocoa/Reference/CoreDataFramework/Classes/NSManagedObjectContext_Class/NSManagedObjectContext.html
    //
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSPersistentStoreCoordinator *coordinator = [ad persistentStoreCoordinator];
    if (coordinator != nil)
    {
        self.managedObjectContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
        [self.managedObjectContext setPersistentStoreCoordinator:coordinator];
        
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        // suppress notifications to any thread until updates are complete.
        [[NSNotificationCenter defaultCenter] removeObserver:ad name:NSManagedObjectContextDidSaveNotification object:self.managedObjectContext];
        [[NSNotificationCenter defaultCenter] removeObserver:ad name:NSManagedObjectContextObjectsDidChangeNotification object:self.managedObjectContext];
        self.airFilterManager = [[AirFilterManager alloc] initWithContext:self.managedObjectContext];
        self.airFilterSummaryManager = [[AirFilterSummaryManager alloc] initWithContext:self.managedObjectContext];
        self.airRuleManager = [[AirRuleManager alloc] initWithContext:self.managedObjectContext];
        self.airViolationManager = [[AirViolationManager alloc] initWithContext:self.managedObjectContext];
    }
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError
{
	
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	
	isInElement = @"YES";
    self.buildString = [[NSMutableString alloc] initWithString:@""];
	
	if ([elementName isEqualToString:@"AirChoice"])
	{		
        self.airFilterSummary = (EntityAirFilterSummary*)[self.airFilterSummaryManager makeNew];
        self.iFlight = -1;
        self.iAirSeg = -1;
	}
	else if ([elementName isEqualToString:@"Flight"])
	{		
        self.iFlight++;
        self.airFilter = (EntityAirFilter*)[self.airFilterManager makeNew];
	}	
    else if ([elementName isEqualToString:@"AirSegment"])
	{
        self.iAirSeg++;
        self.iFlight = -1;
    }
    else if ([elementName isEqualToString:@"Rule"])
    {
        self.airRule = (EntityAirRules*)[self.airRuleManager makeNew];
    }
    else if ([elementName isEqualToString:@"Violation"])
	{
        inViolation = YES;
        self.airViolation = (EntityAirViolation *)[self.airViolationManager makeNew];
	}
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"AirChoice"])
	{		
        [self figureOutRankingsForFlights];
        airFilterSummary.durationTotal = @([airFilterSummary.duration intValue] + [airFilterSummary.roundDuration intValue]);
        //NSLog(@"durationTotal = %d", [airFilterSummary.durationTotal intValue]);
        // Dont save it here do a batch save at the end of document
        // Merge context notifications and save changes in one shot
//        [self.airFilterSummaryManager saveIt:airFilterSummary];
        
	}
	else if ([elementName isEqualToString:@"Flight"])
	{		
        airFilter.fare = airFilterSummary.fare;
        airFilter.crnCode = airFilterSummary.crnCode;
        airFilter.fareId = airFilterSummary.fareId;
        airFilter.flightPos = @(iFlight);
        airFilter.segmentPos = @(iAirSeg);
        airFilter.AirFilterSummary = self.airFilterSummary;
        // Dont save it here do a batch save at the end of document
        // Merge context notifications and save changes in one shot
//        [self.airFilterManager saveIt:airFilter];
        
        if(iFlight > 0)
        {
            //we are a leg, calculate layover
            EntityAirFilter *previousFlight = (EntityAirFilter*)[self.airFilterManager fetchByFareIdSegmentPosFlightPos:airFilterSummary.fareId segPos:iAirSeg flightPos:(iFlight-1)];
            
            EntityAirFilter *layover = (EntityAirFilter*)[self.airFilterManager makeNew];
            layover.fare = airFilterSummary.fare;
            layover.crnCode = airFilterSummary.crnCode;
            layover.fareId = airFilterSummary.fareId;
            float fPos = (iFlight - 1.0) + 0.5f;
            //NSLog(@"iFlight=%d fPos=%.2f", iFlight, fPos);
            layover.flightPos = @(fPos);
            layover.segmentPos = @(iAirSeg);
            layover.carrier = @"LAYOVER";
            
            NSTimeInterval interval = [ airFilter.departureTime timeIntervalSinceDate:  previousFlight.arrivalTime];
            //int dayDiff = interval / 86400;
            layover.elapsedTime = [NSNumber numberWithInt:interval / 60];
            //NSLog(@"interval = %g", interval);
            
            layover.startIata = previousFlight.endIata;
            layover.AirFilterSummary = self.airFilterSummary;
            // Dont save it here do a batch save at the end of document
            // Merge context notifications and save changes in one shot

//            [self.airFilterManager saveIt:layover];
        }
	}	
    else if ([elementName isEqualToString:@"Rule"])
    {
        airRule.AirFilterSummary = airFilterSummary;
        airRule.fareId = airFilterSummary.fareId;
        // Dont save it here do a batch save at the end of document
        // Merge context notifications and save changes in one shot

//        [self.airRuleManager saveIt:airRule];
//        self.airRule = nil;
    }
    else if ([elementName isEqualToString:@"Violation"])
    {
        inViolation = NO;
        airViolation.fareId = airFilterSummary.fareId;
        // Dont save it here do a batch save at the end of document
        // Merge context notifications and save changes in one shot
//        [self.airViolationManager saveIt:airViolation];
//        self.airViolation = nil;
    }
}


-(void)figureOutRankingsForFlights
{
    NSArray *aFlights = [self.airFilterManager fetchByFareId:airFilterSummary.fareId];
    NSString *carrier = airFilterSummary.airlineCode;
    //NSLog(@"carrier = %@", carrier);
//    BOOL allSame = YES;
    for(EntityAirFilter *filter in aFlights)
    {
        if(carrier == nil && ![carrier isEqualToString:@"LAYOVER"])
            carrier = filter.carrier;
        
        if(![carrier isEqualToString:filter.carrier] && ![carrier isEqualToString:@"LAYOVER"] && ![filter.carrier isEqualToString:@"LAYOVER"])
        {
//            allSame = NO;
            carrier = @"SDFSD FDS F";
            break;
        }
    }
    
    NSString *prefRanking = @"0";
    //NSLog(@"carrier2 = %@", carrier);
    if((airShop.prefRankings)[carrier] != nil)
        prefRanking = (airShop.prefRankings)[carrier];
    //NSLog(@"prefRanking REALS %@", prefRanking);
    for(EntityAirFilter *filter in aFlights)
    {
        filter.pref = @([prefRanking intValue]);
    }
    
    airFilterSummary.pref = @([prefRanking intValue]);
    // Dont save it here do a batch save at the end of document
    // Merge context notifications and save changes in one shot

//    [self.airFilterManager saveIt:nil];
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	//NSLog(@"currentElement = %@ string = %@", currentElement, string);
    [buildString appendString:string];
	
	if ([currentElement isEqualToString:@"Crn"])
	{
		self.airFilterSummary.crnCode = buildString;
	}
    else if ([currentElement isEqualToString:@"Fare"])
	{
		self.airFilterSummary.fare = @([ buildString doubleValue]);
	}
    else if ([currentElement isEqualToString:@"FareId"])
	{
		self.airFilterSummary.fareId = buildString;
	}
    else if ([currentElement isEqualToString:@"ChoiceId"])
    {
        self.airFilterSummary.choiceId = buildString;
    }
    else if ([currentElement isEqualToString:@"GdsName"])
	{
		self.airFilterSummary.gdsName = buildString;
	}
    else if ([currentElement isEqualToString:@"InstantPurchase"])
    {
        self.airFilterSummary.isInstantPurchase = @([buildString boolValue]);
    }
    else if ([currentElement isEqualToString:@"RateType"])
    {
        self.airFilterSummary.rateType = buildString;
    }
    else if ([currentElement isEqualToString:@"Refundable"])
	{
//        NSLog(@"[NSNumber numberWithBool:[ buildString boolValue]] %d", [buildString boolValue]);
		self.airFilterSummary.refundable = @([ buildString boolValue]);
	}
    else if ([currentElement isEqualToString:@"CanRedeemTravelPointsAgainstViolations"])
    {
        self.airFilterSummary.canUseTravelPoints = @([buildString boolValue]);
    }
    else if ([currentElement isEqualToString:@"TravelPoints"])
    {
        self.airFilterSummary.travelPoints = @([buildString intValue]);
    }
    
    if ([currentElement isEqualToString:@"ElapsedTimeMin"])
	{
        if(iAirSeg == 0)
            airFilterSummary.duration = @([buildString intValue]);
        else
            airFilterSummary.roundDuration = @([buildString intValue]);
	}
    
    //flight filter
    if ([currentElement isEqualToString:@"AirMiles"])
	{
		self.airFilter.airMiles = @([buildString intValue]);
	}
    else if ([currentElement isEqualToString:@"AircraftCode"])
	{
		self.airFilter.aircraftCode = buildString;
	}
    else if ([currentElement isEqualToString:@"ArrivalTime"])
	{
		self.airFilter.arrivalTime = [DateTimeFormatter getNSDateFromMWSDateString:buildString];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"];
        if(iAirSeg == 0)
            airFilterSummary.arrivalTime = [DateTimeFormatter getNSDateFromMWSDateString:buildString];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"];
        else
            airFilterSummary.roundArrivalTime = [DateTimeFormatter getNSDateFromMWSDateString:buildString];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"];
	}
    else if ([currentElement isEqualToString:@"Bic"])
	{
		self.airFilter.bic = buildString;
	}
    else if ([currentElement isEqualToString:@"Carrier"])
	{
		self.airFilter.carrier = buildString;
        if(self.airFilterSummary.airlineCode == nil)
            self.airFilterSummary.airlineCode = buildString;
	}
    else if ([currentElement isEqualToString:@"DepartureTime"])
	{
		self.airFilter.departureTime = [DateTimeFormatter getNSDateFromMWSDateString:buildString];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"];
        if(iFlight == 0 && iAirSeg == 0)
        {
            airFilterSummary.departureTime = [DateTimeFormatter getNSDateFromMWSDateString:buildString];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"];
        }
        else if (iFlight == 0 )
            airFilterSummary.roundDepartureTime = [DateTimeFormatter getNSDateFromMWSDateString:buildString];//[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd'T'HH:mm:ss"];
	}
    else if ([currentElement isEqualToString:@"EndIata"])
	{
		self.airFilter.endIata = buildString;
        if(iAirSeg == 0)
        {
            //if(self.airFilterSummary.arrivalIata == nil)
                airFilterSummary.arrivalIata = buildString;
        }
        else
        {
            //if(self.airFilterSummary.roundArrivalIata == nil)
                airFilterSummary.roundArrivalIata = buildString;
        }
	}
    else if ([currentElement isEqualToString:@"FlightNum"])
	{
		self.airFilter.flightNum = buildString;
	}
    else if ([currentElement isEqualToString:@"FlightTime"])
	{
		self.airFilter.flightTime = buildString;
	}
    else if ([currentElement isEqualToString:@"FltClass"])
	{
		self.airFilter.fltClass = buildString;
	}
    else if ([currentElement isEqualToString:@"NumStops"])
	{
		self.airFilter.numStops = @([buildString intValue]);
        if(iAirSeg == 0)
        {
            //if(self.airFilterSummary.numStops == nil)
                airFilterSummary.numStops = @(iFlight);
        }
        else
        {
            //if(self.airFilterSummary.roundNumStops == nil)
                airFilterSummary.roundNumStops = @(iFlight);
        }
	}
    else if ([currentElement isEqualToString:@"OperatingCarrier"])
	{
		self.airFilter.operatingCarrier = buildString;
	}
    else if ([currentElement isEqualToString:@"StartIata"])
	{
		self.airFilter.startIata = buildString;
        if(iAirSeg == 0)
        {
            if(self.airFilterSummary.departureIata == nil)
                airFilterSummary.departureIata = buildString;
        }
        else
        {
            if(self.airFilterSummary.roundDepartureIata == nil)
                airFilterSummary.roundDepartureIata = buildString;
        }
            
	}
    else if ([currentElement isEqualToString:@"EnforcementLevel"])
    {
        if (inViolation) 
        {
            airViolation.enforcementLevel = @([buildString intValue]);
            airFilterSummary.enforcementLevel = buildString;
            airFilter.enforcementLevel = buildString;
        }
        else
        {
            airFilter.enforcementLevel = buildString;
            airFilterSummary.enforcementLevel = buildString;
            airRule.exceptionLevel = @([buildString intValue]);
        }
    }
    else if ([currentElement isEqualToString:@"Message"])
    {
        if (inViolation) 
        {
            airViolation.message = buildString;
        }
        else
        {
            airFilter.ruleMessage = buildString;
            airFilterSummary.ruleMessage = buildString;
            airRule.exceptionMessage = buildString;
        }
    }
    else if ([currentElement isEqualToString:@"Code"])
    {
        airViolation.code = buildString;
    }
    else if ([currentElement isEqualToString:@"ViolationType"])
    {
        airViolation.violationType = buildString;
    }
    else if ([currentElement isEqualToString:@"MaxEnforcementLevel"])
	{
		self.airFilterSummary.maxEnforcementLevel = @([buildString intValue]);
	}

}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    
    NSError *error = nil;
    // Merge the context notifications before saving so the viewcontrollers get the notifications.
    // after operations is done remove the listener.
    // processnotification method in appdelegate handles the merge
    DLog(@"saveContext has Changes : %@", [self.managedObjectContext hasChanges] ? @"YES": @"NO");
    if (self.managedObjectContext != nil && [self.managedObjectContext hasChanges] )
    {
        [[NSNotificationCenter defaultCenter] addObserver:ad
                                                 selector:@selector(processNotification:)
                                                     name:NSManagedObjectContextDidSaveNotification
                                                   object:self.managedObjectContext];
        if(![self.managedObjectContext save:&error])
        {
            ALog(@"Unresolved save error: %@, %@", error, [error userInfo]);
            abort();
        }
        // Stop observing changes after save is done.
        [[NSNotificationCenter defaultCenter] removeObserver:ad name:NSManagedObjectContextDidSaveNotification object:self.managedObjectContext];
    }
}



@end
