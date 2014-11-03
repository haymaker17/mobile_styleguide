//
//  EvaLocation.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//


/** 
 * ====== From Evature API doc =======
 * A number representing the location index in the trip. Index numbers usually progress with the duration of the trip (so a
 * location with index 11 is visited before a location with index 21). An index number is unique for a locations in Locations
 * (unless the same location visited multiple times, for example home location at start and end of trip will have the same
 * index) but <code>Alt Locations</code> may have multiple locations with the same index, indicating alternatives for the same
 * part of a trip. Index numbers are not serial, so indexes can be (0,1,11,21,22, etc.). Index number <code>0</code> is unique
 * and always represents the home location.
 Eg: Locations
 
 "Locations": [
 {
 "Index": 0,
 "Airports": "TAO,DLC,WEH,YNT,DOY",
 "Name": "Yatou, China (GID=1786855)",
 "Derived From": "GPS",
 "Longitude": 122.43762,
 "Latitude": 37.1566,
 "Type": "City",
 "Geoid": 1786855,
 "Geo Attributes": {
 "Nearby": true
 },
 "Country": "CN",
 "Home": "GPS"
 },
 {
 "Arrival": {
 "Date": "2013-12-25"
 },
 "Index": 12,
 "Actions": [
 "Get There",
 "Get Accommodation"
 ] ---> Might me more locations

 */


#import <Foundation/Foundation.h>
#import "EvaTime.h"
#import "LocationResult.h"

@interface EvaLocation : NSObject

@property (nonatomic, copy) NSString		*longitude;
@property (nonatomic, copy) NSString		*latitude;
@property (nonatomic, copy) NSString		*name;
@property (nonatomic, copy) NSString		*country;
@property (nonatomic, copy) NSString		*distanceUnit;
@property (nonatomic,assign) NSNumber		*distanceValue;
/**
 * The location type. Example values: Continent, Country, City, Island, Airport.
 */
@property (nonatomic, copy) NSString		*type;
/**
 * A global identifier for the location. IATA code for airports and Geoname ID for other locations. Note: if Geoname ID is not
 * defined for a location, a string representing the name of the location will be given in as value instead. The format of
 * this name is currently not set and MAY CHANGE. If you plan to use this field, please contact us.
 */
@property (nonatomic, copy) NSString		*geoid;

/*
 Index:
 A number representing the location index in the trip. Index numbers usually progress with the duration of the trip (so a location with index 11 is visited before a location with index 21).
 
 An index number is unique for a locations in Locations (unless the same location visited multiple times, for example home location at start and end of trip will have the same index) but “Alt Locations” may have multiple locations with the same index, indicating alternatives for the same part of a trip.
 
 Index numbers are not serial, so indexes can be (0,1,11,21,22, etc.).
 
 Index number ‘0’ is unique and always represents the home location.
 property named as Index so as not conflict with frameworks "index" 
 */
@property (nonatomic, copy)  NSString *Index;

/**
 * The index number of the location in a trip, if known. Default to -1.
 */
@property (nonatomic, assign)  NSString *Next;

/**
 * Will be present in cities that have an <code>all airports</code> IATA code e.g. San Francisco, New York, etc.
 */
@property (nonatomic, copy) NSString		*allAirportCode;
/**
 * If a location is not an airport, this key provides 5 recommended airports for this location. Airports are named by their
 * IATA code.
 */
@property (nonatomic, strong) NSMutableArray *airports;

/*
 There are many general request attributes that apply to the entire request and not just some portion of it.
 Examples: “last minute deals” and “Low deposits”.
 */
@property (nonatomic,strong) NSMutableDictionary     *requestAttributes;


/**
 * Provides a list of actions requested for this location. Actions can include the following values: <code>Get There</code>
 * (request any way to be transported there, mostly flights but can be train, bus etc.), <code>Get Accommodation</code>,
 * <code>Get Car</code>.
 */
@property (nonatomic, strong) NSMutableArray *actions;

/**
 * Complex Eva Time object
 */
@property (nonatomic, strong) EvaTime *departureDateTime;
@property (nonatomic, strong) EvaTime *arrivalDateTime;

/**
 * Complex Eva Time object for return Car.
 */
@property (nonatomic, strong) EvaTime *carPickupDateTime;
@property (nonatomic, strong) EvaTime *carReturnDateTime;

/**
 * The number of days a car is rented out.
 *
 */
@property (nonatomic,assign) NSInteger rentalCarDuration;

/**
 * The number of days a car is rented out.
 *
 */
-(NSInteger) getStayDuration;

// Methods
-(id)initWithDict:(NSDictionary *)dictionary;
-(void)parseJson;
-(EvaSearchCategory)getTransportType;
-(EvaSearchCategory)getActionsforType;
// Build LocationResult
-(LocationResult *)getLocationResult;
-(id)containsKey:(NSString *)key;
-(NSString *)getAirportCode;

@end
