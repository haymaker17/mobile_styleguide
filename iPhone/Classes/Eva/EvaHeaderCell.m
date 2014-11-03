//
//  EvaHeaderCell.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "EvaHeaderCell.h"

@implementation EvaHeaderCell

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

- (void)addSubview:(UIView *)view {
    if (view.bounds.size.height ==1) {
        /*
         * this suppresses the uitableview cell separator line from being displayed
         */
        return;
    }
    [super addSubview:view];
}

@end
