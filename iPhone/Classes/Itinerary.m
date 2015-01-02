//
//  Itinerary.m
//  ConcurMobile
//
//  Created by Wes Barton on 2/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "Itinerary.h"
#import "ItineraryStop.h"
#import "CXRequest.h"
#import "ItineraryConfig.h"

@implementation Itinerary

/*
<Itinerary>
<ItinKey> nNYL70NG$pPPsWPqO5FlaeL1s</ItinKey>
<Name> TA 2</Name>
<ShortDistanceTrip> N</ShortDistanceTrip>
<EmpKey>37</EmpKey>
<TacKey>7</TacKey>
<TacName> GenericStd Fixed/Fixed</TacName>
<DepartDateTime>2013-12-19 12:00</DepartDateTime>
<DepartLocation> Seattle, Washington</DepartLocation>
<ArrivalDateTime>2013-12-23 18:00</ArrivalDateTime>
<ArrivalLocation> San Jose, California</ArrivalLocation>
<IsLocked> N</IsLocked>
<AreAllRowsLocked> N</AreAllRowsLocked>
*/


+ (Itinerary *)processItineraryXML:(RXMLElement *)itin rptKey:(NSString *)rptKey {
    Itinerary *itinerary = [[Itinerary alloc]init];

    NSString *itinKey = [itin child:@"ItinKey"].text;
    itinerary.itinKey = itinKey;

    NSString *itinName = [itin child:@"Name"].text;
    itinerary.itinName = itinName;

    NSString *tacKey = [itin child:@"TacKey"].text;
    itinerary.tacKey = tacKey;

    itinerary.rptKey = rptKey;

    NSString *shortDistanceTrip = [itin child:@"ShortDistanceTrip"].text;
    itinerary.shortDistanceTrip = shortDistanceTrip;

    NSString *areAllRowsLocked = [itin child:@"AreAllRowsLocked"].text;
    itinerary.areAllRowsLocked = areAllRowsLocked;

    NSString *tripLength = [itin child:@"TripLength"].text;
    itinerary.tripLength = tripLength;

    NSString *tacName = [itin child:@"TacName"].text;
    itinerary.tacName = tacName;

    itinerary.isCollapsed = NO;

    return itinerary;
}

+ (void)composeItineraryElements:(Itinerary *)itinerary block:(NSMutableString *)block
{
    [block appendString:[NSString stringWithFormat:@"<Name>%@</Name>",itinerary.itinName]];

    if(itinerary.itinKey != nil) {
        [block appendString:[NSString stringWithFormat:@"<ItinKey>%@</ItinKey>", itinerary.itinKey]];
    }
    else
    {
        [block appendString:@"<ItinKey/>"];
    }

    if(itinerary.tacKey != nil) {
        [block appendString:[NSString stringWithFormat:@"<TacKey>%@</TacKey>", itinerary.tacKey]];
    }

    if(itinerary.tacName != nil)
    {
        [block appendString:[NSString stringWithFormat:@"<TacName>%@</TacName>", itinerary.tacName]];
    }

    if(itinerary.tripLength != nil) {
        [block appendString:[NSString stringWithFormat:@"<TripLength>%@</TripLength>", itinerary.tripLength]];
    }

    if(itinerary.shortDistanceTrip != nil) {
        [block appendString:[NSString stringWithFormat:@"<ShortDistanceTrip>%@</ShortDistanceTrip>", itinerary.shortDistanceTrip]];
    }
    else
    {
        [block appendString:@"<ShortDistanceTrip>N</ShortDistanceTrip>"];
    }

    [block appendString:[NSString stringWithFormat:@"<RptKey>%@</RptKey>",itinerary.rptKey]];
}


+ (NSString *)composeUpdateItinerarySummary:(Itinerary *)itinerary formatter:(NSDateFormatter *)formatter
{
    NSMutableString *block = [[NSMutableString alloc] init];

    [block appendString:@"<Itinerary>"];
    [block appendString:[NSString stringWithFormat:@"<Name>%@</Name>",itinerary.itinName]];

    if(itinerary.itinKey != nil) {
        [block appendString:[NSString stringWithFormat:@"<ItinKey>%@</ItinKey>", itinerary.itinKey]];
    }

    [block appendString:[NSString stringWithFormat:@"<TacKey>%@</TacKey>",itinerary.tacKey]];
    [block appendString:[NSString stringWithFormat:@"<TacName>%@</TacName>",itinerary.tacName]];
    if(itinerary.tripLength != nil) {
        [block appendString:[NSString stringWithFormat:@"<TripLength>%@</TripLength>", itinerary.tripLength]];
    }
    if(itinerary.shortDistanceTrip != nil) {
        [block appendString:[NSString stringWithFormat:@"<ShortDistanceTrip>%@</ShortDistanceTrip>", itinerary.shortDistanceTrip]];
    }
    [block appendString:[NSString stringWithFormat:@"<RptKey>%@</RptKey>",itinerary.rptKey]];
    [block appendString:@"<ItineraryRows/>"];
    [block appendString:@"</Itinerary>"];

    return block;
}

+ (NSString *)composeItineraryElementWithRow:(ItineraryStop *)stop itinerary:(Itinerary *)itinerary formatter:(NSDateFormatter *)formatter {
    NSMutableString *block = [[NSMutableString alloc] init];
    [block appendString:@"<Itinerary>"];
    [self composeItineraryElements:itinerary block:block];
    if(stop != nil){
        [block appendString:@"<ItineraryRows>"];
        [block appendString:[ItineraryStop composeItineraryRow:stop formatter:formatter]];
        [block appendString:@"</ItineraryRows>"];
    }
    else{
        [block appendString:@"<ItineraryRows/>"];
    }
    [block appendString:@"</Itinerary>"];
    return block;
}


+ (NSString *)composeItineraryWithRows:(Itinerary *)itinerary formatter:(NSDateFormatter *)formatter
{
    NSMutableString *block = [[NSMutableString alloc] init];

    [block appendString:@"<Itinerary>"];
    [self composeItineraryElements:itinerary block:block];
    [self composeItineraryRowsElement:itinerary formatter:formatter block:block];
    [block appendString:@"</Itinerary>"];

    return block;
}

+ (void)composeItineraryRowsElement:(Itinerary *)itinerary formatter:(NSDateFormatter *)formatter block:(NSMutableString *)block
{
    if(itinerary.stops != nil || [itinerary.stops count]>0)
    {
        [block appendString:@"<ItineraryRows>"];
        for (ItineraryStop *stop in itinerary.stops) {
            [block appendString:[ItineraryStop composeItineraryRow:stop formatter:formatter]];
        }
        [block appendString:@"</ItineraryRows>"];
    }
    else
    {
        [block appendString:@"<ItineraryRows/>"];
    }
}


+ (NSString *)parseSaveResultForStatus:(NSString *)result
{
    NSLog(@"parse the save result");

    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

    RXMLElement *body = [rootXML child:@"Body"];

    // Restrict to only one Itinerary
    NSArray *itins = [body children:@"Itinerary"];

    if ([itins count] > 0)
    {
        if ([itins count] > 1)
        {
            NSLog(@"Too many Itineraries");
        }
        RXMLElement *itin =[itins objectAtIndex:0];
        RXMLElement *itineraryRows = [itin child:@"ItineraryRows"];

        NSArray *rows = [itineraryRows children:@"ItineraryRow"];
        NSLog(@"Itinerary Rows in Save [rows count] = %lu", (unsigned long)[rows count]);
        for (RXMLElement *row in rows) {
            NSString *status = [row child:@"Status"].text;
            NSLog(@"record save status = %@", status);
            return status;
        }

        return @"SUCCESS";
    }

    return nil;
}

+ (NSString *)parseSaveResultForStatusText:(NSString *)result
{
    return nil;
}

+ (CXRequest *)deleteItinerary:(NSString *)itinKey rptKey:(NSString *)rptKey
{
    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/DeleteItinerary/%@/%@", itinKey, rptKey];

    // Create the request
    return [[CXRequest alloc] initWithServicePath:path];
}


+ (CXRequest *)unassignItinerary:(NSString *)itinKey rptKey:(NSString *)rptKey
{
    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/UnAssignItinerary/%@/%@", itinKey, rptKey];
//    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/UnAssignItinerary/%@", itinKey];

    NSLog(@"path = %@", path);

    // Create the request
    return [[CXRequest alloc] initWithServicePath:path];

}

+ (CXRequest *)deleteItinerary:(NSString *)itinKey
{
    return nil;
}


// <?xml version="1.0" encoding="UTF-8"?><Response><Header><Version>1.0</Version><Log><Level>None</Level></Log><TravelerUID>0</TravelerUID><ExpenseUID>37</ExpenseUID><CliqSessionID>A9DE8F96-D1EE-4A54-893C-3D2FD87D1B1A</CliqSessionID><LoginID>acsontos@outtask.com</LoginID><EntityID>phos123488</EntityID><CompanyID>1</CompanyID><SUVersion>100.0</SUVersion><IsMobile>Y</IsMobile><IsTestUser>N</IsTestUser><SkipVersionCheck>Y</SkipVersionCheck><HmcUserKey /><RequestOrigin>MOBILE</RequestOrigin><PerfData><TotalDuration>70</TotalDuration><DBPerfItemTotal>42</DBPerfItemTotal></PerfData></Header>
// <Body>
// <Itinerary>
// <ItinKey>nNYL70NG$pPPsWPqO5FlaeL1s</ItinKey>
// <Name>TA 2</Name>
// <ShortDistanceTrip>N</ShortDistanceTrip><EmpKey>37</EmpKey><TacKey>7</TacKey><TacName>GenericStd Fixed/Fixed</TacName><DepartDateTime>2013-12-19 12:00</DepartDateTime><DepartLocation>Seattle, Washington</DepartLocation><ArrivalDateTime>2013-12-23 18:00</ArrivalDateTime><ArrivalLocation>San Jose, California</ArrivalLocation><IsLocked>N</IsLocked><AreAllRowsLocked>N</AreAllRowsLocked>
// <ItineraryRows>

// <ItineraryRow><IrKey>n9z$siF5gHmq5cPIU$s$p0ZI4S4</IrKey><ArrivalDateTime>2013-12-19 14:04</ArrivalDateTime><ArrivalLocation>San Jose, California</ArrivalLocation><ArrivalLnKey>30050</ArrivalLnKey><DepartDateTime>2013-12-19 14:04</DepartDateTime><DepartLocation>Oakland, California</DepartLocation><DepartLnKey>28790</DepartLnKey><ArrivalRlKey>49</ArrivalRlKey><ArrivalRateLocation>California, UNITED STATES</ArrivalRateLocation><BorderCrossDateTime>2013-12-19 14:04</BorderCrossDateTime><IsRowLocked>N</IsRowLocked><IsArrivalRateLocationEditable>N</IsArrivalRateLocationEditable></ItineraryRow>
// <ItineraryRow><IrKey>n9ht3rWvRimPBe7POr3j73fE</IrKey><ArrivalDateTime>2013-12-23 18:00</ArrivalDateTime><ArrivalLocation>Seattle, Washington</ArrivalLocation><ArrivalLnKey>29928</ArrivalLnKey><DepartDateTime>2013-12-23 15:00</DepartDateTime><DepartLocation>San Jose, California</DepartLocation><DepartLnKey>30050</DepartLnKey><ArrivalRlKey>68</ArrivalRlKey><ArrivalRateLocation>Seattle, Washington, US</ArrivalRateLocation><BorderCrossDateTime>2013-12-23 15:00</BorderCrossDateTime><IsRowLocked>N</IsRowLocked><IsArrivalRateLocationEditable>N</IsArrivalRateLocationEditable></ItineraryRow>
// </ItineraryRows>
// </Itinerary>
// </Body>
// </Response>"

+ (NSMutableArray *)parseItinerariesXML:(NSString *)result rptKey:(NSString *)rptKey crnCode:(NSString *)crnCode {

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

        itinerary.crnCode = crnCode;

        NSMutableArray *stops = [[NSMutableArray alloc] init];
        [ItineraryStop processItineraryRowXML:itin stops:stops];

        [ItineraryStop applyStopNumbers:stops];
//        [self breakStopsIntoSectionsByDate];
//        NSLog(@"self.itineraryStopsBySection = %@", self.itineraryStopsBySection);

        itinerary.stops = stops;

        [arrayOfItineraries addObject:itinerary];
    }

    return arrayOfItineraries;
}

+ (CXRequest *)getTAItinerariesRequest:(NSString *)reportKey roleCode:(NSString *)roleCode
{
    NSLog(@"~~~~~~~roleCode = %@", roleCode);
    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/GetTAItineraries/%@", reportKey];
    if([Itinerary isApproving:roleCode])
    {
        path = [NSString stringWithFormat:@"Mobile/TravelAllowance/GetTAItineraries/%@/%@", reportKey, @"MANAGER"];
    }

    NSLog(@"path = %@", path);

    // Create the request
    return [[CXRequest alloc] initWithServicePath:path];
}

+ (BOOL)isApproving:(NSString *)role
{
    if (role == nil || ![@"MOBILE_EXPENSE_TRAVELER" isEqualToString:role])
        return TRUE;
    else
        return FALSE;
}

/*
"<?xml version="1.0" encoding="UTF-8"?>
<Response>
<Header><Version>1.0</Version><Log><Level>None</Level></Log><TravelerUID>56489</TravelerUID><ExpenseUID>804</ExpenseUID><CliqSessionID>41B730E9-4E6A-4A98-A1BE-03170D351DCD</CliqSessionID><LoginID>worksforandrew@outtask.com</LoginID><EntityID>phos123488</EntityID><CompanyID>1</CompanyID><SUVersion>106.0</SUVersion><IsMobile>Y</IsMobile><IsTestUser>N</IsTestUser><SkipVersionCheck>Y</SkipVersionCheck><HmcUserKey /><RequestOrigin>MOBILE</RequestOrigin><PerfData><TotalDuration>8220</TotalDuration></PerfData></Header>
<Body>
<Status>SUCCESS</Status>
<StatusText>TravelAllowance.UnAssignItinerary.Success</StatusText>
</Body></Response>"

 */


+ (BOOL )wasDeleteItinerarySuccessful:(NSString *)result
{
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

    RXMLElement *body = [rootXML child:@"Body"];
    NSString *status = [body child:@"Status"].text;
//    NSString *statusText = [body child:@"StatusText"].text;

    if([status isEqualToString:@"SUCCESS"])
    {
        return YES;
    }

    return NO;
}

+ (BOOL )wasUnassignItinerarySuccessful:(NSString *)result
{
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

    RXMLElement *body = [rootXML child:@"Body"];
    NSString *status = [body child:@"Status"].text;
//    NSString *statusText = [body child:@"StatusText"].text;

    if([status isEqualToString:@"SUCCESS"])
    {
        return YES;
    }

    return NO;
}

+ (Itinerary *)getNewItineraryForSingleDay:(ItineraryConfig *)config itineraryName:(NSString *)itineraryName rptKey:(NSString *)rptKey
{
    Itinerary *itinerary = [[Itinerary alloc]init];

    itinerary.isCollapsed = NO;

    itinerary.itinName = itineraryName;
    itinerary.rptKey = rptKey;
    itinerary.stops = [[NSMutableArray alloc] init];

    itinerary.tacKey = config.tacKey;

    //Create an empty ItineraryStop to pass in;
    ItineraryStop *stopFirst = [[ItineraryStop alloc] init];
    [itinerary.stops addObject:stopFirst];

    [ItineraryStop defaultDepartureLocationForStop:config stop:stopFirst];
    [ItineraryStop defaultDatesForStop:stopFirst];
    [ItineraryStop defaultSingleDayTimesForFirstStop:stopFirst];

    stopFirst.arrivalLocation = [Localizer getLocalizedText:@"Destination Location"];

    ItineraryStop *stopLast = [[ItineraryStop alloc] init];
    [ItineraryStop defaultDatesForStop:stopLast]; //TODO improve
    [ItineraryStop defaultSingleDayTimesForLastStop:stopLast];

    [itinerary.stops addObject:stopLast];

    return itinerary;
}

+ (Itinerary *)getNewItineraryRegular:(ItineraryConfig *)config reportName:(NSString *)reportName rptKey:(NSString *)rptKey
{
    Itinerary *newItinerary = [[Itinerary alloc]init];

    newItinerary.itinName = reportName;
    newItinerary.rptKey = rptKey;
    newItinerary.tacKey = config.tacKey;
    newItinerary.stops = [[NSMutableArray alloc] init];

    newItinerary.isCollapsed = NO;

    return newItinerary;
}

+ (NSString *)getRptKey:(NSMutableDictionary *)paramBag
{
    return paramBag[@"RECORD_KEY"];
}

+ (NSString *)getReportName:(NSMutableDictionary *)paramBag
{
    //TODO Is there more logic that should go here?
    return paramBag[@"ReportName"];
}

- (BOOL)hasFailures
{
    for (ItineraryStop *stop in self.stops)
    {
        if(stop.isFailed)
        {
            return YES;
        }
    }

    return NO;
}


@end
