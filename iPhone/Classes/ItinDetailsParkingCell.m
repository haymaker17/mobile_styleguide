//
//  ItinDetailsParkingCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ItinDetailsParkingCell.h"


@implementation ItinDetailsParkingCell

@synthesize			lblVendor, lblAddress1, lblAddress2, lblPhone;
@synthesize			ivVendor, ivMap, ivPhone;

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
