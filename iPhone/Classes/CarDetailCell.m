//
//  CarDetailCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CarDetailCell.h"
#import "CachedImageView.h"
#import "ImageCache.h"


@implementation CarDetailCell


@synthesize imgView;
@synthesize descriptionLabel;
@synthesize imageNotAvailableLabel;


NSString * const CAR_DETAIL_CELL_REUSABLE_IDENTIFIER = @"CAR_DETAIL_CELL_REUSABLE_IDENTIFIER";


// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return CAR_DETAIL_CELL_REUSABLE_IDENTIFIER;
}

+(CarDetailCell*)makeCell:(UITableView*)tableView owner:(id)owner description:(NSString*)description imageUri:(NSString*)imageUri imageCache:(ImageCache*)imageCache
{
	CarDetailCell *cell = (CarDetailCell *)[tableView dequeueReusableCellWithIdentifier: CAR_DETAIL_CELL_REUSABLE_IDENTIFIER];
	
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"CarDetailCell" owner:owner options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[CarDetailCell class]])
			{
				cell = (CarDetailCell *)oneObject;
				break;
			}
		}
	}
	
	cell.imageNotAvailableLabel.text = [Localizer getLocalizedText:@"Image Not Available"];
	cell.descriptionLabel.text = description;
	
	if (imageUri != nil && [imageUri length] > 0)
	{
		[cell.imgView loadDataFromUri:imageUri cache:imageCache];
	}
	
	return cell;
}

@end
