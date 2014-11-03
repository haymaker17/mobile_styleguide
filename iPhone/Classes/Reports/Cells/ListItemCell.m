//
//  ListItemCell.m
//  ConcurMobile
//
//  Created by yiwen on 4/19/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ListItemCell.h"


@implementation ListItemCell
@synthesize lblName, lblGroup;

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


#pragma mark -
#pragma mark Cell data initilation Methods 

-(void) resetCellContent:(NSString*)name withGroup:(NSString*)group
{
    self.lblName.text = name;
    self.lblGroup.text = group;
}

@end
