//
//  HomeTableViewCellDescriptionFactory.m
//  ConcurMobile
//
//  Created by ernest cho on 12/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "HomeTableViewCellDescriptionFactory.h"
#import "HomeTableViewCellDescription.h"
#import "ExSystem.h"

@implementation HomeTableViewCellDescriptionFactory

/*
 * add Travel Request in all descriptions
 * and condition its vivibility if travel request is enabled and user have travel request user role
 */
+(void)addTravelRequestCellTo:(NSMutableArray**)tmp{
    
    if ([[ExSystem sharedInstance] hasTravelRequest] &&
        [[ExSystem sharedInstance] isRequestUser]) {
        
        [*tmp addObject:[self travelRequestsCellDescription]];
    }

}

/**
 Cell descriptions for the default Home layout
 */
+ (NSMutableArray *)descriptionsForDefault
{
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    
    [tmp addObject:[self tripsCellDescription]];
    [tmp addObject:[self expensesCellDescription]];
    [tmp addObject:[self expenseReportsCellDescription]];
    [tmp addObject:[self approvalsCellDescription]];
	[self addTravelRequestCellTo:&tmp];
	
    return tmp;
}

/**
 Cell descriptions for an Expense Only Home layout
 */
+ (NSMutableArray *)descriptionsForExpenseOnly
{
    // expense only user has expense and expense reports only
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    
    [tmp addObject:[self expensesCellDescription]];
    [tmp addObject:[self expenseReportsCellDescription]];
	[self addTravelRequestCellTo:&tmp];
	
    return tmp;
}

/**
 Cell descriptions for a Travel Only Home layout
 */
+ (NSMutableArray *)descriptionsForTravelOnlyWithRail:(BOOL)railEnabled
{
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    
    [tmp addObject:[self tripsCellDescription]];
    [tmp addObject:[self flightCellDescription]];
    [tmp addObject:[self carCellDescription]];
    [tmp addObject:[self hotelCellDescription]];
    if (railEnabled) {
        [tmp addObject:[self railCellDescription]];
    }
	[self addTravelRequestCellTo:&tmp];
	
    return tmp;
}

/**
 Cell descriptions for an Approval Only Home layout
 */
+ (NSMutableArray *)descriptionsForApprovalOnly
{
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    
    [tmp addObject:[self approvalsCellDescription]];
	[self addTravelRequestCellTo:&tmp];
	
    return tmp;
}

/**
 Cell descriptions for Expense and Approval Only Home layout
 */
+ (NSMutableArray *)descriptionsForExpenseAndApprovalOnly
{
    // expense only user has expense and expense reports only
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    
    [tmp addObject:[self expensesCellDescription]];
    [tmp addObject:[self expenseReportsCellDescription]];
    [tmp addObject:[self approvalsCellDescription]];
	[self addTravelRequestCellTo:&tmp];

    return tmp;
}

/**
 Cell descriptions for Expense and Approval Only Home layout
 */
+ (NSMutableArray *)descriptionsForExpenseAndTravelOnly
{
    // expense only user has expense and expense reports only
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    
    [tmp addObject:[self tripsCellDescription]];
    [tmp addObject:[self expensesCellDescription]];
    [tmp addObject:[self expenseReportsCellDescription]];
	[self addTravelRequestCellTo:&tmp];

    return tmp;
}

/**
 Cell descriptions for a Travel and Approval Home layout
 */
+ (NSMutableArray *)descriptionsForTravelandApprovalWithRail:(BOOL)railEnabled
{
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    
    [tmp addObject:[self tripsCellDescription]];
    [tmp addObject:[self approvalsCellDescription]];
    [tmp addObject:[self flightCellDescription]];
    [tmp addObject:[self carCellDescription]];
    [tmp addObject:[self hotelCellDescription]];
    if (railEnabled) {
        [tmp addObject:[self railCellDescription]];
    }
	[self addTravelRequestCellTo:&tmp];
	
    return tmp;
}

#pragma mark -
#pragma mark Cell setup methods

+ (HomeTableViewCellDescription *) tripsCellDescription
{
    HomeTableViewCellDescription *cellDescription = [[HomeTableViewCellDescription alloc] init];
    cellDescription.cellType = TripsHomeCell;
    cellDescription.icon = @"home_icon_trip";
    cellDescription.label = [Localizer getLocalizedText:@"Trips"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"View your trips"];
    cellDescription.count = 0;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeTableViewCellDescription *) expensesCellDescription
{
    HomeTableViewCellDescription *cellDescription = [[HomeTableViewCellDescription alloc] init];
    cellDescription.cellType = ExpensesHomeCell;
    cellDescription.icon = @"home_icon_expense";
    cellDescription.label = [Localizer getLocalizedText:@"Expenses"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"List of your expenses"];
    cellDescription.count = 0;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeTableViewCellDescription *) expenseReportsCellDescription
{
    HomeTableViewCellDescription *cellDescription = [[HomeTableViewCellDescription alloc] init];
    cellDescription.cellType = ExpenseReportsHomeCell;
    cellDescription.icon = @"home_icon_report";
    cellDescription.label = [Localizer getLocalizedText:@"EXPENSE_REPORTS"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"EXPENSE_REPORTS_NEG_TEXT"];
    cellDescription.count = 0;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeTableViewCellDescription *) travelRequestsCellDescription
{
    HomeTableViewCellDescription *cellDescription = [[HomeTableViewCellDescription alloc] init];
    cellDescription.cellType = TravelRequestHomeCell;
    cellDescription.icon = @"icongray_request";
    cellDescription.label = [@"Requests" localize];
    cellDescription.sublabel = [@"ManageYourAuthorizations" localize];
    cellDescription.count = 0;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeTableViewCellDescription *) approvalsCellDescription
{
    HomeTableViewCellDescription *cellDescription = [[HomeTableViewCellDescription alloc] init];
    cellDescription.cellType = ApprovalsHomeCell;
    cellDescription.icon = @"home_icon_approval";
    cellDescription.label = [Localizer getLocalizedText:@"Approvals"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"APPROVALS_NEG_TEXT"];
    cellDescription.count = 0;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeTableViewCellDescription *) flightCellDescription
{
    HomeTableViewCellDescription *cellDescription = [[HomeTableViewCellDescription alloc] init];
    cellDescription.cellType = FlightBookingHomeCell;
    cellDescription.icon = @"home_icon_flight";
    cellDescription.label = [Localizer getLocalizedText:@"Flights"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"Book flights"];
    cellDescription.count = 0;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeTableViewCellDescription *) carCellDescription
{
    HomeTableViewCellDescription *cellDescription = [[HomeTableViewCellDescription alloc] init];
    cellDescription.cellType = CarBookingHomeCell;
    cellDescription.icon = @"home_icon_car";
    cellDescription.label = [Localizer getLocalizedText:@"Cars"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"Rent a car"];
    cellDescription.count = 0;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeTableViewCellDescription *) hotelCellDescription
{
    HomeTableViewCellDescription *cellDescription = [[HomeTableViewCellDescription alloc] init];
    cellDescription.cellType = HotelBookingHomeCell;
    cellDescription.icon = @"home_icon_hotel";
    cellDescription.label = [Localizer getLocalizedText:@"Hotels"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"Discover great hotels"];
    cellDescription.count = 0;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeTableViewCellDescription *) railCellDescription
{
    HomeTableViewCellDescription *cellDescription = [[HomeTableViewCellDescription alloc] init];
    cellDescription.cellType = RailBookingHomeCell;
    cellDescription.icon = @"home_icon_rail";
    cellDescription.label = [Localizer getLocalizedText:@"Rail"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"Book a train"];
    cellDescription.count = 0;
    cellDescription.disabled = NO;
    return cellDescription;
}


@end
