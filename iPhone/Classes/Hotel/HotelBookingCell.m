//
//  HotelBookingCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/5/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "HotelBookingCell.h"


@implementation HotelBookingCell
@synthesize lblLabel, lblValue;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


@end
