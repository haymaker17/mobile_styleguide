//
//  AirShopFilteredCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/10/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AirShopFilteredCell.h"

@implementation AirShopFilteredCell

@synthesize lblCost, lblAirline, lblStarting, lblResultCount, ivLogo, ivPref;
@synthesize     lblDepartIata, lblDepartTime, lblArriveIata, lblArriveTime, lblDurationStops, ivOvernight;
@synthesize     lblRoundDepartIata, lblRoundDepartTime, lblRoundArriveIata, lblRoundArriveTime, lblRoundDurationStops, viewDetails, lblOperatedBy;

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
