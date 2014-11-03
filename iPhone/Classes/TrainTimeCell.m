//
//  TrainTimeCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainTimeCell.h"


@implementation TrainTimeCell
@synthesize lbl, iv;

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
