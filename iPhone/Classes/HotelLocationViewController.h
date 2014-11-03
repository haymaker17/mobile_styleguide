//
//  HotelLocationViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseViewController.h"
#import "LocationDelegate.h"

@class HotelLocationHandler;
@class LocationResult;

@interface HotelLocationViewController : BookingBaseViewController  <UITableViewDelegate, UITableViewDataSource, UISearchBarDelegate, UIScrollViewDelegate>
{
	UITableView				*addressOnlyTableView;
	UITableView				*addressAndOfficeTableView;
	UISearchBar				*addressOnlySearchBar;
	UISearchBar				*addressAndOfficeSearchBar;
	UINavigationBar			*tBar;
	UIBarButtonItem			*cancelBtn;
	UISegmentedControl		*segmentedControl;
	NSString				*tag;
	id<LocationDelegate>	locationDelegate;
	LocationResult			*initialSearchLocation;
	HotelLocationHandler	*activeHotelLocationHandler;
	HotelLocationHandler	*hotelLocationAddressHandler;
	HotelLocationHandler	*hotelLocationOfficeHandler;
	BOOL					neverShowOffices;
    BOOL                    isAirportOnly;
}

@property BOOL                    isAirportOnly;

@property (strong, nonatomic) IBOutlet UITableView			*addressOnlyTableView;
@property (strong, nonatomic) IBOutlet UITableView			*addressAndOfficeTableView;
@property (strong, nonatomic) IBOutlet UISearchBar			*addressOnlySearchBar;
@property (strong, nonatomic) IBOutlet UISearchBar			*addressAndOfficeSearchBar;
@property (strong, nonatomic) IBOutlet UINavigationBar			*tBar;
@property (strong, nonatomic) IBOutlet UIBarButtonItem			*cancelBtn;
@property (strong, nonatomic) IBOutlet UISegmentedControl	*segmentedControl;
@property (strong, nonatomic) NSString						*tag;
@property (strong, nonatomic) id<LocationDelegate>			locationDelegate;
@property (strong, nonatomic) LocationResult				*initialSearchLocation;
@property (strong, nonatomic) HotelLocationHandler			*activeHotelLocationHandler;
@property (strong, nonatomic) HotelLocationHandler			*hotelLocationAddressHandler;
@property (strong, nonatomic) HotelLocationHandler			*hotelLocationOfficeHandler;
@property (nonatomic) BOOL									neverShowOffices;
@property (weak, readonly, nonatomic) UITableView					*activeTableView;
@property (weak, readonly, nonatomic) UISearchBar					*activeSearchBar;

-(void)updateView;

-(void)locationSelected:(LocationResult *)locationResult;

-(void)findLocationsForAddress:(NSString*)address;

-(IBAction) segmentedControlValueChanged:(id)sender;
-(IBAction) btnCancel:(id)sender;
-(void)closeView;

@end
