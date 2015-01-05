//
//  HotelViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <AVFoundation/AVAudioSession.h>

#import "CarViewController.h"
#import "HotelViewController.h"
#import "ExSystem.h" 

#import "HotelSearch.h"
#import "HotelSearchCriteria.h"
#import "FindHotels.h"
#import "HotelLocationViewController.h"
#import	"DateSpanViewController.h"
#import "DistanceViewController.h"
#import "DownloadSystemConfig.h"
#import "BookingWideLabelNarrowValueCell.h"
#import "SystemConfig.h"
#import "UserConfig.h"
#import "TRainBookVC.h"
#import "LabelConstants.h"
#import "HotelOptionsViewController.h"
#import "DistanceViewController.h"
#import "HotelTextEditorViewController.h"
#import "HotelSearchResultsViewController.h"
#import "MobileAlertView.h"
#import "ItinDetailCell.h"
#import "BoolEditCell.h"
#import "HotelBookingCell.h"
#import "CustomFieldTextEditor.h"
#import "FieldOptionsViewController.h"
#import "TravelViolationReasons.h"
#import "Config.h"
#import "EvaVoiceSearchiOS6TableViewController.h"

#import "GovTAField.h"
#import "GovDutyLocationVC.h"
#import "GovSelectTANumVC.h"
#import "GovPerDiemRateData.h"
#import "Fusion14HotelSearchResultsViewController.h"
#import "Config.h"
#import "EvaVoiceSearchViewController.h"

#define KSECTION_GOV_TA_FIELDS @"GOV_TA_FIELDS"
#define KSECTION_TRIP_CUSTOM_FIELDS   @"TRIP_FIELDS"

@interface HotelViewController ()
-(void) reloadCustomFieldsSection;
-(void) updateDynamicCustomFields;
-(void) fetchCustomFields;

-(void) completeSearch;

@property BOOL shouldDisableSearchButton;
@property BOOL dispatchOnce;

@end

@implementation HotelViewController

@synthesize hotelSearch;

@synthesize isCancelled, pickerPopOverVC;
@synthesize viewSearching, lblSearchTo, lblSearchFrom, lblSearchTitle, aSections, tcfRows,isDirty, hideCustomFields, editedDependentCustomField, selectedCustomField;
@synthesize taFields;  // GOV

#define kSectionLocation 0
#define kSectionDate 1
#define kSectionDistance 2
#define kSectionSmoking 3

#pragma mark -
#pragma mark Initialization

-(void)initializeHotelSearch
{
    if (self.hotelSearch == nil)
    {
        self.hotelSearch = [[HotelSearch alloc] init];
    }
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return HOTEL;
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
		if ([msg.idKey isEqualToString:FIND_HOTELS] && !isCancelled)
		{
            // Check if a handled error was returned by Search2
            FindHotels *findHotels = (FindHotels*)msg.responder;
            if ([findHotels.errorMessage length])
            {
                [self cancelSearch:self];
                UIAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:[Localizer getLocalizedText:@"Search Failed"]
                                      message:[findHotels errorMessage]
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
                                      otherButtonTitles:nil];
                [alert show];
            }
            else
            {
                NSArray *aHotels = [[HotelBookingManager sharedInstance] fetchAll];
                
    //			FindHotels *findHotels = (FindHotels*)msg.responder;
    //			self.hotelSearch = findHotels.hotelSearch;
                [self cancelSearch:self];
                if ([aHotels count] == 0)
                {
                    UIAlertView *alert = [[MobileAlertView alloc] 
                                          initWithTitle:[Localizer getLocalizedText:@"HOTEL_VIEW_NO_HOTELS_TITLE"]
                                          message:[Localizer getLocalizedText:@"HOTEL_VIEW_NO_HOTELS_MESSAGE"]
                                          delegate:nil 
                                          cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
                                          otherButtonTitles:nil];
                    [alert show];
                }
                else
                {
                    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SHOW_HOTELS", hotelSearch, @"HOTEL_SEARCH", @"YES", @"SHORT_CIRCUIT", nil];
                    pBag[@"HOTEL_SEARCH_CRITERIA"] = (msg.parameterBag)[@"HOTEL_SEARCH_CRITERIA"];
                    pBag[@"TOTAL_COUNT"] = @(findHotels.totalCount);
                    
                    HotelSearchResultsViewController *nextController = [[HotelSearchResultsViewController alloc] initWithNibName:@"HotelSearchResultsViewController" bundle:nil];
                    [nextController view];
                    nextController.taFields = self.taFields; // pass the value down stream
                    Msg *msg = [[Msg alloc] init];
                    msg.parameterBag = pBag;
                    msg.idKey = @"SHORT_CIRCUIT";
                    nextController.hotelBenchmarks = findHotels.hotelBenchmarks;
                    nextController.travelPointsInBank = findHotels.travelPointsInBank;
                    [self.navigationController pushViewController:nextController animated:YES];
                    [nextController respondToFoundData:msg];

                }
            }
		}
		else if ((msg.parameterBag)[@"TRIP_KEY"] != nil)
		{
			NSString* defaultTripKey = (NSString*)(msg.parameterBag)[@"TRIP_KEY"];
			NSString* defaultHotelLocation = (NSString*)(msg.parameterBag)[@"DEFAULT_HOTEL_LOCATION"];
			NSString* defaultHotelLatitude = (NSString*)(msg.parameterBag)[@"DEFAULT_HOTEL_LATITUDE"];
			NSString* defaultHotelLongitude = (NSString*)(msg.parameterBag)[@"DEFAULT_HOTEL_LONGITUDE"];
			NSString* defaultHotelCheckinDate = (NSString*)(msg.parameterBag)[@"DEFAULT_HOTEL_CHECKIN_DATE"];
			NSString* defaultHotelCheckoutDate = (NSString*)(msg.parameterBag)[@"DEFAULT_HOTEL_CHECKOUT_DATE"];
			
            [self initializeHotelSearch];
            
			if (defaultTripKey != nil)
			{
				hotelSearch.tripKey = defaultTripKey;
			}
			
			if (defaultHotelLocation != nil && defaultHotelLatitude != nil && defaultHotelLongitude != nil)
			{
				LocationResult *locationResult = [[LocationResult alloc] init];
				locationResult.location = defaultHotelLocation;
				locationResult.latitude = defaultHotelLatitude;
				locationResult.longitude = defaultHotelLongitude;
				hotelSearch.hotelSearchCriteria.locationResult = locationResult;
			}
			
			if (defaultHotelCheckinDate != nil)
			{
                // MOB-18824 Log entry added to aid in diagnosing a problem with invalid dates being provided by
                // Air booking TripData (click Book Hotel when viewing an Air Booking in Trips)
                [[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelViewController::respondToFoundData: [DATECHECK] defaultHotelCheckinDate value '%@'", defaultHotelCheckinDate] Level:MC_LOG_DEBU];
                // MOB-18824 "Blind-fix" Check that the string was converted to a valid date before assigning it
                // Also switched use of dateFromString to MWSDate method in DateTimeFormatter
                NSDate *nullCheckedDate = [DateTimeFormatter getNSDateFromMWSDateString:defaultHotelCheckinDate];
                if (nullCheckedDate != nil)
                {
                    hotelSearch.hotelSearchCriteria.checkinDate = nullCheckedDate;
                    // MOB-17430 Log entry added to aid in diagnosing a problem we have with historical searches being submitted
                    [[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelViewController::respondToFoundData: [DATECHECK] checkinDate value %@", hotelSearch.hotelSearchCriteria.checkinDate] Level:MC_LOG_DEBU];
                }
			}
			
			if (defaultHotelCheckoutDate != nil)
			{
                // MOB-18824 Log entry added to aid in diagnosing a problem with invalid dates being provided by
                // Air booking TripData (click Book Hotel when viewing an Air Booking in Trips)
                [[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelViewController::respondToFoundData: [DATECHECK] defaultHotelCheckoutDate value '%@'", defaultHotelCheckoutDate] Level:MC_LOG_DEBU];
                // MOB-18824 "Blind-fix" Check that the string was converted to a valid date before assigning it
                // Also switched use of dateFromString to MWSDate method in DateTimeFormatter
                NSDate *nullCheckedDate = [DateTimeFormatter getNSDateFromMWSDateString:defaultHotelCheckoutDate];
                if (nullCheckedDate != nil)
                {
                    hotelSearch.hotelSearchCriteria.checkoutDate = nullCheckedDate;
                }
			}
		}
		else if ((msg.parameterBag)[@"DISTANCE_VALUE"] != nil && (msg.parameterBag)[@"IS_METRIC_DISTANCE"] != nil)
		{
			hotelSearch.hotelSearchCriteria.distanceValue = (NSNumber*)(msg.parameterBag)[@"DISTANCE_VALUE"];
			hotelSearch.hotelSearchCriteria.isMetricDistance = (NSNumber*)(msg.parameterBag)[@"IS_METRIC_DISTANCE"];
		}
		else if ((msg.parameterBag)[@"TEXT"] != nil)
		{
			hotelSearch.hotelSearchCriteria.containingWords = (NSString*)(msg.parameterBag)[@"TEXT"];
		}
		else if ((msg.parameterBag)[@"OPTION_TYPE_ID"] != nil)
		{
			// We've returned from the HotelOptionsViewController
			NSNumber* selectedRowIndexNumber = (NSNumber*)(msg.parameterBag)[@"SELECTED_ROW_INDEX"];
			NSUInteger selectedRowIndex = [selectedRowIndexNumber intValue];
			self.hotelSearch.hotelSearchCriteria.smokingIndex = selectedRowIndex;
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

-(void) sendHotelSearchMsg
{
    // MOB-17430 Log entry added to aid in diagnosing a problem we have with historical searches being submitted
    if (hotelSearch.hotelSearchCriteria.checkinDate != nil)
    {
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelViewController::sendHotelSearchMsg: [DATECHECK] checkinDate value %@", hotelSearch.hotelSearchCriteria.checkinDate] Level:MC_LOG_DEBU];
    }
    NSMutableDictionary *pBag = nil;
        pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:hotelSearch, @"HOTEL_SEARCH", @"YES", @"SKIP_CACHE", @"0", @"STARTPOS", @"30", @"NUMRECORDS", nil];
     [[ExSystem sharedInstance].msgControl createMsg:FIND_HOTELS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) completeSearch
{
    GovTAField* perDiemFld = [GovTAField getPerDiemField:self.taFields];
    
    if (perDiemFld.perDiemLdgRate != nil)
    {
        hotelSearch.hotelSearchCriteria.perDiemRate = perDiemFld.perDiemLdgRate;

        [self sendHotelSearchMsg];
    }
}

-(void) sendGovPerDiemRateMsg
{
    GovTAField* perDiemFld = [GovTAField getPerDiemField:self.taFields];

    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 perDiemFld.perDiemLocState, @"STATE_CTRY_CODE",
                                 perDiemFld.perDiemLocation, @"LOCATION",
                                 hotelSearch.hotelSearchCriteria.checkinDate, @"EFFECTIVE_DATE",
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
    
	if (hotelSearch.hotelSearchCriteria.locationResult == nil)
	{
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"HOTEL_VIEW_SPECIFY_LOCATION_TITLE"]
							  message:[Localizer getLocalizedText:@"HOTEL_VIEW_SPECIFY_LOCATION_MESSAGE"]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
							  otherButtonTitles:nil];
		[alert show];
		return;
	}
    
    if (![self areCheckInAndCheckOutDateValid]) {
        UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Error"]
							  message:[@"The check-out date must be later than the check-in date." localize]
							  delegate:nil
							  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
							  otherButtonTitles:nil];
		[alert show];
		return;
    }
    
	[hotelSearch.hotelSearchCriteria writeToSettings];
    
	NSString *location = hotelSearch.hotelSearchCriteria.locationResult.location;
    //NSLog(@"location %@", location);
	NSString *checkinDate = [DateTimeFormatter formatHotelOrCarDateForBooking:hotelSearch.hotelSearchCriteria.checkinDate];
    lblSearchTitle.text = [Localizer getLocalizedText:@"Searching for hotels"];
    lblSearchFrom.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"near"], location];
    lblSearchTo.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"check in on"], checkinDate];
    [viewSearching setHidden:NO];

	self.isCancelled = NO;
	[self makeCancelButton];

	[self.navigationItem setHidesBackButton:YES animated:YES];
    
    if ([Config isGov])
    {
        [self sendGovPerDiemRateMsg];
        return;
    }
    
    [self sendHotelSearchMsg];
}

-(BOOL)areCheckInAndCheckOutDateValid
{
    // the function compares Dates stripping out the 'Time' component
    NSCalendar *calendar = [NSCalendar currentCalendar];
	calendar.timeZone = [NSTimeZone localTimeZone];
	NSDateComponents *checkinDateComponents = [calendar components:(NSYearCalendarUnit | NSMonthCalendarUnit | NSDayCalendarUnit) fromDate:hotelSearch.hotelSearchCriteria.checkinDate];
    NSDateComponents *checkoutDateComponents = [calendar components:(NSYearCalendarUnit | NSMonthCalendarUnit | NSDayCalendarUnit) fromDate:hotelSearch.hotelSearchCriteria.checkoutDate];
    
    NSDate* checkinDateOnly = [calendar dateFromComponents:checkinDateComponents];
    NSDate* checkoutDateOnly = [calendar dateFromComponents:checkoutDateComponents];
	return [checkinDateOnly compare:checkoutDateOnly] == NSOrderedAscending;
}


#pragma mark -
#pragma mark Search Cancellation

-(void)cancelSearch:(id)sender
{
    self.navigationController.toolbarHidden = YES;
	self.isCancelled = YES;
	[self hideLoadingView];
    [viewSearching setHidden:YES];
	[self makeSearchButton];
	[self.navigationItem setHidesBackButton:NO animated:YES];
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

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    if ([Config isGov])
        self.aSections = [[NSMutableArray alloc] initWithObjects:KSECTION_GOV_TA_FIELDS, @"Everything", nil];
    else
        self.aSections = [[NSMutableArray alloc] initWithObjects:@"Everything", nil];

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
	tblView.rowHeight = 44;
	
    [self hideWaitView];

	[self initializeHotelSearch];
	
	if([UIDevice isPad])
	{
		UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
		self.navigationItem.leftBarButtonItem = nil;
		self.navigationItem.leftBarButtonItem = btnCancel;
		
	}
    self.title = [Localizer getLocalizedText:@"Hotel Search"];

    if ([SystemConfig getSingleton] == nil)
    {
        [[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_SYSTEM_CONFIG CacheOnly:@"NO" ParameterBag:nil SkipCache:YES];
    }
	
    if ([TravelViolationReasons getSingleton] == nil)
		[[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_TRAVEL_VIOLATIONREASONS CacheOnly:@"NO" ParameterBag:nil SkipCache:YES];

}

-(void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
	
    [self.navigationController setToolbarHidden:YES];
	if (hotelSearch.hotelSearchCriteria.locationResult == nil)
	{
        // MOB-17430 Log entry added to aid in diagnosing a problem we have with historical searches being submitted
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelViewController::viewWillAppear: [DATECHECK] fetching last search criteria"] Level:MC_LOG_DEBU];

		[hotelSearch.hotelSearchCriteria readFromSettings];
        // MOB-17430 Log entry added to aid in diagnosing a problem we have with historical searches being submitted
        if (hotelSearch.hotelSearchCriteria.checkinDate != nil)
        {
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelViewController::viewWillAppear: [DATECHECK] checkinDate value %@", hotelSearch.hotelSearchCriteria.checkinDate] Level:MC_LOG_DEBU];
        }
        if (hotelSearch.hotelSearchCriteria.checkoutDate != nil)
        {
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelViewController::viewWillAppear: [DATECHECK] checkoutDate value %@", hotelSearch.hotelSearchCriteria.checkoutDate] Level:MC_LOG_DEBU];
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
   
    if([Config isEvaVoiceEnabled])
    {
        self.tblView.sectionHeaderHeight = 0.0;
        self.tblView.sectionFooterHeight = 0.0;
        self.tblView.tableFooterView = self.EvaBtnView;
     }
 }


-(void)viewDidAppear:(BOOL)animated
{
	[super viewDidAppear:animated];
	[self.navigationItem setHidesBackButton:NO animated:YES];
}

-(void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];

	[hotelSearch.hotelSearchCriteria writeToSettings];
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

#pragma mark - Custom Fields
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

// Custom fields
-(void) reloadCustomFieldsSection
{

    if ([self.aSections indexOfObject:KSECTION_TRIP_CUSTOM_FIELDS] != NSNotFound)
    {
        NSIndexSet *indexSet = [NSIndexSet indexSetWithIndex:[self.aSections indexOfObject:KSECTION_TRIP_CUSTOM_FIELDS]];
    
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
        else
            [cell.lblValue setTextColor:[UIColor customFieldCellLabelColor]]; 
        
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
    
    [nextController.textField setReturnKeyType:UIReturnKeyDone];
    
    nextController.textField.text = (tcf.attributeValue != nil)? tcf.attributeValue : @"";
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [aSections count];	// Date, location, distance
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString* sectionName = aSections[section];
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS])
        return [tcfRows count];
    else if ([sectionName isEqualToString:KSECTION_GOV_TA_FIELDS])
        return [self.taFields count];
    else
        return 5;
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
    else
    {        
        ItinDetailCell *cell = (ItinDetailCell*)[tableView dequeueReusableCellWithIdentifier:@"ItinDetailCell"];
        if (cell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[ItinDetailCell class]])
                    cell = (ItinDetailCell *)oneObject;
        }
        
        cell.ivDot.hidden = YES;
        
        [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
        

        if ([sectionName isEqualToString:KSECTION_GOV_TA_FIELDS])
        {
            GovTAField * fld = [self.taFields objectAtIndex:row];
            cell.lblLabel.text = fld.label;
            cell.lblValue.text = fld.fieldValue;

            if ([fld.access isEqualToString:@"RO"])
                [cell setAccessoryType:UITableViewCellAccessoryNone];
            return cell;
        }
        
        if (row == 1 || row == 2)
        {
            NSString *dateLabel;
            NSDate *date;
            
            if (row == 1)
            {
                dateLabel = [Localizer getLocalizedText:@"Check-in"];
                date = hotelSearch.hotelSearchCriteria.checkinDate;
            }
            else
            {
                dateLabel = [Localizer getLocalizedText:@"Check-out"];
                date = hotelSearch.hotelSearchCriteria.checkoutDate;
            }

            cell.lblLabel.text = dateLabel;        
            cell.lblValue.text = [DateTimeFormatter formatHotelOrCarDateForBooking:date];
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        }
        else if (row == 0) //kSectionLocation == section
        {
            NSString* location = (hotelSearch.hotelSearchCriteria.locationResult == nil ? @"" : hotelSearch.hotelSearchCriteria.locationResult.location);
            cell.lblLabel.text = [Localizer getLocalizedText:@"HOTEL_VIEW_LOCATION"];        
            cell.lblValue.text = location;
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        }
        else if (row >= 3)//kSectionDistance == section
        {
            if (row == 3) // Find hotels with X miles
            {
                NSString* unit  = ([hotelSearch.hotelSearchCriteria.isMetricDistance boolValue] ? [Localizer getLocalizedText:@"HOTEL_VIEW_KILOMETERS"] : [Localizer getLocalizedText:@"HOTEL_VIEW_MILE"]);
                NSString* units = ([hotelSearch.hotelSearchCriteria.isMetricDistance boolValue] ? [Localizer getLocalizedText:@"HOTEL_VIEW_KILOMETERS"] : [Localizer getLocalizedText:@"miles"]);

                int iDistanceValue = [hotelSearch.hotelSearchCriteria.distanceValue intValue];

                NSString* distanceValueString = (iDistanceValue == 100) ? [Localizer getLocalizedText:@"Greater than 25"] : [hotelSearch.hotelSearchCriteria.distanceValue stringValue];
                NSString* cellValue = [NSString stringWithFormat:[Localizer getLocalizedText:@"HOTEL_VIEW_NUMBER_SPACE_KILOMETERS_OR_MILES"], distanceValueString, (iDistanceValue > 1 ? units : unit)];
                
                cell.lblLabel.text = [Localizer getLocalizedText:@"HOTEL_VIEW_FIND_HOTELS_WITHIN"];        
                cell.lblValue.text = cellValue;
                cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            }
            else // Containing the words
            {
                cell.lblLabel.text = [Localizer getLocalizedText:@"HOTEL_VIEW_WITH_NAMES_CONTAINING"];        
                cell.lblValue.text = hotelSearch.hotelSearchCriteria.containingWords;
                cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            }
        }
        else if (kSectionSmoking == section)
        {
            NSString* smokingPreference = (hotelSearch.hotelSearchCriteria.smokingPreferenceNames)[hotelSearch.hotelSearchCriteria.smokingIndex];
            cell.lblLabel.text = [Localizer getLocalizedText:@"HOTEL_VIEW_SMOKING_PREFERENCE"];        
            cell.lblValue.text = smokingPreference;
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        }
        
        return cell;
    }
}


-(CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString* sectionName = aSections[[indexPath section]];

    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS])
        return 65;
    else
        return 50;
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
            [GovDutyLocationVC showDutyLocationVC:self withCompletion:nil withFields:self.taFields withDelegate:self withPerDiemRate:NO asRoot:NO];
        }
    }
    else
    {
        if (row == 1 || row == 2) // kSectionDate == section)
        {
            if ([UIDevice isPad]) 
            {
                [self pickerDateTapped:self IndexPath:indexPath];
                return;
            }
            else {
                DateSpanViewController * vc = [[DateSpanViewController alloc] initWithNibName:@"DateSpanViewController" bundle:nil];
                vc.dateSpanDelegate = self;
                [vc initStartDate:hotelSearch.hotelSearchCriteria.checkinDate endDate:hotelSearch.hotelSearchCriteria.checkoutDate selectStartDate:(1 == row) title:hotelSearch.hotelSearchCriteria.locationResult.location];

                [[ConcurMobileAppDelegate findHomeVC] presentViewController:vc animated:YES completion:nil];
            }
        }
        else if (row == 0)// kSectionLocation == section)
        {
            HotelLocationViewController * vc = [[HotelLocationViewController alloc] initWithNibName:@"HotelLocationViewController" bundle:nil];
            vc.locationDelegate = self;
            vc.neverShowOffices = NO;
            if (hotelSearch.hotelSearchCriteria != nil && hotelSearch.hotelSearchCriteria.locationResult != nil && hotelSearch.hotelSearchCriteria.locationResult.location != nil)
                vc.initialSearchLocation = hotelSearch.hotelSearchCriteria.locationResult;
            
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
        else if (row >= 3) //kSectionDistance == section)
        {
            if (row == 3) // Find hotel within X miles
            {
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:hotelSearch.hotelSearchCriteria.distanceValue, @"DISTANCE_VALUE", hotelSearch.hotelSearchCriteria.isMetricDistance, @"IS_METRIC_DISTANCE", @"YES", @"SHORT_CIRCUIT", nil];
                
                DistanceViewController *nextController = [[DistanceViewController alloc] initWithNibName:@"DistanceViewController" bundle:nil];
                Msg *msg = [[Msg alloc] init];
                msg.parameterBag = pBag;
                msg.idKey = @"SHORT_CIRCUIT";
                [nextController respondToFoundData:msg];
                [self.navigationController pushViewController:nextController animated:YES];
            }
            else // containing the words 
            {
                NSString *customTitle = [Localizer getLocalizedText:@"HOTEL_VIEW_HOTEL_NAME_RESTRICTIONS"];
                NSString *placeholder = [Localizer getLocalizedText:@"HOTEL_VIEW_HOTEL_NAME_RESTRICTIONS_PLACEHOLDER_TEXT"];
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"FROM_VIEW", placeholder, @"PLACEHOLDER", customTitle, @"TITLE", @"YES", @"SHORT_CIRCUIT", nil];

                NSString *words = hotelSearch.hotelSearchCriteria.containingWords;
                if (words != nil)
                    pBag[@"TEXT"] = words;
                
                HotelTextEditorViewController *nextController = [[HotelTextEditorViewController alloc] initWithNibName:@"HotelTextEditorViewController" bundle:nil];
                // set a reference inside the new VC to the current VC so that values can be passed back easily
                [nextController setParentVC:self ];
                Msg *msg = [[Msg alloc] init];
                msg.parameterBag = pBag;
                msg.idKey = @"SHORT_CIRCUIT";
                [nextController respondToFoundData:msg];
                [self.navigationController pushViewController:nextController animated:YES];
            }
        }
        else if (kSectionSmoking == section)
        {
            NSString *optionsViewTitle = [Localizer getLocalizedText:@"HOTEL_VIEW_SELECT_PREFERENCE"];
            NSString *optionType = @"SMOKING_PREFERENCE";
            NSArray *labels = hotelSearch.hotelSearchCriteria.smokingPreferenceNames;
            NSNumber *selectedRowIndex = [NSNumber numberWithInteger:hotelSearch.hotelSearchCriteria.smokingIndex];

            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"FROM_VIEW", optionType, @"OPTION_TYPE_ID", optionsViewTitle, @"TITLE", labels, @"LABELS", selectedRowIndex, @"SELECTED_ROW_INDEX", @"YES", @"SHORT_CIRCUIT", nil];
            
            HotelOptionsViewController *nextController = [[HotelOptionsViewController alloc] initWithNibName:@"HotelOptionsViewController" bundle:nil];
            nextController.optionTitle = optionsViewTitle;

            Msg *msg = [[Msg alloc] init];
            msg.parameterBag = pBag;
            msg.idKey = @"SHORT_CIRCUIT";
            [nextController respondToFoundData:msg];
            [self.navigationController pushViewController:nextController animated:YES];
        }
    }
}


#pragma mark -
#pragma mark DateSpan delegate methods

-(void)setDateSpanFrom:(NSDate*)startDate to:(NSDate*)endDate
{
	hotelSearch.hotelSearchCriteria.checkinDate = startDate;
	hotelSearch.hotelSearchCriteria.checkoutDate = endDate;
    // MOB-17430 Log entry added to aid in diagnosing a problem we have with historical searches being submitted
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelViewController::setDateSpanFrom: [DATECHECK] checkinDate value %@", hotelSearch.hotelSearchCriteria.checkinDate] Level:MC_LOG_DEBU];
}


#pragma mark -
#pragma mark Location delegate methods

-(void)locationSelected:(LocationResult*)locationResult tag:(NSString*)tag;
{
	hotelSearch.hotelSearchCriteria.locationResult = locationResult;
	[self makeSearchButton];
	
	if([UIDevice isPad])
		[tblView reloadData];
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload
{
    [self setEvaBtnView:nil];
    [self setBtnEvaSearch:nil];
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
    
    self.viewSearching = nil;
    self.lblSearchTo = nil;
    self.lblSearchTitle = nil;
    self.lblSearchFrom = nil;
}




#pragma mark -
#pragma mark PopOver Methods
- (void)pickerTimeTapped:(id)sender IndexPath:(NSIndexPath *)indexPath
{
//	if(pickerPopOver != nil)
//		[pickerPopOver dismissPopoverAnimated:YES];
//	
//	if(pickerPopOverVC != nil)
//		[pickerPopOverVC release];
//	
//	pickerPopOverVC = [[DateTimePopoverVC alloc] initWithNibName:@"DateTimePopoverVC" bundle:nil];
//	pickerPopOverVC.isDate = NO;
//	pickerPopOverVC.delegate = self;
//	pickerPopOverVC.indexPath = indexPath;
//	pickerPopOverVC.view;
//	
//	self.pickerPopOver = [[[UIPopoverController alloc] initWithContentViewController:pickerPopOverVC] autorelease];  
//	
//	int section = [indexPath section];
//	//int row = [indexPath row];
//	
////	NSInteger initialExtendedHour = (kSectionPickup == section ? carSearchCriteria.pickupExtendedHour : carSearchCriteria.dropoffExtendedHour);
////	
////	self.pickerPopOver = [[[UIPopoverController alloc] initWithContentViewController:pickerPopOverVC] autorelease];               
////	[pickerPopOverVC initPicker:initialExtendedHour];
//	
//	CGRect cellRect = [tblView rectForRowAtIndexPath:indexPath];
//	CGRect myRect = [self.view convertRect:cellRect fromView:tblView];
//	
//    [self.pickerPopOver presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionLeft animated:YES]; 
}


- (void)pickerDateTapped:(id)sender IndexPath:(NSIndexPath *)indexPath
{
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
	
	
	pickerPopOverVC = [[DateTimePopoverVC alloc] initWithNibName:@"DateTimePopoverVC" bundle:nil];
	pickerPopOverVC.isDate = YES;
	pickerPopOverVC.delegate = self;
	pickerPopOverVC.indexPath = indexPath;
	
	pickerPopOverVC.datePicker.timeZone = [NSTimeZone localTimeZone];
	pickerPopOverVC.datePicker.maximumDate = [NSDate dateWithTimeIntervalSinceNow:(60.0 * 60.0 * 24.0 * 365.0)];
	pickerPopOverVC.datePicker.minimumDate = [NSDate date];
	if(indexPath.row == 2) //checkout
		pickerPopOverVC.datePicker.minimumDate = [self addDaysToDate:hotelSearch.hotelSearchCriteria.checkinDate NumDaysToAdd:1]; 
	
	//int section = [indexPath section];
	NSInteger row = [indexPath row];
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:pickerPopOverVC];   
	
	NSDate *dasDate = (row == 1 ? hotelSearch.hotelSearchCriteria.checkinDate : hotelSearch.hotelSearchCriteria.checkoutDate);
	
	//NSInteger initialExtendedHour = (kSectionPickup == section ? carSearchCriteria.pickupExtendedHour : carSearchCriteria.dropoffExtendedHour);
	
	[pickerPopOverVC initDate:dasDate];
	
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


- (void)pickedDate:(NSDate *)dateSelected
{
	//int section = [pickerPopOverVC.indexPath section];
	NSInteger row = [pickerPopOverVC.indexPath row];
	
	//	NSMutableArray *sectionValues = [aList objectAtIndex:section];
	//	BookingCellData *bcd = [sectionValues objectAtIndex:row];
	//	bcd.dateValue = dateSelected;
	//	bcd.val = [DateTimeFormatter formatDateForBooking:bcd.dateValue];
	
	if(row == 1)
	{
		//hotelSearch.hotelSearchCriteria.checkinDate = dateSelected;

		double dDayDiff = [hotelSearch.hotelSearchCriteria.checkoutDate timeIntervalSinceDate:hotelSearch.hotelSearchCriteria.checkinDate];
		
		NSNumber *numDayDiff = @(((dDayDiff / 60) /60) / 24);
		NSDate *newDropDate = [self addDaysToDate:dateSelected NumDaysToAdd:[numDayDiff intValue]];
			
		hotelSearch.hotelSearchCriteria.checkinDate = dateSelected;
		hotelSearch.hotelSearchCriteria.checkoutDate = newDropDate;

        // MOB-17430 Log entry added to aid in diagnosing a problem we have with historical searches being submitted
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelViewController::pickedDate: [DATECHECK] checkinDate value %@", hotelSearch.hotelSearchCriteria.checkinDate] Level:MC_LOG_DEBU];

	}
	else 
		hotelSearch.hotelSearchCriteria.checkoutDate = dateSelected;
    
    [self.tblView reloadData];
	
//	NSUInteger _path[2] = {section, row};
//	NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
//	NSArray *_indexPaths = [[NSArray alloc] initWithObjects:_indexPath, nil];
//	[_indexPath release];
//	[tblView reloadRowsAtIndexPaths:_indexPaths withRowAnimation:NO];
//	[_indexPaths release];
//	
//	if(row == 0)
//	{
//		NSUInteger _path[2] = {section, row + 1};
//		NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
//		NSArray *_indexPaths = [[NSArray alloc] initWithObjects:_indexPath, nil];
//		[_indexPath release];
//		[tblView reloadRowsAtIndexPaths:_indexPaths withRowAnimation:NO];
//		[_indexPaths release];
//	}
}

//
//- (void)pickedItem:(NSInteger)pickedTime
//{
//}


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
        NSUInteger govSection = [self.aSections indexOfObject:KSECTION_GOV_TA_FIELDS];
        if (govSection != NSNotFound)
        {
            NSIndexSet *indexSet = [NSIndexSet indexSetWithIndex:govSection];
            [self.tblView reloadSections:indexSet withRowAnimation:UITableViewRowAnimationFade];
        }
    }
}

-(void) fieldCanceled:(FormFieldData *)field
{
}

+ (void) showHotelVC:(UINavigationController*)navi withTAFields:(NSArray*) taFlds
{
    HotelViewController* vc = [[HotelViewController alloc] initWithNibName:@"HotelViewController" bundle:nil];
    vc.taFields = [NSMutableArray arrayWithArray:taFlds];
    if ([Config isGov])
    {
        GovTAField* authFld = [GovTAField getAuthField:vc.taFields];
        if (authFld.useExisting) // coming from add Hotel
            [vc setHideCustomFields:YES];

        GovTAField* perdiemFld = [GovTAField getPerDiemField:vc.taFields];
        
        NSMutableDictionary *pBag = perdiemFld.tripDefaults;

        Msg *msg = [[Msg alloc] init];
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        [vc respondToFoundData:msg];
    }

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


#pragma mark - show Evature

- (IBAction)ShowEvaSearchUI:(id)sender
{
    //MOB-15527 - Starting iOS7, user needs to set mic permission for each app.
    // Check permission and prompt user to change setting if its not turned on
    if([[AVAudioSession sharedInstance] respondsToSelector:@selector(requestRecordPermission:)])
    {
        [[AVAudioSession sharedInstance] requestRecordPermission:^(BOOL granted)
        {
            if (granted) {
                // Microphone enabled code
                NSLog(@"HotelViewController: Microphone is enabled..");
                //MOB-15802 - Show search window in main thread.
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self  showVoiceSearchVC];
                });       
            }
            else {
                // Microphone disabled code
                NSLog(@"HotelViewController: Microphone is disabled..");
                
                // We're in a background thread here, so jump to main thread to do UI work.
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[[UIAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Microphone Access Denied"]
                                                message:[Localizer getLocalizedText:@"This feature requires access"]
                                                delegate:nil
                                       cancelButtonTitle:@"Dismiss"
                                       otherButtonTitles:nil] show];
                    // Return without doing anything.
                    return ;
                }); // End alert code
            }
        }]; // End requestRecordPermission
        
    }
    else
    {
     	NSLog(@"HotelViewController: iOS6 - Mic requestRecordPermission not found ");
        [self  showVoiceSearchVC];
    }

}

// TODO : Possibly move this display method into the voice UI viewcontroller itself.
-(void) showVoiceSearchVC
{
    if ([Config isNewVoiceUIEnabled] ) // show new voice UI for iOS 7 
    {
        UINavigationController *nav = [[UIStoryboard storyboardWithName:@"EvaVoiceSearch_iPhone" bundle:nil]
                                       instantiateInitialViewController];
        
        EvaVoiceSearchViewController *c = [nav viewControllers][0];
        c.category = EVA_HOTELS;
        // MOB-18960 - Evature : Enable new Voice UI for HotelSearch
        // Do a push here instead of presentviewcontroller so as to avoid dealing with SwitchtoDetail hell after reserve is complete.
        // This is temporary only will decide if we have to user present or push when new UI screens for search are added.
        [self.navigationController pushViewController:c animated:YES];
    }
    else
    {
        UINavigationController *navi = [[UIStoryboard storyboardWithName:@"EvaVoiceSearchiOS6" bundle:nil] instantiateInitialViewController];
        EvaVoiceSearchiOS6TableViewController *ctrl = [navi viewControllers][0];
        ctrl.inputSearchCategory = EVA_HOTELS;
       
        [self.navigationController pushViewController:ctrl animated:NO];
        // Below code displayed the voice search in modal.
        // If voice search is displayed in modal then on ipad the manual search window becomes full screen.
        //    [navi setModalPresentationStyle:UIModalPresentationFormSheet];
        //    navi.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];
        //    [self presentViewController:navi animated:YES completion:^{
        //                    [ctrl.cancel setTarget:self];
        //                    [ctrl.cancel setAction:@selector(dismissEVASearch)];
        //                }];
    }

}


- (void)dismissEVASearch
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

@end

