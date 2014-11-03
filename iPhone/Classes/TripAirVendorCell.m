//
//  TripAirVendorCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TripAirVendorCell.h"


@implementation TripAirVendorCell
@synthesize			lblAirFlightNum, lblOperatedByAirFlightNum;
@synthesize			ivVendor, ivOperatedByVendor, ivBackground;

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
