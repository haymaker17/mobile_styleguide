//
//  ItineraryImportRow.h
//  ConcurMobile
//
//  Created by Wes Barton on 5/5/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class RXMLElement;

@interface ItineraryImportRow : NSObject

//<AirportName> Washington Dulles Intl</AirportName>
@property NSString *airportName;
//<City> Washington</City>
@property NSString *city;
//<CountryCode> US</CountryCode>
@property NSString *countryCode;
//<First>false</First>
@property BOOL first;
//<FirstOrLast>true</FirstOrLast>
@property BOOL firstOrLast;
//<HideFromList>false</HideFromList>
@property BOOL hideFromList;
//<Iata> IAD</Iata>
@property NSString *iata;
//<IataCode> IAD</IataCode>
@property NSString *iataCode;
//<IataType> A</IataType>
@property NSString *iataType;
//<IsConnection>false</IsConnection>
@property BOOL isConnection;
//<Last>false</Last>
@property BOOL last;
//<LegDate>2014-04-21 12:00:00.0</LegDate>
//<LegDateNoTime>2014-04-21 00:00:00.0</LegDateNoTime>
//<LegType> Depart</LegType>
@property NSString *legType;
//<LnKey>31038</LnKey>
@property NSString *lnKey;
//<Location> Washington</Location>
@property NSString *location;
//<RowType> Trip</RowType>
@property NSString *rowType;
//<SegmentType> AIR</SegmentType>
@property NSString *segmentType;
//<State> DC</State>
@property NSString *state;
//<ToBeDeleted>false</ToBeDeleted>
@property BOOL toBeDeleted;
//<TripId> T3113</TripId>
@property NSString *tripId;

@property(nonatomic, copy) NSString *itinSourceId;

@property(nonatomic, copy) NSString *taImportId;

@property(nonatomic, copy) NSString *zipCode;

@property(nonatomic, strong) NSDate *legDate;

- (id)initWithXML:(RXMLElement *)row;

@end


/*
<Body>
<Headers>
<Header>
<EndDate>2014-04-21 00:00:00.0</EndDate>
<EndDateNoTime>2014-04-21 00:00:00.0</EndDateNoTime>
<First>false</First>
<FirstOrLast>false</FirstOrLast>
<HasAir>true</HasAir>
<HasHotel>true</HasHotel>
<HasRail>false</HasRail>
<HeaderType> Trip</HeaderType>
<HideFromList>false</HideFromList>
<IsConnection>false</IsConnection>
<Last>false</Last>
<StartDate>2014-04-21 00:00:00.0</StartDate>
<StartDateNoTime>2014-04-21 00:00:00.0</StartDateNoTime>
<TaImportId> T3113</TaImportId>
<ToBeDeleted>false</ToBeDeleted>
<TripId>3113</TripId>
<TripName> DemoTrip (DEMO00)
</TripName>
</Header>
</Headers>
<Rows>
<Row>
<AirportName> Washington Dulles Intl</AirportName>
<City> Washington</City>
<CountryCode> US</CountryCode>
<First>false</First>
<FirstOrLast>true</FirstOrLast>
<HideFromList>false</HideFromList>
<Iata> IAD</Iata>
<IataCode> IAD</IataCode>
<IataType> A</IataType>
<IsConnection>false</IsConnection>
<Last>false</Last>
<LegDate>2014-04-21 12:00:00.0</LegDate>
<LegDateNoTime>2014-04-21 00:00:00.0</LegDateNoTime>
<LegType> Depart</LegType>
<LnKey>31038</LnKey>
<Location> Washington</Location>
<RowType> Trip</RowType>
<SegmentType> AIR</SegmentType>
<State> DC</State>
<ToBeDeleted>false</ToBeDeleted>
<TripId> T3113</TripId>
</Row>
<Row>
<City> Alexandria<
/City>
<CountryCode> US</CountryCode>
<First>false</First>
<FirstOrLast>false</FirstOrLast>
<HideFromList>false</HideFromList>
<Iata> LAX</Iata>
<IsConnection>false</IsConnection>
<ItinSourceId>0</ItinSourceId>
<Last>false</Last>
<LegDate>2014-04-21 16:59:59.0</LegDate>
<LegDateNoTime>2014-04-21 00:00:00.0</LegDateNoTime>
<LegType> Arrival</LegType>
<LnKey>24350</LnKey>
<Location> Alexandria, Virginia</Location>
<RowType> Trip</RowType>
<SegmentType> HOTEL</SegmentType>
<State> VA</State>
<TaImportId> T3113</TaImportId>
<ToBeDeleted>false</ToBeDeleted>
<TripId>3113</TripId>
<ZipCode>22201</ZipCode>
</Row>
<Row>
<AirportName> Los Angeles Intl</AirportName>
<City> Los Angeles</City>
<CountryCode> US</CountryCode>
<First>false</First>
<FirstOrLast>false</FirstOrLast>
<HideFromList>false</HideFromList>
<Iata> LAX</Iata>
<IataCode> LAX</IataCode>
<IataType> A</IataType>
<IsConnection>false</IsConnection>
<Last>false</Last>
<LegDate>2014-04-22 05:00:00.0</LegDate>
<LegDateNoTime>2014-04-22 00:00:00.0</LegDateNoTime>
<LegType> Arrival</LegType>
<LnKey>27447</LnKey>
<Location> Los Angeles</Location>
<RowType> Trip</RowType>
<SegmentType> AIR</SegmentType>
<State> CA</State>
<TaImportId> T3113</TaImportId>
<ToBeDeleted>false</ToBeDeleted>
<TripId>3113</TripId>
</Row>
<Row>
<City> Alexandria</City>
<CountryCode> US</CountryCode>
<First>false</First>
<FirstOrLast>false</FirstOrLast>
<HideFromList>false</HideFromList>
<Iata> LAX</Iata>
<IsConnection>false</IsConnection>
<ItinSourceId>0</ItinSourceId>
<Last>false</Last>
<LegDate>2014-04-23 11:00:00.0</LegDate>
<LegDateNoTime>2014-04-23 00:00:00.0</LegDateNoTime>
<LegType> Depart</LegType>
<LnKey>24350</LnKey>
<Location> Alexandria, Virginia</Location>
<RowType> Trip</RowType>
<SegmentType> HOTEL</SegmentType>
<State> VA</State>
<TaImportId> T3113</TaImportId>
<ToBeDeleted>false</ToBeDeleted>
<TripId>3113</TripId>
<ZipCode>22201</ZipCode>
</Row>
<Row>
<AirportName> Los Angeles Intl</AirportName>
<City> Los Angeles</City>
<CountryCode> US</CountryCode>
<First>false</First>
<FirstOrLast>false</FirstOrLast>
<HideFromList>false</HideFromList>
<Iata> LAX</Iata>
<IataCode> LAX</IataCode>
<IataType> A</IataType>
<IsConnection>false</IsConnection>
<Last>false</Last>
<LegDate>2014-04-23 12:00:00.0</LegDate>
<LegDateNoTime>2014-04-23 00:00:00.0</LegDateNoTime>
<LegType> Depart</LegType>
<LnKey>27447</LnKey>
<Location> Los Angeles</Location>
<RowType> Trip</RowType>
<SegmentType> AIR</SegmentType>
<State> CA</State>
<ToBeDeleted>false</ToBeDeleted>
<TripId> T3113</TripId>
</Row>
<Row>
<AirportName> Washington Dulles Intl</AirportName>
<City> Washington</City>
<CountryCode> US</CountryCode>
<First>false</First>
<FirstOrLast>true</FirstOrLast>
<HideFromList>false</HideFromList>
<Iata> IAD</Iata>
<IataCode> IAD</IataCode>
<IataType> A</IataType>
<IsConnection>false</IsConnection>
<Last>true</Last>
<LegDate>2014-04-24 05:00:00.0</LegDate>
<LegDateNoTime>2014-04-24 00:00:00.0</LegDateNoTime>
<LegType> Arrival</LegType>
<LnKey>31038</LnKey>
<Location> Washington</Location>
<RowType> Trip</RowType>
<SegmentType> AIR</SegmentType>
<State> DC</State>
<TaImportId> T3113</TaImportId>
<ToBeDeleted>false</ToBeDeleted>
<TripId>3113</TripId>
</Row>
</Rows>
</Body>
</Response>

  */
