//
//  GovDocumentCell.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/19/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovDocumentCell.h"

@implementation GovDocumentCell
@synthesize lblName, lblAmount, lblLine1, lblLine2, lblRLine1;
@synthesize img1, img2;

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
