//
//  TrainGroupedCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainGroupedCell.h"


@implementation TrainGroupedCell
@synthesize		lblTrain1, lblTrain1Time, lblTrain1FromCity, lblTrain1FromTime, lblTrain1ToCity, lblTrain1ToTime;
@synthesize		lblTrain2, lblTrain2Time, lblTrain2FromCity, lblTrain2FromTime, lblTrain2ToCity, lblTrain2ToTime;
@synthesize		lblPrice1, lblTo, lblPrice2, lblChoices, lblRegional1, lblRegional2;

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
