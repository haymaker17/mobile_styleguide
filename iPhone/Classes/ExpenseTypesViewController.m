//
//  ExpenseTypesViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ExpenseTypesViewController.h"
#import "ExSystem.h" 

#import "DateTimeFormatter.h"
#import "FormatUtils.h"
#import "ExpenseTypesData.h"
#import "ExpenseTypesManager.h"
#import "AttendeeSearchData.h"
#import "DataConstants.h"
#import "ExpenseTypeTableCell.h"

#import "FakeGroupSectionHeader.h"

#import "CarRatesData.h"

@implementation ExpenseTypesViewController

@synthesize tableList, sections, items, parentMVC, tBar, dupListOfItems, expKey, cancelBtn;
@synthesize delegate = _delegate;
@synthesize isAcceptingData, parentExpKey;
@synthesize polKey;
@synthesize etCol;
@synthesize selectedExpenseType;
@synthesize rpt;


@dynamic expenseTypesEndPointVersion;


#pragma mark -
#pragma mark Dynamic properties
-(NSString*) expenseTypesEndPointVersion
{
	return (_expenseTypesEndPointVersion == nil ? @"V3" : _expenseTypesEndPointVersion);
}

-(void) setExpenseTypesEndPointVersion:(NSString*)newValue
{
	_expenseTypesEndPointVersion = newValue;
}

-(BOOL) isForChild
{
    return self.parentExpKey != nil;
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return EXPENSE_TYPES_LIST;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

-(void)respondToFoundData:(Msg *)msg
{
	// Note: Use the flag of isAcceptingData to NOT to refresh view when the network data comes back 
	// after the view is dismissed 
	// If this form is reused, instead of recreated each time expense type dialog is invoked, we 
	// need to refine this code to update the data store, but not refreshing view elements.
	if ([msg.idKey isEqualToString:EXPENSE_TYPES_DATA] && isAcceptingData)
	{
		ExpenseTypesData *etsData = (ExpenseTypesData *)msg.responder;
		ExpenseTypesManager* expenseTypesManager = [ExpenseTypesManager sharedInstance];
		
		//##TODO## hide mileg if no rates
		ExpenseTypeData *et = (etsData.ets)[@"MILEG"];
		if(et != nil && self.carRatesData != nil)
		{
			float rate = [self.carRatesData fetchRate:[NSDate date] isPersonal:YES isPersonalPartOfBusiness:NO distance:@"1" carKey:@"" ctryCode:@"" numPassengers:@"0" distanceToDate:0];
			if(rate <= 0.0)
			{
				[etsData.ets removeObjectForKey:@"MILEG"];
			}
		}
		
		[expenseTypesManager addExpenseTypes:etsData];
		self.etCol = [expenseTypesManager expenseTypesForVersion:self.expenseTypesEndPointVersion policyKey:self.polKey forChild:self.isForChild];
		ExpenseTypeData* parentExpType = self.parentExpKey==nil? nil : [expenseTypesManager expenseTypeForVersion:self.expenseTypesEndPointVersion policyKey:self.polKey expenseKey:parentExpKey forChild:NO];
		[self gotExpenseTypesData:parentExpType];
	}
	else if ([msg.idKey isEqualToString:DEFAULT_ATTENDEE_DATA])
	{
        if ([self isViewLoaded]) {
            [self hideLoadingView];
        }
		// The DefaultAttendeeData class took care of adding the default attendee the ExpenseTypesManager.
		// Notify the delegate that an exense was selected whether we could find the SYSEMP employee or not.
		[self notifyDelegateOfSelectedExpenseType];
	}
}

- (void)viewWillAppear:(BOOL)animated
{
	isAcceptingData = TRUE;
	[super viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
	isAcceptingData = FALSE;	// To prevent refresh upon receipt of network data after dismissal
	[super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
	//	NSLog(@"View did disappear");
	if (self.delegate != nil && [((NSObject*)self.delegate) respondsToSelector:@selector(expenseTypeDlgDismissed)])
	{
		[self.delegate expenseTypeDlgDismissed];
	}
}

- (void)viewDidAppear:(BOOL)animated 
{
	//[tableList setContentOffset:CGPointMake(0, 0) animated:NO];
//	NSLog(@"View did appear");	
	[super viewDidAppear:animated];
}

- (void)viewDidLayoutSubviews
{
    if ([self respondsToSelector:@selector(topLayoutGuide)])
    {
        CGRect viewBounds = self.view.bounds;
        CGFloat topBarOffset = self.topLayoutGuide.length;

        // This works on iPad, on iPhone it's too short.  I added an iPhone version that's reasonable.
        if ([UIDevice isPad]) {
            [self.view setFrame:CGRectMake(viewBounds.origin.x, topBarOffset, viewBounds.size.width, viewBounds.size.height-topBarOffset)];
        } else {
            [self.view setFrame:CGRectMake(viewBounds.origin.x, topBarOffset, viewBounds.size.width, viewBounds.size.height-5)];
        }
    }
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
	isAcceptingData = TRUE;
    
	if([UIDevice isPad])
	{
		//self.popoverContentSize = CGSizeMake(320, 480);
		 self.contentSizeForViewInPopover = CGSizeMake(320.0, 480.0);
		//[tBar setBarStyle:UIBarStyleBlackOpaque];
		tBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
		searchBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	}
	else
	{
		tBar.tintColor = [UIColor darkBlueConcur_iOS6];
		searchBar.tintColor = [UIColor darkBlueConcur_iOS6];
	}

    // Need the car rates data. The static method on CarRatesData is a last minute hack.
    self.carRatesData = [CarRatesData lastCarRatesDataDownloaded];

	self.etCol = [[ExpenseTypesCollection alloc] init];
	
	self.sections = [[NSMutableDictionary alloc] init];

	self.dupListOfItems = [[NSMutableArray alloc] initWithObjects:nil];

    [super viewDidLoad];
	
	// Mob-2513 Localizing hte Expense types header and Cancel button title
	cancelBtn.title = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	tBar.topItem.title = [Localizer getLocalizedText:@"EXPENSE_TYPES_LIST"];
	
	searchBar.autocorrectionType = UITextAutocorrectionTypeNo;
	
	searching = NO;
	letUserSelectRow = YES;

	ExpenseTypesManager *expenseTypesManager = [ExpenseTypesManager sharedInstance];
	self.etCol = [expenseTypesManager expenseTypesForVersion:self.expenseTypesEndPointVersion policyKey:self.polKey forChild:self.isForChild];
	
	if (etCol != nil)
	{
        ExpenseTypeData* parentExpType = self.parentExpKey==nil ? nil : [expenseTypesManager expenseTypeForVersion:self.expenseTypesEndPointVersion policyKey:self.polKey expenseKey:parentExpKey forChild:NO];
		[self gotExpenseTypesData:parentExpType];
	}
	else
	{
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:EXPENSE_TYPES_LIST, @"TO_VIEW", self.expenseTypesEndPointVersion, @"VERSION", self.polKey, @"POL_KEY", nil];
		if (self.polKey == nil)
			[[ExSystem sharedInstance].msgControl createMsg:EXPENSE_TYPES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
		else
			[[ExSystem sharedInstance].msgControl createMsg:EXPENSE_TYPES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

// Mob-1936
//-(void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
//{
//	[self.tableList reloadData];
//}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	//NSLog(@"View did unload");	// Never called
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc 
{


	
	
	self.delegate = nil;

}


#pragma mark -
#pragma mark SearchBar methods
- (void) searchBarSearchButtonClicked:(UISearchBar *)theSearchBar {
	
	[self searchTableView];
}


- (void) searchTableView 
{
	NSString *searchText = searchBar.text;
	NSMutableArray *searchArray = [[NSMutableArray alloc] init];
	[dupListOfItems removeAllObjects];
    NSString *locMruHeader = [Localizer getLocalizedText:MRUKEY] ;
	for (NSString *key in sections)
	{
        if(![key isEqualToString:locMruHeader])
        {
            NSMutableArray *things = sections[key];
            [searchArray addObjectsFromArray:things];
            
        }
	}
	
	for (ExpenseTypeData *et in searchArray)
	{
		NSString *sTemp = et.expName;
		NSRange titleResultsRange = [sTemp rangeOfString:searchText options:NSCaseInsensitiveSearch];
		
		if (titleResultsRange.length > 0)
        {
            if (![et isPersonalCarMileage] || self.rpt == nil || self.rpt.crnCode == nil
                ||
                [self.carRatesData hasAnyPersonalsWithRates:self.rpt.crnCode])
            {
                [dupListOfItems addObject:et];
            }
        }
	}
	
	searchArray = nil;
}

- (void) doneSearching_Clicked:(id)sender {
	
	searchBar.text = @"";
	[searchBar resignFirstResponder];
	
	letUserSelectRow = YES;
	searching = NO;
	self.navigationItem.rightBarButtonItem = nil;
	self.tableList.scrollEnabled = YES;
	
	[self.tableList reloadData];
}


- (void)searchBar:(UISearchBar *)theSearchBar textDidChange:(NSString *)searchText 
{	
	if([searchText length] > 0) {
		
		searching = YES;
		letUserSelectRow = YES;
		self.tableList.scrollEnabled = YES;
		[self searchTableView];
	}
	else {
		
		searching = NO;
        // MOB-5265 Not sure why these two lines were there, but they were causing trouble.
		//letUserSelectRow = NO;
		//self.tableList.scrollEnabled = NO;
	}
	
	[self.tableList reloadData];
}

- (void) searchBarTextDidBeginEditing:(UISearchBar *)theSearchBar 
{
	NSString *searchText = theSearchBar.text;
	if(searchText != nil && [searchText length] > 0) {
		
		searching = YES;
		[self searchTableView];
	}
	else {
		
		searching = NO;
	}
	
	[self.tableList reloadData];
	
	//Add the done button.
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(doneSearching_Clicked:)];
}

//- (BOOL)searchBarShouldBeginEditing:(UISearchBar *)searchBar {  
//    searchBar.showsCancelButton = YES;  
//}  
//
//- (BOOL)searchBarShouldEndEditing:(UISearchBar *)searchBar {  
//    searchBar.showsCancelButton = NO;  
//}  

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	if (searching)
		return 1;
	else
		return [etCol.aParentNames count];
    
}


- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
	NSString *key = (etCol.aParentNames)[section];
    NSMutableArray *nameSection = sections[key]; 
	
	if (searching)
		return [dupListOfItems count];
	else 
		return [nameSection count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	

	NSString *key; // = [aKeys objectAtIndex:section];
	NSMutableArray *itemArray;// = [sections objectForKey:key];
	ExpenseTypeData *et; // = [itemArray objectAtIndex:row];
	
	if (searching) 
	{
		et = dupListOfItems[row];
	}
	else 
	{
		key = (etCol.aParentNames)[section];
		itemArray = sections[key];
		et = itemArray[row];
	}

	ExpenseTypeTableCell *cell = [tableView dequeueReusableCellWithIdentifier:@"BreezeData"];
	if (cell == nil) 
	{
		cell = [[ExpenseTypeTableCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"BreezeData"];
	}
    cell.text.text = et.expName;

	return cell;
	
}

#pragma mark -
#pragma mark Table Delegate Methods 
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    NSString *key = (etCol.aParentNames)[section];
	//NSString *parentName = [parentNames objectForKey:key];
	if(searching)
		key = [Localizer getLocalizedText:@"Search Results"];
    return key;
}


- (NSIndexPath *)tableView :(UITableView *)theTableView willSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	
	if(letUserSelectRow)
		return indexPath;
	else
		return nil;
}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
	NSUInteger section = [newIndexPath section];
    NSUInteger row = [newIndexPath row];
	NSString *key;// = [aKeys objectAtIndex:section];
	NSMutableArray *itemArray;// = [sections objectForKey:key];
	
	if (!searching) 
	{
		key = (etCol.aParentNames)[section];
		itemArray = sections[key];
		self.selectedExpenseType = itemArray[row];
	}
	else
		self.selectedExpenseType = dupListOfItems[row];

    BOOL supportsAttendees = (selectedExpenseType.supportsAttendees != nil && [selectedExpenseType.supportsAttendees isEqualToString:@"Y"]);
	if (supportsAttendees &&
        ([[ExpenseTypesManager sharedInstance] attendeeRepresentingThisEmployee] == nil) &&
		[ExSystem sharedInstance].entitySettings.firstName != nil &&
		[ExSystem sharedInstance].entitySettings.lastName != nil &&
        [ExSystem connectedToNetwork])
	{
		[self showLoadingView];
		
		NSString *userName = [NSString stringWithFormat:@"%@ %@", [ExSystem sharedInstance].entitySettings.firstName, [ExSystem sharedInstance].entitySettings.lastName];
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:userName, @"QUERY", @"YES", @"SKIP_CACHE", nil];
		[[ExSystem sharedInstance].msgControl createMsg:DEFAULT_ATTENDEE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
	else
	{
		[self notifyDelegateOfSelectedExpenseType];
	}
}

-(void) notifyDelegateOfSelectedExpenseType
{
	[_delegate saveSelectedExpenseType:selectedExpenseType];
}

#pragma mark -
#pragma mark Table Header Overrides

- (CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
	return 30.0;
}

- (UIView *) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    FakeGroupSectionHeader *headerView = [[FakeGroupSectionHeader alloc] initWithFrame:CGRectMake(0, 0, tableView.bounds.size.width, 30)];
    headerView.title.text = [[self tableView:tableView titleForHeaderInSection:section] uppercaseString];

    return headerView;
}

#pragma mark Utility Methods
-(IBAction) closeView:(id)sender
{
	if(![UIDevice isPad])
	{
		[self dismissViewControllerAnimated:YES completion:nil];	
	}
	else {
		if (_delegate != nil) {
			[_delegate cancelExpenseType];
		}
	}

}


-(void) selectCurrentExpenseType:(NSString *)thisExpKey
{
	//method will select the expense type...
	int section = 0;
	int row = 0;
	for(int i = 0; i < [sections count]; i++)
	{
		NSString *key = (etCol.aKeys)[i];
		NSMutableArray *itemArray = sections[key];
		for(int x = 0; x < [itemArray count]; x++)
		{
			ExpenseTypeData *et = itemArray[x];
			if([et.expKey isEqualToString:thisExpKey])
			{
				section = i;
				row = x;
				break;
			}
		}
	}

	NSUInteger _path[2] = {section, row};
	NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
	[tableList selectRowAtIndexPath:_indexPath animated:YES scrollPosition: UITableViewScrollPositionTop];
}

-(void)layoutPad
{
	[tBar setBarStyle:UIBarStyleBlackOpaque];
}

+(void)showExpenseTypeEditor:(id<ExpenseTypeDelegate>)delegate policy:(NSString*)polKey 
					parentVC:(MobileViewController*) pvc
					selectedExpKey:(NSString*) expKey parentExpKey:(NSString*)pExpKey
                    withReport:(ReportData*) rptData
                    
{
	ExpenseTypesViewController *c = [[ExpenseTypesViewController alloc] init];
	c.parentMVC = pvc;
	c.polKey = polKey;
	c.delegate = delegate;
	c.parentExpKey = pExpKey;
    c.rpt = rptData;
	if([UIDevice isPad])
	{
		c.modalPresentationStyle = UIModalPresentationFormSheet;
		    // MOB-21048: hack fix for iOS 8
        double delayInSeconds = 0.5;
        dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
        dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
            [pvc presentViewController:c animated:YES completion:nil];
        });
	}
    else{
        [pvc presentViewController:c animated:YES completion:nil];
    }
}

#pragma mark -
#pragma mark ExpenseTypesData methods
-(void)gotExpenseTypesData:(ExpenseTypeData*) parentExpType
{
   BOOL filterOutPerMileage = ! (self.rpt == nil || self.rpt.crnCode == nil ||
                             [self.carRatesData hasAnyPersonalsWithRates:self.rpt.crnCode]);

    // MOB-10708 Filter out company car mileage
    BOOL filterOutComMileage = ! (self.rpt == nil || self.rpt.crnCode == nil ||
                                  [self.carRatesData hasAnyCompanyCarWithRates:self.rpt.crnCode]);

//        self.sections = etCol.parents;
    NSMutableDictionary* excludedExpKeys = nil;
    if (parentExpType != nil && parentExpType.itemizationUnallowExpKeys != nil)
    {
        // Create a dictionary of excluded child expKeys
        NSArray* expKeys = [parentExpType.itemizationUnallowExpKeys componentsSeparatedByString:@","];
        excludedExpKeys = [[NSMutableDictionary alloc] initWithObjects:expKeys forKeys:expKeys];
    }
    // Filter out car mileage
    self.sections = [[NSMutableDictionary alloc] initWithCapacity:[etCol.parents count]];
    for (NSString* parentKey in etCol.parents)
    {
        NSMutableArray* newExpItems = [[NSMutableArray alloc] init];
        NSArray *expItems = (NSArray*) (etCol.parents)[parentKey];
        if (expItems != nil)
        {
            for (ExpenseTypeData* et in expItems)
            {
                if ((!filterOutPerMileage || ![et isPersonalCarMileage]) && (!filterOutComMileage || ![et isCompanyCarMileage]) &&
                    (excludedExpKeys == nil || excludedExpKeys[et.expKey]==nil))
                    [newExpItems addObject:et];
            }
        }
        
        (self.sections)[parentKey] = newExpItems;
    }

    
	if ([self isViewLoaded])
	{
		[self.tableList reloadData];
		
		if(expKey != nil)
			[self selectCurrentExpenseType:expKey];
	}
}

#pragma mark -
#pragma mark UIScrollViewDelegate Methods
- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
	[searchBar resignFirstResponder];
}

@end
