//
//  ListFieldTableCell.m
//  ConcurMobile
//
//  Created by ernest cho on 10/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ListFieldTableCell.h"

@implementation ListFieldTableCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        self = [[NSBundle mainBundle] loadNibNamed:@"ListFieldTableCell" owner:nil options:nil][0];
        self.clipsToBounds = NO;
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];
}

@end
