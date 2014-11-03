//
//  SegmentSelectCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/18/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "SegmentSelectCell.h"

@implementation SegmentSelectCell
@synthesize lblAmount, lblHeading, lblSubHeading, ivIcon, ivCheck, lblSubHeading2;

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
