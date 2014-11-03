//
//  HotelDetailsViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/6/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelDetailsViewController.h"
#import "ExSystem.h" 

#import "HotelSearchResultsViewController.h"
#import "hotelSearch.h"
#import "HotelSearchCriteria.h"
#import "HotelResult.h"
#import "HotelInfo.h"
#import "HotelFee.h"
#import "HotelDetail.h"
#import "RoomListSummaryCell.h"
#import "HotelDetailedMapViewController.h"
#import "HotelDetailsVariableHeightCell.h"
#import "AsyncImageView.h"
#import "HotelDetailsTableSection.h"
#import "BookingLabelValueCell.h"


@implementation HotelDetailsViewController


@synthesize hotelSearch;
@synthesize tableSections, hotelBooking, aFees;


#define kSectionHotelSummary 0
#define kSectionFees 1
#define kSectionDetails 2

#define kRowHeader 0


-(void)updateTitle
{
	if (hotelBooking != nil)
		self.title = hotelBooking.hotel;
}


#pragma mark -
#pragma mark Initialization

/*
- (id)initWithStyle:(UITableViewStyle)style {
    // Override initWithStyle: if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
    if ((self = [super initWithStyle:style])) {
    }
    return self;
}
*/


#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return HOTEL_DETAILS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		if ((msg.parameterBag)[@"HOTEL_SEARCH"] != nil)
			self.hotelSearch = (HotelSearch *)(msg.parameterBag)[@"HOTEL_SEARCH"];
        
        if ((msg.parameterBag)[@"HOTEL_BOOKING"] != nil)
			self.hotelBooking = (EntityHotelBooking *)(msg.parameterBag)[@"HOTEL_BOOKING"];
        
        self.aFees = [hotelBooking.relHotelFee allObjects];
		
		[self updateTitle];

		[self makeToolbar];
		
		[self makeTableSections];

		[tblView reloadData];
	}
}


#pragma mark -
#pragma mark View lifecycle

/*
- (void)viewDidLoad {
    [super viewDidLoad];

    // Uncomment the following line to preserve selection between presentations.
    //self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}
*/

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	
	[self updateTitle];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation {
	// Call reloadData to cause the variable height cells need to be recalculated and redrawn.
	[tblView reloadData];
}


#pragma mark -
#pragma mark Table Section Methods

- (void)makeTableSections
{
	self.tableSections = nil;
	
//	if (hotelSearch == nil)
//		return;
	
	const NSUInteger feeCount = [hotelBooking.relHotelFee count];
	const NSUInteger detailCount = [hotelBooking.relHotelDetail count];
	
	NSMutableArray *sections = [[NSMutableArray alloc] initWithCapacity: 1 + (feeCount == 0 ? 0 : 1) + detailCount];
	
	// Create the header section
	HotelDetailsTableSection *headerSection = [[HotelDetailsTableSection alloc] initWithTableSectionType:HOTEL_DETAILS_TABLE_SECTION_TYPE_SUMMARY];
	[sections addObject:headerSection];
	
	// Create one section in which to show all fees (if there are any)
	if (feeCount > 0)
	{
		HotelDetailsTableSection *feeSection = [[HotelDetailsTableSection alloc] initWithTableSectionType:HOTEL_DETAILS_TABLE_SECTION_TYPE_FEES];
		[sections addObject:feeSection];
	}
	
	// Create the one section per detail
	for (int i = 0; i < detailCount; i++)
	{
		HotelDetailsTableSection *detailSection = [[HotelDetailsTableSection alloc] initWithTableSectionType:HOTEL_DETAILS_TABLE_SECTION_TYPE_DETAIL];
		detailSection.index = i;
		[sections addObject:detailSection];
	}
	
	self.tableSections = sections;
	
}


#pragma mark -
#pragma mark Table view data source

-(NSString*) tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    HotelDetailsTableSection* tableSection = tableSections[section];
    
    if(section == 0)
        return @"";
    else if (HOTEL_DETAILS_TABLE_SECTION_TYPE_FEES == tableSection.type)
        return [Localizer getLocalizedText:@"HOTEL_DETAILS_VIEW_FEES"];
    else if (HOTEL_DETAILS_TABLE_SECTION_TYPE_DETAIL == tableSection.type)
    {
        NSArray *a = [hotelBooking.relHotelDetail allObjects];
        EntityHotelDetail *detail = a[tableSection.index];
        return detail.name;
//        return hotelBooking.hotel;
//        HotelDetail *hotelDetail = [hotelSearch.selectedHotel.detail.hotelDetails objectAtIndex:tableSection.index];
//        return hotelDetail.name;
    }
    else
        return @"";
        
    
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return (tableSections == nil ? 0 : [tableSections count]);
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	HotelDetailsTableSection* tableSection = tableSections[section];

	if (HOTEL_DETAILS_TABLE_SECTION_TYPE_SUMMARY == tableSection.type)
		return 1; // One big summary row.  No header row.
	else if (HOTEL_DETAILS_TABLE_SECTION_TYPE_FEES == tableSection.type)
		return [hotelBooking.relHotelFee count]; // One header row + one row per fee
	else if (HOTEL_DETAILS_TABLE_SECTION_TYPE_DETAIL == tableSection.type)
		return 1; // One header row + one detail row

	return 0;
}



-(void)configureCell:(RoomListSummaryCell*)cell hotel:(EntityHotelBooking *)hotelResult showAddressLink:(BOOL)showAddressLink
{
	
	cell.hotelName.text = hotelResult.hotel;
	
	cell.address1.text = hotelResult.addr1;
	
	NSMutableString *cityStateZip = [[NSMutableString alloc] initWithString:@""];
    
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
    
	cell.address2.text = cityStateZip;
	
	if (!showAddressLink)
	{
		cell.address1.textColor = [UIColor blackColor];
		cell.address2.textColor = [UIColor blackColor];
	}
	
	cell.phone.text = hotelResult.phone;
	
	cell.distance.text = [NSString stringWithFormat:@"%@ %@", hotelResult.distance, hotelResult.distanceUnit];
	
	int asterisks = [hotelResult.starRating intValue];
	if (asterisks == 0)
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
        int starCount = asterisks;
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
//	cell.starRating.text = (asterisks == nil ? @"" : asterisks);
    
    int diamonds = [hotelResult.hotelPrefRank intValue];
	if (diamonds == 0)
	{
		cell.ivDiamonds.hidden = YES;
        cell.lblPreferred.text = [Localizer getLocalizedText:@"Not Preferred"];
	}
	else
	{
        //NSLog(@"diamonds %d", diamonds);
        cell.ivDiamonds.hidden = NO;
        if(diamonds == 4)
        {
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_1"];
            cell.lblPreferred.text = [Localizer getLocalizedText:@"Least Preferred"];
        }
        else if(diamonds == 5)
        {
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_2"];
            cell.lblPreferred.text = [Localizer getLocalizedText:@"Preferred"];
        }
        else if(diamonds == 10)
        {
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamonds_3"];
            cell.lblPreferred.text = [Localizer getLocalizedText:@"Most Preferred"];
        }
        else if(diamonds == 1)
        {
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_1"];
            cell.lblPreferred.text = [Localizer getLocalizedText:@"Chain Least Preferred"];
        }
        else if(diamonds == 2)
        {
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_2"];
            cell.lblPreferred.text = [Localizer getLocalizedText:@"Chain Preferred"];
        }
        else if(diamonds == 3)
        {
            cell.ivDiamonds.image = [UIImage imageNamed:@"diamondsGRAY_3"];
            cell.lblPreferred.text = [Localizer getLocalizedText:@"Chain Most Preferred"];
        }
        
        CGSize lblSize = [cell.lblPreferred.text sizeWithFont:cell.lblPreferred.font];
        
        int x = cell.lblPreferred.frame.origin.x + lblSize.width + 4;
        cell.ivDiamonds.frame = CGRectMake(x, cell.ivDiamonds.frame.origin.y, cell.ivDiamonds.frame.size.width, cell.ivDiamonds.frame.size.height);
	}
	

	cell.imageViewerMulti = [[ImageViewerMulti alloc] init];	// Retain count = 2
	
	cell.imageViewerMulti.parentVC = self;
	
	[cell.imageViewerMulti configureWithImagePairsForHotel:hotelBooking Owner:self ImageViewer:cell.ivHotel] ; //]:hotelResult.propertyImagePairs Owner:owner ImageViewer:ivHotel];
	cell.imageViewerMulti.aImageURLs = [cell.imageViewerMulti getImageURLsForHotel:hotelBooking ]; //:hotelResult.propertyImagePairs];
	cell.aImageURLs = [cell.imageViewerMulti getImageURLsForHotel:hotelBooking];
	//MOB-10293 Right arrow on hotel name does not work
    cell.accessoryType = UITableViewCellAccessoryNone;
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
//	NSUInteger row = [indexPath row];

	HotelDetailsTableSection* tableSection = tableSections[section];
   
	if (HOTEL_DETAILS_TABLE_SECTION_TYPE_SUMMARY == tableSection.type)
	{
        //EntityHotelBooking* hotelResult = [aHotel objectAtIndex: parentMVC.selectedHotelIndex];
        
        static NSString *CellIdentifier = @"RoomListSummaryCell";
        
        RoomListSummaryCell *cell = (RoomListSummaryCell *)[self.tblView dequeueReusableCellWithIdentifier:CellIdentifier];
        if (cell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"RoomListSummaryCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[RoomListSummaryCell class]])
                    cell = (RoomListSummaryCell*)oneObject;
        }
        
        [self configureCell:cell hotel:self.hotelBooking showAddressLink:NO];

        return cell;
//		RoomListSummaryCell *cell = [RoomListSummaryCell makeAndConfigureCellForTableView:tableView owner:self hotel:hotelSearch.selectedHotel showAddressLink:YES];
//        [cell setAccessoryType:UITableViewCellAccessoryNone];
//		return cell;
    }
	else if (HOTEL_DETAILS_TABLE_SECTION_TYPE_FEES == tableSection.type)
	{
//		if (kRowHeader == row)
//		{
//			BookingLabelValueCell *cell = [BookingLabelValueCell makeHeaderCell:tableView owner:self header:[Localizer getLocalizedText:@"HOTEL_DETAILS_VIEW_FEES"]];
//			return cell;
//		}
//		else
//		{
//			NSUInteger feeIndex = row;	// The first row (header row) does not count
//			HotelFee *hotelFee = [hotelSearch.selectedHotel.detail.hotelFees objectAtIndex:feeIndex];
        EntityHotelFee *fee = aFees[indexPath.row];
        HotelDetailsVariableHeightCell *cell = [HotelDetailsVariableHeightCell makeCell:tableView owner:self cellLabel:fee.type cellValue:fee.details allowDisclosure:NO];
        return cell;
//		}
	}
	else if (HOTEL_DETAILS_TABLE_SECTION_TYPE_DETAIL == tableSection.type)
	{
        NSArray *aDetails = [hotelBooking.relHotelDetail allObjects];
        EntityHotelDetail *detail = aDetails[tableSection.index];
//		HotelDetail *hotelDetail = [hotelSearch.selectedHotel.detail.hotelDetails objectAtIndex:tableSection.index];
//		if (kRowHeader == row)
//		{
//			BookingLabelValueCell *cell = [BookingLabelValueCell makeHeaderCell:tableView owner:self header:hotelDetail.name];
//			return cell;
//		}
//		else
//		{
			HotelDetailsVariableHeightCell *cell = [HotelDetailsVariableHeightCell makeCell:tableView owner:self cellLabel:nil cellValue:detail.descript allowDisclosure:NO];
			return cell;
//		}
	}
	
    return nil;
}


#pragma mark -
#pragma mark Table view delegate

- (CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
//	NSUInteger row = [indexPath row];

	HotelDetailsTableSection* tableSection = tableSections[section];

	if (HOTEL_DETAILS_TABLE_SECTION_TYPE_SUMMARY == tableSection.type)
	{
		return 90;
	}
	
	// The HOTEL_DETAILS_TABLE_SECTION_TYPE_FEES and HOTEL_DETAILS_TABLE_SECTION_TYPE_DETAIL sections have header rows
//	if (kRowHeader == row)
//	{
//		return 30;
//	}
//	else
//	{
		BOOL hideCellLabel;
		NSString *description;
		
		if (HOTEL_DETAILS_TABLE_SECTION_TYPE_FEES == tableSection.type)
		{
//			NSUInteger feeIndex = row;	// The first row (header row) does not count
//			HotelFee *hotelFee = [hotelSearch.selectedHotel.detail.hotelFees objectAtIndex:feeIndex];
            EntityHotelFee *fee = aFees[indexPath.row];
			description = fee.details;
			hideCellLabel = NO;
		}
		else //HOTEL_DETAILS_TABLE_SECTION_TYPE_DETAIL
		{
//			HotelDetail *hotelDetail = [hotelSearch.selectedHotel.detail.hotelDetails objectAtIndex:tableSection.index];
//			description = hotelDetail.description;
            NSArray *aDetails = [hotelBooking.relHotelDetail allObjects];
            EntityHotelDetail *detail = aDetails[tableSection.index];
            description = detail.descript;
			hideCellLabel = YES;
		}
		
		return [HotelDetailsVariableHeightCell calculateCellHeight:tableView hideCellLabel:hideCellLabel cellValue:description allowDisclosure:NO];
//	}
}


#pragma mark -
#pragma mark HotelSummaryDelegate

-(void)addressPressed:(id)sender
{
	HotelDetailedMapViewController * vc = [[HotelDetailedMapViewController alloc] initWithNibName:@"HotelDetailedMapViewController" bundle:nil];
//	vc.hotelResult = hotelSearch.selectedHotel;
	
	if([UIDevice isPad])
	{
		vc.modalPresentationStyle = UIModalPresentationFormSheet;
		[self presentViewController:vc animated:YES completion:nil];
	}
	else 
    {
        [[ConcurMobileAppDelegate findHomeVC] presentViewController:vc animated:YES completion:nil];
    }
}

-(void)phonePressed:(id)sender
{
	RoomListSummaryCell *cell = (RoomListSummaryCell*)sender;
	NSString *phoneNumber = cell.phone.text;
	
	NSString *digitsOnlyPhoneNumber = [[phoneNumber componentsSeparatedByCharactersInSet:[[NSCharacterSet characterSetWithCharactersInString:@"0123456789"] invertedSet]] componentsJoinedByString:@""];
	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", digitsOnlyPhoneNumber]]];
}


#pragma mark -
#pragma mark Toolbar Methods

- (void)makeToolbar
{
	//UIBarButtonItem *btnSearchCriteria = [HotelSearchResultsViewController makeSearchCriteriaButton:hotelSearch.hotelSearchCriteria];
//	NSMutableArray *toolbarItems = [NSArray arrayWithObjects:btnSearchCriteria, nil];
//	[self setToolbarItems:toolbarItems animated:NO];
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
}




@end

