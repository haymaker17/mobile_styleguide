//
//  TrainBookingDetailCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TrainBookListingVC.h"

@interface TrainBookingDetailCell : UITableViewCell <UIScrollViewDelegate> {
	
	UILabel		*lblAmount, *lblSeat, *lblDepartureStation, *lblArrivalStation, *lblDepartureTime, *lblArrivalTime, *lblDepartureDate, *lblArivalDate, *lblDuration, *lblTrain;
	UILabel		*lblDepartureStationR, *lblArrivalStationR, *lblDepartureTimeR, *lblArrivalTimeR, *lblDepartureDateR, *lblArivalDateR, *lblDurationR, *lblTrainR;
	UIImageView	*iv1, *iv2, *iv3, *iv4, *iv5, *iv6;
	TrainBookListingVC		*parentVC;
	UIImageView				*ivBack;
	UIScrollView			*scroller;
}

@property (strong, nonatomic) IBOutlet 	UILabel		*lblAmount;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblSeat;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblDepartureStation;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblArrivalStation;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblDepartureTime;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblArrivalTime;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblDepartureDate;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblArivalDate;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblDuration;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblTrain;
@property (strong, nonatomic) IBOutlet 	UIImageView	*iv1;
@property (strong, nonatomic) IBOutlet 	UIImageView	*iv2;
@property (strong, nonatomic) IBOutlet 	UIImageView	*iv3;
@property (strong, nonatomic) IBOutlet 	UIImageView	*iv4;
@property (strong, nonatomic) IBOutlet 	UIImageView	*iv5;
@property (strong, nonatomic) IBOutlet 	UIImageView	*iv6;

@property (strong, nonatomic) IBOutlet 	UILabel		*lblDepartureStationR;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblArrivalStationR;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblDepartureTimeR;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblArrivalTimeR;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblDepartureDateR;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblArivalDateR;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblDurationR;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblTrainR;

@property (strong, nonatomic) IBOutlet 	UIScrollView			*scroller;

@property (strong, nonatomic) IBOutlet 	UIImageView				*ivBack;

@property (strong, nonatomic) TrainBookListingVC		*parentVC;

-(IBAction) launchStops:(id)sender;

@end
