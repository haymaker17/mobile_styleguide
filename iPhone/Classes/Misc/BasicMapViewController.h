//
//  BasicMapViewController.h
//  ConcurMobile
//
//  Created by yiwen on 8/8/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>
#import "MobileViewController.h"
#import "LocationAnnotation.h"

@interface BasicMapViewController : MobileViewController <MKMapViewDelegate>
{
    MKMapView               *mapView;
    LocationAnnotation      *currentLoc;

}

@property (nonatomic, strong) IBOutlet MKMapView	*mapView;
@property (nonatomic, strong) LocationAnnotation    *currentLoc;

- (void)setSeedData:(LocationAnnotation*)annotation;

@end
