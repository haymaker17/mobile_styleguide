//
//  FlightScheduleCell.m
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/18/12.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "FlightScheduleCell.h"

@implementation FlightScheduleCell
@synthesize lblCost, lblAirline, lblStarting, lblResultCount, ivLogo, ivPref;
@synthesize     lblDepartIata, lblDepartTime, lblArriveIata, lblArriveTime, lblDurationStops;
@synthesize     lblRoundDepartIata, lblRoundDepartTime, lblRoundArriveIata, lblRoundArriveTime, lblRoundDurationStops, ivOvernight, ivRoundOvernight, ivRule, lblRefundable, lblSeats, lblSeatsText;

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
