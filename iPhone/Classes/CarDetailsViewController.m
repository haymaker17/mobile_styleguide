//
//  CarDetailsViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CarDetailsViewController.h"
#import "ExSystem.h" 

#import "CarListViewController.h"
#import "CarMapViewController.h"
#import "CarDetailCell.h"
#import "CarSearchCriteria.h"
#import "Car.h"
#import "CarLocation.h"
#import "UserConfig.h"
#import "DownloadUserConfig.h"
#import "CreditCard.h"
#import "CarBookingTripData.h"
#import "CarReservationRequest.h"
#import "CarReservationResponse.h"
#import "ReserveCar.h"
#import "DateTimeFormatter.h"
#import "CarImageCell.h"
#import "BookingChainCell.h"
#import "ItinDetailsCellLabel.h"
#import "ItinDetailsCellInfo.h"
#import "FormatUtils.h"
#import "ImageCache.h"
#import "LabelConstants.h"
#import "HotelDetailsVariableHeightCell.h"
#import "ViolationReason.h"
#import "HotelViolation.h"
#import "SystemConfig.h"
#import "HotelCreditCardViewController.h"
#import "MobileAlertView.h"
#import "iPadHomeVC.h"
#import "DetailViewController.h"
#import "HotelOptionsViewController.h"
#import "HotelTextEditorViewController.h"
#import "ViolationDetailsVC.h"
#import "PolicyViolationConstants.h"
#import "CachedImageView.h"
#import "CarResult.h"
#import "PreSellOptions.h"
//Custom Fields
#import "BoolEditCell.h"
#import "CustomFieldTextEditor.h"
#import "FieldOptionsViewController.h"
#import "TravelViolationReasons.h"
#import "GovTAField.h"
//#define kSectionViolation 200
#define kAlertGropuAuthUsed 300200
#define kSectionRuleMessages 1000
#define kSectionCC 999
#define kFlightPosAffinity 997
#define kSectionTripFields 1001
#define kSECTION_TRAVEL_CUSTOM_FIELDS 1

@interface CarDetailsViewController (Private)
-(void) fetchCustomFields;
-(void) completeReservation;
-(BOOL) hasDisallowedViolations;

@end

@implementation CarDetailsViewController

#define kSectionCreditCard @"CreditCardSection"
#define kSectionMajorDetails @"MajorDetailsSection"
#define kSectionCarDetails @"CarDetailsSection"
#define kSectionViolation @"ViolationSection"

// Rows belonging to credit card section
#define kRowCreditCard 0
#define kRowTotalCost 1

// Rows belonging to the kSectionMajorDetails section
#define kRowChainName 0
#define kRowCarDescription 1
#define kRowPickup 60
#define kRowDropoff 61
#define kRowChainPhone 0
#define kPickupLocation 1
#define kVendorHours 2

//kSectionCarDetails
#define kRowCarType 0
#define kRowCarDailyRate 1
#define kRowCarTotalRate 2

// Rows beloning to the kSectionViolation section
#define kViolationDescriptionRow 0
#define kViolationReasonRow 1
#define kViolationJustificationRow 2

#define kAlertAreYouSure			138011
#define kAlertReservationSucceeded	138012
#define kAlertReservationFailed		138013
#define kAlertUnhandledViolations	138014

#pragma mark -
#pragma mark MobileViewController Methods

-(NSString *)getViewIDKey
{
	return CAR_DETAILS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

-(void)switchToTripDetailView:(NSString*) itinLocator
{
    EntityTrip *trip = nil;
    NSString *tripKey = nil;
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SKIP_PARSE", nil];

    if (self.carBookingTripData.tripKey != nil)
    {
        tripKey = self.carBookingTripData.tripKey;
        trip = [[TripManager sharedInstance] fetchByTripKey:tripKey];//[tripsData.trips objectForKey:tripKey];
        
        // The trip view is already on the view stack, so just pop until we get back to it.
        pBag[@"POPUNTILVIEW"] = @"YES";
        pBag[@"DONTPUSHVIEW"] = @"YES";
        
        if([UIDevice isPad])
        {
            //ok, we need to actually refresh the trips view, because we came here from a trip
            iPadHomeVC *padHome = [ConcurMobileAppDelegate findiPadHomeVC];
            DetailViewController *dvc = (padHome.homeViews)[@"Trip"];
            if (dvc != nil && [dvc isKindOfClass:[DetailViewController class]])
            {
                [dvc displayTrip:trip TripKey:tripKey];
            }
            else
                [padHome popHome:self];
            
            [self dismissViewControllerAnimated:YES completion:nil];
        }
    }
    else
    {
        trip = [[TripManager sharedInstance] fetchByItinLocator:itinLocator];
        tripKey = trip.tripKey;
        
        // We came here from the home screen, so pop all the way back to it before going to the trip view.
        pBag[@"POP_TO_ROOT_VIEW"] = @"YES";
    }
    
    if (trip != nil && tripKey != nil)
    {
        pBag[@"TRIP"] = trip;
        pBag[@"TRIP_KEY"] = tripKey;

        UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
        if ([homeVC respondsToSelector:@selector(refreshTripsData)])
        {
            [homeVC performSelector:@selector(refreshTripsData) withObject:nil];
        }
        
        if([UIDevice isPad])
        {
            UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
            if ([homeVC.navigationController.topViewController isKindOfClass:[DetailViewController class]])
                [homeVC.navigationController popViewControllerAnimated:NO];
            
            // Create a new trip detail view
            DetailViewController *newDetailViewController = [[DetailViewController alloc] initWithNibName:@"BaseDetailVC_iPad" bundle:nil];
            [newDetailViewController.ivLogo setHidden:YES];
            
            UINavigationController *homeNavigationController = homeVC.navigationController;
            [homeNavigationController pushViewController:newDetailViewController animated:YES];
            
            [newDetailViewController displayTrip:trip TripKey:trip.tripKey];
            
            // TODO: Call this
            //[newDetailViewController updateViews]; // See ReportDetailViewController_iPad for an example of how to implement this method
            [self dismissViewControllerAnimated:YES completion:nil];
        }
        else
        {
            [ConcurMobileAppDelegate switchToView:TRIP_DETAILS viewFrom:CAR_DETAILS ParameterBag:pBag];
        }
    }
    else
    {
        if(![UIDevice isPad])
        {
            NSMutableDictionary* homePageParameterBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"POP_TO_ROOT_VIEW", @"YES", @"DONTPUSHVIEW", nil];
            
            [ConcurMobileAppDelegate switchToView:HOME_PAGE viewFrom:[self getViewIDKey] ParameterBag:homePageParameterBag];
        }
        else 
        {
            iPadHomeVC *padHome = [ConcurMobileAppDelegate findiPadHomeVC];
            [padHome refreshTripData];
            [self dismissViewControllerAnimated:YES completion:nil];
        }
    }
}

-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		if ((msg.parameterBag)[@"CAR"] != nil)
		{
			self.car = (Car *)(msg.parameterBag)[@"CAR"];
            if (self.car.sendCreditCard) // Pre-sell for car only contains CC details -- Change this when car has loyalty programs data
                [self fetchPreSellOptions];
            
			if ((msg.parameterBag)[@"CAR_BOOKING_TRIP_DATA"] != nil)
			{
				self.carBookingTripData = (CarBookingTripData*)(msg.parameterBag)[@"CAR_BOOKING_TRIP_DATA"];
			}
			
            if ((msg.parameterBag)[@"CAR_SEARCH_CRITERIA"] != nil)
                self.carSearchCriteria = (msg.parameterBag)[@"CAR_SEARCH_CRITERIA"];
            
			// If a credit card has not already been selected and there is at least one card to choose from,
			// then select the first card.
			if (self.creditCardIndex == nil && [self.creditCards count])
			{
                self.creditCardIndex = @0;
			}
			
            [self populateSections];
            
            [self updateViolationReasons];
//			[tableList reloadData]; function called later
			
//			[self configureReserveButton];
            
            [self updateHeader];
		}	
		else if ((msg.parameterBag)[@"CREDIT_CARD_INDEX"] != nil)
		{
			self.creditCardIndex = (NSNumber*)(msg.parameterBag)[@"CREDIT_CARD_INDEX"];
		}
		else if ((msg.parameterBag)[@"OPTION_TYPE_ID"] != nil)
		{
			// We've returned from the HotelOptionsViewController
			NSNumber* selectedRowIndexNumber = (NSNumber*)(msg.parameterBag)[@"SELECTED_ROW_INDEX"];
			NSUInteger selectedRowIndex = [selectedRowIndexNumber intValue];
			ViolationReason *reason = self.violationReasons[selectedRowIndex];
			self.violationReasonCode = reason.code;
		}
		else if ((msg.parameterBag)[@"TEXT"] != nil)
		{
			self.violationJustification = (NSString*)(msg.parameterBag)[@"TEXT"];
		}
		
		[self.tableList reloadData];
		
		[self configureReserveButton];
	}
    else if ([msg.idKey isEqualToString:PRE_SELL_OPTIONS])
    {
        self.isPreSellOptionsLoaded = YES;
        PreSellOptions *preSellOptions = (PreSellOptions *)msg.responder;
        self.creditCards = preSellOptions.creditCards;
        
        if (self.creditCardIndex == nil && [self.creditCards count])
            self.creditCardIndex = @0;
        [self.tableList reloadData];
        [self configureReserveButton];
        if (!preSellOptions.isRequestSuccessful) {
            MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"Error" localize]
                                                                    message:[@"An error has occurred retrieving sell information fields. Reservation cannot be done at this time. Please try later." localize]
                                                                   delegate:nil
                                                          cancelButtonTitle:[LABEL_CLOSE_BTN localize]
                                                          otherButtonTitles:nil];
            [alert show];
        }
    }
    else if ([msg.idKey isEqualToString:DOWNLOAD_TRAVEL_CUSTOMFIELDS])
    {
        if ([self isViewLoaded]) {
            self.navigationItem.rightBarButtonItem.enabled = YES;
            [self hideLoadingView];
        }
        
        if (msg.errBody == nil && msg.responseCode == 200) 
        {
            [self.aSections removeObject:@"TRIP_FIELDS"]; // removes the instance if any
            [self.aSections addObject:@"TRIP_FIELDS"];
            self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAllFieldsAtStart:NO];
            [self.dictSections removeObjectForKey:@"TRIP_FIELDS"];
            self.dictSections[@"TRIP_FIELDS"] = self.tcfRows;
            [self.tableList reloadData];
        }
    }
	else if ([msg.idKey isEqualToString:RESERVE_CAR])
	{
		ReserveCar *reserveCar = (ReserveCar *)msg.responder;
		[self showCarReservationResponse:reserveCar.carReservationResponse];
		//[self hideWaitView];
	}
    else if ([msg.idKey isEqualToString:TRIPS_DATA] && (msg.parameterBag)[@"ITIN_LOCATOR"])
	{
        [self hideWaitView];
        
        if (self.carBookingTripData.tripKey != nil && ![UIDevice isPad])
        {
            // MOB-9566 refresh TripsData for both TripDetails and Trips view.
            [TripsViewController refreshViewsWithTripsData:msg fromView:self];
            [self switchToTripDetailView:nil];
        }
        else
        {
            NSString * itinLocator = nil;
            if (self.carBookingTripData.tripKey == nil)
                itinLocator = (NSString*)(msg.parameterBag)[@"ITIN_LOCATOR"];
            [self switchToTripDetailView:itinLocator];
        }
    }
	else if ([msg.idKey isEqualToString:TRIPS_DATA])
    {
        NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.carRezResponse.recordLocator, @"RECORD_LOCATOR",[self getViewIDKey], @"TO_VIEW",self.carRezResponse.itinLocator,@"ITIN_LOCATOR", nil];
        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

#pragma mark -
#pragma mark View lifecycle
- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    if (self.isDirty)
    {
        if (self.editedDependentCustomField && [self.selectedCustomField.attributeValue length])
            [self updateDynamicCustomFields];
        else
            [self reloadCustomFieldsSection];
    
        self.isDirty = NO;// MOB-9648 hold off reserve action

    }
}

-(void)populateSections
{
    self.aSections = [[NSMutableArray alloc] initWithObjects: kSectionCreditCard, kSectionMajorDetails, kSectionCarDetails, nil];
    self.dictSections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    
    if([self getViolationsCount])
    {
        [self.aSections addObject:kSectionViolation];
    }

    //Custom Fields
    if (!self.hideCustomFields)
    {
        // prepopulate the custom fields from cache
        [self.aSections addObject:@"TRIP_FIELDS"];
        self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAllFieldsAtStart:NO];
        self.dictSections[@"TRIP_FIELDS"] = self.tcfRows;
    }
    //MOB-10431
    if (!self.car.sendCreditCard)
    {
        [self.aSections removeObject:kSectionCreditCard];
    }
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	self.imageCache = [[ImageCache alloc] init];
    
	self.updatingItineraryLabel.text = [Localizer getLocalizedText:@"Updating Itinerary"];

	//Show page title on iphone
	//if([UIDevice isPad])
	self.title = [Localizer getLocalizedText:@"CAR_DETAILS"];
    
    if (self.aSections == nil)
        self.aSections = [[NSMutableArray alloc] init];
    
    if (self.dictSections == nil)
        self.dictSections = [[NSMutableDictionary alloc] init];

    //Custom Fields
    if (!self.hideCustomFields)
        [self fetchCustomFields];
    else 
        [self hideLoadingView];
    
    [self.tableList reloadData];
}

-(void)updateViolationReasons
{
    if (self.violationReasons == nil || [self.violationReasons count] == 0)
	{
		NSMutableArray *reasons = [[NSMutableArray alloc] init];
		NSMutableArray *labels = [[NSMutableArray alloc] init];
        
        TravelViolationReasons *travelViolationReasons = [TravelViolationReasons getSingleton];
        if (travelViolationReasons != nil && [travelViolationReasons.violationReasons count] > 0) {
            // car violations are the same as hotel violations
            NSArray *hotelViolations = self.car.violations;
            NSMutableArray *violationTypes = [[NSMutableArray alloc] initWithObjects:nil];
            
            for (HotelViolation *hotelViolation in hotelViolations) {
                [violationTypes addObject:hotelViolation.violationType];
            }
            
            NSMutableArray *tmpReasons = [travelViolationReasons getReasonsFor:violationTypes];
            for (ViolationReason *reason in tmpReasons) {
                [reasons addObject:reason];
                [labels addObject:reason.description];
            }
        }
        
        /*
         SystemConfig *systemConfig = [SystemConfig getSingleton];
         if (systemConfig != nil && [systemConfig.carViolationReasons count] > 0)
         {
         NSArray* allKeys = [systemConfig.carViolationReasons allKeys];
         for (NSString *key in allKeys)
         {
         ViolationReason	*reason = [systemConfig.carViolationReasons objectForKey:key];
         [reasons addObject:reason];
         [labels addObject:reason.description];
         }
         }
         */
		
		self.violationReasons = reasons;
		self.violationReasonLabels = labels;
		
	}
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	
	[self configureReserveButton];

	// No reason to show the wait view initially
	[self hideWaitView];
	
	// No reason to show the updating-itinerary-view initially
	[self.updatingItineraryView setHidden:YES];
	
	if([UIDevice isPad])
	{
		//waitView.frame = CGRectMake(0, 0, 540, 620 - 88);
		self.updatingItineraryView.frame = CGRectMake(0, 0, 540, 620 - 88);
	}
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	if (self.car == nil)
		return 0;

    return [self.aSections count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString *sectionName = self.aSections[section];
    
	if ([sectionName isEqualToString:kSectionCreditCard])
		return (self.car.totalRate == 0 ? 1 : 2);	// Credit card row and total cost row (if we have it)
    else if([sectionName isEqualToString:@"TRIP_FIELDS"])
    {
        NSArray *a = self.dictSections[sectionName];
        return [a count];
    }
	else if ([sectionName isEqualToString:kSectionMajorDetails])
		return (([self.car.carPickupLocation.moOpen length]+[self.car.carPickupLocation.tuOpen length]+[self.car.carPickupLocation.weOpen length]+[self.car.carPickupLocation.thOpen length]+[self.car.carPickupLocation.frOpen length]+[self.car.carPickupLocation.saOpen length]+[self.car.carPickupLocation.suOpen length] > 0) ? 3 : 2); //Only show the vendor hours cell if some opening hours have been specified
    else if ([sectionName isEqualToString:kSectionViolation])
		return [self hasDisallowedViolations] ? 3 : 1; // Rows: violation description, justification, and comment
    else if ([sectionName isEqualToString:kSectionCarDetails])
        return (self.car.totalRate == 0 ? 2 : 3);

	else
		return 0;
}

// Find or create a cell for display.
// This code was extracted from cellForRowAtIndexPath and was being repeated multiple times
- (HotelBookingCell *)getHotelBookingCell:(UITableView *)tableView
{
    // See if we can reuse a cell which is no longer in view
    HotelBookingCell *cell = (HotelBookingCell*)[tableView dequeueReusableCellWithIdentifier:@"HotelBookingSingleCell"];
    if (cell == nil)
    {
        // All available cells are already in view, so create a new one
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelBookingSingleCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[HotelBookingCell class]])
                cell = (HotelBookingCell *)oneObject;
    }
    if (cell != nil)
    {
        // We have a cell, now default the number of lines and cell height
        // just incase this cell was formally used by vendor hours
        // (which uses more lines and height)
        cell.lblValue.numberOfLines = 1;
        CGRect rect = cell.lblValue.frame;
        rect.size.height = 19;
        cell.lblValue.frame = rect;

        // default the cell color
        [cell.lblValue setTextColor:[UIColor blackColor]];
    }
    return cell;
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	
    NSString *sectionName = self.aSections[indexPath.section];
    if ([sectionName isEqualToString:@"TRIP_FIELDS"])
    {
        return [self configureCustomFieldCellAtIndexPath:indexPath];
    }
    else if ([sectionName isEqualToString:kSectionCreditCard])
	{
		if (kRowCreditCard == row)
		{
			NSString *cellValue = [self getCreditCard];
            HotelBookingCell *cell = [self getHotelBookingCell:tableView];
            
            cell.lblLabel.text = [Localizer getLocalizedText:@"Card"];
            cell.lblValue.text = cellValue;
            [cell setAccessoryType:([self canChooseCreditCard] ? UITableViewCellAccessoryDisclosureIndicator : UITableViewCellAccessoryNone)];
			
			return cell;
		}
		else if (kRowTotalCost == row)
		{
			NSString *total = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", self.car.totalRate] crnCode:self.car.currencyCode];
//			UITableViewCell *cell = [ItinDetailsCellLabel makeCell:tableView cellLabel:[Localizer getLocalizedText:@"Total"] cellValue:total];
//			return cell;
            
            HotelBookingCell *cell = [self getHotelBookingCell:tableView];
            
            cell.lblLabel.text = [Localizer getLocalizedText:@"Total"];
            cell.lblValue.text = total;
            [cell setAccessoryType:UITableViewCellAccessoryNone];
			
			return cell;
		}
	}
    else if ([sectionName isEqualToString:kSectionCarDetails])
    {
        HotelBookingCell *cell = [self getHotelBookingCell:tableView];
        [cell setAccessoryType:UITableViewCellAccessoryNone];
        
        NSString *lbl = nil;
        NSString *val = nil;
        
        NSString *carBody = [NSString stringWithFormat:@"%@ %@", self.car.carClass, self.car.carBody];
        NSString *cost = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", self.car.dailyRate] crnCode:self.car.currencyCode];
        NSString *costPerDay = [NSString stringWithFormat:[Localizer getLocalizedText:@"%@ per day"], cost];
        NSString* description = [NSString stringWithFormat:@"%@, %@, %@", carBody, self.car.carAC, self.car.carTrans];
        NSString *total = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", self.car.totalRate] crnCode:self.car.currencyCode];
        
        [cell.lblValue setTextColor:[UIColor blackColor]];
        if(row == kRowCarType)
        {
            lbl = [Localizer getLocalizedText:@"Car Type"];
            val = description;
            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
        }
        else if(row == kRowCarDailyRate)
        {
            lbl = [Localizer getLocalizedText:@"Daily Rate"];
            val = costPerDay;
            
            if ([self.car maxEnforcementLevel] != nil)
            {
                int eLevel = [[self.car maxEnforcementLevel] intValue];
                if(eLevel < kViolationLogForReportsOnly || eLevel == 100)
                {
                    [cell.lblValue setTextColor:[UIColor bookingGreenColor]];
                }
                else if(eLevel >= kViolationLogForReportsOnly && eLevel <= kViolationNotifyManager)
                {
                    [cell.lblValue setTextColor:[UIColor bookingYellowColor]];
                }    
                else if(eLevel >= kViolationRequiresPassiveApproval && eLevel <= kViolationRequiresApproval)
                {
                    [cell.lblValue setTextColor:[UIColor bookingRedColor]];
                } 
                else if(eLevel == kViolationAutoFail)
                {
                    [cell.lblValue setTextColor:[UIColor bookingGrayColor]];
                } 
                else
                {
                    [cell.lblValue setTextColor:[UIColor bookingRedColor]];
                }
            }
            
        }
        else if(row == kRowCarTotalRate)
        {
            lbl = [Localizer getLocalizedText:@"Total Rate"];
            val = total;
        }

        cell.lblValue.text = val;
        cell.lblLabel.text = lbl;
        
        //[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
        return cell;
    }
	else if ([sectionName isEqualToString:kSectionMajorDetails])
	{
//		if (kRowChainName == row)
//		{
//			BookingChainCell *cell = [BookingChainCell makeCell:tableView vc:self chainName:car.chainName logoImageUri:car.chainLogoUri];
//			return cell;
//		}
//		else 
//        if (kRowCarDescription == row)
//		{
//			NSString *carBody = [NSString stringWithFormat:@"%@ %@", car.carClass, car.carBody];
//			NSString *cost = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", car.dailyRate] crnCode:car.currencyCode];
//			NSString *costPerDay = [NSString stringWithFormat:[Localizer getLocalizedText:@"%@ per day"], cost];
//			NSString* description = [NSString stringWithFormat:@"%@\n%@\n%@\n%@", carBody, car.carAC, car.carTrans, costPerDay];
//			CarDetailCell *cell = [CarDetailCell makeCell:tableView owner:self description:description imageUri:car.imageUri imageCache:imageCache];
//			return cell;
//		}
//		else 
        if (kRowPickup == row || kRowDropoff == row)
		{
			NSString *cellName;
			NSString *cellValue;
			
			if (kRowPickup == row)
			{
				cellName = [Localizer getLocalizedText:@"Pickup"];
				cellValue = [DateTimeFormatter formatHotelOrCarDateForBooking: self.car.pickupDate inTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
			}
			else // kRowDropoff
			{
				cellName = [Localizer getLocalizedText:@"Return"];
				cellValue = [DateTimeFormatter formatHotelOrCarDateForBooking: self.car.dropoffDate inTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
			}

            HotelBookingCell *cell = [self getHotelBookingCell:tableView];
            cell.lblLabel.text = cellName;
            cell.lblValue.text = cellValue;
            [cell setAccessoryType:UITableViewCellAccessoryNone];
			return cell;
		}
		else if (kRowChainPhone == row)
		{
			NSString *phoneNumber = (self.car.pickupLocationPhoneNumber == nil ? @"" : self.car.pickupLocationPhoneNumber);
			//ItinDetailsCellLabel* cell = [ItinDetailsCellLabel makePhoneCell:tableView phoneNumber:phoneNumber];
            //NSLog(@"phoneNumber pickuplocation %@", phoneNumber);
            HotelBookingCell *cell = [self getHotelBookingCell:tableView];
            cell.lblValue.text = phoneNumber;
            cell.lblLabel.text = [Localizer getLocalizedText:@"Phone"];
            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
			return cell;
		}
		else if (kPickupLocation == row)
		{
			//ItinDetailsCellLabel* cell = [ItinDetailsCellLabel makeLocationCell:tableView location:car.pickupLocationName];
            
            HotelBookingCell *cell = [self getHotelBookingCell:tableView];
            cell.lblValue.text = self.car.pickupLocationName;
            cell.lblLabel.text = [Localizer getLocalizedText:@"Map Address"];
            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
            
			return cell;
		}
        else if (kVendorHours == row)
        {
            HotelBookingCell *cell = [self getHotelBookingCell:tableView];
            // Build the vendor hours string
            NSString *vendorHours = @"";
            vendorHours = [NSString stringWithFormat:@"%@%@\r", vendorHours, [self textForVendorDayHours:[Localizer getLocalizedText:@"Monday_Short"] openingTime:self.car.carPickupLocation.moOpen closingTime:self.car.carPickupLocation.moClose]];
            vendorHours = [NSString stringWithFormat:@"%@%@\r", vendorHours, [self textForVendorDayHours:[Localizer getLocalizedText:@"Tuesday_Short"] openingTime:self.car.carPickupLocation.tuOpen closingTime:self.car.carPickupLocation.tuClose]];
            vendorHours = [NSString stringWithFormat:@"%@%@\r", vendorHours, [self textForVendorDayHours:[Localizer getLocalizedText:@"Wednesday_Short"] openingTime:self.car.carPickupLocation.weOpen closingTime:self.car.carPickupLocation.weClose]];
            vendorHours = [NSString stringWithFormat:@"%@%@\r", vendorHours, [self textForVendorDayHours:[Localizer getLocalizedText:@"Thursday_Short"] openingTime:self.car.carPickupLocation.thOpen closingTime:self.car.carPickupLocation.thClose]];
            vendorHours = [NSString stringWithFormat:@"%@%@\r", vendorHours, [self textForVendorDayHours:[Localizer getLocalizedText:@"Friday_Short"] openingTime:self.car.carPickupLocation.frOpen closingTime:self.car.carPickupLocation.frClose]];
            vendorHours = [NSString stringWithFormat:@"%@%@\r", vendorHours, [self textForVendorDayHours:[Localizer getLocalizedText:@"Saturday_Short"] openingTime:self.car.carPickupLocation.saOpen closingTime:self.car.carPickupLocation.saClose]];
            vendorHours = [NSString stringWithFormat:@"%@%@", vendorHours, [self textForVendorDayHours:[Localizer getLocalizedText:@"Sunday_Short"] openingTime:self.car.carPickupLocation.suOpen closingTime:self.car.carPickupLocation.suClose]];
            
            // extend the cell size to hold the text
            cell.lblValue.numberOfLines = 8;
            CGRect rect = cell.lblValue.frame;
            rect.size.height = 158;
            cell.lblValue.frame = rect;
            cell.lblValue.text = vendorHours;

            cell.lblLabel.text = [Localizer getLocalizedText:@"Vendor Hours"];
            [cell setAccessoryType:UITableViewCellAccessoryNone];
            cell.selectionStyle = UITableViewCellSelectionStyleNone;
            
			return cell;
        }
	}
    else if ([sectionName isEqualToString:kSectionViolation])
	{
        HotelBookingCell *cell = [self getHotelBookingCell:tableView];
        
        cell.lblValue.textColor = [UIColor blackColor];
        
		if (kViolationDescriptionRow == row)
		{
            NSString *label = [self hasDisallowedViolations] ? [@"Violation" localize] : [@"Travel Policy" localize];;
			NSString *value = [self getViolations];
            
            cell.lblLabel.text = label;
            cell.lblValue.text = value;
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
		}
		else if (kViolationReasonRow == row)
		{
            NSString *reason = [self getViolationReason];
			
			NSString *label = [Localizer getLocalizedText:@"Violation Reason"];
			NSString *value = (reason != nil ? reason : [Localizer getLocalizedText:@"Please specify"]);
            
            cell.lblLabel.text = label;
            cell.lblValue.text = value;
            
			if (reason == nil)
				cell.lblValue.textColor = [UIColor redColor];
			
			cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
		}
		else if (kViolationJustificationRow == row)
		{
            NSString *justification  = [self getViolationJustification];
			
			NSString *label = [Localizer getLocalizedText:@"Violation Justification"];
			NSString *value = ([justification length] ? justification : [Localizer getLocalizedText:@"Please specify"]);
            
            cell.lblLabel.text = label;
            cell.lblValue.text = value;
            
            //MOB-10484
            if (![justification length] && [SystemConfig getSingleton].ruleViolationExplanationRequired )
				cell.lblValue.textColor = [UIColor redColor];
			
			cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
		}
        
        return cell;
	}
	
    return nil;
}

/**
 * Returns a formatted string of the opening hours for a given day
 */
-(NSString *)textForVendorDayHours:(NSString *)dayOfWeek openingTime:(NSString *)openingTime closingTime:(NSString *)closingTime
{
    NSString *textForDay = @"";
    if ([openingTime length])
    {
        if ([openingTime isEqualToString:closingTime])
        {
            // If the opening and closing times match, then output "all-day"
            textForDay = [NSString stringWithFormat:@"%@: %@", dayOfWeek, [Localizer getLocalizedText:@"All day"]];
        }
        else
        {
            // Calculate the time difference between opening and closing
            NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
            [formatter setDateFormat:@"HH:mm"];
            NSDate *dateOpen = [formatter dateFromString:openingTime];
            NSDate *dateClose = [formatter dateFromString:closingTime];
            NSCalendar *calendar = [NSCalendar currentCalendar];
            NSDateComponents *components = [calendar components:(NSHourCalendarUnit | NSMinuteCalendarUnit) fromDate:dateOpen toDate:dateClose options:0];
            NSInteger hour = [components hour];
            NSInteger minute = [components minute];
            if (hour == 23 && minute > 30)
            {
                // If the time difference is pretty close to all-day (over 23.5hrs), output "all-day"
                textForDay = [NSString stringWithFormat:@"%@: %@", dayOfWeek, [Localizer getLocalizedText:@"All day"]];
            }
            else
            {
                // Output the opening hours range
                textForDay = [NSString stringWithFormat:@"%@: %@-%@", dayOfWeek, openingTime, closingTime];
            }
        }
    }
    else
    {
        // No opening hour specified, so output "closed"
        textForDay = [NSString stringWithFormat:@"%@: %@", dayOfWeek, [Localizer getLocalizedText:@"Closed"]];
    }
    return textForDay;
}
#pragma mark -
#pragma mark Table view delegate

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	
    NSString *sectionName = self.aSections[indexPath.section];
    if ([sectionName isEqualToString:@"TRIP_FIELDS"])
        return 65;
    else if ([sectionName isEqualToString:kSectionMajorDetails] && kRowCarDescription == row)
	{
		return 55;
	}
    else if ([sectionName isEqualToString:kSectionMajorDetails] && kVendorHours == row)
	{
		return 195;
	}
    else if ([sectionName isEqualToString:kSectionViolation])
	{
        return 60;
	}

    return 55;

}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
	NSUInteger row = [indexPath row];
    NSString *sectionName = self.aSections[indexPath.section];
    if ([sectionName isEqualToString:@"TRIP_FIELDS"])
    {
        self.isDirty = YES;
        NSArray *a = self.dictSections[sectionName];
        EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)a[indexPath.row];  //[tcfRows objectAtIndex:[newIndexPath row]];
        
        if ([tcf.hasDependency boolValue])
        {
            self.selectedCustomField = tcf;
            self.editedDependentCustomField = YES;
        }
        else
        {
            self.selectedCustomField = nil;
            self.editedDependentCustomField = NO;            
        }
        
        if (tcf.relAttribute != nil && [tcf.relAttribute count] > 0) 
        {
            // Text Options
            NSArray *tcfAttributes = (NSArray *)[tcf.relAttribute allObjects];
            if (tcfAttributes != nil && [tcfAttributes count] > 0) 
            {                    
                FieldOptionsViewController *nextController = [[FieldOptionsViewController alloc] initWithNibName:@"HotelOptionsViewController" bundle:nil];
                nextController.title = [@"Please specify" localize];
                nextController.tcf = tcf;
                [self.navigationController pushViewController:nextController animated:YES];
            }
        }
        else
            [self onSelectLongTextOrNumericFieldCellAtIndexPath:indexPath];
    }
    else if ([sectionName isEqualToString:kSectionCreditCard])
	{
		if (kRowCreditCard == row)
		{
			if ([self canChooseCreditCard])
			{
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:CAR_DETAILS, @"FROM_VIEW", @"YES", @"SHORT_CIRCUIT", self.creditCards, @"CREDIT_CARDS", nil];
				
				if (self.creditCardIndex != nil)
					[pBag setValue:self.creditCardIndex forKey:@"CREDIT_CARD_INDEX"];
				
				if([UIDevice isPad])
				{
					HotelCreditCardViewController *nextController = [[HotelCreditCardViewController alloc] initWithNibName:@"HotelCreditCardViewController" bundle:nil];

					Msg *msg = [[Msg alloc] init];
					msg.parameterBag = pBag;
					msg.idKey = @"SHORT_CIRCUIT";
					[nextController respondToFoundData:msg];
					[self.navigationController pushViewController:nextController animated:YES];
				}
				else 
					[ConcurMobileAppDelegate switchToView:HOTEL_CREDIT_CARD viewFrom:CAR_DETAILS ParameterBag:pBag];

			}
		}
	}
    else if ([sectionName isEqualToString:kSectionCarDetails])
	{
        if(kRowCarType == row)
        {
            NSString *carBody = [NSString stringWithFormat:@"%@ %@", self.car.carClass, self.car.carBody];
            NSString* description = [NSString stringWithFormat:@"%@, %@, %@", carBody, self.car.carAC, self.car.carTrans];
            
            ViolationDetailsVC *vc = [[ViolationDetailsVC alloc] initWithNibName:@"ViolationDetailsVC" bundle:nil];
            vc.violationText = description;
            [self.navigationController pushViewController:vc animated:YES];
        }
    }
	else if ([sectionName isEqualToString:kSectionMajorDetails] == section)
	{
		if (kPickupLocation == row)
		{
            double pickupLatitude = self.car.pickupLocationLatitude;
            double pickupLongitude = self.car.pickupLocationLongitude;
            if(pickupLatitude == 0 || pickupLongitude == 0)
                return;
            
            NSLog(@"pickupLatitude=%.2f pickupLongitude=%.2f", pickupLatitude, pickupLongitude);
            
            // MOB-10654 
            MapViewController *vc = [[MapViewController alloc] init];
            vc.mapAddress = self.car.pickupLocationAddress;
            vc.anoTitle = [NSString stringWithFormat:@"%@ %@", self.car.chainName , self.car.pickupLocationName];
            vc.anoSubTitle = self.car.pickupLocationAddress;
            vc.lati = @(self.car.pickupLocationLatitude) ;
            vc.longi = @(self.car.pickupLocationLongitude) ;
            
            //MOB-10941
			if([UIDevice isPad])
			{
                [self.navigationController pushViewController:vc animated:YES];   
            }
            else
            {
                UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:vc];
                [self presentViewController:navi animated:YES completion:nil];
            }
            
		}
		else if (kRowChainPhone == row)
		{
			if (self.car.pickupLocationPhoneNumber != nil)
			{
				NSString *phoneNumber = self.car.pickupLocationPhoneNumber;
				NSString *digitsOnlyPhoneNumber = [[phoneNumber componentsSeparatedByCharactersInSet:[[NSCharacterSet characterSetWithCharactersInString:@"0123456789"] invertedSet]] componentsJoinedByString:@""];
				[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", digitsOnlyPhoneNumber]]];
			}
		}
	}
    else if ([sectionName isEqualToString:kSectionViolation])
	{
        if (kViolationDescriptionRow == row)
        {
            ViolationDetailsVC *vc = [[ViolationDetailsVC alloc] initWithNibName:@"ViolationDetailsVC" bundle:nil];
            vc.violationText = [self getViolations];
            [self.navigationController pushViewController:vc animated:YES];
        }
        else if (kViolationReasonRow == row)
		{
			NSString *optionsViewTitle = [Localizer getLocalizedText:@"Select Reason"];
			NSString *optionType = @"VIOLATION_REASON";
			NSArray *labels = self.violationReasonLabels;
			int currentReasonIndex = [self getIndexForViolationReasonCode:self.violationReasonCode];
			
			NSNumber *preferredFontSize = @13.0f;
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"FROM_VIEW", optionType, @"OPTION_TYPE_ID", optionsViewTitle, @"TITLE", labels, @"LABELS", preferredFontSize, @"PREFERRED_FONT_SIZE", @"YES", @"SHORT_CIRCUIT", nil];
			
			if (currentReasonIndex >= 0)
				pBag[@"SELECTED_ROW_INDEX"] = @(currentReasonIndex);
			
			if([UIDevice isPad])
			{
				HotelOptionsViewController *nextController = [[HotelOptionsViewController alloc] initWithNibName:@"HotelOptionsViewController" bundle:nil];

				Msg *msg = [[Msg alloc] init];
				msg.parameterBag = pBag;
				msg.idKey = @"SHORT_CIRCUIT";
				[nextController respondToFoundData:msg];
				[self.navigationController pushViewController:nextController animated:YES];
			}
			else 
				[ConcurMobileAppDelegate switchToView:HOTEL_OPTIONS viewFrom:[self getViewIDKey] ParameterBag:pBag];
		}
		else if (kViolationJustificationRow == row)
		{
			NSString *customTitle = [Localizer getLocalizedText:@"Violation Justification"];
			NSString *placeholder = [Localizer getLocalizedText:@"Please enter a justification for booking this car."];
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"FROM_VIEW", placeholder, @"PLACEHOLDER", customTitle, @"TITLE", @"YES", @"SHORT_CIRCUIT", nil];
			
			NSString *justification = self.violationJustification;
			if (justification != nil)
				pBag[@"TEXT"] = justification;
			
			if([UIDevice isPad])
			{
				HotelTextEditorViewController *nextController = [[HotelTextEditorViewController alloc] initWithNibName:@"HotelTextEditorViewController" bundle:nil];
				Msg *msg = [[Msg alloc] init];
				msg.parameterBag = pBag;
				msg.idKey = @"SHORT_CIRCUIT";
                nextController.title = customTitle;
				[nextController respondToFoundData:msg];
				[self.navigationController pushViewController:nextController animated:YES];
			}
			else 
				[ConcurMobileAppDelegate switchToView:HOTEL_TEXT_EDITOR viewFrom:[self getViewIDKey] ParameterBag:pBag];
		}
	}
}


#pragma mark -
#pragma mark Credit Card methods
-(NSString*)getCreditCard
{
	if (self.creditCards == nil || self.creditCardIndex == nil || [self.creditCardIndex integerValue] >= [self.creditCards count])
	{
		return self.isPreSellOptionsLoaded ? [Localizer getLocalizedText:@"Unavailable"] : [@"Loading..." localize];
	}
	else
	{
		CreditCard* card = self.creditCards[[self.creditCardIndex integerValue]];
		return card.name;
	}
}


-(BOOL)canChooseCreditCard
{
	return [self.creditCards count] > 1;
}


#pragma mark -
#pragma mark Violation methods
-(NSUInteger)getViolationsCount
{
	return [self.car.violations count];
}

// Checks if any Violations have enforcementLevel != kViolationAllow
-(BOOL) hasDisallowedViolations
{
    if ([self.car maxEnforcementLevel] != nil)
    {
        if ([[self.car maxEnforcementLevel] intValue] != kViolationAllow)
        {
            return YES;
        }
    }
    return NO;
}

-(NSString*)getViolations
{
	NSArray* violations = self.car.violations;
	
	NSMutableString *str = [[NSMutableString alloc] initWithString:@""];
	for (HotelViolation* violation in violations)
	{
		if ([str length] > 0)
			[str appendString:@"\n"];
		[str appendString:violation.message];
	}
	return str;
}

-(NSString*)getViolationReason
{
	NSString *reason = nil;
	
	if (self.violationReasonCode != nil)
	{
        TravelViolationReasons *travelViolationReasons = [TravelViolationReasons getSingleton];
		if (travelViolationReasons != nil)
		{
			ViolationReason *violationReason = (travelViolationReasons.violationReasons)[self.violationReasonCode];
			reason = violationReason.description;
		}
        
        /*
		SystemConfig *systemConfig = [SystemConfig getSingleton];
		if (systemConfig != nil)
		{
			ViolationReason *violationReason = [systemConfig.carViolationReasons objectForKey:violationReasonCode];
			reason = violationReason.description;
		}
        */
	}
	
	return reason;
}

-(NSString*)getViolationJustification
{
	return self.violationJustification;
}

-(int)getIndexForViolationReasonCode:(NSString*)reasonCode
{
	if (reasonCode != nil && self.violationReasons != nil)
	{
		for (int i = 0; i < [self.violationReasons count]; i++)
		{
			ViolationReason *reason = self.violationReasons[i];
			if (reason.code == reasonCode)
				return i;
		}
	}
	return -1;
}


#pragma mark -
#pragma mark Reservation methods

-(void)configureReserveButton
{
	self.navigationItem.rightBarButtonItem = nil;
	
	if (self.creditCardIndex != nil || (self.car && !self.car.sendCreditCard))
	{
		NSString* title = [Localizer getLocalizedText:@"Reserve"];
		UIBarButtonItem *reserveBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:title style:UIBarButtonItemStyleBordered target:self action:@selector(btnReserve:)];
		self.navigationItem.rightBarButtonItem = reserveBarButtonItem;
	}
}

-(IBAction)btnReserve:(id)sender
{
    if (self.isDirty && self.editedDependentCustomField)
    {
        // MOB-9648 Prevent preserve button, in between child screen and wait view
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[@"PENDING_REQUIRED_TRAVEL_CUSTOM_FIELDS" localize] delegate:nil cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles:nil];
        [alert show];
        
        return;
    }
    else if ([self hasPendingRequiredTripFields] && !self.hideCustomFields)
    {
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[@"PENDING_REQUIRED_TRAVEL_CUSTOM_FIELDS" localize] delegate:nil cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles:nil];
        [alert show];
        
        return;
    }  //MOB-10484
	else if ([self hasDisallowedViolations] == NO || ([self getViolationReason] != nil && ! (![[self getViolationJustification] length] && [SystemConfig getSingleton].ruleViolationExplanationRequired) ))
	{
        if ([Config isGov])
        {
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Please Confirm"]
                                  message:[Localizer getLocalizedText:@"Select 'OK' if you are sure you want to reserve this car."]
                                  delegate:self
                                  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                                  otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
            alert.tag = kAlertAreYouSure;
            [alert show];
        }
        else
        {
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Please Confirm"]
                                  message:[Localizer getLocalizedText:@"Are you sure you want to reserve this car?"]
                                  delegate:self
                                  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                                  otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
            alert.tag = kAlertAreYouSure;
            [alert show];
        }
	}
	else
	{
		NSString *msg;
		
		if ([self getViolationReason] == nil)
		{
            // MOB-10484 : check if justification is required
			if (![[self getViolationJustification] length]  && [SystemConfig getSingleton].ruleViolationExplanationRequired) // MOB-8069
			{
				msg = [Localizer getLocalizedText:@"Before reserving this car in"];
			}
			else
			{
				msg = [Localizer getLocalizedText:@"Before reserving this car in reason"];
			}
		}
		else if([SystemConfig getSingleton].ruleViolationExplanationRequired)
		{
			msg = [Localizer getLocalizedText:@"Before reserving this car in justification"];
		}
        // dont show alert if there is no message
		if (msg != nil)
        {
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Missing fields"]
                                  message:msg
                                  delegate:nil 
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                  otherButtonTitles:nil];
            alert.tag = kAlertUnhandledViolations;
            [alert show];
        }
	}
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if (alertView.tag == kAlertReservationFailed)
	{
		[self configureReserveButton];

		// TODO: what do we want to do when a car booking fails?  As it is, we just stay on the booking screeen.
	}
	else if (alertView.tag == kAlertAreYouSure)
	{
		if (buttonIndex == 1)
			[self requestReservation];
	}
    else if (alertView.tag == kAlertGropuAuthUsed && buttonIndex == alertView.cancelButtonIndex)
    {
        if ([UIDevice isPad])
        {
            [self dismissViewControllerAnimated:NO completion:nil];
        }
        else
        {
            [self.navigationController popToRootViewControllerAnimated:YES];
         }
        UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
        // Force home screen refresh
        if ([homeVC respondsToSelector:@selector(refreshTripsData)])
        {
            [homeVC performSelector:@selector(refreshTripsData) withObject:nil];
        }

    }
}

-(void)requestReservation
{
	if (self.car.sendCreditCard && self.creditCardIndex == nil)
		return;
    
    [self completeReservation];
}

-(void)completeReservation
{
	self.navigationItem.rightBarButtonItem = nil;
	//MOB-10431
    CreditCard *creditCard = nil;
    if (self.car.sendCreditCard)
    {
        NSUInteger ccIndex = [self.creditCardIndex integerValue];
        creditCard = [self.creditCards objectAtIndex:ccIndex];
    }

	CarReservationRequest* reservationRequest = [[CarReservationRequest alloc] init];
	reservationRequest.carId = self.car.carId;
	reservationRequest.creditCardId = (creditCard == nil ? @"" : creditCard.ccId);
	reservationRequest.carBookingTripData = self.carBookingTripData;
	reservationRequest.violationReasonCode = self.violationReasonCode;
	reservationRequest.violationJustification = self.violationJustification;
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:reservationRequest, @"CAR_RESERVATION_REQUEST", nil];
    
    NSString *customFields = [TravelCustomFieldsManager makeCustomFieldsRequestXMLBody];
    
    if (customFields != nil)
        pBag[@"TRAVEL_CUSTOM_FIELDS"] = customFields;
    
    if( [Config isGov])
    {
        if (self.taFields != nil)
        {
            NSString* existingTANumber = [GovTAField getExistingTANumber:self.taFields];
            NSString* perdiemLocationID = [GovTAField getPerdiemLocationID:self.taFields];
            
            GovTAField *currentTAField = [GovTAField getPerDiemField:self.taFields];
            
            if (existingTANumber != nil)
                [pBag setObject:existingTANumber forKey:@"EXISTING_TA_NUMBER"];
            
            if (perdiemLocationID != nil)
                [pBag setObject:perdiemLocationID forKey:@"PER_DIEM_LOCATION_ID"];
            
            if (currentTAField != nil)
            {
                if (currentTAField.perDiemLocation != nil && currentTAField.perDiemLocState != nil)
                {
                    if (currentTAField.isUSContiguous)
                    {
                        [pBag setObject:@"US" forKey:@"GOV_PER_DIEM_COUNTRY"];
                        [pBag setObject:currentTAField.perDiemLocState forKey:@"GOV_PER_DIEM_LOC_STATE"];
                    }
                    else
                        [pBag setObject:currentTAField.perDiemLocState forKey:@"GOV_PER_DIEM_COUNTRY"];
                    
                    [pBag setObject:currentTAField.perDiemLocation forKey:@"PER_DIEM_LOCATION"];
                    
                    if (currentTAField.perDiemLocZip != nil)
                        [pBag setObject:currentTAField.perDiemLocZip forKey:@"GOV_PER_DIEM_LOC_ZIP"];

                }
            }
        }
    }

	[[ExSystem sharedInstance].msgControl createMsg:RESERVE_CAR CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES Options:NO_RETRY RespondTo:self];

    // MOB-11972
    NSDictionary *dict = @{@"Type": @"Car", @"Booked From": self.hideCustomFields? @"Trip": @"Home"};
    [Flurry logEvent:@"Book: Reserve" withParameters:dict];
	[self showWaitViewWithText:[Localizer getLocalizedText:@"Reserving Car"]];
}

-(void)showCarReservationResponse:(CarReservationResponse*)carReservationResponse
{
	if ([carReservationResponse.status isEqualToString:@"SUCCESS"])
	{
        //MOB-10920 use waitView instead of updatingItineraryView
//		[updatingItineraryView setHidden:NO];
//		[self.view bringSubviewToFront:updatingItineraryView];
		self.carRezResponse = carReservationResponse;
		if (self.carRezResponse.recordLocator && self.carRezResponse.itinLocator)
		{
            [self showWaitViewWithText:[Localizer getLocalizedText:@"Updating Itinerary"]];
            //NSLog(@"carReservationResponse.recordLocator %@", carReservationResponse.recordLocator);
            //carReservationResponse.recordLocator, @"RECORD_LOCATOR", 
			NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:carReservationResponse.recordLocator, @"RECORD_LOCATOR",[self getViewIDKey], @"TO_VIEW",nil];
			[[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
		}
        else
            [self hideWaitView];
	}
	else // failure
	{
		NSString *errorMessage = (carReservationResponse.errorMessage != nil ? carReservationResponse.errorMessage : [Localizer getLocalizedText:@"The car could not be booked."]);
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:[Localizer getLocalizedText:@"Error"]
							  message:errorMessage
							  delegate:self 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		alert.tag = kAlertReservationFailed;
		[alert show];
        [self hideWaitView];
	}
}


#pragma mark -
#pragma mark Toolbar Methods

-(void) updateHeader
{
    NSString *pickupDate = [DateTimeFormatter formatHotelOrCarDateForBooking:self.carSearchCriteria.pickupDate inTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
	NSString *dropoffDate = [DateTimeFormatter formatHotelOrCarDateForBooking:self.carSearchCriteria.dropoffDate inTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
    
    self.lblHeading.text = self.car.chainName;
	self.lblSubheading1.text = [NSString stringWithFormat:@"%@: %@", [Localizer getLocalizedText:@"Pick-up"], pickupDate];
    self.lblSubheading2.text = [NSString stringWithFormat:@"%@: %@", [Localizer getLocalizedText:@"Drop-off"], dropoffDate];
    
    if(self.car.imageUri != nil && [self.car.imageUri length] > 0)
    {
        CachedImageView *iv = [[CachedImageView alloc] initWithFrame:CGRectMake(10, 10, 70, 70)];
		[iv loadDataFromUri:self.car.imageUri cache:self.imageCache];
        [self.view addSubview:iv];
    }
    else
    {
        UIImageView *iv = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"car_placeholder"]];
        [iv setFrame:CGRectMake(10, 10, 70, 70)];
        [self.view addSubview:iv];
    }
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
	[self.imageCache clear];
}

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
	[self.imageCache clear];
    self.lblHeading = nil;
    self.lblSubheading1 = nil;
    self.lblSubheading2 = nil;
    self.ivCar = nil;
}




#pragma mark - Custom Fields
-(void) reloadCustomFieldsSection
{
    int sectionIndex = 0;
    for(int i = 0; i < [self.aSections count]; i++)
    {
        if([self.aSections[i] isEqualToString:@"TRIP_FIELDS"])
        {
            sectionIndex = i;
            break;
        }
    }
    NSIndexSet *indexSet = [NSIndexSet indexSetWithIndex:sectionIndex];
    
    [self.tableList reloadSections:indexSet withRowAnimation:UITableViewRowAnimationFade];
}

-(void) updateDynamicCustomFields
{
    if ([self isViewLoaded]) {
        self.navigationItem.rightBarButtonItem.enabled = NO;
        [self showLoadingView];
    }
    
    NSString *customFields =  [TravelCustomFieldsManager makeCustomFieldsRequestXMLBody];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"SKIP_CACHE", @"YES", customFields, @"UPDATED_CUSTOM_FIELDS", nil]; 
    [[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_TRAVEL_CUSTOMFIELDS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) fetchCustomFields
{
    if ([self isViewLoaded]) {
        self.navigationItem.rightBarButtonItem.enabled = NO;
        [self showLoadingView];
    }
    
    NSString *customFields =  [TravelCustomFieldsManager makeCustomFieldsRequestXMLBody];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"SKIP_CACHE", @"YES", nil];
    if ([customFields length])
        pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"SKIP_CACHE", @"YES", customFields, @"UPDATED_CUSTOM_FIELDS", nil]; 
    
    [[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_TRAVEL_CUSTOMFIELDS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void)fetchPreSellOptions // Note: Pre-sell for car only contains CC details, and is only called when car.sendCreditCard is YES -- Change this when car has loyalty programs data
{
    NSMutableDictionary *paramBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING"
                                     , self.car.carResult.choiceId , @"CHOICE_ID",  nil];
	
	[[ExSystem sharedInstance].msgControl createMsg:PRE_SELL_OPTIONS CacheOnly:@"NO" ParameterBag:paramBag SkipCache:YES Options:SILENT_ERROR RespondTo:self];
}

// Custom fields
-(UITableViewCell *)configureCustomFieldCellAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *sectionName = self.aSections[indexPath.section];
    NSArray *a = self.dictSections[sectionName];
    EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)a[indexPath.row];  //[tcfRows objectAtIndex:[newIndexPath row]];
    
    if ([tcf.dataType isEqualToString:@"boolean"]) 
    {
        BoolEditCell *cell = (BoolEditCell *)[self.tableList dequeueReusableCellWithIdentifier:@"BoolEditCell"];
        if (cell == nil)  
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"BoolEditCell" owner:self options:nil];
            for (id oneObject in nib)
            {
                if ([oneObject isKindOfClass:[BoolEditCell class]])
                {
                    cell = (BoolEditCell *)oneObject;
                    break;
                }
            }
            
            cell.label.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:15.0f];
        }
        
        BOOL isON =  false;
        
        if (![tcf.attributeValue isEqualToString:@"true"])
        {
            tcf.attributeValue = @"false";
            [[TravelCustomFieldsManager sharedInstance] saveIt:tcf];
        }
        else 
            isON = true;
        
        [cell setSeedData:isON delegate:self context:tcf label:tcf.attributeTitle];
        [cell.label setTextColor:[UIColor customFieldCellLabelColor]];
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        return cell;
    }
    else
    {
        HotelBookingCell *cell = [self getHotelBookingCell:self.tableList];
        
        NSString *lblText = nil;
        
        if (tcf.attributeValue == nil) 
        {
            if ([tcf.required boolValue]) 
            {
                lblText = [NSString stringWithFormat:@"%@ *",[@"Please specify" localize]];
                [cell.lblValue setTextColor:[UIColor redColor]];
            }
            else 
            {
                lblText = [@"Please specify" localize];
                [cell.lblValue setTextColor:[UIColor customFieldCellLabelColor]];
            }
        }
        else {
            [cell.lblValue setTextColor:[UIColor customFieldCellLabelColor]];
        }
        
        cell.lblLabel.text = tcf.attributeTitle;
        cell.lblValue.text = (tcf.selectedAttributeOptionText != nil)? tcf.selectedAttributeOptionText : lblText;
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        
        return cell;
    }
}

-(void) onSelectLongTextOrNumericFieldCellAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *sectionName = self.aSections[indexPath.section];
    NSArray *a = self.dictSections[sectionName];
    EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)a[indexPath.row];  //[tcfRows objectAtIndex:[newIndexPath row]];
    //    EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)[tcfRows objectAtIndex:[indexPath row]];
    
    UITableViewCell *cell = (UITableViewCell *)[self.tableList cellForRowAtIndexPath:indexPath];
    
    if ([cell isKindOfClass:[BoolEditCell class]])
        return; // Bool cells do not require special editing
    
    NSString *customTitle = @"";
    if ([cell isKindOfClass:[HotelBookingCell class]])
        customTitle = ((HotelBookingCell *)cell).lblLabel.text;
    else if (cell.textLabel != nil && cell.textLabel.text != nil)
        customTitle = cell.textLabel.text;
    
    CustomFieldTextEditor *nextController = [[CustomFieldTextEditor alloc] initWithNibName:@"HotelTextEditorViewController" bundle:nil];
    [self.navigationController pushViewController:nextController animated:YES];
    nextController.title = customTitle;
    nextController.tcf = tcf;
    
    if ([tcf.dataType isEqualToString:@"number"]) 
    {
        nextController.textField.keyboardType = UIKeyboardTypeNumberPad;
    }
    
    nextController.textField.text = (tcf.attributeValue != nil)? tcf.attributeValue : @"";
}


-(BOOL) hasPendingRequiredTripFields
{
    return [[TravelCustomFieldsManager sharedInstance] hasPendingRequiredTripFields];
    return FALSE;
}

#pragma mark - Bool edit delegate
-(void) boolUpdated:(NSObject*) context withValue:(BOOL) val
{
    if (context != nil)
    {
        if ([context isKindOfClass:[EntityTravelCustomFields class]])
        {
            EntityTravelCustomFields *tcf = (EntityTravelCustomFields *) context;
            tcf.attributeValue = (val)?@"true":@"false";
            [[TravelCustomFieldsManager sharedInstance] saveIt:tcf];
        }
    }
}

@end

