//
//  CarImageCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CarImageCell.h"
#import "AsyncImageView.h"

@implementation CarImageCell

NSString * const CAR_IMAGE_CELL_REUSABLE_IDENTIFIER = @"CAR_IMAGE_CELL_REUSABLE_IDENTIFIER";

@synthesize carImage;
@synthesize carClass;
@synthesize carMileage;
@synthesize carTrans;
@synthesize carAC;

+(CarImageCell*) makeCellForTableView:(UITableView*)tableView owner:(id)owner
{
	CarImageCell *cell = (CarImageCell*)[tableView dequeueReusableCellWithIdentifier:CAR_IMAGE_CELL_REUSABLE_IDENTIFIER];
	if (cell == nil)
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"CarImageCell" owner:owner options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[CarImageCell class]])
			{
				cell = (CarImageCell*)oneObject;
				break;
			}
		}
	}
	return cell;
}

// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return CAR_IMAGE_CELL_REUSABLE_IDENTIFIER;
}



@end
