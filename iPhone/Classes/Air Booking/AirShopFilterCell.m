//
//  AirShopFilterCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/9/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AirShopFilterCell.h"

@implementation AirShopFilterCell
@synthesize lblCost, lblAirline, lblStarting, lblResultCount, ivLogo, ivPref;
@synthesize     lblDepartIata, lblDepartTime, lblArriveIata, lblArriveTime, lblDurationStops;
@synthesize     lblRoundDepartIata, lblRoundDepartTime, lblRoundArriveIata, lblRoundArriveTime, lblRoundDurationStops, ivOvernight, ivRoundOvernight, ivRule, lblRefundable, ivRefundable, lblGdsName;

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
