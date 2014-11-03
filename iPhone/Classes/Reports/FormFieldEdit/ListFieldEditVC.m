//
//  ListFieldEditVC.m
//  ConcurMobile
//
//  Created by yiwen on 11/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ListFieldEditVC.h"
#import "ExSystem.h" 
#import "RootViewController.h"
#import "ListFieldSearchData.h"
#import "MRUManager.h"
#import "ReportEntryViewController.h"
#import "ListFieldTableCell.h"

@interface ListFieldEditVC(Private)
-(void) finishListEditing:(ListItem*) li;
-(ListItem*) makeNoneItem;
@end

@implementation ListFieldEditVC
@synthesize delegate = _delegate;
@synthesize field, resultsTableView, searchBar, rptKey;
@synthesize mruList, searchResults, searchText, fullResults, searchHistory,sections,sectionKeys, useSearch;
@synthesize seg, lblTip, txtName, excludedKeys;
@synthesize externalItem;

BOOL searching;
BOOL isFullList;

const NSString *allItemsKey = @"All Items";

-(BOOL) isCombo
{
	return [self.field.ctrlType isEqualToString:@"combo"];
}

- (void)setSeedData:(FormFieldData*)fld delegate:(id<FieldEditDelegate>)del keysToExclude:(NSArray*) exKeys
{
    self.delegate = del;
	self.field = fld;
    self.excludedKeys = exKeys;
    searchBarHidden = NO;
    //MOB-16901 - By default autocorrect is on
    self.self.disableAutoCorrectinSearch = NO;
}

- (void)dealloc 
{
    resultsTableView.delegate = nil;
    resultsTableView.dataSource = nil;
}

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	self.title = [self.field getFullLabel];
	
	if(![UIDevice isPad])
	{
		searchBar.tintColor = [UIColor darkBlueConcur_iOS6];
        searchBar.alpha = 0.9f;
	} 
    else
    {
		searchBar.tintColor = [UIColor navBarTintColor_iPad];
        searchBar.alpha = 0.9f;        
    }
    
	
	self.searchHistory = [[NSMutableDictionary alloc] init];
	
    self.sections = [[NSMutableDictionary alloc] init];
    self.sectionKeys = [[NSMutableArray alloc] init];
	searching = NO;
    
    if (self.searchResults == nil)
    {
        [self showLoadingView];
    }
    
    if (searchBarHidden)
        [self hideSearchBarIfViewLoaded];
        //MOB-16901
    if (self.disableAutoCorrectinSearch) {
        self.searchBar.autocorrectionType = UITextAutocorrectionTypeNo;
    }
    
	// Show soft gray background
	[self.view setBackgroundColor:[UIColor baseBackgroundColor]];
}

- (void)viewWillAppear:(BOOL)animated
{
	//self.searchBar.text = initialSearchLocation.location;
	// TODO - display the existing cell.
	if (self.field != nil)
	{
// TODO - preselect list item		
//		self.lblListFieldValue.text = self.field.fieldValue;
		
		if (([self.field.ctrlType isEqualToString:@"checkbox"] || [field.dataType isEqualToString:@"BOOLEANCHAR"])
			&& self.searchResults == nil)
		{
			ListItem* yesItem = [[ListItem alloc] init];
			yesItem.liKey = @"Y";
			yesItem.liName = [Localizer getLocalizedText:@"Yes"];
			ListItem* noItem = [[ListItem alloc] init];
			noItem.liKey = @"N";
			noItem.liName = [Localizer getLocalizedText:@"No"];
			self.searchResults = @[yesItem, noItem];
            [self hideSearchBar];
//			[self.searchBar removeFromSuperview];
//			CGRect frame = self.resultsTableView.frame;
//			self.resultsTableView.frame = CGRectMake(frame.origin.x, frame.origin.y-44, frame.size.width, frame.size.height+44);
//			[self.resultsTableView reloadData];
//            [self hideLoadingView];
		}
		else if ([self.field.iD isEqualToString:@"CarKey"] && self.searchResults == nil)
		{
            // self.navcontroller.items/view array next viewc count-2 iskindof class ReportEntryvc then cast look in there 
            BOOL isPers = YES;
            
            NSArray *vcStack = self.navigationController.viewControllers;
            if (vcStack != nil && vcStack.count >=2)
            {
                NSObject *ob = vcStack[vcStack.count-2];
                if ([ob isKindOfClass:[ReportEntryViewController class]])
                {
                    ReportEntryViewController *revc = (ReportEntryViewController*)ob;
                    isPers = [revc isPersonalCarMileageExpType:[revc getCurrentExpType]];  
                }
            }
            
            
			NSMutableDictionary *dict = isPers?
                [[ConcurMobileAppDelegate findRootViewController].carRatesData fetchPersonalCarDetails:@""] :
                [[ConcurMobileAppDelegate findRootViewController].carRatesData fetchCompanyCarDetails:@""];
            
			NSMutableArray *a = [[NSMutableArray alloc] initWithObjects:nil];
			
			for(NSString *key in dict)
			{
				CarDetailData *cd = dict[key];
				
				ListItem* li = [[ListItem alloc] init];
				li.liKey = cd.carKey;
				li.liName = cd.vehicleId;
				
				[a addObject:li];
			}

			self.searchResults = [[NSArray alloc] initWithArray:a];
            
            // Add search results to the sections
            sections[allItemsKey] = searchResults;
            [sectionKeys addObject:allItemsKey];

			[self.searchBar removeFromSuperview];
			CGRect frame = self.resultsTableView.frame;
			self.resultsTableView.frame = CGRectMake(frame.origin.x, frame.origin.y-44, frame.size.width, frame.size.height+44);
			[self.resultsTableView reloadData];
            [self hideLoadingView];
		}
        else if ([FormFieldData isLocationFieldId:self.field.iD] && self.searchResults == nil)
        {
            NSArray *locList = [[MRUManager sharedInstance] getLocations];
            
            if(locList != nil && [locList count] !=0)
            {
                NSMutableArray* list = [[NSMutableArray alloc] initWithCapacity:[locList count]];
                for (ListItem *listItem in locList)
                {
                    if (listItem != nil) {
                        [list addObject:listItem];
                    }
                }
                
                // If we're offline...
                if (![ExSystem connectedToNetwork])
                {
                    // If this field is not required, then add the 'None' option to the end of the MRU list
                    if (![self.field isRequired])
                        [list addObject:[self makeNoneItem]];
                    
                    // Set fullResults to the list we have (from the MRU). By making fullResults available, we cause searches to work on the fullResults list instead of querying the server.  See the searchTextChanged method in this class.
                    self.fullResults = list;
                    
                    // When searching for locations offline, an empty search string should yield the whole list (which is comprised of MRU entries)
                    searchHistory[@""] = list;
                }
                
                //self.searchResults = list;
                self.mruList = list;
                sections[MRUKEY] = list;
                
                if(![sectionKeys containsObject:MRUKEY])
                    [sectionKeys insertObject:MRUKEY atIndex:0];

                //[self.resultsTableView reloadData];
                [self hideLoadingView];
            }
        }
        else if ( [FormFieldData isCurrencyFieldId:self.field.iD]  && self.searchResults == nil)
        {
            NSArray *currMruList = [[MRUManager sharedInstance] getMRUsByType:self.field.iD];
            
            if(currMruList != nil && [currMruList count] !=0) 
            {
                NSMutableArray* list = [[NSMutableArray alloc] initWithCapacity:[currMruList count]];
                for (EntityMRU*eMru in currMruList)
                {
                    ListItem* li = [[ListItem alloc] init];
                    li.liKey = eMru.key == nil ? nil : [NSString stringWithFormat:@"%d", [eMru.key intValue]];
                    li.liName = eMru.value;
                    li.liCode = eMru.code;
                    [list addObject:li];
                }
                //self.searchResults = list;
                self.mruList = list;
                sections[MRUKEY] = list;
                
                if(![sectionKeys containsObject:MRUKEY])
                    [sectionKeys insertObject:MRUKEY atIndex:0];
                [self hideLoadingView];
            }
        }

	}
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

-(NSArray*) filterAndaddNoneItem:(NSArray*) listItems
{
    if ([self.field isRequired] && self.excludedKeys == nil)
        return listItems;
    
    NSMutableArray* mli = (NSMutableArray*) listItems;
    if (mli == nil)
    {
        mli = [NSMutableArray array];
    }
    
    if (![self.field isRequired])
    {
        [mli insertObject:[self makeNoneItem] atIndex:0];
    }
    
    if (self.excludedKeys != nil)
    {
        NSDictionary* dict = [[NSDictionary alloc] initWithObjects:self.excludedKeys forKeys:excludedKeys];
        NSMutableArray* exLis = [[NSMutableArray alloc] init];
        for (ListItem* li in mli)
        {
            if (dict[li.liKey] != nil)
            {
                [exLis addObject:li];
            }
        }
        [mli removeObjectsInArray:exLis];
    }
    return mli;
}

-(ListItem*) makeNoneItem
{
    ListItem* noneItem = [[ListItem alloc] init];
    noneItem.liKey = nil;
    noneItem.liCode = @"None";
    noneItem.liName = [Localizer getLocalizedText:@"None"];
    return noneItem;
}

-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];
	//respond to data that might be coming from the cache
	if ([msg.idKey isEqualToString:LIST_FIELD_SEARCH_DATA])
	{
		if (msg.responseCode == 200 || msg.isCache)
		{
			ListFieldSearchData*data = (ListFieldSearchData*) msg.responder;
            
            // MOB-9660 If external list item search succeeded, finish list editing
            if (self.externalItem != nil && [@"CODE" isEqualToString:data.searchBy] && [self.externalItem.liCode isEqualToString:data.query])
            {
                [self hideWaitView];

                // Expect the result to be exactly 1 item, since the liCode is a unique externalId for the list item.
				// Check for 1+, in case something strange happens on the server side.
                if (data.listItems.count > 0)
                {
                    [self finishListEditing:(data.listItems)[0]];
                }
                else 
                {
                    UIAlertView *alert = [[MobileAlertView alloc] 
                                          initWithTitle:nil
                                          message:[@"Failed to save external list item" localize]
                                          delegate:nil 
                                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                          otherButtonTitles:nil];
                    [alert show];

                }
                return;
            }
            
            [self hideLoadingView];
			self.searchResults = [self filterAndaddNoneItem:data.listItems];
           
            // Currency list contains mru list at begining, Try removing duplicates in currency
            // Temp workaround until server code is fixed
            if([data.fieldId isEqualToString:@"CrnKey"])
            {
                
                NSMutableDictionary *distinctCopy = [[NSMutableDictionary alloc]init];
                for(ListItem *item in searchResults)
                {
                    distinctCopy[item.liCode] = item;
                }
                NSMutableArray *tempArray = [NSMutableArray arrayWithArray:[distinctCopy allValues]];
                NSSortDescriptor *aSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"liName" ascending:YES];
                [tempArray sortUsingDescriptors:@[aSortDescriptor]];
                searchResults = tempArray;
            }
            
            sections[allItemsKey] = searchResults;
            if(![sectionKeys containsObject:allItemsKey])
                [sectionKeys addObject:allItemsKey];
 
            if (data.listItems.count < 500)
                isFullList = TRUE;
            else
                isFullList = FALSE;
			if ((data.listItems == nil || isFullList)
				&& (data.query == nil || [data.query isEqualToString:@""]) // always add all items to sections. 
				&& ![data.fieldId isEqualToString:@"LnKey"])
			{
				self.fullResults = self.searchResults;
 			}
			
			if (!data.isMru)
				searchHistory[(data.query==nil?@"":data.query)] = self.searchResults;
			else {
				self.mruList = self.searchResults;
                sections[MRUKEY] = mruList;
			}

			if ([self isViewLoaded])
			{
				[self.resultsTableView reloadData];
			}
			// TODO - Select the current value
		}
		// TODO - Handle error
	}
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    if(searching) // show only 1 section when searching
        return 1;
    else
        return [sectionKeys count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	NSString *key = sectionKeys[section];
    NSMutableArray *nameSection = sections[key];
	
    // while searching there is only one section so search in allkeys
	if (searching) 
    {
		return [sections[allItemsKey] count] ;
    }
	else
		return [nameSection count];
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"ListFieldTableCell";
	        
    ListFieldTableCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[ListFieldTableCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }

    ListItem *li = [self listItemForRowAtIndexPath:indexPath];
    cell.text.text = [self textForListItem:li];
    
    if ([self showCheckForListItem:li]) {
		cell.accessoryType = UITableViewCellAccessoryCheckmark;
    } else {
		cell.accessoryType = UITableViewCellAccessoryNone;
    }

    return cell;
}

- (BOOL)showCheckForListItem:(ListItem *)li
{
    // MOB-11717 do not set the mark it checked if the likey and field value is nil
    if ((field.liKey != 0 &&  [li.liKey isEqualToString:self.field.liKey] ) || (li.liCode != nil && [li.liCode isEqualToString:self.field.liCode])) {
        return YES;
    } else {
        return NO;
    }
}

- (NSString *)textForListItem:(ListItem *)li
{
    NSString *text = @"";

    // MOB-8315 Hide code in Expense Clasification field (picklist/LIST) field for Citi
    BOOL hideCode = [field.ctrlType isEqualToString:@"picklist"] && [field.dataType isEqualToString:@"LIST"];

    // MOB-10603 Pfizer - project codes not appearing on report header on mobile
    BOOL overrideHide = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"MobileViewPicklistCodes" withType:@"OTMODULE"]];

    if (overrideHide)
        hideCode = NO;

	if (!hideCode && li.liCode != nil && !(li.liKey == nil && [li.liCode isEqualToString:@"None"])) {
        text = [NSString stringWithFormat:@"(%@) %@", li.liCode, li.liName];
    } else {
        text = li.liName;
	}
    return text;
}

- (ListItem *)listItemForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
	NSUInteger section = [indexPath section];
	NSString *key;
	NSMutableArray *itemArray;

	if (searching)  // When searching search in all items
	{
        key = (NSString *)allItemsKey;
	}
	else
	{
        key = sectionKeys[section];
	}
    itemArray = sections[key];

    return itemArray[row];
}

// using Wannys trick for calculating tablecell height
// basically we create the cell and measure the variable area, then add it to the static area.
// it's expensive but it's not an issue until you hit a large number of rows
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"ListFieldTableCell";
    ListFieldTableCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[ListFieldTableCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }

    ListItem *li = [self listItemForRowAtIndexPath:indexPath];

    if ([self showCheckForListItem:li]) {
		cell.accessoryType = UITableViewCellAccessoryCheckmark;
    } else {
		cell.accessoryType = UITableViewCellAccessoryNone;
    }

    cell.text.text = [self textForListItem:li];

    CGSize size = [cell.text sizeThatFits:CGSizeMake(cell.frame.size.width, FLT_MAX)];

    // 10 is the buffer on the cell
    return (size.height + 10);
}

#pragma mark -
#pragma mark Table view delegate

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    NSString *key = sectionKeys[section];
	
	if(searching)
		key = [Localizer getLocalizedText:@"Search Results"];
    else
        key = ([Localizer hasLocalizedText:key] ? [Localizer getLocalizedText:key] : nil);
    
    return key;
}

-(void) setListResult:(ListItem*) li
{
	if (li.liKey != nil)
	{
		if ([li.isMru isEqualToString:@"Y"] || [li.isMru isEqualToString:@"L"])
		{
			// Mark the liKey with a "-" at the end
			if ([li.liKey length] == 0 || [li.liKey characterAtIndex:[li.liKey length]-1] != '-')
			{
				field.liKey = [NSString stringWithFormat:@"%@-", li.liKey];
			}
			else 
			{
				field.liKey = li.liKey;
			}

		}
		else
			field.liKey = li.liKey;
	}
    // MOB-7947 In prev releases, we store liKey as "0" for "None" in MRU, so we need to convert them here.
    if ((li.liKey == nil || [li.liKey isEqualToString:@"0"]) && [li.liCode isEqualToString:@"None"])
    {
        // Empty out the field
        field.liKey = nil;
        field.liCode = nil;
        field.fieldValue = nil;
        field.extraDisplayInfo = nil;
    }
    else
    {
        if (li.liCode != nil) {
            field.liCode = li.liCode;
        } 
        if (li.liName != nil) {
            field.fieldValue = li.liName;
        }
        
        // clear the extra data in the field if we don't have any
        if (li.fields != nil) {
            field.extraDisplayInfo = li.fields;
        } else {
            field.extraDisplayInfo = nil;
        }
    }
    
    // Save location to MRU
    if ([FormFieldData isLocationFieldId:field.iD]) {
        [[MRUManager sharedInstance] saveLocation:li];
    }

    // Save currency to MRU
    if ([FormFieldData isCurrencyFieldId:field.iD]) {
        [[MRUManager sharedInstance] saveCurrency:li];
    }
}

-(void) setEditResult:(NSString*) text
{
	field.liKey = nil;
	field.liCode = nil;
	field.fieldValue = text;
}

-(void) finishListEditing:(ListItem*) li
{
	[self setListResult:li];
    [self.navigationController popViewControllerAnimated:YES];
    // call delegate
    if (self.delegate != nil)
        [self.delegate fieldUpdated:self.field];
}

-(void) retrieveExternalItem:(ListItem*) li
{
    // If we previously have retrieved the external list item and it matches current selection, do nothing.
    if (self.externalItem.liKey != nil &&
        li.liCode != nil && [li.liCode isEqualToString:self.externalItem.liCode])
    {
        [self finishListEditing:li];
    }
    else 
    {
        [self showWaitView];
        
        // Make a copy of selected item, nil out liKey as a flag of whether valid liKey has been retrieved
        // Send list search msg with SEARCHBY=CODE to try to save the external item to internal db.
        self.externalItem = [li copy];
        self.externalItem.liKey = nil;
        
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: field, @"FIELD", @"CODE", @"SEARCH_BY", self.externalItem.liCode, @"QUERY", nil];

        if ([field.iD isEqualToString:@"ReceiptType"] && self.rptKey != nil)
        {
            pBag[@"RPT_KEY"] = self.rptKey;
        }

        [[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
	NSUInteger section = [indexPath section];
	NSString *key;
	NSMutableArray *itemArray;
    // MOB-11236
	if (searching)  // When searching search in all items
	{
        key = (NSString *)allItemsKey;
	}
	else
	{
        key = sectionKeys[section];
	}
    
    itemArray = sections[key];
    
    // Get the list item
    ListItem *li = itemArray[row];
    
    // MOB-9960 Handle external list
    if ([@"Y" isEqualToString: li.external])
    {
        [self retrieveExternalItem:li];
    }
    else 
    {
        [self finishListEditing:li];
    }
}

#pragma mark -
#pragma mark Cancel and Close Methods

-(IBAction) btnCancel:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];	
	// call delegate
	if (self.delegate != nil)
		[self.delegate fieldCanceled:self.field];
	
}

-(IBAction) btnDone:(id)sender
{
	BOOL listValueFound = NO;
	if (self.searchResults != nil && [self.searchResults count]>0)
	{
		int nCount = [self.searchResults count];
		for (int ix = 0; ix < nCount; ix++)
		{
			ListItem* li = (self.searchResults)[ix];
			if (li != nil && [li.liName caseInsensitiveCompare:self.field.fieldValue]==NSOrderedSame)
			{
				[self setListResult:li];
				listValueFound = YES;
				break;
			}
		}
	}
	if (!listValueFound)
		[self setEditResult:self.field.fieldValue];
	
	[self dismissViewControllerAnimated:YES completion:nil];	
	// call delegate
	if (self.delegate != nil)
		[self.delegate fieldUpdated:self.field];
	
}


-(BOOL) enoughTextChange:(NSString*) sText
{
	int sLen = [sText length];
	int cLen = self.searchText == nil? 0 : [self.searchText length];
	if (sLen > cLen + 2 || sLen < cLen -2)
		return YES;
	
	int minLen = sLen > cLen? cLen:sLen;
	int maxLen = sLen > cLen? sLen:cLen;
	int firstDiffPos = 0;
	for (int firstDiffPos = 0; firstDiffPos <minLen; firstDiffPos++)
	{
		unichar sChar = [sText characterAtIndex:firstDiffPos];
		unichar cChar = [self.searchText characterAtIndex:firstDiffPos];
		if (sChar != cChar)
			break;
	}
	return (maxLen-firstDiffPos)>=3;
}

-(void) searchTextChanged:(NSString*) sText forceUpdate:(BOOL) flag
{
	if (sText == nil)
		sText = @"";

	if ([sText isEqualToString:self.searchText])
		return;

// This comment is for GOV search field bug. Enable this if Gov search don't show full list result when search string is empty, or when search shows mis-match result.
//        if ([sText length] == 0)
//    		sText = @"";
//    	else if ([sText isEqualToString:self.searchText])
//    		return;
    
	if ([self isCombo])
		self.field.fieldValue = sText;
	
	if ((self.searchHistory)[sText] != nil)
	{
		self.searchResults = (self.searchHistory)[sText];
        sections[allItemsKey] = self.searchResults;
		self.searchText = sText;
		[self.resultsTableView reloadData];
	}
	else if (self.fullResults != nil && isFullList && ![NSString isEmptyIgnoreWhitespace:sText])
	{
		NSMutableArray* sResult = [[NSMutableArray alloc] init];
		for (ListItem* item in fullResults)
		{
			NSRange r = [item.liName rangeOfString:sText options:NSCaseInsensitiveSearch];
			if (r.location != NSNotFound)
			{
				[sResult addObject:item];
			}
			else if (item.liCode != nil)
			{
				r = [item.liCode rangeOfString:sText options:NSCaseInsensitiveSearch];
				if (r.location != NSNotFound)
					[sResult addObject:item];
			}
		}
		self.searchResults = sResult;
        // update all items with search result
        sections[allItemsKey] = sResult;
		self.searchText = sText;		
		[self.resultsTableView reloadData];
	}
	else 
	{
		if (flag || [self enoughTextChange:sText])
            [self getServerSearchResultWithText:sText];
        
        else if ([NSString isEmptyIgnoreWhitespace:sText] && self.fullResults != nil)   // user clear the search text
        {
            sections[allItemsKey] = self.fullResults;
            [self.resultsTableView reloadData];
        }
        else if ([NSString isEmptyIgnoreWhitespace:sText])
        {
            sections[allItemsKey] = self.mruList;
            [self.resultsTableView reloadData];
        }
	}
}

-(void) getServerSearchResultWithText:(NSString*)sText
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: field, @"FIELD", sText, @"QUERY", nil];
    if ([field.iD isEqualToString:@"ReceiptType"] && self.rptKey != nil)
    {
        pBag[@"RPT_KEY"] = self.rptKey;
    }
    
    [[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

#pragma Hide search bar 
-(void) hideSearchBarIfViewLoaded
{
    if ([self isViewLoaded])
    {
        [self.searchBar removeFromSuperview];
        CGRect frame = self.resultsTableView.frame;
        self.resultsTableView.frame = CGRectMake(frame.origin.x, frame.origin.y-44, frame.size.width, frame.size.height+44);
        [self.resultsTableView reloadData];
        [self hideLoadingView];
    }    
}
-(void) hideSearchBar
{
    if (searchBarHidden)
        return;
    
    searchBarHidden = YES;
    if (searchBarHidden)
    {
        [self hideSearchBarIfViewLoaded];
    }
}

#pragma mark -
#pragma mark UISearchBarDelegate Methods
- (void)searchBarSearchButtonClicked:(UISearchBar *)sBar
{
	//NSLog(@"searchBarSearchButtonClicked");
	[self searchTextChanged:sBar.text forceUpdate:YES];
}

-(void)searchBar:(UISearchBar*)searchBar textDidChange:(NSString*)sText
{
    searching = YES;
	[self searchTextChanged:sText forceUpdate:NO];
}


-(IBAction)setComboMode:(id)sender
{
    if(seg.selectedSegmentIndex == 1)
    {
        if (self.resultsTableView.hidden == NO)
        {
            [self.resultsTableView setHidden:YES];
            if ([field.fieldValue lengthIgnoreWhitespace])
                self.txtName.text = self.field.fieldValue;
            else
            {
                self.txtName.placeholder = [NSString stringWithFormat:[@"Enter Field" localize], self.field.label];
                [self.txtName becomeFirstResponder];
            }
            self.lblTip.text = self.field.tip;
        }
    }
    else
    {
        if (self.resultsTableView.hidden == YES)
            [self.resultsTableView setHidden:NO];
        
    }

}

#pragma mark -
#pragma mark TextField stuff
-(void)textFieldDidEndEditing:(UITextField *)textField
{
    if (seg.selectedSegmentIndex == 1 && self.delegate != nil)
    {
        if ((self.field.fieldValue != nil || textField.text != nil) && 
        ![self.field.fieldValue isEqualToString:textField.text])
        {
            self.field.fieldValue = textField.text;
            self.field.liKey = nil;

            [self.delegate fieldUpdated:field];
        }
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    if (seg.selectedSegmentIndex == 1 && self.delegate != nil)
    {
        if ((self.field.fieldValue != nil || textField.text != nil) && 
            ![self.field.fieldValue isEqualToString:textField.text])
        {        
            self.field.fieldValue = textField.text;
            self.field.liKey = nil;
            if (self.delegate != nil)
                [self.delegate fieldUpdated:field];
            
        }
    }
    
    self.delegate = nil;
    [self.navigationController popViewControllerAnimated:YES];
    return YES;
    
}

#pragma mark -
#pragma mark UIScrollViewDelegate Methods
- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
	[self.searchBar resignFirstResponder];
}


@end
