//
//  AirBookingCriteriaVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/4/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "AirBookingCriteriaVC.h"
#import "BookingCellData.h"
#import "TrainBookingCell.h"
#import "TrainStationsVC.h"
#import "ConcurMobileAppDelegate.h"
#import "TrainTimeVC.h"
#import "TrainDateVC.h"
#import "TrainPassengersVC.h"
#import "TrainBookListingVC.h"
#import "TrainSearchingVC.h"
#import "TrainTimeTablesFetchData.h"
#import "TrainStopsVC.h"
#import "RailChoiceSegmentData.h"
#import "RailChoiceTrainData.h"
#import "iPadHomeVC.h"
#import "MobileAlertView.h"
#import "TrainGroupedListVC.h"
#import "HotelLocationViewController.h"
#import "LocationResult.h"
#import "ClassOfServiceVC.h"
#import "UserConfig.h"
#import "CreditCard.h"
#import "BoolEditCell.h"
#import "DateTimeOneVC.h"
#import "SystemConfig.h"
#import "DownloadSystemConfig.h"
#import "DownloadTravelCustomFields.h"
#import "CustomFieldTextEditor.h"
#import "FieldOptionsViewController.h"
#import "HotelBookingCell.h"
#import "TravelViolationReasons.h"
#import "Config.h"
#import "PostMsgInfo.h"

#import "GovTAField.h"
#import "GovDutyLocationVC.h"
#import "GovSelectTANumVC.h"
#import "GovPerDiemRateData.h"
#import "GovAirShopResultsVC.h"

#import "NSArray+Additions.h"

#import "EvaVoiceSearchViewController.h"
#import "Fusion14FlightSearchResultsViewController.h"


#define KSECTION_GOV_TA_FIELDS @"GOV_TA_FIELDS"
#define KSECTION_TRIP_CUSTOM_FIELDS   @"TRIP_FIELDS"

#define FROM_LOCATION_TAG @"0"
#define RETURN_LOCATION_TAG @"1"

#define ECONOMY @"Economy"
#define FIRST_CLASS @"First"
#define BUSINESS_CLASS @"Business"
#define PREMIUM_ECONOMY @"PremiumEconomy"
#define ONE_CLASS_UPGRADE @"OneClassUpgrade"

@interface AirBookingCriteriaVC ()
-(void) reloadCustomFieldsSection;
-(void) reloadBookingCriteriaSection;
-(void) updateDynamicCustomFields;
-(void) fetchCustomFields;

@property BOOL shouldDisableSearchButton;
@property (nonatomic, strong) NSString *lastSearchUuid;

@end

@implementation AirBookingCriteriaVC

@synthesize tableList, aList, aSections, isRoundTrip, shouldReload, trainBooking, isCancelled, viewSegmentHeader, segmentTripDirection, returnDateBCD;
@synthesize pickerPopOverVC, viewSearching, lblSearchTo, lblSearchFrom, lblSearchTitle, aClass, dictClass; 
@synthesize tcfRows, isDirty, hideCustomFields, editedDependentCustomField, selectedCustomField, checkboxDefault, showCheckbox, displayedRefundableInfo, displayedClassOfService;
@synthesize taFields;

-(NSDate*) getCurrentDateWithoutTimeInGMT
{
    return [DateTimeFormatter getDateWithoutTimeInGMT:[DateTimeFormatter getCurrentLocalDateTimeInGMT]];
}

-(NSMutableArray *) groupTrains:(NSMutableArray *) allTrains
{
	NSMutableArray *trains = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableDictionary *trainKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	for(RailChoiceData *rcd in allTrains)
	{
		
		RailChoiceSegmentData *segmentDepart = (rcd.segments)[0];
		RailChoiceSegmentData *segmentReturn = (rcd.segments)[[rcd.segments count] -1];
		RailChoiceTrainData *trainDepart = (segmentDepart.trains)[0];
		RailChoiceTrainData *trainReturn = (segmentReturn.trains)[0];
		
		//rail choice: baseFare, cost, currencyCode, descript, imageUri
		//train: carrier, fltNum, depDateTime, arrDateTime, aircraftCode, meals, bic, fltClass, depAirp, arrAirp
		//key is dep.DateTime dep.FltNum dep.Airp return.dateTime return.FltNum return.dateTime
		//then i can add in different rail choices to get the cost and class of seats
		NSString *key = [NSString stringWithFormat:@"%@_%@_%@_%@_%@_%@", trainDepart.depDateTime, trainDepart.fltNum, trainDepart.depAirp, trainReturn.depDateTime, trainReturn.fltNum, trainReturn.depAirp];
		
		RailChoiceData *railChoice = rcd;
		
		if(trainKeys[key] == nil)
		{
			//i have not found this particular segment
			railChoice.key = key;
			trainKeys[key] = railChoice;
		}
		else {
			railChoice = trainKeys[key];
		}
        
		[railChoice addSeat:rcd.baseFare Cost:rcd.cost CurrencyCode:rcd.currencyCode Description:rcd.descript];
	}
	
	for(NSString *key in trainKeys)
	{
		RailChoiceData *rcd = trainKeys[key];
		[trains addObject:rcd];
	}
	NSSortDescriptor *aSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"key" ascending:YES];
	[trains sortUsingDescriptors:@[aSortDescriptor]];
	
	return trains;
}

-(void) dumpResultsIntoEntity:(AirShop*) airShop
{
    [[AirShopResultsManager sharedInstance] deleteAll];
    int iTotalCount = 0;            // total results in all types of flight
    double dLowest = 99999999999;   // lowest cost in all types of flight
    NSString *lastCrnCode = @"USD";
    
    if ([Config isGov]){
        for(NSString *key in airShop.rateTypeChoices)
        {
            int totalChoices = 0;   // total results in current rateType flight
            double lowest = 99999999999;// lowest cost in current rate Type choice
            NSArray *a = [airShop.rateTypeChoices objectForKey:key];
            for(AirlineEntry *ae in a)
            {
                EntityAirShopResults *airShopResults = (EntityAirShopResults*)[[AirShopResultsManager sharedInstance] makeNew];
                airShopResults.airline = ae.airline;
                airShopResults.airlineName = [airShop.vendors objectForKey:ae.airline];
                airShopResults.numChoices = ae.numChoices;
                totalChoices += [ae.numChoices intValue];
                iTotalCount += [ae.numChoices intValue];
                airShopResults.rateType = ae.rateType;
                airShopResults.lowestCost = ae.lowestCost;
                if(lowest > [ae.lowestCost doubleValue])
                    lowest = [ae.lowestCost doubleValue];
                
                if(dLowest > [ae.lowestCost doubleValue])
                    dLowest = [ae.lowestCost doubleValue];
                
                if(![ae.crnCode length])
                    airShopResults.crnCode = @"USD";
                else
                    airShopResults.crnCode = ae.crnCode;
                
                lastCrnCode = airShopResults.crnCode;
                
                airShopResults.pref = ae.pref;
                [[AirShopResultsManager sharedInstance] saveIt:airShopResults];
            }
            
            EntityAirShopResults *airShopResults = (EntityAirShopResults*)[[AirShopResultsManager sharedInstance] makeNew];
            airShopResults.airline = @"ZZZZZZZZTOTAL";
            airShopResults.airlineName = @"ZZZZZZZZTOTAL";
            airShopResults.numChoices = @(totalChoices);
            airShopResults.rateType = key;
            airShopResults.lowestCost = @(lowest);
            airShopResults.crnCode = lastCrnCode;
            airShopResults.pref = @"";
            [[AirShopResultsManager sharedInstance] saveIt:airShopResults];
        }
        
        if([airShop.rateTypeChoices count] > 0)
        {
            EntityAirShopResults *airShopResults = (EntityAirShopResults*)[[AirShopResultsManager sharedInstance] makeNew];
            airShopResults.airline = @"   TOTAL";
            airShopResults.airlineName = @"   TOTAL";
            airShopResults.numChoices = [NSNumber numberWithInt:iTotalCount];
            airShopResults.rateType = @"";
            airShopResults.lowestCost = [NSNumber numberWithDouble:dLowest];
            airShopResults.crnCode = lastCrnCode;
            airShopResults.pref = @"";
            [[AirShopResultsManager sharedInstance] saveIt:airShopResults];
        }
    }
    else{                  //Concur Mobile
        for(NSString *key in airShop.stopChoices)
        {
            int totalChoices = 0;
            double lowest = 99999999999;
            NSArray *a = (airShop.stopChoices)[key];
            for(AirlineEntry *ae in a)
            {
                EntityAirShopResults *airShopResults = (EntityAirShopResults*)[[AirShopResultsManager sharedInstance] makeNew];
                airShopResults.airline = ae.airline;
                airShopResults.airlineName = (airShop.vendors)[ae.airline];
                airShopResults.numChoices = ae.numChoices;
                totalChoices += [ae.numChoices intValue];
                iTotalCount += [ae.numChoices intValue];
                airShopResults.numStops = ae.numStops;
                airShopResults.lowestCost = ae.lowestCost;
                airShopResults.travelPoints = ae.travelPoints;
                if(lowest > [ae.lowestCost doubleValue])
                    lowest = [ae.lowestCost doubleValue];
                
                if(dLowest > [ae.lowestCost doubleValue])
                    dLowest = [ae.lowestCost doubleValue];
                
                if(![ae.crnCode length])
                    airShopResults.crnCode = @"USD";
                else
                    airShopResults.crnCode = ae.crnCode;
                
                lastCrnCode = airShopResults.crnCode;
                
                airShopResults.pref = ae.pref;
                [[AirShopResultsManager sharedInstance] saveIt:airShopResults];
            }
            
            EntityAirShopResults *airShopResults = (EntityAirShopResults*)[[AirShopResultsManager sharedInstance] makeNew];
            airShopResults.airline = @"ZZZZZZZZTOTAL";
            airShopResults.airlineName = @"ZZZZZZZZTOTAL";
            airShopResults.numChoices = @(totalChoices);
            airShopResults.numStops = @([key intValue]);
            airShopResults.lowestCost = @(lowest);
            airShopResults.crnCode = lastCrnCode;
            airShopResults.pref = @"";
            [[AirShopResultsManager sharedInstance] saveIt:airShopResults];
        }
        
        if([airShop.stopChoices count] > 0)
        {
            EntityAirShopResults *airShopResults = (EntityAirShopResults*)[[AirShopResultsManager sharedInstance] makeNew];
            airShopResults.airline = @"   TOTAL";
            airShopResults.airlineName = @"   TOTAL";
            airShopResults.numChoices = @(iTotalCount);
            airShopResults.numStops = @-1;
            airShopResults.lowestCost = @(dLowest);
            airShopResults.crnCode = lastCrnCode;
            airShopResults.pref = @"";
            [[AirShopResultsManager sharedInstance] saveIt:airShopResults];
        }
    }
}

-(void) makeRefundableCell
{
    BookingCellData *bcd = [[BookingCellData alloc]init];
    bcd.cellID = @"RefundableOnly";
    bcd.lbl = [Localizer getLocalizedText:@"Refundable Only"];
    bcd.val = (checkboxDefault)? @"Y" : @"N";
    bcd.isDisclosure = NO;
    [aList addObject:bcd];
}

#pragma mark -
#pragma mark MVC stuff
-(void)respondToFoundData:(Msg *)msg
{
    if (![self.navigationController.viewControllers containsObject:self]) { // if VC is unloaded from the Nav controllers
        return;
    }
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
    
    [viewSearching setHidden:YES];
    
	if ([msg.idKey isEqualToString:AIR_SHOP] && !isCancelled && [msg.parameterBag[@"SEARCH_UUID"] isEqualToString:self.lastSearchUuid])
	{
		AirShop *airShop = (AirShop*) msg.responder;
        [self dumpResultsIntoEntity:airShop];
        //MOB-16983	 New flight search screen used on Gov App.
        if ([Config isGov])
        {
            GovAirShopResultsVC *vc = [[UIStoryboard storyboardWithName:@"GovAirShopResultsVC" bundle:nil] instantiateInitialViewController];
            vc.taFields = self.taFields;
            vc.vendors = airShop.vendors;
            vc.airShop = airShop;
            [self.navigationController pushViewController:vc animated:YES];
        }
       else if ([Config isNewAirBooking]){
            Fusion14FlightSearchResultsViewController *fusionFlightSearchVC = [[UIStoryboard storyboardWithName:@"Fusion14FlightSearchResults_iPhone" bundle:nil] instantiateInitialViewController];
            fusionFlightSearchVC.vendors = airShop.vendors;
            fusionFlightSearchVC.airShop = airShop;
            fusionFlightSearchVC.shouldGetAllResults = YES;
           
            [self.navigationController pushViewController:fusionFlightSearchVC animated:YES];
        }
        else
        {
            AirShopResultsVC *vc = [[AirShopResultsVC alloc] initWithNibName:@"AirShopResultsVC" bundle:nil];
            vc.taFields = self.taFields;
            vc.vendors = airShop.vendors;
            vc.airShop = airShop;
            [self.navigationController pushViewController:vc animated:YES];
        }

		[self hideLoadingView];
		[self makeSearchButton];
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
    else if ([msg.idKey isEqualToString:DOWNLOAD_SYSTEM_CONFIG])
    {
        if ([self isViewLoaded])
            [self hideLoadingView];
        
        DownloadSystemConfig *sysConfig = (DownloadSystemConfig *)msg.responder;
        
        if (msg.errBody == nil && msg.responseCode == 200)
        {
            self.checkboxDefault = sysConfig.checkboxDefault;
            self.showCheckbox = sysConfig.showCheckbox;
            
            if (showCheckbox)
            {
                if (!displayedRefundableInfo) 
                {
                    [self makeRefundableCell];
                }
                
                self.displayedRefundableInfo = YES;
                [self loadBCDFromEntity];
                
                [self reloadBookingCriteriaSection];
            }
        }
    }
    else if ([msg.idKey isEqualToString:DOWNLOAD_USER_CONFIG])
    {
        if ([self isViewLoaded])
            [self hideLoadingView];
        
        if (msg.errBody == nil && msg.responseCode == 200)
        {
            if ([self fillClass:[UserConfig getSingleton].classOfServices] && !displayedClassOfService)
            {
                BookingCellData *bcd = [[BookingCellData alloc]init];
                bcd.cellID = @"ClassOfService";
                bcd.lbl = [Localizer getLocalizedText:@"Class of Service"];
                bcd.val = dictClass[@"Y"];
                bcd.val2 = @"Y";
                [aList addObject:bcd];
                
                self.displayedClassOfService = YES;
                
                [self loadBCDFromEntity];
                [self reloadBookingCriteriaSection];
            }
        }
    }
}


-(NSString *)getViewIDKey
{
	return TRAIN_BOOK;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

#pragma mark -
#pragma mark View Controller Metheads
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

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    if (isDirty) 
    {
        isDirty = NO;
        
        if (editedDependentCustomField && [selectedCustomField.attributeValue length])
            [self updateDynamicCustomFields];
        else if (!shouldReload)// Prevent multiple reloads of table view. Note: If you change shouldReload, please make sure that custom fields section is updated.
            [self reloadCustomFieldsSection];
    }
    else if (!hideCustomFields)
    {
        // MOB-9721 Refresh core data objects after coming back, since they may have been updated during data fetch.
        self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAllFieldsAtStart:YES];
        if (!shouldReload)
            [self reloadCustomFieldsSection];
    }
    
    if(shouldReload)
	{
		[tableList reloadData];
		shouldReload = NO;
	}
    [self.navigationItem setHidesBackButton:NO animated:YES];
    if(isCancelled)
        self.navigationController.toolbarHidden = YES;
    
    if ([SystemConfig getSingleton] == nil)
    {
        if ([self isViewLoaded])
            [self showLoadingView];
        
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: [self getViewIDKey], @"TO_VIEW", @"YES", @"SKIP_CACHE", nil];
        [[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_SYSTEM_CONFIG CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    
    if ([TravelViolationReasons getSingleton] == nil)
		[[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_TRAVEL_VIOLATIONREASONS CacheOnly:@"NO" ParameterBag:nil SkipCache:YES];

}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self.navigationController setToolbarHidden:YES];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [self.navigationController setToolbarHidden:YES];
    [super viewDidLoad];
    
    if ([UserConfig getSingleton] == nil)
    {
        if ([self isViewLoaded])
            [self showLoadingView];
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: [self getViewIDKey], @"TO_VIEW", @"YES", @"SKIP_CACHE", nil];
		[[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_USER_CONFIG CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    
    if ([Config isGov])
        self.aSections = [[NSMutableArray alloc] initWithObjects:KSECTION_GOV_TA_FIELDS, @"Everything", nil];
    else
        self.aSections = [[NSMutableArray alloc] initWithObjects:@"Everything", nil];
    
    self.shouldDisableSearchButton = false;
    if (!hideCustomFields)
    {
        self.shouldDisableSearchButton = true;
        
        [[TravelCustomFieldsManager sharedInstance] deleteAll]; // Reset for this search
        [self fetchCustomFields];

        // prepopulate the custom fields from cache
        [aSections addObject:KSECTION_TRIP_CUSTOM_FIELDS];
        // Start fresh, with no custom fields.
        self.tcfRows = nil;
    }
    else 
        [self hideLoadingView];
    
    
    [viewSearching setHidden:YES];
    
	isRoundTrip = YES;
	[self resetForRoundTrip];
    self.isCancelled = YES;
	
	
	[self initTableData];
	self.title = [Localizer getLocalizedText:@"Book Air"];
	trainBooking = [[TrainBooking alloc] init];
	[self makeSearchButton];
    
    [segmentTripDirection setTitle:[@"One Way" localize] forSegmentAtIndex:0];
    [segmentTripDirection setTitle:[@"Round Trip" localize] forSegmentAtIndex:1];
    
    [self loadBCDFromEntity];
	
	if([UIDevice isPad])
	{
		UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
		self.navigationItem.leftBarButtonItem = nil;
		self.navigationItem.leftBarButtonItem = btnCancel;
	}
	
    [self.viewSegmentHeader setHidden:YES];
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload 
{
	//self.navigationController.toolbar.hidden = NO;
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.viewSegmentHeader = nil;
    self.tableList = nil;
    self.segmentTripDirection = nil;
    self.lblSearchTo = nil;
    self.lblSearchTitle = nil;
    self.lblSearchFrom = nil;
}




#pragma mark - View Utility methods
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

-(void)makeSearchButton
{
    NSString *searchButtonTitle = [Localizer getLocalizedText:@"Search"];
    UIBarButtonItem *btnSearch = nil;
    if ([ExSystem is7Plus])
        btnSearch = [[UIBarButtonItem alloc] initWithTitle:searchButtonTitle style:UIBarButtonSystemItemSearch target:self action:@selector(searchBooking:)];
    else
        btnSearch = [ExSystem makeColoredButton:@"BLUE" W:80 H:30 Text:searchButtonTitle SelectorString:@"searchBooking:" MobileVC:self];
    self.navigationItem.rightBarButtonItem = btnSearch;
    
    NSArray *toolbarItems = @[];
	[self setToolbarItems:toolbarItems animated:YES];
    
    if (self.shouldDisableSearchButton) {
        self.navigationItem.rightBarButtonItem.enabled = NO;
    }
}


-(void)cancelSearch:(id)sender
{
    self.navigationController.toolbarHidden = YES;
	isCancelled = YES;
    
    [viewSearching setHidden:YES];
	[self makeSearchButton];
	[self.navigationItem setHidesBackButton:NO animated:YES];
}


-(void)makeCancelButton
{
    self.navigationController.toolbarHidden = NO;
	// Mob-2523 Localize cancel button title string 
	NSString *cancel = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	
    UIBarButtonItem *btnCancel = nil;
    if ([ExSystem is7Plus])
    {
        btnCancel = [[UIBarButtonItem alloc] initWithTitle:cancel style:UIBarButtonSystemItemCancel target:self action:@selector(cancelSearch:)];
        [btnCancel setTintColor:[UIColor redColor]];
    }
    else
        btnCancel = [TrainBookVC makeColoredButton:[UIColor redColor] W:100 H:30.0 Text:cancel Target:self SelectorString:@"cancelSearch:"];

	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSArray *toolbarItems = @[flexibleSpace, btnCancel, flexibleSpace];
	[self setToolbarItems:toolbarItems animated:YES];

    self.navigationItem.rightBarButtonItem = nil;
}

#pragma mark -
#pragma mark View setup methods
-(void)initTableData
{
    if (aList == nil)
    {
        [self createTableData];
    }
    
    int indexOfDepartureDate = NSNotFound;
    int indexOfReturnDate = NSNotFound;
    
    int numCells = [aList count];
    for (int i = 0; i < numCells; i++)
    {
        BookingCellData *bcd = aList[i];
        
        if ([bcd.cellID isEqualToString:@"DepartureDate"])
            indexOfDepartureDate = i;
        else if ([bcd.cellID isEqualToString:@"ReturnDate"])
            indexOfReturnDate = i;
    }

    if (isRoundTrip)
    {
        // Add the return date if it is not already there.
        if (indexOfReturnDate == NSNotFound && indexOfDepartureDate != NSNotFound)
            [aList insertObject:self.returnDateBCD atIndex:(indexOfDepartureDate + 1)];
    }
    else // One way
    {
        // Remove the return date if it is there.
        if (indexOfReturnDate != NSNotFound)
            [aList removeObjectAtIndex:indexOfReturnDate];
    }
}

-(void)createTableData
{
	self.aList = [[NSMutableArray alloc] initWithObjects:nil];
    
	BookingCellData *bcd; 
	
	bcd = [[BookingCellData alloc]init];
	bcd.cellID = @"From";
	bcd.lbl = [Localizer getLocalizedText:@"Departure City"];
	bcd.val = @""; 
	bcd.isDisclosure = YES;
	bcd.isDetailLocation = YES;
	[aList addObject:bcd];
	
	bcd = [[BookingCellData alloc]init];
	bcd.cellID = @"To";
	bcd.lbl = [Localizer getLocalizedText:@"Arrival City"];
	bcd.val = @"";
	bcd.isDisclosure = YES;
	bcd.isDetailLocation = YES;
	[aList addObject:bcd];
	
    
	//Departure
	bcd = [[BookingCellData alloc]init];
	bcd.cellID = @"DepartureDate";
	bcd.lbl = [Localizer getLocalizedText:@"Departure Date"];
    [TrainBookVC initBCDDate:bcd withDate:nil withTime:nil];
    NSDate* fromDate = bcd.dateValue;
	[aList addObject:bcd];

    // Return date
    self.returnDateBCD = [[BookingCellData alloc]init];
    returnDateBCD.cellID = @"ReturnDate";
    returnDateBCD.lbl = [Localizer getLocalizedText:@"Return Date"];
    returnDateBCD.isDisclosure = YES;
    [TrainBookVC initReturnBCDDate:returnDateBCD withFromDate:fromDate afterDays:4];
    [aList addObject:returnDateBCD];
    
    // Display Refundable cell data
    self.showCheckbox = [SystemConfig getSingleton].showCheckbox;
    if (showCheckbox) 
    {
        self.checkboxDefault = [SystemConfig getSingleton].checkboxDefault;
        [self makeRefundableCell];
    }
    
    if ([self fillClass:[UserConfig getSingleton].classOfServices])
    {
        bcd = [[BookingCellData alloc]init];
        bcd.cellID = @"ClassOfService";
        bcd.lbl = [Localizer getLocalizedText:@"Class of Service"];
        bcd.val = dictClass[@"Y"];
        bcd.val2 = @"Y";
        [aList addObject:bcd];
    }
    
    [self loadBCDFromEntity];
}


-(void)resetForRoundTrip
{
    [self initTableData];
    [self.tableList reloadData];
}

-(void) reloadBookingCriteriaSection
{
    int bookingSection = [self.aSections indexOfObject:@"Everything"];
    if (bookingSection >= 0)
    {
        NSIndexSet *indexSet = [NSIndexSet indexSetWithIndex:bookingSection];
        [tableList reloadSections:indexSet withRowAnimation:UITableViewRowAnimationFade];
    }
}

-(void) reloadCustomFieldsSection
{
    int travelCustomFieldSection = [self.aSections indexOfObject:KSECTION_TRIP_CUSTOM_FIELDS];
    if (travelCustomFieldSection >= 0)
    {
        NSIndexSet *indexSet = [NSIndexSet indexSetWithIndex:travelCustomFieldSection];
    
        [tableList reloadSections:indexSet withRowAnimation:UITableViewRowAnimationFade];
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
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return [aSections count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString* sectionName = aSections[section];
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS])
        return [tcfRows count];
    else if ([sectionName isEqualToString:KSECTION_GOV_TA_FIELDS])
        return [taFields count];
    else 
        return [aList count];
}

// Custom fields
-(UITableViewCell *)configureCustomFieldCellAtIndexPath:(NSIndexPath *)indexPath
{
    EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)tcfRows[[indexPath row]];
    
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

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
    NSString* sectionName = aSections[indexPath.section];
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS])
    {
        return [self configureCustomFieldCellAtIndexPath:indexPath];
    }
    else if ([sectionName isEqualToString:KSECTION_GOV_TA_FIELDS])
    {
        GovTAField * fld = [self.taFields objectAtIndex:row];
        ItinDetailCell *cell = (ItinDetailCell*)[tableView dequeueReusableCellWithIdentifier:@"ItinDetailCell"];
        if (cell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[ItinDetailCell class]])
                    cell = (ItinDetailCell *)oneObject;
        }
        
        cell.lblLabel.text = fld.label;
        cell.lblValue.text = fld.fieldValue;
        cell.ivDot.hidden = YES;

        return cell;
    }
    else
    {
        BookingCellData *bcd = aList[row];
            
        if ([bcd.cellID isEqualToString:@"RefundableOnly"])
        {
            BoolEditCell *cell = (BoolEditCell *)[tableView dequeueReusableCellWithIdentifier:@"BoolEditCell"];
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
            }
            cell.label.font = [cell.label.font fontWithSize:15];
            [cell setSeedData:[bcd.val isEqualToString:@"Y"] delegate:self context:bcd label:bcd.lbl];
            return cell;
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
        
            cell.lblLabel.text = bcd.lbl;
            
            cell.lblValue.text = bcd.val;
            cell.ivDot.hidden = YES;
            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
            
            return cell;
        }  
    }
}


#pragma mark -
#pragma mark Table view delegate
-(void) onSelectLongTextOrNumericFieldCellAtIndexPath:(NSIndexPath *)indexPath
{
    EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)tcfRows[[indexPath row]];
    
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


-(CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString* sectionName = aSections[indexPath.section];
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS])
        return 65;
    else
        return 50;
}

-(UIView *) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    NSString* sectionName = aSections[section];
    if ([sectionName isEqualToString:@"Everything"])
    {
        if(viewSegmentHeader.hidden)
            viewSegmentHeader.hidden = NO;
        return viewSegmentHeader;
    }
    
    return nil;
}

-(CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    NSString* sectionName = aSections[section];
    if ([sectionName isEqualToString:KSECTION_GOV_TA_FIELDS])
        return 0;
    else
        return 50;
}


-(UIView *) tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
{
    NSString* sectionName = aSections[section];
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS])
    {
        UIView *sectionHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, viewSegmentHeader.frame.size.width, 50)];
        sectionHeaderView.backgroundColor = [UIColor clearColor];
        return sectionHeaderView;
    }
    
    else if([Config isEvaVoiceEnabled] && [ExSystem is7Plus])    // Flight search is enabled only for iOS7 and above
    {
        return self.EvaBtnView;
    }

    return nil;
}

-(CGFloat) tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
    NSString* sectionName = aSections[section];
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS])
        return 50;
    else
        if([Config isEvaVoiceEnabled] && [ExSystem is7Plus])    // Flight search is enabled only for iOS7 and above
        {
            return self.EvaBtnView.frame.size.height;
        }
    else
        return 0;
}


-(BookingCellData*) getBCD:(NSString *)bcdId
{
    for(BookingCellData *bcdFound in aList)
    {
        if([bcdFound.cellID isEqualToString:bcdId])
        {
            return bcdFound;
        }
    }
    return nil;
}

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
	NSUInteger	row = indexPath.row;
    NSString* sectionName = aSections[indexPath.section];
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
        BookingCellData *bcd = aList[row];
        
        if([bcd.cellID isEqualToString:@"From"])
        {
            HotelLocationViewController * vc = [[HotelLocationViewController alloc] initWithNibName:@"HotelLocationViewController" bundle:nil];
            vc.locationDelegate = self;
            vc.neverShowOffices = YES;
            vc.isAirportOnly = YES;
            vc.tag = FROM_LOCATION_TAG;
            if([UIDevice isPad])
            {
                if([ExSystem is6Plus])
                    vc.modalPresentationStyle = UIModalPresentationCurrentContext;
                else
                    vc.modalPresentationStyle = UIModalPresentationFormSheet;
                [self presentViewController:vc animated:YES completion:nil];
            }
            else
            {   // Finds generic homeVC
                [[ConcurMobileAppDelegate findHomeVC] presentViewController:vc animated:YES completion:nil];
            }
        }
        else if([bcd.cellID isEqualToString:@"To"])
        {
            HotelLocationViewController * vc = [[HotelLocationViewController alloc] initWithNibName:@"HotelLocationViewController" bundle:nil];
            vc.locationDelegate = self;
            vc.neverShowOffices = YES;
            vc.isAirportOnly = YES;
            vc.tag = RETURN_LOCATION_TAG;
            if([UIDevice isPad])
            {
                if([ExSystem is6Plus])
                    vc.modalPresentationStyle = UIModalPresentationCurrentContext;
                else
                    vc.modalPresentationStyle = UIModalPresentationFormSheet;
                [self presentViewController:vc animated:YES completion:nil];
            }
            else 
            {
                [[ConcurMobileAppDelegate findHomeVC] presentViewController:vc animated:YES completion:nil];
            }
            
        }
        else if ([bcd.cellID isEqualToString:@"DepartureDate"]|| [bcd.cellID isEqualToString:@"ReturnDate"])
        {
            if ([UIDevice isPad]) 
            {
                [self pickerDateTapped:self IndexPath:indexPath];
            }
            else
            {
                DateTimeOneVC * vc = [[DateTimeOneVC alloc] initWithNibName:@"DateTimeOneVC" bundle:nil];
                NSString* lbl = [bcd.cellID isEqualToString:@"DepartureDate"]? [Localizer getLocalizedText:@"Departure Date"]:[Localizer getLocalizedText:@"Return Date"];
                [vc setSeedData:self withFullDate:bcd.dateValue withLabel:lbl withContext:bcd];
                [self.navigationController pushViewController:vc animated:TRUE];
            }
        }
        else if([bcd.cellID isEqualToString:@"ClassOfService"])
        {
            ClassOfServiceVC *vc = [[ClassOfServiceVC alloc] initWithNibName:@"ClassOfServiceVC" bundle:nil];
            vc.aClass = self.aClass;
            vc.dictClass = self.dictClass;
            [vc findSelected:bcd.val2];
            vc.bcd = bcd;
            self.shouldReload = YES;
            [self.navigationController pushViewController:vc animated:YES];
        }
    }
}


#pragma mark - Check to see if all fields are filled
-(NSString*) getCountryAbbrev :(BookingCellData*) bcd
{
    if (bcd.values != nil && [bcd.values count]>0)
        return (bcd.values)[0];
    return nil;
}

-(BOOL) isFlexFaringCountry:(NSString*)ctryAbbrev
{
    if (![ctryAbbrev length])
        return NO;
    
    NSArray* ffCountries = @[@"AU", @"NZ", @"CA", @"IN",  @"SE",  @"NO", @"DK", @"FI"];
    NSDictionary* ffDict = [NSDictionary dictionaryWithObjects:ffCountries forKeys:ffCountries];
    if (ffDict[[ctryAbbrev uppercaseString]])
    {
        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:nil message:[Localizer getLocalizedText:@"FLEX_FARING_ERROR_MSG"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles: nil];
        [av show];
        return YES;
    }
    return NO;
}

- (BOOL) hasSuffientProfile:(NSString*)fromCtry toCountry:(NSString*) toCtry
{
    if ([fromCtry isEqualToString:@"US"] || [toCtry isEqualToString:@"US"])
    {
        NSString* profileStatus = [[ExSystem sharedInstance] getUserSetting:@"ProfileStatus" withDefault:@"0"];
        // MOB-10390 Check users with profileStatus 1 (missing middlename, gender) on to/from country before allow to search air.
        if ([profileStatus isEqualToString:@"1"])
        {
            NSString* msg = [NSString stringWithFormat:@"%@\n\n%@", [Localizer getLocalizedText:@"AIR_BOOKING_PROFILE_1_MSG"], [@"AIR_BOOKING_PROFILE_PROLOG_MSG" localize]];
            MobileAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:nil
                                      message:msg
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                      otherButtonTitles:nil];
            [alert show];
            return NO;
        }
    }
    
    return TRUE;
}
-(BOOL) canContinue
{
    // FlexFaring
    bool checkFlexFaring = [[ExSystem sharedInstance] hasRole:ROLE_FLEX_FARING];

    BookingCellData *bcdFromCity = [self getBCD:@"From"];
    if(bcdFromCity.val2 == nil || [bcdFromCity.val2 length] < 3)
    {
        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Missing Departure"] message:[Localizer getLocalizedText:@"Missing Departure Message"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles: nil];
        [av show];
        return NO;
    }
    
    NSString *fromCountry = [self getCountryAbbrev:bcdFromCity];
    BookingCellData *bcdToCity = [self getBCD:@"To"];
    if((bcdToCity.val2 == nil || [bcdToCity.val2 length] < 3))
    {
        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Missing Arrival"] message:[Localizer getLocalizedText:@"Missing Arrival Message"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles: nil];
        [av show];
        return NO;
    }
    NSString *toCountry = [self getCountryAbbrev:bcdToCity];

    // Ensure that depart and arrival city are not the same
    if ([bcdFromCity.val2 isEqualToString:bcdToCity.val2])
    {
        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Departure Matches Arrival"] message:[Localizer getLocalizedText:@"Departure Matches Arrival Message"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles: nil];
        [av show];
        return NO;
    }

    // Check ProfileStatus = 1 and from and to location
    if (![self hasSuffientProfile:fromCountry toCountry:toCountry])
    {
        return NO;
    }
    
    if (checkFlexFaring)
    {
        if ([self isFlexFaringCountry:fromCountry])
            return NO;
        if ([self isFlexFaringCountry:toCountry])
            return NO;
    }

    if(isRoundTrip)
    {
        BookingCellData *bcdDepartDate = [self getBCD:@"DepartureDate"];
        BookingCellData *bcdReturnDate = [self getBCD:@"ReturnDate"];
        NSTimeInterval interval = [bcdReturnDate.dateValue timeIntervalSinceDate: bcdDepartDate.dateValue];

        // MOB-10506 We're good as long as return date is before departure date
        if(interval <= 0)
        {
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Return Date must be"] message:[Localizer getLocalizedText:@"Return Date must be Message"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles: nil];
            [av show];
            return NO;
        }
    }
    
    return YES;
}

#pragma mark -
#pragma mark Bar Methods
-(BOOL) hasPendingRequiredTripFields
{
    return [[TravelCustomFieldsManager sharedInstance] hasPendingRequiredTripFieldsAtStart:YES] && !hideCustomFields;
}

-(void) sendSearchMsg
{
	self.isCancelled = NO;
    
    EntityAirCriteria *lastAir = [self loadEntity];
    if(lastAir == nil)
        lastAir = [self makeNewEntity];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW",  nil];
    BookingCellData *bcd = [self getBCD:@"From"];
    pBag[@"StartIata"] = bcd.val2;
    lastAir.DepartureCity = bcd.val;
    lastAir.DepartureAirportCode = bcd.val2;
    
    bcd = [self getBCD:@"To"];
    pBag[@"EndIata"] = bcd.val2;
    lastAir.ArrivalCity = bcd.val;
    lastAir.ReturnAirportCode = bcd.val2;
    
    // MOB-7240 the date in cell is gmt date, and needs to be formatted using gmt time zone
    bcd = [self getBCD:@"DepartureDate"];
    NSString* fmtDate = [DateTimeFormatter formatDateYYYYMMddByDate:bcd.dateValue];
    
    pBag[@"Date"] = fmtDate;
    
    lastAir.DepartureDate = [bcd.dateValue dateByAddingTimeInterval:[bcd.val2 intValue]*60];
    pBag[@"SearchTime"] = [NSString stringWithFormat:@"%d", bcd.extendedTime];
    lastAir.DepartureTime = nil;
    
    if (isRoundTrip)
    {
        bcd = [self getBCD:@"ReturnDate"];
        pBag[@"ReturnDate"] = [DateTimeFormatter formatDateYYYYMMddByDate:bcd.dateValue];
        lastAir.ReturnDate = [bcd.dateValue dateByAddingTimeInterval:[bcd.val2 intValue]*60];
        pBag[@"ReturnTime"] = [NSString stringWithFormat:@"%d", bcd.extendedTime];
        lastAir.ReturnTime = nil;
    }
    
    if (showCheckbox)
    {
        bcd = [self getBCD:@"RefundableOnly"];
        if (bcd != nil && bcd.val != nil)
        {
            pBag[@"RefundableOnly"] = bcd.val;
        }
        lastAir.refundableOnly = @([@"Y" isEqualToString:bcd.val]);
    }
    else
        lastAir.refundableOnly = @NO;
    
    bcd = [self getBCD:@"ClassOfService"];
    if (bcd != nil) {
        lastAir.ClassOfService = bcd.val2;
        NSString *cos = bcd.val2;
        if([cos isEqualToString:@"ANY"])
            cos = @"";
        pBag[@"ClassOfService"] = cos;
    }
    else    //If no class of service option allowed. Always use Economy as default
    {
        lastAir.ClassOfService = @"Y";
        pBag[@"ClassOfService"] = @"Y";
    }
    
    if ([Config isGov])
        [pBag setObject:@"true" forKey:@"GovRateTypes"];
    else
        pBag[@"GovRateTypes"] = @"false";
    
    [self saveEntity];
    
    pBag[@"isRound"] = @(isRoundTrip);
    
    self.lastSearchUuid = [PostMsgInfo getUUID];
    pBag[@"SEARCH_UUID"] = self.lastSearchUuid;

    [[ExSystem sharedInstance].msgControl createMsg:AIR_SHOP CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


-(void) completeSearch
{
    [self sendSearchMsg];
}

-(void) sendGovPerDiemRateMsg
{
    GovTAField* perDiemFld = [GovTAField getPerDiemField:self.taFields];

    // TODO - verify date, gmt or local?
    BookingCellData* bcd = [self getBCD:@"DepartureDate"];
    NSDate * departureDate = [bcd.dateValue dateByAddingTimeInterval:[bcd.val2 intValue]*60];

    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 perDiemFld.perDiemLocState, @"STATE_CTRY_CODE",
                                 perDiemFld.perDiemLocation, @"LOCATION",
                                 departureDate, @"EFFECTIVE_DATE",
                                 perDiemFld.perDiemExpDate, @"EXPIRATEION_DATE",
                                 nil];
    
    [[ExSystem sharedInstance].msgControl createMsg:GOV_PER_DIEM_RATE CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


-(void) searchBooking:(id)sender
{
    if ([self hasPendingRequiredTripFields]) 
    {
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[@"PENDING_REQUIRED_TRAVEL_CUSTOM_FIELDS" localize] delegate:nil cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles:nil];
        [alert show];
        
        return;
    }
    
    if(![self canContinue])
        return;
    
	[self.navigationItem setHidesBackButton:YES animated:YES];
    
    BookingCellData* bcd = [self getBCD:@"From"];
    BookingCellData *bcdDate = [self getBCD:@"DepartureDate"];
    NSString *departingText = [NSString stringWithFormat: [Localizer getLocalizedText:@"Departing: token token"], bcd.val2 , bcdDate.val];
    bcd = [self getBCD:@"To"];
    bcdDate = [self getBCD:@"ReturnDate"];
    NSString *returningText = [NSString stringWithFormat: [Localizer getLocalizedText:@"Returning: token token"], bcd.val2 , bcdDate.val];
    [self showSearchingView:departingText line2:returningText];
    [self makeCancelButton];
    

    if ([Config isGov])
    {
        [self sendGovPerDiemRateMsg];
        return;
    }
    
    [self sendSearchMsg];
}


-(void) showSearchingView:(NSString *) line1 line2:(NSString *)line2
{
    [viewSearching setHidden:NO];
    lblSearchFrom.text = line1;
    if(isRoundTrip)
        lblSearchTo.text = line2;
    else
        lblSearchTo.text = @"";
    lblSearchTitle.text = [Localizer getLocalizedText:@"Searching for flights"];
}

-(NSString *)formatDate:(NSDate *)dt ExtendedHour:(NSInteger)extendedHour
{
	NSDateComponents *timeOnlyComponents = [[NSDateComponents alloc] init];
	[timeOnlyComponents setHour:[ExtendedHour getHourFromExtendedHour:extendedHour]];
	[timeOnlyComponents setMinute:0];
	[timeOnlyComponents setSecond:0];
	
	NSCalendar *calendar = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
	[calendar setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	NSDate *timeOnlyDate = [calendar dateFromComponents:timeOnlyComponents];
	
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// specify timezone
	[dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateFormat:@"HH:mm:ss"];
	NSString* time = [dateFormatter stringFromDate:timeOnlyDate];
    
	NSString *fun = [DateTimeFormatter formatDateYYYYMMddByDateLocal:dt];
	NSString *formedDate = [NSString stringWithFormat:@"%@T%@", fun, time]; 
	
	return formedDate;
}


+(NSDate *) getNSDateFromString:(NSString *)dt DateFormat:(NSString *)dateFormat
{
	NSDate *nsDate = [DateTimeFormatter getNSDate:dt Format:dateFormat];
	return nsDate;
}


#define kButtonWidth 100
#define kButtonHeight 29
-(UIView *)makeSelectButton
{
	UIView *v = [[UIView alloc] initWithFrame:CGRectMake(self.view.frame.size.width - (kButtonWidth + 15), 57, kButtonWidth, kButtonHeight)];
	v.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
	
	UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
	
	[button setBackgroundImage:[[UIImage imageNamed:@"blue_clean_light_blue_h32.png"]
								stretchableImageWithLeftCapWidth:8.0f 
								topCapHeight:0.0f]
					  forState:UIControlStateNormal];
	
	//set the frame of the button to the size of the image (see note below)
	button.frame = CGRectMake(0, 0, kButtonWidth, 30);
	
	[button addTarget:self action:@selector(searchBooking:) forControlEvents:UIControlEventTouchUpInside];
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, kButtonWidth, kButtonHeight)];
	
	lbl.font = [UIFont boldSystemFontOfSize:13];
	lbl.textColor = [UIColor whiteColor];
	lbl.shadowColor = [UIColor lightGrayColor];
	lbl.shadowOffset = CGSizeMake(0, -1);
	lbl.backgroundColor = [UIColor clearColor];
	lbl.textAlignment = NSTextAlignmentCenter;

	NSString *delText = [Localizer getLocalizedText:@"Create & Add"];		// TODO: Do not hard code the text.
	lbl.numberOfLines = 2;
	[lbl setLineBreakMode:NSLineBreakByWordWrapping];
	lbl.text = delText;
	
	[v addSubview:button];
	[v addSubview:lbl];

	return v;
}


+(UIBarButtonItem *)makeColoredButton:(UIColor *)btnColor W:(float)w H:(float)h Text:(NSString *)btnTitle Target:(id)target SelectorString:(NSString *)selectorString
{
	
	UIView *v = [[UIView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	
	UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
	
	NSString *btnImage = @"red_test_button";
	
	if([btnColor isEqual:[UIColor blueColor]])
		btnImage = @"blue_button";
	else if([btnColor isEqual:[UIColor greenColor]])
		btnImage = @"green_button";
	else if([btnColor isEqual:[UIColor grayColor]])
		btnImage = @"gray_button";
	
	[button setBackgroundImage:[[UIImage imageNamed:btnImage]
								stretchableImageWithLeftCapWidth:12.0f 
								topCapHeight:0.0f]
					  forState:UIControlStateNormal];
    
	
	//set the frame of the button to the size of the image (see note below)
	button.frame = CGRectMake(0, 0,w, h);
    
	[button addTarget:target action:NSSelectorFromString(selectorString) forControlEvents:UIControlEventTouchUpInside];
    
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	
	lbl.font = [UIFont boldSystemFontOfSize:13];
	lbl.shadowOffset = CGSizeMake(0, -1);
	lbl.backgroundColor = [UIColor clearColor];
	lbl.textAlignment = NSTextAlignmentCenter;
	
	lbl.textColor =  [UIColor whiteColor];
	lbl.shadowColor = [UIColor blackColor];
    
	lbl.text = btnTitle;
	
	[v addSubview:button];
	[v addSubview:lbl];
	
	//create a UIBarButtonItem with the button as a custom view
	UIBarButtonItem *customBarItem = [[UIBarButtonItem alloc] initWithCustomView:v];
	
	return customBarItem;
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
	
	int row = [indexPath row];
    BookingCellData *bcd = nil;
    if(row == 2)
        bcd = [self getBCD:@"DepartureDate"];
    else if(row == 3)
        bcd = [self getBCD:@"ReturnDate"];
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:pickerPopOverVC]; 
	[pickerPopOverVC initDate:bcd.dateValue];
    
	//MOB-3813, only allow one year out and make minimum date today
	pickerPopOverVC.datePicker.maximumDate = [NSDate dateWithTimeIntervalSinceNow:(60.0 * 60.0 * 24.0 * 365.0)];
    NSDate* now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
	pickerPopOverVC.datePicker.minimumDate = now;
    
	CGRect cellRect = [tableList rectForRowAtIndexPath:indexPath];
	CGRect myRect = [self.view convertRect:cellRect fromView:tableList];
	
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

#pragma mark -
#pragma mark Last Entity
-(EntityAirCriteria *) loadEntity
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityAirCriteria" inManagedObjectContext:[ExSystem sharedInstance].context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *a = [[ExSystem sharedInstance].context executeFetchRequest:fetchRequest error:&error];
    
    if([a count] > 0)
        return a[0];
    else
        return nil;
}


-(void) saveEntity
{
    NSError *error;
    if (![[ExSystem sharedInstance].context save:&error]) {
        NSLog(@"Whoops, couldn't save air criteria: %@", [error localizedDescription]);
    }
}


-(void) clearEntity:(EntityAirCriteria *) ent
{
    [[ExSystem sharedInstance].context deleteObject:ent];
}


-(EntityAirCriteria *) makeNewEntity
{
    return [NSEntityDescription insertNewObjectForEntityForName:@"EntityAirCriteria" inManagedObjectContext:[ExSystem sharedInstance].context];
}


#pragma mark - Segment Stuff
-(IBAction)setRoundTrip:(id)sender
{
    UISegmentedControl *seg = (UISegmentedControl*)sender;
	if(seg.selectedSegmentIndex == 1)
		self.isRoundTrip = YES;
	else 
		self.isRoundTrip = NO;
	
	[self resetForRoundTrip];
	
	[tableList reloadData];
}


-(IBAction)setOneWay
{
	self.isRoundTrip = NO;
	[self initTableData];
}

#pragma mark - BCD Loader
-(void) loadBCDFromEntity
{
    EntityAirCriteria *lastAir = [self loadEntity];
    
	if(lastAir != nil)
	{
		BookingCellData *bcdFrom = [self getBCD:@"From"];
		BookingCellData *bcdTo = [self getBCD:@"To"];

		// MOB-17829 Removing feature for Gov app.
        if (![Config isGov])
        {
            bcdFrom.val = lastAir.DepartureCity;
            bcdFrom.val2 = lastAir.DepartureAirportCode;
            bcdFrom.stationDepart.stationCode = lastAir.DepartureAirportCode;
        }
        
		if (bcdFrom.stationDepart == nil)
			bcdFrom.stationDepart = [[TrainStationData alloc] init];
		
		if (bcdTo.stationDepart == nil)
			bcdTo.stationDepart = [[TrainStationData alloc] init];
		
        if (![Config isGov])
        {
            bcdTo.val = lastAir.ArrivalCity;
            bcdTo.val2 = lastAir.ReturnAirportCode;
            bcdTo.stationDepart.stationCode = lastAir.ReturnAirportCode;
        }
		
        BookingCellData *bcdFromDate = [self getBCD:@"DepartureDate"];
        if (lastAir.DepartureDate != nil)
            [TrainBookVC initBCDDate:bcdFromDate withDate:lastAir.DepartureDate withTime:lastAir.DepartureTime];
        
		if(isRoundTrip)
		{
			BookingCellData *bcdReturnDate = [self getBCD:@"ReturnDate"];

            // Check that the return date is not in the past
            if ([lastAir.ReturnDate timeIntervalSinceNow] < 1)
            {
                // if the date is in the past, then set to nil so that return date will be defaulted based on the from date
                lastAir.ReturnDate = nil;
            }

            if (lastAir.ReturnDate != nil)
            {
                [TrainBookVC initBCDDate:bcdReturnDate withDate:lastAir.ReturnDate withTime:lastAir.ReturnTime];
            }
            else
            {
                // Reset to from date
                [TrainBookVC initReturnBCDDate:bcdReturnDate withFromDate:bcdFromDate.dateValue afterDays:4];
            }
		}
        
        BookingCellData *bcdClass = [self getBCD:@"ClassOfService"];
        if(lastAir.ClassOfService == nil)
        {
            bcdClass.val2 = @"Y";
            bcdClass.val = dictClass[@"Y"];
        }
        else
        {
            NSUInteger objectIdx = [aClass indexOfObject:lastAir.ClassOfService];
            if (objectIdx == NSNotFound)
            {
                bcdClass.val2 = @"Y";
                bcdClass.val = dictClass[@"Y"];
            }
            else
            {
                bcdClass.val2 = lastAir.ClassOfService;
                bcdClass.val = dictClass[lastAir.ClassOfService];
            }
        }
        
        if (showCheckbox) 
        {
            BookingCellData *bcdRef = [self getBCD:@"RefundableOnly"];
            if([lastAir.refundableOnly boolValue])
            {
                bcdRef.val = @"Y";
            }
            else
                bcdRef.val = @"N";
        }
        
		[tableList reloadData];
        
        if(bcdFrom.val != nil && bcdTo.val != nil)
            [self makeSearchButton];
	}
}


#pragma mark -
#pragma mark Location delegate methods

-(void)locationSelected:(LocationResult*)locationResult tag:(NSString*)tag;
{
	if ([tag isEqualToString:FROM_LOCATION_TAG])
	{
        BookingCellData *bcdFrom = [self getBCD:@"From"];
		bcdFrom.val = locationResult.location;
        NSArray *aa = [locationResult.location componentsSeparatedByString:@"("];
        if([aa count] == 2)
        {
            NSString *s2 = aa[1];
            s2 = [s2 substringToIndex:3];
            bcdFrom.val2 = s2;
        }
        // MOB-9637 store country in values array and later to entityAirCriteria
        bcdFrom.values = [NSMutableArray arrayWithObject:locationResult.countryAbbrev];
        
        [self.tableList reloadData];
	}
    else if ([tag isEqualToString:RETURN_LOCATION_TAG])
	{
		BookingCellData *bcdTo = [self getBCD:@"To"];
        bcdTo.val = locationResult.location;
        NSArray *aa = [locationResult.location componentsSeparatedByString:@"("];
        if([aa count] == 2)
        {
            NSString *s2 = aa[1];
            s2 = [s2 substringToIndex:3];
            bcdTo.val2 = s2;
        }
        // MOB-9637 store country in values array and later to entityAirCriteria
        bcdTo.values = [NSMutableArray arrayWithObject:locationResult.countryAbbrev];

        [self.tableList reloadData];
	}
}


#pragma mark - Class Stuff
-(BOOL) fillClass:(NSArray *) listOfAllowedClass
{
    aClass = [[NSMutableArray alloc] init];
    self.dictClass = [[NSMutableDictionary alloc] init];
    
    // Economy is always allowed even no other Class of Service avaiable.
    [self.aClass addObject:@"Y"];
    (self.dictClass)[@"Y"] = [Localizer getLocalizedText:@"Economy"];
    
    if (listOfAllowedClass != nil && [listOfAllowedClass count] > 0) {
        for (int i = 0; i < [listOfAllowedClass count]; i++)
        {
            NSString *oneAllowedClass = [listOfAllowedClass[i] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
            if([oneAllowedClass isEqualToString:FIRST_CLASS])
            {
                [self.aClass addObject:@"F"];
                (self.dictClass)[@"F"] = [Localizer getLocalizedText:@"First"];
            }
            else if ([oneAllowedClass isEqualToString:BUSINESS_CLASS])
            {
                [self.aClass addObject:@"C"];
                (self.dictClass)[@"C"] = [Localizer getLocalizedText:@"Business"];
            }
            else if ([oneAllowedClass isEqualToString:PREMIUM_ECONOMY])
            {
                [self.aClass addObject:@"W"];
                (self.dictClass)[@"W"] = [Localizer getLocalizedText:@"Premium Economy"];
            }
            else if ([oneAllowedClass isEqualToString:ONE_CLASS_UPGRADE]){
                [self.aClass addObject:@"ANY"];
                (self.dictClass)[@"ANY"] = [Localizer getLocalizedText:@"Any"];
            }
        }
        return YES;
    } else
        return NO;
}


-(NSDate *)addDaysToDate:(NSDate *)dateDepart NumDaysToAdd:(int)daysToAdd
{	
	// set up date components
	NSDateComponents *components = [[NSDateComponents alloc] init];
	[components setDay:daysToAdd];
	
	// create a calendar
	NSCalendar *gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
	
	NSDate *newDate2 = [gregorian dateByAddingComponents:components toDate:dateDepart options:0];
	
	return newDate2;
}

#pragma mark - Bool edit delegate

-(void) boolUpdated:(NSObject*) context withValue:(BOOL) val
{
    if (context != nil)
    {
        if ([context isKindOfClass:[BookingCellData class]]) 
        {
            BookingCellData * bcd = (BookingCellData*) context;
            bcd.val = val?@"Y":@"N";
        }
        else if ([context isKindOfClass:[EntityTravelCustomFields class]])
        {
            EntityTravelCustomFields *tcf = (EntityTravelCustomFields *) context;
            tcf.attributeValue = (val)?@"true":@"false";
            [[TravelCustomFieldsManager sharedInstance] saveIt:tcf];
        }
    }
}

#pragma mark - datetime editor callback
-(void) dateSelected:(NSObject*) context withDate:(NSDate*) newDate
{    
    NSDate* dawnDate = [DateTimeFormatter getDateWithoutTime:newDate withTimeZoneAbbrev:@"GMT"];    
    NSInteger tmInMinutes = [newDate timeIntervalSinceDate:dawnDate]/60;
    
    BookingCellData *bcdDate = (BookingCellData*) context;
    if ([bcdDate.dateValue compare:newDate]== NSOrderedSame) // No change
        return;
    
    NSDate* origPDate = [DateTimeFormatter getDateWithoutTimeInGMT: bcdDate.dateValue]; // To calc original date difference
    
    bcdDate.dateValue = newDate;
    
    bcdDate.val = [DateTimeFormatter formatBookingDateTime:bcdDate.dateValue];
    bcdDate.extendedTime = (tmInMinutes+59)/60;
    
	if([bcdDate.cellID isEqualToString:@"DepartureDate"])
	{
        NSDate* origDDate = [DateTimeFormatter getDateWithoutTimeInGMT:returnDateBCD.dateValue];
        NSTimeInterval originalTimeInterval = [origDDate timeIntervalSinceDate:origPDate];
        if (originalTimeInterval <= 0)
            return;
        
        NSInteger retTimeInMin = [DateTimeFormatter getTimeInSeconds:returnDateBCD.dateValue withTimeZoneAbbrev:@"GMT"]/60;
        NSDate* retDate = [dawnDate dateByAddingTimeInterval:originalTimeInterval];
        returnDateBCD.dateValue = [retDate dateByAddingTimeInterval:retTimeInMin*60];
        returnDateBCD.val = [DateTimeFormatter formatBookingDateTime:returnDateBCD.dateValue];
    }
    [self.tableList reloadData];
}

- (void)pickedDate:(NSDate *)dateSelected
{
    NSIndexPath * indexPath = pickerPopOverVC.indexPath;
    BookingCellData *bcdDate = [self getBCD:(indexPath.row ==2?@"DepartureDate":@"ReturnDate")];
    
    [self dateSelected:bcdDate withDate:dateSelected];
}


#pragma  mark - GOV fields

-(void) fieldUpdated:(FormFieldData*) field
{
    if ([self.aSections containsObject:KSECTION_GOV_TA_FIELDS])
    {
        int govSection = [self.aSections indexOfObject:KSECTION_GOV_TA_FIELDS];
        if (govSection >=0)
        {
            NSIndexSet *indexSet = [NSIndexSet indexSetWithIndex:govSection];
            [self.tableList reloadSections:indexSet withRowAnimation:UITableViewRowAnimationFade];
        }
    }
}

-(void) fieldCanceled:(FormFieldData *)field
{
}

+ (void) showAirVC:(UINavigationController*)navi withTAFields:(NSArray*) taFlds
{
    AirBookingCriteriaVC* vc = [[AirBookingCriteriaVC alloc] initWithNibName:@"AirBookingCriteriaVC" bundle:nil];
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
#pragma mark - show Evature UI  

- (IBAction)showEvaSearchUI:(id)sender
{
    //MOB-15527 - Starting iOS7, user needs to set mic permission for each app.
    // Check permission and prompt user to change setting if its not turned on
    if([[AVAudioSession sharedInstance] respondsToSelector:@selector(requestRecordPermission:)])
    {
        [[AVAudioSession sharedInstance] requestRecordPermission:^(BOOL granted)
         {
             if (granted) {
                 // Microphone enabled code
                 ALog(@" Microphone is enabled..");
                 //MOB-15802 - Show search window in main thread.
                 dispatch_async(dispatch_get_main_queue(), ^{
                     [self  showVoiceSearchVC];
                 });
             }
             else {
                 // Microphone disabled code
                 ALog(@" Microphone is disabled..");
                 
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
     	ALog(@"iOS6 - Mic requestRecordPermission not found ");
        [self  showVoiceSearchVC];
    }
    
    
}

-(void) showVoiceSearchVC
{
    if ([Config isNewVoiceUIEnabled] )
    {
        UINavigationController *nav = [[UIStoryboard storyboardWithName:@"EvaVoiceSearch_iPhone" bundle:nil]
                                       instantiateInitialViewController];
        
        EvaVoiceSearchViewController *c = [nav viewControllers][0];
        c.category = EVA_FLIGHTS;
        
        // MOB-18960 - Evature : Enable new Voice UI for HotelSearch
        // Do a push here instead of presentviewcontroller so as to avoid dealing with SwitchtoDetail hell after reserve is complete.
		// This is temporary only will decide if we have to user present or push when new UI screens for search are added.
        [self.navigationController pushViewController:c animated:YES];
//        [self presentViewController:nav animated:YES completion:nil];
    }

}


- (void)dismissEVASearch
{
    [self dismissViewControllerAnimated:YES completion:nil];
}


@end
