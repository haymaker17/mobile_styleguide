//
//  ItinPassbookCell.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 10/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "ItinPassbookCell.h"

@implementation ItinPassbookCell
@synthesize btnAddToPassbook;

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
