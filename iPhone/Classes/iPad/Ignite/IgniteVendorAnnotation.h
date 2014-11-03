//
//  IgniteVendorAnnotation.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/27/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>

@interface IgniteVendorAnnotation : NSObject <MKAnnotation>
{
    NSString	*name, *latitude, *longitude, *address, *cityzip, *reviewSummary;
    NSString    *type; // SEG_TYPE_DINING, SEG_TYPE_HOTEL, SEG_TYPE_EVENT
}

@property (strong, nonatomic) NSString *name;
@property (strong, nonatomic) NSString *latitude;
@property (strong, nonatomic) NSString *longitude;
@property (strong, nonatomic) NSString *address;
@property (strong, nonatomic) NSString *cityzip;
@property (strong, nonatomic) NSString *reviewSummary;
@property (strong, nonatomic) NSString *type;

- (NSString*) addressWithCityZip;

@end
