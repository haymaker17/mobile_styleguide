//
//  HotelMapViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import "HotelCollectionViewController.h"
#import "EntityHotelBooking.h"
#import "EntityHotelCheapRoom.h"
#import "HotelBookingManager.h"
#import "FormatUtils.h"

@class HotelAnnotationView;


@interface HotelMapViewController : HotelCollectionViewController<UITableViewDelegate, UITableViewDataSource, MKMapViewDelegate>
{
	UITableView		*tblView;
	MKMapView		*mapView;
}

@property (nonatomic, strong) IBOutlet UITableView	*tblView;
@property (nonatomic, strong) IBOutlet MKMapView	*mapView;

-(void)updateMap;
-(void)annotationSelected:(HotelAnnotationView*)annotation;
-(void)showSelectedHotel;

-(void)configureCell:(HotelListCell*)cell hotelBooking:(EntityHotelBooking*)hotel;
@end
