//
//  TripApprovalCell.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 02/05/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TripApprovalCell.h"

@implementation TripApprovalCell

@synthesize lblAmount, lblBottom, lblDate, lblName;

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
