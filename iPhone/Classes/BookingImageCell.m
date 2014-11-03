//
//  BookingImageCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "BookingImageCell.h"
#import "UIImageScrollView.h"

@implementation BookingImageCell


@synthesize scroller;
@synthesize descriptionLabel;
@synthesize imageViewerMulti, aImageURLs, parentVC, btnImage, ivImage;

NSString * const BOOKING_IMAGE_CELL_REUSABLE_IDENTIFIER = @"BOOKING_IMAGE_CELL_REUSABLE_IDENTIFIER";


// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return BOOKING_IMAGE_CELL_REUSABLE_IDENTIFIER;
}

+(BookingImageCell*)makeCell:(UITableView*)tableView owner:(id)owner description:(NSString*)description propertyImagePairs:(NSArray*)propertyImagePairs
{
	BookingImageCell *cell = (BookingImageCell *)[tableView dequeueReusableCellWithIdentifier: BOOKING_IMAGE_CELL_REUSABLE_IDENTIFIER];
	
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"BookingImageCell" owner:owner options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[BookingImageCell class]])
			{
				cell = (BookingImageCell *)oneObject;
				break;
			}
		}
	}
	
	cell.descriptionLabel.text = description;
	//[cell.scroller configureWithImagePairs:propertyImagePairs owner:owner]; //removed so that we can call this correctly.

	return cell;
}




#pragma mark -
#pragma mark ImageViewerMulti Methods
-(void)configureImages:(id)owner propertyImagePairs:(NSArray*)propertyImagePairs
{
	self.imageViewerMulti = [[ImageViewerMulti alloc] init];
	imageViewerMulti.parentVC = owner;
	
	[imageViewerMulti configureWithImagePairs:propertyImagePairs Owner:owner ImageViewer:ivImage];
	imageViewerMulti.aImageURLs = [imageViewerMulti getImageURLs:propertyImagePairs];
	self.aImageURLs = [imageViewerMulti getImageURLs:propertyImagePairs];
}

-(IBAction) showHotelImages:(id)sender
{	
	[imageViewerMulti showHotelImages:sender];	
}

@end
