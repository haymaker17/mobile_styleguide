//
//  EvaJsonResponseHandler.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//
// Purpose :
// This class handles the JSON response from the Evature voice and parses the response into objects.
// Encapsulates the searching and showing of nextviewcontroller. 
// See Evature API documentation for details on the json tags output
// http://www.evature.com/docs/response.html
// See sample json text below the code.
//
// ===================================================================
// --- Unit Test strategy ---
// Case set 1. Init the class with sample JSON string and parse the string. Verify different sets of data. 
// Case set 2. Assert Check the parse status for error status for each. 
// Case set 3. Assert Verify the type of classes formed based on the json response. hotel/car/air
// Case set 4. Verify if the nextVC is identified correcly determined based on the the type of search
// Case set 5. Verify search results are returned correctly from MWS for a given type of search.
// Case set 6. (Future) Verify that the class can handle any type json response from evature and correct MWS endpoint is called based on the response.
// Unit Test Strategy :
// Use the sample json files to parse the json data.
//
// 
#import <Foundation/Foundation.h>
#import "EvaApiReply.h"
#import "DataConstants.h"
#import "ExMsgRespondDelegate.h"


@interface EvaJsonResponseHandler : NSObject <ExMsgRespondDelegate>

/*
 properties represent different json tags in the root level. The main object is the EvaApireply class which contains the actual details . 
*/
@property (nonatomic, copy) NSString					*input_text;
@property (nonatomic, copy) NSString					*version;
@property (nonatomic, copy) NSString					*rid;
@property (nonatomic, copy) NSString					*transaction_key;
@property (nonatomic, copy) NSString					*message;
@property (nonatomic, copy) NSString					*session_id;
@property BOOL	status;

@property EvaSearchCategory inputSearchCategory;

@property (nonatomic,copy) void(^onRespondToFoundData)(NSMutableDictionary *);

//designated init methods
-(id)initWithData:(NSData*)data evaSearchCategory:(EvaSearchCategory)evaSearchCategory;
-(instancetype)initWithDict:(NSDictionary*)dictionary evaSearchCategory:(EvaSearchCategory)evaSearchCategory;

-(EvaApiReply *)parseResponse;
-(BOOL)performSearch;
-(BOOL)isParseSuccess;
-(BOOL)canProceedSearch;
-(NSString *)getSearchCriteria ;

-(void)mockFlightsearch;
//-(HotelSearchCriteriaV2 *)getHotelSearchCriteria;

    
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