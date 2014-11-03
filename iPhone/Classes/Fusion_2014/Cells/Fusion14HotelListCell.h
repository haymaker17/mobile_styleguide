//
//  HotelListCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 3/13/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Fusion14HotelListCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UIImageView *hotelImage;
@property (weak, nonatomic) IBOutlet UIImageView *hotelRating;
@property (weak, nonatomic) IBOutlet UILabel *hotelName;
@property (weak, nonatomic) IBOutlet UILabel *hotelCityAndState;
@property (weak, nonatomic) IBOutlet UILabel *hotelDistance;
@property (weak, nonatomic) IBOutlet UILabel *hotelPrice;
@property (weak, nonatomic) IBOutlet UILabel *hotelPreferred;
@property (weak, nonatomic) IBOutlet UILabel *travelPoints;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coHotelPreferredWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coHotelPreferredHeight;

@end
