//
//  TripSegmentCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/23/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "TripSegmentCell.h"


@implementation TripSegmentCell
@synthesize lblAmPm, lblSub1, lblSub2, lblTime, lblHeading, ivIcon;

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
