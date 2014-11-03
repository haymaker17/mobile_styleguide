//
//  TripsViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/13/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "TripsData.h"
#import "TripManager.h"
#import "WebViewController.h"
@class iPadHomeVC;
@class RootViewController;

@interface TripsViewController : MobileViewController  <UITableViewDelegate, UITableViewDataSource, UIActionSheetDelegate>
{
	NSMutableArray			*listKeys, *aSections;
	NSMutableDictionary		*dictData, *dictSections;
	UITableView				*tableList;
	UINavigationBar			*navBar;
	BOOL					showedNo;
	
	//fetching view
	UIView					*fetchView;
	UILabel					*lblFetch;
	UIActivityIndicatorView	*spinnerFetch;
    
	iPadHomeVC				*iPadHome;
	MobileViewController	*fromMVC;
    NSMutableArray          *aAction;
    
    BOOL                    isExpensed;
}

@property BOOL isExpensed;
@property (nonatomic, strong) NSMutableArray        *aAction;
@property (nonatomic, strong) NSMutableArray		*listKeys;
@property (nonatomic, strong) NSMutableArray		*aSections;
@property (nonatomic, strong) IBOutlet UITableView	*tableList;
@property (nonatomic, strong) IBOutlet UINavigationBar *navBar;
@property (nonatomic, strong) NSMutableDictionary	*dictData;
@property (nonatomic, strong) NSMutableDictionary	*dictSections;

//fetching view
@property (nonatomic, strong) IBOutlet UIView					*fetchView;
@property (nonatomic, strong) IBOutlet UILabel					*lblFetch;
@property (nonatomic, strong) IBOutlet UIActivityIndicatorView	*spinnerFetch;

@property (nonatomic, strong) iPadHomeVC				*iPadHome;
@property (nonatomic, strong) MobileViewController		*fromMVC;

-(IBAction)switchViews:(id)sender;
- (void)displayTripOniPhone:(EntityTrip *)selectedTrip withLoadedTrip:(BOOL)isTripLoaded;

-(void) refreshData;
-(void) fetchAgencyInfo;
-(void)loadTrips;
-(void) createSections:(TripsData*) td;

-(void)showAction:(id)sender;
-(void)showRefreshAgencyAction:(id)sender;
-(void)buttonAirportsPressed:(id)sender;
- (void)switchToAirports:(id)sender;
- (void)buttonTaxiPressed:(id)sender;
- (void)buttonMetroPressed:(id)sender;
- (IBAction)buttonTripItPressed:(id)sender;

-(void)loadTripsWithExpenseData;
-(void)checkTripItLink;

-(BOOL) hasTrips;

+(void) refreshViewsWithTripsData:(Msg*) msg fromView:(MobileViewController*) srcVc;

@end
