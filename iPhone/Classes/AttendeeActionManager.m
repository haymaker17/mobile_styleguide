//
//  AttendeeActionManager.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/14/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "AttendeeActionManager.h"
#import "LabelConstants.h"
#import "MCLogging.h"
#import "ExSystem.h" 

#import "AttendeeSearchVC.h"
#import "AttendeeEntryEditViewController.h"
#import "ReportAttendeesViewController.h"
#import "MobileActionSheet.h"
#import "AttendeeFullSearchVC.h"
#import "ExpenseTypesManager.h"
#import "AttendeeGroup.h"

@interface AttendeeActionManager (private)
-(NSArray*) getExcludedAtnTypeKeys;
@end

@implementation AttendeeActionManager

@synthesize attendeeActionDelegate = _attendeeActionDelegate;
@synthesize viewController;
@synthesize attendees;
@synthesize canEdit;
@synthesize expKey, polKey, rpeKey;
@synthesize atnColumns;

// Id tag for action sheets
const int kAddAttendeeActions = 802;

#pragma mark -
#pragma mark Beginning of Lifecycle Methods
-(id) initWithViewController:(MobileViewController*)mvc attendeeActionDelegate:(id<AttendeeActionDelegate>)actionDelegate canEdit:(BOOL)editFlag
{
	self = [super init];
	if (self)
    {
        self.viewController = mvc;
        self.attendeeActionDelegate = actionDelegate;
        
        self.canEdit = editFlag;
    }
	return self;
}


#pragma mark -
#pragma mark ActionSheet Methods
-(void) showAttendeeActionSheetForAttendees:(NSMutableArray*)attendeesArray clicked:(id)sender
{
	if (attendeesArray == nil)
	{
		[[MCLogging getInstance] log:@"AttendeeActionManager::showAttendeeActionSheetForAttendees: Error: attendeesArray is nil." Level:MC_LOG_DEBU];	
		return;
	}
	
	self.attendees = attendeesArray;
	
	MobileActionSheet * addAttendeeActions = nil;
	
    // MOB-8317 Check Display Add New Attendee setting.  In this case, the isForChild does not matter.
    ExpenseTypeData* expType = [[ExpenseTypesManager sharedInstance] 
                                expenseTypeForVersion:@"V3" policyKey:self.polKey 
                                expenseKey:self.expKey forChild:NO];
    if (expType == nil)
        expType = [[ExpenseTypesManager sharedInstance] 
                   expenseTypeForVersion:@"V3" policyKey:self.polKey
                   expenseKey:self.expKey forChild:YES];

    BOOL hideAddAttendeeBtn = [@"N" isEqualToString: expType.displayAddAtnOnForm];
    
    NSMutableArray* btnIds = [[NSMutableArray alloc] initWithObjects:@"ATTENDEE_SELECT_CONTACT", @"ATTENDEE_ADD_CONTACT", @"ATTENDEE_SEARCH_FOR_ATTENDEE", nil];
	addAttendeeActions = [[MobileActionSheet alloc] initWithTitle:nil
													 delegate:self 
											cancelButtonTitle:nil
									   destructiveButtonTitle:nil
											otherButtonTitles: 
						  nil];
	
    if (hideAddAttendeeBtn)
    {
        btnIds = [[NSMutableArray alloc] initWithObjects: @"ATTENDEE_SEARCH_FOR_ATTENDEE", nil];
    }
    else
    {
         [addAttendeeActions addButtonWithTitle:[Localizer getLocalizedText:@"ATTENDEE_SELECT_CONTACT"]];
        [addAttendeeActions addButtonWithTitle:[Localizer getLocalizedText:@"ATTENDEE_ADD_CONTACT"]];
    }
    [addAttendeeActions addButtonWithTitle:[Localizer getLocalizedText:@"ATTENDEE_SEARCH_FOR_ATTENDEE"]];
    addAttendeeActions.btnIds = btnIds;
    [addAttendeeActions addButtonWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
    addAttendeeActions.cancelButtonIndex = [btnIds count]; 
    

	addAttendeeActions.tag = kAddAttendeeActions;
	if([UIDevice isPad])
	{
        if (viewController.actionPopOver != nil)
        {
            [viewController.actionPopOver dismissWithClickedButtonIndex:(viewController.actionPopOver.numberOfButtons-1) animated:NO];
        }
        viewController.actionPopOver = addAttendeeActions;

        if ([sender isKindOfClass:[UIBarButtonItem class]])
            [addAttendeeActions showFromBarButtonItem:sender animated:YES];
        else
        {
            CGRect myRect = CGRectMake(0, 300, 1, 1);
            if (sender != nil && [sender isKindOfClass:[UIView class]])
            {
                myRect = ((UIView*) sender).frame;
                UIView* parentView = ((UIView*) sender).superview;
                if (viewController.view != nil && parentView != viewController.view)
                    myRect = [viewController.view convertRect:myRect fromView:parentView];
            }
            [addAttendeeActions showFromRect:myRect inView:viewController.view animated:YES];
        }
	}
	else 
	{
		addAttendeeActions.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
		// MOB-4525 This screen has no toolbar, so show from view boundaries
        BOOL tbrHidden = self.viewController.navigationController.toolbar.isHidden;
        if (tbrHidden)
            [addAttendeeActions showFromRect:CGRectMake(0, 0, viewController.view.frame.size.width, viewController.view.frame.size.height) inView:viewController.view animated:YES];
        else
            [addAttendeeActions showFromToolbar:self.viewController.navigationController.toolbar];
	}
}

-(void) showAttendeeActionSheetForAttendees:(NSMutableArray*)attendeesArray
{
    [self showAttendeeActionSheetForAttendees:attendeesArray clicked:nil];
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex 
{
    MobileActionSheet* mas = (MobileActionSheet*) actionSheet;
    
	if ([@"ATTENDEE_SELECT_CONTACT" isEqualToString:[mas getButtonId:buttonIndex]])
	{
		[self showContactList];
	} else if ([@"ATTENDEE_ADD_CONTACT" isEqualToString:[mas getButtonId:buttonIndex]])
	{
		[self showManualAttendee];
	} else if ([@"ATTENDEE_SEARCH_FOR_ATTENDEE" isEqualToString:[mas getButtonId:buttonIndex]])
	{
		[self showAttendeeSearch];
	}
    
    self.viewController.actionPopOver = nil;
}


- (void)showContactList
{
	ABPeoplePickerNavigationController *contactPicker =
	[[ABPeoplePickerNavigationController alloc] init];
	
    contactPicker.peoplePickerDelegate = self;
	
    [viewController presentViewController:contactPicker animated:YES completion:nil];
	
}

- (void)showAttendeeSearch
{
	// It is possible that the same issue as MOB-4477 is occurring here.
	if (viewController.presentedViewController != nil)
		return;

    // MOB-7283 - SU app only supports Attendee quick search
    // MOB-6079
    AttendeeFullSearchVC *searchVC = [[AttendeeFullSearchVC alloc] initWithNibName:@"AttendeeFullSearchVC" bundle:nil];
    [searchVC setSeedData:self keysToExclude:[self getExcludedAtnTypeKeys] expKey:self.expKey polKey:self.polKey rpeKey:self.rpeKey];
    searchVC.attendeeExclusionList = self.attendees;
        
    [viewController.navigationController pushViewController:searchVC animated:YES];
    
	
}

-(void)showAddContact
{
	ABNewPersonViewController *personVC = [[ABNewPersonViewController alloc] init];
	personVC.newPersonViewDelegate = self;
	
	UINavigationController *newNavigationController = [[UINavigationController alloc] initWithRootViewController:personVC];
	
	[viewController presentViewController:newNavigationController animated:YES completion:nil];
	
}

-(void)showManualAttendee
{
	[self addAttendee:nil firstName:nil companyName:nil];	
}

-(NSArray*) getExcludedAtnTypeKeys
{
    if (self.attendeeActionDelegate != nil)
        return [self.attendeeActionDelegate getExcludedAtnTypeKeys];
    return nil;
}

-(void)setOptionsForAttendeeDetailView:(AttendeeEntryEditViewController*) vc
{
    ExpenseTypeData* expType = [[ExpenseTypesManager sharedInstance] 
                                expenseTypeForVersion:@"V3" policyKey:self.polKey 
                                expenseKey:self.expKey forChild:NO];
    if (expType == nil)
        expType = [[ExpenseTypesManager sharedInstance] 
                   expenseTypeForVersion:@"V3" policyKey:self.polKey 
                   expenseKey:self.expKey forChild:YES];
    
    BOOL fAllowEditAtnAmt = [@"Y" isEqualToString: expType.allowEditAtnAmt];
    BOOL fAllowEditAtnCount = [@"Y" isEqualToString: expType.allowEditAtnCount];
    vc.bAllowEditAtnCount = fAllowEditAtnCount;
    vc.bAllowEditAtnAmt = fAllowEditAtnAmt;
}
-(void)addAttendee:(NSString*) lastName firstName:(NSString*) firstName companyName:(NSString*) companyName
{
	AttendeeEntryEditViewController *editorVC = [[AttendeeEntryEditViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
	editorVC.editorDelegate = self;
	editorVC.excludedAtnTypeKeys = [self getExcludedAtnTypeKeys];
    editorVC.bAllowEditAtnCount = NO;
    editorVC.bAllowEditAtnAmt = NO;

	// Create a dictionary of initial values for fields on the new attendee.
	// Each dictionary entry's key is the field id.  The dictionary entry's value is the field value.
	// This is optional; the dictionary could be empty if we choose.
	//
	NSMutableDictionary *valuesDict = [[NSMutableDictionary alloc] init];
	
	if (lastName != nil)
		valuesDict[@"LastName"] = lastName;
	
	if (firstName != nil)
		valuesDict[@"FirstName"] = firstName;
	
	if (companyName != nil)
		valuesDict[@"Company"] = companyName;
	
	[editorVC createAttendeeWithInitialValues:valuesDict];
	
//	UINavigationController *nc = [[UINavigationController alloc] initWithRootViewController:editorVC];
//
//	if ([UIDevice isPad])
//		nc.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
//	else
//		nc.navigationBar.tintColor = [UIColor blackColor];
//	
//	if([UIDevice isPad])
//		nc.modalPresentationStyle = UIModalPresentationFormSheet;
//	[viewController presentViewController:nc animated:YES completion:nil];
    [viewController.navigationController pushViewController:editorVC animated:YES];
	
//	[nc release];
}


- (void)peoplePickerNavigationControllerDidCancel:(ABPeoplePickerNavigationController *)peoplePicker {
	
    [viewController dismissViewControllerAnimated:YES completion:nil];
	
}

- (void) addAttendeeFromAddressBook:(ABRecordRef) person
{
    NSString* firstName = (NSString *)CFBridgingRelease(ABRecordCopyValue(person, kABPersonFirstNameProperty));
    NSString* lastName = (NSString *)CFBridgingRelease(ABRecordCopyValue(person, kABPersonLastNameProperty));
	NSString* companyName = (NSString *)CFBridgingRelease(ABRecordCopyValue(person, kABPersonOrganizationProperty));
	
	[self addAttendee:lastName firstName:firstName companyName:companyName];	
}

- (BOOL)peoplePickerNavigationController:(ABPeoplePickerNavigationController *)peoplePicker
      shouldContinueAfterSelectingPerson:(ABRecordRef)person {
    // MOB-6096 Need to delay modal dismissal.  Dismiss modal view here causes iPad crash, after search contact.
    // No need to dismiss modal before show AttendeeForm, which is not modal.
    [viewController performSelector:@selector(dismissModalViewControllerAnimated:) withObject:NO afterDelay:0.0f];
	[self addAttendeeFromAddressBook:person];

    return NO;
}

- (BOOL)peoplePickerNavigationController:
(ABPeoplePickerNavigationController *)peoplePicker
	  shouldContinueAfterSelectingPerson:(ABRecordRef)person
                                property:(ABPropertyID)property
                              identifier:(ABMultiValueIdentifier)identifier{
	
    return NO;
	
}

- (void)newPersonViewController:(ABNewPersonViewController *)newPersonViewController 
	   didCompleteWithNewPerson:(ABRecordRef)person
{
    // MOB-6096 Need to delay modal dismissal.  Dismiss modal view here causes iPad crash, when search contact.
    // No need to dismiss modal before show AttendeeForm, which is not modal.
    [viewController performSelector:@selector(dismissModalViewControllerAnimated:) withObject:NO afterDelay:0.0f];
	if (person != nil)
		[self addAttendeeFromAddressBook:person];
}

-(void) showAttendeeEditor:(AttendeeData*)attendee parentView:(BOOL)isItemizedParentView
{
	// It is possible that the same issue as MOB-4477 is occurring here.
	if (viewController.presentedViewController != nil)
		return;
	
	AttendeeEntryEditViewController *editorVC = [[AttendeeEntryEditViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
	editorVC.editorDelegate = self;
	[editorVC editAttendee:attendee];
    [self setOptionsForAttendeeDetailView:editorVC];
    editorVC.crnCode = [self.attendeeActionDelegate getAttendeeCrnCode];
    
    // MOB-15565: if the item is itemized, it should display 0 on the attendee under the attendeeparent view
    if(isItemizedParentView){
        editorVC.isDisplayZero = YES;
    }
    
//	UINavigationController *nc = [[UINavigationController alloc] initWithRootViewController:editorVC];
//
//	if ([UIDevice isPad])
//		nc.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
//	else
//		nc.navigationBar.tintColor = [UIColor blackColor];
//	
//	if([UIDevice isPad])
//		nc.modalPresentationStyle = UIModalPresentationFormSheet;
//	[viewController presentViewController:nc animated:YES completion:nil];

	[viewController.navigationController pushViewController:editorVC animated:YES];
//	[nc release];
}

#pragma mark -
#pragma mark AttendeeEditorDelegate Methods
-(void)editedAttendee:(AttendeeData*)attendee createdByEditor:(BOOL)created
{
    // MOB-8699 - need to support list columns.  TODO - refetch the attendee in list view detail form
    // merge fields to support list detail view
    [attendee supportFields:self.atnColumns];
	if (created)
		[self.attendees addObject:attendee];
//    else {
//        for (AttendeeData* atn in self.attendees)
//        {
//            if ([atn.attnKey isEqualToString:attendee.attnKey])
//            {
//                [atn mergeFields:attendee];
//            }
//        }
//    }
	[self.attendeeActionDelegate attendeeListChanged];
}


#pragma mark -
#pragma mark AttendeeSearchDelegate Methods
-(void) attendeeSelected:(AttendeeData*)attendee
{
    // MOB-8107 Filter out attendees excluded but does not belong to entry in db
    for (AttendeeData* atn in self.attendees)
    {
        // Block duplicate attendee from being entered
        if ([atn.attnKey isEqualToString: attendee.attnKey])
            return;
    }
    // MOB-8699 - need to support list columns.  TODO - refetch the attendee in list view detail form
    [attendee supportFields:self.atnColumns];

	[self.attendees addObject:attendee];
	[self.attendeeActionDelegate attendeeListChanged];
}

-(void) attendeesSelected:(NSArray*)selAttendees
{
    BOOL hasExcludedAtnTypes = FALSE;
    BOOL attendeeAdded = FALSE;
    NSMutableDictionary* existingAtnKeys = [[NSMutableDictionary alloc] init];
    
    if (self.attendees != nil && [self.attendees count]>0)
    {
        for (AttendeeData* existingAtn in self.attendees)
        {
            existingAtnKeys[existingAtn.attnKey] = existingAtn;
        }
    }
    
    for (AttendeeData* attendee in selAttendees)
    {
        BOOL atnTypeExclude = FALSE;
        for (NSString* atnTypeKey in [self getExcludedAtnTypeKeys])
        {
            if ([attendee.atnTypeKey isEqualToString:atnTypeKey])
            {
                atnTypeExclude = TRUE;
                break;
            }
        }
        
        if (atnTypeExclude)
        {
            hasExcludedAtnTypes = TRUE;
            continue; // Skip this attendee
        }
        
        // Block duplicate attendee from being entered
        if (existingAtnKeys[attendee.attnKey] != nil)
        {// MOB-8958 fix duplicate detection
            continue;
        }
        
        attendeeAdded = TRUE;
        [attendee supportFields:self.atnColumns];
        [self.attendees addObject:attendee];
    }
    if (hasExcludedAtnTypes)
    {
        // Alert user certain attendees from group are not added
        UIAlertView *alert = [[MobileAlertView alloc] 
                              initWithTitle:nil
                              message:[Localizer getLocalizedText:@"ATTENDEES_EXCLUDED_FOR_TYPE"]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                              otherButtonTitles:nil];
        [alert show];

    }
    if (attendeeAdded)
        [self.attendeeActionDelegate attendeeListChanged];    
}

#pragma mark -
#pragma mark End of Lifecycle

@end
