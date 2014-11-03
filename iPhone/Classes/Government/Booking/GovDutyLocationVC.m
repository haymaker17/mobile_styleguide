//
//  GovDutyLocationVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovDutyLocationVC.h"
#import "LocationResult.h"
#import "GovLocationsData.h"
#import "FormFieldData.h"
#import "ListFieldEditVC.h"
#import "GovPerDiemRateData.h"
#import "HotelViewController.h"
#import "CarViewController.h"
#import "TrainBookVC.h"

#import "GovTAField.h"

@interface GovDutyLocationVC(Private)
- (void)showListEditor;
- (void)completeTask;
- (BOOL)shouldFetchPerDiemRate;
- (GovTAField*)getPerDiemField;
-(NSString*) getLocationName:(GovLocation*) num;

@end

@implementation GovDutyLocationVC
@synthesize locations, actionAfterCompletion, perDiemLdgRate, selectedLocation;
@synthesize delegate = _delegate;
@synthesize taFields, needPerDiemRate;

-(void) setSeedData:(NSArray*) taFlds needPerDiem:(BOOL) bFlag
{
    self.taFields = taFlds;
    self.needPerDiemRate = bFlag;
}

- (GovTAField*)getPerDiemField
{
    for (GovTAField* fld in self.taFields)
    {
        if ([fld isPerDiemLocationField])
            return fld;
    }
    return nil;
}

- (BOOL) shouldFetchPerDiemRate
{
    return needPerDiemRate;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.title = [@"Duty Location" localize];
    [self.tBar removeFromSuperview];
    CGRect tblFrame = self.addressOnlyTableView.frame;
    self.addressOnlyTableView.frame = CGRectMake(tblFrame.origin.x, 0, tblFrame.size.width, tblFrame.size.height+ tblFrame.origin.y);
    [self.navigationController setNavigationBarHidden:NO];
}

- (void)findLocationsForAddress:(NSString *)address
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:address, @"ADDRESS", nil];
    if(isAirportOnly)
        pBag[@"AIRPORT_ONLY"] = @"true";

    [[ExSystem sharedInstance].msgControl createMsg:GOV_FIND_DUTY_LOCATION CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:GOV_LOCATIONS])
	{
        if ([self isViewLoaded])
        {
            [self hideWaitView];
        }
        
		GovLocationsData *resp = (GovLocationsData*)msg.responder;
		
		if (msg.responseCode == 200)
        {
            self.locations = resp.locations;
            [self showListEditor];
        }
    }
    else if ([msg.idKey isEqualToString:GOV_PER_DIEM_RATE])
    {
        if ([self isViewLoaded])
        {
            [self hideWaitView];
        }
        
		GovPerDiemRateData *resp = (GovPerDiemRateData*)msg.responder;
		
		if (msg.responseCode == 200)
        {
            self.perDiemLdgRate = resp.currentPerDiemRate.ldgRate;
            GovTAField* perDiemFld = [self getPerDiemField];
            perDiemFld.perDiemLocationId = resp.currentPerDiemRate.perDiemId;

            [self completeTask];
        }
    }
    else
        [super respondToFoundData:msg];
}

- (void)locationSelected:(LocationResult *)locationResult
{
    [self showWaitView];

    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 locationResult.countryAbbrev, @"CTRY_CODE",
                                 locationResult.longitude, @"LONGITUDE",
                                 locationResult.latitude, @"LATITUDE",
                                 locationResult.city, @"CITY",
                                 locationResult.state, @"STATE_CODE",
                                 locationResult.zipCode, @"ZIP_CODE",
                                 nil];
    
    [[ExSystem sharedInstance].msgControl createMsg:GOV_LOCATIONS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) completeTaskImpl
{
    // Update per diem field
    GovTAField* perDiemFld = [self getPerDiemField];
    perDiemFld.perDiemLocState = self.selectedLocation.locState;
    perDiemFld.perDiemLocation = self.selectedLocation.location;
    perDiemFld.perDiemExpDate = self.selectedLocation.expirationDate;
    perDiemFld.perDiemLocationName = [self getLocationName:self.selectedLocation];
    perDiemFld.perDiemLdgRate = self.perDiemLdgRate;
    perDiemFld.perDiemLocZip = self.selectedLocation.locZip;
    perDiemFld.isUSContiguous = self.selectedLocation.isUSContiguous;
    
    [self hideWaitView];
    // If no delegate, switch to booking page and pass along parameters
    // otherwise, call delegate with parameters
    if (self.actionAfterCompletion != nil)
    {
        UINavigationController * navi = self.navigationController;
        // Allow switchToView after add booking to existing trip
        // This change behaviors for booking use cases
        // Book from home screen: if click 'back' when entering search info, user go back to selectAuth instead of home
        // Book from trip detail: if click 'back' when entering search info, user go back to trip detail.
        
//        [navi popToRootViewControllerAnimated:NO];
        [navi popViewControllerAnimated:NO];
        if ([@"Book Hotel" isEqualToString:actionAfterCompletion])
        {
            [HotelViewController showHotelVC:navi withTAFields:self.taFields];
        }
        else if ([@"Book Car" isEqualToString:actionAfterCompletion])
        {
            [CarViewController showCarVC:navi withTAFields:self.taFields];
        }
        else if ([@"Book Air" isEqualToString:actionAfterCompletion])
        {
            [AirBookingCriteriaVC showAirVC:navi withTAFields:self.taFields];
        }
        else if ([@"Book Rail" isEqualToString:actionAfterCompletion])
        {
            [TrainBookVC showTrainVC:navi withTAFields:self.taFields];
        }
    }
    else
    {
        UINavigationController * navi = self.navigationController;
        [navi popViewControllerAnimated:NO];
        // Call delegate
        [self.delegate fieldUpdated:[self getPerDiemField]];
    }
}

-(void) completeTask
{
    [self showWaitView];
//    [self performSelector:@selector(completeTaskImpl) withObject:nil afterDelay:0.5];
    // iOS 7 delay differnetly
    // This is a hack on top of old hack
    // This really should be done correctly with Apples new syn view management
    [self performSelector:@selector(completeTaskImpl) withObject:nil afterDelay:1.0];
}

#pragma mark DutyLocation result list
-(NSString*) getLocationName:(GovLocation*) num
{
    return [NSString stringWithFormat:@"%@ -- %@", num.location, num.county];
}

- (NSArray* )getListItems
{
    NSMutableArray* result = [[NSMutableArray alloc] init];
    for (GovLocation *num in self.locations)
    {
        ListItem * li = [[ListItem alloc] init];
        li.liKey = num.location;
        li.liName = [self getLocationName:num];
        [result addObject:li];
    }
    return result;
}

- (void)showListEditor
{
    FormFieldData * fld = [[FormFieldData alloc] initField:@"TDYPerDiemLoc" label:[@"TDY Per Diem Location" localize] value:nil ctrlType:@"LIST" dataType:@"INTEGER"];
    ListFieldEditVC *lvc = nil;
    lvc = [[ListFieldEditVC alloc] initWithNibName:@"ListFieldEditVC" bundle:nil];
    [lvc setSeedData:fld delegate:self keysToExclude:nil];
    [lvc view];
	if([UIDevice isPad])
		lvc.modalPresentationStyle = UIModalPresentationFormSheet;
	[self.navigationController pushViewController:lvc animated:YES];
    lvc.sectionKeys = [[NSMutableArray alloc] initWithObjects: @"MAIN_SECTION", nil];
    lvc.sections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: [self getListItems], @"MAIN_SECTION", nil];
    [lvc hideSearchBar];
}


-(void) fieldUpdated:(FormFieldData*) fld
{
    // Find the location, and get the per diem, if needed
    for (GovLocation *num in self.locations)
    {
        if ([num.location isEqualToString:fld.liKey])
        {
            self.selectedLocation = num;
            break;
        }
    }

    if (self.selectedLocation != nil && [self shouldFetchPerDiemRate])
    {
        [self showWaitView];
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                     selectedLocation.locState, @"STATE_CTRY_CODE",
                                     selectedLocation.location, @"LOCATION",
                                     selectedLocation.effectiveDate, @"EFFECTIVE_DATE",
                                     selectedLocation.expirationDate, @"EXPIRATEION_DATE",
                                     nil];
        
        [[ExSystem sharedInstance].msgControl createMsg:GOV_PER_DIEM_RATE CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    else if (self.selectedLocation != nil)
    {
        [self completeTask];
    }
}

-(void) fieldCanceled:(FormFieldData *)field
{
    
}

-(IBAction)actionClose:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark show DutyLocation view
+ (void) showDutyLocationVC:(UIViewController*)pvc withCompletion:(NSString*)action withFields:(NSArray*) taFlds withDelegate:(id<FieldEditDelegate>) del withPerDiemRate:(BOOL)needRate asRoot:(BOOL)isRoot
{
    GovDutyLocationVC * vc = [[GovDutyLocationVC alloc] initWithNibName:@"HotelLocationViewController" bundle:nil];
    [vc setSeedData:taFlds needPerDiem:NO];
    vc.locationDelegate = nil;
    vc.neverShowOffices = YES;
    vc.isAirportOnly = NO;
    vc.actionAfterCompletion = action;
    vc.delegate = del;

    if (isRoot)
    {
        // Add Close button
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:vc action:@selector(actionClose:)];
        vc.navigationItem.leftBarButtonItem = btnClose;
    }

    if ([UIDevice isPad])
        [pvc.navigationController pushViewController:vc animated:NO];
    else
        [pvc.navigationController pushViewController:vc animated:YES];
}

@end
