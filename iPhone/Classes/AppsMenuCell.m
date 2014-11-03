//
//  AppsMenuCell.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "AppsMenuCell.h"

@implementation AppsMenuCell
@synthesize ivLogo, lblAppName;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        [self setBackgroundColor:[ExSystem getBaseBackgroundColor]];
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
