//
//  GovDocExceptionAlertCell.m
//  ConcurMobile
//
//  Created by Shifan Wu on 1/2/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovDocExceptionAlertCell.h"

@implementation GovDocExceptionAlertCell

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

- (void)prepareForReuse
{
    [super prepareForReuse];
    [self.alertName setHidden:NO];
    
    self.alertName.text = @"";
    self.exceptionName.text = @"";
    self.passOrFail.text = @"";
    
}
@end
