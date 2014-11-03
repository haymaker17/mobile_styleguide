//
//  BookingChainCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "BookingChainCell.h"
#import "MobileViewController.h"
#import "ExSystem.h" 


@implementation BookingChainCell

@synthesize chainNameLabel;
@synthesize logoImageView;

NSString * const BOOKING_CHAIN_CELL_REUSABLE_IDENTIFIER = @"BOOKING_CHAIN_CELL_REUSABLE_IDENTIFIER";


// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return BOOKING_CHAIN_CELL_REUSABLE_IDENTIFIER;
}

+(BookingChainCell*)makeCell:(UITableView*)tableView vc:(MobileViewController*)vc chainName:(NSString*)chainName logoImageUri:(NSString*)logoImageUri
{
	BookingChainCell *cell = (BookingChainCell *)[tableView dequeueReusableCellWithIdentifier:BOOKING_CHAIN_CELL_REUSABLE_IDENTIFIER];
	
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"BookingChainCell" owner:vc options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[BookingChainCell class]])
			{
				cell = (BookingChainCell *)oneObject;
				break;
			}
		}
	}
	
	cell.chainNameLabel.text = chainName;
	
	if (logoImageUri != nil && [logoImageUri length] > 0)
	{
		cell.logoImageView.image = nil;  // The cell may have been dequeued with an image already in it.  Get rid of it.
		UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
		[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:logoImageUri RespondToImage:img IV:cell.logoImageView MVC:vc];
	}
	
	return cell;
}



@end
