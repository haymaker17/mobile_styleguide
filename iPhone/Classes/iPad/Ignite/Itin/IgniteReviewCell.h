//
//  IgniteReviewCell.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface IgniteReviewCell : UITableViewCell
{
    UILabel             *lblUserName, *lblLocation, *lblReview, *lblDate;
    UIImageView         *ivProfile, *ivRating;
}

@property (nonatomic, strong) IBOutlet UILabel              *lblUserName;
@property (nonatomic, strong) IBOutlet UILabel              *lblLocation;
@property (nonatomic, strong) IBOutlet UILabel              *lblReview;
@property (nonatomic, strong) IBOutlet UILabel              *lblDate;
@property (nonatomic, strong) IBOutlet UIImageView          *ivProfile;
@property (nonatomic, strong) IBOutlet UIImageView          *ivRating;

@end
