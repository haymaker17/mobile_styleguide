//
//  ExpenseLocationsViewController.m
//  ConcurMobile
//ƒ
//  Created by Charlotte Fallarme on 1/21/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ExpenseLocationsViewController.h"
#import "ExSystem.h" 

#import "FormFieldData.h"
#import "ListFieldSearchData.h"
#import "ExSystem.h"

@implementation ExpenseLocationsViewController


@synthesize tableList;
@synthesize sbar;
@synthesize tBar;
@synthesize cancelBtn;
@synthesize results;
@synthesize prevResultsDicitonary;
@synthesize originalLocationName;
@synthesize delegate = _delegate;


-(NSString *)getViewIDKey
{
	return EXPENSE_LOCATIONS_LIST;
}

-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:LIST_FIELD_SEARCH_DATA])
	{
		//self.isSearching = NO;
		
		if (msg.responseCode != 200 && !msg.isCache)
		{
			//[self showErrorMessage:[Localizer getLocalizedText:@"Unable to load location data."]]; //method looks to be copied but not implemented into this VC
			return;
		}
		
		self.results = [[NSMutableArray alloc] init]; // Incs ref count by 2
		 // Decs ref count by 1

		ListFieldSearchData *data = (ListFieldSearchData*)msg.responder;
		if (data != nil && data.listItems != nil)
		{
			NSUInteger listItemCount = [data.listItems count];
			for (int i = 0; i < listItemCount; i++)
			{
				ListItem* listItem = (data.listItems)[i];
				NSString* locationName = listItem.liName;
				if (locationName != nil)
				{
					[results addObject:locationName];
				}
			}
		}
        if (data.query) {
            prevResultsDicitonary[data.query] = results;
        }

		if ([self isViewLoaded])
		{
			[tableList reloadData];
		}
	}
}


#pragma mark -
#pragma mark Initialization

-(id) init
{
	return self;
}

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];

	self.title = [Localizer getLocalizedText:@"Expense Locations"];
	
	UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
	self.navigationItem.leftBarButtonItem = btnCancel;
	//cancelBtn.title = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	
	sbar.placeholder = [Localizer getLocalizedText:@"Type to search for a location"];
	
	self.prevResultsDicitonary = [[NSMutableDictionary alloc] init]; // Incs ref count by 2
	 // Decs ref count by 1
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
	
	if (originalLocationName != nil && [originalLocationName length] > 0)
	{
		self.sbar.text = originalLocationName;
		[self searchFor:originalLocationName];
	}
}

// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return (results == nil ? 0 : [results count]);
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"Cell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
    
	NSInteger row = [indexPath row];
	
	NSString *locationName = nil;
	
	if (results != nil && row < [results count])
	{
		locationName = results[row];
	}
	
    cell.textLabel.text = (locationName == nil ? @"" : locationName);
    
    return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	if (results != nil && row < [results count])
	{
		NSString *locationName = results[row];
		
		if (locationName != nil && [locationName length] > 0)
		{
			if (self.delegate != nil)
			{
				[self.delegate selectedExpenseLocation:locationName];
				[self closeView:self];
			}
		}
	}
}


#pragma mark -
#pragma mark UISearchBarDelegate Methods
-(void)searchBar:(UISearchBar*)searchBar textDidChange:(NSString*)searchText
{
	[self searchFor:searchText];
}
		 

#pragma	mark -
#pragma Search Methods
-(void) searchFor:(NSString*)searchText
{
	if (searchText == nil || [searchText length] == 0)
	{
		self.results = nil;
		[tableList reloadData];
		return;
	}
	
	NSMutableArray* cachedResults = prevResultsDicitonary[searchText];
	if (cachedResults != nil)
	{
		self.results = cachedResults;
		[tableList reloadData];
	}
	else
	{
		//self.isSearching = YES;

		FormFieldData *formField = [[FormFieldData alloc] init];
		formField.iD = @"LocName";
		formField.ftCode = @"RPTINFO";
		
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:formField, @"FIELD", searchText, @"QUERY", nil];
		[[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
		
	}
}


#pragma mark -
#pragma mark Close
-(IBAction) closeView:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];	
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc. that aren't in use.
	[prevResultsDicitonary removeAllObjects];
}

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
}




@end

