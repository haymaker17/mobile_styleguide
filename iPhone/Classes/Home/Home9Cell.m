//
//  Home9Cell.m
//  ConcurMobile
//
//  Created by Pavan on 2/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "Home9Cell.h"

@implementation Home9Cell
@synthesize lblSubTitle, lblTitle, ivIcon,whiteback;


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
