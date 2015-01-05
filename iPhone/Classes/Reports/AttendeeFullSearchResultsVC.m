//
//  AttendeeFullSearchResultsVC.m
//  ConcurMobile
//
//  Created by yiwen on 10/14/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "AttendeeFullSearchResultsVC.h"
#import "SummaryCellMLines.h"
#import "AttendeeViewDetailVC.h"
#import "SaveAttendeeData.h"
#import "LabelConstants.h"

@implementation AttendeeFullSearchResultsVC

@synthesize searchResults, tableList;
@synthesize delegate = _delegate;
@synthesize atnColumns, selectedAtnDict, inAddAttendeeMode;
@synthesize externalAttendees, entryFields;

- (void)setSeedData:(id<AttendeeSearchDelegate>)del searchResults:(NSArray*) attendees columns:(NSArray*) cols entryForm:(NSArray *)fields
{
    self.delegate = del;
    self.searchResults = attendees;
    self.entryFields = fields;
    
    if (fields == nil)
    {
        // MOB-9693 If no entry form retrieved, just use detail form.
        self.entryFields = cols;
    }
    
    // Limit to first 4 columns, excluding count/amount
    self.atnColumns = [[NSMutableArray alloc] init];
    for (FormFieldData* ff in cols)
    {
//        if ([self.atnColumns count] >=4)
//            break;
        
        if (![ff.iD isEqualToString:@"InstanceCount"] && ![ff.access isEqualToString:@"HD"])
        {
            [self.atnColumns addObject:ff];
        }
    }
}

-(FormFieldData*) findField:(NSString*) fldId inArray:(NSArray*) fields
{
    for (FormFieldData* fld in fields)
    {
        if ([fldId isEqualToString:fld.iD])
            return fld;
    }
    return nil;
}


- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - Msg Responder
-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];
	//respond to data that might be coming from the cache
    
	if ([msg.idKey isEqualToString:SAVE_ATTENDEE_DATA])
	{
		SaveAttendeeData *resp = (SaveAttendeeData*)msg.responder;
		
		if (msg.responseCode == 200 && 
            (![resp.status.status length] || [resp.status.status isEqualToString:@"SUCCESS"]))
		{
            NSString *externalId = [resp.attendeeToSave getNonNullableValueForFieldId:@"ExternalId"];
            if (resp.duplicateAttendees != nil && [resp.duplicateAttendees count]>0 /*&& self.createdAttendee*/)
            {
                // Check attendeeToSave.atnKey is nil
                resp.attendeeToSave.attnKey = nil;
                // Locate first duplicate with the same externalId; or the first duplicate
                for (AttendeeData * dupAtn in resp.duplicateAttendees)
                {
                    if ([externalId isEqualToString:[dupAtn getNullableValueForFieldId:@"ExternalId"]])
                    {
                        resp.attendeeToSave.attnKey = dupAtn.attnKey;
                        resp.attendeeToSave.versionNumber = dupAtn.versionNumber;
                        resp.attendeeToSave.currentVersionNumber = dupAtn.versionNumber;
                        break;
                    }
                }
                if (resp.attendeeToSave.attnKey == nil)
                {
                    // TODO - Prompt the user?
                    AttendeeData* dupAtn = (resp.duplicateAttendees)[0];
                    resp.attendeeToSave.attnKey = dupAtn.attnKey;
                    resp.attendeeToSave.versionNumber = dupAtn.versionNumber;
                    resp.attendeeToSave.currentVersionNumber = dupAtn.versionNumber;                    
                }
            }

            [self.externalAttendees removeObjectForKey:externalId];
            if ([self.externalAttendees count]==0)
            {
                [self hideWaitView];
                [self.navigationController popViewControllerAnimated:NO];

                // Notify the delegate
                if (self.delegate != nil)
                {
                    [self.delegate attendeesSelected:[self.selectedAtnDict allValues]];
                }
            }
		}
		else
		{
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:nil
								  message:[Localizer getLocalizedText:@"Could not save attendee data.  Please try again later."]
								  delegate:nil
								  cancelButtonTitle:nil
								  otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
			[alert show];
            [self hideWaitView];
		}
        
    }
}

-(NSString*) makeAtnKeyForDict :(AttendeeData*) atn
{
    if ([ atn.attnKey length])
    {
        return atn.attnKey;
    }
    else if ([[atn getNullableValueForFieldId:@"ExternalId"] length])
    {
        return [NSString stringWithFormat:@"ExternalId_%@", [atn getNullableValueForFieldId:@"ExternalId"]]; 
    }
    else {
        return @"";
    }
}

#pragma mark - Edit Methods
#pragma mark - AddToReport Stuff
#define kButtonA2RW 110
#define kButtonA2RH 30
-(UIBarButtonItem *)makeAddAttendeeButton:(NSInteger)count
{
    NSString *addText = count == -2? [@"Select Attendee to Add" localize] : [Localizer getLocalizedText:@"Add Attendee"];
    
    if(count > 0)
        addText = [NSString stringWithFormat:@"%@ (%ld)", addText, (long)count];
    //CGFloat fontSize = 13.0;
    
    CGSize textSize = [addText sizeWithFont:[UIFont boldSystemFontOfSize:12]];
    //CGSize textSize = [addText sizeWithFont:[UIFont boldSystemFontOfSize:13] minFontSize:fontSize actualFontSize:fontSize forWidth:150 lineBreakMode:NSLineBreakByTruncatingTail];
    
    CGFloat w = 150.0;
    CGFloat h = 30.0;
    
    if((textSize.width + 20) < w)
        w = textSize.width + 20;
    
    if ([ExSystem is7Plus]) {
        if(count == -2)
            return [[UIBarButtonItem alloc] initWithTitle:addText style:UIBarButtonItemStylePlain target:self action:@selector(buttonSelectPressed:)];
        else if(count == -1 || count == 0)
            return [[UIBarButtonItem alloc] initWithTitle:addText style:UIBarButtonItemStylePlain target:self action:nil];
        else if(count > 0)
            return [[UIBarButtonItem alloc] initWithTitle:addText style:UIBarButtonItemStylePlain target:self action:@selector(buttonAddAttendeesPressed:)];
    }
    else
    {
        if(count == -2)
            return [ExSystem makeColoredButton:@"BLUE_INACTIVE" W:w H:h Text:addText SelectorString:@"buttonSelectPressed:" MobileVC:self];
        else if(count == -1 || count == 0)
            return [ExSystem makeColoredButton:@"BLUE_INACTIVE" W:w H:h Text:addText SelectorString:nil MobileVC:self];
        else if(count > 0)
            return [ExSystem makeColoredButton:@"BLUE" W:w H:h Text:addText SelectorString:@"buttonAddAttendeesPressed:" MobileVC:self];
    }
    
    return nil;
}

-(void) updateAddAttendeeButton:(NSInteger) count
{
	UIBarButtonItem *btnSelect = [self makeAddAttendeeButton:count];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
	NSArray *toolbarItems = @[flexibleSpace, btnSelect];
	[self setToolbarItems:toolbarItems animated:YES];
}
-(void)buttonCancelPressed:(id)sender
{
//	[tableList setEditing:NO];
	inAddAttendeeMode = NO;
    
    self.navigationItem.rightBarButtonItem = nil;
    [self updateAddAttendeeButton:-2];
	[self.selectedAtnDict removeAllObjects];
	[tableList reloadData];
}

- (void)buttonSelectPressed:(id)sender
{
	inAddAttendeeMode = YES;
	
	NSString *cancel = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];

//	[self.tableList setEditing:YES animated:YES];
	
	UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:cancel style:UIBarButtonItemStyleBordered target:self action:@selector(buttonCancelPressed:)];
	self.navigationItem.rightBarButtonItem = nil;
    self.navigationItem.rightBarButtonItem = btnCancel;
	
	if([ExSystem connectedToNetwork])
	{
		[self updateAddAttendeeButton:0];
	}
	else 
		[self makeOfflineBar];
    
	
	[tableList reloadData];
}

- (void)buttonAddAttendeesPressed:(id)sender
{
    self.externalAttendees = [[NSMutableDictionary alloc] init];
    
    // MOB-9693 Check for SF attendee and save them first before notify delegate
    NSArray* selectedAttendees = [self.selectedAtnDict allValues];
    for (AttendeeData* atn in selectedAttendees)
    {
        if (atn.attnKey == nil)
        {
            NSString* externalId = [atn getNullableValueForFieldId:@"ExternalId"];
            if ([ externalId length])
            {
                (self.externalAttendees)[externalId] = atn;
                
                NSArray* existingFields = [atn.fields allValues];
                for (FormFieldData* fld in existingFields)
                {
                    FormFieldData* entrySrcFld = [self findField:fld.iD inArray:self.entryFields];
                    if (entrySrcFld != nil)
                    {
                        // Copy over field schema from attendee entry form.
                        fld.dataType = entrySrcFld.dataType;
                        fld.ctrlType = entrySrcFld.ctrlType;
                        // Copy over default value, if current value is empty
                        if (![fld.fieldValue length] && ![fld.liKey length])
                        {
                            fld.fieldValue = entrySrcFld.fieldValue;
                            fld.liKey = entrySrcFld.liKey;
                        }
                        
                    } 
                    else if ([fld.iD isEqualToString:@"InstanceCount"] || [fld.iD isEqualToString:@"AtnTypeKey"]|| [fld.iD isEqualToString:@"CrnKey"])
                    {
                        fld.dataType = @"LIST";
                        if ([fld.iD isEqualToString:@"InstanceCount"])
                            fld.ctrlType = @"edit";
                        else {
                            fld.ctrlType = @"picklist";
                        }
                    }
                    else 
                    {
                        fld.dataType = @"VARCHAR";
                        fld.ctrlType = @"edit";
                    }
                    
                    if ([fld.iD isEqualToString:@"AtnTypeKey"])
                        fld.liKey = atn.atnTypeKey;

                    fld.access = @"RW";
                }
                
                for (FormFieldData* entryFld in self.entryFields)
                {
                    // If field is not already in and has no default value, discard; otherwise, add a copy to atn
                    FormFieldData* existingFld = [self findField:entryFld.iD inArray:existingFields];
                    if (existingFld == nil)
                    {
                        if ([ entryFld.fieldValue length] || [entryFld.liKey length])
                        {
                            FormFieldData* entryFldCopy = [entryFld copyWithZone:nil];
                            [atn.fieldKeys addObject:entryFldCopy.iD];
                            (atn.fields)[entryFldCopy.iD] = entryFldCopy;
                        }
                    }
                }
                
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:atn, @"ATTENDEE", @"YES", @"SKIP_CACHE", nil];
                [[ExSystem sharedInstance].msgControl createMsg:SAVE_ATTENDEE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
                
                if ([self.externalAttendees count]==1)
                    [self showWaitView];
            }
        }
    }
	// Notify the delegate
	if (self.delegate != nil && [self.externalAttendees count] == 0)
    {
        [self.navigationController popViewControllerAnimated:NO];
		[self.delegate attendeesSelected:selectedAttendees];
    }
}
#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.title = [@"Attendee Search Results" localize];
    self.navigationController.toolbarHidden = NO;
	[self.tableList setAllowsSelectionDuringEditing:YES];
	
    self.selectedAtnDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    
	if (searchResults != nil && [searchResults count] > 0)
		[self buttonCancelPressed:nil];

}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)viewDidAppear:(BOOL)animated 
{
	[super viewDidAppear:animated];

    if (searchResults == nil || [searchResults count]==0)
    {
        [self showNoDataView:self];
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

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (self.atnColumns == nil || [self.atnColumns count] <= 2)
        return 54;
    else if ([self.atnColumns count] == 3) 
        return 64;
    else
        return 80;
}


-(SummaryCellMLines*)makeCell:(UITableView*)tblView attendee:(AttendeeData*)atn
{
	SummaryCellMLines *cell = (SummaryCellMLines *)[tblView dequeueReusableCellWithIdentifier:@"SummaryCell4Lines"];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SummaryCell4Lines" owner:self options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[SummaryCellMLines class]])
			{
				cell = (SummaryCellMLines *)oneObject;
				break;
			}
		}
	}
	
    NSUInteger lineCount = self.atnColumns == nil? 0 : [self.atnColumns count];
    // Line 0-1 should not be empty - default to FullName/Company if no column definition.
    NSString* name = lineCount >0?[atn getNonNullableValueForFieldId:((FormFieldData*)(self.atnColumns)[0]).iD]
                :[atn getFullName];
    NSString* line1 = lineCount>1? [atn getNonNullableValueForFieldId:((FormFieldData*)(self.atnColumns)[1]).iD] 
                : [atn getNonNullableValueForFieldId:@"Company"];
    NSString* line2 = lineCount>2? [atn getNullableValueForFieldId:((FormFieldData*)(self.atnColumns)[2]).iD] : nil;
    NSString* line3 = lineCount>3? [atn getNullableValueForFieldId:((FormFieldData*)(self.atnColumns)[3]).iD] : nil;
    [cell resetCellContent:name withAmount:@"" withLine1:line1 withLine2:line2 withImage1:nil withImage2:nil withImage3:nil];
    cell.lblLine3.text = line3;
	return cell;
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
//	static NSString *CellIdentifier = @"Cell";
	
    NSUInteger row = [indexPath row];
    
//    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
//    if (cell == nil) {
//        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:CellIdentifier] autorelease];
//    }
    
    // Configure the cell...
    AttendeeData *attendee = searchResults[row];
    SummaryCellMLines* cell = [self makeCell:tableView attendee:attendee];
    [cell layoutWithSelect:self.inAddAttendeeMode];
    
    if(self.inAddAttendeeMode)
	{
		NSString *sRow = [self makeAtnKeyForDict: attendee];
        [cell selectCell:selectedAtnDict[sRow] != nil];
	}
    else
        [cell selectCell: NO];
	
	if (!self.inAddAttendeeMode) 
    {
        if (self.atnColumns != nil && [self.atnColumns count]>4)
            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    }
	else 
		[cell setAccessoryType:UITableViewCellAccessoryNone];

//	cell.textLabel.text = [attendee getFullName];
//	cell.detailTextLabel.text = [attendee getNonNullableValueForFieldId:@"Company"];
    return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
	AttendeeData *attendee = searchResults[row];
    if (self.inAddAttendeeMode)
    {
        NSString* atnDictKey = [self makeAtnKeyForDict:attendee];
        // Toggle the selection
        if ((self.selectedAtnDict)[atnDictKey]==nil)
        {
            (self.selectedAtnDict)[atnDictKey] = attendee;
        }
        else {
            [self.selectedAtnDict removeObjectForKey:atnDictKey];
        }
        SummaryCellMLines* cell = (SummaryCellMLines*) [tableView cellForRowAtIndexPath:indexPath];
		[cell selectCell: (selectedAtnDict[atnDictKey] != nil)];
        [self updateAddAttendeeButton:[self.selectedAtnDict count]];
    }
    else
    {
        [self showAttendeeEditor:attendee];
    }
}

-(void) showAttendeeEditor:(AttendeeData*)attendee
{
	AttendeeEntryEditViewController *editorVC = [[AttendeeEntryEditViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
	editorVC.editorDelegate = self;
	[editorVC editAttendee:attendee];
    editorVC.bAllowEditAtnAmt = NO;
    editorVC.bAllowEditAtnCount = NO;

//    editorVC.crnCode = [self.attendeeActionDelegate getAttendeeCrnCode];
    [self.navigationController pushViewController:editorVC animated:YES];
}

-(BOOL) canEdit
{
    return FALSE;
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

@end
