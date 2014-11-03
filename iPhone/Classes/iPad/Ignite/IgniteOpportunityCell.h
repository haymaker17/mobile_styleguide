//
//  IgniteOpportunityCell.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface IgniteOpportunityCell : UITableViewCell
{
    UILabel         *lblName, *lblAmount, *lblAccountLocation, *lblPhone;
    UIImageView     *ivProfile;
    UIView          *vwBack;
}

@property (strong, nonatomic) IBOutlet UILabel         *lblName;
@property (strong, nonatomic) IBOutlet UILabel         *lblAmount;
@property (strong, nonatomic) IBOutlet UILabel         *lblAccountLocation;
@property (strong, nonatomic) IBOutlet UILabel         *lblPhone;
@property (strong, nonatomic) IBOutlet UIImageView     *ivProfile;
@property (strong, nonatomic) IBOutlet UIView          *vwBack;

@end
