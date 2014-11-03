//
//  CarListCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CarListCell.h"
#import "CachedImageView.h"

@implementation CarListCell


@synthesize chainName;
@synthesize dailyRate;
@synthesize dailyRateUnit;
@synthesize total;
@synthesize carType;
@synthesize airConditioning;
@synthesize mileage;
@synthesize transmissionType;
@synthesize logoView;


NSString * const CAR_LIST_CELL_REUSABLE_IDENTIFIER = @"CAR_LIST_CELL_REUSABLE_IDENTIFIER";


// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return CAR_LIST_CELL_REUSABLE_IDENTIFIER;
}

@end
