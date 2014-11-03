//
//  CarViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CarViewController.h"
#import "ExSystem.h" 

#import "CarSearchCriteria.h"
#import "CarDateTimeViewController.h"
#import "HotelLocationViewController.h" // TODO: Rename it to LocationViewController!
#import "ItinDetailsCellLabel.h"
#import "CarBookingTripData.h"
#import "BookingLabelValueCell.h"
#import "SystemConfig.h"
#import "UserConfig.h"
#import "FindCars.h"
#import "CarShop.h"
#import "TrainBookVC.h"
#import "LabelConstants.h"
#import "DateTimeVC.h"
#import "HotelOptionsViewController.h"
#import "CarListViewController.h"
#import "MobileAlertView.h"
#import "DateTimeOneVC.h"
#import "BoolEditCell.h"
#import "HotelBookingCell.h"
#import "CustomFieldTextEditor.h"
#import "FieldOptionsViewController.h"
#import "TravelViolationReasons.h"
#import "Config.h"

#import "GovTAField.h"
#import "GovDutyLocationVC.h"
#import "GovSelectTANumVC.h"
#import "GovPerDiemRateData.h"

#define KSECTION_GOV_TA_FIELDS @"GOV_TA_FIELDS"
#define KSECTION_TRIP_CUSTOM_FIELDS   @"TRIP_FIELDS"

@interface CarViewController ()
-(void) reloadCustomFieldsSection;
-(void) updateDynamicCustomFields;
-(void) fetchCustomFields;
-(void) completeSearch;

@property BOOL shouldDisableSearchButton;

@end

@implementation CarViewController

// date and time now stores in the date field in search criteria entity
@synthesize carSearchCriteria;
@synthesize carBookingTripData;
@synthesize isCancelled, pickerPopOverVC;
@synthesize viewSearching, lblSearchTo, lblSearchFrom, lblSearchTitle, aSections, tcfRows, isDirty, hideCustomFields, editedDependentCustomField, selectedCustomField;
@synthesize taFields;

#define kSectionPickup 0
#define kSectionDropoff 1
#define kSectionPreferences 2

#define kRowHeader 0

#define kRowPickupLocation 0
#define kRowPickupDate 1
//#define kRowPickupTime 2

#define kRowDropoffDate 2
//#define kRowDropoffTime 4

#define kRowPreferenceCarType 3
#define kRowOffAirport 4
#define kRowPReferenceSmoking 2


NSString * const PICKUP_LOCATION_TAG = @"PICKUP_LOCATION_TAG";


#pragma mark -
#pragma mark Initialization


-(void)initializeCarSearchCriteria
{
    if (carSearchCriteria != nil)
        return;  // Already initialized
    
	self.carSearchCriteria = [[CarSearchCriteria alloc] init];
    //update carType based on user config
    UserConfig *curUserCarConfig = [UserConfig getSingleton];
    if (curUserCarConfig != nil) {
        [carSearchCriteria updateAllowedCarType:curUserCarConfig.allowedCarTypes];
    }
    
    // Round time to next hour
    NSDate *now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
    
    NSDate* dateWithoutTime = [DateTimeFormatter getDateWithoutTime:now withTimeZoneAbbrev:@"GMT"];
    NSInteger timeInMinutes = [now timeIntervalSinceDate:dateWithoutTime]/60;
    timeInMinutes = (timeInMinutes+59)/60*60;
    
    NSDate* nextHour = [dateWithoutTime dateByAddingTimeInterval:timeInMinutes * 60];
    self.carSearchCriteria.pickupDate = nextHour; //[DateTimeFormatter getDateWithoutTime:now withTimeZoneAbbrev:@"GMT"];
    [self.carSearchCriteria setNextDayDropoff];
    
    self.carSearchCriteria.pickupExtendedHour = timeInMinutes/60;
    self.carSearchCriteria.dropoffExtendedHour = timeInMinutes/60;
}


#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return CAR;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:GOV_PER_DIEM_RATE])
    {
		GovPerDiemRateData *resp = (GovPerDiemRateData*)msg.responder;
		
		if (msg.responseCode == 200)
        {
            GovTAField *perDiemFld = [GovTAField getPerDiemField:self.taFields];
            perDiemFld.perDiemLdgRate = resp.currentPerDiemRate.ldgRate;
            perDiemFld.perDiemLocationId = resp.currentPerDiemRate.perDiemId;
            
            if (perDiemFld.perDiemLdgRate != nil)
            {
                [self completeSearch];
            }
            else
            {
                [self cancelSearch:self];
                // Alert
                UIAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:nil
                                      message:[Localizer getLocalizedText:@"No per-diem rates found on chosen location and date"]
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                      otherButtonTitles:nil];
                [alert show];
                
            }
        }
        return;
    }
    
	else if (msg.parameterBag != nil)
	{
		if ([msg.idKey isEqualToString:FIND_CARS] && !isCancelled)
		{
			FindCars *findCars = (FindCars *)msg.responder;
			NSArray *cars = ((findCars == nil || findCars.carShop == nil) ? nil : findCars.carShop.cars);
			
			if (cars == nil || [cars count] == 0)
			{
				UIAlertView *alert = [[MobileAlertView alloc] 
									  initWithTitle:[Localizer getLocalizedText:@"No Matching Cars"]
									  message:[Localizer getLocalizedText:@"No matching cars were found.  Please modify your search criteria and try again."]
									  delegate:nil 
									  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
									  otherButtonTitles:nil];
				[alert show];
			}
			else
			{
				NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SHOW_CARS", cars, @"CARS", carSearchCriteria, @"CAR_SEARCH_CRITERIA", @"YES", @"SHORT_CIRCUIT", nil];
				if (carBookingTripData != nil)
				{
					pBag[@"CAR_BOOKING_TRIP_DATA"] = carBookingTripData;
				}
				
//				if([UIDevice isPad])
//				{
					CarListViewController *nextController = [[CarListViewController alloc] initWithNibName:@"CarListViewController" bundle:nil];
                    [nextController view];
					nextController.taFields = self.taFields;
                    Msg *msg = [[Msg alloc] init];
					msg.parameterBag = pBag;
					msg.idKey = @"SHORT_CIRCUIT";
					[self.navigationController pushViewController:nextController animated:YES];
                    [nextController respondToFoundData:msg]; // Load view first, before process msg
//				}
//				else 
//					[ConcurMobileAppDelegate switchToView:CAR_LIST viewFrom:CAR ParameterBag:pBag];
				
			}
           
            [self stopSearch];

		}
		if ((msg.parameterBag)[@"OPTION_TYPE_ID"] != nil)
		{
			// We've returned from the HotelOptionsViewController
			NSString* optionId = (NSString*)(msg.parameterBag)[@"OPTION_TYPE_ID"];
			NSNumber* selectedRowIndexNumber = (NSNumber*)(msg.parameterBag)[@"SELECTED_ROW_INDEX"];
			NSUInteger selectedRowIndex = [selectedRowIndexNumber intValue];
			
			if ([optionId isEqualToString:@"CAR_TYPE_PREFERENCE"])
			{
				self.carSearchCriteria.carTypeIndex = selectedRowIndex;
			}
		}
		else if ((msg.parameterBag)[@"TRIP_KEY"] != nil)
		{
			NSString* tripKey = (NSString*)(msg.parameterBag)[@"TRIP_KEY"];
			NSString* clientLocator = (NSString*)(msg.parameterBag)[@"CLIENT_LOCATOR"];
			NSString* recordLocator = (NSString*)(msg.parameterBag)[@"RECORD_LOCATOR"];
			NSString* defaultCarPickupLocation = (NSString*)(msg.parameterBag)[@"DEFAULT_CAR_PICKUP_LOCATION"];
			NSString* defaultCarPickupLatitude = (NSString*)(msg.parameterBag)[@"DEFAULT_CAR_PICKUP_LATITUDE"];
            NSString* defaultCarPickupIata = (NSString*)(msg.parameterBag)[@"DEFAULT_CAR_PICKUP_IATA"];
			NSString* defaultCarPickupLongitude = (NSString*)(msg.parameterBag)[@"DEFAULT_CAR_PICKUP_LONGITUDE"];
			NSString* defaultCarPickupDate = (NSString*)(msg.parameterBag)[@"DEFAULT_CAR_PICKUP_DATE"];
			NSString* defaultCarDropoffDate = (NSString*)(msg.parameterBag)[@"DEFAULT_CAR_DROPOFF_DATE"];
			
			CarBookingTripData *tripData = [[CarBookingTripData alloc] init];
			tripData.tripKey = tripKey;
			tripData.clientLocator = clientLocator;
			tripData.recordLocator = recordLocator;
			self.carBookingTripData = tripData;
            
            // Start with the default intialization search criteria.  The logic that follows will try
            // to fill in dates from the trip if they can be found.
            [self initializeCarSearchCriteria];
            
			if (defaultCarPickupLocation != nil && defaultCarPickupLatitude != nil && defaultCarPickupLongitude != nil)
			{
				LocationResult *locationResult = [[LocationResult alloc] init];
				locationResult.location = defaultCarPickupLocation;
				locationResult.latitude = defaultCarPickupLatitude;
				locationResult.longitude = defaultCarPickupLongitude;
                if ([defaultCarPickupIata length])
                    locationResult.iataCode = defaultCarPickupIata;
				carSearchCriteria.pickupLocationResult = locationResult;
				carSearchCriteria.dropoffLocationResult = locationResult;
			}
			
			if (defaultCarPickupDate != nil)
			{
				carSearchCriteria.pickupDate = [self dateFromString:defaultCarPickupDate];
                carSearchCriteria.pickupExtendedHour = [CarSearchCriteria hourFromDate:carSearchCriteria.pickupDate];
			}
			
			if (defaultCarDropoffDate != nil)
			{
				carSearchCriteria.dropoffDate = [self dateFromString:defaultCarDropoffDate];
                carSearchCriteria.dropoffExtendedHour = [CarSearchCriteria hourFromDate:carSearchCriteria.dropoffDate];
			}
		}
        else if ([msg.idKey isEqualToString:DOWNLOAD_TRAVEL_CUSTOMFIELDS])
        {
            if ([self isViewLoaded]) {
                self.navigationItem.rightBarButtonItem.enabled = YES;
                self.shouldDisableSearchButton = false;
                [self hideLoadingView];
            }
            
            [self.navigationController.navigationItem.rightBarButtonItem setEnabled:YES];            
            
            if (msg.errBody == nil && msg.responseCode == 200) 
            {
                [aSections removeObject:KSECTION_TRIP_CUSTOM_FIELDS]; // removes the instance if any
                [aSections addObject:KSECTION_TRIP_CUSTOM_FIELDS];
                self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAllFieldsAtStart:YES]; 
                [self reloadCustomFieldsSection];
            }
        }
	}
    if ([msg.idKey isEqualToString:DOWNLOAD_USER_CONFIG])
    {
        // re set the allowed car type based on userConfig
        UserConfig *curUserCarConfig = [UserConfig getSingleton];
        if (curUserCarConfig != nil) {
            [carSearchCriteria updateAllowedCarType:curUserCarConfig.allowedCarTypes];
            [carSearchCriteria readFromSettings];
        }
    }

	[self makeSearchButton];
	
	if (tblView != nil)
		[tblView reloadData];
}

#pragma mark -
#pragma mark Search

-(void)makeSearchButton
{
    NSString *searchButtonTitle = [Localizer getLocalizedText:@"Search"];
    UIBarButtonItem *btnSearch = nil;
    if ([ExSystem is7Plus])
        btnSearch = [[UIBarButtonItem alloc] initWithTitle:searchButtonTitle style:UIBarButtonSystemItemSearch target:self action:@selector(btnSearch:)];
    else
        btnSearch = [ExSystem makeColoredButton:@"BLUE" W:80 H:30 Text:searchButtonTitle SelectorString:@"btnSearch:" MobileVC:self];
    self.navigationItem.rightBarButtonItem = btnSearch;
    
    NSArray *toolbarItems = @[];
	[self setToolbarItems:toolbarItems animated:YES];
    
    if (self.shouldDisableSearchButton) {
        self.navigationItem.rightBarButtonItem.enabled = NO;
    }
}

-(BOOL) hasPendingRequiredTripFields
{
    return ([[TravelCustomFieldsManager sharedInstance] hasPendingRequiredTripFieldsAtStart:YES] && !hideCustomFields);
}

-(void) sendSearchMsg
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:carSearchCriteria, @"CAR_SEARCH_CRITERIA", @"YES", @"SKIP_CACHE", nil];
	[[ExSystem sharedInstance].msgControl createMsg:FIND_CARS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];    
}

-(void) completeSearch
{
    [self sendSearchMsg];
}

-(void) sendGovPerDiemRateMsg
{
    GovTAField* perDiemFld = [GovTAField getPerDiemField:self.taFields];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 perDiemFld.perDiemLocState, @"STATE_CTRY_CODE",
                                 perDiemFld.perDiemLocation, @"LOCATION",
                                 carSearchCriteria.pickupDate, @"EFFECTIVE_DATE",
                                 perDiemFld.perDiemExpDate, @"EXPIRATEION_DATE",
                                 nil];
    
    [[ExSystem sharedInstance].msgControl createMsg:GOV_PER_DIEM_RATE CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(IBAction)btnSearch:(id)sender
{
    if ([self hasPendingRequiredTripFields])
    {
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[@"PENDING_REQUIRED_TRAVEL_CUSTOM_FIELDS" localize] delegate:nil cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles:nil];
        [alert show];
        
        return;
    }
    
	if (carSearchCriteria.pickupLocationResult == nil || carSearchCriteria.dropoffLocationResult == nil)
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:[Localizer getLocalizedText:@"Location Required"]
							  message:[Localizer getLocalizedText:@"Please specify a location."]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
							  otherButtonTitles:nil];
		[alert show];
		return;
	}

	[carSearchCriteria writeToSettings];
	
	NSString *location = carSearchCriteria.pickupLocationResult.location;
	NSString *pickupDate = [DateTimeFormatter formatDateMediumByDate:carSearchCriteria.pickupDate];
    NSString *pickupDateDay = [DateTimeFormatter formatDate:carSearchCriteria.pickupDate Format:@"EEE" TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    
    lblSearchTitle.text = [Localizer getLocalizedText:@"Searching for cars"];
    lblSearchFrom.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"near"], location];
    lblSearchTo.text = [NSString stringWithFormat:@"%@ %@ %@", [Localizer getLocalizedText:@"pickup on"], pickupDateDay, pickupDate];
    [viewSearching setHidden:NO];
	
	self.isCancelled = NO;
	[self makeCancelButton];
	
	[self.navigationItem setHidesBackButton:YES animated:YES];

    if ([Config isGov])
    {
        GovTAField* perDiemFld = [GovTAField getPerDiemField:self.taFields];

        if (perDiemFld!= nil && !perDiemFld.useExisting)
        {
            [self sendGovPerDiemRateMsg];
            return;
        }
    }
    
    [self sendSearchMsg];
}


#pragma mark -
#pragma mark Search Cancellation
-(void)stopSearch
{
    // Stop and cancel have been separated
    // These are the common steps for stopping the search
	self.isCancelled = YES;
	[self hideLoadingView];
    [viewSearching setHidden:YES];
	[self.navigationItem setHidesBackButton:NO animated:YES];
}

-(void)cancelSearch:(id)sender
{
    // Stop and cancel have been separated
    [self stopSearch];
    // These are the additional steps needed when the search has been cancelled, rather than just stopped
    self.navigationController.toolbarHidden = YES;
    [self makeSearchButton];
}

-(void)makeCancelButton
{
    self.navigationController.toolbarHidden = NO;

    UIBarButtonItem *btnCancel = nil;
    if ([ExSystem is7Plus])
    {
        btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN] style:UIBarButtonSystemItemCancel target:self action:@selector(cancelSearch:)];
        [btnCancel setTintColor:[UIColor redColor]];
    }
    else
        btnCancel = [TrainBookVC makeColoredButton:[UIColor redColor] W:100 H:30.0 Text:[Localizer getLocalizedText:LABEL_CANCEL_BTN] Target:self SelectorString:@"cancelSearch:"];
    
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSArray *toolbarItems = @[flexibleSpace, btnCancel, flexibleSpace];
	[self setToolbarItems:toolbarItems animated:YES];
    
    self.navigationItem.rightBarButtonItem = nil;
}


-(void)closeView:(id)sender
{
    // MOB-18668 Check to see whether the VC needs to be dismissed or popped.
    // When called from the Trips List it needs to be popped.
    UINavigationController *navi = self.navigationController;
    if ([navi.title isEqualToString:[Localizer getLocalizedText:@"Trips"]] && [UIDevice isPad])
    {
        [self.navigationController popViewControllerAnimated:YES];
    }
    else
    {
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}

#pragma mark - Custom fields
-(void) updateDynamicCustomFields
{
    if ([self isViewLoaded]) {
        self.navigationItem.rightBarButtonItem.enabled = NO;
        [self showLoadingView];
    }
    
    [self.navigationController.navigationItem.rightBarButtonItem setEnabled:NO];
    
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

    [self.navigationController.navigationItem.rightBarButtonItem setEnabled:NO];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"SKIP_CACHE", @"YES", nil];
    [[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_TRAVEL_CUSTOMFIELDS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];
    
    if ([Config isGov])
        self.aSections = [[NSMutableArray alloc] initWithObjects:KSECTION_GOV_TA_FIELDS, @"Everything", nil];
    else
        self.aSections = [[NSMutableArray alloc] initWithObjects: @"Everything",  nil];
    
    self.shouldDisableSearchButton = false;
    if (!hideCustomFields)
    {
        self.shouldDisableSearchButton = true;
        
        // MOB-9721 CF start as well as finish
        [[TravelCustomFieldsManager sharedInstance] deleteAll]; // Reset for this search
        [self fetchCustomFields];
        
        // placeholder for the CF section
        [aSections addObject:KSECTION_TRIP_CUSTOM_FIELDS];
        // Start fresh, with no custom fields.
        self.tcfRows = nil;
    }
    else 
    {
        [self hideLoadingView];
    }
    
    [viewSearching setHidden:YES];
    
    [self initializeCarSearchCriteria];
    
	if([UIDevice isPad])
	{
		UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStylePlain/*UIBarButtonSystemItemSave*/ target:self action:@selector(closeView:)];
		self.navigationItem.leftBarButtonItem = nil;
		self.navigationItem.leftBarButtonItem = btnCancel;
					  
	}
    self.title = [Localizer getLocalizedText:@"CAR"];

}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

	if ([SystemConfig getSingleton] == nil)
    {
        [[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_SYSTEM_CONFIG CacheOnly:@"NO" ParameterBag:nil SkipCache:YES];
    }
	
	if ([UserConfig getSingleton] == nil)
		[[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_USER_CONFIG CacheOnly:@"NO" ParameterBag:nil SkipCache:YES];
    if ([TravelViolationReasons getSingleton] == nil)
		[[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_TRAVEL_VIOLATIONREASONS CacheOnly:@"NO" ParameterBag:nil SkipCache:YES];

    
	if (carSearchCriteria.pickupLocationResult == nil)
	{
		[carSearchCriteria readFromSettings];
        
        if (carSearchCriteria.pickupLocationResult != nil)
        {
            // Round to next half hour
            NSDate* dateWithoutTime = [DateTimeFormatter getDateWithoutTime:self.carSearchCriteria.pickupDate withTimeZoneAbbrev:@"GMT"];
            NSInteger timeInMinutes = [self.carSearchCriteria.pickupDate timeIntervalSinceDate:dateWithoutTime]/60;
            timeInMinutes = (timeInMinutes+29)/30*30;
            NSDate* nextHalfHour = [dateWithoutTime dateByAddingTimeInterval:timeInMinutes * 60];
            self.carSearchCriteria.pickupDate = nextHalfHour;
            self.carSearchCriteria.pickupExtendedHour = timeInMinutes/60;
            
            dateWithoutTime = [DateTimeFormatter getDateWithoutTime:self.carSearchCriteria.dropoffDate withTimeZoneAbbrev:@"GMT"];
            timeInMinutes = [self.carSearchCriteria.dropoffDate timeIntervalSinceDate:dateWithoutTime]/60;
            timeInMinutes = (timeInMinutes+29)/30*30;
            nextHalfHour = [dateWithoutTime dateByAddingTimeInterval:timeInMinutes * 60];
            
            self.carSearchCriteria.dropoffDate =nextHalfHour;
            self.carSearchCriteria.dropoffExtendedHour = timeInMinutes/60;
        }
	}
	
	[self makeSearchButton];
	
    BOOL loadingDynamicFields = FALSE;

    if (isDirty) 
    {
        isDirty = NO;
        
        if (editedDependentCustomField && [selectedCustomField.attributeValue length])
        {
            loadingDynamicFields = TRUE;
            [self updateDynamicCustomFields];
        }
//        else // Prevent multiple reloads of table view. Note: Please make sure that custom fields section is updated in case the [tblView reloadData] is removed.
//            [self reloadCustomFieldsSection];
    }
    else if (!hideCustomFields)
    {
        // MOB-9721 Refresh core data objects after coming back, since they may have been updated during data fetch.
        self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAllFieldsAtStart:YES];
        [self reloadCustomFieldsSection];
    }

    // MOB-9608 prevent table reload at the same time fields are modified by DOWNLOAD_TRAVEL_CUSTOMFIELDS
	if (tblView != nil && !loadingDynamicFields)
		[tblView reloadData];
}

-(void) viewDidAppear:(BOOL)animated  
{
    [super viewDidAppear:animated];
}


-(void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
	
	[carSearchCriteria writeToSettings];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

-(void) dismissDateTimePopover
{
	if (pickerPopOver != nil) {
        [pickerPopOver dismissPopoverAnimated:YES];
        self.pickerPopOver = nil;
    }
	if(pickerPopOverVC != nil)
    {
		self.pickerPopOverVC = nil;
    }
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	if ([UIDevice isPad]) 
        [self dismissDateTimePopover];
    
    [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}


#pragma mark -
#pragma mark Table view data source
// Custom fields
-(void) reloadCustomFieldsSection
{
    int travelCustomFieldSection = [self.aSections indexOfObject:KSECTION_TRIP_CUSTOM_FIELDS];
    if (travelCustomFieldSection >= 0)
    {
        NSIndexSet *indexSet = [NSIndexSet indexSetWithIndex:travelCustomFieldSection];
        [tblView reloadSections:indexSet withRowAnimation:UITableViewRowAnimationFade];
    }
}

-(UITableViewCell *)configureCustomFieldCellAtIndexPath:(NSIndexPath *)indexPath
{
    EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)tcfRows[[indexPath row]];
    
    if ([tcf.dataType isEqualToString:@"boolean"]) 
    {
        BoolEditCell *cell = (BoolEditCell *)[tblView dequeueReusableCellWithIdentifier:@"BoolEditCell"];
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
        HotelBookingCell *cell = (HotelBookingCell*)[tblView dequeueReusableCellWithIdentifier:@"HotelBookingSingleCell"];
        if (cell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelBookingSingleCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[HotelBookingCell class]])
                    cell = (HotelBookingCell *)oneObject;
        }
        
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
        else{
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
    EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)tcfRows[[indexPath row]];
    
    UITableViewCell *cell = (UITableViewCell *)[tblView cellForRowAtIndexPath:indexPath];
    
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

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [aSections count]; // Sections: pickup, dropoff, preferences
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString* sectionName = aSections[section];
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS])
        return [tcfRows count];
    else if ([sectionName isEqualToString:KSECTION_GOV_TA_FIELDS])
        return [self.taFields count];
    else
        return 4; //7 gives us of airport
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	
    NSString* sectionName = aSections[section];
    
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS])
    {
        return [self configureCustomFieldCellAtIndexPath:indexPath];
    }
    else if ([sectionName isEqualToString:KSECTION_GOV_TA_FIELDS])
    {
        GovTAField * fld = [self.taFields objectAtIndex:row];
        
        BookingLabelValueCell* cell =  [BookingLabelValueCell makeCell:tableView owner:self label:fld.label value:fld.fieldValue];
        [cell.labelHead setHidden:YES];
        [cell.labelLabel setHidden:NO];
        [cell.labelValue setHidden:NO];
        
        if ([fld.access isEqualToString:@"RO"])
            [cell setAccessoryType:UITableViewCellAccessoryNone];
        else
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        
        return cell;
    }
    else
    {
        if (kRowPreferenceCarType == row)
        {
            NSString* carTypePreference = [carSearchCriteria.carTypeNames count] > carSearchCriteria.carTypeIndex ? (carSearchCriteria.carTypeNames)[carSearchCriteria.carTypeIndex] : @"";
            BookingLabelValueCell *cell =  [BookingLabelValueCell makeCell:tableView owner:self label:[Localizer getLocalizedText:@"Car Type"] value:carTypePreference];
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            return cell;
        }
        else if (kRowOffAirport == row)
        {
            NSString* offAir = [Localizer getLocalizedText:@"Yes"];
            if(!carSearchCriteria.isOffAirport)
                offAir = [Localizer getLocalizedText:@"No"];
            BookingLabelValueCell *cell =  [BookingLabelValueCell makeCell:tableView owner:self label:[Localizer getLocalizedText:@"Off Airport"]  value:offAir];
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            return cell;
        }
        else	// kSectionPickup OR kSectionDropoff
        {
            BookingLabelValueCell *cell = nil;
            
            NSString *cellText;
            NSString *cellDetailText;
            
            if (kRowPickupLocation == row )
            {
                // We are only going to allow pickups from airports in mobile
                cellText = [Localizer getLocalizedText:@"Car Hire Airport Location"];
                cellDetailText = (carSearchCriteria.pickupLocationResult == nil ? @"" : carSearchCriteria.pickupLocationResult.location);
            }
            else 
            {

                NSTimeZone* gmtTz = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
                if (kRowPickupDate == row)
                {
                    cellText = [Localizer getLocalizedText:@"Pick-up Date"];
                    // Fix for : MOB-7820
                    cellDetailText = [DateTimeFormatter formatDate:carSearchCriteria.pickupDate Format:@"EEE MMM dd, h:mm aa" TimeZone:gmtTz];
                }
                else if (kRowDropoffDate == row)
                {
                    cellText = [Localizer getLocalizedText:@"Drop-off Date"];
                      // Fix for : MOB-7820
                    cellDetailText = [DateTimeFormatter formatDate:carSearchCriteria.dropoffDate Format:@"EEE MMM dd, h:mm aa" TimeZone:gmtTz];
                }
            }

            
            cell =  [BookingLabelValueCell makeCell:tableView owner:self label:cellText value:cellDetailText];
            [cell.labelHead setHidden:YES];
            [cell.labelLabel setHidden:NO];
            [cell.labelValue setHidden:NO];
            
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;

            return cell;
        }
    }
	
	return nil;
}


#pragma mark -
#pragma mark Table view delegate
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
{
    NSString* sectionName = aSections[section];
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS] && tcfRows != nil && [tcfRows count] > 0)
        return [@"Booking Info" localize];
    else 
        return nil;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 55;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    NSString* sectionName = aSections[section];
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS])
    {
        self.isDirty = YES;
        
        EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)tcfRows[[indexPath row]];
        
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
    else if ([sectionName isEqualToString:KSECTION_GOV_TA_FIELDS])
    {
        GovTAField * fld = [self.taFields objectAtIndex:row];
        if ([fld isAuthField])
        {
            [GovSelectTANumVC showSelectTANum:self withCompletion:nil withFields:self.taFields withDelegate:self asRoot:NO];
            
        }
        else
        {
            [GovDutyLocationVC showDutyLocationVC:self withCompletion:nil withFields:self.taFields withDelegate:self withPerDiemRate:YES asRoot:NO];
            
        }
    }
    else
    { 
        if (kRowPreferenceCarType == row)
        {
            NSString *optionsViewTitle = nil;
            NSString *optionType = nil;
            NSArray *labels = nil;
            NSNumber *selectedRowIndex = nil;
            
            if (kRowPreferenceCarType == row)
            {
                optionsViewTitle = [Localizer getLocalizedText:@"Select Car Type"];
                optionType = @"CAR_TYPE_PREFERENCE";
                labels = carSearchCriteria.carTypeNames;
                selectedRowIndex = [NSNumber numberWithInt:carSearchCriteria.carTypeIndex];
            }

            if([UIDevice isPad])
            {
                
                HotelOptionsViewController *nextController = [[HotelOptionsViewController alloc] initWithNibName:@"HotelOptionsViewController" bundle:nil];
                nextController.optionTitle = optionsViewTitle;
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"FROM_VIEW", optionType, @"OPTION_TYPE_ID", optionsViewTitle, @"TITLE", labels, @"LABELS", selectedRowIndex, @"SELECTED_ROW_INDEX", @"YES", @"SHORT_CIRCUIT", nil];
                Msg *msg = [[Msg alloc] init];
                msg.parameterBag = pBag;
                msg.idKey = @"SHORT_CIRCUIT";
                [nextController respondToFoundData:msg];
                [self.navigationController pushViewController:nextController animated:YES];
            }
            else 
            {
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"FROM_VIEW", optionType, @"OPTION_TYPE_ID", optionsViewTitle, @"TITLE", labels, @"LABELS", selectedRowIndex, @"SELECTED_ROW_INDEX", @"YES", @"SHORT_CIRCUIT", nil];
                [ConcurMobileAppDelegate switchToView:HOTEL_OPTIONS viewFrom:CAR ParameterBag:pBag];
            }
        }
        else if (kRowOffAirport == row)
        {
            if(carSearchCriteria.isOffAirport)
                carSearchCriteria.isOffAirport = NO;
            else
                carSearchCriteria.isOffAirport = YES;
            [tableView reloadData];
        }
        else if (kRowPickupLocation == row)
        {
            HotelLocationViewController * vc = [[HotelLocationViewController alloc] initWithNibName:@"HotelLocationViewController" bundle:nil];
            vc.locationDelegate = self;
            vc.neverShowOffices = YES;
            // We are only going to allow pickups from airports in mobile
            vc.isAirportOnly = YES;
            if (carSearchCriteria != nil && carSearchCriteria.pickupLocationResult != nil && carSearchCriteria.pickupLocationResult.location != nil)
                vc.initialSearchLocation = carSearchCriteria.pickupLocationResult;
            vc.tag = PICKUP_LOCATION_TAG;
            
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
        else // date or time row
        {
            //Return Date
            if ([UIDevice isPad]) 
            {
                [self pickerDateTapped:self IndexPath:indexPath];
                return;
            }
            else {
                BOOL isPickup = (kRowPickupDate == row);
                DateTimeOneVC * vc = [[DateTimeOneVC alloc] initWithNibName:@"DateTimeOneVC" bundle:nil];
                NSString* lbl = [Localizer getLocalizedText: isPickup? @"Pick-up Date":@"Drop-off Date"];
                NSDate* initialDate = (isPickup? carSearchCriteria.pickupDate : carSearchCriteria.dropoffDate);
                [vc setSeedData:self withFullDate:initialDate withLabel:lbl withContext:isPickup? @"Pickup":@"Dropoff"];
                [self.navigationController pushViewController:vc animated:TRUE];

            }
        }
    }
}


#pragma mark -
#pragma mark Date setting methods

-(void)changePickupDate:(NSDate*)date timeInMinutes:(NSInteger)extMinutes
{
	// Before changing the pickup date, determine the time interval between the
	// original pickup date and dropoff date
    NSDate* origPDate = [DateTimeFormatter getDateWithoutTime:carSearchCriteria.pickupDate withTimeZoneAbbrev:@"GMT"];
    NSDate* origDDate = [DateTimeFormatter getDateWithoutTime:carSearchCriteria.dropoffDate withTimeZoneAbbrev:@"GMT"];
	NSTimeInterval originalTimeInterval = [origDDate timeIntervalSinceDate:origPDate];
    
	// Change to the new pickup date and time
	carSearchCriteria.pickupDate = [date dateByAddingTimeInterval:extMinutes*60];
	carSearchCriteria.pickupExtendedHour = extMinutes/60;
	
	// Adjust the drop off date to maintain the same time interval from the
	// new pickup date.
	if (originalTimeInterval > 0)
	{
		NSDate *newDropOffDate = [[NSDate alloc] initWithTimeInterval:originalTimeInterval sinceDate:date];
        NSInteger timeInMin = [DateTimeFormatter getTimeInSeconds:carSearchCriteria.dropoffDate withTimeZoneAbbrev:@"GMT"]/60;
		carSearchCriteria.dropoffDate = [newDropOffDate dateByAddingTimeInterval:timeInMin*60];
	}

	[tblView reloadData];
}

-(void)changeDropoffDate:(NSDate*)date timeInMinutes:(NSInteger)extMinutes
{
	carSearchCriteria.dropoffDate = [date dateByAddingTimeInterval:extMinutes*60];
	carSearchCriteria.dropoffExtendedHour = extMinutes/60;
	[tblView reloadData];
}


#pragma mark -
#pragma mark Location delegate methods

-(void)locationSelected:(LocationResult*)locationResult tag:(NSString*)tag;
{
	if ([tag isEqualToString:PICKUP_LOCATION_TAG])
	{
		carSearchCriteria.pickupLocationResult = locationResult;
		
		// Always make the drop-off location the same as the pick-up location.
		carSearchCriteria.dropoffLocationResult = locationResult;
		
		if([UIDevice isPad])
			[tblView reloadData];
	}
}


#pragma mark -
#pragma mark Conversions

-(NSDate*)dateFromString:(NSString*)string
{
    return [DateTimeFormatter getLocalDate:string]; // MOB-9802 use GMT for travel dates
//	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init]; 
//	// specify timezone
//	[dateFormatter setTimeZone:[NSTimeZone localTimeZone]];
//	// Localizing date
//	[dateFormatter setLocale:[NSLocale currentLocale]];
//	
//	[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
//	NSDate *date = [dateFormatter dateFromString:string];
//	[dateFormatter release];
//	return date;
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
    self.viewSearching = nil;
    self.lblSearchTo = nil;
    self.lblSearchTitle = nil;
    self.lblSearchFrom = nil;
}




#pragma mark -
#pragma mark PopOver Methods
- (void)pickerDateTapped:(id)sender IndexPath:(NSIndexPath *)indexPath
{
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
	
	
	pickerPopOverVC = [[DateTimePopoverVC alloc] initWithNibName:@"DateTimePopoverVC" bundle:nil];
	pickerPopOverVC.isDate = NO;
	pickerPopOverVC.delegate = self;
	pickerPopOverVC.indexPath = indexPath;
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:pickerPopOverVC];   
    BOOL isPickup = indexPath.row == kRowPickupDate;
	NSDate *dasDate = isPickup? carSearchCriteria.pickupDate : carSearchCriteria.dropoffDate;
    [pickerPopOverVC initDate:dasDate];
    
	//MOB-3813, only allow one year out and make minimum date today
	pickerPopOverVC.datePicker.maximumDate = [NSDate dateWithTimeIntervalSinceNow:(60.0 * 60.0 * 24.0 * 365.0)];
    NSDate* now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
	pickerPopOverVC.datePicker.minimumDate = now;
    
	if(indexPath.row == kRowDropoffDate)
		pickerPopOverVC.datePicker.minimumDate = carSearchCriteria.pickupDate;
	
	
	CGRect cellRect = [tblView rectForRowAtIndexPath:indexPath];
	CGRect myRect = [self.view convertRect:cellRect fromView:tblView];
	
    [self.pickerPopOver presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionLeft animated:YES]; 
}

- (void)cancelPicker
{
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
}


- (void)donePicker:(NSDate *)dateSelected
{
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
}


-(void) dateSelected:(NSObject*) context withDate:(NSDate*) date
{
    NSDate* dawnDate = [DateTimeFormatter getDateWithoutTime:date withTimeZoneAbbrev:@"GMT"];
    NSInteger tmInMin = [DateTimeFormatter getTimeInSeconds:date withTimeZoneAbbrev:@"GMT"]/60;

    if ([@"Pickup" isEqualToString:(NSString*) context])
    {
//		double dDayDiff = [carSearchCriteria.dropoffDate timeIntervalSinceDate:carSearchCriteria.pickupDate];
		
//		NSNumber *numDayDiff = [NSNumber numberWithDouble:(((dDayDiff / 60) /60) / 24)];
//		NSDate *newDropDate = [self addDaysToDate:date NumDaysToAdd:[numDayDiff intValue]];
        
		[self changePickupDate:dawnDate timeInMinutes:tmInMin];
//        tmInMinutesPickup = tmInMin;
//		[self changeDropoffDate:newDropDate timeInMinutes:tmInMinutesDropOff];
	}
	else 
	{
		[self changeDropoffDate:dawnDate timeInMinutes:tmInMin];
	}
	
}

- (void)pickedDate:(NSDate *)dateSelected
{
	int row = [pickerPopOverVC.indexPath row];
	NSString* rowId = @"Dropoff";
	if(row == kRowPickupDate)
        rowId = @"Pickup";
    [self dateSelected:rowId withDate:dateSelected];
}


-(NSDate *)addDaysToDate:(NSDate *)dateDepart NumDaysToAdd:(int)daysToAdd
{
	//NSDate *now = [NSDate date];
	//int daysToAdd = 50;  // or 60 :-)
	
	// set up date components
	NSDateComponents *components = [[NSDateComponents alloc] init];

	[components setDay:daysToAdd];
	
	// create a calendar
	NSCalendar *gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
	
	NSDate *newDate2 = [gregorian dateByAddingComponents:components toDate:dateDepart options:0];
	//NSLog(@"Clean: %@", newDate2);
	
	return newDate2;
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

#pragma  mark - GOV fields

-(void) fieldUpdated:(FormFieldData*) field
{
    if ([Config isGov])
    {
        int govSection = [self.aSections indexOfObject:KSECTION_GOV_TA_FIELDS];
        if (govSection >=0)
        {
            NSIndexSet *indexSet = [NSIndexSet indexSetWithIndex:govSection];
            [self.tblView reloadSections:indexSet withRowAnimation:UITableViewRowAnimationFade];
        }
    }
}

-(void) fieldCanceled:(FormFieldData *)field
{
}


+ (void) showCarVC:(UINavigationController*)navi withTAFields:(NSArray*) taFlds
{
    CarViewController* vc = [[CarViewController alloc] initWithNibName:@"CarViewController" bundle:nil];
    vc.taFields = [NSMutableArray arrayWithArray:taFlds];
    // MOB-18897 Reworking of 18668 and bringing back in changes prior to that
    if ([UIDevice isPad] && ![Config isGov])
    {
        // Check if we have come from TripsViewController
        if (![[navi topViewController] isKindOfClass:[TripsViewController class]])
        {
            // If not, then we need to create a new modal NC to hold the reservation screens in
            UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:vc];
            [localNavigationController setModalPresentationStyle:UIModalPresentationFormSheet];
            [navi presentViewController:localNavigationController animated:YES completion:nil];
        }
        else
        {
            // If we have come from the TripsViewController we already have a modal VC, so we can just push onto it
            [navi pushViewController:vc animated:YES];
        }
    }
    else
    {
        // Non-iPad can push the view safely
        [navi pushViewController:vc animated:YES];
    }
}

@end

