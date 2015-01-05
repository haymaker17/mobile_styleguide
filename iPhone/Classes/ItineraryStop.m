//
//  ItineraryStop.m
//  ConcurMobile
//
//  Created by Wes Barton on 1/22/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryStop.h"

#import "CXRequest.h"
#import "ItineraryConfig.h"
#import "RXMLElement.h"
#import "Itinerary.h"
#import "ItineraryStopCell.h"

@implementation ItineraryStop

+ (CXRequest *)deleteItineraryStop:(NSString *)itinKey irKey:(NSString *)irKey
{
    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/DeleteItineraryRow/%@/%@",itinKey,irKey];

    // Create the request
    return [[CXRequest alloc] initWithServicePath:path];
}

+ (BOOL )wasDeleteItineraryStopSuccessful:(NSString *)result {
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

//    <Status>SUCCESS</Status>
//            <StatusText>TravelAllowance.DeleteItineraryRow.Success</StatusText>
//            <ModifiedReports/>

    RXMLElement *body = [rootXML child:@"Body"];
    NSString *status = [body child:@"Status"].text;
    NSString *statusText = [body child:@"StatusText"].text;
    NSString *modifiedReports = [body child:@"ModifiedReports"].text;

    if([status isEqualToString:@"SUCCESS"])
    {
        return YES;
    }

    return NO;
}

// <ItineraryRow>
//<Status>FAILURE</Status>
// <StatusText>TravelAllowance.ItineraryRow.Error.InvalidArrivalDepartureTime</StatusText>
// <IrKey>n9DN2Jy06b6K9Xk8R7OUp45k</IrKey>
// <ArrivalDateTime>2013-12-19 14:00</ArrivalDateTime>
// <ArrivalLocation>Louisville, Mississippi</ArrivalLocation>
//<ArrivalLnKey>27637</ArrivalLnKey>
// <DepartDateTime>2013-12-19 12:00</DepartDateTime>
// <DepartLocation>Seattle, Washington</DepartLocation>
//<DepartLnKey>29928</DepartLnKey>
//<ArrivalRlKey>44</ArrivalRlKey>
//<ArrivalRateLocation>UNITED STATES</ArrivalRateLocation>
//<BorderCrossDateTime>2013-12-19 12:00</BorderCrossDateTime>
//<IsRowLocked>N</IsRowLocked>
//<IsArrivalRateLocationEditable>N</IsArrivalRateLocationEditable>
// </ItineraryRow>

+ (ItineraryStop *)parseItineraryRow:(RXMLElement *)row {
    ItineraryStop *stop = [[ItineraryStop alloc] init];
    stop.stopNumber = @0;

    stop.isFailed = NO;
    stop.status = [row child:@"Status"].text;
    if(stop.status != nil && [stop.status isEqualToString:@"FAILURE"])
    {
        stop.isFailed = YES;
    }

    stop.statusText = [row child:@"StatusText"].text;
    stop.statusTextLocalized = [row child:@"StatusTextLocalized"].text;

    NSString *irKey = [row child:@"IrKey"].text;
    stop.irKey = irKey;

    //Departure
    stop.departureLocation = [row child:@"DepartLocation"].text;
    stop.departLnKey = [row child:@"DepartLnKey"].text;

    NSString *departDateTime = [row child:@"DepartDateTime"].text;
    stop.departureDate = [self getNSDateFromItineraryRow:departDateTime];


    //Arrival
    stop.arrivalLocation = [row child:@"ArrivalLocation"].text;
    stop.arrivalLnKey = [row child:@"ArrivalLnKey"].text;

    NSString *arrivalDateTime = [row child:@"ArrivalDateTime"].text;
    stop.arrivalDate = [self getNSDateFromItineraryRow:arrivalDateTime];


    stop.arrivalRlKey = [row child:@"ArrivalRlKey"].text;
    stop.arrivalRateLocation = [row child:@"ArrivalRateLocation"].text;
    stop.arrivalRateLocationEditable = [[row child:@"IsArrivalRateLocationEditable"].text boolValue];


    NSString *borderCrossDateTime = [row child:@"BorderCrossDateTime"].text;
    NSDate *borderCrossDate = [self getNSDateFromItineraryRow:borderCrossDateTime];
    stop.borderCrossDate = borderCrossDate;

    stop.rowLocked = [[row child:@"IsRowLocked"].text boolValue];

    NSLog(@"depart %@ -- arrival %@", departDateTime, arrivalDateTime);
    return stop;
}

+ (NSDate *) getNSDateFromItineraryRow:(NSString *) dateString
{
    NSDateFormatter *dateFormatter= [self getDateFormatter];
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

        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm"];
    }
    return dateFormatter;
}

/*

<Itinerary>
        <ItinKey>nNXTzmD3I6jHjasXOLPBwVKfB</ItinKey>
        <Name>3.4.14 Report</Name>
        <ShortDistanceTrip>N</ShortDistanceTrip>
        <EmpKey>37</EmpKey>
        <TacKey>7</TacKey>
        <TacName>GenericStd Fixed/Fixed</TacName>
        <IsLocked>N</IsLocked>
        <AreAllRowsLocked>N</AreAllRowsLocked>
        <ItineraryRows>
            <ItineraryRow>
                <Status>SUCCESS</Status>
                <StatusText>TravelAllowance.ValidateAndSaveItineraryRow.Success</StatusText>
                <IrKey>n98SLDClmogF2t97Gzk2E3qg0</IrKey>
                <ArrivalDateTime>2014-03-03 20:20</ArrivalDateTime>
                <ArrivalLocation>Bellevue, Washington</ArrivalLocation>
                <ArrivalLnKey>24811</ArrivalLnKey>
                <DepartDateTime>2014-03-03 19:19</DepartDateTime>
                <DepartLocation>Renton, Washington</DepartLocation>
                <DepartLnKey>29691</DepartLnKey>
                <ArrivalRlKey>68</ArrivalRlKey>
                <ArrivalRateLocation>Seattle, Washington, US</ArrivalRateLocation>
                <IsRowLocked>N</IsRowLocked>
            </ItineraryRow>
        </ItineraryRows>
    </Itinerary>
 */

+ (NSString *)parseSaveResultForStatus:(NSString *)result {

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
    }
    return nil;
}

+ (NSString *)parseSaveResultForStatusTextLocalized:(NSString *)result {
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

            NSString *statusTextLocalized = [row child:@"StatusTextLocalized"].text;
            NSLog(@"statusTextLocalized = %@", statusTextLocalized);

            if([status isEqualToString:@"FAILURE"])
            {
                return statusTextLocalized;
            }
            else if([status isEqualToString:@"WARNING"])
                {
                    return statusTextLocalized;
                }
        }
    }
    return nil;

}

+ (NSString *)parseSaveResultForStatusText:(NSString *)result {

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

            NSString *statusText = [row child:@"StatusText"].text;
            NSString *statusTextLocalized = [row child:@"StatusTextLocalized"].text;
            NSLog(@"statusText = %@", statusText);
            NSLog(@"statusTextLocalized = %@", statusTextLocalized);

            if([status isEqualToString:@"FAILURE"])
            {
                return statusText;
            }
            else if([status isEqualToString:@"WARNING"])
            {
                return statusText;
            }
        }
    }
    return nil;
}

+ (NSString *)composeItineraryRow:(ItineraryStop *)stop formatter:(NSDateFormatter *)formatter
{
    NSMutableString *block = [[NSMutableString alloc] init];

    [block appendString:@"<ItineraryRow>"];
    if(stop.irKey != nil) {
        [block appendString:[NSString stringWithFormat:@"<IrKey>%@</IrKey>", stop.irKey]];
    }else{
        [block appendString:@"<IrKey/>"];
    }
    [block appendString:[NSString stringWithFormat:@"<DepartLnKey>%@</DepartLnKey>",stop.departLnKey]];
    [block appendString:[NSString stringWithFormat:@"<DepartDateTime>%@</DepartDateTime>", [formatter stringFromDate:stop.departureDate]]];
    [block appendString:[NSString stringWithFormat:@"<ArrivalLnKey>%@</ArrivalLnKey>",stop.arrivalLnKey]];
    if(stop.arrivalRlKey != nil) {
        [block appendString:[NSString stringWithFormat:@"<ArrivalRlKey>%@</ArrivalRlKey>", stop.arrivalRlKey]];
    }
    [block appendString:[NSString stringWithFormat:@"<ArrivalDateTime>%@</ArrivalDateTime>",[formatter stringFromDate:stop.arrivalDate]]];
    if(stop.borderCrossDate != nil)
    {
        [block appendString:[NSString stringWithFormat:@"<BorderCrossDateTime>%@</BorderCrossDateTime>",[formatter stringFromDate:stop.borderCrossDate]]];
    }
    [block appendString:@"</ItineraryRow>"];

    return block;
}




+ (void)applyStopNumbers:(NSMutableArray *)stops {
    for (int i = 0; i < [stops count]; i++)
    {
        ItineraryStop *stop = [stops objectAtIndex:i];
        stop.stopNumber = [NSNumber numberWithInt:(i+1)];
    }
}

+ (void)processItineraryRowXML:(RXMLElement *)itin stops:(NSMutableArray *)stops {
    RXMLElement *itineraryRows = [itin child:@"ItineraryRows"];
    [itineraryRows iterate:@"ItineraryRow" usingBlock:^(RXMLElement *row){
        ItineraryStop *stop= [ItineraryStop parseItineraryRow:row];
        [stops addObject:stop];
    }];
}

+ (void)defaultDatesForStop:(ItineraryStop *)stop
{
// set default dates
    NSDate *now = [NSDate date];

    NSTimeZone *localTz = [NSTimeZone systemTimeZone];
    NSInteger secondsFromGMT = [localTz secondsFromGMTForDate:now];
    NSLog(@"secondsFromGMT = %li", (long)secondsFromGMT);

    now = [NSDate dateWithTimeInterval:secondsFromGMT sinceDate:now];

    NSLog(@"now = %@", now);

    stop.departureDate = now;
    stop.arrivalDate = now;
    stop.borderCrossDate = now;
}
+ (void)copyDatesFromStopArrival:(ItineraryStop *)to from:(ItineraryStop *)from
{
    to.departureDate = from.arrivalDate;
    to.arrivalDate = from.arrivalDate;
    to.borderCrossDate = from.arrivalDate;
}

+ (void)defaultDepartureLocationForStop:(ItineraryConfig *)config stop:(ItineraryStop *)stop
{
    if(config.defaultLnKey != nil)
    {
        stop.departLnKey = config.defaultLnKey;
        stop.departureLocation = config.defaultLocName;
    }
    else
    {
        // TODO set a real default
        stop.departureLocation = @"Default Departure";
    }
}

- (BOOL)isRowFailureDateTime
{
    NSString *errorString = @"TravelAllowance.ItineraryRow.Error.InvalidArrivalDepartureTime";

    if(self.isFailed && [self.statusText isEqualToString:errorString])
    {
        return YES;
    }
    return NO;
}

- (BOOL)isRowFailureBorderDateTime
{
    NSString *errorString = @"TravelAllowance.ItineraryRow.Error.InvalidBorderCrossTime";

    if(self.isFailed && [self.statusText isEqualToString:errorString])
    {
        return YES;
    }
    return NO;
}

+ (ItineraryStop *)getNewStop:(ItineraryConfig *)config itinerary:(Itinerary *)itinerary
{
    ItineraryStop *newItineraryStop = nil;
    // Are there any stops
    if(itinerary.stops != nil && [itinerary.stops count] > 0)
    {
        //Adding another stop.

        //Create an empty ItineraryStop to pass in;
        ItineraryStop *stop = [[ItineraryStop alloc] init];

        //Get the first stop
        ItineraryStop *firstStop = [itinerary.stops firstObject];
        stop.arrivalLnKey = firstStop.departLnKey;
        stop.arrivalLocation = firstStop.departureLocation;

        //Get the last stop
        ItineraryStop *lastStop = [itinerary.stops lastObject];
        stop.departLnKey = lastStop.arrivalLnKey;
        stop.departureLocation = lastStop.arrivalLocation;

        //Copy dates
        [ItineraryStop copyDatesFromStopArrival:stop from:lastStop];

        newItineraryStop = stop;
    } else {
        //Create the first stop

        //Create an empty ItineraryStop to pass in;
        ItineraryStop *stop = [[ItineraryStop alloc] init];

        // Get the default location
        [ItineraryStop defaultDepartureLocationForStop:config stop:stop];

        stop.arrivalLocation = [Localizer getLocalizedText:@"Destination Location"];

        [ItineraryStop defaultDatesForStop:stop];

        newItineraryStop = stop;
    }

    return newItineraryStop;
}


+ (void)defaultSingleDayTimesForFirstStop:(ItineraryStop *)stop
{
    NSDate *startOfBusinessDay = [ItineraryStopCell getStartOfBusinessDay:stop.departureDate];

    stop.departureDate = startOfBusinessDay;
    stop.borderCrossDate = startOfBusinessDay;
    stop.arrivalDate = startOfBusinessDay;
}


+ (void)defaultSingleDayTimesForLastStop:(ItineraryStop *)stop
{
    NSDate *endOfBusinessDay = [ItineraryStopCell getEndOfBusinessDay:stop.departureDate];

    stop.arrivalDate = endOfBusinessDay;
    stop.borderCrossDate = endOfBusinessDay;
    stop.departureDate = endOfBusinessDay;
}


@end
