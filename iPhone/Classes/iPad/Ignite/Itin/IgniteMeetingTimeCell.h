//
//  IgniteMeetingTimeCell.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface IgniteMeetingTimeCell : UITableViewCell
{
    UILabel             *lblStarts, *lblEnds, *lblStartTime, *lblEndTime;
}

@property (nonatomic, strong) IBOutlet UILabel          *lblStarts;
@property (nonatomic, strong) IBOutlet UILabel          *lblEnds;
@property (nonatomic, strong) IBOutlet UILabel          *lblStartTime;
@property (nonatomic, strong) IBOutlet UILabel          *lblEndTime;

@end
