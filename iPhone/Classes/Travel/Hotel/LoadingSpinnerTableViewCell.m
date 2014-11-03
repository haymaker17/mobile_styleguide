//
//  LoadingSpinnerTableViewCell.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 8/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "LoadingSpinnerTableViewCell.h"

@implementation LoadingSpinnerTableViewCell

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

-(void)setCellData:(LoadingSpinnerCellData *)cellData
{
    self.lblCaption.text = cellData.loadingCaption;
    [self beginSpinning];
}

- (void)beginSpinning{
    
    CABasicAnimation* rotationAnimation;
    rotationAnimation = [CABasicAnimation animationWithKeyPath:@"transform.rotation.z"];
    rotationAnimation.toValue = [NSNumber numberWithFloat: M_PI * 2.0 /* full rotation*/];
    rotationAnimation.duration = 2.0;
    rotationAnimation.cumulative = YES;
    rotationAnimation.repeatCount = HUGE_VALF;
    [self.ivLoadingSpinner.layer addAnimation:rotationAnimation forKey:@"rotationAnimation"];
}


@end
