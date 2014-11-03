//
//  FindMe.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/3/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "FindMe.h"
#import "Weather.h"
#import "Location.h"
#import "WOEID.h"
#import "Msg.h"
#import "MsgControl.h"

@implementation FindMe

@synthesize locationManager;
@synthesize coordinateBeingObtained;

@synthesize conn;
@synthesize thisData;

@synthesize city;
@synthesize state;
@synthesize zip;
@synthesize country;
@synthesize latitude;
@synthesize longitude;
@synthesize streetAddress;
@synthesize woeid;
@synthesize doneLoading;

@synthesize lastLoaded;

@synthesize fLongitude;
@synthesize fLatitude, mvc;
@synthesize delegate = _delegate;

@synthesize cityLookupIssued;


static NSDate* lastAccessTime = nil;
static BOOL lookupQueued = FALSE;

+ (NSDate*) getLastAccessTime
{
	return lastAccessTime;
}

// Initialize msgId to msg class mapping here
+ (void)initialize
{
	if (self == [FindMe class]) 
	{
        // Perform initialization here.
		lastAccessTime = nil;
    }
}

+(void) updatelastAccess
{
    lastAccessTime = [NSDate date];
}

+(BOOL) lastAccessEarlierThanSeconds:(NSTimeInterval) seconds
{
    if (lastAccessTime == nil)
        return TRUE;
    
    else 
    {
        NSDate* thresholdTime = [NSDate dateWithTimeIntervalSinceNow:-seconds];
        return [lastAccessTime compare:thresholdTime] == NSOrderedAscending;
    }
}


-(void) startLocationMgr
{
    self.cityLookupIssued = FALSE;
    lookupQueued = FALSE;

    doneLoading = @"NO";
    self.locationManager = [[CLLocationManager alloc] init];
    
    locationManager.delegate = self;
    locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    [locationManager startUpdatingLocation];    
}

-(id) init
{
    self = [super init];
    if (self)
    {
        [self startLocationMgr];
    }
    
    return self;
}

-(id) initWithDelegate:(id<FindMeDelegate>) del
{
    self = [super init];
    if (self)
    {
        self.delegate = del;
        [self startLocationMgr];
    }
    
    return self;
}


-(void) lookupLocationFromCoordinates
{
    if (cityLookupIssued)
        return;
    cityLookupIssued = TRUE;
    [locationManager stopUpdatingLocation];

    [self getLocation:[NSString stringWithFormat:@"%.7f", self.coordinateBeingObtained.longitude]  latString:[NSString stringWithFormat:@"%.7f", coordinateBeingObtained.latitude]];
    
}

-(void) getLocation:(NSString *)longi latString:(NSString *)lati
{
	NSString *address2 = [NSString stringWithFormat:@"%@,%@", lati, longi];
	
    // setup maps api key
	//Google maps API key = ABQIAAAAnTacZnb1w1Xgs7z06z0J2RQjFYan1vKo2hwr5-KQNzJxfkE0rxRZ_e3x1J3--_E5nYtM_moGRQ_2Fw
    //NSString * MAPS_API_KEY = @"ABQIAAAAnTacZnb1w1Xgs7z06z0J2RQjFYan1vKo2hwr5-KQNzJxfkE0rxRZ_e3x1J3--_E5nYtM_moGRQ_2Fw";
	
    NSString *escaped_address =  [address2 stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
	
    // Contact Google and make a geocoding request
	// See Google API docs at: http://googlegeodevelopers.blogspot.com/2010/03/introducing-new-google-geocoding-web.html
	// See http://code.google.com/apis/maps/documentation/geocoding/ for documentation about "results influenced by the region"
    NSString *requestString = [NSString stringWithFormat:@"http://maps.google.com/maps/api/geocode/xml?latlng=%@&sensor=false", escaped_address];
	
	//OLD API:
    //NSString *requestString = [NSString stringWithFormat:@"http://maps.google.com/maps/geo?q=%@&output=xml&oe=utf8&key=%@&sensor=false&gl=it", escaped_address, MAPS_API_KEY];
	
	self.thisData = [[NSMutableData alloc] initWithCapacity:(13 * 1024)];
	NSURL *requestUrl = [NSURL URLWithString:requestString];
	NSURLRequest *request = [NSURLRequest requestWithURL:requestUrl];
	self.conn = [NSURLConnection connectionWithRequest:request delegate:self];
    
//    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Query location API %@", address2] Level:MC_LOG_DEBU];

}

-(void) respondToXML:(NSString *)xml
{
	////NSLog(@"respondToXML, theXML=%@ end of respondToXML", xml);
}

-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	if (lastLoaded == nil)
	{//get location
		Location  *loc = [Location alloc];
		[loc parseXMLFileAtData:data]; 	
		self.lastLoaded = @"Location";
		self.city = loc.city;
		self.state = loc.state;
		self.zip = loc.zip;
		self.country = loc.country;
		self.streetAddress = loc.streetAddress;
		
		//NSString * YAHOO_APP_KEY = @"4aqFmpHV34Es4iYT6aojobpTRa9UDtwUCEQmubFC156XQQhLnJrauKzE7wKJcIg-";
		NSString *woeidValues = [NSString stringWithFormat:@"('%@ %@ %@')", city, state, country];
		NSString *escapedWoeidValues =  [woeidValues stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
		NSString *pathWoeid = [NSString stringWithFormat:@"https://where.yahooapis.com/v1/places.q%@?appid=4aqFmpHV34Es4iYT6aojobpTRa9UDtwUCEQmubFC156XQQhLnJrauKzE7wKJcIg-"
							   ,escapedWoeidValues];
		Msg *myMsg = [[Msg alloc] initWithData:@"WOEID" State:@"" Position:nil MessageData:nil URI:pathWoeid MessageResponder:self ParameterBag:nil];
		//[myMsg init:@"WOEID" State:@"" Position:nil MessageData:nil URI:pathWoeid MessageResponder:self ParameterBag:nil];
		[ExSystem addMsg:myMsg];
	}
	else if ([lastLoaded isEqualToString:@"Location"])
	{//get woeid
		WOEID *wOEID = [WOEID alloc];
		[wOEID parseXMLFileAtData:data];
		woeid = wOEID.woeid;
		lastLoaded = @"woeid";
	}
}


-(void)dealloc
{
	if (locationManager != nil)
		locationManager.delegate = nil;

	
	
}

#pragma mark  NSURLConnection delegate Methods
-(void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)moreData
{
	[thisData appendData:moreData];
}

-(void)connectionDidFinishLoading:(NSURLConnection *)connection
{
	UIApplication* app = [UIApplication sharedApplication]; 
	app.networkActivityIndicatorVisible = NO;

	fLongitude = coordinateBeingObtained.longitude;
	fLatitude = coordinateBeingObtained.latitude;
	self.latitude = [NSString stringWithFormat:@"%.7f", coordinateBeingObtained.latitude];
	self.longitude = [NSString stringWithFormat:@"%.7f", coordinateBeingObtained.longitude];
	
	Location  *loc = [[Location alloc] init];
	
	[loc parseXMLFileAtData:thisData];
    
	self.city = [loc.city copy];
	self.state = [loc.stateAbbrev copy];
	self.zip = [loc.zip copy];
	self.country = [loc.countryAbbrev copy];
	self.streetAddress = [loc.streetAddress copy];
	
	//	//get the weather based on this location
	//	//yahoo app id: 4aqFmpHV34Es4iYT6aojobpTRa9UDtwUCEQmubFC156XQQhLnJrauKzE7wKJcIg-
	//	NSString * YAHOO_APP_KEY = @"4aqFmpHV34Es4iYT6aojobpTRa9UDtwUCEQmubFC156XQQhLnJrauKzE7wKJcIg-";
	//	//http://where.yahooapis.com/v1/places.q%28%27Boston%20MA%20USA%27%29?appid=4aqFmpHV34Es4iYT6aojobpTRa9UDtwUCEQmubFC156XQQhLnJrauKzE7wKJcIg-
	//	NSString *woeidValues = [NSString stringWithFormat:@"('%@ %@ %@')", loc.localityName, loc.adminAreaName, loc.countryName];
	//	NSString *escapedWoeidValues =  [woeidValues stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
	//	NSString *pathWoeid = [NSString stringWithFormat:@"http://where.yahooapis.com/v1/places.q%@?appid=4aqFmpHV34Es4iYT6aojobpTRa9UDtwUCEQmubFC156XQQhLnJrauKzE7wKJcIg-"
	//						   ,escapedWoeidValues];
	//	WOEID *wOEID = [WOEID alloc];
	//	[wOEID parseXMLFileAtURL:pathWoeid];
	//	woeid = wOEID.woeid;
	//	
	////	NSString * path = [NSString stringWithFormat:@"http://weather.yahooapis.com/forecastrss?w=%@&u=f", woeid.woeid];
	////	Weather *weather = [Weather alloc];
	////	[weather parseXMLFileAtURL:path];
	////	labelTemp.text = weather.temperature;
	////	imgTemp.image = weather.imgWeather;
	//	
	//	[wOEID release];
	////	[weather release];
	//	[loc release];
	
	coordinateBeingObtained.latitude = 0;
	coordinateBeingObtained.longitude = 0;
	self.thisData = nil;
	
	app.networkActivityIndicatorVisible = NO;	
	
	doneLoading = @"YES";	
	
    if (self.delegate != nil)
        [self.delegate locationFound:self];

// Disabled findMe code through this project. We need to refactor it before we use it    
//	if (mvc != nil)
//		[mvc findMeCallBack:self];
}

-(void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
	coordinateBeingObtained.latitude = 0;
	coordinateBeingObtained.longitude = 0;
	self.thisData = nil;

	UIApplication* app = [UIApplication sharedApplication]; 
	app.networkActivityIndicatorVisible = NO;
}

#pragma mark -
#pragma mark CLLocationManagerDelegate Methods
- (void)locationManager:(CLLocationManager *)manager 
    didUpdateToLocation:(CLLocation *)newLocation 
           fromLocation:(CLLocation *)oldLocation 
{
    NSString* newLocStr = [NSString stringWithFormat:@"%.7f,%.7f", newLocation.coordinate.longitude, newLocation.coordinate.latitude];
    NSString* oldLocStr = [NSString stringWithFormat:@"%.7f,%.7f", oldLocation.coordinate.longitude, oldLocation.coordinate.latitude];
    
    if (self.cityLookupIssued)
    {
//        [[MCLogging getInstance] log:[NSString stringWithFormat:@"Refused Location %@ accu %f vs old %@ ", newLocStr, newLocation.horizontalAccuracy, oldLocStr] Level:MC_LOG_DEBU];
        return;
    }
    
	UIApplication* app = [UIApplication sharedApplication]; 
	app.networkActivityIndicatorVisible = YES; // to stop it, set this to NO 
	
	self.coordinateBeingObtained = newLocation.coordinate;
	
//    [[MCLogging getInstance] log:[NSString stringWithFormat:@"UpdateLocation %@ accu %f vs old %@", newLocStr, newLocation.horizontalAccuracy, oldLocStr] Level:MC_LOG_DEBU];
    
    if (![newLocStr isEqualToString:oldLocStr] && newLocation.horizontalAccuracy >= 0.0 && newLocation.horizontalAccuracy <= 500.0 && ! lookupQueued)
    {
        //[self getLocation:@"14.4204598" latString:@"50.0878114"];	// Prague, Czech Republic
        //[self getLocation:@"-123.1139268" latString:@"49.261226"];	// Vancouver, BC
        //[self getLocation:@"2.3509871" latString:@"48.8566667"];	// Paris, France
        // MOB-7214
        lookupQueued = TRUE;
        if ([FindMe lastAccessEarlierThanSeconds:36000]) // last access 10 hours ago 
        {
            [self performSelector:@selector(lookupLocationFromCoordinates) withObject:nil afterDelay:10.0f];
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"Wait 10 seconds"] Level:MC_LOG_DEBU];
        }
        else 
        {
            [self performSelector:@selector(lookupLocationFromCoordinates) withObject:nil afterDelay:2.0f];
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"Wait 2 seconds"] Level:MC_LOG_DEBU];
        }
        
        [FindMe updatelastAccess];
        //        [self getLocation:[NSString stringWithFormat:@"%.7f", newLocation.coordinate.longitude]  latString:[NSString stringWithFormat:@"%.7f", newLocation.coordinate.latitude]];
    }
}

- (void)locationManager:(CLLocationManager *)manager 
       didFailWithError:(NSError *)error {
    
    NSString *errorType = (error.code == kCLErrorDenied) ? 
    @"Access Denied" : @"Unknown Error";
    if (self.delegate != nil)
        [self.delegate locationNotFound:errorType];
    
//    UIAlertView *alert = [[UIAlertView alloc] 
//                          initWithTitle:@"Error getting Location" 
//                          message:errorType 
//                          delegate:nil 
//                          cancelButtonTitle:@"Okay" 
//                          otherButtonTitles:nil];

//    [alert show];
//    [alert release];
}

@end
