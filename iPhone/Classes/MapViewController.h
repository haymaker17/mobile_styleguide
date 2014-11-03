//
//  MapViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/20/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import "MobileViewController.h"
#import "MyAnnotation.h"

@class RootViewController;
@class Location;

@interface MapViewController : MobileViewController <MKMapViewDelegate>
{
    UIControl           *backCover;
}

@property (nonatomic, strong) IBOutlet MKMapView *mapView;
@property (nonatomic, strong) NSNumber *lati;
@property (nonatomic, strong) NSNumber *longi;
@property (nonatomic, strong) NSString *anoTitle;
@property (nonatomic, strong) NSString *anoSubTitle;
@property (nonatomic, strong) NSString *mapAddress;
@property (nonatomic, strong) NSString *strLati;
@property (nonatomic, strong) NSString *strLongi;
@property (nonatomic, strong) IBOutlet UILabel *lblAddress;
@property (nonatomic, strong) MyAnnotation		*annotationToSelect;

//-(void)updateMap;
-(IBAction)closeMe:(id)sender;
-(IBAction)buttonRoutePressed:(id)sender;

+(Location *)requestLocationByAddress:(NSString *)address;


// Disabled findMe code through this project. We need to refactor it before we use it
//-(void)doFindMe;

#pragma mark - Address Stuff
- (void)openAnnotation:(id)annotation;
@end
