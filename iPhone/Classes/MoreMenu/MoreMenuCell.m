//
//  MoreMenuCell.m
//  ConcurMobile
//
//  Created by ernest cho on 3/11/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MoreMenuCell.h"

@implementation MoreMenuCell
@synthesize label, icon;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        self = [[NSBundle mainBundle] loadNibNamed:@"MoreMenuCell" owner:nil options:nil][0];
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
