//
//  IgniteVendorAnnotation.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/27/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteVendorAnnotation.h"


@implementation IgniteVendorAnnotation;
@synthesize latitude, longitude, name, address, cityzip, reviewSummary, type;


- (CLLocationCoordinate2D)coordinate {
    CLLocationCoordinate2D theCoordinate;
    theCoordinate.latitude = [self.latitude floatValue];
    theCoordinate.longitude = [self.longitude floatValue];
    return theCoordinate;
}

// Optional
- (NSString *)title {
    return name;
}

// Optional
- (NSString *)subtitle {
    if ([self.reviewSummary length])
        return self.reviewSummary;
    else 
        return [self addressWithCityZip];
}

- (NSString *)addressWithCityZip
{
    return [NSString stringWithFormat:@"%@, %@", self.address, self.cityzip];
}

@end
