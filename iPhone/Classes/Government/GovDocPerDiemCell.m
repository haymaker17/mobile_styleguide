//
//  GovDocPerDiemCell.m
//  ConcurMobile
//
//  Created by Shifan Wu on 1/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovDocPerDiemCell.h"

@implementation GovDocPerDiemCell

@synthesize location, tripDate, rate;

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
