//
//  ApproverSearchVC.h
//  ConcurMobile
//
//  Created by yiwen on 8/26/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ApproverSearchDelegate.h"
#import "ApproverInfo.h"

@interface ApproverSearchVC : MobileViewController 
    < UITableViewDelegate
    , UITableViewDataSource
    , UIScrollViewDelegate
    , UISearchBarDelegate >  
{
    
    NSString                    *rptKey;
    id<ApproverSearchDelegate>	__weak _delegate;

    UITableView					*resultsTableView;
	UISearchBar					*searchBar;
    
	UISegmentedControl          *seg;
    UILabel                     *lblTip;
    
//	NSArray						*mruList;
	NSArray						*searchResults;
	NSString					*searchText;
	
	NSArray						*fullResults;
	NSMutableDictionary			*searchHistory;  // key - searchText; val - results
    
	ApproverInfo				*selectedItem;
    UIBarButtonItem             *btnSubmit;
}

@property (strong, nonatomic) IBOutlet UITableView			*resultsTableView;
@property (strong, nonatomic) IBOutlet UISearchBar			*searchBar;
@property (strong, nonatomic) IBOutlet UISegmentedControl	*seg;
@property (strong, nonatomic) IBOutlet UILabel              *lblTip;

@property (strong, nonatomic) ApproverInfo                  *selectedItem;
@property (strong, nonatomic) NSString                      *rptKey;
@property (weak, nonatomic) id<ApproverSearchDelegate>     delegate;
@property (strong, nonatomic) NSArray						*searchResults;
@property (strong, nonatomic) NSString						*searchText;
@property (strong, nonatomic) NSArray						*fullResults;
@property (strong, nonatomic) NSMutableDictionary			*searchHistory;
@property (strong, nonatomic) UIBarButtonItem               *btnSubmit;

- (void)setSeedData:(NSString*)reportKey approver:(ApproverInfo*)curApp canDrawSubmit:(BOOL)canSubmit delegate:(id<ApproverSearchDelegate>)del;

-(IBAction)setSearchField:(id)sender;

@end
