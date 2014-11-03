//
//  FormVCBaseInline.m
//  ConcurMobile
//
//  Created by Shifan Wu on 10/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "FormVCBaseInline.h"
#import "Config.h"
#import "BoolEditCell.h"
#import "EditInlineCell.h"

#define kPickerAnimationDuration    0.40   // duration for the animation to slide the date picker into view
#define kDatePickerTag              99     // view tag identifiying the date picker view

#define kTitleKey       @"title"   // key for obtaining the data source item's title
#define kDateKey        @"date"    // key for obtaining the data source item's date value
#define kImageKey       @"Undefined.png"
#define kValueKey       @"input value"

// This is the default action when user press save
// Setting the default allow actionAfterSave to be used to flag saving operation in progress
int kActionAfterSaveDft = 101101;
int kAlertViewVerifyCD = 101791;
int kAlertViewConfirmSaveUponBackBtn = 101792;
int kAlertViewMissingReqFld = 101794;
int kAlertViewReceiptUploadMsg = 101795;

static NSString *kDateCellID = @"dateCell";     // the cells with the start or end date
static NSString *kDatePickerID = @"datePicker"; // the cell containing the date picker
static NSString *kDrillCellID = @"drillCell";
static NSString *kTextFieldID = @"textField";     // the cells with the start or end date
static NSString *kBooleanID = @"boolean"; // the cell containing the date picker
static NSString *kTextViewID = @"textView";
static NSString *formFieldCellID = @"formFieldCell";
static NSString *kOtherCell = @"otherCell";     // the remaining cells at the end

#pragma mark -

@interface FormVCBaseInline ()
{
    BOOL isRevertBack;
}
@end

@implementation FormVCBaseInline

@synthesize sections, sectionFieldsMap, sectionDataMap, ccopyDownSrcChanged;
@synthesize allFields;
@synthesize actionAfterSave;
@dynamic isDirty;
@synthesize isFromAlert;


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self.formDataSource setFormTableView:self.tableView];
    
    /*
    createExpDS = [[CreateExpenseDS alloc] init];
    [createExpDS setSeedData:self.tableList withDelegate:self];
    */
    self.dateFormatter = [[NSDateFormatter alloc] init];
    [self.dateFormatter setDateStyle:NSDateFormatterShortStyle];    // show short-style date format
    [self.dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    
    // obtain the picker view cell's height, works because the cell was pre-defined in our storyboard
    UITableViewCell *pickerViewCellToCheck = [self.tableView dequeueReusableCellWithIdentifier:kDatePickerID];
    self.pickerCellRowHeight = pickerViewCellToCheck.frame.size.height;
    
    // if the local changes while in the background, we need to be notified so we can update the date
    // format in the table view cells
    //
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(localeChanged:)
                                                 name:NSCurrentLocaleDidChangeNotification
                                               object:nil];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:NSCurrentLocaleDidChangeNotification
                                                  object:nil];
}

#pragma mark - Locale

/*! Responds to region format or locale changes.
 */
- (void)localeChanged:(NSNotification *)notif
{
    // the user changed the locale (region format) in Settings, so we are notified here to
    // update the date format in the table view cells
    //
    [self.tableView reloadData];
}


#pragma mark - Utilities

/*! Returns the major version of iOS, (i.e. for iOS 6.1.3 it returns 6)
 */
NSUInteger DeviceSystemMajorVersion()
{
    static NSUInteger _deviceSystemMajorVersion = -1;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        
        _deviceSystemMajorVersion = [[[[[UIDevice currentDevice] systemVersion] componentsSeparatedByString:@"."] objectAtIndex:0] intValue];
    });
    
    return _deviceSystemMajorVersion;
}

#define EMBEDDED_DATE_PICKER (DeviceSystemMajorVersion() >= 7)

/*! Determines if the given indexPath has a cell below it with a UIDatePicker.
 
 @param indexPath The indexPath to check if its cell has a UIDatePicker below it.
 */
- (BOOL)hasPickerForIndexPath:(NSIndexPath *)indexPath
{
    BOOL hasDatePicker = NO;
    
    NSInteger targetedRow = indexPath.row;
    targetedRow++;
    
    UITableViewCell *checkDatePickerCell = [self.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:targetedRow inSection:0]];
    UIDatePicker *checkDatePicker = (UIDatePicker *)[checkDatePickerCell viewWithTag:kDatePickerTag];
    
    hasDatePicker = (checkDatePicker != nil);
    return hasDatePicker;
}

/*! Updates the UIDatePicker's value to match with the date of the cell above it.
 */
- (void)updateDatePicker
{
    if (self.datePickerIndexPath != nil)
    {
        UITableViewCell *associatedDatePickerCell = [self.tableView cellForRowAtIndexPath:self.datePickerIndexPath];
        
        UIDatePicker *targetedDatePicker = (UIDatePicker *)[associatedDatePickerCell viewWithTag:kDatePickerTag];
        if (targetedDatePicker != nil)
        {
            // we found a UIDatePicker in this cell, so update it's date value
            //
            NSDictionary *itemData = self.dataArray[self.datePickerIndexPath.row - 1];
            [targetedDatePicker setDate:[itemData valueForKey:kDateKey] animated:NO];
        }
    }
}

/*! Determines if the UITableViewController has a UIDatePicker in any of its cells.
 */
- (BOOL)hasInlineDatePicker
{
    return (self.datePickerIndexPath != nil);
}

/*! Determines if the given indexPath points to a cell that contains the UIDatePicker.
 
 @param indexPath The indexPath to check if it represents a cell with the UIDatePicker.
 */
- (BOOL)indexPathHasPicker:(NSIndexPath *)indexPath
{
    return ([self hasInlineDatePicker] && self.datePickerIndexPath.row == indexPath.row);
}

/*! Determines if the given indexPath points to a cell that contains the date.
 
 @param indexPath The indexPath to check if it represents date cell.
 */
- (BOOL)formFieldHasDate:(FormFieldData *)fld
{
    BOOL hasDate = NO;
        
    if ([fld.dataType isEqualToString:@"TIMESTAMP" ])
    {
        hasDate = YES;
    }
    
    return hasDate;
}

/*! Determines if the given indexPath points to a cell that can be drill in
 
 @param indexPath The indexPath to check if it represents a drill in cell.
 */
- (BOOL)formFieldHasDrillCell:(FormFieldData *)fld
{
    BOOL hasDrillCell = NO;
    if ([fld.iD isEqualToString:@"Attendees"] || [self canUseListEditor:fld] || [fld.dataType isEqualToString:@"EXPTYPE"] || [Config isGov]) {
        hasDrillCell = YES;
    }
    
    return hasDrillCell;
}

/*! Determines if the given indexPath points to a cell that contains a textfield.
 
 @param indexPath The indexPath to check if it represents a cell with textfield.
 */
- (BOOL)formFieldHasTextField:(FormFieldData *)fld
{
    BOOL hasTextField = NO;
    
    if ([self canUseTextFieldEditor:fld]) {
        hasTextField = YES;
    }
    
    return hasTextField;
}

/*! Determines if the given indexPath points to a cell that contains a boolean switch.
 
 @param indexPath The indexPath to check if it represents cell with boolean switch.
 */
- (BOOL)formFieldHasBoolean:(FormFieldData *)fld
{
    BOOL hasBoolean = NO;
    
    if ([self canUseBoolCell:fld]) {
        hasBoolean = YES;
    }

    return hasBoolean;
}

/*! Determines if the given indexPath points to a cell that contains a textView.
 
 @param indexPath The indexPath to check if it represents a cell with textView.
 */
- (BOOL)formFieldHasTextView:(FormFieldData *)fld
{
    BOOL hasTextView = NO;
    
    if ([fld.ctrlType isEqualToString:@"textarea"]) {
        hasTextView = YES;
    }
    
    return hasTextView;
}

#pragma mark - FormFieldData Utilities methods

-(FormFieldData*)findFieldWithIndexPath:(NSIndexPath*) indexPath
{
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	NSString* sectionKey = (section >= [sections count])? nil : sections[section];
	NSArray* fields = (sectionKey == nil)? nil : sectionFieldsMap[sectionKey];
	if (fields == nil)
		return nil;
	
	FormFieldData* field = (FormFieldData*)fields[row];
    return field;
}

-(FormFieldData*) findEditingField:(NSString*) fldString
{
	for (FormFieldData* fld in self.allFields)
	{
		if ([fld.iD isEqualToString:fldString])
			return fld;
	}
	
	return nil;
}

-(NSIndexPath *)getFieldCellPosition:(FormFieldData*) field
{
	for (int ix = 0; ix < [sections count]; ix++)
	{
		NSString* sectionKey = sections[ix];
		NSArray* fields = (sectionKey == nil)? nil : sectionFieldsMap[sectionKey];
		if (fields != nil)
		{
			for (int jx = 0; jx < [fields count]; jx++)
			{
				FormFieldData* ff = (FormFieldData*)fields[jx];
				// MOB-14843 - check both id and label since a form can have fields with same labels
				if (ff != nil && [ff.iD isEqualToString:field.iD] && [ff.label isEqualToString:field.label])
				{
					NSUInteger _path[2] = {ix, jx};
					__autoreleasing NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
					return _indexPath;
				}
			}
		}
	}
	return nil;
}

-(BOOL) canUseListEditor:(FormFieldData*)field
{
    if (![field isEditable])
		return NO;
    
	return (([field.ctrlType isEqualToString:@"picklist"] && ![field.dataType isEqualToString:@"BOOLEANCHAR"])
			|| [field.ctrlType isEqualToString:@"list_edit"]
			||[field.ctrlType isEqualToString:@"combo"]
			|| [field.dataType isEqualToString:@"MLIST"]
			|| [field.dataType isEqualToString:@"LIST"]
            //			||[field.ctrlType isEqualToString:@"checkbox"]
			||[field.dataType isEqualToString:@"CURRENCY"]
			||[field.dataType isEqualToString:@"LOCATION"]
			);
}

-(BOOL) canUseTextFieldEditor:(FormFieldData*)field
{
	return ([field.dataType isEqualToString:@"VARCHAR"] ||
			[field needsSecureEntry] || [field.dataType isEqualToString:@"TEXT"]||
			[field.dataType isEqualToString:@"MONEY"]||
			[field.dataType isEqualToString:@"CHAR"]||
			[field.dataType isEqualToString:@"INTEGER"]||
			[field.dataType isEqualToString:@"NUMERIC"])
    && ([field.ctrlType isEqualToString:@"edit"]);
}

-(BOOL) canUseBoolCell:(FormFieldData*) field
{
    if (![field isEditable])
		return NO;
    
    return [field.dataType isEqualToString:@"BOOLEANCHAR"];
}

#pragma mark - Field utilities

-(void) initFields
{
	[self.helper initFields];
	self.ccopyDownSrcChanged = [[NSMutableDictionary alloc] init];
}

-(void) refreshField:(FormFieldData*) field
{
    if ([self canUseBoolCell:field])
        return;
    
    if (isRevertBack)
        return;
    
    NSIndexPath* ixPath = [self getFieldCellPosition:field];
    if (ixPath != nil)
    {
        NSArray* ixPaths = @[ixPath];
        [self.tableView reloadRowsAtIndexPaths:ixPaths withRowAnimation:UITableViewRowAnimationRight];
    }
}

-(void) refreshFields:(NSArray*) fields
{
    NSMutableArray* ixPaths = [[NSMutableArray alloc] init];
    for (FormFieldData* fld in fields)
    {
        if ([self canUseBoolCell:fld])
            continue;
        
    	NSIndexPath* ixPath = [self getFieldCellPosition:fld];
        // MOB-15104 - make sure there are no dupe indexpaths otherwise this will crash the app.
        if (ixPath != nil && ![ixPaths containsObject:ixPath])
    	{
   			[ixPaths addObject:ixPath];
    	}
    }
    [self.tableView reloadRowsAtIndexPaths:ixPaths withRowAnimation:UITableViewRowAnimationRight];
}

-(BOOL) validateFields:(BOOL*)missingReqFlds;
{
	return [self.helper validateFields:missingReqFlds];
}

#pragma mark - Save utilities

-(void)clearActionAfterSave
{
	self.actionAfterSave = 0;
}

-(BOOL)isSaveConfirmDialog:(int) tag
{
	return tag == kAlertViewConfirmSaveUponBackBtn;
}

-(BOOL)isReceiptUploadAlertTag:(int) tag
{
	return tag == kAlertViewReceiptUploadMsg;
}

-(void) saveForm:(BOOL) copyDownToChildForms
{
}

-(void) confirmToSave:(int) callerId
{
    //To avoid any saving action for AppDemo target, I did:
    // 1.Search for keyword "save" in en_string.plist
    // 2.Look for alert that reminds user to do a save action (e.g. back button on QEForm.VC)
    // 3.hide the alert, so user can't save it
    
    // Alert to set required fields before save
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:nil
                          message:[Localizer getLocalizedText:@"RPT_SAVE_CONFIRM_MSG"]
                          delegate:self
                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
                          otherButtonTitles:[Localizer getLocalizedText:@"Yes"],[Localizer getLocalizedText:@"No"], nil];
    
    alert.tag = callerId;
    
    [alert show];
}

-(NSString*) getCDMsg
{
	return [Localizer getLocalizedText:@"COPY_DOWN_RPT_MSG"];
}

-(NSString*) getCDChangedFieldNames // CD - Copy Down
{
	NSMutableString *buffer = [[NSMutableString alloc] init];
	NSEnumerator *enumerator = [ccopyDownSrcChanged keyEnumerator];
	id key;
	while ((key = [enumerator nextObject]))
	{
		if ([buffer length] > 0)
			[buffer appendString:@","];
		[buffer appendString:(NSString*)key];
	}
	
	__autoreleasing NSString* result = [NSString stringWithString:buffer];
	return result;
}

-(BOOL) hasCopyDownChildren
{
    return YES;
}

-(void) checkCopyDownForSave
{
	if (self.ccopyDownSrcChanged == nil || [self.ccopyDownSrcChanged count] ==0
        || ![self hasCopyDownChildren])
		[self saveForm:NO];
	else {
		NSString *fields = [self getCDChangedFieldNames];
		NSString *msg = [NSString stringWithFormat:[self getCDMsg], fields, fields];
		// Alert to set required fields before save
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:nil
							  message:msg
							  delegate:self
							  cancelButtonTitle:[Localizer getLocalizedText:@"No"]
							  otherButtonTitles:[Localizer getLocalizedText:@"Yes"], nil];
		
		alert.tag = kAlertViewVerifyCD;
		
		[alert show];
	}
}

-(BOOL) canSaveOffline
{
    return NO;
}

-(void)actionSaveImpl
{
	if(![self canSaveOffline] && ![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle: [Localizer getLocalizedText:@"Cannot Save Changes"]
							  message: [Localizer getLocalizedText:@"OFFLINE_MSG"]
							  delegate:nil
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
        [self clearActionAfterSave];
		return;
	}
	
    /*	if (self.firstResponder != nil)
     {
     [self.firstResponder resignFirstResponder];
     self.firstResponder = nil;
     }
     */
    if (self.actionAfterSave == 0)
        self.actionAfterSave = kActionAfterSaveDft;
    
	NSIndexPath * ixSel = [self.tableView indexPathForSelectedRow];
	if (ixSel != nil)
		[self.tableView deselectRowAtIndexPath:ixSel animated:YES];//ss
	
	BOOL missingRequiredFields = NO;
	BOOL isFormValid = [self validateFields:&missingRequiredFields];
	[self.tableView reloadData];
	if (!isFormValid)
	{
		// Alert to set required fields before save
        [self performSelector:@selector(showAlertAboutInvalidFields) withObject:self afterDelay:0.5f];
		[self clearActionAfterSave];
		return;
	} else if (missingRequiredFields){
        [self performSelector:@selector(showAlertAboutRequiredFields) withObject:self afterDelay:0.5f];
		return;
	}
    
	[self checkCopyDownForSave];
}

-(void)executeActionAfterSave
{
	self.isDirty = NO;
    
	if(self.actionAfterSave == kAlertViewConfirmSaveUponBackBtn)
	{
		[self actionBack:nil];
	}
	
	self.actionAfterSave = 0;
}

-(void) actionBack:(id)sender
{
	//NSLog(@"Back btn pressed");
	if ([self isDirty])
	{
        // Block back button if saving is in progress
        if (self.actionAfterSave == 0)
            [self confirmToSave:kAlertViewConfirmSaveUponBackBtn];
	}
	else
	{
		if ([UIDevice isPad])
		{
			if ([self.navigationController.viewControllers count]>1)
				[self.navigationController popViewControllerAnimated:YES];
			else {
				[self dismissModalViewControllerAnimated:YES];
			}
		}
		else
			[self.navigationController popViewControllerAnimated:YES];
	}
}

-(BOOL) canEdit
{
    return self.allFields != nil && [self.allFields count]>0;
}

-(void)updateSaveBtn
{
    
    if(![ExSystem connectedToNetwork] || ![self canEdit])
    {
        self.navigationItem.rightBarButtonItem = nil;
        return;
    }
    
    UIBarButtonItem *btnSave = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemSave target:self action:@selector(actionSave:)];
    
    if(self.isDirty)
        [btnSave setEnabled:YES];
    else
        [btnSave setEnabled:NO];
	[self.navigationItem setRightBarButtonItem:btnSave animated:NO];
}

-(BOOL) isDirty
{
	return self.isDirty;
}

-(void) setIsDirty:(BOOL) val
{
	if (val != self.isDirty)
	{
		self.isDirty = val;
		[self updateSaveBtn];
	}
}

#pragma mark - FieldEditDelegate methods

- (void)fieldCanceled:(FormFieldData *)field
{
    
}

-(void) fieldUpdated:(FormFieldData*) field
{
	self.isDirty = TRUE;
	[self clearActionAfterSave]; // MOB-8532 Restart action count
    
	if ([@"Y" isEqualToString:field.isCopyDownSourceForOtherForms])
	{
		ccopyDownSrcChanged[field.label] = field.label;
	}
    
	if ([field.iD isEqualToString:@"CtryCode"])
	{
		FormFieldData* ctrySubCodeFld = [self findEditingField:@"CtrySubCode"];
		if (ctrySubCodeFld != nil)
		{
			ctrySubCodeFld.parLiKey = field.liKey;
		}
	}
	
    BOOL reqMissing = FALSE;
    [self.helper validateField:field missing:&reqMissing];
    
    if (field.validationErrMsg == nil && [@"MONEY" isEqualToString:field.dataType] && [@"edit" isEqualToString:field.ctrlType])
    {
        field.fieldValue = [FormatUtils formatStyledMoneyWithoutCrn:field.fieldValue crnCode:[field getCrnCodeForMoneyFldType]];
    }
    
    // Iterate allFields, update the parLiKey for child fields
	if (field.hierKey > 0 && self.allFields != nil)
	{
		// Parse hier-node-key-chain for MRU
		BOOL isMru = [field.liKey length]>0 && [field.liKey characterAtIndex:[field.liKey length]-1] == '-';
        
		// MOB-4340 - remove keys generated by trailing "-"
		if (isMru)
			field.liKey = [field.liKey substringToIndex:([field.liKey length]-1)];
        
		NSArray* nodeKeys = nil;
		NSArray* nodeTexts = nil;
		NSArray* nodeCodes = nil;
		if (field.liKey != nil)
		{
			nodeKeys = [field.liKey componentsSeparatedByString:@"-"];
			nodeTexts = [field.fieldValue componentsSeparatedByString:@"\t"];
			nodeCodes = [field.liCode componentsSeparatedByString:@"\t"];
		}
		NSMutableArray *fieldsToUpdate = [[NSMutableArray alloc] initWithObjects:field, nil];
		int levelsUpdated = nodeKeys == nil? 1 : [nodeKeys count];
		
		// if isMru, then we need to make sure the first level is in the form.
		BOOL canProceed = YES;
		if (isMru)
		{
			canProceed = NO;
			for (FormFieldData* fld in self.allFields)
			{
				if (fld.hierKey == field.hierKey && fld.hierLevel == 1)
					canProceed = YES;
			}
			if (canProceed == NO)
			{
				field.liKey = nil;
				field.liCode = nil;
				field.fieldValue = nil;
			}
		}
        
		if (canProceed)
		{
            for (FormFieldData* fld in self.allFields)
            {
                if (fld.hierKey == field.hierKey)
                {
                    int ix = fld.hierLevel - (isMru?1:field.hierLevel);
                    // Update child and current field, if not MRU;
                    // otherwise, update all in the chain
                    if ((ix > 0 && ix <= levelsUpdated) || (ix == 0 && isMru))
                    {
                        NSString* curLiKey = isMru? nil:field.liKey;
                        if (nodeKeys != nil && ix > 0)
                            curLiKey = nodeKeys[ix-1];
                        fld.parLiKey = curLiKey;	// TODO - test with all hierarchies
                        if (ix < levelsUpdated)
                        {
                            fld.liKey = nodeKeys[ix];
                            fld.liCode = nodeCodes[ix];
                            fld.fieldValue = nodeTexts[ix];
                        }
                        else {
                            fld.liKey = nil;
                            fld.liCode = nil;
                            fld.fieldValue = nil;
                        }
                        
                        if (fld != field)
                            [fieldsToUpdate addObject:fld];
                    }
                    else if (ix > levelsUpdated)
                    {
                        fld.parLiKey = nil;
                        fld.liKey = nil;
                        fld.liCode = nil;
                        fld.fieldValue = nil;
                        if (fld != field)
                            [fieldsToUpdate addObject:fld];
                    }
                }
            }
		}
		// update this field and all child fields
		[self refreshFields:fieldsToUpdate];
		
		if (!canProceed)
		{
			// Notify user that we cannot select MRU items.
			UIAlertView *alert = [[MobileAlertView alloc]
								  initWithTitle:nil
								  message:[Localizer getLocalizedText:@"MLIST_MRU_NOT_SUPPORT"]
								  delegate:nil
								  cancelButtonTitle:nil
								  otherButtonTitles:[Localizer getLocalizedText:@"LABEL_OK_BTN"], nil];
			[alert show];
		}
	}
	else {
		// update single field
		[self refreshField:field];
	}
    
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView{
    return [self.formDataSource numberOfFields];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
//    if ([self doesrowinsection:section hasInlineEditingengaled]) {
//        return 2;
//    }
    return 1;// or 2 when whe show the inline editing
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    if (indexPath.row == 0) {
        UITableViewCell *cell =[self.formDataSource tableView:tableView fieldForIndex:indexPath.section];
        if ([cell isKindOfClass:@"TextClass"]) {
            
        }
        return cell;
        
    } else {
        return nil; // return the inline picker
    }
}

- (void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    if (indexPath.row == 0) {
//        self.formDataSource tableView:tableView didSelectFieldAtIndex:indexPath.section];
//        [self toogleinlineEditing]
        
    }
    if (indexPath.row ==1) {
//        set new date and hide inline..
    }
    
}
//#pragma mark - UITableViewDataSource

//- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
//{
//    return [sections count];
//}
//
//- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
//{
//    NSArray* sectionData = [self getSectionData:section];
//    
//	if (sectionData == nil)
//        return 0;
//
//    if ([self hasInlineDatePicker])
//    {
//        // we have a date picker, so allow for it in the number of rows in this section
//        NSInteger numRows = [sectionData count];
//        return ++numRows;
//    }
//    else
//        return [sectionData count];
//}
//
//- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    FormFieldData* field = [self findFieldWithIndexPath:indexPath];
//	if (field == nil)
//		return nil;
//	   
//    UITableViewCell *cell = nil;
//    
//    //testing
//    
//    //testing
//    
//    
//    if ([self indexPathHasPicker:indexPath])
//    {
//        // the indexPath is the one containing the inline date picker
//        NSString *cellID = kDatePickerID;     // the current/opened date picker cell
//        cell = [tableView dequeueReusableCellWithIdentifier:cellID];
//        return cell;
//    }
//    else
//    {
//        cell = [self makeCell:tableView owner:self field:field];
//        return cell;
//    }
//}
//
////- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
////{
////    return nil;
////}
//
//-(UITableViewCell*)makeCell:(UITableView*)tableView owner:(id)owner field:(FormFieldData*) field
//{
//    NSString *cellID = kOtherCell;
//    
//    if ([self formFieldHasDate:field])
//    {
//        // the indexPath is one that contains the date information
//        cellID = kDateCellID;       // the start/end date cells
//        UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellID];
//        cell.textLabel.text = [@"Date" localize];
//        cell.detailTextLabel.text = [self.dateFormatter stringFromDate:[NSDate date]];
//        
//        return cell;
//    }
//    else if ([self formFieldHasDrillCell:field])
//    {
//        // the indexPath is one that contains the date information
//        cellID = kDrillCellID;       // the start/end date cells
//        FormFieldCell *cell = (FormFieldCell *)[tableView dequeueReusableCellWithIdentifier: cellID];
//        
//        if (cell == nil)
//        {
//            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:cellID owner:owner options:nil];
//            for (id oneObject in nib)
//            {
//                if ([oneObject isKindOfClass:[FormFieldCell class]])
//                {
//                    cell = (FormFieldCell *)oneObject;
//                    break;
//                }
//            }
//        }
//        [cell resetCellContent:field];
//        return cell;
//        
//    }
//    else if ([self formFieldHasTextField:field])
//    {
//        // the indexPath is one that contains the date information
//        cellID = kTextFieldID;       // the start/end date cells
//        EditInlineCell *cell = (EditInlineCell*)[tableView dequeueReusableCellWithIdentifier:cellID];
//        if (cell == nil) {
//            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:cellID owner:self options:nil];
//            for (id oneObject in nib)
//            {
//                if ([oneObject isKindOfClass:[BoolEditCell class]])
//                {
//                    cell = (EditInlineCell *)oneObject;
//                    break;
//                }
//            }
//        }
//        
//        [cell resetCellContent:field];
//    }
//    else if ([self formFieldHasBoolean:field])
//    {
//        // the indexPath is one that contains the date information
//        cellID = kBooleanID;       // the start/end date cells
//        BoolEditCell *cell = (BoolEditCell *)[tableView dequeueReusableCellWithIdentifier:cellID];
//        if (cell == nil)
//        {
//            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:cellID owner:self options:nil];
//            for (id oneObject in nib)
//            {
//                if ([oneObject isKindOfClass:[BoolEditCell class]])
//                {
//                    cell = (BoolEditCell *)oneObject;
//                    break;
//                }
//            }
//        }
//        
//        [cell setSeedData:[field.liKey isEqualToString:@"Y"] delegate:self context:field label:field.label];
//        
//        return cell;
//    }
//    else if ([self formFieldHasTextView:field])
//    {
//        // the indexPath is one that contains the date information
//        cellID = kTextViewID;       // the start/end date cells
//        //TODO: COMPLETE TEXT AREA CELL
//    }
//    
//    return nil;
//}
//
//#pragma mark - UITableViewDelegate
//
//- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    UITableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
//    if (cell.reuseIdentifier == kDateCellID)
//    {
//        if (EMBEDDED_DATE_PICKER)
//            [self displayInlineDatePickerForRowAtIndexPath:indexPath];
//        else
//            [self displayExternalDatePickerForRowAtIndexPath:indexPath];
//    }
//    else
//    {
//        [tableView deselectRowAtIndexPath:indexPath animated:YES];
//    }
//}
//
//- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    return ([self indexPathHasPicker:indexPath] ? self.pickerCellRowHeight : self.tableView.rowHeight);
//}

#pragma mark Section support Methods

-(NSMutableArray*) getSectionData:(NSInteger) section
{
    if (sections == nil || [sections count]<=section)
        return nil;
    
    NSString *key = sections[section];
    
    if (sectionFieldsMap != nil && [sectionFieldsMap count] > 0)
    {
        NSMutableArray *sectionData = (self.sectionFieldsMap)[key];
        if (sectionData != nil)
            return sectionData;
    }
    
    if (sectionDataMap != nil)
    {
        NSMutableArray *sectionData = (self.sectionDataMap)[key];
        return sectionData;
    }
    return nil;
}

#pragma mark Date Picker Display methods

/*! Adds or removes a UIDatePicker cell below the given indexPath.
 
 @param indexPath The indexPath to reveal the UIDatePicker.
 */
- (void)toggleDatePickerForSelectedIndexPath:(NSIndexPath *)indexPath
{
    [self.tableView beginUpdates];
    
    NSArray *indexPaths = @[[NSIndexPath indexPathForRow:indexPath.row + 1 inSection:0]];
    
    // check if 'indexPath' has an attached date picker below it
    if ([self hasPickerForIndexPath:indexPath])
    {
        // found a picker below it, so remove it
        [self.tableView deleteRowsAtIndexPaths:indexPaths
                              withRowAnimation:UITableViewRowAnimationFade];
    }
    else
    {
        // didn't find a picker below it, so we should insert it
        [self.tableView insertRowsAtIndexPaths:indexPaths
                              withRowAnimation:UITableViewRowAnimationFade];
    }
    
    [self.tableView endUpdates];
}

/*! Reveals the date picker inline for the given indexPath, called by "didSelectRowAtIndexPath".
 
 @param indexPath The indexPath to reveal the UIDatePicker.
 */
- (void)displayInlineDatePickerForRowAtIndexPath:(NSIndexPath *)indexPath
{
    // display the date picker inline with the table content
    [self.tableView beginUpdates];
    
    BOOL before = NO;   // indicates if the date picker is below "indexPath", help us determine which row to reveal
    if ([self hasInlineDatePicker])
    {
        before = self.datePickerIndexPath.row < indexPath.row;
    }
    
    BOOL sameCellClicked = (self.datePickerIndexPath.row - 1 == indexPath.row);
    
    // remove any date picker cell if it exists
    if ([self hasInlineDatePicker])
    {
        [self.tableView deleteRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:self.datePickerIndexPath.row inSection:0]]
                              withRowAnimation:UITableViewRowAnimationFade];
        self.datePickerIndexPath = nil;
    }
    
    if (!sameCellClicked)
    {
        // hide the old date picker and display the new one
        NSInteger rowToReveal = (before ? indexPath.row - 1 : indexPath.row);
        NSIndexPath *indexPathToReveal = [NSIndexPath indexPathForRow:rowToReveal inSection:0];
        
        [self toggleDatePickerForSelectedIndexPath:indexPathToReveal];
        self.datePickerIndexPath = [NSIndexPath indexPathForRow:indexPathToReveal.row + 1 inSection:0];
    }
    
    // always deselect the row containing the start or end date
    [self.tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    [self.tableView endUpdates];
    
    // inform our date picker of the current date to match the current cell
    [self updateDatePicker];
}

/*! Reveals the UIDatePicker as an external slide-in view, iOS 6.1.x and earlier, called by "didSelectRowAtIndexPath".
 
 @param indexPath The indexPath used to display the UIDatePicker.
 */
- (void)displayExternalDatePickerForRowAtIndexPath:(NSIndexPath *)indexPath
{
    // first update the date picker's date value according to our model
    NSDictionary *itemData = self.dataArray[indexPath.row];
    [self.pickerView setDate:[itemData valueForKey:kDateKey] animated:YES];
    
    // the date picker might already be showing, so don't add it to our view
    if (self.pickerView.superview == nil)
    {
        CGRect startFrame = self.pickerView.frame;
        CGRect endFrame = self.pickerView.frame;
        
        // the start position is below the bottom of the visible frame
        startFrame.origin.y = self.view.frame.size.height;
        
        // the end position is slid up by the height of the view
        endFrame.origin.y = startFrame.origin.y - endFrame.size.height;
        
        self.pickerView.frame = startFrame;
        
        [self.view addSubview:self.pickerView];
        
        // animate the date picker into view
        [UIView animateWithDuration:kPickerAnimationDuration animations: ^{ self.pickerView.frame = endFrame; }
                         completion:^(BOOL finished) {
                             // add the "Done" button to the nav bar
                             self.navigationItem.rightBarButtonItem = self.doneButton;
                         }];
    }
}

#pragma mark - Actions

/*! User chose to change the date by changing the values inside the UIDatePicker.
 
 @param sender The sender for this action: UIDatePicker.
 */
- (IBAction)dateAction:(id)sender
{
    NSIndexPath *targetedCellIndexPath = nil;
    
    if ([self hasInlineDatePicker])
    {
        // inline date picker: update the cell's date "above" the date picker cell
        //
        targetedCellIndexPath = [NSIndexPath indexPathForRow:self.datePickerIndexPath.row - 1 inSection:0];
    }
    else
    {
        // external date picker: update the current "selected" cell's date
        targetedCellIndexPath = [self.tableView indexPathForSelectedRow];
    }
    
    UITableViewCell *cell = [self.tableView cellForRowAtIndexPath:targetedCellIndexPath];
    UIDatePicker *targetedDatePicker = sender;
    
    // update our data model
    NSMutableDictionary *itemData = self.dataArray[targetedCellIndexPath.row];
    [itemData setValue:targetedDatePicker.date forKey:kDateKey];
    
    // update the cell's date string
    cell.detailTextLabel.text = [self.dateFormatter stringFromDate:targetedDatePicker.date];
}


/*! User chose to finish using the UIDatePicker by pressing the "Done" button, (used only for non-inline date picker), iOS 6.1.x or earlier
 
 @param sender The sender for this action: The "Done" UIBarButtonItem
 */
- (IBAction)doneAction:(id)sender
{
    CGRect pickerFrame = self.pickerView.frame;
    pickerFrame.origin.y = self.view.frame.size.height;
    
    // animate the date picker out of view
    [UIView animateWithDuration:kPickerAnimationDuration animations: ^{ self.pickerView.frame = pickerFrame; }
                     completion:^(BOOL finished) {
                         [self.pickerView removeFromSuperview];
                     }];
    
    // remove the "Done" button in the navigation bar
	self.navigationItem.rightBarButtonItem = nil;
    
    // deselect the current table cell
	NSIndexPath *indexPath = [self.tableView indexPathForSelectedRow];
	[self.tableView deselectRowAtIndexPath:indexPath animated:YES];
}

#pragma mark - UIAlertView methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
	if(alertView.tag == kAlertViewVerifyCD)
	{
		if (buttonIndex == 1)
		{
			self.copyToChildForms = YES;
			[self saveForm:YES];
		}
		else
		{
			self.copyToChildForms = NO;
			[self saveForm:NO];
		}
	}
	else if (alertView.tag == kAlertViewMissingReqFld)
	{
		if (buttonIndex == 1)
		{
			[self checkCopyDownForSave];
		}
		else
		{
			[self clearActionAfterSave];
            //			[self.tableList reloadData];
		}
	}
	else if ([self isSaveConfirmDialog:alertView.tag])
	{
		self.isFromAlert = YES;
		if (buttonIndex == 1) // Yes
		{
			self.actionAfterSave = alertView.tag;
			[self actionSaveImpl];
		}
		else if (buttonIndex == 2) // No
		{
			self.actionAfterSave = alertView.tag;
			[self initFields];  // Revert changes back
            isRevertBack = TRUE;
			if (self.actionAfterSave == kActionAfterSaveDft)
			{
				[self.tableView reloadData];
			}
			
			[self executeActionAfterSave];
			self.isFromAlert = NO;
		}
		// Cancel
	}
    
}

@end
