//
//  HotelDetailsTableViewCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 9/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import "FadeTruncatingLabel.h"
#import "HotelDetailsCellData.h"

@interface HotelDetailsTableViewCell : UITableViewCell<MKMapViewDelegate>

@property (weak, nonatomic) IBOutlet FadeTruncatingLabel *labelHotelName;
@property (weak, nonatomic) IBOutlet UILabel *labelHotelAddress;
@property (weak, nonatomic) IBOutlet UILabel *labelHotelPhoneNumber;
@property (copy, nonatomic) void (^mapViewTapped)();

-(void)setCellData:(HotelDetailsCellData *)hotelDetailsCellData;

@end
