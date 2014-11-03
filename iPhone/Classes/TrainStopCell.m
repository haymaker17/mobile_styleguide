//
//  TrainStopCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainStopCell.h"


@implementation TrainStopCell
@synthesize iv, lblStop, lbl1, lbl2;

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
