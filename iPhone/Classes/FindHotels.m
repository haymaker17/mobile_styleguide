//
//  FindHotels.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FindHotels.h"
#import "FormatUtils.h"
#import "HotelImageData.h"
#import "HotelBenchmarkData.h"

@interface FindHotels()
@property (strong, nonatomic) NSData *responseData;
@property (nonatomic) BOOL hasBenchmarks;
@end

@implementation FindHotels

@synthesize currentElement;
@synthesize pathRoot;
@synthesize path;
@synthesize hotelSearch;
@synthesize currentHotelResult;
@synthesize currentImagePair;
@synthesize inCheapestRoom;
@synthesize inCheapestRoomWithViolation, buildString, cheapRoom, hotelImage, hotelViolation, totalCount;

-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
    
//    NSString *xml = [[NSString alloc] initWithData:data encoding:NSStringEncodingConversionAllowLossy];
//    NSLog(@"FindHotels XML = %@", xml);
    self.responseData = data; // To parse Hotel Benchmarks if any are present
	[self parseXMLFileAtData:data];
}


- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    NSString *startPos = parameterBag[@"STARTPOS"];
    NSString *numRecords = parameterBag[@"NUMRECORDS"];
    NSString *pollingID = parameterBag[@"POLLINGID"];
    
    if(([startPos intValue] == 0) && pollingID == nil)
        [[HotelBookingManager sharedInstance] deleteAll];
    
	self.hotelSearch = parameterBag[@"HOTEL_SEARCH"];
	self.pathRoot =[ExSystem sharedInstance].entitySettings.uri;
    ///Mobile/Hotel/Search2/{start_index}/{count}
    //@"%@/mobile/Hotel/Search"
    //@"%@/mobile/Hotel/Search2/0/30"

    if ([pollingID length] > 0)
    {
        self.path = [NSString stringWithFormat:@"%@/mobile/Hotel/PollSearchResults/%@",[ExSystem sharedInstance].entitySettings.uri, pollingID];
    }
    else
    {
        self.path = [NSString stringWithFormat:@"%@/mobile/Hotel/Search3",[ExSystem sharedInstance].entitySettings.uri];
    }
	NSLog(@"path = %@", self.path);
    
	Msg* msg = [[Msg alloc] initWithData:FIND_HOTELS State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];

    // New Search3 and polling code
    NSString *hotelSearchCriteria = [self makeXMLBody:startPos withCount:numRecords];
    [msg setBody:hotelSearchCriteria];
    parameterBag[@"HOTEL_SEARCH_CRITERIA"] = hotelSearchCriteria;
    
	return msg;
}


-(NSString *)makeDateString:(NSDate*)date
{
	NSCalendar *calendar = [NSCalendar currentCalendar];
	calendar.timeZone = [NSTimeZone localTimeZone];
	NSDateComponents *components = [calendar components:(NSYearCalendarUnit | NSMonthCalendarUnit | NSDayCalendarUnit) fromDate:date];
	NSString *dateString = [NSString stringWithFormat:@"%i/%i/%i", components.month, components.day, components.year];
	return dateString;
}

-(NSString *)makeXMLBody
{
    return [self makeXMLBody:nil withCount:nil];
}

-(NSString *)makeXMLBody:(NSString*)startPos withCount:(NSString*)count
{//knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<HotelCriteria>"];
    if ([count length] > 0)
    {
        [bodyXML appendString:[NSString stringWithFormat:@"<Count>%@</Count>", count]];
    }
	[bodyXML appendString:@"<DateEnd>%@</DateEnd>"];
	[bodyXML appendString:@"<DateStart>%@</DateStart><GetBenchmarks>true</GetBenchmarks>"];
	[bodyXML appendString:@"<Hotel>%@</Hotel>"];
    [bodyXML appendFormat:@"<IncludeDepositRequired>true</IncludeDepositRequired>"];
	[bodyXML appendString:@"<Lat>%@</Lat>"];
	[bodyXML appendString:@"<Lon>%@</Lon>"];
    if (hotelSearch.hotelSearchCriteria.perDiemRate!= nil)
    {
        NSString *perDiemRateStr = [FormatUtils formatMoneyWithNumber :hotelSearch.hotelSearchCriteria.perDiemRate crnCode:@"USD" withCurrency:NO];
        [bodyXML appendString:[NSString stringWithFormat:@"<PerdiemRate>%@</PerdiemRate>", perDiemRateStr]];
    }
	[bodyXML appendString:@"<Radius>%i</Radius>"];
	[bodyXML appendString:@"<Scale>%@</Scale>"];
	[bodyXML appendString:@"<Smoking>%@</Smoking>"];
    if ([startPos length] > 0)
    {
        [bodyXML appendString:[NSString stringWithFormat:@"<Start>%@</Start>", startPos]];
    }
	[bodyXML appendString:@"</HotelCriteria>"];
	
	HotelSearchCriteria* criteria = hotelSearch.hotelSearchCriteria;
	
	NSString* checkoutDate = [self makeDateString:criteria.checkoutDate];
	NSString* checkinDate = [self makeDateString:criteria.checkinDate];
	NSString* containingWords = [FormatUtils makeXMLSafe:criteria.containingWords];
	NSString* latitude = criteria.locationResult.latitude;
	NSString* longitude = criteria.locationResult.longitude;
	int distance = [criteria.distanceValue intValue];
	NSString* distanceUnit  = ([criteria.isMetricDistance boolValue] ? @"K" : @"M");
	NSString* smoking = (criteria.smokingPreferenceCodes)[criteria.smokingIndex];
	
	NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML,
				checkoutDate,
				checkinDate,
				containingWords,
				latitude,
				longitude,
				distance,
				distanceUnit, // TODO: Paul, what are the allowable values?
				smoking  // TODO: Paul, what are the allowable values?
			];
	
	//NSLog(@"hotel formattedBodyXml = %@", formattedBodyXml);
	return formattedBodyXml;
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
    // On occasions we have received results back from MWS saying FINAL but there are no rates
    self.hotelSearch.ratesFound = NO;
//	self.hotelSearch.hotels = [[NSMutableArray alloc] initWithObjects:nil];	// Retain count = 2
//	[self.hotelSearch.hotels release];	// Retain count = 1
    self.hasBenchmarks = NO;
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	NSString * errorString = [NSString stringWithFormat:@"Unable to get hotel search results (Error code %i )", [parseError code]];
	NSLog(@"error parsing XML: %@", errorString);
	
	// TODO: handle error
	//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error loading content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	//	[errorAlert show];
	//	[errorAlert release];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	self.currentElement = elementName;
    
    self.buildString = [[NSMutableString alloc] initWithString:@""];
    
	if ([elementName isEqualToString:@"HotelChoice"])
	{
        self.hotelBooking = [[HotelBookingManager sharedInstance] makeNew];
	}
	else if ([elementName isEqualToString:@"ImagePair"])
	{
        self.hotelImage = (EntityHotelImage*)[NSEntityDescription insertNewObjectForEntityForName:@"EntityHotelImage" inManagedObjectContext:[HotelBookingManager sharedInstance].context];
//		self.currentImagePair = [[HotelImageData alloc] init];	// Retain count = 2
//		[self.currentImagePair release];	// Retain count = 1
	}
	else if ([elementName isEqualToString:@"CheapestRoom"])
	{
        self.cheapRoom = (EntityHotelCheapRoom*)[NSEntityDescription insertNewObjectForEntityForName:@"EntityHotelCheapRoom" inManagedObjectContext:[HotelBookingManager sharedInstance].context];
		self.inCheapestRoom = YES;
	}
	else if ([elementName isEqualToString:@"CheapestRoomWithViolation"])
	{
        self.cheapRoom = (EntityHotelCheapRoom*)[NSEntityDescription insertNewObjectForEntityForName:@"EntityHotelCheapRoom" inManagedObjectContext:[HotelBookingManager sharedInstance].context];
		self.inCheapestRoomWithViolation = YES;
        cheapRoom.isViolation = @YES;
	}
    else if ([elementName isEqualToString:@"Violation"])
	{
        self.hotelViolation = (EntityHotelViolation*)[NSEntityDescription insertNewObjectForEntityForName:@"EntityHotelViolation" inManagedObjectContext:[HotelBookingManager sharedInstance].context];
	}
    else if ([elementName isEqualToString:@"ActionStatus"])
    {
		[self setInActionStatus:YES];
    }
    else if ([elementName isEqualToString:@"HotelBenchmark"])
    {
        self.hasBenchmarks = YES;
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
//	if (currentHotelResult == nil)
//		return;
	
	if ([elementName isEqualToString:@"HotelChoice"])
	{
        [self updateCheapestRoomRate];
        // Only keep the existing booking if the result is not final. As 'isFinal' flag on existing hotel objects isn't set, they are going to be cleared at the end of parsing
        if (!hotelSearch.isFinal && self.hotelSearch.isPolling && self.existingBooking != nil && [self.existingBooking.propertyId isEqualToString:self.hotelBooking.propertyId])
        {
            self.existingBooking.isFinal = @(hotelSearch.isFinal);
            self.existingBooking.cheapestRoomRate = self.hotelBooking.cheapestRoomRate;
            self.existingBooking.isAddtional = self.hotelBooking.isAddtional;
            self.existingBooking.isNoRates = self.hotelBooking.isNoRates;
            self.existingBooking.isSoldOut = self.hotelBooking.isSoldOut;
            if (self.hotelBooking.relCheapRoom != nil)
            {
                self.existingBooking.relCheapRoom = self.hotelBooking.relCheapRoom;
            }
            if (self.hotelBooking.relCheapRoomViolation != nil)
            {
                self.existingBooking.relCheapRoomViolation = self.hotelBooking.relCheapRoomViolation;
            }
            [[HotelBookingManager sharedInstance] saveIt:self.existingBooking];
            [[HotelBookingManager sharedInstance] deleteObj:self.hotelBooking];
            self.hotelBooking = nil;
            self.existingBooking = nil;
        }
        else
        {
            self.hotelBooking.isFinal = @(hotelSearch.isFinal);
            [[HotelBookingManager sharedInstance] saveIt:self.hotelBooking];
        }
//		// TODO: Temporarily added to work around MOB-2132 for NBTA.  Need to determine
//		// and implement a permanent solution.
//		//
////		if ((currentHotelResult.cheapestRoomRate > 0 && currentHotelResult.cheapestRoomCurrencyCode != nil) ||
////			(currentHotelResult.cheapestRoomWithViolationRate > 0 && currentHotelResult.cheapestRoomWithViolationCurrencyCode != nil))
////		{
//			[hotelSearch.hotels addObject:currentHotelResult];
//			currentHotelResult = nil;
////		}
	}
	else if ([elementName isEqualToString:@"ImagePair"])
	{
        [self.hotelBooking addRelHotelImageObject:hotelImage];
	}
	else if ([elementName isEqualToString:@"CheapestRoom"])
	{
		self.inCheapestRoom = NO;
        self.hotelBooking.relCheapRoom = self.cheapRoom;
	}
	else if ([elementName isEqualToString:@"CheapestRoomWithViolation"])
	{
		self.inCheapestRoomWithViolation = NO;
        self.hotelBooking.relCheapRoomViolation = self.cheapRoom;
	}
    else if ([elementName isEqualToString:@"Violation"])
	{
        [self.cheapRoom addRelViolationObject:self.hotelViolation];
    }
    else if ([elementName isEqualToString:@"ActionStatus"])
    {
		[self setInActionStatus:NO];
    }
}

-(void) updateCheapestRoomRate
{
    if (self.hotelBooking.relCheapRoom && self.hotelBooking.relCheapRoomViolation)
    {
        if ([self.hotelBooking.relCheapRoom.rate floatValue] < [self.hotelBooking.relCheapRoomViolation.rate floatValue]) {
            self.hotelBooking.cheapestRoomRate = self.hotelBooking.relCheapRoom.rate;
            self.hotelBooking.travelPoints = self.hotelBooking.relCheapRoom.travelPoints;
        }
        else {
            self.hotelBooking.cheapestRoomRate = self.hotelBooking.relCheapRoomViolation.rate;
            self.hotelBooking.travelPoints = self.hotelBooking.relCheapRoomViolation.travelPoints;
        }
    }
    else if (self.hotelBooking.relCheapRoom)
    {
        self.hotelBooking.cheapestRoomRate = self.hotelBooking.relCheapRoom.rate;
        self.hotelBooking.travelPoints = self.hotelBooking.relCheapRoom.travelPoints;
    }
    else if (self.hotelBooking.relCheapRoomViolation)
    {
        self.hotelBooking.cheapestRoomRate = self.hotelBooking.relCheapRoomViolation.rate;
        self.hotelBooking.travelPoints = self.hotelBooking.relCheapRoomViolation.travelPoints;
    }
    if (self.hotelSearch.isPolling)
    {
        if ([self.hotelBooking.isAddtional isEqual: @NO])
        {
            if ([self.hotelBooking.cheapestRoomRate intValue] == 0)
            {
                self.hotelBooking.isAddtional = @YES;
            }
        }
        else
        {
            if ([self.hotelBooking.cheapestRoomRate intValue] > 0)
            {
                self.hotelBooking.isAddtional = @NO;
            }

        }
        
        // If we have received a rate back, then set the flag to indicate rates received
        if ([self.hotelBooking.cheapestRoomRate intValue] > 0)
        {
            self.hotelSearch.ratesFound = YES;
        }
    }
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    
    [buildString appendString:string];
    
    if([currentElement isEqualToString:@"TotalCount"])
        self.totalCount = [buildString intValue];
    else if([currentElement isEqualToString:@"PointsAvailableToSpend"])
        self.travelPointsInBank = buildString;
	else if ([currentElement isEqualToString:@"IsAdditional"])
    {
        if([buildString isEqualToString:@"false"])
            self.hotelBooking.isAddtional = @NO;
        else
            self.hotelBooking.isAddtional = @YES;
    }
	else if ([currentElement isEqualToString:@"ChainCode"])
	{
		self.hotelBooking.chainCode = buildString;
	}
	else if ([currentElement isEqualToString:@"ChainName"])
	{
		self.hotelBooking.chainName = buildString;
	}
    else if ([currentElement isEqualToString:@"TravelPoints"])
	{
		self.cheapRoom.travelPoints = @([buildString intValue]);
	}
	else if ([currentElement isEqualToString:@"PropertyId"])
	{
        // We have received a PropertyId, let's check to see if CoreData already has an entry for it
        NSString *propertyId = [buildString stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]];
        self.existingBooking =  [[HotelBookingManager sharedInstance] fetchByPropertyId:propertyId];
		self.hotelBooking.propertyId = propertyId;
	}
	else if ([currentElement isEqualToString:@"Addr1"])
	{
		self.hotelBooking.addr1 = buildString;
	}
	else if ([currentElement isEqualToString:@"Addr2"])
	{
		self.hotelBooking.addr2 = buildString;
	}
	else if ([currentElement isEqualToString:@"City"])
	{
		self.hotelBooking.city	= buildString;
	}
    else if ([currentElement isEqualToString:@"ChoiceId"])
    {
        self.hotelBooking.choiceId = buildString;
    }
// Warning: to add this feature(MOB-18969) for Gov branch, I need to modify coredata on branch
// This will *****break migration path for Gov first release to next release*****
// After talking to couple devs, since Gov is not using coredata as persistent storage
// we can remove user's coredata in first release and use a new one to avoid migration crash.
    else if ([currentElement isEqualToString:@"IsFedRoom"])
    {
        self.hotelBooking.isFedRoom = @([buildString boolValue]);
    }
	else if ([currentElement isEqualToString:@"Lat"])
	{
		self.hotelBooking.lat = @([buildString doubleValue]);
	}
	else if ([currentElement isEqualToString:@"Lng"])
	{
		self.hotelBooking.lng = @([buildString doubleValue]);
	}
	else if ([currentElement isEqualToString:@"Distance"])
	{
		self.hotelBooking.distance =  @([buildString doubleValue]);
	}
	else if ([currentElement isEqualToString:@"DistanceUnit"])
	{
		self.hotelBooking.distanceUnit = buildString;
	}
	else if ([currentElement isEqualToString:@"Hotel"])
	{
		self.hotelBooking.hotel = buildString;
	}
	else if ([currentElement isEqualToString:@"Phone"])
	{
		self.hotelBooking.phone = buildString;
	}
	else if ([currentElement isEqualToString:@"State"])
	{
		self.hotelBooking.state = buildString;
	}
	else if ([currentElement isEqualToString:@"StateAbbrev"])
	{
		self.hotelBooking.stateAbbrev = buildString;
	}
	else if ([currentElement isEqualToString:@"TollFree"])
	{
		self.hotelBooking.tollFree = buildString;
	}
	else if ([currentElement isEqualToString:@"Zip"])
	{
		self.hotelBooking.zip = buildString;
	}
	else if ([currentElement isEqualToString:@"StarRating"])
	{
//        NSLog(@"starRating %@", buildString);
		self.hotelBooking.starRating = @([buildString intValue]);
	}
    else if ([currentElement isEqualToString:@"HotelPrefRank"])
	{
		self.hotelBooking.hotelPrefRank = @([buildString intValue]);
	}
	else if ([currentElement isEqualToString:@"PropertyUri"])
	{
		NSString *propertyPath = [NSString stringWithFormat:@"%@%@", pathRoot, buildString];
		self.hotelBooking.propertyUri = propertyPath;
	}
    else if ([currentElement isEqualToString:@"DisplayValue"])
	{
		self.hotelBooking.recommendationDisplayValue = buildString;
	}
    else if ([currentElement isEqualToString:@"Source"])
	{
		self.hotelBooking.recommendationSource = buildString;
	}
    else if ([currentElement isEqualToString:@"TotalScore"])
	{
		self.hotelBooking.recommendationScore = @([buildString doubleValue]);
	}
	else if ([currentElement isEqualToString:@"Rate"])
	{
//		if (inCheapestRoom)
//			currentHotelResult.cheapestRoomRate = [buildString doubleValue];
//		else if (inCheapestRoomWithViolation)
//			currentHotelResult.cheapestRoomWithViolationRate = [buildString doubleValue];
        cheapRoom.rate = @([buildString doubleValue]);
	}
	else if ([currentElement isEqualToString:@"CrnCode"])
	{
//		if (inCheapestRoom)
//		{
//			currentHotelResult.cheapestRoomCurrencyCode = buildString;
//		}
//		else if (inCheapestRoomWithViolation)
//		{
//			currentHotelResult.cheapestRoomWithViolationCurrencyCode = buildString;
//		}
        cheapRoom.crnCode = buildString;
	}
    else if ([currentElement isEqualToString:@"DepositRequired"])
	{
        cheapRoom.depositRequired = @([buildString boolValue]);
	}

    else if ([currentElement isEqualToString:@"Image"])
        hotelImage.imageURI = buildString;
    else if ([currentElement isEqualToString:@"Thumbnail"])
        hotelImage.thumbURI = buildString;
    else if ([currentElement isEqualToString:@"IsAdditional"])
        self.hotelBooking.isAddtional = @([buildString boolValue]);
    else if ([currentElement isEqualToString:@"GdsRateErrorCode"])
    {
        if ([buildString isEqualToString:@"PropertyNotAvailable"])
            self.hotelBooking.isSoldOut = @YES;
        else if ([buildString length])
            self.hotelBooking.isNoRates = @YES;
    }
    else if ([currentElement isEqualToString:@"IsFinal"])
    {
        // Detect the IsFinal received on the final PollHotelShop results from MWS
        hotelSearch.isFinal = [buildString boolValue];
    }
    else if ([currentElement isEqualToString:@"PollingId"])
    {
        // Detect the PollingId received from Search3, which is then used for calling PollHotelShop
        hotelSearch.pollingID = buildString;
    }
    else if (self.inActionStatus)
    {
        if ([currentElement isEqualToString:@"ErrorMessage"])
        {
            [self setErrorMessage:buildString];
        }
    }
    else if ([currentElement isEqualToString:@"SystemMessage"])
    {
        [self setCommonResponseSystemMessage:buildString];
    }
    else if ([currentElement isEqualToString:@"UserMessage"])
    {
        [self setCommonResponseUserMessage:buildString];
    }
}

- (void)parserDidEndDocument:(NSXMLParser *)parser
{
    //int x = 0;
    if (hotelSearch.isFinal)
    {
        // Remove any entries which are not marked as being present in the final result set
        [[HotelBookingManager sharedInstance] deletePartialResults];
    }
    
    if (self.hasBenchmarks) {
        NSString *theXML = [[NSString alloc] initWithBytes: [self.responseData bytes] length:[self.responseData length]  encoding:NSUTF8StringEncoding];
        self.hotelBenchmarks = [HotelBenchmarkData getHotelBenchmarksFromXml:theXML atPath:@"/MWSResponse/Response/BenchmarksCollection/Benchmarks/HotelBenchmark"];
    }
    self.responseData = nil;
}



@end

