//
//  RoomListSegmentsTableViewCell.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 9/17/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface RoomListSegmentsTableViewCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UISegmentedControl *segmentedCtrl;
@property (copy, nonatomic) void(^onSegmentsSelected)(NSDictionary *info);
@end
