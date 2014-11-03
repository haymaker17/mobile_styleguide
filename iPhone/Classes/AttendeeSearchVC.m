//
//  AttendeeSearchVC.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "AttendeeSearchVC.h"
#import "AttendeeSearchData.h"
#import "DataConstants.h"
#import "Msg.h"
#import "ExSystem.h" 

#import "MobileAlertView.h"
#import "ConcurMobileAppDelegate.h"
#import "MCLogging.h"
#import "AttendeesInGroupData.h"

@implementation AttendeeSearchVC

@synthesize delegate = _delegate;
@synthesize attendeeExclusionList;
@synthesize resultsTableView, searchBar, tBar, cancelBtn;
@synthesize searchResults, searchText, searchHistory;

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	tBar.topItem.title = [Localizer getLocalizedText:@"Search for Attendee"];
	cancelBtn.title = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	searchBar.placeholder = [Localizer getLocalizedText:@"Type name or select frequent attendee"];

	resultsTableView.bounces = YES; // UIScrollView delegate does not get called when all rows are currently visible unless this is set

	self.searchHistory = [[NSMutableDictionary alloc] init];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

-(void) handleNoDataView
{
    if (self.searchResults ==  nil || [self.searchResults count]==0)
    {
        [self showNoDataView:self];
    }
    else
    {
        [self hideNoDataView];
    }
}

-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];
	//respond to data that might be coming from the cache
	if ([msg.idKey isEqualToString:ATTENDEE_SEARCH_DATA])
	{
		AttendeeSearchData *data = (AttendeeSearchData*) msg.responder;
		if (msg.responseCode == 200)
		{
			NSSortDescriptor *firstNameSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"firstName" ascending:YES];
			NSSortDescriptor *lastNameSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"lastName" ascending:YES];
			[data.attendees sortUsingDescriptors:@[firstNameSortDescriptor, lastNameSortDescriptor]];
			
			if (searchHistory[data.query] == nil)
			{
				searchHistory[data.query] = data.attendees;
			}

			self.searchResults = data.attendees;

			if ([self isViewLoaded])
			{
				[self.resultsTableView reloadData];
			}
            
            [self handleNoDataView];
			// TODO - Select the current value
		}
		else if (msg.responseCode == 500)
		{
			NSArray *emptyAttendeeArray = @[];
			searchHistory[data.query] = emptyAttendeeArray;

			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:[Localizer getLocalizedText:@"Search Failed"]
								  message:[Localizer getLocalizedText:@"The server encountered an error."]
								  delegate:nil
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
		}
	}
    else if ([msg.idKey isEqualToString:ATTENDEES_IN_GROUP_DATA])
    {
		AttendeesInGroupData *data = (AttendeesInGroupData*) msg.responder;
		if (msg.responseCode == 200)
		{
            [self dismissViewControllerAnimated:YES completion:nil];
            
            // Notify the delegate
            if (self.delegate != nil)
                [self.delegate attendeesSelected:data.attendees];
            
		}
		else if (msg.responseCode == 500)
		{            
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:[Localizer getLocalizedText:@"Failed to retrieve attendees in selected group"]
								  message:[Localizer getLocalizedText:@"The server encountered an error."]
								  delegate:nil
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
            [self hideWaitView];
		}
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
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:CellIdentifier];
    }
    
    // Configure the cell...
	NSObject *atnObj = searchResults[row];
    if ([atnObj isKindOfClass:[AttendeeGroup class]])
    {
        AttendeeGroup *atnGroup = (AttendeeGroup *) atnObj;
        cell.textLabel.text = atnGroup.name;
        cell.detailTextLabel.text = [@"Attendee Group" localize];        
    }
    else
    {
        AttendeeData *attendee = (AttendeeData*)atnObj;
        cell.textLabel.text = [attendee getFullName];
        cell.detailTextLabel.text = [attendee getNonNullableValueForFieldId:@"Company"];
    }
    return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
	AttendeeData *attendee = searchResults[row];

    if ([attendee isKindOfClass:[AttendeeGroup class]])
    {
        AttendeeGroup* atnGroup = (AttendeeGroup*) attendee;
        // Send out msg to get attendees in group
        [self showWaitView];
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:atnGroup.groupKey, @"GROUP_KEY", @"YES", @"SKIP_CACHE", nil];
        [[ExSystem sharedInstance].msgControl createMsg:ATTENDEES_IN_GROUP_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        
        return;
    }

	[self dismissViewControllerAnimated:YES completion:nil];
	
	// Notify the delegate
	if (self.delegate != nil)
		[self.delegate attendeeSelected:attendee];
}


#pragma mark -
#pragma mark Search

-(void) searchTextChanged:(NSString*) sText
{
	if (sText == nil)
		sText = @"";
	
	if ([sText isEqualToString:self.searchText])
		return;
	
	if ((self.searchHistory)[sText] != nil)
	{
		self.searchResults = (self.searchHistory)[sText];
		self.searchText = sText;
		[self.resultsTableView reloadData];
        [self handleNoDataView];
	}
	else 
	{
		[self doSearch:sText];
	}
}

-(void) doSearch:(NSString*)text
{
	if (text == nil)
		text = @"";
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:text, @"QUERY", @"YES", @"SKIP_CACHE", nil];
	if (attendeeExclusionList != nil)
	{
		pBag[@"EXCLUSION_LIST"] = attendeeExclusionList;
	}
	[[ExSystem sharedInstance].msgControl createMsg:ATTENDEE_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

#pragma mark -
#pragma mark UISearchBarDelegate Methods
- (void)searchBarSearchButtonClicked:(UISearchBar *)sBar
{
	[self searchTextChanged:sBar.text];
}

-(void)searchBar:(UISearchBar*)sBar textDidChange:(NSString*)sText
{
	[self searchTextChanged:sText];
}

#pragma mark -
#pragma mark UIScrollViewDelegate Methods

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
	[self.searchBar resignFirstResponder];
}

#pragma mark -
#pragma mark Cancel and Close Methods
-(IBAction) btnCancel:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];	
	/*
	 // call delegate
	 if (self.delegate != nil)
	 [self.delegate fieldCanceled:self.field];
	 */
}

#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload
{
	self.resultsTableView = nil;
	self.searchBar = nil;
	self.tBar = nil;
	self.cancelBtn = nil;
}


#pragma NoDataMasterViewDelegate method
-(BOOL)canShowActionOnNoData
{
    return NO;
}

-(NSString*) titleForNoDataView
{
    return [@"No Attendee Found" localize]; 
}

-(NSString*) imageForNoDataView
{
    return @"neg_attendee_icon";
}

- (BOOL)adjustNoDataView:(NoDataMasterView*) negView  
{
    float width = self.resultsTableView.frame.size.width;
    float height = self.resultsTableView.frame.size.height-44-192;  // take out search bar and keyboard
    negView.frame = CGRectMake(self.resultsTableView.frame.origin.x, self.resultsTableView.frame.origin.y+44, width, height);
    return NO; // No need to adjust for toolbar
}


@end

