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

@implementation ListFieldEditVC
@synthesize delegate = _delegate;
@synthesize field, resultsTableView, searchBar, tBar, cancelBtn, doneBtn, rptKey;
@synthesize mruList, searchResults, searchText, fullResults, searchHistory, useSearch, selectedItem;
@synthesize lblListFieldValue, lblListFieldTitle, btnBackListField;

+(BOOL) canUseListEditor:(FormFieldData*)field
{
	return ([field.ctrlType isEqualToString:@"picklist"] 
			|| [field.ctrlType isEqualToString:@"list_edit"]
			||[field.ctrlType isEqualToString:@"combo"] 
			|| [field.dataType isEqualToString:@"MLIST"]
			||[field.ctrlType isEqualToString:@"checkbox"] 
			|| [field.dataType isEqualToString:@"BOOLEANCHAR"]
			||[field.dataType isEqualToString:@"CURRENCY"]
			||[field.dataType isEqualToString:@"LOCATION"]
			);
}

-(BOOL) isCombo
{
	return [self.field.ctrlType isEqualToString:@"combo"];
}

- (void)dealloc 
{
	[field release];
	[resultsTableView release];
	[searchBar release];
	[tBar release];
	[cancelBtn release];
	[doneBtn release];
	[mruList release];
	[searchResults release];
	[searchText release];
	[fullResults release];
	[searchHistory release];
	[selectedItem release];
	[btnBackListField release];
	[lblListFieldTitle release];
	[lblListFieldValue release];
	[rptKey release];
	[super dealloc];
}

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	tBar.topItem.title = [Localizer getLocalizedText:@"List"];
	cancelBtn.title = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	doneBtn.title = [Localizer getLocalizedText:@"LABEL_DONE_BTN"];
	
	if([ExSystem isPad])
	{
		tBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
		searchBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	}
	
	self.searchHistory = [[NSMutableDictionary alloc] init];
	[self.searchHistory release];
	
	// Show soft gray background
	[self.view setBackgroundColor:[UIColor colorWithRed:0.882871 green:0.887548 blue:0.892861 alpha:1]];	
}

- (void)viewWillAppear:(BOOL)animated
{
	//self.searchBar.text = initialSearchLocation.location;
	// TODO - display the existing cell.
	if (self.field != nil)
	{
		if (![self isCombo])
		{
			[self.tBar.topItem setRightBarButtonItem:nil];
		}
		
		self.lblListFieldTitle.text = self.field.label;
		self.lblListFieldValue.text = self.field.fieldValue;
		[self.btnBackListField setHighlighted:TRUE];
		
		if (([self.field.ctrlType isEqualToString:@"checkbox"] || [field.dataType isEqualToString:@"BOOLEANCHAR"])
			&& self.searchResults == nil)
		{
			ListItem* yesItem = [[ListItem alloc] init];
			yesItem.liKey = @"Y";
			yesItem.liName = [Localizer getLocalizedText:@"Yes"];
			ListItem* noItem = [[ListItem alloc] init];
			noItem.liKey = @"N";
			noItem.liName = [Localizer getLocalizedText:@"No"];
			self.searchResults = [[NSArray alloc] initWithObjects:yesItem, noItem, nil];
			[yesItem release];
			[noItem release];
			[self.searchBar removeFromSuperview];
			CGRect frame = self.resultsTableView.frame;
			self.resultsTableView.frame = CGRectMake(frame.origin.x, frame.origin.y-44, frame.size.width, frame.size.height+44);
			[self.resultsTableView reloadData];
		}
		else if ([self.field.iD isEqualToString:@"CarKey"] && self.searchResults == nil)
		{
			NSMutableDictionary *dict = [[ConcurMobileAppDelegate findRootViewController].carRatesData fetchPersonalCarDetails:@""];
			NSMutableArray *a = [[NSMutableArray alloc] initWithObjects:nil];
			
			for(NSString *key in dict)
			{
				CarDetailData *cd = [dict objectForKey:key];
				
				ListItem* li = [[ListItem alloc] init];
				li.liKey = cd.carKey;
				li.liName = cd.vehicleId;
				
				[a addObject:li];
				[li release];
			}

			self.searchResults = [[NSArray alloc] initWithArray:a];
			[a release];

			[self.searchBar removeFromSuperview];
			CGRect frame = self.resultsTableView.frame;
			self.resultsTableView.frame = CGRectMake(frame.origin.x, frame.origin.y-44, frame.size.width, frame.size.height+44);
			[self.resultsTableView reloadData];
		}
	}
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
	return YES;
}


-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];
	//respond to data that might be coming from the cache
	if ([msg.idKey isEqualToString:LIST_FIELD_SEARCH_DATA])
	{
		if (msg.responseCode == 200)
		{
			ListFieldSearchData*data = (ListFieldSearchData*) msg.responder;
			self.searchResults = data.listItems;
			if ((data.listItems == nil || data.listItems.count < 500)
				&& (data.query == nil || [data.query isEqualToString:@""])
				&& !data.isMru && ![data.fieldId isEqualToString:@"LnKey"])
			{
				self.fullResults = data.listItems;
			}
			
			if (!data.isMru)
				[searchHistory setObject:data.listItems forKey:(data.query==nil?@"":data.query)];
			else {
				self.mruList = data.listItems;
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
	return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return searchResults == nil? 0: [searchResults count];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";
	
    NSUInteger row = [indexPath row];
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];
    }

	if (cell.textLabel.font.pointSize != 16)
		cell.textLabel.font = [UIFont systemFontOfSize:16];
    
    // Configure the cell...
	ListItem *li = [searchResults objectAtIndex:row];
	if (li.liCode != nil)
		cell.textLabel.text = [NSString stringWithFormat:@"(%@) %@", li.liCode, li.liName];
    else {
		cell.textLabel.text = li.liName;
	}

    return cell;
}


#pragma mark -
#pragma mark Table view delegate
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
	if (li.liCode != nil)
		field.liCode = li.liCode;
	if (li.liName != nil)
		field.fieldValue = li.liName;
}

-(void) setEditResult:(NSString*) text
{
	field.liKey = nil;
	field.liCode = nil;
	field.fieldValue = text;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	// Update field
    NSUInteger row = [indexPath row];
	ListItem *li = [searchResults objectAtIndex:row];
	[self setListResult:li];
	 
	[self.parentViewController dismissModalViewControllerAnimated:YES];	
	// call delegate
	if (self.delegate != nil)
		[self.delegate fieldUpdated:self.field];
}

#pragma mark -
#pragma mark Cancel and Close Methods

-(IBAction) btnCancel:(id)sender
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];	
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
			ListItem* li = [self.searchResults objectAtIndex:ix];
			if (li != nil && [li.liName caseInsensitiveCompare:self.lblListFieldValue.text]==NSOrderedSame)
			{
				[self setListResult:li];
				listValueFound = YES;
				break;
			}
		}
	}
	if (!listValueFound)
		[self setEditResult:self.lblListFieldValue.text];
	
	[self.parentViewController dismissModalViewControllerAnimated:YES];	
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
	//NSLog(@"Search text:%@", sText);
	if (sText == nil)
		sText = @"";

	if ([sText isEqualToString:self.searchText])
		return;

	if ([self isCombo])
		self.lblListFieldValue.text = sText;
	
	if ([self.searchHistory objectForKey:sText] != nil)
	{
		self.searchResults = [self.searchHistory objectForKey:sText];
		self.searchText = sText;
		[self.resultsTableView reloadData];
	}
	else if (self.fullResults != nil)
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
		[sResult release];
		self.searchText = sText;		
		[self.resultsTableView reloadData];
	}
	else 
	{
		if (flag || [self enoughTextChange:sText])
		{
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: field, @"FIELD", sText, @"QUERY", nil];
			if ([field.iD isEqualToString:@"ReceiptType"])
			{
				[pBag setObject:self.rptKey forKey:@"RPT_KEY"];
			}
			
			[[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
			[pBag release];
		}
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
	[self searchTextChanged:sText forceUpdate:NO];
}


@end
