//
//  HomeCollectionViewCell.h
//  ConcurHomeCollectionView
//
//  Created by ernest cho on 11/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Badge.h"

@interface HomeCollectionViewCell : UICollectionViewCell

@property (nonatomic, readwrite, strong) IBOutlet UIImageView *icon;
@property (nonatomic, readwrite, strong) IBOutlet UILabel *topLabel;        // This is for Gov long text label on home cell.
@property (nonatomic, readwrite, strong) IBOutlet UILabel *label;
@property (nonatomic, readwrite, strong) IBOutlet UILabel *sublabel;
@property (nonatomic, readwrite, strong) IBOutlet Badge *badge;

@end
