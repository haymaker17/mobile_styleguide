//
//  EvaJsonResponseHandler.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "EvaJsonResponseHandler.h"
#import "EvaApiReply.h"
#import "HotelSearch.h"
#import "HotelSearchCriteria.h"
#import "LocationResult.h"
#import "EvaLocation.h"
#import "FindHotels.h"
#import "HotelBookingManager.h"
#import "DateTimeFormatter.h"
#import "EntityAirCriteria.h"
#import "AirShop.h"
#import "AirShopResultsManager.h"
#import "EntityAirShopResults.h"
#import "Config.h"
#import "LabelConstants.h"
#import "PostMsgInfo.h"
#import "UserDefaultsManager.h"
#import "Config.h"
#import "DateTimeFormatter.h"
#import "HotelSearchCriteriaV2.h"

// Define private variables here

@interface EvaJsonResponseHandler ()

// Declare private properties
@property (strong, nonatomic) EvaApiReply	*apireply;
@property (strong, nonatomic) EntityAirCriteria *flightSearchCriteria;
@property (strong, nonatomic) NSMutableDictionary *flightParameterBag;
@property (strong, nonatomic) EvaLocation *endLocation ;
@property (strong, nonatomic) EvaLocation *startLocation;
@property (strong, nonatomic) NSDictionary *jsonResponse;
@property (strong, nonatomic) HotelSearchCriteriaV2 *hotelSearchCriteriaV2;

@end

@implementation EvaJsonResponseHandler

HotelSearch *hotelSearch = nil;

NSData *jsonData = nil;


#pragma - Init methods

-(id)initWithData:(NSData*)data evaSearchCategory:(EvaSearchCategory)evaSearchCategory
{
    self = [super init];
    if(self)
    {
        jsonData = [[NSData alloc] initWithData:data];
        self.inputSearchCategory = evaSearchCategory;
    }
    return self;
    
}

-(instancetype)initWithDict:(NSDictionary*)dictionary evaSearchCategory:(EvaSearchCategory)evaSearchCategory
{
    self = [super init];
    if(self)
    {
        self.jsonResponse = dictionary;
        self.inputSearchCategory = evaSearchCategory;
    }
    return self;
    
}


// Parse the input string into objects
// Return the reply so calling class can inspect the errors if needed
// This can be bool to indicate parse failed

-(EvaApiReply *)parseResponse
{
    // Deserialize data into JSON
    NSError *error = nil;
    NSDictionary *json = nil;
    if (jsonData != nil) {
       json = [NSJSONSerialization JSONObjectWithData:jsonData options:0 error:&error];
    }
    else if (self.jsonResponse != nil) {
        json = self.jsonResponse;
    }
    else
        return nil;
    
    
   
    // Handle error deserializing into JSON
    if (error != nil)
    {
        NSString *errorDomain = (error.domain == nil ? @"" : error.domain);
        NSString *localizedDescription = (error.localizedDescription == nil ? @"": error.localizedDescription);
        NSString *localizedFailureReason = (error.localizedFailureReason == nil ? @"" : error.localizedFailureReason);
        
        NSString *errorMessage = [NSString stringWithFormat:@"EvaJSONResponseHandler.::parseJsonString: Error code = %li, domain = %@, description = %@, failure reason = %@", (long)error.code, errorDomain, localizedDescription, localizedFailureReason];
        
        [[MCLogging getInstance] log:errorMessage Level:MC_LOG_ERRO];
        return nil;
    }

    // Expecting top-level JSON to be a dictionary
    if ([json count] > 0)
    {
        // An 'items' key should be in the dictionary
        // may move all the string literals to a common constants 

        self.status = [json[@"status"] boolValue];
        // Status is returned as BOOL so use this BOOL somestatus = items[@"status"];
        self.message = json[@"message"];
        self.session_id = json[@"session_id"];
        self.transaction_key = json[@"transaction_key"];
        self.rid = json[@"rid"];
        
        if([json[@"input_text"] isKindOfClass:[NSString class]])
            self.input_text = json[@"input_text"] ;
        else
            self.input_text = nil;
        
        if(!self.status || !json[@"api_reply"])
            return  nil;
        
        if([json[@"api_reply"] count] > 0)
        {
            self.apireply = [[EvaApiReply alloc]initWithDict:json[@"api_reply"]];
            self.apireply.evaSearchCategory = self.inputSearchCategory;
            [self.apireply parseJson];
        }
        else
            return nil;
    }
    // TODO:More checks
    // Also check if sayit is nil or processed text is nil
    
    return self.apireply;
}

// All the checks to see if parsing succeeded
-(BOOL)isParseSuccess
{
    BOOL result = NO;
    
    // check the "status" in the response
    
    if(!self.status)
        return NO;
    
//    result = [self.message lengthIgnoreWhitespace]
//                && ([self.message isEqualToString:EVA_SUCCESSFUL_PARSE] || [self.message isEqualToString:EVA_PARTIAL_PARSE])
//                    && ![self.message isEqualToString:EVA_NOTACCEPTABLE_PARSE]
//                        && ([self.apireply getSearchCategory] != EVA_UNKNOWN);
    
    result = [self.message lengthIgnoreWhitespace]
    && ([self.message isEqualToString:EVA_SUCCESSFUL_PARSE])
    && ![self.message isEqualToString:EVA_NOTACCEPTABLE_PARSE]
    && ([self.apireply getSearchCategory] != EVA_UNKNOWN);

    return result;
}

// returns true if we can proceed with an actual MWS search.
// Checks if the input response is same as eva response.
// Can add more checks here as required. 
-(BOOL)canProceedSearch
{
    return ([self.apireply.sayit lengthIgnoreWhitespace] && [self isParseSuccess] && self.inputSearchCategory == [self.apireply getSearchCategory]);
}

// Posts Search request to MWS based on the inputSearchCategory if parsing is success. 

-(BOOL)performSearch
{
    if(! [self isParseSuccess])
        return NO;
    // if initial searchg category is set then Check if type of search returned by json response is same is requested search category.
    // if they are requested and response search category are same then call appropriate method to build the search object and post message.
    // for now only category used is hotel.
    // Add handlers for Air/Car etc from here.
    
    EvaSearchCategory evaresponseSearchcategory = [self.apireply getSearchCategory];
    if(self.inputSearchCategory == EVA_UNKNOWN || self.inputSearchCategory == evaresponseSearchcategory )
    {
        switch (evaresponseSearchcategory) {
            case EVA_HOTELS:
                // Create hotelSearch object from Apireply and post the message
                    // Make this method public so handler can build search criteria text
                    // [self getHotelSearchCriteria];
                
                // TODO: Do some checks here ?? check if start date and end date looking good
                 // Post hotel search message
                // Save search criteria settings
                [hotelSearch.hotelSearchCriteria writeToSettings];
                [self sendHotelSearchMsg];
                return YES;
                break;
            case EVA_FLIGHTS :

                [self sendFlightSearchMsg];
                return YES;
            default:
                return NO;
                break;
        }
    }
    return NO;
}

// This method builds the search criteria for a given search category (inputSearchCategory) and populates corresponding search Criteria object.
// Returns a parsed Search Criteria in a string. Nil if fails

-(NSString *)getSearchCriteria
{
    NSString *searchCriteria = nil;
    if(! [self isParseSuccess])
        return searchCriteria;
    
    EvaSearchCategory evaresponseSearchcategory = [self.apireply getSearchCategory];
    
    if(self.inputSearchCategory == EVA_UNKNOWN || self.inputSearchCategory == evaresponseSearchcategory )
    {
        switch (evaresponseSearchcategory) {
            case EVA_HOTELS:
                searchCriteria =  [self getHotelSearchCriteria];
                break;
                /*
                 Flight search logic is different from Hotel search. The following are done for flight search
                 1. Create an managedobject called flightSearchCriteria.
                 2. Parse the JSON to fill the appropriate criteria fields
                 3. Build the parameter bag based on the flightSearchCriteria.
                 4. Check if the user profile has sufficient previleges for the country.
                 5. If has previleges then Send the message to search for flight
                 */

            case EVA_FLIGHTS :
                searchCriteria =  [self getFlightSearchCriteria];
                
            // Add other case statements here
            default:
                break;
        }
    }
    return searchCriteria;
}

#pragma mark - utility methods

-(NSString *)getSearchDestinationCityName
{
    return self.endLocation.name;
}

#pragma - mark Get different search results
-(NSString *)getHotelSearchCriteriaV2
{
    _hotelSearchCriteriaV2 = [[HotelSearchCriteriaV2 alloc] init];
    NSMutableString *hotelSearchString = nil;
    
    EvaLocation *hotelLocation =  [self.apireply getFlowLocation];  //[self.apireply getHotelLocation];
    LocationResult *locationResult  = [hotelLocation getLocationResult];
    _hotelSearchCriteriaV2.latitude = [locationResult.latitude doubleValue];
    _hotelSearchCriteriaV2.longitude = [locationResult.longitude doubleValue];
    self.endLocation = hotelLocation;
    // City
    hotelSearchString =  [[NSMutableString alloc] initWithFormat:@"Hotels near %@, ",hotelLocation.name];
    
    NSDate *hotelcheckinDate = [hotelLocation.arrivalDateTime getDateTime];
    NSDate *hotelCheckoutDate = [hotelLocation.departureDateTime getDateTime];
    
    if( hotelcheckinDate != nil )
    {
        _hotelSearchCriteriaV2.checkinDate = hotelcheckinDate;
        if(hotelCheckoutDate == nil )
        {
            NSInteger numofdays = 1;
            // Check if number of nights is given
            // Add delta days to the checkout date.
            numofdays = [hotelLocation getStayDuration];
            NSTimeInterval secondsInDay = 60.0 * 60.0 * 24.0 * numofdays; // 60 seconds per minute * 60 minutes per hour * 24 hours per day
            hotelCheckoutDate = [NSDate dateWithTimeInterval:secondsInDay sinceDate:hotelcheckinDate];
        }
        _hotelSearchCriteriaV2.checkoutDate = hotelCheckoutDate;
    }
    // Get the Hotel Attributes so we can determine room type, hotel chain, rating, etc.
    // set HotelSearchCriteria from hotel attributes
    //    [hotelSearchString appendFormat:@" arriving between %@ and %@ ",
    //                                                [DateTimeFormatter formatDateForBooking:hotelSearch.hotelSearchCriteria.checkinDate],
    //                                                [DateTimeFormatter formatDateForBooking:hotelSearch.hotelSearchCriteria.checkoutDate]];
    
    if(self.apireply.hotelAttributes != nil && [self.apireply.hotelAttributes count] > 0)
    {
        //self.smokingPreferenceCodes = @[@"0", @"N", @"S"]
        hotelSearch.hotelSearchCriteria.smokingIndex = [self.apireply getHotelSmokingPreferenceIndex];
        // TODO : if smoking preference is default do not add to read out text
        
        NSString *units  = [hotelLocation distanceUnit];
        // Set the distance and isMetric only if the units have kilometers or miles, units can be minutes also
        if([units lengthIgnoreWhitespace] && [units rangeOfString:@"Miles" options:NSCaseInsensitiveSearch].location != NSNotFound )
        {
            _hotelSearchCriteriaV2.isMetricDistance = @NO;
            _hotelSearchCriteriaV2.distanceValue =  [hotelLocation.distanceValue doubleValue];
            // Add distance value to read out string
            [hotelSearchString appendFormat:@"within %f miles",_hotelSearchCriteriaV2.distanceValue];
        }
        else if([units lengthIgnoreWhitespace] && [units rangeOfString:@"Kilometers" options:NSCaseInsensitiveSearch].location != NSNotFound )
        {
            _hotelSearchCriteriaV2.isMetricDistance = @YES;
            _hotelSearchCriteriaV2.distanceValue =  [hotelLocation.distanceValue doubleValue];
            // Add distance value to read out string.
            [hotelSearchString appendFormat:@"within %f kilometers",_hotelSearchCriteriaV2.distanceValue];
        }
        NSString  *containingwords = [self.apireply getHotelContainingWords];
        if(containingwords != nil)
        {
            _hotelSearchCriteriaV2.hotelName = containingwords;
            [hotelSearchString appendFormat:@" words containing %@",containingwords];
        }
        
    }
    // Check if location latitude and longitude are not nil
    if(_hotelSearchCriteriaV2.latitude == 0.0 ||
       _hotelSearchCriteriaV2.longitude == 0.0 )
    {
        return nil;
    }
    // Return the search string for read out.
    return hotelSearchString;

    
}

// Sets the Hotel search critera 
-(NSString *)getHotelSearchCriteria
{
    NSMutableString *hotelSearchString = nil;
    // Create hotelSearch object
    hotelSearch = [[HotelSearch alloc] init];
    EvaLocation *hotelLocation =  [self.apireply getFlowLocation];  //[self.apireply getHotelLocation];
    hotelSearch.hotelSearchCriteria.locationResult  = [hotelLocation getLocationResult];
    self.endLocation = hotelLocation;
    // City
    hotelSearchString =  [[NSMutableString alloc] initWithFormat:@"Hotels near %@, ",hotelLocation.name];

    NSDate *hotelcheckinDate = [hotelLocation.arrivalDateTime getDateTime];
    NSDate *hotelCheckoutDate = [hotelLocation.departureDateTime getDateTime];
    
    if( hotelcheckinDate != nil )
    {
        hotelSearch.hotelSearchCriteria.checkinDate = hotelcheckinDate;
        if(hotelCheckoutDate == nil )
        {
            NSInteger numofdays = 1;
            // Check if number of nights is given
            // Add delta days to the checkout date.
            numofdays = [hotelLocation getStayDuration];
            NSTimeInterval secondsInDay = 60.0 * 60.0 * 24.0 * numofdays; // 60 seconds per minute * 60 minutes per hour * 24 hours per day
            hotelCheckoutDate = [NSDate dateWithTimeInterval:secondsInDay sinceDate:hotelcheckinDate];
        }
        hotelSearch.hotelSearchCriteria.checkoutDate = hotelCheckoutDate;
    }
    // Get the Hotel Attributes so we can determine room type, hotel chain, rating, etc.
    // set HotelSearchCriteria from hotel attributes
//    [hotelSearchString appendFormat:@" arriving between %@ and %@ ",
//                                                [DateTimeFormatter formatDateForBooking:hotelSearch.hotelSearchCriteria.checkinDate],
//                                                [DateTimeFormatter formatDateForBooking:hotelSearch.hotelSearchCriteria.checkoutDate]];
    
    if(self.apireply.hotelAttributes != nil && [self.apireply.hotelAttributes count] > 0)
    {
        //self.smokingPreferenceCodes = @[@"0", @"N", @"S"]
        hotelSearch.hotelSearchCriteria.smokingIndex = [self.apireply getHotelSmokingPreferenceIndex];
        // TODO : if smoking preference is default do not add to read out text
        
        NSString *units  = [hotelLocation distanceUnit];
        // Set the distance and isMetric only if the units have kilometers or miles, units can be minutes also 
        if([units lengthIgnoreWhitespace] && [units rangeOfString:@"Miles" options:NSCaseInsensitiveSearch].location != NSNotFound )
        {
            hotelSearch.hotelSearchCriteria.isMetricDistance = @NO;
            hotelSearch.hotelSearchCriteria.distanceValue =  hotelLocation.distanceValue;
            // Add distance value to read out string
            [hotelSearchString appendFormat:@"within %@ miles",hotelSearch.hotelSearchCriteria.distanceValue];
        }
        else if([units lengthIgnoreWhitespace] && [units rangeOfString:@"Kilometers" options:NSCaseInsensitiveSearch].location != NSNotFound )
        {
            hotelSearch.hotelSearchCriteria.isMetricDistance = @YES;
            hotelSearch.hotelSearchCriteria.distanceValue =  hotelLocation.distanceValue;
            // Add distance value to read out string.
            [hotelSearchString appendFormat:@"within %@ kilometers",hotelSearch.hotelSearchCriteria.distanceValue];
        }
        NSString  *containingwords = [self.apireply getHotelContainingWords];
        if(containingwords != nil)
        {
            hotelSearch.hotelSearchCriteria.containingWords = containingwords;
            [hotelSearchString appendFormat:@" words containing %@",containingwords];
        }
        
    }
    // Check if location latitude and longitude are not nil
    if(hotelSearch.hotelSearchCriteria.locationResult.latitude == nil ||
            hotelSearch.hotelSearchCriteria.locationResult.longitude == nil )
    {
        return nil;
    }
    // Return the search string for read out. 
    return hotelSearchString;

}
#pragma mark - Flight search methods


/**
 Returns the flight search criteria string
 */
-(NSString *)getFlightSearchCriteria
{
    NSMutableString *flightSearchString = nil;
    self.flightParameterBag = [[NSMutableDictionary alloc] init];
    NSDictionary *locations  = nil;
    
    self.flightSearchCriteria = [self loadEntity];
    if(self.flightSearchCriteria == nil)
        self.flightSearchCriteria = [self makeNewEntity];
    
   locations =  [self.apireply getFlightLocations];
    
    EvaLocation *departureLocation = self.startLocation = [locations objectForKey:@"Source"];
    EvaLocation *arrivalLocation = self.endLocation = [locations objectForKey:@"Destination"];

    //(lldb) po lastAir.DepartureCity
    //Seattle Tacoma Intl Arpt, Seattle, WA (SEA)
    //(lldb) po lastAir.ArrivalCity
    //McCarran Intl, Las Vegas, NV (LAS)
    
    // Add nil check here.
    // Return nil query string if any of these are nil
    if(departureLocation == nil || arrivalLocation == nil)
        return nil;
    
    // City
    flightSearchString =  [[NSMutableString alloc] initWithFormat:@"Flights to %@ ", arrivalLocation.name];

    self.flightParameterBag[@"StartIata"] = [departureLocation getAirportCode];;
    self.flightParameterBag[@"EndIata"] = [arrivalLocation getAirportCode];
    
    // Handle if Date time is nil
    if (departureLocation.departureDateTime == nil ) {
        // Default it to today + 3 days
        self.flightParameterBag[@"Date"] = @"2014-06-09";
        self.flightParameterBag[@"SearchTime"] = @"9";
        self.flightParameterBag[@"ReturnDate"] = @"2014-06-11";
        self.flightParameterBag[@"ReturnTime"] = @"12";
        self.flightSearchCriteria.DepartureDate = [DateTimeFormatter getLocalDateMedium:@"2014-06-09"];
        self.flightSearchCriteria.DepartureTime = [NSNumber numberWithInt:9];
        self.flightSearchCriteria.ReturnDate = [DateTimeFormatter getLocalDateMedium:@"2014-06-11"];
        self.flightSearchCriteria.ReturnTime = [NSNumber numberWithInt:12];
        
    }
    else
    {
        self.flightParameterBag[@"Date"] = [departureLocation.departureDateTime date];
        self.flightSearchCriteria.DepartureDate = [departureLocation.departureDateTime getDateTime];
        NSDate* dawnDate = [DateTimeFormatter getDateWithoutTime:self.flightSearchCriteria.DepartureDate withTimeZoneAbbrev:nil];
        NSInteger tmInMinutes = [self.flightSearchCriteria.DepartureDate timeIntervalSinceDate:dawnDate]/60;
        
        self.flightParameterBag[@"SearchTime"] = [NSString stringWithFormat:@"%ld", (tmInMinutes+59)/60];
        self.flightSearchCriteria.DepartureTime = [NSNumber numberWithInteger:(tmInMinutes+59)/60] ;

        if ([self.apireply isTwoWayFlightSearch])
        {
            
            self.flightParameterBag[@"ReturnDate"] = [arrivalLocation.departureDateTime date];
            //        lastAir.ReturnDate = [bcd.dateValue dateByAddingTimeInterval:[bcd.val2 intValue]*60];
            dawnDate = [DateTimeFormatter getDateWithoutTime:[arrivalLocation.departureDateTime getDateTime] withTimeZoneAbbrev:nil];
            tmInMinutes = [[arrivalLocation.departureDateTime getDateTime] timeIntervalSinceDate:dawnDate]/60;
            
            self.flightParameterBag[@"ReturnTime"] = [NSString stringWithFormat:@"%ld", (tmInMinutes+59)/60];
            //        lastAir.ReturnTime = nil;
            self.flightSearchCriteria.ReturnDate = [arrivalLocation.departureDateTime getDateTime];
            self.flightSearchCriteria.ReturnTime = [NSNumber numberWithInteger:(tmInMinutes+59)/60] ;
        }

    }
    //TODO: handle refundable -
    self.flightParameterBag[@"RefundableOnly"] = @"Y";
    // TODO : Handle classofservice. 
    self.flightParameterBag[@"ClassOfService"] = @"";
    self.flightSearchCriteria.DepartureCity = [departureLocation getLocationResult].location ;
    self.flightSearchCriteria.DepartureAirportCode = [departureLocation getAirportCode];
    // getDateTime uses a different format. have to check if its same as manual Air search
    self.flightSearchCriteria.ArrivalCity = [arrivalLocation getLocationResult].location;
    self.flightSearchCriteria.ReturnAirportCode = [arrivalLocation getAirportCode];  
    self.flightSearchCriteria.ArrivalCity = [arrivalLocation getLocationResult].location;
    self.flightSearchCriteria.ReturnAirportCode = [arrivalLocation getAirportCode];
    
    if ([Config isGov])
        self.flightParameterBag[@"GovRateTypes"] = @"true";
    else
        self.flightParameterBag[@"GovRateTypes"] = @"false";
	
    self.flightParameterBag[@"isRound"] = @([self.apireply isTwoWayFlightSearch]);
    
    self.flightParameterBag[@"SEARCH_UUID"] = [PostMsgInfo getUUID];

    [self saveEntity];
//
//    bcd = [self getBCD:@"To"];
//    pBag[@"EndIata"] = bcd.val2;
//    self.flightSearchCriteria.ArrivalCity = bcd.val;
//    self.flightSearchCriteria.ReturnAirportCode = bcd.val2;
//    
//    // MOB-7240 the date in cell is gmt date, and needs to be formatted using gmt time zone
//    bcd = [self getBCD:@"DepartureDate"];
//    NSString* fmtDate = [DateTimeFormatter formatDateYYYYMMddByDate:bcd.dateValue];
//    
//    pBag[@"Date"] = fmtDate;
//    
//    self.flightSearchCriteria.DepartureDate = [bcd.dateValue dateByAddingTimeInterval:[bcd.val2 intValue]*60];
//    pBag[@"SearchTime"] = [NSString stringWithFormat:@"%d", bcd.extendedTime];
//    lastAir.DepartureTime = nil;
//    
//    // TODO : Add check to identify if its a round trip
//    if (isRoundTrip)
//    {
//        bcd = [self getBCD:@"ReturnDate"];
//        pBag[@"ReturnDate"] = [DateTimeFormatter formatDateYYYYMMddByDate:bcd.dateValue];
//        lastAir.ReturnDate = [bcd.dateValue dateByAddingTimeInterval:[bcd.val2 intValue]*60];
//        pBag[@"ReturnTime"] = [NSString stringWithFormat:@"%d", bcd.extendedTime];
//        lastAir.ReturnTime = nil;
//    }
//    // TODO : identify how to get refundable flights
////    if (showCheckbox)
////    {
////        bcd = [self getBCD:@"RefundableOnly"];
////        if (bcd != nil && bcd.val != nil)
////        {
////            pBag[@"RefundableOnly"] = bcd.val;
////        }
////        lastAir.refundableOnly = @([@"Y" isEqualToString:bcd.val]);
////    }
////    else
//        self.flightSearchCriteria.refundableOnly = @NO;
//    
//    bcd = [self getBCD:@"ClassOfService"];
//    if (bcd != nil) {
//        self.flightSearchCriteria.ClassOfService = bcd.val2;
//        NSString *cos = bcd.val2;
//        if([cos isEqualToString:@"ANY"])
//            cos = @"";
//        pBag[@"ClassOfService"] = cos;
//    }
//    else    //If no class of service option allowed. Always use Economy as default
//    {
//        self.flightSearchCriteria.ClassOfService = @"Y";
//        pBag[@"ClassOfService"] = @"Y";
//    }
//    

    return flightSearchString;
}

// =============== Hard Code Everything here for the Fusion 2014 Demo =======================
-(void)mockFlightsearch
{
      // =========== To use this methord copy this part into the evaDidreceiveData method
//    if ([Config isFusion2014] && (self.category == EVA_FLIGHTS)) {
//        [self showCannedResponse];
//        EvaJsonResponseHandler * fakehandler = [[EvaJsonResponseHandler alloc] initWithData:dataFromServer
//                                                                          evaSearchCategory:self.category];
//         EvaApiReply *apireply = [fakehandler parseResponse];
//        // Set callback code here.
//        [fakehandler setOnRespondToFoundData:^(NSMutableDictionary *pBag) {
//       
//            [self printTic:@"evaDidReceiveData Respondtofounddata"];
//            DLog(@"pBag = %@", pBag);
//            // TODO: What do we do if no AirShop items come back?
//            // TODO : find CXWaitViewController and Dismiss only if its a waitview showing.
//            //            [self dismissViewControllerAnimated:NO completion:nil];
//            self.isShowingSearchResults = YES;
//            [self showSearchResults:pBag];
//        }];
//        
//        [fakehandler mockFlightsearch];
//        return;
//    }

    if ([Config isNewAirBooking]) {
        self.flightParameterBag = [[NSMutableDictionary alloc] init];
        self.flightSearchCriteria = [self loadEntity];
        if(self.flightSearchCriteria == nil)
            self.flightSearchCriteria = [self makeNewEntity];
        
        self.flightParameterBag[@"StartIata"] = @"SEA";
        self.flightParameterBag[@"EndIata"] = @"SFO";
        self.flightParameterBag[@"Date"] = @"2014-06-09";
        self.flightParameterBag[@"SearchTime"] = @"7";
        self.flightParameterBag[@"ReturnDate"] = @"2014-06-11";
        self.flightParameterBag[@"ReturnTime"] = @"17";
        self.flightSearchCriteria.DepartureDate = [DateTimeFormatter getLocalDateMedium:@"2014-06-09"];
        self.flightSearchCriteria.DepartureTime = [NSNumber numberWithInt:9];
        self.flightSearchCriteria.ReturnDate = [DateTimeFormatter getLocalDateMedium:@"2014-06-11"];
        self.flightSearchCriteria.ReturnTime = [NSNumber numberWithInt:12];
        self.flightSearchCriteria.DepartureCity = @"Seattle, WA" ;
        self.flightSearchCriteria.DepartureAirportCode = @"SEA";
        // getDateTime uses a different format. have to check if its same as manual Air search
        self.flightSearchCriteria.ArrivalCity = @"San Franscisco, CA";
        self.flightSearchCriteria.ReturnAirportCode = @"SFO";
        if ([Config isGov])
            self.flightParameterBag[@"GovRateTypes"] = @"true";
        else
            self.flightParameterBag[@"GovRateTypes"] = @"false";
        
        self.flightParameterBag[@"isRound"] = @"YES";
        self.flightParameterBag[@"ClassOfService"] = @"";
        self.flightParameterBag[@"SEARCH_UUID"] = [PostMsgInfo getUUID];

        [self saveEntity];
        [self sendFlightSearchMsg];
    }

}
#pragma mark EntityAirCriteria Handler methods
// These methods should be in a common data handler. Added these here for now to
-(EntityAirCriteria *) loadEntity
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityAirCriteria" inManagedObjectContext:[ExSystem sharedInstance].context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *a = [[ExSystem sharedInstance].context executeFetchRequest:fetchRequest error:&error];
    
    if([a count] > 0)
        return a[0];
    else
        return nil;
}


-(void) saveEntity
{
    NSError *error;
    if (![[ExSystem sharedInstance].context save:&error]) {
        NSLog(@"Whoops, couldn't save air criteria: %@", [error localizedDescription]);
    }
}


-(void) clearEntity:(EntityAirCriteria *) ent
{
    [[ExSystem sharedInstance].context deleteObject:ent];
}


-(EntityAirCriteria *) makeNewEntity
{
    return [NSEntityDescription insertNewObjectForEntityForName:@"EntityAirCriteria" inManagedObjectContext:[ExSystem sharedInstance].context];
}


// Post the MWS message for search results.
-(void) sendHotelSearchMsg
{
    NSMutableDictionary *pBag =nil;
    pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:hotelSearch, @"HOTEL_SEARCH", @"YES", @"SKIP_CACHE", @"0", @"STARTPOS", @"30", @"NUMRECORDS", nil];
    
    [[ExSystem sharedInstance].msgControl createMsg:FIND_HOTELS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void)sendFlightSearchMsg
{
//    if([Config isFusion14DemoMode])
//    {
//        sleep(6);
//    }
//    
//    if ([Config isFusion14DemoMode]) {
//        [[ExSystem sharedInstance].msgControl createMsg:AIR_SHOP CacheOnly:@"YES" ParameterBag:self.flightParameterBag SkipCache:NO RespondTo:self];
//    }
//    else {
        [[ExSystem sharedInstance].msgControl createMsg:AIR_SHOP CacheOnly:@"NO" ParameterBag:self.flightParameterBag SkipCache:YES RespondTo:self];
//    }
}

# pragma mark - implementation delegate msghandler

-(void) didProcessMessage:(Msg *)msg
{
    [self respondToFoundData:msg]; // TODO: handle case where msg.didConnectionFail is YES
}

-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:FIND_HOTELS])  // Handle hotel search MWS response. 
    {
        //NSArray *aHotels = [[HotelBookingManager sharedInstance] fetchAll];
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SHOW_HOTELS", hotelSearch, @"HOTEL_SEARCH", @"YES", @"SHORT_CIRCUIT", nil];
        pBag[@"HOTEL_SEARCH_CRITERIA"] = (msg.parameterBag)[@"HOTEL_SEARCH_CRITERIA"];
        FindHotels *findHotels = (FindHotels*)msg.responder;
        pBag[@"TOTAL_COUNT"] = @(findHotels.totalCount);
        //TODO: tafields for gov ??
        // Call back the code block with pbag
        self.onRespondToFoundData(pBag);
    }
    if ([msg.idKey isEqualToString:AIR_SHOP])
    {
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] init];
        if (msg.responseCode == 200 || msg.isCache) {
            AirShop *airShop = (AirShop*) msg.responder;
            [self dumpResultsIntoEntity:airShop];
            [pBag setObject:airShop forKey:@"AIR_SHOP"];
        }
        else
        {
            pBag[@"Error"] = @"YES";
            pBag[@"ErrorBody"] = msg.errBody;
            pBag[@"ErrorCode"] = msg.errCode;
        }
        self.onRespondToFoundData(pBag);

    }
    
}


-(void) dumpResultsIntoEntity:(AirShop*) airShop
{
    [[AirShopResultsManager sharedInstance] deleteAll];
    int iTotalCount = 0;
    double dLowest = 99999999999;
    NSString *lastCrnCode = @"USD";
    
    if ([Config isGov]){
        for(NSString *key in airShop.rateTypeChoices)
        {
            int totalChoices = 0;
            double lowest = 99999999999;
            NSArray *a = [airShop.rateTypeChoices objectForKey:key];
            for(AirlineEntry *ae in a)
            {
                EntityAirShopResults *airShopResults = (EntityAirShopResults*)[[AirShopResultsManager sharedInstance] makeNew];
                airShopResults.airline = ae.airline;
                airShopResults.airlineName = [airShop.vendors objectForKey:ae.airline];
                airShopResults.numChoices = ae.numChoices;
                totalChoices += [ae.numChoices intValue];
                iTotalCount += [ae.numChoices intValue];
                airShopResults.rateType = ae.rateType;
                airShopResults.lowestCost = ae.lowestCost;
                if(lowest > [ae.lowestCost doubleValue])
                    lowest = [ae.lowestCost doubleValue];
                
                if(dLowest > [ae.lowestCost doubleValue])
                    dLowest = [ae.lowestCost doubleValue];
                
                if(![ae.crnCode length])
                    airShopResults.crnCode = @"USD";
                else
                    airShopResults.crnCode = ae.crnCode;
                
                lastCrnCode = airShopResults.crnCode;
                
                airShopResults.pref = ae.pref;
                [[AirShopResultsManager sharedInstance] saveIt:airShopResults];
            }
            
            EntityAirShopResults *airShopResults = (EntityAirShopResults*)[[AirShopResultsManager sharedInstance] makeNew];
            airShopResults.airline = @"ZZZZZZZZTOTAL";
            airShopResults.airlineName = @"ZZZZZZZZTOTAL";
            airShopResults.numChoices = @(totalChoices);
            airShopResults.rateType = key;
            airShopResults.lowestCost = @(lowest);
            airShopResults.crnCode = lastCrnCode;
            airShopResults.pref = @"";
            [[AirShopResultsManager sharedInstance] saveIt:airShopResults];
        }
        
        if([airShop.rateTypeChoices count] > 0)
        {
            EntityAirShopResults *airShopResults = (EntityAirShopResults*)[[AirShopResultsManager sharedInstance] makeNew];
            airShopResults.airline = @"   TOTAL";
            airShopResults.airlineName = @"   TOTAL";
            airShopResults.numChoices = [NSNumber numberWithInt:iTotalCount];
            airShopResults.rateType = @"";
            airShopResults.lowestCost = [NSNumber numberWithDouble:dLowest];
            airShopResults.crnCode = lastCrnCode;
            airShopResults.pref = @"";
            [[AirShopResultsManager sharedInstance] saveIt:airShopResults];
        }
    }
    else{                  //Concur Mobile
        for(NSString *key in airShop.stopChoices)
        {
            int totalChoices = 0;
            double lowest = 99999999999;
            NSArray *a = (airShop.stopChoices)[key];
            for(AirlineEntry *ae in a)
            {
                EntityAirShopResults *airShopResults = (EntityAirShopResults*)[[AirShopResultsManager sharedInstance] makeNew];
                airShopResults.airline = ae.airline;
                airShopResults.airlineName = (airShop.vendors)[ae.airline];
                airShopResults.numChoices = ae.numChoices;
                totalChoices += [ae.numChoices intValue];
                iTotalCount += [ae.numChoices intValue];
                airShopResults.numStops = ae.numStops;
                airShopResults.lowestCost = ae.lowestCost;
                if(lowest > [ae.lowestCost doubleValue])
                    lowest = [ae.lowestCost doubleValue];
                
                if(dLowest > [ae.lowestCost doubleValue])
                    dLowest = [ae.lowestCost doubleValue];
                
                if(![ae.crnCode length])
                    airShopResults.crnCode = @"USD";
                else
                    airShopResults.crnCode = ae.crnCode;
                
                lastCrnCode = airShopResults.crnCode;
                
                airShopResults.pref = ae.pref;
                [[AirShopResultsManager sharedInstance] saveIt:airShopResults];
            }
            
            EntityAirShopResults *airShopResults = (EntityAirShopResults*)[[AirShopResultsManager sharedInstance] makeNew];
            airShopResults.airline = @"ZZZZZZZZTOTAL";
            airShopResults.airlineName = @"ZZZZZZZZTOTAL";
            airShopResults.numChoices = @(totalChoices);
            airShopResults.numStops = @([key intValue]);
            airShopResults.lowestCost = @(lowest);
            airShopResults.crnCode = lastCrnCode;
            airShopResults.pref = @"";
            [[AirShopResultsManager sharedInstance] saveIt:airShopResults];
        }
        
        if([airShop.stopChoices count] > 0)
        {
            EntityAirShopResults *airShopResults = (EntityAirShopResults*)[[AirShopResultsManager sharedInstance] makeNew];
            airShopResults.airline = @"   TOTAL";
            airShopResults.airlineName = @"   TOTAL";
            airShopResults.numChoices = @(iTotalCount);
            airShopResults.numStops = @-1;
            airShopResults.lowestCost = @(dLowest);
            airShopResults.crnCode = lastCrnCode;
            airShopResults.pref = @"";
            [[AirShopResultsManager sharedInstance] saveIt:airShopResults];
        }
    }
}



@end

