//
//  SearchYodleeCardsVC.h
//  ConcurMobile
//
//  Created by yiwen on 11/3/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "MobileViewController.h"

@interface SearchYodleeCardsVC : MobileViewController<UITableViewDelegate, 
    UITableViewDataSource, 
    UISearchBarDelegate, 
    UIScrollViewDelegate>
{
    UITableView					*searchResultsView;
    UITableView					*popularCardsView;
	UISearchBar					*searchBar;
    
	UISegmentedControl          *seg;
    
	NSArray						*popularCards;
    
	NSArray						*searchResults;
	NSString					*searchText;
	NSMutableDictionary			*searchHistory;  // key - searchText; val - results
    
}

@property (strong, nonatomic) IBOutlet UITableView			*searchResultsView;
@property (strong, nonatomic) IBOutlet UITableView			*popularCardsView;
@property (strong, nonatomic) IBOutlet UISearchBar			*searchBar;
@property (strong, nonatomic) IBOutlet UISegmentedControl	*seg;

@property (strong, nonatomic) NSArray						*searchResults;
@property (strong, nonatomic) NSArray						*popularCards;
@property (strong, nonatomic) NSString						*searchText;
@property (strong, nonatomic) NSMutableDictionary			*searchHistory;

// Quick Search APIs
-(void) searchTextChanged:(NSString*) sText;
-(void) doSearch:(NSString*)text;

-(IBAction)setSearchMode:(id)sender;

//-(void) configureWaitView;

@end
