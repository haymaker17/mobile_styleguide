//
//  TrainDetailVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainDetailVC.h"
#import "DateTimeFormatter.h"
#import "FormatUtils.h"
#import "ExSystem.h" 

#import "TrainDeliveryVC.h"
#import "ConcurMobileAppDelegate.h"
#import "HotelCreditCardViewController.h"
#import "UserConfig.h"
#import "TrainDetailLegCell.h"
#import "TrainDetailHeaderCell.h"
#import "AmtrakSellData.h"
#import "AmtrakSell.h"
#import "TrainFareChoicesVC.h"
#import "TripsData.h"
#import "MobileAlertView.h"
#import "iPadHomeVC.h"
#import "HotelBookingCell.h"
#import "SystemConfig.h"
#import "HotelViolation.h"
#import "TravelViolationReasons.h"
#import "ViolationReason.h"
#import "HotelOptionsViewController.h"
#import "HotelTextEditorViewController.h"
#import "PreSellOptions.h"
#import "PolicyViolationConstants.h"

#import "GovTAField.h"

// Rows beloning to the kSectionViolation section
#define kViolationDescriptionRow 0
#define kViolationReasonRow 1
#define kViolationJustificationRow 2

#define kAlertGropuAuthUsed 300200
#define kAlertReservationFailed		138013
#define kAlertConfirmReserve		138750

@interface TrainDetailVC (Private)
-(NSUInteger)getViolationsCount;
-(void)completeReservation;

@end

@implementation TrainDetailVC
@synthesize		lblTrain1, lblTrain1Time, lblTrain1FromCity, lblTrain1FromTime, lblTrain1ToCity, lblTrain1ToTime;
@synthesize		lblTrain2, lblTrain2Time, lblTrain2FromCity, lblTrain2FromTime, lblTrain2ToCity, lblTrain2ToTime;
@synthesize lblFromLabel, lblFrom, lblToLabel, lblTo, lblDateRange, tableList, aKeys, dictGroups, railChoice, deliveryData;
@synthesize		lblCost, lblDelivery, lblCard, btnDelivery, btnCard, lblSeat1, lblSeat2, deliveryOption, chosenCardIndex, chosenCreditCard;
@synthesize		from, to, dateRange, trainDeliveryData;
@synthesize					activity;
@synthesize					lblLoading, pickerPopOverVC, aTrains, aButtons;
@synthesize taFields;
@synthesize violationJustification, violationReasonCode, violationReasonLabels, violationReasons;

-(void) switchToTripDetailView:(NSString*) itinLocator
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SKIP_PARSE", nil];
    EntityTrip *trip;
    NSString *tripKey;
    
    // MOB-10671
    trip = [[TripManager sharedInstance] fetchByItinLocator:itinLocator];
    tripKey = trip.tripKey;
    
    // We came here from the home screen, so pop all the way back to it before going to the trip view.
    pBag[@"POP_TO_ROOT_VIEW"] = @"YES";
    
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
			//MOB-12699
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
            [ConcurMobileAppDelegate switchToView:TRIP_DETAILS viewFrom:CAR_DETAILS ParameterBag:pBag];
        
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


#pragma mark - MVC Methods
-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:TRAIN_DELIVERY] && deliveryData == nil)
	{
		self.trainDeliveryData = (TrainDeliveryData *)msg.responder;
		
		if(trainDeliveryData != nil && [trainDeliveryData.keys count] > 0)
		{
			NSString *typeKey = (trainDeliveryData.keys)[0];
			self.deliveryData = (trainDeliveryData.items)[typeKey];
			
			self.deliveryOption = deliveryData.name;
            
            [aButtons removeObjectAtIndex:2];
            [aButtons addObject:deliveryOption];
            [tableList reloadData];
			
			[self makeReserveButton:self];
		}

	}
    else if ([msg.idKey isEqualToString:PRE_SELL_OPTIONS])
	{
        [self hideLoadingView];
        PreSellOptions *preSellOptions = (PreSellOptions *)msg.responder;
        
        self.creditCards = preSellOptions.creditCards;
        [self chooseFirstCard];
		
        self.trainDeliveryData = preSellOptions.trainDeliveryData;
		if(trainDeliveryData != nil && [trainDeliveryData.keys count] > 0)
		{
			NSString *typeKey = (trainDeliveryData.keys)[0];
			self.deliveryData = (trainDeliveryData.items)[typeKey];
			
			self.deliveryOption = deliveryData.name;
            
            [aButtons removeObjectAtIndex:2];
            [aButtons addObject:deliveryOption];
            [tableList reloadData];
			
			[self makeReserveButton:self];
		}
        if (!preSellOptions.isRequestSuccessful) {
            MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"Error" localize]
                                                                    message:[@"An error has occurred retrieving sell information fields. Reservation cannot be done at this time. Please try later." localize]
                                                                   delegate:nil
                                                          cancelButtonTitle:[LABEL_CLOSE_BTN localize]
                                                          otherButtonTitles:nil];
            [alert show];
        }
    }
	else if ([msg.idKey isEqualToString:AMTRAK_SELL])
	{
		AmtrakSell *as = (AmtrakSell *)msg.responder;
		
		if([as.obj.sellStatus isEqualToString:@"SUCCESS"])
		{
            trainRezResponse = as.obj;
            if (trainRezResponse.tripLocator && trainRezResponse.itinLocator)
            {
                [self showWaitViewWithText:[Localizer getLocalizedText:@"Updating Itinerary"]];
                NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:as.obj.tripLocator, @"RECORD_LOCATOR", @"AMTRAK_DETAIL", @"TO_VIEW",nil];
                [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
            }
            else
                [self showHideWait:NO];
		}
		else
        {
            NSString *errorMessage = [Localizer getLocalizedText:@"The train could not be booked"];
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Error"]
                                  message:errorMessage
                                  delegate:self
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
            alert.tag = kAlertReservationFailed;
            [alert show];
            [self showHideWait:NO];
		}
	}
	else if ([msg.idKey isEqualToString:TRIPS_DATA] && (msg.parameterBag)[@"ITIN_LOCATOR"])
	{
		[self hideWaitView];

        NSString *itinLocator = (NSString*)(msg.parameterBag)[@"ITIN_LOCATOR"];
        [self switchToTripDetailView:itinLocator];
        	
	}
    else if ([msg.idKey isEqualToString:TRIPS_DATA])
    {
        NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:trainRezResponse.tripLocator, @"RECORD_LOCATOR", @"AMTRAK_DETAIL", @"TO_VIEW",trainRezResponse.itinLocator, @"ITIN_LOCATOR", nil];
        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
	else if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		if ((msg.parameterBag)[@"OPTION_TYPE_ID"] != nil)
		{
			// We've returned from the HotelOptionsViewController
			NSNumber* selectedRowIndexNumber = (NSNumber*)(msg.parameterBag)[@"SELECTED_ROW_INDEX"];
			NSUInteger selectedRowIndex = [selectedRowIndexNumber intValue];
			ViolationReason *reason = violationReasons[selectedRowIndex];
			self.violationReasonCode = reason.code;
		}
		else if ((msg.parameterBag)[@"TEXT"] != nil)
		{
            self.violationJustification = (NSString*)(msg.parameterBag)[@"TEXT"];
		}
        
		[tableList reloadData];
	}
}

#pragma mark -
#pragma mark Fetching Methods
-(void)fetchDeliveryOptions:(id)sender
{	
	//NSLog(@"groupid %@", parentVC.railChoice.groupId);
//	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"TRAIN_DELIVERY_VIEW", @"TO_VIEW"
//								 , self.railChoice.groupId, @"GROUP_ID"
//								 , self.railChoice.bucket, @"BUCKET",  nil];
//	
//	[[ExSystem sharedInstance].msgControl createMsg:TRAIN_DELIVERY CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];  
    
    NSMutableDictionary *paramBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"TRAIN_DELIVERY_VIEW", @"TO_VIEW", @"YES", @"REFRESHING"
								 , self.railChoice.choiceId, @"CHOICE_ID",  nil];
	
	[[ExSystem sharedInstance].msgControl createMsg:PRE_SELL_OPTIONS CacheOnly:@"NO" ParameterBag:paramBag SkipCache:YES Options:SILENT_ERROR RespondTo:self];
    
    [self showLoadingViewWithText:[@"Loading Data" localize]];
}


// The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
/*
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization.
    }
    return self;
}
*/

-(void) viewWillAppear:(BOOL)animated
{
	self.title = [Localizer getLocalizedText:@"Train Detail"];
	
//	if(railChoice != nil)
//	{
//		RailChoiceSegmentData *seg = [railChoice.segments objectAtIndex:0];
//		RailChoiceTrainData	*train1 = [seg.trains objectAtIndex:0];
//		
//		RailChoiceSegmentData *seg2 = [railChoice.segments objectAtIndex:1];
//		RailChoiceTrainData	*train2 = [seg2.trains objectAtIndex:0];
//		
//		NSString *vehicle = [Localizer getLocalizedText:@"Train"];
//		if([train1.bic isEqualToString:@"T"])
//			vehicle = [Localizer getLocalizedText:@"Bus"];
//		
//		lblTrain1.text = [NSString stringWithFormat:@"%@ # %@", vehicle, train1.fltNum];
//		//lblTrain1.text = [NSString stringWithFormat:@"Train # %@", train1.fltNum];
//		int hours = seg.totalTime / 60;
//		int mins = seg.totalTime - (60 * hours);
//		lblTrain1Time.text = [NSString stringWithFormat:@"%dh %dm", hours, mins];
//		lblTrain1FromCity.text = train1.depAirp;
//		lblTrain1FromTime.text = [DateTimeFormatter formatTimeForTravel:train1.depDateTime];
//		lblTrain1ToCity.text = train1.arrAirp;
//		lblTrain1ToTime.text = [DateTimeFormatter formatTimeForTravel:train1.arrDateTime];
//		
//		if([train2.bic isEqualToString:@"T"])
//			vehicle = [Localizer getLocalizedText:@"Bus"];
//		else 
//			vehicle = [Localizer getLocalizedText:@"Train"];
//		lblTrain2.text = [NSString stringWithFormat:@"%@ # %@", vehicle, train2.fltNum];
//		//lblTrain2.text = [NSString stringWithFormat:@"Train # %@", train2.fltNum];
//		hours = seg2.totalTime / 60;
//		mins = seg2.totalTime - (60 * hours);
//		lblTrain2Time.text = [NSString stringWithFormat:@"%dh %dm", hours, mins];
//		lblTrain2FromCity.text = train2.depAirp;
//		lblTrain2FromTime.text = [DateTimeFormatter formatTimeForTravel:train2.depDateTime];
//		lblTrain2ToCity.text = train2.arrAirp;
//		lblTrain2ToTime.text = [DateTimeFormatter formatTimeForTravel:train2.arrDateTime];
//		
//		NSArray *aSeats = [railChoice.descript componentsSeparatedByString:@" / "];
//		
//		NSMutableString *seats2 = [[NSMutableString alloc] initWithString:@""];
//		for(int i = ([aSeats count] - 1); i > -1; i--)
//		{
//			NSString *val = [aSeats objectAtIndex:i];
//			if([seats2 length] > 0)
//				[seats2 appendString:@" / "];
//			
//			[seats2 appendString:val];
//		}
//		
//		lblSeat1.text = railChoice.descript;
//		lblSeat2.text = seats2;
//		
//		lblCost.text = [FormatUtils formatMoney:railChoice.cost crnCode:railChoice.currencyCode]; 
//	}

	[self refreshOrientation];
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	
    [super viewDidLoad];
    
    [self updateViolationReasons];

    self.aButtons = [[NSMutableArray alloc] initWithObjects:@"Total", @"Card", @"Delivery",  nil];

	[self chooseFirstCard];
	
	self.aKeys = [[NSMutableArray alloc] initWithObjects:@"Legs", @"Purchases", nil];
//	if([railChoice.segments count] == 2)
//		[aKeys addObject:@"Return"];
    
    if ([self getViolationsCount] > 0)
    {
        [self.aKeys addObject:@"Violations"];
    }
    
    RailChoiceSegmentData *rcsd = (railChoice.segments)[0];
    self.aTrains = [[NSMutableArray alloc] initWithArray:rcsd.trains];
    
    if([railChoice.segments count] == 2)
    {
        rcsd = (railChoice.segments)[1];
        for(int i = 0; i < [rcsd.trains count]; i++)
            [aTrains addObject:(rcsd.trains)[i]];
    }
	

    
	[self fetchDeliveryOptions:self];
}



- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


-(void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	[self refreshOrientation];
}

-(void) refreshOrientation
{

}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
	
	self.activity = nil;
	self.lblLoading = nil;
}




#pragma mark -
#pragma mark Show sub view methods
-(void) showDelivery:(id)sender
{
	if(trainDeliveryData == nil || trainDeliveryData.items == nil || [trainDeliveryData.items count] == 0 )
		return;
	
	if([UIDevice isPad])
	{
		[self pickerTapped:sender IndexPath:sender];
		return;
	}

	TrainDeliveryVC *tblvc = [[TrainDeliveryVC alloc] initWithNibName:@"TrainDeliveryVC" bundle:nil];
	tblvc.hidesBottomBarWhenPushed = NO;
	tblvc.parentVC = self;
	
	if(trainDeliveryData == nil)
		[tblvc fetchDeliveryOptions:sender];
	else {
		//
		NSMutableArray *aDeliveryOptions = [[NSMutableArray alloc] initWithObjects:nil];
		
		for(NSString *typeKey in trainDeliveryData.keys)
		{
			DeliveryData *dd = (trainDeliveryData.items)[typeKey];
			[aDeliveryOptions addObject:dd];
		}
		
		tblvc.aDeliveryOptions = aDeliveryOptions;
		
		if([aDeliveryOptions count] > 0)
		{
			
			DeliveryData *dd = aDeliveryOptions[0];
			tblvc.lblDeliveryOption.text = dd.name;
			
			[tblvc.dPicker reloadComponent:0];
		}
	}	

    [self.navigationController pushViewController:tblvc animated:YES];
}

-(void) showCards:(id)sender
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 @"TRAIN_BOOKING", @"FROM_VIEW", 
                                 @"YES", @"SHORT_CIRCUIT", 
                                 self.creditCards, @"CREDIT_CARDS",
                                 nil];
	
	if (chosenCardIndex > -1)
		[pBag setValue:@(chosenCardIndex) forKey:@"CREDIT_CARD_INDEX"];
	else
    {
		return;
    }

	HotelCreditCardViewController *nextController = [[HotelCreditCardViewController alloc] initWithNibName:@"HotelCreditCardViewController" bundle:nil];
	nextController.parentVC = self;
	Msg *msg = [[Msg alloc] init];
	msg.parameterBag = pBag;
	msg.idKey = @"SHORT_CIRCUIT";
	[nextController respondToFoundData:msg];
	[self.navigationController pushViewController:nextController animated:YES];
    nextController.title = [Localizer getLocalizedText:@"Select Card"];

}

-(void)chooseCard:(int)cardIndex
{
	if(cardIndex < 0)
	{
        [aButtons removeObjectAtIndex:1];
        [aButtons insertObject:[Localizer getLocalizedText:@"Unavailable"] atIndex:1];
        [tableList reloadData];
		return; //we don't actully have a card...
	}
	
//	UserConfig *userConfig = [UserConfig getSingleton];
	CreditCard *creditCard = self.creditCards[cardIndex];
	chosenCardIndex = cardIndex;
	self.chosenCreditCard = creditCard;
    
    [aButtons removeObjectAtIndex:1];
    [aButtons insertObject:[ NSString stringWithFormat:@"%@ %@", chosenCreditCard.name, chosenCreditCard.maskedNumber] atIndex:1];
    [tableList reloadData];
    
	if(chosenCardIndex > -1)
		[self makeReserveButton:self];
}

-(void)chooseFirstCard
{
//	UserConfig *userConfig = [UserConfig getSingleton];
    if ([self.creditCards count] < 1)
	{
        [aButtons removeObjectAtIndex:1];
        [aButtons insertObject:[Localizer getLocalizedText:@"Unavailable"] atIndex:1];
        [tableList reloadData];
		chosenCardIndex = -1;
	}
	else {
		chosenCardIndex = 0;
		self.chosenCreditCard = self.creditCards[0];
		if((chosenCreditCard.name == nil || chosenCreditCard.maskedNumber == nil) && !isDelayingFirstCard)
		{
			[self performSelector:@selector(chooseFirstCard) withObject:nil afterDelay:2.0f];
			isDelayingFirstCard = YES;
		}

		
		NSString *cardName = chosenCreditCard.name;
		NSString *cardMask = chosenCreditCard.maskedNumber;
		if(cardName == nil)
			cardName = @"";
		
		if(cardMask == nil)
			cardMask = @"";
		

        [aButtons removeObjectAtIndex:1];
        [aButtons insertObject:[ NSString stringWithFormat:@"%@ %@", cardName, cardMask] atIndex:1];
        [tableList reloadData];
		[self makeReserveButton:self];
	}

}

-(void) makeReserveButton:(id)sender
{
	if(trainDeliveryData == nil || [trainDeliveryData.keys count] <= 0)
    {
        self.navigationItem.rightBarButtonItem = nil;
		return;
    }
	
	if(chosenCardIndex <= -1)
    {
        self.navigationItem.rightBarButtonItem = nil;
		return;
    }
    
    UIBarButtonItem *btnReserve = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Reserve"] style:UIBarButtonItemStyleBordered target:self action:@selector(reserveTrain:)];
    self.navigationItem.rightBarButtonItem = btnReserve;
}


-(void) reserveTrain:(id)sender
{
	
	if(chosenCreditCard == nil && chosenCreditCard.ccId != nil)
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:[Localizer getLocalizedText:@"No Credit Card"]
							  message:[Localizer getLocalizedText:@"You need to select a valid credit card in order to reserve your train"]
							  delegate:nil 
							  cancelButtonTitle:nil
							  otherButtonTitles:[Localizer getLocalizedText:@"OK"], nil];
		[alert show];
		return;
	}
	
	if(deliveryData == nil && deliveryData.type != nil)
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:[Localizer getLocalizedText:@"No Delivery Option"]
							  message:[Localizer getLocalizedText:@"You need to select a Delivery Option in order to reserve your train"]
							  delegate:nil 
							  cancelButtonTitle:nil
							  otherButtonTitles:[Localizer getLocalizedText:@"OK"], nil];
		[alert show];
		return;
	}
	
    
    BOOL hasViolation = [self hasDisallowedViolations];
    BOOL hasViolationReason = [[self getViolationReason] length];
    BOOL hasViolationJustification = [[self getViolationJustification] length];
    BOOL hasViolationJustificationIfRequired = hasViolationJustification || ![SystemConfig getSingleton].ruleViolationExplanationRequired;
    
    if (hasViolation && (!hasViolationReason || !hasViolationJustificationIfRequired))
    {
		NSString *msg;
		
		if ([self getViolationReason] == nil)
		{
			if (!hasViolationJustificationIfRequired)
			{
                // reason and justification are missing
				msg = [Localizer getLocalizedText:@"Rail violation reason and justification are missing"];
			}
			else
			{
                // only reason is missing
				msg = [Localizer getLocalizedText:@"Rail violation reason is missing"];
			}
		}
		else
		{
            // only justification is missing
			msg = [Localizer getLocalizedText:@"Rail violation justification is missing"];
		}

        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Missing fields"]
                              message:msg
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                              otherButtonTitles:nil];
        [alert show];
        
        return;
	}
    
    UIAlertView* alert = [[MobileAlertView alloc] initWithTitle: [Localizer getLocalizedText:@"Reserve Train"]
                                                        message:[Localizer getLocalizedText:@"Are you sure that you want to reserve the selected train(s)?"]
                                                       delegate:self
                                              cancelButtonTitle: [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
                                              otherButtonTitles:[Localizer getLocalizedText:@"OK"], 
						  nil];
    alert.tag = kAlertConfirmReserve;
	[alert show];
}

-(void)completeReservation
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"TRAIN_DELIVERY_VIEW", @"TO_VIEW"
								 , self.railChoice.groupId, @"GROUP_ID"
								 , self.railChoice.bucket, @"BUCKET"
								 ,chosenCreditCard.ccId, @"CREDIT_CARD_ID"
								 , deliveryData.type, @"DELIVERY_OPTION"
								 ,  nil];
    
    if (violationReasonCode != nil)
        pBag[@"VIOLATION_REASON_CODE"] = violationReasonCode;
    
    if (violationJustification != nil)
        pBag[@"VIOLATION_JUSTIFICATION"] = violationJustification;
	
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
            
            if (currentTAField != nil)
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
    
    [[ExSystem sharedInstance].msgControl createMsg:AMTRAK_SELL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES Options:NO_RETRY RespondTo:self];
	
    NSDictionary *dict = @{@"Type": @"Train", @"Booked From": @"Home"};
    [Flurry logEvent:@"Book: Reserve" withParameters:dict];
    [self showHideWait:YES];
}

-(void)showHideWait:(BOOL)isShow
{
	if(isShow)
	{
        [self showWaitViewWithText:[Localizer getLocalizedText:@"Reserving Train"]];
        
		NSArray *toolbarItems = @[];
		[self setToolbarItems:toolbarItems animated:YES];
	}
	else 
	{
        [self hideWaitView];
		[self makeReserveButton:self];
	}
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return [aKeys count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{	
    NSString *sectionName = aKeys[section];
	
	if ([@"Purchases" isEqualToString:sectionName])
		return 3;
	else if ([@"Legs" isEqualToString:sectionName])
	{
		RailChoiceSegmentData *rcsd = (railChoice.segments)[0];
		int currentCount = [rcsd.trains count];
        
        if([railChoice.segments count] == 2)
        {
            RailChoiceSegmentData *rcsd = (railChoice.segments)[1];
            currentCount = currentCount + [rcsd.trains count];
        }
        return currentCount;
	}
    else if ([@"Violations" isEqualToString:sectionName])
    {
        return [self hasDisallowedViolations] ? 3 : 1;	// Rows: violation description, justification, and comment
    }

	return 0;
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	NSUInteger section = [indexPath section];
    NSString *sectionName = aKeys[section];
	
	if ([@"Purchases" isEqualToString:sectionName])
	{
	
		TrainDetailLegCell *cell = (TrainDetailLegCell*)[tableView dequeueReusableCellWithIdentifier:@"TrainDetailButtonCell"];
		if (cell == nil)
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TrainDetailButtonCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[TrainDetailLegCell class]])
					cell = (TrainDetailLegCell *)oneObject;
		}
		

        if(indexPath.row == 0)
        {
            cell.lblInfo.text = [Localizer getLocalizedText:@"Total Rate"];
            cell.lblFromStation.text = [FormatUtils formatMoney:railChoice.cost crnCode:railChoice.currencyCode]; 
            [cell setAccessoryType:UITableViewCellAccessoryNone];
        }
        else if(indexPath.row == 1)
        {
            cell.lblInfo.text = [Localizer getLocalizedText:@"Card"];
            cell.lblFromStation.text = aButtons[indexPath.row]; 
            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
        }
        else if(indexPath.row == 2)
        {
            cell.lblInfo.text = [Localizer getLocalizedText:@"Ticket Delivery"];
            cell.lblFromStation.text = aButtons[indexPath.row]; 
            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
        }
		
		return cell;
	}
	else if ([@"Legs" isEqualToString:sectionName])
	{
		TrainDetailLegCell *cell = (TrainDetailLegCell*)[tableView dequeueReusableCellWithIdentifier:@"TrainDetailLegCell"];
		if (cell == nil)
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TrainDetailLegCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[TrainDetailLegCell class]])
					cell = (TrainDetailLegCell *)oneObject;
		}
		
		RailChoiceSegmentData *rcsd = nil;
		
//		NSArray *aSeats = [railChoice.descript componentsSeparatedByString:@" / "];
//		NSString *seat = @"";
        
        rcsd = (railChoice.segments)[0];
        int departTrainsCount = [rcsd.trains count];
        if(indexPath.row > (departTrainsCount -1))
        {
            //we are in seg 2
//            rcsd = [railChoice.segments objectAtIndex:1];
//            int trainCount = [rcsd.trains count];
//			int start = [aSeats count] - trainCount;
//			start = start + row;
//			seat = [TrainFareChoicesVC fetchSegmentSeats:railChoice.descript NumberOfSeatsDesired:[rcsd.trains count] FrontToBack:YES JustTheOne:start];
            
            cell.lblToStation.text = [Localizer getLocalizedText:@"Return"];
        }
        else
            cell.lblToStation.text = [Localizer getLocalizedText:@"Departure"];
		
//		if (section == 1)
//		{
//			rcsd = [railChoice.segments objectAtIndex:0];
//			seat = [aSeats objectAtIndex:row];
//		}
//		else if (section == 2)
//		{
//			rcsd = [railChoice.segments objectAtIndex:1];
////			if([aSeats count] < (row + 2) )
////				seat = [aSeats objectAtIndex:row + 2];
//			int trainCount = [rcsd.trains count];
//			int start = [aSeats count] - trainCount;
//			start = start + row;
//			seat = [TrainFareChoicesVC fetchSegmentSeats:railChoice.descript NumberOfSeatsDesired:[rcsd.trains count] FrontToBack:YES JustTheOne:start];
//		}

		RailChoiceTrainData *train = aTrains[indexPath.row]; // [rcsd.trains objectAtIndex:row];

        
        int hours = rcsd.totalTime / 60;
        int mins = rcsd.totalTime - (60 * hours);
        NSString *duration = [NSString stringWithFormat:@"%dh %dm", hours, mins];
		cell.lblFromStation.text = [NSString stringWithFormat:@"%@ #%@ (%@)",[Localizer getLocalizedText:@"Train"], train.fltNum, duration];
		
		cell.lblFromTime.text = [DateTimeFormatter formatTimeForTravel:train.depDateTime];
        
        NSMutableString *sReturn = [[NSMutableString alloc] initWithString:@""];
        [sReturn appendString:train.depAirp];
        [sReturn appendString:@" "];
        [sReturn appendString:[DateTimeFormatter formatTimeForTravel:train.depDateTime]];
        [sReturn appendString:@" - "];
        [sReturn appendString:train.arrAirp];
        [sReturn appendString:@" "];
        [sReturn appendString: [DateTimeFormatter formatTimeForTravel:train.arrDateTime]];
        cell.lblFromTime.text = sReturn;
        
        //railChoice.descript
        NSString *seats = [TrainFareChoicesVC fetchSegmentSeats:railChoice.descript NumberOfSeatsDesired:[rcsd.trains count] FrontToBack:YES JustTheOne:0];
        if(indexPath.row > (departTrainsCount -1))
            seats = [TrainFareChoicesVC fetchSegmentSeats:railChoice.descript NumberOfSeatsDesired:[rcsd.trains count] FrontToBack:NO JustTheOne:0];
        
//		cell.lblToStation.text = train.arrAirp;
//		cell.lblToTime.text = [DateTimeFormatter formatTimeForTravel:train.arrDateTime];
//			
//		NSString *vehicle = [Localizer getLocalizedText:@"Train"];
//		if([train.bic isEqualToString:@"T"])
//			vehicle = [Localizer getLocalizedText:@"Bus"];
		
//		NSString *trainInfo = [NSString stringWithFormat:@"%@ # %@", vehicle, train.fltNum];
//		int hours = [train.flightTime intValue] / 60;
//		int mins = [train.flightTime intValue] - (60 * hours);
//		NSString *duration = [NSString stringWithFormat:@"%dh %dm", hours, mins];
		
		cell.lblInfo.text = [NSString stringWithFormat:@"%@", seats];
        
		return cell;	
	}
	else if ([@"Violations" isEqualToString:sectionName])
    {
        HotelBookingCell *cell = (HotelBookingCell*)[tableView dequeueReusableCellWithIdentifier:@"HotelBookingSingleCell"];
        if (cell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelBookingSingleCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[HotelBookingCell class]])
                    cell = (HotelBookingCell *)oneObject;
        }
        
        cell.lblValue.textColor = [UIColor blackColor];
        
		if (kViolationDescriptionRow == row)
		{
            NSString *label = [self hasDisallowedViolations] ? [@"Violation" localize] : [@"Travel Policy" localize];
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


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	NSUInteger section = [indexPath section];
    NSString *sectionName = aKeys[section];
	
	if ([@"Purchases" isEqualToString:sectionName])
    {
        if(indexPath.row == 1)
        {
            [self showCards:nil];
        }
        else if (indexPath.row == 2)
        {
            [self showDelivery:indexPath];
        }
    }
    else if ([sectionName isEqualToString:@"Violations"])
	{
        if(kViolationDescriptionRow == row)
        {
            ViolationDetailsVC *vc = [[ViolationDetailsVC alloc] initWithNibName:@"ViolationDetailsVC" bundle:nil];
            vc.violationText = [self getViolations];
            [self.navigationController pushViewController:vc animated:YES];
        }
		if (kViolationReasonRow == row)
		{
			NSString *optionsViewTitle = [Localizer getLocalizedText:@"Select Reason"];
			NSString *optionType = @"VIOLATION_REASON";
			NSArray *labels = violationReasonLabels;
			int currentReasonIndex = [self getIndexForViolationReasonCode:self.violationReasonCode];
			
			NSNumber *preferredFontSize = @13.0f;
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"TRAIN_DETAILS", @"FROM_VIEW", optionType, @"OPTION_TYPE_ID", optionsViewTitle, @"TITLE", labels, @"LABELS", preferredFontSize, @"PREFERRED_FONT_SIZE", @"YES", @"SHORT_CIRCUIT", nil];
			
			if (currentReasonIndex >= 0)
				pBag[@"SELECTED_ROW_INDEX"] = @(currentReasonIndex);
			
            HotelOptionsViewController *nextController = [[HotelOptionsViewController alloc] initWithNibName:@"HotelOptionsViewController" bundle:nil];
            
            Msg *msg = [[Msg alloc] init];
            msg.parameterBag = pBag;
            msg.idKey = @"SHORT_CIRCUIT";
            //MOB-12288 missing title on rail violation page.
            [nextController respondToFoundData:msg];
            [self.navigationController pushViewController:nextController animated:YES];
		}
		else if (kViolationJustificationRow == row)
		{
			NSString *customTitle = [Localizer getLocalizedText:@"Violation Justification"];
			NSString *placeholder = [Localizer getLocalizedText:@"Please enter a justification for this booking."];
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"TRAIN_DETAILS", @"FROM_VIEW", placeholder, @"PLACEHOLDER", customTitle, @"TITLE", @"YES", @"SHORT_CIRCUIT", nil];
			
			NSString *justification = self.violationJustification;
			if (justification != nil)
				pBag[@"TEXT"] = justification;
			
            HotelTextEditorViewController *nextController = [[HotelTextEditorViewController alloc] initWithNibName:@"HotelTextEditorViewController" bundle:nil];
            Msg *msg = [[Msg alloc] init];
            msg.parameterBag = pBag;
            msg.idKey = @"SHORT_CIRCUIT";
            nextController.title = customTitle;
            [nextController respondToFoundData:msg];
            [self.navigationController pushViewController:nextController animated:YES];
		}
	}
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	if(indexPath.section == 1)
		return 55;
	else 
		return 90;
	
}



#pragma mark -
#pragma mark PopOver Methods
- (void)cancelPicker
{

}


- (void)pickedItem:(NSInteger)pickedTime
{

}

- (void)pickedItemString:(NSString*)pickedKey
{
	for(NSString *typeKey in trainDeliveryData.keys)
	{
		DeliveryData *dd = (trainDeliveryData.items)[typeKey];
		if([dd.name isEqualToString:pickedKey])
		{
			self.deliveryOption = dd.name;
			self.deliveryData = dd;
            [aButtons removeObjectAtIndex:2];
            [aButtons addObject:self.deliveryOption];
            [tableList reloadData];
			return;
		}
	}
	

}


- (void)pickerTapped:(id)sender IndexPath:(NSIndexPath *)indexPath
{
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
	
	
	self.pickerPopOverVC = [[PadPickerPopoverVC alloc] initWithNibName:@"PadPickerPopoverVC" bundle:nil];

	pickerPopOverVC.delegate = self;
	
	NSMutableArray *aDeliveryOptions = [[NSMutableArray alloc] initWithObjects:nil];
	
	for(NSString *typeKey in trainDeliveryData.keys)
	{
		DeliveryData *dd = (trainDeliveryData.items)[typeKey];
		[aDeliveryOptions addObject:dd.name];
	}
	
	pickerPopOverVC.aList = aDeliveryOptions;
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:pickerPopOverVC];  
	
//	int section = [indexPath section];
//	int row = [indexPath row];
//	NSMutableArray *sectionValues = [aList objectAtIndex:section];
//	BookingCellData *bcd = [sectionValues objectAtIndex:row];
	
	//self.pickerPopOver = [[[UIPopoverController alloc] initWithContentViewController:pickerPopOverVC] autorelease];               
	//[pickerPopOverVC initPicker:bcd.extendedTime];
	
	if(deliveryData != nil)
		pickerPopOverVC.key = deliveryData.name;
		//[pickerPopOverVC selectValue:deliveryData.name];
	
//	UIButton *btn = (UIButton *)sender;
//	CGRect sendRect = btn.frame; // [tableList rectForRowAtIndexPath:indexPath];
//	//CGRect myRect = [self.view convertRect:cellRect fromView:];
    
    CGRect cellRect = [tableList rectForRowAtIndexPath:indexPath];
	CGRect myRect = [self.view convertRect:cellRect fromView:tableList];
	
    [self.pickerPopOver presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES]; 
}

#pragma mark - Violation methods
-(NSUInteger)getViolationsCount
{
	return [self.railChoice.violations count];
}

// Checks if any Violations have enforcementLevel != kViolationAllow
-(BOOL) hasDisallowedViolations
{
    if ([railChoice maxEnforcementLevel])
    {
        if ([[railChoice maxEnforcementLevel] intValue] != kViolationAllow)
        {
            return YES;
        }
    }
    return NO;
}

-(NSString*)getViolations
{
	NSArray* violations = self.railChoice.violations;
	
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
	
	if (violationReasonCode != nil)
	{
        TravelViolationReasons *travelViolationReasons = [TravelViolationReasons getSingleton];
		if (travelViolationReasons != nil)
		{
			ViolationReason *violationReason = (travelViolationReasons.violationReasons)[violationReasonCode];
			reason = violationReason.description;
		}
        
	}
	
	return reason;
}

-(NSString*)getViolationJustification
{
	return self.violationJustification;
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

-(void)updateViolationReasons
{
    if (violationReasons == nil || [violationReasons count] == 0)
	{
		NSMutableArray *reasons = [[NSMutableArray alloc] init];
		NSMutableArray *labels = [[NSMutableArray alloc] init];
        
        TravelViolationReasons *travelViolationReasons = [TravelViolationReasons getSingleton];
        if (travelViolationReasons != nil && [travelViolationReasons.violationReasons count] > 0) {
            // car violations are the same as hotel violations
            NSArray *hotelViolations = self.railChoice.violations;
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
		
		self.violationReasons = reasons;
		self.violationReasonLabels = labels;
		
	}
}

#pragma mark UIAlertViewDelegate
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == kAlertGropuAuthUsed && buttonIndex == alertView.cancelButtonIndex)
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
    else if (alertView.tag == kAlertReservationFailed)
    {
        [self makeReserveButton:self];
        // booking train failed.
    }
    else if (alertView.tag == kAlertConfirmReserve)
    {
        if (buttonIndex != alertView.cancelButtonIndex) {
            [self completeReservation];
        }
    }
}
@end
