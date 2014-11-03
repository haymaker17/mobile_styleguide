//
//  AirShopMessageCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/21/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AirShopMessageCell.h"

@implementation AirShopMessageCell
@synthesize ivIcon, lblText;

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
