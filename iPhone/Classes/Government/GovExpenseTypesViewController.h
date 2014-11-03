//
//  GovExpenseTypesViewController.h
//  ConcurMobile
//
//  Created by ernest cho on 9/25/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MobileViewController.h"
#import "FieldEditDelegate.h"
#import "GovExpenseTypeDelegate.h"
#import "GovExpenseTypesData.h"

@interface GovExpenseTypesViewController : MobileViewController <UITableViewDelegate, UITableViewDataSource, UITextFieldDelegate, UIScrollViewDelegate, UISearchBarDelegate>
{
    FormFieldData                                       *field;
	id<FieldEditDelegate, GovExpenseTypeDelegate>       __weak _delegate;
	
	UITableView                                         *resultsTableView;
	UISearchBar                                         *searchBar;
    
	NSArray                                             *searchResults;
	NSString                                            *searchText;	
	NSArray                                             *fullResults;
	NSMutableDictionary                                 *searchHistory;  // key - searchText; val - results
    
    GovExpenseTypesData                                     *expenseTypes;
}

@property (strong, nonatomic) IBOutlet UITableView			*resultsTableView;
@property (strong, nonatomic) IBOutlet UISearchBar			*searchBar;

@property (strong, nonatomic) FormFieldData                 *field;
@property (strong, nonatomic) NSArray						*searchResults;
@property (strong, nonatomic) NSString						*searchText;
@property (strong, nonatomic) NSArray						*fullResults;
@property (strong, nonatomic) NSMutableDictionary			*searchHistory;
@property (strong, nonatomic) GovExpenseTypesData               *expenseTypes;

@property (weak, nonatomic) id<FieldEditDelegate, GovExpenseTypeDelegate>         delegate;

- (void)setSeedData:(FormFieldData*)field delegate:(id<FieldEditDelegate, GovExpenseTypeDelegate>)del;
- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar;
- (void)searchBar:(UISearchBar*)searchBar textDidChange:(NSString*)searchText;
- (void)updateDelegates:(FormFieldData *)textField;

-(void) setListResult:(NSString*) text;

@end
