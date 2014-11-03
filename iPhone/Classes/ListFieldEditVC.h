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


@interface ListFieldEditVC : MobileViewController <UITableViewDelegate, UITableViewDataSource, UISearchBarDelegate> 
{
	FormFieldData				*field;
	id<FieldEditDelegate>		_delegate;
	
	UITableView					*resultsTableView;
	UISearchBar					*searchBar;
	UINavigationBar				*tBar;
	UIBarButtonItem				*cancelBtn;
	UIBarButtonItem				*doneBtn;

	NSArray						*mruList;
	NSArray						*searchResults;
	NSString					*searchText;
	BOOL						useSearch;
	
	NSArray						*fullResults;
	NSMutableDictionary			*searchHistory;  // key - searchText; val - results

	ListItem					*selectedItem;
	
	UILabel						*lblListFieldValue, *lblListFieldTitle;
	UIButton					*btnBackListField;
	NSString					*rptKey;			// For ReceiptType
}

@property (retain, nonatomic) IBOutlet UITableView			*resultsTableView;
@property (retain, nonatomic) IBOutlet UISearchBar			*searchBar;
@property (retain, nonatomic) IBOutlet UINavigationBar		*tBar;
@property (retain, nonatomic) IBOutlet UIBarButtonItem		*cancelBtn;
@property (retain, nonatomic) IBOutlet UIBarButtonItem		*doneBtn;

@property (retain, nonatomic) IBOutlet UILabel				*lblListFieldValue;
@property (retain, nonatomic) IBOutlet UILabel				*lblListFieldTitle;
@property (retain, nonatomic) IBOutlet UIButton				*btnBackListField;

@property (retain, nonatomic) FormFieldData					*field;
@property (assign, nonatomic) id<FieldEditDelegate>		delegate;
@property (retain, nonatomic) NSArray						*mruList;
@property (retain, nonatomic) NSArray						*searchResults;
@property (retain, nonatomic) NSString						*searchText;
@property (retain, nonatomic) NSArray						*fullResults;
@property (retain, nonatomic) NSMutableDictionary			*searchHistory;
@property BOOL useSearch;

@property (retain, nonatomic) ListItem						*selectedItem;
@property (retain, nonatomic) NSString						*rptKey;

-(IBAction) btnCancel:(id)sender;
-(IBAction) btnDone:(id)sender;

-(void)searchBarSearchButtonClicked:(UISearchBar *)searchBar;
-(void)searchBar:(UISearchBar*)searchBar textDidChange:(NSString*)searchText;

+(BOOL) canUseListEditor:(FormFieldData*)field;

@end
