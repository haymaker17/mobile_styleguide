//
//  CarMileageDataLoader.m
//  ConcurMobile
//
//  Created by ernest cho on 3/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CarMileageDataLoader.h"
#import "SelectReportViewController.h"
#import "UIColor+CCPalette.h"

@interface CarMileageDataLoader()

@end

// this class loads the car mileage data
@implementation CarMileageDataLoader

- (id)init
{
    self = [super init];
    if (self != nil) {
        if([ConcurMobileAppDelegate findRootViewController].carRatesData == nil)
            [self fetchCarRatesAndSkipCache:NO];
        else
            self.carRatesData = [ConcurMobileAppDelegate findRootViewController].carRatesData;
    }
    return self;
}

// get the car rate data
- (void)fetchCarRatesAndSkipCache:(BOOL)shouldSkipCache
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	[[ExSystem sharedInstance].msgControl createMsg:CAR_RATES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache: shouldSkipCache RespondTo:self];
}

- (BOOL)isCarMileageDataReady
{
    if (self.carRatesData == nil || ![self.carRatesData hasAnyPersonalsWithRates:[ExSystem sharedInstance].sys.crnCode]) {
        // MOB-5171 - Pop up dialog to warn user on Car Mileage
        if (self.carRatesData == nil) {
            UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[Localizer getLocalizedText:@"WAIT_FOR_CAR_RATES_DATA"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] otherButtonTitles:nil];
            [alert show];
            
        } else {
            UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[Localizer getLocalizedText:@"ADD_CAR_MILEAGE_HOME_NOT_SUPPORTED"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] otherButtonTitles:nil];
            [alert show];
        }
        return false;
    }
    return true;
}

- (void)openReportSelectView:(UIViewController *)parentView
{
    if(self.carRatesData == nil)
        [self fetchCarRatesAndSkipCache:NO];
    
	SelectReportViewController * pVC = [[SelectReportViewController alloc] initWithNibName:@"SelectReportViewController" bundle:nil];
	pVC.meKeys = nil;
	pVC.pctKeys = nil;
	pVC.cctKeys = nil;
	pVC.meAtnMap = nil;
	pVC.isCarMileage = YES;
    
    // do I need to set this?  doesn't look like it hurts to do it this way
	//pVC.parentMVC = parentView;
    if ([UIDevice isPad]) {
        [parentView presentViewController:[self getNavigationControllerWithRootVC:pVC] animated:YES completion:nil];
    } else {
        [parentView.navigationController pushViewController:pVC animated:YES];
    }
}

- (void)didProcessMessage:(Msg *)msg
{
    [self respondToFoundData:msg]; // TODO: handle case where msg.didConnectionFail is YES
}

- (void)respondToFoundData:(Msg *)msg
{
	if([msg.idKey isEqualToString:CAR_RATES_DATA]) {
		self.carRatesData = (CarRatesData*) msg.responder;
        
        // save this for the report detail screen.  There's a lot of places where code asks root for car rate data.
        // This will make sure it's set correctly.
        // in Case home has already set it. 
        if([ConcurMobileAppDelegate findRootViewController].carRatesData == nil)
            [ConcurMobileAppDelegate findRootViewController].carRatesData = self.carRatesData;
	}
}

- (UINavigationController*)getNavigationControllerWithRootVC:(UIViewController*)vc
{
    UINavigationController *modalViewNavController = [[UINavigationController alloc] initWithRootViewController:vc];
    modalViewNavController.modalPresentationStyle = UIModalPresentationFormSheet;
    [modalViewNavController setToolbarHidden:NO];
    modalViewNavController.toolbar.tintColor = [UIColor darkBlueConcur_iOS6];
    modalViewNavController.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];
    
    return modalViewNavController;
}

@end
