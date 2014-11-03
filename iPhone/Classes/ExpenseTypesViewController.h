//
//  ExpenseTypesViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "ExpenseTypeData.h"
#import "ReportData.h"
#import "ExpenseTypeDelegate.h"

#import "CarRatesData.h"

//@class OutOfPocketFormViewController;
@class ExpenseTypesCollection;

@interface ExpenseTypesViewController : MobileViewController <UITableViewDelegate 
    , UITableViewDataSource
    , UISearchBarDelegate
    , UIScrollViewDelegate>
{
	UITableView				*tableList;
	NSMutableArray			*dupListOfItems;
	NSMutableDictionary		*items, *sections;
	MobileViewController	*parentMVC;
	
	// Mob-2513 Localization of Expenses Header and cancel button
	//UIToolbar				*tBar;
	UINavigationBar			*tBar;
	UIBarButtonItem			*cancelBtn;
	
	IBOutlet UISearchBar	*searchBar;
	BOOL                    searching;
	BOOL                    letUserSelectRow;

	NSString				*expKey;

	id<ExpenseTypeDelegate> __weak _delegate;
	
	BOOL					isAcceptingData;

	NSString				*polKey;
	
	// Whether we are selecting expense types for (YES)child only or for (NO)both regular and parents expenses.
//	BOOL					isForChild;
	NSString                *parentExpKey;
    
	NSString				*_expenseTypesEndPointVersion;
	ExpenseTypesCollection	*etCol;
	
	ExpenseTypeData			*selectedExpenseType;
    
    ReportData              *rpt; // Optional, to check crnCode against car config
}
@property (strong, nonatomic) IBOutlet UITableView			*tableList;
@property (strong, nonatomic) NSMutableDictionary			*items;
@property (strong, nonatomic) NSMutableDictionary			*sections;
@property (strong, nonatomic) MobileViewController			*parentMVC;

// Mob-2513 Localization of Expenses Header and cancel button
//@property (retain, nonatomic) IBOutlet UIToolbar			*tBar;
@property (strong, nonatomic) IBOutlet UINavigationBar		*tBar;
@property (strong, nonatomic) IBOutlet UIBarButtonItem		*cancelBtn;

@property (strong, nonatomic) NSMutableArray				*dupListOfItems;

@property (strong, nonatomic) NSString				*expKey;
@property (strong, nonatomic) NSString				*parentExpKey;

@property (nonatomic, weak) id<ExpenseTypeDelegate> delegate;
@property BOOL isAcceptingData;
@property (strong, nonatomic) NSString						*polKey;
//@property BOOL isForChild;
@property (strong, nonatomic) NSString						*expenseTypesEndPointVersion;
@property (strong, nonatomic) ExpenseTypesCollection		*etCol;

@property (strong, nonatomic) ExpenseTypeData				*selectedExpenseType;
@property (strong, nonatomic) ReportData                    *rpt;
@property (strong, nonatomic) CarRatesData *carRatesData;


-(IBAction) closeView:(id)sender;
- (void) searchTableView;
- (void) doneSearching_Clicked:(id)sender;

-(void) selectCurrentExpenseType:(NSString *)thisExpKey;
-(void)layoutPad;
-(void)gotExpenseTypesData:(ExpenseTypeData*) parentExpType;

-(void) notifyDelegateOfSelectedExpenseType;

// Utility API to show expense type with filtering for child/parent.
+(void)showExpenseTypeEditor:(id<ExpenseTypeDelegate>)delegate  
    policy:(NSString*)polKey 
    parentVC:(MobileViewController*) pvc 
    selectedExpKey:(NSString*) expKey parentExpKey:(NSString*)parentExpKey 
    withReport:(ReportData*) rptData;

@end
