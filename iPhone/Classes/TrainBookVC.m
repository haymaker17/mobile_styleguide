//
//  TrainBookVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainBookVC.h"
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
#import "DateTimeOneVC.h"
#import "BoolEditCell.h"
#import "HotelBookingCell.h"
#import "CustomFieldTextEditor.h"
#import "FieldOptionsViewController.h"
#import "TravelViolationReasons.h"
#import "SystemConfig.h"
#import "Config.h"
#import "PostMsgInfo.h"

#import "GovTAField.h"
#import "GovDutyLocationVC.h"
#import "GovSelectTANumVC.h"
#import "GovPerDiemRateData.h"

#define KSECTION_GOV_TA_FIELDS @"GOV_TA_FIELDS"
#define KSECTION_TRIP_CUSTOM_FIELDS   @"TRIP_FIELDS"

@interface TrainBookVC ()
-(void) reloadCustomFieldsSection;
-(void) updateDynamicCustomFields;
-(void) fetchCustomFields;
-(void) completeSearch;

@property BOOL shouldDisableSearchButton;
@property (nonatomic, strong) NSString *lastSearchUuid;

@end

@implementation TrainBookVC
@synthesize tableList, aList, aSections, isRoundTrip, shouldReload, trainBooking, isCancelled, viewSegmentHeader, segmentTripDirection;
@synthesize pickerPopOverVC, viewSearching, lblSearchTo, lblSearchFrom, lblSearchTitle, tcfRows, isDirty, hideCustomFields, editedDependentCustomField, selectedCustomField; 
@synthesize taFields;

-(NSMutableArray *) groupTrains:(NSMutableArray *) allTrains
{
	__autoreleasing NSMutableArray *trains = [[NSMutableArray alloc] initWithObjects:nil];
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
		//NSLog(@"key = %@", key);
		
		RailChoiceData *railChoice = rcd;
		
		if(trainKeys[key] == nil)
		{
			//i have not found this particular segment
			railChoice.key = key;
			trainKeys[key] = railChoice;
			//[trains addObject:railChoice];
		}
		else {
			railChoice = trainKeys[key];
		}

		[railChoice addSeat:rcd.baseFare Cost:rcd.cost CurrencyCode:rcd.currencyCode Description:rcd.descript];
	}
	
	//NSLog(@"Number of choices [trainKeys] = %d", [trainKeys count]);
	for(NSString *key in trainKeys)
	{
		//NSLog(@"key = %@", key);
		RailChoiceData *rcd = trainKeys[key];
		[trains addObject:rcd];
		//NSLog(@"seat count = %d", [rcd.seats count]);
	}
	NSSortDescriptor *aSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"key" ascending:YES];
	[trains sortUsingDescriptors:@[aSortDescriptor]];
	
	return trains;
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
#pragma mark MVC stuff
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
    
    [viewSearching setHidden:YES];
    
	if ([msg.idKey isEqualToString:TRAIN_SHOP] && !isCancelled && [msg.parameterBag[@"SEARCH_UUID"] isEqualToString:self.lastSearchUuid])
	{
		TrainTimeTablesFetchData *trainTimes = (TrainTimeTablesFetchData *)msg.responder;
            
		NSMutableArray *aggTrains = [[NSMutableArray alloc] initWithObjects:nil];// trainTimes.railChoices; // = [self groupTrains:trainTimes.keys];
		
		for(NSString *key in trainTimes.railChoices)
			[aggTrains addObject:key];
        
		TrainGroupedListVC *tblvc = [[TrainGroupedListVC alloc] initWithNibName:@"TrainGroupedListVC" bundle:nil];
        [tblvc view];
		tblvc.hidesBottomBarWhenPushed = NO;
		tblvc.taFields = self.taFields;
		tblvc.aKeys = aggTrains;// trainTimes.keys;
		tblvc.dictGroups = trainTimes.railChoices;
		
		//ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
		if([UIDevice isPad])
			[self.navigationController pushViewController:tblvc animated:YES];
		else 
			[self.navigationController pushViewController:tblvc animated:YES];
        
		BookingCellData *bcdFrom = [self getBCD:@"From"]; // [sectionValues objectAtIndex:1];
		BookingCellData *bcdTo = [self getBCD:@"To"];// [sectionValues objectAtIndex:2];
		
		//tblvc.view;
        if(bcdTo != nil)
            tblvc.lblFrom.text = [NSString stringWithFormat:@"%@ - %@",  bcdFrom.val, bcdTo.val];
        else
            tblvc.lblFrom.text = [NSString stringWithFormat:@"%@",  bcdFrom.val];
		
		if([trainTimes.railChoices count] < 1)
		{
			[tblvc.vNothing setHidden:NO];
			tblvc.lblNothing.text = [Localizer getLocalizedText:@"No trains were found"];
		}
		else 
			[tblvc.vNothing setHidden:YES];
		
		BookingCellData *bcdFromDate = [self getBCD:@"DepartureDate"];
		BookingCellData *bcdReturnDate = nil; 
        
		if(isRoundTrip)
		{
			bcdReturnDate = [self getBCD:@"ReturnDate"];
			tblvc.lblDateRange.text = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateForBooking:bcdFromDate.dateValue], [DateTimeFormatter formatDateForBooking:bcdReturnDate.dateValue]];
		}
		else 
			tblvc.lblDateRange.text = bcdFromDate.val;
        
        
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
            self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAll];
            [tableList reloadData];
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

	if ([SystemConfig getSingleton] == nil)
    {
        [[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_SYSTEM_CONFIG CacheOnly:@"NO" ParameterBag:nil SkipCache:YES];
    }

    if ([TravelViolationReasons getSingleton] == nil)
		[[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_TRAVEL_VIOLATIONREASONS CacheOnly:@"NO" ParameterBag:nil SkipCache:YES];

    if (isDirty)
    {
        isDirty = NO;
        
        if (editedDependentCustomField && [selectedCustomField.attributeValue length])
            [self updateDynamicCustomFields];
        else if (!shouldReload) // Prevent multiple reloads of table view. Note: If you change shouldReload, please make sure that custom fields section is updated.
            [self reloadCustomFieldsSection];
    }
}

- (void)viewDidAppear:(BOOL)animated
{
	//self.navigationController.toolbar.hidden = YES;
    [super viewDidAppear:animated];
    
	if(shouldReload)
	{
		[tableList reloadData];
		shouldReload = NO;
	}
    
	[self.navigationItem setHidesBackButton:NO animated:YES];
}



// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    if ([Config isGov])
        self.aSections = [[NSMutableArray alloc] initWithObjects:KSECTION_GOV_TA_FIELDS, @"Everything", nil];
    else
        self.aSections = [[NSMutableArray alloc] initWithObjects:@"Everything", nil];
    
    self.shouldDisableSearchButton = false;
    if (!hideCustomFields)
    {
        self.shouldDisableSearchButton = true;
        
        [self fetchCustomFields];

        // prepopulate the custom fields from cache
        [aSections addObject:KSECTION_TRIP_CUSTOM_FIELDS];
        self.tcfRows = (NSMutableArray *)[[TravelCustomFieldsManager sharedInstance] fetchAll];
    }
    else 
    	[self hideLoadingView];
    
    [viewSearching setHidden:YES];
    
    [segmentTripDirection setTitle:[@"One Way" localize] forSegmentAtIndex:0];
    [segmentTripDirection setTitle:[@"Round Trip" localize] forSegmentAtIndex:1];
    
	isRoundTrip = YES;
	[self resetForRoundTrip];
	
	[self initTableData];
	self.title = [Localizer getLocalizedText:@"Rail"];
	trainBooking = [[TrainBooking alloc] init];
	[self makeSearchButton];
	
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
	self.navigationController.toolbar.hidden = NO;
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
//	//UIBarButtonItem *btnSearch = [[UIBarButtonItem alloc] initWithTitle:@"Search schedules" style:UIBarButtonItemStyleBordered target:self action:@selector(searchBooking:)];
//	//	UIBarButtonItem *btnAddToReport = [[UIBarButtonItem alloc] initWithTitle:addToReport style:UIBarButtonItemStyleBordered target:self action:@selector(buttonAddToReportOnePressed:)];
//	UIBarButtonItem *btnSearch2 = [TrainBookVC makeColoredButton:[UIColor blueColor] W:150 H:30.0 Text:[Localizer getLocalizedText:@"Search schedules"] Target:self SelectorString:@"searchBooking:"];
//	//UIBarButtonItem *btnSearch2 = [ExSystem makeColoredButton:@"BLUE" W:150.0 H:30.0 Text:@"Search schedules" Target:self SelectorString:@"searchBooking:" MobileVC:self];
//	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
//	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
//	
//	//UIBarButtonItem *btnSearchBlue = [[UIBarButtonItem alloc] initWithCustomView:[self makeSelectButton]];
//	NSMutableArray *toolbarItems = [NSArray arrayWithObjects:flexibleSpace, btnSearch2, flexibleSpace, nil];
//	[self setToolbarItems:toolbarItems animated:YES];
//	//	[btnAdd release];
//	//	[btnAddToReport release];
//	[flexibleSpace release];
//	
//	//self.navigationItem.rightBarButtonItem = nil;
//	//self.navigationItem.rightBarButtonItem = btnSearch;
//	//[btnSearch release];
//	//[btnSearch2 release];
    
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
	[self hideLoadingView];
    [self makeSearchButton];
	[viewSearching setHidden:YES];
	[self.navigationItem setHidesBackButton:NO animated:YES];
}


-(void)makeCancelButton
{
    self.navigationController.toolbarHidden = NO;
	// Mob-2523 Localize cancel button title string 
	NSString *cancel = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];//[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	
    UIBarButtonItem *btnCancel = nil;
    if ([ExSystem is7Plus])
    {
        btnCancel = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:self action:@selector(cancelSearch:)];
        [btnCancel setTintColor:[UIColor redColor]];
    }
    else
        btnCancel = [TrainBookVC makeColoredButton:[UIColor redColor] W:100 H:30.0 Text:cancel Target:self SelectorString:@"cancelSearch:"]; //@"Cancel"
	//	UIBarButtonItem *btnAddToReport = [[UIBarButtonItem alloc] initWithTitle:addToReport style:UIBarButtonItemStyleBordered target:self action:@selector(buttonAddToReportOnePressed:)];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSArray *toolbarItems = @[flexibleSpace, btnCancel, flexibleSpace];
	[self setToolbarItems:toolbarItems animated:YES];
	//	[btnAdd release];
	//	[btnAddToReport release];
	
	//self.navigationItem.rightBarButtonItem = nil;
	//self.navigationItem.rightBarButtonItem = btnSearch;
	//[btnCancel release];
    
    self.navigationItem.rightBarButtonItem = nil;
}




#pragma mark -
#pragma mark View setup methods
-(void)initTableData
{
	self.aList = [[NSMutableArray alloc] initWithObjects:nil];
	
	BookingCellData *bcd; 
	
	bcd = [[BookingCellData alloc]init];
	bcd.cellID = @"From";
	bcd.lbl = [Localizer getLocalizedText:@"Departure Station"];
	bcd.val = @""; //[NSString stringWithFormat:@"%@, %@", rootViewController.findMe.city, rootViewController.findMe.state];
	bcd.isDisclosure = YES;
	bcd.isDetailLocation = YES;
	[aList addObject:bcd];
	
	bcd = [[BookingCellData alloc]init];
	bcd.cellID = @"To";
	bcd.lbl = [Localizer getLocalizedText:@"Arrival Station"];
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
    
	if(isRoundTrip)
	{
		bcd = [[BookingCellData alloc]init];
		bcd.cellID = @"ReturnDate";
		bcd.lbl = [Localizer getLocalizedText:@"Return Date"];
		bcd.isDisclosure = YES;
        [TrainBookVC initReturnBCDDate:bcd withFromDate:fromDate afterDays:1];
		[aList addObject:bcd];
	}
    
    [self loadBCDFromEntity];
}


-(void)resetForRoundTrip
{
    [self.tableList reloadData];
}
	
#pragma mark -
#pragma mark Table view data source
// Custom fields
-(void) reloadCustomFieldsSection
{
    NSUInteger travelCustomFieldSection = [self.aSections indexOfObject:KSECTION_TRIP_CUSTOM_FIELDS];
    if (travelCustomFieldSection != NSNotFound)
    {
        NSIndexSet *indexSet = [NSIndexSet indexSetWithIndex:travelCustomFieldSection];
        [tableList reloadSections:indexSet withRowAnimation:UITableViewRowAnimationFade];
    }
}

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

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return [aSections count];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
{
    NSString* sectionName = aSections[section];
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS] && tcfRows != nil && [tcfRows count] > 0)
        return [@"Booking Info" localize];
    else 
        return nil;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString* sectionName = aSections[section];
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS])
        return [tcfRows count];
    else if ([sectionName isEqualToString:KSECTION_GOV_TA_FIELDS])
        return [taFields count];
    else if (self.isRoundTrip || [aList count] == 0)
    {
        // if roundTrip is true, then we want to return all the tableView cells. The check for 0 count was added to
        // avoid execution dropping thru to the final else condition and trying to return -1 as the rowCount
        return [aList count];
    }
    else
    {
        // if roundTrip is false, then we don't want the final cell "ReturnDate" to be returned,
        // adjusting the rowCount returned achieves this.
        return [aList count]-1;
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
        
        cell.ivDot.hidden = YES;
        
        cell.lblLabel.text = fld.label;
        cell.lblValue.text = fld.fieldValue;

        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        
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
        
        BookingCellData *bcd = aList[row];
        
        cell.lblLabel.text = bcd.lbl;
        
        cell.lblValue.text = bcd.val;
        cell.ivDot.hidden = YES;
        
        [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];

        return cell;
    }
}


#pragma mark -
#pragma mark Table view delegate
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


-(CGFloat) tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
    NSString* sectionName = aSections[section];
    if ([sectionName isEqualToString:KSECTION_TRIP_CUSTOM_FIELDS])
        return 50;
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
        if ([Config isGov])
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
    }
    else
    { 
        BookingCellData *bcd = aList[row];
        
        if([bcd.cellID isEqualToString:@"From"])
        {
            //From
            TrainStationsVC *tsvc = [[TrainStationsVC alloc] initWithNibName:@"TrainStationsVC" bundle:nil];
            tsvc.bcdFrom = [self getBCD:@"From"];
            tsvc.bcdTo = [self getBCD:@"To"];

            tsvc.parentVC = self;
            shouldReload = YES;
            if([UIDevice isPad])
            tsvc.modalPresentationStyle = UIModalPresentationFormSheet;
        
            tsvc.isFrom = YES;
            
            [self.navigationController pushViewController:tsvc animated:YES];
            //[self presentViewController:tsvc animated:YES completion:nil];
        }
        else if([bcd.cellID isEqualToString:@"To"])
        {
            //To
            TrainStationsVC *tsvc = [[TrainStationsVC alloc] initWithNibName:@"TrainStationsVC" bundle:nil];
            tsvc.bcdFrom = [self getBCD:@"From"];
            tsvc.bcdTo = [self getBCD:@"To"];
            
            tsvc.parentVC = self;
            shouldReload = YES;
            
            if([UIDevice isPad])
                tsvc.modalPresentationStyle = UIModalPresentationFormSheet;
            
            tsvc.isFrom = NO;
            
            [self.navigationController pushViewController:tsvc animated:YES];

        }
        else if([bcd.cellID isEqualToString:@"DepartureDate"] || [bcd.cellID isEqualToString:@"ReturnDate"])
        {
            //Depart Date
            if ([UIDevice isPad]) 
            {
                [self pickerDateTapped:self IndexPath:indexPath];
            }
            else 
            {
                DateTimeOneVC * vc = [[DateTimeOneVC alloc] initWithNibName:@"DateTimeOneVC" bundle:nil];
                [vc setSeedData:self withFullDate:bcd.dateValue withLabel:bcd.lbl withContext:bcd];
                [self.navigationController pushViewController:vc animated:TRUE];
            }
        }
    }
}


#pragma mark -
#pragma mark Bar Methods
-(BOOL) hasPendingRequiredTripFields
{
    return [[TravelCustomFieldsManager sharedInstance] hasPendingRequiredTripFields];
    return FALSE;
}

-(void) sendSearchMsg
{
    trainBooking.numAdults = 1;

	BookingCellData *bcdFrom = [self getBCD:@"From"];
	BookingCellData *bcdTo = [self getBCD:@"To"];
    BookingCellData *bcdDepart = [self getBCD:@"DepartureDate"];
    BookingCellData *bcdReturn = [self getBCD:@"ReturnDate"];
	
	NSDate *dateTo = bcdReturn.dateValue;
	
	AmtrakShop *shop = [[AmtrakShop alloc] init];
    
	shop.arrivalStation = bcdTo.stationDepart.stationCode; //@"SEA"; //
	shop.arrivalStationTimeZoneId = bcdTo.stationDepart.timeZoneName; // @"50"; //
	shop.classOfTravel = @"Y";
	shop.departureDateTime = [self formatDate:bcdDepart.dateValue ExtendedHour:bcdDepart.extendedTime]; // @"2010-08-22T12:00:00"; // bcdDepartureDate;
    
	shop.departureStation = bcdFrom.stationDepart.stationCode; //@"PDX"; //
	shop.departureStationTimeZoneId = bcdFrom.stationDepart.timeZoneName; // @"50";//
	shop.directOnly = @"false";
	shop.numberOfPassengers = @"1";
	shop.refundableOnly = @"false";
	if (isRoundTrip)
		shop.returnDateTime = [self formatDate:dateTo ExtendedHour:bcdReturn.extendedTime];
    
	EntityRail *lastRail = [self loadEntity];
    
    if(lastRail == nil)
        lastRail = [self makeNewEntity];
    
	lastRail.stationDepartCode = bcdFrom.stationDepart.stationCode;// forKey:@"STATION_DEPART_CODE"];
	lastRail.stationArriveCode = bcdTo.stationDepart.stationCode;// forKey:@"STATION_ARRIVE_CODE"];
	lastRail.stationDepartTZ = bcdFrom.stationDepart.timeZoneName;// forKey:@"STATION_DEPART_TZ"];
	lastRail.stationArriveTZ = bcdTo.stationDepart.timeZoneName;// forKey:@"STATION_ARRIVE_TZ"];
	lastRail.to = bcdTo.val;// forKey:@"TO_VAL"];
	lastRail.from = bcdFrom.val;// forKey:@"FROM_VAL"];
	
	lastRail.departDate = bcdDepart.dateValue;// forKey:@"DEPART_DATE"];
    lastRail.departTime = nil;
	
	if(isRoundTrip)
	{
		//BookingCellData *bcdReturnTime = [sectionValues objectAtIndex:3];
		lastRail.arriveDate = bcdReturn.dateValue;// forKey:@"ARRIVE_DATE"];
		lastRail.arriveTime = nil;// forKey:@"ARRIVE_TIME"];
	}
	[self saveEntity];
	
    if( bcdReturn.val != nil)
        [self showSearchingView:[NSString stringWithFormat:@"%@ - %@", shop.departureStation, bcdDepart.val] line2:[NSString stringWithFormat:@"%@ - %@", bcdTo.val, bcdReturn.val]];
    else
        [self showSearchingView:[NSString stringWithFormat:@"%@ - %@", shop.departureStation, bcdDepart.val] line2:[NSString stringWithFormat:@"%@", bcdTo.val]];
    
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", shop, @"SHOP", nil];
    
    self.lastSearchUuid = [PostMsgInfo getUUID];
    pBag[@"SEARCH_UUID"] = self.lastSearchUuid;
	[[ExSystem sharedInstance].msgControl createMsg:TRAIN_SHOP CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

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
    
	[self.navigationItem setHidesBackButton:YES animated:YES];

	self.isCancelled = NO;
    
	BookingCellData *bcdFrom = [self getBCD:@"From"];
	BookingCellData *bcdTo = [self getBCD:@"To"];
    BookingCellData *bcdDepart = [self getBCD:@"DepartureDate"];
    BookingCellData *bcdReturn = [self getBCD:@"ReturnDate"];
	
	if(bcdFrom.val == nil || bcdFrom.stationDepart == nil)
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:[Localizer getLocalizedText:@"Invalid Departure Location"]
							  message:[Localizer getLocalizedText:@"An invalid station for departure has been specified.  Please select a station to depart from."]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
        [self.navigationItem setHidesBackButton:NO animated:YES];
		return;
	}
	
	if(bcdTo.val == nil || bcdTo.stationDepart == nil)
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:[Localizer getLocalizedText:@"Invalid Arrival Location"]
							  message:[Localizer getLocalizedText:@"An invalid station for arrival has been specified.  Please select a station to arrive at."]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
        [self.navigationItem setHidesBackButton:NO animated:YES];
		return;
	}
    
	NSDate *dateFrom = bcdDepart.dateValue;
	NSDate *dateTo = bcdReturn.dateValue;
	
	NSDate * now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
	NSComparisonResult result = [now compare:dateFrom];
	BOOL isPast = result == NSOrderedDescending;
	
	if(isPast)
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:[Localizer getLocalizedText:@"Invalid Departure Date"]
							  message:[Localizer getLocalizedText:@"The date specified for the departure occurs in the past."]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
        [self.navigationItem setHidesBackButton:NO animated:YES];
		return;
	}
	
	if (isRoundTrip) 
	{
		NSComparisonResult result = [dateTo compare:dateFrom];
		BOOL isPast = result == NSOrderedAscending;
		
		if(isPast)
		{
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:[Localizer getLocalizedText:@"Invalid Return Date"]
								  message:[Localizer getLocalizedText:@"The date specified for the return is earlier than the date of departure."]
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
            [self.navigationItem setHidesBackButton:NO animated:YES];
			return;
		}
	}
        
    [self makeCancelButton];

     if ([Config isGov])
     {
        [self sendGovPerDiemRateMsg];
         return;
     }
     else{
        [self sendSearchMsg];
     }
}


-(void) showSearchingView:(NSString *) line1 line2:(NSString *)line2
{
    [viewSearching setHidden:NO];
    lblSearchFrom.text = line1;
    lblSearchTo.text = line2;
    lblSearchTitle.text = [Localizer getLocalizedText:@"Searching for trains"];
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
	[dateFormatter setLocale:[[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"]];
	
	[dateFormatter setDateFormat:@"HH:mm:ss"];
	NSString* time = [dateFormatter stringFromDate:timeOnlyDate];

	NSString *fun = [DateTimeFormatter formatDateYYYYMMddByDateLocal:dt];
	NSLog(@"fun = %@", fun);
	NSString *formedDate = [NSString stringWithFormat:@"%@T%@", fun, time]; // [DateTimeFormatter getLocalDate:[NSString stringWithFormat:@"%@T%@", dt, formedTime]];
	NSLog(@"formedDate = %@", formedDate);
	return formedDate;
}


+(NSDate *) getNSDateFromString:(NSString *)dt DateFormat:(NSString *)dateFormat
{
	NSDate *nsDate = [DateTimeFormatter getNSDate:dt Format:dateFormat];
	//NSDate *nsDate = [DateTimeFormatter getNSDate:dt Format:@"EEE MM dd, yyyy"];
	//NSLog(@"nsDate = %@", nsDate);
	return nsDate;
}


#define kButtonWidth 100
#define kButtonHeight 29
-(UIView *)makeSelectButton
{
    //return [ExSystem makeColoredButton:@"BLUE" W:kButtonWidth H:30 Text:<#(NSString *)#> SelectorString:<#(NSString *)#> MobileVC:<#(MobileViewController *)#>
	__autoreleasing UIView *v = [[UIView alloc] initWithFrame:CGRectMake(self.view.frame.size.width - (kButtonWidth + 15), 57, kButtonWidth, kButtonHeight)];
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
	//NSString *delText = [Localizer getLocalizedText:ADD_TO_RPT_CONFIRM_MSG];
	NSString *delText = [Localizer getLocalizedText:@"Create & Add"];		// TODO: Do not hard code the text.
	lbl.numberOfLines = 2;
	[lbl setLineBreakMode:NSLineBreakByWordWrapping];
	lbl.text = delText;
	
	[v addSubview:button];
	[v addSubview:lbl];
	
//	// Add the button, or more specifically, the view that contains it, to the cell
//	[self.view addSubview:v];
//	
//	[v release];
	
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

	//[button addTarget:self action:@selector(searchBooking:) forControlEvents:UIControlEventTouchUpInside];
	[button addTarget:target action:NSSelectorFromString(selectorString) forControlEvents:UIControlEventTouchUpInside];

	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	
	lbl.font = [UIFont boldSystemFontOfSize:13];
	//lbl.textColor = [UIColor whiteColor];
	//lbl.shadowColor = [UIColor lightGrayColor];
	lbl.shadowOffset = CGSizeMake(0, -1);
	lbl.backgroundColor = [UIColor clearColor];
	lbl.textAlignment = NSTextAlignmentCenter;
	
	lbl.textColor =  [UIColor whiteColor];
	lbl.shadowColor = [UIColor blackColor];

	lbl.text = btnTitle;
	
	[v addSubview:button];
	[v addSubview:lbl];
	
	//create a UIBarButtonItem with the button as a custom view
	__autoreleasing UIBarButtonItem *customBarItem = [[UIBarButtonItem alloc] initWithCustomView:v];
	
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
	
	NSInteger section = [indexPath section];
	NSInteger row = [indexPath row];
	//NSMutableArray *sectionValues = [aList objectAtIndex:section];
    BookingCellData *bcd = nil;
    if(row == 2)
        bcd = [self getBCD:@"DepartureDate"];
    else if(row == 3)
        bcd = [self getBCD:@"ReturnDate"];
	//BookingCellData *bcd = [sectionValues objectAtIndex:row];
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:pickerPopOverVC];               
	[pickerPopOverVC initDate:bcd.dateValue];

	//MOB-3813, only allow one year out and make minimum date today
	pickerPopOverVC.datePicker.maximumDate = [NSDate dateWithTimeIntervalSinceNow:(60.0 * 60.0 * 24.0 * 365.0)];
    NSDate* now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
	pickerPopOverVC.datePicker.minimumDate = now;

	if(section == 0 && row != 2)
	{
		BookingCellData *bcd2 = [self getBCD:@"DepartureDate"];
		pickerPopOverVC.datePicker.minimumDate = bcd2.dateValue;
	}
	
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
//	NSUInteger _path[2] = {kSectionEntry, kDateRow};
//	NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
//	NSArray *_indexPaths = [[NSArray alloc] initWithObjects:_indexPath, nil];
//	[_indexPath release];
//	[tableList reloadRowsAtIndexPaths:_indexPaths withRowAnimation:NO];
//	[_indexPaths release];
	
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
}

#pragma mark -
#pragma mark Last Entity
-(EntityRail *) loadEntity
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityRail" inManagedObjectContext:[ExSystem sharedInstance].context];
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
        NSLog(@"Whoops, couldn't save car: %@", [error localizedDescription]);
    }
}


-(void) clearEntity:(EntityRail *) ent
{
    [[ExSystem sharedInstance].context deleteObject:ent];
}


-(EntityRail *) makeNewEntity
{
    return [NSEntityDescription insertNewObjectForEntityForName:@"EntityRail" inManagedObjectContext:[ExSystem sharedInstance].context];
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
// Pass in a date to init with that date, or pass nil to init with current date
+(void) initBCDDate:(BookingCellData*)bcd withDate:(NSDate*)dateSource withTime:(NSNumber*) timeSource
{
    // Check that the date passed is no more than an hour ago
    if ([dateSource timeIntervalSinceNow] < -3600)
    {
        // if the date is in the past, then set to nil so that current date is used
        dateSource = nil;
    }
    
    if (dateSource!= nil)
    {
        NSDate* dateWithoutTime = [DateTimeFormatter getDateWithoutTime:dateSource withTimeZoneAbbrev:@"GMT"];
        NSInteger timeInMinutes = [dateSource timeIntervalSinceDate:dateWithoutTime]/60;
        timeInMinutes = (timeInMinutes+29)/30*30; // Round to half hour
    
        bcd.extendedTime = [timeSource intValue]; // objectForKey:@"DEPART_TIME"] intValue];
        if (bcd.extendedTime == 0 || timeSource == nil) {
            bcd.extendedTime = (timeInMinutes+59)/60;
        }
        else
        {
            // Use existing time
            timeInMinutes = bcd.extendedTime * 60;
        }
    
        bcd.dateValue = [dateWithoutTime dateByAddingTimeInterval:timeInMinutes*60]; // objectForKey:@"DEPART_DATE"];
        bcd.val = [DateTimeFormatter formatBookingDateTime:bcd.dateValue];
    }
    else
    {
        NSDate* nextHour = [DateTimeFormatter getNextHourDateTimeInGMT];
        bcd.dateValue = nextHour;
        bcd.extendedTime = [DateTimeFormatter getTimeInSeconds:bcd.dateValue withTimeZoneAbbrev:@"GMT"]/(60*60);
        if (bcd.extendedTime < 9)
        {
            NSInteger diff = 9 - bcd.extendedTime;
            bcd.extendedTime = 9;
            bcd.dateValue = [nextHour dateByAddingTimeInterval:diff*60*60];
        }
        bcd.val = [DateTimeFormatter formatBookingDateTime:bcd.dateValue];
    }
}

+(void) initReturnBCDDate:(BookingCellData*) bcd withFromDate:(NSDate*) fromDate afterDays:(NSInteger) daysDiff
{
    bcd.dateValue = [fromDate dateByAddingTimeInterval:24*60*60*daysDiff];//[NSDate date];
    bcd.extendedTime = [DateTimeFormatter getTimeInSeconds:fromDate withTimeZoneAbbrev:@"GMT"]/(60*60);
    bcd.val = [DateTimeFormatter formatBookingDateTime:bcd.dateValue];
}


-(void) loadBCDFromEntity
{
    EntityRail *lastRail = [self loadEntity];
    
	if(lastRail != nil)
	{
		NSString *from = lastRail.from; // objectForKey:@"FROM_VAL"];
		NSString *to = lastRail.to;// objectForKey:@"TO_VAL"];
		
		BookingCellData *bcdFrom = [self getBCD:@"From"];
		BookingCellData *bcdTo = [self getBCD:@"To"];
		
		bcdFrom.val = from;
		if (bcdFrom.stationDepart == nil)
			bcdFrom.stationDepart = [[TrainStationData alloc] init];
		
		if (bcdTo.stationDepart == nil)
			bcdTo.stationDepart = [[TrainStationData alloc] init];
		
		bcdFrom.stationDepart.stationCode = lastRail.stationDepartCode;// objectForKey:@"STATION_DEPART_CODE"];
		bcdFrom.stationDepart.timeZoneName = lastRail.stationDepartTZ;// objectForKey:@"STATION_DEPART_TZ"];
		
		bcdTo.val = to;
		bcdTo.stationDepart.stationCode = lastRail.stationArriveCode;// objectForKey:@"STATION_ARRIVE_CODE"];
		bcdTo.stationDepart.timeZoneName = lastRail.stationArriveTZ; // objectForKey:@"STATION_ARRIVE_TZ"];	
		
		BookingCellData *bcdFromDate = [self getBCD:@"DepartureDate"];
        if (lastRail.departDate != nil)
            [TrainBookVC initBCDDate:bcdFromDate withDate:lastRail.departDate withTime:lastRail.departTime];
        
		if(isRoundTrip)
		{
			BookingCellData *bcdReturnDate = [self getBCD:@"ReturnDate"];

            // Check that the arrive date is not in the past
            if ([lastRail.arriveDate timeIntervalSinceNow] < 1)
            {
                // if the date is in the past, then set to nil so that arrive date will be defaulted based on the from date
                lastRail.arriveDate = nil;
            }

            if (lastRail.arriveDate != nil)
            {
                [TrainBookVC initBCDDate:bcdReturnDate withDate:lastRail.arriveDate withTime:lastRail.arriveTime];
            }
            else
            {
                // Reset to from date
                [TrainBookVC initReturnBCDDate:bcdReturnDate withFromDate:bcdFromDate.dateValue afterDays:1];
            }
		}
		
		[tableList reloadData];
        
        if(bcdFrom.val != nil && bcdTo.val != nil)
            [self makeSearchButton];
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
    
	if([bcdDate.cellID isEqualToString:@"DepartureDate"] && self.isRoundTrip)
	{
        BookingCellData *bcdReturnDate = [self getBCD:@"ReturnDate"];// [sectionValues objectAtIndex:2];
        NSDate* origDDate = [DateTimeFormatter getDateWithoutTimeInGMT:bcdReturnDate.dateValue];
        NSTimeInterval originalTimeInterval = [origDDate timeIntervalSinceDate:origPDate];
        if (originalTimeInterval <= 0)
            return;
        
        NSInteger retTimeInMin = [DateTimeFormatter getTimeInSeconds:bcdReturnDate.dateValue withTimeZoneAbbrev:@"GMT"]/60;
        NSDate* retDate = [dawnDate dateByAddingTimeInterval:originalTimeInterval];
        bcdReturnDate.dateValue = [retDate dateByAddingTimeInterval:retTimeInMin*60];
        bcdReturnDate.val = [DateTimeFormatter formatBookingDateTime:bcdReturnDate.dateValue];
    }
    [self.tableList reloadData];
}

- (void)pickedDate:(NSDate *)dateSelected
{
    NSIndexPath * indexPath = pickerPopOverVC.indexPath;
    BookingCellData *bcdDate = [self getBCD:(indexPath.row ==2?@"DepartureDate":@"ReturnDate")];
    
    [self dateSelected:bcdDate withDate:dateSelected];
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
            [self.tableList reloadSections:indexSet withRowAnimation:UITableViewRowAnimationFade];
        }
    }
}

-(void) fieldCanceled:(FormFieldData *)field
{
}


+ (void) showTrainVC:(UINavigationController*)navi withTAFields:(NSArray*) taFlds
{
    TrainBookVC* vc = [[TrainBookVC alloc] initWithNibName:@"TrainBookVC" bundle:nil];
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
