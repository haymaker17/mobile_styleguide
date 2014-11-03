//
//  PhotoAlbumTableViewCell.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 23/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PhotoAlbumTableViewCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UICollectionView *photosCollectionView;

-(void)setCollectionViewDataSourceDelegate:(id<UICollectionViewDataSource, UICollectionViewDelegate>)dataSourceDelegate;

@end
