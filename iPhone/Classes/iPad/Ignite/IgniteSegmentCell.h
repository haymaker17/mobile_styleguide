//
//  IgniteItinTripSegmentCell.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TripSegmentCell.h"

@interface IgniteSegmentCell : TripSegmentCell
{
    UILabel         *lblCost;
}

@property (strong, nonatomic) IBOutlet UILabel         *lblCost;

@end
