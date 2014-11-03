//
//  CarListCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class CachedImageView;

@interface CarListCell : UITableViewCell
{
	UILabel			*chainName;
	UILabel			*dailyRate;
	UILabel			*dailyRateUnit;
	UILabel			*total;
	UILabel			*carType;
	UILabel			*airConditioning;
	UILabel			*mileage;
	UILabel			*transmissionType;
	UIImageView		*logoView;
}

@property (nonatomic, strong) IBOutlet UILabel			*chainName;
@property (nonatomic, strong) IBOutlet UILabel			*dailyRate;
@property (nonatomic, strong) IBOutlet UILabel			*dailyRateUnit;
@property (nonatomic, strong) IBOutlet UILabel			*total;
@property (nonatomic, strong) IBOutlet UILabel			*carType;
@property (nonatomic, strong) IBOutlet UILabel			*airConditioning;
@property (nonatomic, strong) IBOutlet UILabel			*mileage;
@property (nonatomic, strong) IBOutlet UILabel			*transmissionType;
@property (nonatomic, strong) IBOutlet UIImageView		*logoView;

extern NSString * const CAR_LIST_CELL_REUSABLE_IDENTIFIER;

-(NSString*)reuseIdentifier;



@end
