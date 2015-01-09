//
//  FormViewControllerBase.m
//  ConcurMobile
//
//  Created by yiwen on 4/19/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "FormViewControllerBase.h"
#import "FormFieldCell.h"
#import "ExpenseTypesViewController.h"
#import "LabelConstants.h"
#import "TextEditVC.h"
#import "ListFieldEditVC.h"
#import "DateEditVC.h"
#import "DateTimeFormatter.h"
#import "BoolEditCell.h"
#import "TextAreaEditVC.h"
#import "CommentListVC.h"
#import "FormatUtils.h"
#import "Config.h"

@interface FormViewControllerBase()
{
    BOOL isRevertBack;
}
-(void) showAlertAboutInvalidFields;
-(void) showAlertAboutRequiredFields;
@end

@implementation FormViewControllerBase

@synthesize tableList, sections, sectionDataMap, sectionFieldsMap, allFields, formKey, copyToChildForms, googleHandler;
@synthesize isFromAlert, ccopyDownSrcChanged, actionAfterSave, helper;
@dynamic isDirty;
@synthesize pickerPopOverVC;
@synthesize noSaveConfirmationUponExit;

// This is the default action when user press save
// Setting the default allow actionAfterSave to be used to flag saving operation in progress
int kActionAfterSaveDefault = 101101;
int kAlertViewVerifyCopyDown = 101791;
int kAlertViewConfirmSaveUponBack = 101792;
int kAlertViewMissingReqFlds = 101794;
int kAlertViewReceiptUploadMessage = 101795;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
        self.helper = [[EditFormHelper alloc] initWithEditForm:self];
    }
    return self;
}

- (void)dealloc
{
    googleHandler.delegate = nil;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle
// Let subclass override this one
- (void) refreshView
{
    if ([self isViewLoaded])
	{
		[tableList reloadData];
		[self setupToolbar];
	}
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];
// Use default back button
//    if (!self.noSaveConfirmationUponExit)
//        [self.navigationItem setHidesBackButton:YES animated:NO];
    

    [self checkTitle];
    [self refreshView];
}

-(void) checkTitle
{
    if (self.title == nil) {
        self.title = [Localizer getViewTitle:[self getViewIDKey]];
    }

    // MOB-15061
    // on iOS 6 and lower, the back button does not appear if the pushing screen lacks a title.  Stupid design.
    // make sure there is always a back button of sorts.  Really should never come to this, but users are reporting that this occurs.
    if (self.title.length <= 0) {
        self.title = @"    ";
    }
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self setupToolbar]; // Now that we have view controller stack, need to fix back button here.
    // Cover the back button (cannot do this in viewWillAppear -- too soon)
    // UIControl *backCover is the best way at the time to change the default behavior for BACK button
    // MOB-17108 only show saving alert when required to do so
    if ( backCover == nil && !self.noSaveConfirmationUponExit)
    {
        const int kButtonA2RW_Max = 100;
        const int kButtonA2RW_Min = 80; //40;
        
        NSUInteger viewIndex = [self.navigationController.viewControllers count];
        
        if(viewIndex<2){
            viewIndex = 0;      // viewIndex cannot be negative
        }
        else{
            viewIndex = viewIndex - 2;
        }
        
        UIViewController *parentView = self.navigationController.viewControllers[viewIndex];

        CGSize s = [parentView.title sizeWithFont:[UIFont boldSystemFontOfSize:22]];
        int size = (s.width > kButtonA2RW_Max) ? kButtonA2RW_Max : ((s.width < kButtonA2RW_Min)?kButtonA2RW_Min:s.width);
        size += 10;
        backCover = [[UIControl alloc] initWithFrame:CGRectMake( 0, 0, size, 44)];
        
        // MOB-17433: fixes to enable accessibility for UIAutomation
        [backCover setIsAccessibilityElement:YES];
        [backCover setAccessibilityIdentifier:@"backCover"];
        
// Uncomment these lines to see the coverage of back button
#if TARGET_IPHONE_SIMULATOR
//        // show the cover for testing
//        backCover.backgroundColor = [UIColor colorWithRed:1.0 green:0.0 blue:0.0 alpha:0.15];
#endif
        [backCover addTarget:self action:@selector(actionBack:) forControlEvents:UIControlEventTouchDown];
        UINavigationBar *navBar = self.navigationController.navigationBar;
        [navBar addSubview:backCover];
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
    [backCover removeFromSuperview];
    backCover = nil;
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.pickerPopOver = nil;
    self.pickerPopOverVC = nil;
    
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	if([UIDevice isPad])
	{
		if(pickerPopOver != nil)
        {
			[pickerPopOver dismissPopoverAnimated:YES];
            self.pickerPopOver = nil;
        }
		if(pickerPopOverVC != nil)
		{
			self.pickerPopOverVC = nil;
		}
		
	}
	
	[super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

#pragma mark -
#pragma mark Editing Methods
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

-(void)setupToolbar
{
    if([ExSystem connectedToNetwork])
    {
        if (![self canEdit])
        {
            // Hide home button; Do show the save button in edit mode
            if ([UIDevice isPad])
                self.navigationItem.rightBarButtonItem = nil;
        }else 
        {
            [self updateSaveBtn];
        }
    }

    //for iOS 5 and 6 support
//    if (![ExSystem is7Plus]){
//        [self setupFakeBackButton];
//    }
    
}

-(BOOL) isDirty
{
	return isDirty;
}

-(void) setIsDirty:(BOOL) val
{
	if (val != isDirty)
	{
		isDirty = val;
		[self updateSaveBtn];
	}
}

-(BOOL) canEdit
{
    return self.allFields != nil && [self.allFields count]>0;
}

-(BOOL) shouldAllowOfflineEditingwAtIndexPath:(NSIndexPath *)indexPath
{
    return NO;
}

-(void) attemptedToEditWhileOffline
{
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:[Localizer getLocalizedText:@"Offline"]
                          message:[Localizer getLocalizedText:@"Please wait until online to edit field"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                          otherButtonTitles:nil];
    [alert show];
}

-(void) initFields
{
    /*	FormFieldData* ctryCodeFld = nil;
     FormFieldData* ctrySubCodeFld = nil;
     
     // Convert number fields from en-US format to local format for editing
     for (FormFieldData* fld in self.allFields)
     {
     if ([@"MONEY" isEqualToString:fld.dataType] && [@"edit" isEqualToString:fld.ctrlType])
     {
     fld.fieldValue = [FormatUtils formatMoneyWithoutCrn:fld.fieldValue crnCode:[self getCurrencyCodeForField:fld]];
     }
     else if ([@"NUMERIC" isEqualToString:fld.dataType] && [@"edit" isEqualToString:fld.ctrlType])
     {
     fld.fieldValue = [FormatUtils formatDouble:fld.fieldValue];
     }
     else if ([@"INTEGER" isEqualToString:fld.dataType] && [@"edit" isEqualToString:fld.ctrlType] && ![fld.iD isEqualToString:@"CurrencyName"])
     {
     fld.fieldValue = [FormatUtils formatInteger:fld.fieldValue];
     }
     else if ([@"TIMESTAMP" isEqualToString:fld.dataType] && [fld.fieldValue isEqualToString:@"0001-01-01T00:00:00"]) 
     {
     fld.fieldValue = nil;
     }
     
     if ([fld.iD isEqualToString:@"CtryCode"])
     ctryCodeFld = fld;
     else if ([fld.iD isEqualToString:@"CtrySubCode"])
     ctrySubCodeFld = fld;
     }
     
     if (ctryCodeFld != nil && ctrySubCodeFld != nil)
     {
     ctrySubCodeFld.parLiKey = ctryCodeFld.liKey;
     }
     */
	[self.helper initFields];
	self.ccopyDownSrcChanged = [[NSMutableDictionary alloc] init];
}

-(NSDictionary*) getComments
{
    return nil;
}

-(FormFieldData*) findEditingField:(NSString*) fId
{
	for (FormFieldData* fld in self.allFields)
	{
		if ([fld.iD isEqualToString:fId])
			return fld;
	}
	
	return nil;
}

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
        [self.tableList reloadRowsAtIndexPaths:ixPaths withRowAnimation:UITableViewRowAnimationRight];
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
    [self.tableList reloadRowsAtIndexPaths:ixPaths withRowAnimation:UITableViewRowAnimationRight];
}


-(void) showField:(FormFieldData*)fld afterField:(FormFieldData*) srcFld
{
	if (fld == nil || srcFld == nil) return;
    
	if ([fld.access isEqualToString:@"HD"])
		fld.access = @"RO";
    
	NSIndexPath * fldPos = [self getFieldCellPosition:fld];
	if (fldPos != nil)
		return;
	
	NSIndexPath * srcFldPos = [self getFieldCellPosition:srcFld];
	if (srcFldPos == nil)
		return;
	
	NSUInteger section = [srcFldPos section];
    NSUInteger row = [srcFldPos row];
	
	NSString* sectionKey = (section >= [sections count])? nil : sections[section];
	NSMutableArray* fields = (sectionKey == nil)? nil : sectionFieldsMap[sectionKey];
	
	[fields insertObject:fld atIndex:row+1];
}

-(void) hideField:(FormFieldData*)fld
{
	if (fld == nil) return;
	
	NSIndexPath * fldPos = [self getFieldCellPosition:fld];
	if (fldPos == nil)
		return;
	fld.access = @"HD";
	NSUInteger section = [fldPos section];
    NSUInteger row = [fldPos row];
    
	NSString* sectionKey = (section >= [sections count])? nil : sections[section];
	NSMutableArray* fields = (sectionKey == nil)? nil : sectionFieldsMap[sectionKey];
    
	[fields removeObjectAtIndex:row];
}

-(void) hideFieldWithId:(NSString*) fId
{
	FormFieldData* fld = [self findEditingField:fId];
	[self hideField:fld];
}

-(double) getDoubleFromField:(NSString*) fldId
{
	FormFieldData* fld = [self findEditingField:fldId];
	NSString* strVal = [fld getServerValue];
	
	if (strVal != nil)
		return [strVal doubleValue];
	return 0.0;
}

-(NSDecimalNumber*) getDecimalNumberFromField:(NSString*) fldId
{
	FormFieldData* fld = [self findEditingField:fldId];
	NSString* strVal = [fld getServerValue];
	
	if (strVal != nil)
		return [FormatUtils decimalNumberFromServerString:strVal];
	return [NSDecimalNumber zero];
}

#pragma mark -
#pragma mark FieldEditDelegate Methods

-(void) fieldCanceled:(FormFieldData*) field
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
            // LAZY server code.  Why not just put these values in separate nodes?  It's dangerous to design xml's like this.
            /*
             Example MRU entry:
             
             <ListItem>
                <Code>RnD-DEV</Code>
                <IsMru>L</IsMru>
                <Key>1397-1402</Key>
                <Text>Research and Development	Development</Text>
             </ListItem>
             
             Is really the following two list items:
             
             At the first level:
             <ListItem>
                <Code>RnD</Code>
                <Key>1397</Key>
                <Text>Research and Development</Text>
             </ListItem>

             At the second level:
             <ListItem>
                <Code>DEV</Code>
                <Key>1402</Key>
                <Text>Development</Text>
             </ListItem>

             */

            // keys and codes are split with -
			nodeKeys = [field.liKey componentsSeparatedByString:@"-"];
			nodeCodes = [field.liCode componentsSeparatedByString:@"-"];

            // text is split with \t
            nodeTexts = [field.fieldValue componentsSeparatedByString:@"\t"];
		}
		NSMutableArray *fieldsToUpdate = [[NSMutableArray alloc] initWithObjects:field, nil];
		NSUInteger levelsUpdated = nodeKeys == nil? 1 : [nodeKeys count];
		
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

// Merge existing changes to the set of fields passed in.  Return an array of fields for display.
-(NSMutableArray*) mergeFields:(NSDictionary*) fields withKeys:(NSArray*) keys // Form data
{
	__autoreleasing NSMutableArray *detailsData = [[NSMutableArray alloc] initWithObjects:nil];
	
	if (fields != nil) 
	{
        BOOL clearExpTypeHier = NO;
        FormFieldData* newExpTypeLiKeyFld = fields[@"ExpTypeLiKey"];
        // MOB-11852 Call out expTypeLiKey field, if it has been changed
        FormFieldData *localExpTypeLiKeyFld = [self findEditingField:@"ExpTypeLiKey"];
        if (newExpTypeLiKeyFld != nil && localExpTypeLiKeyFld!= nil && newExpTypeLiKeyFld.liKey != nil &&
            ![newExpTypeLiKeyFld.liKey isEqualToString:localExpTypeLiKeyFld.liKey])
        {
            clearExpTypeHier = YES;
        }
		for (NSString* key in keys)
		{
			FormFieldData *ffd = fields[key];
			FormFieldData *updatedFfd = [self findEditingField:ffd.iD];
			if (updatedFfd != nil)
			{
				NSString *updatedFieldValue = updatedFfd.fieldValue;
				NSString *curFieldValue = ffd.fieldValue;
                BOOL listChanged = (ffd.listKey != nil || updatedFfd.listKey != nil) && ![ffd.listKey isEqualToString:updatedFfd.listKey];
                BOOL descendantOfExpTypeLiKey = NO; // MOB-11852
                if (clearExpTypeHier && ffd.hierKey > 0 && ffd != newExpTypeLiKeyFld && ffd.hierKey ==newExpTypeLiKeyFld.hierKey)
                {
                    descendantOfExpTypeLiKey = YES;
                }
				if (!descendantOfExpTypeLiKey && !listChanged && !(updatedFieldValue == nil  && curFieldValue == nil) &&
					!(updatedFieldValue != nil && [updatedFieldValue isEqualToString:curFieldValue])
                    // Do not modify RO/HD values MOB-6944
                    && (ffd.access == nil || [ffd.access isEqualToString:@"RW"]) 
                    && ((ffd.defaultValue == nil && updatedFfd.defaultValue==nil) ||[ffd.defaultValue isEqualToString:updatedFfd.defaultValue])
                    && ((ffd.cpDownFormType == nil && updatedFfd.cpDownFormType==nil) ||[ffd.cpDownFormType isEqualToString:updatedFfd.cpDownFormType])
                    && ((ffd.cpDownSource == nil && updatedFfd.cpDownSource==nil) ||[ffd.cpDownSource isEqualToString:updatedFfd.cpDownSource])
                    )
				{
					ffd.fieldValue = [updatedFfd getServerValue];
					ffd.liKey = updatedFfd.liKey;
					ffd.liCode = updatedFfd.liCode;
				}
			}
			
			if (ffd != nil && (ffd.access == nil || ![ffd.access isEqualToString:@"HD"]))
			{
				//[detailLabelData addObject:ffd.label];
				[detailsData addObject:ffd];
			}
		}
	}
	
	return detailsData;
}



#pragma mark -
#pragma mark - UITableViewDataSource
-(UITableViewCell*)makeCell:(UITableView*)tableView owner:(id)owner field:(FormFieldData*) fld
{
    if ([self canUseBoolCell:fld])
    {
        BoolEditCell *cell = (BoolEditCell *)[tableView dequeueReusableCellWithIdentifier:@"BoolEditCell"];
        if (cell == nil)  
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"BoolEditCell" owner:owner options:nil];
            for (id oneObject in nib)
            {
                if ([oneObject isKindOfClass:[BoolEditCell class]])
                {
                    cell = (BoolEditCell *)oneObject;
                    break;
                }
            }
        }
        
        [cell setSeedData:[fld.liKey isEqualToString:@"Y"] delegate:self context:fld label:fld.label];

        return cell;
    }
    else
    {
        FormFieldCell *cell = (FormFieldCell *)[tableView dequeueReusableCellWithIdentifier: @"FormFieldCell"];
        
        if (cell == nil)  
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"FormFieldCell" owner:owner options:nil];
            for (id oneObject in nib)
            {
                if ([oneObject isKindOfClass:[FormFieldCell class]])
                {
                    cell = (FormFieldCell *)oneObject;
                    break;
                }
            }
        }
        
        // MOB-11918: for connected list, the field should be read only when the parent field (first level list) is hidden
        FormFieldData* parFld = [self findEditingField:fld.parFieldId];
        if ([fld.dataType isEqualToString:@"MLIST"] && fld.parFieldId != nil && fld.parLiKey == nil && [parFld.access isEqualToString:@"HD"]){
            fld.access = @"RO";
        }

        [cell resetCellContent:fld];
        return cell;
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
//	if (![self canEdit])
//		return nil;
    
    FormFieldData* field = [self findFieldWithIndexPath:indexPath];
	if (field == nil)
		return nil;
	
	UITableViewCell *cell =  [self makeCell:tableView owner:self field:field];
    
	return cell;
	
}	

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [sections count];
}

- (NSString *)tableView:(UITableView *)tblView titleForHeaderInSection:(NSInteger)section
{
	return nil;
}

// Customize the number of rows in the table view.
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section 
{
    NSArray* sectionData = [self getSectionData:section];
    
	if (sectionData == nil)
        return 0;
	
    return [sectionData count];
}

-(BOOL) canUseBoolCell:(FormFieldData*) field
{
    if (![field isEditable])
		return NO;

    return [field.dataType isEqualToString:@"BOOLEANCHAR"];
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

#pragma mark - UITableViewDelegate
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    FormFieldData* field = [self findFieldWithIndexPath:indexPath];
	if (field.iD != nil  && [field.iD isEqualToString:@"Comment"]) // Do not localize!
    {
        [self showCommentsEditor:field];
        return;
    }
    
    // Call canEdit BEFORE calling shouldAllowOfflineEditing to avoid telling the user to wait until they're online if they can't edit the field at all.
	if (![self canEdit])
		return;
    
    if (![ExSystem connectedToNetwork] && ![self shouldAllowOfflineEditingwAtIndexPath:indexPath])
    {
        [self attemptedToEditWhileOffline];
        return;
    }
    
	if (field == nil)
		return;

	if (field.access != nil && [field.access isEqualToString:@"RO"]){
        [self.tableList deselectRowAtIndexPath:indexPath animated:NO];
		return;
    }
//	else if (([field.iD isEqualToString:@"FromLocation"] || [field.iD isEqualToString:@"ToLocation"]) && [ExSystem sharedInstance].isSingleUser)
//    {
//        //    //FromLocation
//        //    //ToLocation
//        //    //BusinessDistance
//        //    for(FormFieldData *fld in fields)
//        
//    }
    else if (field.iD != nil  && [field.iD isEqualToString:@"Attendees"]) // Do not localize!  This is an id, it is NOT text shown to the user!
	{
		[self showAttendeeEditor];
	}
	else if ([field.dataType isEqualToString:@"TIMESTAMP"])
	{
		if ([UIDevice isPad])
		{
			[self pickerDateTapped:self IndexPath:indexPath];
		}else 
			[self showDateEditor:field]; // Go to date screen
	}
	else if ([self canUseListEditor:field])
	{
		// Go to list screen
		[self showListEditor:field];
	}
	else if ([field.dataType isEqualToString:@"EXPTYPE"])
	{
		[self showExpenseTypeEditor:field];
	}
    else if ([field.iD isEqualToString:@"expense.tran_description"] && [Config isGov])
    {
        [self showExpenseTypeEditor:field];
    }
	else if ([field.ctrlType isEqualToString:@"textarea"])
	{
		[self showTextAreaEditor:field];
	}
    else if ([self canUseTextFieldEditor:field])
    {
        [self showTextFieldEditor:field];
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 60;
}

#pragma mark -
#pragma mark Editing subviews Methods
-(void)showCommentsEditor:(FormFieldData*) field
{
    if (![self canEdit])
        field.access = @"RO";
	CommentListVC *vc = [[CommentListVC alloc] initWithNibName:@"EditFormView" bundle:nil];
	[vc setSeedData:[self getComments] field:field delegate:self];

    [self checkTitle];
	[self.navigationController pushViewController:vc animated:YES];
}

// Override this method
-(void) showAttendeeEditor
{
}


-(void) showTextAreaEditor:(FormFieldData*)field
{
	TextAreaEditVC *vc = [[TextAreaEditVC alloc] initWithNibName:@"TextAreaEditView" bundle:nil];
	
	vc.field = field;
	vc.delegate = self;

    [self checkTitle];
	[self.navigationController pushViewController:vc animated:YES];
}

// Default to nothing, implement in EntryVC to supply polKey and rpt
-(void)showExpenseTypeEditor:(FormFieldData*) field
{
}


-(void)showDateEditor:(FormFieldData*) field
{
	DateEditVC *dvc = [[DateEditVC alloc] initWithNibName:@"DateEditVC" bundle:nil];
	dvc.context = field;
    
    // MOB-19705 Date picker problem.
    // Fix the time zone problem of the date picker. There is no directly utility function we can use. Try to write a new one.
    dvc.date = [CCDateUtilities formatDateStringWithoutTimeZoneToNSDate:field.fieldValue];
    
    dvc.viewTitle = [field getFullLabel];
    // Show errMsg as tip?
	dvc.delegate = self;

    [self checkTitle];
	[self.navigationController pushViewController:dvc animated:YES];
}

-(NSArray*) getExcludeKeysForListEditor:(FormFieldData*) field
{
    return nil;
}

-(void)prefetchForListEditor:(ListFieldEditVC*) lvc
{
    FormFieldData* field = lvc.field;
    
    if (!([field.ctrlType isEqualToString:@"checkbox"] || [field.dataType isEqualToString:@"BOOLEANCHAR"] || [field.iD isEqualToString:@"CarKey"]))
	{
		// Prefetch MRU data
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: field, @"FIELD", @"Y", @"MRU", nil];
        
        bool shouldUseCacheOnly = [self shouldUseCacheOnlyForListEditor:lvc];
        NSString *shouldUseCacheOnlyStr = (shouldUseCacheOnly ? @"YES" : @"NO");
        
		[[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:shouldUseCacheOnlyStr ParameterBag:pBag SkipCache:!shouldUseCacheOnly RespondTo:lvc];
	}
}

-(BOOL) shouldUseCacheOnlyForListEditor:(ListFieldEditVC*)lvc
{
    return NO;
}

-(void)showListEditor:(FormFieldData*) field
{
	// Check if we can edit, if this is a connected list field
	if (field.parFieldId != nil && field.parLiKey == nil)
	{
		FormFieldData* parFld = [self findEditingField:field.parFieldId];
		if (field.parFtCode != nil && [field.parFtCode isEqualToString:field.ftCode]
			&& (parFld.access == nil || [parFld.access isEqualToString:@"RW"]))
		{
			// alert user that they need to select value for parent field first
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:nil
								  message:[NSString stringWithFormat:[Localizer getLocalizedText:@"FILL_IN_PARENT_MLIST_FIELD"], parFld.label]
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
		}
		else {
			// alert user that they need to select value for parent field first
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:nil
								  message:[Localizer getLocalizedText:@"MLIST_FIELD_EDIT_NOT_SUPPORT"]
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
		}
		return;
	}
	
	ListFieldEditVC *lvc = nil;
    if ([field.ctrlType isEqualToString:@"combo"]) 
    {
        lvc = [[ListFieldEditVC alloc] initWithNibName:@"ComboFieldEditVC" bundle:nil];
        
    }else
    {
    	lvc = [[ListFieldEditVC alloc] initWithNibName:@"ListFieldEditVC" bundle:nil];
    }
    [lvc setSeedData:field delegate:self keysToExclude:[self getExcludeKeysForListEditor:field]];
    
    // MOB-16901 - for car mileage location fields disable auto correct.
    if ([field.iD rangeOfString:@"location" options:NSCaseInsensitiveSearch].location != NSNotFound || [field.dataType rangeOfString:@"location" options:NSCaseInsensitiveSearch].location != NSNotFound) {
        lvc.disableAutoCorrectinSearch = YES;
    }

    
	if([UIDevice isPad])
		lvc.modalPresentationStyle = UIModalPresentationFormSheet;

    [self checkTitle];
	[self.navigationController pushViewController:lvc animated:YES];
    //TODO: Fix this in right way. pushviewcontroller is an async call so prefecthcforlisteditor call is not guaranteed to close the loading view.
    //MOB-15053 : Temporary hack to ensure that the LVC view is loaded, other wise the view will be stuck in loading view.
    //We are seeing this issue in iOS 7
    double delayInSeconds = 0.05;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        [self prefetchForListEditor:lvc];
    });

	
    
}

-(void)showTextFieldEditor:(FormFieldData*) field
{
    TextEditVC *vc = [[TextEditVC alloc] initWithNibName:@"TextEditVC" bundle:nil];
    BOOL isNumeric = [field requiresNumericInput];
    NSString* val = field.fieldValue;

    if ([@"MONEY" isEqualToString:field.dataType] || ([@"NUMERIC" isEqualToString:field.dataType] && [@"edit" isEqualToString:field.ctrlType]))
    {
        double dblVal = 0.0;
        if ([self.helper validateDouble:val doubleValue:&dblVal] && dblVal == 0.0)
        {
            val = @"";
        }
    }
    else if ([@"INTEGER" isEqualToString:field.dataType] && [@"edit" isEqualToString:field.ctrlType] && ![@"CurrencyName" isEqualToString:field.iD])
    {
        int intVal = 0;
        if ([self.helper validateInteger:val integerValue:&intVal] && intVal == 0)
        {
            val = @"";
        }
    }
    
    [vc setSeedData:val context:field
        delegate:self
        tip:field.tip 
        title:[field getFullLabel]
        prompt:nil
        isNumeric:isNumeric
        isPassword:[field needsSecureEntry]
        err:field.validationErrMsg];

    // MOB-16901 - for car mileage location fields disable auto correct.
    if ([field.iD rangeOfString:@"location" options:NSCaseInsensitiveSearch].location != NSNotFound) {
        vc.disableAutoCorrect = YES;
    }

    [self checkTitle];
    [self.navigationController pushViewController:vc animated:YES];
    
}

#pragma mark - Google Stuff
-(void) handleGoogleLocation:(NSMutableDictionary *)dict didFail:(BOOL)didFail
{
    if(!didFail && dict[@"distanceValue"] != nil)
    {
        for(FormFieldData *field in allFields)
        {
            if ([field.iD isEqualToString:@"BusinessDistance"])
            {
                NSString *distance = dict[@"distanceText"];
                distance = [distance stringByReplacingOccurrencesOfString:@" mi" withString:@""];
                distance = [distance stringByReplacingOccurrencesOfString:@"," withString:@""];
                int iDistance = [distance intValue];
                field.fieldValue = [NSString stringWithFormat:@"%d", iDistance]; 
                field.label = [@"Distance calculated by Google" localize];
                
                NSLog(@"Distance details: %@, %@, %@", dict[@"start_address"], dict[@"end_address"], dict[@"distanceText"]);
                [self fieldUpdated:field];
//                break;
            }
            else if ([field.iD isEqualToString:@"FromLocation"])
            { 
                NSString *locResolved = [@"From Location" localize];
                NSString *googleAddress = dict[@"start_address"];
                googleAddress = [googleAddress stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
                locResolved = [NSString stringWithFormat:@"%@: %@", locResolved, googleAddress];
                field.label = locResolved;
                [self fieldUpdated:field];
                //                break;
            }
            else if ([field.iD isEqualToString:@"ToLocation"])
            { 
                NSString *locResolved = [@"To Location" localize];
                NSString *googleAddress = dict[@"end_address"];
                googleAddress = [googleAddress stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
                locResolved = [NSString stringWithFormat:@"%@: %@", locResolved, googleAddress];
                field.label = locResolved;
                [self fieldUpdated:field];
                //                break;
            }
        }
    }
    else
    {//blow the value away 
        for(FormFieldData *field in allFields)
        {
            if ([field.iD isEqualToString:@"BusinessDistance"])
            {
                field.fieldValue = nil;
                field.label = [@"Please enter in your distance" localize];
                [self fieldUpdated:field];
                
//                break;
            }
            else if ([field.iD isEqualToString:@"FromLocation"])
            { 
                NSString *locResolved = [@"From Location" localize];
                field.label = locResolved;
                [self fieldUpdated:field];
                //                break;
            }
            else if ([field.iD isEqualToString:@"ToLocation"])
            { 
                NSString *locResolved = [@"To Location" localize];
                field.label = locResolved;
                [self fieldUpdated:field];
                //                break;
            }
        }
    }
}

// it seems this method is not being used anymore...
-(void) doGoogleMessage
{
    NSString *fromLocation = nil;
    NSString *toLocation = nil;
    for(FormFieldData *field in allFields)
    {
        if ([field.iD isEqualToString:@"FromLocation"])
            fromLocation = field.fieldValue;
        else if ([field.iD isEqualToString:@"ToLocation"])
            toLocation = field.fieldValue;
    }
    
    if(fromLocation != nil && toLocation != nil)
    {
        self.googleHandler = [[GoogleRouteFinderHandler alloc] init];
        googleHandler.delegate = self;
        [googleHandler makeDirectionRequest:fromLocation toLocation:toLocation];
    }
}



#pragma mark -
#pragma mark Editing delegate Methods
-(void) textUpdated:(NSObject*) context withValue:(NSString*) value
{
    FormFieldData* fld = (FormFieldData*) context;
    fld.fieldValue = value;
    [self fieldUpdated:fld];
    
}

-(void) dateSelected:(NSObject*) context withValue:(NSDate*) date
{
    FormFieldData* fld = (FormFieldData*) context;
	fld.fieldValue = [CCDateUtilities formatDateToYearMonthDateTimeZoneMidNight:date];
    [self fieldUpdated:fld];    
}

-(void) boolUpdated:(NSObject*) context withValue:(BOOL) val
{
    FormFieldData* fld = (FormFieldData*) context;
    fld.liKey = val? @"Y":@"N";
    [self fieldUpdated:fld];    
}

#pragma mark -
#pragma mark PopOver Methods
- (void)pickerDateTapped:(id)sender IndexPath:(NSIndexPath *)indexPath
{
    if(pickerPopOver != nil)
    {
		[pickerPopOver dismissPopoverAnimated:YES];
        self.pickerPopOver = nil;
    }
	if(pickerPopOverVC != nil)
		self.pickerPopOverVC = nil;
	
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	NSString* sectionKey = (section >= [sections count])? nil : sections[section];
	NSArray* fields = (sectionKey == nil)? nil : sectionFieldsMap[sectionKey];
	if (fields == nil)
		return;
	FormFieldData* field = (FormFieldData*)fields[row];
    
    // it seems the dt is only used for travel
    NSDate *dt =  [DateTimeFormatter getNSDate:field.fieldValue Format:@"yyyy-MM-dd'T'HH:mm:ss" TimeZone:[NSTimeZone localTimeZone]];
    
	self.pickerPopOverVC = [[DateTimePopoverVC alloc] initWithNibName:@"DateTimePopoverVC" bundle:nil];
	pickerPopOverVC.isDate = YES;
	pickerPopOverVC.delegate = self;
	pickerPopOverVC.indexPath = indexPath;
	
	self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:pickerPopOverVC];               
    
	[pickerPopOverVC initDate:dt];
	
	CGRect cellRect = [tableList rectForRowAtIndexPath:indexPath];
	CGRect myRect = [self.view convertRect:cellRect fromView:tableList];
	
    [self.pickerPopOver presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionLeft animated:YES]; 
}

- (void)cancelPicker
{
	if(pickerPopOver != nil)
		[pickerPopOver dismissPopoverAnimated:YES];
    self.pickerPopOver = nil;
}

- (void)pickedDate:(NSDate *)dateSelected
{
	NSInteger section = [pickerPopOverVC.indexPath section];
	NSInteger row = [pickerPopOverVC.indexPath row];
	
	NSString* sectionKey = (section >= [sections count])? nil : sections[section];
	NSArray* fields = (sectionKey == nil)? nil : sectionFieldsMap[sectionKey];
	if (fields == nil)
		return;
	FormFieldData* field = (FormFieldData*)fields[row];
    field.fieldValue = [CCDateUtilities formatDateToYearMonthDateTimeZoneMidNight:dateSelected];
    
	NSUInteger _path[2] = {section, row};
	NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
	NSArray *_indexPaths = @[_indexPath];
	[tableList reloadRowsAtIndexPaths:_indexPaths withRowAnimation:NO];
	
	[self fieldUpdated:field];
}

- (void)donePicker:(NSDate *)dateSelected
{
}

- (void)pickedItem:(NSInteger)row
{
}

#pragma mark -
#pragma mark Save Methods
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


-(void) saveForm:(BOOL) copyDownToChildForms
{
}


-(BOOL) validateFields:(BOOL*)missingReqFlds;
{
	return [self.helper validateFields:missingReqFlds];
}

-(BOOL) validateAttendees
{
	return TRUE;	
    // Yiwen - If Attendees field is required, then no attendee is not valid.
}

-(NSString*) getCDMsg
{
	return [Localizer getLocalizedText:@"COPY_DOWN_RPT_MSG"];
}

-(NSString*) getFormFieldsInvalidMsg
{
	return [Localizer getLocalizedText:@"FORM_FIELDS_INVALID"];
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
	if (self.ccopyDownSrcChanged == nil || [self.ccopyDownSrcChanged count] ==0 || ![self hasCopyDownChildren])
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
		
		alert.tag = kAlertViewVerifyCopyDown;
		
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
        self.actionAfterSave = kActionAfterSaveDefault;
    
	NSIndexPath * ixSel = [self.tableList indexPathForSelectedRow];
	if (ixSel != nil)
		[self.tableList deselectRowAtIndexPath:ixSel animated:YES];//ss
	
	BOOL missingRequiredFields = NO;
	BOOL isFormValid = [self validateFields:&missingRequiredFields];
	[self.tableList reloadData];
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

-(void) showAlertAboutInvalidFields
{
    // Alert to set required fields before save
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:nil
                          message:[self getFormFieldsInvalidMsg]
                          delegate:nil
                          cancelButtonTitle:nil
                          otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
    [alert show];
}

-(void) showAlertAboutRequiredFields
{
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:nil
                          message:[Localizer getLocalizedText:@"WARN_MISSING_REQ_FLD"]
                          delegate:self
                          cancelButtonTitle:[Localizer getLocalizedText:@"No"]
                          otherButtonTitles:[Localizer getLocalizedText:@"Yes"], nil];
    alert.tag = kAlertViewMissingReqFlds;
    [alert show];
}

// Subclass needs to override saveButtonPressed to perform save.
-(void)actionSave:(id)sender
{
    // Do not allow duplicate Save requests
	if (![self isDirty] || self.actionAfterSave != 0 || !self.canEdit)
		return;
    
	[self actionSaveImpl];
    
}


-(void) actionBack:(id)sender
{
	if ([self isDirty])
	{
        // Block back button if saving is in progress
        if (self.actionAfterSave == 0)
            [self confirmToSave:kAlertViewConfirmSaveUponBack];
	}
	else
	{
		if ([UIDevice isPad])
		{
			if ([self.navigationController.viewControllers count]>1)
				[self.navigationController popViewControllerAnimated:YES];
			else {
				[self dismissViewControllerAnimated:YES completion:nil];
			}
		}
		else
			[self.navigationController popViewControllerAnimated:YES];
	}
}

-(void)executeActionAfterSave
{
	self.isDirty = NO;
    
	if(self.actionAfterSave == kAlertViewConfirmSaveUponBack)
	{
		[self actionBack:nil];
	}
	
	self.actionAfterSave = 0;
}

// Please call this API after save action fails in editing view
-(void)clearActionAfterSave
{
	self.actionAfterSave = 0;
}

-(BOOL)isSaveConfirmDialog:(NSInteger) tag
{
	return tag == kAlertViewConfirmSaveUponBack;
}

-(BOOL)isReceiptUploadAlertTag:(NSInteger) tag
{
	return tag == kAlertViewReceiptUploadMessage;
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if(alertView.tag == kAlertViewVerifyCopyDown)
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
	else if (alertView.tag == kAlertViewMissingReqFlds)
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
	else if ([self isSaveConfirmDialog:(int)alertView.tag])
	{
		self.isFromAlert = YES;
		if (buttonIndex == 1) // Yes
		{
			self.actionAfterSave = (int)alertView.tag;
			[self actionSaveImpl];
		}
		else if (buttonIndex == 2) // No
		{
			self.actionAfterSave = (int)alertView.tag;
			[self initFields];  // Revert changes back
            isRevertBack = TRUE;
			if (self.actionAfterSave == kActionAfterSaveDefault)
			{
				[self.tableList reloadData];
			}
			
			[self executeActionAfterSave];
			self.isFromAlert = NO;
		}
		// Cancel
	}

}

#pragma mark -
#pragma mark BarButtonItem Utility methods

+(UIBarButtonItem*) makeNavButton:(NSString*)strKey enabled:(BOOL)state target:(id)tgt action:(NSString*) sel
{
	NSString *text4Btn = [Localizer getLocalizedText:strKey];
    
	UIFont* sysFont13B = [UIFont boldSystemFontOfSize:13]; 
	CGSize s = [text4Btn sizeWithFont:sysFont13B];
	
	const int kButtonA2RW_Max = 80;
	const int kButtonA2RW_Min = 40;
	const int kButtonA2RH = 30;
	int size = (s.width > kButtonA2RW_Max) ? kButtonA2RW_Max : ((s.width < kButtonA2RW_Min)?kButtonA2RW_Min:s.width);
	size += 10;

    if (!state)
    {
        return [ExSystem makeColoredButton:@"BLUE_INACTIVE" W:size H:kButtonA2RH Text:(NSString *)text4Btn SelectorString:nil MobileVC:tgt];
    }
    else
    {
        return [ExSystem makeColoredButton:@"BLUE" W:size H:kButtonA2RH Text:(NSString *)text4Btn SelectorString:sel MobileVC:tgt];
        
    }
}

#pragma mark Section support Methods
-(BOOL) isFieldsSection:(NSInteger) section
{
    if (sections == nil || [sections count]<=section)
        return NO;
    
    if (sectionFieldsMap != nil && [sectionFieldsMap count] > 0)
    {
        NSString *key = sections[section];
        NSMutableArray *sectionData = (self.sectionFieldsMap)[key];
        if (sectionData != nil)
            return YES;
    }
    return NO;
}

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

-(void)closeMe:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];
}


@end
