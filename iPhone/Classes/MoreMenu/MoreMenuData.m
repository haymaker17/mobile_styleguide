//
//  MoreMenuData.m
//  ConcurMobile
//
//  Created by ernest cho on 3/13/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MoreMenuData.h"
#import "MoreMenuSectionData.h"
#import "SettingsViewController.h"
#import "ProfileViewController.h"
#import "AppsUtil.h"
#import "QuickExpensesReceiptStoreVC.h"
#import "TravelBookingActionSheet.h"
#import "TravelBookingAlertController.h"
#import "CarMileageDataLoader.h"
#import "Config.h"
#import "TourVC.h"
#import "Config.h"
#import "TransparentViewUnderMoreMenu.h"
#import "SendFeedBackVC.h"

#import "FirstViewController.h"
#import "AirBenchmarkSearchVC.h"
#import "HotelBenchmarkSearchVC.h"
#import "UIActionSheet+Blocks.h"
#import "UberIntroductionVC.h"
#import "AppCenterRequest.h"
#import "AppCenterListing.h"

typedef NS_ENUM(NSUInteger, MenuItem) {
    // tags to decide what view should be opened
    MENU_PROFILE,
    MENU_TRAVEL_BOOKING,
    MENU_PRICE_TO_BEAT,
    MENU_RECEIPTS,
    MENU_CAR_MILEAGE,
    MENU_LOCATION_CHECK_IN,
    MENU_TOUR,
    MENU_JPT,
    MENU_FEEDBACK,
    MENU_TRIP_IT,
    MENU_EXPENSEIT,
    MENU_GATE_GURU,
    MENU_TAXI_MAGIC,
    MENU_TRAVEL_TEXT,
    MENU_METRO,
    MENU_UBER,
    MENU_SETTINGS,
    // this will alert the user that the feature is disabled on mobile
    MENU_FEATURE_DISABLED_ON_MOBILE
};

@interface MoreMenuData()

@property (nonatomic, strong) NSMutableArray *sections;
@property (nonatomic, strong) CarMileageDataLoader *carMileageDataLoader;
@property (nonatomic, strong) NSDictionary *appCenterConnectedApps;

@end

@implementation MoreMenuData

- (id)init
{
    self = [super init];
    if (self != nil) {
        self.carMileageDataLoader = [[ExSystem sharedInstance] hasCarMileageOnHome] ? [[CarMileageDataLoader alloc] init] : nil;
        self.sections = [[NSMutableArray alloc] init];
        [self setupMenuData];
        [self setupAppCenter];
    }
    return self;
}

#pragma -
#pragma Menu data methods for the View Controller
- (NSString *)getTitleForSection:(NSInteger)section
{
    return [self getDataForSection:section].sectionTitle;
}

- (NSInteger)numberOfSections
{
    return [self.sections count];
}

- (NSInteger)numberOfRowsInSection:(NSInteger)section
{
    return [[self getDataForSection:section] getRowCount];
}

- (NSString *)getTextForCell:(NSIndexPath *)indexPath
{
    return [[self getDataForSection:indexPath.section] getTextForRow:indexPath.row];
}

- (UIImage *)getImageForCell:(NSIndexPath *)indexPath
{
    return [[self getDataForSection:indexPath.section] getImageForRow:indexPath.row];
}

- (NSInteger)getTagForCell:(NSIndexPath *)indexPath
{
    return [[self getDataForSection:indexPath.section] getTagForRow:indexPath.row];
}

- (MoreMenuSectionData *)getDataForSection:(NSInteger)section
{
    return (self.sections)[section];
}

#pragma -
#pragma Cell Actions
- (BOOL)didSelectCell:(NSIndexPath *)indexPath withView:(UIViewController *)viewController atLocation:(CGRect)rect
{
    if (viewController != nil) {
        
        // this is a bit of a hack.  I tried to use view.parentView but that always returns null.
        UIViewController *parentView = [ConcurMobileAppDelegate findHomeVC];
        
        NSInteger tag = [self getTagForCell:indexPath];
        
        if ([UIDevice isPad]) {
            // More menu will fade out by default, hence no need to remove it in case of MENU_FEATURE_DISABLED_ON_MOBILE
        } else {
            if (tag != MENU_FEATURE_DISABLED_ON_MOBILE && tag != MENU_SETTINGS && tag != MENU_TOUR
                && tag != MENU_FEEDBACK && tag!= MENU_UBER) {
                [viewController dismissViewControllerAnimated:NO completion:nil];
            }
        }
        
        switch (tag) {
            case MENU_PROFILE:
                [self openProfileMenu:parentView];
                break;
            case MENU_SETTINGS:
                [self openSettingsMenu:viewController];
                break;
            case MENU_TRAVEL_BOOKING:
                [self openBookingsActionSheet:parentView];
                break;
            case MENU_PRICE_TO_BEAT:
                [self openPriceToBeatMenu:parentView withViewController:viewController];
                break;
            case MENU_CAR_MILEAGE:
                if ([UIDevice isPad]) {
                    [self openCarMileage:viewController];
                } else {
                    [self openCarMileage:parentView];
                }
                break;
            case MENU_RECEIPTS:
                if ([UIDevice isPad]) {
                    [self openReceipts:viewController];
                } else {
                    [self openReceipts:parentView];
                }
                break;
            case MENU_LOCATION_CHECK_IN:
                if ([UIDevice isPad]) {
                    [self openLocationCheckIn:viewController];
                } else {
                    [self openLocationCheckIn:parentView];
                }
                break;
            case MENU_FEATURE_DISABLED_ON_MOBILE: // this is the case when we display feature on the menu but they are disabled
                [self openFeatureDisabledAlert:parentView];
                break;
            case MENU_GATE_GURU:
                [self launchGateGuru];
                break;
            case MENU_UBER:
                [self launchUber:viewController];
                break;
            case MENU_TAXI_MAGIC:
                [self launchTaxiMagic];
                break;
            case MENU_TRAVEL_TEXT:
                [self launchTravelText];
                break;
            case MENU_METRO:
                [self launchMetro];
                break;
            case MENU_TRIP_IT:
                [self launchTripIt];
                break;
            case MENU_EXPENSEIT:
                [self launchExpenseIt];
                break;
            case MENU_TOUR:
                [self openTour:viewController];
                break;
            case MENU_JPT:
                if ([UIDevice isPad]) {
                    [self openJpt:viewController];
                } else {
                    [self openJpt:parentView];
                }
                break;
            case MENU_FEEDBACK:
                if ([UIDevice isPad]){
                    [self openFeedBacks:viewController];
                }
                else{
                    [self openFeedBacks:viewController];
                }
                break;
        }
    }
    return YES;
}

- (void)openJpt:(UIViewController *)parentView
{
    FirstViewController *nextController = [[FirstViewController alloc] initWithNibName:@"FirstViewController" bundle:nil];
    if ([UIDevice isPad])
    {
        // show JPT in modal view
        [parentView presentViewController:[self getNavigationControllerWithRootVC:nextController] animated:YES completion:nil];
    } else {
        [parentView.navigationController pushViewController:nextController animated:YES];
    }
}

- (void)openAirPriceToBeat:(UIViewController *)parentView
{
    AirBenchmarkSearchVC *nextController = [[AirBenchmarkSearchVC alloc] initWithTitle:[@"Air Price to Beat" localize]];
    if ([UIDevice isPad])
    {
        // show in modal view
        [parentView presentViewController:[self getNavigationControllerWithRootVC:nextController] animated:YES completion:nil];
    } else {
        [parentView.navigationController pushViewController:nextController animated:YES];
    }
    [Flurry logEvent:@"Price-to-Beat: Air Price-to-Beat Search Viewed"];
}

- (void)openHotelPriceToBeat:(UIViewController *)parentView
{
    HotelBenchmarkSearchVC *nextController = [[HotelBenchmarkSearchVC alloc] initWithTitle:[@"Hotel Price to Beat" localize]];
    if ([UIDevice isPad])
    {
        // show in modal view
        [parentView presentViewController:[self getNavigationControllerWithRootVC:nextController] animated:YES completion:nil];
    } else {
        [parentView.navigationController pushViewController:nextController animated:YES];
    }
    [Flurry logEvent:@"Price-to-Beat: Hotel Price-to-Beat Search Viewed"];
}

- (void)openTour:(UIViewController *)view
{
    TourVC *nextController = [[TourVC alloc] initWithNibName:@"TourVC" bundle:nil];
    [nextController setTitle:[Localizer getLocalizedText:@"Tour"]];
    [view.navigationController pushViewController:nextController animated:YES];
}

// alerts the user that the feature is disabled
- (void)openFeatureDisabledAlert:(UIViewController *)parentView
{
    MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[Localizer getLocalizedText:@"MODULE_DISABLED_ALERT_TEXT"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] otherButtonTitles:nil];
    [alert show];
}

- (void)openBookingsActionSheet:(UIViewController *)parentView
{
    if ([ExSystem is8Plus] && [UIDevice isPad])
    {
        // iOS8 deprecates UIActionSheet - have to use UIAlertController going forward
        // UIActionSheet is still working generally, but on iPad in iOS8 it doesn't behave correctly, hence the switch now
        // We need to revisit this in future and completely remove UIActionSheet
        TravelBookingAlertController *bookingAlert = [[TravelBookingAlertController alloc] initWithNavigationController:parentView.navigationController];
        [bookingAlert showInViewController:parentView];
    }
    else
    {
        TravelBookingActionSheet *bookingSheet = [[TravelBookingActionSheet alloc] initWithNavigationController:parentView.navigationController];
        [bookingSheet showActionSheetInView:[UIApplication sharedApplication].keyWindow];
    }

}

- (void)openPriceToBeatMenu:(UIViewController *)parentVC withViewController:(UIViewController *)mainVC
{
    // UIActionSheet in iOS8 does not work correctly on iPad
    if ([ExSystem is8Plus] && [UIDevice isPad])
    {
        // Need to use UIAlertController instead of UIActionSheet
        UIAlertController *pricetoBeat = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
        
        UIAlertAction *airPrice = [UIAlertAction actionWithTitle:[@"Airfare Price to Beat" localize] style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
            NSLog(@"air clicked");
            [self openAirPriceToBeat:parentVC];
        }];
        [pricetoBeat addAction:airPrice];
        
        UIAlertAction *hotelPrice = [UIAlertAction actionWithTitle:[@"Hotel Price to Beat" localize] style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
            NSLog(@"hotel clicked");
            [self openHotelPriceToBeat:parentVC];
        }];
        [pricetoBeat addAction:hotelPrice];
        
        [parentVC setModalPresentationStyle:UIModalPresentationPopover];
        [pricetoBeat.popoverPresentationController setSourceView:[UIApplication sharedApplication].keyWindow];
        [pricetoBeat.popoverPresentationController setSourceRect:[UIApplication sharedApplication].keyWindow.bounds];
        [pricetoBeat.popoverPresentationController setPermittedArrowDirections:0];
        
        [parentVC presentViewController:pricetoBeat animated:YES completion:nil];
    }
    else{
        [UIActionSheet showInView:[UIApplication sharedApplication].keyWindow
                        withTitle:nil
                cancelButtonTitle:[@"Cancel" localize]
           destructiveButtonTitle:nil
                otherButtonTitles:@[[@"Airfare Price to Beat" localize],[@"Hotel Price to Beat" localize]]
                         tapBlock:^(UIActionSheet *actionSheet, NSInteger buttonIndex) {
                             UIViewController *nextVCsParent = [UIDevice isPad] ? mainVC : parentVC;
                             if (buttonIndex == 0)
                                 [self openAirPriceToBeat:nextVCsParent];
                             else if (buttonIndex == 1)
                                 [self openHotelPriceToBeat:nextVCsParent];
                         }];
    }
    [Flurry logEvent:@"Price-to-Beat: Price-to-Beat Menu Viewed"];
}
// this is copied from rootVC's switchToView logic
- (void)openReceipts:(UIViewController *)parentView
{
    NSDictionary *dictionary = @{@"Action": @"Receipt Store"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];

    QuickExpensesReceiptStoreVC *view = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    [view setSeedDataAndShowReceiptsInitially:YES allowSegmentSwitch:YES allowListEdit:YES];
    if ([UIDevice isPad])
    {
        [parentView presentViewController:[self getNavigationControllerWithRootVC:view] animated:YES completion:nil];
    }
    else {
        [parentView.navigationController pushViewController:view animated:YES];
    }
}

// reuse the rootVC car mileage logic.  This is too much to refactor out.
- (void)openCarMileage:(UIViewController *)parentView
{
    NSDictionary *dictionary1 = @{@"Add from": @"More Menu"};
    [Flurry logEvent:@"Car Mileage: Add from" withParameters:dictionary1];
    
    if ([self.carMileageDataLoader isCarMileageDataReady])
    {
        if ([UIDevice isPad])
        {
            // car Mileage does not behave like the other :-(
        }
        [self.carMileageDataLoader openReportSelectView:parentView];
    }
}

// this is copied from rootVC.
- (void)openLocationCheckIn:(UIViewController *)parentView
{
    if (![ExSystem connectedToNetwork])
    {
 		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Location Check Offline"]
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return;
    }
    
    NSDictionary *dictionary = @{@"Action": @"Safety Checkin"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    SafetyCheckInVC* vc = [[SafetyCheckInVC alloc] initWithNibName:@"EditFormView" bundle:nil];
    [vc setSeedData:nil];
    
    if([UIDevice isPad])
    {
        [parentView presentViewController:[self getNavigationControllerWithRootVC:vc] animated:YES completion:nil];
    } else {
        [parentView.navigationController pushViewController:vc animated:YES];
    }
}

/**
 *  Open a email dialog box to send feedbacks to "mobilealphafeedbackios@concur.com"
 */
- (void)openFeedBacks:(UIViewController *)parentView
{
    if (![MFMailComposeViewController canSendMail])
	{
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Mail Unavailable"]
							  message:[Localizer getLocalizedText:@"This device is not configured for sending mail."]
							  delegate:nil
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
	}
    else{
        SendFeedBackVC *vc = [[SendFeedBackVC alloc] init];
        [vc sendLogAction];
        if([UIDevice isPad]){
            vc.modalPresentationStyle = UIModalPresentationFormSheet;
            [parentView presentViewController:vc animated:YES completion:nil];
        }
        else{
            [parentView presentViewController:vc animated:YES completion:nil];
        }
    }
}

- (void)launchGateGuru
{
    [AppsUtil launchGateGuruAppWithUrl:nil];
}

- (void)launchTaxiMagic
{
    [AppsUtil launchTaxiMagicApp];
}

- (void)launchMetro
{
    [AppsUtil launchMetroApp];
}

- (void)launchUber:(UIViewController *)parentView
{
//    // uber introduction vc
//    UberIntroductionVC *vc = [[UberIntroductionVC alloc] init];
//    if ([UIDevice isPad])
//    {
//        // show in modal view
//        [parentView presentViewController:[self getNavigationControllerWithRootVC:vc] animated:YES completion:nil];
//    } else {
//        [parentView.navigationController pushViewController:vc animated:YES];
//    }

    BOOL uberConnected = NO;
    if (self.appCenterConnectedApps != nil){
        uberConnected = (BOOL)[self.appCenterConnectedApps objectForKey:@"Uber"];
    }
    
    if ([Config isDevConBuild]){
        if (uberConnected)
        {
            [AppsUtil launchUberApp];
            [parentView dismissViewControllerAnimated:NO completion:nil];
        } else {
            UberIntroductionVC *vc = [[UberIntroductionVC alloc] init];
            if ([UIDevice isPad])
            {
                // show in modal view
                [parentView presentViewController:[self getNavigationControllerWithRootVC:vc] animated:YES completion:nil];
            } else {
                [parentView.navigationController pushViewController:vc animated:YES];
            }
        }
    }
    else{
        [AppsUtil launchUberApp];
        [parentView dismissViewControllerAnimated:NO completion:nil];
    }
}

- (void)launchTripIt
{
    [AppsUtil launchTripItApp];
}

-(void)launchExpenseIt
{
    [AppsUtil launchExpenseItApp];
}

- (void)openProfileMenu:(UIViewController *)parentView
{

    ProfileViewController *vc = [[ProfileViewController alloc] initWithTitle];
    if([UIDevice isPad]){
        [parentView presentViewController:[self getNavigationControllerWithRootVC:vc] animated:YES completion:nil];
    }
    else{
        [parentView.navigationController pushViewController:vc animated:YES];
    }
}

- (void)openSettingsMenu:(UIViewController *)parentView
{
    SettingsViewController *vc = [[SettingsViewController alloc] init];
    if ([UIDevice isPad])
    {
        [parentView presentViewController:[self getNavigationControllerWithRootVC:vc] animated:YES completion:nil];
    } else {
        [parentView.navigationController pushViewController:vc animated:YES];
    }
}

#pragma -
#pragma Menu data setup
- (void)setupMenuData
{
    self.sections = [[NSMutableArray alloc] init];
    
    
    
    MoreMenuSectionData *tmp = [self getProfileRow];
    
    if([tmp getRowCount]>0){
        [self.sections addObject:tmp];
    }
    
    tmp = [self getTopRows];
    if ([tmp getRowCount] > 0) {
        [self.sections addObject:tmp];
    }
    
    tmp = [self getAppsRows];
    if ([tmp getRowCount] > 0) {
        [self.sections addObject:tmp];
    }
    
    tmp = [self getSettingsRow];
    if ([tmp getRowCount] > 0) {
        [self.sections addObject:tmp];
    }
}

- (MoreMenuSectionData *)getProfileRow
{
    MoreMenuSectionData *tmp = [[MoreMenuSectionData alloc] init];
    tmp.sectionTitle = @"";
    
    // Add Profile
    if ([Config isProfileEnable]){
        NSString *text = [@"Profile" localize];
        [tmp saveRowData:text withImage:@"icon_profile" withTag:MENU_PROFILE];
    }
    
    return tmp;
}

- (MoreMenuSectionData *)getSettingsRow
{
    MoreMenuSectionData *tmp = [[MoreMenuSectionData alloc] init];
    tmp.sectionTitle = [Localizer getLocalizedText:@"Settings"];
    
    NSString *text = [Localizer getLocalizedText:@"Settings"];
    [tmp saveRowData:text withImage:@"icon_menu_settings" withTag:MENU_SETTINGS];
    return tmp;
}

- (MoreMenuSectionData *)getAppsRows
{
    MoreMenuSectionData *tmp = [[MoreMenuSectionData alloc] init];
    tmp.sectionTitle = [Localizer getLocalizedText:@"Apps"];
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_TRIPITAD_USER])
    {
        [tmp saveRowData:@"TripIt" withImage:@"icon_menu_tripit" withTag:MENU_TRIP_IT];
    }

    if ([[ExSystem sharedInstance] hasExpenseIt]) {
        [tmp saveRowData:@"ExpenseIt" withImage:@"icon_menu_expenseit" withTag:MENU_EXPENSEIT];
    }

    [tmp saveRowData:@"Uber" withImage:@"icon_menu_uber" withTag:MENU_UBER];

    if ([[ExSystem sharedInstance] hasTaxiMagic])
    {
        [tmp saveRowData:@"Curb" withImage:@"icon_menu_curb" withTag:MENU_TAXI_MAGIC];
    }

    // MOB-16556
    // Henry says we show this to everyone!!!
    [tmp saveRowData:@"TravelText" withImage:@"icon_menu_traveltext" withTag:MENU_TRAVEL_TEXT];

    return tmp;
}

// Get the list of configured Apps from the Concur App Center, populate a list of connected apps
-(void)setupAppCenter
{
    // Call the appcenter for listings
    AppCenterRequest *request = [[AppCenterRequest alloc] init];
    [request requestListOfApps:^(NSArray *appListings, NSString *info) {
        if (appListings != nil && [appListings count] > 0) {
            NSMutableDictionary *appDict = [[NSMutableDictionary alloc] init];
            for (AppCenterListing *listing in appListings) {		
                if ([listing.name isEqualToString:@"Uber"]) {
                    if (listing.isUserConnected) {
                        [appDict setValue:@YES forKey:@"Uber"];
                    }
                }
            }
            // populate the list of connection flags
            self.appCenterConnectedApps = appDict;
        }
    } failure:^(CTEError *error) {
        // log the error
        ALog(@"Listing of connected apps failed with error : %@", [error localizedDescription]);
    }];
}

/**
 Last second implementation of travel text in apps section cause of contract obligations...
 Fuckin not getting this information ahead of time.
 */
- (void)launchTravelText
{
    [AppsUtil launchTravelTextApp];
}

- (MoreMenuSectionData *)getTopRows
{
    MoreMenuSectionData *tmp = [[MoreMenuSectionData alloc] init];
    tmp.sectionTitle = [Config isProfileEnable] ? @"ShortCuts"
                                                : @"";
    
    // booking options
    if ([[ExSystem sharedInstance] hasTravelBooking]) {
        // this string actually says "Book Air, Hotel and more"
        NSString *text = [Localizer getLocalizedText:@"Book Travel"];
        if ([[ExSystem sharedInstance] siteSettingAllowsTravelBooking]) {
            [tmp saveRowData:text withImage:@"icon_trip" withTag:MENU_TRAVEL_BOOKING];
        } else {
            [tmp saveRowData:text withImage:@"icon_trip" withTag:MENU_FEATURE_DISABLED_ON_MOBILE];
        }
    }
    
    if ([[ExSystem sharedInstance] shouldShowPriceToBeatGenerator]) {
        NSString *text = [@"Price to Beat" localize];
        [tmp saveRowData:text withImage:@"icon_trip" withTag:MENU_PRICE_TO_BEAT];
    }
    
    // add receipt store
    if ([[ExSystem sharedInstance] hasReceiptStore]) {
        NSString *text = [Localizer getLocalizedText:@"Receipts"];
        [tmp saveRowData:text withImage:@"icon_menu_receipt" withTag:MENU_RECEIPTS];
    }
    
    // add personal car mileage
    // MOB-13216 : check if carrates has any personal car mileage
    // if carrates is nil check if rvc.carrates is available, it might have it from home
    // Not the right fix. making too many calls to CAR_RATES_DATA, fix this later. 
    CarRatesData *carrates = self.carMileageDataLoader.carRatesData==nil? [ConcurMobileAppDelegate findRootViewController].carRatesData : self.carMileageDataLoader.carRatesData;

    if ([[ExSystem sharedInstance] hasCarMileageOnHome] && [carrates hasAnyPersonalsWithRates:[ExSystem sharedInstance].sys.crnCode]) {
        NSString *text = [Localizer getLocalizedText:@"Car Mileage"];
        if ([[ExSystem sharedInstance] siteSettingAllowsExpenseReports]) {
            [tmp saveRowData:text withImage:@"icon_menu_mileage" withTag:MENU_CAR_MILEAGE];
        } else {
            [tmp saveRowData:text withImage:@"icon_menu_mileage" withTag:MENU_FEATURE_DISABLED_ON_MOBILE];
        }
    }
    
    // add Locate and Alert
    if ([[ExSystem sharedInstance] hasLocateAndAlert]) {
        // this string actually says "Location Check In"
        NSString *text = [Localizer getLocalizedText:@"Safety Check In"];
        [tmp saveRowData:text withImage:@"icon_menu_location" withTag:MENU_LOCATION_CHECK_IN];
    }

    // MOB-16951 'Learn More' button on drawer menu needs to be removed.
//    // add tour
//    if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER] && [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER] && ![UIDevice isPad]) {
//        NSString *text = [Localizer getLocalizedText:@"Learn More"];
//        [tmp saveRowData:text withImage:@"icon_menu_tour" withTag:MENU_TOUR];
//    }

    // Add JPT
    if ([[ExSystem sharedInstance] hasJpt]) {
       [tmp saveRowData:[Localizer getLocalizedText:@"japan_public_transit"] withImage:@"icon_menu_jpt" withTag:MENU_JPT];
    }
    
    // Add FeedBacks
    if ([[ExSystem sharedInstance] hasFeedBacks]){
        [tmp saveRowData:@"Leave Feedback" withImage:@"icon_feedback" withTag:MENU_FEEDBACK];
    }
    
    
    return tmp;
}

#pragma mark - View Utility methods

- (UINavigationController*)getNavigationControllerWithRootVC:(UIViewController*)vc
{
    UINavigationController *modalViewNavController = [[UINavigationController alloc] initWithRootViewController:vc];
    modalViewNavController.modalPresentationStyle = UIModalPresentationFormSheet;
    [modalViewNavController setToolbarHidden:NO];
    
    return modalViewNavController;
}

@end
