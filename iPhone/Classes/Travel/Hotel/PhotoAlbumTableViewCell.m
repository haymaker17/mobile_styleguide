//
//  PhotoAlbumTableViewCell.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 23/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "PhotoAlbumTableViewCell.h"

@interface PhotoAlbumTableViewCell()
@property (nonatomic) BOOL viewResized;
@end

@implementation PhotoAlbumTableViewCell

-(void)setCollectionViewDataSourceDelegate:(id<UICollectionViewDataSource, UICollectionViewDelegate>)dataSourceDelegate {
    self.photosCollectionView.dataSource = dataSourceDelegate;
    self.photosCollectionView.delegate = dataSourceDelegate;
    
    if (!self.viewResized) {
        CGSize contentSize = [self.photosCollectionView.collectionViewLayout collectionViewContentSize];
        self.photosCollectionView.superview.frame = CGRectMake(0, 0, contentSize.width, contentSize.height);
        self.viewResized = YES;
    }
}

@end
