//
//  BasicMapViewController.m
//  ConcurMobile
//
//  Created by yiwen on 8/8/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "BasicMapViewController.h"

@interface BasicMapViewController (Private)
-(void)updateMap;
@end

@implementation BasicMapViewController
@synthesize mapView, currentLoc;


- (void)setSeedData:(LocationAnnotation*)annotation
{
    self.currentLoc = annotation;
}

- (void)dealloc
{
    mapView.delegate = nil; // MOB-7280
}

#pragma mark -
#pragma mark View lifecycle

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	[self updateMap];
}

#pragma mark -
#pragma mark Map methods
-(void)showLocation
{
    for (id<MKAnnotation> ann in mapView.annotations)
	{
        [mapView selectAnnotation:ann animated:FALSE];
	}

}
-(void)updateMap
{
	// Remove all existing annotations
	[mapView removeAnnotations:[mapView annotations]];
    
	double boxWidth = 0.02;
	double boxHeight = 0.02;
	
	// The center location is the location around which the map will be centered.
	CLLocationCoordinate2D centerLocation;
	centerLocation.latitude = [currentLoc.latitude doubleValue];
	centerLocation.longitude = [currentLoc.longitude doubleValue];
	
	// The span is as big as it needs to be to show all the hotel
	MKCoordinateSpan span;
	span.latitudeDelta = boxWidth;
	span.longitudeDelta = boxHeight;
	
	// Set the map region
	MKCoordinateRegion region;
	region.center = centerLocation;
	region.span = span;
	
	[mapView setRegion:region animated:YES];
	[mapView regionThatFits:region];
	
	// Add the annotations (push pins)
	[mapView addAnnotation:self.currentLoc];
    // Show the selected hotel after a very brief delay.  Note: attempting to do it
	// immediately will fail because the annotation views are not available yet.
	//[self performSelector:@selector(showLocation) withObject:nil afterDelay:0.5f];
    [mapView selectAnnotation:self.currentLoc animated:FALSE];

}

#pragma mark -
#pragma mark MKMapViewDelegate methods


-(MKAnnotationView *)mapView:(MKMapView *)thismapView viewForAnnotation:(id <MKAnnotation>)annotation
{
    __autoreleasing MKPinAnnotationView *annView=[[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"currentloc"];
    annView.image = [UIImage imageNamed:@"map_location"];
    //annView.animatesDrop=TRUE;
    annView.canShowCallout = YES;
    annView.calloutOffset = CGPointMake(1, 2);
    annView.leftCalloutAccessoryView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"map_man"]];
    return annView;
}

@end
