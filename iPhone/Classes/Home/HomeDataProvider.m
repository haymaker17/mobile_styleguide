//
//  HomeDataProvider.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 10/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "HomeDataProvider.h"
#import "ExpenseTypesManager.h"
#import "DataConstants.h"
#import "Localizer.h"
#import "Config.h"

//// TODO : Move these to a common constants file



@interface HomeDataProvider ()

@property (nonatomic, strong) EntityTrip    *currentTrip;
@property int pendingServerCalls;

@end

@implementation HomeDataProvider


-(NSString *)getViewIDKey
{
	return @"HOME_DATA_PROVIDER";
}


-(void) setupHomeData:(BOOL)shouldSkipCache
{
    self.pendingServerCalls = 0;
    [self getTripsData:shouldSkipCache];
    [self getSummaryData:shouldSkipCache];
    // Add other methods here
    // ...
    
}

-(void) didProcessMessage:(Msg *)msg
{
    if ([msg.idKey isEqualToString:TRIPS_DATA])
	{
		TripsData* tripsData = (TripsData *)msg.responder;
		[self refreshUIWithTripsData:tripsData];
        self.pendingServerCalls--;
	}
   else if ([msg.idKey isEqualToString:SUMMARY_DATA])
	{
		if ([ExSystem sharedInstance].sessionID == nil)
		{
			// Fix for MOB-1599:
			//
			// The session id is nil, but we're here because a response
			// was just received from the server for the user who logged out.
			// Do not remove any objects from the sectionData, sectionKeys, and
			// sections arrays.  Those arrays were cleaned up in initSections
			// which was called by buttonLogoutPressed when the user pressed
			// the logout button.  If we mess with them now, things could go
			// wrong for the next user who logs in.
			//
			return;
		}
		
        // Check role in SummaryData instead
		self.summaryData = (SummaryData *)msg.responder;
		[self refreshUIWithSummaryData:self.summaryData];
        self.pendingServerCalls--;
 	}

    if(self.pendingServerCalls == 0 && self.delegate != nil )
    {
        [self.delegate refreshComplete];
    }
}

-(BOOL)isTravelOnly
{
 	// check if the user has Travel only role
    if ([[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER] &&
        !( [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER] || [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER] || [[ExSystem sharedInstance] hasRole:MOBILE_INVOICE_PAYMENT_USER] || [[ExSystem sharedInstance] hasRole:ROLE_INVOICE_APPROVER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_REQUEST_APPROVER] || [[ExSystem sharedInstance] hasRole:ROLE_TRIP_APPROVER])  )
    {
        return YES;
    }
    
    return NO;
}

-(void)preFetchExpenseData:(BOOL)shouldSkipCache
{
    if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
    {
        // for offline QE
        // Load the expense types
        ExpenseTypesManager* etMgr = [ExpenseTypesManager sharedInstance];
        [etMgr loadExpenseTypes:nil msgControl:[ExSystem sharedInstance].msgControl];
        
        // Load the expenses
        NSMutableDictionary* pBag3 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
        
        // Call new endpoint if eReceipts is enabled.
        [[ExSystem sharedInstance].msgControl createMsg:ME_LIST_DATA() CacheOnly:@"NO" ParameterBag:pBag3 SkipCache:shouldSkipCache RespondTo:self];
        

        // Load currencies
        FormFieldData *currencyField = [[FormFieldData alloc] init];
        currencyField.iD = @"TransactionCurrencyName";
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: currencyField, @"FIELD", @"Y", @"MRU", nil];
        [[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:shouldSkipCache RespondTo:self];
        
    }

}

//Place a server call to get Trips data
-(void)getTripsData:(BOOL)shouldSkipCache
{
    
    if (([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER] || [[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER] || [[ExSystem sharedInstance] hasRole:ROLE_OPEN_BOOKING_USER]))
    {
        self.pendingServerCalls++;
        NSMutableDictionary* pBag2 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag2 SkipCache:shouldSkipCache RespondTo:self];
    }
    else
    {
        // Remove any old Travel section
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_TRIPS_FIND_TRAVEL];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];

        // MOB-16180
        // Don't show an empty cell.
        // Empty cell is used for 9.0 design for top picture.
//        EntityHome *emptyentity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRAVEL];
//        emptyentity.sectionValue = kSECTION_TRIPS;
//        if([ExSystem sharedInstance].isSingleUser)
//            emptyentity.sectionPosition = @0;
//        else
//            emptyentity.sectionPosition = @kSECTION_TRIPS_POS;
//        emptyentity.rowPosition = @0;
//        emptyentity.key = kSECTION_TRAVEL;
//        [[HomeManager sharedInstance] saveIt:emptyentity];
    }
    
}

// Build trips data for UI
- (void) refreshUIWithTripsData: (TripsData *) tripsData
{
	//TripData* currentTrip = nil;
    int upcoming = 0;
    int active = 0;
    
    NSArray *aTrips = [[TripManager sharedInstance] fetchAll];
    
    // MOB-7192 Strip out timezone info in current local time, so that we can compare it with travel time in GMT(w/o timezone info).
    NSDate* now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
    
    // MOB-5945 Whenever new TripsData comes, we need to reset active/upcoming trips as well as currentTrip
	if ([aTrips count] > 0) /*&& self.currentTrip == nil*/
	{
		for (int i = 0; i < [aTrips count]; i++)
		{
			EntityTrip* trip = (EntityTrip*)aTrips[i];
            // MOB-5945 - Make comparison consistent with the logic in TripsViewController
			if (([trip.tripEndDateLocal compare:now] == NSOrderedDescending) && ([trip.tripStartDateLocal compare:now] == NSOrderedAscending))
            {
                //is the end date of the looped to trip greater than today and is the startdate before now?
                active ++;
                if (self.currentTrip == nil)
                {
                    self.currentTrip = trip;
                }
                else
                {
                    if ([trip.tripStartDateLocal compare:self.currentTrip.tripStartDateLocal] == NSOrderedAscending)
                    {
                        self.currentTrip = trip;
                    }
                }
            }
		}
	}
    else
        self.currentTrip = nil;  // No trips
    
    
    if ([aTrips count] > 0)
	{
		for (int i = 0; i < [aTrips count]; i++)
		{
			EntityTrip* trip = (EntityTrip*)aTrips[i];
			//AJC -- is this code needed? delete after 2013-09-20 if not needed by then
            //NSDate* startDate = [DateTimeFormatter getLocalDate:trip.tripStartDateLocal];
			if ([trip.tripStartDateLocal compare:now] == NSOrderedAscending) //is the start date of the looped to trip greater than today?
				continue; //yup yup
			
            upcoming++;
		}
	}

    //Current Trip Row
	if (self.currentTrip != nil)
	{
        
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRAVEL];
        entity.name = self.currentTrip.tripName;
        entity.subLine  = [NSString stringWithFormat:@"%@ - %@", [DateTimeFormatter formatDateForTravelByDate:self.currentTrip.tripStartDateLocal], [DateTimeFormatter formatDateForTravelByDate:self.currentTrip.tripEndDateLocal]];
        entity.keyValue = self.currentTrip.tripKey;
        entity.imageName = @"icon_current_trip";
        entity.sectionValue = kSECTION_TRIPS;
        entity.sectionPosition = @kSECTION_TRIPS_POS;
        entity.rowPosition = @0;
        entity.key = kSECTION_TRAVEL;
        [[HomeManager sharedInstance] saveIt:entity];
	}
    else
    {
        //kill off find travel if the user should not have it
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_TRAVEL];
        if (entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
    }
    
    //Trips Row
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRAVEL];
    entity.name = [Localizer getLocalizedText:@"Trips"];
    entity.subLine = [Localizer getLocalizedText:@"View your trips"];
    // end MOB-16175 Show static text for new Home design
    // new UI checks the itemCount.  need to set it
    entity.itemCount = [[NSNumber alloc] initWithInt:upcoming];

    entity.key = kSECTION_TRAVEL;
    entity.sectionValue = kSECTION_TRIPS;
    entity.sectionPosition = @kSECTION_TRIPS_POS;
    entity.imageName = @"home_icon_trip";
    entity.rowPosition = @0;
    [[HomeManager sharedInstance] saveIt:entity];
    
    int rowPosition = 1 ;
    if (self.isTravelOnly && [[ExSystem sharedInstance] siteSettingAllowsTravelBooking])
    {
        // Add booking sections
        
        //book Hotel
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:BOOKINGS_BTN_HOTEL];
        entity.name =[Localizer getLocalizedText:@"Hotels"];
        entity.subLine = [Localizer getLocalizedText:@"Hotels_subline"] ;
        entity.key = BOOKINGS_BTN_HOTEL;
        entity.sectionValue = kSECTION_TRIPS;
        entity.imageName = @"home_icon_hotel";
        entity.rowPosition = @(rowPosition++);
        [[HomeManager sharedInstance] saveIt:entity];
        
        
        //book flight always shows up, site settings/profile status is checked when user clicks on flight booking
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:BOOKINGS_BTN_AIR];
        entity.name = [Localizer getLocalizedText:@"Flights"];
        entity.subLine = [Localizer getLocalizedText:@"Flights_subline"];
        entity.key = BOOKINGS_BTN_AIR;
        entity.sectionValue = kSECTION_TRIPS;
        entity.imageName = @"home_icon_flight";
        entity.rowPosition = @(rowPosition++);
        [[HomeManager sharedInstance] saveIt:entity];
        
        //book car
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:BOOKINGS_BTN_CAR];
        entity.name = [Localizer getLocalizedText:@"Cars"];
        entity.subLine = [Localizer getLocalizedText:@"Cars_subline"];
        entity.key = BOOKINGS_BTN_CAR;
        entity.sectionValue = kSECTION_TRIPS;
        entity.imageName = @"home_icon_car";
        entity.rowPosition = @(rowPosition++);
        [[HomeManager sharedInstance] saveIt:entity];
        
        if([[ExSystem sharedInstance] hasRole:ROLE_AMTRAK_USER])
        {
            entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:BOOKINGS_BTN_RAIL];
            entity.name = [Localizer getLocalizedText:@"Rail"];
            entity.subLine = [Localizer getLocalizedText:@"Rail_subline"];
            entity.key = BOOKINGS_BTN_RAIL;
            entity.sectionValue = kSECTION_TRIPS;
            entity.imageName = @"home_icon_rail";
            entity.rowPosition = @(rowPosition++);
            [[HomeManager sharedInstance] saveIt:entity];
        }
    }
    else     // Delete any stale rows
    {
        //kill off book travel rows if the user should not have it
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:BOOKINGS_BTN_HOTEL];
        if (entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:BOOKINGS_BTN_AIR];
        if (entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:BOOKINGS_BTN_CAR];
        if (entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:BOOKINGS_BTN_RAIL];
        if (entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
    }
    
    // update the new UI counts
    [self.delegate updateBadgeCounts];
}


// Place a server call to get summary data
-(void)getSummaryData:(BOOL)shouldSkipCache
{
    if( [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER] || [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER] || [[ExSystem sharedInstance] hasRole:MOBILE_INVOICE_PAYMENT_USER] || [[ExSystem sharedInstance] hasRole:ROLE_INVOICE_APPROVER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_REQUEST_APPROVER] || [[ExSystem sharedInstance] hasRole:ROLE_TRIP_APPROVER] || [[ExSystem sharedInstance]hasRole:ROLE_MOBILE_INVOICE_PURCH_APRVR] )
    {
        self.pendingServerCalls++;
        NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
        
        // MOB-13374 mark REPORT_APPROVAL_LIST_DATA as expired so if the approver tap on it it can find new data
        //MOB-15536 - Call only if user has the Expense Approver role
        if( [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER])
            [[ExSystem sharedInstance].cacheData markAsNeedingRefresh:REPORT_APPROVAL_LIST_DATA UserID:[ExSystem sharedInstance].userName RecordKey:@"0"];
        
        [[ExSystem sharedInstance].msgControl createMsg:SUMMARY_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:shouldSkipCache RespondTo:self];
        
    }
    else // if user is travel only then coredata might have other info left out
    {
        // Delete QE/Expenses/Reports rows from home.
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_QUICK];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_CARDS];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_REPORTS];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        //kill off approvals if the user should not have it
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
    }
    
}


// Build UI data here
-(void)refreshUIWithSummaryData:(SummaryData*)sd
{
	NSString *reportsToApproveCount = (sd.dict)[@"ReportsToApproveCount"];
    if (![reportsToApproveCount length])
        reportsToApproveCount = @"0";
	NSString *unsubmittedReportsCount = (sd.dict)[@"UnsubmittedReportsCount"];
    if (![unsubmittedReportsCount length])
        unsubmittedReportsCount = @"0";
	NSString *corpCardTransactionCount = (sd.dict)[@"CorporateCardTransactionCount"];
    if (![corpCardTransactionCount length])
        corpCardTransactionCount = @"0";

    NSString *travelRequestsToApprove = (sd.dict)[@"TravelRequestApprovalCount"];
    NSString *invoicesToApprove = (sd.dict)[@"InvoicesToApproveCount"];
    NSString *invoicesToSubmit = (sd.dict)[@"InvoicesToSubmitCount"];
    NSString *tripsToApproveCount = (sd.dict)[@"TripsToApproveCount"];
    NSString *purchaseRequestCount = (sd.dict)[purchaseRequestsToApproveCount];
    
    if (![travelRequestsToApprove length])
        travelRequestsToApprove = @"0";
    
    int iPos = 0;
    EntityHome *entity = nil;
    
    int totalApprovals =  [travelRequestsToApprove intValue] + [invoicesToApprove intValue] + [invoicesToSubmit intValue] + [tripsToApproveCount intValue] + [purchaseRequestCount intValue];
    
    // MOB-13478 - If site settings doesnt allow expense approvals remove approval reports count from the total.
    // Approval counts dont match if user has invoice or request approvals and sitesettings disabled since the approval screen hides request approvals if sitesetting is disabled.
    // To avoid confusion add reportsto approve count to totalapprovals only if sitesettings allows expense report approvals
    if([[ExSystem sharedInstance] siteSettingAllowsExpenseApprovals])
    {
        totalApprovals += [reportsToApproveCount intValue];
    }
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
    {
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_REPORTS];
        entity.name = [Localizer getLocalizedText:@"EXPENSE_REPORTS"];
        entity.itemCount = @([unsubmittedReportsCount intValue]);
        //entity.subLine  = [NSString stringWithFormat:@"%@ %@", unsubmittedReportsCount, [Localizer getLocalizedText:@"unsubmitted Reports"]];
        // MOB-16175 Show static text for new Home design
//        NSString *sub = nil;
//        int unSubReports = [unsubmittedReportsCount intValue];
//        if(unSubReports == 0)
//        {
//            sub = [Localizer getLocalizedText:@"EXPENSE_REPORTS_NEG_TEXT"];
//        }
//        else
//        {
//            sub = [NSString stringWithFormat:[Localizer getLocalizedText:@"REPORTS_TO_SUBMIT"], unSubReports];
//        }
        
        entity.subLine = [Localizer getLocalizedText:@"EXPENSE_REPORTS_NEG_TEXT"];
        // end MOB-16175 Show static text for new Home design
        entity.key = kSECTION_EXPENSE_REPORTS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = @kSECTION_EXPENSES_REPORTS_POS;
        entity.imageName = @"home_icon_report";
        entity.rowPosition = @(iPos);
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
        
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_CARDS];
            
        entity.name = [Localizer getLocalizedText:@"Expenses"];
        entity.subLine  = [Localizer getLocalizedText:@"List of your expenses"];
        // end MOB-16175 Show static text for new Home design
        entity.itemCount = @([corpCardTransactionCount intValue]);
        entity.key = kSECTION_EXPENSE_CARDS;
        entity.sectionValue = kSECTION_EXPENSE;
            
        entity.sectionPosition = @kSECTION_EXPENSES_POS;
        entity.imageName = @"home_icon_expense";
        entity.rowPosition = @(iPos);
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
    }
    else
    {
        // Delete QE/Expenses/Reports rows from home.
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_QUICK];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_CARDS];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_REPORTS];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
    }
    
    // Approvals - show approvals for all these roles
    if([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER] || [[ExSystem sharedInstance] hasRole:MOBILE_INVOICE_PAYMENT_USER] || [[ExSystem sharedInstance] hasRole:ROLE_INVOICE_APPROVER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_REQUEST_APPROVER] || [[ExSystem sharedInstance] hasRole:ROLE_TRIP_APPROVER] || [[ExSystem sharedInstance]hasRole:ROLE_MOBILE_INVOICE_PURCH_APRVR])
    {
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_APPROVALS];
        entity.name = [Localizer getLocalizedText:@"Approvals"];
        
        entity.subLine  = [Localizer getLocalizedText:@"APPROVALS_NEG_TEXT"];
        // end MOB-16175 Show static text for new Home design
        entity.itemCount = @(totalApprovals);
        entity.key = kSECTION_EXPENSE_APPROVALS;
        entity.sectionValue = kSECTION_EXPENSE;
        entity.sectionPosition = @kSECTION_EXPENSE_POS;
        entity.imageName = @"home_icon_approval";
        entity.rowPosition = @(iPos);
        [[HomeManager sharedInstance] saveIt:entity];
        iPos++;
    }
    else if([[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS] != nil)
    {
        //kill off approvals if the user should not have it
        entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_APPROVALS];
        [[HomeManager sharedInstance] deleteObj:entity];
    }

    // update the new UI counts
    [self.delegate updateBadgeCounts];
}


@end
