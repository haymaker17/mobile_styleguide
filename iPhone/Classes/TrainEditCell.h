//
//  TrainEditCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TrainPassengersVC.h"

@interface TrainEditCell : UITableViewCell {
	UILabel	*lbl;
	UITextField	*txt;
	int			row;
	TrainPassengersVC	*parentVC;
}

@property (strong, nonatomic) IBOutlet UILabel	*lbl;
@property (strong, nonatomic) IBOutlet UITextField	*txt;
@property int row;
@property (strong, nonatomic) TrainPassengersVC	*parentVC;

-(IBAction)txtChange:(id)sender;
@end
