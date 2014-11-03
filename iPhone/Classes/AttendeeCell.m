//
//  AttendeeCell.m
//  ConcurMobile
//
//  Created by yiwen on 9/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "AttendeeCell.h"


@implementation AttendeeCell
@synthesize lblAmount, lblName, lblType;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if ((self = [super initWithStyle:style reuseIdentifier:reuseIdentifier])) {
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}




@end
