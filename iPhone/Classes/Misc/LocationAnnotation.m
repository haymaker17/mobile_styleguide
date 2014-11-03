//
//  LocationAnnotation.m
//  ConcurMobile
//
//  Created by yiwen on 8/9/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "LocationAnnotation.h"


@implementation LocationAnnotation
@synthesize city;
@synthesize state;
@synthesize zip;
@synthesize country;
@synthesize latitude;
@synthesize longitude;
@synthesize streetAddress;


- (CLLocationCoordinate2D)coordinate {
    CLLocationCoordinate2D theCoordinate;
    theCoordinate.latitude = [self.latitude floatValue];
    theCoordinate.longitude = [self.longitude floatValue];
    return theCoordinate;
}

// Optional
- (NSString *)title {
    return @"Current Location";
}

// Optional
- (NSString *)subtitle {
    NSString *locName = @"";
    NSString *ctry = @"";
    if(country != nil)
        ctry = country;

    if(streetAddress != nil)
        locName = [NSString stringWithFormat:@"%@, ", streetAddress];
    
    if(city != nil)
        locName = [NSString stringWithFormat:@"%@%@, %@", locName, city, (state != nil && [state length] > 0 ? state : ctry)];
    else
        locName = [NSString stringWithFormat:@"%@%@", locName, (state != nil && [state length] > 0 ? state : ctry)];
        
    return locName;
}


@end
