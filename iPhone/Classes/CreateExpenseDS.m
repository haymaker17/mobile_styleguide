//
//  CreateExpenseDS.m
//  ConcurMobile
//
//  Created by Shifan Wu on 10/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CreateExpenseDS.h"
#import "MobileEntryManager.h"
#import "UploadableReceipt.h"
#import "MRUManager.h"


@implementation CreateExpenseDS

#define     kSectionCardAuth    @"CARD_AUTH"

-(id)init
{
    self = [super init];
    if (self)
    {
        [self.tableList registerNib:[UINib nibWithNibName:@"DateCell" bundle:nil] forCellReuseIdentifier:@"DateCell"];
        [self.tableList registerNib:[UINib nibWithNibName:@"AmountCell" bundle:nil] forCellReuseIdentifier:@"AmountCell"];
        [self.tableList registerNib:[UINib nibWithNibName:@"TextViewCell" bundle:nil] forCellReuseIdentifier:@"TextViewCell"];
//        [self.tableView registerNib:[UINib nibWithNibName:@"DateCell" bundle:nil] forCellReuseIdentifier:@"DateCell"];
    }
    return self;
}

- (void)setSeedData:(UITableView *)tbl withDelegate:(id<CreateExpenseDSDelegate>)del
{
    self.delegate = del;
    self.tableList = tbl;
    self.tableList.delegate = self;
    self.tableList.dataSource = self;
}

- (id)initWithEntryOrNil:(EntityMobileEntry*) mobileEntry withCloseButton:(BOOL)withCloseButton
{
    self = [super init];
    if (self)
    {
        [self.tableList registerNib:[UINib nibWithNibName:@"DateCell" bundle:nil] forCellReuseIdentifier:@"DateCell"];
        [self.tableList registerNib:[UINib nibWithNibName:@"AmountCell" bundle:nil] forCellReuseIdentifier:@"AmountCell"];
        [self.tableList registerNib:[UINib nibWithNibName:@"TextViewCell" bundle:nil] forCellReuseIdentifier:@"TextViewCell"];
        
        self.hasCloseButton = withCloseButton;
        
        if (mobileEntry != nil)
        {
            self.entry = mobileEntry;
            // MOB-13680 - Check if there is any key. All keys will be nil for offline/uploadqueue items
            if ([MobileEntryManager getKey:self.entry] == nil )
            {
                self.isInUploadQueue = true;
            }
            else
            {
                self.isInUploadQueue = false;
                self.smartExpenseManager2 = [[SmartExpenseManager2 alloc] initWithContext:self.managedObjectContext];
            }
        }
        else
        {
            self.entry = [[MobileEntryManager sharedInstance] makeNew];
            self.isInUploadQueue = false;
        }
        [self makeFieldsArray:nil];
        [self loadReceipt];
    }
    
    return self;
}

- (id)initWithEntryOrNil:(EntityMobileEntry*) mobileEntry
{
    return [self initWithEntryOrNil:mobileEntry withCloseButton:NO];
}

- (void)loadReceipt {
    
    if (receipt == nil)
    {
        receipt = [[Receipt alloc] init];
    }
    
    if (entry.localReceiptImageId != nil && entry.localReceiptImageId.length > 0) {
        // We have a local receipt, i.e. one that has not yet been uploaded to the server
        NSString *filePath = [UploadableReceipt filePathForLocalReceiptImageId:entry.localReceiptImageId];
        if (filePath != nil && filePath.length > 0)
        {
            // We found the file in which the local receipt image is stored
            // AJC - unused code 1 line below. please delete if unused by 2013-11-29
            //formVC.entry.receiptImage = [UIImage imageWithContentsOfFile:filePath];
            entry.hasReceipt = @"Y";
            self.updatedReceiptImageId = NO;
            
            receipt.receiptId = entry.receiptImageId;
            receipt.receiptImg = [UIImage imageWithContentsOfFile:filePath];
            //MOB-13161
            receipt.localReceiptId = entry.localReceiptImageId;
        }
    }
    else if (entry.receiptImageId != nil && entry.receiptImageId.length > 0)
    {
        // Hmmm. The mobile entry has not yet been saved to the server, yet we are referencing a receipt that is on the server.  This could have happened if the receipt was uploaded, but the mobile expense wasn't. (Upload cancelled or lost connectivity during upload)
        // TODO: figure out how to load this (and maybe check if it's already loaded)
        receipt.receiptId = entry.receiptImageId;
    }
}

#pragma mark - Make Fields Array
- (void)makeFieldsArray:(id)sender
{
    if (self.entry != nil && [MobileEntryManager isCardAuthorizationTransaction:self.entry])
        self.sections = [[NSMutableArray alloc] initWithObjects:@"0", kSectionCardAuth, @"1", nil];
    else
        self.sections = [[NSMutableArray alloc] initWithObjects:@"0", @"1", nil];
    
    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
    self.allFields = [[NSMutableArray alloc] initWithObjects: nil];
    
    // MIB : MOB-13462  - Re-order fields
    [allFields addObject:[self getExpenseTypeField]];
    [allFields addObject:[self getAmountField]];
    [allFields addObject:[self getCurrencyField]];
    [allFields addObject:[self getDateField]];
    [allFields addObject:[self getLocationField]];
    [allFields addObject:[self getVendorField]];
    [allFields addObject:[self getCommentField]];
    
    sectionFieldsMap[@"1"] = allFields;
}

- (FormFieldData *)getCommentField
{
    return [[FormFieldData alloc] initField:@"CommentEx" label:[Localizer getLocalizedText:@"Comment"] value:entry.comment ctrlType:@"textarea" dataType:@"VARCHAR"];
}

- (FormFieldData *)getLocationField
{
    FormFieldData *field = [[FormFieldData alloc] initField:@"LocName" label:[Localizer getLocalizedText:@"Location"] value:entry.locationName ctrlType:@"edit" dataType:@"LOCATION"];
    field.liKey = entry.locationName; // Prevent None gets checked
    
    //MOB-14662 - if we have no location, and this is a new entry then default to the MRU value
    if (self.entry.locationName == nil &&  ![MobileEntryManager isCardTransaction:entry] && self.entry.key == nil && self.entry.expKey == nil) {
        ListItem *lastUsedLocation = [[MRUManager sharedInstance] getLastUsedLocation];
        
        if (lastUsedLocation != nil) {
            field.fieldValue = lastUsedLocation.liName;
            field.liKey = [NSString stringWithFormat:@"%@", lastUsedLocation.liKey];
            field.liCode = lastUsedLocation.liCode;
        }
    }
    return field;
}

- (FormFieldData *)getVendorField
{
    FormFieldData *field = [[FormFieldData alloc] initField:@"VENDOR" label:[Localizer getLocalizedText:@"Vendor"] value:entry.vendorName ctrlType:@"edit" dataType:@"VARCHAR"];
    if (entry.cctKey != nil) {
        field.access = @"RO"; // MOB-6874
    }
    return field;
}

// assumes that currency has already been setup by calling getCurrencyField
- (FormFieldData *)getAmountField
{
    NSString *amountFieldValue = [NSNumberFormatter localizedStringFromNumber:entry.transactionAmount numberStyle:NSNumberFormatterDecimalStyle];
    
    FormFieldData *field = [[FormFieldData alloc] initField:@"TransactionAmount" label:[Localizer getLocalizedText:@"Amount"] value: amountFieldValue ctrlType:@"edit" dataType:@"MONEY"];
    
    if (entry.crnCode) {
        field.extraDisplayInfo = entry.crnCode;
    } else {
        field.extraDisplayInfo = [self getDefaultCrnCode];
    }
    
    field.fieldValue = [FormatUtils formatMoneyWithoutCrn:field.fieldValue crnCode:[field getCrnCodeForMoneyFldType]];
    if (entry.pctKey != nil || entry.cctKey != nil) {
        field.access = @"RO";
    }
    field.required = @"Y";
    
    // MOB-12779: Force the transaction field to be blank when the quick expense is newly created
    double dblVal = 0.0;
    NSString *amount = [NSString stringWithFormat:@"%@", entry.transactionAmount];
    if ([self isZeroAmount:amount doubleValue:&dblVal field:field] && [self isNewQuickExpense] && ![self isInUploadQueue] && !([field.access isEqualToString:@"RO"] || [field.access isEqualToString:@"HD"]) )
        field.fieldValue = @"";
    
    return field;
}

- (NSString *)getDefaultCrnCode
{
    NSLocale* locale = [NSLocale currentLocale];
    return [locale objectForKey:NSLocaleCurrencyCode];
}

- (FormFieldData *)getCurrencyField
{
    FormFieldData *field = [[FormFieldData alloc] initField:@"TransactionCurrencyName" label:[Localizer getLocalizedText:@"Currency"] value:entry.crnCode ctrlType:@"edit" dataType:@"CURRENCY"];
    // MIB : Minor fixes
    NSString *crnCode = entry.crnCode;
    
    if (![[entry crnCode] lengthIgnoreWhitespace]) {
        // default to USD
        crnCode = [ExSystem sharedInstance].sys.crnCode == nil ? @"USD" : [ExSystem sharedInstance].sys.crnCode;
        
        // check MRU
        ListItem *lastUsedCurrency = [[MRUManager sharedInstance] getLastUsedCurrency];
        if (lastUsedCurrency != nil) // if there is mru, use it
        {
            field.fieldValue = lastUsedCurrency.liName;
            field.liKey = lastUsedCurrency.liKey == nil ? nil : [NSString stringWithFormat:@"%d", [lastUsedCurrency.liKey intValue]];
            crnCode = lastUsedCurrency.liCode;
        }
    }
    field.liCode = crnCode;
    NSLocale *locale = [NSLocale currentLocale];
    NSString *currencyName = [locale displayNameForKey:NSLocaleCurrencyCode value:field.liCode];
    field.fieldValue = currencyName;
    if (entry.pctKey != nil || entry.cctKey != nil) {
        field.access = @"RO";
    }
    field.required = @"Y";
    return field;
}

- (FormFieldData *)getExpenseTypeField
{
    FormFieldData *field = [[FormFieldData alloc] initField:@"ExpKey" label:[Localizer getLocalizedText:@"Type"] value:entry.expName ctrlType:@"EXPTYPE" dataType:@"EXPTYPE"];
    if (entry.expKey == nil) {
        field.fieldValue = [Localizer getLocalizedText:@"Undefined"];
        field.liKey = @"UNDEF";
    } else {
        field.liKey = entry.expKey;
    }
    field.required = @"Y";
    return field;
}

// All OOP date is current date in local sense but GMT based, so to get today's date using [NSDate date],
// we have to convert it to string using local timezone first.
- (FormFieldData *)getDateField
{
    FormFieldData *field;
    
    NSDate *transactionDate = [entry transactionDate];
    if (transactionDate == nil || ![[transactionDate description] lengthIgnoreWhitespace]) {
        NSString *lastDate = [DateTimeFormatter formatDateForExpenseServer:[NSDate date]];
        field = [[FormFieldData alloc] initField:@"TransactionDate" label:[Localizer getLocalizedText:@"Date"] value:lastDate ctrlType:@"TIMESTAMP" dataType:@"TIMESTAMP"];
    } else {
        field = [[FormFieldData alloc] initField:@"TransactionDate" label:[Localizer getLocalizedText:@"Date"] value:[DateTimeFormatter getLocalDateAsString:entry.transactionDate] ctrlType:@"TIMESTAMP" dataType:@"TIMESTAMP"];
    }
    
    field.required = @"Y";
    if (entry.pctKey != nil || entry.cctKey != nil) {
        field.access = @"RO";
    }
    return field;
}

-(BOOL) isZeroAmount:(NSString*) fieldValue doubleValue:(double*) dblVal field:(FormFieldData*) fld
{
	NSScanner* scanner = [NSScanner scannerWithString:fieldValue];
    // AJC - unused code 1 line below. please delete if unused by 2013-11-29
    //	[scanner setLocale:[NSLocale currentLocale]];
	if ([scanner isAtEnd] == NO)
	{
        if (![scanner scanDouble:dblVal])
        {
            fld.validationErrMsg = [Localizer getLocalizedText:@"NUMERIC_VALIDATION_ERR_MSG"];
            return FALSE;
        }
        else{
			// Make sure no garbage character at the end or 0.0 value
			if (0.0 == *dblVal)
				return TRUE;
			
			if (![scanner isAtEnd] || *dblVal == HUGE_VAL)
			{
				if (*dblVal == HUGE_VAL)
					fld.validationErrMsg = [Localizer getLocalizedText:@"NUMERIC_TOO_BIG_ERR_MSG"];
				else
					fld.validationErrMsg = [Localizer getLocalizedText:@"NUMERIC_VALIDATION_ERR_MSG"];
				
				return FALSE;
			}
		}
	}
	return TRUE;
}

-(BOOL) isNewQuickExpense
{
    BOOL isNew = [MobileEntryManager getKey:self.entry] == nil;
    return isNew;
}

-(void) processFieldsToEntry:(id)sender
{
    for(FormFieldData *field in allFields)
    {
        if([field.iD isEqualToString:@"TransactionDate"])
        {
            // Convert back to GMT date
            entry.transactionDate =[DateTimeFormatter getNSDate:field.fieldValue Format:@"yyyy-MM-dd'T'HH:mm:ss"];
        }
        else if([field.iD isEqualToString:@"ExpKey"])
        {
            if(field.liKey != nil)
                entry.expKey = field.liKey;
            else
                entry.expKey = field.liCode;
            
            entry.expName = field.fieldValue;
        }
        else if([field.iD isEqualToString:@"TransactionAmount"])
        {
            entry.transactionAmount = [NSDecimalNumber decimalNumberWithString:[field getServerValue]];
        }
        else if([field.iD isEqualToString:@"TransactionCurrencyName"])
        {
            entry.crnCode = field.liCode;
        }
        else if([field.iD isEqualToString:@"VENDOR"])
        {
            entry.vendorName = field.fieldValue;
        }
        else if([field.iD isEqualToString:@"LocName"])
        {
            entry.locationName = field.fieldValue;
        }
        else if([field.iD isEqualToString:@"CommentEx"])
        {
            entry.comment = field.fieldValue;
        }
    }
}

#pragma mark - Table view data source
-(UIView*)makeTableHeaderView
{
    UIView *expenseItHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 40)];
    int xoffset = 15;
    if ([UIDevice isPad])
        xoffset = 30;
    UILabel *headerLbl = [[UILabel alloc] initWithFrame:CGRectMake(xoffset, 5, 300, 32)];
    [headerLbl setBackgroundColor:[UIColor clearColor]];
    [headerLbl setFont:[UIFont fontWithName:@"Helvetica neueu" size:14.0]];
    [headerLbl setLineBreakMode:UILineBreakModeWordWrap];
    [headerLbl setShadowColor:[UIColor whiteColor]];
    [headerLbl setTextColor:[UIColor colorWithRed:(69.0/255.0) green:(69.0/255.0) blue:(69.0/255.0) alpha:1.0f]];
    [headerLbl setTextAlignment:UITextAlignmentLeft];
    [headerLbl setNumberOfLines:1];
    
    //    [headerLbl setAutoresizingMask:UIViewAutoresizingFlexibleWidth];
    [headerLbl setText:[Localizer getLocalizedText:@"ExpenseIt Entry"]];
    [expenseItHeaderView addSubview:headerLbl];
    
    return expenseItHeaderView;
}

- (UIView*)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    if (self.entry.rcKey != nil && section == 0)
    {
        return [self makeTableHeaderView];
    }
    return nil;
}

-(CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    if (self.entry.rcKey != nil && section == 0)
        return [[self makeTableHeaderView] frame].size.height;
    return 0;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return [self.sections count];
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

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString *sectionName = [self.sections objectAtIndex:section];
    if ([sectionName isEqualToString:kSectionCardAuth]) {
        return 1;
    }
    
    if(section == 0) {
    	// 1 is the receipt select, 2 is the report select
        return 1;
        // TODO: uncomment this after the 9.7 branch.  This enables add quick expense to report!!
        //return 2;
    } else {
        NSArray* sectionData = [self getSectionData:section];
        
        if (sectionData == nil)
            return 0;
        
        return [sectionData count];
    }
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

-(UITableViewCell*)makeCell:(UITableView*)tableView owner:(id)owner field:(FormFieldData*) field
{
    NSString *CellIdentifier = @"DateCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    return cell;
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
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    FormFieldData* field = [self findFieldWithIndexPath:indexPath];
	if (field == nil)
		return nil;
	
	UITableViewCell *cell =  [self makeCell:tableView owner:self field:field];
    
	return cell;
}

-(CGFloat)getMessageTextHeight:(NSString*) text withWidth:(CGFloat)width
{
    CGFloat height =  [FormatUtils getTextFieldHeight:width Text:text Font:[UIFont fontWithName:@"HelveticaNeue" size:14.0f]];
    
    if((height) < 36)
        height =  36;
    
    height = height + 4;
    
    // AJC - unused code 1 line below. please delete if unused by 2013-11-29
    //    if([ExSystem isLandscape])
    // AJC - unused code 1 line below. please delete if unused by 2013-11-29
    //        w = 420;
    return height;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *sectionName = [self.sections objectAtIndex:indexPath.section];
    if ([sectionName isEqualToString:kSectionCardAuth])
	{
		int exceptionW = 260;
		CGFloat height =  [self getMessageTextHeight:[@"CARD_AUTH_MSG" localize] withWidth:exceptionW];
        
		return height;
	}
    
    if(indexPath.section == 0)
        return 44;
    else
        return 59;
}

#pragma mark - Table view data source

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    [tableView deselectRowAtIndexPath:newIndexPath animated:YES];
//
//    if(newIndexPath.section == 0)
//    {
//        if (newIndexPath.row == 0) {
//            BOOL isOnline = [ExSystem connectedToNetwork];
//            BOOL isNewExpense = [self isNewQuickExpense];
//            BOOL isExpenseQueued = [self isQueuedQuickExpense];
//            
//            // MOB-14916: ExpenseIT: User is able to Detach receipt
//            // for 9.5 release the expenseIT entry receipts are read/view only.
//            BOOL allowReceiptEdits = (isOnline || isNewExpense || isExpenseQueued) && ( self.entry.rcKey == nil);
//            
//            BOOL willQueueExpense = (isExpenseQueued || (!isOnline && isNewExpense));
//            BOOL excludeReceiptStoreOption = willQueueExpense; // User not allowed to select from receipt store for queued expense
//            if (isExpenseQueued)
//            {
//                NSDictionary *pbag = @{@"Queued Receipt": @YES};
//                [Flurry logEvent:@"Offline: Viewed" withParameters:pbag];
//            }
//            
//            [self showReceiptViewerAndAllowEdits:allowReceiptEdits excludeReceiptStoreOption:excludeReceiptStoreOption];
//        } else {
//            // TEST expense to report code
//            SelectReportViewController *vc = [[SelectReportViewController alloc] init];
//            
//            vc.delegate = self;
//            [self.navigationController pushViewController:vc animated:YES];
//        }
//    }
//    else
//    {
//        [super tableView:tableView didSelectRowAtIndexPath:newIndexPath];
//        return;
//    }
//    
}
@end
