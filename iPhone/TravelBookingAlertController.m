//
//  TravelBookingAlertView.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 15/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "TravelBookingAlertController.h"
#import "HotelSearchTableViewController.h"
#import "LabelConstants.h"

#import "GovSelectTANumVC.h"
#import "HotelViewController.h"
#import "TrainBookVC.h"
#import "CarViewController.h"
#import "HotelViewController.h"
#import "TransparentViewUnderMoreMenu.h"

@interface TravelBookingAlertController()

@property (strong, nonatomic) UIAlertController *action;

// weak, in case the parent navigation controller has a reference to this action sheet.
@property (weak, nonatomic) UINavigationController *navigationController;
// weak, this class is based on TravelBookingActionSheet, so this property may not be needed but has been added just in case
@property (weak, nonatomic) UIViewController *viewController;

@end

@implementation TravelBookingAlertController

// need the parent navigation controller in order to push the booking screens
- (id)initWithNavigationController:(UINavigationController *)navigationController {
    self = [super init];
    if (self != nil) {
        self.navigationController = navigationController;
        [self makeAlertView];
    }
    return self;
}

- (void)makeAlertView
{
    self.action = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    UIAlertAction *airBooking = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Book Air"] style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        NSLog(@"air clicked");
        [self openAirBooking];
    }];
    [self.action addAction:airBooking];

    UIAlertAction *hotelBooking = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Book Hotel"] style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        NSLog(@"hotel clicked");
        [self openHotelBooking];
    }];
    [self.action addAction:hotelBooking];

    UIAlertAction *carBooking = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Book Car"] style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        NSLog(@"car clicked");
        [self openCarBooking];
    }];
    [self.action addAction:carBooking];

    if([[ExSystem sharedInstance] canBookRail])
    {
        UIAlertAction *railBooking = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Book Rail"] style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
            NSLog(@"rail clicked");
            [self openRailBooking];
        }];
        [self.action addAction:railBooking];
    }
    
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN] style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
        NSLog(@"cancel clicked");
        //
    }];
    [self.action addAction:cancel];
}

- (void)showInViewController:(UIViewController *)vc
{
    if (![self isOffline]) {
        NSLog(@"display uialertcontroller in viewcontroller");
        [vc setModalPresentationStyle:UIModalPresentationPopover];
        [self.action.popoverPresentationController setSourceView:[UIApplication sharedApplication].keyWindow];
        [self.action.popoverPresentationController setSourceRect:[UIApplication sharedApplication].keyWindow.bounds];
        [self.action.popoverPresentationController setPermittedArrowDirections:0];
        [vc presentViewController:self.action animated:YES completion:nil];
    }
}

- (void)showInRect:(CGRect )rect withViewController:(UIViewController *)vc withSender:(id)sender
{
    if (![self isOffline]) {
        NSLog(@"display uialertcontroller in rect");
        [self.action setModalPresentationStyle:UIModalPresentationPopover];
        [self.action.popoverPresentationController setSourceView:[sender view]];
        [self.action.popoverPresentationController setSourceRect:rect];
        [self.action.popoverPresentationController setPermittedArrowDirections:UIPopoverArrowDirectionDown];
        [vc presentViewController:self.action animated:YES completion:nil];
    }
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
