//
//  HotelDetailsCallHotelTableViewCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 9/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HotelPhoneCellData.h"

@interface HotelDetailsCallHotelTableViewCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *lblCallHotel;
@property (weak, nonatomic) IBOutlet UIImageView *imgCallHotel;
-(void)setCellData:(HotelPhoneCellData *)hotelDetailsCellData;

@end
