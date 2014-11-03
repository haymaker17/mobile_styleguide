//
//  TrainBookingCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TrainBookVC.h"

@interface TrainBookingCell : UITableViewCell {
	UILabel				*lbl, *lblValue, *lblButton;
	UISegmentedControl	*seg;
	TrainBookVC			*parentVC;
}

@property (strong, nonatomic) IBOutlet UILabel		*lbl;
@property (strong, nonatomic) IBOutlet UILabel		*lblValue;
@property (strong, nonatomic) IBOutlet UILabel		*lblButton;
@property (strong, nonatomic) IBOutlet UISegmentedControl	*seg;
@property (strong, nonatomic) TrainBookVC	*parentVC;

-(IBAction)setRoundTrip:(id)sender;
-(IBAction)setOneWay;

@end
