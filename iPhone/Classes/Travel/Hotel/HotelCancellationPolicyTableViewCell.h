//
//  HotelCancellationPolicyTableViewCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 8/29/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface HotelCancellationPolicyTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UILabel *labelCancellationPolicy;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLabelHeight;

@end
