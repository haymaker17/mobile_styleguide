//
//  CommentCell.m
//  ConcurMobile
//
//  Created by yiwen on 5/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "CommentCell.h"


@implementation CommentCell
@synthesize lblComment, lblCommentedBy;

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

-(void) resetCellContent:(NSString*)comment commentedBy:(NSString *)commentedBy
{
    self.lblComment.text = comment;
    self.lblCommentedBy.text = commentedBy;
}


@end
