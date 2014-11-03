//
//  HotelCollectionViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelCollectionViewController.h"
#import "HotelListCell.h"
#import "HotelSearch.h"
#import "HotelSearchCriteria.h"
#import "HotelResult.h"
#import "FormatUtils.h"
#import "AsyncImageView.h"
#import "HotelSearchResultsViewController.h"
#import "DateTimeFormatter.h"
#import "ExSystem.h" 

#import "MCLogging.h"
#import "HotelImageData.h"

@implementation HotelCollectionViewController


@synthesize parentMVC, hotelSearchCriteria, hotelSearch;


-(void)notifyChange
{
}


-(void)didSwitchViews
{
}


#pragma mark -
#pragma mark View lifecycle

- (void)viewWillAppear:(BOOL)animated
{
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelCollectionViewController::viewWillAppear: %@", [self class]] Level:MC_LOG_DEBU];
    [super viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelCollectionViewController::viewWillDisappear: %@", [self class]] Level:MC_LOG_DEBU];
    [super viewWillDisappear:animated];
}

-(void)viewDidLayoutSubviews
{
    [super viewDidLayoutSubviews];
    if ([self respondsToSelector:@selector(topLayoutGuide)])
    {
        CGRect viewBounds = self.view.bounds;
        CGFloat topBarOffset = self.topLayoutGuide.length;

        [self.view setFrame:CGRectMake(viewBounds.origin.x, topBarOffset, viewBounds.size.width, viewBounds.size.height-topBarOffset)];
    }
}

#pragma mark -
#pragma mark Toolbar Methods

- (void)updateToolbar
{
	// overriden
}


#pragma mark -
#pragma mark Methods called by cell

-(void)addressPressed:(id)sender
{
	HotelListCell *cell = (HotelListCell *)sender;
	[parentMVC.hotelSearch selectHotel:cell.hotelIndex];
	[parentMVC showMap];
}

-(void)phonePressed:(id)sender
{
	HotelListCell *cell = (HotelListCell*)sender;
	NSString *phoneNumber = cell.phone.text;
	
	NSString *digitsOnlyPhoneNumber = [[phoneNumber componentsSeparatedByCharactersInSet:[[NSCharacterSet characterSetWithCharactersInString:@"0123456789"] invertedSet]] componentsJoinedByString:@""];
	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", digitsOnlyPhoneNumber]]];
}


#pragma mark -
#pragma mark Action Sheet button handlers

- (IBAction)buttonReorderPressed:(id)sender
{
	// overriden
}

- (IBAction)buttonActionPressed:(id)sender
{
	// overriden
}


#pragma mark -
#pragma mark Cell Methods

- (HotelListCell *)makeAndConfigureHotelListCellForTable:(UITableView *)tableView hotel:(HotelResult*)hotelResult
{
    static NSString *CellIdentifier = @"HotelListSimplerCell";
	
	HotelListCell *cell = (HotelListCell *)[tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil)
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelListSimplerCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[HotelListCell class]])
				cell = (HotelListCell*)oneObject;
    }
    
    // Configure the cell...
	cell.parentMVC = self;
	
	cell.name.text = hotelResult.hotel;
	cell.address1.text = hotelResult.address1;
	
	NSString *state = ((hotelResult.stateAbbrev != nil && [hotelResult.stateAbbrev length]) > 0 ? hotelResult.stateAbbrev : hotelResult.state);
    /*MOB-4400
    Fixed by checking to see if state is nil.  If so, make it an empty string.*/
    if(state == nil)
        state = @"";
    
	NSString *cityStateZip = [NSString stringWithFormat:@"%@, %@ %@", hotelResult.city, state, hotelResult.zip];
	cell.address2.text = cityStateZip;

//	if (hotelResult.address2 != nil && hotelResult.address2.length > 0)
//	{
//		cell.address2.text = hotelResult.address2;
//		cell.address3.text = cityStateZip;
//	}
//	else
//	{
//		cell.address2.text = cityStateZip;
//		cell.address3.text = @"";
//	}
	
	cell.phone.text = hotelResult.phone;
	cell.distance.text = [NSString stringWithFormat:@"%@ %@", hotelResult.distance, hotelResult.distanceUnit];
	
	cell.amount.text = hotelResult.cheapestRoomRateAsFormattedString;
	//cell.amount.textColor = amountTextColor;
	

    
    NSString *asterisks = hotelResult.starRatingAsterisks;
	if (asterisks == nil)
	{
		cell.starRating.hidden = YES;
		cell.shadowStarRating.hidden = YES;
		cell.notRated.hidden = YES;
        cell.ivStars.hidden = NO;
        cell.ivStars.image = [UIImage imageNamed:@"stars_0"];
	}
	else
	{
        cell.ivStars.hidden = NO;
        int starCount = [asterisks length];
        if(starCount == 1)
            cell.ivStars.image = [UIImage imageNamed:@"stars_1"];
        else if(starCount == 2)
            cell.ivStars.image = [UIImage imageNamed:@"stars_2"];
        else if(starCount == 3)
            cell.ivStars.image = [UIImage imageNamed:@"stars_3"];
        else if(starCount == 4)
            cell.ivStars.image = [UIImage imageNamed:@"stars_4"];
        else if(starCount == 5)
            cell.ivStars.image = [UIImage imageNamed:@"stars_5"];
		cell.starRating.hidden = NO;
		cell.shadowStarRating.hidden = NO;
		cell.notRated.hidden = YES;
	}
	
    
    int diamonds = hotelResult.hotelPrefRank;
	if (diamonds == 0)
	{
		cell.ivDiamonds.hidden = YES;
	}
	else
	{
        //NSLog(@"diamonds %d", diamonds);
        cell.ivDiamonds.hidden = NO;
        if(diamonds == 4)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_1"];
        else if(diamonds == 5)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_2"];
        else if(diamonds == 10)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_3"];
        else if(diamonds == 1)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_1"];
        else if(diamonds == 2)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_2"];
        else if(diamonds == 3)
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_3"];
	}
	// Load the logo asynchronously
//	if (hotelResult.propertyUri != nil && [hotelResult.propertyUri length] > 0)
//	{
//		cell.logoView.image = nil;  // The cell may have been dequeued with an image already in it.  Get rid of it.
//		UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
//		[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:hotelResult.propertyUri RespondToImage:img IV:cell.logoView MVC:parentMVC];
//	}
    
    if (hotelResult.propertyImagePairs != nil && [hotelResult.propertyImagePairs count] > 0)
	{
		cell.logoView.image = nil;  // The cell may have been dequeued with an image already in it.  Get rid of it.
		UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
        HotelImageData *hid = (hotelResult.propertyImagePairs)[0];
		[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:hid.hotelThumbnail RespondToImage:img IV:cell.logoView MVC:parentMVC];
	}
    
    cell.ivRecommendation.hidden = YES;
    cell.recommendationText.hidden = YES;
	
    return cell;
}

- (void)dealloc
{
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelCollectionViewController::dealloc: %@", [self class]] Level:MC_LOG_DEBU];
}

@end

