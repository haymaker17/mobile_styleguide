//
//  Fusion2014HotelDetailCell.h
//  ConcurMobile
//
//  Created by Shifan Wu on 4/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>

@interface Fusion2014HotelDetailCell : UITableViewCell

@property (strong, nonatomic) IBOutlet UIView *uvCardBackground;
@property (strong, nonatomic) IBOutlet UILabel *lblHotelName;
@property (strong, nonatomic) IBOutlet UILabel *lblPhoneNumber;
@property (strong, nonatomic) IBOutlet UIImageView *ivPhoneIcon;
@property (strong, nonatomic) IBOutlet UIImageView *ivBookingTypeHotel;
@property (strong, nonatomic) IBOutlet UIImageView *ivLocationIcon;
@property (strong, nonatomic) IBOutlet UILabel *lblHotelAddress;
@property (strong, nonatomic) IBOutlet MKMapView *mvHotelMap;
@property (strong, nonatomic) IBOutlet UILabel *lblConfirmationNumber;
@property (strong, nonatomic) IBOutlet UIButton *btnEdit;
@property (strong, nonatomic) IBOutlet UIButton *btnCancel;
@property (strong, nonatomic) IBOutlet UILabel *lblHintText;

@property (strong, nonatomic) IBOutlet UILabel *lblLine1;
@property (strong, nonatomic) IBOutlet UILabel *lblLine2;
@end
