//
//  TrainStopCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface TrainStopCell : UITableViewCell {
	UIImageView		*iv;
	UILabel			*lblStop, *lbl1, *lbl2;

}

@property (strong, nonatomic) IBOutlet UIImageView		*iv;
@property (strong, nonatomic) IBOutlet UILabel			*lblStop;
@property (strong, nonatomic) IBOutlet UILabel			*lbl1;
@property (strong, nonatomic) IBOutlet UILabel			*lbl2;

@end
