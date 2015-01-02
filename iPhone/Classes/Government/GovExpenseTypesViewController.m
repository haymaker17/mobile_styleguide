//
//  GovExpenseTypesViewController.m
//  ConcurMobile
//
//  Created by ernest cho on 9/25/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovExpenseTypesViewController.h"

@implementation GovExpenseTypesViewController

@synthesize field;
@synthesize delegate = _delegate;
@synthesize resultsTableView, searchBar;
@synthesize searchResults, searchText, fullResults, searchHistory;
@synthesize expenseTypes;

- (void)dealloc
{
    resultsTableView.delegate = nil;
    resultsTableView.dataSource = nil;
}

- (void)setSeedData:(FormFieldData*)fld delegate:(id<FieldEditDelegate, GovExpenseTypeDelegate>)del
{
    self.delegate = del;
	self.field = fld;
}

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	self.title = @"Expense Type";
	
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
	
    if (self.searchResults == nil)
    {
        [[ExSystem sharedInstance].msgControl createMsg:GET_GOV_EXPENSE_TYPES CacheOnly:@"NO" ParameterBag:nil SkipCache:YES RespondTo:self];
        [self showLoadingView];
    }
    
	// Show soft gray background
	[self.view setBackgroundColor:[UIColor baseBackgroundColor]];
}

-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:GET_GOV_EXPENSE_TYPES])
	{
        self.expenseTypes = (GovExpenseTypesData *)msg.responder;
        self.fullResults = [expenseTypes getDescriptions];
        
        self.searchResults = self.fullResults;
        searchHistory[(@"")] = self.searchResults;
        
        [self.resultsTableView reloadData];
        [self hideLoadingView];
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

    __autoreleasing UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
    
	if (cell.textLabel.font.pointSize != 16)
		cell.textLabel.font = [UIFont systemFontOfSize:16];
    
    // Configure the cell...
	NSString *str = searchResults[row];
    
    cell.textLabel.text = str;
    cell.accessoryType = UITableViewCellAccessoryNone;
    
    return cell;
}

#pragma mark -
#pragma mark Table view delegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	// Update field
    NSUInteger row = [indexPath row];
	NSString *text = searchResults[row];

    [self setListResult:text];
    [self.navigationController popViewControllerAnimated:YES];
    // call delegate
    if (self.delegate != nil)
        [self updateDelegates:field];
}

-(void) setListResult:(NSString*) text
{
    field.liKey = nil;
    field.liCode = nil;
    field.fieldValue = text;
    field.extraDisplayInfo = nil;
}

-(void) setEditResult:(NSString*) text
{
	field.liKey = nil;
	field.liCode = nil;
	field.fieldValue = text;
    field.extraDisplayInfo = nil;
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
    [self setEditResult:self.field.fieldValue];
	
    [self dismissViewControllerAnimated:YES completion:nil];
	// call delegate
	if (self.delegate != nil)
        [self updateDelegates:field];
}

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
    
	if ([sText isEqualToString:self.searchText])
		return;
    
	if ((self.searchHistory)[sText] != nil)
	{
		self.searchResults = (self.searchHistory)[sText];
		self.searchText = sText;
		[self.resultsTableView reloadData];
	}
	else if (self.fullResults != nil)
	{
		NSMutableArray* sResult = [[NSMutableArray alloc] init];
		for (NSString* item in fullResults)
		{
			NSRange r = [item rangeOfString:sText options:NSCaseInsensitiveSearch];
			if (r.location != NSNotFound)
			{
				[sResult addObject:item];
			}
		}

		self.searchResults = sResult;
		self.searchText = sText;
		[self.resultsTableView reloadData];
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
#pragma mark TextField stuff
-(void)textFieldDidEndEditing:(UITextField *)textField
{
    if (self.delegate != nil)
    {
        if ((self.field.fieldValue != nil || textField.text != nil) &&
            ![self.field.fieldValue isEqualToString:textField.text])
        {
            self.field.fieldValue = textField.text;
            self.field.liKey = nil;
            
            [self updateDelegates:field];
        }
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    if (self.delegate != nil)
    {
        if ((self.field.fieldValue != nil || textField.text != nil) &&
            ![self.field.fieldValue isEqualToString:textField.text])
        {
            self.field.fieldValue = textField.text;
            self.field.liKey = nil;
            if (self.delegate != nil)
                [self updateDelegates:field];
            
        }
    }
    self.delegate = nil;
    [self.navigationController popViewControllerAnimated:YES];
    return YES;
}

- (void)updateDelegates:(FormFieldData *)textField
{
    if (self.delegate != nil) {
        [self.delegate fieldUpdated:textField];
        GovExpenseType *expenseType = [expenseTypes getExpenseFor:textField.fieldValue];
        if (expenseType != nil) {
            [self.delegate updateExpenseType:expenseType];
        }
    }
}

#pragma mark -
#pragma mark UIScrollViewDelegate Methods
- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
	[self.searchBar resignFirstResponder];
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return GOV_EXPENSE_TYPES;
}

@end
