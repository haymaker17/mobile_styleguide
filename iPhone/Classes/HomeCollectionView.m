//
//  HomeCollectionView.m
//
//  Created by ernest cho on 11/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "HomeCollectionView.h"
#import "HomeCollectionViewCell.h"
#import "HomeCollectionViewCellDescription.h"
#import "HomeCollectionViewCellDescriptionFactory.h"

@interface HomeCollectionView()
@property (nonatomic, readwrite, strong) NSMutableArray *cellDescriptions;
@property (nonatomic, readwrite, assign) BOOL isPortrait;
@property (nonatomic, readwrite, strong) UIRefreshControl *refreshControl;
@end

@implementation HomeCollectionView

/**
 This UIView is essentially a controller.

 This allows us to instantiate it via Interface Builder.
 */
- (id)initWithCoder:(NSCoder *)aDecoder
{
    if ((self = [super initWithCoder:aDecoder])) {
        [self sharedInit];
    }
    return self;
}

// Not used. Here for completeness, but untested.
- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self sharedInit];
    }
    return self;
}

/**
 Shared init behavior.  Loads from nib and registers the proper cell class.
 */
- (void)sharedInit
{
    [[NSBundle mainBundle] loadNibNamed:@"HomeCollectionView" owner:self options:nil];
    [self addSubview:self.topLevelSubView];
    self.isPortrait = YES;

    // enable pull to refresh on this view
    [self setupPullToRefresh];

    // register all the UICollectionViewCell classes.
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellDefaultPortrait" bundle:nil] forCellWithReuseIdentifier:@"HomeCellDefaultPortrait"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellDefaultLandscape" bundle:nil] forCellWithReuseIdentifier:@"HomeCellDefaultLandscape"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellTravelOnlyTripsPortrait" bundle:nil] forCellWithReuseIdentifier:@"HomeCellTravelOnlyTripsPortrait"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellTravelOnlyTripsLandscape" bundle:nil] forCellWithReuseIdentifier:@"HomeCellTravelOnlyTripsLandscape"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellTravelOnlyBookingPortrait" bundle:nil] forCellWithReuseIdentifier:@"HomeCellTravelOnlyBookingPortrait"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellTravelOnlyBookingLandscape" bundle:nil] forCellWithReuseIdentifier:@"HomeCellTravelOnlyBookingLandscape"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellExpenseOnlyPortrait" bundle:nil] forCellWithReuseIdentifier:@"HomeCellExpenseOnlyPortrait"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellExpenseOnlyLandscape" bundle:nil] forCellWithReuseIdentifier:@"HomeCellExpenseOnlyLandscape"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellApprovalOnlyPortrait" bundle:nil] forCellWithReuseIdentifier:@"HomeCellApprovalOnlyPortrait"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellApprovalOnlyLandscape" bundle:nil] forCellWithReuseIdentifier:@"HomeCellApprovalOnlyLandscape"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellTravelAndApprovalLandscape" bundle:nil] forCellWithReuseIdentifier:@"HomeCellTravelAndApprovalLandscape"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellExpenseAndTravelOnlyTripsPortrait" bundle:nil] forCellWithReuseIdentifier:@"HomeCellExpenseAndTravelOnlyTripsPortrait"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellExpenseAndTravelOnlyTripsLandscape" bundle:nil] forCellWithReuseIdentifier:@"HomeCellExpenseAndTravelOnlyTripsLandscape"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellGovOnlyDocumentsLandscape" bundle:nil] forCellWithReuseIdentifier:@"HomeCellGovOnlyDocumentsLandscape"];
    [self.collectionView registerNib:[UINib nibWithNibName:@"HomeCellGovOnlyDocumentsPortrait" bundle:nil] forCellWithReuseIdentifier:@"HomeCellGovOnlyDocumentsPortrait"];

    // default to all roles
    [self setupDefaultCellData];
}

#pragma mark -
#pragma mark Pull to refresh support.  Only works if the delegate is set

/**
 Adds pull to refresh to this collection view.  I think this is a crutch for poor design. -Ernest
 */
- (void)setupPullToRefresh
{
    self.refreshControl = [[UIRefreshControl alloc] init];
    [self.refreshControl addTarget:self action:@selector(refreshDelegate) forControlEvents:UIControlEventValueChanged];
    [self.collectionView addSubview:self.refreshControl];

    // this forces it to always scroll for at least the refresh
    self.collectionView.alwaysBounceVertical = YES;
}

/**
 Asks the delegate to refresh itself
 */
- (void)refreshDelegate
{
    if (self.delegate) {
        // force iPadHome9VC to reload
        [self.delegate refreshView:self.refreshControl];
    }
    else if (self.govDelegate){
        [self.govDelegate refreshView:self.refreshControl];
    }
}

#pragma mark -
#pragma mark Orientation management

/**
 Let view know that it should switch to landscape cells
 */
- (void)switchToLandscape
{
    self.isPortrait = NO;
    [self.collectionView reloadData];
}

/**
 Let view know that it should switch to portrait cells
 */
- (void)switchToPortrait
{
    self.isPortrait = YES;
    [self.collectionView reloadData];
}

#pragma mark -
#pragma mark Set overall layout style

/**
 Switch layout to default
 */
- (void)switchLayoutToDefault
{
    [self setupDefaultCellData];
    [self.collectionView reloadData];
}

/**
 Switch layout to travel only
 */
- (void)switchLayoutToTravelOnly
{
    [self setupTravelOnlyCellData];
    [self.collectionView reloadData];
}

/**
 Switch layout to expense only
 */
- (void)switchLayoutToExpenseOnly
{
    [self setupExpenseOnlyCellData];
    [self.collectionView reloadData];
}

/**
 Switch layout to approval only
 */
- (void)switchLayoutToApprovalOnly
{
    [self setupApprovalOnlyCellData];
    [self.collectionView reloadData];
}

/**
 Switch layout to expense and approval only
 */
- (void)switchLayoutToExpenseAndApprovalOnly
{
    [self setupExpenseAndApprovalOnlyCellData];
    [self.collectionView reloadData];
}

/**
 Switch layout to expense and travel only
 */
- (void)switchLayoutToExpenseAndTravelOnly
{
    [self setupExpenseAndTravelOnlyCellData];
    [self.collectionView reloadData];
}

/**
 Switch layout to approval only
 */
- (void)switchLayoutToTravelAndApproval
{
    [self setupTravelAndApprovalCellData];
    [self.collectionView reloadData];
}

/**
 Switch layout to Gov only
 */
- (void)switchLayoutToGovOnly
{
    [self setupGovCellData];
    [self.collectionView reloadData];
}


#pragma mark - 
#pragma mark Disable cells on home

/**
 Disables the Flight booking cell
 */
- (void)disableFlightBooking
{
    [self disableCellWithType:FlightBookingHomeCell];
}

/**
 Disables the Hotel booking cell
 */
- (void)disableHotelBooking
{
    [self disableCellWithType:HotelBookingHomeCell];
}

/**
 Disables the Car booking cell
 */
- (void)disableCarBooking
{
    [self disableCellWithType:CarBookingHomeCell];
}

/**
 Disables the Rail booking cell
 */
- (void)disableRailBooking
{
    [self disableCellWithType:RailBookingHomeCell];
}

/**
 Disables the Expenses cell
 */
- (void)disableExpenses
{
    [self disableCellWithType:ExpensesHomeCell];
}

/**
 Disables the Expense Reports cell
 */
- (void)disableExpenseReports
{
    [self disableCellWithType:ExpenseReportsHomeCell];
}

/**
 Disables the Approval cell
 */
- (void)disableApprovals
{
    [self disableCellWithType:ApprovalsHomeCell];
}

/**
 Disables cell with a type
 */
- (void)disableCellWithType:(HomeCollectionViewCellType)cellType
{
    for (HomeCollectionViewCellDescription *description in self.cellDescriptions) {
        if (description.cellType == cellType) {
            description.disabled = YES;
        }
    }
}

#pragma mark -
#pragma mark Set Badge count on home

/**
 Sets the expenses count on home
 */
- (void)setExpensesCount:(NSNumber *)count
{
    [self setBadgeCount:count forCellType:ExpensesHomeCell];
}

/**
 Sets the expense reports count on home
 */
- (void)setExpenseReportsCount:(NSNumber *)count
{
    [self setBadgeCount:count forCellType:ExpenseReportsHomeCell];
}

/**
 Sets the approval count on home
 */
- (void)setApprovalCount:(NSNumber *)count
{
    [self setBadgeCount:count forCellType:ApprovalsHomeCell];
}

/**
 Sets the upcoming trips count on home
 */
- (void)setTripsCount:(NSNumber *)count
{
    [self setBadgeCount:count forCellType:TripsHomeCell];
}

/**
 Sets the badge count for a given cell type
 */
- (void)setBadgeCount:(NSNumber *)count forCellType:(HomeCollectionViewCellType)cellType
{
    for (HomeCollectionViewCellDescription *description in self.cellDescriptions) {
        if (description.cellType == cellType) {
            description.count = count;
        }
    }
    [self.collectionView reloadData];
}

#pragma mark -
#pragma mark Cell array setup

/**
 Sets up the Home screen with all cells
 */
- (void)setupDefaultCellData
{
    self.cellDescriptions = [HomeCollectionViewCellDescriptionFactory descriptionsForDefault];
}

/**
 Sets up the Home screen with only trip cells
 */
- (void)setupTravelOnlyCellData
{
    self.cellDescriptions = [HomeCollectionViewCellDescriptionFactory descriptionsForTravelOnlyWithRail:[[ExSystem sharedInstance] canBookRail]];
}

/**
 Sets up the Home screen with only expense cells
 */
- (void)setupExpenseOnlyCellData
{
    self.cellDescriptions = [HomeCollectionViewCellDescriptionFactory descriptionsForExpenseOnly];
}

/**
 Sets up the Home screen with only approval cells
 */
- (void)setupApprovalOnlyCellData
{
    self.cellDescriptions = [HomeCollectionViewCellDescriptionFactory descriptionsForApprovalOnly];
}

/**
 Sets up the Home screen with only expense and approval cells
 */
- (void)setupExpenseAndApprovalOnlyCellData
{
    self.cellDescriptions = [HomeCollectionViewCellDescriptionFactory descriptionsForExpenseAndApprovalOnly];
}

/**
 Sets up the Home screen with only trip cells
 */
- (void)setupExpenseAndTravelOnlyCellData
{
    self.cellDescriptions = [HomeCollectionViewCellDescriptionFactory descriptionsForExpenseAndTravelOnly];
}

/**
 Sets up the Home screen with only approval cells
 */
- (void)setupTravelAndApprovalCellData
{
    self.cellDescriptions = [HomeCollectionViewCellDescriptionFactory descriptionsForTravelandApprovalWithRail:[[ExSystem sharedInstance] canBookRail]];
}

/**
 Sets up the Home screen with only gov cells
 */
- (void)setupGovCellData
{
    self.cellDescriptions = [HomeCollectionViewCellDescriptionFactory descriptionsForGovOnly];
}


#pragma mark -
#pragma mark UICollectionViewDataSource

/**
 There's only one section in the current design
 */
- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView
{
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    return [self.cellDescriptions count];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    HomeCollectionViewCell *cell = nil;

    if ([self.cellDescriptions count] > indexPath.row) {
        HomeCollectionViewCellDescription *tmp = (HomeCollectionViewCellDescription *)self.cellDescriptions[indexPath.row];

        // get the cell style for the current orientation
        HomeCollectionViewCellStyle style = tmp.cellStylePortrait;
        if (!self.isPortrait) {
            style = tmp.cellStyleLandscape;
        }

        // make sure we dequeue the correct type of cell layout.  Any of them should work, but we want layout to be pixel perfect.
        switch (style) {
            case HomeCellDefaultPortrait:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellDefaultPortrait" forIndexPath:indexPath];
                break;
            case HomeCellDefaultLandscape:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellDefaultLandscape" forIndexPath:indexPath];
                break;
            case HomeCellTravelOnlyTripsPortrait:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellTravelOnlyTripsPortrait" forIndexPath:indexPath];
                break;
            case HomeCellTravelOnlyTripsLandscape:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellTravelOnlyTripsLandscape" forIndexPath:indexPath];
                break;
            case HomeCellTravelOnlyBookingPortrait:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellTravelOnlyBookingPortrait" forIndexPath:indexPath];
                break;
            case HomeCellTravelOnlyBookingLandscape:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellTravelOnlyBookingLandscape" forIndexPath:indexPath];
                break;
            case HomeCellExpenseOnlyPortrait:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellExpenseOnlyPortrait" forIndexPath:indexPath];
                break;
            case HomeCellExpenseOnlyLandscape:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellExpenseOnlyLandscape" forIndexPath:indexPath];
                break;
            case HomeCellApprovalOnlyLandscape:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellApprovalOnlyLandscape" forIndexPath:indexPath];
                break;
            case HomeCellApprovalOnlyPortrait:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellApprovalOnlyPortrait" forIndexPath:indexPath];
                break;
            case HomeCellTravelAndApprovalLandscape:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellTravelAndApprovalLandscape" forIndexPath:indexPath];
                break;
            case HomeCellExpenseAndTravelOnlyTripsPortrait:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellExpenseAndTravelOnlyTripsPortrait" forIndexPath:indexPath];
                break;
            case HomeCellExpenseAndTravelOnlyTripsLandscape:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellExpenseAndTravelOnlyTripsLandscape" forIndexPath:indexPath];
                break;
            case HomeCellGovOnlyDocumentsPortrait:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellGovOnlyDocumentsPortrait" forIndexPath:indexPath];
                break;
            case HomeCellGovOnlyDocumentsLandscape:
                cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellGovOnlyDocumentsLandscape" forIndexPath:indexPath];
                break;
        }

        // setup the cell here!
        cell.topLabel.text = tmp.topLabel;
        cell.label.text = tmp.label;
        cell.sublabel.text = tmp.sublabel;
        [cell.icon setImage:[UIImage imageNamed:tmp.icon]];
        [cell.badge updateBadgeCount:tmp.count];

        // visual indicator that cell is disabled.
        if (tmp.disabled) {
            [self updateCell:cell alpha:0.5];

            // hide the badge when the cell is disabled
            [cell.badge updateBadgeCount:0];
        } else {
            [self updateCell:cell alpha:1];
        }
    }

    if (cell == nil) {
        // the cellStyle isn't handled or the indexPath makes no sense.  This is very bad. :(
        cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"HomeCellDefaultPortrait" forIndexPath:indexPath];
    }

    return cell;
}

/**
 Sets the alpha for all parts of the cell.

 Setting the full cell alpha is unreliable on UICollectionViewCells.  It resets on rotation.
 */
- (void)updateCell:(HomeCollectionViewCell *)cell alpha:(float)alpha
{
    cell.label.alpha = alpha;
    cell.sublabel.alpha = alpha;
    cell.icon.alpha = alpha;
    cell.badge.alpha = alpha;
    cell.backgroundColor = [UIColor colorWithRed:255 green:255 blue:255 alpha:alpha];
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath
{
    if ([self.cellDescriptions count] > indexPath.row) {
        HomeCollectionViewCellDescription *tmp = (HomeCollectionViewCellDescription *)self.cellDescriptions[indexPath.row];

        // notify user that the feature is disabled
        if (tmp.disabled) {
            [self handleFeatureIsDisabled];
            return;
        }

        // handle various features
        switch (tmp.cellType) {
            case TripsHomeCell:
                [self handleTripsHomeCell];
                break;
            case FlightBookingHomeCell:
                [self handleFlightBookingHomeCell];
                break;
            case HotelBookingHomeCell:
                [self handleHotelBookingHomeCell];
                break;
            case CarBookingHomeCell:
                [self handleCarBookingHomeCell];
                break;
            case RailBookingHomeCell:
                [self handleRailBookingHomeCell];
                break;
            case ExpensesHomeCell:
                [self handleExpensesHomeCell];
                break;
            case ExpenseReportsHomeCell:
                [self handleExpenseReportsHomeCell];
                break;
            case ApprovalsHomeCell:
                [self handleApprovalsHomeCell];
                break;
            case GovAuthorizationHomeCell:
                [self handleAuthorizatiosHomeCell];
                break;
            case GovVoucherHomeCell:
                [self handleVouchersHomeCell];
                break;
            case GovStampDocumentCell:
                [self handleStampDocumentsHomeCell];
                break;
        }
    }
}

#pragma mark -
#pragma mark Handle cell behavior
- (void)handleFeatureIsDisabled
{
    [self showModuleDisabledMessage];
}

- (void)handleTripsHomeCell
{
    [self.delegate tripsButtonPressed];
    [self.govDelegate tripsButtonPressed:self];
}

- (void)handleFlightBookingHomeCell
{
    [self.delegate btnBookFlightsPressed:self];
    [self.govDelegate btnBookFlightsPressed:self];
}

- (void)handleHotelBookingHomeCell
{
    [self.delegate btnBookHotelPressed:self];
    [self.govDelegate btnBookHotelPressed:self];
}

- (void)handleCarBookingHomeCell
{
    [self.delegate btnBookCarPressed:self];
    [self.govDelegate btnBookCarPressed:self];
}

- (void)handleRailBookingHomeCell
{
    [self.delegate btnBookRailPressed:self];
    [self.govDelegate btnBookRailPressed:self];
}

- (void)handleExpensesHomeCell
{
    [self.delegate btnExpensesPressed:self];
    [self.govDelegate btnExpensesPressed:self];
}

- (void)handleExpenseReportsHomeCell
{
    [self.delegate btnReportsPressed:self];
}

- (void)handleApprovalsHomeCell
{
    [self.delegate btnApprovalsPressed:self];
}

- (void)handleAuthorizatiosHomeCell
{
    [self.govDelegate btnAuthorizationsPressed:self];
}

- (void)handleVouchersHomeCell
{
    [self.govDelegate btnVouchersPressed:self];
}

- (void)handleStampDocumentsHomeCell
{
    [self.govDelegate btnStampDocumentsPressed:self];
}

/**
 Shows a simple alert message box.  
 
 Used to test behavior handling.
 */
- (void)showTestAlertWithMessage:(NSString *)message
{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:message message:@"" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
    [alert show];
}

/**
 This is copied from iPadHome9VC.  The official disabled message!
 */
- (void)showModuleDisabledMessage
{
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:[Localizer getLocalizedText:@"MODULE_DISABLED_ALERT_TITLE"]
                          message:[Localizer getLocalizedText:@"MODULE_DISABLED_ALERT_TEXT"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                          otherButtonTitles:nil];
    [alert show];
}

#pragma mark -
#pragma mark UICollectionViewFlowLayoutDelegate

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    if ([self.cellDescriptions count] > indexPath.row) {
        HomeCollectionViewCellDescription *tmp = (HomeCollectionViewCellDescription *)self.cellDescriptions[indexPath.row];

        // get the cell style for the current orientation
        HomeCollectionViewCellStyle style = tmp.cellStylePortrait;
        if (!self.isPortrait) {
            style = tmp.cellStyleLandscape;
        }

        // HACK!
        // need to return the correct cell size based on cell type.
        // I thought this would just be calculated from the cell xib, but alas it is not.
        //
        // Be careful with the Cell size.  The flow layout fails in cryptic ways if the size isn't within it's expected size range.
        switch (style)
        {
            case HomeCellDefaultPortrait:
                return CGSizeMake(372, 324);
            case HomeCellDefaultLandscape:
                return CGSizeMake(500, 175);
            case HomeCellTravelOnlyTripsPortrait:
                return CGSizeMake(752, 250);
            case HomeCellTravelOnlyBookingPortrait:
                return CGSizeMake(372, 239);
            case HomeCellTravelOnlyTripsLandscape:
                return CGSizeMake(502, 446);
            case HomeCellTravelOnlyBookingLandscape:
                return CGSizeMake(245, 219);
            case HomeCellExpenseOnlyPortrait:
                return CGSizeMake(752, 324);
            case HomeCellExpenseOnlyLandscape:
                return CGSizeMake(500, 358);
            case HomeCellApprovalOnlyPortrait:
                return CGSizeMake(752, 744);
            case HomeCellApprovalOnlyLandscape:
                return CGSizeMake(1008, 446);
            case HomeCellTravelAndApprovalLandscape:
                return CGSizeMake(498, 219);
            case HomeCellExpenseAndTravelOnlyTripsPortrait:
                return CGSizeMake(752, 324);
            case HomeCellExpenseAndTravelOnlyTripsLandscape:
                return CGSizeMake(500, 358);
            case HomeCellGovOnlyDocumentsPortrait:
                return CGSizeMake(245, 324);
            case HomeCellGovOnlyDocumentsLandscape:
                return CGSizeMake(330, 175);
        }
    }

    // well.  it appears we're in a bad state...
    return CGSizeMake(0, 0);
}

@end
