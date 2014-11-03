//
//  EvaLocation.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "EvaLocation.h"
#import "DataConstants.h"

@interface EvaLocation ()

@property (nonatomic,strong) NSDictionary *location;

@end

@implementation EvaLocation



-(id)initWithDict:(NSDictionary *)dictionary
{
    self = [super init];
    if(self)
    {
        self.location = [[NSDictionary alloc] initWithDictionary:dictionary];
        [self parseJson];
    }
    return self;
    
}

-(void)parseJson
{

    if([self.location  objectForKey:@"Index"])
    {
        self.Index = [self.location  objectForKey:@"Index"];
    }
    if ([self.location  objectForKey:@"Next"]) {
        self.Next = [self.location  objectForKey:@"Next"];;
    }
    
    if([self.location objectForKey:@"All Airports Code"])
    {
        self.allAirportCode = [self.location objectForKey:@"All Airports Code"];
    }
    if([self.location objectForKey:@"Geoid"])
    {
        self.geoid = [self.location objectForKey:@"Geoid"];
    }
    if([self.location objectForKey:@"Name"])
    {
        // Name could be like
        //Name": "Portland, Oregon, United States (GID=5746545)" or 
        //"Name": "'PTJ' = Portland, AU",
        self.name = [self.location objectForKey:@"Name"];
        if([self.name lengthIgnoreWhitespace] && [self.name rangeOfString:@"GID" ].location != NSNotFound )
        {
            self.name = [self.name substringToIndex:[self.name rangeOfString:@"GID" ].location - 1];
        }
        else if([self.name lengthIgnoreWhitespace] && [self.name rangeOfString:@"=" ].location != NSNotFound )
        {
            self.name = [self.name substringFromIndex:[self.name rangeOfString:@"=" ].location + 1];
        }
    }
    
    if([self.location objectForKey:@"Country"])
    {
        self.country = [self.location objectForKey:@"Country"];
    }
    
    if([self.location objectForKey:@"Type"])
    {
        self.type = [self.location objectForKey:@"Type"];
    }
    if([self.location objectForKey:@"Longitude"])
    {
        self.longitude = [[self.location objectForKey:@"Longitude"] stringValue];
    }
    if([self.location objectForKey:@"Latitude"])
    {
        self.latitude = [[self.location objectForKey:@"Latitude"] stringValue];
    }
    if([self.location objectForKey:@"Arrival"])
    {
        self.arrivalDateTime = [[EvaTime alloc]initWithDict:[self.location objectForKey:@"Arrival"]];
    }
    if([self.location objectForKey:@"Departure"])
    {
        self.departureDateTime = [[EvaTime alloc]initWithDict:[self.location objectForKey:@"Departure"]];
    }
    if([self.location objectForKey:@"Pickup Car"])
    {
        self.carPickupDateTime = [[EvaTime alloc]initWithDict:[self.location objectForKey:@"Pickup Car"]];
    }

    if([self.location objectForKey:@"Return Car"])
    {
        self.carReturnDateTime = [[EvaTime alloc]initWithDict:[self.location objectForKey:@"Return Car"]];
    }
    if([self.location objectForKey:@"Stay"])
    {
        // Not required as getStayDuraction takes care of this for hotel
        // TODO: check if its required for Air/Carx
       
    }
    if([self.location objectForKey:@"Actions"])
    {
        if([[self.location objectForKey:@"Actions"] isKindOfClass:[NSArray class]])
            self.actions = [[NSMutableArray alloc]initWithArray:[self.location objectForKey:@"Actions"]];
    }

    if([self.location objectForKey:@"Airports"])
    {
        if([[self.location objectForKey:@"Airports"] isKindOfClass:[NSArray class]])
            self.airports = [[NSMutableArray alloc]initWithArray:[self.location objectForKey:@"Airports"]];
        else    // its a string so split the string
            self.airports = [[[self.location objectForKey:@"Airports"]  componentsSeparatedByString:@","] mutableCopy];
    }
    
    if([self.location objectForKey:@"Request Attributes"])
    {
        self.requestAttributes = [[NSMutableDictionary alloc] initWithDictionary:[self.location objectForKey:@"Request Attributes"]];
    }

    if([self.location objectForKey:@"Geo Attributes"])
    {
        NSDictionary *geoattributes = [self.location objectForKey:@"Geo Attributes"];
        NSDictionary *distance = [geoattributes objectForKey:@"Distance"];
        if(distance != nil)
        {
            self.distanceUnit = [distance objectForKey:@"Units"];
            
            NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
            [formatter setNumberStyle:NSNumberFormatterDecimalStyle];
            
            if ([[distance objectForKey:@"Quantity"] isKindOfClass:[NSString class]]) {
                self.distanceValue = [formatter numberFromString:[distance objectForKey:@"Quantity"]];
            }
            else
                self.distanceValue = [distance objectForKey:@"Quantity"];
 
        }
    }
    if([self.location objectForKey:@"Rental Car Duration"])
    {
        NSString *delta = [[self.location objectForKey:@"Rental Car Duration"] objectForKey:@"Delta"];
        if([delta lengthIgnoreWhitespace])
        {
            NSUInteger index = [delta rangeOfString:@"+"].location;
            if(index > 0)
                self.rentalCarDuration = [[delta substringToIndex:index +1] integerValue];
        }
    }
}

-(EvaSearchCategory)getTransportType
{
    if (self.requestAttributes != nil && [self.requestAttributes count] > 0)
    {
        // Get the request attributes and check the transport type.
        // TODO :  add data type check for transport type
        NSArray *transportType = [self.requestAttributes objectForKey:@"TransportType"];
        if(transportType != nil && [transportType count] > 1)
        {
            if ([transportType containsObject:@"Train"]) {
                return EVA_TRAINS;
            }
            if([transportType containsObject:@"Airplane"]) {
                return EVA_FLIGHTS;
            }
            if([transportType containsObject:@"Car"]) {
                return EVA_CARS;
            }
            
        }
    }
    
    return EVA_UNKNOWN;
}

-(EvaSearchCategory)getActionsforType
{
    if([self.actions containsObject:@"Get there"]){
        return EVA_FLIGHTS;
    }
    
    if( [self.actions containsObject:@"Get Accommodation"]){
        return EVA_HOTELS;
    }

    if([self.actions containsObject:@"Pickup Car"] || [self.actions containsObject:@"Return Car"]){
        return EVA_CARS;
    }

    return EVA_UNKNOWN;
    
}

// Builds a locaiton result object from the location setting
// TODO : add some checks to see if latitude and longitude are not nil etc.
-(LocationResult *)getLocationResult
{
    LocationResult *locationResult = [[LocationResult alloc]init];
    
    locationResult.latitude = self.latitude;
    locationResult.longitude = self.longitude;
    
//    if(self.airports != nil && [self.airports count] >0)
//    {
//        locationResult.location = self.airports[0];
//    }
//    else
    if([self.name lengthIgnoreWhitespace])
    {
        locationResult.location = self.name;
    }

    locationResult.countryAbbrev = self.country;
    return locationResult;
}

/**
 Returns aiportcode of the given location
 */
-(NSString *)getAirportCode
{
    NSString *ariportCode = nil;
    
    if(self.airports != nil && [self.airports count] >0)
    {
        ariportCode = self.airports[0];
    }
    else if([self.allAirportCode lengthIgnoreWhitespace])
    {
        ariportCode = self.allAirportCode;
    }
    
    return ariportCode;
}


-(NSInteger)getStayDuration
{
    NSInteger stay = 1 ;
    NSString *delta = [self.location valueForKeyPath:@"Stay.Delta"];
    NSUInteger index = [delta rangeOfString:@"+"].location;
    if(index > 0)
        delta = [delta substringFromIndex:index + 1];
    
    if([delta lengthIgnoreWhitespace])
        stay = [delta integerValue];
    return stay;
}


// Generic method to check any key in the json return 
-(id)containsKey:(NSString *)key
{
    NSObject *value = nil;
    
    if(self.location[key])
    {
        value = self.location[key];
    }
        
    return value;
}
/*

 
 public boolean isFlightSearch() {
 if (locations != null && locations.length > 1) {
 if (locations[0].requestAttributes != null) {
 if(locations[0].requestAttributes.transportType.contains("Airplane")){
 return true; // It is an Air search!
 }
 if(locations[0].requestAttributes.transportType.contains("Train")
 || locations[0].requestAttributes.transportType.contains("Car")) {
 return false;
 }
 }
 if (locations[1].actions == null) {
 return true;
 } else {
 List<String> actions = Arrays.asList(locations[1].actions);
 if (actions.contains("Get There"))
 return true;
 }
 }
 return false;
 }
 
 
 public boolean isTrainSearch() {
 if (locations != null && locations.length > 1) {
 if ((locations[0].requestAttributes != null)
 && (locations[0].requestAttributes.transportType.contains("Train"))) {
 return true; // It is a train search!
 }
 }
 return false;
 }
 
 
 public boolean isCarSearch() {
 
 if (locations != null && locations.length > 0) {
 
 EvaLocation loc = locations[0];
 
 if (loc.requestAttributes != null
 && loc.requestAttributes.transportType.contains("Car")) {
 return true; // It is a car search!
 }
 
 if(loc.actions != null) {
 for(String action : loc.actions) {
 if(action.equalsIgnoreCase("Pickup Car")
 || action.equalsIgnoreCase("Return Car")) {
 return true;
 }
 }
 }
 }
 
 return false;
 }
 */

@end
