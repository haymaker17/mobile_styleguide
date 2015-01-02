
//  HotelBookingViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FeedbackManager.h"
#import "HotelBookingViewController.h"
#import "ExSystem.h" 

#import "RoomListSummaryCell.h"
#import "DownloadUserConfig.h"
#import "UserConfig.h"
#import "CreditCard.h"
#import "HotelSearch.h"
#import "HotelResult.h"
#import "HotelSearchResultsViewController.h"
#import "HotelDetailedMapViewController.h"
#import "HotelDetailsVariableHeightCell.h"
#import "HotelInfo.h"
#import "HotelViolation.h"
#import "RoomResult.h"
#import "HotelReservationRequest.h"
#import "HotelReservationResponse.h"
#import "ReserveHotel.h"
#import "FormatUtils.h"
#import "SystemConfig.h"
#import "ViolationReason.h"
#import "TripsData.h"
#import "TripData.h"
#import "LabelConstants.h"
#import "BookingChainCell.h"
#import "BookingImageCell.h"
#import "ItinDetailsCellLabel.h"
#import "DateTimeFormatter.h"
#import "HotelSearchCriteria.h"
#import "HotelCreditCardViewController.h"
#import "HotelOptionsViewController.h"
#import "HotelTextEditorViewController.h"
#import "PreSellOptions.h"

#import "MobileAlertView.h"
#import "iPadHomeVC.h"
#import "DetailViewController.h"

#import "HotelBookingCell.h"
#import "ViolationDetailsVC.h"
#import "CancellationDetailsVC.h"
#import "PolicyViolationConstants.h"
#import "TravelViolationReasons.h"
#import "ManageViolationsVC.h"

//Custom Fields
#import "BoolEditCell.h"
#import "CustomFieldTextEditor.h"
#import "FieldOptionsViewController.h"

#import "GovTAField.h"

@interface HotelBookingViewController ()
@property (nonatomic, strong) NSArray *rowsInViolationSection;
-(void) fetchCustomFields;
-(void) completeReservation;
-(BOOL) hasDisallowedViolations;

@end

@implementation HotelBookingViewController


@synthesize hotelSearch, ivStars, hotelRezResponse;
@synthesize creditCardIndex;
@synthesize updatingItineraryView;
@synthesize violationReasons;
@synthesize violationReasonLabels;
@synthesize updatingItineraryLabel;

//@synthesize hotelSummaryDelegate;
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
@synthesize imageViewerMulti, hotelBooking, hotelBookingRoom, tableList, aSections, hideCustomFields, tcfRows, selectedCustomField, editedDependentCustomField, viewSegmentHeader, dictSections, isDirty;
@synthesize taFields;

#define		kAlertViewRateApp	101781

#define kSectionSummary @"SummarySection"
#define kSectionFinancial @"FinancialSection"
#define kSectionViolation @"ViolationSection"
#define kSectionCustomFields @"CustomFieldsSection"

#define kRoomDescriptionRow 0
#define	kRowHotelPhone 1
#define kRowHotelAddress 2

#define kCheckinDateRow 0
#define kCheckoutDateRow 1
#define kRoomRateRow 2
#define kCreditCardRow 3
#define kCancellationPolicyRow 4

#define kViolationDescriptionRow 0
#define kViolationReasonRow 1
#define kViolationJustificationRow 2

#define kRowManageViolations @"RowManageViolations"
#define kRowUsingPointsViolations @"RowUsingPointsViolations"
#define kRowViolationsText @"RowViolationsText"
#define kRowViolationReason @"RowViolationReason"
#define kRowViolationJustification @"RowViolationJustification"

#define kAlertAreYouSure			132089
#define kAlertReservationSucceeded	132090
#define kAlertReservationFailed		132091
#define kAlertUnhandledViolations	132092
#define kAlertGropuAuthUsed         300200

#pragma mark -
#pragma mark MobileViewController Methods

-(NSString *)getViewIDKey
{
	return HOTEL_BOOKING;
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
    
    if (hotelSearch.tripKey != nil)
    {
        tripKey = hotelSearch.tripKey;
        trip = [[TripManager sharedInstance] fetchByTripKey:tripKey];//[tripsData.trips objectForKey:tripKey];
        
        // The trip view is already on the view stack, so just pop until we get back to it.
        pBag[@"POPUNTILVIEW"] = @"YES";
        pBag[@"DONTPUSHVIEW"] = @"YES";
        
        if([UIDevice isPad])
        {
            //ok, we need to actually refresh the trips view, because we came here from a trip
            iPadHomeVC *padHome = [ConcurMobileAppDelegate findiPadHomeVC];
//            padHome.tripsData = tripsData;
            DetailViewController *dvc = (padHome.homeViews)[@"Trip"];
            if (dvc != nil && [dvc isKindOfClass:[DetailViewController class]])
            {
//                dvc.tripsData = tripsData;
                //					dvc.trip = trip;
                [dvc displayTrip:trip TripKey:tripKey];
            }
            else
                [padHome popHome:self];
            [self dismissViewControllerAnimated:YES completion:nil];
        }

    }
    else
    {
        trip = [[TripManager sharedInstance] fetchByItinLocator:itinLocator];//[tripsData getTripWithLocator:recordLocator];
        tripKey = trip.tripKey;
        
        // We came here from the home screen, so pop all the way back to it before going to the trip view.
        pBag[@"POP_TO_ROOT_VIEW"] = @"YES";
    }
    
    
    if (trip != nil && tripKey != nil)
    {
        pBag[@"TRIP"] = trip;
        pBag[@"TRIP_KEY"] = tripKey;
        
        UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
        // In iOS 8 onwards, Home is displayed for a split-second before TripDetailsVC is pushed over it. This causes viewDidAppear on Home9VC to be triggered which in-turn reloads Trips data. This causes MOB-21531 and an app crash
        if (![ExSystem is8Plus] && [homeVC respondsToSelector:@selector(refreshTripsData)])
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
             [ConcurMobileAppDelegate switchToView:TRIP_DETAILS viewFrom:HOTEL_BOOKING ParameterBag:pBag];
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
		if ((msg.parameterBag)[@"HOTEL_SEARCH"] != nil)
		{
			self.hotelSearch = (HotelSearch*)(msg.parameterBag)[@"HOTEL_SEARCH"];
            self.hotelBooking = (msg.parameterBag)[@"HOTEL_BOOKING"];
            self.hotelBookingRoom = (msg.parameterBag)[@"HOTEL_ROOM"];
            self.travelPointsInBank = (msg.parameterBag)[@"TRAVEL_POINTS_IN_BANK"];
            
            [self populateSections];
            
			// If a credit card has not already been selected and there is at least one card to choose from,
			// then select the first card.
			if (self.creditCardIndex == nil && [self.creditCards count])
			{
                self.creditCardIndex = @0;
			}
			
            [self updateViolationReasons];
            [self fetchPreSellOptions];
//			[tableList reloadData]; // Commented as same calls are being made below
//          [self makeHeader];
//			
//			[self configureConfirmButton];
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
			ViolationReason *reason = violationReasons[selectedRowIndex];
            if(hotelBookingRoom.relHotelViolationCurrent == nil)
                hotelBookingRoom.relHotelViolationCurrent = [[HotelBookingManager sharedInstance] makeNewViolation];
            
            hotelBookingRoom.relHotelViolationCurrent.code = reason.code;
            hotelBookingRoom.relHotelViolationCurrent.message = reason.description;
            hotelBookingRoom.relHotelViolationCurrent.violationType = reason.violationType;
            [[HotelBookingManager sharedInstance] saveIt:hotelBooking];
		}
		else if ((msg.parameterBag)[@"TEXT"] != nil)
		{
            hotelBookingRoom.violationJustification = (NSString*)(msg.parameterBag)[@"TEXT"];
		}
        else if ((msg.parameterBag)[@"USE_TRAVEL_POINTS"] != nil)
		{
            hotelBookingRoom.isUsingPointsAgainstViolations = (NSNumber*)(msg.parameterBag)[@"USE_TRAVEL_POINTS"];
		}
		
		[tableList reloadData];
        [self makeHeader];
		
		[self configureConfirmButton];
	}
    else if ([msg.idKey isEqualToString:PRE_SELL_OPTIONS])
    {
        self.isPreSellOptionsLoaded = YES;
        PreSellOptions *preSellOptions = (PreSellOptions *)msg.responder;
        
        self.cancellationPolicyNeedsViewing = NO;
        if ([preSellOptions.cancellationPolicyLines count])
        {
            self.cancellationPolicyText = [preSellOptions.cancellationPolicyLines componentsJoinedByString:@"\n"];
            // This line has been commented out due to initial feedback from UX team
            // They have requested to look into their own approach for ensuring that customers read the policy
            // but we can keep the display of the policy in. They may change their mind, so we'll leave the code in
            // and just disable it for now by commenting this line out.
            //self.cancellationPolicyNeedsViewing = YES;
        }
        self.creditCards = preSellOptions.creditCards;
        
        if (self.creditCardIndex == nil && [self.creditCards count])
            self.creditCardIndex = @0;
        [tableList reloadData];
        [self configureConfirmButton];
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
            [aSections removeObject:kSectionCustomFields]; // removes the instance if any
            [aSections addObject:kSectionCustomFields];
            self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAllFieldsAtStart:NO];
            [dictSections removeObjectForKey:kSectionCustomFields];
            dictSections[kSectionCustomFields] = tcfRows;
            [tableList reloadData];
        }
    }
	else if ([msg.idKey isEqualToString:RESERVE_HOTEL])
	{
		ReserveHotel *reserveHotel = (ReserveHotel *)msg.responder;
		[self showHotelReservationResponse:reserveHotel.hotelReservationResponse];
        
        if ([self isViewLoaded]) 
            [self hideWaitView];

	}
	else if ([msg.idKey isEqualToString:TRIPS_DATA] && (msg.parameterBag)[@"ITIN_LOCATOR"])
	{
		[updatingItineraryView setHidden:YES];
		
        if (hotelSearch.tripKey != nil && ![UIDevice isPad])
        {
            // MOB-9566 refresh TripsData for both TripDetails and Trips view.
            [TripsViewController refreshViewsWithTripsData:msg fromView:self];
            [self switchToTripDetailView:nil];
        }
        else
        {
            NSString * itinLocator = nil;
            if (hotelSearch.tripKey == nil)
                itinLocator = (NSString*)(msg.parameterBag)[@"ITIN_LOCATOR"];
            [self switchToTripDetailView:itinLocator];
        }
		
	}
    else if ([msg.idKey isEqualToString:TRIPS_DATA])
    {
        NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.hotelRezResponse.recordLocator, @"RECORD_LOCATOR",[self getViewIDKey], @"TO_VIEW",self.hotelRezResponse.itinLocator,@"ITIN_LOCATOR", nil];
        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

- (void)initData:(NSMutableDictionary*)paramBag
{
	if (paramBag != nil)
	{
		HotelSearchCriteria *hotelSearchCriteria = (HotelSearchCriteria*)paramBag[@"HOTEL_SEARCH_CRITERIA"];
		if (hotelSearchCriteria != nil)
		{
			[self makeToolbar:hotelSearchCriteria];
		}
	}
	
	if (paramBag == nil || paramBag[@"POPTOVIEW"] == nil)
		self.hotelSearch = nil;
}


#pragma mark -
#pragma mark Alerts
#pragma mark App Rating Methods
-(void)afterChoiceToRateApp
{
    if (self.hotelRezResponse.recordLocator && self.hotelRezResponse.itinLocator)
    {
        [updatingItineraryView setHidden:NO];
        [self.view bringSubviewToFront:updatingItineraryView];
        
        NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.hotelRezResponse.recordLocator, @"RECORD_LOCATOR", [self getViewIDKey], @"TO_VIEW",nil];
        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

-(void)showHotelReservationResponse:(HotelReservationResponse*)hotelReservationResponse
{
	if ([hotelReservationResponse.status isEqualToString:@"SUCCESS"])
	{
        self.hotelRezResponse = hotelReservationResponse;
        
        //[AppRating offerChoiceToRateApp:self alertTag:kAlertViewRateApp];
        // DISABLE feedback manager for Gov
        if (![Config isGov])
        {
            [[FeedbackManager sharedInstance] requestRatingFromViewController:self withBlock:^{
                [self afterChoiceToRateApp];
            }];
        }
        //MOB-17159 Gov app not responding after booking hotel
        else
        {
            [self afterChoiceToRateApp];
        }
	}
	else // failure
	{
		MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Error"]
                                                                message:(hotelReservationResponse.errorMessage && hotelReservationResponse.errorMessage.length) ? hotelReservationResponse.errorMessage : [@"HOTEL_BOOKING_VIEW_BOOKING_ERROR_MESSAGE" localize]
                                                               delegate:self
                                                      cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
                                                      otherButtonTitles:nil];
		alert.tag = kAlertReservationFailed;
		[alert show];
	}
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if (alertView.tag == kAlertReservationFailed)
	{
		[self configureConfirmButton];
		
		// TODO: what do we want to do when a car booking fails?  As it is, we just stay on the booking screeen.
	}
	else if (alertView.tag == kAlertAreYouSure)
	{
		if (buttonIndex == 1)
			[self requestReservation];
	}
    else if (alertView.tag == kAlertGropuAuthUsed && buttonIndex == alertView.cancelButtonIndex)
    {
        UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
        // Force home screen refresh
        if ([homeVC respondsToSelector:@selector(refreshTripsData)])
        {
            [homeVC performSelector:@selector(refreshTripsData) withObject:nil];
        }

        if ([UIDevice isPad])
        {
            [self dismissViewControllerAnimated:NO completion:nil];
        }
        else
        {
            [self.navigationController popToRootViewControllerAnimated:YES];
         }
    }
}


#pragma mark -
#pragma mark Confirm

-(void)configureConfirmButton
{
	self.navigationItem.rightBarButtonItem = nil;
	
	if (creditCardIndex != nil)
	{
		NSString* title = [Localizer getLocalizedText:@"Reserve"];

		UIBarButtonItem *confirmBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:title style:UIBarButtonItemStyleBordered target:self action:@selector(btnConfirm:)];
		self.navigationItem.rightBarButtonItem = confirmBarButtonItem;
	}
}

-(IBAction)btnConfirm:(id)sender
{
//    BOOL hasNoDisallowedViolation = ![self hasDisallowedViolations];
//    BOOL hasViolationReason = [self getViolationReason] == nil;
//    BOOL hasViolationJustification = [[self getViolationJustification] length];
    if (isDirty && editedDependentCustomField)
    {
        // MOB-9648 Prevent preserve button, in between child screen and wait view
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[@"PENDING_REQUIRED_TRAVEL_CUSTOM_FIELDS" localize] delegate:nil cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles:nil];
        [alert show];
        
        return;
    }
    else if ([self hasPendingRequiredTripFields]) 
    {
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[@"PENDING_REQUIRED_TRAVEL_CUSTOM_FIELDS" localize] delegate:nil cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles:nil];
        [alert show];
        
        return;
    }
	else if (![self isBookingAllowed])
    {
 		MobileAlertView *alert = [[MobileAlertView alloc] 
                                  initWithTitle:[Localizer getLocalizedText:@"Reservation Not Allowed"]
                                  message:[Localizer getLocalizedText:@"Your company's travel policy will not allow this reservation."]
                                  delegate:nil 
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                  otherButtonTitles:nil];
		[alert show];
    }
//    else if(hasNoDisallowedViolation ||
//            (hasViolationReason &&
//             (hasViolationJustification && ![SystemConfig getSingleton].ruleViolationExplanationRequired) ) )
//    {
//		MobileAlertView *alert = [[MobileAlertView alloc]
//                                  initWithTitle:[Localizer getLocalizedText:@"HOTEL_BOOKING_VIEW_CONFIRM_BOOKING_TITLE"]
//                                 message:[Localizer getLocalizedText:@"HOTEL_BOOKING_VIEW_CONFIRM_BOOKING_MESSAGE"]
//                                  delegate:self
//                                  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
//                                  otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
//		alert.tag = kAlertAreYouSure;
//		[alert show];
//    }
    //MOB-10484
    else if(self.cancellationPolicyNeedsViewing)
    {
        MobileAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[@"HOTEL_BOOKING_VIEW_CANCELLATION_POLICY_TITLE" localize]
                                  message:[@"HOTEL_BOOKING_VIEW_CANCELLATION_POLICY_DESCRIPTION" localize]
                                  delegate:nil
                                  cancelButtonTitle:[LABEL_CLOSE_BTN localize]
                                  otherButtonTitles:nil];
        [alert show];
    }
 	else if (![self hasDisallowedViolations] || [self.hotelBookingRoom.isUsingPointsAgainstViolations boolValue] || ([self getViolationReason] != nil && !(![[self getViolationJustification] length] && [SystemConfig getSingleton].ruleViolationExplanationRequired ) ))
	{
        NSString *displayMessage = [hotelBookingRoom.depositRequired boolValue] ? [Localizer getLocalizedText:@"HOTEL_BOOKING_WITH_DEPOSIT_CONFIRM_BOOKING_MESSAGE"] : [@"HOTEL_BOOKING_VIEW_CONFIRM_BOOKING_MESSAGE" localize];
        if([Config isGov])
        {
            displayMessage = [Localizer getLocalizedText:@"Select 'OK' if you are sure you want to reserve this room."];
        }
		MobileAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"HOTEL_BOOKING_VIEW_CONFIRM_BOOKING_TITLE"]
							  message:displayMessage
							  delegate:self 
							  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
							  otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
		alert.tag = kAlertAreYouSure;
		[alert show];
	}
	else
	{
		NSString *msg = nil;
		
        if ([self.hotelBookingRoom.canUseTravelPoints boolValue] && !self.hotelBookingRoom.isUsingPointsAgainstViolations)
        {
            msg = [@"HOTEL_BOOKING_ADDRESS_MISSING_VIOLATIONS" localize];
        }
		else if ([self getViolationReason] == nil)
		{
             //MOB-10484 - check if justificaiton is required
			if (![[self getViolationJustification] length] &&  [SystemConfig getSingleton].ruleViolationExplanationRequired ) // MOB-8069
			{
				msg = [Localizer getLocalizedText:@"HOTEL_BOOKING_VIEW_MISSING_VIOLATION_REASON_AND_JUSTIFICATION"];
			}
			else
			{
				msg = [Localizer getLocalizedText:@"HOTEL_BOOKING_VIEW_MISSING_VIOLATION_REASON"];
			}
		}
		else if([SystemConfig getSingleton].ruleViolationExplanationRequired)
		{
			msg = [Localizer getLocalizedText:@"HOTEL_BOOKING_VIEW_MISSING_VIOLATION_JUSTIFICATION"];
		}
		// dont show alert if there is no message
        if(msg!=nil)
        {
            MobileAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"HOTEL_BOOKING_VIEW_MISSING_VIOLATION_INFO_TITLE"]
                                  message:msg
                                  delegate:nil 
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                  otherButtonTitles:nil];
            alert.tag = kAlertUnhandledViolations;
            [alert show];
        }
	}
}

-(void)requestReservation
{
    [self completeReservation];
}

-(void)completeReservation
{
	self.navigationItem.rightBarButtonItem = nil;	// Remove confirm button from toolbar
	
	NSUInteger ccIndex = [creditCardIndex integerValue];
	CreditCard* creditCard = [self.creditCards objectAtIndex:ccIndex];
	
	HotelReservationRequest* reservationRequest = [[HotelReservationRequest alloc] init];
	reservationRequest.bicCode = hotelBookingRoom.bicCode; // roomResult.bicCode;
	reservationRequest.creditCardId = (creditCard == nil ? @"" : creditCard.ccId);
	reservationRequest.hotelChainCode = hotelBooking.chainCode; // hotelResult.chainCode;
	reservationRequest.propertyId = [NSString stringByEncodingXmlEntities:hotelBooking.propertyId];// [NSString stringByEncodingXmlEntities:hotelResult.propertyId];
	reservationRequest.propertyName = [NSString stringByEncodingXmlEntities:hotelBooking.hotel]; // [NSString stringByEncodingXmlEntities:hotelResult.hotel];
	reservationRequest.sellSource = hotelBookingRoom.sellSource; // roomResult.sellSource;
	reservationRequest.tripKey = hotelSearch.tripKey;
    reservationRequest.isUsingTravelPointsAgainstViolations = [self.hotelBookingRoom.isUsingPointsAgainstViolations boolValue];
    
    if (hotelBookingRoom.relHotelViolationCurrent != nil)
        reservationRequest.violationCode = hotelBookingRoom.relHotelViolationCurrent.code;
    
    reservationRequest.violationJustification = hotelBookingRoom.violationJustification;
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:reservationRequest, @"HOTEL_RESERVATION_REQUEST", nil];
    
    NSString *customFields = [TravelCustomFieldsManager makeCustomFieldsRequestXMLBody];
    
    if (customFields != nil) 
        pBag[@"TRAVEL_CUSTOM_FIELDS"] = customFields;
    
    if ([Config isGov])
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
            if (currentTAField.isUSContiguous)
            {
                [pBag setObject:@"US" forKey:@"GOV_PER_DIEM_COUNTRY"];
                [pBag setObject:currentTAField.perDiemLocState forKey:@"GOV_PER_DIEM_LOC_STATE"];
            }
            else
            {
                [pBag setObject:currentTAField.perDiemLocState forKey:@"GOV_PER_DIEM_COUNTRY"];
                //                [pBag setObject:@"" forKey:@"GOV_PER_DIEM_LOC_STATE"];
            }
            [pBag setObject:currentTAField.perDiemLocation forKey:@"PER_DIEM_LOCATION"];
            if (currentTAField.perDiemLocZip != nil)
                [pBag setObject:currentTAField.perDiemLocZip forKey:@"GOV_PER_DIEM_LOC_ZIP"];
        }
    }

	[[ExSystem sharedInstance].msgControl createMsg:RESERVE_HOTEL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES Options:NO_RETRY RespondTo:self];
    
    if(self.isVoiceBooking)
    {
    	//Record flurry event for voice booking 
        NSDictionary *dictionary = @{@"Type": @"Hotel"};
        [Flurry logEvent:FLURRY_VOICE_BOOK_COMPLETED withParameters:dictionary];

    }
    else
    {
        NSDictionary *dict = @{@"Type": @"Hotel", @"Booked From": hideCustomFields? @"Trip": @"Home"};
        [Flurry logEvent:@"Book: Reserve" withParameters:dict];
    }
    BOOL isRecommendedHotel = [hotelBooking.recommendationSource length] ? YES : NO;
    BOOL isAnyHotelRecommended = isRecommendedHotel || [[HotelBookingManager sharedInstance] isAnyHotelRecommended];
    NSDictionary *dict = @{@"Recommended": (isRecommendedHotel ? @"YES" : @"NO"), @"Search had recommendations" : (isAnyHotelRecommended ? @"YES" : @"NO"), @"Property ID": (hotelBooking.propertyId ?: @"")};
    [Flurry logEvent:@"Hotel Recommendations: Hotel Reserved" withParameters:dict];
    [self logFlurryEventsForTravelPoints];
    [self showWaitViewWithText: [Localizer getLocalizedText:@"Reserving Hotel Room"]];
}

- (void)logFlurryEventsForTravelPoints
{
    if([[UserConfig getSingleton].travelPointsConfig[@"HotelTravelPointsEnabled"] boolValue]) // Log only if earning points is enabled
    {
        NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
        if ([ExSystem sharedInstance].entitySettings.companyName)
            dict[@"User Company Name"] = [ExSystem sharedInstance].entitySettings.companyName;
        dict[@"Selected Positive Points"] = [self.hotelBookingRoom.travelPoints intValue] == 0 ? @"NA" : ([self.hotelBookingRoom.travelPoints intValue] > 0 ? @"YES" : @"NO");
        if (self.travelPointsInBank)
            dict[@"Travel Points In Bank"] = self.travelPointsInBank;
        if ([self.hotelBookingRoom.travelPoints intValue] > 0)
            dict[@"Travel Points Earned"] = self.hotelBookingRoom.travelPoints;
        if ([self.hotelBookingRoom.isUsingPointsAgainstViolations boolValue])
            dict[@"Travel Points Used"] = @(-[self.hotelBookingRoom.travelPoints intValue]);
        if (self.hotelBookingRoom.isUsingPointsAgainstViolations)
            dict[@"Use Travel Points Selected"] = [self.hotelBookingRoom.isUsingPointsAgainstViolations boolValue] ? @"YES" : @"NO";
        [Flurry logEvent:@"Price-to-Beat: Hotel Reserve" withParameters:dict];
    }
}

#pragma mark -
#pragma mark View lifecycle
- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    if (isDirty) 
    {
        if (editedDependentCustomField && [selectedCustomField.attributeValue length])
            [self updateDynamicCustomFields];
        else
            [self reloadCustomFieldsSection];
        
        isDirty = NO;// MOB-9648 hold off reserve action
    }
}

-(void)populateSections
{
    self.aSections = [[NSMutableArray alloc] initWithObjects: kSectionSummary, kSectionFinancial, nil];
    self.dictSections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    
    if([self getViolationsCount])
    {
        [aSections addObject:kSectionViolation];
    }
    
    //Custom Fields
    if (!hideCustomFields) 
    {
        // prepopulate the custom fields from cache
        [aSections addObject:kSectionCustomFields];
        self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAllFieldsAtStart:NO];
        dictSections[kSectionCustomFields] = self.tcfRows;
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];

	self.updatingItineraryLabel.text = [Localizer getLocalizedText:@"Updating Itinerary"];

	//Show page title on iphone
	//if([UIDevice isPad])
	self.title = [Localizer getLocalizedText:@"Reserving Hotel Room"];
    
    if (self.aSections == nil)
        self.aSections = [[NSMutableArray alloc] init];
    
    if (self.dictSections == nil)
        self.dictSections = [[NSMutableDictionary alloc] init];
    
    //Custom Fields
    if (!hideCustomFields) 
        [self fetchCustomFields];
    else 
        [self hideLoadingView];
    
    [tableList reloadData];
}

- (void)updateViolationReasons
{
    if (violationReasons == nil || [violationReasons count] == 0)
	{
		NSMutableArray *reasons = [[NSMutableArray alloc] init];
		NSMutableArray *labels = [[NSMutableArray alloc] init];
		
        TravelViolationReasons *travelViolationReasons = [TravelViolationReasons getSingleton];
        if (travelViolationReasons != nil && [travelViolationReasons.violationReasons count] > 0) {
            NSArray *hotelViolations = [hotelBookingRoom.relHotelViolation allObjects];
            
            NSMutableArray *violationTypes = [[NSMutableArray alloc] initWithObjects:nil];
            
            for (EntityHotelViolation *hotelViolation in hotelViolations) {
                if (hotelViolation.violationType != nil) {
                    [violationTypes addObject:hotelViolation.violationType];
                }
            }
            
            NSMutableArray *tmpReasons = [travelViolationReasons getReasonsFor:violationTypes];
            for (ViolationReason *reason in tmpReasons) {
                [reasons addObject:reason];
                [labels addObject:reason.description];
            }
            
        }
        
        /*
         SystemConfig *systemConfig = [SystemConfig getSingleton];
         if (systemConfig != nil && [systemConfig.hotelViolationReasons count] > 0)
         {
         NSArray* allKeys = [systemConfig.hotelViolationReasons allKeys];
         for (NSString *key in allKeys)
         {
         ViolationReason	*reason = [systemConfig.hotelViolationReasons objectForKey:key];
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
	[self configureConfirmButton];

	// No reason to show the wait view initially
	[self hideWaitView];
	
	// No reason to show the updating-itinerary-view initially
	[updatingItineraryView setHidden:YES];
	
	if([UIDevice isPad])
	{
		//Cover toolbar area on iPad
		updatingItineraryView.frame = CGRectMake(0, 0, 540, 620);
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
    return [aSections count];
//	return ([self getViolationsCount] == 0 ? 2 : 3); // Sections: hotel summary, financial, and violations (if available)
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString *sectionName = aSections[section];
    
	if ([kSectionSummary isEqualToString:sectionName])
		return 3;	// Rows: chain name, hotel name, hotel image, room description, hotel phone, hotel address
    else if([sectionName isEqualToString:kSectionCustomFields])
    {
        NSArray *a = dictSections[sectionName];
        return [a count];
    }
	else if ([kSectionFinancial isEqualToString:sectionName])
		return [self.cancellationPolicyText length] ? 5 : 4; // Rows: checkin date, checkout date, daily rate, credit card, cancellation policy (if needed)
	else if ([kSectionViolation isEqualToString:sectionName])
		return [self numberOfRowsInViolationsSection];
	else
		return 0;
}

-(NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    NSString *sectionName = aSections[section];
    
    if ([kSectionViolation isEqualToString:sectionName])
        return [@"Violations" localize];
    return nil;
}


- (UITableViewCell *)cellForViolationSection:(UITableView *)tableView row:(NSUInteger)row
{
    HotelBookingCell *cell = [self getHotelBookingSingleCell];
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    
    if ([kRowViolationsText isEqualToString:self.rowsInViolationSection[row]])
    {
        NSString *label = [self hasDisallowedViolations] ? [@"Violation" localize] : [@"Travel Policy" localize];
        NSString *value = [self getViolations];
        
        cell.lblLabel.text = label;
        cell.lblValue.text = value;
    }
    else if ([kRowViolationReason isEqualToString:self.rowsInViolationSection[row]])
    {
        NSString *reason = [self getViolationReason];
        
        NSString *label = [Localizer getLocalizedText:@"Violation Reason"];
        NSString *value = (reason != nil ? reason : [Localizer getLocalizedText:@"Please specify"]);
        
        cell.lblLabel.text = label;
        cell.lblValue.text = value;
        
        if (reason == nil)
            cell.lblValue.textColor = [UIColor redColor];
    }
    else if ([kRowViolationJustification isEqualToString:self.rowsInViolationSection[row]])
    {
        NSString *justification  = [self getViolationJustification];
        NSString *label = [Localizer getLocalizedText:@"Violation Justification"];
        NSString *value = ([justification length] ? justification : [Localizer getLocalizedText:@"Please specify"]);
        
        cell.lblLabel.text = label;
        cell.lblValue.text = value;
        
        // //MOB-10484
        if (![justification length] && [SystemConfig getSingleton].ruleViolationExplanationRequired)
            cell.lblValue.textColor = [UIColor redColor];
    }
    else if ([kRowManageViolations isEqualToString:self.rowsInViolationSection[row]])
    {
        UITableViewCell *singleLabelCell = [tableView dequeueReusableCellWithIdentifier:@"ManageViolationsCell"];
        if (!singleLabelCell) {
            singleLabelCell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"ManageViolationsCell"];
            singleLabelCell.textLabel.minimumScaleFactor = 0.6;
            singleLabelCell.textLabel.adjustsFontSizeToFitWidth = YES;
            singleLabelCell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        }
        
        if (self.hotelBookingRoom.isUsingPointsAgainstViolations)
        {
            singleLabelCell.textLabel.text = [@"Manage Violations" localize];
            singleLabelCell.textLabel.textColor = [UIColor grayColor];
        }
        else
        {
            singleLabelCell.textLabel.text = [@"Please address policy violations" localize];
            singleLabelCell.textLabel.textColor = [UIColor redColor];
        }
        return singleLabelCell;
    }
    else if ([kRowUsingPointsViolations isEqualToString:self.rowsInViolationSection[row]])
    {
        NSString *label = [Localizer getLocalizedText:@"Violations"];
        NSString *value = [NSString stringWithFormat:[@"You are using %@ Travel Points." localize],[@(abs([self.hotelBookingRoom.travelPoints intValue])) stringValue]];
        
        cell.lblLabel.text = label;
        cell.lblValue.text = value;
        cell.accessoryType = UITableViewCellAccessoryNone;
    }
    
    return cell;
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	
    NSString *sectionName = aSections[indexPath.section];
    if ([sectionName isEqualToString:kSectionCustomFields])
    {
        return [self configureCustomFieldCellAtIndexPath:indexPath];
    }
    else if ([kSectionSummary isEqualToString:sectionName])
	{
		if ((kRoomDescriptionRow) == row)
		{
			NSString *value = [self getRoomDescription];
			return [HotelDetailsVariableHeightCell makeCell:tableView owner:self cellLabel:nil cellValue:value allowDisclosure:NO];
		}
		else if ((kRowHotelPhone) == row)
		{
            HotelBookingCell *cell = [self getHotelBookingSingleCell];
            cell.lblLabel.text = [Localizer getLocalizedText:@"Phone"];
            cell.lblValue.text = hotelBooking.phone;
            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
			return cell; 
		}
		else if ((kRowHotelAddress) == row)
		{
			NSString *street;
			if (hotelSearch.selectedHotel.address2 == nil)
				street = hotelBooking.addr1;
			else
				street = [NSString stringWithFormat:@"%@\n%@", hotelBooking.addr1, hotelBooking.addr2];
			
            HotelBookingCell *cell = [self getHotelBookingSingleCell];
            cell.lblLabel.text = [Localizer getLocalizedText:@"Map Address"];
            cell.lblValue.text = street;
            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
            return cell; // [ItinDetailsCellLabel makeLocationCell:tableView location:address];
		}
    }
	else if ([kSectionFinancial isEqualToString:sectionName])
	{
        HotelBookingCell *cell = [self getHotelBookingSingleCell];
        
		if (kCheckinDateRow == row)
		{
			cell.lblLabel.text = [Localizer getLocalizedText:@"Check-in"];
			cell.lblValue.text = [DateTimeFormatter formatHotelOrCarDateForBooking:hotelSearch.hotelSearchCriteria.checkinDate];
			//return [ItinDetailsCellLabel makeCell:tableView cellLabel:label cellValue:value];
		}
		else if (kCheckoutDateRow == row)
		{
			cell.lblLabel.text = [Localizer getLocalizedText:@"Check-out"];
			cell.lblValue.text = [DateTimeFormatter formatHotelOrCarDateForBooking:hotelSearch.hotelSearchCriteria.checkoutDate];
			//return [ItinDetailsCellLabel makeCell:tableView cellLabel:label cellValue:value];
		}
		else if (kRoomRateRow == row)
		{
			cell.lblLabel.text = [Localizer getLocalizedText:@"HOTEL_BOOKING_VIEW_DAILY_RATE"];
			cell.lblValue.text = [self getRoomRate];
            cell.lblValue.textColor = [UIColor bookingBlueColor];
			if ([hotelBookingRoom.travelPoints intValue] != 0 && (!hotelBookingRoom.isUsingPointsAgainstViolations || [hotelBookingRoom.isUsingPointsAgainstViolations boolValue]))
            {
                cell.lblSubValue.hidden = NO;
                if ([hotelBookingRoom.travelPoints intValue] > 0) {
                    cell.lblSubValue.text = [NSString stringWithFormat:[@"Earn %d pts." localize],[hotelBookingRoom.travelPoints intValue]];
                    cell.lblSubValue.textColor = [UIColor bookingGreenColor];
                }
                else {
                    cell.lblSubValue.text = [NSString stringWithFormat:[@"Use %d pts." localize],-[hotelBookingRoom.travelPoints intValue]];
                    cell.lblSubValue.textColor = [UIColor bookingRedColor];
                }
            }
		}
		else if (kCreditCardRow == row)
		{

			cell.lblLabel.text = [Localizer getLocalizedText:@"Credit Card"];
			cell.lblValue.text = [self getCreditCard];
			//UITableViewCell *cell = [ItinDetailsCellLabel makeCell:tableView cellLabel:label cellValue:value];
            cell.accessoryType = [self canChooseCreditCard] ? UITableViewCellAccessoryDisclosureIndicator : UITableViewCellAccessoryNone;
		}
        else if (kCancellationPolicyRow == row)
        {
            cell.lblLabel.text = [Localizer getLocalizedText:@"Cancellation Policy"];
            cell.lblValue.text = [self getCancellationPolicy];
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            if (self.cancellationPolicyNeedsViewing)
            {
                cell.lblValue.textColor = [UIColor redColor];
            }

        }
        return cell;
	}
	else if ([kSectionViolation isEqualToString:sectionName])
	{
        return [self cellForViolationSection:tableView row:row];
	}
	return nil;
}

-(NSString*)getRoomRate
{
	return [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", [hotelBookingRoom.rate doubleValue]] crnCode:hotelBookingRoom.crnCode];
}

-(NSString*)getRoomDescription
{
	return hotelBookingRoom.summary; // hotelSearch.selectedHotel.detail.selectedRoom.summary;
}

-(NSString*)getCreditCard
{
	if (self.creditCards == nil || creditCardIndex == nil || [creditCardIndex integerValue] >= [self.creditCards count])
	{
		return self.isPreSellOptionsLoaded ? [Localizer getLocalizedText:@"Unavailable"] : [@"Loading..." localize];
	}
	else
	{
		CreditCard* card = self.creditCards[[creditCardIndex integerValue]];
		return card.name;
	}
}

-(NSString*)getCancellationPolicy
{
	if (self.cancellationPolicyText == nil)
	{
		return self.isPreSellOptionsLoaded ? [Localizer getLocalizedText:@"Unavailable"] : [@"Loading..." localize];
	}
	else
	{
		return [Localizer getLocalizedText:@"Please Select To View"];
	}
}

-(BOOL)canChooseCreditCard
{
    return [self.creditCards count] > 1;
}

-(bool)isBookingAllowed
{
	NSArray* violations = [hotelBookingRoom.relHotelViolation allObjects];
	
	for (EntityHotelViolation* violation in violations)
	{
        if (violation.enforcementLevel != nil && [violation.enforcementLevel intValue] == kViolationAutoFail)
        {
            return false;
        }
	}
    
	return true;    
}

-(NSUInteger)getViolationsCount
{
	return [hotelBookingRoom.relHotelViolation count];
}

-(NSString*)getViolations
{
	NSArray* violationTexts = [self getViolationMessages];
	return [violationTexts componentsJoinedByString:@"\n"];
}

-(NSArray*)getViolationMessages
{
    NSArray* violations = [hotelBookingRoom.relHotelViolation allObjects];// hotelSearch.selectedHotel.detail.selectedRoom.violations;
    return [violations valueForKeyPath:@"@distinctUnionOfObjects.message"];
}

// Checks if any Violations have enforcementLevel != kViolationAllow
-(BOOL) hasDisallowedViolations
{
    if ([hotelBookingRoom maxEnforcementLevel] != nil)
    {
        if ([[hotelBookingRoom maxEnforcementLevel] intValue] != kViolationAllow)
        {
            return YES;
        }
    }
    return NO;
}

-(NSString*)getViolationReason
{
	return hotelBookingRoom.relHotelViolationCurrent.message; // hotelSearch.selectedHotel.detail.selectedRoom.violationReason;
}

-(NSString*)getViolationJustification
{
	return hotelBookingRoom.violationJustification; // hotelSearch.selectedHotel.detail.selectedRoom.violationJustification;
}

-(int)getIndexForViolationReasonCode:(NSString*)reasonCode
{
	if (reasonCode != nil && violationReasons != nil)
	{
		for (int i = 0; i < [violationReasons count]; i++)
		{
			ViolationReason *reason = violationReasons[i];
			if (reason.code == reasonCode)
				return i;
		}
	}
	return -1;
}

-(NSUInteger)numberOfRowsInViolationsSection
{
    if ([self.hotelBookingRoom.canUseTravelPoints boolValue])
    {
        if (self.hotelBookingRoom.isUsingPointsAgainstViolations) // User has made a selection or not
        {
            if ([self.hotelBookingRoom.isUsingPointsAgainstViolations boolValue])
                self.rowsInViolationSection = @[kRowUsingPointsViolations, kRowManageViolations];
            else
                self.rowsInViolationSection = @[kRowViolationsText, kRowViolationReason, kRowViolationJustification, kRowManageViolations];
        }
        else
        {
            self.rowsInViolationSection = @[kRowManageViolations];// 1; // 'Handle Policy Violations' row
        }
    }
    else
    {
        if ([self hasDisallowedViolations])
            self.rowsInViolationSection = @[kRowViolationsText, kRowViolationReason, kRowViolationJustification];// 1. violations text, 2. reason, 3. justification
        else
            self.rowsInViolationSection = @[kRowViolationsText]; // 1. Violations text

    }
    return [self.rowsInViolationSection count];
}


#pragma mark -
#pragma mark Table view delegate

- (CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	
    NSString *sectionName = aSections[indexPath.section];
    if ([sectionName isEqualToString:kSectionCustomFields])
        return 65;
    else if ([kSectionSummary isEqualToString:sectionName])
	{
		if ((kRoomDescriptionRow) == row)
		{
			NSString *value = nil;
			value = [self getRoomDescription];
			return [HotelDetailsVariableHeightCell calculateCellHeight:tableView hideCellLabel:YES cellValue:value allowDisclosure:NO];
		}
		else if ((kRowHotelPhone) == row)
			return 60;
		else if ((kRowHotelAddress) == row)
			return 60;
	}
	else if ([kSectionFinancial isEqualToString:sectionName])
	{
        if (kRoomRateRow == row && [hotelBookingRoom.travelPoints intValue] != 0 && (!hotelBookingRoom.isUsingPointsAgainstViolations || [hotelBookingRoom.isUsingPointsAgainstViolations boolValue]))
            return 72;
        return 60;
	}
	else if ([kSectionViolation isEqualToString:sectionName] && ![kRowManageViolations isEqualToString:self.rowsInViolationSection[row]])
	{
        return 60;
	}
	
	return 44;
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	
    NSString *sectionName = aSections[indexPath.section];
    if ([sectionName isEqualToString:kSectionCustomFields])
    {
        self.isDirty = YES;
        NSArray *a = dictSections[sectionName];
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
    else if ([kSectionSummary isEqualToString:sectionName])
	{
		if ((kRowHotelAddress) == row)
		{

            // MOB-10654
            MapViewController *vc = [[MapViewController alloc] init];
            NSString *address = (hotelBooking.addr1 != nil ? hotelBooking.addr1 : @"");
            NSString *city = (hotelBooking.city != nil ? hotelBooking.city : @"");
            NSString *state = (hotelBooking.state != nil ? hotelBooking.state : @"");
            NSString *zip = (hotelBooking.zip != nil ? hotelBooking.zip : @"");
            
            vc.mapAddress = [NSString stringWithFormat:@"%@ %@ %@ %@", address, city, state, zip];
            NSLog(@"Hotel Address - %@", vc.mapAddress);
            vc.anoTitle = hotelBooking.hotel; 
            vc.anoSubTitle = hotelBooking.addr1;
            vc.lati = hotelBooking.lat ;
            vc.longi = hotelBooking.lng;
            // MOB-10941
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
		else if ((kRowHotelPhone) == row)
		{
			NSString *phoneNumber = hotelBooking.phone;
			NSString *digitsOnlyPhoneNumber = [[phoneNumber componentsSeparatedByCharactersInSet:[[NSCharacterSet characterSetWithCharactersInString:@"0123456789"] invertedSet]] componentsJoinedByString:@""];
			[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", digitsOnlyPhoneNumber]]];
		}
	}
	if ([kSectionFinancial isEqualToString:sectionName])
    {
        if (kCreditCardRow == row)
        {
            if ([self canChooseCreditCard])
            {
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:HOTEL_BOOKING, @"FROM_VIEW", @"YES", @"SHORT_CIRCUIT", nil];
                
                if (creditCardIndex != nil)
                    [pBag setValue:creditCardIndex forKey:@"CREDIT_CARD_INDEX"];
                if (self.creditCards) {
                    [pBag setValue:self.creditCards forKey:@"CREDIT_CARDS"];
                }
                
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
                    [ConcurMobileAppDelegate switchToView:HOTEL_CREDIT_CARD viewFrom:HOTEL_BOOKING ParameterBag:pBag];
            }
        }
        else if(kCancellationPolicyRow == row)
        {
            CancellationDetailsVC *vc = [[CancellationDetailsVC alloc] initWithNibName:@"CancellationDetailsVC" bundle:nil];
            vc.cancellationText = [self cancellationPolicyText];
            [self.navigationController pushViewController:vc animated:YES];
            self.cancellationPolicyNeedsViewing = NO;
            [tableList reloadData];
        }
    }
	else if ([kSectionViolation isEqualToString:sectionName])
	{
        if([kRowViolationsText isEqualToString:self.rowsInViolationSection[row]])
        {
            ViolationDetailsVC *vc = [[ViolationDetailsVC alloc] initWithNibName:@"ViolationDetailsVC" bundle:nil];
            vc.violationText = [self getViolations];
            [self.navigationController pushViewController:vc animated:YES];
        }
		else if ([kRowViolationReason isEqualToString:self.rowsInViolationSection[row]])
		{
			NSString *optionsViewTitle = [Localizer getLocalizedText:@"Select Reason"];
			NSString *optionType = @"VIOLATION_REASON";
			NSArray *labels = violationReasonLabels;
            
            
			int currentReasonIndex = [self getIndexForViolationReasonCode:hotelSearch.selectedHotel.detail.selectedRoom.violationReasonCode];
            
            NSString *currentCode = hotelBookingRoom.relHotelViolationCurrent.code;
            
            if(currentCode != nil)
            {
                for(int i = 0; i < [violationReasons count]; i++)
                {
                    ViolationReason *reason = violationReasons[i];

                    if([currentCode isEqualToString:reason.code])
                    {
                        currentReasonIndex = i;
                        break;
                    }
                }
            }
			
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
		else if ([kRowViolationJustification isEqualToString:self.rowsInViolationSection[row]])
		{
			NSString *customTitle = [Localizer getLocalizedText:@"HOTEL_BOOKING_VIOLATION_JUSTIFICATION_TITLE"];
			NSString *placeholder = [Localizer getLocalizedText:@"HOTEL_BOOKNIG_VIOLATION_JUSTIFICATION_PLACEHOLDER_TEXT"];
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"FROM_VIEW", placeholder, @"PLACEHOLDER", customTitle, @"TITLE", @"YES", @"SHORT_CIRCUIT", nil];

			NSString *justification = hotelBookingRoom.violationJustification; // hotelSearch.selectedHotel.detail.selectedRoom.violationJustification;
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
        else if([kRowManageViolations isEqualToString:self.rowsInViolationSection[row]])
        {
            ManageViolationsVC *vc = [[ManageViolationsVC alloc] initWithTitle:[@"Points or Approval" localize]];
            vc.travelPointsInBank = self.travelPointsInBank;
            vc.violationTexts = [self getViolationMessages];
            vc.hotelRoom = self.hotelBookingRoom;
            vc.violationReasons = self.violationReasons;
            vc.violationReasonLabels = self.violationReasonLabels;
            [self.navigationController pushViewController:vc animated:YES];
        }
	}
}


#pragma mark -
#pragma mark HotelSummaryDelegate

-(void)addressPressed:(id)sender
{
	HotelDetailedMapViewController * vc = [[HotelDetailedMapViewController alloc] initWithNibName:@"HotelDetailedMapViewController" bundle:nil];

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
- (void)makeToolbar:(HotelSearchCriteria*)hotelSearchCriteria
{

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
    self.ivStars = nil;
}


- (void)dealloc
{
    if (imageViewerMulti != nil)
		imageViewerMulti.parentVC = nil;
    
    
    //Custom Fields
}


-(void) makeHeader
{	
	hotelName.text = hotelBooking.hotel;	
	address1.text = hotelBooking.addr1;
	
    NSMutableString *cityStateZip = [[NSMutableString alloc] initWithString:@""];// [NSString stringWithFormat:@"%@, %@ %@", hotelResult.city, hotelResult.stateAbbrev, hotelResult.zip];
    if(hotelBooking.city != nil)
        [cityStateZip appendString:hotelBooking.city];
    
    if(hotelBooking.stateAbbrev != nil)
    {   [cityStateZip appendString:@", "];
        [cityStateZip appendString:hotelBooking.stateAbbrev];
    }
    
    if(hotelBooking.zip != nil)
    {   [cityStateZip appendString:@" "];
        [cityStateZip appendString:hotelBooking.zip];
    }
    
	address2.text = cityStateZip;
	phone.text = hotelBooking.phone;
	distance.text = [NSString stringWithFormat:@"%@ %@", hotelBooking.distance, hotelBooking.distanceUnit];
	
	int asterisks = [hotelBooking.starRating intValue];
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

	self.imageViewerMulti = [[ImageViewerMulti alloc] init];	// Retain count = 2
	 // Retain count = 1
	imageViewerMulti.parentVC = self;
	
	[imageViewerMulti configureWithImagePairsForHotel:hotelBooking Owner:self ImageViewer:ivHotel] ; //]:hotelResult.propertyImagePairs Owner:owner ImageViewer:ivHotel];
	imageViewerMulti.aImageURLs = [imageViewerMulti getImageURLsForHotel:hotelBooking ]; //:hotelResult.propertyImagePairs];
	self.aImageURLs = [imageViewerMulti getImageURLsForHotel:hotelBooking];
}

#pragma mark - Custom Fields
-(void) reloadCustomFieldsSection
{
    int sectionIndex = 0;
    for(int i = 0; i < [aSections count]; i++)
    {
        if([aSections[i] isEqualToString:kSectionCustomFields])
        {
            sectionIndex = i;
            break;
        }
    }
    NSIndexSet *indexSet = [NSIndexSet indexSetWithIndex:sectionIndex];
    
    [tableList reloadSections:indexSet withRowAnimation:UITableViewRowAnimationFade];
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

-(void)fetchPreSellOptions
{
    NSMutableDictionary *paramBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING"
                                     , self.hotelBookingRoom.choiceId, @"CHOICE_ID",  nil];
	
	[[ExSystem sharedInstance].msgControl createMsg:PRE_SELL_OPTIONS CacheOnly:@"NO" ParameterBag:paramBag SkipCache:YES Options:SILENT_ERROR RespondTo:self];
}

-(HotelBookingCell*)getHotelBookingSingleCell
{
    HotelBookingCell *cell = (HotelBookingCell*)[tableList dequeueReusableCellWithIdentifier:@"HotelBookingSingleCell"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelBookingSingleCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[HotelBookingCell class]])
                cell = (HotelBookingCell *)oneObject;
    }
    cell.lblSubValue.hidden = YES;
    cell.lblValue.textColor = [UIColor blackColor];
    cell.accessoryType = UITableViewCellAccessoryNone;
    
    return cell;
}

// Custom fields
-(UITableViewCell *)configureCustomFieldCellAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *sectionName = aSections[indexPath.section];
    NSArray *a = dictSections[sectionName];
    EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)a[indexPath.row];  //[tcfRows objectAtIndex:[newIndexPath row]];
    
    if ([tcf.dataType isEqualToString:@"boolean"]) 
    {
        BoolEditCell *cell = (BoolEditCell *)[tableList dequeueReusableCellWithIdentifier:@"BoolEditCell"];
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
        HotelBookingCell *cell = [self getHotelBookingSingleCell];
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
    NSString *sectionName = aSections[indexPath.section];
    NSArray *a = dictSections[sectionName];
    EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)a[indexPath.row];  //[tcfRows objectAtIndex:[newIndexPath row]];
    //    EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)[tcfRows objectAtIndex:[indexPath row]];
    
    UITableViewCell *cell = (UITableViewCell *)[tableList cellForRowAtIndexPath:indexPath];
    
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

