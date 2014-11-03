//
//  TrainFareChoiceCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface TrainFareChoiceCell : UITableViewCell {
	
	UILabel		*lblCost, *lblLine1, *lblLine2, *lblLine3, *lblLine4, *lblLine5;
    UIImageView *ivRule;
}

@property (strong, nonatomic) IBOutlet UILabel		*lblCost;
@property (strong, nonatomic) IBOutlet UILabel		*lblLine1;
@property (strong, nonatomic) IBOutlet UILabel		*lblLine2;
@property (strong, nonatomic) IBOutlet UILabel		*lblLine3;
@property (strong, nonatomic) IBOutlet UILabel		*lblLine4;
@property (strong, nonatomic) IBOutlet UILabel		*lblLine5;
@property (strong, nonatomic) IBOutlet UIImageView  *ivRule;

@end
