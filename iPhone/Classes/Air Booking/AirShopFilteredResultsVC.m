//
//  AirShopFilteredResultsVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/10/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AirShopFilteredResultsVC.h"
#import "FormatUtils.h"
#import "AirFilterManager.h"
#import "EntityAirFilter.h"
#import "EntityAirFilterSummary.h"
#import "DateTimeFormatter.h"
#import "SettingsBaseCell.h"
#import "HotelCreditCardViewController.h"
#import "UserConfig.h"
#import "AirSell.h"
#import "TripsData.h"
#import "ConcurMobileAppDelegate.h"
#import "AirLayoverCell.h"
#import "AirBookingCriteriaVC.h"
#import "EntityAirRules.h"
#import "EntityAirViolation.h"
#import "AirRuleManager.h"
#import "AirViolationManager.h"
#import "OptionsSelectVC.h"
#import "TravelCustomField.h"
#import "FieldOptionsViewController.h"
#import "CustomFieldTextEditor.h"
#import "BoolEditCell.h"
#import "BookingCellData.h"
#import "SystemConfig.h"
#import "ViolationReason.h"
#import "HotelBookingCell.h"
#import "HotelOptionsViewController.h"
#import "HotelTextEditorViewController.h"
#import "TravelViolationReasons.h"
#import "Config.h"
#import "PreSellCustomFieldSelectVC.h"
#import "ManageViolationsVC.h"

#import "GovTAField.h"
//#import "GovDocDetailVC.h"
//#import "GovDocDetailVC_iPad.h"
//#import "GovDocInfoFromTripLocatorData.h"


#define kFieldIdFrequentFlyer @"FrequentFlyer"
#define kAlertTagFrequentFlyerSoftStop  300201
#define kAlertUnhandledViolations 300202
#define kAlertGropuAuthUsed 300203
#define kSectionViolation 200
#define kSectionRuleMessages 1000
#define kSectionCC 999
#define kFlightPosAffinity 997
#define kCreditCardCvv 76598
#define kSectionTripFields 1001
#define kSectionPreSellFlightOptions 12112

#define kRowManageViolations @"RowManageViolations"
#define kRowUsingPointsViolations @"RowUsingPointsViolations"
#define kRowViolationsText @"RowViolationsText"
#define kRowViolationReason @"RowViolationReason"
#define kRowViolationJustification @"RowViolationJustification"

@interface AirShopFilteredResultsVC ()
@property (nonatomic, strong) NSArray *rowsInViolationSection;
-(NSString*)getViolationsByFareId:(NSString *)fareId;
-(NSString*)getViolationReason;
-(NSString*)getViolationJustification;
-(int)getIndexForViolationReasonCode:(NSString*)reasonCode;
-(BOOL) hasEnforcementLevel:(int)level;

-(void) completeReservation;

-(BOOL) hasDisallowedViolations;

@end

@implementation AirShopFilteredResultsVC
@synthesize fetchedResultsController=__fetchedResultsController;
@synthesize managedObjectContext=__managedObjectContext;
@synthesize tableList, airShopResults, airSummary, airShop, chosenCardIndex, chosenCreditCard, aClass, dictClass, aButtons;
@synthesize chosenFrequentFlyer;
@synthesize violationReasons, violationReasonLabels, aSections, hideCustomFields, tcfRows, selectedCustomField, editedDependentCustomField, viewSegmentHeader, dictSections, isDirty;
@synthesize taFields;

-(NSString *)getViewIDKey
{
	return @"AIR_BOOKING";
}

-(void) switchToTripDetailView:(NSString*) itinLocator
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SKIP_PARSE", nil];
    
    EntityTrip *trip;
    NSString *tripKey;
    
    trip = [[TripManager sharedInstance] fetchByItinLocator:itinLocator];
    tripKey = trip.tripKey;
    
    // We came here from the home screen, so pop all the way back to it before going to the trip view.
    pBag[@"POP_TO_ROOT_VIEW"] = @"YES";
    
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
            //MOB-12699 After booking, trip detail not shown.
            // If the flight detail screen is already being shown, then pop it (Add booking to an exsiting trip)
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
            [ConcurMobileAppDelegate switchToView:TRIP_DETAILS viewFrom:AIR_SELL ParameterBag:pBag];
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



#pragma mark - MVC Methods
-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:AIR_SELL])
	{
		AirSell *as = (AirSell *)msg.responder;
		
		if([as.obj.sellStatus isEqualToString:@"SUCCESS"])
		{
            airRezResponse = as.obj; // For GOV tripLocator and AuthorizationNumber
            if (airRezResponse.tripLocator && airRezResponse.itinLocator)
            {
                NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:as.obj.tripLocator, @"RECORD_LOCATOR", @"AMTRAK_DETAIL", @"TO_VIEW",nil];
                [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
            }
		}
		else {
			//whoops
            [self hideWaitView];
			[self hideLoadingView];
            MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"Error" localize]
                                                                    message:([as.obj.errorMessage length] ? as.obj.errorMessage : [@"The flight could not be booked." localize])
                                                                   delegate:nil
                                                          cancelButtonTitle:[LABEL_CLOSE_BTN localize]
                                                          otherButtonTitles:nil];
            [alert show];
		}
	}
    else if ([msg.idKey isEqualToString:PRE_SELL_OPTIONS])
	{
        self.isPreSellOptionsLoaded = YES;
        self.preSellOptions = (PreSellOptions *)msg.responder;
        self.creditCards = self.preSellOptions.creditCards;
        self.affinityPrograms = self.preSellOptions.affinityPrograms;
        
        if ([self isViewLoaded])
        {
            [self hideLoadingView];
            [self hideWaitView];
        }
        
        if (self.preSellOptions.isCreditCardCvvRequired)
            [self createCvvRow];
        [self createPreSellFlightOptionsSection];
        
        if ([self.affinityPrograms count])
        {
            [self createAffinitySection];
            if (self.preSellOptions.defaultProgram)
                [self optionSelected:self.preSellOptions.defaultProgram withIdentifier:kFieldIdFrequentFlyer];
        }
        [self chooseCard:([self.creditCards count] ? 0 : -1)];
        
        if (!self.preSellOptions.isRequestSuccessful)
        {
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
        if ([self isViewLoaded])
        {
            self.navigationItem.rightBarButtonItem.enabled = YES;
            if (self.isPreSellOptionsLoaded)
            {
                [self hideLoadingView];
                [self hideWaitView];
            }
        }
        
        if (msg.errBody == nil && msg.responseCode == 200) 
        {
            [aSections removeObject:@"TRIP_FIELDS"]; // Removes the instance if any. Will be re-added if needed.
            self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAllFieldsAtStart:NO];
            if (tcfRows != nil && [tcfRows count] > 0)
            {
                [aSections addObject:@"TRIP_FIELDS"];
                [dictSections removeObjectForKey:@"TRIP_FIELDS"];
                dictSections[@"TRIP_FIELDS"] = tcfRows;
            }
            [tableList reloadData];
        }
    }
	else if ([msg.idKey isEqualToString:TRIPS_DATA] && (msg.parameterBag)[@"ITIN_LOCATOR"])
	{
		[self hideLoadingView];
        [self hideWaitView];
        
        NSString *itinLocator = (NSString*)(msg.parameterBag)[@"ITIN_LOCATOR"];
        [self switchToTripDetailView:itinLocator];

	}
    else if ([msg.idKey isEqualToString:TRIPS_DATA])
    {
        NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:airRezResponse.tripLocator, @"RECORD_LOCATOR", @"AMTRAK_DETAIL", @"TO_VIEW",airRezResponse.itinLocator, @"ITIN_LOCATOR", nil];
        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    else if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
    {
        NSString *optionType = (msg.parameterBag)[@"OPTION_TYPE_ID"];
        if (optionType != nil)
        {
            NSNumber* selectedRowIndexNumber = (NSNumber*)(msg.parameterBag)[@"SELECTED_ROW_INDEX"];
            NSUInteger selectedRowIndex = [selectedRowIndexNumber intValue];
            
            if (violationReasons != nil && [violationReasons count] > selectedRowIndex) 
            {
                ViolationReason *reason = violationReasons[selectedRowIndex]; 
                
                airSummary.violationReason = reason.description;
                if(airSummary.relAirViolationCurrent == nil)
                    airSummary.relAirViolationCurrent = [[AirFilterSummaryManager sharedInstance] makeNewViolation];
                
                airSummary.relAirViolationCurrent.code = reason.code;
                airSummary.relAirViolationCurrent.message = reason.description;
                [[AirFilterSummaryManager sharedInstance] saveIt:airSummary];
            }
        }
        else if (msg.parameterBag[@"FROM_INDEX_PATH"] != nil)
        {
            NSString *userEnteredText = (NSString*)(msg.parameterBag)[@"TEXT"];
            NSIndexPath *indexPath = (NSIndexPath *)(msg.parameterBag)[@"FROM_INDEX_PATH"];
            if ([aSections[indexPath.section] intValue] == kSectionPreSellFlightOptions )
            {
                NSString *sectionName = aSections[indexPath.section];
                NSArray *a = dictSections[sectionName];
                PreSellCustomField *pscf = (PreSellCustomField *)a[indexPath.row];
                pscf.userInputValue = [userEnteredText lengthIgnoreWhitespace] ? userEnteredText : nil;
            }
            else
            {
                NSString *sectionName = aSections[indexPath.section];
                NSArray *a = dictSections[sectionName];
                EntityAirFilter *entity = (EntityAirFilter *)a[indexPath.row];
                if([entity.segmentPos intValue] == kSectionCC && [entity.flightPos intValue] == kCreditCardCvv)
                    self.creditCardCvvNumber = userEnteredText;
            }
        }
        else if ((msg.parameterBag)[@"TEXT"] != nil)
        {
            airSummary.violationJustification = (NSString*)(msg.parameterBag)[@"TEXT"];
        }
        else if ((msg.parameterBag)[@"USE_TRAVEL_POINTS"] != nil)
		{
            airSummary.isUsingPointsAgainstViolations = (NSNumber*)(msg.parameterBag)[@"USE_TRAVEL_POINTS"];
		}
        
        [tableList reloadData];
        [self makeReserveButton:self];
    }
}

#pragma mark - View Controller Stuff

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle
-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setToolbarHidden:YES];
    self.title = [Localizer getLocalizedText:@"Flight Details"];
    
    if (violationReasons == nil || [violationReasons count] == 0)
	{
		NSMutableArray *reasons = [[NSMutableArray alloc] init];
		NSMutableArray *labels = [[NSMutableArray alloc] init];
		
        TravelViolationReasons *travelViolationReasons = [TravelViolationReasons getSingleton];
        if (travelViolationReasons != nil && [travelViolationReasons.violationReasons count] > 0) {
            NSArray *airViolations = [[AirViolationManager sharedInstance] fetchByFareId:airSummary.fareId];
            
            NSMutableArray *violationTypes = [[NSMutableArray alloc] initWithObjects:nil];
            for (EntityAirViolation *airViolation in airViolations) {
                [violationTypes addObject:airViolation.violationType];
            }
            
            NSMutableArray *tmpReasons = [travelViolationReasons getReasonsFor:violationTypes];
            for (ViolationReason *reason in tmpReasons) {
                [reasons addObject:reason];
                [labels addObject:reason.description];
            }
        }
        
        /*
		SystemConfig *systemConfig = [SystemConfig getSingleton];
		if (systemConfig != nil && [systemConfig.airViolationReasons count] > 0)
		{
			NSArray* allKeys = [systemConfig.airViolationReasons allKeys];
			for (NSString *key in allKeys)
			{
				ViolationReason	*reason = [systemConfig.airViolationReasons objectForKey:key];
				[reasons addObject:reason];
				[labels addObject:reason.description];
			}
		}
        */
		
		self.violationReasons = reasons;
		self.violationReasonLabels = labels;
		
	}
    
    if ([self hasEnforcementLevel:kViolationAutoFail]) {
        [self.navigationItem.rightBarButtonItem setEnabled:NO];
    }
    
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    //Custom Fields
    if (isDirty) 
    {
        if (editedDependentCustomField && [selectedCustomField.attributeValue length])
            [self updateDynamicCustomFields];
        else
            [self reloadCustomFieldsSection];

        isDirty = NO;// MOB-9648 hold off reserve action
        
    }
    else if (self.reloadFlightOptionsSection)
    {
        [self refreshFlightOptionsSection];
        self.reloadFlightOptionsSection = NO;
    }
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Need to check if there is a benchmark to display or not
//    if (airShop.benchmark != nil && airShop.benchmark.price != nil)
//    {
//        if (self.lblBenchmark.hidden)
//        {
//            // Only make adjustments if the we have a benchmark to show and the label is currently hidden
//            // Just incase we are reusing old object references
//            // I'm being very defensive here
//            CGRect oldframe = [self.imageView frame];
//            [self.lblBenchmark setHidden:NO];
//            [self.imageView setFrame:CGRectMake(oldframe.origin.x,
//                                                oldframe.origin.y,
//                                                oldframe.size.width,
//                                                oldframe.size.height + 14)];
//            oldframe = [self.tableList frame];
//            [self.tableList setFrame:CGRectMake(oldframe.origin.x,
//                                                oldframe.origin.y + 14,
//                                                oldframe.size.width,
//                                                oldframe.size.height - 14)];
//        }
//    }
//    else
//    {
//        if (!self.lblBenchmark.hidden)
//        {
//            // Only make adjustments if we have no benchmark and the label is visible
//            // Just incase we are reusing old object references
//            // I'm being very defensive here
//            CGRect oldframe = [self.imageView frame];
//            [self.lblBenchmark setHidden:YES];
//            [self.imageView setFrame:CGRectMake(oldframe.origin.x,
//                                                oldframe.origin.y,
//                                                oldframe.size.width,
//                                                oldframe.size.height - 14)];
//            oldframe = [self.tableList frame];
//            [self.tableList setFrame:CGRectMake(oldframe.origin.x,
//                                                oldframe.origin.y - 14,
//                                                oldframe.size.width,
//                                                oldframe.size.height + 14)];
//        }
//    }

    //Custom Fields
    self.aSections = [[NSMutableArray alloc] initWithObjects: nil];
    self.dictSections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    
    [self fillClass];
    self.aButtons = [[NSMutableArray alloc] initWithObjects:@"Total", @"Card", @"Delivery",  nil];
    self.chosenCardIndex = -1;
    [self chooseFirstCard];
    self.title = [Localizer getLocalizedText:@"Flight Details"];
    
    // Do any additional setup after loading the view from its nib.
    [self.navigationController setToolbarHidden:YES];
    
    //[self makeFake];
    NSArray *aFilters = [[AirFilterManager sharedInstance] fetchByFareIdSegmentPos:airSummary.fareId segPos:kSectionCC];
    for(EntityAirFilter *aFilter in aFilters)
    {
        [[AirFilterManager sharedInstance] deleteObj:aFilter];
    }
    
    aFilters = [[AirFilterManager sharedInstance] fetchByFareIdSegmentPos:airSummary.fareId segPos:kSectionRuleMessages];
    for(EntityAirFilter *aFilter in aFilters)
    {
        [[AirFilterManager sharedInstance] deleteObj:aFilter];
    }
    
    aFilters = [[AirFilterManager sharedInstance] fetchByFareIdSegmentPos:airSummary.fareId segPos:kSectionViolation];
    for(EntityAirFilter *aFilter in aFilters)
    {
        [[AirFilterManager sharedInstance] deleteObj:aFilter];
    }
    
//    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//    self.managedObjectContext = [ad managedObjectContext];
    
    //Violations
    
    // MOB-10543 Violations with enforcement level zero (meaning allowed) are not really violations.
    // No justification or explanations are required.
    //
    // The only time we should show violations is if there is at least once with an enforcement level
    // that is not zero, i.e. that is not allowed.
    //
    if ([self getViolationsCount])
    {
        NSString *violationCrnCode = [self getViolationsByFareId:airSummary.fareId];
        int numberOfRowsInViolationSection = [self hasDisallowedViolations] ? 3 : ([[violationCrnCode stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] length] ? 1 : 0); // Only show Violation Justification and code rows if user input is required.
        for (int j=0; j < numberOfRowsInViolationSection; j++)
        {
            EntityAirFilter *afViolation = (EntityAirFilter*)[[AirFilterManager sharedInstance] makeNew];
            afViolation.segmentPos = @kSectionViolation;
            afViolation.flightPos = @kSectionViolation;
            afViolation.crnCode = violationCrnCode;
            afViolation.fareId = airSummary.fareId;
            [[AirFilterManager sharedInstance] saveIt:afViolation];
            afViolation = nil;
        }
    }
    
    //Credit Card
    EntityAirFilter *afCard = (EntityAirFilter*)[[AirFilterManager sharedInstance] makeNew];
    afCard.segmentPos = @kSectionCC;
    afCard.flightPos = @kSectionCC;
    afCard.fareId = airSummary.fareId;
    afCard.bic = aButtons[1];
    [[AirFilterManager sharedInstance] saveIt:afCard];
    afCard = nil;

    //Total Cost
    EntityAirFilter *afTotal = (EntityAirFilter*)[[AirFilterManager sharedInstance] makeNew];
    afTotal.segmentPos = @kSectionCC;
    afTotal.flightPos = @998;
    afTotal.crnCode = airSummary.crnCode;
    afTotal.fare = airSummary.fare;
    afTotal.fareId = airSummary.fareId;
    [[AirFilterManager sharedInstance] saveIt:afTotal];
    afTotal = nil;
    
    NSArray *airRuleArray = [[AirRuleManager sharedInstance] fetchByFareId:airSummary.fareId];
    int i = 0;
    for(EntityAirRules *airRule in airRuleArray)
    {
        EntityAirFilter *af = (EntityAirFilter*)[[AirFilterManager sharedInstance] makeNew];
        af.segmentPos = @kSectionRuleMessages;
        int ix = ([airRule.exceptionLevel intValue] + i) * -1;
        af.flightPos = @(ix);
        af.crnCode = airRule.exceptionMessage;
        af.fare = airRule.exceptionLevel;
        af.fareId = airSummary.fareId;
        [[AirFilterManager sharedInstance] saveIt:af];
        af = nil;
    }

    [self processFilterDataForArray];
    
    if (!hideCustomFields) 
    {
        [self fetchCustomFields];
        
        // prepopulate the custom fields from cache
        [aSections removeObject:@"TRIP_FIELDS"]; // Removes the instance if any. Will be re-added if needed.
        self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAllFieldsAtStart:NO];
        if (tcfRows != nil && [tcfRows count] > 0)
        {
            [aSections addObject:@"TRIP_FIELDS"];
            dictSections[@"TRIP_FIELDS"] = self.tcfRows;
        }
    }
    else 
        [self hideLoadingView];
    
    [tableList reloadData];
//    [self refetchData];
    [self makeReserveButton:nil];
    [self fetchPreSellOptions];
}

-(void) createAffinitySection
{
    if ([self.affinityPrograms count])
    {
        //Frequent Flyer or Affinity
        EntityAirFilter *afFrequentFlyer = (EntityAirFilter*)[[AirFilterManager sharedInstance] makeNew];
        afFrequentFlyer.segmentPos = @kSectionCC;
        afFrequentFlyer.flightPos = @kFlightPosAffinity;
        afFrequentFlyer.crnCode = airSummary.crnCode;
        afFrequentFlyer.fare = airSummary.fare;
        afFrequentFlyer.fareId = airSummary.fareId;
        afFrequentFlyer.bic = [Localizer getLocalizedText:@"Please Specify Program"];  // Start without selecting any default one
        [[AirFilterManager sharedInstance] saveIt:afFrequentFlyer];
        //Add the Affinity Program node to CC section
        NSMutableArray *a = dictSections[[NSString stringWithFormat:@"%d",kSectionCC]];
        [a insertObject:afFrequentFlyer atIndex:0];
    }
}

-(void) createPreSellFlightOptionsSection
{
    if ([self.preSellOptions.optionItems count])
    {
        dictSections[[NSString stringWithFormat:@"%d",kSectionPreSellFlightOptions]] = self.preSellOptions.optionItems;
        if ([[aSections lastObject] isEqualToString:@"TRIP_FIELDS"])
            [aSections insertObject:[NSString stringWithFormat:@"%d",kSectionPreSellFlightOptions] atIndex:[aSections count]-1];
        else
            [aSections addObject:[NSString stringWithFormat:@"%d",kSectionPreSellFlightOptions]];
    }
}

-(void) createCvvRow
{
    //Frequent Flyer or Affinity
    EntityAirFilter *afFrequentFlyer = (EntityAirFilter*)[[AirFilterManager sharedInstance] makeNew];
    afFrequentFlyer.segmentPos = @kSectionCC;
    afFrequentFlyer.flightPos = @kCreditCardCvv;
    afFrequentFlyer.crnCode = airSummary.crnCode;
    afFrequentFlyer.fare = airSummary.fare;
    afFrequentFlyer.fareId = airSummary.fareId;
    afFrequentFlyer.bic = [@"Please specify" localize];  // Start without selecting any default one
    [[AirFilterManager sharedInstance] saveIt:afFrequentFlyer];
    //Add the Affinity Program node to CC section
    NSMutableArray *a = dictSections[[NSString stringWithFormat:@"%d",kSectionCC]];
    [a addObject:afFrequentFlyer];
}

-(void) processFilterDataForArray
{
    NSArray *a = [[AirFilterManager sharedInstance] fetchAirFilters:airSummary.fareId];
    NSMutableArray *aFilters = [[NSMutableArray alloc] initWithObjects: nil];
    int segPos = -1;
    for(int i = 0; i < [a count]; i++)
    {
        //NSLog(@"segmentPos %d", segPos);
        EntityAirFilter *filter = a[i];
        if(segPos == -1)
        {
            aFilters = [[NSMutableArray alloc] initWithObjects: nil];
            segPos = [filter.segmentPos intValue];
        }
        else if([filter.segmentPos intValue] != segPos)
        {
            //new section
            [dictSections removeObjectForKey:[NSString stringWithFormat:@"%i", segPos]];
            dictSections[[NSString stringWithFormat:@"%i", segPos]] = aFilters;
            [aSections removeObject:[NSString stringWithFormat:@"%i", segPos]];
            [aSections addObject:[NSString stringWithFormat:@"%i", segPos]];
            aFilters = [[NSMutableArray alloc] initWithObjects: nil];
            segPos = [filter.segmentPos intValue];
        }
        
        [aFilters addObject:filter];
    }
    
    //do the last one
    [dictSections removeObjectForKey:[NSString stringWithFormat:@"%i", segPos]];
    dictSections[[NSString stringWithFormat:@"%i", segPos]] = aFilters;
    [aSections removeObject:[NSString stringWithFormat:@"%i", segPos]];
    [aSections addObject:[NSString stringWithFormat:@"%i", segPos]];

}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.viewSegmentHeader = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark -
#pragma mark Table View Data Source Methods
-(UITableViewCell *)cellForViolationSection:(UITableView *)tableView row:(NSUInteger)row
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
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    
    if ([kRowViolationsText isEqualToString:self.rowsInViolationSection[row]])
    {
        NSString *label = [self hasDisallowedViolations] ? [@"Violation" localize] : [@"Travel Policy" localize];
        NSString *value = [self getViolationsByFareId:airSummary.fareId];
        
        cell.lblLabel.text = label;
        cell.lblValue.text = value;
    }
    else if ([kRowViolationReason isEqualToString:self.rowsInViolationSection[row]])
    {
        NSString *reason = [self getViolationReason];
        
        NSString *label = [Localizer getLocalizedText:@"Violation Reason"];
        NSString *value = ([reason length] ? reason : [Localizer getLocalizedText:@"Please specify"]);
        
        cell.lblLabel.text = label;
        cell.lblValue.text = value;
        
        if (![reason length])
            cell.lblValue.textColor = [UIColor redColor];
    }
    else if ([kRowViolationJustification isEqualToString:self.rowsInViolationSection[row]])
    {
        NSString *justification  = [self getViolationJustification];
        NSString *label = [Localizer getLocalizedText:@"Violation Justification"];
        NSString *value = ([justification length] ? justification : [Localizer getLocalizedText:@"Please specify"]);
        
        cell.lblLabel.text = label;
        cell.lblValue.text = value;
        
        //MOB-10484
        if (![justification length] && [SystemConfig getSingleton].ruleViolationExplanationRequired )
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
        
        if (self.airSummary.isUsingPointsAgainstViolations)
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
        NSString *value = [NSString stringWithFormat:[@"You are using %@ Travel Points." localize],[@(abs([self.airSummary.travelPoints intValue])) stringValue]];
        
        cell.lblLabel.text = label;
        cell.lblValue.text = value;
        cell.accessoryType = UITableViewCellAccessoryNone;
    }
    
    return cell;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [aSections count];
//    return [[self.fetchedResultsController sections] count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
//    id <NSFetchedResultsSectionInfo> sectionInfo = [[self.fetchedResultsController sections] objectAtIndex:section];
//    return [sectionInfo numberOfObjects];
    NSString *sectionName = aSections[section];
    if ([sectionName intValue] == kSectionViolation) {
        return [self numberOfRowsInViolationsSection];
    }
    NSArray *a = dictSections[sectionName];
    return [a count];
}

-(int)numberOfRowsInViolationsSection
{
    if ([self.airSummary.canUseTravelPoints boolValue])
    {
        if (self.airSummary.isUsingPointsAgainstViolations) // User has made a selection or not
        {
            if ([self.airSummary.isUsingPointsAgainstViolations boolValue])
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

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    NSString *sectionName = aSections[indexPath.section];
    if ([sectionName isEqualToString:@"TRIP_FIELDS"])
    {
        return [self configureCustomFieldCellAtIndexPath:indexPath];
    }
    else if ([sectionName intValue] == kSectionPreSellFlightOptions)
    {
        return [self configureFlightOptionCellAtIndexPath:indexPath];
    }
    else if([sectionName intValue] == kSectionViolation)
    {
        return [self cellForViolationSection:tableView row:indexPath.row];
    }
    else
    {
//        NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
        NSString *sectionName = aSections[indexPath.section];
        NSArray *a = dictSections[sectionName];
        EntityAirFilter *entity = (EntityAirFilter *)a[indexPath.row];

        if([entity.segmentPos intValue] == kSectionCC)
        {//Credit Card & Total
            SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"LabelCell"];
            if (cell == nil)  
            {
                NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"LabelCell" owner:self options:nil];
                for (id oneObject in nib)
                    if ([oneObject isKindOfClass:[SettingsBaseCell class]])
                        cell = (SettingsBaseCell *)oneObject;
            }
            cell.lblRefundable.hidden = YES;
            cell.lblTravelPoints.hidden = YES;
            
            if([entity.flightPos intValue] == 998)
            {
                NSString *crnCode = entity.crnCode;
                if(![crnCode length])
                    crnCode = @"USD";
                cell.lblSubheading.text = [Localizer getLocalizedText:@"Total Price"];
                cell.lblHeading.text = [FormatUtils formatMoney:[entity.fare stringValue] crnCode:crnCode];
                
                if([self.airSummary.refundable boolValue])
                {
                    cell.lblRefundable.hidden = NO;
                    cell.lblRefundable.text = [Localizer getLocalizedText:@"(Refundable)"];
                }
                
                [cell.lblHeading setShadowOffset:CGSizeMake(0, 0)];
                cell.lblHeading.textColor = [UIColor bookingBlueColor];
//                if(airSummary.maxEnforcementLevel != nil)
//                {
//                    int eLevel = [airSummary.maxEnforcementLevel intValue];
//                    if(eLevel < kViolationLogForReportsOnly || eLevel == 100)
//                    {
//                        [cell.lblHeading setTextColor:[UIColor bookingGreenColor]];
//                    }
//                    else if(eLevel >= kViolationLogForReportsOnly && eLevel <= kViolationNotifyManager)
//                    {
//                        [cell.lblHeading setTextColor:[UIColor bookingYellowColor]];
//                    }    
//                    else if(eLevel >= kViolationRequiresPassiveApproval && eLevel <= kViolationRequiresApproval)
//                    {
//                        [cell.lblHeading setTextColor:[UIColor bookingRedColor]];
//                    } 
//                    else if(eLevel == kViolationAutoFail)
//                    {
//                        [cell.lblHeading setTextColor:[UIColor bookingGrayColor]];
//                    } 
//                    else
//                    {
//                        [cell.lblHeading setTextColor:[UIColor bookingRedColor]];
//                    }
//                }
//                else
//                {
//                    [cell.lblHeading setTextColor:[UIColor bookingGreenColor]];
//                }
                
                if ([airSummary.travelPoints intValue] != 0 && (!airSummary.isUsingPointsAgainstViolations || [airSummary.isUsingPointsAgainstViolations boolValue]))
                {
                    cell.lblTravelPoints.hidden = NO;
                    if ([airSummary.travelPoints intValue] > 0) {
                        cell.lblTravelPoints.text = [NSString stringWithFormat:[@"Earn %d pts." localize],[airSummary.travelPoints intValue]];
                        cell.lblTravelPoints.textColor = [UIColor bookingGreenColor];
                    }
                    else {
                        cell.lblTravelPoints.text = [NSString stringWithFormat:[@"Use %d pts." localize],-[airSummary.travelPoints intValue]];
                        cell.lblTravelPoints.textColor = [UIColor bookingRedColor];
                    }
                }
            }
            else if([entity.flightPos intValue] == kFlightPosAffinity)
            {
                cell.lblSubheading.text = [Localizer getLocalizedText:@"Frequent Flyer Program"];
                cell.lblHeading.text = entity.bic;
                cell.lblHeading.textColor =  [UIColor blackColor];
            }
            else if ([entity.flightPos intValue] == kCreditCardCvv)
            {
                cell.lblSubheading.text = [@"CVV Number" localize];
                cell.lblHeading.text = entity.bic;
                cell.lblHeading.textColor =  [self.creditCardCvvNumber length] ? [UIColor blackColor] : [UIColor redColor];
            }
            else
            {
                cell.lblSubheading.text = [Localizer getLocalizedText:@"Card"];
                cell.lblHeading.text = entity.bic;
                cell.lblHeading.textColor =  [UIColor blackColor];
            }
            
            if([entity.flightPos intValue] == kFlightPosAffinity || [entity.flightPos intValue] == kCreditCardCvv || ([cell.lblSubheading.text isEqualToString:@"Card"] && [self.creditCards count]))
                [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
            else
                [cell setAccessoryType:UITableViewCellAccessoryNone];
            
            return cell;
        }
        else if([entity.segmentPos intValue] == kSectionRuleMessages)
        {//Credit Card & Total
            AirShopMessageCell *cell = (AirShopMessageCell *)[tableView dequeueReusableCellWithIdentifier: @"AirShopMessageCell"];
            if (cell == nil)  
            {
                NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"AirShopMessageCell" owner:self options:nil];
                for (id oneObject in nib)
                    if ([oneObject isKindOfClass:[AirShopMessageCell class]])
                        cell = (AirShopMessageCell *)oneObject;
            }
            
            if([entity.fare intValue] < 21)
            {
                cell.ivIcon.image = [UIImage imageNamed:@"icon_yellowex"];
            }
            else if([entity.fare intValue] > 20)
            {
                cell.ivIcon.image = [UIImage imageNamed:@"icon_redex"];

            }
            else
            {
                cell.ivIcon.image = nil;
            }
            
            cell.lblText.text = entity.crnCode;
            
            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
            
            return cell;
        }
        else if([entity.carrier isEqualToString:@"LAYOVER"])
        {
            AirLayoverCell *cell = (AirLayoverCell *)[tableView dequeueReusableCellWithIdentifier: @"AirLayoverCell"];
            if (cell == nil)  
            {
                NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"AirLayoverCell" owner:self options:nil];
                for (id oneObject in nib)
                    if ([oneObject isKindOfClass:[AirLayoverCell class]])
                        cell = (AirLayoverCell *)oneObject;
            }
            
            NSString *layover = @"%@ %@ %@";
            
            int flightMinutes = [entity.elapsedTime intValue];
            int flightHours = flightMinutes / 60;
            if (flightHours > 0) 
                flightMinutes = flightMinutes - (flightHours * 60);
            NSString *dur = [NSString stringWithFormat:@"%dh %dm", flightHours, flightMinutes];
            
            cell.lblLayover.text = [NSString stringWithFormat:layover, dur, [Localizer getLocalizedText:@"Layover in"], entity.startIata];
            return cell;
        }
        else
        {
            AirShopFilteredCell *cell = (AirShopFilteredCell *)[tableView dequeueReusableCellWithIdentifier: @"AirShopFilteredCell"];
            if (cell == nil)  
            {
                NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"AirShopFilteredCell" owner:self options:nil];
                for (id oneObject in nib)
                    if ([oneObject isKindOfClass:[AirShopFilteredCell class]])
                        cell = (AirShopFilteredCell *)oneObject;
            }
            
            [self configureCell:cell atIndexPath:indexPath];
            return cell;
        }
    }
}


#pragma mark -
#pragma mark Table Delegate Methods 
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
{	
    NSString *sectionName = aSections[section];
    if ([sectionName isEqualToString:@"TRIP_FIELDS"]) 
        return [@"Booking Info" localize];
    else {
//        return @"BM LINX";
//        id <NSFetchedResultsSectionInfo> sectionInfo = [[self.fetchedResultsController sections] objectAtIndex:section];
        
        if ([sectionName intValue] == kSectionViolation)
            return [self hasDisallowedViolations] ? [@"Violation" localize] : @"";
        else if([sectionName intValue] == 0)
            return [Localizer getLocalizedText:@"Departure"];
        else if([sectionName intValue] == kSectionCC)
            return @"";
        else if([sectionName intValue] == kSectionRuleMessages)
            return [Localizer getLocalizedText:@"Rule Messages"];
        else if([sectionName intValue] == kSectionTripFields)
            return [Localizer getLocalizedText:@"Trip Fields"];
        else if([sectionName intValue] == kSectionPreSellFlightOptions)
            return [Localizer getLocalizedText:@"Flight Options"];
        else 
            return [Localizer getLocalizedText:@"Returning"];
    }
	
}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    NSString *sectionName = aSections[newIndexPath.section];
    if ([sectionName isEqualToString:@"TRIP_FIELDS"])
    {
        self.isDirty = YES;
        NSArray *a = dictSections[sectionName];
        EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)a[newIndexPath.row];  //[tcfRows objectAtIndex:[newIndexPath row]];
        
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
            [self onSelectLongTextOrNumericFieldCellAtIndexPath:newIndexPath];
    }
    else if ([sectionName intValue] == kSectionPreSellFlightOptions)
    {
        [self showFlightOptionCellEditorAtIndexPath:newIndexPath];
    }
    else if ([sectionName intValue] == kSectionViolation)
    {
        [self handleSelectInViolationSectionAtRow:newIndexPath.row];
    }
    else 
    {  
//        NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:newIndexPath];
//        EntityAirFilter *entity = (EntityAirFilter *)managedObject;
        NSString *sectionName = aSections[newIndexPath.section];
        NSArray *a = dictSections[sectionName];
        EntityAirFilter *entity = (EntityAirFilter *)a[newIndexPath.row];
        if([entity.flightPos intValue] == kSectionCC)
            [self showCards:nil];
        else if([entity.flightPos intValue] == kCreditCardCvv)
            [self showTextEditorWithTitle:[@"CVV Number" localize] textValue:self.creditCardCvvNumber indexPath:newIndexPath placeholderText:[@"Please specify" localize]];
        else if([entity.flightPos intValue] == kFlightPosAffinity)
            [self showAffinityPrograms:nil];
        else if([entity.segmentPos intValue] == kSectionRuleMessages)
        {
            ViolationDetailsVC *vc = [[ViolationDetailsVC alloc] initWithNibName:@"ViolationDetailsVC" bundle:nil];
            vc.violationText = entity.crnCode;
            [self.navigationController pushViewController:vc animated:YES];
        }
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *sectionName = aSections[indexPath.section];
    if ([sectionName intValue] == kSectionViolation) {
        return [kRowManageViolations isEqualToString:self.rowsInViolationSection[indexPath.row]] ? 44 : 64;
    }
    else if ([sectionName isEqualToString:@"TRIP_FIELDS"] || [sectionName intValue] == kSectionPreSellFlightOptions)
        return 65;
    else {
        NSString *sectionName = aSections[indexPath.section];
        NSArray *a = dictSections[sectionName];
//        NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
        EntityAirFilter *entity = (EntityAirFilter *)a[indexPath.row];
        
        if([entity.carrier isEqualToString:@"LAYOVER"])
            return 30;
        else if(entity.operatingCarrier != nil)
            return 80;
        else if([entity.segmentPos intValue] == kSectionCC)
        {
            if ([entity.flightPos intValue] == 998 && [airSummary.travelPoints intValue] !=0 && (!airSummary.isUsingPointsAgainstViolations || [airSummary.isUsingPointsAgainstViolations boolValue]))
                return 64;
            return 54;
        }
        else
            return 64;	
    }
}

- (void)handleSelectInViolationSectionAtRow:(int)row
{
    if ([kRowViolationsText isEqualToString:self.rowsInViolationSection[row]])
    {
        ViolationDetailsVC *vc = [[ViolationDetailsVC alloc] initWithNibName:@"ViolationDetailsVC" bundle:nil];
        vc.violationText = [self getViolationsByFareId:airSummary.fareId];
        [self.navigationController pushViewController:vc animated:YES];
    }
    else if ([kRowViolationReason isEqualToString:self.rowsInViolationSection[row]])
    {
        NSString *optionsViewTitle = [Localizer getLocalizedText:@"Select Reason"];
        NSString *optionType = @"VIOLATION_REASON";
        NSArray *labels = violationReasonLabels;
        
        int currentReasonIndex = [self getIndexForViolationReasonCode:airSummary.relAirViolationCurrent.code];
        
        NSString *currentCode = airSummary.relAirViolationCurrent.code;
        
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
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] init];
        pBag[@"FROM_VIEW"] = [self getViewIDKey];
        pBag[@"OPTION_TYPE_ID"] = optionType;
        pBag[@"LABELS"] = labels;
        pBag[@"PREFERRED_FONT_SIZE"] = preferredFontSize;
        pBag[@"SHORT_CIRCUIT"] = @"YES";
        
        if (currentReasonIndex >= 0)
            pBag[@"SELECTED_ROW_INDEX"] = @(currentReasonIndex);
        
        HotelOptionsViewController *nextController = [[HotelOptionsViewController alloc] initWithNibName:@"HotelOptionsViewController" bundle:nil];
        nextController.title = optionsViewTitle;
        
        Msg *msg = [[Msg alloc] init];
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        [nextController respondToFoundData:msg];
        [self.navigationController pushViewController:nextController animated:YES];
        nextController.title = optionsViewTitle;
        
    }
    else if ([kRowViolationJustification isEqualToString:self.rowsInViolationSection[row]])
    {
        NSString *customTitle = [Localizer getLocalizedText:@"Violation Justification"];
        NSString *placeholder = [Localizer getLocalizedText:@"Please enter a justification for this booking."];
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"FROM_VIEW",  @"YES", @"SHORT_CIRCUIT", nil];
        NSString *justification = [self getViolationJustification];
        if ([justification length])
            pBag[@"TEXT"] = justification;
        
        HotelTextEditorViewController *nextController = [[HotelTextEditorViewController alloc] initWithNibName:@"HotelTextEditorViewController" bundle:nil];
        Msg *msg = [[Msg alloc] init];
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        [nextController respondToFoundData:msg];
        nextController.placeholderText = placeholder;
        nextController.title = customTitle;
        [self.navigationController pushViewController:nextController animated:YES];
    }
    else if([kRowManageViolations isEqualToString:self.rowsInViolationSection[row]])
    {
        ManageViolationsVC *vc = [[ManageViolationsVC alloc] initWithTitle:[@"Points or Approval" localize]];
        vc.travelPointsInBank = self.airShop.travelPointsInBank;
        vc.airSummary = self.airSummary;
        vc.violationTexts = [self getCurrentFareViolationMessages];
        vc.violationReasons = self.violationReasons;
        vc.violationReasonLabels = self.violationReasonLabels;
        [self.navigationController pushViewController:vc animated:YES];
    }
}

-(CGFloat) tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
    NSString *sectionName = aSections[section];
    if ([sectionName isEqualToString:@"TRIP_FIELDS"])
        return 50;
    else
        return 0;
}


#pragma mark - Fetched results controller
- (NSFetchedResultsController *)fetchedResultsController 
{
    if (__fetchedResultsController != nil) {
        return __fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityAirFilter" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"segmentPos" ascending:YES];
    NSSortDescriptor *sort2 = [[NSSortDescriptor alloc] initWithKey:@"flightPos" ascending:YES];

    [fetchRequest setSortDescriptors:@[sort, sort2]];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(fareId = %@)", airSummary.fareId];
    [fetchRequest setPredicate:pred];
    
    NSFetchedResultsController *theFetchedResultsController = 
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest 
                                        managedObjectContext:self.managedObjectContext sectionNameKeyPath:@"segmentPos" 
                                                   cacheName:@"Root"];
    self.fetchedResultsController = theFetchedResultsController;
    __fetchedResultsController.delegate = self;
    

    
    return __fetchedResultsController;    
    
}


#pragma mark - Fetched results controller delegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    [self hideWaitView];
    [self.tableList beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo
           atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
{
    switch(type)
    {
        case NSFetchedResultsChangeInsert:
            [self.tableList insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [self.tableList deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject
       atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type
      newIndexPath:(NSIndexPath *)newIndexPath
{
    UITableView *tableView = self.tableList;
    
    switch(type)
    {
            
        case NSFetchedResultsChangeInsert:
            [tableView insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeUpdate:
            [self configureCell:(AirShopFilteredCell*)[self.tableList cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
            break;
            
        case NSFetchedResultsChangeMove:
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [tableView insertRowsAtIndexPaths:@[newIndexPath]withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableList endUpdates];
}

#pragma mark - reset and then fetch the managed results
-(void) refetchData
{
    self.fetchedResultsController = nil;
    NSError *error;
	if (![[self fetchedResultsController] performFetch:&error]) {
		// Update to handle the error appropriately.
		NSLog(@"Unresolved error viewDidLoad fetching %@, %@", error, [error userInfo]);

        if ([Config isDevBuild]) {
            exit(-1);  // Fail
        } else {
            // be more graceful when dying abort();
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"airShopResults::viewDidLoad: fetchedResultsController %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
        }
	}
}

#pragma mark - Cell Config
- (void)configureCell:(AirShopFilteredCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    NSString *sectionName = aSections[indexPath.section];
    NSArray *a = dictSections[sectionName];
    EntityAirFilter *entity = (EntityAirFilter *)a[indexPath.row];
    
    if([entity.segmentPos intValue] == kSectionCC)
    {//Credit Card & Total
        NSString *crnCode = entity.crnCode;
        if(![crnCode length])
            crnCode = @"USD";
        SettingsBaseCell *cellBase = (SettingsBaseCell*)cell;
        if([entity.flightPos intValue] == 998)
        {
            cellBase.lblSubheading.text = @"Total Price";
            cellBase.lblHeading.text = [FormatUtils formatMoney:[entity.fare stringValue] crnCode:crnCode];
        }
        else
        {
            cellBase.lblSubheading.text = @"Card";
            cellBase.lblHeading.text = entity.bic;
        }
    }
    else if ([entity.carrier isEqualToString: @"LAYOVER"])
    {
        cell.lblAirline.text = entity.carrier;
        UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:airShopResults.airline VendorType:@"a_small" RespondToIV:cell.ivLogo];
        if(gotImg != nil)
            cell.ivLogo.image = gotImg;
        
        cell.lblDepartIata.text = entity.startIata;

        int flightMinutes = [entity.elapsedTime intValue];
        int flightHours = flightMinutes / 60;
        if (flightHours > 0) 
            flightMinutes = flightMinutes - (flightHours * 60);
        NSString *dur = [NSString stringWithFormat:@"%dh %dm", flightHours, flightMinutes];
        
        cell.lblDepartTime.text = dur;

    }
    else
    {
        NSString *airlineCode = entity.carrier;
        NSString *airlineName = (airShop.vendors)[airlineCode];
        cell.lblAirline.text = [NSString stringWithFormat:@"%@ %@", airlineName, entity.flightNum];
        UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:airlineCode VendorType:@"a_small" RespondToIV:cell.ivLogo];
        if(gotImg != nil)
            cell.ivLogo.image = gotImg;
        
        cell.lblDepartIata.text = entity.startIata;
        cell.lblArriveIata.text = entity.endIata;
        cell.lblDepartTime.text = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:entity.departureTime];
        cell.lblArriveTime.text = [DateTimeFormatter formatDateTimeEEEhmmaaByDate:entity.arrivalTime];
        
        NSString *departDOW = [DateTimeFormatter formatDateEEEByDate:entity.departureTime];
        NSString *arriveDOW = [DateTimeFormatter formatDateEEEByDate:entity.arrivalTime];
        if([departDOW isEqualToString:arriveDOW])
            cell.ivOvernight.image = nil;
        else
            cell.ivOvernight.image = [UIImage imageNamed:@"overnight_flight"];
        
        // Default duration to a blank string
        NSString *dur = @"";
        int flightMinutes = [entity.flightTime intValue];
        if (flightMinutes > 0)
        {
            // Only format a new duration string if there will be a duration to show
            // This was added as we had a problem with no flight duration being provided by MWS
            // leading to us showing 0h 0m
            int flightHours = flightMinutes / 60;
            if (flightHours > 0) 
                flightMinutes = flightMinutes - (flightHours * 60);
            dur = [NSString stringWithFormat:@"%dh %dm / ", flightHours, flightMinutes];
        }
        NSString *flightClassSeat = [NSString stringWithFormat:@"%@ (%@)", dictClass[entity.fltClass], entity.bic];
       
        cell.lblDurationStops.text = [NSString stringWithFormat:@"%@Stops: %@ / %@", dur, [entity.numStops stringValue], flightClassSeat];
        
        if(entity.operatingCarrier == nil)
        {
            cell.lblOperatedBy.text = @"";
            cell.viewDetails.frame = CGRectMake(0, 28,  cell.viewDetails.frame.size.width, 44);
        }
        else
        {
            NSString *opByName = (airShop.vendors)[entity.operatingCarrier];
            if(opByName == nil)
                opByName = entity.operatingCarrier;
            cell.lblOperatedBy.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"Operated by"], opByName];
            cell.viewDetails.frame = CGRectMake(0, 42, cell.viewDetails.frame.size.width, 44);
        }
    }
}

#pragma mark - Violations methods
-(BOOL) hasEnforcementLevel:(int)level
{
    NSNumber *enforcementLevel =@(level);
    
    NSArray *violations = [[AirViolationManager sharedInstance] fetchByFareId:airSummary.fareId];
    
	for (EntityAirViolation* violation in violations)
	{
        if ([violation.enforcementLevel isEqualToNumber:enforcementLevel]) {
            return TRUE;
        }
	}
    
    return FALSE;
}

// Checks if any Violations have enforcementLevel != kViolationAllow
-(BOOL) hasDisallowedViolations
{
    if ([airSummary maxEnforcementLevel] != nil)
    {
        if ([[airSummary maxEnforcementLevel] intValue] > kViolationExcludeFromLLF)
        {
            return YES;
        }
    }
    return NO;
}

-(NSUInteger)getViolationsCount
{
	return [[[AirViolationManager sharedInstance] fetchByFareId:airSummary.fareId] count];
}

-(NSString*)getViolationsByFareId:(NSString *)fareId
{
    NSArray *violations = [self getCurrentFareViolationMessages];
    return [violations componentsJoinedByString:@"\n"];
}

-(NSArray*)getCurrentFareViolationMessages
{
    NSArray *aviolations = [[AirViolationManager sharedInstance] fetchByFareId:airSummary.fareId];
    return [aviolations valueForKeyPath:@"@distinctUnionOfObjects.message"];
}


-(NSString*)getViolationJustification
{
    return airSummary.violationJustification;
}

-(NSString*)getViolationReason
{
    return airSummary.relAirViolationCurrent.message;
//    NSString *reason = nil;
//    
//	if (violationReasonCode != nil)
//	{
//    
//        TravelViolationReasons *travelViolationReasons = [TravelViolationReasons getSingleton];
//        if (travelViolationReasons != nil)
//        {
//            ViolationReason *violationReason = (travelViolationReasons.violationReasons)[violationReasonCode];
//            reason = violationReason.description;
//        }
//        
//        /*
//		SystemConfig *systemConfig = [SystemConfig getSingleton];
//		if (systemConfig != nil)
//		{
//			ViolationReason *violationReason = [systemConfig.airViolationReasons objectForKey:violationReasonCode];
//			reason = violationReason.description;
//		}
//         */
//	}
//	
//	return reason;
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


#pragma mark - Reserver
-(BOOL) isTicketRefundable
{
    return [self.airSummary.refundable boolValue] == TRUE;
}

-(BOOL) areAllPreSellOptionsRequiredFieldsFilled
{
    if (self.preSellOptions.isCreditCardCvvRequired && [self.creditCardCvvNumber length] == 0)
        return NO;
    
    for (PreSellCustomField *cf in self.preSellOptions.optionItems)
        if (!cf.isOptional && cf.userInputValue == nil)
            return NO;
    
    return YES;
}

-(void) showReserveAlert
{
    NSString *vJustification = [self getViolationJustification];
    if ([self hasDisallowedViolations] && ![self.airSummary.isUsingPointsAgainstViolations boolValue] && ([self getViolationReason] == nil || (![vJustification length] && [SystemConfig getSingleton].ruleViolationExplanationRequired) ))
    {
        NSString *msg = nil;
        
        if ([self.airSummary.canUseTravelPoints boolValue] && !self.airSummary.isUsingPointsAgainstViolations)
        {
            msg = [@"Before reserving this flight, please complete the fields shown in red." localize];
        }
        else if ([self getViolationReason] == nil)
        {
            //MOB-10484 : check if justification is required
            if (![vJustification length] && [SystemConfig getSingleton].ruleViolationExplanationRequired)
            {
                msg = [@"AIR_WARNING_REASON_JUSTIFICATION" localize];
            }
            else
            {
                msg = [@"AIR_WARNING_REASON" localize];
            }
        }
        else if([SystemConfig getSingleton].ruleViolationExplanationRequired)
        {
            msg = [@"AIR_WARNING_JUSTIFICATION" localize];
        }
        
        if(msg!=nil)
        {
        	MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"HOTEL_BOOKING_VIEW_MISSING_VIOLATION_INFO_TITLE" localize] message:msg delegate:self cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles:nil];
        	alert.tag = kAlertUnhandledViolations;
        	[alert show];
        }
        return;
    }
    else if (![self areAllPreSellOptionsRequiredFieldsFilled])
    {
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"HOTEL_BOOKING_VIEW_MISSING_VIOLATION_INFO_TITLE" localize] message:[@"Before reserving this flight, please complete the fields shown in red." localize] delegate:self cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles:nil];
        alert.tag = kAlertUnhandledViolations;
        [alert show];
        return;
    }
    
    MobileAlertView *av = nil;
    
    if ([self.affinityPrograms count] == 0 || self.chosenFrequentFlyer != nil)
    {
        BOOL showNonRefundableMsg = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"ShowNonrefundableMessage" withType:@"mobile"]];
        
        if ([self.airSummary.isInstantPurchase boolValue]) // Instant Purchase alert takes precedence over others
        {
            av = [[MobileAlertView alloc] initWithTitle:[@"Please Confirm" localize] message:[@"The selected credit card will be charged and the ticket will be issued immediately. The reservation may not be eligible for changes or refunds. Are you sure you want to reserve this flight?" localize] delegate:self cancelButtonTitle:[@"Cancel" localize] otherButtonTitles:[@"OK" localize], nil];
        }
        else if ([self isTicketRefundable] || !showNonRefundableMsg) // Display confirmation alert for refundable tickets and nonrefundable tickets with no alert set by admin
        {
            if ([Config isGov])
            {
                av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Please Confirm"] message:[Localizer getLocalizedText:@"Select 'OK' if you are sure you want to reserve this flight."] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Cancel"] otherButtonTitles:[Localizer getLocalizedText:@"OK"], nil];
            }
            else
            {
                av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Please Confirm"] message:[Localizer getLocalizedText:@"Are you sure you want to reserve this flight?"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Cancel"] otherButtonTitles:[Localizer getLocalizedText:@"OK"], nil];
            }
        }
        else // Display non-refundable alert if set by admin
        {
            av =  [[MobileAlertView alloc] initWithTitle:nil message:[Localizer getLocalizedText:@"NON_REFUNDABLE_WARN_MSG"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Cancel"] otherButtonTitles:[Localizer getLocalizedText:@"OK"], nil];
        }
    }
    else
    {
        av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Frequent Flyer Warning"] message:[Localizer getLocalizedText:@"FREQUENT_FLYER_NO_SEL_MSG"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN] otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
        av.tag = kAlertTagFrequentFlyerSoftStop;
    }
    [av show];
    
}

-(void) showSecondReserveAlert:(UIAlertView*) alert
{
    if(alert != nil)
        [alert show];
}

-(void) alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == kAlertTagFrequentFlyerSoftStop && buttonIndex ==1 && (![self isTicketRefundable] || [self.airSummary.isInstantPurchase boolValue]))
    {
        NSString *displayMessage = [self.airSummary.isInstantPurchase boolValue] ?
                                        [@"The selected credit card will be charged and the ticket will be issued immediately. The reservation may not be eligible for changes or refunds. Are you sure you want to reserve this flight?" localize] :
                                        [Localizer getLocalizedText:@"NON_REFUNDABLE_WARN_MSG"];
        MobileAlertView *av =  [[MobileAlertView alloc] initWithTitle:nil message:displayMessage delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"No"] otherButtonTitles:[Localizer getLocalizedText:@"Yes"], nil];
        // MOB-11383 need delay before show second alert view
        [self performSelector:@selector(showSecondReserveAlert:) withObject:av afterDelay:0.5f];
        //[av show];
    }
    else if (alertView.tag == kAlertGropuAuthUsed && buttonIndex == alertView.cancelButtonIndex)
    {
        // Refresh trips data.
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
    else if(buttonIndex == 1)
        [self reserveFlight];
    else
        return;
}

-(void)reserveFlight
{
    if(chosenCreditCard == nil && chosenCreditCard.ccId != nil)
	{
		MobileAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"No Credit Card"]
                                  message:[Localizer getLocalizedText:@"You need to select a valid credit card in order to reserve your train"]
                                  delegate:nil
                                  cancelButtonTitle:nil
                                  otherButtonTitles:[Localizer getLocalizedText:@"OK"], nil];
        
        // MOB-11382 need delay before show second alert view
        [self performSelector:@selector(showSecondReserveAlert:) withObject:alert afterDelay:0.5f];
		//[alert show];
		return;
	}
    
    if (isDirty && editedDependentCustomField)
    {
        // MOB-9648 Prevent preserve button, in between child screen and wait view
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[@"PENDING_REQUIRED_TRAVEL_CUSTOM_FIELDS" localize] delegate:nil cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles:nil];
        // MOB-11382 need delay before show second alert view
        [self performSelector:@selector(showSecondReserveAlert:) withObject:alert afterDelay:0.5f];
        //[alert show];
        
        return;
    }
    
    if ([self hasPendingRequiredTripFields])
    {
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[@"PENDING_REQUIRED_TRAVEL_CUSTOM_FIELDS" localize] delegate:nil cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles:nil];
        // MOB-11382 need delay before show second alert view
        [self performSelector:@selector(showSecondReserveAlert:) withObject:alert afterDelay:0.5f];
        //[alert show];
        
        return;
    }
    
    [self completeReservation];
}

-(void)completeReservation
{
    [self performSelector:@selector(showHideWait:) withObject:@"Y" afterDelay:0.01f];
	
    NSString *itinName = [NSString stringWithFormat:@"%@ %@ %@ %@", [Localizer getLocalizedText:@"Trip just trip"], airSummary.departureIata, [Localizer getLocalizedText:@"to"], airSummary.arrivalIata];
    
    NSString *customFields =  [TravelCustomFieldsManager makeCustomFieldsRequestXMLBody];
    
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"TRAIN_DELIVERY_VIEW", @"TO_VIEW"
								 , self.airSummary.fareId, @"FARE_ID"
								 ,itinName, @"TRIP_NAME"
								 ,chosenCreditCard.ccId, @"CREDIT_CARD_ID"
								 , nil];
    
    if (airSummary.violationJustification)
        pBag[@"VIOLATION_JUSTIFICATION"] = airSummary.violationJustification;
    if (airSummary.relAirViolationCurrent.code) 
        pBag[@"VIOLATION_CODE"] = airSummary.relAirViolationCurrent.code;
    if ([airSummary.isUsingPointsAgainstViolations boolValue])
        pBag[@"REDEEM_POINTS"] = @(YES);
	
    if (customFields != nil)
        pBag[@"TRAVEL_CUSTOM_FIELDS"] = customFields;
        
    if (self.chosenFrequentFlyer != nil)
        pBag[@"PROGRAM_ID"] = self.chosenFrequentFlyer.programId;
    
    if (self.preSellOptions.isCreditCardCvvRequired && [self.creditCardCvvNumber lengthIgnoreWhitespace])
        pBag[@"CREDIT_CARD_CVV"] = self.creditCardCvvNumber;
    if ([self.preSellOptions.optionItems count])
        [self addFlightOptionsXMLTo:pBag];
    
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
                {
                    [pBag setObject:currentTAField.perDiemLocState forKey:@"GOV_PER_DIEM_COUNTRY"];
                }
                [pBag setObject:currentTAField.perDiemLocation forKey:@"PER_DIEM_LOCATION"];
                if (currentTAField.perDiemLocZip != nil)
                    [pBag setObject:currentTAField.perDiemLocZip forKey:@"GOV_PER_DIEM_LOC_ZIP"];
            }
        }
    }

    NSDictionary *dict = @{@"Type": @"Air"};
    [Flurry logEvent:@"Book: Reserve" withParameters:dict];
    [self logFlurryEventsForTravelPoints];
    
	[[ExSystem sharedInstance].msgControl createMsg:AIR_SELL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES Options:NO_RETRY RespondTo:self];
}

- (void)logFlurryEventsForTravelPoints
{
    if([[UserConfig getSingleton].travelPointsConfig[@"AirTravelPointsEnabled"] boolValue]) // Log only if earning points is enabled
    {
        NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
        if ([ExSystem sharedInstance].entitySettings.companyName)
            dict[@"User Company Name"] = [ExSystem sharedInstance].entitySettings.companyName;
        dict[@"Selected Positive Points"] = [self.airSummary.travelPoints intValue] == 0 ? @"NA" : ([self.airSummary.travelPoints intValue] > 0 ? @"YES" : @"NO");
        if (self.airShop.travelPointsInBank)
            dict[@"Travel Points In Bank"] = self.airShop.travelPointsInBank;
        if ([self.airSummary.travelPoints intValue] > 0)
            dict[@"Travel Points Earned"] = self.airSummary.travelPoints;
        if ([self.airSummary.isUsingPointsAgainstViolations boolValue])
            dict[@"Travel Points Used"] = @(-[self.airSummary.travelPoints intValue]);
        if (self.airSummary.isUsingPointsAgainstViolations)
            dict[@"Use Travel Points Selected"] = [self.airSummary.isUsingPointsAgainstViolations boolValue] ? @"YES" : @"NO";
        [Flurry logEvent:@"Price-to-Beat: Air Reserve" withParameters:dict];
    }
}

-(void) addFlightOptionsXMLTo:(NSMutableDictionary *)pBag
{
//    <FlightOptionsSelected>
//        <FlightOption>
//            <Id>BE|BE383|DUB|SOU|20/11/2013-08:35|20/11/2013-10:00|NumberOfBags|NumberOfBags</Id>
//            <Value>2</Value>
//        </FlightOption>
//    </FlightOptionsSelected>
    NSMutableString *optionXml = [[NSMutableString alloc] init];
    for (PreSellCustomField *cf in self.preSellOptions.optionItems)
        if ([cf.userInputValue lengthIgnoreWhitespace])
            [optionXml appendFormat:@"<FlightOption><Id>%@</Id><Value>%@</Value></FlightOption>",[NSString stringByEncodingXmlEntities:cf.itemId],[NSString stringByEncodingXmlEntities:cf.userInputValue]];
    
    if ([optionXml length])
        pBag[@"FLIGHT_OPTIONS"] = [NSString stringWithFormat:@"<FlightOptionsSelected>%@</FlightOptionsSelected>",optionXml];
    pBag[@"HAS_FLIGHT_OPTIONS"] = @"YES";
}

-(void)showHideWait:(NSString *)sShow 
{
	BOOL isShow = NO;
	if([sShow isEqualToString:@"Y"])
		isShow = YES;
	
	if(isShow)
	{
		[self showWaitViewWithText:[Localizer getLocalizedText:@"Reserving Flights"]];        
		NSArray *toolbarItems = @[];
		[self setToolbarItems:toolbarItems animated:YES];
	}
	else 
	{
		[self hideWaitView];
		[self makeReserveButton:self];
	}
}

#pragma mark - frequent flyer
-(void) showAffinityPrograms:(id)sender
{
    OptionsSelectVC *nextController = [[OptionsSelectVC alloc] initWithNibName:@"EditFormView" bundle:nil];
    nextController.optionTitle = [Localizer getLocalizedText:@"Select Program"];
    nextController.items = self.affinityPrograms;
    NSMutableArray* labels = [[NSMutableArray alloc] init];
    nextController.selectedRowIndex = -1;// None selected
    int idx = 0;
    for (AffinityProgram * ap in self.affinityPrograms)
    {
        [labels addObject:ap.description];
        if (self.chosenFrequentFlyer != nil && [ap.description isEqualToString:self.chosenFrequentFlyer.description])
        {
            nextController.selectedRowIndex = idx;
        }
        idx++;
    }
    nextController.labels = labels;
    nextController.identifier = kFieldIdFrequentFlyer;
    nextController.delegate = self;
	[self.navigationController pushViewController:nextController animated:YES];
    

}
#pragma mark - cards
-(void) showCards:(id)sender
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"AIR_BOOKING", @"FROM_VIEW", @"YES", @"SHORT_CIRCUIT", self.creditCards, @"CREDIT_CARDS", nil];
	
	if (chosenCardIndex > -1)
		[pBag setValue:@(chosenCardIndex) forKey:@"CREDIT_CARD_INDEX"];
    
	HotelCreditCardViewController *nextController = [[HotelCreditCardViewController alloc] initWithNibName:@"HotelCreditCardViewController" bundle:nil];
	nextController.airShopFilteredResultsVC = self;

	Msg *msg = [[Msg alloc] init];
	msg.parameterBag = pBag;
	msg.idKey = @"SHORT_CIRCUIT";
	[nextController respondToFoundData:msg];
	[self.navigationController pushViewController:nextController animated:YES];
    nextController.title = [Localizer getLocalizedText:@"Select Card"];
    
}

-(void) showTextEditorWithTitle:(NSString *)title textValue:(NSString*)text indexPath:(NSIndexPath *)indexPath placeholderText:(NSString *)placeholder
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"FROM_VIEW",  @"YES", @"SHORT_CIRCUIT", nil];
    if ([text length])
        pBag[@"TEXT"] = text;
    HotelTextEditorViewController *nextController = [[HotelTextEditorViewController alloc] initWithNibName:@"HotelTextEditorViewController" bundle:nil];
    [self.navigationController pushViewController:nextController animated:YES];
    Msg *msg = [[Msg alloc] init];
    msg.parameterBag = pBag;
    msg.idKey = @"SHORT_CIRCUIT";
    [nextController respondToFoundData:msg];
    nextController.placeholderText = placeholder;
    nextController.title = title;
    nextController.parentVC = self;
    nextController.fromIndexPath = indexPath;
}

-(void) setCreditCardCvvNumber:(NSString *)creditCardCvvNumber
{
    _creditCardCvvNumber = [creditCardCvvNumber lengthIgnoreWhitespace] ? creditCardCvvNumber : nil;
    EntityAirFilter *af = (EntityAirFilter*)[[AirFilterManager sharedInstance] fetchByFareIdSegmentPosFlightPos:airSummary.fareId segPos:kSectionCC flightPos:kCreditCardCvv];
    af.bic = [creditCardCvvNumber lengthIgnoreWhitespace] ? creditCardCvvNumber : @"Please specify";
}

-(void)chooseCard:(int)cardIndex
{
	if(cardIndex < 0)
	{
        [aButtons removeObjectAtIndex:1];
        [aButtons insertObject:(self.isPreSellOptionsLoaded ? [Localizer getLocalizedText:@"Unavailable"] : [@"Loading..." localize]) atIndex:1];
	}
	else
    {
        CreditCard *creditCard = [self.creditCards objectAtIndex:cardIndex];
        chosenCardIndex = cardIndex;
        self.chosenCreditCard = creditCard;
        
        // MOB-13173 cut down the card num to fit the field space by only showing two "*"
        NSString *cardNum = self.chosenCreditCard.maskedNumber;
        NSString *trimedCardNum = nil;
        if (cardNum != nil && [cardNum length] >= 6)
            trimedCardNum = [cardNum substringFromIndex:[cardNum length] - 6];
        else
            trimedCardNum = cardNum;
        
        [aButtons removeObjectAtIndex:1];
        [aButtons insertObject:[ NSString stringWithFormat:@"%@ %@", chosenCreditCard.name, trimedCardNum] atIndex:1];
        
        [self makeReserveButton:self];
    }
    EntityAirFilter *af = (EntityAirFilter*)[[AirFilterManager sharedInstance] fetchByFareIdSegmentPosFlightPos:airSummary.fareId segPos:kSectionCC flightPos:999];
    af.bic = aButtons[1];
    
    [tableList reloadData];
}

-(void)chooseFirstCard
{
    if ([self.creditCards count] < 1)
	{
        [aButtons removeObjectAtIndex:1];
        [aButtons insertObject:(self.isPreSellOptionsLoaded ? [Localizer getLocalizedText:@"Unavailable"] : [@"Loading..." localize]) atIndex:1];
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
        
        // MOB-13173 cut down the card num to fit the field space by only showing two "*"
        NSString *trimedCardNum = nil;
		if(cardName == nil)
			cardName = @"";
		
		if(cardMask == nil)
			cardMask = @"";
        else if ([cardMask length] >= 6)
            trimedCardNum = [cardMask substringFromIndex:[cardMask length] - 6];
        else
            trimedCardNum = cardMask;
		
        [aButtons removeObjectAtIndex:1];
        [aButtons insertObject:[ NSString stringWithFormat:@"%@ %@", cardName, trimedCardNum] atIndex:1];
		[self makeReserveButton:self];
	}
    
}


#pragma mark - Reserve Button
-(void) makeReserveButton:(id)sender
{	
	if(chosenCardIndex <= -1)
    {
        self.navigationItem.rightBarButtonItem = nil;
		return;
    }
    
    UIBarButtonItem *btnReserve = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Reserve"] style:UIBarButtonItemStyleBordered target:self action:@selector(showReserveAlert)];
    self.navigationItem.rightBarButtonItem = btnReserve;
    
    if ([self hasEnforcementLevel:kViolationAutoFail]) {
        [self.navigationItem.rightBarButtonItem setEnabled:NO];
    }
}

#pragma mark - Class Stuff
-(void) fillClass
{
    self.aClass = [[NSMutableArray alloc] initWithObjects:@"Y", @"W", @"C", @"F", @"ANY", nil];
    self.dictClass = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[Localizer getLocalizedText:@"Economy"], @"Y", [Localizer getLocalizedText:@"Premium Economy"], @"W",[Localizer getLocalizedText:@"Business"], @"C", [Localizer getLocalizedText:@"First"], @"F", [Localizer getLocalizedText:@"Any"], @"ANY", nil];
}

#pragma mark - Frequent Flyer program select
-(void) optionSelected:(NSObject*)obj withIdentifier:(NSObject*) identifier
{
    if ([kFieldIdFrequentFlyer isEqualToString:(NSString*)identifier])
    {
        self.chosenFrequentFlyer = (AffinityProgram*)obj;
        EntityAirFilter *af = (EntityAirFilter*)[[AirFilterManager sharedInstance] fetchByFareIdSegmentPosFlightPos:airSummary.fareId segPos:kSectionCC flightPos:kFlightPosAffinity];
        af.bic = self.chosenFrequentFlyer.description;

        [self.tableList reloadData];
    }
}

-(void) optionSelectedAtIndex:(NSInteger)row withIdentifier:(NSObject*) identifier
{
    // Not in use
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


#pragma mark - Custom Fields
-(void) reloadCustomFieldsSection
{
    int sectionIndex = 0;
    for(int i = 0; i < [aSections count]; i++)
    {
        if([aSections[i] isEqualToString:@"TRIP_FIELDS"])
        {
            sectionIndex = i;
            break;
        }
    }
    NSIndexSet *indexSet = [NSIndexSet indexSetWithIndex:sectionIndex];
    
    [tableList reloadSections:indexSet withRowAnimation:UITableViewRowAnimationFade];
}

-(void) refreshFlightOptionsSection
{
    int sectionIndex = 0;
    NSString *sectionNameToMatch = [NSString stringWithFormat:@"%d",kSectionPreSellFlightOptions];
    for(int i = 0; i < [aSections count]; i++)
    {
        if([aSections[i] isEqualToString:sectionNameToMatch])
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
        [self showWaitView];
    }
    
    NSString *customFields =  [TravelCustomFieldsManager makeCustomFieldsRequestXMLBody];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"SKIP_CACHE", @"YES", customFields, @"UPDATED_CUSTOM_FIELDS", nil]; 
    [[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_TRAVEL_CUSTOMFIELDS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void)fetchPreSellOptions // Note: Pre-sell for car only contains CC details, and is only called when car.sendCreditCard is YES -- Change this when car has loyalty programs data
{
    NSMutableDictionary *paramBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING"
                                     , self.airSummary.choiceId , @"CHOICE_ID",  nil];
	
	[[ExSystem sharedInstance].msgControl createMsg:PRE_SELL_OPTIONS CacheOnly:@"NO" ParameterBag:paramBag SkipCache:YES Options:SILENT_ERROR RespondTo:self];
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
        HotelBookingCell *cell = (HotelBookingCell*)[tableList dequeueReusableCellWithIdentifier:@"HotelBookingSingleCell"];
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

-(UITableViewCell *)configureFlightOptionCellAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *sectionName = aSections[indexPath.section];
    NSArray *a = dictSections[sectionName];
    PreSellCustomField *cf = (PreSellCustomField *)a[indexPath.row];
    HotelBookingCell *cell = (HotelBookingCell*)[tableList dequeueReusableCellWithIdentifier:@"HotelBookingSingleCell"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelBookingSingleCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[HotelBookingCell class]])
                cell = (HotelBookingCell *)oneObject;
    }
    
    NSString *lblText = nil;
    
    if (cf.userInputValue == nil)
    {
        if (!cf.isOptional)
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
    
    cell.lblLabel.text = cf.title;
    cell.lblValue.text = ([cf.attributeValues count] && [cf.userInputValueDisplayText length]) ? cf.userInputValueDisplayText : ( (cf.userInputValue != nil)? cf.userInputValue : lblText );
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    
    return cell;
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

-(void) showFlightOptionCellEditorAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *sectionName = aSections[indexPath.section];
    NSArray *a = dictSections[sectionName];
    PreSellCustomField *cf = (PreSellCustomField *)a[indexPath.row];
    if ([cf.attributeValues count]) {
        PreSellCustomFieldSelectVC *nextController = [[PreSellCustomFieldSelectVC alloc] initWithNibName:@"HotelOptionsViewController" bundle:nil];
        nextController.title = [@"Please specify" localize];
        nextController.tcf = cf;
        [self.navigationController pushViewController:nextController animated:YES];
        self.reloadFlightOptionsSection = YES;
    }
    else
        [self showTextEditorWithTitle:cf.title textValue:cf.userInputValue indexPath:indexPath placeholderText:[@"Please specify" localize]];
}

-(BOOL) hasPendingRequiredTripFields
{
    return [[TravelCustomFieldsManager sharedInstance] hasPendingRequiredTripFields];
    return FALSE;
}
@end
