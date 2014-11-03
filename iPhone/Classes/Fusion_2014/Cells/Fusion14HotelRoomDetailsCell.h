//
//  Fusion14HotelRoomDetailsCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 4/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Fusion14HotelRoomDetailsCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *labelRoomType;
@property (weak, nonatomic) IBOutlet UILabel *labelRoomSize;
@property (weak, nonatomic) IBOutlet UILabel *labelRoomPrice;
@property (weak, nonatomic) IBOutlet UILabel *labelRecommendationTag;
@property (weak, nonatomic) IBOutlet UILabel *labelTravelPoints;
@property (weak, nonatomic) IBOutlet UIView *viewCancellation;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coRecommendationTagHeight;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coRecommendationTagWidth;

@end
