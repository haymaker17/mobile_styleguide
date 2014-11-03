//
//  ChatterFeedTableCell.m
//  ConcurMobile
//
//  Created by ernest cho on 6/13/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChatterFeedTableCell.h"

@interface ChatterFeedTableCell()

@end

@implementation ChatterFeedTableCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
         self = [[NSBundle mainBundle] loadNibNamed:@"ChatterFeedTableCell2" owner:nil options:nil][0];
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
