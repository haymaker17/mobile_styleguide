//
//  ReportAttendeesViewController.m
//  ConcurMobile
//
//  Created by yiwen on 5/26/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReportAttendeesViewController.h"
#import "ExSystem.h" 

#import "EntryData.h"
#import "AttendeeEntryEditViewController.h"
#import "LabelConstants.h"
#import "SummaryCellMLines.h"
#import "AttendeeActionManager.h"
#import "ExpenseTypesManager.h"
#import "MobileAlertView.h"
#import "FormatUtils.h"
#import "TextEditVC.h"
#import "UserConfig.h"
#import "AttendeeType.h"

@interface ReportAttendeesViewController (Private)
-(void)buttonAddPressed:(id) sender;
- (NSString*) getNoShowsLabel;

@end

@implementation ReportAttendeesViewController

@synthesize delegate = _delegate;
@synthesize tableView;
@synthesize attendeeActionManager;
@synthesize attendees;
@synthesize transactionAmount;


@synthesize lblBumpHelpTitle, lblBumpHelpText1, lblBumpHelpText2, btnBumpShare;
@synthesize btnBumpCancel, ivBumpBackground, viewBumpHelp;

@synthesize isDirty;
@synthesize canEdit;
@synthesize excludedAtnTypeKeys, expKey, polKey, entry;
@synthesize atnColumns;

#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return APPROVE_VIEW_ATTENDEES;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_MODAL;
}

-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:DEFAULT_ATTENDEE_DATA])
	{
		// The DefaultAttendeeData class took care of adding the default attendee the ExpenseTypesManager,
		// so all we have to do here is hide the wait view.
		//
        if ([self isViewLoaded]) {
            [self hideLoadingView];
        }
	}
}


#pragma mark -
#pragma mark Initialization
-(void) configureAttendees:(NSMutableArray*)attendeesArray columns:(NSMutableArray*)cols crnCode:(NSString*)atnCrnCode transactionAmount:(NSDecimalNumber*)amount
{
	self.crnCode = atnCrnCode;
	self.transactionAmount = amount;
	
	self.attendees = [[NSMutableArray alloc] init]; 	
	for (AttendeeData *attendee in attendeesArray)
	{
		AttendeeData *copyOfAttendee = [attendee copy];
		[self.attendees addObject:copyOfAttendee];
	}
    
    // Limit to first 4 columns, excluding count/amount
    self.atnColumns = [[NSMutableArray alloc] init];
    for (FormFieldData* ff in cols)
    {
        if ([self.atnColumns count] >=4)
            break;
        
        if (![ff.iD isEqualToString:@"InstanceCount"] && ![ff.access isEqualToString:@"HD"])
        {
            [self.atnColumns addObject:ff];
        }
    }
}


#pragma mark -
#pragma mark View lifecycle
-(void)setupToolbar
{
    if (self.canEdit && [ExSystem connectedToNetwork])
    {
		UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
		
        self.navigationItem.rightBarButtonItem = nil;
        [self.navigationItem setRightBarButtonItem:btnAdd animated:NO];
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.title = [Localizer getLocalizedText:@"Attendees"];
	if (self.attendeeActionManager == nil)
	{
		self.attendeeActionManager = [[AttendeeActionManager alloc] initWithViewController:self attendeeActionDelegate:self canEdit:self.canEdit];
        attendeeActionManager.rpeKey = self.entry.rpeKey;
        attendeeActionManager.expKey = self.expKey;
        attendeeActionManager.polKey = self.polKey;
        attendeeActionManager.atnColumns = self.atnColumns;
        
	}
	
	[viewBumpHelp setHidden:YES];
    
	// Load the attendee representing this employee (if it hasn't already been loaded)
	if ([[ExpenseTypesManager sharedInstance] attendeeRepresentingThisEmployee] == nil &&
		[ExSystem sharedInstance].entitySettings.firstName != nil &&
		[ExSystem sharedInstance].entitySettings.lastName != nil &&
        [ExSystem connectedToNetwork])
	{
		[self showLoadingView];
		
		NSString *userName = [NSString stringWithFormat:@"%@ %@", [ExSystem sharedInstance].entitySettings.firstName, [ExSystem sharedInstance].entitySettings.lastName];
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:userName, @"QUERY", @"YES", @"SKIP_CACHE", nil];
		[[ExSystem sharedInstance].msgControl createMsg:DEFAULT_ATTENDEE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
    
    if ((attendees == nil || [attendees count] ==0)&& ![self allowNoShow])
    {
        [self showNoDataView:self];
    }
    [self setupToolbar];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
	[tableView reloadData];
    
    // MOB-10793 This view controller might bring up the AttendeeDuplicateVC which will show the nav bar.
    // When that view disappears and this view reappears, it may be necessary to re-hide the toolbar.
    BOOL shouldHideToolbar = ((attendees == nil || [attendees count] ==0) && ![self allowNoShow]);
    [self.navigationController setToolbarHidden:shouldHideToolbar];
}

-(BOOL) validateInteger:(NSString*) trimmedVal
		   integerValue:(int*)intVal
{
	NSScanner* scanner = [NSScanner scannerWithString:trimmedVal];
	[scanner setLocale:[NSLocale currentLocale]];
	
	if ([scanner isAtEnd] == NO)
	{
		if (![scanner scanInteger:intVal])
		{
			return FALSE;
		}
		else 
		{
			// Make sure no garbage character at the end or 0.0 value
			if (![scanner isAtEnd] || *intVal == INT_MAX)
			{
				return FALSE;
			}
		}
	}
	else {
		return FALSE;
	}
	
	return TRUE;
}

#pragma mark Editing Delegate methods
-(void) textUpdated:(NSObject*) context withValue:(NSString*) value
{
    // Validate the text
    int intVal = 0;
    if (![value length])
    {
        if (![self.entry.noShowCount length])
            return;
        else
            value = @"0"; // Set the value to zero
    }
    
    // Return, if no change made
    if ([value isEqualToString:self.entry.noShowCount])
        return;
    
    BOOL isValid = [self validateInteger: value integerValue:&intVal];
    
    if (isValid && intVal>=0)
    {
        if ([value length])
        {
            self.entry.noShowCount = value;
            
//            NSUInteger _path[2] = {0, 0};
//            NSIndexPath *ixPath = [[[NSIndexPath alloc] initWithIndexes:_path length:2] autorelease];
//            NSArray* ixPaths = [[[NSArray alloc] initWithObjects:ixPath, nil] autorelease];
//            [self.tableView reloadRowsAtIndexPaths:ixPaths withRowAnimation:UITableViewRowAnimationRight];
//            
            // To trigger isDirty=TRUE on EntryView, and recalc amount
            [self attendeeListChanged];

        }
    }
    else
    {
        MobileAlertView *alert = [[MobileAlertView alloc] 
                                  initWithTitle:nil
                                  message:[NSString stringWithFormat:@"%@ %@", [@"INTEGER_VALIDATION_ERR_MSG" localize], [@"No Shows Tip" localize]]
                                  delegate:nil 
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
        [alert show];

    }
}



-(void)showNoShowEditor
{
    TextEditVC *vc = [[TextEditVC alloc] initWithNibName:@"TextEditVC" bundle:nil];
    NSString* val = self.entry.noShowCount;

    NSString* noShowsLabel = [self getNoShowsLabel];
    NSString* noShowsTip = [NSString stringWithFormat:[@"No Shows Tip" localize], noShowsLabel];
    [vc setSeedData:val context:self
           delegate:self
                tip:noShowsTip
              title:noShowsLabel
             prompt:nil
          isNumeric:YES
         isPassword:NO
                err:@""];
    
    [self.navigationController pushViewController:vc animated:YES];
    
}

-(BOOL) allowNoShow
{
    ExpenseTypeData* expType = [[ExpenseTypesManager sharedInstance] 
                                expenseTypeForVersion:@"V3" policyKey:self.polKey
                                expenseKey:self.expKey forChild:self.entry.parentRpeKey!=nil];
    BOOL result = [@"Y" isEqualToString: expType.allowNoShows];
    return result;
}

#pragma mark Table Header Overrides
- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    // MOB-8523 
    if ([self allowNoShow] && section ==0)
        return nil;
    
    if (attendees == nil || [attendees count]== 0)
        return nil;
    
    // MOB-10197 don't show "Swipe to delete" label if user is approver
    if (!self.canEdit || ![ExSystem connectedToNetwork])
        return nil;
    
    float w = 320;//[self isLandscape]? 480:320;//self.view.frame.size.width;
    NSString *emptyText = [Localizer getLocalizedText:@"Swipe to delete"];
    
    CGSize maxSize = CGSizeMake(w-30, 200);
    CGSize s = [emptyText sizeWithFont:[UIFont systemFontOfSize:14.0f] constrainedToSize:maxSize];
    
    CGRect lblRect =  CGRectMake(10, 0, w-20, s.height+12); // 6 px paddling
    UILabel* labelView = [[UILabel alloc] initWithFrame:lblRect];
    __autoreleasing UIView* footerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, w, s.height+21)];
    footerView.autoresizingMask =UIViewAutoresizingFlexibleWidth;
    footerView.backgroundColor = [UIColor clearColor];
    [labelView setText:emptyText];
    [labelView setBackgroundColor:[UIColor clearColor]];
    [labelView setTextAlignment:NSTextAlignmentCenter];
    [labelView setFont:[UIFont systemFontOfSize:14.0f]];
    [labelView setTextColor:[UIColor darkGrayColor]];
    [labelView setShadowColor:[UIColor whiteColor]]; 
    [labelView setShadowOffset:CGSizeMake(1.0f, 1.0f)];
    labelView.numberOfLines = 0;
    labelView.lineBreakMode = NSLineBreakByWordWrapping;
    labelView.autoresizingMask = UIViewAutoresizingFlexibleWidth; // Do not adjust height
    [footerView addSubview:labelView];
	return footerView;
}

- (CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    if ([self allowNoShow] && section ==0)
        return 0.0;
    
    if (attendees == nil || [attendees count]== 0)
        return 0.0;
    
    //MOB-10197 don't show "Swipe to delete" label if user is approver
    if (!self.canEdit)
        return 0.0;
    
	return 44.0;
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    int atnSecCount = ((attendees == nil || [attendees count] == 0) ? 0 : 1);
    if ([self allowNoShow])
        atnSecCount ++;
    return atnSecCount;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if ([self allowNoShow] && section ==0)
        return 1;
	return (attendees == nil ? 0 : [attendees count]);
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if ([self allowNoShow] && [indexPath section] == 0)
        return 54;
    else if (self.atnColumns == nil || [self.atnColumns count] <= 2)
        return 54;
    else if ([self.atnColumns count] == 3) 
        return 64;
    else
        return 80;
}

-(UITableViewCell*)makeCell:(UITableView*)tblView attendee:(AttendeeData*)atn amount:(NSString*)amount count:(NSString*)instanceCount
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
	
    int lineCount = [self.atnColumns count];
    NSString* name = [atn getNonNullableValueForFieldId:((FormFieldData*)(self.atnColumns)[0]).iD];
    NSString* line1 = lineCount>1? [atn getNonNullableValueForFieldId:((FormFieldData*)(self.atnColumns)[1]).iD] : @"";
    NSString* line2 = lineCount>2? [atn getNullableValueForFieldId:((FormFieldData*)(self.atnColumns)[2]).iD] : nil;
    NSString* line3 = lineCount>3? [atn getNullableValueForFieldId:((FormFieldData*)(self.atnColumns)[3]).iD] : nil;
    [cell resetCellContent:name withAmount:amount withLine1:line1 withLine2:line2 withImage1:nil withImage2:nil withImage3:nil];
    cell.lblLine3.text = line3;
	cell.lblRLine1.text = instanceCount;
	return cell;
}

-(UITableViewCell*)makeCell:(UITableView*)tblView name:(NSString*)name company:(NSString*)company amount:(NSString*)amount attendeeType:(NSString*)attendeeType count:(NSString*)instanceCount
{
	SummaryCellMLines *cell = (SummaryCellMLines *)[tblView dequeueReusableCellWithIdentifier:@"SummaryCell2Lines"];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SummaryCell2Lines" owner:self options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[SummaryCellMLines class]])
			{
				cell = (SummaryCellMLines *)oneObject;
				break;
			}
		}
	}
	
    NSString* line1 = attendeeType;
    if ([company length])
        line1 = [NSString stringWithFormat:@"%@ - %@", company, attendeeType]; 
    [cell resetCellContent:name withAmount:amount withLine1:line1 withLine2:nil withImage1:nil withImage2:nil withImage3:nil];
	cell.lblRLine1.text = instanceCount;
	return cell;
}

// MOB-9321 custom No Shows label
- (NSString*) getNoShowsLabel
{
    UserConfig* userCfg = [UserConfig getSingleton];
    NSString * result = [@"No Shows" localize];
    if (userCfg.attendeeTypes != nil && [userCfg.attendeeTypes count] > 0)
    {
        for (AttendeeType *atnType in [userCfg.attendeeTypes allValues])
        {
            if ([@"NOSHOWS" isEqualToString: atnType.atnTypeCode] && [atnType.atnTypeName length])
            {
                result = atnType.atnTypeName;
                break;
            }
        }
    }
    return result;
}

- (UITableViewCell *)tableView:(UITableView *)tblView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    NSUInteger section = [indexPath section];
    
    ExpenseTypeData* expType = [[ExpenseTypesManager sharedInstance] 
                                expenseTypeForVersion:@"V3" policyKey:self.polKey 
                                expenseKey:self.expKey forChild:self.entry.parentRpeKey!=nil];
    NSString *displayAtnAmounts = expType.displayAtnAmounts;
    NSString *allowEditAtnCount = expType.allowEditAtnCount;
	UITableViewCell *cell = nil;
    NSString* instanceCount = @"";

    if ([self allowNoShow] && section ==0)
    {
        if (![self.entry.noShowCount length])
            self.entry.noShowCount = @"0";
        NSDecimalNumber *noShowAmt = [NSDecimalNumber zero];
        NSString* noShowAmtStr = nil;
        if ([@"N" isEqualToString:displayAtnAmounts])
        {
            // MOB-9438 Per Novartis request, do not show "No Show" amount, if displayAtnAmounts = N.
            noShowAmtStr = @"";
// MOB-8776 No show is not limited by displayAtnAmounts and editAtnCount flags
        }
        else
        {
      		// MOB-15571: Amount for No shows should be 0 on parent entry (expense is itemized)
            if ( ![@"0" isEqualToString:self.entry.noShowCount] && ![self.delegate isParentEntry])
                noShowAmt = [AttendeeData getNoShowsAmount:attendees amount:transactionAmount];
            noShowAmtStr = [NSString stringWithFormat:@"%@", noShowAmt];
            noShowAmtStr = [FormatUtils formatMoneyWithoutCrn :noShowAmtStr crnCode: self.crnCode];
        }
        
        instanceCount = self.entry.noShowCount;
        NSString *noShowsLabel = [self getNoShowsLabel];
        cell = [self makeCell:tblView name:noShowsLabel company:@"" amount:noShowAmtStr attendeeType:@"" count:instanceCount];
        if ([self canEdit])
            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];

        return cell;
    }
    
    // Configure the cell...
	AttendeeData *attendee = attendees[row];
	NSString *name = [attendee getFullName];
	NSString *company = [attendee getNonNullableValueForFieldId:@"Company"];
	NSString *atnTypeName = [attendee getNonNullableValueForFieldId:@"AtnTypeName"];
    
    // MOB-10794: attendee.amount is coming in with decimal separator, the formatMoneyWithoutCrn expects local separators
    double d = [attendee.amount doubleValue];
    NSString* amt = [NSNumberFormatter localizedStringFromNumber:@(d)  numberStyle:NSNumberFormatterDecimalStyle];
    amt = [FormatUtils formatMoneyWithoutCrn:([self.delegate isParentEntry] ? @"0" : amt) crnCode:self.crnCode];

    if ([@"N" isEqualToString:displayAtnAmounts])
        amt = @"";
        
    if ([@"Y" isEqualToString:allowEditAtnCount])
        instanceCount = [NSString stringWithFormat:@"%d", attendee.instanceCount];
    
    int lineCount = [self.atnColumns count];
    if (lineCount <=2)
        cell = [self makeCell:tblView name:name company:company amount:amt attendeeType:atnTypeName count:instanceCount];
    else 
        cell = [self makeCell:tblView attendee:attendee amount:amt count:instanceCount];
    
	if ([self isAttendeeEditable:attendee] || [self canViewAttendee:attendee])
		[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
	else
		[cell setAccessoryType:UITableViewCellAccessoryNone];
    
    return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSInteger row = [indexPath row];
    NSUInteger section = [indexPath section];

    if (![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Offline"]
                              message:[Localizer getLocalizedText:@"Attendees not editable offline"]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                              otherButtonTitles:nil];
        [alert show];
        return;
    }
    
    if ([self allowNoShow] && section ==0)
    {
        if ([self canEdit])
            [self showNoShowEditor];
        return;
    }
    
	if (row < [self.attendees count])
	{
		AttendeeData *selectedAttendee = (self.attendees)[row];
        if (![self isAttendeeEditable:selectedAttendee] && ![self canViewAttendee:selectedAttendee])
        {
            return;
        }
        
        AttendeeData *thisEmployee = [[ExpenseTypesManager sharedInstance] attendeeRepresentingThisEmployee];
        
        if (thisEmployee == nil ||
            thisEmployee.attnKey == nil ||
            selectedAttendee.attnKey == nil ||
            ![selectedAttendee.attnKey isEqualToString:thisEmployee.attnKey])
        {
            if ((selectedAttendee.versionNumber == nil && selectedAttendee.currentVersionNumber == nil) ||
                [selectedAttendee.versionNumber isEqualToString:selectedAttendee.currentVersionNumber])
            {
                [attendeeActionManager showAttendeeEditor:selectedAttendee parentView:[self.delegate isParentEntry]];
            }
            else
            {
                MobileAlertView *alert = [[MobileAlertView alloc] 
                                          initWithTitle:[Localizer getLocalizedText:@"ATTENDEE_ENTRY"]
                                          message:[Localizer getLocalizedText:@"This attendee has been modified"] //@"This attendee has been modified since it was last used on this expense.  Any edits must be completed in Profile on the web."
                                          delegate:nil 
                                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                          otherButtonTitles:nil];
                [alert show];
            }
        }
        else
        {
            MobileAlertView *alert = [[MobileAlertView alloc] 
                                      initWithTitle:[Localizer getLocalizedText:@"ATTENDEE_ENTRY"]
                                      message:[Localizer getLocalizedText:@"This attendee cannot be edited."]
                                      delegate:nil 
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                      otherButtonTitles:nil];
            [alert show];
        }
	}
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)aTableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
	if (self.canEdit && [ExSystem connectedToNetwork])
	{
        NSUInteger section = [indexPath section];
        if (!([self allowNoShow] && section ==0))
            return UITableViewCellEditingStyleDelete;
    }
    return UITableViewCellEditingStyleNone;
}


- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete && self.canEdit)
	{
		NSUInteger row = [indexPath row];
		
        if (row < [attendees count])
        {
            [attendees removeObjectAtIndex:row];
            [self attendeeListChanged];
/*            if ([attendees count] > 0)
            {
                [AttendeeData divideAmountAmongAttendees:attendees amount:transactionAmount crnCode:crnCode];
                [self.tableView deleteRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath, nil] withRowAnimation:UITableViewRowAnimationFade];
            }
            
            [self.tableView reloadData];	// Redraw everything whether there are attendees left (amounts may have changed) or not (no more attendee rows)*/
        }
	}
}


#pragma mark -
#pragma mark Editing Methods
-(BOOL) isAttendeeEditable:(AttendeeData*)attendee
{
    return canEdit; 
}

-(BOOL) canViewAttendee:(AttendeeData*) attendee
{
    return TRUE;
// MOB-8368 This employee is handled in didSelect API by comparing to default attendee
// We don't pass back atnTypeCode.  Approver can view This Employee record anyway.
//    NSString* atnTypeCode = [attendee getNonNullableValueForFieldId:@"AtnTypeCode"];
//    return !([atnTypeCode isEqualToString:@"SYSEMP"]);
}

#pragma mark -
#pragma mark AttendeeActionDelegate Methods
-(NSString*) getAttendeeCrnCode
{
    return self.crnCode;
}

-(NSArray*) getExcludedAtnTypeKeys
{
    return self.excludedAtnTypeKeys;
}

-(void) attendeeListChanged
{
    self.isDirty = TRUE;
    int noShowCount = ![self.entry.noShowCount length] ? 0: [self.entry.noShowCount intValue];
	if ([attendees count] > 0)
	{
		[AttendeeData divideAmountAmongAttendees:attendees noShows:noShowCount amount:transactionAmount crnCode:self.crnCode];
        
        [self hideNoDataView];
        [self.navigationController setToolbarHidden:NO];
	}
    else
    {
        if ((attendees == nil || [attendees count] ==0) && ![self allowNoShow])
        {
            [self showNoDataView:self];
        }
        else
        {
            [self.navigationController setToolbarHidden:NO];
        }
    }
	
    [self.delegate attendeesEdited:attendees];

	if ([self isViewLoaded])
		[tableView reloadData];
}

#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc. that aren't in use.
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
}

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
	self.tableView = nil;
    [super viewDidUnload];
}

- (void)dealloc
{
	
	
    
	if (attendeeActionManager != nil)
	{
		attendeeActionManager.viewController = nil;
		attendeeActionManager.attendeeActionDelegate = nil;
	}
	
    
}


#pragma mark -
#pragma mark NoData Delegate Methods 
-(void)buttonAddPressed:(id) sender
{
    if (attendees == nil)
    {
        self.attendees = [[NSMutableArray alloc] init];
    }
    
    // Note: The AttendeeActionManager will modify the attendees array directly.
    [attendeeActionManager showAttendeeActionSheetForAttendees:self.attendees clicked:sender];
    
}

-(void) actionOnNoData:(id)sender
{
    [self buttonAddPressed:sender];
}

@end
