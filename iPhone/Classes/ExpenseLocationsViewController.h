//
//  ExpenseLocationsViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/21/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"


@protocol ExpenseLocationDelegate
- (void)selectedExpenseLocation:(NSString*)locationName;
@end


@interface ExpenseLocationsViewController : MobileViewController <UITableViewDelegate, UITableViewDataSource, UISearchBarDelegate>
{
	UITableView				*tableList;
	UISearchBar				*sbar;
	UINavigationBar			*tBar;
	UIBarButtonItem			*cancelBtn;
	NSMutableArray			*results;
	NSMutableDictionary		*prevResultsDicitonary; // key: search string, value: results array
	NSString				*originalLocationName;
	id<ExpenseLocationDelegate> __weak _delegate;
}

@property (nonatomic, strong) IBOutlet UITableView			*tableList;
@property (nonatomic, strong) IBOutlet UISearchBar			*sbar;
@property (nonatomic, strong) IBOutlet UINavigationBar		*tBar;
@property (nonatomic, strong) IBOutlet UIBarButtonItem		*cancelBtn;
@property (nonatomic, strong) NSMutableArray				*results;
@property (nonatomic, strong) NSMutableDictionary			*prevResultsDicitonary;
@property (nonatomic, strong) NSString						*originalLocationName;
@property (nonatomic, weak) id<ExpenseLocationDelegate>	delegate;

-(IBAction) closeView:(id)sender;

-(void) searchFor:(NSString*)searchText;

@end
