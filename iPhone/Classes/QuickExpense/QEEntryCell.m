//
//  QEEntryCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/6/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "QEEntryCell.h"


@implementation QEEntryCell
@synthesize lblSub1, lblSub2, lblAmount, lblHeading, ivIcon1, ivIcon2, ivSelected;

#define EDITING_HORIZONTAL_OFFSET 38

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

- (void)setEditing:(BOOL)editing animated:(BOOL)animated
{
    [self setNeedsLayout];
}

- (void)layoutSubviews
{
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationBeginsFromCurrentState:YES];
    
    [super layoutSubviews];
    // MOB-6044
    CGFloat viewWidthOffset = self.contentView.frame.size.width - 320;
    
    if (self.relatedTableView.isEditing)
    {
        CGRect contentFrame = self.contentView.frame;
        contentFrame.origin.x = EDITING_HORIZONTAL_OFFSET;
        self.contentView.frame = contentFrame;
        self.lblAmount.frame = CGRectMake(217 + viewWidthOffset - EDITING_HORIZONTAL_OFFSET, lblAmount.frame.origin.y, lblAmount.frame.size.width, lblAmount.frame.size.height);
        self.ivIcon1.frame = CGRectMake(262 + viewWidthOffset- EDITING_HORIZONTAL_OFFSET, ivIcon1.frame.origin.y, ivIcon1.frame.size.width, ivIcon1.frame.size.height);
        self.ivIcon2.frame = CGRectMake(280 + viewWidthOffset- EDITING_HORIZONTAL_OFFSET, ivIcon2.frame.origin.y, ivIcon2.frame.size.width, ivIcon2.frame.size.height);
    }
    else
    {
        CGRect contentFrame = self.contentView.frame;
        contentFrame.origin.x = 0;
        self.contentView.frame = contentFrame;
        
        self.lblAmount.frame = CGRectMake(217+ viewWidthOffset, lblAmount.frame.origin.y, lblAmount.frame.size.width, lblAmount.frame.size.height);
        self.ivIcon1.frame = CGRectMake(262+ viewWidthOffset, ivIcon1.frame.origin.y, ivIcon1.frame.size.width, ivIcon1.frame.size.height);
        self.ivIcon2.frame = CGRectMake(280+ viewWidthOffset, ivIcon2.frame.origin.y, ivIcon2.frame.size.width, ivIcon2.frame.size.height);
    }
    
    [UIView commitAnimations];
}

-(void) updateAppearanceWithSelection:(BOOL)selected editing:(BOOL)editing deleting:(BOOL)deleting
{
    NSString *selectionImageName = @"check_unselect";
    
	if (!editing)
		[self setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    else
	{
		[self setAccessoryType:UITableViewCellAccessoryNone];
        
        if (selected)
        {
            selectionImageName = (deleting ? @"check_redselect" : @"check_greenselect");
        }
	}
    
    self.ivSelected.image = [UIImage imageNamed:selectionImageName];
}

@end
