//
//  AirLayoverCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/16/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AirLayoverCell.h"

@implementation AirLayoverCell
@synthesize lblLayover;

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
