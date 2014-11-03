//
//  FindAddress.m
//  ConcurMobile
//
//  Created by Paul Kramer on 2/4/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FindAddress.h"
#import "Location.h"
#import "Msg.h"

@implementation FindAddress


@synthesize latitude;
@synthesize longitude;
@synthesize address;
@synthesize mapView;
@synthesize rootVC;


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	Location  *loc = [[Location alloc] init];
	[loc parseXMLFileAtData:data]; 	
	self.longitude = loc.viewport.northeast.longitude;
	self.latitude = loc.viewport.northeast.latitude;
	
    [mapView setLati:[NSNumber numberWithFloat:[self.latitude floatValue]]];
    [mapView setLongi:[NSNumber numberWithFloat:[self.longitude floatValue]]];
    
	//[mapView updateMap]; //method no longer exists
}

-(void) getLocationByAddress:(NSString *)address
{
	// setup maps api key
	//Google maps API key = ABQIAAAAnTacZnb1w1Xgs7z06z0J2RQjFYan1vKo2hwr5-KQNzJxfkE0rxRZ_e3x1J3--_E5nYtM_moGRQ_2Fw
   // NSString * MAPS_API_KEY = @"ABQIAAAAnTacZnb1w1Xgs7z06z0J2RQjFYan1vKo2hwr5-KQNzJxfkE0rxRZ_e3x1J3--_E5nYtM_moGRQ_2Fw";
	
    //NSString *escaped_address =  [address stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
	
    // Contact Google and make a geocoding request
    //NSString *requestString = [NSString stringWithFormat:@"http://maps.google.com/maps/geo?q=%@&output=xml&oe=utf8&key=%@&sensor=false&gl=it", escaped_address, MAPS_API_KEY];


//	Msg *myMsg = [[Msg alloc] initWithData:@"LOCATION" State:@"" Position:nil MessageData:nil URI:requestString MessageResponder:self ParameterBag:nil];
//	
//	[ExSystem addMsg:myMsg];
//	[myMsg release];
}



@end
