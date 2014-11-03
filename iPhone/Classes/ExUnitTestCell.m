//
//  ExUnitTestCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ExUnitTestCell.h"


@implementation ExUnitTestCell
@synthesize lbl, iv;

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
