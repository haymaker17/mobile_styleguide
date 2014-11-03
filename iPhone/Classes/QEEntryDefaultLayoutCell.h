//
//  QEEntryDefaultLayoutCell.h
//  ConcurMobile
//
//  Created by Shifan Wu on 11/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface QEEntryDefaultLayoutCell : UITableViewCell
{
    UILabel     *lblHeading, *lblAmount, *lblSub1, *lblSub2;
    UIImageView *ivIcon1, *ivIcon2, *ivSelected;
}

@property (strong, nonatomic) IBOutlet UILabel     *lblHeading;
@property (strong, nonatomic) IBOutlet UILabel     *lblAmount;
@property (strong, nonatomic) IBOutlet UILabel     *lblSub1;
@property (strong, nonatomic) IBOutlet UILabel     *lblSub2;
@property (strong, nonatomic) IBOutlet UIImageView *ivIcon1;
@property (strong, nonatomic) IBOutlet UIImageView *ivIcon2;
@property (strong, nonatomic) IBOutlet UIImageView *ivSelected;


@end
