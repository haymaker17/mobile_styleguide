//
//  CarDetailCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class CachedImageView;
@class ImageCache;

@interface CarDetailCell : UITableViewCell
{
	CachedImageView	*imgView;
	UILabel			*descriptionLabel;
	UILabel			*imageNotAvailableLabel;
}

@property (nonatomic, strong) IBOutlet CachedImageView	*imgView;
@property (nonatomic, strong) IBOutlet UILabel			*descriptionLabel;
@property (nonatomic, strong) IBOutlet UILabel			*imageNotAvailableLabel;

+(CarDetailCell*)makeCell:(UITableView*)tableView owner:(id)owner description:(NSString*)description imageUri:(NSString*)imageUri imageCache:(ImageCache*)imageCache;

@end
