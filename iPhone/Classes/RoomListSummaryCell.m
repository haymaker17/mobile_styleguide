//
//  RoomListSummaryCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "RoomListSummaryCell.h"
#import "RoomListViewController.h"
#import "HotelImageData.h"
#import "MobileViewController.h"
#import "ExSystem.h" 

#import "ImageViewerVC.h"
#import "iPadImageViewerVC.h"

@implementation RoomListSummaryCell

NSString * const ROOM_LIST_SUMMARY_CELL_REUSABLE_IDENTIFIER = @"ROOM_LIST_SUMMARY_CELL_REUSABLE_IDENTIFIER";

@synthesize hotelSummaryDelegate;
@synthesize scroller;
@synthesize hotelName;
@synthesize address1;
@synthesize address2;
@synthesize address3;
@synthesize phone;
@synthesize distance;
@synthesize starRating;
@synthesize shadowStarRating;
@synthesize notRated;
@synthesize isAddressLinked;
@synthesize currentPage, ivHotel, btnHotel, aImageURLs;
@synthesize imageViewerMulti, ivStars, ivDiamonds, lblPreferred;

+(RoomListSummaryCell*) makeCellForTableView:(UITableView*)tableView owner:(id)owner
{
	RoomListSummaryCell *cell = (RoomListSummaryCell*)[tableView dequeueReusableCellWithIdentifier:ROOM_LIST_SUMMARY_CELL_REUSABLE_IDENTIFIER];
	if (cell == nil)
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"RoomListSummaryCell" owner:owner options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[RoomListSummaryCell class]])
			{
				cell = (RoomListSummaryCell*)oneObject;
				break;
			}
		}
	}
	return cell;
}

+(RoomListSummaryCell*) makeAndConfigureCellForTableView:(UITableView*)tableView owner:(MobileViewController*)owner hotel:(EntityHotelBooking*)hotelResult showAddressLink:(BOOL)showAddressLink
{
	RoomListSummaryCell* cell = [RoomListSummaryCell makeCellForTableView:tableView owner:owner];
	[cell configure:owner hotel:hotelResult showAddressLink:showAddressLink];
	return cell;
}

-(void)configure:(id)owner hotel:(EntityHotelBooking *)hotelResult showAddressLink:(BOOL)showAddressLink
{
	self.hotelSummaryDelegate = (id<HotelSummaryDelegate>)owner;
	
	self.isAddressLinked = showAddressLink;
	
	hotelName.text = hotelResult.hotel;
	
	address1.text = hotelResult.addr1;
	
	NSMutableString *cityStateZip = [[NSMutableString alloc] initWithString:@""];// [NSString stringWithFormat:@"%@, %@ %@", hotelResult.city, hotelResult.stateAbbrev, hotelResult.zip];
    if(hotelResult.city != nil)
        [cityStateZip appendString:hotelResult.city];
    
    if(hotelResult.stateAbbrev != nil)
    {   [cityStateZip appendString:@", "];
        [cityStateZip appendString:hotelResult.stateAbbrev];
    }
    
    if(hotelResult.zip != nil)
    {   [cityStateZip appendString:@" "];
        [cityStateZip appendString:hotelResult.zip];
    }
    
	address2.text = cityStateZip;
	
	if (!showAddressLink)
	{
		address1.textColor = [UIColor blackColor];
		address2.textColor = [UIColor blackColor];
	}
	
	phone.text = hotelResult.phone;
	
	distance.text = [NSString stringWithFormat:@"%@ %@", hotelResult.distance, hotelResult.distanceUnit];
	
	int asterisks = [hotelResult.starRating intValue];
	if (asterisks == 0)
	{
		starRating.hidden = YES;
		shadowStarRating.hidden = YES;
		notRated.hidden = YES;
        ivStars.hidden = NO;
        ivStars.image = [UIImage imageNamed:@"stars_0"];
	}
	else
	{
        ivStars.hidden = NO;
        int starCount = asterisks;
        if(starCount == 1)
            ivStars.image = [UIImage imageNamed:@"stars_1"];
        else if(starCount == 2)
            ivStars.image = [UIImage imageNamed:@"stars_2"];
        else if(starCount == 3)
            ivStars.image = [UIImage imageNamed:@"stars_3"];
        else if(starCount == 4)
            ivStars.image = [UIImage imageNamed:@"stars_4"];
        else if(starCount == 5)
            ivStars.image = [UIImage imageNamed:@"stars_5"];
		starRating.hidden = NO;
		shadowStarRating.hidden = NO;
		notRated.hidden = YES;
	}
//	starRating.text = (asterisks == nil ? @"" : asterisks);
    
    int diamonds = [hotelResult.hotelPrefRank intValue];
	if (diamonds == 0)
	{
		ivDiamonds.hidden = YES;
        lblPreferred.text = [Localizer getLocalizedText:@"Not Preferred"];
	}
	else
	{
        //NSLog(@"diamonds %d", diamonds);
        ivDiamonds.hidden = NO;
        if(diamonds == 4)
        {
            ivDiamonds.image = [UIImage imageNamed:@"diamonds_1"];
            lblPreferred.text = [Localizer getLocalizedText:@"Least Preferred"];
        }
        else if(diamonds == 5)
        {
            ivDiamonds.image = [UIImage imageNamed:@"diamonds_2"];
            lblPreferred.text = [Localizer getLocalizedText:@"Preferred"];
        }
        else if(diamonds == 10)
        {
            ivDiamonds.image = [UIImage imageNamed:@"diamonds_3"];
            lblPreferred.text = [Localizer getLocalizedText:@"Most Preferred"];
        }
        else if(diamonds == 1)
        {
            ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_1"];
            lblPreferred.text = [Localizer getLocalizedText:@"Chain Least Preferred"];
        }
        else if(diamonds == 2)
        {
            ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_2"];
            lblPreferred.text = [Localizer getLocalizedText:@"Chain Preferred"];
        }
        else if(diamonds == 3)
        {
            ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_3"];
            lblPreferred.text = [Localizer getLocalizedText:@"Chain Most Preferred"];
        }
        
        CGSize lblSize = [lblPreferred.text sizeWithFont:lblPreferred.font];
        
        int x = lblPreferred.frame.origin.x + lblSize.width + 4;
        ivDiamonds.frame = CGRectMake(x, ivDiamonds.frame.origin.y, ivDiamonds.frame.size.width, ivDiamonds.frame.size.height);
	}
	

	
}

#pragma mark -
#pragma mark Image Handlers

-(IBAction) showHotelImages:(id)sender
{
	[imageViewerMulti showHotelImages:sender];	
}


#pragma mark -
#pragma mark Confusion!!!!!
// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return ROOM_LIST_SUMMARY_CELL_REUSABLE_IDENTIFIER;
}

-(IBAction)btnAddress:(id)sender
{
	if (isAddressLinked)
		[hotelSummaryDelegate addressPressed:self];
}

-(IBAction)btnPhone:(id)sender
{
	[hotelSummaryDelegate phonePressed:self];
}

- (void)dealloc
{
	if (imageViewerMulti != nil)
		imageViewerMulti.parentVC = nil;
}

@end
