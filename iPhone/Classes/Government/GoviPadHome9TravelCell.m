//
//  GoviPadHome9TravelCell.m
//  ConcurMobile
//
//  Created by Shifan Wu on 4/8/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GoviPadHome9TravelCell.h"

@implementation GoviPadHome9TravelCell
@synthesize lblSubTitle, lblTitle, btnCurrTrip, imageView;

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
