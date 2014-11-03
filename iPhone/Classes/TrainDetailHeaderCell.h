//
//  TrainDetailHeaderCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface TrainDetailHeaderCell : UITableViewCell {
	UILabel		*lblTrain1, *lblTrain1Time, *lblTrain1FromCity, *lblTrain1FromTime, *lblTrain1ToCity, *lblTrain1ToTime;
	UILabel		*lblTrain2, *lblTrain2Time, *lblTrain2FromCity, *lblTrain2FromTime, *lblTrain2ToCity, *lblTrain2ToTime;
	UILabel		*lblFromLabel, *lblFrom, *lblToLabel, *lblTo, *lblDateRange;
	UILabel		*lblCost, *lblSeat1, *lblSeat2;

}


@property (strong, nonatomic) IBOutlet UILabel		*lblFromLabel;
@property (strong, nonatomic) IBOutlet UILabel		*lblFrom;
@property (strong, nonatomic) IBOutlet UILabel		*lblToLabel;
@property (strong, nonatomic) IBOutlet UILabel		*lblTo;
@property (strong, nonatomic) IBOutlet UILabel		*lblDateRange;


@property(strong, nonatomic) IBOutlet UILabel		*lblTrain1;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain1Time;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain1FromCity;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain1FromTime;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain1ToCity;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain1ToTime;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain2;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain2Time;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain2FromCity;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain2FromTime;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain2ToCity;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain2ToTime;

@property(strong, nonatomic) IBOutlet UILabel		*lblSeat1;
@property(strong, nonatomic) IBOutlet UILabel		*lblSeat2;
@property(strong, nonatomic) IBOutlet UILabel		*lblCost;


@end
