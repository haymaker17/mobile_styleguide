//
//  RoomListSegmentsTableViewCell.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 9/17/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RoomListSegmentsTableViewCell.h"

@implementation RoomListSegmentsTableViewCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)awakeFromNib
{
    // Initialization code
    // set the Rooms is selected as default
    [_segmentedCtrl setSelectedSegmentIndex:1];
    [self.segmentedCtrl addTarget:self action:@selector(segmentSelected:) forControlEvents:UIControlEventValueChanged];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void)segmentSelected:(id)sender
{
    if (self.onSegmentsSelected) {
        NSDictionary *dic = @{@"selectedSegmentIndex": [NSNumber numberWithInt:self.segmentedCtrl.selectedSegmentIndex]};
        self.onSegmentsSelected(dic);
    }
}

@end
