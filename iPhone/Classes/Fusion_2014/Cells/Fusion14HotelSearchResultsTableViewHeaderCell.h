//
//  HotelSearchResultsTableViewHeaderCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 3/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Fusion14HotelSearchResultsTableViewHeaderCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UILabel *labelLocation;
@property (weak, nonatomic) IBOutlet UILabel *labelDate;
@property (weak, nonatomic) IBOutlet UILabel *labelTitlePriceToBeat;
@property (weak, nonatomic) IBOutlet UILabel *labelPriceToBeat;
@property (weak, nonatomic) IBOutlet UIImageView *imageViewIcon;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coPriceToBeatLeft;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coViewPriceToBeatWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coTextPriceToBeatWidth;
@end
