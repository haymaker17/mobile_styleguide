//
//  HotelDetailsMapViewTableViewCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 9/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import "HotelAnnotation.h"
#import "HotelDetailsMapViewCellData.h"

@interface HotelDetailsMapViewTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet MKMapView *mapView;

- (void)setCellData:(HotelDetailsMapViewCellData *)cellData;

@end
