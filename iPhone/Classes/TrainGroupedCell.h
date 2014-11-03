//
//  TrainGroupedCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface TrainGroupedCell : UITableViewCell {
	
	UILabel		*lblTrain1, *lblTrain1Time, *lblTrain1FromCity, *lblTrain1FromTime, *lblTrain1ToCity, *lblTrain1ToTime;
	UILabel		*lblTrain2, *lblTrain2Time, *lblTrain2FromCity, *lblTrain2FromTime, *lblTrain2ToCity, *lblTrain2ToTime;
	UILabel		*lblPrice1, *lblTo, *lblPrice2, *lblChoices, *lblRegional1, *lblRegional2;

}

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
@property(strong, nonatomic) IBOutlet UILabel		*lblPrice1;
@property(strong, nonatomic) IBOutlet UILabel		*lblTo;
@property(strong, nonatomic) IBOutlet UILabel		*lblPrice2;
@property(strong, nonatomic) IBOutlet UILabel		*lblChoices;
@property(strong, nonatomic) IBOutlet UILabel		*lblRegional1;
@property(strong, nonatomic) IBOutlet UILabel		*lblRegional2;

@end
