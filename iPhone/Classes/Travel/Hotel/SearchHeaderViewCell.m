//
//  SearchHeaderViewCell.m
//  PastDestinations
//
//  Created by Pavan Adavi on 6/3/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SearchHeaderViewCell.h"

@implementation SearchHeaderViewCell

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

-(void)setCellData:(SearchTableHeaderCellData *)cellData
{
    self.lblLocation.text = cellData.location;
    self.lblStayDates.text = cellData.stayDatesString;
    self.ivLocationIcon.image = [UIImage imageNamed:cellData.imageName];
}
@end
