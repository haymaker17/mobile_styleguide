//
//  EvaApiReply.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//
//
//

#import "EvaApiReply.h"
#import "HotelSearch.h"
#import "EvaLocation.h"


@interface EvaApiReply()

@property (nonatomic,strong) NSData *api_reply;
@property (nonatomic,strong) NSDictionary *api_reply_dict;
@property (nonatomic,strong) HotelSearch *hotelsearch;
@property (nonatomic,strong) NSMutableArray *evaFlows;

@end

@implementation EvaApiReply

-(id)initWithDict:(NSDictionary *)dictionary
{
    self = [super init];
    if(self)
    {
        self.api_reply_dict = [[NSDictionary alloc] initWithDictionary:dictionary];
    }
    return self;

}

-(void)parseJson
{
    if(self.api_reply_dict !=nil && [self.api_reply_dict count] > 0)
    {
        [self parseJsonDictionary];
    }
}

// Parse the api_reply dictionary
-(void)parseJsonDictionary
{
    // Get Attributes. Each attribute is a multitude of different key value settings. 
    if( self.api_reply_dict[@"Hotel Attributes"]!=nil )
    {
        self.hotelAttributes = [[NSDictionary alloc] initWithDictionary:self.api_reply_dict[@"Hotel Attributes"]];
         NSLog(@"Hotel attribute : %@", self.hotelAttributes[@"Accommodation Type"]);
    }
    
    if(self.api_reply_dict[@"Car Attributes"]!=nil)
    {
        self.carAttributes = [[NSDictionary alloc] initWithDictionary:self.api_reply_dict[@"Car Attributes"]];
    }
    
    if(self.api_reply_dict[@"Flight Attributes"]!=nil)
    {
        self.flightAttributes = [[NSDictionary alloc] initWithDictionary:self.api_reply_dict[@"Flight Attributes"]];
    }
    if(self.api_reply_dict[@"EvaMoney"]!=nil)
    {
        self.carAttributes = [[NSDictionary alloc] initWithDictionary:self.api_reply_dict[@"Car Attributes"]];
    }
    if(self.api_reply_dict[@"SayIt"]!=nil)
    {
        self.sayit = self.api_reply_dict[@"SayIt"];
    }
    if(self.api_reply_dict[@"Flow"])
    {
       NSArray *flows = self.api_reply_dict[@"Flow"];
        // if the resposne has any flows then parse the flow
        if([flows count] > 0)
        {
            self.evaFlows = [[NSMutableArray alloc] initWithCapacity:[flows count]];
            for (NSDictionary *flowdict in flows) {
                EvaFlow *evaflow = [[EvaFlow alloc] initWithDict:flowdict];
                [self.evaFlows addObject:evaflow];
            }
        }
        
    }

    // Handle locations
    // Locations are an array get the locatins array
    // each locations is a dictionary.
    NSArray *locations = self.api_reply_dict[@"Locations"];
    if([locations count]>0)
    {
        self.evaLocations = [[NSMutableArray alloc]initWithCapacity:[locations count]];
    }
    for (NSDictionary *location in locations) {
        EvaLocation *evalocation = [[EvaLocation alloc]initWithDict:location];
        [self.evaLocations addObject:evalocation];
    }
    
    if(self.api_reply_dict[@"Warnings"])
    {
        self.warnings = [[NSArray alloc] initWithArray:self.api_reply_dict[@"Warnings"]];
    }
}

#pragma  mark - Utility methods

    // Helper method to return the search category based on other checks
//-(EvaSearchCategory)getSearchCategory;
//{
//    if([self isHotelSearch])
//        return EVA_HOTELS;
//    if([self isFlightSearch])
//        return EVA_FLIGHTS;
//    
//    return EVA_UNKNOWN;
//}


// GetSearch category based on flow
-(EvaSearchCategory)getSearchCategory
{
    /*
     if flow has an open question then
     check the question Actiontype.
     if question actionType is unknown then
     check the search type of Location pointed by the flow.
     */

    EvaFlow *questionflow = [self getFlowforSearchCategory:EVA_QUESTION];
    if(questionflow != nil)
        return questionflow.evaFlowType;

    
    questionflow = [self getFlowforSearchCategory:self.evaSearchCategory];
    if(questionflow != nil)
        return self.evaSearchCategory ;
    
    // Try getting the flow location 
//    EvaLocation *flowLocation = [self getFlowLocation];
//    if(flowLocation != nil)
//    {
//        // found a locaiton that matches the current search criteria so just return true
//        
//        EvaSearchCategory flocActionType = [flowLocation getActionsforType];
//        if (flocActionType != EVA_UNKNOWN )
//            return flocActionType;
//
//        flocActionType = [flowLocation getTransportType];
//        if(flocActionType != EVA_UNKNOWN)
//            return flocActionType;
//    }
    
    
    return  EVA_UNKNOWN;
}


// Determine if its a hotel search.
// Check if the hotel attributes are set in the API reply otherwise look into the locations for the type of search 
-(BOOL)isHotelSearch
{
    
    // Use flow to determine if the type of search
    
    return [self getSearchCategory] == EVA_HOTELS ;
//
//    if (self.evaLocations != nil && [self.evaLocations count] > 0) {
//        EvaLocation *firstLocation = self.evaLocations[0];
//        if ( [firstLocation getTransportType] == EVA_HOTELS) {
//            return NO;
//        }
//        EvaLocation *secondLocation = self.evaLocations[1];
//        if (secondLocation.actions == nil) {
//            return YES;
//        } else {
//            if ([secondLocation getActionsforType] == EVA_HOTELS) {
//                return YES;
//            }          
//        }
//    }
//    return NO;
    
}

// Check if this is a flight search. 
-(BOOL)isFlightSearch
{
//    NSLog(@"EvaApiReply::isFlightSearch : NOT IMPLEMENTED");
//    return NO;
    return [self getSearchCategory] == EVA_FLIGHTS ;
}

// Returns pending question if its valid otherwise nil.
-(NSString *)getPendingQuestion
{
    EvaFlow *questionFlow = [self getFlowforSearchCategory:EVA_QUESTION];
       
    //TODO : Probably check if flow.questioncategory  is "Informative"
    if( questionFlow.actionType == EVA_UNKNOWN ||  questionFlow.actionType == self.evaSearchCategory )
    {
        return questionFlow.sayIt;
    }
    
    return  nil;
}

// Use flow :
// When using flow for Search for the type Hotel/given type in the flow
// Get the location pointed by the flow
// SayIt Text is from the flow for given type

-(EvaFlow *)getFlowforSearchCategory:(EvaSearchCategory)category
{
    EvaFlow *matchingflow = nil;
    for (EvaFlow *flow in self.evaFlows) {
        // If there is an open question then pick that first 
        if(flow.evaFlowType == category)
        {
            matchingflow = flow ;
            break;
        }
    }

    return matchingflow;
}

// Get the location pointed by the flow
-(EvaLocation *)getFlowLocation
{
    EvaLocation *flowLocation = nil;
    // get the location pointed by the related location.
    // if there is more than 1 then take the first location.
    
    // TODO : Return all the matching flow locations as described in the flow related locations.
    EvaFlow *flow = [self getFlowforSearchCategory:self.evaSearchCategory];

    if([flow.evaRelatedLocations count]>0)
    {
        flowLocation = self.evaLocations[[flow.evaRelatedLocations[0] intValue]];
    }
    
    return flowLocation;
}

// Get the sayit for the final location 
-(NSString *)getFlowSayIt
{
    return [self getFlowforSearchCategory:self.evaSearchCategory].sayIt ;
}

#pragma mark - flight related methods
/**
 This method returns the arrival and departure locations for flight search.
 @returns empty dictionary if not found
 */
-(NSDictionary *)getFlightLocations
{
    NSMutableDictionary *flightLocations = [[NSMutableDictionary alloc]init];
    // get all Locations.
    // check each locations and set departure and arrival.
    // There may not be any locations other than flight since the scope and context filters other locations
    // we dont support if its not a return trip and has multiple hops. nice to have is to identify if user is going more than one hop
        
    BOOL isRoundTrip = [self isTwoWayFlightSearch];
    NSLog(@"isTwoWay:%@" ,isRoundTrip ? @"YES" : @"NO");
    // Make sure there are alteast one location

    // TODO : Handle if there are more than 3 locations ??
    for ( int index = 0; index < [self.evaLocations count]; index++) {
        
        // Check if the location category is same as the flight
        EvaLocation *location = self.evaLocations[index];
        // if the location has departure time or arrival time then those are the departure and arrival locations
        if (index == 0 ) {
            [flightLocations setObject:location forKey:@"Source"];
        }
        
        if (index == 1) {
            [flightLocations setObject:location forKey:@"Destination"];
        }
    }
    return flightLocations;
    
}

/**
 returns true if flight search is two way
 */

-(BOOL)isTwoWayFlightSearch
{
    if ([self.flightAttributes objectForKey:@"Two-Way"]) {
        return YES;
    }
    else
        return NO;
}

#pragma mark - Hotel Related methods

// Returns the destination hotel location
-(EvaLocation *)getHotelLocation
{
    
    // Getting hotel location is not straight forward
    
    // step 1
    // Get the locations
    // Get the 0-index Location to find the "Next" Location.
    // Get the first non-0 (non Home) Location.

    // step 2
    // Get the lat/long of the location.
    // If it's not specified, default to the user's current (GPS/cell) location.
    
    // step 3
    // Get the location name. If it's not specified,
    // use the current (GPS/cell) Address attribute(s).
    
    // Fuzzy logic, could use better implementation
    NSMutableDictionary *locationDictionary = [[NSMutableDictionary alloc]initWithCapacity:[self.evaLocations count]];
  
    NSString *nextIndex = nil;
    EvaLocation *nonHomeLocation = nil;
    
    // Get the 0-index Location to find the "Next" Location.
    // work around by iterating through dictionary.
    
    for (EvaLocation *location in self.evaLocations) {
        // Check for index fails since the parser is returning the type as int 
        if(location.Index != nil)
        {
            locationDictionary[location.Index] = location;
            if ([location.Index intValue] == 0) {
                nextIndex = location.Next;
            }
        }
    }
    
    // Return first non-0 location
    for (NSString *key in locationDictionary) {
        if([key intValue] == [nextIndex intValue] && [nextIndex intValue] != 0)
        {
            nonHomeLocation = locationDictionary[nextIndex];
        }
    }
    
    // Err: if No appropriate Location in non-home, so Log error and try other means.
    if(nonHomeLocation == nil)
    {
       //This is bad news too. non-nome locaiton is missing. Log some error here.
        [[MCLogging getInstance] log:@"No location found" Level:MC_LOG_ERRO];
        
        // Try another way
        // Check if there are more locations in the list
        if([self.evaLocations count] > 1 )
        {
            // so there are more than one location but not linked. so get the next location if location 0 is default
            // then return the second location
            EvaLocation *firstlocaton =self.evaLocations[0];
            if([firstlocaton containsKey:@"Home"] )
            {
                nonHomeLocation = self.evaLocations[1];
            }
        }
     }
    
    
    return nonHomeLocation;
}


-(NSUInteger)getHotelSmokingPreferenceIndex
{
   // self.smokingPreferenceCodes =  @[@"0", @"N", @"S"];
    // Codes are taken from hotelSearchCriteria.m logic
    NSUInteger smokingindex = 0 ; // no preference by default
    // json valueForKeyPath:@"response.hotel.array"]
    if([self.hotelAttributes objectForKey:@"Rooms"] )
    {
        NSArray *rooms = [self.hotelAttributes objectForKey:@"Rooms"];
        DLog(@"roomtypes %@",[rooms class]);
        // Rooms is an Array get the first room types
        if ([rooms count] == 0) {
            return  smokingindex;
        }
        NSDictionary *roomTypes = rooms[0];
        if([roomTypes objectForKey:@"Smoking"] )
        {
            BOOL isSmoking = [roomTypes objectForKey:@"Smoking"];
            smokingindex = isSmoking ? 2 : 1;
        }
    }
    return smokingindex;
}

-(NSString *)getHotelContainingWords
{
    NSString *words = nil;
    if([self.hotelAttributes objectForKey:@"Chain"] )
    {
        words = [self.hotelAttributes objectForKey:@"Chain"];
    }
    return words;
}


@end
