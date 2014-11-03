//
//  TrainBookingDetailCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainBookingDetailCell.h"
#import "TrainStopsVC.h"


@implementation TrainBookingDetailCell
@synthesize		lblAmount, lblSeat, lblDepartureStation, lblArrivalStation, lblDepartureTime, lblArrivalTime, lblDepartureDate, lblArivalDate, lblDuration, lblTrain;
@synthesize		lblDepartureStationR, lblArrivalStationR, lblDepartureTimeR, lblArrivalTimeR, lblDepartureDateR, lblArivalDateR, lblDurationR, lblTrainR;
@synthesize	iv1, iv2, iv3, iv4, iv5, iv6, parentVC, ivBack, scroller;

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




-(IBAction) launchStops:(id)sender
{
	TrainStopsVC *tsvc = [[TrainStopsVC alloc] initWithNibName:@"TrainStopsVC" bundle:nil];
	[parentVC presentViewController:tsvc animated:YES completion:nil];
	
}

@end
