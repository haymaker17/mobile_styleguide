//
//  CarListViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CarListViewController.h"
#import "ExSystem.h" 

#import "Car.h"
#import "FindCars.h"
#import "CarShop.h"
#import "CarListCell.h"
#import "CarBookingTripData.h"
#import "CarSearchCriteria.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "LabelConstants.h"
#import "CarVendorDescriptor.h"
#import "CarDetailsViewController.h"
#import "MobileActionSheet.h"
#import "HotelViolation.h"
#import "PolicyViolationConstants.h"
#import "CarRateCell.h"
#import "UserConfig.h"

@implementation CarListViewController

#define SORT_BY_PREFERRED_VENDORS	0
#define SORT_BY_VENDOR_NAMES		1
#define SORT_BY_PRICE				2


#pragma mark -
#pragma mark MobileViewController Methods

-(NSString *)getViewIDKey
{
	return CAR_LIST;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void)respondToFoundData:(Msg *)msg
{
	if (msg.parameterBag != nil &&
		(msg.parameterBag)[@"SHOW_CARS"] != nil &&
		(msg.parameterBag)[@"CARS"] != nil &&
		(msg.parameterBag)[@"CAR_SEARCH_CRITERIA"] != nil)
	{
		self.carBookingTripData = nil;
		if ((msg.parameterBag)[@"CAR_BOOKING_TRIP_DATA"] != nil)
		{
			self.carBookingTripData = (CarBookingTripData*)(msg.parameterBag)[@"CAR_BOOKING_TRIP_DATA"];
		}
		
		self.carSearchCriteria = (CarSearchCriteria*)(msg.parameterBag)[@"CAR_SEARCH_CRITERIA"];

		// Retain the original order of the cars
		self.carsInOriginalOrder = (NSArray*)(msg.parameterBag)[@"CARS"];

		// Populate the vendors array which will be used to show the tableview
		[self populateVendors:self.carsInOriginalOrder combineAllVendors:NO];
		
		// Default sort order is by preferred vendors
		self.sortOrder = SORT_BY_PREFERRED_VENDORS;
		
		// Sort the cars
		[self sortCars];
		
		[tblView reloadData];
		
		[self makeToolbar];
	}
}


#pragma mark -
#pragma mark View lifecycle
- (void)viewDidLoad 
{
    self.title = [Localizer getLocalizedText:@"CAR_LIST"];
    [super viewDidLoad];
    
    // May not need this
    if([UIDevice isPad])
    {
        NSString *location = [CarListViewController shortenLocation:self.carSearchCriteria.pickupLocationResult.location];
        NSString *pickupDate = [DateTimeFormatter formatHotelOrCarDateForBooking:self.carSearchCriteria.pickupDate inTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
        NSString *dropoffDate = [DateTimeFormatter formatHotelOrCarDateForBooking:self.carSearchCriteria.dropoffDate inTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
        self.lblHeading.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"Cars near"], location];
        self.lblSubheading.text = [NSString stringWithFormat:@"%@ - %@", pickupDate, dropoffDate];
        
    }
}


-(void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];

	[self sortCars];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
	
	if (tblView != nil)
		[tblView reloadData];
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


#pragma mark -
#pragma mark Toolbar Methods

+ (NSString *)shortenLocation:(NSString *)location
{
	const int maxLocationLength = 30;
	
	if ([location length] <= maxLocationLength)
		return location;
	else
		return [NSString stringWithFormat:@"%@...", [location substringToIndex:(maxLocationLength - 2)]];
}

+ (UIBarButtonItem *)makeSearchCriteriaButton:(CarSearchCriteria*)carSearchCriteria
{
	NSString *location = [CarListViewController shortenLocation:carSearchCriteria.pickupLocationResult.location];
	NSString *pickupDate = [DateTimeFormatter formatHotelOrCarDateForBooking:carSearchCriteria.pickupDate inTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
	NSString *dropoffDate = [DateTimeFormatter formatHotelOrCarDateForBooking:carSearchCriteria.dropoffDate inTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
	NSString *searchCriteria = [NSString stringWithFormat:@"%@\n%@ - %@", location, pickupDate, dropoffDate];
	
	const int buttonWidth = 220;
	const int buttonHeight = 30;
	
	UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	
	UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	lblText.numberOfLines = 2;
	lblText.lineBreakMode = NSLineBreakByWordWrapping;
	lblText.textAlignment = NSTextAlignmentLeft;
	lblText.text = searchCriteria;
	[lblText setBackgroundColor:[UIColor clearColor]];
	[lblText setTextColor:[UIColor whiteColor]];
	[lblText setShadowColor:[UIColor grayColor]];
	[lblText setShadowOffset:CGSizeMake(1, 1)];
	[lblText setFont:[UIFont boldSystemFontOfSize:12.0f]];
	[cv addSubview:lblText];
	
	UIBarButtonItem* btnSearchCriteria = [[UIBarButtonItem alloc] initWithCustomView:cv];
	
	return btnSearchCriteria;
}

- (UIBarButtonItem *)makeSearchResultsButton:(CarSearchCriteria*)carSearchCriteria
{
//	NSString *location = [CarListViewController shortenLocation:carSearchCriteria.pickupLocationResult.location];
//	NSString *pickupDate = [DateTimeFormatter formatHotelOrCarDateForBooking:carSearchCriteria.pickupDate];
//	NSString *dropoffDate = [DateTimeFormatter formatHotelOrCarDateForBooking:carSearchCriteria.dropoffDate];
//	NSString *searchCriteria = [NSString stringWithFormat:@"%@\n%@ - %@", location, pickupDate, dropoffDate];

    NSString *sortName = @"";
    if(self.sortOrder == SORT_BY_PREFERRED_VENDORS)
        sortName = [Localizer getLocalizedText:@"Preferred Vendors"];
    else if(self.sortOrder == SORT_BY_VENDOR_NAMES)
        sortName = [Localizer getLocalizedText:@"Vendor Names"];
    else if(self.sortOrder == SORT_BY_PRICE)
        sortName = [Localizer getLocalizedText:@"Price"];

	const int buttonWidth = 200;
	const int buttonHeight = 32;
	
	UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	
	UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	lblText.numberOfLines = 1;
	lblText.lineBreakMode = NSLineBreakByWordWrapping;
	lblText.textAlignment = NSTextAlignmentCenter;
    lblText.text = [NSString stringWithFormat:@"%d %@ %@", [self.carsInOriginalOrder count], [Localizer getLocalizedText:@"results by"], sortName];

    if(![ExSystem is7Plus])
	{
        // Only change the results text color if using iOS6
        [lblText setBackgroundColor:[UIColor clearColor]];
        [lblText setTextColor:[UIColor whiteColor]];
    }
	//[lblText setShadowColor:[UIColor grayColor]];
	//[lblText setShadowOffset:CGSizeMake(1, 1)];
	[lblText setFont:[UIFont boldSystemFontOfSize:11]];
	[cv addSubview:lblText];
	
	UIBarButtonItem* btnSearchCriteria = [[UIBarButtonItem alloc] initWithCustomView:cv];
	
	return btnSearchCriteria;
}

- (void)makeToolbar
{
	if (self.carsInOriginalOrder != nil && [self.carsInOriginalOrder count] > 0)
	{
        NSString *location = [CarListViewController shortenLocation:self.carSearchCriteria.pickupLocationResult.location];
        NSString *pickupDate = [DateTimeFormatter formatHotelOrCarDateForBooking:self.carSearchCriteria.pickupDate inTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
        NSString *dropoffDate = [DateTimeFormatter formatHotelOrCarDateForBooking:self.carSearchCriteria.dropoffDate inTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
        //NSString *searchCriteria = [NSString stringWithFormat:@"%@\n%@ - %@", location, pickupDate, dropoffDate];
        
        self.lblHeading.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"Cars near"], location];
        self.lblSubheading.text = [NSString stringWithFormat:@"%@ - %@", pickupDate, dropoffDate];
        
		UIBarButtonItem *btnSearchCriteria = [self makeSearchResultsButton:self.carSearchCriteria];
		UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
        UIBarButtonItem *btnAction = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Sort"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonActionPressed:)];
		NSArray *toolbarItems = @[btnAction, flexibleSpace, btnSearchCriteria, flexibleSpace];
		[self setToolbarItems:toolbarItems animated:NO];
	}
}


#pragma mark -
#pragma mark Table view data source

// Uncomment these if you want to have vendor indices.
/*
- (NSArray *)sectionIndexTitlesForTableView:(UITableView *)tableView
{
	if (vendors == nil)
		return nil;
	
	NSMutableArray* indexTitles = [[[NSMutableArray alloc] init] autorelease];
	for (CarVendorDescriptor *vendor in vendors)
	{
		// When we have only one vendor (a combined vendor), the vendor name is nil
		// and we don't want to show an index
		if (vendor.vendorName == nil)
			return nil;
		
		const NSUInteger maxIndexTitleLength = 1;
		NSString* indexTitle = [vendor.vendorName length] <= maxIndexTitleLength ? vendor.vendorName : [vendor.vendorName substringToIndex:maxIndexTitleLength];
		[indexTitles addObject:indexTitle];
	}
	
	return indexTitles;
}

- (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)index
{
	// The indices of the table sections match up with the indices of the index sections
	return index;
}
*/

-(UIView*) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    float maxW = 320;
    if([UIDevice isPad])
        maxW = 540;
    UIView *viewHeader = [[UIView alloc] initWithFrame:CGRectMake(0, 0, maxW, 20)];
    viewHeader.backgroundColor = [UIColor colorWithRed:191/255.0 green:191/255.0 blue:191/255.0 alpha:1.0];
   
    CarVendorDescriptor *vendor = self.vendors[section];
    
	Car *car = (Car*)(vendor.cars)[0];
    
    UIImageView *ivBack = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, maxW, 20)];
    ivBack.image = [UIImage imageNamed: @"segment_header"];
    [viewHeader addSubview:ivBack];
    
    
    UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(maxW - 100, 4, 80, 14)];
    
	if (car.chainLogoUri != nil && [car.chainLogoUri length] > 0)
	{
		//cell.logoView.image = nil;  // The cell may have been dequeued with an image already in it.  Get rid of it.
		UIImage *img = [UIImage imageNamed:@"car_placeholder"];
		[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:car.chainLogoUri RespondToImage:img IV:iv MVC:self];
	}
    
    [viewHeader addSubview:iv];
    
    UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(20, 0, maxW - 120 , 20)];
	lblText.numberOfLines = 1;
	//lblText.lineBreakMode = NSLineBreakByWordWrapping;
	lblText.textAlignment = NSTextAlignmentLeft;
	lblText.text = vendor.vendorName;
	[lblText setBackgroundColor:[UIColor clearColor]];
	[lblText setTextColor:[UIColor blackColor]];
	[lblText setShadowColor:[UIColor whiteColor]];
	[lblText setShadowOffset:CGSizeMake(0, 1)];
	[lblText setFont:[UIFont boldSystemFontOfSize:15.0f]];
	[viewHeader addSubview:lblText];
    
    
    return viewHeader;
                           
}

//- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
//{
//	CarVendorDescriptor *vendor = [vendors objectAtIndex:section];
//	return vendor.vendorName;
//}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return ((self.vendors == nil || [self.vendors count] == 0) ? 0 : [self.vendors count]);
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	CarVendorDescriptor *vendor = self.vendors[section];
	return [vendor.cars count];
}

-(CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    int rowHeight = 55;
    
    if([UserConfig getSingleton].showGDSNameInSearchResults)
    {
        NSUInteger section = [indexPath section];
        NSUInteger row = [indexPath row];
        CarVendorDescriptor *vendor = self.vendors[section];
        Car *car = (Car*)(vendor.cars)[row];
        
        if ([car.gdsName length])
        {
            rowHeight += 13;
        }
    }

    return rowHeight;
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
	NSUInteger row = [indexPath row];
	
	CarRateCell *cell = (CarRateCell*)[tableView dequeueReusableCellWithIdentifier:@"CarRateCell"];
	if (cell == nil)
	{
		cell = [self makeCarListCell];
	}
	
	cell.lblPer.text = [Localizer getLocalizedText:@"per day"];

	CarVendorDescriptor *vendor = self.vendors[section];
	Car *car = (Car*)(vendor.cars)[row];
	cell.lblAmount.text = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", car.dailyRate] crnCode:car.currencyCode];
    
    if ([car maxEnforcementLevel] != nil)
    {
        int eLevel = [[car maxEnforcementLevel] intValue];
        if(eLevel < kViolationLogForReportsOnly || eLevel == 100)
        {	
            [cell.lblAmount setTextColor:[UIColor bookingGreenColor]];
            cell.ivRule.hidden = YES;
            cell.lblPer.hidden = NO;
        }
        else if(eLevel >= kViolationLogForReportsOnly && eLevel <= kViolationNotifyManager)
        {
            [cell.lblAmount setTextColor:[UIColor bookingYellowColor]];
            cell.ivRule.image = [UIImage imageNamed:@"icon_yellowex"];
            cell.ivRule.hidden = NO;
            cell.lblPer.hidden = YES;
        }    
        else if(eLevel >= kViolationRequiresPassiveApproval && eLevel <= kViolationRequiresApproval)
        {
            [cell.lblAmount setTextColor:[UIColor bookingRedColor]];
            cell.ivRule.image = [UIImage imageNamed:@"icon_redex"];
            cell.ivRule.hidden = NO;
            cell.lblPer.hidden = YES;
        } 
        else if(eLevel == kViolationAutoFail)
        {
            [cell.lblAmount setTextColor:[UIColor bookingGrayColor]];
            cell.ivRule.hidden = YES;
            cell.lblPer.hidden = NO;
        } 
        else
        {
            [cell.lblAmount setTextColor:[UIColor bookingRedColor]];
            cell.ivRule.image = [UIImage imageNamed:@"icon_redex"];
            cell.ivRule.hidden = NO;
            cell.lblPer.hidden = YES;
        }
    }
	cell.lblHeading.text = [NSString stringWithFormat:@"%@ %@", car.carClass, car.carBody];
	cell.lblSub.text = car.carTrans;

    cell.lblGdsName.hidden = YES;
    if([UserConfig getSingleton].showGDSNameInSearchResults && [car.gdsName length])
    {
        cell.lblGdsName.hidden = NO;
        cell.lblGdsName.text = [NSString stringWithFormat:@"(%@)", car.gdsName];
    }

	return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
	NSUInteger row = [indexPath row];
	
	CarVendorDescriptor *vendor = self.vendors[section];
	Car *car = (Car*)(vendor.cars)[row];
    
	if ([car maxEnforcementLevel] != nil && [[car maxEnforcementLevel] intValue] != kViolationAutoFail)
    {
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:car, @"CAR", self.carSearchCriteria, @"CAR_SEARCH_CRITERIA", @"YES", @"SHORT_CIRCUIT", nil];
        if (self.carBookingTripData != nil)
        {
            pBag[@"CAR_BOOKING_TRIP_DATA"] = self.carBookingTripData;
        }
        
        CarDetailsViewController *nextController = [[CarDetailsViewController alloc] initWithNibName:@"CarDetailsViewController" bundle:nil];
        // MOB-9547 Do not display custom fields if add car/hotel
        nextController.hideCustomFields = [self.carBookingTripData.tripKey length];
    	[nextController view];
        nextController.taFields = self.taFields;
        Msg *msg = [[Msg alloc] init];
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        [self.navigationController pushViewController:nextController animated:YES];
        [nextController respondToFoundData:msg];
    }
    else
    {
        NSMutableString *violationMessage = [[NSMutableString alloc] init];
        for (HotelViolation *violation in car.violations)
        {
            if ([[violation enforcementLevel] intValue] == kViolationAutoFail)
                [violationMessage appendFormat:([violationMessage length] ? @"\n%@" : @"%@"),violation.message];
        }
        NSString *displayMessage = [violationMessage length] ? violationMessage : [@"Your company's travel policy will not allow this reservation." localize];
        //Display error message
        MobileAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Reservation Not Allowed"]
                                  message:displayMessage
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                  otherButtonTitles:nil];
		[alert show];
        [tableView deselectRowAtIndexPath:indexPath animated:YES];
    }
}

/*
- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
}
*/

#pragma mark -
#pragma mark Make cell

-(CarRateCell*)makeCarListCell
{
	NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"CarRateCell" owner:self options:nil];
	for (id oneObject in nib)
	{
		if ([oneObject isKindOfClass:[CarRateCell class]])
		{
			CarRateCell *cell = (CarRateCell*)oneObject;
			return cell;
		}
	}
	return nil;
}


#pragma mark -
#pragma mark Action Sheet button handler
- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    self.actionPopOver = nil;
}

-(IBAction)buttonActionPressed:(id)sender
{
    if (self.actionPopOver) {
        [self.actionPopOver dismissWithClickedButtonIndex:-1 animated:YES];
        self.actionPopOver = nil;
        return;
    }
	
	self.actionPopOver = [[MobileActionSheet alloc] initWithTitle:[Localizer getLocalizedText:@"Sort By"]
											   delegate:self 
									  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
								 destructiveButtonTitle:nil
									  otherButtonTitles:  [Localizer getLocalizedText:@"Preferred Vendors"]
														, [Localizer getLocalizedText:@"Vendor Names"]
														, [Localizer getLocalizedText:@"Price"]
														, nil];
	
	if([UIDevice isPad])
		[self.actionPopOver showFromBarButtonItem:sender animated:YES];
	else 
	{
		self.actionPopOver.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
		[self.actionPopOver showFromToolbar:[ConcurMobileAppDelegate getBaseNavigationController].toolbar];
	}
}


#pragma mark -
#pragma mark UIActionSheetDelegate Methods

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if (buttonIndex == 0)
	{
		self.sortOrder = SORT_BY_PREFERRED_VENDORS;
	}
	if (buttonIndex == 1)
	{
		self.sortOrder = SORT_BY_VENDOR_NAMES;
	} 
	else if (buttonIndex == 2)
	{
		self.sortOrder = SORT_BY_PRICE;
	}
	
	[self sortCars];
}

- (void)sortCars 
{
	if (self.carsInOriginalOrder == nil)
		return;
	
	NSMutableArray *cars = [[NSMutableArray alloc] initWithArray:self.carsInOriginalOrder copyItems:FALSE];
	BOOL combineAllVendors = NO;
	
	if (self.sortOrder == SORT_BY_PREFERRED_VENDORS)
	{
		// No op.  The carsInOriginalOrder is already in the perferred vendors order.
	}
	if (self.sortOrder == SORT_BY_VENDOR_NAMES)
	{
		[self sortCarsByPrice:cars];		// Secondary sort
		[self sortCarsByVendorNames:cars];	// Primary sort
	} 
	else if (self.sortOrder == SORT_BY_PRICE)
	{
		[self sortCarsByVendorNames:cars];	// Secondary sort
		[self sortCarsByPrice:cars];		// Primary sort
		combineAllVendors = YES;
	}
	
	[self populateVendors:cars combineAllVendors:combineAllVendors];
	
	[tblView reloadData];
    [self makeToolbar];
}


#pragma mark -
#pragma mark Sorting Methods
		 
NSInteger compareCarsByVendorNames(id car1, id car2, void* context)
{
	return [((Car*)car1).chainName caseInsensitiveCompare:((Car*)car2).chainName];
}

- (void)sortCarsByVendorNames:(NSMutableArray *)cars
{
	[cars sortUsingFunction:compareCarsByVendorNames context:nil];
}

NSInteger compareCarsByPrice(id car1, id car2, void* context)
{
	double p1 = ((Car*)car1).dailyRate;
	double p2 = ((Car*)car2).dailyRate;
	return (p1 < p2 ? NSOrderedAscending : (p1 > p2 ? NSOrderedDescending : NSOrderedSame));
}

- (void)sortCarsByPrice:(NSMutableArray *)cars
{
	[cars sortUsingFunction:compareCarsByPrice context:nil];
}

- (void)populateVendors:(NSArray*)cars combineAllVendors:(BOOL)combineAllVendors
{
	self.vendors = [[NSMutableArray alloc] init];
	
	if (combineAllVendors)
	{
		NSMutableArray *copyOfCars = [[NSMutableArray alloc] initWithArray:cars copyItems:NO];

		CarVendorDescriptor *combinedVendors = [[CarVendorDescriptor alloc] init];
		combinedVendors.vendorName = nil;
		combinedVendors.cars = copyOfCars;
		
		[self.vendors addObject:combinedVendors];
		
	}
	else
	{
		CarVendorDescriptor *mostRecentVendor = nil;
		for (Car *car in cars)
		{
			NSString *carVendorName = car.chainName;
			
			if (mostRecentVendor != nil && [carVendorName isEqualToString:mostRecentVendor.vendorName])
			{
				[mostRecentVendor.cars addObject:car];
			}
			else
			{
				mostRecentVendor = [[CarVendorDescriptor alloc] init];
				
				mostRecentVendor.vendorName = carVendorName;
				mostRecentVendor.cars = [[NSMutableArray alloc] initWithObjects:car, nil];
				
				[self.vendors addObject:mostRecentVendor];
			}
		}
	}
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}


@end
