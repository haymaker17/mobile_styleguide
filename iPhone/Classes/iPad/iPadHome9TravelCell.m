//
//  iPadHome9TravelCell.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/20/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "iPadHome9TravelCell.h"

@implementation iPadHome9TravelCell

@synthesize lblSubTitle, lblTitle, ivIcon,whiteback;
@synthesize ivIndicator;


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
