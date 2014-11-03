//
//  IPhoneBookingActionSheet.m
//  ConcurMobile
//
//  Created by ernest cho on 3/19/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TravelBookingActionSheet.h"
#import "LabelConstants.h"
#import "MobileActionSheet.h"

#import "GovSelectTANumVC.h"
#import "HotelViewController.h"
#import "HotelSearchTableViewController.h"
#import "TrainBookVC.h"
#import "CarViewController.h"
#import "HotelViewController.h"
#import "TransparentViewUnderMoreMenu.h"

// Book Travel Action sheet button IDs
#define BOOKINGS_BTN_AIR @"Book Air"
#define BOOKINGS_BTN_HOTEL @"Book Hotel"
#define BOOKINGS_BTN_CAR @"Book Car"
#define BOOKINGS_BTN_RAIL @"Book Rail"

@interface TravelBookingActionSheet()

@property (strong, nonatomic) MobileActionSheet *action;

// weak, in case the parent navigation controller has a reference to this action sheet.
@property (weak, nonatomic) UINavigationController *navigationController;
@property (weak, nonatomic) UIViewController *viewController;

@end

// Justin does not like separating the actionsheet from it's viewcontroller.
// He's concerned that devs will be tempted to use this as a global booking dispatch.
//
// It's really just intended to be a booking sheet widget for the more menu and trip list.  
// do NOT subclass or force this class to know anything about it's caller.
//
// Ernest
@implementation TravelBookingActionSheet

// need the parent navigation controller in order to push the booking screens
- (id)initWithNavigationController:(UINavigationController *)navigationController {
    self = [super init];
    if (self != nil) {
        self.navigationController = navigationController;
        [self makeActionSheet];
    }
    return self;
}

- (void)makeActionSheet
{
    self.action = [[MobileActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:nil destructiveButtonTitle:nil otherButtonTitles:nil];
    NSMutableArray* btnIds = [[NSMutableArray alloc] init];
    
    [self.action addButtonWithTitle:[Localizer getLocalizedText:@"Book Air"]];
    [btnIds addObject:BOOKINGS_BTN_AIR];
    [self.action addButtonWithTitle:[Localizer getLocalizedText:@"Book Hotel"]];
    [btnIds addObject:BOOKINGS_BTN_HOTEL];
    [self.action addButtonWithTitle:[Localizer getLocalizedText:@"Book Car"]];
    [btnIds addObject:BOOKINGS_BTN_CAR];
    
    if([[ExSystem sharedInstance] canBookRail])
    {
        [self.action addButtonWithTitle:[Localizer getLocalizedText:@"Book Rail"]];
        [btnIds addObject:BOOKINGS_BTN_RAIL];
    }
    
    [self.action addButtonWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
    self.action.cancelButtonIndex = [btnIds count];
    
    self.action.btnIds = btnIds;
}

- (BOOL)isOffline
{
    if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Offline"] message:[Localizer getLocalizedText:@"Bookings offline"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return true;
	}
    return false;
}

- (void)showActionSheetFromToolBar:(UIToolbar *)toolBar
{
    if (![self isOffline]) {
        [self.action showFromToolbar:toolBar];
    }
}

- (void)showActionSheetInView:(UIView *)view
{
    if (![self isOffline]) {
        [self.action showInView:view];
    }
}

- (void)showActionSheetFromBarButtonItem:(UIBarButtonItem *)item
{
    if (![self isOffline] && item != nil) {
        [self.action showFromBarButtonItem:item animated:YES];
    }
}

- (void)showActionSheetFromRect:(CGRect)rect inView:(UIView *)view
{
    if (![self isOffline]) {
        [self.action showFromRect:rect inView:view animated:YES];
    }
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    // just give up if there's no navigation controller. if we're here the parent was dealloc'd.
    // or the class was incorrectly init'd

    if (buttonIndex != actionSheet.cancelButtonIndex)
    {
        MobileActionSheet* mas = (MobileActionSheet*) actionSheet;
        NSString* btnId = [mas getButtonId:buttonIndex];
        
        if ([BOOKINGS_BTN_HOTEL isEqualToString:btnId])
        {
            [self openHotelBooking];
        }
        else if ([BOOKINGS_BTN_CAR isEqualToString:btnId])
        {
            [self openCarBooking];
        }
        else if ([BOOKINGS_BTN_RAIL isEqualToString:btnId])
        {
            [self openRailBooking];
        }
        else if ([BOOKINGS_BTN_AIR isEqualToString:btnId])
        {
            [self openAirBooking];
        }
    }
}

- (void)openHotelBooking
{
    if ([Config isGov]) {
        [GovSelectTANumVC showSelectTANum:self withCompletion:BOOKINGS_BTN_HOTEL withFields:nil withDelegate:nil asRoot:NO];
    } else {
        if ([UIDevice isPad] && self.navigationController == nil)
        {
            if ([Config isNewHotelBooking] && [UIDevice isPhone]) {
                [HotelSearchTableViewController showHotelsNearMe:[self getBookTripsNavigationController]];
            }else{
                [HotelViewController showHotelVC:[self getBookTripsNavigationController] withTAFields:nil];
            }
        }else{
            if ([Config isNewHotelBooking] && [UIDevice isPhone]) {
                [HotelSearchTableViewController showHotelsNearMe:self.navigationController];
            }else{
                [HotelViewController showHotelVC:self.navigationController withTAFields:nil];
            }
        }
    }
}

- (void)openCarBooking
{
    if ([Config isGov]) {
        [GovSelectTANumVC showSelectTANum:self withCompletion:BOOKINGS_BTN_CAR withFields:nil withDelegate:nil asRoot:NO];
    } else {
        if ([UIDevice isPad] && self.navigationController == nil)
        {
            [CarViewController showCarVC:[self getBookTripsNavigationController] withTAFields:nil];
        } else {
            [CarViewController showCarVC:self.navigationController withTAFields:nil];
        }
        
    }
}

- (void)openRailBooking
{
    if ([Config isGov]) {
        [GovSelectTANumVC showSelectTANum:self withCompletion:BOOKINGS_BTN_RAIL withFields:nil withDelegate:nil asRoot:NO];
    } else {
        if ([UIDevice isPad] && self.navigationController == nil)
        {
            [TrainBookVC showTrainVC:[self getBookTripsNavigationController] withTAFields:nil];
        } else {
            [TrainBookVC showTrainVC:self.navigationController withTAFields:nil];
        }
    }
}

- (void)openAirBooking
{
    if ([self checkBookAir])
    {
         if ([Config isGov])
             [GovSelectTANumVC showSelectTANum:self withCompletion:BOOKINGS_BTN_AIR withFields:nil withDelegate:nil asRoot:NO];
         else
         {
             if ([UIDevice isPad] && self.navigationController == nil)
             {
                 [AirBookingCriteriaVC showAirVC:[self getBookTripsNavigationController] withTAFields:nil];
             }
             else
                 [AirBookingCriteriaVC showAirVC:self.navigationController withTAFields:nil];
         }
    }
}

- (BOOL)checkBookAir
{
    NSString* msg = nil;
    if (!([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_AIR_BOOKING_ENABLED]))
    {
        msg = [Localizer getLocalizedText:@"AIR_BOOKING_DISABLED_MSG"];
    }
    else
    {
        NSString* profileStatus = [[ExSystem sharedInstance] getUserSetting:@"ProfileStatus" withDefault:@"0"];
        // MOB-10390 Allow users with profileStatus 1 (missing middlename, gender) to go ahead and search air.
        if (![profileStatus isEqualToString:@"0"] && ![profileStatus isEqualToString:@"1"])
        {
            if ([profileStatus isEqualToString:@"20"])
                profileStatus = @"2";
            NSString* msgKey = [NSString stringWithFormat:@"AIR_BOOKING_PROFILE_%@_MSG", profileStatus];
            msg = [NSString stringWithFormat:@"%@\n\n%@", [Localizer getLocalizedText:msgKey], [@"AIR_BOOKING_PROFILE_PROLOG_MSG" localize]];
        }
        else
            return TRUE;
    }
    
    MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:msg delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] otherButtonTitles:nil];
    [alert show];
    
    return FALSE;
}

-(UINavigationController*)getBookTripsNavigationController
{
    TripsViewController *tripsListVC = [[TripsViewController alloc] initWithNibName:@"TripsView" bundle:nil];
    UINavigationController *navcontroller = [[UINavigationController alloc] initWithRootViewController:tripsListVC];
    navcontroller.modalPresentationStyle = UIModalPresentationFormSheet;

    [navcontroller setToolbarHidden:NO];
    if (self.viewController != nil)
        [self.viewController presentViewController:navcontroller animated:YES completion:nil];
    
    return navcontroller;
}

@end
