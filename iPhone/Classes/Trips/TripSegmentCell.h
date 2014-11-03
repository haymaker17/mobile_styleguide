//
//  TripSegmentCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/23/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface TripSegmentCell : UITableViewCell {
    UILabel         *lblTime, *lblAmPm, *lblHeading, *lblSub1, *lblSub2;
    UIImageView     *ivIcon;
    
}

@property (strong, nonatomic) IBOutlet UILabel         *lblTime;
@property (strong, nonatomic) IBOutlet UILabel         *lblAmPm;
@property (strong, nonatomic) IBOutlet UILabel         *lblHeading;
@property (strong, nonatomic) IBOutlet UILabel         *lblSub1;
@property (strong, nonatomic) IBOutlet UILabel         *lblSub2;
@property (strong, nonatomic) IBOutlet UIImageView     *ivIcon;

@end
