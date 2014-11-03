//
//  ItineraryImport.m
//  ConcurMobile
//
//  Created by Wes Barton on 5/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryImport.h"
#import "CXRequest.h"
#import "RXMLElement.h"
#import "ItineraryImportRow.h"
#import "ItineraryStop.h"
#import "Itinerary.h"

@implementation ItineraryImport

+ (NSDate *)getNSDateFromImport:(NSString *)dateString
{
    NSDateFormatter *dateFormatter= [self getDateFormatter];
//    NSLog(@"dateString = %@", dateString);
    return [dateFormatter dateFromString:dateString];
}

+ (NSDateFormatter *)getDateFormatter {
    static NSDateFormatter *dateFormatter = nil; // Caching- Creating a date formatter is not a cheap operation and this one does not depend on UserSettings so it is not going to change
    if (dateFormatter == nil) {
        NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];

        dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setLocale:enUSPOSIXLocale];

        NSTimeZone *tz = [NSTimeZone timeZoneForSecondsFromGMT:0];
//        NSTimeZone *tzSystem = [NSTimeZone systemTimeZone];
        NSLog(@"tz = %@", tz);
//        NSLog(@"tzSystem = %@", tzSystem);

        [dateFormatter setTimeZone:tz]; // GMT time Zone

        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss.S"];
    }
    return dateFormatter;
}

- (id)initWithXML:(RXMLElement *)header
{
    self.rows = [[NSMutableArray alloc] init];
//    <Header>
//    <EndDate>2014-04-23 17:00:00.0</EndDate>
    self.endDate = [ItineraryImport getNSDateFromImport:[ header child:@"EndDate"].text];
//    <EndDateNoTime>2014-04-23 00:00:00.0</EndDateNoTime>
//    <HasAir>true</HasAir>
    self.hasAir = (BOOL) [[ header child:@"HasAir"].text boolValue];
//    <HasHotel>true</HasHotel>
    self.hasHotel = (BOOL) [[ header child:@"HasHotel"].text boolValue];
//    <HasRail>false</HasRail>
    self.hasRail = (BOOL) [[ header child:@"HasHotel"].text boolValue];
//    <HeaderType>Trip</HeaderType>
    self.headerType = [ header child:@"HeaderType"].text;
//    <StartDate>2014-04-21 12:00:00.0</StartDate>
    self.startDate = [ItineraryImport getNSDateFromImport:[ header child:@"StartDate"].text];
//    <StartDateNoTime>2014-04-21 00:00:00.0</StartDateNoTime>
//    <TaImportId>T3113</TaImportId>
    self.taImportId = [ header child:@"TaImportId"].text;
//    <ToBeDeleted>false</ToBeDeleted>
    self.hasAir = (BOOL) [[ header child:@"HasAir"].text boolValue];
//    <TripId>3113</TripId>
    self.tripId = [ header child:@"TripId"].text;
//    <TripName>Demo Trip (DEMO00)</TripName>
    self.tripName = [ header child:@"TripName"].text;
//    </Header>

    self.include = NO;

    return self;
}

+ (CXRequest *)getTravelAllowanceImport:(NSString *)reportKey
{

    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/GetTravelAllowanceImport/%@", reportKey];

    // Create the request
    return [[CXRequest alloc] initWithServicePath:path];

}

+ (NSMutableArray *)parseImportXML:(NSString *)result
{
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

    NSMutableArray *arrayOfHeaders = [[NSMutableArray alloc]init];
    NSMutableArray *arrayOfRows = [[NSMutableArray alloc]init];

    RXMLElement *body = [rootXML child:@"Body"];
    
    RXMLElement *headersX = [body child:@"Headers"];
    NSArray *headers = [headersX children:@"Header"];
    for (RXMLElement *headerX in headers)
    {
        ItineraryImport *header = [[ItineraryImport alloc] initWithXML:headerX];
        [arrayOfHeaders addObject:header];
    }

    RXMLElement *rowsX = [body child:@"Rows"];
    NSArray *rows = [rowsX children:@"Row"];
    for (RXMLElement *rowX in rows) {
        ItineraryImportRow *row = [[ItineraryImportRow alloc] initWithXML:rowX];
        [arrayOfRows addObject:row];
    }

    //Match rows to headers
    for (ItineraryImportRow *row in arrayOfRows) {
        for (ItineraryImport *header in arrayOfHeaders) {
            if([header.tripId isEqualToString:row.tripId])
            {
                //                NSLog(@"header %@ to %@", header.startDate, header.endDate);
                NSLog(@"row.legDate = %@", row.legDate);
                NSLog(@"row.segmentType = %@", row.segmentType);
//                NSLog(@"row.first = %d", row.first);
//                NSLog(@"row.last = %d", row.last);
                if([row.segmentType isEqualToString:@"AIR"])
                {
                    [header.rows addObject:row];
                }
            }
        }
    }

    return arrayOfHeaders;
}

+ (CXRequest *)getImportTravelAllowanceItinerary
{
    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/ImportTravelAllowanceItinerary"];

    // Create the request
//    return [CXAuthenticatedRequest requestWithMethod:@"POST" path:path];
    return [[CXRequest alloc] initWithServicePath:path];
}

+ (NSString *)composeImportTravelAllowanceItinerary:(NSMutableArray *)importIds rptKey:(NSString *)rptKey itinKey:(NSString *)itinKey role:(NSString *)role
{
    NSMutableString *block = [[NSMutableString alloc]init];
    [block appendString:@"<ImportItinerary>"];
    [block appendString:[NSString stringWithFormat:@"<RptKey>%@</RptKey>",rptKey]];
    if(itinKey != nil) //Add to an existing Itinerary
    {
        [block appendString:[NSString stringWithFormat:@"<ItinKey>%@</ItinKey>",itinKey]];
    }
    [block appendString:[NSString stringWithFormat:@"<Role>%@</Role>",role]];
    [block appendString:@"<TripIds>"];
    for (NSString *importId in importIds) {
        [block appendString:[NSString stringWithFormat:@"<TripId>%@</TripId>", importId]];
    }
    [block appendString:@"</TripIds>"];
    [block appendString:@"</ImportItinerary>"];

    return block;
}


/*
<?xml version="1.0" encoding="UTF-8"?><Response><Header><Version>1.0</Version><Log><Level>None</Level></Log><TravelerUID>55540</TravelerUID><ExpenseUID>803</ExpenseUID><CliqSessionID>7DA5EB4C-5557-40F8-B658-0653A810000F</CliqSessionID><LoginID>boss@outtask.com</LoginID><EntityID>phos123488</EntityID><CompanyID>1</CompanyID><SUVersion>103.0</SUVersion><IsMobile>Y</IsMobile><IsTestUser>N</IsTestUser><SkipVersionCheck>Y</SkipVersionCheck><HmcUserKey />
<RequestOrigin>MOBILE</RequestOrigin></Header><Body>
<Headers>
<Header>
<EndDate>2014-04-23 17:00:00.0</EndDate>
<EndDateNoTime>2014-04-23 00:00:00.0</EndDateNoTime>
<HasAir>true</HasAir>
<HasHotel>true</HasHotel>
<HasRail>false</HasRail>
<HeaderType>Trip</HeaderType>
<StartDate>2014-04-21 12:00:00.0</StartDate>
<StartDateNoTime>2014-04-21 00:00:00.0</StartDateNoTime>
<TaImportId>T3113</TaImportId>
<ToBeDeleted>false</ToBeDeleted>
<TripId>3113</TripId>
<TripName>Demo Trip (DEMO00)</TripName>
</Header>
</Headers><Rows><Row><AirportName>Washington Dulles Intl</AirportName><City>Washington</City><CountryCode>US</CountryCode><First>true</First><FirstOrLast>true</FirstOrLast><HideFromList>false</HideFromList><Iata>IAD</Iata><IataCode>IAD</IataCode><IataType>A</IataType><IsConnection>false</IsConnection><Last>false</Last><LegDate>2014-04-21 12:00:00.0</LegDate><LegDateNoTime>2014-04-21 00:00:00.0</LegDateNoTime><LegType>Depart</LegType><LnKey>31038</LnKey><Location>Washington</Location><RowType>Trip</RowType><SegmentType>AIR</SegmentType><State>DC</State><ToBeDeleted>false</ToBeDeleted>
<TripId>T3113</TripId></Row><Row><City>Alexandria</City><CountryCode>US</CountryCode><First>false</First><FirstOrLast>false</FirstOrLast><HideFromList>false</HideFromList><Iata>LAX</Iata><IsConnection>false</IsConnection><ItinSourceId>0</ItinSourceId><Last>false</Last><LegDate>2014-04-21 16:59:59.0</LegDate><LegDateNoTime>2014-04-21 00:00:00.0</LegDateNoTime><LegType>Arrival</LegType><LnKey>24350</LnKey><Location>Alexandria, Virginia</Location><RowType>Trip</RowType><SegmentType>HOTEL</SegmentType><State>VA</State><TaImportId>T3113</TaImportId><ToBeDeleted>false</ToBeDeleted>
<TripId>3113</TripId><ZipCode>22201</ZipCode></Row><Row><AirportName>Los Angeles Intl</AirportName><City>Los Angeles</City><CountryCode>US</CountryCode><First>false</First><FirstOrLast>false</FirstOrLast><HideFromList>false</HideFromList><Iata>LAX</Iata><IataCode>LAX</IataCode><IataType>A</IataType><IsConnection>false</IsConnection><Last>false</Last><LegDate>2014-04-22 05:00:00.0</LegDate><LegDateNoTime>2014-04-22 00:00:00.0</LegDateNoTime><LegType>Arrival</LegType><LnKey>27447</LnKey><Location>Los Angeles</Location><RowType>Trip</RowType><SegmentType>AIR</SegmentType><State>CA</State><TaImportId>T3113</TaImportId><ToBeDeleted>false</ToBeDeleted>
<TripId>3113</TripId></Row><Row><City>Alexandria</City><CountryCode>US</CountryCode><First>false</First><FirstOrLast>false</FirstOrLast><HideFromList>false</HideFromList><Iata>LAX</Iata><IsConnection>false</IsConnection><ItinSourceId>0</ItinSourceId><Last>false</Last><LegDate>2014-04-23 11:00:00.0</LegDate><LegDateNoTime>2014-04-23 00:00:00.0</LegDateNoTime><LegType>Depart</LegType><LnKey>24350</LnKey><Location>Alexandria, Virginia</Location><RowType>Trip</RowType><SegmentType>HOTEL</SegmentType><State>VA</State><TaImportId>T3113</TaImportId><ToBeDeleted>false</ToBeDeleted>
<TripId>3113</TripId><ZipCode>22201</ZipCode></Row><Row><AirportName>Los Angeles Intl</AirportName><City>Los Angeles</City><CountryCode>US</CountryCode><First>false</First><FirstOrLast>false</FirstOrLast><HideFromList>false</HideFromList><Iata>LAX</Iata><IataCode>LAX</IataCode><IataType>A</IataType><IsConnection>false</IsConnection><Last>false</Last><LegDate>2014-04-23 12:00:00.0</LegDate><LegDateNoTime>2014-04-23 00:00:00.0</LegDateNoTime><LegType>Depart</LegType><LnKey>27447</LnKey><Location>Los Angeles</Location><RowType>Trip</RowType><SegmentType>AIR</SegmentType><State>CA</State><ToBeDeleted>false</ToBeDeleted>
<TripId>T3113</TripId></Row><Row><AirportName>Washington Dulles Intl</AirportName><City>Washington</City><CountryCode>US</CountryCode><First>false</First><FirstOrLast>true</FirstOrLast><HideFromList>false</HideFromList><Iata>IAD</Iata><IataCode>IAD</IataCode><IataType>A</IataType><IsConnection>false</IsConnection><Last>true</Last><LegDate>2014-04-24 05:00:00.0</LegDate><LegDateNoTime>2014-04-24 00:00:00.0</LegDateNoTime><LegType>Arrival</LegType><LnKey>31038</LnKey><Location>Washington</Location><RowType>Trip</RowType><SegmentType>AIR</SegmentType><State>DC</State><TaImportId>T3113</TaImportId><ToBeDeleted>false</ToBeDeleted>
<TripId>3113</TripId></Row></Rows></Body></Response>
  */

/*
<Body>
<ModifiedReports/>
<Itinerary>
    <ItinKey>40</ItinKey>
    <Name> one</Name>
    <ShortDistanceTrip> N</ShortDistanceTrip>
    <EmpKey>803</EmpKey>
    <TacKey>7</TacKey>
    <TacName> GenericStd Fixed/Fixed</TacName>
    <IsLocked> N</IsLocked>
    <AreAllRowsLocked> N</AreAllRowsLocked>
    <ItineraryRows>
        <ItineraryRow>
            <Status> SUCCESS</Status>
            <StatusText> TravelAllowance.ValidateAndSaveItineraryRow.Success</StatusText>
            <IrKey>63</IrKey>
            <ArrivalDateTime>2014-05-13 17:00</ArrivalDateTime>
            <ArrivalLocation> Los Angeles, California</ArrivalLocation>
            <ArrivalLnKey>27447</ArrivalLnKey>
            <DepartDateTime>2014-05-13 12:00</DepartDateTime>
            <DepartLocation> Washington, District of Columbia</DepartLocation>
            <DepartLnKey>31038</DepartLnKey>
            <ArrivalRlKey>47</ArrivalRlKey>
            <ArrivalRateLocation> Los Angeles, California, US</ArrivalRateLocation>
            <IsRowLocked> N</IsRowLocked>
        </ItineraryRow>
        <ItineraryRow>
            <Status> SUCCESS</Status>
            <StatusText> TravelAllowance.ValidateAndSaveItineraryRow.Success</StatusText>
            <IrKey>64</IrKey>
            <ArrivalDateTime>2014-05-15 17:00</ArrivalDateTime>
            <ArrivalLocation> Washington, District of Columbia</ArrivalLocation>
            <ArrivalLnKey>31038</ArrivalLnKey>
            <DepartDateTime>2014-05-15 12:00</DepartDateTime>
            <DepartLocation> Los Angeles, California</DepartLocation>
            <DepartLnKey>27447</DepartLnKey>
            <ArrivalRlKey>44</ArrivalRlKey>
            <ArrivalRateLocation> UNITED STATES</ArrivalRateLocation>
            <IsRowLocked> N</IsRowLocked>
        </ItineraryRow>
    </ItineraryRows>
</Itinerary>
</Body>
  */

+ (NSMutableArray *)parseItineraryImportResult:(NSString *)result rptKey:(NSString *)rptKey
{
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

    NSMutableArray *arrayOfItineraries = [[NSMutableArray alloc]init];

    RXMLElement *body = [rootXML child:@"Body"];
    // Restrict to only one Itinerary
    NSArray *itins = [body children:@"Itinerary"];
    for (RXMLElement *itin in itins)
    {
        Itinerary *itinerary = [Itinerary processItineraryXML:itin rptKey:rptKey];

//        itinerary.crnCode = crnCode;

        NSMutableArray *stops = [[NSMutableArray alloc] init];
        [ItineraryStop processItineraryRowXML:itin stops:stops];
        itinerary.stops = stops;

        [arrayOfItineraries addObject:itinerary];
    }
    return arrayOfItineraries;
}
@end