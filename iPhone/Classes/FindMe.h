//
//  FindMe.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/3/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import "MsgResponder.h"
#import "ExSystem.h" 
#import "MobileViewController.h"
#import "FindMeDelegate.h"

@interface FindMe : MsgResponder <CLLocationManagerDelegate>
{
	CLLocationManager		*locationManager;
	CLLocationCoordinate2D	coordinateBeingObtained;
	
	NSURLConnection			*conn;
	NSMutableData			*thisData;
	
	NSString				*city, *state, *zip, *country, *latitude, *longitude, *streetAddress, *woeid, *lastLoaded;
	NSString				*doneLoading;
	float					fLatitude, fLongitude;
	MobileViewController	*mvc;
    id<FindMeDelegate>      __weak _delegate;
    
    bool                    cityLookupIssued;

}

@property (strong, nonatomic) CLLocationManager *locationManager;
@property (assign, nonatomic) CLLocationCoordinate2D coordinateBeingObtained;

@property (strong, nonatomic) NSURLConnection *conn;
@property (strong, nonatomic) NSMutableData *thisData;

@property (strong, nonatomic) NSString *city;
@property (strong, nonatomic) NSString *state;
@property (strong, nonatomic) NSString *zip;
@property (strong, nonatomic) NSString *country;
@property (strong, nonatomic) NSString *latitude;
@property (strong, nonatomic) NSString *longitude;
@property (strong, nonatomic) NSString *streetAddress;
@property (strong, nonatomic) NSString *woeid;
@property (strong, nonatomic) NSString *doneLoading;
@property (strong, nonatomic) NSString *lastLoaded;
@property float fLatitude;
@property float fLongitude;
@property bool cityLookupIssued;

@property (strong, nonatomic) MobileViewController	*mvc;
@property (nonatomic, weak) id<FindMeDelegate>	delegate;

-(void) getLocation:(NSString *)longi latString:(NSString *)lati;
-(id) initWithDelegate:(id<FindMeDelegate>) del;

@end
