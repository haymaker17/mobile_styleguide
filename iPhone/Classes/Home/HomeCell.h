//
//  HomeCell.h
//  ConcurMobile
//
//  Created by ernest cho on 2/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Badge.h"

@interface HomeCell : UITableViewCell

@property (strong, nonatomic) IBOutlet UILabel *lblTitle;
@property (strong, nonatomic) IBOutlet UILabel *lblSubTitle;
@property (strong, nonatomic) IBOutlet UIImageView *ivIcon;
@property (strong, nonatomic) IBOutlet UILabel *whiteback;
@property (strong, nonatomic) IBOutlet UIImageView *ivBackgroundImage;
@property (strong, nonatomic) IBOutlet UILabel *lblDivider;
@property (weak, nonatomic) IBOutlet Badge *badge;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coTitleTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coSubTitleTop;
@end
