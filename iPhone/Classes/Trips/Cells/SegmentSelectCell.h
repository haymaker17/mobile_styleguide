//
//  SegmentSelectCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/18/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SegmentSelectCell : UITableViewCell
{
    UILabel *lblHeading, *lblSubHeading, *lblAmount;
    UIImageView *ivCheck, *ivIcon;
}


@property (strong, nonatomic) IBOutlet UILabel *lblHeading;
@property (strong, nonatomic) IBOutlet UILabel *lblSubHeading;
@property (strong, nonatomic) IBOutlet UILabel *lblSubHeading2;
@property (strong, nonatomic) IBOutlet UILabel *lblAmount;
@property (strong, nonatomic) IBOutlet UIImageView *ivCheck;
@property (strong, nonatomic) IBOutlet UIImageView *ivIcon;
@end
