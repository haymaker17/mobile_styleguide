//
//  DetailApprovalCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DetailApprovalCell.h"


@implementation DetailApprovalCell
@synthesize lblLabel, lblValue, lblSep;

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
