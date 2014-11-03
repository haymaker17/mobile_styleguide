//
//  HomeCollectionViewCellDescriptionFactory.m
//  ConcurHomeCollectionView
//
//  Created by ernest cho on 11/19/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "HomeCollectionViewCellDescriptionFactory.h"
#import "HomeCollectionViewCellDescription.h"

@implementation HomeCollectionViewCellDescriptionFactory

#pragma mark -
#pragma mark Cell description arrays

/**
 Cell descriptions for the default Home layout
 
 All cells are the the same.
 */
+ (NSMutableArray *)descriptionsForDefault
{
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    [tmp addObject:[self tripsCellDescriptionPortrait:HomeCellDefaultPortrait landscape:HomeCellDefaultLandscape]];

    [tmp addObject:[self expensesCellDescriptionPortrait:HomeCellDefaultPortrait landscape:HomeCellDefaultLandscape]];

    [tmp addObject:[self expenseReportsCellDescriptionPortrait:HomeCellDefaultPortrait landscape:HomeCellDefaultLandscape]];

    [tmp addObject:[self approvalsCellDescriptionPortrait:HomeCellDefaultPortrait landscape:HomeCellDefaultLandscape]];
    return tmp;
}

/**
 Cell descriptions for an Expense Only Home layout
 
 All cells are the same.
 */
+ (NSMutableArray *)descriptionsForExpenseOnly
{
    // expense only user has expense and expense reports only
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    [tmp addObject:[self expensesCellDescriptionPortrait:HomeCellExpenseOnlyPortrait landscape:HomeCellExpenseOnlyLandscape]];

    [tmp addObject:[self expenseReportsCellDescriptionPortrait:HomeCellExpenseOnlyPortrait landscape:HomeCellExpenseOnlyLandscape]];
    return tmp;
}

/**
 Cell descriptions for a Travel Only Home layout
 
 All cells are the same, except for Trips.
 */
+ (NSMutableArray *)descriptionsForTravelOnlyWithRail:(BOOL)railEnabled
{
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    [tmp addObject:[self tripsCellDescriptionPortrait:HomeCellTravelOnlyTripsPortrait landscape:HomeCellTravelOnlyTripsLandscape]];

    [tmp addObject:[self flightCellDescriptionPortrait:HomeCellTravelOnlyBookingPortrait landscape:HomeCellTravelOnlyBookingLandscape]];

    [tmp addObject:[self carCellDescriptionPortrait:HomeCellTravelOnlyBookingPortrait landscape:HomeCellTravelOnlyBookingLandscape]];

    [tmp addObject:[self hotelCellDescriptionPortrait:HomeCellTravelOnlyBookingPortrait landscape:HomeCellTravelOnlyBookingLandscape]];

    if (railEnabled) {
        [tmp addObject:[self railCellDescriptionPortrait:HomeCellTravelOnlyBookingPortrait landscape:HomeCellTravelOnlyBookingLandscape]];
    }
    return tmp;
}

/**
 Cell descriptions for an Approval Only Home layout
 
 All cells are the same.
 */
+ (NSMutableArray *)descriptionsForApprovalOnly
{
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    [tmp addObject:[self approvalsCellDescriptionPortrait:HomeCellApprovalOnlyPortrait landscape:HomeCellApprovalOnlyLandscape]];
    return tmp;
}

/**
 Cell descriptions for Expense and Approval Only Home layout
 
 All cells are the same and reuses the default cells.  Except for the approvals, which uses the ExpenseAndTravelOnly Trips cell.
 */
+ (NSMutableArray *)descriptionsForExpenseAndApprovalOnly
{
    // expense only user has expense and expense reports only
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    [tmp addObject:[self expensesCellDescriptionPortrait:HomeCellExpenseAndTravelOnlyTripsPortrait landscape:HomeCellExpenseAndTravelOnlyTripsLandscape]];
    [tmp addObject:[self expenseReportsCellDescriptionPortrait:HomeCellDefaultPortrait landscape:HomeCellDefaultLandscape]];
    [tmp addObject:[self approvalsCellDescriptionPortrait:HomeCellDefaultPortrait landscape:HomeCellDefaultLandscape]];
    
    return tmp;
}

/**
 Cell descriptions for Expense and Approval Only Home layout
 
 All cells are the same and reuses the default cells.  Except for the trips.
 */
+ (NSMutableArray *)descriptionsForExpenseAndTravelOnly
{
    // expense only user has expense and expense reports only
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    [tmp addObject:[self tripsCellDescriptionPortrait:HomeCellExpenseAndTravelOnlyTripsPortrait landscape:HomeCellExpenseAndTravelOnlyTripsLandscape]];
    [tmp addObject:[self expensesCellDescriptionPortrait:HomeCellDefaultPortrait landscape:HomeCellDefaultLandscape]];
    [tmp addObject:[self expenseReportsCellDescriptionPortrait:HomeCellDefaultPortrait landscape:HomeCellDefaultLandscape]];
    
    return tmp;
}

/**
 Cell descriptions for a Travel and Approval Home layout
 
 All cells are the same and reuses the TravelOnly cells.  Except for trips and approvals in landscape.
 */
+ (NSMutableArray *)descriptionsForTravelandApprovalWithRail:(BOOL)railEnabled
{
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    [tmp addObject:[self tripsCellDescriptionPortrait:HomeCellTravelOnlyBookingPortrait landscape:HomeCellTravelAndApprovalLandscape]];

    [tmp addObject:[self approvalsCellDescriptionPortrait:HomeCellTravelOnlyBookingPortrait landscape:HomeCellTravelAndApprovalLandscape]];

    [tmp addObject:[self flightCellDescriptionPortrait:HomeCellTravelOnlyBookingPortrait landscape:HomeCellTravelOnlyBookingLandscape]];
    
    [tmp addObject:[self carCellDescriptionPortrait:HomeCellTravelOnlyBookingPortrait landscape:HomeCellTravelOnlyBookingLandscape]];

    [tmp addObject:[self hotelCellDescriptionPortrait:HomeCellTravelOnlyBookingPortrait landscape:HomeCellTravelOnlyBookingLandscape]];
    
    if (railEnabled) {
        [tmp addObject:[self railCellDescriptionPortrait:HomeCellTravelOnlyBookingPortrait landscape:HomeCellTravelOnlyBookingLandscape]];
    }

    return tmp;
}

/**
 Cell descriptions for a Gov only layout


 */
+ (NSMutableArray *)descriptionsForGovOnly
{
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    [tmp addObject:[self tripsCellDescriptionPortrait:HomeCellDefaultPortrait landscape:HomeCellDefaultLandscape]];

    [tmp addObject:[self govExpensesCellDescriptionPortrait:HomeCellDefaultPortrait landscape:HomeCellDefaultLandscape]];

    [tmp addObject:[self authCellDescriptionPortrait:HomeCellGovOnlyDocumentsPortrait landscape:HomeCellGovOnlyDocumentsLandscape]];

    [tmp addObject:[self vchCellDescriptionPortrait:HomeCellGovOnlyDocumentsPortrait landscape:HomeCellGovOnlyDocumentsLandscape]];

    [tmp addObject:[self stampDocCellDescriptionPortrait:HomeCellGovOnlyDocumentsPortrait landscape:HomeCellGovOnlyDocumentsLandscape]];
    
    return tmp;
}


#pragma mark -
#pragma mark Cell setup methods

+ (HomeCollectionViewCellDescription *) tripsCellDescriptionPortrait:(HomeCollectionViewCellStyle)portrait landscape:(HomeCollectionViewCellStyle)landscape
{
    HomeCollectionViewCellDescription *cellDescription = [[HomeCollectionViewCellDescription alloc] init];
    cellDescription.cellType = TripsHomeCell;
    cellDescription.icon = @"home_icon_trip";
    cellDescription.label = [Localizer getLocalizedText:@"Trips"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"View your trips"];
    cellDescription.count = 0;
    cellDescription.cellStylePortrait = portrait;
    cellDescription.cellStyleLandscape = landscape;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeCollectionViewCellDescription *) expensesCellDescriptionPortrait:(HomeCollectionViewCellStyle)portrait landscape:(HomeCollectionViewCellStyle)landscape
{
    HomeCollectionViewCellDescription *cellDescription = [[HomeCollectionViewCellDescription alloc] init];
    cellDescription.cellType = ExpensesHomeCell;
    cellDescription.icon = @"home_icon_expense";
    cellDescription.label = [Localizer getLocalizedText:@"Expenses"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"List of your expenses"];
    cellDescription.count = 0;
    cellDescription.cellStylePortrait = portrait;
    cellDescription.cellStyleLandscape = landscape;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeCollectionViewCellDescription *) expenseReportsCellDescriptionPortrait:(HomeCollectionViewCellStyle)portrait landscape:(HomeCollectionViewCellStyle)landscape
{
    HomeCollectionViewCellDescription *cellDescription = [[HomeCollectionViewCellDescription alloc] init];
    cellDescription.cellType = ExpenseReportsHomeCell;
    cellDescription.icon = @"home_icon_report";
    cellDescription.label = [Localizer getLocalizedText:@"EXPENSE_REPORTS"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"EXPENSE_REPORTS_NEG_TEXT"];
    cellDescription.count = 0;
    cellDescription.cellStylePortrait = portrait;
    cellDescription.cellStyleLandscape = landscape;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeCollectionViewCellDescription *) approvalsCellDescriptionPortrait:(HomeCollectionViewCellStyle)portrait landscape:(HomeCollectionViewCellStyle)landscape
{
    HomeCollectionViewCellDescription *cellDescription = [[HomeCollectionViewCellDescription alloc] init];
    cellDescription.cellType = ApprovalsHomeCell;
    cellDescription.icon = @"home_icon_approval";
    cellDescription.label = [Localizer getLocalizedText:@"Approvals"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"APPROVALS_NEG_TEXT"];
    cellDescription.count = 0;
    cellDescription.cellStylePortrait = portrait;
    cellDescription.cellStyleLandscape = landscape;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeCollectionViewCellDescription *) flightCellDescriptionPortrait:(HomeCollectionViewCellStyle)portrait landscape:(HomeCollectionViewCellStyle)landscape
{
    HomeCollectionViewCellDescription *cellDescription = [[HomeCollectionViewCellDescription alloc] init];
    cellDescription.cellType = FlightBookingHomeCell;
    cellDescription.icon = @"home_icon_flight";
    cellDescription.label = [Localizer getLocalizedText:@"Flights"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"Book flights"];
    cellDescription.count = 0;
    cellDescription.cellStylePortrait = portrait;
    cellDescription.cellStyleLandscape = landscape;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeCollectionViewCellDescription *) carCellDescriptionPortrait:(HomeCollectionViewCellStyle)portrait landscape:(HomeCollectionViewCellStyle)landscape
{
    HomeCollectionViewCellDescription *cellDescription = [[HomeCollectionViewCellDescription alloc] init];
    cellDescription.cellType = CarBookingHomeCell;
    cellDescription.icon = @"home_icon_car";
    cellDescription.label = [Localizer getLocalizedText:@"Cars"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"Rent a car"];
    cellDescription.count = 0;
    cellDescription.cellStylePortrait = portrait;
    cellDescription.cellStyleLandscape = landscape;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeCollectionViewCellDescription *) hotelCellDescriptionPortrait:(HomeCollectionViewCellStyle)portrait landscape:(HomeCollectionViewCellStyle)landscape
{
    HomeCollectionViewCellDescription *cellDescription = [[HomeCollectionViewCellDescription alloc] init];
    cellDescription.cellType = HotelBookingHomeCell;
    cellDescription.icon = @"home_icon_hotel";
    cellDescription.label = [Localizer getLocalizedText:@"Hotels"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"Discover great hotels"];
    cellDescription.count = 0;
    cellDescription.cellStylePortrait = portrait;
    cellDescription.cellStyleLandscape = landscape;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeCollectionViewCellDescription *) railCellDescriptionPortrait:(HomeCollectionViewCellStyle)portrait landscape:(HomeCollectionViewCellStyle)landscape
{
    HomeCollectionViewCellDescription *cellDescription = [[HomeCollectionViewCellDescription alloc] init];
    cellDescription.cellType = RailBookingHomeCell;
    cellDescription.icon = @"home_icon_rail";
    cellDescription.label = [Localizer getLocalizedText:@"Rail"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"Book a train"];
    cellDescription.count = 0;
    cellDescription.cellStylePortrait = portrait;
    cellDescription.cellStyleLandscape = landscape;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeCollectionViewCellDescription *) govExpensesCellDescriptionPortrait:(HomeCollectionViewCellStyle)portrait landscape:(HomeCollectionViewCellStyle)landscape
{
    HomeCollectionViewCellDescription *cellDescription = [[HomeCollectionViewCellDescription alloc] init];
    cellDescription.cellType = ExpensesHomeCell;
    cellDescription.icon = @"home_icon_expense";
    cellDescription.label = [Localizer getLocalizedText:@"Expenses"];
   cellDescription.sublabel = [Localizer getLocalizedText:@"View unapplied expenses"];
    cellDescription.count = 0;
    cellDescription.cellStylePortrait = portrait;
    cellDescription.cellStyleLandscape = landscape;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeCollectionViewCellDescription *) authCellDescriptionPortrait:(HomeCollectionViewCellStyle)portrait landscape:(HomeCollectionViewCellStyle)landscape
{
    HomeCollectionViewCellDescription *cellDescription = [[HomeCollectionViewCellDescription alloc] init];
    cellDescription.cellType = GovAuthorizationHomeCell;
    cellDescription.icon = @"icon_authorizations";
    cellDescription.label = [Localizer getLocalizedText:@"Authorizations"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"View and update authorizations"];
    cellDescription.count = 0;
    cellDescription.cellStylePortrait = portrait;
    cellDescription.cellStyleLandscape = landscape;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeCollectionViewCellDescription *) vchCellDescriptionPortrait:(HomeCollectionViewCellStyle)portrait landscape:(HomeCollectionViewCellStyle)landscape
{
    HomeCollectionViewCellDescription *cellDescription = [[HomeCollectionViewCellDescription alloc] init];
    cellDescription.cellType = GovVoucherHomeCell;
    cellDescription.icon = @"icon_vouchers";
    cellDescription.label = [Localizer getLocalizedText:@"Vouchers"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"View, create and update vouchers"];
    cellDescription.count = 0;
    cellDescription.cellStylePortrait = portrait;
    cellDescription.cellStyleLandscape = landscape;
    cellDescription.disabled = NO;
    return cellDescription;
}

+ (HomeCollectionViewCellDescription *) stampDocCellDescriptionPortrait:(HomeCollectionViewCellStyle)portrait landscape:(HomeCollectionViewCellStyle)landscape
{
    HomeCollectionViewCellDescription *cellDescription = [[HomeCollectionViewCellDescription alloc] init];
    cellDescription.cellType = GovStampDocumentCell;
    cellDescription.icon = @"icon_stamp_documents";
    cellDescription.topLabel = [Localizer getLocalizedText:@"Stamp"];
    cellDescription.label = [Localizer getLocalizedText:@"Documents"];
    cellDescription.sublabel = [Localizer getLocalizedText:@"Approve authorizations and vouchers"];
    cellDescription.count = 0;
    cellDescription.cellStylePortrait = portrait;
    cellDescription.cellStyleLandscape = landscape;
    cellDescription.disabled = NO;
    return cellDescription;
}

@end