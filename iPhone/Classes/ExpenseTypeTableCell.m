//
//  ExpenseTypeTableCell.m
//  ConcurMobile
//
//  Created by ernest cho on 2/19/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ExpenseTypeTableCell.h"

@implementation ExpenseTypeTableCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        self = [[NSBundle mainBundle] loadNibNamed:@"ExpenseTypeTableCell" owner:nil options:nil][0];
        self.clipsToBounds = NO;
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];
}

@end
