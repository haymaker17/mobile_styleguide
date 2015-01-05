//
//  ApproverSearchVC.m
//  ConcurMobile
//
//  Created by yiwen on 8/26/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ApproverSearchVC.h"
#import "SearchApproverData.h"
#import "FormViewControllerBase.h"

@interface ApproverSearchVC(){
    BOOL        canDrawSubmit;
}
@end

@implementation ApproverSearchVC
@synthesize resultsTableView, searchBar, seg, lblTip, selectedItem, rptKey;
@synthesize delegate = _delegate;
@synthesize searchResults, searchText, fullResults, searchHistory;
@synthesize btnSubmit;

- (void)setSeedData:(NSString*)reportKey approver:(ApproverInfo*)curApp canDrawSubmit:(BOOL)canSubmit delegate:(id<ApproverSearchDelegate>)del
{
    self.delegate = del;
	self.rptKey = reportKey;
    self.selectedItem = curApp;
    canDrawSubmit = canSubmit;
}


#pragma mark - View lifecycle
-(void)closeMe:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	self.title = [Localizer getLocalizedText:@"Approver"];
    
	if(![UIDevice isPad])
	{
		searchBar.tintColor = [UIColor darkBlueConcur_iOS6];
        searchBar.alpha = 0.9f;
	} 
    else
    {
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeMe:)];
        self.navigationItem.leftBarButtonItem = btnClose;

		searchBar.tintColor = [UIColor navBarTintColor_iPad];
        searchBar.alpha = 0.9f;        
    }
    
	self.searchHistory = [[NSMutableDictionary alloc] init];
	
	// Show soft gray background
	[self.view setBackgroundColor:[UIColor baseBackgroundColor]];	

    // MOB-15540: if canDrawSubmit is true, allow the user to submit the report without a approver
    if (self.selectedItem != nil || (self.selectedItem == nil && canDrawSubmit))
    {
        self.searchResults =  [[NSMutableArray alloc] initWithObjects:self.selectedItem, nil];
        self.searchBar.text = self.selectedItem.lastName;
        self.lblTip.text =[Localizer getLocalizedText:@"Review Approver Instruction"];
        if ([ExSystem is7Plus])
        {
            self.btnSubmit = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_SUBMIT_BTN"] style:UIBarButtonItemStyleDone target:self action:@selector(submit)];
        }
        else
        {
            self.btnSubmit = [FormViewControllerBase makeNavButton:@"LABEL_SUBMIT_BTN" enabled:YES target:self action:@"submit"];
        }
    }
    else
    {
        self.lblTip.text =[Localizer getLocalizedText:@"Search Approver Instruction"];
        if ([ExSystem is7Plus])
        {
            self.btnSubmit = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_SUBMIT_BTN"] style:UIBarButtonItemStyleDone target:self action:@selector(submit)];
            [self.btnSubmit setEnabled:NO];
        }
        else
        {
            self.btnSubmit = [FormViewControllerBase makeNavButton:@"LABEL_SUBMIT_BTN" enabled:NO target:self action:@"submit"];
        }
    }
	self.navigationItem.rightBarButtonItem = nil;
	[self.navigationItem setRightBarButtonItem:btnSubmit animated:NO];
    

}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

#pragma mark - Delegate methods

- (void) submit
{
    if ([UIDevice isPad])
        [self closeMe:nil];
    else
        [self.navigationController popViewControllerAnimated:YES];	
	// call delegate
	if (self.delegate != nil)
		[self.delegate approverSelected:self.selectedItem];
}

- (NSString*) getFieldName :(NSInteger) fieldIndex
{
    switch (fieldIndex) {
        case 0:
            return @"LAST_NAME";
        case 1:
            return @"FIRST_NAME";
        case 2:
            return @"EMAIL_ADDRESS";
        case 3:
            return @"LOGIN_ID";
            
        default:
            break;
    }
    return nil;
}

- (void) doSearch :(NSInteger) fieldIndex withQuery:(NSString*) sText
{
    NSString* fieldName = [self getFieldName:fieldIndex];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.rptKey, @"RPT_KEY", fieldName, @"SEARCH_FIELD", sText, @"QUERY", nil];
    
    [[ExSystem sharedInstance].msgControl createMsg:SEARCH_APPROVER_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];
	//respond to data that might be coming from the cache
	if ([msg.idKey isEqualToString:SEARCH_APPROVER_DATA])
	{
		if (msg.responseCode == 200)
		{
			SearchApproverData *data = (SearchApproverData*) msg.responder;
			self.searchResults = data.approverList;
			if ((data.approverList == nil || data.approverList.count < 500)
				&& (data.query == nil || [data.query isEqualToString:@""]))
			{
				self.fullResults = self.searchResults;
			}
			
            NSString *sKey = [NSString stringWithFormat:@"%@%@", data.searchField, data.query==nil?@"":data.query];
            (self.searchHistory)[sKey] = self.searchResults;

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
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
    
	if (cell.textLabel.font.pointSize != 16)
		cell.textLabel.font = [UIFont systemFontOfSize:16];
    
    // Configure the cell...
	ApproverInfo *app = searchResults[row];
	NSString *fullName = nil;
    if (![app.firstName length])
        fullName = app.lastName == nil? @"" :app.lastName;
    else if (![app.lastName length])
        fullName = app.firstName;
    else
        fullName = [NSString stringWithFormat:@"%@, %@", app.lastName, app.firstName];
    
    if (![app.email length])
		cell.textLabel.text = fullName;
    else
        cell.textLabel.text = [NSString stringWithFormat:@"%@ (%@)", fullName, app.email];
    
    if (self.selectedItem != nil && [selectedItem.empKey isEqualToString:app.empKey])
		cell.accessoryType = UITableViewCellAccessoryCheckmark;
	else
		cell.accessoryType = UITableViewCellAccessoryNone;	
    
    return cell;
}


#pragma mark -
#pragma mark Table view delegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    bool enableSubmitButton = self.selectedItem == nil;
	// Update field
    NSUInteger row = [indexPath row];
	ApproverInfo *app = searchResults[row];
    self.selectedItem = app;
    
    if (enableSubmitButton && app != nil)
    {
        if ([ExSystem is7Plus])
        {
            self.btnSubmit = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_SUBMIT_BTN"] style:UIBarButtonItemStyleDone target:self action:@selector(submit)];
        }
        else
        {
            self.btnSubmit = [FormViewControllerBase makeNavButton:@"LABEL_SUBMIT_BTN" enabled:YES target:self action:@"submit"];
        }
        self.navigationItem.rightBarButtonItem = nil;
        [self.navigationItem setRightBarButtonItem:btnSubmit animated:NO];
    }
        
    [tableView reloadData];
//    
//	[self.navigationController popViewControllerAnimated:YES];	
//	// call delegate
//	if (self.delegate != nil)
//		[self.delegate approverSelected:app];
}

#pragma mark -
#pragma mark Cancel and Close Methods
-(BOOL) enoughTextChange:(NSString*) sText
{
	NSUInteger sLen = [sText length];
	NSUInteger cLen = self.searchText == nil? 0 : [self.searchText length];
	if (sLen > cLen + 2 || sLen < cLen -2)
		return YES;
	
	NSUInteger minLen = sLen > cLen? cLen:sLen;
	NSUInteger maxLen = sLen > cLen? sLen:cLen;
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
    
    NSString* fieldName = [self getFieldName:self.seg.selectedSegmentIndex];
    NSString *sKey = [NSString stringWithFormat:@"%@%@", fieldName, sText];
	if ([sKey isEqualToString:self.searchText])
		return;
    
	if ((self.searchHistory)[sKey] != nil)
	{
		self.searchResults = (self.searchHistory)[sKey];
		self.searchText = sKey;
		[self.resultsTableView reloadData];
	}
//	else if (self.fullResults != nil)
//	{
//		NSMutableArray* sResult = [[NSMutableArray alloc] init];
//		for (ApproverInfo* item in fullResults)
//		{
//			NSRange r = [item.lastName rangeOfString:sText options:NSCaseInsensitiveSearch];
//			if (r.location != NSNotFound)
//			{
//				[sResult addObject:item];
//			}
//			else if (item.email != nil)
//			{
//				r = [item.email rangeOfString:sText options:NSCaseInsensitiveSearch];
//				if (r.location != NSNotFound)
//					[sResult addObject:item];
//			}
//			else if (item.firstName != nil)
//			{
//				r = [item.firstName rangeOfString:sText options:NSCaseInsensitiveSearch];
//				if (r.location != NSNotFound)
//					[sResult addObject:item];
//			}
//			else if (item.loginId != nil)
//			{
//				r = [item.loginId rangeOfString:sText options:NSCaseInsensitiveSearch];
//				if (r.location != NSNotFound)
//					[sResult addObject:item];
//			}
//		}
//		self.searchResults = sResult;
//		[sResult release];
//		self.searchText = sText;		
//		[self.resultsTableView reloadData];
//	}
	else 
	{
		if (flag || [self enoughTextChange:sText])
		{
            [self doSearch:self.seg.selectedSegmentIndex withQuery:sText];
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

#pragma mark -
#pragma mark UISegmentedControlDelegate Methods
-(IBAction)setSearchField:(id)sender
{
	[self searchTextChanged:self.searchBar.text forceUpdate:YES];
}

#pragma mark -
#pragma mark UIScrollViewDelegate Methods
- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
	[self.searchBar resignFirstResponder];
}


@end
