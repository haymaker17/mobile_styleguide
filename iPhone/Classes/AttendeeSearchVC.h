//
//  AttendeeSearchVC.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "AttendeeSearchDelegate.h"

@interface AttendeeSearchVC : MobileViewController <UITableViewDelegate, UITableViewDataSource, UISearchBarDelegate, UIScrollViewDelegate>
{
	id<AttendeeSearchDelegate>	__weak _delegate;
	NSArray						*attendeeExclusionList;

	UITableView					*resultsTableView;
	UISearchBar					*searchBar;
	UINavigationBar				*tBar;
	UIBarButtonItem				*cancelBtn;
	
	NSArray						*searchResults;
	NSString					*searchText;
	NSMutableDictionary			*searchHistory;  // key - searchText; val - results
}

@property (weak, nonatomic) id<AttendeeSearchDelegate>	delegate;
@property (strong, nonatomic) NSArray						*attendeeExclusionList;

@property (strong, nonatomic) IBOutlet UITableView			*resultsTableView;
@property (strong, nonatomic) IBOutlet UISearchBar			*searchBar;
@property (strong, nonatomic) IBOutlet UINavigationBar		*tBar;
@property (strong, nonatomic) IBOutlet UIBarButtonItem		*cancelBtn;

@property (strong, nonatomic) NSArray						*searchResults;
@property (strong, nonatomic) NSString						*searchText;
@property (strong, nonatomic) NSMutableDictionary			*searchHistory;

-(IBAction) btnCancel:(id)sender;

-(void) searchTextChanged:(NSString*) sText;
-(void) doSearch:(NSString*)text;


#pragma NoDataMasterViewDelegate methods
- (BOOL)adjustNoDataView:(NoDataMasterView*) negView;  // Return whether to hide toolbar
- (NSString *)titleForNoDataView;
- (NSString*) imageForNoDataView;
- (BOOL) canShowActionOnNoData;

@end
