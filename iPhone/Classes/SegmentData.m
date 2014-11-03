//
//  SegmentData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/16/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "SegmentData.h"
#import "DateTimeFormatter.h"
#import "EntitySegment.h"
#import "EntityFlightStats.h"
#import "EntitySegmentLocation.h"

@implementation SegmentData

@synthesize type;
@synthesize startDateLocal;
@synthesize cliqbookId;
@synthesize legId;
@synthesize startDateUTC;
@synthesize confirmationNumber;
@synthesize flightNumber;
@synthesize duration;
@synthesize startCityCode;
@synthesize endCityCode;
@synthesize startTerminal;
@synthesize endTerminal;
@synthesize startGate;
@synthesize endGate;
@synthesize vendor;
@synthesize idKey;
@synthesize bookingKey;
@synthesize operatedBy;
@synthesize operatedByFlightNumber;
@synthesize endDateLocal;
@synthesize status;
@synthesize numStops;
@synthesize aircraftCode;
@synthesize classOfService;
@synthesize meals;
@synthesize specialInstructions;

@synthesize startLocation;
@synthesize parkingName;
@synthesize parkingLocationId;
@synthesize startAddress;
@synthesize startCity;
@synthesize startPostalCode;
@synthesize startState; 
@synthesize startCountry;
@synthesize phoneNumber;
@synthesize totalRate;
@synthesize currency;
@synthesize pin;

@synthesize dailyRate;
@synthesize cancellationPolicy;
@synthesize roomDescription;
@synthesize hotelName;

@synthesize pickupInstructions;
@synthesize meetingInstructions;
@synthesize endAddress;
@synthesize endCity;
@synthesize endState;
@synthesize endPostalCode;
@synthesize dropoffInstructions;
@synthesize rateDescription;

@synthesize startLatitude;
@synthesize startLongitude;
@synthesize endLatitude;
@synthesize endLongitude;

@synthesize vendorName;

@synthesize reservationId;
@synthesize numPersons;
@synthesize startAddress2;

@synthesize trainNumber;
@synthesize operatedByVendor;
@synthesize operatedByTrainNumber;
@synthesize startPlatform;
@synthesize startRailStation;
@synthesize wagonNumber;
@synthesize amenities;
@synthesize endRailStation;
@synthesize endPlatform;
@synthesize trainTypeCode;
@synthesize cabin;

@synthesize miles;
@synthesize seatNumber;

@synthesize segmentName;
@synthesize startCityCodeLocalized;
@synthesize endCityCodeLocalized;
@synthesize classOfServiceLocalized;

@synthesize startAirportCity ;
@synthesize startAirportName ;
@synthesize startAirportState ;
@synthesize startAirportCountry ;
@synthesize endAirportCity ;
@synthesize endAirportName ;
@synthesize endAirportState;
@synthesize endAirportCountry;

@synthesize rateType;
@synthesize bodyType;
@synthesize bodyTypeName;
@synthesize transmission;
@synthesize airCond;
@synthesize discountCode;
@synthesize specialEquipment;

@synthesize startAirportCountryCode;
@synthesize endAirportCountryCode;
@synthesize classOfCar;

@synthesize numRooms;
@synthesize imageCarURI;
@synthesize imageVendorURI, eTicket, numCars, classOfCarLocalized;
@synthesize flightStats, gdsId, propertyId, bookSource, statusLocalized, startRailStationLocalized, endRailStationLocalized;

static NSMutableDictionary* mtgAttributeMap = nil;
static NSMutableDictionary* diningAttributeMap = nil;

// Initialize msgId to msg class mapping here
+ (void)initialize
{
	if (self == [SegmentData class]) 
	{
        // Perform initialization here.
        mtgAttributeMap = [[NSMutableDictionary alloc] init];
		
        mtgAttributeMap[@"ContactName"] = @"DescriptionSeg";
        mtgAttributeMap[@"AccountName"] = @"VendorName";
        mtgAttributeMap[@"OpportunityName"] = @"Vendor";
        mtgAttributeMap[@"OpportunityId"] = @"Amenities";
        
        diningAttributeMap = [[NSMutableDictionary alloc] init];
		
        diningAttributeMap[@"VendorName"] = @"VendorName";
        diningAttributeMap[@"Address"] = @"Vendor";
    }
}



 - (id) valueForKeyPath:(id) inKey
 {
	 return startDateUTC;
//	 if ([inKey isEqualToString: @"model.pageNumber"])
//	 return [model pageNumber];
//	 else
//	 return [super valueForKeyPath: inKey];
 }


-(NSComparisonResult) startDateCompare:(SegmentData *)seg
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// specify timezone
	[dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterFullStyle];
	[dateFormatter setTimeStyle:NSDateFormatterFullStyle];
	NSDate *dateSelf = [DateTimeFormatter getNSDateFromMWSDateString:self.startDateUTC];//[DateTimeFormatter getNSDate:self.startDateUTC Format:@"yyyy-MM-dd'T'HH:mm:ss"]; // [NSDate dateWithNaturalLanguageString:self.startDateUTC locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
	NSDate *datePassedIn = [DateTimeFormatter getNSDateFromMWSDateString:seg.startDateUTC];//[DateTimeFormatter getNSDate:seg.startDateUTC Format:@"yyyy-MM-dd'T'HH:mm:ss"];// [NSDate dateWithNaturalLanguageString:seg.startDateUTC locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
	
	if ([dateSelf isEqualToDate:datePassedIn])
	{
		return NSOrderedSame;
	}
	else if([dateSelf earlierDate:datePassedIn])
	{
		return NSOrderedDescending;
	}
	else 
	{
		return NSOrderedAscending;
	}
}

+(void) getArriveTimeString:(EntitySegment*) segment timeStr:(NSMutableString*) time dateStr:(NSMutableString*) date
{
    if(segment.relFlightStats.arrivalActual != nil)
    {
        if (time != nil)
            [time appendString:[DateTimeFormatter formatTimeForTravel:segment.relFlightStats.arrivalActual]];
      
        if (date != nil)
            [date appendString:[DateTimeFormatter formatDateForTravel:segment.relFlightStats.arrivalActual]];
    }
    else if(segment.relFlightStats.arrivalEstimated != nil)
    {
        if (time != nil)
            [time appendString:[DateTimeFormatter formatTimeForTravel:segment.relFlightStats.arrivalEstimated]];
        
        if (date != nil)
            [date appendString: [DateTimeFormatter formatDateForTravel:segment.relFlightStats.arrivalEstimated]];
    }
    else if (segment.relEndLocation.dateLocal != nil)
    {
        if (time != nil)
            [time appendString: [DateTimeFormatter formatTimeForTravel:segment.relEndLocation.dateLocal]];
        
        if (date != nil)
            [date appendString: [DateTimeFormatter formatDateForTravel:segment.relEndLocation.dateLocal]];
    }

}

+(void) getDepartTimeString:(EntitySegment*) segment timeStr:(NSMutableString*) time dateStr:(NSMutableString*) date
{
    if(segment.relFlightStats.departureActual != nil)
    {
        if (time != nil)
            [time appendString:[DateTimeFormatter formatTimeForTravel:segment.relFlightStats.departureActual]];
        
        if (date != nil)
            [date appendString:[DateTimeFormatter formatDateForTravel:segment.relFlightStats.departureActual]];
    }
    else if(segment.relFlightStats.departureEstimated != nil)
    {
        if (time != nil)
            [time appendString:[DateTimeFormatter formatTimeForTravel:segment.relFlightStats.departureEstimated]];
        
        if (date != nil)
            [date appendString: [DateTimeFormatter formatDateForTravel:segment.relFlightStats.departureEstimated]];
    }
    else if (segment.relStartLocation.dateLocal != nil)
    {
        if (time != nil)
            [time appendString: [DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal]];
        
        if (date != nil)
            [date appendString: [DateTimeFormatter formatDateForTravel:segment.relStartLocation.dateLocal]];
    }
    
}

+(NSString *)getAirportFullName:(EntitySegmentLocation *)segLoc
{
    NSString *result = nil;
    if (segLoc.airportState == nil || [segLoc.airportState isEqualToString:@""])
		result = [NSString stringWithFormat:@"(%@) %@", segLoc.cityCode, segLoc.airportName];
	else
		result = [NSString stringWithFormat:@"(%@) %@, %@", segLoc.cityCode, segLoc.airportName, segLoc.airportState];
    return result;
}

+(NSString *)getAirportNameCode:(EntitySegmentLocation *)segLoc
{
    __autoreleasing NSMutableString *loName = [[NSMutableString alloc] initWithString:segLoc.airportName];
    //Logan Intl Arpt, Boston, MA (BOS)
    if (segLoc.airportCity!=nil)
    {
        [loName appendString:@", "];
        [loName appendString:segLoc.airportCity];
    }
    if (segLoc.airportState!= nil)
    {
        [loName appendString:@", "];
        [loName appendString:segLoc.airportState];
    }
    if (segLoc.cityCode != nil)
    {
        [loName appendString:@" ("];
        [loName appendString:segLoc.cityCode];
        [loName appendString:@")"];
    }
    return loName;
}

+(NSString *)getAirportFullAddress:(EntitySegmentLocation *)segLoc
{
    NSString *airportAddress = @"";
    if (segLoc.airportName != nil)
        airportAddress = [airportAddress stringByAppendingFormat:@"%@", segLoc.airportName];
    if (segLoc.airportCity != nil)
        airportAddress = [airportAddress stringByAppendingFormat:@", %@", segLoc.airportCity];
    if (segLoc.airportState != nil)
        airportAddress = [airportAddress stringByAppendingFormat:@", %@", segLoc.airportState];
    if (segLoc.airportCountry != nil)
        airportAddress = [airportAddress stringByAppendingFormat:@", %@", segLoc.airportCountry];
    return airportAddress;
}

+(void) getArriveTermGate:(EntitySegment*) segment terminal:(NSMutableString *) termStr gate:(NSMutableString*) gateStr
{
    NSString *term = segment.relEndLocation.terminal;
    NSString *gate = segment.relEndLocation.gate;
    
    if(segment.relFlightStats.arrivalTerminalScheduled != nil)
        term = segment.relFlightStats.arrivalTerminalScheduled;
    
    if(segment.relFlightStats.arrivalTerminalActual != nil)
        term = segment.relFlightStats.arrivalTerminalActual;
    
    if(segment.relFlightStats.arrivalGate != nil)
        gate = segment.relFlightStats.arrivalGate;
    
    if (term == nil)
        term = @"--";
    
    if (gate == nil)
        gate = @"--";
    
    if (term != nil && termStr != nil)
        [termStr setString:term];
    
    if (gate != nil && gateStr != nil)
        [gateStr setString:gate];
}

+(void) getDepartTermGate:(EntitySegment*) segment terminal:(NSMutableString *) termStr gate:(NSMutableString*) gateStr
{
    NSString *term = segment.relStartLocation.terminal;
    NSString *gate = segment.relStartLocation.gate;
    
    if(segment.relFlightStats.departureTerminalScheduled != nil)
        term = segment.relFlightStats.departureTerminalScheduled;
    if(segment.relFlightStats.departureTerminalActual != nil)
        term = segment.relFlightStats.departureTerminalActual;
    
    if(segment.relFlightStats.departureGate != nil)
        gate = segment.relFlightStats.departureGate;
    
    if (term == nil)
        term = @"--";
    
    if (gate == nil)
        gate = @"--";

    if (term != nil)
        [termStr setString:term];
    
    if (gate != nil)
        [gateStr setString:gate];
}

+(NSString *)getAirportCity:(EntitySegmentLocation *)segLoc
{
    __autoreleasing NSMutableString *location = [[NSMutableString alloc] initWithString:@""];
    if(segLoc.cityCode != nil)
        [location appendString:[NSString stringWithFormat:@"(%@)", segLoc.cityCode]];
    
    if(segLoc.airportCity != nil)
        [location appendString:[NSString stringWithFormat:@" %@", segLoc.airportCity]];

    return location;
}

+(NSString *)getCityState:(EntitySegmentLocation *)segLoc
{
    __autoreleasing NSMutableString *location = [[NSMutableString alloc] initWithString:@""];
    if(segLoc.city != nil)
        [location appendString:segLoc.city];
    if(segLoc.state != nil)
        [location appendString:[NSString stringWithFormat:@", %@", segLoc.state]];
    
    return location;
}

+(NSString *)getCityStateZip:(EntitySegmentLocation *)segLoc
{
    __autoreleasing NSMutableString *location = [[NSMutableString alloc] initWithString:@""];
    if(segLoc.city != nil)
        [location appendString: segLoc.city];
    
    if(segLoc.state != nil)
    {
        if([location length] > 0)
            [location appendString:@" "];
        [location appendString: segLoc.state];
    }
    
    if(segLoc.postalCode != nil)
    {
        if([location length] > 0)
            [location appendString:@" "];
        [location appendString: segLoc.postalCode];
    }
    return location;
}

+(NSString *)getRailStation:(EntitySegmentLocation *)segLoc
{
    NSString *railStation = segLoc.railStationLocalized;
    if(railStation == nil)
        railStation = segLoc.railStation;
    
    if (railStation == nil && segLoc.railStation == nil && segLoc.cityCode != nil) 
        railStation = segLoc.cityCode;
    else if (railStation == nil)
        railStation = [Localizer getLocalizedText:@"Unknown Station"];

    return railStation;
}

// YES/YES @"%@\n%@, %@, %@", NO/YES @"%@, %@, %@ %@", NO/NO @"%@ %@ %@ %@"
+(NSString *)getMapAddress:(EntitySegmentLocation *)segLoc withLineBreaker:(BOOL) flb withDelimitor:(BOOL) fdel
{
    NSString *location = @"";
    
    if(segLoc.address != nil)
        location = [NSString stringWithFormat:@"%@%@", segLoc.address, (flb?@"\n":(fdel?@", ":@" "))];
    
    if(segLoc.city != nil)
        location = [NSString stringWithFormat:@"%@%@%@", location, segLoc.city, (fdel ? @",":@"")];
    
    if(segLoc.state != nil)
        location = [NSString stringWithFormat:@"%@ %@%@", location, segLoc.state, (fdel && flb ? @",":@"")];
    
    if(segLoc.postalCode)
        location = [NSString stringWithFormat:@"%@ %@", location, segLoc.postalCode];
    
    return location;
}

+(NSString *)getMapAddress:(EntitySegmentLocation *)segLoc
{
    return [self getMapAddress:segLoc withLineBreaker:YES withDelimitor:YES];
}
@end
