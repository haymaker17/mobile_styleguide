//
//  HotelDetailsTableViewCell.m
//  ConcurMobile
//
//  Created by Sally Yan on 9/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelDetailsTableViewCell.h"

@implementation HotelDetailsTableViewCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)awakeFromNib
{
    // Initialization code
//    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(mapViewSelected)];
//    
//    [self.mapView addGestureRecognizer:tapGesture];
    
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void)setCellData:(HotelDetailsCellData *)hotelDetailsCellData
{
    
    CTEHotel *cteHotel = [hotelDetailsCellData getCTEHotelData];
    self.labelHotelName.text = cteHotel.propertyName;
    self.labelHotelAddress.text = [NSString stringWithFormat:@"%@ %@ %@ %@", cteHotel.street, cteHotel.city, cteHotel.state, cteHotel.zipCode];
    self.labelHotelPhoneNumber.text = cteHotel.phoneNumber;
}


-(void)mapViewSelected
{
    if (self.mapViewTapped) {
        self.mapViewTapped();
    }
}


@end
