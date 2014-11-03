//
//  ItinDetailCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface ItinDetailCell : UITableViewCell {
    UILabel         *lblLabel, *lblValue;
    UIImageView     *ivDot;
}

@property (strong, nonatomic) IBOutlet UILabel          *lblLabel;
@property (strong, nonatomic) IBOutlet UILabel          *lblValue;
@property (strong, nonatomic) IBOutlet UIImageView      *ivDot;

@end
