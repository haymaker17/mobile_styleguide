//
//  CarImageCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class AsyncImageView;

@interface CarImageCell : UITableViewCell
{
	AsyncImageView	*carImage;
	UILabel			*carClass;
	UILabel			*carMileage;
	UILabel			*carTrans;
	UILabel			*carAC;
}

@property (nonatomic, strong) IBOutlet AsyncImageView	*carImage;
@property (nonatomic, strong) IBOutlet UILabel			*carClass;
@property (nonatomic, strong) IBOutlet UILabel			*carMileage;
@property (nonatomic, strong) IBOutlet UILabel			*carTrans;
@property (nonatomic, strong) IBOutlet UILabel			*carAC;

+(CarImageCell*) makeCellForTableView:(UITableView*)tableView owner:(id)owner;

@end
