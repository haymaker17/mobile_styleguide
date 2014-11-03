//
//  QEEntryDefaultLayoutCell.m
//  ConcurMobile
//
//  Created by Shifan Wu on 11/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "QEEntryDefaultLayoutCell.h"

@implementation QEEntryDefaultLayoutCell
@synthesize lblSub1, lblSub2, lblAmount, lblHeading, ivIcon1, ivIcon2, ivSelected;

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
