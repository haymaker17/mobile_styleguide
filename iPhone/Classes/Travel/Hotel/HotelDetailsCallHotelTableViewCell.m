//
//  HotelDetailsCallHotelTableViewCell.m
//  ConcurMobile
//
//  Created by Sally Yan on 9/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelDetailsCallHotelTableViewCell.h"

@interface HotelDetailsCallHotelTableViewCell()
@property (nonatomic, strong) NSString *hotelPhoneNumber ;
@end

@implementation HotelDetailsCallHotelTableViewCell

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
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void)setCellData:(HotelPhoneCellData *)hotelDetailsCellData
{
    CTEHotel *cteHotel = [hotelDetailsCellData getCTEHotelData];
    self.hotelPhoneNumber = [@"tel://" stringByAppendingString:cteHotel.phoneNumber];
    
    UITapGestureRecognizer *lblTapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(callHotelPressed)];
    [self.lblCallHotel setUserInteractionEnabled:YES];
    [self.lblCallHotel addGestureRecognizer: lblTapGesture];
    UITapGestureRecognizer *imgTapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(callHotelPressed)];
    [self.imgCallHotel setUserInteractionEnabled:YES];
    [self.imgCallHotel addGestureRecognizer: imgTapGesture];

}
- (void)callHotelPressed {
    UIDevice *device = [UIDevice currentDevice];
    if ([[device model] isEqualToString:@"iPhone"] ) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:self.hotelPhoneNumber]];
    }}
@end
