//
//  GovAirShopSummaryCell.h
//  ConcurMobile
//
//  Created by Shifan Wu on 2/19/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface GovAirShopSummaryCell : UITableViewCell

@property (strong, nonatomic) IBOutlet UILabel *lblResultCount;
@property (strong, nonatomic) IBOutlet UIImageView *ivLogo;
@property (strong, nonatomic) IBOutlet UIImageView *ivPref;
@property (strong, nonatomic) IBOutlet UILabel *lblAirline;
@property (strong, nonatomic) IBOutlet UILabel *lblStarting;
@property (strong, nonatomic) IBOutlet UILabel *lblCost;

@end
