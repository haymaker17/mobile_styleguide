//
//  GovAirShopSectionHeader.h
//  ConcurMobile
//
//  Created by Shifan Wu on 2/20/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "VerticallyAlignedLabel.h"

@interface GovAirShopSectionHeader : UIView

@property (strong, nonatomic) IBOutlet VerticallyAlignedLabel *lblResultsType;
@property (strong, nonatomic) IBOutlet UIButton *btnAllResults;

@end
