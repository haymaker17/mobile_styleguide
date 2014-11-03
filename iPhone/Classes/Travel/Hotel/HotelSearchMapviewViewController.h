//
//  HotelSearchMapviewViewController.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/16/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>

@interface HotelSearchMapviewViewController : UIViewController <MKMapViewDelegate, UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, strong) IBOutlet MKMapView *mapView;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (nonatomic, strong) NSArray *hotelList;
@property BOOL isSingleMapView;

@end