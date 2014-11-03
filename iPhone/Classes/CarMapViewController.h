//
//  CarMapViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import "BookingBaseTableViewController.h"

@class Car;

@interface CarMapViewController : BookingBaseViewController <MKMapViewDelegate>
{
	MKMapView		*mapView;
	UIBarButtonItem	*screenTitleBarButtonItem;
	Car				*car;
    UIToolbar       *toolBar;
}

@property (nonatomic, strong) IBOutlet UIToolbar       *toolBar;
@property (nonatomic, strong) IBOutlet MKMapView		*mapView;
@property (nonatomic, strong) IBOutlet UIBarButtonItem *screenTitleBarButtonItem;
@property (nonatomic, strong) Car						*car;

-(void)updateMap;

-(IBAction) btnDone:(id)sender;

@end
