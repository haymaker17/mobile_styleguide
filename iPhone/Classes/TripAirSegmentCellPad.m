//
//  TripAirSegmentCellPad.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TripAirSegmentCellPad.h"


@implementation TripAirSegmentCellPad

@synthesize						lblDepartAirport, lblDepartTime, lblDepartAMPM, lblDepartDate, lblDepartGateTerminal;
@synthesize						lblArriveAirport, lblArriveTime, lblArriveAMPM, lblArriveDate, lblArriveGateTerminal, lblVendor, ivVendorIcon;
@synthesize                     lblConfirmation, lblTravelTime, ivTripType;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if ((self = [super initWithStyle:style reuseIdentifier:reuseIdentifier])) {
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}




@end
