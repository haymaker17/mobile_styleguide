//
//  iPadHome9Cell.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/20/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "iPadHome9Cell.h"

@implementation iPadHome9Cell

@synthesize viewCell1, lblViewCell1Title, lblViewCell1SubTitle, lblViewCell1whiteback, ivViewCell1Indicator, ivViewCell1Icon, btnViewCell1, viewCell2, lblViewCell2Title, lblViewCell2SubTitle, ivViewCell2Icon, lblViewCell2whiteback, ivViewCell2Indicator, btnViewCell2, viewCell3, lblViewCell3Title, lblViewCell3SubTitle, ivViewCell3Icon, lblViewCell3whiteback, ivViewCell3Indicator, btnViewCell3;

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
