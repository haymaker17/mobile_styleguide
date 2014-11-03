//
//  EvaApiReply.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//
// Purpose
// Object that represents the apiReply tag in the json response. Also holds the different utility methods to process the other fields.
// 
//
// Unit Test Cases 
// 1. Approach: pass the api_reply dictionary or Json file to this class to check it can initialize correctly.
// 2. Assert check if parsing is successful and handles incorrect data.
// 3. Assert check if depenedet objects are created correctly.
// 4. Assert check with nil data. no exceptions.
//
#import <Foundation/Foundation.h>
#import "DataConstants.h"
#import "EvaLocation.h"
#import "EvaFlow.h"

@interface EvaApiReply : NSObject

@property (nonatomic, copy) NSString			*processedText;
@property (nonatomic, copy) NSString			*sayit;
/*
 Information about the travelers number and age.
 */
@property (nonatomic,assign) NSInteger          travellers;

// Custom field - Doesnt represent any of the json tag
@property EvaSearchCategory                     evaSearchCategory;

/*
Information about the locations included in the trip as specified by the user (countries, cities, areas, airports, properties) and their related information (including all TIME information).

Note: All the attributes described bellow CAN appear within a location IF the attribute is related specifically to a particular location. Example “Center of Chicago” - the Geo Attribute “Center” will appear under Chicago and not in the global context.

Alt Locations
Structured the same as Locations, but include alternate locations, to locations with similar indexes in the Locations key.

Example: “vacation in Italy or France” will place “France” in Locations while Italy in “Alt Locations” while both would share the same Index.
*/
@property (nonatomic,strong) NSMutableArray     *evaLocations;
@property (nonatomic,strong) NSMutableArray     *alt_evaLocations;

/*
 All possbilities in case of ambiguity = true and best possibility
 */
@property (nonatomic,strong) NSArray     *possibilites;
@property (nonatomic,strong) NSDictionary *bestPossibility;
@property BOOL ambiguity;
@property int position;
/*
 Attributes describing the requested flight/car/Hotel.
 */
@property (nonatomic,strong) NSDictionary     *flightAttributes;     // TODO : This might be an object
@property (nonatomic,strong) NSDictionary     *hotelAttributes;      // TODO : This might be an object
@property (nonatomic,strong) NSDictionary     *carAttributes;        // TODO : This might be an object
@property (nonatomic,strong) NSDictionary     *evaMoney;             // TODO : This might be an object

/* 
 Various Warnings provided by the system.
 eg: API Warning, Parse Warning, Home Warning, HTTP Status Codes
 */
@property (nonatomic,strong) NSArray     *warnings;


-(id)initWithDict:(NSDictionary *)dictionary;
-(void)parseJson;
-(EvaSearchCategory)getSearchCategory;
-(NSString *)getPendingQuestion;
// Keep this method private for now
//-(EvaFlow *)getPendingQuestionFlow;
-(NSString *)getFlowSayIt;
-(EvaLocation *)getFlowLocation;

-(BOOL)isHotelSearch;

-(EvaLocation *)getHotelLocation;
-(NSDictionary *)getFlightLocations;

-(NSUInteger)getHotelSmokingPreferenceIndex;
-(NSString *)getHotelContainingWords;
-(NSString *)getFlowSayIt;
// TODO : Implement later
-(BOOL)isFlightSearch;
-(BOOL)isTwoWayFlightSearch;
@end



/*
 Sample JSON Response
 
 Data from Eva
 {
 "status": true,
 "ver": "v1.0.3456",
 "input_text": "find me a hotel near Palace doing Christmas",
 "rid": null,
 "transaction_key": "11e2-d3b3-6f9ff902-9759-123139163cc4",
 "api_reply": {
     "Travelers": {
     "Adult": "1"
     },
     "ProcessedText": "find me a hotel near Palace doing Christmas",
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
     ]
     }
     ],
     "Hotel Attributes": {
     "Accommodation Type": "Hotel",
     "Castle": true
     }
     },
 "message": "Successful Parse",
 "session_id": "704f0aca-d3b3-11e2-9759-123139163cc4"
 }
 
 
 */