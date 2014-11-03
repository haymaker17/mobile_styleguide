//
//  IntroCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 2/20/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TourCell : UICollectionViewCell

@property (weak, nonatomic) IBOutlet UIImageView *imageView;
@property (weak, nonatomic) IBOutlet UIView *viewBehindLabels;
@property (weak, nonatomic) IBOutlet UILabel *labelTitle;
@property (weak, nonatomic) IBOutlet UILabel *labelSubTitle;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *textViewHeight;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *textViewWidth;
@property (weak,nonatomic) IBOutlet  NSLayoutConstraint *labelSubTitileHeight;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *labelTitleHeight;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *labelTitleWidth;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coSubTitleBottom;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coTitileTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *textViewBottom;

@end
