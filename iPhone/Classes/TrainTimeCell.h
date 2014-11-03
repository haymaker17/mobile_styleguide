//
//  TrainTimeCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface TrainTimeCell : UITableViewCell {
	UILabel	*lbl;
	UIImageView *iv;
}

@property (strong, nonatomic) IBOutlet UILabel *lbl;
@property (strong, nonatomic) IBOutlet UIImageView *iv;

@end
