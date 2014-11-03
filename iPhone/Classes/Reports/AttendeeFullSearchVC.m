//
//  AttendeeFullSearchVC.m
//  ConcurMobile
//
//  Created by yiwen on 9/19/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "AttendeeFullSearchVC.h"
#import "AttendeeSearchData.h"
#import "ListFieldEditVC.h"
#import "ListFieldSearchData.h"
#import "AttendeeSearchFieldsData.h"
#import "AttendeeFullSearchData.h"
#import "AttendeeFullSearchResultsVC.h"
#import "AttendeeGroup.h"
#import "AttendeesInGroupData.h"

@implementation AttendeeFullSearchVC

@synthesize delegate = _delegate;
@synthesize attendeeExclusionList;
@synthesize resultsTableView, searchBar;
//@synthesize mruList, fullResults, selectedItem;
@synthesize searchResults, searchText, searchHistory, atnTypes;
@synthesize seg, loadingAttendeeTypes, loadingAttendeeForm, excludedAtnTypeKeys;
@synthesize curAtnTypeKey, atnSearchForms;
@synthesize expKey, polKey, rpeKey;
@synthesize atnEntryForm, fullSearchResults;

- (void)setSeedData:(id<AttendeeSearchDelegate>)del keysToExclude:(NSArray*) exKeys expKey:(NSString*) expK polKey:(NSString*)pK rpeKey:(NSString*) rpeK
{
    self.delegate = del;
    self.excludedAtnTypeKeys = exKeys;
    self.curAtnTypeKey = nil;
    self.expKey = expK;
    self.polKey = pK;
    self.rpeKey = rpeK;
}



- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    self.noSaveConfirmationUponExit = true;

    [super viewDidLoad];
//    self.title = [Localizer getLocalizedText:@"Attendee"];
	self.title = [Localizer getLocalizedText:@"Search for Attendee"];

    // Do any additional setup after loading the view from its nib.
    [self.seg setTitle:[Localizer getLocalizedText:@"Quick Search"] forSegmentAtIndex:0];
    [self.seg setTitle:[Localizer getLocalizedText:@"Advanced Search"] forSegmentAtIndex:1];
    
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
	
	// Show soft gray background
	[self.view setBackgroundColor:[UIColor baseBackgroundColor]];	

}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

#pragma mark - FormViewControllerBase override
-(NSArray*) getExcludeKeysForListEditor:(FormFieldData*) field
{
    if ([@"AtnTypeKey" isEqualToString: field.iD])
        return self.excludedAtnTypeKeys;
    return nil;
}

-(void)prefetchForListEditor:(ListFieldEditVC*) lvc
{
    if (![lvc.field.iD isEqualToString:@"AtnTypeKey"])
    {
        [super prefetchForListEditor:lvc];
        return;
    }
    
    // Prepopulate AtnTypeKey list
    NSMutableArray *attendeeTypes = (self.atnTypes == nil ? [[NSMutableArray alloc] init] : [NSMutableArray arrayWithArray:self.atnTypes]);
    
    lvc.sectionKeys = [[NSMutableArray alloc] initWithObjects: @"ATTENDEE_TYPES", nil];
    lvc.sections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: attendeeTypes, @"ATTENDEE_TYPES", nil];
    
    if ([lvc isViewLoaded])
    {
        [lvc.searchBar removeFromSuperview];
        CGRect frame = lvc.resultsTableView.frame;
        lvc.resultsTableView.frame = CGRectMake(frame.origin.x, frame.origin.y-44, frame.size.width, frame.size.height+44);
        [lvc.resultsTableView reloadData];
        [lvc hideLoadingView];
    }
}

-(BOOL) canEdit
{
    return seg.selectedSegmentIndex == 1;
}

-(void)updateSaveBtn
{
    if(![ExSystem connectedToNetwork] ||
       ![self canEdit])
    {
        self.navigationItem.rightBarButtonItem = nil;
        return;
    }
    
	// Display edit button on nav bar
    UIBarButtonItem* saveBtn = nil;
    if ([ExSystem is7Plus])
    {
        saveBtn = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Search"] style:UIBarButtonSystemItemSearch target:self action:@selector(actionSave:)];
        [saveBtn setEnabled:self.isDirty];
    }
    else
    {
        saveBtn = [FormViewControllerBase makeNavButton:@"Search" enabled:self.isDirty target:self action:@"actionSave:"];
    }
	  

	self.navigationItem.rightBarButtonItem = nil;
	
	[self.navigationItem setRightBarButtonItem:saveBtn animated:NO];
}

-(void) loadAttendeeFormForAttendeeTypeKey:(NSString*)atnTypeKey
{
    if (self.atnEntryForm == nil || ![atnTypeKey isEqualToString: atnEntryForm.atnTypeKey])
    {
        self.atnEntryForm = nil;
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:atnTypeKey, @"ATTENDEE_TYPE_KEY", nil];
        [[ExSystem sharedInstance].msgControl createMsg:LOAD_ATTENDEE_FORM CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

-(void)actionSave:(id)sender
{
	[self showWaitView];

    [self loadAttendeeFormForAttendeeTypeKey:self.curAtnTypeKey];  // MOB-9693
    self.fullSearchResults = nil;
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.allFields, @"FIELDS", self.curAtnTypeKey, @"ATN_TYPE_KEY", self.expKey, @"EXP_KEY", self.polKey, @"POL_KEY", self.rpeKey, @"RPE_KEY", nil];
    if (attendeeExclusionList != nil)
	{
		pBag[@"EXCLUSION_LIST"] = attendeeExclusionList;
	}

	[[ExSystem sharedInstance].msgControl createMsg:ATTENDEE_FULL_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void)initFieldsWithData:(NSArray*)newFields
{
    FormFieldData* atnTypeFld = self.allFields != nil && [self.allFields count]>0?
        ( FormFieldData*) (self.allFields)[0]:nil;
    
    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
	self.sections = [[NSMutableArray alloc] initWithObjects:@"Data", nil];
	
	NSMutableArray *fields = [[NSMutableArray alloc] init];
	
    self.allFields = [[NSMutableArray alloc] init];
    if (newFields == nil)
    {
        // Start with just the AtnType field
        FormFieldData* field = [[FormFieldData alloc] initField:@"AtnTypeKey" label:[Localizer getLocalizedText:@"Attendee Type"] value:@"" ctrlType:@"picklist" dataType:@"INTEGER"];
        field.required = @"Y";
        [allFields addObject:field];
        [fields addObject:field];
    }
    else
    {
        if (atnTypeFld) {
            [fields addObject:atnTypeFld];
        }
        [self.allFields addObject:atnTypeFld];
        for (FormFieldData *fld in newFields)
        {
            if (![fld.access isEqualToString:@"HD"])
            {
                [fields addObject:fld];
            }
            [self.allFields addObject:fld];
        }
    }
    sectionFieldsMap[@"Data"] = fields;
    [super initFields];
    
}
//
//-(void) initFields
//{
//    // Just the attendee type
//    self.sections = [[NSMutableArray alloc] initWithObjects:@"0", nil];
//    [self.sections release];
//    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
//	[self.sectionFieldsMap release];
//    
//    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
//	[self.sectionFieldsMap release];
//    
//    self.allFields = [[NSMutableArray alloc] initWithObjects: nil];
//    [allFields release];
//    
//    FormFieldData* field= nil;
//    field = [[FormFieldData alloc] initField:@"DaysRemaining" label:[Localizer getLocalizedText:@"Days Remaining at Location"] value:[Localizer getLocalizedText:@"Not Sure"] ctrlType:@"picklist" dataType:@"INTEGER"];
//    field.required = @"Y";
//    field.liKey = @"-1";   // -1 for Not Sure, 7 for week, 30 for month, 365 for year 
//    
//    [allFields addObject:field];
//    [field release];
//    [sectionFieldsMap setObject:[NSArray arrayWithObject:field] forKey:@"1"];
//    
//    field = [[FormFieldData alloc] initField:@"ImmediateAssistance" label:[Localizer getLocalizedText:@"Immediate Assistance Required"] value:@"Yes" ctrlType:@"edit" dataType:@"BOOLEANCHAR"];
//    field.liKey = @"Y";
//    field.required = @"Y";
//    
//    [allFields addObject:field];
//    [field release];
//    [sectionFieldsMap setObject:[NSArray arrayWithObject:field] forKey:@"2"];
//    
//    
//    field = [[FormFieldData alloc] initField:@"CommentPlain" label:[Localizer getLocalizedText:@"Comment"] value:@"" ctrlType:@"textarea" dataType:@"VARCHAR"];
//    [allFields addObject:field];
//    [field release];
//    
//    [sectionFieldsMap setObject:[NSArray arrayWithObject:field] forKey:@"3"];
//    
//	[super initFields];
//}

#pragma mark -
#pragma mark Form Edit Methods 
#pragma mark -
#pragma mark Error Alert Methods
-(void) showErrorMessage:(NSString*)message
{
	MobileAlertView* errorAlert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Error"]
																 message:message  
																delegate:nil 
                                                       cancelButtonTitle: [Localizer getLocalizedText:@"OK"] 
                                                       otherButtonTitles:nil];
	[errorAlert show];
}


-(void) fieldUpdated:(FormFieldData*) field
{
    if ([field.iD isEqualToString:@"AtnTypeKey"])
    {
        if (![field.liKey isEqualToString:self.curAtnTypeKey])
        {
            [self showLoadingViewWithText:[Localizer getLocalizedText:@"Waiting"]];
            // Switch fields
            NSArray* fields = (self.atnSearchForms)[field.liKey];
            [self initFieldsWithData:fields];
            self.curAtnTypeKey = field.liKey;
            [tableList reloadData];
            [self hideLoadingView];
        }
    }
    [super fieldUpdated:field];
}

-(void) showAttendeeFullSearchResults
{
    if (self.atnEntryForm != nil && self.fullSearchResults != nil)
    {
        // Show attendee results screen
        AttendeeFullSearchResultsVC *vc = [[AttendeeFullSearchResultsVC alloc] initWithNibName:@"AttendeeFullSearchResultsVC" bundle:nil];
        [vc setSeedData:self searchResults:self.fullSearchResults.attendees columns:self.fullSearchResults.atnColumns entryForm:self.atnEntryForm.fields];
        // MOB-7809. Wait view needs to be hidden *before* the new view controller (vc) is pushed,
        // because the hideWaitView code works on the top view controller of the nav.
        [self hideWaitView];
        
        [self.navigationController pushViewController:vc animated:YES];	
    }
}

#pragma mark - Msg Responder
-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];
	//respond to data that might be coming from the cache
    
	if ([msg.idKey isEqualToString:LIST_FIELD_SEARCH_DATA])
	{
		ListFieldSearchData *data = (ListFieldSearchData*)msg.responder;
		
		if (![data.fieldId isEqualToString:@"AtnTypeKey"])
			return;
        
		self.loadingAttendeeTypes = NO;
		if (msg.responseCode != 200 && !msg.isCache)
		{
//			[self showErrorMessage:[Localizer getLocalizedText:@"Could not load attendee data."]];
			return;
		}
		if (data.listItems == nil || [data.listItems count] == 0)
		{
//			[self showErrorMessage:[Localizer getLocalizedText:@"Attendee data is unavailable."]];
			return;
		}
        
		// Hold onto the array of attendee type list items
		self.atnTypes = data.listItems;
        
		// Walk through the list of attendee types and add the name of each attendee type to an array.
		ListItem* businessGuestType = nil;
		ListItem* employeeType = nil;
        NSDictionary* dict = self.excludedAtnTypeKeys==nil? nil:[[NSDictionary alloc] initWithObjects:self.excludedAtnTypeKeys forKeys:self.excludedAtnTypeKeys];
        NSMutableArray* exAtnTypes = [[NSMutableArray alloc] init];
		for (ListItem* attendeeType in atnTypes)
		{
			if (attendeeType.liKey != nil && attendeeType.liCode != nil)
			{
				if (businessGuestType == nil && [attendeeType.liCode isEqualToString:@"BUSGUEST"])
				{
					businessGuestType = attendeeType;
				}
				else if (employeeType == nil && [attendeeType.liCode isEqualToString:@"EMPLOYEE"])
				{
					employeeType = attendeeType;
				}
                
                if (dict[attendeeType.liKey]!=nil)
                {
                    [exAtnTypes addObject:attendeeType];
                }
			}
		}
        [((NSMutableArray*)self.atnTypes) removeObjectsInArray:exAtnTypes];
        
//        NSString * attendeeTypeKeyForForm = nil;
//        if (employeeType != nil && [dict objectForKey:employeeType.liKey] == nil)
//        {
//            attendeeTypeKeyForForm = employeeType.liKey;
//        }
//        else if (businessGuestType != nil && [dict objectForKey:businessGuestType.liKey] == nil)
//        {
//            attendeeTypeKeyForForm = businessGuestType.liKey;
//        }
//        else
//        {
//            attendeeTypeKeyForForm = ((ListItem*)[atnTypes objectAtIndex:0]).liKey;
//        }
		[self configureWaitView];
        
        // Release after default type form is loaded to prevent busGuestType from out of scope.
        
	}
    else if ([msg.idKey isEqualToString:ATTENDEE_SEARCH_FIELDS_DATA])
    {
        self.loadingAttendeeForm = NO;
		
		if (msg.responseCode != 200 && !msg.isCache)
		{
            
            MobileAlertView* errorAlert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Error"]                                
                    message:[Localizer getLocalizedText:@"Could not load attendee search fields data."]  
                    delegate:nil 
                    cancelButtonTitle: [Localizer getLocalizedText:@"OK"] 
                    otherButtonTitles:nil];
            [errorAlert show];
			return;
		}
		
		AttendeeSearchFieldsData *data = (AttendeeSearchFieldsData*)msg.responder;
        
        self.atnSearchForms = data.forms;
        
        [self refreshView]; // Refresh navbar, etc
		[self configureWaitView];

    }
    else if ([msg.idKey isEqualToString:ATTENDEE_FULL_SEARCH_DATA])
    {
		AttendeeFullSearchData *data = (AttendeeFullSearchData*) msg.responder;
		if (msg.responseCode == 200)
		{
            if (data.status != nil && ![@"SUCCESS" isEqualToString: data.status.status])
            {
                // MOB-9958
                NSString *errMsg = data.status.errMsg;
                if (![errMsg length])
                    errMsg = [Localizer getLocalizedText:@"The server encountered an error."];
                UIAlertView *alert = [[MobileAlertView alloc] 
                                      initWithTitle:[Localizer getLocalizedText:@"Search Failed"]
                                      message: errMsg
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                      otherButtonTitles:nil];
                [alert show];
                [self hideWaitView];
            }
            else 
            {   
                self.fullSearchResults = data;
                [self showAttendeeFullSearchResults];
            }
        }      
        else 
        {
            UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:[Localizer getLocalizedText:@"Search Failed"]
								  message:[Localizer getLocalizedText:@"The server encountered an error."]
								  delegate:nil
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
            [self hideWaitView];
        }
    }
	else if ([msg.idKey isEqualToString:ATTENDEE_SEARCH_DATA])
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
            [self hideWaitView];
            [self.navigationController popViewControllerAnimated:YES];
            
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
	else if ([msg.idKey isEqualToString:LOAD_ATTENDEE_FORM])
	{
		if (msg.responseCode != 200 && !msg.isCache)
		{
//			[self showErrorMessage:[Localizer getLocalizedText:@"Could not load attendee form data."]];
            self.atnEntryForm = [[LoadAttendeeForm alloc] init]; // creates a dummy form and allow to continue
		}
        else {
            self.atnEntryForm =  (LoadAttendeeForm*)msg.responder;
        }
		
		// Remove the OwnerEmpName field.
		for (FormFieldData *fld in atnEntryForm.fields)
		{
			if ([fld.iD isEqualToString:@"OwnerEmpName"])
			{
				[atnEntryForm.fields removeObject:fld];
				break;
			}
		}
        
        [self showAttendeeFullSearchResults];
    }
}


-(void) configureWaitView
{	
    // MOB-8010 load search forms before start full search
    if ((!loadingAttendeeTypes && !loadingAttendeeForm)) {
        [self hideWaitView];
        [self hideLoadingView];
    }
    else {
        [self showLoadingView];
    }
}

-(void) loadAttendeeTypes
{
	self.loadingAttendeeTypes = YES;
	FormFieldData *field = [[FormFieldData alloc] init];
	field.iD = @"AtnTypeKey";
    field.ftCode = @"ATNSEARCH";
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: field, @"FIELD", nil];
	[[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	[self configureWaitView];
}

-(void) loadAttendeeSearchFields
{
	
	self.loadingAttendeeForm = YES;
	// call GetAttendeeSearchFields end point
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];

	[[ExSystem sharedInstance].msgControl createMsg:ATTENDEE_SEARCH_FIELDS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	[self configureWaitView];
}


#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (tableView == self.tableList)
    {
        return [super tableView:tableView numberOfRowsInSection:section];
    }
	return searchResults == nil? 0: [searchResults count];
}

- (UITableViewCell *) qSearchTableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *) indexPath
{
    // This is for quick search results 
    
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

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (tableView == self.tableList)
    {
        return [super tableView:tableView cellForRowAtIndexPath:indexPath];
    }
    else
        return [self qSearchTableView:tableView cellForRowAtIndexPath:indexPath];
}


#pragma mark -
#pragma mark Table view delegate
- (void)qSearchAtnSelected:(NSIndexPath *)indexPath
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
    
	[self.navigationController popViewControllerAnimated:YES];
	
	// Notify the delegate
	if (self.delegate != nil)
		[self.delegate attendeeSelected:attendee];
    
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (tableView == self.tableList)
    {
        [super tableView:tableView didSelectRowAtIndexPath:indexPath];
    }
    else
        [self qSearchAtnSelected:indexPath];
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
	[[ExSystem sharedInstance].msgControl createMsg:ATTENDEE_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES Options:SILENT_ERROR RespondTo:self ];
}

-(void) startAdvancedSearch
{
    self.curAtnTypeKey = nil;
    [self initFieldsWithData:nil];
    [self.tableList reloadData];

    // Retrieve AtnType list
    [self loadAttendeeTypes];
    [self loadAttendeeSearchFields];
    self.isDirty = NO;
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
#pragma mark SegmentedControl Methods
-(IBAction)setSearchMode:(id)sender
{
    if(seg.selectedSegmentIndex == 1)
    {
        if (self.resultsTableView.hidden == NO)
        {
            [self.resultsTableView setHidden:YES];
        }
        [self startAdvancedSearch];
        [self.tableList setHidden:NO];
    }
    else
    {
        if (self.resultsTableView.hidden == YES)
        {
            [self.resultsTableView setHidden:NO];
        }
        [self.tableList setHidden:YES];
    }
    
    [self updateSaveBtn];
}

#pragma mark -
#pragma mark AttendeeSearchDelegate Methods
-(void) attendeeSelected:(AttendeeData*)attendee
{
    if (self.delegate != nil)
        [self.delegate attendeeSelected:attendee];
    
    [self.navigationController popViewControllerAnimated:YES];
}

-(void) attendeesSelected:(NSArray*)attendees
{
    if (self.delegate != nil)
        [self.delegate attendeesSelected:attendees];
    
    [self.navigationController popViewControllerAnimated:YES];
}


@end
