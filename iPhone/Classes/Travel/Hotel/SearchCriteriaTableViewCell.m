//
//  SearchCriteriaTableViewCell.m
//  PastDestinations
//
//  Created by Pavan Adavi on 6/18/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SearchCriteriaTableViewCell.h"

@implementation SearchCriteriaTableViewCell

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
    DLog(@"come into awakeFromNib");
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void)setCellData:(SearchCriteriaCellData *)cellData
{
    self.lblTitle.text = cellData.title;
    self.lblSubTitle.text = cellData.subTitle;
    self.ivIcon.image = [UIImage imageNamed:cellData.imageName];

}

@end
