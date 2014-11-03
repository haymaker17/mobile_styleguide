//
//  TrainDetailLegCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface TrainDetailLegCell : UITableViewCell {
	
	UILabel		*lblFromStation, *lblToStation, *lblFromTime, *lblToTime, *lblInfo;

}

@property (strong, nonatomic) IBOutlet UILabel		*lblFromStation;
@property (strong, nonatomic) IBOutlet UILabel		*lblToStation;
@property (strong, nonatomic) IBOutlet UILabel		*lblFromTime;
@property (strong, nonatomic) IBOutlet UILabel		*lblToTime;
@property (strong, nonatomic) IBOutlet UILabel		*lblInfo;

@end
