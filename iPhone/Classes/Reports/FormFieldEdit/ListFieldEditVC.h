//
//  ListFieldEditVC.h
//  ConcurMobile
//
//  Created by yiwen on 11/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FormFieldData.h"
#import "MobileViewController.h"
#import "ListItem.h"
#import "FieldEditDelegate.h"


@interface ListFieldEditVC : MobileViewController <UITableViewDelegate
    , UITableViewDataSource
    , UITextFieldDelegate
    , UIScrollViewDelegate
    , UISearchBarDelegate>
{
	FormFieldData				*field;
	id<FieldEditDelegate>		__weak _delegate;
	
	UITableView					*resultsTableView;
	UISearchBar					*searchBar;

	UISegmentedControl          *seg;
    UITextField                 *txtName;
    UILabel                     *lblTip;
    
	NSArray						*mruList;
	NSArray						*searchResults;
	NSString					*searchText;
	BOOL						useSearch;
	
	NSArray						*fullResults;
	NSMutableDictionary			*searchHistory;  // key - searchText; val - results
    NSMutableDictionary         *sections;  // holds sections
    NSMutableArray              *sectionKeys; // holds keys for section

	NSString					*rptKey;			// For ReceiptType
    NSArray                     *excludedKeys;      // e.g. Excluded AtnTypeKeys for pol/expKey
    
    ListItem                    *externalItem;   // selected External ListItem to be saved and refreshed
    
    BOOL                        searchBarHidden; // Default to NO, set to YES by hideSearchBar.  Can only hide once and cannot unhide.
}

@property (strong, nonatomic) IBOutlet UITableView			*resultsTableView;
@property (strong, nonatomic) IBOutlet UISearchBar			*searchBar;
@property (strong, nonatomic) IBOutlet UISegmentedControl	*seg;
@property (strong, nonatomic) IBOutlet UITextField          *txtName;
@property (strong, nonatomic) IBOutlet UILabel              *lblTip;

@property (strong, nonatomic) FormFieldData					*field;
@property (weak, nonatomic) id<FieldEditDelegate>           delegate;
@property (strong, nonatomic) NSArray						*mruList;
@property (strong, nonatomic) NSArray						*searchResults;
@property (strong, nonatomic) NSString						*searchText;
@property (strong, nonatomic) NSArray						*fullResults;
@property (strong, nonatomic) NSMutableDictionary			*searchHistory;
@property (strong, nonatomic) NSMutableDictionary			*sections;
@property (strong, nonatomic) NSMutableArray                *sectionKeys;
@property BOOL useSearch;
@property BOOL disableAutoCorrectinSearch;

@property (strong, nonatomic) ListItem						*externalItem;
@property (strong, nonatomic) NSString						*rptKey;
@property (strong, nonatomic) NSArray                       *excludedKeys;

- (void)setSeedData:(FormFieldData*)field delegate:(id<FieldEditDelegate>)del keysToExclude:(NSArray*) exclKeys;

-(void)searchBarSearchButtonClicked:(UISearchBar *)searchBar;
-(void)searchBar:(UISearchBar*)searchBar textDidChange:(NSString*)searchText;

-(IBAction)setComboMode:(id)sender;

// Call this API in prefetchForListEditor override to hide search bar in list field editor.
-(void) hideSearchBar;


-(NSArray*) filterAndaddNoneItem:(NSArray*) listItems;
-(BOOL) isCombo;
-(BOOL) enoughTextChange:(NSString*) sText;
@end
