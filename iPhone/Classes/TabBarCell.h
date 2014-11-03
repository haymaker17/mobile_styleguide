//
//  TabBarCell.h
//  ConcurMobile
//
//  Created by Shifan Wu on 11/14/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TabBarCell : UICollectionViewCell

@property (nonatomic, weak) IBOutlet UIImageView *ivIconImage;
@property (nonatomic, weak) IBOutlet UILabel *lblAction;
@property (nonatomic, weak) IBOutlet UILabel *lblLine;

@end
