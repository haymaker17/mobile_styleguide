//
//  AttendeeFullSearchVC.h
//  ConcurMobile
//
//  Created by yiwen on 9/19/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FormViewControllerBase.h"
#import "AttendeeSearchDelegate.h"
#import "LoadAttendeeForm.h"
#import "AttendeeFullSearchData.h"

// Implement AtnSearchDel protocol, so that the full search result screen can notify atn selection;
// In turn, it uses AtnSearchDel protocol to inform AtnListVC of final atn selection.
@interface AttendeeFullSearchVC : FormViewControllerBase <AttendeeSearchDelegate, UITableViewDelegate, UITableViewDataSource, 
    UISearchBarDelegate, UIScrollViewDelegate>
{
    id<AttendeeSearchDelegate>	__weak _delegate;
	NSArray						*attendeeExclusionList;

    UITableView					*resultsTableView;
	UISearchBar					*searchBar;
    
	UISegmentedControl          *seg;
    
//	NSArray						*mruList;
	NSArray						*searchResults;
	NSString					*searchText;
	NSMutableDictionary			*searchHistory;  // key - searchText; val - results
	BOOL						loadingAttendeeTypes;
	BOOL                        loadingAttendeeForm;
    NSArray                     *atnTypes;
    NSDictionary                *atnSearchForms; // SearchFields by atnTypeKey
    NSString                    *curAtnTypeKey;
//	ListItem					*selectedItem;

    // Info needed for rpeKey/ExpKey/polKey filtering
    NSArray                             *excludedAtnTypeKeys;
    NSString                            *rpeKey;
    NSString                            *expKey;
    NSString                            *polKey;

    // Display full search results after entry form is loaded, thus the need to cache either atnEntryForm of fullSearchResults
    LoadAttendeeForm                    *atnEntryForm; // cached atnEntryForm for the given atnTypeKey
    AttendeeFullSearchData              *fullSearchResults;
}

@property (weak, nonatomic) id<AttendeeSearchDelegate>	delegate;
@property (strong, nonatomic) NSArray						*attendeeExclusionList;
@property (nonatomic, strong) NSString								*expKey;
@property (nonatomic, strong) NSString								*polKey;
@property (nonatomic, strong) NSString								*rpeKey;

@property (strong, nonatomic) IBOutlet UITableView			*resultsTableView;
@property (strong, nonatomic) IBOutlet UISearchBar			*searchBar;
@property (strong, nonatomic) IBOutlet UISegmentedControl	*seg;

//@property (retain, nonatomic) NSArray						*mruList;
@property (strong, nonatomic) NSArray						*searchResults;
@property (strong, nonatomic) NSString						*searchText;
@property (strong, nonatomic) NSArray						*atnTypes;
@property (strong, nonatomic) NSDictionary                  *atnSearchForms;
@property (strong, nonatomic) NSMutableDictionary			*searchHistory;
@property BOOL loadingAttendeeTypes;
@property BOOL loadingAttendeeForm;
//
//@property (retain, nonatomic) ListItem						*selectedItem;
@property (strong, nonatomic) NSArray                       *excludedAtnTypeKeys;
@property (strong, nonatomic) NSString                      *curAtnTypeKey;
@property (strong, nonatomic) LoadAttendeeForm              *atnEntryForm;
@property (strong, nonatomic) AttendeeFullSearchData        *fullSearchResults;

- (void)setSeedData:(id<AttendeeSearchDelegate>)del keysToExclude:(NSArray*) exKeys expKey:(NSString*) expK polKey:(NSString*)pK rpeKey:(NSString*) rpeK;

// Quick Search APIs
-(void) searchTextChanged:(NSString*) sText;
-(void) doSearch:(NSString*)text;

-(IBAction)setSearchMode:(id)sender;
-(void) attendeeSelected:(AttendeeData*)attendee;

-(void) configureWaitView;
-(void) loadAttendeeTypes;
-(void) loadAttendeeSearchFields;
//-(void) showErrorMessage:(NSString*)message;

@end
