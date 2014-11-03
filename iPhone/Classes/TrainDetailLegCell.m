//
//  TrainDetailLegCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainDetailLegCell.h"


@implementation TrainDetailLegCell
@synthesize lblFromStation, lblToStation, lblFromTime, lblToTime, lblInfo;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code.
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    
    [super setSelected:selected animated:animated];
    
    // Configure the view for the selected state.
}




@end
