//
//  HomeCell.m
//  ConcurMobile
//
//  Created by Pavan on 2/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "HomeCell.h"

@implementation HomeCell
@synthesize lblSubTitle, lblTitle, ivIcon;


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
