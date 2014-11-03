//
//  TrainStationsVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "TrainBookingCell.h"
#import "BookingCellData.h"
#import "TrainBookVC.h"

@interface TrainStationsVC : MobileViewController <UITableViewDelegate, UITableViewDataSource, UISearchBarDelegate, UITextFieldDelegate>{
	UITableView				*tableList;
	IBOutlet UISearchBar	*searchBar;
	NSMutableArray			*aSections, *cpListOfItems;
	NSMutableArray			*aList, *cityList;
	NSArray					*stateList;
	NSString				*iataFrom, *iataTo, *fromName, *toName;
	UITextField				*txtFrom, *txtTo;
	BOOL					isFrom;
	BOOL					searching;
	BOOL					isShowingStates;
	UILabel					*lblStations;
	
	BookingCellData		*bcdFrom, *bcdTo;
	
	UINavigationBar		*tbTop;
	UIBarButtonItem		*cancelBtn;
	UIBarButtonItem		*doneBtn;
	
	UIToolbar			*tbBottom;
	UIView				*loadingView;
	UILabel				*lblLoading;
	UIBarButtonItem		*bbiTitle;
	
	TrainBookVC			*parentVC;
}

@property BOOL isFrom;
@property BOOL searching;
@property BOOL isShowingStates;

@property (strong, nonatomic) TrainBookVC			*parentVC;

@property (strong, nonatomic) IBOutlet UILabel			*lblStations;
@property (strong, nonatomic) IBOutlet UITableView		*tableList;
@property (strong, nonatomic) NSMutableArray			*aList;
@property (strong, nonatomic) NSMutableArray			*cityList;
@property (strong, nonatomic) NSArray					*stateList;
@property (strong, nonatomic) NSMutableArray			*aSections;
@property (strong, nonatomic) NSMutableArray			*cpListOfItems;

@property (strong, nonatomic) IBOutlet UIView				*loadingView;
@property (strong, nonatomic) IBOutlet UILabel				*lblLoading;
@property (strong, nonatomic) IBOutlet UIBarButtonItem		*bbiTitle;

@property (strong, nonatomic) IBOutlet UITextField	*txtFrom;
@property (strong, nonatomic) IBOutlet UITextField	*txtTo;

@property (strong, nonatomic) BookingCellData		*bcdFrom;
@property (strong, nonatomic) BookingCellData		*bcdTo;

@property (strong, nonatomic) IBOutlet UINavigationBar		*tbTop;
@property (strong, nonatomic) IBOutlet UIBarButtonItem		*cancelBtn;
@property (strong, nonatomic) IBOutlet UIBarButtonItem		*doneBtn;

@property (strong, nonatomic) IBOutlet UIToolbar			*tbBottom;

-(IBAction)textFromStarted:(id)sender;
-(IBAction)textToStarted:(id)sender;
//-(IBAction) startSearchFrom:(id)sender;
- (void) searchTableView:(NSString *)searchText;
-(void)configureToolbar;
-(void)initStations;
//-(IBAction) startSearchTo:(id)sender;

-(IBAction)cancelView:(id)sender;
-(IBAction)doneView:(id)sender;

-(void)fetchStations:(id)sender;

-(IBAction)showCities:(id)sender;
-(void)showCitiesInState:(NSString*)state;
-(IBAction)showStates:(id)sender;
-(void)makeListOfStates;
-(void)makeListOfCitiesInState:(NSString*)state;
//- (void)sortStationsByState;
-(void)configureStationsLabel;
-(void)scrollToFirstRow;

-(IBAction)textReset:(id)sender;

-(BOOL) existsInCopy:(TrainStationData *) currStation;

-(void)configureToolbar;

@end
