//
//  TrainBookingCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainBookingCell.h"


@implementation TrainBookingCell
@synthesize 	lbl, lblValue, seg, parentVC, lblButton;

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



-(IBAction)setRoundTrip:(id)sender
{
	if(seg.selectedSegmentIndex == 1)
		parentVC.isRoundTrip = YES;
	else 
		parentVC.isRoundTrip = NO;
	
	[parentVC resetForRoundTrip];
	
	[parentVC.tableList reloadData];
}


-(IBAction)setOneWay
{
	parentVC.isRoundTrip = NO;
	[parentVC initTableData];
}


@end
