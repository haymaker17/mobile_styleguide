//
//  ItineraryInformationCell.m
//  ConcurMobile
//
//  Created by Wes Barton on 5/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryInformationCell.h"

@implementation ItineraryInformationCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)awakeFromNib
{
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end