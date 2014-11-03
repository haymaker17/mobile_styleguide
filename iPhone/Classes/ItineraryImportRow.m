//
//  ItineraryImportRow.m
//  ConcurMobile
//
//  Created by Wes Barton on 5/5/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryImportRow.h"
#import "RXMLElement.h"
#import "ItineraryImport.h"

@implementation ItineraryImportRow

- (id)initWithXML:(RXMLElement *)row
{

//    <Row>
//    <City> Alexandria</City>
    self.city = [ row child:@"City"].text;
//    <CountryCode> US</CountryCode>
    self.countryCode = [ row child:@"CountryCode"].text;
//    <First>false</First>
    self.first = (BOOL) [[ row child:@"First"].text boolValue];
//    <FirstOrLast>false</FirstOrLast>
    self.firstOrLast = (BOOL) [[ row child:@"FirstOrLast"].text boolValue];
//    <HideFromList>false</HideFromList>
    self.hideFromList = (BOOL) [[ row child:@"HideFromList"].text boolValue];
//    <Iata> LAX</Iata>
    self.iata = [ row child:@"Iata"].text;
//    <IsConnection>false</IsConnection>
    self.IsConnection = (BOOL) [[ row child:@"IsConnection"].text boolValue];
//    <ItinSourceId>0</ItinSourceId>
    self.itinSourceId = [ row child:@"ItinSourceId"].text;
//    <Last>false</Last>
    self.Last = (BOOL) [[ row child:@"Last"].text boolValue];
//    <LegDate>2014-04-21 16:59:59.0</LegDate>
    self.legDate = [ItineraryImport getNSDateFromImport:[ row child:@"LegDate"].text];
//    <LegDateNoTime>2014-04-21 00:00:00.0</LegDateNoTime>
//    <LegType> Arrival</LegType>
    self.legType = [ row child:@"LegType"].text;
//    <LnKey>24350</LnKey>
    self.lnKey = [ row child:@"LnKey"].text;
//    <Location> Alexandria, Virginia</Location>
    self.location = [ row child:@"Location"].text;
//    <RowType> Trip</RowType>
    self.rowType = [ row child:@"RowType"].text;
//    <SegmentType> HOTEL</SegmentType>
    self.segmentType = [ row child:@"SegmentType"].text;
//    <State> VA</State>
    self.state = [ row child:@"State"].text;
//    <TaImportId> T3113</TaImportId>
    self.taImportId = [ row child:@"TaImportId"].text;
//    <ToBeDeleted>false</ToBeDeleted>
    self.toBeDeleted = (BOOL) [[ row child:@"ToBeDeleted"].text boolValue];
//    <TripId>3113</TripId>
    self.tripId = [ row child:@"TripId"].text;
//    <ZipCode>22201</ZipCode>
    self.zipCode = [ row child:@"ZipCode"].text;
//    </Row>



    return self;
}

@end


