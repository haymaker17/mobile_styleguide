//
//  CarRateCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/5/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface CarRateCell : UITableViewCell {
    UILabel             *lblHeading, *lblAmount, *lblSub, *lblPer, *lblGdsName;
    UIImageView         *ivRule;
}


@property (strong, nonatomic) IBOutlet UILabel             *lblHeading;
@property (strong, nonatomic) IBOutlet UILabel             *lblAmount;
@property (strong, nonatomic) IBOutlet UILabel             *lblSub;
@property (strong, nonatomic) IBOutlet UILabel             *lblPer;
@property (strong, nonatomic) IBOutlet UIImageView         *ivRule;
@property (strong, nonatomic) IBOutlet UILabel             *lblGdsName;
@end
