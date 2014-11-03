//
//  HotelDetailedMapViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import "BookingBaseTableViewController.h"
#import "HotelSummaryDelegate.h"
#import "EntityHotelBooking.h"

@class HotelResult;


@interface HotelDetailedMapViewController : BookingBaseTableViewController <UITableViewDelegate, UITableViewDataSource, HotelSummaryDelegate, MKMapViewDelegate>
{
	MKMapView		*mapView;
	EntityHotelBooking		*hotelResult;
	UIBarButtonItem	*bbiDone;
	UIToolbar		*tBar;
}

@property (nonatomic, strong) IBOutlet MKMapView			*mapView;
@property (nonatomic, strong) EntityHotelBooking					*hotelResult;
@property (nonatomic, strong) IBOutlet UIBarButtonItem		*bbiDone;
@property (nonatomic, strong) IBOutlet UIToolbar			*tBar;

-(IBAction) btnDone:(id)sender;

-(void)updateMap;

@end
